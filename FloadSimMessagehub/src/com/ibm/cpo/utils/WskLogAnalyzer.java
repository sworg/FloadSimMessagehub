package com.ibm.cpo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.ssl.SSLContexts;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.cpo.fload.workload.WhiskResult;
import com.ibm.oauth.OAuth2Details;
import com.ibm.oauth.OAuthConstants;
import com.ibm.oauth.WhiskCredential;
import com.sun.javafx.scene.paint.GradientUtils.Parser;

 

public class WskLogAnalyzer {

	public static final int RETRY_LIMIT = 99999;
	public static final int SUCCESS = 0;
	public static final int EXP_BAD_URL = -1;
	public static final int EXP_CONN_ERROR = -2;
	public static final int EXP_SERVICE = -7;
	public static final int EXP_SERVICE_ERROR = -8;
	public static final int EXP_UNSUPPORTED_MEDIA_TYPE= -9;
	public static final int EXP_TOO_MANY_REQUESTS= -10;
	
	private static String scope = "";
	private static Random randForID;
		
	final static String YP = "{ \"BMIX_AUTH_SVR\" : \"https://login.ng.bluemix.net:443/UAALoginServerWAR/oauth/token\","
			+ "\"BMIX_USER\": \"xswang@us.ibm.com\","
			+ "\"BMIX_PWD\":\"con88tract\","
			+ "\"WHISK_AUTH_SRV\":\"https://openwhisk.ng.bluemix.net/bluemix/v1/authenticate\","
			+ "\"API_HOST\":\"https://openwhisk.ng.bluemix.net/api/v1\""
			+ "}";
	
	final static String YS0 = 
			//"{ \"BMIX_AUTH_SVR\" : \"https://login.stage0.ng.bluemix.net:443/UAALoginServerWAR/oauth/token\","
			"{ \"BMIX_AUTH_SVR\" : \"https://mccp.stage0.ng.bluemix.net/login/oauth/token\","
			+ "\"BMIX_USER\": \"ibmuser1@us.ibm.com\","
			+ "\"BMIX_PWD\":\"passw0rd\","
			+ "\"WHISK_AUTH_SRV\":\"https://openwhisk.stage0.ng.bluemix.net/bluemix/v1/authenticate\","
			+ "\"API_HOST\":\"https://openwhisk.stage0.ng.bluemix.net/api/v1\""
			+ "}";
			

	public static JSONObject conninfo = null;

	private WhiskCredential cred = null;
	private CloseableHttpClient cclient;

	
	public static void main(String[] args) throws Exception{				
		//BluemixOath2 bmixoath = new BluemixOath2(YP); 
		//WhiskCredential  authcred = WskLogAnalyzer.getAuth(YP);
		//WskLogAnalyzer bmixoath = new WskLogAnalyzer(authcred); 


		//--- for YS0 ---
		//WhiskResult result =  bmixoath.invoke(999,"POST","ibmuser1@us.ibm.com_dev", "actions", "ctflogin", "?blocking=true", "");	
		
		//--- for YP --- 
		//WhiskResult result =  bmixoath.invoke(999,"POST","xswang@us.ibm.com_dev", "actions", "ctflogin", "?blocking=true", "");	
		//WhiskResult result =  bmixoath.invoke(999,"POST","xswang@us.ibm.com_dev", "ctflogin", "", "");
		//WhiskResult result =  bmixoath.invoke(999,"GET","xswang@us.ibm.com_dev", "", "", "");
		//System.out.println("Return Code:"+result.retcode);
		//result.print();

	
		analyzeWhiskLogs("xswang@us.ibm.com", "ctflogin2a500", 1479414731034L, 1500); 
		//analyzeWhiskLogs("xswang@us.ibm.com", "ctflogin2a500", 1479333375626L, 1500); 
		
		
	}
	
	public WskLogAnalyzer(WhiskCredential cred) {
		this.cred = cred;
		
		try {
			//building HTTP client
			SSLContext sslContext = SSLContexts.custom().useProtocol("TLSv1.2").build();			
			RequestConfig.Builder requestBuilder = RequestConfig.custom();
			requestBuilder = requestBuilder.setCookieSpec(CookieSpecs.IGNORE_COOKIES);
		    requestBuilder = requestBuilder.setSocketTimeout(600000);
			HttpClientBuilder client = HttpClientBuilder.create();
		    client.setDefaultRequestConfig(requestBuilder.build());
			this.cclient = client.setSSLContext(sslContext).build();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static WhiskCredential getAuth(String connstr) {
		
		WhiskCredential cred = null;
		JSONParser parser = new JSONParser();
		randForID = new Random();
		try {
			conninfo = (JSONObject) parser.parse(connstr);
			String BMIX_AUTH_SVR = (String)conninfo.get("BMIX_AUTH_SVR");
			System.out.println(BMIX_AUTH_SVR+"\n");

			// step 1; get oAuth access token from bluemix auth server
			Map<String, String> map= getBluemixAccessToken((String)conninfo.get("BMIX_AUTH_SVR"), (String)conninfo.get("BMIX_USER"), (String)conninfo.get("BMIX_PWD"));
			String accessToken = map.get(OAuthConstants.ACCESS_TOKEN);
			String refreshToken = map.get(OAuthConstants.REFRESH_TOKEN);
			//String expires_in = map.get(OAuthConstants.EXPIRES_IN);
			System.out.println("Response from Bluemix Authentication:\n"+accessToken+"\n"+refreshToken+"\n");

			//reformat for the next step
			StringBuffer retBuf = new StringBuffer().append("{\"accessToken\" : \"").append(accessToken).append("\", \"refreshToken\" : \"").append(refreshToken).append("\"}");		
			// step 2; get openwhisk credential from openwhisk auth server 
			cred = getOpenWhiskAccessToken((String)conninfo.get("WHISK_AUTH_SRV"), retBuf.toString());
			
		} catch(Exception e) {
			e.printStackTrace();
		}

		return cred;
		
	}
	
	
	
		
	public WhiskResult invoke(int threadID, String method, String namesp, String cmd, String cmdvalue, String opts, String payload) throws Exception {
		
		StringBuffer target  = new StringBuffer().append((String) conninfo.get("API_HOST")).append("/namespaces/").append(namesp);
		// compose cmd section 
		if (cmd !=null && cmdvalue!=null && cmd.equalsIgnoreCase("activations")) {
			target.append("/activations").append(cmdvalue);			
		} else if (cmd !=null && cmdvalue!=null && cmd.equalsIgnoreCase("actions")) {
			target.append("/actions/").append(cmdvalue);
		}

		// compose option section
		if (opts != null && opts.trim().length()>0) {
			target.append("?").append(opts);
		}


		System.out.println("*** Target:"+target.toString());
		WhiskResult result = null;		
		boolean retryInd = false;
		int retryCount = 0;
		int retcode = 0;	
		do {
			//System.out.println("@@@invoking - count="+retryCount+ "method:"+method);
			// step 3; use the openwhisk credential to issue invoke openwhisk action
			if (method!=null&&method.equalsIgnoreCase("POST")) {
				//System.out.println("@@@invoking["+threadID+"] - count="+retryCount+ "method:"+method+" payload:"+payload+"tgt:"+target.toString());
				result = httpAction("POST", target.toString(), payload, this.cred.getUuid(), this.cred.getKey());
			} else
				result = httpAction("GET", target.toString(), "", this.cred.getUuid(), this.cred.getKey());					
			if (result.retcode < 300) {
				retcode = 0;
				retryInd = false;
			} else if (result.retcode == 429) {
				retcode = 429;
				retryInd = true;
				//retryInd = false;
				retryCount++;
				//System.out.println("@@@retrying["+threadID+"] - count="+retryCount);
			} else if (result.retcode >= 400 && result.retcode  < 500) {
				retcode = EXP_SERVICE;
			} else if (result.retcode  >= 500) {
				retcode = EXP_SERVICE_ERROR;
			}
		} while (retryInd && retryCount<RETRY_LIMIT);		
		result.retcode = retcode;
		result.retrycnt = retryCount;		
		return result;		
		
	}

	public static boolean hasTokenExpired() {
		boolean indicator = false;
		return indicator;
		
	}
	
	//$ retrieve activations by using api 
	//$ curl -H 'Authorization: Basic ...' 'https://openwhisk.ng.bluemix.net/api/v1/namespaces/_/activations?limit=100&skip=0&docs=true'
	//$ then analyze them.   
	public static void analyzeWhiskLogs(String namespace, String action, long upto, int limit) throws Exception{	
		
		WhiskCredential  authcred = WskLogAnalyzer.getAuth(YP);
		WskLogAnalyzer bmixoath = new WskLogAnalyzer(authcred); 

		// to get a list of activationIds 
		System.out.println("Getting OpenWhisk activations using HTTP GET command... ");
		
		int batch = limit/20+1;
		long new_upto = upto;
		for (int j=0;j<batch;j++) {
			@SuppressWarnings("unchecked")
			ArrayList<String> activationId_list = new ArrayList();	
			StringBuffer target1  = new StringBuffer().append((String) conninfo.get("API_HOST")).append("/namespaces/").append(namespace);
			//target1.append("/activations").append("?limit=").append(limit).append("&name=").append(action).append("&since=").append(since).append("&skip=0");		
			target1.append("/activations").append("?upto=").append(new_upto).append("&name=").append(action).append("&skip=0").append("&limit=").append(20);	
			System.out.println("Bach["+batch+"] - Target:"+target1);
			WhiskResult result = bmixoath.httpAction("GET", target1.toString(), "", bmixoath.cred.getUuid(), bmixoath.cred.getKey());						
			if (result.retcode >= 300) {
				throw new Exception("Error when getting activationId list.  Code:"+result.retcode);
			}
			JSONArray json_activations=(JSONArray) (new JSONParser()).parse(result.result);		
			if (json_activations != null) {
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> iterator = json_activations.iterator();
				int count = 1;
				while (iterator.hasNext()) {
					JSONObject one_activation = iterator.next();
					//System.out.println("["+count+"]"+one_activation.toString());				
					String activationId = (one_activation.get("activationId")).toString();
					//System.out.println("["+count+"]<"+activationId);
					activationId_list.add(activationId);				
					count++;
				}
			} else 
				System.out.println("No activation is found.");		
		
			System.out.println("==== end of activationId list of batch ["+batch+"]====");		
	
			// Retrieve individual activations and analyze them
			long runStart = 9223372036854775807L;
			long runEnd = 0L;
			long runStartTS = 9223372036854775807L;
			long runEndTS = 0L;
			for (int i=0;i<activationId_list.size();i++) {
				
				String activationId = activationId_list.get(i);
				//to retrieve full activation body by using activationId 
				StringBuffer target2  = new StringBuffer().append((String) conninfo.get("API_HOST")).append("/namespaces/").append(namespace);
				target2.append("/activations/").append(activationId);	
				WhiskResult result2 = bmixoath.httpAction("GET", target2.toString(), "", bmixoath.cred.getUuid(), bmixoath.cred.getKey());		
				if (result2.retcode >= 300) {
					throw new Exception("Error when getting activation detail of "+activationId+".  Code:"+result.retcode);
				}
				String returned = result2.result.trim().substring(0,1);
				JSONArray mul_activations = null;
				JSONObject one_activation = null;
				if (returned.equalsIgnoreCase("[")) {
					mul_activations =(JSONArray) (new JSONParser()).parse(result2.result);					
					one_activation=(JSONObject) mul_activations.get(0);					
					//System.out.println("*** From an array[0]:"+one_activation.toString());
				} else if (returned.equalsIgnoreCase("{")) {
					one_activation=(JSONObject) (new JSONParser()).parse(result2.result);	
					//System.out.println("*** From an single:"+one_activation.toString());
				}
	
				String start = (one_activation.get("start")).toString();
				String end = (one_activation.get("end")).toString();
				String duration = (one_activation.get("duration")).toString();
				long startl = Long.parseLong(start);
				long endl = Long.parseLong(end);
				//System.out.println("Got time - start:"+start+" end:"+end+" duration:"+duration+" my duration:"+(endl-startl));
	
				JSONArray logs = (JSONArray)(one_activation.get("logs"));
				String log_entry = logs.get(1).toString();
				//System.out.println("Got logs:"+logs.get(1).toString());
				String my_start = log_entry.substring(log_entry.indexOf("ST@")+4, log_entry.indexOf("ST@")+17);
				String my_end = log_entry.substring(log_entry.indexOf("ET@")+4, log_entry.indexOf("ET@")+17);
				long my_startl = Long.parseLong(my_start);
				long my_endl = Long.parseLong(my_end);
				//System.out.println("Got my time - start:"+my_start+" end:"+my_end+" my duration:"+(my_endl-my_startl));
				
				if (startl < runStart)
					runStart = startl;
				if (endl > runEnd)
					runEnd = endl;
				long run_duration = runEnd - runStart;
				
				if (my_startl < runStartTS)
					runStartTS = my_startl;
				if (my_endl > runEndTS)
					runEndTS = my_endl;
				long ts_duration = runEndTS-runStartTS;
				
				System.out.println("*** ["+i+","+new_upto+"]<"+activationId+">**Overall** Duration Wsk:"+run_duration+" Duration TS:"+ts_duration+" Wsk Act Start:"+runStart+" Start TS inside:"+runStartTS+" End TS inside:"+runEndTS+" WSK Act End:"+runEnd);
				//System.out.println("*** ["+i+","+new_upto+"]<"+activationId+">*This actn* Duration Wsk:"+(endl-startl)+" Duration TS:"+(my_endl-my_startl)+" Wsk Act Start:"+startl+  " Start TS inside:"+my_startl+ " End TS inside:"+my_endl+ " WSK Act End:"+endl);							
				
			}
			new_upto = runStart;
			System.out.println("new upto="+new_upto);
				
		}
		

	}
	
	
	
	
	//activations?limit=100&skip=0&docs=true
	public JSONArray getActivations(String namespace, String activationId, int limit, long since) throws Exception {

		System.out.println("Getting OpenWhisk activations using HTTP GET command... ");
		StringBuffer target  = new StringBuffer().append((String) conninfo.get("API_HOST")).append("/namespaces/").append(namespace);
		if (activationId == null || activationId.trim().length()==0) {
			target.append("/activations").append("?limit=").append(limit).append("&since=").append(since).append("&skip=0");				
		} else {
			target.append("/activations/").append(activationId).append("?limit=").append(limit);	
		}
		WhiskResult result = httpAction("GET", target.toString(), "", this.cred.getUuid(), this.cred.getKey());		
		//System.out.println(result);		
		if (result.retcode < 300) {
			if (activationId == null || activationId.trim().length()==0) {
				target.append("/activations").append("?limit=").append(limit).append("&since=").append(since).append("&skip=0");				
			} else {
				target.append("/activations/").append(activationId).append("?limit=").append(limit);	
			}
			JSONArray json_result=(JSONArray) (new JSONParser()).parse(result.result);		
			return json_result;			
		} else
			return null;
				
	}
	
	public WhiskResult httpAction(String action, String url, String payload, String uuid, String key) throws Exception {

		//System.out.println("Action:"+action);
		//System.out.println("URL:"+url);
		//System.out.println("UUID:"+uuid);
		//System.out.println("KEY:"+key);
		
		HttpRequestBase act = null;
		if (action.equalsIgnoreCase("GET")) {
			act = new HttpGet(url);
		} else if (action.equalsIgnoreCase("POST")) {
			act = new HttpPost(url);
		} else if (action.equalsIgnoreCase("HEAD")) {
			act = new HttpHead(url);
		} else if (action.equalsIgnoreCase("DELETE")) {
			act = new HttpDelete(url);
		}
		
		HttpResponse response = null;
		WhiskResult result = new WhiskResult(0,"",0);
		String response_text = "";
		int code = 0;
		try {			
			if (action.equalsIgnoreCase("POST")) {
				((HttpPost)act).setEntity(new StringEntity(payload));
			}	
			act.addHeader("Content-Type", "application/json");		
			act.addHeader(OAuthConstants.AUTHORIZATION, getBasicAuthorizationHeader(uuid, key));						
			response = this.cclient.execute(act);			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		    String line = "";
		    while ((line = rd.readLine()) != null) {
	    	  response_text += line;
		    }
		    //System.out.println("***RESP:"+response_text);
		    code = response.getStatusLine().getStatusCode();
			if (code == OAuthConstants.HTTP_UNAUTHORIZED) {
				response_text = null;
				System.out.println("Authorization fails.");
			}			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		result.retcode = code;
		result.result = response_text;

		return result;

	}

	
	
	// Get OpenWhisk authorization by using the access token provided by the Bluemix authentication 	
	public static WhiskCredential getOpenWhiskAccessToken(String whiskAuthServer, String bluemixAccessString) throws Exception {
		
		HttpPost post = new HttpPost(whiskAuthServer);
		HttpResponse response = null;
		WhiskCredential cred = new WhiskCredential();		
		try {
			post.addHeader("Content-Type", "application/json");			
			post.setEntity(new StringEntity(bluemixAccessString));
			SSLContext sslContext = SSLContexts.custom().useProtocol("TLSv1.2").build();
			HttpClientBuilder client = HttpClientBuilder.create();
			response = client.setSSLContext(sslContext).build().execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code == OAuthConstants.HTTP_UNAUTHORIZED) {
				System.out.println("Authorization fails.");
			}
			//System.out.println("Whisk Auth RESP="+response);
			Map<String, String> map = handleResponse(response);			
			cred.setUuid(map.get("uuid"));
			cred.setKey(map.get("key"));
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return cred;
	} 

	
	//Retrieve Bluemix access token based on user/pwd on the IBM ID for Bluemix account e.g. abc@us.ibm.com/abcpwd
	public static Map<String, String> getBluemixAccessToken(String bluemixHost, String bluemixUname, String bluemixPass) throws Exception {
		
		HttpPost post = new HttpPost(bluemixHost);
		List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
		parametersBody.add(new BasicNameValuePair(OAuthConstants.GRANT_TYPE, "password"));	
		parametersBody.add(new BasicNameValuePair("username", bluemixUname));		
		parametersBody.add(new BasicNameValuePair("password", bluemixPass));
		if (isValid(scope)) {
			parametersBody.add(new BasicNameValuePair(OAuthConstants.SCOPE,	scope));
		}
		
		HttpResponse response = null;
		String accessToken = null;
		String refreshToken = null;
		Map<String, String> map = null;
		try {
			post.setEntity(new UrlEncodedFormEntity(parametersBody));
			post.addHeader(OAuthConstants.AUTHORIZATION, getBasicAuthorizationHeader("cf", ""));
			SSLContext sslContext = SSLContexts.custom().useProtocol("TLSv1.2").build();
			HttpClientBuilder client = HttpClientBuilder.create();
			response = client.setSSLContext(sslContext).build().execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code == OAuthConstants.HTTP_UNAUTHORIZED) {
				System.out.println("Authorization fails.");
			}
			map = handleResponse(response);
			accessToken = map.get(OAuthConstants.ACCESS_TOKEN);
			refreshToken = map.get(OAuthConstants.REFRESH_TOKEN);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;
	}

	
	public static Map handleResponse(HttpResponse response) {
		String contentType = OAuthConstants.JSON_CONTENT;
		if (response.getEntity().getContentType() != null) {
			contentType = response.getEntity().getContentType().getValue();
		}
		if (contentType.contains(OAuthConstants.JSON_CONTENT)) {
			return handleJsonResponse(response);
		} else if (contentType.contains(OAuthConstants.URL_ENCODED_CONTENT)) {
			return handleURLEncodedResponse(response);
		} else if (contentType.contains(OAuthConstants.XML_CONTENT)) {
			return handleXMLResponse(response);
		} else {
			// Unsupported Content type
			throw new RuntimeException("Cannot handle " + contentType + " content type. Supported content types include JSON, XML and URLEncoded");
		}

	}

	public static Map handleJsonResponse(HttpResponse response) {
		Map<String, String> oauthLoginResponse = null;
		String contentType = response.getEntity().getContentType().getValue();
		try {
			oauthLoginResponse = (Map<String, String>) new JSONParser().parse(EntityUtils.toString(response.getEntity()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException();
		} catch (RuntimeException e) {
			System.out.println("Could not parse JSON response");
			throw e;
		}
		System.out.println();
		System.out.println("********** Response Received **********");
		for (Map.Entry<String, String> entry : oauthLoginResponse.entrySet()) {
			System.out.println(String.format("  %s = %s", entry.getKey(),
					entry.getValue()));
		}
		return oauthLoginResponse;
	}

	public static Map handleURLEncodedResponse(HttpResponse response) {
		Map<String, Charset> map = Charset.availableCharsets();
		Map<String, String> oauthResponse = new HashMap<String, String>();
		Set<Map.Entry<String, Charset>> set = map.entrySet();
		Charset charset = null;
		HttpEntity entity = response.getEntity();

		System.out.println();
		System.out.println("********** Response Received **********");

		for (Map.Entry<String, Charset> entry : set) {
			System.out.println(String.format("  %s = %s", entry.getKey(), entry.getValue()));
			if (entry.getKey().equalsIgnoreCase(HTTP.UTF_8)) {
				charset = entry.getValue();
			}
		}

		try {
			List<NameValuePair> list = URLEncodedUtils.parse(EntityUtils.toString(entity), Charset.forName(HTTP.UTF_8));
			for (NameValuePair pair : list) {
				System.out.println(String.format("  %s = %s", pair.getName(), pair.getValue()));
				oauthResponse.put(pair.getName(), pair.getValue());
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Could not parse URLEncoded Response");
		}

		return oauthResponse;
	}

	public static Map handleXMLResponse(HttpResponse response) {
		Map<String, String> oauthResponse = new HashMap<String, String>();
		try {
			String xmlString = EntityUtils.toString(response.getEntity());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = factory.newDocumentBuilder();
			InputSource inStream = new InputSource();
			inStream.setCharacterStream(new StringReader(xmlString));
			Document doc = db.parse(inStream);
			//System.out.println("********** Response Receieved **********");
			parseXMLDoc(null, doc, oauthResponse);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Exception occurred while parsing XML response");
		}
		return oauthResponse;
	}

	public static void parseXMLDoc(Element element, Document doc,
			Map<String, String> oauthResponse) {
		NodeList child = null;
		if (element == null) {
			child = doc.getChildNodes();
		} else {
			child = element.getChildNodes();
		}
		for (int j = 0; j < child.getLength(); j++) {
			if (child.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {org.w3c.dom.Element childElement = (org.w3c.dom.Element) child.item(j);
				if (childElement.hasChildNodes()) {
					System.out.println(childElement.getTagName() + " : " + childElement.getTextContent());
					oauthResponse.put(childElement.getTagName(), childElement.getTextContent());
					parseXMLDoc(childElement, null, oauthResponse);
				}

			}
		}
	}

	public static String getAuthorizationHeaderForAccessToken(String accessToken) {
		return OAuthConstants.BEARER + " " + accessToken;
	}

	public static String getBasicAuthorizationHeader(String username, String password) {
		return OAuthConstants.BASIC + " " + encodeCredentials(username, password);
	}

	public static String encodeCredentials(String username, String password) {
		String cred = username + ":" + password;
		String encodedValue = null;
		byte[] encodedBytes = Base64.encodeBase64(cred.getBytes());
		encodedValue = new String(encodedBytes);
		//System.out.println("encodedBytes " + new String(encodedBytes));

		byte[] decodedBytes = Base64.decodeBase64(encodedBytes);
		//System.out.println("decodedBytes " + new String(decodedBytes));

		return encodedValue;

	}
	
	public static boolean isValidInput(OAuth2Details input){
			
		
		if(input == null){
			return false;
		}
		
		String grantType = input.getGrantType();
		
		if(!isValid(grantType)){
			System.out.println("Please provide valid value for grant_type");
			return false;
		}
		
		if(!isValid(input.getAuthenticationServerUrl())){
			System.out.println("Please provide valid value for authentication server url");
			return false;
		}
		
		if(grantType.equals(OAuthConstants.GRANT_TYPE_PASSWORD)){
			//if(!isValid(input.getUsername()) || !isValid(input.getPassword())){
			if(!isValid(input.getUsername())){
				System.out.println("Please provide valid username and password for password grant_type");
				return false;
			}
		}
		
		if(grantType.equals(OAuthConstants.GRANT_TYPE_CLIENT_CREDENTIALS)){
			if(!isValid(input.getClientId()) || !isValid(input.getClientSecret())){
				System.out.println("Please provide valid client_id and client_secret for client_credentials grant_type");
				return false;
			}
		}
		
		System.out.println("Validated Input");
		return true;
		
	}

	public static boolean isValid(String str) {
		return (str != null && str.trim().length() > 0);
	}

}
