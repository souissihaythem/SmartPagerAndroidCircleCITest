package net.smartpager.android.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.rey.material.widget.Button;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.view.ScrollingWebView;
import net.smartpager.android.view.ScrollingWebView.ScrollListener;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ClickListenerInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class LicenseActivity extends Activity {
	
	private final float alphaDisabledButton = 0.3f;
	private final float alphaEnabledButton = 1.0f;
	private final float percentageForEnable = 0.9f;
	
	@ViewInjectAnatation(viewID = R.id.license_webView)	
	ScrollingWebView mLicenseWebView;

	@ViewInjectAnatation(viewID = R.id.license_agree_button)	
	Button btnAgree; 
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_license);
		
		Injector.doInjection(this);		
		btnAgree.setEnabled(false);
		btnAgree.setAlpha(alphaDisabledButton);
		mLicenseWebView.loadUrl("file:///android_asset/eula.html");
		mLicenseWebView.setOnScrollListener( new ScrollListener() {
			@Override
			public void onScrollChanged(float percent, int position, int max) {
				if (percent > percentageForEnable){
					btnAgree.setEnabled(true);	
					btnAgree.setAlpha(alphaEnabledButton);
				}
			}
		});
	}

	@ClickListenerInjectAnatation(viewID = R.id.license_decline_button)
	public void onClickDecline(View view) {
		finish();
	}

	@ClickListenerInjectAnatation(viewID = R.id.license_agree_button)
	public void onClickAgree(View view) {
        SmartPagerApplication.getInstance().getPreferences().setLicenseAgree(true);
		startActivity(new Intent(this, MobileVerificationActivity.class));
		finish();
	}
}
