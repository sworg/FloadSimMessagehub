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

package com.ibm.cpo.fload.workload;

import com.ibm.cpo.fload.metrics.BaseMetrics;
import com.ibm.cpo.fload.metrics.Errors;

public class ObjectConfig {
	
	private String bucketName     	= "swbucket";
	private String keyName        = "";
	private String uploadFileName 	= "";
	private String content = "";
	
	private BaseMetrics metrics;
	private Errors errors;


	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getBucketName() {
		return bucketName;
	}
	public BaseMetrics getMetrics() {
		return metrics;
	}
	public void setMetrics(BaseMetrics metrics) {
		this.metrics = metrics;
	}
	public Errors getErrors() {
		return errors;
	}
	public void setErrors(Errors errors) {
		this.errors = errors;
	}
	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
	public String getkeyName() {
		return keyName;
	}
	public void setkeyName(String keyName) {
		this.keyName = keyName;
	}
	public String getUploadFileName() {
		return uploadFileName;
	}
	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}
	
}
