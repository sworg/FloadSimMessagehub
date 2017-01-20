package com.ibm.cpo.fload.msghub;

import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MessageHubClient1 {


    
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
		
	
	
	
	public static void main(String[] args) throws Exception{
    
		JSONParser parser = new JSONParser();
		Random randForID = new Random();
		try {
			conninfo = (JSONObject) parser.parse(MSGHUB);    			
		} catch(Exception e) {
			e.printStackTrace();
		}

        JSONArray array = (JSONArray)conninfo.get("kafka_brokers_sasl");                
        String bootstrapServers = array.toString();          
        bootstrapServers = bootstrapServers.replace('[', ' ');
        bootstrapServers = bootstrapServers.replace(']', ' ');
        bootstrapServers = bootstrapServers.replace('"',' ');
        bootstrapServers = bootstrapServers.replaceAll("\\s+","");
        
        String adminRestURL = (String)conninfo.get("kafka_admin_url");
        String apiKey = (String)conninfo.get("api_key");
        
	    //Assign topicName to string variable
	    String topicName = "swtopic";
	    // create instance for properties to access producer configs   
	    Properties props = new Properties();
	    //Assign localhost id
	    //props.put("bootstrap.servers", "localhost:9092");
	    props.put("bootstrap.servers", bootstrapServers);
	    //Set acknowledgements for producer requests.      
	    props.put("acks", "all");
	    //If the request fails, the producer can automatically retry,
	    props.put("retries", 0);
	    //Specify buffer size in config
	    props.put("batch.size", 16384);
	    //Reduce the no of requests less than 0   
	    props.put("linger.ms", 1);	    
	    //The buffer.memory controls the total amount of memory available to the producer for buffering.   
	    props.put("buffer.memory", 33554432);
	    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
	    
	    Producer<String, String> producer = new KafkaProducer<String, String>(props);
	          
	    //for(int i = 0; i < 10; i++)
	    int i=11;
	    producer.send(new ProducerRecord<String, String>(topicName, Integer.toString(i), Integer.toString(i)));            
	    System.out.println("Message sent successfully");

	    // close it   
	    producer.close();
	}
}
