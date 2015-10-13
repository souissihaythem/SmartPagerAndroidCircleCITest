package net.smartpager.android.dialer;

import java.util.Locale;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.squareup.otto.Subscribe;
import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.utils.Log;
import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.events.VoipEvent;
import net.smartpager.android.events.VoipEvent.VoipEventType;
import net.smartpager.android.utils.AudioManagerUtils;

public class VoipPhoneScreen extends Activity {
	
	boolean isSpeakerEnabled;
	boolean isMicMuted;
	
	RelativeLayout hangupBtn;
	TextView numberToCallTV;
	TextView voipStatus;
	private AsYouTypeFormatter aytf;
	private String formattedNumber;
	
	ImageView speakerBtn;
	ImageView microphoneBtn;
	
	public VoipPhone voipPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Hide Action Bar
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		getActionBar().hide();
		
		SmartPagerApplication.getInstance().getBus().register(this);
		aytf = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(Locale.getDefault().getCountry());
		
		setContentView(R.layout.activity_voip_phone_screen);
		
		
		isSpeakerEnabled = false;
		isMicMuted = false;
		
		voipPhone = new VoipPhone(getApplicationContext());
		
		numberToCallTV = (TextView) findViewById(R.id.tv_voip_number_to_call);
		Intent thisIntent = getIntent();
		String phoneNumber = thisIntent.getStringExtra("phoneNumber");
		for (char num : phoneNumber.toCharArray()) {
			formattedNumber = aytf.inputDigit(num);
		}
		
		numberToCallTV.setText(formattedNumber);
		
		voipStatus = (TextView) findViewById(R.id.tv_voip_status);
		setStatusConnecting();
		
		hangupBtn = (RelativeLayout) findViewById(R.id.voip_phone_screen_hangup_btn);
		hangupBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				disconnect();
			}
		});
		
		speakerBtn = (ImageView) findViewById(R.id.iv_speaker_toggle);
		speakerBtn.setAlpha(100);
		speakerBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				updateSpeakerSetting();
			}
		});
		
		microphoneBtn = (ImageView) findViewById(R.id.iv_mic_toggle);
		microphoneBtn.setAlpha(100);
		microphoneBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				updateMicrophoneSetting();
			}
		});
		
	}
	
	private void disconnect() {
		voipPhone.disconnect();
		finish();
	}
	
	private void updateSpeakerSetting() {
		isSpeakerEnabled = !isSpeakerEnabled;
		if (isSpeakerEnabled) {
			speakerBtn.setAlpha(255);
		} else {
			speakerBtn.setAlpha(100);
		}
		AudioManagerUtils.setLoudSpeaker(isSpeakerEnabled);
	}
	
	private void updateMicrophoneSetting() {
		isMicMuted = !isMicMuted;
		if (isMicMuted) {
			microphoneBtn.setAlpha(255);
		} else {
			microphoneBtn.setAlpha(100);
		}
		AudioManagerUtils.setMicrophoneMute(isMicMuted);
	}
	
	public void setStatusConnecting() {
		voipStatus.setText("Connecting...");
	}
	
	public void setStatusConnected() {
		voipStatus.setText("Connected");
	}
	
	public void setStatusDisconnected() {
		voipStatus.setText("Disconnected");
	}
	
	@Subscribe 
	public void onVoipEvent(VoipEvent event) {
		if (event.eventType == VoipEventType.CONNECTING) {
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setStatusConnecting();
					
				}
			});
		} else if (event.eventType == VoipEventType.CONNECTED) {
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setStatusConnected();
					
				}
			});
		} else if (event.eventType == VoipEventType.DISCONNECTED) {
			this.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setStatusDisconnected();
					
				}
			});
			disconnect();
		}
	}
}
