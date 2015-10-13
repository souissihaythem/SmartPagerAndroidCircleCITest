package net.smartpager.android.web.response;

import net.smartpager.android.consts.WebSecretQuestionsParams;
import net.smartpager.android.model.SecretQuestion;

import org.json.JSONObject;

/**
 * Created by dmitriy on 3/14/14.
 */
public class VerifyPhoneResponse extends AbstractResponse {

    private static final long serialVersionUID = 532895158995559758L;

    private SecretQuestion m_currQuestion;

    public VerifyPhoneResponse(String response) {
        super(response);
    }

    @Override
    public void processResponse(JSONObject jsonObject) {
        if(jsonObject != null)
        {
            m_currQuestion = SecretQuestion.parseFromJSON(jsonObject.optJSONObject(WebSecretQuestionsParams.question.name()));
        }
    }

    public SecretQuestion getSecretQuestion()
    {
        return m_currQuestion;
    }
}
