package net.smartpager.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import biz.mobidev.framework.utils.Log;

public class NetworkUtils {

	public static boolean isNetworksAvailable() {

		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		ConnectivityManager mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnMgr != null) {
			NetworkInfo[] mNetInfo = mConnMgr.getAllNetworkInfo();
			if (mNetInfo != null) {
				for (int i = 0; i < mNetInfo.length; i++) {
					if (mNetInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isInternetConnected() {
		String urlToCheck = Constants.BASE_URL;
		if (NetworkUtils.isNetworksAvailable()) {
			try {
				HttpURLConnection mURLConnection = (HttpURLConnection) (new URL(urlToCheck).openConnection());
				mURLConnection.setRequestProperty("User-Agent", "ConnectionTest");
				mURLConnection.setRequestProperty("Connection", "close");
				mURLConnection.setConnectTimeout(Constants.CHECK_CONNECTION_TIMEOUT);
				mURLConnection.setReadTimeout(Constants.CHECK_CONNECTION_TIMEOUT);
				mURLConnection.connect();
				int responseCode = mURLConnection.getResponseCode();
				return (responseCode >= 200 && responseCode < 300);
			} catch (IOException ioe) {
				 Log.e("isInternetConnected", "Exception occured while checking for Internet connection: ", ioe);
			}
		} else {
			 Log.e("isInternetConnected", "Not connected to WiFi/Mobile and no Internet available.");
		}
		return false;
	}
	
	public static boolean isInternetConnectedAsync() {
		SendRequestAsyncTask task = new SendRequestAsyncTask();
		task.execute();
		try {
			return task.get();
		} catch (Exception e) {
			
		}
		return false;
	}
	
}

class SendRequestAsyncTask extends AsyncTask<Void, Void, Boolean> {
	
	@Override
	protected Boolean doInBackground(Void... params) {
		return NetworkUtils.isInternetConnected();
	}
}
