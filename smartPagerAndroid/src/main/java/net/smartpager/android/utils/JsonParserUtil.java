package net.smartpager.android.utils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.WebSendParam;

import org.json.JSONArray;

public class JsonParserUtil {
	
	public static String getStringFromJsonArray(JSONArray array, String delimiter) {
		StringBuilder builder = new StringBuilder();
		if (array != null) {
			for (int i = 0; i != array.length(); i++) {
				if (i > 0) {
					builder.append(delimiter);
				}
				builder.append(array.opt(i));
			}
		}
		return builder.toString();
	}

	public static String getUrl(String id) {
		return String.format("%s/getProfilePicture?smartPagerID=%s&%s=%s&contactSmartPagerID=%s",
                Constants.BASE_REST_URL, SmartPagerApplication.getInstance().getPreferences().getUserID(), WebSendParam.uberPassword.name(),
                SmartPagerApplication.getInstance().getPreferences().getPassword(), id);
	}
	
	public static String getCapitalizeString(String value){
		
		String firstChar = value.substring(0, 1);
		return firstChar + value.toLowerCase().substring(1);
	}
}
