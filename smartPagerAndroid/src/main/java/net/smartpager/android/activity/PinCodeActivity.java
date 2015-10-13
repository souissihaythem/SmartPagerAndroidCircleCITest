package net.smartpager.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.notification.AlertFilesUtils;
import net.smartpager.android.notification.Notificator;
import net.smartpager.android.utils.ClipboardUtils;
import net.smartpager.android.view.HiddenEditText;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;

public class PinCodeActivity extends BaseActivity {

	
	@ViewInjectAnatation(viewID = R.id.pin_editText)
	HiddenEditText mPinEditText;
	@ViewInjectAnatation(viewID = R.id.pin_imageView1)
	ImageView mPinImageView1;
	@ViewInjectAnatation(viewID = R.id.pin_imageView2)
	ImageView mPinImageView2;
	@ViewInjectAnatation(viewID = R.id.pin_imageView3)
	ImageView mPinImageView3;
	@ViewInjectAnatation(viewID = R.id.pin_imageView4)
	ImageView mPinImageView4;
	@ViewInjectAnatation(viewID = R.id.pin_next_button)
	Button mNextButton;

	@ViewInjectAnatation(viewID = R.id.pin_caption_textView)
	TextView mCaptionTextView;
	@ViewInjectAnatation(viewID = R.id.pin_description_textView)
	TextView mDescriptionTextView;
	@ViewInjectBackgraundAnatation(viewID=R.id.backgroundView, imageID = R.drawable.bg_regestration_step_two)
	View mBackgroundHeaderView;
	
	private String mFirstEnteredPin = "";
	
	@Override
	protected boolean needCheckPin() {
		return false;
	};
	
	TextWatcher mPinTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}
		
		@Override
		public void afterTextChanged(Editable s) {			
			setPinImages(s.toString());
		}
	};

	OnClickListener onPinImageClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mPinEditText.setFocusableInTouchMode(true);
			mPinEditText.requestFocus();

			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mPinEditText, 0);
		}
	}; 
	
	OnEditorActionListener mPinKeyListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {		
				switch (actionId) {
					case EditorInfo.IME_ACTION_NEXT:
						return onNextActionClick();											
				}
			return false;
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pin_code);
		
		Injector.doInjection(this);
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextActionClick();
			}
		});
		mPinEditText.setOnEditorActionListener(mPinKeyListener);
		mPinEditText.addTextChangedListener(mPinTextWatcher);
		ClipboardUtils.disableCopyPasteOperations(mPinEditText);
		
		ClipboardUtils.cleanUpClipboard(this);
		
		mPinEditText.setOnClickListener(onPinImageClickListener);
		mPinImageView1.setOnClickListener(onPinImageClickListener);
		mPinImageView2.setOnClickListener(onPinImageClickListener);
		mPinImageView3.setOnClickListener(onPinImageClickListener);
		mPinImageView4.setOnClickListener(onPinImageClickListener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.next, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.action_next:
				onNextActionClick();
			break;
			default:
				return false;
		}
		return true;
	}
	
	private boolean onNextActionClick() {
		if (getEnteredPin().length() < 4) {
			return true;
		}
		if (TextUtils.isEmpty(mFirstEnteredPin)) {
			firstPinEnterProcess();
		} else if (mFirstEnteredPin.equalsIgnoreCase(getEnteredPin())) {
            SmartPagerApplication.getInstance().getPreferences().setPin(getEnteredPin());
            SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(3);
            SmartPagerApplication.getInstance().getSettingsPreferences().setPinEnteredTime();
            int defaultNormalAlertPosition = AlertFilesUtils.getRawFilenames().size() - 1;
            SmartPagerApplication.getInstance().getSettingsPreferences().setNormalAlert(Math.max(0, defaultNormalAlertPosition));
//            Notificator.soundcheckNormalAlert();
            showSecretQuestions();
		}else{
			showErrorDialog(getString(R.string.change_pin_incorrect));
		}
		return true;
	}
	
	private void setPinImages(String pinCode) {
		mPinImageView1.setImageResource(R.drawable.password_cell_empty);
		mPinImageView2.setImageResource(R.drawable.password_cell_empty);
		mPinImageView3.setImageResource(R.drawable.password_cell_empty);
		mPinImageView4.setImageResource(R.drawable.password_cell_empty);
		int length = pinCode.length();
		if (length > 0)
			mPinImageView1.setImageResource(R.drawable.password_cell_dot);
		if (length > 1)
			mPinImageView2.setImageResource(R.drawable.password_cell_dot);	
		if (length > 2)
			mPinImageView3.setImageResource(R.drawable.password_cell_dot);
		if (length > 3)
			mPinImageView4.setImageResource(R.drawable.password_cell_dot);
	}
	
	private void showSecretQuestions() {
		Intent intent = new Intent(this, SecretQuestionsActivity.class);
		startActivity(intent);
		finish();
	}
	
	private String getEnteredPin() {
		return mPinEditText.getText().toString();
	}

	private void firstPinEnterProcess() {
		mFirstEnteredPin = getEnteredPin(); 
		restorePinTries();
		mPinEditText.setText("");
		mPinEditText.clearFocus();
		mPinEditText.requestFocus();
		mDescriptionTextView.setVisibility(View.GONE);
		mCaptionTextView.setText(getString(R.string.please_enter_the_pin_a_second_time_to_confirm));
	}
	
	private void restorePinTries() {
        SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(3);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		
	}	

}
