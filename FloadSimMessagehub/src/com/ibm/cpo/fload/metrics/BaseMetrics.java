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

package com.ibm.cpo.fload.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BaseMetrics {

	private int readAcount = 0;
	private int writeAcount = 0;
	private int readUcount = 0;
	private int writeUcount = 0;
	private int retrycount = 0;
	private long endts;

	
	private HashMap<Integer, Integer> retryDist = null;


	
	public static void main(String[] args) {

		BaseMetrics met = new BaseMetrics();
		met.reset();
		
		met.incRetryDistCount(2);
		met.addTotalRetryCount(2);
		
		met.incRetryDistCount(8);
		met.addTotalRetryCount(8);
		
		met.incRetryDistCount(9);
		met.addTotalRetryCount(9);		
		
		met.incRetryDistCount(2);
		met.addTotalRetryCount(2);	
		
		met.incRetryDistCount(28);
		met.addTotalRetryCount(28);				

		met.incRetryDistCount(38);
		met.addTotalRetryCount(38);				

		met.incRetryDistCount(28);
		met.addTotalRetryCount(28);				

		met.incRetryDistCount(38);
		met.addTotalRetryCount(38);				

		met.incRetryDistCount(9);
		met.addTotalRetryCount(9);	
		
		met.incRetryDistCount(9);
		met.addTotalRetryCount(9);	
		
		met.incRetryDistCount(9);
		met.addTotalRetryCount(9);	
		
		met.printRetryDist(1);
		met.printResult(1);
		
	}
	
	
	
	public void reset() {
		
		this.readAcount = 0;
		this.writeAcount = 0;
		this.readUcount = 0;
		this.writeUcount = 0;
		this.retrycount = 0;
		this.retryDist = new HashMap<Integer, Integer>();
		
	}
	
	
	
	public void incRetryDistCount(int no_of_retries) {
		Integer retry_count_int = (Integer)this.retryDist.get(new Integer(no_of_retries));
		if (retry_count_int != null) {
			retry_count_int = new Integer(retry_count_int.intValue()+1);
			this.retryDist.put(new Integer(no_of_retries), retry_count_int);
		} else {
			this.retryDist.put(new Integer(no_of_retries), new Integer(1));			
		}	
	}
	
	public void printRetryDist(int threadid) {
		StringBuffer disttxt = new StringBuffer("{");
		Iterator<Map.Entry<Integer, Integer>> itr = retryDist.entrySet().iterator();
		boolean hasfirstitem = false;
		while (itr.hasNext()) { 
			if (hasfirstitem) {
				disttxt.append(",");
			}
			Map.Entry<Integer, Integer> pair = (Map.Entry<Integer, Integer>)itr.next();
			int no_of_retries = (Integer)pair.getKey().intValue();
			int count = (Integer)pair.getValue().intValue();
			disttxt.append("'").append(no_of_retries).append("_retries':'").append(count).append("'");			
			itr.remove(); // avoids a ConcurrentModificationException
			hasfirstitem = true;
		}	
		disttxt.append("}");
		System.out.println("*** Client Thread ["+threadid+"] RETRY DISTRIBUTION: "+disttxt.toString());
		
		
	}
	
	
	public void addTotalRetryCount(int count) {
		this.retrycount = this.retrycount + count;
	}
	
	public int getRetryCount() {
		return retrycount;
	}


	public void setRetryCount(int retrycount) {
		this.retrycount = retrycount;
	}


	public void incReadCount() {
		this.readAcount++;
	}
	
	public void incWriteCount() {
		this.writeAcount++;
	}

	public int getReadAcount() {
		return this.readAcount;
	}
	
	public void setReadAcount(int readAcount) {
		this.readAcount = readAcount;
	}
	
	public int getWriteAcount() {
		return writeAcount;
	}
	public long getEndTimestamp() {
		return this.endts;
	}
	
	public void setWriteAcount(int writeAcount) {
		this.writeAcount = writeAcount;
	}
	public int getReadUcount() {
		return readUcount;
	}
	public void setReadUcount(int readUcount) {
		this.readUcount = readUcount;
	}
	public void setEndTimestamp(long ts) {
		this.endts = ts;
	}
	public int getWriteUcount() {
		return writeUcount;
	}
	public void setWriteUcount(int writeUcount) {
		this.writeUcount = writeUcount;
	}

	public void printResult(int threadid) {
		System.out.println("*** Client Thread ["+threadid+"] METRICS: {'readA':'"+readAcount+"', 'writeA':'"+writeAcount+"', 'readU':'"+readUcount+"', 'writeU':"+writeUcount+"', 'retry':"+retrycount+"'}");		
	}
}
