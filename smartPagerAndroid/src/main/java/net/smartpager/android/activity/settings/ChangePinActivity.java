package net.smartpager.android.activity.settings;

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
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.activity.LockActivity;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.utils.ClipboardUtils;
import net.smartpager.android.view.HiddenEditText;
import net.smartpager.android.web.response.AbstractResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class ChangePinActivity extends BaseActivity implements OnDialogDoneListener{

	enum PinChangeStage {
		oldPinFirstEnter, 
		oldPinWrongEnter, 
		newPinFirstEnter, 
		newPinConfirm, 
		allTriesExceeded,
		success
	}

	private PinChangeStage mCurrentStage = PinChangeStage.oldPinFirstEnter;;
	private String mNewPin;
	private String mCaptionCurrent;
	private String mCaptionEnterNew;
	private String mCaptionEnterSecond;
	private String mCaptionIncorrect;
	private String mCaptionTries;
	private String mCaptionSuccess;
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
	@ViewInjectAnatation(viewID = R.id.change_pin_done_button)
	Button mDoneButton;

	private TextWatcher mPinTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void afterTextChanged(Editable s) {
			if (!mBlockPinEnter) {
				setPinImages(s.toString());
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pin);
		Injector.doInjection(this);
		initCaptions();
		mBlockPinEnter = false;
		mPinEditText.setOnKeyListener(mPinKeyListener);
		mPinEditText.addTextChangedListener(mPinTextWatcher);
		ClipboardUtils.disableCopyPasteOperations(mPinEditText);	
		
		ClipboardUtils.cleanUpClipboard(this);
		
		prepareOldPinFirstEnter();
		int triesLeft = SmartPagerApplication.getInstance().getSettingsPreferences().getEnterPinTriesLeft();
		if (triesLeft < 3) {
			prepareOldPinWrongEnter(triesLeft);
		}
		mDoneButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDoneActionClick();
			}
		});
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
		mCaptionEnterNew = getResources().getString(R.string.change_pin_enter_new);
		mCaptionEnterSecond = getResources().getString(R.string.change_pin_enter_second);
		mCaptionIncorrect = getResources().getString(R.string.change_pin_incorrect);
		mCaptionTries = getResources().getString(R.string.change_pin_tries);
		mCaptionSuccess = getResources().getString(R.string.change_pin_success);
	}

	OnKeyListener mPinKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {

			if (event.getAction() == KeyEvent.ACTION_UP) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_ENTER:
						return onDoneActionClick();
				}
			}
			return false;
		}
	};

	private boolean onDoneActionClick() {
		if (mPinEditText.getText().toString().length() >= 4) {
			ClipboardUtils.cleanUpClipboard(this);
			switch (mCurrentStage) {
				case oldPinFirstEnter:
				case oldPinWrongEnter:
					String oldPin = SmartPagerApplication.getInstance().getPreferences().getPin();
					if (oldPin.equalsIgnoreCase(getEnteredPin())) {
						return prepareNewPinFirstEnter();
					} else {
						int triesLeft = SmartPagerApplication.getInstance().getSettingsPreferences().getEnterPinTriesLeft() - 1;
						return prepareOldPinWrongEnter(triesLeft);
					}
				case newPinFirstEnter:
					return prepareNewPinConfirm();
				case newPinConfirm:
					return stageNewPinConfirm();
				default:
				break;
			}
			return true;
		}
		return false;
	}

	private boolean prepareOldPinFirstEnter() {
		mCurrentStage = PinChangeStage.oldPinFirstEnter;
		mCaptionTextView.setText(mCaptionCurrent);
		mCaptionTextView.setGravity(Gravity.CENTER);
		mIncorrecrTextView.setVisibility(View.GONE);
		mTriesTextView.setVisibility(View.GONE);

		return true;
	}

	private boolean prepareOldPinWrongEnter(int triesLeft) {
		mCurrentStage = PinChangeStage.oldPinWrongEnter;
		mCaptionTextView.setText(mCaptionCurrent);
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
		clearPinEditText();
		return true;
	}

	private boolean prepareNewPinFirstEnter() {	
		mCurrentStage = PinChangeStage.newPinFirstEnter;
		mCaptionTextView.setText(mCaptionEnterNew);
		mCaptionTextView.setGravity(Gravity.CENTER);
		mIncorrecrTextView.setVisibility(View.GONE);
		mTriesTextView.setVisibility(View.GONE);
		clearPinEditText();
		restorePinTries();		
		return true;
	}

	private void clearPinEditText() {
		mPinEditText.setText("");
		mPinEditText.clearFocus();
		mPinEditText.requestFocus();
	}

	private boolean prepareNewPinConfirm() {
		String newPin = getEnteredPin();
		if (newPin.length() == 4) {
			mNewPin = newPin;
			mCurrentStage = PinChangeStage.newPinConfirm;
			mCaptionTextView.setText(mCaptionEnterSecond);
			mIncorrecrTextView.setVisibility(View.GONE);
			mTriesTextView.setVisibility(View.GONE);
			clearPinEditText();
		}
		return true;
	}

	private boolean stageNewPinConfirm() {
		String confirmedPin = getEnteredPin();
		mCurrentStage = PinChangeStage.newPinConfirm;
		mCaptionTextView.setText(mCaptionEnterSecond);
		mIncorrecrTextView.setVisibility(View.GONE);
		mTriesTextView.setVisibility(View.GONE);
		if (confirmedPin.equalsIgnoreCase(mNewPin)) {
            SmartPagerApplication.getInstance().getPreferences().setPin(mNewPin);
			clearPinEditText();
			mBlockPinEnter = true;
			setPinImagesEmpty();
			mCaptionTextView.setText(mCaptionSuccess);
			mCurrentStage = PinChangeStage.success;
			showSuccessDialog();
		} else {
			mIncorrecrTextView.setVisibility(View.VISIBLE);
		}
		return true;
	}


	private boolean stageAllTriesExceeded() {
		hideSoftKeyboard(mPinEditText);
		mCurrentStage = PinChangeStage.allTriesExceeded;
		mBlockPinEnter = true;
		clearPinEditText();
		setPinImagesEmpty();
		mCaptionTextView.setText(mCaptionCurrent);
		mIncorrecrTextView.setVisibility(View.VISIBLE);
		mIncorrecrTextView.setText(mCaptionIncorrect);
		mTriesTextView.setVisibility(View.VISIBLE);
		mTriesTextView.setText("No" + mCaptionTries);		
//		showExceededDialog();
		sendSingleComand(WebAction.lock);

        SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(0);
        SmartPagerApplication.getInstance().getPreferences().setLock(true);
		startActivity(new Intent(this, LockActivity.class));
		finish();
		return false;
	}
	
	private void showSuccessDialog() {
		String title = getString(R.string.change_pin_your_pin);
		String message = getString(R.string.change_pin_has_been_successfully_changed);
		AlertFragmentDialog dialog = 
				AlertFragmentDialog.newInstance(title, message, false);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.Success.name());
	}

	@SuppressWarnings("unused")
	private void showExceededDialog() {
		String title = getString(R.string.change_pin_incorrect);
		String message = "No" + getString(R.string.change_pin_tries);
		AlertFragmentDialog dialog = 
				AlertFragmentDialog.newInstance(title, message, false);
		dialog.show(getSupportFragmentManager(), FragmentDialogTag.ErrorDialog.name());
	}
	
	private void restorePinTries() {
        SmartPagerApplication.getInstance().getSettingsPreferences().setEnterPinTriesLeft(3);
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


	private String getEnteredPin() {
		return mPinEditText.getText().toString();
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		if (action == WebAction.lock){
            SmartPagerApplication.getInstance().getPreferences().setConfirmLockFromService(true);
		}
	}
	
	@Override
	public void onDialogDone(FragmentDialogTag tag, String data) {
		finish();
	}

	@Override
	public void onDialogNo(FragmentDialogTag tag, String data) {
		
	}

}
