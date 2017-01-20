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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;


import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.messagehub.samples.ProducerAsClient;


/**
 * Console-based sample interacting with Message Hub, authenticating with SASL/PLAIN over an SSL connection.
 *
 * @author IBM
 */
public class MessageHubClient3 {

    private static final String JAAS_CONFIG_PROPERTY = "java.security.auth.login.config";
    private static final String APP_NAME = "kafka-java-console-sample-2.0";
    private static final String TOPIC_NAME = "swtopic";  
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
    private final Properties clientProperties = new Properties();
    private ProducerAsClient producer_client;
    
	public MessageHubClient3(){
		initialize();
        Properties producerProperties = getClientConfiguration(clientProperties, "producer.properties");
        producer_client = new ProducerAsClient(producerProperties, TOPIC_NAME);   
	}
	
		
	public void checkTopicExistance(Properties producerProperties, String topic) {
		
        // Create a Kafka producer with the provided client configuration
        kafkaProducer = new KafkaProducer<String, String>(producerProperties);
        try {
            // Checking for topic existence.
            // If the topic does not exist, the kafkaProducer will retry for about 60 secs
            // before throwing a TimeoutException see configuration parameter 'metadata.fetch.timeout.ms'
            List<PartitionInfo> partitions = kafkaProducer.partitionsFor(topic);
            System.out.println(partitions.toString());                        
        } catch (TimeoutException kte) {
            System.out.println("Topic '" + topic + "' may not exist - application will terminate");
            kafkaProducer.close();
            throw new IllegalStateException("Topic '" + topic + "' may not exist - application will terminate", kte);
        }

	}
	    
    public static void main(String args[])  {
    	
    	MessageHubClient3 client3 = new MessageHubClient3();
    	client3.invoke();
    	client3.close();
    	
    }
    
    
    public void initialize() {
    	
        try {        	
            final String userDir = System.getProperty("user.dir");
            String bootstrapServers = null;
            String adminRestURL = null;
            String apiKey = null;            
			JSONParser parser = new JSONParser();
			try {
				conninfo = (JSONObject) parser.parse(MSGHUB);
			} catch (Exception e) {
				e.printStackTrace();
			}

			resourceDir = userDir + File.separator + "WebContent" + File.separator + "resources";
			System.out.println("Running Kafka Producer in local mode. resourceDir:" + resourceDir);

			JSONArray array = (JSONArray) conninfo.get("kafka_brokers_sasl");
			bootstrapServers = array.toString();
			bootstrapServers = bootstrapServers.replace('[', ' ');
			bootstrapServers = bootstrapServers.replace(']', ' ');
			bootstrapServers = bootstrapServers.replace('"', ' ');
			bootstrapServers = bootstrapServers.replaceAll("\\s+", "");
			adminRestURL = (String) conninfo.get("kafka_admin_url");
			apiKey = (String) conninfo.get("api_key");
			updateJaasConfiguration(apiKey.substring(0, 16), apiKey.substring(16));
            
            //inject bootstrapServers in configuration, for both consumer and producer
            clientProperties.put("bootstrap.servers", bootstrapServers);
            System.out.println("==> Kafka Endpoints: " + bootstrapServers);
            System.out.println("==> Kafka Admin REST Endpoint: " + adminRestURL);

            //Using Message Hub Admin REST API to create and list topics
            //If the topic already exists, creation will be a no-op
            //try {
            //    System.out.println("Creating the topic " + TOPIC_NAME);
            //    String restResponse = RESTAdmin.createTopic(adminRestURL, apiKey, TOPIC_NAME);
            //    System.out.println("Admin REST response :" +restResponse);
            //    String topics = RESTAdmin.listTopics(adminRestURL, apiKey);
            //    System.out.println("Admin REST Listing Topics: " + topics);
            //} catch (Exception e) {
            //    System.out.println("Error occurred accessing the Admin REST API ");
            //    //The application will carry on regardless of Admin REST errors, as the topic may already exist
            //}
                
         } catch (Exception e) {
        	e.printStackTrace();
            System.out.println("Exception occurred, application will terminate");
            System.exit(-1);
        }
    }

    
    public void invoke() {
    	String text = "Stream test message";
        producer_client.postMsg(text);
        
    }
    
    public void close() {
    	producer_client.close();
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
