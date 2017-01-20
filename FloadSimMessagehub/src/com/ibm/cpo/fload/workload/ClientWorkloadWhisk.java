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
/*   Initial version:  Feburary, 2016									 */
/* 	  xswang@us.ibm.com              							         */
/*   Modifed version for OpenWhisk test: Feburary and October, 2016      */
/*    Sidney Wang xswang@us.ibm.com						                 */
/*                                                                       */
//************************************************************************/

package com.ibm.cpo.fload.workload;

import java.util.Calendar;

import com.ibm.cpo.fload.engine.LoadClientThread;
import com.ibm.cpo.fload.metrics.BaseMetrics;
import com.ibm.cpo.fload.metrics.Errors;
import com.ibm.oauth.BluemixOath2;
import com.ibm.oauth.WhiskCredential;

import cpo.ibm.cpo.fload.deprecated.WhiskClient;

import java.util.Properties;
import org.json.*;


public class ClientWorkloadWhisk implements ClientWorkLoadIF {

	private LoadClientThread _parent = null;
	private WhiskClient client = null;
	private long _clientID = 0;
	private WorkloadConfig _config;
	private Properties _prop;
	private BluemixOath2 bmixoath = null;
	
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

	// test harness, used only for unit testing
	public static void main(String[] args) 
	{
		
		String base ="https://openwhisk.ng.bluemix.net/api/v1";
		String auth = "4e72033c-cd36-4308-844d-1823f3dc45b6:q1G6qXkZYg53dHXiXQsm0FW2WMHDF5Yr5e4kAxF2lOcMLuLhAwaEl660DEa55n6E";
		String wsknamespace = "xswang@us.ibm.com_dev";

		
		Properties prop = new Properties();
		prop.setProperty("authstring",auth);
		prop.setProperty("apihost",base);
		prop.setProperty("wsknamespace",wsknamespace);
		prop.setProperty("action","ctflogin");
		prop.setProperty("method","POST");
		prop.setProperty("options","blocking=true");
		prop.setProperty("payload","{\"userid\": \"aaa\", \"password\": \"passw0rd\", \"remark\": \"none\"}");

		BaseMetrics met = new BaseMetrics();
		met.reset();
		Errors err = new Errors();
		err.reset();
		
		WorkloadConfig config = new WorkloadConfig();
		config.setMetrics(met);
		config.setErrors(err);		
		config.setConfigObject(prop);

		ClientWorkloadWhisk wl = new ClientWorkloadWhisk(18, config);
		wl.runWorkload(1);
		
		met.printResult(98);
		
	}
		
	
	public ClientWorkloadWhisk(long clientID, WorkloadConfig config) 
	{
		super();	
		
		this._parent = null;
		this._clientID = clientID;
		this._config = config;
		
		this._prop = (Properties)_config.getConfigObject();		
		WhiskCredential authcred = new WhiskCredential();
		authcred.setUuid(this._prop.getProperty("UUID"));
		authcred.setKey(this._prop.getProperty("Key"));
		this.bmixoath = new BluemixOath2(authcred); 
		
		//this.client = new WhiskClient(base, auth);
		//this.client.createService();
			
	}
	
	public LoadClientThread getParent()
	{
		return(this._parent);
	}
	
	public long getClientID()
	{
		return this._clientID;
	}
	
	
	public String runWorkload(int threadID) {
		
		WhiskResult result = null;
		try {
			//result =  this.bmixoath.invoke(threadID, "POST","xswang@us.ibm.com_dev", "ctflogin", "?blocking=true", "");
			result =  this.bmixoath.invoke(threadID, this._prop.getProperty("method"),this._prop.getProperty("wsk_namespace"), this._prop.getProperty("action"), this._prop.getProperty("options"), this._prop.getProperty("payload"));
		} catch(Exception e) {
			e.printStackTrace();
		}
		//result.print();		
		int status = result.retcode;
		String payload = result.result;
		int retry = result.retrycnt;
		//result.print();		
		if (status == 0) {
			this._config.getMetrics().incReadCount();
			//this._config.getMetrics().incWriteCount();
			if (retry > 0) {
				System.out.println("*** Success after "+retry+" retries.");
				this._config.getMetrics().incRetryDistCount(retry);
				this._config.getMetrics().addTotalRetryCount(retry);				
			} else {
				
			}
		} else {
			//System.out.println("### Workload error("+threadID+"):"+payload+" status:"+status+" retry:"+retry);
			long nowts = Calendar.getInstance().getTime().getTime();
			this._config.getMetrics().incRetryDistCount(retry);
			this._config.getMetrics().addTotalRetryCount(retry);				
			this._config.getErrors().addExp(payload, status, status, nowts);			
		} 
		return payload;
		
	}


	@Override
	public void setClientID(long id) {
		// TODO Auto-generated method stub
		
	}
	


	
}
