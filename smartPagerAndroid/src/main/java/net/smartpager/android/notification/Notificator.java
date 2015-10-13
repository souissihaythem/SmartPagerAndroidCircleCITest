package net.smartpager.android.notification;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.service.NotificationService;
import net.smartpager.android.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.TimeZone;

public class Notificator {

	private static Context mContext = SmartPagerApplication.getInstance().getApplicationContext();

	
	public static void disable(){
		sendComand(NotificationService.NotificationAction.disable.name(), null);
	}

	public static void enable(){
		sendComand(NotificationService.NotificationAction.enable.name(), null);
	}

	public static void cancelAll(){
		sendComand(NotificationService.NotificationAction.cancelAll.name(), null);
	}

	public static void soundcheckCriticalAlert(){
		sendComand(NotificationService.NotificationAction.soundcheckCriticalAlert.name(), null);
	}
	
	public static void soundcheckNormalAlert(){
		sendComand(NotificationService.NotificationAction.soundcheckNormalAlert.name(), null);
	}
	
	public static void soundcheckCasualAlert(){
		sendComand(NotificationService.NotificationAction.soundcheckCasualAlert.name(), null);
	}
	
	public static void notifyInbox(Bundle bundle){
		sendComand(NotificationService.NotificationAction.notifyInbox.name(), bundle);
	}

	public static void notifyInboxChatSelfThread(Bundle bundle){
		sendComand(NotificationService.NotificationAction.notifyInboxChatSelfThread.name(), bundle);
	}
	
	public static void notifyInboxChatAnotherThread(Bundle bundle){
		sendComand(NotificationService.NotificationAction.notifyInboxChatAnotherThread.name(), bundle);
	}
	
	public static void notifyInboxRepeat(Bundle bundle, int intervalMinutes){
		notifyInbox(bundle);
		cancelAlarm();
		Context context = SmartPagerApplication.getInstance();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.add(Calendar.MINUTE, intervalMinutes);
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC, 
				calendar.getTimeInMillis(), 
				intervalMinutes * DateTimeUtils.MS_IN_MIN, 
				getPendingIntent(context));
	}
	
	public static void cancelAlarm(){
//		Log.e("^^^^^^^^^^^^^^^^^ C A N C E L ^^^^^^^^^^^^^^^^^^");
		Context context = SmartPagerApplication.getInstance();
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		am.cancel( getPendingIntent(context));
	}
	
	public static void notifySent(){
		sendComand(NotificationService.NotificationAction.notifySent.name(), null);
	}

	private static void sendComand(String command, Bundle bundle) {
		Intent intent = new Intent(mContext, NotificationService.class);
		intent.setAction(command);
		if (bundle != null){
			intent.putExtras(bundle);
		}
		mContext.startService(intent);
	}
	
	private static PendingIntent getPendingIntent(Context context){
		Intent intent = new Intent(context, NotificationService.class);
		intent.setAction(NotificationService.NotificationAction.notifyInboxRepeat.name());
		return PendingIntent.getService(context, 0, intent, 0);
		
		
	}
}

