/*
 * Copyright 2013 Netatmo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netatmo.weatherstation.api;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.netatmo.weatherstation.api.model.Module;
import com.netatmo.weatherstation.api.model.Params;
import com.netatmo.weatherstation.api.model.Station;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class NetatmoHttpClient {
    // API URLs that will be used for requests, see: http://dev.netatmo.com/doc/restapi.
    protected final String URL_BASE = "https://api.netatmo.net";
    protected final String URL_REQUEST_TOKEN = URL_BASE + "/oauth2/token";
    protected final String URL_GET_DEVICES_LIST = URL_BASE + "/api/devicelist";
    protected final String URL_GET_MEASURES = URL_BASE + "/api/getmeasure";

    // You can find the AsyncHttpClient library documentation here: http://loopj.com/android-async-http.
    //AsyncHttpClient mClient;
    CloseableHttpClient mClient;
    
    List<Station> stations;
    
    public static String TAG = "NetatmoHttpClient: ";
    
    final String userAgent = "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";
    
    public NetatmoHttpClient() {
    	mClient = (CloseableHttpClient) NetatmoUtils.trustEveryoneSslHttpClient();
    }
    
    /**
     * POST request using AsyncHttpClient.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    @SuppressWarnings("rawtypes")
	protected void post(String url, HashMap<String, String> params, final NetatmoResponseHandler responseHandler) throws ClientProtocolException, IOException {    	
		HttpPost request = new HttpPost(url);
		
		request.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());  
		for (Map.Entry entry : params.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
		}
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		mClient.execute(request,responseHandler);
    }

    /**
     * GET request using AsyncHttpClient.
     * Since the access token is needed for each GET request to the Netatmo API,
     * we need to check if it has not expired.
     * See {@link #refreshToken(String, com.loopj.android.http.JsonHttpResponseHandler)}.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    protected void get(final String url, final HashMap<String, String> params, final NetatmoResponseHandler rh) throws ClientProtocolException, IOException {
    	// Add some stuff here to refresh token if needed ...
    	params.put("access_token",getAccessToken());
    	post(url,params,rh);
    }

    
    /**
     * This is the first request you have to do before being able to use the API.
     * It allows you to retrieve an access token in one step,
     * using your application's credentials and the user's credentials.
     * @throws IOException 
     * @throws ClientProtocolException 
     */
    public void login(String email, String password, NetatmoResponseHandler responseHandler) throws ClientProtocolException, IOException {
    	HashMap<String, String> params = new HashMap<String, String>();
        params.put("grant_type", "password");
        params.put("client_id", getClientId());
        params.put("client_secret", getClientSecret());
        params.put("username", email);
        params.put("password", password);

        post(URL_REQUEST_TOKEN, params, responseHandler);
        
    }

	/**
	 * Returns the list of devices owned by the user, and their modules.
	 * A device is identified by its _id (which is its mac address) and each device may have one,
	 * several or no modules, also identified by an _id.
	 * See <a href="http://dev.netatmo.com/doc/restapi/devicelist">http://dev.netatmo.com/doc/restapi/devicelist</a> for more.
	 */
	public void getDevicesList(NetatmoResponseHandler responseHandler) {
	    try {
			get(URL_GET_DEVICES_LIST, new HashMap<String, String>(), responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
     * Returns the last measurements for the given parameters.
     *
     * @param stationId A main device id from the {@link #getDevicesList(com.loopj.android.http.JsonHttpResponseHandler)} request.
     * @param moduleId Put here the stationId if you want measurements from the main device but not from a module.
     * @param scale You can specify a SCALE_* from {@link com.netatmo.weatherstation.api.model.Params}.
     * @param types If you want to use {@link com.netatmo.weatherstation.api.NetatmoUtils#parseMeasures(org.json.JSONObject, String[])}
     *              you have to use TYPES_* from {@link com.netatmo.weatherstation.api.model.Params}.
     * @param responseHandler
     *
     * See <a href="http://dev.netatmo.com/doc/restapi/getmeasure">http://dev.netatmo.com/doc/restapi/getmeasure</a> for more.
     */
    public void getLastMeasures(String stationId, String moduleId, String scale, String[] types, NetatmoResponseHandler responseHandler) {    
    	HashMap<String, String> params = new HashMap<String, String> ();
        params.put("device_id", stationId);

        if (!moduleId.equals(stationId)) {
            params.put("module_id", moduleId);
        }

        params.put("scale", scale);

        String stringTypes = "";
        for (int i=0; i < types.length; i++) {
            stringTypes += types[i];
            if (i+1 < types.length) {
                stringTypes += ",";
            }
        }

        params.put("type", stringTypes);
        params.put("date_end", "last");

        try {
			get(URL_GET_MEASURES, params, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void getAllMeasures(NetatmoResponseHandler responseHandler) {
        for(int incr=0; incr <stations.size(); incr++) {
        	Station s = stations.get(incr);
        	for (Module m : s.getModules()) {
        		System.out.println(s.getName() + "("+m.getName()+") :");
        		this.getLastMeasures(s.getId(), m.getId(), Params.SCALE_MAX, NetatmoConstants.types, responseHandler);
        	}
        }
    }
	
    /**
     * Making sure to call {@link #storeTokens(String, String, long)} with proper values.
     */
    protected void processOAuthResponse(JSONObject response) {
        HashMap<String,String> parsedResponse = NetatmoUtils.parseOAuthResponse(response);

        storeTokens(parsedResponse.get(NetatmoUtils.KEY_REFRESH_TOKEN),
                parsedResponse.get(NetatmoUtils.KEY_ACCESS_TOKEN),
                Long.valueOf(parsedResponse.get(NetatmoUtils.KEY_EXPIRES_AT)));
    }

	public void setStationList(List<Station> list) {
		this.stations = list;
	}
	
	public List<Station> getStationlist() {
		return this.stations;
	}
    
    /**
     * You can get your client id by creating a Netatmo app first:
     * <a href="http://dev.netatmo.com/dev/createapp">http://dev.netatmo.com/dev/createapp</a>
     */
    protected abstract String getClientId();

    /**
     * You can get your client secret by creating a Netatmo app first:
     * <a href="http://dev.netatmo.com/dev/createapp">http://dev.netatmo.com/dev/createapp</a>
     */
    protected abstract String getClientSecret();

    /**
     * You have to call this method when receiving the response from
     * {@link #login(String, String, com.loopj.android.http.JsonHttpResponseHandler)}.
     */
    protected abstract void storeTokens(String refreshToken, String accessToken, long expiresAt);

    /**
     * You have to this method when signing out the user.
     */
    protected abstract void clearTokens();

    /**
     * Must return the refresh token stored by {@link #storeTokens(String, String, long)}.
     */
    protected abstract String getRefreshToken();

    /**
     * Must return the access token stored by {@link #storeTokens(String, String, long)}.
     */
    protected abstract String getAccessToken();

    /**
     * Must return expiration date stored by {@link #storeTokens(String, String, long)}.
     */
    protected abstract long getExpiresAt();
}