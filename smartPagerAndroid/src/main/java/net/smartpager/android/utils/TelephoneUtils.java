package net.smartpager.android.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;

public class TelephoneUtils {
	public static void dial(String phoneNumber){
		Context context = SmartPagerApplication.getInstance().getBaseContext();
		try {							
			TelephonyManager telManager = 
					(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

			if (	(telManager.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM 
					|| telManager.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
					&& (telManager.getNetworkType() != TelephonyManager.NETWORK_TYPE_UNKNOWN)) {
				String uri = String.format("tel:%s", phoneNumber.trim());
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse(uri));
				context.startActivity(intent);
			} else {
				Toast.makeText(	context, 
								R.string.not_mobile_connection_to_make_a_call,
								Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Toast.makeText(	context, 
					R.string.failed_to_call_contact,
					Toast.LENGTH_LONG).show();
		}
	}
	
	public static String format(String inputNumber){
		if (TextUtils.isEmpty(inputNumber)){
			return "";
		}
		
		if (inputNumber.length() < 10){
			return inputNumber;
		}
		if (inputNumber.startsWith("1")){
			inputNumber = inputNumber.substring(1);
		}
		return String.format("(%s) %s-%s", 
				inputNumber.substring(0, 3),
				inputNumber.substring(3, 6),
				inputNumber.substring(6, inputNumber.length()));
		
		
	}
	
	
	public static String formatIgnoreLength(String inputNumber){
		String[] d = {"_", "_", "_", "_", "_", "_", "_", "_", "_", "_"};		
		if (inputNumber != null){
			char[] ca = inputNumber.toCharArray();
			for (int i = 0; i < ca.length; i++){
				if (i >= d.length){
					break;
				}
				d[i] = String.valueOf(ca[i]);				
			}
		}
		return String.format("(%s%s%s) %s%s%s-%s%s%s%s", 
				d[0],d[1],d[2],d[3],d[4],d[5],d[6],d[7],d[8],d[9] );		
		
	}

	public static String toNumber(String formattedNumber){	
		StringBuilder builder = new StringBuilder();
		for (char c : formattedNumber.toCharArray()){
			String s = String.valueOf(c);
			if (TextUtils.isDigitsOnly(s)){
				builder.append(s);
			}
		}			
		return builder.toString();
	}
	
	
}
