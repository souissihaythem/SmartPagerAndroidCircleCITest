package net.smartpager.android.utils.log;

import android.database.Cursor;
import android.widget.Toast;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.utils.DateTimeUtils;

import java.util.Date;

import biz.mobidev.framework.utils.Log;

/**
 * Helper class for logging some significant information for testing and debugging purpose
 * @author Roman
 *
 */
public class TestLogUtils {
	
	/**
	 * Shows toast with some significant parameters of Message from cursor  
	 * @param cursor
	 * @see DatabaseHelper.MessageTable
	 */
	public static void 	showMessageData(Cursor cursor) {
		long putToArchive = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getArchiveMessages());
		long remove = DateTimeUtils.getTimeInMillis(SmartPagerApplication.getInstance().getSettingsPreferences().getRemoveFromArchive());
		long lastUpdate = cursor.getLong(DatabaseHelper.MessageTable.lastUpdate.ordinal());
		long read = cursor.getLong(DatabaseHelper.MessageTable.readTime.ordinal());
		boolean sent = cursor.getInt(DatabaseHelper.MessageTable.messageType.ordinal()) == MessageType.sent.ordinal();
		boolean archived = cursor.getInt(DatabaseHelper.MessageTable.archived.ordinal()) != 0;
		String str = ("Time \t" +  new Date(lastUpdate).toString() + 
				  "\nPut \t " + new Date(putToArchive).toString() +
				  "\nRem \t " + new Date(remove).toString() + 
				  "\nRead \t " + new Date(read).toString() + 
				  "\nSent \t " + sent +
				  "\nArc \t " + archived 
				  );
		Toast.makeText(SmartPagerApplication.getInstance(), str, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Writes to log some significant parameters of Message from cursor  
	 * @param cursor
	 * @see DatabaseHelper.MessageTable
	 */
	public static void logMessageData(Cursor c){
		if (c.moveToFirst()){
			do{
				Log.e("******************* UNREAD ************************");
				Log.e("*** ThreadID", c.getString(DatabaseHelper.MessageTable.threadID.ordinal()));
				Log.e("*** Subject", c.getString(DatabaseHelper.MessageTable.subjectText.ordinal()));
				Log.e("*** Text", c.getString(DatabaseHelper.MessageTable.text.ordinal()));
//				Log.e("*** readTime", c.getLong(DatabaseHelper.MessageTable.readTime.ordinal()));
//				Log.e("*** lastUpdate", c.getLong(DatabaseHelper.MessageTable.lastUpdate.ordinal()));
//				Log.e("*** critical", c.getInt(DatabaseHelper.MessageTable.critical.ordinal()));
//				Log.e("*** type", c.getInt(DatabaseHelper.MessageTable.type.ordinal()));
//				Log.e("*** messageType is send", MessageType.sent.ordinal() == c.getInt(DatabaseHelper.MessageTable.messageType.ordinal()));
			}while(c.moveToNext());
		}
	}
}
