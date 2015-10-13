package net.smartpager.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.rey.material.widget.Button;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.fragment.dialog.EditYesNoFragmentDialog;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.utils.TelephoneUtils;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetSecretQuestionResponse;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

public class LockActivity extends BaseActivity implements OnDialogDoneListener{

    private SecretQuestion m_currSecretQuestion = null;

	@ViewInjectAnatation(viewID = R.id.lock_number_textview)
	private TextView mPhoneNumberTextView;
    @ViewInjectAnatation(viewID = R.id.lock_tv_unlock_title)
    private TextView mUnlockTitleTextView;
    @ViewInjectAnatation(viewID = R.id.lock_btn_unlock)
    private Button mBtUnlock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock);

        if ( !SmartPagerApplication.getInstance().getPreferences().isConfirmLockFromService()){
            startLockCommand();
        }
        
		Injector.doInjection(this);
		mPhoneNumberTextView.setText( TelephoneUtils.format( SmartPagerApplication.getInstance().getPreferences().getUserID()));
        mBtUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSecretQuestionDialog();
            }
        });
        getSecretQuestion();
	}

    private void showSecretQuestionDialog() {
        EditYesNoFragmentDialog.Builder dialogBuilder = new EditYesNoFragmentDialog.Builder(this).setCancelable(false).setTitle(R.string.unlock_dialog_title).setMessage(m_currSecretQuestion.getQuestion()).setHint(R.string.hint_type_answer)
                .setPositiveButtonText(R.string.btn_unlock_question).setNegativeButtonText(R.string.dialog_btn_cancel);
        dialogBuilder.show(getSupportFragmentManager(), FragmentDialogTag.UnlockQuestionRequest);
    }

    private void getSecretQuestion() {
        showProgressDialog(R.string.notification_connecting);
        sendSingleComand(WebAction.getSecretQuestion);
    }

    private void sendVerifyingAnswer()
    {
        showProgressDialog(R.string.unlock_verifying);
        Bundle extras = new Bundle();
        extras.putParcelable(WebSecretQuestionsParams.question.name(), m_currSecretQuestion);
        SmartPagerApplication.getInstance().startWebAction(WebAction.checkSecretQuestion, extras);
    }

    @Override
	protected void onResume() {
//		 else{
			startGetUpdates();
//		}
		super.onResume();
	}

	private void startLockCommand() {
//		Intent intent = new Intent(this, WebService.class);
//		intent.setAction(SmartPagerApplication.getActionName(WebAction.lock.name()));
//		startService(intent);
        sendSingleComand(WebAction.lock);
	}


    private void updateGUI() {
        int visibility = (m_currSecretQuestion != null ? View.VISIBLE : View.GONE);
        mUnlockTitleTextView.setVisibility(visibility);
        mBtUnlock.setVisibility(visibility);
    }

	@Override
	public void onBackPressed() {
		if (!SmartPagerApplication.getInstance().getPreferences().isLocked()) {
			showToast(getString(R.string.number_has_been_unlocked));
			if (needCheckPin()) {
				startActivity(new Intent(this, CheckPinActivity.class));
			}
			super.onBackPressed();
		}
	}

	@Override
	protected void onSuccessResponse(WebAction action, AbstractResponse response) {
		if (action == WebAction.getUpdates) {
			finish();
		}
		if (action == WebAction.lock){
			SmartPagerApplication.getInstance().getPreferences().setConfirmLockFromService(true);
		}
        if(action == WebAction.getSecretQuestion)
        {
            m_currSecretQuestion = ((GetSecretQuestionResponse)response).getSecretQuestion();
            updateGUI();
            hideProgressDialog();
        }
        if(action == WebAction.checkSecretQuestion)
        {
            hideProgressDialog();
            showToast(R.string.unlock_successful);
            SmartPagerApplication.getInstance().unlockDevice();
            this.finish();
        }
	}

    @Override
	protected void onErrorResponse(WebAction action, AbstractResponse response) {
        if(action == WebAction.getSecretQuestion)
        {
            updateGUI();
            hideProgressDialog();
        }
        if(action == WebAction.checkSecretQuestion)
        {
            hideProgressDialog();
            showErrorDialog(response.getMessage());
        }
	}

    @Override
    public void onDialogDone(FragmentDialogTag tag, String data) {
        switch (tag)
        {
            case UnlockQuestionRequest:
                m_currSecretQuestion.setAnswer(data);
                sendVerifyingAnswer();
                break;
            default:
                break;
        }
    }

    @Override
    public void onDialogNo(FragmentDialogTag tag, String data) {

    }
}
