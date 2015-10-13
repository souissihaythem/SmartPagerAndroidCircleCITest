package net.smartpager.android.web;

import android.content.Context;
import android.net.ConnectivityManager;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.web.command.AbstractCommand;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

import java.io.IOException;

/**
 * @author Roman
 * Sends GET- and POST-requests to serves
 */
public class RequestSender {
	
	/**
	 * Send request to server <br>
	 * GET - if command doesn't contain any parameters<br>
	 * POST - otherwise
	 * @param command
	 * @return JSON response string from server
	 * @throws Exception
	 * @see AbstractCommand
	 * @see #executeGet
	 * @see #executePost
	 */
	public String execRaw(AbstractCommand command) throws Exception{
        ConnectivityManager cm = (ConnectivityManager) SmartPagerApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (SmartPagerApplication.getInstance().getSettingsPreferences().getPreferCellularToggled()) {
            System.out.println("Prefer Cellular");
            cm.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
        } else {
            System.out.println("Prefer Default Network Preference");
            cm.setNetworkPreference(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
        }

		String resString;
		 HttpEntity params = command.getEntity();
        if (params==null)
            resString = executeGet(command.getUrl());
        else{
            resString = executePost(command.getUrl(), params);
        }
        return resString;
	}
	
	/**
	 * Forms POST-request 
	 * @param url
	 * @param params
	 * @return JSON response string from server 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @see #execRaw(AbstractCommand)
	 */
    private String executePost(String url, HttpEntity params) throws ClientProtocolException, IOException {

        ConnectivityManager cm = (ConnectivityManager) SmartPagerApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (SmartPagerApplication.getInstance().getSettingsPreferences().getPreferCellularToggled()) {
            System.out.println("Prefer Cellular");
            cm.setNetworkPreference(ConnectivityManager.TYPE_MOBILE);
        } else {
            System.out.println("Prefer Default Network Preference");
            cm.setNetworkPreference(ConnectivityManager.DEFAULT_NETWORK_PREFERENCE);
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(params);

        String response =  SmartPagerHTTPClient.getHttpClient().execute(httpPost, new BasicResponseHandler());

        return response;
    }
    
	/**
	 * Forms GET-request 
	 * @param url
	 * @return JSON response string from server 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @see #execRaw(AbstractCommand)
	 */
    private String executeGet(String url) throws IOException, ClientProtocolException {
        HttpGet httpPost = new HttpGet(url);
        String response =  SmartPagerHTTPClient.getHttpClient().execute(httpPost, new BasicResponseHandler());
        return response;
    }
}
