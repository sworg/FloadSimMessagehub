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
//************************************************************************/


package com.ibm.cpo.fload;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ibm.cpo.fload.engine.FLoadEngine;
import com.ibm.cpo.utils.CPOLogger;
/*
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
*/

/**
 * Servlet implementation class FLoadCmdServlet
 */
@WebServlet("/CmdServlet")
public class CmdServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private Boolean _firstTimeInd = true;
	private static final String _propFileName = "/engine.properties";
	private Properties _engineProperties = null;

    /**
     * Default constructor. 
     */
    public CmdServlet() 
    {
    	super();
		//CPOLogger.setDebugIndicator(true);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		this.processCmd(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		this.processCmd(request, response);
	}

	private void processCmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String action = null;
		char cmdInd = '0';
		
/*		
		//get all headerrs
		System.out.println("\n- - - Request Headers");			
		Enumeration<String> names = request.getHeaderNames();
		do
		{
			String attrName = names.nextElement();
			System.out.println(attrName + ":" + request.getHeader(attrName));			
		}
		while( names.hasMoreElements() );
*/		
		
		// if this is first time, process first time code
		if( this._firstTimeInd == true )
			this.firstTimeProcessing();
		
		// valid actions are 'action=xx':
		//  'p' "properties"	manage properties from the engine.properties file
		// 	'i' "initialize"	re-initialize the engine
		// 	'r' "run"			run the engine
		//  's' "status"		check the run's status
		// 	'h' "halt"			halt/stop the run
				
		// fetch 'action' parameter from the request
		action = (String)request.getParameter("action");
		if(action!=null)
		{
			action.toLowerCase();
			
			cmdInd = action.charAt(0);
			switch(cmdInd)
			{
				case 'r':	// run the test
					this.processRunCmd(request, response);
				break;

				case 's':	// status, check the run's status
					this.processStatusCmd(request, response);
				break;

				case 'h':	// halt the test
					this.processHaltCmd(request, response);
				break;

				case 'i':	// re-initialize engine
				break;

				case 'p':	// load properties
					this.processPropsCmd(request, response);
				break;

				default:	// action not valid
					response.getWriter().println("ERROR: '?action=" + action + "' not a valid action!");
				break;	
			}
		}
		else
		{ // ERROR: action=xx parameter not set
			response.getWriter().println("ERROR: '?action=' not specified!");
		}
	}

	// process the manage properties 'action=props'
	// Valid operations '&oper=s' are:
	// 	's'	show 	shows the current properties
	//	'u'	update 	updates the give key:val pair
	//  'a' add		adds the given key:val pair
	//	'd'	delete	deletes the given key:val pair
	//  'r' reload	reloads the engine props from engine.props file
	//
	private void processPropsCmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String parm = null;
		char operInd = 's'; // assume show properties
		String key = null;
		String value = null;
		
		// fetch operation 'oper' parameter from the request
		parm = (String)request.getParameter("oper");
		if( (parm!=null) && parm.length()>0 )
		{
			// fetch key and value parms
			key = (String)request.getParameter("key");
			value = (String)request.getParameter("value");
			//System.out.println(key + ":" + value);			
			
			parm.toLowerCase();
			operInd = parm.charAt(0);
			switch(operInd)
			{
				case 's':	// display properties operation
					// does nothing special, just displays current engine properties
				break;	
			
				case 'u':	// update property operation
					if(value.length()>0)
					{	
						// re-put the key:val into the properties, does update
						this._engineProperties.put(key, value);
					}
					else
					{ // value is blank, delete the item
						this._engineProperties.remove(key);
					}
				break;
				
				case 'a':	// add property operation
					// re-put the key:val into the properties, does update
					this._engineProperties.put(key, value);
				break;
				
				case 'd':	// delete property operation
					this._engineProperties.remove(key);
				break;
				
				case 'r':
					// reload the Engine Properties from the engine.properties file
					this._engineProperties = this.loadEngineProperties(CmdServlet._propFileName);
				break;
				
				default:
					// do nothing, will just display current engine properties
				break;	
			}
		}
		
		// pass the FLoadEngine instance to the JSP
		request.setAttribute( "EngineProperties", this._engineProperties );
		// Dispatch the JSP
		RequestDispatcher RequetsDispatcherObj = request.getRequestDispatcher("DisplayProperties.jsp");
		RequetsDispatcherObj.forward(request, response);
	}
	
	private void processRunCmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		int threadCount = 1;
		long runDuration = 60;
		
		CPOLogger.printDebug("CmdServlet:processCmd(request, response) called.");	
		
		threadCount = Integer.parseInt( this._engineProperties.getProperty("numClients") );
		FLoadEngine engine = FLoadEngine.getInstance();		// obtain the load engine instance
		engine.initializeEngine();							// set engine to READY mode
		engine.setEngineProperties(this._engineProperties);	// refresh the engine's properties list
		engine.createClientThreads(threadCount);			// create n number of threads in the engine
			
		// get the run duration in seconds from the properties file
		runDuration = Long.parseLong( this._engineProperties.getProperty("runDuration") );
		engine.run(runDuration*1000L);				// run the threads for given time
		
		// pass properties to the JSP
		request.setAttribute( "loadEngine", FLoadEngine.getInstance() );
		
		RequestDispatcher RequetsDispatcherObj = request.getRequestDispatcher("RunStatus.jsp");
		RequetsDispatcherObj.forward(request, response);
	}
	
	private void processStatusCmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// assume the engine is running and dispatch the RunStats.jsp
		String jspPage = "RunStatus.jsp";
		
		// pass properties to the JSP
		request.setAttribute( "loadEngine", FLoadEngine.getInstance() );
		
		FLoadEngine engine = FLoadEngine.getInstance();
		if( engine.getState() != FLoadEngine.RUNNING  )
		{ // engine is not running, dispatch the RunResults.jsp
			request.setAttribute( "totalRuntime", engine.getTotalRuntime() );

			jspPage = "RunResults.jsp";
		}
		
		// Dispatch the JSP
		RequestDispatcher RequetsDispatcherObj = request.getRequestDispatcher(jspPage);
		RequetsDispatcherObj.forward(request, response);
	}

	private void processHaltCmd(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// JSP page to be invoked by this command RunResults.jsp
		String jspPage = "RunResults.jsp";
		
		FLoadEngine engine = FLoadEngine.getInstance();
		engine.halt();
		
		// pass properties to the JSP
		request.setAttribute( "loadEngine", FLoadEngine.getInstance() );
	
		// Dispatch the JSP
		RequestDispatcher RequetsDispatcherObj = request.getRequestDispatcher(jspPage);
		RequetsDispatcherObj.forward(request, response);
	}
	
	private Properties loadEngineProperties(String propFileName)
	{
		CPOLogger.printDebug("CmdServlet:loadEngineProperties(" + propFileName + ") called.");		

		Properties engineProps = new Properties();
		InputStream input = null;
	 
		try 
		{
			ServletContext context = getServletContext();
		    input = context.getResourceAsStream(propFileName);
		    
	 		//input = new FileInputStream(propFileName);
			//input = this.getClass().getResourceAsStream(propFileName);
			if(input != null )
			{	
	 			// load a properties file
				engineProps.load(input);
	 
				// get the property value and print it out
				CPOLogger.printDebug(engineProps.getProperty("host"));
				CPOLogger.printDebug(engineProps.getProperty("port"));
			}
			else
			{
				CPOLogger.printError( propFileName + " file not found.");
			}
	 	} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
		} 
		finally 
		{
			if (input != null) 
			{
				try 
				{
					input.close();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}
		
		return(engineProps);
	}
	
	// This method processes servlet code that only needs to be done once.
	// - Load engine properties from 'engine.properties' file located in the WAR
	private void firstTimeProcessing()
	{
		// thread safety! Only one thread at a time looking at and changing _firstTimeInd!
		synchronized(this._firstTimeInd)
		{
			if(this._firstTimeInd == true )
			{	
				this._firstTimeInd = false;
				this._engineProperties = this.loadEngineProperties(_propFileName);
				FLoadEngine engine = FLoadEngine.getInstance();
				engine.setEngineProperties(this._engineProperties);
			}
		}
	}	
}
