<%@page import=	"com.ibm.cpo.fload.engine.FLoadEngine,java.util.Calendar" %>



<%
	// Obtain the engine and run status
	FLoadEngine engine = (FLoadEngine)request.getAttribute("loadEngine");
	int engineState = engine.getState();
	long runDuration = engine.getRunDuration()/1000L;
	
	// fetch 'polling' parameter from the request
	boolean polling = false;
	String parm = (String)request.getParameter("polling");
	if( (parm!=null) && parm.length()>0 && parm.charAt(0)=='t' )
	{ // polling parm is set to 'true'
		polling = true;
	}
%>

<html>
<head>

<!-- CSS goes in the document HEAD or added to your external stylesheet -->
<link rel="stylesheet" type="text/css" href="css/table.css">	


<title>Run Status</title>
<%
	// if we are not yet polling force polling
	if(polling!=true)
	{ // we have not yet entered polling mode, we force polling mode via a
		// http redirect using the META tag below.
		// we wait 3 seconds and then enter polling mode to check on the 
		// run's status
%>
<META http-equiv="refresh" content="3;URL=CmdServlet?action=status&polling=true"> 
<%	
	}
%>
</head>
<body>

<!-- create the DOM node for the chart -->

<%
	// check the state of the run
	if(engineState == FLoadEngine.RUNNING)
	{ // engine is running
		
		long threadCount = engine.getThreadCount();
		int pause = engine.getPause();

		engine.calcTotalWorkloadCount();
		if(polling==true)
		{ // we are polling for engine state
			float elapseTime = engine.getElapseTime();
			if(elapseTime>0)
				elapseTime = elapseTime / 1000L;	// convert to seconds
				
			//Long[][] counters = (Long[][])session.getAttribute("Counters");
			//if (counters == null)				
			//	counters = new Long[500][50];
					
			int idx = 0;
			Integer itg = (Integer)session.getAttribute("PullCount");
			if (itg != null) {
				idx = itg.intValue();
			}

			
			String rpsDataString = "";
			rpsDataString = (String)session.getAttribute("rpsData");
			//System.out.println("rpsData from session ="+rpsDataString);
			String srpsDataString = "";
			srpsDataString = (String)session.getAttribute("srpsData");
			
			
			Float[] elapsedTimeList = (Float[])session.getAttribute("ElapsedTime");
			if (elapsedTimeList == null)				
				elapsedTimeList = new Float[500];
			elapsedTimeList[idx] = elapseTime;  
			
		
			long nowts = Calendar.getInstance().getTime().getTime();
			//counters[idx][0] = nowts;   // current time for ref
					
			long sessionCount = engine.getSessionCount();
			//counters[idx][1] = new Long(sessionCount);	

			long succRequest = engine.getSuccRequestCount();
			long retryRequest = engine.getRetryCount();
			
			//counters[idx][2] = new Long(succRequest);
			long srps = Math.round(succRequest/elapseTime);
			if (srpsDataString == null || srpsDataString.length() == 0 || "null".equalsIgnoreCase(srpsDataString)) {
				srpsDataString = String.valueOf(srps);
			} else {
				srpsDataString = srpsDataString + "," + String.valueOf(srps);
			}
			long smaxRPS = 0;
			Long lsmaxRPS = ((Long)session.getAttribute("maxRPS"));
			if (lsmaxRPS !=null) 
				smaxRPS = lsmaxRPS.longValue();
			if (srps > smaxRPS) 
				smaxRPS = srps;

			long totalRequest = engine.getSuccRequestCount()+engine.getExpCount();
			long expRequest = engine.getExpCount();
			//counters[idx][3] = new Long(totalRequest);
			long rps = Math.round(totalRequest/elapseTime);
			if (rpsDataString == null || rpsDataString.length() == 0 || "null".equalsIgnoreCase(rpsDataString)) {
				rpsDataString = String.valueOf(rps);
			} else {
				rpsDataString = rpsDataString + "," + String.valueOf(rps);
			}
			long maxRPS = 0;
			Long lmaxRPS = ((Long)session.getAttribute("maxRPS"));
			if (lmaxRPS !=null) 
				maxRPS = lmaxRPS.longValue();
			if (rps > maxRPS) 
				maxRPS = rps;
	
			
			// Exception counts
			long readACount = 0;
			long readUCount = 0;
			long writeACount = 0;
			long writeUCount = 0;
			//long expConnTimedOut = engine.getCntConnTimedOut();
			//counters[idx][4] = expConnTimedOut;
			//long expSocketTimedOut = engine.getCntConnSocketTimedOut();
			//counters[idx][5] = expSocketTimedOut;

			
			idx++;   // bump up idx for the next poll
			//System.out.println("idx="+idx+"Counters:");
			//for (int xi = 0;xi<idx;xi++)  { 
				//System.out.println("Page views:"+ 
				//counters[xi][1].longValue() + "," + 
				//counters[xi][20].longValue() + "," + 
				//counters[xi][23].longValue() + "," + 
				//counters[xi][24].longValue() + "," + 
				//counters[xi][25].longValue() + "," + 
				//counters[xi][26].longValue() + "," + 
				//counters[xi][22].longValue());
			//} 
			//session.setAttribute("Counters", counters);
			session.setAttribute("ElapsedTime",elapsedTimeList);
			session.setAttribute("PullCount", new Integer(idx));
			session.setAttribute("rpsData", rpsDataString);
			session.setAttribute("maxRPS", new Long(maxRPS));
			session.setAttribute("smaxRPS", new Long(smaxRPS));
				
	
%>
<script>
var thd = "<%= threadCount %>";
</script>

	<h1><font color=blue>Run Status</font></h1>
	<h2><font color=blue>Duration: <%=runDuration%> seconds - Thread: <%= threadCount %> - Pause Between Workloads: <%=pause %> milliseconds</font></h2>

	<hr>
	<table>
		<tr>
		<td><h2><font color=green>Current Run Status:</font>&nbsp;</h2></td>
		<td><form action="CmdServlet"><input type="submit" value="Halt"><input type="hidden" name="action" value="halt"></form></td>
		</tr>
	</table>

	<table>
	<tr><td>
		<h3>Statistics:</h3>
		<table class="gridtable">
		<tr>
		<th>Elapsed Time (second)</th>
		<th>Total Requests Processed</th>
		<th>Requests Per Second</th>
		<th>Successful Requests</th>
		<th>Successful Requests Per Second (RPS)</th>
		<th>Failed Requests</th>
		<th>Retries</th>
		</tr>
		<tr>
		<td width="16"><font color="blue"><%=elapseTime%></font></td>
		<td width="14"><%=totalRequest%></td>
		<td width="28"><%=rps%><br><strong>(MAX:<%= smaxRPS %>)</strong></td>
		<td width="14"><font color="green"><%=succRequest%></font></td>
		<td	width="28"><font color="green"><%=srps%><br><strong>(MAX:<%= maxRPS %>)</strong></font></td>
		<td	width="28"><%=expRequest%><br></td>
		<td	width="28"><%=retryRequest%><br></td>
		</tr>
		</table>
		<br />


	
	</td>
	<td> &nbsp;
	</td>
	</tr>
	
	
	</table>

	
	<hr>

<%		
			// Set refresh, autoload time as 3 seconds
			// this forces the page to auto refresh every 3 seconds.
			response.setIntHeader("Refresh", 10);
		}
		else
		{ // we are not yet polling for engine state
%>
	<hr>		
	<H3><font color=green>... Please wait few seconds while we enter polling mode ...</font></H3>
	<h3><A href=CmdServlet?action=status&polling=true>Click here to start polling now</A></h3>
	<hr>
<%
		}
	}
	else
	{
%>	
		<p>Engine has finished running.
<%		
	}
%>	



</body>
</html>
