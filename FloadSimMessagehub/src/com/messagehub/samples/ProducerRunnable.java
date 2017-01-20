/**
 * Copyright 2015-2016 IBM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KINDither express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corp. 2015-2016
 */
package com.messagehub.samples;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class ProducerRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger(ProducerRunnable.class);

    private final KafkaProducer<String, String> kafkaProducer;
    private final String topic;
    private volatile boolean closing = false;

    public ProducerRunnable(Properties producerProperties, String topic) {
        this.topic = topic;

        // Create a Kafka producer with the provided client configuration
        kafkaProducer = new KafkaProducer<String, String>(producerProperties);
        
        try {
            // Checking for topic existence.
            // If the topic does not exist, the kafkaProducer will retry for about 60 secs
            // before throwing a TimeoutException
            // see configuration parameter 'metadata.fetch.timeout.ms'
            List<PartitionInfo> partitions = kafkaProducer.partitionsFor(topic);
            System.out.println(partitions.toString());
        } catch (TimeoutException kte) {
            System.out.println("Topic '" + topic + "' may not exist - application will terminate");
            kafkaProducer.close();
            throw new IllegalStateException("Topic '" + topic + "' may not exist - application will terminate", kte);
        }
    }

    @Override
    public void run() {
        // Simple counter for messages sent
        int producedMessages = 0;
        System.out.println(ProducerRunnable.class.toString() + " is starting.");

        try {
            while (!closing) {
                String key = "key";
                String message = "This is a test message #" + producedMessages;

                try {
                    // If a partition is not specified, the client will use the default partitioner to choose one.
                    ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic,key,message);
                    
                    // Send record asynchronously
                    Future<RecordMetadata> future = kafkaProducer.send(record);
                    
                    // Synchronously wait for a response from Message Hub / Kafka on every message produced.
                    // For high throughput the future should be handled asynchronously.
                    RecordMetadata recordMetadata = future.get(5000, TimeUnit.MILLISECONDS);
                    producedMessages++;

                    System.out.println("Message produced, offset: " + recordMetadata.offset());

                    // Short sleep for flow control in this sample app
                    // to make the output easily understandable
                    Thread.sleep(2000); 

                } catch (final InterruptedException e) {
                    System.out.println("Producer closing - caught exception: ");
                } catch (final Exception e) {
                    System.out.println("Sleeping for 5s - Producer has caught : ");
                    try {
                        Thread.sleep(5000); // Longer sleep before retrying
                    } catch (InterruptedException e1) {
                        System.out.println("Producer closing - caught exception: ");
                    }
                }
            }
        } finally {
            kafkaProducer.close(5000, TimeUnit.MILLISECONDS);
            System.out.println(ProducerRunnable.class.toString() + " has shut down.");
        }
    }

    public void shutdown() {
        closing = true;
        System.out.println(ProducerRunnable.class.toString() + " is shutting down.");
    }
}