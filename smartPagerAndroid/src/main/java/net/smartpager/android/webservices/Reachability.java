package net.smartpager.android.webservices;

import android.net.NetworkInfo;

import android.net.ConnectivityManager;

import android.content.Context;

public class Reachability
{
	// Single instance
	private static Reachability mInstance;
	
	// Connectivity manager
	private ConnectivityManager manager;
	
	// Constructor
	private Reachability(Context context)
	{
		manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	// Singleton
	
	/**
	 * Gets the single instance of {@link Reachability} with a given {@link Context} object.
	 * 
	 * @param context the given {@link Context} object
	 */
	public static Reachability getInstance(Context context)
	{
		if (mInstance == null){
			mInstance = new Reachability(context);
        }
		return mInstance;
	}
	
	// Public methods	
	/**
	 * Indicates whether network connectivity exists.
	 * 
	 * @return <b>true</b> if network connectivity exists, <b>false</b> otherwise.
	 */
	public boolean isConnected()
	{
		if (manager != null){
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info != null) return info.isConnected();
		}
		return false;
	}
	
	/**
	 * Indicates whether the device is connected through Wifi.
	 * 
	 * @return <b>true</b> if the device is connected through Wifi, <b>false</b> otherwise.
	 */
	public boolean isWifiConnected()
	{
		if (manager != null){
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info != null && info.isConnectedOrConnecting()){
				return info.getType() == ConnectivityManager.TYPE_WIFI;
	        }
		}
		return false;
	}
}