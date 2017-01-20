function savedata() {
	
	console.debug("updated in the function");
	JSONStr = JSONStr + "vm_was_cpu_idle:";
	JSONStr = JSONStr + "'" + document.getElementById("wascpuidle").value + "',";
	JSONStr = JSONStr + "vm_was_mem_free:";
	JSONStr = JSONStr + "'" + document.getElementById("wasmemfree").value + "',";
	JSONStr = JSONStr + "vm_was_nwk_recv:";
	JSONStr = JSONStr + "'" + document.getElementById("wasnwkrecv").value + "',";
	JSONStr = JSONStr + "vm_was_nwk_tran:";
	JSONStr = JSONStr + "'" + document.getElementById("wasnwktran").value + "',";
	JSONStr = JSONStr + "vm_was_dsk_busy:";
	JSONStr = JSONStr + "'" + document.getElementById("wasdskbusy").value + "',";

	JSONStr = JSONStr + "vm_db2_cpu_idle:";
	JSONStr = JSONStr + "'" + document.getElementById("db2cpuidle").value + "',";
	JSONStr = JSONStr + "vm_db2_mem_free:";
	JSONStr = JSONStr + "'" + document.getElementById("db2memfree").value + "',";
	JSONStr = JSONStr + "vm_db2_nwk_recv:";
	JSONStr = JSONStr + "'" + document.getElementById("db2nwkrecv").value + "',";
	JSONStr = JSONStr + "vm_db2_nwk_tran:";
	JSONStr = JSONStr + "'" + document.getElementById("db2nwktran").value + "',";
	JSONStr = JSONStr + "vm_db2_tbs_dsk_busy:";
	JSONStr = JSONStr + "'" + document.getElementById("db2tdkbusy").value + "',";
	JSONStr = JSONStr + "vm_db2_log_dsk_busy:";
	JSONStr = JSONStr + "'" + document.getElementById("db2ldkbusy").value + "',";

	JSONStr = JSONStr + "vm_ldr_cpu_idle:";
	JSONStr = JSONStr + "'" + document.getElementById("ldrcpuidle").value + "',";
	JSONStr = JSONStr + "vm_ldr_nwk_recv:";	
	JSONStr = JSONStr + "'" + document.getElementById("ldrnwkrecv").value + "',";
	JSONStr = JSONStr + "vm_ldr_nwk_tran:";	
	JSONStr = JSONStr + "'" + document.getElementById("ldrnwktran").value + "',";
	
	JSONStr = JSONStr + "run_by:";
	JSONStr = JSONStr + "'" + document.getElementById("runby").value + "',";
	JSONStr = JSONStr + "run_remark:";
	JSONStr = JSONStr + "'" + document.getElementById("runremark").value + "'}";

	//alert("about to ajax:"+JSONStr);
	var myObject = eval('(' + JSONStr + ')');
	senddatatoserver(myObject)

}

function senddatatoserver(jsonobj, type) {

	require(["dojo/_base/xhr"], function(xhr){
	    // post some data, ignore the response:
	    xhr.post({
	        timeout: 3000, // give up after 3 seconds
	        content: jsonobj, // creates ?part=one&another=part with GET, Sent as POST data when using xhrPost
	        url:"/FloadWebSWF/DataSavingServlet",
	        handleAs:"json",
	        load: function(data){
	               //console.log("got response from server");
	               alert("Run result saved successfully.");
	        }
	    });
	});	
}