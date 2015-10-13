package net.smartpager.android.activity.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.RequiredPinToBeEntered;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;

public class PinSettingsActivity extends BaseActivity {

	@Override
	protected boolean isUpNavigationEnabled() {
		
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin_settings);
		initGui();
	}

	private void initGui() {
		initChangePin();
		setPinEntryTimeFromServer();
		initRequiredPinRadiogroup();
	}
	
	private void setPinEntryTimeFromServer() {
		TextView pinEntryDisabledTextView = (TextView) findViewById(R.id.settings_pin_entry_disabled);
		
		RequiredPinToBeEntered required = null;
		if (SmartPagerApplication.getInstance().getPreferences().getPinEntryTimeout().equals("null")) {
			pinEntryDisabledTextView.setVisibility(View.GONE);
		} else {
			pinEntryDisabledTextView.setVisibility(View.VISIBLE);
			
			int timeout = Integer.parseInt(SmartPagerApplication.getInstance().getPreferences().getPinEntryTimeout());
			switch(timeout) {
				case 0 :
					required = RequiredPinToBeEntered.always;
					break;
				case 1800:
					required = RequiredPinToBeEntered._30Min;
					break;
				case 3600:
					required = RequiredPinToBeEntered._1Hour;
					break;
				case 28800:
					required = RequiredPinToBeEntered._8Hours;
					break;
			}
			
			SmartPagerApplication.getInstance().getSettingsPreferences().setRequiredPin(required);
			
			pinEntryDisabledTextView.setText("Input disabled. Value set on server.");
			
			RadioGroup requiredRadioGroup = (RadioGroup) findViewById(R.id.settings_required_pin_radiogroup);
			for (int i = 0;i < requiredRadioGroup.getChildCount(); i++) {
				requiredRadioGroup.getChildAt(i).setEnabled(false);
			}
		}
	}
	
	private void initChangePin() {
		findViewById(R.id.settings_change_pin_layout)
			.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(PinSettingsActivity.this, ChangePinActivity.class);
				startActivity( intent );
			}
		});
		
	}

	private void initRequiredPinRadiogroup() {
		RadioGroup requiredRadioGroup = (RadioGroup) findViewById(R.id.settings_required_pin_radiogroup);
		int checkedRadiobutton = R.id.settings_required_pin_always;
		switch (SmartPagerApplication.getInstance().getSettingsPreferences().getRequiredPin()) {
			case _30Min:
				checkedRadiobutton = R.id.settings_required_pin_30m;
				break;
			case _1Hour:
				checkedRadiobutton = R.id.settings_required_pin_1h;
				break;
			case _8Hours:
				checkedRadiobutton = R.id.settings_required_pin_8h;
				break;
			case always:
				checkedRadiobutton = R.id.settings_required_pin_always;
				break;
			default:
				break;
		}
		requiredRadioGroup.check(checkedRadiobutton);
		
		requiredRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				RequiredPinToBeEntered required = RequiredPinToBeEntered.always;
				switch (checkedId) {
					case R.id.settings_required_pin_30m:
						required = RequiredPinToBeEntered._30Min;
						break;
					case R.id.settings_required_pin_1h:
						required = RequiredPinToBeEntered._1Hour;
						break;
					case R.id.settings_required_pin_8h:
						required = RequiredPinToBeEntered._8Hours;
						break;
					case R.id.settings_required_pin_always:
						required = RequiredPinToBeEntered.always;
						break;
					default:
						break;
				}
				SmartPagerApplication.getInstance().getSettingsPreferences().setRequiredPin(required);
			}
		});
	}
	
	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		
	}

}
