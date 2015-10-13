package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.GetSecretQuestionResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Created by dmitriy on 3/13/14.
 */
public class GetSecretQuestionCommand extends AbstractUserCommand {

    private static final long serialVersionUID = 4758394040991109951L;

    @Override
    public HttpEntity getSubEntity(JSONObject root) throws Exception {
        return new StringEntity(root.toString());
    }

    @Override
    public WebAction getWebAction() {
        return WebAction.getSecretQuestion;
    }

    @Override
    public void setArguments(Bundle bundle) {

    }

    @Override
    public AbstractResponse getResponse(String _response) {
        GetSecretQuestionResponse response = new GetSecretQuestionResponse(_response);
        return response;
    }
}
