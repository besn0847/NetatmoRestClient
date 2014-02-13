package netatmo.rest.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.security.cert.CertificateException;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;

public class NetatmoSimpleClient {
	private String username, password, id, secret;
	private String URL_BASE, URL_REQUEST_TOKEN;
	
	private ResponseHandler<String> responseHandler;
	private CloseableHttpClient httpclient;
    
	public NetatmoSimpleClient(String fileName) throws IOException, NoSuchAlgorithmException, KeyManagementException {	    
	    Properties props = new Properties();
        File propFile = new File(fileName);
        props.load(new FileInputStream(propFile));
        
		username = props.getProperty("netatmo_username");
		password = props.getProperty("netatmo_password");
		id = props.getProperty("netatmo_client_id");
		secret = props.getProperty("netatmo_client_secret");
		
		URL_BASE = props.getProperty("netatmo_url_base");
		URL_REQUEST_TOKEN = URL_BASE + props.getProperty("netatmo_url_request");
		
		httpclient = (CloseableHttpClient) NetatmoSimpleClient.trustEveryoneSslHttpClient();
		
		responseHandler = new ResponseHandler<String>() {

            public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                System.out.println("Http response : " + status);
                
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                while ((line = rd.readLine()) != null) {
                	System.out.println(line);
                }
                
                return "";
            }
		};
	}
	
	@SuppressWarnings("deprecation")
	private static HttpClient trustEveryoneSslHttpClient() {
	    try {
			SchemeRegistry registry = new SchemeRegistry();

	        SSLSocketFactory socketFactory = new SSLSocketFactory(new TrustStrategy() {
	            public boolean isTrusted(final X509Certificate[] chain, String authType) {
	                return true;
	            }
	        }, org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        registry.register(new Scheme("https", 443, socketFactory));
	        ThreadSafeClientConnManager mgr = new ThreadSafeClientConnManager(registry);
	        DefaultHttpClient client = new DefaultHttpClient(mgr, new DefaultHttpClient().getParams());
	        return client;
	    } catch (GeneralSecurityException e) {
	        throw new RuntimeException(e);
	    }
	}

	protected void post(String url, HashMap<String, String> params, ResponseHandler<String> responseHandler) throws ClientProtocolException, IOException {
		HttpPost request = new HttpPost(url);
		
		request.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());  
		for (Map.Entry entry : params.entrySet()) {
			nameValuePairs.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
		}
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		httpclient.execute(request,responseHandler);
	}
	
	public void login(String email, String password, ResponseHandler<String> responseHandler) throws ClientProtocolException, IOException {
	    	HashMap<String, String> params = new HashMap<String, String>();
	        params.put("grant_type", "password");
	        params.put("client_id", id);
	        params.put("client_secret", secret);
	        params.put("username", email);
	        params.put("password", password);

	        post(URL_REQUEST_TOKEN, params, responseHandler);
	}
	
	public void init() throws ClientProtocolException, IOException {
		login(username, password, responseHandler);
	}
	
	public static void main(String[] args) {
		try {
			NetatmoSimpleClient nc = new NetatmoSimpleClient("/tmp/netatmo.properties");
			nc.init();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
	}

}
