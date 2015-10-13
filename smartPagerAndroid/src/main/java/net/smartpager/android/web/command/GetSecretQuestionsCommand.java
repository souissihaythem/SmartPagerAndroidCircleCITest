package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetSecretQuestionsResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by dmitriy on 3/11/14.
 */
public class GetSecretQuestionsCommand extends AbstractUserCommand {

    private static final long serialVersionUID = -514976728500289368L;

    @Override
    public WebAction getWebAction() {
        return WebAction.getSecretQuestions;
    }

    @Override
    public void setArguments(Bundle bundle) {

    }

    @Override
    public AbstractResponse getResponse(String _response) {
        GetSecretQuestionsResponse response = new GetSecretQuestionsResponse(_response);
        return response;
    }

    @Override
    public HttpEntity getSubEntity(JSONObject root) throws Exception {
        return new StringEntity(root.toString());
    }
}
