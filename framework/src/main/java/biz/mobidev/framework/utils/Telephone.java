package biz.mobidev.framework.utils;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Telephone {

	private static TelephonyManager getTelephonyManager(Context context){
		return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}
	public static boolean isHasRadioModule(Context context){
		boolean result = false;
		TelephonyManager telephonyManager = getTelephonyManager(context);
		if(telephonyManager.getPhoneType()!= TelephonyManager.PHONE_TYPE_NONE){
			result = true;
		}
		return result;
	}
	

	public static boolean isSimRady(Context context){
		boolean result = false;
		TelephonyManager telephonyManager = getTelephonyManager(context);
		if(isHasRadioModule(context)){
			if(telephonyManager.getSimState()== TelephonyManager.SIM_STATE_READY){
				result = true;
			}
		}
		return result;
	}
}
