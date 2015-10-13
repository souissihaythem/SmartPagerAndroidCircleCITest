package net.smartpager.android.consts;

import android.os.Environment;

import net.smartpager.android.SmartPagerApplication;

import java.io.File;

public class FileNames {	
	public static final String AUDIO_EXT = ".3gp";
	public static final String APP_ABSOLUTE_PATH = 
			SmartPagerApplication.getInstance().getApplicationContext().getFilesDir().getAbsolutePath();
	public static final String PACKAGE_NAME = 
			SmartPagerApplication.getInstance().getApplicationContext().getPackageName();
	public static final File DATA_DIR = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
	public static final File CACHE_DIR = new File(new File(DATA_DIR, PACKAGE_NAME), "cache");
	
	//public static final String VOICE_MESSAGE 				=	APP_ABSOLUTE_PATH	+ "/voice_message" 		+ AUDIO_EXT;
//	public static final String PAGER_ON_GREETING_DEFAULT 	= 	APP_ABSOLUTE_PATH	+ "/pager_on_greeting" 	+ AUDIO_EXT;
//	public static final String PAGER_OFF_GREETING_DEFAULT = 	APP_ABSOLUTE_PATH	+ "/pager_off_greeting" + AUDIO_EXT;
//	public static final String PAGER_TEMP_GREETING 		=	APP_ABSOLUTE_PATH 	+ "/temp_greeting" 		+ AUDIO_EXT;
}
