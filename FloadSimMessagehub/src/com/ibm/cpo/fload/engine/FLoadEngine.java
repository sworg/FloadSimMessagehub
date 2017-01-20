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
/* 	  Salvador (Sal) Carceller  carceller@us.ibm.com      				 */
/*   Modifed version for OpenWhisk test: Feburary and October, 2016      */
/*    Sidney Wang xswang@us.ibm.com						                 */
/*                                                                       */
//************************************************************************/
// Singleton class, only 1 instance allowed!
// use com.ibm.cpo.fload.engine.FLoadEngine.getInstance() to obtain engine instance. 

package com.ibm.cpo.fload.engine;

import com.ibm.cpo.utils.CPOLogger;
import com.ibm.oauth.BluemixOath2;
import com.ibm.oauth.WhiskCredential;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Properties;

public class FLoadEngine 
{
	private static final FLoadEngine INSTANCE = new FLoadEngine();
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
	
	private int _engineState = 0;									// State of engine:
	public static final int READY = 0;								//   0=ready			
	public static final int RUNNING = 1;							//   1=running
	public static final int DONE = 2;								//   2=DONE
	
	public static long totalCount = 0;
	public static long succReqCount = 0;
	public static long totalReqCount = 0;
	public static long totalExpCount = 0;
	public static long totalRetries = 0;

	//private boolean _firstTimeInd = true;
	private Properties _engineProperties = null;
	private int _numClients = 0;
	private LoadClientThread[] _threadGroup = null;
	
	private EngineRunTimer _timer = null;
	
	private static int exceptionConnectionTimedOut = 0;
	private static int exceptionConnectionSocketTimedOut = 0;
	private long _startTime = 0;
	private long _endTime = 0;
	private long _endExecTime = 0;
	private long _totalRunTime = 0;


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
				
	public static void main(String[] args) 
	{
		CPOLogger.setDebugIndicator(true);
		CPOLogger.printDebug("FLoadEngine:main() called.");		
		
		// obtain the engine instance
		FLoadEngine engine = FLoadEngine.getInstance();
		if (engine == null) 
			engine = new FLoadEngine();
		engine.initializeEngine();
		
		engine._engineProperties = new Properties();
		engine._engineProperties.setProperty("url1", "");
		engine._engineProperties.setProperty( "numURLs", "1" );
		engine._engineProperties.setProperty( "uidStart", "1" );
		engine._engineProperties.setProperty( "uidEnd", "5" );
		engine._engineProperties.setProperty( "numClients", "5" );
			
		//engine.createThreadGroup(5);
		engine.run(1200);
		
	}
	

/*	
	public static void main(String[] args) 
	{
		CPOLogger.setDebugIndicator(true);
		CPOLogger.printDebug("FLoadEngine:main() called.");		
		
		// obtain the engine instance
		FLoadEngine engine = FLoadEngine.getInstance();
	}
*/	
	
	/* Here we are not creating Singleton instance inside getInstance() method 
	 * instead it will be created by ClassLoader. 
	 * Also private constructor makes impossible to create another instance , except one case. 
	 * You can still access private constructor by reflection and calling setAccessible(true). 
	 * By the way You can still prevent creating another instance of Singleton by this way by 
	 * throwing Exception from constructor.
	 * Read more: 
	 * http://javarevisited.blogspot.com/2012/12/how-to-create-thread-safe-singleton-in-java-example.html#ixzz34KzagVKf
	 */
	public static FLoadEngine getInstance()
	{
/*		
		// if first time engine has been referenced
		// load the default engine properties from the properties file 'engine.properties'
		if( INSTANCE._firstTimeInd == true )
		{
			INSTANCE._firstTimeInd = false;
			INSTANCE.setEngineProperties(engineProperties);
		}
*/		
        return INSTANCE;
    }

	// initializes the engine back to ready state
	public void initializeEngine()
	{
		this.halt();	// just in case engine is running halt it first 
		
		// if timer is running interrupt it and kill it
		if(this._timer != null)
		{	
			this._timer.interrupt();
//			this._timer = null;
		}
		
		this._engineState = FLoadEngine.READY;
		this._engineProperties = null;
		this._threadGroup = null;
		this._numClients = 0;
		this._startTime = 0;
		this._endTime = 0;
		this._totalRunTime = 0;
	}

	// set engine properties
	public void setEngineProperties(Properties engineProperties)
	{
		CPOLogger.printDebug("FLoadEngine:setEngineProperties(Properties) called.");
		// make a deep copy of the engine properties
		this._engineProperties = new Properties(engineProperties);	// set the properties for the engine
		this.processEngineProperties();								// process the properties
	}
	// get engine properties
	public Properties getEngineProperties()
	{
		CPOLogger.printDebug("FLoadEngine:getEngineProperties() called.");
		return(this._engineProperties);
	}
	
	public void createClientThreads(Integer numClients)
	{
		this._numClients = numClients.intValue();
		this._threadGroup = new LoadClientThread[this._numClients];
		
		// create n number of threads
		for(int ii=0; ii<this._numClients; ii++)
		{	
			LoadClientThread t1 = createClientThread(ii+1);	// create a thread
			this._threadGroup[ii] = t1;				// add the created thread to the thread group list
		}
	}
	
	/*
	 * Run the engine, start all client threads running
	 */
	public void run(long time)
	{
		int numThreads = 0;
		
		totalCount = 0;
		succReqCount = 0;
		totalReqCount = 0;
		totalRetries = 0;
		_numClients = 0;
	
		_startTime = 0;
		_endTime = 0;
		_totalRunTime = 0;

		// do we have threads ready?
		numThreads = this._threadGroup.length;
		if( numThreads > 0)
		{
			// record the current time for start of run
			this._startTime = System.currentTimeMillis();
			
			// start all threads running!
			for(int ii=0; ii<numThreads; ii++)
				this._threadGroup[ii].start();
	
			// create the timer for the run and start the timer
			// when timer expires it will invoke the timerEvent() method in this class
			this._timer = new EngineRunTimer(this, time);
			this._timer.start();	// start the timer
		
			// mark engine in RUNNING state
			this._engineState = FLoadEngine.RUNNING;
		}

	}

	/*
	 * Halt the engine, stop all client threads neatly
	 */
	public void halt()
	{
		int numThreads = 0;
		
		CPOLogger.printDebug("FLoadEngine:halt() called.");
		
		// is engine running?
		if(this._engineState == FLoadEngine.RUNNING)
		{ // engine is running, halt it	
			// do we have threads ready?
			numThreads = this._threadGroup.length;
			if( numThreads > 0)
			{
				// halt all threads
				for(int ii=0; ii<numThreads; ii++)
				{	
					this._threadGroup[ii].halt();	// tell thread to halt
				}	
			}
		
			// set engine's state to DONE
			this._engineState = FLoadEngine.DONE;
		
			// record the current time for end of run and calculate the runs elapse time
			this._endTime = System.currentTimeMillis();
			this._totalRunTime = this._endTime - this._startTime;
		}
	}
	
	// returns the state of the engine
	// Valid states:
	// 		READY = 0;											
	// 		RUNNING = 1;			
	// 		DONE = 2;				
	public int getState()
	{
		return(this._engineState);
	}
	
	// Returns how many times the Engine invoked the workload
	// only to be called after a run completes.
	public void calcTotalWorkloadCount()
	{		
		// tally up how many times each thread executed the workload
		LoadClientThread[] threads = this._threadGroup;
		int numThreads = threads.length;
		
		totalCount 	= 0;
		totalReqCount = 0; 
		succReqCount = 0; 
		totalExpCount = 0;
		totalRetries = 0;
		this._endExecTime = 0;		
		for(int ii=0; ii<numThreads; ii++) 	{
			if (threads[ii] !=null && this._endExecTime < threads[ii].getEndTimestamp()) {
				this._endExecTime = threads[ii].getEndTimestamp();
				//System.out.println("End Exec Time["+ii+"]:"+this._endExecTime);
			}
			if (threads[ii] !=null) {
				succReqCount 	+= threads[ii].getSuccessCount();	
				totalExpCount 	+= threads[ii].getFailureCount();	
				totalRetries 	+= threads[ii].getRetryCount();	
				if (threads[ii].getFailureCount() > 0) {				
				}
			} else {
				System.out.println("*** no metrics for thrads "+ii);
			}		
		}
		totalCount = succReqCount + totalExpCount;
		System.out.println("*** STATUS: {'date_time':'"+dateFormat.format(Calendar.getInstance().getTime())+"', 'total_req_issued':'" + totalCount + "', 'successful_req':'"+ succReqCount + "', exception_req':'"+totalExpCount+"', 'retry_count':'"+totalRetries+"' }");
	}
	
	public long getTotalWorkloadCount()
	{	
		return(totalCount);
	}

	public long getSuccRequestCount()
	{
		return(succReqCount);
	}

	public long getTotalRequestCount()
	{
		return(totalReqCount);
	}

	public long getExpCount()
	{
		return(totalExpCount);
	}

	public long getRetryCount()
	{
		return(totalRetries);
	}

		
	
	// returns the number of threads in the Workload EngineCounterSucc
	public int getThreadCount()
	{
		int threadCount = 0;
		
		threadCount = this._threadGroup.length;
		
		return(threadCount);
	}
	
	// After a run has finished (state=DONE) returns the total elapse time for the run
	// returns -1 if engine is Not DONE
	public long getTotalRuntime()
	{
		long totalRunTime = -1; // assume engine is not in the DONE state!
		
		if(this._engineState == FLoadEngine.DONE)
		{
			totalRunTime = this._totalRunTime;
		}
		
		return(totalRunTime);
	}

	// After all threads have completed by themselves, calculate the total time for these runs
	public long getTotalActualExectime() {
		long actualExecTime = this._endExecTime - this._startTime;		
		System.out.println("ExecTime:"+actualExecTime+" EndTime:"+this._endExecTime+" StartTime:"+this._startTime);
		return(actualExecTime);
	}

	// if engine is not running returns -1
	// if running it returns the amount of time (elapsed time) in milliseconds 
	// that the engine has been running for.
	public long getElapseTime()
	{
		long elapseTime = -1;	// assume engine is not running
		
		if(this._engineState == FLoadEngine.RUNNING)
		{
			elapseTime = System.currentTimeMillis() - this._startTime;
		}
		
		return(elapseTime);
	}
	
	public long getRunDuration()
	{
		long runDuration = -1;
		
		// if we have a timer, get the timer's time
		if(this._timer!=null)
			runDuration = this._timer.getTime();
				
		return(runDuration);
	}
	
//------------------------------------------------------------------------------	
// package scope methods start here	
//------------------------------------------------------------------------------
	// Timer thread calls this method when timer expires
	// this halts the run
	void timerEvent()
	{
		this.halt();			// halt the run, stop all threads!
		this._timer = null;		// timer is done, destroy it
	}
	
//------------------------------------------------------------------------------	
// private methods start here	
//------------------------------------------------------------------------------
	private void processEngineProperties()
	{
		String propKey = null;
		String propValue = null;
		int numURLs = 0;
				
		// obtain each url1, url2, url3, ... from the props and replace [host]:[port]
		int ii = 1;
		propKey = "url";
		while( (propValue = this._engineProperties.getProperty(propKey + ii)) != null )
		{			
			this._engineProperties.setProperty(propKey + ii, propValue);
			System.out.println("$$$ key(" + propKey+ii + ") : value(" + this._engineProperties.getProperty(propKey + ii) + ")");
			ii++;
		}
		numURLs = ii-1;		
			
		String YX = "{ \"BMIX_AUTH_SVR\" : \""+this._engineProperties.getProperty("bmix_authsvr")+"\","
				+ "\"BMIX_USER\": \""+this._engineProperties.getProperty("bmix_user")+"\","
				+ "\"BMIX_PWD\":\""+this._engineProperties.getProperty("bmix_pwd")+"\","
				+ "\"WHISK_AUTH_SRV\":\""+this._engineProperties.getProperty("wsk_authsrv")+"\","
				+ "\"API_HOST\":\""+this._engineProperties.getProperty("wsk_apihost")+"\""
				+ "}";
		
		System.out.println("YX="+YX);
		WhiskCredential  authstr = BluemixOath2.getAuth(YX);
		this._engineProperties.setProperty( "numURLs", String.valueOf(numURLs) );
		this._engineProperties.setProperty( "UUID", (String)authstr.getUuid());
		this._engineProperties.setProperty( "Key", (String)authstr.getKey() );
		//System.out.println("key(numURLs) : value(" + this._engineProperties.getProperty("numURLs") + ")");
	}
	
	private LoadClientThread createClientThread(Integer clientThreadID) {
		LoadClientThread clientThread = new LoadClientThread(clientThreadID, this.getEngineProperties());		
		return(clientThread);
	}
	
	// hide the constructor, this is a singleton object
	private FLoadEngine() 
	{
		CPOLogger.printDebug("FLoadEngine:FLoadEngine() constructor called.");		
				
		if( INSTANCE != null )
		{ // Do NOT allow constructor to be called more than once!	
			java.lang.RuntimeException e = new java.lang.RuntimeException
				("FLoadEngine() consructor should never be called! This is a singleton instance!");
			
			throw(e);
		}	
	}
	
	public long getSessionCount()
	{
		return totalCount;
	}

	public int getCntConnTimedOut() {
		return exceptionConnectionTimedOut;
	}
	
	public int getCntConnSocketTimedOut() {
		return exceptionConnectionSocketTimedOut;
	}
	
	public int getPause() {
		String pauseStr = this._engineProperties.getProperty("pause");
		if (pauseStr == null) 
			pauseStr = "0";
		return Integer.parseInt(pauseStr);
	}
	

	
}
