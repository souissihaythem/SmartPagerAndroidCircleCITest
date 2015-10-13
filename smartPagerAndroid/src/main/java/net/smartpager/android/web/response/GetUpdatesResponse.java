package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.UpdatesParams;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.model.RequestedContactItem;
import net.smartpager.android.web.command.AbstractCommand;
import net.smartpager.android.web.command.GetContactsCommand;
import net.smartpager.android.web.command.GetInboxCommand;
import net.smartpager.android.web.command.GetPagingGroupsCommand;
import net.smartpager.android.web.command.GetSentMessagesCommand;
import net.smartpager.android.web.command.GetUpdatesCommand;
import net.smartpager.android.web.command.RequestContactCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Response on {@linkplain GetUpdatesCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;     success: &lt;true/false&gt;,<br>
 * &nbsp;     errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;     errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;     date: &lt;datetime&gt;,<br>
 * &nbsp;     contacts: [ &nbsp; &nbsp; &nbsp; &nbsp; <i>// Only add if there are new contacts to be added</i><br>
 * &nbsp; &nbsp;   added: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    modified: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;     deleted: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;     ]<br>
 * &nbsp;    ],<br>
 * &nbsp;     inboxMessages: [ &nbsp; &nbsp; &nbsp; &nbsp; <i>// Only add if there are new inbox messages to be added</i><br>
 * &nbsp; &nbsp;   added: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;   ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    modified: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    statusUpdates: { <br>
 * &nbsp; &nbsp; &nbsp;&lt;long,message id&gt;:<br> 
 * &nbsp; &nbsp; &nbsp; 	{ <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &lt;long,user id&gt;:<br> 
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; 	{ <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  timeDelayed: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  timeFailed: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  timeDelivered: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;    timeRead: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  timeReplied: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  timeDeleted: &lt;datetime(optional)&gt; <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;  	}, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp;  ... <br>
 * &nbsp; &nbsp; &nbsp; 	}, <br>
 * &nbsp; &nbsp; &nbsp;... <br>
 * &nbsp; &nbsp;    }<br>
 * &nbsp;     ],<br>
 * &nbsp;     sentMessages: [ &nbsp; &nbsp; &nbsp; &nbsp; <i>// Only add if there are new sent messages to be added</i><br>
 * &nbsp; &nbsp;    added: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    modified: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ] <br>
 * &nbsp; &nbsp;    statusUpdates: { <br>
 * &nbsp; &nbsp; &nbsp;&lt;long,message id&gt;:<br> 
 * &nbsp; &nbsp; &nbsp; &nbsp;     	{ <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeDelayed: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeFailed: &lt;datetime(optional)&gt;,<br> 
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeDelivered: &lt;datetime(optional)&gt;,<br> 
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeRead: &lt;datetime(optional)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeReplied: &lt;datetime(optional)&gt;,<br> 
 * &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; timeDeleted: &lt;datetime(optional)&gt; <br>
 * &nbsp; &nbsp; &nbsp; &nbsp;	}, <br>
 * &nbsp; &nbsp; &nbsp;... <br>
 * &nbsp; &nbsp;    }<br>
 * &nbsp;     ],<br>
 * &nbsp;     fromGroupId: [ &nbsp; &nbsp; &nbsp; &nbsp; <i>// Only add if there are new fromGroupId to be added</i><br>
 * &nbsp; &nbsp;    added: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    modified: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ],<br>
 * &nbsp; &nbsp;    deleted: [<br>
 * &nbsp; &nbsp; &nbsp;    {&lt;long&gt;},<br>
 * &nbsp; &nbsp; &nbsp;    ...<br>
 * &nbsp; &nbsp;    ]<br>
 * &nbsp;     ] ,<br>
 * &nbsp;     contactRequests: [ <br>
 * &nbsp; &nbsp;     	{ <br>
 * &nbsp; &nbsp; &nbsp; smartPagerID: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp; mobileNubmer: &lt;string(11 digits)&gt;, <br>
 * &nbsp; &nbsp; &nbsp; title: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp; firstName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp; lastName: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp; contactID: &lt;long&gt; <br>
 * &nbsp; &nbsp;     	}, <br>
 * &nbsp; &nbsp;     	... <br>
 * &nbsp; ]<br>
 * ]</tt><br>
 * 
 * @author Roman
 */
public class GetUpdatesResponse extends AbstractResponse {
	
	private static final long serialVersionUID = 1779612281659465883L;

	private String mDate;
    private List<Integer> mAddedGroups;
    private List<Integer> mDeletedGroups;
    private List<Integer> mModifiedGroups;
    private List<Integer> mAddedInbox;
    private List<Integer> mAddedSent;
    private List<Integer> mModifiedSent;
    private List<Integer> mModifiedInbox;
    private List<Integer> mAddedContacts;
	private List<Integer> mDeletedContacts;
	private List<Integer> mModifiedContacts;
	private List<RequestedContactItem> mRequestedContacts;

	public GetUpdatesResponse(String responceContent) {
		super(responceContent);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		if (isSuccess()){
            init(jsonObject);
            parse(jsonObject);
            prepareCommands();
        }
	}
	
	/**
	 * Sets LastUpdate date and initialize auxiliaries id-lists for post-commands
	 * @param jsonObject
	 */
	private void init(JSONObject jsonObject){
		mDate = jsonObject.optString(UpdatesParams.date.name());
        SmartPagerApplication.getInstance().getPreferences().setLastUpdate(mDate);
        mAddedInbox = new ArrayList<Integer>();
        mAddedSent = new ArrayList<Integer>();
        mModifiedInbox = new ArrayList<Integer>();
        mModifiedSent = new ArrayList<Integer>();
        mAddedContacts = new ArrayList<Integer>();
        mModifiedContacts = new ArrayList<Integer>();
        mDeletedContacts = new ArrayList<Integer>();
        mAddedGroups = new ArrayList<Integer>();
        mModifiedGroups = new ArrayList<Integer>();
        mDeletedGroups = new ArrayList<Integer>();
        mRequestedContacts = new ArrayList<RequestedContactItem>();
	}
	
	/**
	 * Parses JSON-response from server and fills all auxiliaries id-lists for post-commands
	 * @param jsonObject
	 * @see #parseInboxChanges
	 * @see #parseContactRequests
	 */
	private void parse(JSONObject jsonObject) {
		parseItemsChanges(	jsonObject,mAddedInbox,
							UpdatesParams.inboxMessages.name(),
							UpdatesStatus.added.name());
		parseItemsChanges(	jsonObject,mModifiedInbox, 
							UpdatesParams.inboxMessages.name(), 
							UpdatesStatus.modified.name());
		parseItemsChanges(	jsonObject,mAddedSent,
							UpdatesParams.sentMessages.name(),
							UpdatesStatus.added.name());
		parseItemsChanges(	jsonObject,mModifiedSent,
							UpdatesParams.sentMessages.name(),
							UpdatesStatus.modified.name());
		parseItemsChanges(	jsonObject,mAddedContacts,
							UpdatesParams.contacts.name(),
							UpdatesStatus.added.name());
		parseItemsChanges(	jsonObject,mModifiedContacts,
							UpdatesParams.contacts.name(),
							UpdatesStatus.modified.name());
		parseItemsChanges(	jsonObject,mDeletedContacts,
							UpdatesParams.contacts.name(),
							UpdatesStatus.deleted.name());
		parseItemsChanges(	jsonObject,mDeletedGroups,
							UpdatesParams.groups.name(),
							UpdatesStatus.deleted.name());
		parseItemsChanges(	jsonObject,mAddedGroups,
							UpdatesParams.groups.name(),
							UpdatesStatus.added.name());
		parseItemsChanges(	jsonObject,mModifiedGroups,
							UpdatesParams.groups.name(),
							UpdatesStatus.modified.name());
		parseContactRequests(jsonObject,mRequestedContacts,
							UpdatesParams.contactRequests.name());
	}
	

	/**
	 * Fills post-command list for calling {@linkplain GetInboxCommand},
	 * {@linkplain GetSentMessagesCommand} ,  {@linkplain GetContactsCommand} , 
	 * {@linkplain GetPagingGroupsCommand} according to JSON response and statuses
	 *  ( added, modified, deleted)
	 */
	private void prepareCommands(){
		
		List<AbstractCommand> commandsList = new ArrayList<AbstractCommand>();
		// inbox messages
		if (mAddedInbox != null && mAddedInbox.size() > 0){
			GetInboxCommand inboxAddedCommand = new GetInboxCommand();
			inboxAddedCommand.setStatus(UpdatesStatus.added);
			inboxAddedCommand.setIds(mAddedInbox);
			commandsList.add(inboxAddedCommand);			
		}
		if (mModifiedInbox != null && mModifiedInbox.size() > 0){
			GetInboxCommand inboxModifiedCommand = new GetInboxCommand();
			inboxModifiedCommand.setStatus(UpdatesStatus.modified);
			inboxModifiedCommand.setIds(mModifiedInbox);
			commandsList.add(inboxModifiedCommand);			
		}
		// sent messages
		if (mAddedSent != null && mAddedSent.size() > 0){
			GetSentMessagesCommand sentAddedCommand = new GetSentMessagesCommand();
			sentAddedCommand.setStatus(UpdatesStatus.added);
			sentAddedCommand.setIds(mAddedSent);
			commandsList.add(sentAddedCommand);			
		}
		if (mModifiedSent != null && mModifiedSent.size() > 0){
			GetSentMessagesCommand sentModifiedCommand = new GetSentMessagesCommand();
			sentModifiedCommand.setStatus(UpdatesStatus.modified);
			sentModifiedCommand.setIds(mModifiedSent);
			commandsList.add(sentModifiedCommand);			
		}
		// contacts		
		if (mAddedContacts != null && mAddedContacts.size() > 0){
			GetContactsCommand contactsAddedCommand = new GetContactsCommand();
			contactsAddedCommand.setStatus(UpdatesStatus.added);
			contactsAddedCommand.setIds(mAddedContacts);
			commandsList.add(contactsAddedCommand);			
		}
		if (mModifiedContacts != null && mModifiedContacts.size() > 0){			
			GetContactsCommand contactsModifiedCommand = new GetContactsCommand();
			contactsModifiedCommand.setStatus(UpdatesStatus.modified);
			contactsModifiedCommand.setIds(mModifiedContacts);
			commandsList.add(contactsModifiedCommand);			
		}
		if (mDeletedContacts != null && mDeletedContacts.size() > 0){
			delete(	SmartPagerContentProvider.CONTENT_CONTACT_URI,
					ContactTable.id.name(),
					mDeletedContacts);
		}
		// fromGroupId
		if (mAddedGroups != null && mAddedGroups.size() > 0){
			GetPagingGroupsCommand groupsAddedCommand = new GetPagingGroupsCommand();
			groupsAddedCommand.setStatus(UpdatesStatus.added);
			groupsAddedCommand.setIds(mAddedGroups);
			commandsList.add(groupsAddedCommand);			
		}
		if (mModifiedGroups != null && mModifiedGroups.size() > 0){
			GetPagingGroupsCommand groupsModifiedCommand = new GetPagingGroupsCommand();
			groupsModifiedCommand.setStatus(UpdatesStatus.modified);
			groupsModifiedCommand.setIds(mModifiedGroups);
			commandsList.add(groupsModifiedCommand);			
		}
		if (mDeletedGroups != null && mDeletedGroups.size() > 0){
			delete(	SmartPagerContentProvider.CONTENT_PAGINGROUP_URI,
					PagingGroupTable.id.name(),
					mDeletedGroups);			
		}
		this.setPostComandList(commandsList);
	}			
	
	
	/**
	 * Helper method for parsing JSON-response for messages, fromGroupId and contacts
	 * @param obj
	 * @param list
	 * @param root
	 * @param subRoot
	 * @see #parse
	 */
    private void parseItemsChanges(JSONObject obj, List<Integer> list, String root, String subRoot) {
        JSONObject mes = obj.optJSONObject(root);
        if (mes==null){
            return;
        }
        JSONArray items = mes.optJSONArray(subRoot);
        if (items==null){
            return;
        }
        for (int i=0;i<items.length();++i){
            list.add(items.optInt(i));
        }
    }
    
    /**
     * Helper method for parsing JSON-response for contactRequests
     * @param obj
     * @param list
     * @param root
     * @see #parse
     * @see RequestContactCommand
     */
	private void parseContactRequests(JSONObject obj, List<RequestedContactItem> list, String root) {
		JSONArray items = obj.optJSONArray(root);
		if (items == null || items.length() == 0) return;

		for (int i = 0; i < items.length(); ++i) {
			JSONObject item = items.optJSONObject(i);
			if (item != null) {
				RequestedContactItem rci = RequestedContactItem.getFromJSONObject(item);
				list.add(rci);
			}
		}
	}
    
	/**
	 * Helper method for deleting records from database if item status is "deleted"
	 * @param uri
	 * @param key for WHERE clause composing
	 * @param ids list for WHERE clause composing
	 */
    private void delete(Uri uri, String key, List<Integer> ids){			
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (int i = 0; i != ids.size(); i++) {			
			String id = String.valueOf(ids.get(i));
			Builder builder = ContentProviderOperation.newDelete(uri);
			builder.withSelection(key + " = ? ", new String[] {id});			
			operations.add(builder.build());
		}
		Context context = SmartPagerApplication.getInstance().getApplicationContext();
		try {
		context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY,  operations);
		
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}
		
    }
    
	public List<Integer> getAddedGroups() {
		return mAddedGroups;
	}
	
	public void setAddedGroups(List<Integer> addedGroups) {
		this.mAddedGroups = addedGroups;
	}
	
	public List<Integer> getDeletedGroups() {
		return mDeletedGroups;
	}
	
	public void setDeletedGroups(List<Integer> deletedGroups) {
		this.mDeletedGroups = deletedGroups;
	}
	
	public List<Integer> getModifiedGroups() {
		return mModifiedGroups;
	}
	
	public void setModifiedGroups(List<Integer> modifiedGroups) {
		this.mModifiedGroups = modifiedGroups;
	}
    
    public List<Integer> getDeletedContacts() {
        return mDeletedContacts;
    }

    public List<Integer> getModifiedContacts() {
        return mModifiedContacts;
    }

    public String getDate() {
        return this.mDate;
    }

    public List<Integer> getAddedInbox() {
        return mAddedInbox;
    }

    public List<Integer> getAddedSent() {
        return mAddedSent;
    }

    public List<Integer> getAddedContacts() {
        return mAddedContacts;
    }

    public List<Integer> getModifiedInbox() {
        return mModifiedInbox;
    }
    
	public List<RequestedContactItem> getRequestedContacts() {
		return mRequestedContacts;
	}

}
