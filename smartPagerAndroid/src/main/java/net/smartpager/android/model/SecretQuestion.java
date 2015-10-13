package net.smartpager.android.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by dmitriy on 3/12/14.
 */
public class SecretQuestion implements Parcelable, Serializable {

    private static final long serialVersionUID = -3281666207549482890L;

    private static final String FIELD_ID = "id";
    private static final String FIELD_ENABLED = "enabled";
    private static final String FIELD_QUESTION = "question";
    private static final String FIELD_CLASS = "class";
    private static final String FIELD_ANSWER = "answer";

    private int m_id = -1;
    private String m_sQuestion = "";
    private String m_sClass = "";
    private String m_sAnswer = "";
    private boolean m_bIsEnabled;

    public SecretQuestion()
    {
    }

    public SecretQuestion(Parcel in)
    {
        m_id = in.readInt();
        m_sQuestion = in.readString();
        m_sClass = in.readString();
        m_sAnswer = in.readString();
        m_bIsEnabled = (in.readByte() == 1);
    }

    public SecretQuestion(int _id, String _question, String _class, boolean isEnabled, String _answer)
    {
        m_bIsEnabled = isEnabled;
        m_sClass = _class;
        m_sQuestion = _question;
        m_id = _id;
        m_sAnswer = _answer;
    }

    static public SecretQuestion parseFromJSON(JSONObject object)
    {
        if(object == null)
            return null;
        SecretQuestion question = new SecretQuestion();
        question.m_id = object.optInt(FIELD_ID);
        question.m_bIsEnabled = object.optBoolean(FIELD_ENABLED);
        question.m_sQuestion = object.optString(FIELD_QUESTION);
        question.m_sClass = object.optString(FIELD_CLASS);
        return question;
    }

    public void setAnswer(String _answer)
    {
        m_sAnswer = _answer;
    }

    public int getID()
    {
        return m_id;
    }

    public String getQuestionClass()
    {
        return m_sClass;
    }

    public boolean isEnabled()
    {
        return m_bIsEnabled;
    }

    public String getQuestion()
    {
        return m_sQuestion;
    }

    public String getAnswer()
    {
        return m_sAnswer;
    }

    public JSONObject createAnswerObject() throws Exception
    {
        JSONObject res = new JSONObject();
        res.put(FIELD_ID, m_id);
        res.put(FIELD_ANSWER, m_sAnswer);
        return res;
    }

    @Override
    public String toString() {
        return m_sQuestion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(m_id);
        dest.writeString(m_sQuestion);
        dest.writeString(m_sClass);
        dest.writeString(m_sAnswer);
        dest.writeByte((byte) (m_bIsEnabled ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SecretQuestion createFromParcel(Parcel in) {
            return new SecretQuestion(in);
        }

        public SecretQuestion[] newArray(int size) {
            return new SecretQuestion[size];
        }
    };
}
