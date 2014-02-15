package com.netatmo.weatherstation.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.json.JSONObject;

import com.netatmo.weatherstation.api.model.Measures;
import com.netatmo.weatherstation.api.model.Params;
import com.netatmo.weatherstation.api.model.Station;

public class NetatmoResponseHandler implements ResponseHandler<String> {
    public static final int REQUEST_LOGIN = 0;
    public static final int REQUEST_GET_DEVICES_LIST = 1;
    public static final int REQUEST_GET_LAST_MEASURES = 2;

    NetatmoHttpClient mHttpClient;
    int mRequestType;
    String[] mMeasuresTypes;
    
	public NetatmoResponseHandler() {
		super();
	}
	
    public NetatmoResponseHandler(NetatmoHttpClient httpClient, int requestType, String[] measuresTypes) {
        super();
        mHttpClient = httpClient;
        mRequestType = requestType;
        mMeasuresTypes = measuresTypes;
    }

    public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status != 200) return "";
        
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line = "", msg = "";
        while ((line = rd.readLine()) != null) {
        	msg += line;
        }
        
        JSONObject jresponse = new JSONObject(msg);
    	
    	switch(mRequestType) {
    		case REQUEST_LOGIN:
    			mHttpClient.processOAuthResponse(jresponse);
    		case REQUEST_GET_DEVICES_LIST:
    			List<Station> list = NetatmoUtils.parseDevicesList(jresponse);
    			
    			String stationList = "";
    			for(int i=0;i < list.size();i++) {
    				Station s = list.get(i);
    				stationList = "Station "+i+" : " + s.getName() + " (" + s.getId() +")\n"; 
    			}
    			
    			if(list.size() > 0) System.out.println(stationList);
    			
    			mHttpClient.setStationList(list);
    			
    			return stationList;
    		
    		case REQUEST_GET_LAST_MEASURES:
    			Measures measures = NetatmoUtils.parseMeasures(jresponse, NetatmoConstants.types);
    			
    			String measuresOut = "";
    			for(int i=0; i < NetatmoConstants.types.length; i++) {
    				measuresOut += "\t" + NetatmoConstants.types[i] + " : ";

    				switch (NetatmoConstants.types[i]) {
    					case Params.TYPE_CO2:
    						if(!measures.getCO2().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getCO2() + " ppm";
    						break;
    					case Params.TYPE_HUMIDITY:
    						if(!measures.getHumidity().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getHumidity() + " %";
    						break;
    					case Params.TYPE_MAX_TEMP:
    						if(!measures.getMaxTemp().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getMaxTemp() + " °C";
    						break;
    					case Params.TYPE_MIN_TEMP:
    						if(!measures.getMinTemp().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getMinTemp() + " °C";
    						break;
    					case Params.TYPE_NOISE:
    						if(!measures.getNoise().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getNoise() + " dB";
    						break;
    					case Params.TYPE_PRESSURE:
    						if(!measures.getPressure().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getPressure() + " mbar";
    						break;
    					case Params.TYPE_TEMPERATURE:
    						if(!measures.getTemperature().equals(Measures.STRING_NO_DATA))
    							measuresOut += measures.getTemperature() + " °C";
    						break;
    					default :
    						break;
    				}
    				 
    				measuresOut += "\n";
    			}
    			
    			if (measuresOut.length() > 0) 
    				System.out.println(measuresOut);
    			
    		default:
    			break;
    	}
        
        return "";
    }
}
