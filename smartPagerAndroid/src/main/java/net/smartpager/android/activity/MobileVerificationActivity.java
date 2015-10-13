package net.smartpager.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.fragment.dialog.AlertFragmentDialog;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.utils.PhoneTextWatcher;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.VerifyPhoneResponse;
import net.smartpager.android.web.response.VerifySMSResponse;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;

public class MobileVerificationActivity extends BaseActivity {

	public static final int SMS_WAITING_TIME = 20000; 
	public static final String SMS_EXTRA_NAME = "pdus";
	
	@ViewInjectAnatation(viewID = R.id.phone_number_editText)
	EditText mEdtPhoneNumber;
	@ViewInjectAnatation(viewID = R.id.phone_number_formatted_textview)
	TextView mTxtPhoneNumberFormatted;
	@ViewInjectAnatation(viewID = R.id.animation_imageView)
	ImageView mImgAnimation;
	@ViewInjectAnatation(viewID = R.id.frameLayout)
	FrameLayout mFrameLayout;
	@ViewInjectAnatation(viewID = R.id.animationLayout)
	View mAnimationlayout;
	@ViewInjectAnatation(viewID = R.id.contentLayout)
	View mContentLayout;
	@ViewInjectAnatation(viewID = R.id.phone_number_next_button)
	Button mNextButton;
	
	@ViewInjectBackgraundAnatation(viewID=R.id.backgroundView, imageID = R.drawable.bg_regestration_step_one)
	View mBackgroundHeaderView;
	private boolean isLockStartRequest;
//	private Timer mSmsWaiter;
    private SecretQuestion m_currQuestion = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mobile_verification);
		Injector.doInjection(this);
		
		mAnimationlayout.setVisibility(View.GONE);
		mContentLayout.setVisibility(View.VISIBLE);
		
		isLockStartRequest = false;
		mTxtPhoneNumberFormatted.setOnClickListener(onPhoneTextClickListener);
		mEdtPhoneNumber.addTextChangedListener(new PhoneTextWatcher(mTxtPhoneNumberFormatted));
		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextActionClick();
			}
		});
		mEdtPhoneNumber.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT) {
					onNextActionClick();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		if (!isLockStartRequest){
			inflater.inflate(R.menu.next, menu);
		}
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
	
	private void onNextActionClick() {
		if (!TextUtils.isEmpty(mEdtPhoneNumber.getText().toString())
				&& mEdtPhoneNumber.getText().toString().length() == Constants.PHONE_LENGTH) {
			if (!isLockStartRequest) {
				isLockStartRequest = true;
				startVerifyPhone();
				showVerifiAnimation();
				invalidateOptionsMenu();
			}
		} else {
			showInforAlertDialog();
		}
	}
	
	private OnClickListener onPhoneTextClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mEdtPhoneNumber, 0);
		}
	};
	
	@Override
	protected boolean needCheckPin() {

		return false;
	}
	
	private void showVerifiAnimation() {
		mFrameLayout.bringChildToFront(mAnimationlayout);
		mContentLayout.setVisibility(View.GONE);
		mAnimationlayout.setVisibility(View.VISIBLE);
		playAnimationToWeb();
		InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		keyboard.hideSoftInputFromWindow(mEdtPhoneNumber.getWindowToken(), 0);
	}

	private void playAnimationToWeb() {
		playAnimation(R.drawable.animation_from_device_to_web);
	}

	private void playAnimationFromWeb() {
		playAnimation(R.drawable.animation_from_web_to_device);
	}

	private void playAnimation(int animationID) {
		AnimationDrawable animationDrawable = (AnimationDrawable) mImgAnimation.getBackground();
		if (animationDrawable.isRunning()) {
			animationDrawable.stop();
		}
		mImgAnimation.setBackgroundResource(animationID);
		animationDrawable = (AnimationDrawable) mImgAnimation.getBackground();
		animationDrawable.start();
	}

    private void startVerifyPhone() {
        Bundle extras = new Bundle();
        extras.putString(WebSendParam.phoneNumber.name(), 1 + mEdtPhoneNumber.getText().toString());

        String platform = "android";
        extras.putString(WebSendParam.devicePlatform.name(), platform);
        String deviceModel = getDeviceModel();
        extras.putString(WebSendParam.deviceModel.name(), deviceModel);
        String version = Build.VERSION.RELEASE;
        extras.putString(WebSendParam.deviceOSVersion.name(), version);
        String deviceHardware = Build.HARDWARE;
        extras.putString(WebSendParam.deviceProduct.name(), deviceHardware);
        TelephonyManager manager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        extras.putString(WebSendParam.carrierName.name(), carrierName);
        SmartPagerApplication.getInstance().startWebAction(WebAction.deviceRegistrationStep1, extras);
    }

    private String getDeviceModel() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

	private void showInforAlertDialog() {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		AlertFragmentDialog fragmentDialog = AlertFragmentDialog.newInstance(getString(R.string.warning), getString(R.string.this_is_not_a_valid_phone_number));
		fragmentDialog.show(transaction, FragmentDialogTag.AlertInforDialog.name());
	}

	private void startPersonalInfo() {
		startActivity(new Intent(this, PersonalInfoActivity.class));
		finish();
	}

    @Override
    protected void onResume() {
        hideSoftKeyboard(mEdtPhoneNumber);
        hideSoftKeyboard(mTxtPhoneNumberFormatted);
        super.onResume();
    }

    @Override
	protected void onStop() {		
		super.onStop();
	}
	
	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		returnToPhoneNumberEnter();
		super.onErrorResponse(action, responce);		
	}

	private void returnToPhoneNumberEnter() {
		mAnimationlayout.setVisibility(View.GONE);
		mContentLayout.setVisibility(View.VISIBLE);
		isLockStartRequest = false;
		mFrameLayout.bringChildToFront(mContentLayout);
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		switch (action) {
			case deviceRegistrationStep1:
                m_currQuestion = ((VerifyPhoneResponse)responce).getSecretQuestion();
				playAnimationFromWeb();
				startManualDevicesCode();
				break;
			default:
				break;
		}
	}

	private WaitForManualCodeTask mTimerTask;
	private class WaitForManualCodeTask extends TimerTask {

		@Override
		public void run() {
			startManualDevicesCode();
		}
	}
	
	private void startManualDevicesCode() {
		Intent intent = new Intent(this, ManualDevicesCodeActivity.class);
		intent.putExtra(WebSendParam.phoneNumber.name(), 1 + mEdtPhoneNumber.getText().toString());
        intent.putExtra(WebSendParam.question.name(), (Serializable) m_currQuestion);
		startActivity(intent);
		finish();				
	}
}
