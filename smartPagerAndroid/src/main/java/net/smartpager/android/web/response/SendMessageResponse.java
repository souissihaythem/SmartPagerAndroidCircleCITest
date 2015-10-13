package net.smartpager.android.web.response;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.web.command.GetUpdatesCommand;
import net.smartpager.android.web.command.SendMessageCommand;

import org.json.JSONObject;

/**
 * Response on {@linkplain SendMessageCommand} <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;      success: &lt;true/false&gt;,<br>
 * &nbsp;      errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;      errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;      messageId: &lt;long&gt;,<br>
 * &nbsp;      threadID: &lt;int&gt;,<br>
 * &nbsp;      timeSent: &lt;datetime&gt;<br>
 * ]</tt><br>
 * 
 * @author Roman
 */
public class SendMessageResponse extends AbstractResponse {

	private static final long serialVersionUID = 4407175551625284083L;

	public SendMessageResponse(String responceContent, UpdatesStatus status) {
		super(responceContent, status);
	}

	public SendMessageResponse(String responceContent) {
		super(responceContent);
	}

	/**
	 * Adds {@linkplain GetUpdatesCommand} to post-command list
	 */
	@Override
	public void processResponse(JSONObject jsonObject) {
		if(isSuccess()){
//			GetSentMessagesCommand command = new GetSentMessagesCommand();
//			List<Integer>ids = new ArrayList<Integer>();
//			ids.add(jsonObject.optInt("messageId"));
//			command.setIds(ids);
//			addPostComand(command);
		}
	}
}
