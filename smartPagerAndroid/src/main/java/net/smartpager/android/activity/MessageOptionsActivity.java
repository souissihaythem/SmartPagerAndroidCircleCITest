package net.smartpager.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.rey.material.widget.CheckBox;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class MessageOptionsActivity extends BaseActivity {

	@ViewInjectAnatation(viewID = R.id.options_urgent_layout)
	private RelativeLayout mUrgentLayout;
	@ViewInjectAnatation(viewID = R.id.options_acknowlegement_layout)
	private RelativeLayout mAcknowledgementLayout;

	@ViewInjectAnatation(viewID = R.id.options_urgent_checkbox)
	private CheckBox mUrgentCheckBox;
	@ViewInjectAnatation(viewID = R.id.options_acknowlegement_checkbox)
	private CheckBox mAckCheckBox;
	
	@Override
	protected boolean needCheckPin() {
		return false;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_options);
		Injector.doInjection(this);

		mUrgentLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mUrgentCheckBox.setChecked(!mUrgentCheckBox.isChecked());
			}
		});

		mAcknowledgementLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mAckCheckBox.setChecked(!mAckCheckBox.isChecked());
			}
		});
	}
	
	@Override
	protected void onResume() {
		boolean urgent = getIntent().getBooleanExtra(BundleKey.optUrgent.name(), false);
		mUrgentCheckBox.setChecked(urgent);
		boolean ack = getIntent().getBooleanExtra(BundleKey.optAcknowledgement.name(), false);
		mAckCheckBox.setChecked(ack);
		super.onResume();
	}
		
	@Override
	public void finish() {
		Intent data = new Intent();
		data.putExtra(BundleKey.optUrgent.name(), mUrgentCheckBox.isChecked());
		data.putExtra(BundleKey.optAcknowledgement.name(), mAckCheckBox.isChecked());
		setResult(RESULT_OK, data);
		super.finish();
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		
	}

}
