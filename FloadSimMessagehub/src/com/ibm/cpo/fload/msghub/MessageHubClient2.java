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
package com.ibm.cpo.fload.msghub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.messagehub.samples.ConsumerRunnable;
import com.messagehub.samples.ProducerRunnable;
import com.messagehub.samples.bluemix.BluemixEnvironment;
import com.messagehub.samples.bluemix.MessageHubCredentials;
import com.messagehub.samples.rest.RESTAdmin;

/**
 * Console-based sample interacting with Message Hub, authenticating with SASL/PLAIN over an SSL connection.
 *
 * @author IBM
 */
public class MessageHubClient2 {

    private static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    private static final String APP_NAME = "kafka-java-console-sample-2.0";
    private static final String TOPIC_NAME = "swtopic";

    private static Thread consumerThread = null;
    private static ConsumerRunnable consumerRunnable = null;
    private static Thread producerThread = null;
    private static ProducerRunnable producerRunnable = null;
    private static String resourceDir;

    private static final String MSGHUB = "{"
	  +"\"mqlight_lookup_url\": \"https://mqlight-lookup-prod01.messagehub.services.us-south.bluemix.net/Lookup?serviceId=8967900f-a755-40b7-acce-350b51d0acb2\","
	  +"\"api_key\": \"HSgWa4KFnDmDTJSVaMx3lSBn9yPG6sKYHj8vKEi41qJDubIJ\","
	  +"\"kafka_admin_url\": \"https://kafka-admin-prod01.messagehub.services.us-south.bluemix.net:443\","
	  +"\"kafka_rest_url\": \"https://kafka-rest-prod01.messagehub.services.us-south.bluemix.net:443\","
	  +"\"kafka_brokers_sasl\": ["
	  +"\"kafka01-prod01.messagehub.services.us-south.bluemix.net:9093\","
	  +"\"kafka02-prod01.messagehub.services.us-south.bluemix.net:9093\","
	  +"\"kafka03-prod01.messagehub.services.us-south.bluemix.net:9093\","
	  +"\"kafka04-prod01.messagehub.services.us-south.bluemix.net:9093\","
	  +"\"kafka05-prod01.messagehub.services.us-south.bluemix.net:9093\""
	  +"],"
	  +"\"user\": \"HSgWa4KFnDmDTJSV\","
	  +"\"password\": \"aMx3lSBn9yPG6sKYHj8vKEi41qJDubIJ\""
	  +"}";

	public static JSONObject conninfo = null;

    private KafkaProducer<String, String> kafkaProducer;
    private final String topic="";

    
	public MessageHubClient2(Properties producerProperties, String topic){
		
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
	
    //add shutdown hooks (intercept CTRL-C etc.)  
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { 
                System.out.println("Shutdown received.");
                shutdown();
            }
        });
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                System.out.println("Uncaught Exception on " + t.getName() + " : ");
                shutdown();
            }
        });
    }
    
    public static void main(String args[])  {
        try {
            final String userDir = System.getProperty("user.dir");
            final boolean isRunningInBluemix = BluemixEnvironment.isRunningInBluemix();
            final Properties clientProperties = new Properties();

            String bootstrapServers = null;
            String adminRestURL = null;
            String apiKey = null;
            boolean runConsumer = true;
            boolean runProducer = false;

            // Check environment: Bluemix vs Local, to obtain configuration parameters
            if (isRunningInBluemix) {

                System.out.println("Running in Bluemix mode.");
                resourceDir = userDir + File.separator + APP_NAME + File.separator + "bin" + File.separator + "resources";

                MessageHubCredentials credentials = BluemixEnvironment.getMessageHubCredentials();

                bootstrapServers = stringArrayToCSV(credentials.getKafkaBrokersSasl());
                adminRestURL = credentials.getKafkaRestUrl();
                apiKey = credentials.getApiKey();

                updateJaasConfiguration(credentials.getUser(), credentials.getPassword());

            } else {
            	
                System.out.println("Running in local mode.");
        		JSONParser parser = new JSONParser();
        		Random randForID = new Random();
        		try {
        			conninfo = (JSONObject) parser.parse(MSGHUB);    			
        		} catch(Exception e) {
        			e.printStackTrace();
        		}

                resourceDir = userDir + File.separator + "WebContent" + File.separator + "resources";
                System.out.println("Running in local mode. resourceDir:"+resourceDir);
 
                JSONArray array = (JSONArray)conninfo.get("kafka_brokers_sasl");                
                bootstrapServers = array.toString();          
                bootstrapServers = bootstrapServers.replace('[', ' ');
                bootstrapServers = bootstrapServers.replace(']', ' ');
                bootstrapServers = bootstrapServers.replace('"',' ');
                bootstrapServers = bootstrapServers.replaceAll("\\s+","");
                adminRestURL = (String)conninfo.get("kafka_admin_url");
                apiKey = (String)conninfo.get("api_key");

                updateJaasConfiguration(apiKey.substring(0, 16), apiKey.substring(16));

             }
            
            //inject bootstrapServers in configuration, for both consumer and producer
            clientProperties.put("bootstrap.servers", bootstrapServers);

            System.out.println("Kafka Endpoints: " + bootstrapServers);
            System.out.println("Admin REST Endpoint: " + adminRestURL);

            //Using Message Hub Admin REST API to create and list topics
            //If the topic already exists, creation will be a no-op
            try {
                System.out.println("Creating the topic " + TOPIC_NAME);
                String restResponse = RESTAdmin.createTopic(adminRestURL, apiKey, TOPIC_NAME);
                System.out.println("Admin REST response :" +restResponse);

                String topics = RESTAdmin.listTopics(adminRestURL, apiKey);
                System.out.println("Admin REST Listing Topics: " + topics);
                
            } catch (Exception e) {
                System.out.println("Error occurred accessing the Admin REST API ");
                //The application will carry on regardless of Admin REST errors, as the topic may already exist
            }

            //create the Kafka clients
            if (runConsumer) {
                Properties consumerProperties = getClientConfiguration(clientProperties, "consumer.properties");
                consumerRunnable = new ConsumerRunnable(consumerProperties, TOPIC_NAME);
                consumerThread = new Thread(consumerRunnable, "Consumer Thread");
                consumerThread.start();
            }

            if (runProducer) {
                Properties producerProperties = getClientConfiguration(clientProperties, "producer.properties");
                //MessageHubClient2 client = new MessageHubClient2(producerProperties, TOPIC_NAME);
                
                producerRunnable = new ProducerRunnable(producerProperties, TOPIC_NAME);
                producerThread = new Thread(producerRunnable, "Producer Thread");
                producerThread.start();

            
            }
            
            System.out.println("MessageHubConsoleSample will run until interrupted.");
        } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Exception occurred, application will terminate");
            System.exit(-1);
        }
    }

    
    
    public void postMsgs() {
    	
        int producedMessages = 0;
        System.out.println( ProducerRunnable.class.toString() + " is starting.");

        try {
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
                    System.out.println( "Message produced, offset: " + recordMetadata.offset());
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
        } finally {
            kafkaProducer.close(5000, TimeUnit.MILLISECONDS);
            System.out.println( ProducerRunnable.class.toString() + " has shut down.");
        }

    } // end of method
    
    
    
    /*
     * convenience method for cleanup on shutdown
     */
    private static void shutdown() {
        if (producerRunnable != null)
            producerRunnable.shutdown();
        if (consumerRunnable != null)
            consumerRunnable.shutdown();
        if (producerThread != null)
            producerThread.interrupt();
        if (consumerThread != null)
            consumerThread.interrupt();
    }
    
    
    /*
     * Return a CSV-String from a String array
     */
    private static String stringArrayToCSV(String[] sArray) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sArray.length; i++) {
            sb.append(sArray[i]);
            if (i < sArray.length -1) sb.append(",");
        }
        return sb.toString();
    }

    /*
     * Retrieve client configuration information, using a properties file, for
     * connecting to Message Hub Kafka.
     */
    static final Properties getClientConfiguration(Properties commonProps, String fileName) {
        Properties result = new Properties();
        InputStream propsStream;

        try {
            propsStream = new FileInputStream(resourceDir + File.separator + fileName);
            result.load(propsStream);
            propsStream.close();
        } catch (IOException e) {
            System.out.println("Could not load properties from file");
            return result;
        }

        result.putAll(commonProps);
        return result;
    }

    /*
     * Updates JAAS config file with provided credentials.
     */ 
    private static void updateJaasConfiguration(String username, String password) throws IOException {
        // Set JAAS configuration property.
        String jaasConfPath = System.getProperty("java.io.tmpdir") + "jaas.conf";
        System.setProperty(JAAS_CONFIG_PROPERTY, jaasConfPath);

        String templatePath = resourceDir + File.separator + "jaas.conf.template";
        OutputStream jaasOutStream = null;

        System.out.println("Updating JAAS configuration");

        try {
            String templateContents = new String(Files.readAllBytes(Paths.get(templatePath)));
            jaasOutStream = new FileOutputStream(jaasConfPath, false);

            // Replace username and password in template and write
            // to jaas.conf in resources directory.
            String fileContents = templateContents
                    .replace("$USERNAME", username)
                    .replace("$PASSWORD", password);

            jaasOutStream.write(fileContents.getBytes(Charset.forName("UTF-8")));
        } catch (final IOException e) {
            System.out.println("Failed accessing to JAAS config file");
            throw e;
        } finally {
            if (jaasOutStream != null) {
                try {
                    jaasOutStream.close();
                } catch(final Exception e) {
                    System.out.println("Error closing generated JAAS config file");
                }
            }
        }
    }
}
