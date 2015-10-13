package net.smartpager.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.smartpager.android.service.UnreadAlarmService;
import net.smartpager.android.utils.DateTimeUtils;

import java.util.Calendar;

public class Autostart extends BroadcastReceiver {
	private static final long REPEAT_INTERVAL = 1 * DateTimeUtils.MS_IN_MIN;
	public Autostart() {}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		startAlarm(context);
	}

	public static void startAlarm(Context context) {
		context.startService(new Intent(context, UnreadAlarmService.class));
		AlarmManager manager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		Intent alarmIntent = new Intent(context, AlarmService.class);
		PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, alarmIntent, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.add(Calendar.SECOND, 5);
		manager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), REPEAT_INTERVAL, pendingIntent);
	}
}
