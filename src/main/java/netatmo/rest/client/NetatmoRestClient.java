package netatmo.rest.client;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.netatmo.weatherstation.api.NetatmoResponseHandler;

public class NetatmoRestClient {
	private RestHttpClient nc;
	private String username, password;

	public NetatmoRestClient(String u, String p) throws FileNotFoundException, IOException {
		 this.username = u;
		 this.password = p;
	}
	
	public void init() throws ClientProtocolException, IOException {
		nc = new RestHttpClient();
		
		// Login to Netatmo service
		nc.login(username, password, new NetatmoResponseHandler(nc, NetatmoResponseHandler.REQUEST_LOGIN, null));
		
		// Retrieve all available stations
		nc.getDevicesList(new NetatmoResponseHandler(nc, NetatmoResponseHandler.REQUEST_GET_DEVICES_LIST, null));
		
		// Rerieve measures for each station
		nc.getAllMeasures(new NetatmoResponseHandler(nc, NetatmoResponseHandler.REQUEST_GET_LAST_MEASURES, null));
		
	}
	
	public void log() {
		String logmsg = "";
		
		logmsg = "Access token : " + nc.getAccessToken() + "\n";
		logmsg += "Refresh token : " + nc.getRefreshToken() + "\n";
		logmsg += "Expires at : " + nc.getExpiresAt();
		
		System.out.println(logmsg);
	}
	
	public static void main(String[] args) {
		String filename = "netatmo.properties";
		
		if(args.length != 2) {
			System.out.println("Usage: NetatmoRestClient <username> <password>");
			return;
		} 
		
		try {
			NetatmoRestClient nrc = new NetatmoRestClient(args[0],args[1]);
			nrc.init();
			// nrc.log();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
