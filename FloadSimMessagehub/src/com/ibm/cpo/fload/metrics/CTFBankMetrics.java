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

public class CTFBankMetrics extends BaseMetrics {

	private int displayedPageCleanLogon = 0;
	private int displayedPageWelcomePage = 0;
	private int displayedPageRelogonAfterLogout = 0;
	private int displayedPagePageWithAllButtons = 0;
	private int displayedPageAccountSummary = 0;
	private int displayedPageCustomerProfile = 0;
	private int displayedPageTransactionHistory = 0;
	
	
	public int getDisplayedPageCleanLogon() {
		return displayedPageCleanLogon;
	}
	public void setDisplayedPageCleanLogon(int displayedPageCleanLogon) {
		this.displayedPageCleanLogon = displayedPageCleanLogon;
	}
	public int getDisplayedPageWelcomePage() {
		return displayedPageWelcomePage;
	}
	public void setDisplayedPageWelcomePage(int displayedPageWelcomePage) {
		this.displayedPageWelcomePage = displayedPageWelcomePage;
	}
	public int getDisplayedPageRelogonAfterLogout() {
		return displayedPageRelogonAfterLogout;
	}
	public void setDisplayedPageRelogonAfterLogout(int displayedPageRelogonAfterLogout) {
		this.displayedPageRelogonAfterLogout = displayedPageRelogonAfterLogout;
	}
	public int getDisplayedPagePageWithAllButtons() {
		return displayedPagePageWithAllButtons;
	}
	public void setDisplayedPagePageWithAllButtons(int displayedPagePageWithAllButtons) {
		this.displayedPagePageWithAllButtons = displayedPagePageWithAllButtons;
	}
	public int getDisplayedPageAccountSummary() {
		return displayedPageAccountSummary;
	}
	public void setDisplayedPageAccountSummary(int displayedPageAccountSummary) {
		this.displayedPageAccountSummary = displayedPageAccountSummary;
	}
	public int getDisplayedPageCustomerProfile() {
		return displayedPageCustomerProfile;
	}
	public void setDisplayedPageCustomerProfile(int displayedPageCustomerProfile) {
		this.displayedPageCustomerProfile = displayedPageCustomerProfile;
	}
	public int getDisplayedPageTransactionHistory() {
		return displayedPageTransactionHistory;
	}
	public void setDisplayedPageTransactionHistory(int displayedPageTransactionHistory) {
		this.displayedPageTransactionHistory = displayedPageTransactionHistory;
	}

	
	
}
