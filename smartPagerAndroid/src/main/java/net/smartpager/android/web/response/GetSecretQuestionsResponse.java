package net.smartpager.android.web.response;

import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.model.SecretQuestion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by dmitriy on 3/11/14.
 */
public class GetSecretQuestionsResponse extends AbstractResponse {

    private LinkedList<SecretQuestion> m_questions = null;
    private ArrayList<Integer> m_answeredQuestionsIDs = null;

    public GetSecretQuestionsResponse(String responseContent) {
        super(responseContent);
    }

    @Override
    public void processResponse(JSONObject jsonObject) {
        m_questions = new LinkedList<SecretQuestion>();
        m_answeredQuestionsIDs = new ArrayList<Integer>();
        JSONArray questions = jsonObject.optJSONArray(ResponseParts.questions.name());
        JSONObject question = null;
        if (questions != null) {
            SecretQuestion secretQuestion = null;
            for(int i = 0; i < questions.length(); i++)
            {
                question = questions.optJSONObject(i);
                secretQuestion = SecretQuestion.parseFromJSON(question);
                if(secretQuestion != null)
                    m_questions.add(secretQuestion);
            }
        }
        JSONArray selectedQuestions = jsonObject.optJSONArray(ResponseParts.answeredQuestions.name());
        if (selectedQuestions != null) {
            for(int i = 0; i < selectedQuestions.length(); i++)
            {
                m_answeredQuestionsIDs.add(selectedQuestions.optInt(i));
            }
        }
    }

    public ArrayList<Integer> getAnsweredQuestions()
    {
        return m_answeredQuestionsIDs;
    }

    public LinkedList<SecretQuestion> getQuestions()
    {
        return m_questions;
    }

    public LinkedList<SecretQuestion> getAvailableQuestions()
    {
        LinkedList<SecretQuestion> res = new LinkedList<SecretQuestion>();
        if(m_questions != null)
        {
            for(SecretQuestion question : m_questions)
            {
                if(question.isEnabled())
                    res.add(question);
            }
        }
        return res;
    }
}
