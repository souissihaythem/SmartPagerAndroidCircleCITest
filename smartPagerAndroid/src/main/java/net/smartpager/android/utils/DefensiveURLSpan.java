package net.smartpager.android.utils;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.activity.ChatActivity;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.fragment.dialog.AlertFragmentDialogYesNO;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

public class DefensiveURLSpan extends URLSpan {
	Context mContext;

	public DefensiveURLSpan(String url, Context context) {
		super(url);
		mContext = context;
	}
	
	@Override
	public void onClick(View widget) {
		try {
			final ChatActivity activity = (ChatActivity) mContext;
	        Uri clickedUri = Uri.parse(getURL());
	        if (clickedUri.toString().contains("http:")) {
	        	super.onClick(widget);
	        } else if (clickedUri.toString().contains("tel:")) {
	        	final String number = clickedUri.toString().replace("tel:", "");
	        	
	        	final String strippedNumber = number.replaceAll("[^0-9]", "");
	        	Log.d("DefensiveURLSpan", "number clicked: " + strippedNumber);
	        	
	        	final EditText areaCodeET = new EditText(mContext);
	        	areaCodeET.setHint("Area Code (3 digits)");
	        	areaCodeET.setInputType(InputType.TYPE_CLASS_NUMBER);
	        	
	        	if (number.length() < 10) {
	        		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	        		builder
	        			.setView(areaCodeET)
	        			.setMessage("Please enter the area code for " + strippedNumber)
		        		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (areaCodeET.getText().toString().length() != 3) {
									AlertDialog.Builder error = new AlertDialog.Builder(mContext);
									error.setMessage("Could not dial the number " + areaCodeET.getText().toString() + "" + number)
									.setPositiveButton("OK", null)
									.show();
									
								} else {
								
								Log.d("DefensiveURLSpan", "[[[: " + areaCodeET.getText().toString() + strippedNumber);

								if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
				        			makeVoipCall(number);
				        			return;
				        		}
								
									switch(SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber()) {
						        		case off:
						        			TelephoneUtils.dial(areaCodeET.getText().toString() + strippedNumber);
						        			break;
						        		case ask:
					                        activity.showDialogBlockId(areaCodeET.getText().toString() + strippedNumber);
						        			break;
						        		case on:
					                        activity.startArmCallback(areaCodeET.getText().toString() + strippedNumber);
						        			break;
						        		default:
						        			activity.startArmCallback(areaCodeET.getText().toString() + strippedNumber);
						        			break;
						        			
						        	}
								}
							}
						})
						.setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								
							}
						});
	        		AlertDialog dialog = builder.create();
	        		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	        		dialog.show();
	        		
	        	} else {
	        		
	        		if (SmartPagerApplication.getInstance().getSettingsPreferences().getVoipToggled()) {
	        			makeVoipCall(number);
	        			return;
	        		}
		        	
		        	
		        	switch(SmartPagerApplication.getInstance().getSettingsPreferences().getBlockMyNumber()) {
		        		case off:
		        			TelephoneUtils.dial(number);
		        			break;
		        		case ask:
	                        activity.showDialogBlockId(number);
		        			break;
		        		case on:
	                        activity.startArmCallback(number);
		        			break;
		        		default:
		        			activity.startArmCallback(number);
		        			break;
		        			
		        	}
	        	}
	        }
	        
	        
		} catch (ActivityNotFoundException e) {
			// do something useful here
		}
	}
	
	public void makeVoipCall(final String number) {
		AudioManagerUtils.setLoudSpeaker(false);
		((BaseActivity) mContext).showProgressDialog("Connecting VOIP Call");
    	RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        
        AsyncHttpClient httpClient = new AsyncHttpClient();        
        httpClient.post(mContext, Constants.BASE_REST_URL + "/getTwilioClientToken", params, new JsonHttpResponseHandler() {
        	
        	@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("successful response: " + response);
				((BaseActivity) mContext).hideProgressDialog();
				
				VoipPhone voipPhone = new VoipPhone(((BaseActivity) mContext).getApplicationContext(), response.optString("token"));
				voipPhone.connect(number);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
				System.out.println("failure response: " + responseBody);
				((BaseActivity) mContext).hideProgressDialog();
			}
        	
        });
    }
	

}
