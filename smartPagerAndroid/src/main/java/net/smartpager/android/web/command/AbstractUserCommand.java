package net.smartpager.android.web.command;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.WebSendParam;

import org.apache.http.HttpEntity;
import org.json.JSONObject;

import biz.mobidev.framework.utils.Log;

/**
 * Abstract representation of web-command according to server-side API
 * with filled <i>"smartPagerID"</i> and <i>"uberPassword"</i> fields
 * @author Roman
 * @see AbstractCommand
 */
public abstract class AbstractUserCommand extends AbstractCommand {
	private static final long serialVersionUID = 4181836140447549951L;

	private String mUserID;
	private String mPassword;
	public AbstractUserCommand(){
		mUserID = SmartPagerApplication.getInstance().getPreferences().getUserID();
		mPassword = SmartPagerApplication.getInstance().getPreferences().getPassword();
	}
	
	/**
	 * Forms additional parameters for POST-request, called in 
	 * {@linkplain #getEntity()} method.<br>. 
	 * <b>root</b> parameter already contains <i>"smartPagerID"</i> and <i>"uberPassword"</i> fields
	 * @param json-root to form JSON request
	 * @return
	 * @throws Exception
	 * @see #getEntity
	 */
	public abstract HttpEntity getSubEntity(JSONObject root) throws Exception;

	@Override
	public HttpEntity getEntity() throws Exception {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(WebSendParam.smartPagerID.name(), mUserID);
		jsonObject.put(WebSendParam.uberPassword.name(), mPassword);
		HttpEntity entity = getSubEntity(jsonObject);
		Log.d(getWebAction(), jsonObject);		
		return entity;
	}
}
