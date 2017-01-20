<%@page import="com.ibm.cpo.fload.engine.FLoadEngine,java.util.Date,java.util.Calendar,java.text.SimpleDateFormat" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<!-- CSS goes in the document HEAD or added to your external stylesheet -->
	<link rel="stylesheet" type="text/css" href="css/table.css">	
	<script>
		var JSONStr = "";
	</script>
	<title>Run Results</title>
</head>
<body>
	<h2><font color=blue>Run Results</font></h2>	
<%
// Exception counts
	// calculate all run statistics
	FLoadEngine engine = (FLoadEngine)request.getAttribute("loadEngine");
	engine.calcTotalWorkloadCount();
	long totalRunTime = engine.getTotalRuntime();
	long totalActualExecTime = engine.getTotalActualExectime();
	long threadCount = engine.getThreadCount();
	long reqInOneSession = 1;
	long throughput = Math.round((float)(engine.getTotalWorkloadCount()*1000)/totalRunTime);
	long sessionCount = engine.getSessionCount();
	long succRequest = engine.getSuccRequestCount();
	long retryRequest = engine.getRetryCount();
	long expRequest = engine.getExpCount();
	long totalRequest = succRequest + expRequest+retryRequest;
	
	long reqPerSec = (int)(((float)totalRequest/totalRunTime)*1000);
	long sucReqPerSec = (int)(((float)succRequest/totalRunTime)*1000);
	
	//long avgTimePerWLPerThread = (int)((float)1000/(float)((float)throughput/(float)threadCount));
	float avgTimePerSSN = 	 (float)(totalRunTime)/engine.getTotalWorkloadCount();
	float avgTimePerReq = 	 (float)(totalRunTime)/totalRequest;
	float avgTimePerSucReq = (float)(totalRunTime)/succRequest;

	// Exception counts
	long readACount = 0;
	long readUCount = 0;
	long writeACount = 0;
	long writeUCount = 0;
	
	
	//long writeUCount = engine.getWriteUCount();
	//long readUCount = engine.getReadUCount();
	//long writeACount = engine.getWriteACount();
	//long readACount = engine.getReadACount();
	
	String rpsDataString= (String)session.getAttribute("rpsData");
	Long lsMaxRPS = (Long)session.getAttribute("smaxRPS");
	Long lMaxRPS = (Long)session.getAttribute("smaxRPS");
				
%>

	<h3>Run Details:</h3>
	<table  class="resulttable" width="50%" border="2">
	<tr>
	<th>No. of Threads</th>
	<th>Pause Between Workloads (millisec)</th>
	<th>Elapse Time (millisec)</th>
	<th>Actual Execution Time (millisec)</th>
	<th>Session Executed</th>
	<th>Requests in one Workload</th>
	<th>Total Requests</th>
	<th>Total Successful Requests</th>
	<th>Total Retries</th>
	<th>Total Failed Requests</th>
	<th><font color=purple>Sessions Per Second</font></th>
	<th><font color=purple>Average Millisec Per Session</font></th>
	<th><font color=blue>Requests Per Second</font></th>
	<th><font color=blue>Average Millisec Per Request (succ/exp/retry)</font></th>
	<th><font color=green>Successful Requests Per Second</font></th>
	<th><font color=green>Average Millisec Per Successful Request</font></th>
	</tr>
	<tr>
	<td><%=engine.getThreadCount()%></td>
	<td><%=engine.getPause()%></td>
	<td><%=totalRunTime%></td>
	<td><%=totalActualExecTime%></td>
	<td><font color=brown><%=engine.getTotalWorkloadCount()%></font></td>
	<td><%=reqInOneSession%></td>
	<td><%=totalRequest%></td>
	<td><%=succRequest%></td>
	<td><%=retryRequest%></td>
	<td><%=expRequest%></td>
	<td><font color=purple><b><%=throughput%></b></font></td>
	<td><font color=purple><b><%=avgTimePerSSN%></b></font></td>
	<td><b><font color=blue><%=reqPerSec %></font></b> </td>
	<td><b><font color=blue><%=avgTimePerReq %></font></b> </td>
	<td><b><font color=green><%=sucReqPerSec %></font></b> </td>
	<td><b><font color=green><%=avgTimePerSucReq %></font></b> </td>
	
	</tr>
	</table>




<% 
Calendar cal = Calendar.getInstance();
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
String dts = dateFormat.format(cal.getTime());
//System.out.println("date:"+dts);

String JSONStr = "{";
JSONStr = JSONStr + "'exec_time':";
JSONStr = JSONStr + "'"+dts+"',";
JSONStr = JSONStr + "'no_of_thread':";
JSONStr = JSONStr + "'"+engine.getThreadCount()+"',";
JSONStr = JSONStr + "'wld_executed':";
JSONStr = JSONStr + "'"+engine.getTotalWorkloadCount()+"',";
JSONStr = JSONStr + "'execution_time':";
JSONStr = JSONStr +"'"+totalRunTime+"',";
JSONStr = JSONStr + "'reqs_in_one_wld':";
JSONStr = JSONStr + "'"+reqInOneSession+"',";
JSONStr = JSONStr + "'total_req':";
JSONStr = JSONStr + "'"+totalRequest+"',";
JSONStr = JSONStr + "'total_suc_req':";
JSONStr = JSONStr + "'"+succRequest+"',";
JSONStr = JSONStr + "'total_exp_req':";
JSONStr = JSONStr + "'"+expRequest+"',";
JSONStr = JSONStr + "'ssn_per_sec':";
JSONStr = JSONStr + "'"+throughput+"',";
JSONStr = JSONStr + "'avg_time_per_ssn':";
JSONStr = JSONStr + "'"+avgTimePerSSN+"',";
JSONStr = JSONStr + "'req_per_sec':";
JSONStr = JSONStr + "'"+reqPerSec+"',";
JSONStr = JSONStr + "'avg_time_per_req':";
JSONStr = JSONStr + "'"+avgTimePerReq+"',";
JSONStr = JSONStr + "'suc_req_per_rec':";
JSONStr = JSONStr + "'"+sucReqPerSec+"',";
JSONStr = JSONStr + "'avg_time_per_suc_req':";
JSONStr = JSONStr + "'"+avgTimePerSucReq+"' }";

System.out.println("*** RESULT: "+JSONStr);

%>


</body>
</html>