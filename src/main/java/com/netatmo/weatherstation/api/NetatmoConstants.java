package com.netatmo.weatherstation.api;

import com.netatmo.weatherstation.api.model.Params;

public final class NetatmoConstants {

	/**
	 * Data to be collected from Netatmo modules
	 */
	static String[] types = new String[]{
	        Params.TYPE_NOISE,
	        Params.TYPE_CO2,
	        Params.TYPE_PRESSURE,
	        Params.TYPE_HUMIDITY,
	        Params.TYPE_TEMPERATURE,
	};	
	
	/**
	 * Netatmo base REST URLs
	 * see: http://dev.netatmo.com/doc/restapi
	 */
	static String URL_BASE = "https://api.netatmo.net";
    static String URL_REQUEST_TOKEN = URL_BASE + "/oauth2/token";
    static String URL_GET_DEVICES_LIST = URL_BASE + "/api/devicelist";
    static String URL_GET_MEASURES = URL_BASE + "/api/getmeasure";

    /**
     * Netatmo Application Id & Secret 
     */
    public static String CLIENT_ID = "52ea7ef91b7759f96250c24b";
    public static String CLIENT_SECRET = "CoONiS67jOcSIXQlNh3ZLqmReXLiU6TFixJ4hso4mhkgn";

}
