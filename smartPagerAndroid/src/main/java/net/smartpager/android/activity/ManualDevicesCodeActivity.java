package net.smartpager.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
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

import com.google.gson.JsonObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.utils.ClipboardUtils;
import net.smartpager.android.view.HiddenEditText;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.VerifySMSResponse;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;
import biz.mobidev.framework.injector.anatation.ViewInjectBackgraundAnatation;

public class ManualDevicesCodeActivity extends BaseActivity {

	private String mPhoneNumber = "";
    private SecretQuestion m_currQuestion;

    @ViewInjectAnatation(viewID = R.id.manual_devices_code_answer)
    com.rey.material.widget.EditText mSecretQuestionField;
    @ViewInjectAnatation(viewID = R.id.manual_devices_code_secret_question)
    TextView mSecretQuestion;
    @ViewInjectAnatation(viewID = R.id.manual_devices_code_secret_question_line)
    LinearLayout mSecretQuestionLine;
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
	@ViewInjectAnatation(viewID = R.id.pin_imageView5)
	ImageView mPinImageView5;

	@ViewInjectAnatation(viewID = R.id.pin_caption_textView)
	TextView mCaptionTextView;
	@ViewInjectAnatation(viewID = R.id.pin_description_textView)
	TextView mDescriptionTextView;
	@ViewInjectBackgraundAnatation(viewID = R.id.backgroundView, imageID = R.drawable.bg_regestration_step_one)
	View mBackgroundHeaderView;
	@ViewInjectAnatation(viewID = R.id.manual_next_button)
	Button mNextButton;
    @ViewInjectAnatation(viewID = R.id.manual_back_button)
    Button mBackButton;

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
					onNextActionClick();
			}
			return false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manual_devices_code);

		Injector.doInjection(this);

		mNextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onNextActionClick();
			}
		});
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
		mPhoneNumber = getIntent().getStringExtra(WebSendParam.phoneNumber.name());
        m_currQuestion = (SecretQuestion) getIntent().getSerializableExtra(WebSendParam.question.name());
		mPinEditText.setOnEditorActionListener(mPinKeyListener);
		mPinEditText.addTextChangedListener(mPinTextWatcher);
        TextView title = (TextView)findViewById(R.id.pin_caption_textView);
        String titleText = String.format(getResources().getString(R.string.enter_code_title), mPhoneNumber.substring(1, 4), mPhoneNumber.substring(4, 7), mPhoneNumber.substring(7, mPhoneNumber.length()));
        title.setText(Html.fromHtml(titleText));
        //Next line was added for bug SPANDROID-174
        mDescriptionTextView.setVisibility(View.GONE);
        mDescriptionTextView.performClick();
        ClipboardUtils.disableCopyPasteOperations(mPinEditText);

		ClipboardUtils.cleanUpClipboard(this);

		mPinImageView1.setOnClickListener(onPinImageClickListener);
		mPinImageView2.setOnClickListener(onPinImageClickListener);
		mPinImageView3.setOnClickListener(onPinImageClickListener);
		mPinImageView4.setOnClickListener(onPinImageClickListener);
		mPinImageView5.setOnClickListener(onPinImageClickListener);
        updateGUI();
	}

    private void updateGUI() {
        int visibility = (m_currQuestion == null ? View.GONE : View.VISIBLE);
        mSecretQuestionLine.setVisibility(visibility);
        if(m_currQuestion != null)
            mSecretQuestion.setText(m_currQuestion.getQuestion());
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        hideSoftKeyboard(mPinEditText);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!isProgressDialogShowing())
        {
            if (!SmartPagerApplication.getInstance().getPreferences().getMobileVerify()) {
            	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            	imm.hideSoftInputFromWindow(mPinEditText.getWindowToken(), 0);

                startActivity(new Intent(this, MobileVerificationActivity.class));
                finish();
                return;
            }
        }
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

	private void onNextActionClick() {
		String code = getEnteredCode();
		if (code.length() != 5) {
            showErrorDialog(R.string.error_message_empty_sms_code);
            return;
        }
        if(m_currQuestion != null)
        {
            m_currQuestion.setAnswer(getSecretQuestionAnswer());
            if(TextUtils.isEmpty(m_currQuestion.getAnswer()))
            {
                showErrorDialog(R.string.error_message_verification_answer);
            }else
            {
                startRegistrationStep3(code);
            }
        }else
            startRegistrationStep3(code);
	}

    private String getSecretQuestionAnswer() {
        return mSecretQuestionField.getText().toString();
    }

    private void setPinImages(String pinCode) {

		mPinImageView1.setImageResource(R.drawable.password_cell_empty);
		mPinImageView2.setImageResource(R.drawable.password_cell_empty);
		mPinImageView3.setImageResource(R.drawable.password_cell_empty);
		mPinImageView4.setImageResource(R.drawable.password_cell_empty);
		mPinImageView5.setImageResource(R.drawable.password_cell_empty);

		int length = pinCode.length();
		if (length > 0) mPinImageView1.setImageResource(R.drawable.password_cell_dot);
		if (length > 1) mPinImageView2.setImageResource(R.drawable.password_cell_dot);
		if (length > 2) mPinImageView3.setImageResource(R.drawable.password_cell_dot);
		if (length > 3) mPinImageView4.setImageResource(R.drawable.password_cell_dot);
		if (length > 4) mPinImageView5.setImageResource(R.drawable.password_cell_dot);
	}

	private String getEnteredCode() {
		return mPinEditText.getText().toString();
	}

	private void startRegistrationStep3(String code) {
        Bundle extras = new Bundle();
		extras.putString(WebSendParam.phoneNumber.name(), mPhoneNumber);
		extras.putString(WebSendParam.confirmationCode.name(), code);
        if(m_currQuestion != null)
            extras.putSerializable(WebSendParam.question.name(), m_currQuestion);
//        SmartPagerApplication.getInstance().startWebAction(WebAction.deviceRegistrationStep2, extras);
		showProgressDialog("Send data");

		JSONObject jObject = new JSONObject();

		try {
			jObject.put(WebSendParam.phoneNumber.name(), mPhoneNumber);
			jObject.put(WebSendParam.confirmationCode.name(), code);
			if (m_currQuestion != null) {
				jObject.put(WebSendParam.question.name(), m_currQuestion.createAnswerObject());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringEntity entity = null;
		try {
			entity = new StringEntity(jObject.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		AsyncHttpClient httpClient = new AsyncHttpClient();
		httpClient.post(this, Constants.BASE_REST_URL + "/deviceRegistrationStep2", entity, "application/json", new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				System.out.println("***** successful response: " + response);
				hideProgressDialog();
				if (response.optString("errorMessage").equalsIgnoreCase("Incorrect answer")) {
					showErrorDialog("Incorrect answer to your secret question.  Please try again.");
				} else if (response.optString("errorMessage").equalsIgnoreCase("")) {
					SmartPagerApplication.getInstance().getPreferences().setPassword(response.optString("uberPassword"));
					SmartPagerApplication.getInstance().getPreferences().setUserID(mPhoneNumber);
					startPinCode();
				}

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject responseBody) {
				System.out.println("***** failure response: " + responseBody);
				hideProgressDialog();
				showErrorDialog("Incorrect PIN or answer to your secret question.  Please try again.");
			}

		});


	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		switch (action) {
			case deviceRegistrationStep2:
				SmartPagerApplication.getInstance().getPreferences().setPassword(((VerifySMSResponse) responce).getUberPassword());
				SmartPagerApplication.getInstance().getPreferences().setUserID(mPhoneNumber);
				startPinCode();
				break;
			default:
				break;
		}
	}

	@Override
	protected void onErrorResponse(WebAction action, AbstractResponse responce) {
		hideProgressDialog();
		switch (action) {
			case deviceRegistrationStep2:
				showErrorDialog(getString(R.string.error_registration));
				break;
			default:
				super.onErrorResponse(action, responce);
				break;
		}

	}

	private void startPinCode() {
		SmartPagerApplication.getInstance().getPreferences().setMobileVerify(true);
		Intent intent = new Intent(this, PinCodeActivity.class);
		startActivity(intent);
		finish();
	}

}