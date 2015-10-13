package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendParam;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.BaseResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

/**
 * Set the status of the authenticated user as either DO_NOT_DISTURB or ONLINE.<br>
 * <b>Request JSON</b><br>
 * <tt>{ <br>
 *&nbsp; 	smartPagerID: &lt;string&gt;,<br> 
 *&nbsp; 	uberPassword: &lt;string&gt;, <br>
 *&nbsp; 	status: &lt;string[ "DO_NOT_DISTURB", "ONLINE" ]&gt;<br> 
 * }</tt><br>
 * <b>Response JSON</b><br>
 * <tt>{ <br>
 *&nbsp; 	success: &lt;boolean&gt;,<br> 
 *&nbsp; 	errorCode: &lt;integer&gt;, <br>
 *&nbsp; 	errorMessage: &lt;string&gt; <br>
 * }</tt><br>
 * 
 * @author Roman
 */
public class SetStatusCommand extends AbstractUserCommand {

	private static final long serialVersionUID = -8607776562001504079L;
	private String mPagerStatus = ContactStatus.DO_NOT_DISTURB.name();

	public String getPagerStatus() {
		return mPagerStatus;
	}

	public void setPagerStatus(String pagerStatus) {
		mPagerStatus = pagerStatus;
	}

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		root.put(WebSendParam.status.name(), mPagerStatus);
		return new StringEntity(root.toString());
	}

	/**
	 * If server response is successful - ends synchronization process and sets the status  
	 */
	@Override
	public AbstractResponse getResponse(String string) {
//		if(!Preferences.isSynchronized()) {
//			Preferences.setSynchronised(true);
//		}
		BaseResponse baseResponce = new BaseResponse(string);
		if(baseResponce.isSuccess()){
            SmartPagerApplication.getInstance().getPreferences().setStatus(mPagerStatus);
		}
		return baseResponce;
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.setStatus;
	}

	@Override
	public void setArguments(Bundle bundle) {
		if (bundle != null){
			mPagerStatus = bundle.getString(WebSendParam.status.name());
		}

	}
}
