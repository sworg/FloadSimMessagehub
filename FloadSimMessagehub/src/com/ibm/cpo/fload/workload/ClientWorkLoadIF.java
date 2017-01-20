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

public interface ClientWorkLoadIF {
	
	public static long clientID = 0;
	public static BaseMetrics metrics = null;
	public static Errors errors = null;

	public String runWorkload(int id);
	
	//public void setWorkloadMetrics(BaseMetrics metrics);
	//public void setWorkloadError(Errors err);	
	public void setClientID(long id);
	
}
