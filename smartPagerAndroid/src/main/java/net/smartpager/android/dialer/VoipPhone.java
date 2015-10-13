package net.smartpager.android.dialer;

import java.util.HashMap;
import java.util.Map;

import com.squareup.otto.Produce;
import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.events.VoipEvent;
import net.smartpager.android.events.VoipEvent.VoipEventType;

public class VoipPhone implements Twilio.InitListener, DeviceListener, ConnectionListener {
	private static final String TAG = "VoipPhone";

	private Context mContext;
	private Device device;
	private Connection connection;
	private String capabilityToken;
	private String phoneNumber;
	
	public VoipPhone(Context mContext) {
		this.mContext = mContext;
	}
	
	public VoipPhone(Context mContext, String capabilityToken) {
		this.mContext = mContext;
		this.capabilityToken = capabilityToken;
	}
	
	public void connect(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		
		if (!Twilio.isInitialized()) {
			Twilio.initialize(mContext, this);
		} else {
			setCapabilityToken(this.capabilityToken);
		}
	}
	
	public void disconnect() {
		Log.e(TAG, "disconnect");
		if (connection != null) {
			connection.disconnect();
			connection = null;
		}

		try {
			Twilio.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void setCapabilityToken(String capabilityToken) {
		Log.e(TAG, "setCapabilityToken");
		try {
			device = Twilio.createDevice(capabilityToken, this);
			Log.e(TAG, "capability token: " + capabilityToken);
			Log.e(TAG, "device: " + device);
			Log.e(TAG, "device created: " + device.toString());
		} catch (Exception e) {
			Log.e(TAG, "Failed to obtain capability token");
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("To", this.phoneNumber);
		parameters.put("targetNumber", this.phoneNumber);
		parameters.put("username", SmartPagerApplication.getInstance().getPreferences().getSmartPagerID());
		
		Log.e("VoipPhone", "SmartPagerApplication.getInstance().getPreferences().getSmartPagerID(): " + SmartPagerApplication.getInstance().getPreferences().getSmartPagerID());
		
		connection = device.connect(parameters, this);
		if (connection == null) {
			Log.w(TAG, "Failed to create new connection");
		}
	}
	
	// Twilio.InitListener
	@Override
	public void onInitialized() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onInitialized");

		setCapabilityToken(this.capabilityToken);
	}
	
	@Override
	public void onError(Exception e) {
		Log.e(TAG, "Twilio SDK couldn't start: " + e.getLocalizedMessage());
		
	}
	
	// DeviceListener
	@Override
	public boolean receivePresenceEvents(Device arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onStopListening(Device arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStopListening(Device arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStartListening(Device arg0) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onPresenceChanged(Device arg0, PresenceEvent arg1) {
		// TODO Auto-generated method stub
		
	}
	
	// ConnectionListener
	@Override
	public void onDisconnected(Connection arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Disconnected 1 - Arg0: " + arg0 + ", Arg1: " + arg1 + ", Arg2: " + arg2);
	}
	
	@Override
	public void onDisconnected(Connection arg0) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Disconnected 2 - Arg 0: " + arg0);
		SmartPagerApplication.getInstance().getBus().post(produceVoipEvent(VoipEventType.DISCONNECTED));
	}
	
	@Override
	public void onConnecting(Connection arg0) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Connecting");
		Intent voipPhoneScreen = new Intent(mContext, VoipPhoneScreen.class);
		voipPhoneScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		voipPhoneScreen.putExtra("phoneNumber", this.phoneNumber);
		mContext.startActivity(voipPhoneScreen);
		
		SmartPagerApplication.getInstance().getBus().post(produceVoipEvent(VoipEventType.CONNECTING));
	}
	
	@Override
	public void onConnected(Connection arg0) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Connected");
		SmartPagerApplication.getInstance().getBus().post(produceVoipEvent(VoipEventType.CONNECTED));
	}
	
	@Produce
	public VoipEvent produceVoipEvent(VoipEventType eventType) {
		return new VoipEvent(eventType);
	}

}


