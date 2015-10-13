package net.smartpager.android.web.response;

import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.model.SecretQuestion;

import org.json.JSONObject;

/**
 * Created by dmitriy on 3/13/14.
 */
public class GetSecretQuestionResponse extends AbstractResponse {

    private static final long serialVersionUID = 4716284040447549951L;

    private SecretQuestion m_question = null;

    public GetSecretQuestionResponse(String response) {
        super(response);
    }

    @Override
    public void processResponse(JSONObject jsonObject) {
        if(jsonObject != null)
        {
            m_question = SecretQuestion.parseFromJSON(jsonObject.optJSONObject(WebSecretQuestionsParams.question.name()));
        }
    }

    public SecretQuestion getSecretQuestion()
    {
        return m_question;
    }
}
