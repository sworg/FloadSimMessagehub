package com.ibm.cpo.fload.workload;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class BluemixAuth {

	private static String host = "login.stage0.ng.bluemix.net";
	private static int port = 443;
	private static boolean useSSL = true;

	public static void main(String[] args) {
		try {
			post(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void post(long timeoutMS) throws IOException {
		Socket socket = null;
		System.out.println("Openning socket - " + host + ":" + port);
		try {
			socket = SSLSocketFactory.getDefault().createSocket(host, port);
			System.out.println("socket opened:" + socket);
			send(socket);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("done");
		if (socket !=null) 
			socket.close();
	}

	public static void send(Socket socket) throws Exception {

		String userid = "cf:";
		byte[] authEncBytes = Base64.encodeBase64(userid.getBytes());
		String authStringEnc = new String(authEncBytes);
		String data = "grant_type=client_credentials&client_id=myApp&client_secret=ab32vr";
		String input = "grant_type=password&username=ibmuser1@us.ibm.com&password=passw0rd";

		BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
		wr.write("POST /UAALoginServerWAR/oauth/token HTTP/1.1\r\n");
		wr.write("Host: login.stage0.ng.bluemix.net\r\n");
		wr.write("Authorization: Basic " + authStringEnc + "\r\n");
		wr.write("Content-Length: " + input.length() + "\r\n");
		wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
		wr.write(input);
		wr.flush();
		System.out.println("HTTPS flushed");

		
		BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line;
		System.out.println("result:");
		while ((line = rd.readLine()) != null) {
			System.out.println(line);
		}
		
		wr.close();
		rd.close();
	}

}