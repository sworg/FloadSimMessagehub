package com.ibm.cpo.fload.workload;

public class WhiskResult {
	public int retcode;
	public String result;
	public int retrycnt;
	public WhiskResult(int rcd, String rslt, int rcnt) {
		this.retcode = rcd;
		this.retrycnt = rcnt;
		this.result = rslt;
	}
	public void print() {
		System.out.println("retcode:"+this.retcode+" retries:"+this.retrycnt+" result:"+this.result);
	}
}
