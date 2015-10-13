package net.smartpager.android.database;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;

public class ChatsTable extends CursorTable {

	public enum MessageStatus {
		Sent, Pending, Delayed, Failed, Delivered, Received, Read, Replied, Accepted, Rejected, Called
	}

	public enum MessageType {
		sent, inbox
	}

	private boolean isArhive;

	public ChatsTable(boolean isArhive) {
		this.isArhive = isArhive;
	}

	@Override
	public Uri getUri() {
		return SmartPagerContentProvider.CONTENT_CHATS_URI;
	}

	@Override
	public CursorLoader getLoader() {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		String select = makeSelection(isArhive);
		CursorLoader cursorLoader = new CursorLoader(context, getUri(), null, null, new String[] { "", select }, null);
		return cursorLoader;
	}

	@Override
	public CursorLoader getFilterLoader(String... filterparams) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		String select = makeSelection(isArhive);
		CursorLoader cursorLoader = new CursorLoader(context, getUri(), null, null, new String[] { filterparams[0],
				select }, null);
		return cursorLoader;
	}

	public static String makeSelection(boolean isArchive) {
		return  (isArchive) 
				? MessagesQueryHelper.selectArchivedChatTable() 
				: MessagesQueryHelper.selectNonArchivedChatTable();
	}

}
