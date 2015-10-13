package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by dmitriy on 3/13/14.
 */
public class CheckSecretQuestionCommand extends AbstractUserCommand {

    private static final long serialVersionUID = 4181830928447533951L;

    private SecretQuestion m_question = null;

    @Override
    public HttpEntity getSubEntity(JSONObject root) throws Exception {
        if(m_question != null)
            root.put(WebSecretQuestionsParams.question.name(), m_question.createAnswerObject());
        return new StringEntity(root.toString());
    }

    @Override
    public WebAction getWebAction() {
        return WebAction.checkSecretQuestion;
    }

    @Override
    public void setArguments(Bundle bundle) {
        if(bundle != null && bundle.containsKey(WebSecretQuestionsParams.question.name()))
        {
            m_question = bundle.getParcelable(WebSecretQuestionsParams.question.name());
        }
    }

    @Override
    public AbstractResponse getResponse(String _response) {
        return new BaseResponse(_response);
    }
}
