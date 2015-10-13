package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.model.SecretQuestion;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.VerifySMSResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * In the third step of the handshake the user authenticates the phone number to a user account by providing the SMS'ed
 * confirmation back to the server. On a positive confirmation, the mobile device responds with the uberPassword, which
 * can be subsequently used in conjunction with the phone number to make all authenticated rest calls. <br>
 * Below is the JSON schema used to transmit this data:<br>
 * <b>Request JSON</b><br>
 * <tt>{<br>
 * &nbsp;  phoneNumber: &lt;string(11 digits)&gt;,<br>
 * &nbsp;  confirmationCode: &lt;string(5 digits)&gt;<br>
 * }</tt><br>
 * @author Roman
 * @see VerifySMSResponse
 */
public class VerifySMSCommand extends AbstractCommand {

	private static final long serialVersionUID = -7857284929339869350L;
	private String mNumber;
	private String mConfirmationCode;
    private SecretQuestion m_secretQuestion;

	@Override
	public HttpEntity getEntity() throws Exception {
		JSONObject obj = new JSONObject();
		obj.put(WebSendParam.phoneNumber.name(), mNumber);
		obj.put(WebSendParam.confirmationCode.name(), mConfirmationCode);
        if(m_secretQuestion != null)
            obj.put(WebSendParam.question.name(), m_secretQuestion.createAnswerObject());
		return new StringEntity(obj.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		return new VerifySMSResponse(string);
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.deviceRegistrationStep2;
	}

	@Override
	public void setArguments(Bundle bundle) {
		mNumber = bundle.getString(WebSendParam.phoneNumber.name());
		mConfirmationCode = bundle.getString(WebSendParam.confirmationCode.name());
        if(bundle.containsKey(WebSendParam.question.name()))
            m_secretQuestion = (SecretQuestion) bundle.getSerializable(WebSendParam.question.name());
	}

}
