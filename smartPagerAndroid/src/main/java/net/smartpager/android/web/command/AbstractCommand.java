package net.smartpager.android.web.command;

import android.os.Bundle;

import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.service.WebService;
import net.smartpager.android.web.RequestSender;
import net.smartpager.android.web.response.AbstractResponse;

import org.apache.http.HttpEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
/**
 * Abstract representation of web-command according to server-side API
 *  @author Roman
 */
public abstract class AbstractCommand implements Serializable {

	private static final long serialVersionUID = -6623825306376957344L;
	
	/**
	 * Pre-commands are used for execution of auxiliaries operations before currentcurent command starts,  
	 * e.g. we have to obtain images and voice records URLs for compose {@linkplain SendMessageCommand},
	 * so first {@linkplain PutFileCommand} for all attachments has to be executed 
	 * @see #addPreCommand(AbstractCommand)
	 * @see #setPreCommandList(List)
	 * @see #getPreCommandList()
	 */
	protected List<AbstractCommand> mPreCommandList = new ArrayList<AbstractCommand>();	
	
	/**
	 * Result list contains results of pre-commands execution
	 * and is used for creating additional commands parameters   
	 * such as images and voice records URL
	 * @see #setPreCommandResults(List)
	 */
	protected List<String> mPreCommandResultList;

	public WebAction getResponseAction() {
		return getWebAction();
	}
	
	/**
	 * Updates status is used for creating correct SQL query when 
	 * GetUpdatesResponse is processing - <br>
	 *  <tt>INSERT if UpdatesStatus.added<br>  
	 *  UPDATE if UpdatesStatus.modified<br> 
	 *  DELETE if UpdatesStatus.deleted </tt>
	 *  @see #setStatus(UpdatesStatus)
	 *  @see #getStatus()
	 */
	protected UpdatesStatus mStatus = UpdatesStatus.added;

	private boolean mIsInitiatedByUser = true;
    protected boolean m_bWasCancelled = false;

	/**
	 * constructor
	 */
	public AbstractCommand() {
	}
	
	/**
	 * Forms additional parameters for POST-request
	 * @see RequestSender
	 * @return 
	 * @throws Exception
	 */
	public abstract HttpEntity getEntity() throws Exception;

	/**
	 * Returns web-action name according to server-side API  
	 * for URL GET- and POST-request formation
	 * @see #getUrl()
	 * @return action name of current command
	 */
	public abstract WebAction getWebAction();		

	/**
	 * Sets additional parameters for current command via the {@link Bundle bundle}
	 * according to server-side API
	 * @param bundle with parameter set as key-value pairs
	 * @see WebService
	 */
	public abstract void setArguments(Bundle bundle);
	
	/**
	 * Forms URL for GET- and POST-requests in <i><b>"base_rest_url/web_action_name"</b></i> format
	 * @return URL string representation
	 * @see RequestSender
	 */
	public String getUrl() {
		return String.format("%s/%s", Constants.BASE_REST_URL, getWebAction());
	}

	/**
	 * Forms response object based on sever JSON response string 
	 * @param response content from server
	 * @return Response object 
	 * @see AbstractResponse
	 * @see WebService
	 */
	public abstract AbstractResponse getResponse(String response);
	
	/**
	 * Forms response object if an error is occuring during command execution 
	 * @param error message
	 * @return Response object
	 * @see AbstractResponse
	 */
	public AbstractResponse getErrorResponce(String error){
		AbstractResponse abstractResponce = getResponse("");
		abstractResponce.setSuccess(false);
		abstractResponce.setMessage(error);
		return abstractResponce;
	}	
	
	/**
	 * Replace existing pre-command list with new command list
	 * @param new pre-command list
	 * @see #mPreCommandList
	 */
	public void setPreCommandList(List<AbstractCommand> commandList) {
		this.mPreCommandList = commandList;
	}

    public void cancel()
    {
        m_bWasCancelled = true;
    }

	/**
	 * Adds single command to the existing pre-command list
	 * @param command
	 * @see #mPreCommandList
	 */
	public void addPreCommand(AbstractCommand command) {
		this.mPreCommandList.add(command);
	}
	
	/**
	 * @return pre-commands list
	 * @see #mPreCommandList
	 */
	public List<AbstractCommand> getPreCommandList() {
		return mPreCommandList;
	}
	
	/**
	 * Set results of pre-commands execution
	 * @param pre-command results list
	 * @see #mPreCommandResultList
	 */
	public void setPreCommandResults(List<String> results) {
		this.mPreCommandResultList = results;
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
	

    public boolean wasCancelled()
    {
        return m_bWasCancelled;
    }

	/**
	 * Determine whether this command initiated by user
	 * but not the application (true by default).
	 * @return
	 */
	public boolean isInitiatedByUser() {
		return mIsInitiatedByUser;
	}
	
	/**
	 * Define whether this command, initiated by user
	 * but not the application (true by default).
	 * @param byUser - true if you want it to be 'by user', false otherwise
	 */
	public void setInitiatedByUser(boolean byUser) {
		
		mIsInitiatedByUser = byUser;
		
		// set the same value for every Pre-command
		for (AbstractCommand everyPreCmd : mPreCommandList) {
			everyPreCmd.setInitiatedByUser(byUser);
		}
	}
}