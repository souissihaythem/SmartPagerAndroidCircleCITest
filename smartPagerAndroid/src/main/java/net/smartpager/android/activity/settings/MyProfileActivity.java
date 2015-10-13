package net.smartpager.android.activity.settings;

import android.os.Bundle;

import net.smartpager.android.R;
import net.smartpager.android.activity.AbstractProfileActivity;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.utils.NetworkUtils;
import net.smartpager.android.web.response.AbstractResponse;

public class MyProfileActivity extends AbstractProfileActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_profile);
		initGui();
		setDataFromPrefferences();
		getProfilePicture();
	}
	
	@Override
	protected boolean isUpNavigationEnabled() {

		return true;
	}

	@Override
	public void onBackPressed() {
		if (!NetworkUtils.isInternetConnectedAsync()) {
			showErrorDialog(getString(R.string.connection_is_required));
		//	super.onBackPressed();
		} else {
			saveData();
			showProgressDialog("Saving data...");
		}
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.setProfile) {
			hideProgressDialog();
			super.onBackPressed();
		}
	}

	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		super.onBackPressed();

	}

	@Override
	protected boolean needCheckPin() {
		return false;
	}
}
