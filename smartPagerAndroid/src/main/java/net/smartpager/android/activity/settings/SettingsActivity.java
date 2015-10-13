package net.smartpager.android.activity.settings;

import java.util.List;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.CallsSettings;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.IncomingCallModes;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.notification.AlertFilesUtils;
import net.smartpager.android.notification.Notificator;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.response.AbstractResponse;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.rey.material.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class SettingsActivity extends BaseActivity implements BaseActivity.IOnProgressDialogCancelledListener{

	private TextView mPagerOnGreetingsTextView;
	private TextView mPagerOffGreetingsTextView;
	private EditText mBlockingPrefixEditText;
	private boolean m_bIsSynchingContacts;
	private RadioGroup blockRadioGroup;
	private TextView mBlockMyNumberLabel;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		initGui();
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {

		return true;
	}
	
	@Override
	protected void onResume() {
		
		// pager greetings ---------------------
		mPagerOnGreetingsTextView.setText(SmartPagerApplication.getInstance().getPreferences().getOnlinePagingRecordingTimestamp());
		mPagerOffGreetingsTextView.setText(SmartPagerApplication.getInstance().getPreferences().getOfflinePagingRecordingTimestamp());
		// autoresponce text -------------------
		TextView autoResponce = (TextView) findViewById(R.id.settings_auto_text_response_description);		
		if (SmartPagerApplication.getInstance().getPreferences().getAutoResponseEnabled() && !TextUtils.isEmpty(SmartPagerApplication.getInstance().getPreferences().getAutoResponse())){
			autoResponce.setVisibility(View.VISIBLE);
			autoResponce.setText(SmartPagerApplication.getInstance().getPreferences().getAutoResponse());
		} else {
			autoResponce.setVisibility(View.GONE);
		}
		// forward to text -------------------	
		TextView forwardTo = (TextView) findViewById(R.id.settings_forward_pages_description);		
		if (SmartPagerApplication.getInstance().getPreferences().getForwardPagesEnabled() && !TextUtils.isEmpty(SmartPagerApplication.getInstance().getPreferences().getForwardName())){
			forwardTo.setVisibility(View.VISIBLE);
			forwardTo.setText( getString(R.string.forward_to_) + SmartPagerApplication.getInstance().getPreferences().getForwardName() );
		} else {
			forwardTo.setVisibility(View.INVISIBLE);
		}		
		startGetProfile();
		super.onResume();
	}

	private void startGetProfile() {
//		Intent intent = new Intent(this, WebService.class);
//		intent.setAction(SmartPagerApplication.getActionName(WebAction.getProfile.name()));
//		startService(intent);
        sendSingleComand(WebAction.getProfile);
	}
	
	private void initGui() {

		initAlertSpinners();
		initIncomingCallModeSpinners();
		initVolume();
		initVibrationSwitch();
		initVoipSwitch();
		initPreferCellularSwitch();
		initPagerGreetings();
		initBlockSettings();		
		setOnClickListeners();
		// FIXME disable calls properties
		findViewById(R.id.settings_calls_properties).setVisibility(View.GONE);
		// initLogCallsRadiogroup();
		// initRecordCallsRadiogroup();
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.getProfile){
			// pager greetings ---------------------
			mPagerOnGreetingsTextView.setText(SmartPagerApplication.getInstance().getPreferences().getOnlinePagingRecordingTimestamp());
			mPagerOffGreetingsTextView.setText(SmartPagerApplication.getInstance().getPreferences().getOfflinePagingRecordingTimestamp());
			updateBlockNumberRadioGroup();
		}
        if(m_bIsSynchingContacts && action == WebAction.getContacts)
        {
            hideProgressDialog();
            showToast(SmartPagerApplication.getInstance().getString(R.string.resynch_contacts_finished));
            m_bIsSynchingContacts = false;
        }
	}

    @Override
    protected void onErrorResponse(WebAction action, AbstractResponse response) {
        if(action == WebAction.getContacts)
        {
            showErrorDialog(SmartPagerApplication.getInstance().getString(R.string.error_resynch_contacts));
        }
    }

    private void setOnClickListeners() {
		findViewById(R.id.settings_my_profile_layout).setOnClickListener(mOnGeneralSettingsClickListener);
		findViewById(R.id.settings_archive_layout).setOnClickListener(mOnGeneralSettingsClickListener);
		findViewById(R.id.settings_pin_layout).setOnClickListener(mOnGeneralSettingsClickListener);
		findViewById(R.id.settings_quick_responses_layout).setOnClickListener(mOnGeneralSettingsClickListener);
		findViewById(R.id.settings_auto_text_response_layout).setOnClickListener(mOnGeneralSettingsClickListener);
		findViewById(R.id.settings_forward_pages_layout).setOnClickListener(mOnGeneralSettingsClickListener);
        findViewById(R.id.settings_personal_verification_questions).setOnClickListener(mOnGeneralSettingsClickListener);
        findViewById(R.id.settings_bt_resynch).setOnClickListener(mOnGeneralSettingsClickListener);
	}

	private View.OnClickListener mOnGeneralSettingsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Class<? extends Activity> activityToCall = null;
			boolean needNetworkConnection = false;
			switch (v.getId()) {
				case R.id.settings_my_profile_layout:
					activityToCall = MyProfileActivity.class;
					needNetworkConnection = true;
				break;
				case R.id.settings_archive_layout:
					activityToCall = ArchiveSettingsActivity.class;
					needNetworkConnection = false;
				break;
				case R.id.settings_pin_layout:
					activityToCall = PinSettingsActivity.class;
					needNetworkConnection = false;
				break;
				case R.id.settings_quick_responses_layout:
					activityToCall = QuickResponsesActivity.class;
					needNetworkConnection = false;
				break;
				case R.id.settings_auto_text_response_layout:
					activityToCall = AutoTextResponseActivity.class;
					needNetworkConnection = true;
				break;
				case R.id.settings_forward_pages_layout:
					activityToCall = ForwardPagesActivity.class;
					needNetworkConnection = true;
				break;
                case R.id.settings_personal_verification_questions:
                    activityToCall = PersonalVerificationQuestionsActivity.class;
                    needNetworkConnection = true;
                    break;
                case R.id.settings_bt_resynch:
                    m_bIsSynchingContacts = true;
                    Bundle extras = new Bundle();
                    extras.putBoolean(WebSendParam.resetContacts.name(), true);
                    SmartPagerApplication.getInstance().startWebAction(WebAction.getContacts, extras);
                    showCancellableProgressDialog(R.string.synching_contacts, SettingsActivity.this);
//                    showProgressDialog(SmartPagerApplication.getInstance().getString(R.string.synching_contacts));
                    break;
				default:
				break;
			}
			if (activityToCall != null) {
				if (needNetworkConnection && !NetworkUtils.isInternetConnectedAsync()){
					showErrorDialog(getString(R.string.connection_is_required));					
				} else {				
					startActivity(new Intent(SettingsActivity.this, activityToCall));
				}
			}
		}
	};

	private void initAlertSpinners() {

		List<String> settingsAlerts = AlertFilesUtils.getRawFilenames();
		List<String> casualAlerts = AlertFilesUtils.getCasualRawFilenames();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_spinner_settings,
				settingsAlerts);
		ArrayAdapter<String> casualAdapter = new ArrayAdapter<String>(this, R.layout.item_spinner_settings,
				casualAlerts);
		
		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		casualAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		
		// critical alert
		final Spinner criticalAlertSpinner = (Spinner) findViewById(R.id.settings_critical_alert_spinner);
		criticalAlertSpinner.setAdapter(adapter);
		int criticalPosition = Math.min(SmartPagerApplication.getInstance().getSettingsPreferences().getCriticalAlert(), settingsAlerts.size() - 1);
		criticalAlertSpinner.setSelection(criticalPosition);
		// post - to avoid playing when spinners init
		criticalAlertSpinner.post(new Runnable() {
			@Override
			public void run() {
				criticalAlertSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

					@Override
					public void onItemSelected(Spinner spinner, View view, int position, long l) {
						setRingTone("critical", position);
						SmartPagerApplication.getInstance().getSettingsPreferences().setCriticalAlert(position);
						Notificator.soundcheckCriticalAlert();
					}
				});
			}
		});

		// normal alert
		final Spinner normalAlertSpinner = (Spinner) findViewById(R.id.settings_normal_alert_spinner);
		normalAlertSpinner.setAdapter(adapter);
		int normalPosition = Math.min(SmartPagerApplication.getInstance().getSettingsPreferences().getNormalAlert(), settingsAlerts.size() - 1);
		normalAlertSpinner.setSelection(normalPosition);
		normalAlertSpinner.post(new Runnable() {
			@Override
			public void run() {
				normalAlertSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {


					@Override
					public void onItemSelected(Spinner spinner, View view, int i, long l) {
						setRingTone("normal", i);
						SmartPagerApplication.getInstance().getSettingsPreferences().setNormalAlert(i);
						Notificator.soundcheckNormalAlert();
					}
				});
			}
		});
		
		// chat alert
		final Spinner chatAlertSpinner = (Spinner) findViewById(R.id.settings_casual_alert_spinner);
		chatAlertSpinner.setAdapter(casualAdapter);
		int casualPosition = Math.min(SmartPagerApplication.getInstance().getSettingsPreferences().getCasualAlert(), casualAlerts.size() - 1);
		chatAlertSpinner.setSelection(casualPosition);
		chatAlertSpinner.post(new Runnable() {
			@Override
			public void run() {
				chatAlertSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {


					@Override
					public void onItemSelected(Spinner spinner, View view, int position, long l) {
						setRingTone("casual", position);
						SmartPagerApplication.getInstance().getSettingsPreferences().setCasualAlert(position);
						Notificator.soundcheckCasualAlert();
					}
				});
			}
		});

	}
	
	private void setRingTone(String ringToneType, int position) {
		
		List<String> alertFileNames = AlertFilesUtils.getRawFilenames();
		
		if (ringToneType.equals("casual")) {
			alertFileNames = AlertFilesUtils.getCasualRawFilenames();
		}
		
		String ringTone = new String();
		try {
			ringTone = alertFileNames.get(position)+".mp3";
		} catch(IndexOutOfBoundsException e) {
			ringTone = "SmartPager.mp3";
		}
		
		RequestParams params = new RequestParams();
		params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
		params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
		params.put("alertType", ringToneType);
		params.put("ringTone", ringTone);
		
		AsyncHttpClient client = new AsyncHttpClient();
		client.post(getApplicationContext(), Constants.BASE_REST_URL + "/setRingTone", params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//				System.out.println("successful response: " + response);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
//				System.out.println("failure resonse: " + responseBody);
			}
		});
	}
	
	private void initIncomingCallModeSpinners() {
		final String[] inboundModeValues = getResources().getStringArray(R.array.settings_inbound_modes);

		ArrayAdapter<String> inboundModeAdapter = new ArrayAdapter<String>(this, R.layout.item_spinner_settings,
				inboundModeValues);
		inboundModeAdapter.setDropDownViewResource(
				R.layout.simple_spinner_dropdown_item);
		
		final Spinner inboundModeSpinner = (Spinner) findViewById(R.id.settings_inbound_mode_spinner);
		inboundModeSpinner.setAdapter(inboundModeAdapter);
		int inboundModePosition = IncomingCallModes.valueOf(SmartPagerApplication.getInstance().getPreferences().getIncomingCallMode()).ordinal();
		inboundModeSpinner.setSelection(inboundModePosition);
		inboundModeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(Spinner spinner, View view, int i, long l) {
				String selected = spinner.getAdapter().getItem(i).toString();

				Bundle extras = new Bundle();
				extras.putString(WebSendParam.incomingCallMode.name(), IncomingCallModes.values()[i].name());
				SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);

				SmartPagerApplication.getInstance().getPreferences().setIncomingCallMode(IncomingCallModes.values()[i].name());

			}
		});
		
		final String[] failoverToPageValues = getResources().getStringArray(R.array.settings_failover_to_page);
		
		ArrayAdapter<String> failoverToPageAdapter = new ArrayAdapter<String>(this, R.layout.item_spinner_settings, failoverToPageValues);
		failoverToPageAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		
		final Spinner failoverToPageSpinner = (Spinner) findViewById(R.id.settings_failover_to_page_spinner);
		failoverToPageSpinner.setAdapter(failoverToPageAdapter);
		int failoverToPagePosition = failoverToPageAdapter.getPosition(SmartPagerApplication.getInstance().getPreferences().getIncomingCallModeFailoverToPageSeconds() + " seconds");
		failoverToPageSpinner.setSelection(failoverToPagePosition);
		failoverToPageSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(Spinner spinner, View view, int i, long l) {
				String selected = spinner.getAdapter().getItem(i).toString();

				Bundle extras = new Bundle();
				extras.putString(WebSendParam.incomingCallModeFailoverToPageSeconds.name(), Integer.toString(5*(i + 1)));
				SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);

				SmartPagerApplication.getInstance().getPreferences().setIncomingCallModeFailoverToPageSeconds(Integer.toString(5*(i + 1)));

			}

			
		});
	}

	private void initVolume() {
		SeekBar volumeSeekBar = (SeekBar) findViewById(R.id.settings_volume_seekbar);
		volumeSeekBar.setMax(AudioManagerUtils.getStreamMaxVolume());
		volumeSeekBar.setProgress( SmartPagerApplication.getInstance().getSettingsPreferences().getVolume());
		volumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
	}

	private void initVibrationSwitch() {
		Switch vibrationSwitch = (Switch) findViewById(R.id.settings_vibration_switch);
		vibrationSwitch.setChecked(SmartPagerApplication.getInstance().getSettingsPreferences().getVibration());
		vibrationSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setVibration(isChecked);
			}
		});
	}

	private OnSeekBarChangeListener mSeekBarChangeListener = new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (fromUser) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setVolume(progress);
				AudioManagerUtils.setStreamVolume(progress);
			}
		}
	};

	private void initPagerGreetings() {
		findViewById(R.id.settings_pager_on_layout).setOnClickListener(mOnPagerGreetingsClickListener);
		findViewById(R.id.settings_pager_off_layout).setOnClickListener(mOnPagerGreetingsClickListener);
		
		mPagerOnGreetingsTextView = (TextView) findViewById(R.id.settings_pager_on_description);
		mPagerOffGreetingsTextView = (TextView) findViewById(R.id.settings_pager_off_description);				
		
	}

	private View.OnClickListener mOnPagerGreetingsClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!NetworkUtils.isInternetConnectedAsync()){
				showErrorDialog(getString(R.string.connection_is_required));
				return;
			}
			boolean isPagerOn = true;
			switch (v.getId()) {
				case R.id.settings_pager_on_layout:
					isPagerOn = true;
				break;
				case R.id.settings_pager_off_layout:
					isPagerOn = false;
				break;
				default:
				break;
			}
			Intent intent = new Intent(SettingsActivity.this, GreetingPlayerActivity.class);
			intent.putExtra(BundleKey.isPagerOn.name(), isPagerOn);
			startActivity(intent);
		}
	};

	private void initBlockSettings() {
		mBlockMyNumberLabel = (TextView) findViewById(R.id.settings_block_my_number_label);
		
		mBlockingPrefixEditText = (EditText) findViewById(R.id.settings_blocking_prefix_edittext);		
		mBlockingPrefixEditText.setText( SmartPagerApplication.getInstance().getSettingsPreferences().getBlockingPrefix());
		mBlockingPrefixEditText.setOnKeyListener( new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_UP 
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					SmartPagerApplication.getInstance().getSettingsPreferences().setBlockingPrefix(mBlockingPrefixEditText.getText().toString());
					hideSoftKeyboard(mBlockingPrefixEditText);
				}
				return false;
			}
		});
		
		blockRadioGroup = (RadioGroup) findViewById(R.id.settings_block_number_radiogroup);
        updateBlockNumberRadioGroup();

		blockRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				CallsSettings block = getCallSettingsByID(checkedId);
                String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
                String routingNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
                if(block == CallsSettings.on && TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routingNumber))
                    showErrorDialog(R.string.block_my_number_error);
                else
    				if (SmartPagerApplication.getInstance().getPreferences().getRestrictCallBlocking() == false) {
    					SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(block);
    					
    					switch (block) {
    						case on:
    							SmartPagerApplication.getInstance().getPreferences().setCallBlocking("on");
    							break;
    						case off:
    							SmartPagerApplication.getInstance().getPreferences().setCallBlocking("off");
    							break;
    						case ask:
    							SmartPagerApplication.getInstance().getPreferences().setCallBlocking("ask");
    							break;
    						default:
    							break;
    					}
    					
    					Bundle extras = new Bundle();
    					extras.putString(WebSendParam.callBlocking.name(), SmartPagerApplication.getInstance().getPreferences().getCallBlocking());
    					SmartPagerApplication.getInstance().startWebAction(WebAction.setProfile, extras);
    				}
                updateBlockNumberRadioGroup();
			}
		});
	}

    private CallsSettings getCallSettingsByID(int checkedViewID)
    {
        CallsSettings res = CallsSettings.ask;
        switch (checkedViewID) {
            case R.id.settings_block_number_on:
            case R.id.settings_record_calls_on:
            case R.id.settings_log_calls_on:
                res = CallsSettings.on;
                break;
            case R.id.settings_block_number_off:
            case R.id.settings_record_calls_off:
            case R.id.settings_log_calls_off:
                res = CallsSettings.off;
                break;
            case R.id.settings_block_number_ask:
            case R.id.settings_record_calls_ask:
            case R.id.settings_log_calls_ask:
                res = CallsSettings.ask;
                break;
            default:
                break;
        }
        return res;
    }

    private void updateBlockNumberRadioGroup()
    {
        updateSettingsRadioGroup(R.id.settings_block_number_radiogroup, R.id.settings_block_number_on, R.id.settings_block_number_off, R.id.settings_block_number_ask);
        
        
        if (SmartPagerApplication.getInstance().getPreferences().getRestrictCallBlocking()) {
        	for (int i = 0; i < blockRadioGroup.getChildCount(); i++) {
        		mBlockMyNumberLabel.setText(R.string.settings_block_my_number_set_on_server);
    			((RadioButton)blockRadioGroup.getChildAt(i)).setEnabled(false);
    			((RadioButton)blockRadioGroup.getChildAt(i)).getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    		}
        } else {
        	for (int i = 0; i < blockRadioGroup.getChildCount(); i++) {
        		mBlockMyNumberLabel.setText(R.string.settings_block_my_number);
    			((RadioButton)blockRadioGroup.getChildAt(i)).setEnabled(true);
    			((RadioButton)blockRadioGroup.getChildAt(i)).getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
    		}
        }
        
    }

    private void updateSettingsRadioGroup(int radioGroupID, int btnOnID, int btnOffID, int btnAskID)
    {
        RadioGroup blockRadioGroup = (RadioGroup) findViewById(radioGroupID);
        int checkedRadioButton = btnAskID;
        switch (SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber()) {
            case on:
                checkedRadioButton = btnOnID;
                break;
            case off:
                checkedRadioButton = btnOffID;
                break;
            case ask:
                checkedRadioButton = btnAskID;
                break;
            default:
                break;
        }
        blockRadioGroup.check(checkedRadioButton);
    }
    
    private void initVoipSwitch() {
    	Boolean voipAllowed = SmartPagerApplication.getInstance().getPreferences().getVoipAllowed();
    	
    	TextView voipSwitchLabel = (TextView) findViewById(R.id.settings_voip_switch_label);
		Switch voipSwitch = (Switch) findViewById(R.id.settings_voip_switch);
		
		if (voipAllowed) {
			voipSwitch.setChecked(SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled());
			voipSwitchLabel.setText(R.string.settings_voip);
			voipSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					SmartPagerApplication.getInstance().getSettingsPreferences().setVoipToggled(isChecked);
				}
			});
		} else {
			voipSwitch.setChecked(false);
			voipSwitch.setEnabled(false);
			voipSwitchLabel.setText(R.string.settings_voip_disabled);
		}
		
		
	}

	private void initPreferCellularSwitch() {
		Boolean preferCellular = SmartPagerApplication.getInstance().getPreferences().getPreferCellular();

		TextView preferCellularSwitchLabel = (TextView) findViewById(R.id.settings_prefer_cellular_switch_label);
		Switch preferCellularSwitch = (Switch) findViewById(R.id.settings_prefer_cellular_switch);

		preferCellularSwitch.setChecked(SmartPagerApplication.getInstance().getSettingsPreferences().getPreferCellularToggled());
		preferCellularSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SmartPagerApplication.getInstance().getSettingsPreferences().setPreferCellularToggled(isChecked);
			}
		});
	}

	@SuppressWarnings("unused")
	private void initLogCallsRadiogroup() {
		RadioGroup logCallsRadioGroup = (RadioGroup) findViewById(R.id.settings_log_calls_radiogroup);
        updateSettingsRadioGroup(R.id.settings_log_calls_radiogroup, R.id.settings_log_calls_on, R.id.settings_log_calls_off, R.id.settings_log_calls_ask);

		logCallsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				CallsSettings log = getCallSettingsByID(checkedId);
				SmartPagerApplication.getInstance().getSettingsPreferences().setLogCalls(log);
			}
		});
	}

	@SuppressWarnings("unused")
	private void initRecordCallsRadiogroup() {
		RadioGroup recordCallsRadioGroup = (RadioGroup) findViewById(R.id.settings_record_calls_radiogroup);
        updateSettingsRadioGroup(R.id.settings_record_calls_radiogroup, R.id.settings_record_calls_on, R.id.settings_record_calls_off, R.id.settings_record_calls_ask);

		recordCallsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				CallsSettings record = getCallSettingsByID(checkedId);
				SmartPagerApplication.getInstance().getSettingsPreferences().setRecordCalls(record);
			}
		});
	}
	
	@Override
	protected void onPause() {
		SmartPagerApplication.getInstance().getSettingsPreferences().setBlockingPrefix(mBlockingPrefixEditText.getText().toString());
		super.onPause();
	}

    @Override
    public void onProgressDialogCancelled() {
        Bundle extras = new Bundle();
        extras.putString(BundleKey.canceledWebAction.name(), WebAction.getContacts.name());
        SmartPagerApplication.getInstance().startWebAction(WebAction.cancelWebService, extras);
    }

    @Override
    protected void onCancelledWebAction(WebAction action, AbstractResponse response) {
        if(action == WebAction.getContacts)
            showToast(R.string.resynch_cancelled);
        else
            super.onCancelledWebAction(action, response);
    }
}
