package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dmitriy on 3/12/14.
 */
public class SetSecretQuestionsCommand extends AbstractUserCommand {

    private static final long serialVersionUID = 4758394040447549951L;

    private ArrayList<SecretQuestion> m_questions;

    @Override
    public HttpEntity getSubEntity(JSONObject root) throws Exception {
        JSONArray questions = new JSONArray();
        for(SecretQuestion question : m_questions)
        {
            questions.put(question.createAnswerObject());
        }
        root.put(WebSecretQuestionsParams.questions.name(), questions);
        return new StringEntity(root.toString());
    }

    @Override
    public WebAction getWebAction() {
        return WebAction.setSecretQuestions;
    }

    @Override
    public void setArguments(Bundle bundle) {
        m_questions = new ArrayList<SecretQuestion>();
        if(bundle != null && bundle.containsKey(WebSecretQuestionsParams.questions.name()))
        {
            m_questions = bundle.getParcelableArrayList(WebSecretQuestionsParams.questions.name());
        }
    }

    @Override
    public AbstractResponse getResponse(String _response) {
        return new BaseResponse(_response);
    }
}
