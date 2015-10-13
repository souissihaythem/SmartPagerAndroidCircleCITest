package net.smartpager.android.notification;

import android.content.Context;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class-helper for work with alert mp3-files in Raw-directory  
 * @author Roman
 */
public class AlertFilesUtils {

	private static Map<String, String> mMap;
	private static Map<String, String> mCasualMap;
	private static Map<String, Integer> mLengthMap;
	private static Map<String, Integer> mCasualLengthMap;
	private static final String IGNORED = "Ignored";

	static {
		mMap = new HashMap<String, String>();
		mMap.put("chat_default", "Chat Default");
		mMap.put("beep", "Beep");
		mMap.put("knock", "Knock");
		mMap.put("strum", "Strum");
		mMap.put("ffmpeg", IGNORED);
		mMap.put("arrival", "Arrival");
		mMap.put("bells", "Bells");
		mMap.put("chime", "Chime");
		mMap.put("chime2", IGNORED);
		mMap.put("classic", "Classic");
		mMap.put("delivery", IGNORED);
		mMap.put("ding", "Ding");
		mMap.put("echo", "Echo");
		mMap.put("gentle", "Gentle");
		mMap.put("inbound_chat_new_message", IGNORED);
		mMap.put("modern", "Modern");
		mMap.put("sentmessages_chat", IGNORED);
		mMap.put("smart_pager", "SmartPager");
		mMap.put("silent", IGNORED);
		mMap.put("sox", IGNORED);
		mMap.put("disconnect", IGNORED);
		mMap.put("outgoing", IGNORED);
		mMap.put("incoming", IGNORED);
		
		mCasualMap = new HashMap<String, String>();
		mCasualMap.putAll(mMap);
		mCasualMap.put("silent", "Silent");

		mLengthMap = new HashMap<String, Integer>();
		mLengthMap.put("chat_default", 1000);
		mLengthMap.put("beep", 1000);
		mLengthMap.put("knock", 1000);
		mLengthMap.put("strum", 5500);
		mLengthMap.put("ffmpeg", 1);
		mLengthMap.put("arrival", 2500);
		mLengthMap.put("bells", 5500);
		mLengthMap.put("chime", 3500);
		mLengthMap.put("chime2", 1500);
		mLengthMap.put("classic", 4500);
		mLengthMap.put("delivery", 1500);
		mLengthMap.put("ding", 4500);
		mLengthMap.put("echo", 2500);
		mLengthMap.put("gentle", 1500);
		mLengthMap.put("inbound_chat_new_message", 1000);
		mLengthMap.put("modern", 2500);
		mLengthMap.put("sentmessages_chat", 1000);
		mLengthMap.put("smart_pager", 5500);
		mLengthMap.put("silent", 1000);
		mLengthMap.put("sox", 1);
		
		mCasualLengthMap = new HashMap<String, Integer>();
		mCasualLengthMap.putAll(mLengthMap);
		mCasualLengthMap.put("silent", 1000);
	}

	/**
	 * @return list of filenames in raw directory according to ignore list
	 */
	public static List<String> getRawFilenames() {
		ArrayList<String> list = new ArrayList<String>();
		Field[] fields = R.raw.class.getFields();
		for (Field f : fields) {
			try {
				String name = (String) mMap.get(f.getName());
				if (!name.equals(IGNORED)) {
					list.add(name);
				}
			} catch (IllegalArgumentException e) {}
		}
		return list;
	}
	
	/**
	 * @return list of filenames in raw directory according to ignore list
	 */
	public static List<String> getCasualRawFilenames() {
		ArrayList<String> list = new ArrayList<String>();
		Field[] fields = R.raw.class.getFields();
		for (Field f : fields) {
			try {
				String name = (String) mCasualMap.get(f.getName());
				if (!name.equals(IGNORED)) {
					list.add(name);
				}
			} catch (IllegalArgumentException e) {}
		}
		return list;
	}

	/**
	 * @return list of R.raw.id of files in raw directory according to ignore list
	 */
	public static List<Integer> getRawRes() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		Field[] fields = R.raw.class.getFields();
		for (Field f : fields)
			try {
				String name = (String) mMap.get(f.getName());
				if (!name.equals(IGNORED)) {
					list.add(f.getInt(null));
				}
			} catch (IllegalAccessException e) {}
		return list;
	}
	
	/**
	 * @return list of R.raw.id of files in raw directory according to ignore list
	 */
	public static List<Integer> getCasualRawRes() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		Field[] fields = R.raw.class.getFields();
		for (Field f : fields)
			try {
				String name = (String) mCasualMap.get(f.getName());
				if (!name.equals(IGNORED)) {
					list.add(f.getInt(null));
				}
			} catch (IllegalAccessException e) {}
		return list;
	}

	/**
	 * @param R.id
	 * @return duration of mp3 - file in raw directory
	 */
	public static int getDurationById(int id) {
		int duration = 0;
		try {
			Context context = SmartPagerApplication.getInstance();
			String ignore = context.getPackageName() + ":raw/";
			String fullname = context.getResources().getResourceName(id);
			String key = fullname.replace(ignore, "");
			duration = mLengthMap.get(key);
		} catch (Exception e) {

		}
		return duration;

	}
}
