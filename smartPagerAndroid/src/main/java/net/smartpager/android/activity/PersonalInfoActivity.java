package net.smartpager.android.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;

public class PersonalInfoActivity extends AbstractProfileActivity {

	@ViewInjectBackgraundAnatation(viewID = R.id.backgroundView, imageID = R.drawable.bg_regestration_step_three)
	View mBackgroundHeaderView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_infor);
		
		Injector.doInjection(this);
		initGui();
		setDataFromPrefferences();
		getProfilePicture();
	}

	@Override
	protected boolean needCheckPin() {
		return false;
	}

	@ClickListenerInjectAnatation(viewID = R.id.personal_info_save_button)
	public void onClickSaveButton(View view) {
		showProgressDialog(getString(R.string.save_data));
		saveData();
	}

	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	@Override
	@SuppressLint("SimpleDateFormat")
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		super.onSuccessResponse(action, responce);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        SmartPagerApplication.getInstance().getPreferences().setLastUpdate(dateFormat.format(new Date()));
		switch (action) {
			case setProfile:
				startSyncronization();
				SmartPagerApplication.getInstance().getPreferences().setSaveProfile(true);
				startMainActivity();
			break;
			default:
			break;
		}
	}

}
