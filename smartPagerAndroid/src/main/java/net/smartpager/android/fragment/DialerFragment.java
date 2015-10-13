package net.smartpager.android.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.CallsSettings;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.dialer.DialpadKeyButton;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.receivers.OutgoingCallReceiver;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.activity.NewMessageActivity;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.CallsSettings;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.dialer.DialpadKeyButton;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.receivers.OutgoingCallReceiver;
import net.smartpager.android.utils.AudioManagerUtils;
import net.smartpager.android.utils.TelephoneUtils;

import com.twilio.sdk.TwilioClient;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

// Show dialer
public class DialerFragment extends Fragment implements DialpadKeyButton.OnPressedListener, View.OnLongClickListener, View.OnClickListener, OnDialogDoneListener {

	private EditText 		digitEntry;
	private View			deleteButton;
	private ProgressDialog 	mProgressDialog;
	protected static AlertFragmentDialog mFragmentDialog;
	private String 			mFormattedNumber;
	private AsYouTypeFormatter aytf;
	private View fragmentView;
	private PhoneStateListener mPhoneStateListener;
	private RadioGroup blockRadioGroup;
	private TextView blockMyNumberLabel;
	
	private VoipPhone voipPhone;
	
	private AsyncHttpClient httpClient;
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d("DialerFragment", "onResume");
//		updateBlockNumberRadioGroup(fragmentView);
		updateVoipSwitch(fragmentView);
	}
	
	private void getProfile() {
		RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        
        httpClient.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/getProfile", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				hideProgressDialog();
				
				JSONObject jsonObject = response.optJSONObject("profile");
				
				
				Log.e("DialerFragment", "response.optBoolean('restrictCallBlocking'): " + jsonObject.optBoolean("restrictCallBlocking"));
				Log.e("DialerFragment", "response.optString('callBlocking'): " + jsonObject.optString("callBlocking"));
				
				
				SmartPagerApplication.getInstance().getPreferences().setRestrictCallBlocking(jsonObject.optBoolean("restrictCallBlocking", false));
				SmartPagerApplication.getInstance().getPreferences().setCallBlocking(jsonObject.optString("callBlocking", "off"));
				if (jsonObject.optString("callBlocking").equalsIgnoreCase("on")) {
					SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.on);
				} else if (jsonObject.optString("callBlocking").equalsIgnoreCase("off")) {
					SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.off);
				} else if (jsonObject.optString("callBlocking").equalsIgnoreCase("ask")) {
					SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.ask);
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				hideProgressDialog();
			}
        	
        });
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		fragmentView = inflater.inflate(R.layout.fragment_dialer, container, false);
		
		Log.e("DialerFragment", "onCreateView");
		
		httpClient = new AsyncHttpClient();
		mFormattedNumber = new String();
		aytf = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(Locale.getDefault().getCountry());

		initializeCallButton(fragmentView);
		initializeDigitEntry(fragmentView);
		initializeDeleteButton(fragmentView);
		initializeKeyPad(fragmentView);
		initVoipSwitch(fragmentView);
		
		mPhoneStateListener = new PhoneStateListener() {
			public void onCallStateChanged(int state, String incomingNumber) {
				switch(state) {
					case TelephonyManager.CALL_STATE_RINGING:
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						digitEntry.getText().clear();
						aytf.clear();
						mFormattedNumber = new String();
						break;
				}
			}
		};
		
		TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		return fragmentView;
	}
	
	

	private void initializeCallButton(View fragmentView) {
		LinearLayout callButton = (LinearLayout) fragmentView.findViewById(R.id.call_button);
		callButton.setOnClickListener(this);
	}

	private void initializeDigitEntry(View fragmentView) {
		digitEntry = (EditText) fragmentView.findViewById(R.id.digits);
		digitEntry.setOnClickListener(this);
		digitEntry.setOnLongClickListener(this);
	}

	private void initializeDeleteButton(View fragmentView) {
		deleteButton = fragmentView.findViewById(R.id.deleteButton);
		if (deleteButton != null) {
			deleteButton.setOnClickListener(this);
			deleteButton.setOnLongClickListener(this);
		}
	}

	private void initializeKeyPad(View fragmentView) {
		int[] buttonIds = new int[] {R.id.zero, R.id.one, R.id.two, R.id.three, R.id.four,
				R.id.five, R.id.six, R.id.seven, R.id.eight, R.id.nine,
				R.id.star, R.id.pound};

		String[] numbers = new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "", ""};
		String[] letters = new String[] {"", "", "ABC", "DEF", "GHI","JKL", "MNO", "PQRS", "TUV", "WXYZ", "", ""};

		DialpadKeyButton dialpadKey;
		TextView         numberView;
		TextView         lettersView;

		for (int i = 0; i < buttonIds.length; i++) {
			dialpadKey = (DialpadKeyButton) fragmentView.findViewById(buttonIds[i]);
			dialpadKey.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.MATCH_PARENT));
			dialpadKey.setOnPressedListener(this);
			numberView  = (TextView) dialpadKey.findViewById(R.id.dialpad_key_number);
			lettersView = (TextView) dialpadKey.findViewById(R.id.dialpad_key_letters);

			numberView.setText(numbers[i]);
			dialpadKey.setContentDescription(numbers[i]);

			if (lettersView != null) {
				lettersView.setText(letters[i]);
			}
		}

		fragmentView.findViewById(R.id.zero).setOnLongClickListener(this);
	}
	
	private void keyPressed(int keyCode) {
		KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		
		if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mFormattedNumber.length() > 0) {
				
				String cleanString = mFormattedNumber.replaceAll("[^0-9]", "");
				
				mFormattedNumber = cleanString.substring(0, cleanString.length()-1);
				Log.d("DialerFragment", "delete button");
				
				String[] ary = mFormattedNumber.split("");
				
				List<String> listOfStrings = new ArrayList<String>(Arrays.asList(ary));
				if (listOfStrings.get(0).length() == 0) {
					listOfStrings.remove(0);
				}
				
				aytf.clear();

				for (String str : listOfStrings) {
					Log.d("DialerFragment",  "str: " + str);
					char theChar = str.charAt(0);
					Log.d("DialerFragment", "char: " + theChar);
					mFormattedNumber = aytf.inputDigit(theChar);
				}
			}
			digitEntry.setText(mFormattedNumber);
			return;
		}
		
		if (mFormattedNumber.length() == 14) {
			return;
		}
		
		mFormattedNumber = aytf.inputDigit(event.getNumber());
		
		Log.d("DialerFragment", "mFormattedNumbeR: " + mFormattedNumber);
		digitEntry.setText(mFormattedNumber);
				
		final int length = digitEntry.length();
		if (length == digitEntry.getSelectionStart() && length == digitEntry.getSelectionEnd()) {
			digitEntry.setCursorVisible(false);
		}
	}
	
	private void removePreviousDigitIfPossible() {
		final int currentPosition = digitEntry.getSelectionStart();
		
		if (currentPosition > 0) {
			digitEntry.setSelection(currentPosition);
			digitEntry.getText().delete(currentPosition - 1, currentPosition);
		}
	}

	@Override
	public void onPressed(View view, boolean pressed) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ? !pressed : pressed) return;
		
		

		switch (view.getId()) {
			case R.id.one:   keyPressed(KeyEvent.KEYCODE_1);     break;
			case R.id.two:   keyPressed(KeyEvent.KEYCODE_2);     break;
			case R.id.three: keyPressed(KeyEvent.KEYCODE_3);     break;
			case R.id.four:  keyPressed(KeyEvent.KEYCODE_4);     break;
			case R.id.five:  keyPressed(KeyEvent.KEYCODE_5);     break;
			case R.id.six:   keyPressed(KeyEvent.KEYCODE_6);     break;
			case R.id.seven: keyPressed(KeyEvent.KEYCODE_7);     break;
			case R.id.eight: keyPressed(KeyEvent.KEYCODE_8);     break;
			case R.id.nine:  keyPressed(KeyEvent.KEYCODE_9);     break;
			case R.id.zero:  keyPressed(KeyEvent.KEYCODE_0);     break;
			case R.id.pound: break;
			case R.id.star:  break;
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.deleteButton:
				keyPressed(KeyEvent.KEYCODE_DEL);
				return;
			case R.id.digits:
				if (!(digitEntry.length() == 0)) {
					digitEntry.setCursorVisible(true);
				}
				return;
			case R.id.call_button:
				String number = digitEntry.getText().toString().replaceAll("[^0-9]", "");

				if (number == null || number.trim().length() == 0) {
					Toast.makeText(getActivity(),
							"You must dial a number first.",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				if (number.trim().length() != 10) {
					Toast.makeText(getActivity(),
							"Enter a 10 digit number.",
							Toast.LENGTH_LONG).show();
					return;
				}
				
				if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
					makeVoipCall(number);
					return;
				}
				
		        switch (SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber()) {
		            case off:
		                TelephoneUtils.dial(number);
		                break;
		            case ask:
		                showDialogBlockId(number);
		                break;
		            case on:
		                startArmCallback(number);
		                break;
		            default:
		            	startArmCallback(number);
		                break;
		        }
		}

	}

	@Override
	public boolean onLongClick(View view) {
		Editable digits = digitEntry.getText();
		int      id     = view.getId();

		switch (id) {
			case R.id.deleteButton: {
				digits.clear();
				aytf.clear();
				// TODO: The framework forgets to clear the pressed
				// status of disabled button. Until this is fixed,
				// clear manually the pressed status. b/2133127
				deleteButton.setPressed(false);
				return true;
			}
			case R.id.zero: {
				// Remove tentative input ('0') done by onTouch().
				// removePreviousDigitIfPossible();
				// keyPressed(KeyEvent.KEYCODE_PLUS);
				return true;
			}
			case R.id.digits: {
				digitEntry.setCursorVisible(true);
				return false;
			}
		}
		return false;
	}
	
	public void hideSoftKeyboard(View view) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
	
	private void initVoipSwitch(final View fragmentView) {
    	Boolean voipAllowed = SmartPagerApplication.getInstance().getPreferences().getVoipAllowed();
    	
    	TextView voipSwitchLabel = (TextView) fragmentView.findViewById(R.id.dialer_voip_switch_label);
		Switch voipSwitch = (Switch) fragmentView.findViewById(R.id.dialer_voip_switch);
		
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
	
	private void updateVoipSwitch(View fragmentView) {
		initVoipSwitch(fragmentView);
	}
    
    public void showDialogBlockId(String number) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getActivity().getApplicationContext(), getString(R.string.security),
                getString(R.string.do_you_want_to_block_your_id), number, true, this);
        dialog.show(transaction, FragmentDialogTag.AlertInforDialogYesNo.name());
    }
    
    protected void showDialogUnableBlock(String number) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        AlertFragmentDialogYesNO dialog = AlertFragmentDialogYesNO.newInstance(getActivity().getApplicationContext(), getString(R.string.security),
                getString(R.string.we_are_unable_to_block), number, false, this);
        dialog.show(transaction, FragmentDialogTag.UnableToBlockId.name());
    }
    
    public void startArmCallback(final String number) {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.phoneNumber.name(), number);
//        SmartPagerApplication.getInstance().startWebAction(WebAction.armCallback, extras);
        showProgressDialog(getString(R.string.sending_request));
        
        RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        params.put("phoneNumber", number);
        
        httpClient.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/armCallback", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				hideProgressDialog();
				makeArmCallBack(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				hideProgressDialog();
			}
        	
        });
        
    }
    
    public void makeVoipCall(final String number) {
    	AudioManagerUtils.setLoudSpeaker(false);
    	showProgressDialog("Connecting VOIP Call");
    	RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
//        params.put("phoneNumber", number);
        
        httpClient.post(getActivity().getApplicationContext(), Constants.BASE_REST_URL + "/getTwilioClientToken", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				hideProgressDialog();
//				makeArmCallBack(number);
				
				voipPhone = new VoipPhone(getActivity().getApplicationContext(), response.optString("token"));
				voipPhone.connect(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				hideProgressDialog();
			}
        	
        });
    }
    
    public void makeArmCallBack(String number) {
        String pagerNumber = SmartPagerApplication.getInstance().getPreferences().getPagerNumber();
        String routerNumber = SmartPagerApplication.getInstance().getPreferences().getCallRouterPhoneNumber();
        
        if (TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + !RouterNo
            showErrorDialog(getString(R.string.your_pager_number_is_invalid));
        } else if (!TextUtils.isEmpty(pagerNumber) && TextUtils.isEmpty(routerNumber)) {
        	// PagerNo + !RouterNo
            pushOutgoingCallNumber(number);
            TelephoneUtils.dial(pagerNumber);
        } else if (TextUtils.isEmpty(pagerNumber) && !TextUtils.isEmpty(routerNumber)) {
        	// !PagerNo + RouterNo
        	pushOutgoingCallNumber(number);
            TelephoneUtils.dial(routerNumber);
        } else {
        	// PagerNo + RouterNo
        	pushOutgoingCallNumber(number);
            TelephoneUtils.dial(pagerNumber);
        }
    }
    
    private void pushOutgoingCallNumber (String number)
    {
        Intent outgoingReceiver = OutgoingCallReceiver.createOutgoingCallNumberAction(number, null);
        getActivity().sendBroadcast(outgoingReceiver);
    }
    
    public void showProgressDialog(int resID)
    {
        showProgressDialog(getString(resID));
    }

	public void showProgressDialog(String message) {
		try {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(getActivity(), R.style.ThemeDialogAccountStyle);
				mProgressDialog.setIndeterminate(false);
				mProgressDialog.setMessage(message);
				mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mProgressDialog.setCancelable(false);
				mProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
				mProgressDialog.show();
			}
		} catch (Exception e) {
			mProgressDialog = null;
		}
	}
	
	public void hideProgressDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.hide();
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
	
	protected void showErrorDialog(int resID)
    {
        showErrorDialog(getString(resID));
    }

	protected void showErrorDialog(String message) {
		if (mFragmentDialog != null) {
			mFragmentDialog.dismiss(); // to avoid multiple dialogs at same time
		};
		mFragmentDialog = AlertFragmentDialog.newErrorInstance(message);
		mFragmentDialog.show(getFragmentManager(), FragmentDialogTag.AlertInforDialog.name());
	}

    
    @Override
    public void onDialogDone(FragmentDialogTag tag, String data) {
        switch (tag) {
            case AlertInforDialogYesNo:
            	Log.d("DialerFragment", "Yes clicked");
                startArmCallback(data);
                break;
//            case IsExit:
//                File voiceMessage = new File(getActivityRecordFilePath());
//                if (voiceMessage.exists()) {
//                    voiceMessage.delete();
//                }
//                finish();
//                break;
//            case ForwardMessage:
//                Intent intent = new Intent(this, NewMessageActivity.class);
//                intent.putExtra(BundleKey.messageId.name(), mMessageIdToForward);
//                startActivity(intent);
//                break;
//            case UnableToBlockId:
//                pushOutgoingCallNumber(mCallbackNumber, mCallBackMessageID);
//                TelephoneUtils.dial(data);
//                break;
            case AlertBlockMyNumber:
                SmartPagerApplication.getInstance().getSettingsPreferences().setBlockMyNumber(CallsSettings.off);
                Toast.makeText(getActivity(), R.string.block_my_number_turned_off, Toast.LENGTH_LONG).show();
                startArmCallback(data);
                break;
            default:
                break;
        }

    }

    @Override
    public void onDialogNo(FragmentDialogTag tag, String data) {
        switch (tag) {
            case AlertInforDialogYesNo:
                pushOutgoingCallNumber(data);
                TelephoneUtils.dial(data);
                break;
            default:
                break;
        }
    }

}
