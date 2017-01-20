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

package com.ibm.cpo.utils;
import java.io.*;

public class PersistData {

	static String filename = "/rundata/floadwebdata.txt";
	public static boolean persist(String text) {
		
		boolean status = persistToFile(text);
		return status;
	}
	
	
	private static boolean persistToFile(String text) {
		
		System.out.println("** Persisting:"+text);
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename), true));
			bw.write(text);
			bw.newLine();
			bw.close();
			System.out.println(text+" ** are saved successfully **");
			return true;
		} catch (Exception e) {
			System.out.println("file exception");
			e.printStackTrace();
		}
	
		return false;
	}
}
