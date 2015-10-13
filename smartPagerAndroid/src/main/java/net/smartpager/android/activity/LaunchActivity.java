package net.smartpager.android.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BuildSettings;
import net.smartpager.android.consts.Constants;
import com.crittercism.app.Crittercism;
import com.crittercism.app.CrittercismConfig;

import java.util.List;

// Check if the application is already running
// Strat Crittercism
// Set configuration
// Check if there is an update
public class LaunchActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        CrittercismConfig config = new CrittercismConfig();
        String myCustomVersionName = null;
        try {
            myCustomVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(myCustomVersionName != null)
            config.setCustomVersionName(myCustomVersionName);
        if(BuildSettings.USE_CRITTERCISM)
            Crittercism.initialize(getApplicationContext(), Constants.CRITTERCISM_APP_KEY, config);
        SmartPagerApplication.getInstance().getPreferences().checkBuildUpdate();
		if (needStartApp()) {
			Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
			startActivity(intent);
		}
		finish();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// this prevents StartupActivity recreation on Configuration changes
		// (device orientation changes or hardware keyboard open/close).
		// just do nothing on these changes:
		super.onConfigurationChanged(newConfig);
	}

	private boolean needStartApp() {
		final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningTaskInfo> tasksInfo = am.getRunningTasks(1024);

		if (!tasksInfo.isEmpty()) {
			final String ourAppPackageName = getPackageName();
			RunningTaskInfo taskInfo;
			final int size = tasksInfo.size();
			for (int i = 0; i < size; i++) {
				taskInfo = tasksInfo.get(i);
				if (ourAppPackageName.equals(taskInfo.baseActivity.getPackageName())) {
					// continue application start only if there is the only Activity in the task
					// (BTW in this case this is the StartupActivity)
					return taskInfo.numActivities == 1;
				}
			}
		}

		return true;
	}

}