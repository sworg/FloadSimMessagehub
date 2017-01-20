package com.ibm.cpo.fload.metrics;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Errors {
	
	private class WorkloadException {
		public String expMessage;
		public int code;
		public int expType;
		public Timestamp ts;
		
		 WorkloadException(String msg, int code, int type, Timestamp ts) {
			this.expMessage = msg;
			this.code = code;
			this.expType = type;
			this.ts = ts;
		 }
		 
	}
	

	private ArrayList<WorkloadException> explist = new ArrayList<WorkloadException>();

	public static final int EXP_TYPE_CONN_TIMED_OUT = 1;
	public static final int EXP_TYPE_SOCKET_TIMED_OUT = 2;
	public static final int EXP_TYPE_IO_FILE_NOT_FOUND = 3;
	public static final int EXP_TYPE_OTHER = 4;
	public static final int EXP_TYPE_CONN_RESET = 5;
	public static final int EXP_TYPE_NULL_HEADER = 6;
	public static final int EXP_TYPE_NULL_COOKIE = 7;
	public static final int EXP_TYPE_NULL = 8;
	
	
	public static void main(String[] args) {
		long nowts = Calendar.getInstance().getTime().getTime();
		Errors err = new Errors();
		err.reset();
		err.addExp("exp1", 1, 1, nowts);
		err.addExp("exp5", 5, 5, nowts);
		err.addExp("exp5", 5, 5, nowts);
		err.addExp("exp1", 1, 1, nowts);
		err.addExp("exp5", 5, 5, nowts);
		err.addExp("exp2", 2, 2, nowts);
		err.addExp("exp2", 2, 2, nowts);
		err.addExp("exp2", 2, 2, nowts);
		err.addExp("exp6", 6, 6, nowts);
		err.addExp("exp5", 5, 5, nowts);
		err.addExp("exp3", 3, 3, nowts);
		err.addExp("exp4", 4, 4, nowts);
		err.printError(88);
		
	}
	
	public void reset() {		
		this.explist =  new ArrayList<WorkloadException>();		
	}

	public void addExp(String msg, int code, int type, long lts) {
		Timestamp ts = new Timestamp(lts);
		WorkloadException exp = new WorkloadException(msg, code, type, ts);
		explist.add(exp);
		//System.out.println("### error count so far:"+explist.size());
	}

	public String getExpMsg(int idx) {
		return explist.get(idx).expMessage;
	}
	
	public int getExpcode(int idx) {
		return explist.get(idx).code;
	}
	
	public int getExpType(int idx) {
		return explist.get(idx).expType;
	}
	
	public long getTimestamp(int idx) {
		return explist.get(idx).ts.getTime();
	}
	
	public long getExpCount() {
		return explist.size();
	}

	private int error400 = 0;
	private int error401 = 0;
	private int error403 = 0;
	private int error409 = 0;
	private int error500 = 0;

	private int exceptionConnectionTimedOut = 0;
	private int exceptionConnectionSocketTimedOut = 0;
	private int exceptionConnectionIOFileNotFound = 0;
	private int exceptionConnectionOtherIO = 0;
	private int exceptionConnectionOther = 0;
	private int exceptionConnectionNull = 0;
	private int exceptionConnectionReset = 0;
	private int exceptionConnectionNullHeader = 0;
	private int exceptionConnectionNullCookie = 0;
	
	
	public int getError400() {
		return error400;
	}
	public void setError400(int error400) {
		this.error400 = error400;
	}
	public int getError401() {
		return error401;
	}
	public void setError401(int error401) {
		this.error401 = error401;
	}
	public int getError403() {
		return error403;
	}
	public void setError403(int error403) {
		this.error403 = error403;
	}
	public int getError409() {
		return error409;
	}
	public void setError409(int error409) {
		this.error409 = error409;
	}
	public int getError500() {
		return error500;
	}
	public void setError500(int error500) {
		this.error500 = error500;
	}
	public int getExceptionConnectionTimedOut() {
		return exceptionConnectionTimedOut;
	}
	public void setExceptionConnectionTimedOut(int exceptionConnectionTimedOut) {
		this.exceptionConnectionTimedOut = exceptionConnectionTimedOut;
	}
	public int getExceptionConnectionSocketTimedOut() {
		return exceptionConnectionSocketTimedOut;
	}
	public void setExceptionConnectionSocketTimedOut(int exceptionConnectionSocketTimedOut) {
		this.exceptionConnectionSocketTimedOut = exceptionConnectionSocketTimedOut;
	}
	public int getExceptionConnectionIOFileNotFound() {
		return exceptionConnectionIOFileNotFound;
	}
	public void setExceptionConnectionIOFileNotFound(int exceptionConnectionIOFileNotFound) {
		this.exceptionConnectionIOFileNotFound = exceptionConnectionIOFileNotFound;
	}
	public int getExceptionConnectionOtherIO() {
		return exceptionConnectionOtherIO;
	}
	public void setExceptionConnectionOtherIO(int exceptionConnectionOtherIO) {
		this.exceptionConnectionOtherIO = exceptionConnectionOtherIO;
	}
	public int getExceptionConnectionOther() {
		return exceptionConnectionOther;
	}
	public void setExceptionConnectionOther(int exceptionConnectionOther) {
		this.exceptionConnectionOther = exceptionConnectionOther;
	}
	public int getExceptionConnectionNull() {
		return exceptionConnectionNull;
	}
	public void setExceptionConnectionNull(int exceptionConnectionNull) {
		this.exceptionConnectionNull = exceptionConnectionNull;
	}
	public int getExceptionConnectionReset() {
		return exceptionConnectionReset;
	}
	public void setExceptionConnectionReset(int exceptionConnectionReset) {
		this.exceptionConnectionReset = exceptionConnectionReset;
	}
	public int getExceptionConnectionNullHeader() {
		return exceptionConnectionNullHeader;
	}
	public void setExceptionConnectionNullHeader(int exceptionConnectionNullHeader) {
		this.exceptionConnectionNullHeader = exceptionConnectionNullHeader;
	}
	public int getExceptionConnectionNullCookie() {
		return exceptionConnectionNullCookie;
	}
	public void setExceptionConnectionNullCookie(int exceptionConnectionNullCookie) {
		this.exceptionConnectionNullCookie = exceptionConnectionNullCookie;
	}

	public void printError(int threadid) {
		
		HashMap<Integer, String>  namehm = new HashMap<Integer, String>();	
		HashMap<Integer, Integer> stathm = new HashMap<Integer, Integer>();	
		int cnt = 1;
		for (int counter = 0; counter < explist.size(); counter++) { 		      
			WorkloadException wle = explist.get(counter); 	
			if (stathm.containsKey(new Integer(wle.code))) {
				Integer cntInteger = (Integer)stathm.get(wle.code);
				cnt = cntInteger.intValue() + 1;
			} else {
				namehm.put(new Integer(wle.code), wle.expMessage);        	         	  	          	
			}
			stathm.put(new Integer(wle.code), new Integer(cnt));      
			cnt = 1;
		}   		
		
		StringBuffer errortxt = new StringBuffer("{");
		Iterator<Map.Entry<Integer, String>> itr = namehm.entrySet().iterator();
		boolean hasfirstitem = false;
		while (itr.hasNext()) { 
			if (hasfirstitem) {
				errortxt.append(",");
			}
			Map.Entry<Integer, String> pair = (Map.Entry<Integer, String>)itr.next();
			Integer codeInteger = (Integer)pair.getKey();
			int count = ((Integer)stathm.get(codeInteger)).intValue();
			String name = pair.getValue();
			errortxt.append("'").append(name).append("':'").append(count).append("'");			
			itr.remove(); // avoids a ConcurrentModificationException
			hasfirstitem = true;
		}	
		errortxt.append("}");
		System.out.println("*** Client Thread["+threadid+"] ERRORS: "+errortxt.toString());
		
	}

}
