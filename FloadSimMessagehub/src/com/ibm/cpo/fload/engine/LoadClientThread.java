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
/*   Initial version:  October, 2014									 */
/* 	  Salvador (Sal) Carceller  carceller@us.ibm.com                     */
/*                                                                       */
/*   Modifed version for OpenWhisk test: Feburary and October, 2016      */
/*    Sidney Wang xswang@us.ibm.com						                 */
//************************************************************************/

package com.ibm.cpo.fload.engine;

import java.util.Properties;

import com.ibm.cpo.utils.CPOLogger;
import com.ibm.cpo.fload.metrics.BaseMetrics;
import com.ibm.cpo.fload.metrics.Errors;
import com.ibm.cpo.fload.workload.ClientWorkLoadIF;
import com.ibm.cpo.fload.workload.ClientWorkloadWhisk;
import com.ibm.cpo.fload.workload.WorkloadConfig;

public class LoadClientThread extends Thread
{

	//private ClientWorkloadLambda _workload;
	//private ClientWorkloadS3 _workload;
	private ClientWorkLoadIF 	_workload;
	private WorkloadConfig		_config;
	
	private int _clientThreadID;
	private long  _pauseMil = 0;
	
	private Properties _clientProperties = null;
	private long _currentUserId = 0;
	private String[] _urlList = null;
	private boolean _haltIndicator = false;
	private boolean _singleExec = false;
	//private long _workLoadCounter = 0;
	//private long _workLoadCounterSucc = 0;
	//private long _workLoadCounterRetry = 0;
	//private long _workLoadCounterTotal = 0;
	private long _uidStartID = 0;	// lowest userID to be used by the engine
	private long _uidBlockSize = 0;	// block size for generating userIDs

	
	public LoadClientThread(Integer clientThreadID, Properties clientprop)
	{
		super(clientThreadID.toString());
		
		//this._clientProperties = clientprop;
		setProperties(clientprop);
		BaseMetrics _metrics = new BaseMetrics();
		_metrics.reset();
		
		Errors _errors = new Errors();
		_errors.reset();

		this._config = new WorkloadConfig();
		this._config.setConfigObject(clientprop);
		this._config.setMetrics(_metrics);
		this._config.setErrors(_errors);
				
		//this._workload = new ClientWorkloadLambda(this._clientThreadID,null);
		//this._workload = new ClientWorkloadS3(null);
		this._workload = new ClientWorkloadWhisk(_clientThreadID, this._config);
		
		CPOLogger.printDebug("ClientThread:ClientThread(" + clientThreadID + ") constructor called. Target ="+clientThreadID.toString());	
		
		this._clientThreadID = clientThreadID.intValue();
		System.out.println("*** Creating thread.  ThreadID:=" + this._clientThreadID);		
	}
	
	public void run()
	{
		callWhisk();
	}
	
	private void callWhisk() {
		CPOLogger.printDebug("*** ClientThread[" + this._clientThreadID + "] initiated...");
		//this._workLoadCounter = 0;	// set the workload counter to Zero
		//this._workLoadCounterSucc = 0;
		//this._workLoadCounterRetry = 0;
		//this._workLoadCounterTotal = 0;
		
		// just keep calling the workload till told to halt
		this._haltIndicator = false;
		int count=0;
		while( this._haltIndicator == false && count < 1000000) {		
			count++;
	        try {
	            sleep(this._pauseMil);
	        } catch (Exception e) {}
        	//System.out.println("waiting done after pause for "+this._pauseMil+" millisec. Single:"+this._singleExec);
			String payload = this._workload.runWorkload(this._clientThreadID);			
			if (this._singleExec) { 
				break;
			}
			
		}			
		this._config.getMetrics().setEndTimestamp(System.currentTimeMillis());
		System.out.println("*** ClientThread ["+this._clientThreadID+"] with pause of "+this._pauseMil+" milliseconds has completed at "+this._config.getMetrics().getEndTimestamp());
		this._config.getMetrics().printResult(this._clientThreadID);
		this._config.getErrors().printError(this._clientThreadID);
		this._config.getMetrics().printRetryDist(this._clientThreadID);
		return;
	}

	
	
	public void halt()
	{
		CPOLogger.printDebug("ClientThread:halt(" + this._clientThreadID + ") called.");
		
		this._haltIndicator = true;
	}	
	
	public void setSingleExec() {
		CPOLogger.printDebug("ClientThread:single(" + this._clientThreadID + ") called.");
		this._singleExec = true;
	}	
	
	
	//public long getWorkloadCounter()
	//{
	//	return(this._workLoadCounter);
	//}
	/*		
	public long getRetryReqCounter()
	{
		return(this._workLoadCounterRetry);
	}
	
	public long getSuccReqCounter()
	{
		return(this._workLoadCounterSucc);
	}
		
	public long getTotalReqCounter()
	{
		return(this._workLoadCounterTotal);
	}
		
	*/
	
	
	public void setProperties(Properties props)
	{
		CPOLogger.printDebug("ClientThread:setProperties(Properties) called.");

		// make a deep copy (clone) of the properties, each thread needs it own dedicated properties. 
		this._clientProperties = new Properties(props);
		this.processProperties();	// process the properties for this thread
	}
	
	public long generateUniqueUserID()
	{		
		long blockSize = this._uidBlockSize;					// number of userIDs possible for this thread
		long blockNumber = this._clientThreadID;	// clientID block number for this thread
		//userID = this._rad.nextLong(); // generate a random long		
		this._currentUserId++; // bump up relative uid by 1 instead of random to reduce chance of collision
		if (this._currentUserId >= blockSize) 
			this._currentUserId = 1;  // wrap back to 1
		long absNewUserID = (blockNumber-1)*blockSize+this._currentUserId;		
		absNewUserID += this._uidStartID-1; 
		//if (this._clientThreadID==1) 
		//	System.out.println("************> blocksize:"+blockSize+" >> Thread:"+this._clientThreadID+"  >> UID="+absNewUserID);
		return(absNewUserID);
	}
	
//------------------------------------------------------------------------------	
// private methods start here	
//------------------------------------------------------------------------------
	// process properties does things like:
	// - calculate unique user ID range forthe given thread. 
	//   Each thread has a block of userids it may use during login.
	// - Populate the this._urlList array of URLs to be called.
	//   We want an array of urls for extremely fast performance. 
	private void processProperties()
	{
		// setup the unique userID generator for this client thread 
		this.setupUserIdGenerator();

		// build the array list of URLs to be called
		this.buildUrlListArray();
		
	}
	
	private void buildUrlListArray()
	{
		String propKey = null;
		String propValue = null;
		int numURLs = 0;
		
		// obtain the total number of urls to be called. 
		propValue = this._clientProperties.getProperty("numURLs");
		numURLs = Integer.parseInt(propValue);

		String pause = this._clientProperties.getProperty("pause");
		if (pause == null) 
			pause = "10";
		this._pauseMil = Integer.parseInt(pause);

		// signaling that each thread only execute one time
		String singleExec = this._clientProperties.getProperty("single");
		if (singleExec != null && singleExec.equalsIgnoreCase("yes")) 
			this.setSingleExec();
		
		// intatiate the array list of urls
		this._urlList = new String[numURLs];
		
		propKey = "url";
		for( int ii=1; ii<=numURLs; ii++ )
		{
			propValue = this._clientProperties.getProperty(propKey + ii);
			if (propValue.indexOf("userLogon")>0)
				propValue +="&userid=";
			//System.out.println("setting URLs: key(" + propKey+ii + ") : value(" + propValue + ")");
			// add each url to the array of urls
			this._urlList[ii-1] = propValue;
		}
	}
	
	private void setupUserIdGenerator()
	{
		String propValue = null;
		long uidStart = 0;
		long uidEnd = 0;
		int numClients = 0;
		long uidTotalRange = 0;

		propValue = this._clientProperties.getProperty("uidStart");
		uidStart = Long.valueOf(propValue);
		this._uidStartID = uidStart; 
		
		propValue = this._clientProperties.getProperty("uidEnd");
		uidEnd = Long.valueOf(propValue);

		propValue = this._clientProperties.getProperty("numClients");
		numClients = Integer.valueOf(propValue);
		
		uidTotalRange = uidEnd-uidStart+1;
		
		this._uidBlockSize = uidTotalRange/numClients;
	}

	public long getEndTimestamp() {
		if (this._config != null && this._config.getMetrics() != null) {
			return this._config.getMetrics().getEndTimestamp();
		}
		return 0;
	}
	
	public long getSuccessCount() {
		if (this._config != null && this._config.getMetrics() != null) {
			return this._config.getMetrics().getReadAcount();
		}
		return 0;
	}
	
	public long getFailureCount() {
		if (this._config != null && this._config.getErrors() != null) {
			return this._config.getErrors().getExpCount();
		}
		return 0;
	}
	
	public long getRetryCount() {
		if (this._config != null && this._config.getErrors() != null) {
			return this._config.getMetrics().getRetryCount();
		}
		return 0;
	}
	
}
