package net.smartpager.android.consts;

import java.util.Calendar;
import java.util.Date;

public class Constants {
	public static final int SEND_REQUEST_MAX_COUNT = 2;
	public static final int PHONE_LENGTH = 10;
	public static final int HTTP_CLIENT_TIMEOUT = 20000;
	public static final int HTTP_CLIENT_CONNECTION_TIMEOUT = 20000;
	public static final int CHECK_CONNECTION_TIMEOUT = 1000;
//    public static final String BASE_URL = "https://can.smartpager.net";
//    public static final String BASE_URL = "https://app.smartpager.net";
	public static final String BASE_URL = "https://test.smartpager.net";
	public static final String BASE_REST_URL = String.format("%s/rest",BASE_URL);
	public static final Date DEFAULT_DATE;
    public static final String CRITTERCISM_APP_KEY = "52e18a35e432f501e0000008";
	
	
	static{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date(0));
		DEFAULT_DATE = calendar.getTime();
	}
}
