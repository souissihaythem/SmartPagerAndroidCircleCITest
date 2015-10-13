package net.smartpager.android.database;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;

public class ContactCursorTable extends CursorTable {

	public enum ContactStatus{
		ONLINE,
		OFFLINE,
		LOCKED,
		DO_NOT_DISTURB,
		BAD_CONNECTION
	}

	@Override
	public Uri getUri() {
		return SmartPagerContentProvider.CONTENT_CONTACT_URI;
	}

	@Override
	public CursorLoader getLoader() {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();

		return new CursorLoader(context, SmartPagerContentProvider.CONTENT_CONTACT_URI, null, null, null,
				null);

	}

	@Override
	public CursorLoader getFilterLoader(String... filterparams) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		String selection = null;
		if (filterparams != null) {
			selection = "id = ?";
		}
		return new CursorLoader(context, SmartPagerContentProvider.CONTENT_CONTACT_URI, null, selection, filterparams,
				null);
	}

	public CursorLoader getPhoneFilterLoader(String phoneNumber) {
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		return new CursorLoader(context, SmartPagerContentProvider.CONTENT_CONTACT_URI, null, "phoneNumber like ?",
				new String[] { phoneNumber }, null);
	}

}
