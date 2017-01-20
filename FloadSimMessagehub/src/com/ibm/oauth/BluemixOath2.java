package com.ibm.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.http.ssl.SSLContexts;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.ibm.cpo.fload.workload.WhiskResult;



public class BluemixOath2 {

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
	
	public BluemixOath2(WhiskCredential cred) {
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
			System.out.println(BMIX_AUTH_SVR);

			// step 1; get oAuth access token from bluemix auth server
			Map<String, String> map= getBluemixAccessToken((String)conninfo.get("BMIX_AUTH_SVR"), (String)conninfo.get("BMIX_USER"), (String)conninfo.get("BMIX_PWD"));
			String accessToken = map.get(OAuthConstants.ACCESS_TOKEN);
			String refreshToken = map.get(OAuthConstants.REFRESH_TOKEN);
			//String expires_in = map.get(OAuthConstants.EXPIRES_IN);
			StringBuffer retBuf = new StringBuffer().append("{\"accessToken\" : \"").append(accessToken).append("\", \"refreshToken\" : \"").append(refreshToken).append("\"}");		
			System.out.println("Return from Bluemix -- "+retBuf.toString()+" expires_in="+" \n");

			// step 2; get openwhisk credential from openwhisk auth server 
			cred = getOpenWhskAccessToken((String)conninfo.get("WHISK_AUTH_SRV"), retBuf.toString());
			
		} catch(Exception e) {
			e.printStackTrace();
		}

		return cred;
		
	}
	
	
	
	public static void main(String[] args) throws Exception{				
		//BluemixOath2 bmixoath = new BluemixOath2(YP); 
		WhiskCredential  authcred = BluemixOath2.getAuth(YP);
		BluemixOath2 bmixoath = new BluemixOath2(authcred); 
		WhiskResult result =  bmixoath.invoke(1, "POST","xswang@us.ibm.com_dev", "ctflogin", "?blocking=true", "");
		//WhiskResult result =  bmixoath.invoke("POST","ibmuser1@us.ibm.com_dev", "ctflogin", "?blocking=true", "");		
		//WhiskResult result =  bmixoath.invoke("POST","xswang@us.ibm.com_dev", "ctflogin", "", "");
		//WhiskResult result =  bmixoath.invoke("GET","xswang@us.ibm.com_dev", "", "", "");
		result.print();
	}
		
	public WhiskResult invoke(int threadID, String method, String namesp, String act, String opts, String payload) throws Exception {
		
		StringBuffer target  = new StringBuffer().append((String) conninfo.get("API_HOST")).append("/namespaces/").append(namesp);
		if (act !=null && act.trim().length()>0)
			target.append("/actions/").append(act);
		if (opts != null && opts.trim().length()>0) {
			target.append("?");
			target.append(opts);
		}

		WhiskResult result = null;		
		boolean retryInd = false;
		int retryCount = 0;
		int retcode = 0;	
		do {
			//System.out.println("@@@invoking - count="+retryCount+ "method:"+method);
			// step 3; use the openwhisk credential to issue invoke openwhisk action
			if (method!=null&&method.equalsIgnoreCase("POST")) {
				//System.out.println("@@@invoking["+threadID+"] - count="+retryCount+ "method:"+method+" payload:"+payload+"tgt:"+target.toString());
				result = postOpenWhiskAction(target.toString(), payload);
			} else
				result = getOpenWhiskAction(target.toString());		
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

	
	
	public static WhiskCredential getOpenWhskAccessToken(String whiskAuthServer, String bluemixAccessString) throws Exception {
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return cred;
	} 

	// curl --verbose --tlsv1.2 --trace-time --request GET --header "Content-Type: application/json" 
	// --user "fd0a3087-f2ff-4cb5-9cae-292c27828996:botyDGPPhElzKsH12k0eCy5hn6dT3gpBfLwYvyGxupSU1h9zHH87MI1OZxqqzPh2" --basic https://openwhisk.stage0.ng.bluemix.net/api/v1/namespaces	
	public WhiskResult postOpenWhiskAction(String base,  String payload) throws Exception {

		//System.out.println("Issuing OpenWhisk POST command... ");
		//System.out.println("base="+base);
		//System.out.println("payload="+payload);
		
		//String base = bluemixHost + "/namespaces/xswang@us.ibm.com_dev/actions/ctflogin?blocking=true";	
		HttpPost 	post 	  = new HttpPost(base);

		HttpResponse response = null;
		String result = "";
		int code = 0;
		try {	
			
			post.addHeader("Content-Type", "application/json");			
			post.setEntity(new StringEntity(payload));
			post.addHeader(OAuthConstants.AUTHORIZATION, getBasicAuthorizationHeader(this.cred.getUuid(), this.cred.getKey()));
			
			//execute the HTTP call
			response = this.cclient.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		      String line = "";
		      while ((line = rd.readLine()) != null) {
		        result += line;
		      }
			code = response.getStatusLine().getStatusCode();
			if (code == OAuthConstants.HTTP_UNAUTHORIZED) {
				System.out.println("Authorization fails.");
			}			
			//System.out.println("@@@ action response:"+response);			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (new WhiskResult(code, result, 0));

	}

	// curl --verbose --tlsv1.2 --trace-time --request GET --header "Content-Type: application/json" 
	// --user "fd0a3087-f2ff-4cb5-9cae-292c27828996:botyDGPPhElzKsH12k0eCy5hn6dT3gpBfLwYvyGxupSU1h9zHH87MI1OZxqqzPh2" --basic https://openwhisk.stage0.ng.bluemix.net/api/v1/namespaces	
	public WhiskResult getOpenWhiskAction(String base) throws Exception {

		//System.out.println("Issuing OpenWhisk GET command... ");
		//System.out.println("base="+base);
		
		//String base = bluemixHost + "/namespaces/xswang@us.ibm.com_dev/actions/";	
		HttpGet get 	  = new HttpGet(base);

		HttpResponse response = null;
		String result = "";
		int code = 0;
		try {
			
			get.addHeader("Content-Type", "application/json");		
			get.addHeader(OAuthConstants.AUTHORIZATION, getBasicAuthorizationHeader(this.cred.getUuid(), this.cred.getKey()));
						
			response = this.cclient.execute(get);			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		      String line = "";
		      while ((line = rd.readLine()) != null) {
		        result += line;
		      }
		     code = response.getStatusLine().getStatusCode();
			if (code == OAuthConstants.HTTP_UNAUTHORIZED) {
				System.out.println("Authorization fails.");
			}			
			//System.out.println("### action response:"+response);
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return (new WhiskResult(code, result, 0));

	}
	
	

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

			System.out.println("********** Response Receieved **********");
			parseXMLDoc(null, doc, oauthResponse);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Exception occurred while parsing XML response");
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
