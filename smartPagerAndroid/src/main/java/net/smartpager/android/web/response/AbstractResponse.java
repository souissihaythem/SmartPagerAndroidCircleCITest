package net.smartpager.android.web.response;

import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebResponceParam;
import net.smartpager.android.web.command.AbstractCommand;
import net.smartpager.android.web.command.GetContactsCommand;
import net.smartpager.android.web.command.GetInboxCommand;
import net.smartpager.android.web.command.GetPagingGroupsCommand;
import net.smartpager.android.web.command.GetSentMessagesCommand;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import biz.mobidev.framework.utils.Log;

/**
 * Abstract representation of server response according to server-side API
 *  @author Roman
 */
public abstract class AbstractResponse implements Serializable {

	private static final long serialVersionUID = -5324305158995559333L;

	private boolean isSuccess;
	private String mErrorMessage;
	private String mJsonString;
	
public JSONObject getJsonObject() throws JSONException {
		return new JSONObject(mJsonString);
	}

	//	private int mErrorCode;
	private boolean mIsInitiatedByUser = true;

	/**
	 * Updates status is used for creating correct SQL query when 
	 * GetUpdatesResponse is processing - <br>
	 *  <tt>INSERT if UpdatesStatus.added<br>  
	 *  UPDATE if UpdatesStatus.modified<br> 
	 *  DELETE if UpdatesStatus.deleted </tt>
	 *  @see #setStatus(UpdatesStatus)
	 *  @see #getStatus()
	 */
	protected UpdatesStatus mStatus;
	
	/**
	 * Post-commands are used for execution of auxiliaries operations after current 
	 * command will be completed,  e.g. we have to execute {@linkplain GetInboxCommand}, 
	 * {@linkplain GetSentMessagesCommand}, {@linkplain GetContactsCommand} and
	 * {@linkplain GetPagingGroupsCommand} according to {@linkplain GetInboxResponse} results
	 * @see #addPostComand(AbstractCommand)
	 * @see #setPostComandList(List)
	 * @see #getPostComandList()
	 */
	protected List<AbstractCommand> mPostCommandList = new ArrayList<AbstractCommand>();

    public AbstractResponse(String responceContent, UpdatesStatus status){
        this(responceContent, status, true);
    }
	
	/**
	 * Constructor
	 * @param response content from server
	 * @param status
	 */
	public AbstractResponse(String response, UpdatesStatus status, boolean startImmediatly){
		Log.i(response);
		setStatus(status);
		mPostCommandList = new ArrayList<AbstractCommand>();
		try {
			mJsonString = response;
			JSONObject jsonObject = new JSONObject(response);
			isSuccess = jsonObject.optBoolean(WebResponceParam.success.name());
			mErrorMessage = jsonObject.optString(WebResponceParam.errorMessage.name());
//			mErrorCode = jsonObject.getInt(WebResponceParam.errorCode.name());
//            if(startImmediatly)
//    			processResponse(jsonObject);
		} catch (JSONException e) {
			isSuccess = false;
			mErrorMessage = "Responce not valid";
//			mErrorCode=-1;
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param response content from server
	 */
	public AbstractResponse(String response){
		this(response, UpdatesStatus.added);
	}

	/**
	 * Parses JSON response from server if response is successful
	 * @param jsonObject
	 * @see #AbstractResponse(String, UpdatesStatus)
	 */
	public abstract void processResponse(JSONObject jsonObject);
	
	/**
	 * @return is server response success
	 */
	public boolean isSuccess() {
		return isSuccess;
	}

	/**
	 * Set response success status
	 * @param isSuccess
	 */
	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	/**
	 * Gets error message text
	 * @return error message
	 */
	public String getMessage() {
		return mErrorMessage;
	}

	/**
	 * Sets error message text
	 * @param message
	 */
	public void setMessage(String message) {
		mErrorMessage = message;
	}

	/**
	 * Replace existing post-command list with new command list
	 * @param new post-command list
	 * @see #mPostCommandList
	 */
	public void setPostComandList(List<AbstractCommand> comandList) {
		this.mPostCommandList = comandList;
	}
	
	/**
	 * Adds single command to the existing post-command list
	 * @param comand
	 * @see #mPostCommandList
	 */
	public void addPostComand(AbstractCommand comand) {
		this.mPostCommandList.add(comand);
	}

	/**
	 * @return post-command list
	 * @see #mPostCommandList
	 */
	public List<AbstractCommand> getPostComandList() {
		return mPostCommandList;
	}
	
	/**
	 * @return current command status
	 * @see #mStatus
	 */
	public UpdatesStatus getStatus(){
		return mStatus;
	}

	/**
	 * Sets command status
	 * @param status
	 * @see #mStatus
	 */
	public void setStatus(UpdatesStatus status){
		mStatus = status;
	}
	
	/**
	 * Determine whether this response comes from a command, initiated by user
	 * but not the application (true by default).
	 * @return
	 */
	public boolean isInitiatedByUser() {
		return mIsInitiatedByUser;
	}
	
	/**
	 * Define whether this response comes from a command, initiated by user
	 * but not the application (true by default).
	 * @param byUser - true if you want it to be 'by user', false otherwise
	 */
	public void setInitiatedByUser(boolean byUser) {
		mIsInitiatedByUser = byUser;
		// set the same value for every Post-command
		for (AbstractCommand everyPostCmd : mPostCommandList) {
			everyPostCmd.setInitiatedByUser(byUser);
		}
	}
}
