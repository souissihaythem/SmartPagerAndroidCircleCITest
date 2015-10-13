package net.smartpager.android.database;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.database.DatabaseHelper.MessageTable;

public class MessageCursorTable extends CursorTable {

	@Override
	public Uri getUri() {
		return SmartPagerContentProvider.CONTENT_MESSAGE_URI;
	}

	@Override
	public CursorLoader getLoader() {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		return new CursorLoader(context, getUri(), null, null, null, null);
	}

	@Override
	public CursorLoader getFilterLoader(String... filterparams) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		return new CursorLoader(context, getUri(), null, MessageTable.id.name()+" = ?", filterparams, null);
	}
	
	public CursorLoader getUnreadMessagesByThread(String thread){
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		return new CursorLoader(context, SmartPagerContentProvider.CONTENT_SIMPLE_MESSAGE_URI, null,  MessagesQueryHelper.selectUnreadByThread(thread), null, null);
	}
	
	public CursorLoader getMessageById(String msgId) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		return new CursorLoader(context, SmartPagerContentProvider.CONTENT_MESSAGE_URI, null, MessagesQueryHelper.selectByMessageId(msgId), null, null);
	}

}
