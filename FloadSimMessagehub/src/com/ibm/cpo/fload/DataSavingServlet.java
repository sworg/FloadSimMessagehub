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

package com.ibm.cpo.fload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.ibm.cpo.utils.PersistData;

/**
 * Servlet implementation class DataSavingServlet
 */
@WebServlet("/DataSavingServlet")
public class DataSavingServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DataSavingServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("got get request");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("doPost: got post request");
		
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dts = dateFormat.format(cal.getTime());
		
		if (request !=null) {
			String dataStr = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = in.readLine();
			while (line != null) {
			    dataStr += line;
			    line = in.readLine();
			}
			dataStr = dataStr.replaceAll("&", ",").replaceAll("=", ":");
			boolean rslt = PersistData.persist("*"+dts+"* "+dataStr);
			System.out.println("Persist result:"+rslt);		
		} else
			System.out.println("null request");

		HttpSession session = request.getSession();
		String rpsHistory = (String) session.getAttribute("rpsData");
		boolean rslt = PersistData.persist("*"+dts+"* RPS History:"+rpsHistory);
		
	}

}
