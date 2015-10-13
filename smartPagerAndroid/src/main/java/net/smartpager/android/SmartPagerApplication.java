package net.smartpager.android;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.rey.material.app.ThemeManager;
import com.google.gson.JsonArray;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import net.smartpager.android.activity.LockActivity;
import net.smartpager.android.consts.AbstractPreferences;
import net.smartpager.android.consts.BuildSettings;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.Preferences;
import net.smartpager.android.consts.SettingsPreferences;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.dialer.VoipPhone;
import net.smartpager.android.loader.AbstractCursorLoaderCallbacksListener;
import net.smartpager.android.model.AbstractTaskTimer;
import net.smartpager.android.receivers.TaskTimerReceiver;
import net.smartpager.android.service.IWebServiceLauncher;
import net.smartpager.android.service.WebService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.widget.TextView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import biz.mobidev.framework.utils.Log;
import biz.mobidev.library.lazybitmap.ImageLoader;

public class SmartPagerApplication extends Application implements IWebServiceLauncher{
	
	public static final String PREFS_NAME = "SMARTPAGER";
	public static final String PREFS_MUTED_THREADS = "MUTEDTHREADS";
	public static final String PREFS_PAGING_GROUP_MEMBERS = "PAGINGGROUPMEMBERS";
	public static final String FAVORITES = "favouriteContacts";
	
	private Bus bus;
	private static SmartPagerApplication singleton = null;
    private IWebServiceLauncher m_webServiceLauncher = null;
    private AbstractCursorLoaderCallbacksListener m_cursorLoaderCallbacks = null;
	private ImageLoader mImageLoader;
    private AbstractPreferences m_preferences = new Preferences();
    private AbstractPreferences m_settingsPreferences = new SettingsPreferences();
    private ActivityLifecycleCallbacks mActivitiesLifeCycleListener = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            getInstance().getImageLoader().setCurrActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            getInstance().getImageLoader().resetViews();
            getInstance().getImageLoader().setCurrActivity(null);
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

	public static SmartPagerApplication getInstance() {
		return singleton;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.setUseLog(BuildSettings.ARE_LOGS_ENABLED);
		singleton = this;
        m_webServiceLauncher = singleton;
        this.registerActivityLifecycleCallbacks(mActivitiesLifeCycleListener);
        bus = new Bus(ThreadEnforcer.ANY);

		ThemeManager.init(this, 2, 0, null);
	}

    public void startTaskTimer(AbstractTaskTimer task)
    {
        Intent intent = new Intent(TaskTimerReceiver.TASK_TIMER_ACTION);
        intent.putExtra(BundleKey.taskTimer.name(), task);
        sendBroadcast(intent);
    }

    public void startWebAction (WebAction action, Bundle extras) {
		startWebAction(action, extras, null);
    }

    public void startWebAction (WebAction action, Bundle extras, BroadcastReceiver receiver)
    {
        Intent intent = new Intent(this, WebService.class);
        intent.setAction(getActionName(action.name()));
        if(extras != null)
            intent.putExtras(extras);
//        if(m_webServiceLauncher == null)
//            launchWebService(intent);
//        else
        m_webServiceLauncher.launchWebService(intent);
    }

    public AbstractPreferences getPreferences ()
    {
        return m_preferences;
    }

    public AbstractPreferences getSettingsPreferences ()
    {
        return m_settingsPreferences;
    }

	public ImageLoader getImageLoader() {
		if (mImageLoader == null) {
			mImageLoader = new ImageLoader(this);
		}
		return mImageLoader;
	}

	public static boolean isApplicationInBackground() {
		// android.permission.GET_TASKS
		Context context = getInstance().getApplicationContext();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public static String activityOnFront() {
		String topActivity = "";
		Context context = getInstance().getApplicationContext();
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			topActivity = tasks.get(0).topActivity.toString();
		}
		return topActivity;
	}

	public static boolean isActivityOnFront(Class<?> activity) {
		return activityOnFront().contains(activity.getName());
	}

	private boolean isFromBackground = true;

	public void onStopActivity() {

		isFromBackground = false;

		final Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				isFromBackground = true;
				timer.cancel();
				timer.purge();
			}
		}, 500);
	}

	public boolean isFromBackground() {
		return isFromBackground;
	}
	
	/**
	 * Forms full action name which contains of package name and {@linkplain WebAction} name 
	 * @param actionName
	 * @return full action name
	 * @see #getExtractedActionName
	 */
	public static String getActionName(String actionName) {
		return getInstance().getPackageName() + "." + actionName;
	}

	/**
	 * Extracts {@linkplain WebAction} name from full action name
	 * @param full action name
	 * @return web-action name without package prefix
	 * @see #getActionName
	 */
	public static String getExtractActionName(String action) {
		return action.replace(getInstance().getPackageName() + ".", "");
	}

	public static boolean isGalaxyS() {
		String  model = Build.MODEL;
		return  model.equalsIgnoreCase("GT-I9000") || // base model
				model.equalsIgnoreCase("SPH-D700") || // Epic (Sprint)
				model.equalsIgnoreCase("SGH-I897") || // Captivate (AT&T)
				model.equalsIgnoreCase("SGH-T959") || // Vibrant (T-Mobile)
				model.equalsIgnoreCase("SCH-I500") || // Fascinate (Verizon)
				model.equalsIgnoreCase("SCH-I400"); // Continuum (T-Mobile)
	}

	public void tryUnlockDevice() {
		if (m_preferences.isLocked() && m_preferences.isConfirmLockFromService()) {
            unlockDevice();
		}
	}

	public void unlockDevice() {
		m_preferences.setConfirmLockFromService(false);
		m_preferences.setLock(false);
		m_settingsPreferences.setEnterPinTriesLeft(3);
	}

	public void lockDevice() {
		if (!m_preferences.isLocked()) {
            m_preferences.setConfirmLockFromService(true);
            m_preferences.setLock(true);

            Intent lockIntent = new Intent(getInstance(), LockActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getInstance().startActivity(lockIntent);
		}

	}

    public void initCursorLoader (FragmentActivity activity, int id, Bundle bundle, LoaderManager.LoaderCallbacks<Cursor> callbacksListener)
    {
        if(activity != null)
        {
            if(m_cursorLoaderCallbacks == null)
                activity.getSupportLoaderManager().initLoader(id, bundle, callbacksListener);
            else
            {
                m_cursorLoaderCallbacks.setNext(callbacksListener);
                activity.getSupportLoaderManager().initLoader(id, bundle, m_cursorLoaderCallbacks);
            }
        }
    }

    public void restartCursorLoader (FragmentActivity activity, int id, Bundle bundle, LoaderManager.LoaderCallbacks<Cursor> callbacksListener)
    {
        if(activity != null)
        {
            if(m_cursorLoaderCallbacks == null)
                activity.getSupportLoaderManager().restartLoader(id, bundle, callbacksListener);
            else
            {
                m_cursorLoaderCallbacks.setNext(callbacksListener);
                activity.getSupportLoaderManager().restartLoader(id, bundle, m_cursorLoaderCallbacks);
            }
        }
    }

    public void registerCursorLoaderCallbacks (AbstractCursorLoaderCallbacksListener callbacks)
    {
        m_cursorLoaderCallbacks = callbacks;
    }

    public void setPreferences (AbstractPreferences preferences)
    {
        m_preferences = preferences;
    }

    public void setSettingsPreferences (AbstractPreferences preferences)
    {
        m_settingsPreferences = preferences;
    }

    public void setWebServiceLauncher (IWebServiceLauncher launcher)
    {
        m_webServiceLauncher = launcher;
    }

    @Override
    public void launchWebService(Intent intent) {
        startService(intent);
    }
    
    public void saveFavorites(Context context, List<String> favorites) {
		SharedPreferences settings;
		Editor editor;

		settings = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);
		editor = settings.edit();

		Gson gson = new Gson();
		String jsonFavorites = gson.toJson(favorites);

		editor.putString(FAVORITES, jsonFavorites);

		editor.commit();
	}

	public void addFavorite(Context context, String smartpagerID) {
		List<String> favorites = getFavorites(context);
		if (favorites == null)
			favorites = new ArrayList<String>();
		
		if (!isFavourite(context, smartpagerID)) {
			favorites.add(smartpagerID);
			saveFavorites(context, favorites);
		}
		
	}

	public void removeFavorite(Context context, String smartpagerID) {
		ArrayList<String> favorites = getFavorites(context);
		if (favorites != null) {
			favorites.remove(smartpagerID);
			saveFavorites(context, favorites);
		}
	}

	public ArrayList<String> getFavorites(Context context) {
		SharedPreferences settings;
		List<String> favorites;

		settings = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE);

		if (settings.contains(FAVORITES)) {
			String jsonFavorites = settings.getString(FAVORITES, null);
			Gson gson = new Gson();
			String[] favouriteContacts = gson.fromJson(jsonFavorites,
					String[].class);

			favorites = Arrays.asList(favouriteContacts);
			favorites = new ArrayList<String>(favorites);
		} else
			return null;

		return (ArrayList<String>) favorites;
	}
	
	public boolean isFavourite(Context context, String smartpagerID) {
		ArrayList<String> favourites = getFavorites(context);
		if (favourites != null) {
			for(String str : favourites) {
				if(str.trim().contains(smartpagerID)) {
					return true;
				}
			}
		}
		
		
		return false;
	}
	
	public Bus getBus() {
		return bus;
	}
	
	public void muteThread(Context context, Integer threadID, String mutedUntil) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_MUTED_THREADS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();

		editor.putString(threadID.toString(), mutedUntil);
		
		editor.commit();
	}
	
	public void unMuteThread(Context context, Integer threadID) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_MUTED_THREADS, Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();

		editor.remove(threadID.toString());
		
		editor.commit();
	}
	
	public String threadMutedUntil(Context context, Integer threadID) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_MUTED_THREADS, Context.MODE_PRIVATE);

		String thread_muted_until = sharedPreferences.getString(threadID.toString(), null);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date convertedDate = new Date();
		try {
			convertedDate = dateFormat.parse(thread_muted_until);

			SimpleDateFormat formatterMonthAndDay = new SimpleDateFormat("MMM d");
			if(DateFormat.is24HourFormat(context)){
				SimpleDateFormat formatterHour = new SimpleDateFormat("HH:mm");
				String mutedUntil = formatterMonthAndDay.format(convertedDate) +
						context.getResources().getString(R.string.at)
						+ formatterHour.format(convertedDate);
				return mutedUntil;
			}
			else{
				SimpleDateFormat formatterHour = new SimpleDateFormat("h:mm aa");
				String mutedUntil = formatterMonthAndDay.format(convertedDate) + context.getResources().getString(R.string.at)
						+ formatterHour.format(convertedDate);
				return mutedUntil;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
		return thread_muted_until;
	}
	
	public boolean isThreadMuted(Context context, Integer threadID) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_MUTED_THREADS, Context.MODE_PRIVATE);
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		
		String mutedUntil = sharedPreferences.getString(threadID.toString(), null);
		
		if (mutedUntil != null) {
			Date mutedUntilDate = null;
			try {
				mutedUntilDate = df.parse(mutedUntil);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			long diffBetweenMutedUntilAndNow = mutedUntilDate.getTime() - (new Date()).getTime();
			
			if (diffBetweenMutedUntilAndNow > 0) {
				return true;
			}
		}
		
		return false;
	}

//	public void muteThread(Context context, Integer threadID, String mutedUntil) {
//		SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_MUTED_THREADS, Context.MODE_PRIVATE);
//		Editor editor = sharedPreferences.edit();
//
//		editor.putString(threadID.toString(), mutedUntil);
//
//		editor.commit();
//	}

    public int getNumberOfPagingGroupMembers(Context context, String pagingGroupID) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PAGING_GROUP_MEMBERS, Context.MODE_PRIVATE);
        int numberOfMembers =  sharedPreferences.getInt(pagingGroupID, 0);
        return numberOfMembers;
    }

    public void saveNumberOfPagingGroupMembers(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_PAGING_GROUP_MEMBERS, Context.MODE_PRIVATE);
        final Editor editor = sharedPreferences.edit();

		ArrayList<String> pagingGroupIDs = new ArrayList<String>();

        String query = "Select * from PagingGroups";
        DatabaseHelper dh = new DatabaseHelper(context, SmartPagerContentProvider.DATABASE_NAME, null, SmartPagerContentProvider.DATABASE_VERSION);
        SQLiteDatabase db = dh.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();

        if (cursor.moveToFirst()) {
            do {
				pagingGroupIDs.add(cursor.getString(DatabaseHelper.PagingGroupTable.id.ordinal()));
            } while (cursor.moveToNext());
        }
        cursor.close();

        RequestParams params = new RequestParams();
        params.put("smartPagerID", SmartPagerApplication.getInstance().getPreferences().getUserID());
        params.put("uberPassword", SmartPagerApplication.getInstance().getPreferences().getPassword());
        AsyncHttpClient httpClient = new AsyncHttpClient();

		for (final String pagingGroupID : pagingGroupIDs) {
            params.put("groupId", pagingGroupID);

            httpClient.post(this, Constants.BASE_REST_URL + "/getPagingGroupDetails", params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("***** successful response: " + response);
                    try {
                        JSONObject details = response.optJSONObject("details");
                        JSONArray users = details.optJSONArray("users");
                        editor.putInt(pagingGroupID, users.length());
                        editor.commit();
                    } catch (Exception e) {
                        System.out.println("An exception occurred while getting paging group members: " + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                    System.out.println("***** failure response: " + responseBody);
                }

            });

		}
    }

	public String md5(String s) {
		try {
			// Create MD5 Hash
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++)
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}

}
