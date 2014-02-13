package com.netatmo.weatherstation.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

public abstract class HttpUrlConnectionService {

	public static final String TAG = "HttpUrlConnectionService: ";
	
	String user_agent;

	public HttpUrlConnectionService(String url, HashMap<String, String>  url_params, String user_agent) {
		this.user_agent = user_agent;
	}

	private static String readStream(InputStream in) {
		String rv = null;

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = null;
		  try {
		    reader = new BufferedReader(new InputStreamReader(in));
		    String line = "";

		    while ((line = reader.readLine()) != null) 
		    	sb.append(line);
		      rv = sb.toString();
		  } catch (Exception e) {
			  e.printStackTrace();
		  }

		  try { reader.close(); } catch (Exception ee) { ee.printStackTrace();  }

		  return rv;
	}

	private static boolean applyParams(HttpURLConnection connection, HashMap<String, String> params_hash) {
		try {
		String params = createParamsLine(params_hash);

		OutputStream os = connection.getOutputStream();
		BufferedWriter writer = new BufferedWriter(
		        new OutputStreamWriter(os, "UTF-8"));
		writer.write(params);
		writer.close();
		os.close();

			return true;
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}
	}

	private static String createParamsLine(HashMap<String, String> p) throws UnsupportedEncodingException
	{
	    StringBuilder result = new StringBuilder();
	    boolean first = true;

	    if ( p.size() > 0)
	    for (Entry<String,String> pair : p.entrySet() ) {

	        if (first) 
	            first = false;
	        else
	            result.append("&");

	        result.append(URLEncoder.encode(pair.getKey(), "UTF-8")).append("=").append(URLEncoder.encode(pair.getValue(), "UTF-8"));
	    }

	    return result.toString();
	}
}