package net.smartpager.android.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import net.smartpager.android.consts.BuildSettings;

import java.io.IOException;

public class GCMAsyncTaskLoader extends AsyncTaskLoader<String> {

	public GCMAsyncTaskLoader(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	public String loadInBackground() {
		// TODO Auto-generated method stub
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getContext());
		String registrationId = null;
		try {
			registrationId = gcm.register(BuildSettings.GCM_SENDER_ID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return registrationId;
	}

}
