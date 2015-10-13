package net.smartpager.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.utils.ClipboardUtils;
import net.smartpager.android.view.HiddenEditText;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class CheckPinActivity extends BaseActivity implements OnDialogDoneListener{

	private String mCaptionCurrent;
	private String mCaptionIncorrect;
	private String mCaptionTries;
	private boolean mBlockPinEnter;

	@ViewInjectAnatation(viewID = R.id.change_pin_editText)
	private HiddenEditText mPinEditText;
	@ViewInjectAnatation(viewID = R.id.change_pin_imageView1)
	private ImageView mPinImageView1;
	@ViewInjectAnatation(viewID = R.id.change_pin_imageView2)
	private ImageView mPinImageView2;
	@ViewInjectAnatation(viewID = R.id.change_pin_imageView3)
	private ImageView mPinImageView3;
	@ViewInjectAnatation(viewID = R.id.change_pin_imageView4)
	private ImageView mPinImageView4;

	@ViewInjectAnatation(viewID = R.id.change_pin_caption_textView)
	private TextView mCaptionTextView;
	@ViewInjectAnatation(viewID = R.id.change_pin_incorrect_textView)
	private TextView mIncorrecrTextView;
	@ViewInjectAnatation(viewID = R.id.change_pin_tries_textView)
	private TextView mTriesTextView;
	
	private TextWatcher mPinTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void afterTextChanged(Editable s) {
			if (!mBlockPinEnter) {
				setPinImages(s.toString());
			}
			
			if (mPinEditText.getText().toString().length() == 4) {
				onDoneActionClick();
			}
		}
	};

	
	private OnClickListener onPinImageClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mPinEditText.setFocusableInTouchMode(true);
			mPinEditText.requestFocus();
			
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mPinEditText, 0);
		}
	};
	
	@Override
	protected boolean needCheckPin() {
		return false;
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_check_pin);
		Injector.doInjection(this);
		initCaptions();
		
		mBlockPinEnter = false;
		mPinEditText.setOnKeyListener(mPinKeyListener);
		mPinEditText.addTextChangedListener(mPinTextWatcher);
		ClipboardUtils.disableCopyPasteOperations(mPinEditText);
		
		ClipboardUtils.cleanUpClipboard(this);
		
		preparePinFirstEnter();
		int triesLeft = SmartPagerApplication.getInstance().getSettingsPreferences().getEnterPinTriesLeft();
		if (triesLeft < 3) {
			preparePinWrongEnter(triesLeft);
		}
		
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
		inflater.inflate(R.menu.done, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case R.id.action_done:
				onDoneActionClick();
			break;
			default:
				return false;
		}
		return true;
	}
	
	private void initCaptions() {
		mCaptionCurrent = getResources().getString(R.string.change_pin_enter_current);
		mCaptionIncorrect = getResources().getString(R.string.change_pin_incorrect);
		mCaptionTries = getResources().getString(R.string.change_pin_tries);
	}

	OnKeyListener mPinKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_UP) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER: {
						return onDoneActionClick();
					}
				}
			}
			return false;
		}


	};

	private boolean onDoneActionClick() {
		mPinEditText.clearFocus();
		mPinEditText.requestFocus();
		if (mPinEditText.getText().toString().length() >= 4) {
			if (isCorrectPinEntered()) {
				finishCheckPinActivity();
			} else {
				// showWrongPinDialog();
				int triesLeft = SmartPagerApplication.getInstance().getSettingsPreferences().getEnterPinTriesLeft() - 1;
				return preparePinWrongEnter(triesLeft);
			}
		}
		return false;
	}
	
	private void finishCheckPinActivity() {
        SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(3);
        SmartPagerApplication.getInstance().getSettingsPreferences().setPinEnteredTime();
		finish();
	}

	private boolean preparePinFirstEnter() {
		mCaptionTextView.setText(mCaptionCurrent);
		mCaptionTextView.setGravity(Gravity.CENTER);
		mIncorrecrTextView.setVisibility(View.GONE);
		mTriesTextView.setVisibility(View.GONE);
		return true;
	}
	
	private boolean preparePinWrongEnter(int triesLeft) {
		clearPinEditText(); 
		setPinImagesEmpty();
		mCaptionTextView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		mIncorrecrTextView.setVisibility(View.VISIBLE);
		mIncorrecrTextView.setText(mCaptionIncorrect);
		mTriesTextView.setVisibility(View.VISIBLE);
		mTriesTextView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		if (triesLeft > 0) {
			mTriesTextView.setText(triesLeft + mCaptionTries);
            SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(triesLeft);
		} else {
			return stageAllTriesExceeded();
		}
		return true;
	}
	
	private boolean stageAllTriesExceeded() {
//		Intent intent = new Intent(this, WebService.class);
//		intent.setAction(SmartPagerApplication.getActionName(WebAction.lock.name()));
//		startService(intent);
		sendSingleComand(WebAction.lock);
        SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(0);
        SmartPagerApplication.getInstance().getPreferences().setLock(true);
		startActivity(new Intent(this, LockActivity.class));
		finish();
		return false;
	}
	
	private boolean isCorrectPinEntered() {
		
		String oldPin = SmartPagerApplication.getInstance().getPreferences().getPin();
		String enteredPin = mPinEditText.getText().toString();
		
		if (oldPin.equalsIgnoreCase(enteredPin)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onBackPressed() {
		
	}

	@SuppressWarnings("unused")
	private void showWrongPinDialog() {
		String title = getString(R.string.check_pin_incorrect_title);
		String message = getString(R.string.check_pin_incorrect_msg);
		AlertFragmentDialog dialog = 
				AlertFragmentDialog.newInstance(title, message, false);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.ErrorDialog.name());
	}

	private void setPinImagesEmpty() {
		mPinImageView1.setImageResource(R.drawable.password_cell_empty);
		mPinImageView2.setImageResource(R.drawable.password_cell_empty);
		mPinImageView3.setImageResource(R.drawable.password_cell_empty);
		mPinImageView4.setImageResource(R.drawable.password_cell_empty);
	}
	
	private void setPinImages(String pinCode) {
		setPinImagesEmpty();
		int length = pinCode.length();
		if (length > 0) mPinImageView1.setImageResource(R.drawable.password_cell_dot);
		if (length > 1) mPinImageView2.setImageResource(R.drawable.password_cell_dot);
		if (length > 2) mPinImageView3.setImageResource(R.drawable.password_cell_dot);
		if (length > 3) mPinImageView4.setImageResource(R.drawable.password_cell_dot);
	}

	private void clearPinEditText() {
		mPinEditText.setText("");
		mPinEditText.clearFocus();
		mPinEditText.requestFocus();
	}
	
	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.lock){
            SmartPagerApplication.getInstance().getPreferences().setConfirmLockFromService(true);
		}
	}

	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		
		clearPinEditText();
		setPinImages("");
	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {
		
	}

}
