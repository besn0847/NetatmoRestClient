package netatmo.rest.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.http.client.ClientProtocolException;

import com.netatmo.weatherstation.api.NetatmoResponseHandler;
import com.netatmo.weatherstation.sample.SampleHttpClient;

public class NetatmoRestClient {
	
	private SampleHttpClient nc;
	private String username, password;

	public NetatmoRestClient(String fileName) throws FileNotFoundException, IOException {
		 Properties props = new Properties();
		 File propFile = new File(fileName);
		 props.load(new FileInputStream(propFile));
		 
		 username = props.getProperty("netatmo_username");
		 password = props.getProperty("netatmo_password");
	}
	
	public void init() throws ClientProtocolException, IOException {
		nc = new SampleHttpClient();
		
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
		
		if(args.length > 0)
			filename = args[0];
		
		try {
			NetatmoRestClient nrc = new NetatmoRestClient(filename);
			nrc.init();
			// nrc.log();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
