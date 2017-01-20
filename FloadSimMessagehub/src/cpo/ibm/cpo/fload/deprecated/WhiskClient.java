/*
//************************************************************************/
/*   begin_generated_IBM_copyright_prolog                                */
/*   This is an automatically generated copyright prolog.                */ 
/*   After initializing,  DO NOT MODIFY OR MOVE                          */
/*   ------------------------------------------------------------------  */ 
/*   IBM Confidential                                                    */
/*                                                                       */
/*   OCO Source Materials                                                */
/*                                                                       */
/*   Product(s):                                                         */
/*      IBM CPO Performance Test Load Simulator                          */
/*      0000-0000                                                        */
/*                                                                       */
/*   (C)Copyright IBM Corp. 2014, 2015, 2016                             */
/*                                                                       */
/*   The source code for this program is not published or otherwise      */
/*   divested of its trade secrets, irrespective of what has been        */
/*   deposited with the US Copyright Office.                             */
/*   ------------------------------------------------------------------  */
/*                                                                       */
/*   end_generated_IBM_copyright_prolog                                  */
/*   ==============================================================      */
/*                                                                       */
/*   Initial version:  October, 2016									 */
/* 	  xswang@us.ibm.com                     */
/*                                                                       */
//************************************************************************/


package cpo.ibm.cpo.fload.deprecated;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;

import com.ibm.cpo.fload.workload.WhiskResult;
import com.ibm.oauth.BluemixOath2;


public class WhiskClient {

	public static final int RETRY_LIMIT = 99;
	public static final int SUCCESS = 0;
	public static final int EXP_BAD_URL = -1;
	public static final int EXP_CONN_ERROR = -2;
	public static final int EXP_SERVICE = -7;
	public static final int EXP_SERVICE_ERROR = -8;
	public static final int EXP_UNSUPPORTED_MEDIA_TYPE= -9;
	public static final int EXP_TOO_MANY_REQUESTS= -10;

	public static final String HTTP_METHOD_GET = "GET";
	public static final String HTTP_METHOD_PUT = "PUT";
	public static final String HTTP_METHOD_POST = "POST";

	public static final String WHISK_CMD_NAMESPACE = "/namespaces";
	public static final String WHISK_CMD_ACTION = "/action";
	public static final String WHISK_CMD_ACTIVATION = "/activation";
	public static final String WHISK_CMD_PACKAGE = "/package";
	public static final String WHISK_CMD_TRIGGER = "/trigger";
	public static final String WHISK_CMD_RULE = "/rule";
	public static final String WHISK_CMD_STATE = "/namespace";
	public static final String WHISK_CMD_LOGS= "/namespace";

	
	
	
	public static final int EXP_OTHER = -99;
	
	
	private String action = "";
	private String parameters="";
	private String apihost= "https://openwhisk.ng.bluemix.net/api/v1";
	private String payload = "";
	
	
	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getApihost() {
		return apihost;
	}

	public void setApihost(String apihost) {
		this.apihost = apihost;
	}

	public String getAuthStringEnc() {
		return authStringEnc;
	}

	public void setAuthStringEnc(String authStringEnc) {
		this.authStringEnc = authStringEnc;
	}


	// Define endpoint path
	//public String apiBaseUrl="https://openwhisk.ng.bluemix.net/api/v1";
	//public String authString = "4e72033c-cd36-4308-844d-1823f3dc45b6:q1G6qXkZYg53dHXiXQsm0FW2WMHDF5Yr5e4kAxF2lOcMLuLhAwaEl660DEa55n6E";
	public String apiBaseUrl="";
	public String authString = "";
	
	private String authStringEnc = null;
	
	private String _urlString = null;

	
	public class WskCmdOption {
		public int retcode;
		public String result;
		public int retrycnt;
		public WskCmdOption(int rcd, String rslt, int rcnt) {
			this.retcode = rcd;
			this.retrycnt = rcnt;
			this.result = rslt;
		}
	}

			
	public WhiskClient(String baseURL, String userid, String passwd) {
		this.apiBaseUrl = baseURL;
		this.authString = userid +":"+passwd;
		System.out.println("Auth by uid/pwd:"+this.authString);
		byte[] authEncBytes = Base64.encodeBase64(this.authString.getBytes());
		this.authStringEnc = new String(authEncBytes);
	}

	public WhiskClient(String baseURL, String authStr) {
		this.apiBaseUrl = baseURL;
		this.authString = authStr;
		byte[] authEncBytes = Base64.encodeBase64(this.authString.getBytes());
		this.authStringEnc = new String(authEncBytes);
	}

	//* GET /namespaces
	public WhiskResult getAllNamespaces() {
		return new WhiskResult(0, "", 0);		
	}
	
	//* GET /namespaces/{namespace}
	public WhiskResult getAllEntitiesInNamespace(String namespace) {
		return new WhiskResult(0, "", 0);		
	}
	
	//* GET /namespaces/{namespace}/actions
	public WhiskResult getAllActions(String namespace) {
		
		return new WhiskResult(0, "", 0);
	}
	
	
	//* DELETE /namespaces/{namespace}/actions/{actionName}
	public WhiskResult deleteActions(String namespace, String actionname) {
		
		return new WhiskResult(0, "", 0);
	}
	
	//* GET /namespaces/{namespace}/actions
	public WhiskResult getActionByName(String namespace, String actionname) {
		
		return new WhiskResult(0, "", 0);
	}
	
	//* POST /namespaces/{namespace}/actions/{actionName}
	public WhiskResult invokeAction(String namespace, String actionname, WskCmdOption opt) {
		
		return new WhiskResult(0, "", 0);
	}
	
	//* PUT /namespaces/{namespace}/actions/{actionName}
	public WhiskResult updateAction(String namespace, String actionname, WskCmdOption opt) {
			
		return new WhiskResult(0, "", 0);
	}
	
	
	//GET /namespaces/{namespace}/activations
	public WhiskResult getActivations(String namespace) {
		
		return new WhiskResult(0, "", 0);
	}
	

	//GET /namespaces/{namespace}/activations/{activationid}
	public WhiskResult getActivation(String namespace, String activationid, WskCmdOption opt) {
		
		return new WhiskResult(0, "", 0);
	}
	
	
	//GET /namespaces/{namespace}/packages
	public WhiskResult getAllPackages(String namespace) {
		
		return new WhiskResult(0, "", 0);
	}
	
	

	
	public WhiskResult sendReq(String namespace, String cmd, String httpMethod, String detail, String payload) {
		
		int retcode = EXP_OTHER;

		// Make a REST call
		String urlstr = apiBaseUrl+namespace+cmd;
		if (detail != null && detail.trim().length()>0) {
			urlstr = urlstr + detail;
		}
		
		String output = "";
		boolean retryInd = false;
		int retryCount = 0;
		
		try {

			System.out.println("Request:"+urlstr);
			URL url = new URL(urlstr);
			do {
				// Make a REST call			
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Authorization", "Basic " + this.authStringEnc);
				conn.setDoOutput(true);
				conn.setRequestMethod(httpMethod);
				conn.setRequestProperty("Content-Type", "application/json");
				
				System.out.println("Payload="+payload);
				OutputStream os = conn.getOutputStream();
				os.write(payload.getBytes());
				os.flush();	
				//System.out.println("Response:"+conn.getResponseMessage());
				if (conn.getResponseCode() < 300) {
					//System.out.println("Response:"+conn.getResponseMessage());
					BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
					//System.out.println("Output from Server .... \n");
					String out = "";
					while ((out = br.readLine()) != null) {
						output += out;
						//System.out.println(output);
					}				
					retcode = SUCCESS;	
					
				} else if (conn.getResponseCode() == 429) {
					retryInd = true;
					retryCount++;
					System.out.println("Response 429:"+conn.getResponseMessage());
				} else if (conn.getResponseCode() >= 400 &&conn.getResponseCode() < 500) {
					retcode = EXP_SERVICE;
					output = conn.getResponseMessage();				
				} else if (conn.getResponseCode() >= 500) {
					retcode = EXP_SERVICE_ERROR;
					output = conn.getResponseMessage();				
				}
				conn.disconnect();
			} while (retryInd && retryCount<RETRY_LIMIT);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (new WhiskResult(retcode, output, retryCount));
	}
	
	
	public WhiskResult invoke(String namesp, String act, String method, String opts, String payload) {
		String actionpath = "/actions/"+act;
		String namespace = "/namespaces/"+namesp;
		String options = "?"+opts;   //e.g. blocking=true
		return sendReq(namespace, actionpath, method, options, payload);		
	}
	
	
	
	// used for unit testing this class
	public static void main(String[] args) {
		
		System.out.println("From main.........");		
		try {
			
			String auth = "4e72033c-cd36-4308-844d-1823f3dc45b6:q1G6qXkZYg53dHXiXQsm0FW2WMHDF5Yr5e4kAxF2lOcMLuLhAwaEl660DEa55n6E";
			String uuid = "4e72033c-cd36-4308-844d-1823f3dc45b6";
			String key = "q1G6qXkZYg53dHXiXQsm0FW2WMHDF5Yr5e4kAxF2lOcMLuLhAwaEl660DEa55n6E";			
			String base ="https://api.stage0.ng.bluemix.net";
			String input = "{\"userid\": \"aaa\", \"password\": \"passw0rd\", \"remark\": \"none\"}";

			WhiskClient wc = new WhiskClient(base, auth);	
			
			//wc.send(NAME_SPACE, "/actions", HTTP_METHOD_GET, "");
			//wc.send(NAME_SPACE, "/packages", HTTP_METHOD_GET, "");
			//wc.send("/namespaces/xswang@us.ibm.com", "/actions", HTTP_METHOD_GET, "");
			//wc.send("/namespaces/xswang@us.ibm.com", "/actions/hello", HTTP_METHOD_POST, "");
			//wc.post("/namespaces/xswang@us.ibm.com_dev", "/actions/hellodemo", HTTP_METHOD_POST, "?blocking=true", input);
			//WhiskResult result = wc.post("/namespaces/xswang@us.ibm.com", "/actions/ctflogin", HTTP_METHOD_POST, "", "");
			WhiskResult result = wc.sendReq("/namespaces/xswang@us.ibm.com_dev", "/actions/ctflogin?blocking=true", HTTP_METHOD_POST, "", input);
			System.out.println("Results:"+result.retcode+" Output:"+result.result+" Retry:"+result.retrycnt);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	
	
	
	public void createService() {
		return;
	}
	

	// returns the url string for the request
	public String getUrlString() {
		return (this._urlString);
	}


}
