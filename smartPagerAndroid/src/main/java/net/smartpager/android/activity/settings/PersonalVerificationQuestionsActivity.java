package net.smartpager.android.activity.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;
import com.rey.material.widget.Spinner;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.BaseActivity;
import net.smartpager.android.consts.FragmentDialogTag;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.fragment.dialog.OnDialogDoneListener;
import net.smartpager.android.model.CancelTimerTask;
import net.smartpager.android.model.PersonalVerificationAlertTimer;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetSecretQuestionsResponse;

import java.util.ArrayList;
import java.util.LinkedList;

import biz.mobidev.framework.injector.Injector;
import biz.mobidev.framework.injector.anatation.ViewInjectAnatation;

/**
 * Created by dmitriy on 3/11/14.
 */
public class PersonalVerificationQuestionsActivity extends BaseActivity implements OnDialogDoneListener{

    private LinkedList<SecretQuestion> m_arrAvailableQuestions = new LinkedList<SecretQuestion>();
    private LinkedList<SecretQuestion> m_arrFirstQuestions = new LinkedList<SecretQuestion>();
    private LinkedList<SecretQuestion> m_arrSecondQuestions = new LinkedList<SecretQuestion>();
    private LinkedList<SecretQuestion> m_arrThirdQuestions = new LinkedList<SecretQuestion>();

    private Spinner.OnItemSelectedListener m_onQuestionSelectListener = new Spinner.OnItemSelectedListener()
    {
        @Override
        public void onItemSelected(Spinner spinner, View view, int i, long l) {
//            refreshQuestionsLists(false);
        }

    };

    private boolean m_bAreQuestionsAvailable = true;

    @ViewInjectAnatation(viewID = R.id.personal_verification_et_first_answer)
    private com.rey.material.widget.EditText m_etFirstAnswer;

    @ViewInjectAnatation(viewID = R.id.personal_verification_et_second_answer)
    private com.rey.material.widget.EditText m_etSecondAnswer;

    @ViewInjectAnatation(viewID = R.id.personal_verification_et_third_answer)
    private com.rey.material.widget.EditText m_etThirdAnswer;

    @ViewInjectAnatation(viewID = R.id.personal_verification_spinner_question_one)
    private Spinner m_spinnerFirstQuestion;

    @ViewInjectAnatation(viewID = R.id.personal_verification_spinner_question_two)
    private Spinner m_spinnerSecondQuestion;

    @ViewInjectAnatation(viewID = R.id.personal_verification_spinner_question_three)
    private Spinner m_spinnerThirdQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_verification_questions);
        Injector.doInjection(this);
        initGui();
        SmartPagerApplication.getInstance().startWebAction(WebAction.getSecretQuestions, null);
        showProgressDialog(R.string.loading_questions);
    }

    private void initGui()
    {
        ((TextView)findViewById(R.id.personal_verification_first_title)).setText(getString(R.string.settings_verification_question, "1"));
        ((TextView)findViewById(R.id.personal_verification_second_title)).setText(getString(R.string.settings_verification_question, "2"));
        ((TextView)findViewById(R.id.personal_verification_third_title)).setText(getString(R.string.settings_verification_question, "3"));
        ArrayAdapter<SecretQuestion> adapter = new ArrayAdapter<SecretQuestion>(this,R.layout.item_spinner_settings, m_arrFirstQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinnerFirstQuestion.setAdapter(adapter);
        m_spinnerFirstQuestion.setOnItemSelectedListener(m_onQuestionSelectListener);

        adapter = new ArrayAdapter<SecretQuestion>(this,R.layout.item_spinner_settings, m_arrSecondQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinnerSecondQuestion.setAdapter(adapter);
        m_spinnerSecondQuestion.setOnItemSelectedListener(m_onQuestionSelectListener);

        adapter = new ArrayAdapter<SecretQuestion>(this,R.layout.item_spinner_settings, m_arrThirdQuestions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinnerThirdQuestion.setAdapter(adapter);
        m_spinnerThirdQuestion.setOnItemSelectedListener(m_onQuestionSelectListener);

        ((Button)findViewById(R.id.personal_verification_bt_save)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateAnswers()) {
                    showProgressDialog(R.string.saving_questions);
                    saveAnswers();
                } else {
                    showErrorDialog(R.string.personal_verification_error_save);
                }
            }
        });
    }

    private void saveAnswers()
    {
        ArrayList<SecretQuestion> selectedQuestions = new ArrayList<SecretQuestion>();
        SecretQuestion question = (SecretQuestion)m_spinnerFirstQuestion.getSelectedItem();
        question.setAnswer(m_etFirstAnswer.getText().toString());
        selectedQuestions.add(question);

        question = (SecretQuestion)m_spinnerSecondQuestion.getSelectedItem();
        question.setAnswer(m_etSecondAnswer.getText().toString());
        selectedQuestions.add(question);

        question = (SecretQuestion)m_spinnerThirdQuestion.getSelectedItem();
        question.setAnswer(m_etThirdAnswer.getText().toString());
        selectedQuestions.add(question);
        Bundle extras = new Bundle();
        extras.putParcelableArrayList(WebSecretQuestionsParams.questions.name(), selectedQuestions);
        SmartPagerApplication.getInstance().startWebAction(WebAction.setSecretQuestions, extras);
    }

    private boolean validateAnswers()
    {
        if(TextUtils.isEmpty(m_etFirstAnswer.getText()) || TextUtils.isEmpty(m_etSecondAnswer.getText()) || TextUtils.isEmpty(m_etThirdAnswer.getText()))
                return false;
        return true;
    }

    @Override
    protected boolean isUpNavigationEnabled() {

        return true;
    }


    private void refreshQuestionsLists(boolean isFirstRefresh)
    {
        if(m_arrAvailableQuestions == null || m_arrAvailableQuestions.size() < 3)
            return;
        SecretQuestion firstSelectedQuestion = (isFirstRefresh ? m_arrAvailableQuestions.get(0) : (SecretQuestion) m_spinnerFirstQuestion.getSelectedItem());
        SecretQuestion secondSelectedQuestion = (isFirstRefresh ? m_arrAvailableQuestions.get(1) : (SecretQuestion)m_spinnerSecondQuestion.getSelectedItem());
        SecretQuestion thirdSelectedQuestion = (isFirstRefresh ? m_arrAvailableQuestions.get(2) : (SecretQuestion)m_spinnerThirdQuestion.getSelectedItem());
        fillInQuestions(m_arrFirstQuestions, m_arrAvailableQuestions);
        fillInQuestions(m_arrSecondQuestions, m_arrAvailableQuestions);
        fillInQuestions(m_arrThirdQuestions, m_arrAvailableQuestions);
        int selectedPos = -1;
        if(firstSelectedQuestion != null)
        {
            m_arrSecondQuestions.remove(firstSelectedQuestion);
            m_arrThirdQuestions.remove(firstSelectedQuestion);
        }
        if(secondSelectedQuestion != null)
        {
            m_arrFirstQuestions.remove(secondSelectedQuestion);
            m_arrThirdQuestions.remove(secondSelectedQuestion);
        }
        if(thirdSelectedQuestion != null)
        {
            m_arrSecondQuestions.remove(thirdSelectedQuestion);
            m_arrFirstQuestions.remove(thirdSelectedQuestion);
        }
        replaceSelectedQuestion(m_arrFirstQuestions, firstSelectedQuestion, m_spinnerFirstQuestion.getSelectedItemPosition());
        replaceSelectedQuestion(m_arrSecondQuestions, secondSelectedQuestion, m_spinnerSecondQuestion.getSelectedItemPosition());
        replaceSelectedQuestion(m_arrThirdQuestions, thirdSelectedQuestion, m_spinnerThirdQuestion.getSelectedItemPosition());

        ((ArrayAdapter) m_spinnerFirstQuestion.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) m_spinnerSecondQuestion.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter) m_spinnerThirdQuestion.getAdapter()).notifyDataSetChanged();
    }

    private void replaceSelectedQuestion(LinkedList<SecretQuestion> list, SecretQuestion value, int pos)
    {
        if(pos != -1 && value != null && list.contains(value))
        {
            list.remove(value);
            list.add(pos, value);
        }
    }

    private void fillInQuestions(LinkedList<SecretQuestion> destination, LinkedList<SecretQuestion> values)
    {
        if(values == null || destination == null)
            return;
        destination.clear();
        destination.addAll(values);
    }

    @Override
    protected void onSuccessResponse(WebAction action, AbstractResponse response) {
        if(action == WebAction.getSecretQuestions)
        {
            hideProgressDialog();
            fillInQuestions(m_arrAvailableQuestions, ((GetSecretQuestionsResponse) response).getAvailableQuestions());
            m_bAreQuestionsAvailable = (m_arrAvailableQuestions != null && m_arrAvailableQuestions.size() > 0);
            if(!m_bAreQuestionsAvailable)
            {
                showErrorDialog(R.string.questions_not_available);
            }else
            {
                refreshQuestionsLists(true);
            }
        }
        if(action == WebAction.setSecretQuestions)
        {
            hideProgressDialog();
            showToast(R.string.personal_verification_success_saving_questions);
            SmartPagerApplication.getInstance().getPreferences().setPersonalVerificationQuestionsSaved(true);
            SmartPagerApplication.getInstance().startTaskTimer(new CancelTimerTask(new PersonalVerificationAlertTimer()));
            this.finish();
        }
    }

    @Override
    protected void onErrorResponse(WebAction action, AbstractResponse response) {
        if(action == WebAction.getSecretQuestions)
        {
            hideProgressDialog();
            m_bAreQuestionsAvailable = false;
            showErrorDialog(R.string.questions_not_available);
        }
        if(action == WebAction.setSecretQuestions)
        {
            hideProgressDialog();
            showErrorDialog(R.string.personal_verification_error_saving_questions);
        }
    }

    @Override
    public void onDialogDone(FragmentDialogTag tag, String data) {
        if(!m_bAreQuestionsAvailable)
        {
            this.finish();
        }
    }

    @Override
    public void onDialogNo(FragmentDialogTag tag, String data) {

    }
}
