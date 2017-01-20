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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corp. 2015-2016
 */
package com.messagehub.samples;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ConsumerRunnable implements Runnable {
    private static final Logger logger = Logger.getLogger(ConsumerRunnable.class);

    private final KafkaConsumer<String, String> kafkaConsumer;
    private volatile boolean closing = false;

    public ConsumerRunnable(Properties consumerProperties, String topic) {
        // Create a Kafka consumer with the provided client configuration
        kafkaConsumer = new KafkaConsumer<String, String>(consumerProperties);
        System.out.println("constructor: to list partions....");
        // Checking for topic existence before subscribing
        List<PartitionInfo> partitions = kafkaConsumer.partitionsFor(topic);
        if (partitions == null || partitions.isEmpty()) {
            logger.log(Level.ERROR, "Topic '" + topic + "' does not exists - application will terminate");
            kafkaConsumer.close();
            throw new IllegalStateException("Topic '" + topic + "' does not exists - application will terminate");
        } else {
            logger.log(Level.INFO, partitions.toString());
        }
        
        System.out.println("constructor: to subscribe....");
        kafkaConsumer.subscribe(Arrays.asList(topic));
        System.out.println("constructor: subscribed");
    }

    @Override
    public void run() {
        ///logger.log(Level.INFO, ConsumerRunnable.class.toString() + " is starting.");
        System.out.println("consumer thread: start running....");

        try {
            while (!closing) {
                try {
                    System.out.println("consumer thread: loop....");
                    // Poll on the Kafka consumer, waiting up to 3 secs if there's nothing to consume.
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(3000);
                    
                    if (records.isEmpty()) {
                        System.out.println("No messages consumed");
                    } else {
                        // Iterate through all the messages received and print their content
                        for (ConsumerRecord<String, String> record : records) {
                            System.out.println("Message consumed: " + record.toString());
                        }
                    }

                } catch (final WakeupException e) {
                	System.out.println("Consumer closing - caught exception: ");
                } catch (final KafkaException e) {
                	System.out.println("Sleeping for 5s - Consumer has caught: ");
                    try {
                        Thread.sleep(5000); // Longer sleep before retrying
                    } catch (InterruptedException e1) {
                    	System.out.println("Consumer closing - caught exception:"); 
                    }
                }
            }
        } finally {
            kafkaConsumer.close();
            logger.log(Level.INFO, ConsumerRunnable.class.toString() + " has shut down.");
        }
    }

    public void shutdown() {
        closing = true;
        kafkaConsumer.wakeup();
        logger.log(Level.INFO, ConsumerRunnable.class.toString() + " is shutting down.");
    }
}
