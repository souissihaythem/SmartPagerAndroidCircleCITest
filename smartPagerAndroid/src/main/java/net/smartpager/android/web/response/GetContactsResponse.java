package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.utils.JsonParserUtil;
import net.smartpager.android.web.command.GetContactsCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Response on {@linkplain GetContactsCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;   success: &lt;true/false&gt;,<br>
 * &nbsp;   errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;   errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;   contacts: [<br>
 * &nbsp;&nbsp;        {<br>
 *  &nbsp; &nbsp; &nbsp;            id: &lt;long&gt;,<br>
 * &nbsp; &nbsp; &nbsp;            smartPagerID: &lt;string&gt;,<br>
 *  &nbsp; &nbsp; &nbsp;             status: &lt;ONLINE, OFFLINE, BAD_CONNECTION&gt;,<br>
 * &nbsp; &nbsp; &nbsp;             title: &lt;string&gt;,<br>
 * &nbsp; &nbsp; &nbsp;             firstName: &lt;string&gt;,<br>
 * &nbsp; &nbsp; &nbsp;             lastName: &lt;string&gt;,<br>
 *  &nbsp; &nbsp; &nbsp;             phoneNumber: &lt;string&gt;,<br>
 * &nbsp; &nbsp; &nbsp;             pagerNumber: &lt;string(11 digits)&gt;,<br>
 * &nbsp; &nbsp; &nbsp;            departments: [&lt;string(department name)&gt;, ...],<br>
 * &nbsp; &nbsp; &nbsp;             accountName: &lt;string(aka organization name)&gt;<br>
 * &nbsp;&nbsp; },<br>
 *         ...<br>
 * &nbsp;   ]<br>
 * ]</tt><br>
 * 
 * @author Roman
 */
public class GetContactsResponse extends AbstractResponse {

	private static final long serialVersionUID = -2518921945357309114L;

    private boolean m_bIsNeedResetOldContacts = false;

	public GetContactsResponse(String responceContent) {
		super(responceContent);
	}

	public GetContactsResponse(String responceContent, UpdatesStatus status) {
		super(responceContent, status);
	}

    public GetContactsResponse(String responceContent, UpdatesStatus status, boolean startImmediatly) {
        super(responceContent, status, startImmediatly);
    }

	@Override
	public void processResponse(JSONObject jsonObject) {
		
		JSONArray array = jsonObject.optJSONArray(ResponseParts.contacts.name());
		if (array != null) {
			// context.getContentResolver().applyBatch(authority, operations)
			// ContentProviderOperation operation = new ContentProviderOperation()
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			for (int i = 0; i != array.length(); i++) {
				JSONObject contactJson = array.optJSONObject(i);
				
				Uri uri = SmartPagerContentProvider.CONTENT_CONTACT_URI;
				Builder builder = ContentProviderOperation.newInsert(uri);
				
				switch (getStatus()) {
				case added:
					builder = ContentProviderOperation.newInsert(uri);				
					break;
				case modified:
					builder = ContentProviderOperation.newUpdate(uri);	
					
					break;		
				default:					
					break;
				}
				
				for (ContactTable item : ContactTable.values()) {
					switch (item) {
						case _id:

						break;
						case id:
							switch (getStatus()) {
							case added:
								builder.withValue(item.name(),
										contactJson.optString(item.name()));				
								break;
							case modified:
								builder.withSelection(item.name() + " = ? ", new String[]{
									contactJson.optString(item.name())});
								break;		
							default:					
								break;
							}
						break;
						case departments:
							builder.withValue(item.name(),
									JsonParserUtil.getStringFromJsonArray(contactJson.optJSONArray(item.name()), "\n"));
						break;
						case photoUrl:
							String photoUrl="";
							if( contactJson.optInt("hasPhoto")>0) {
								photoUrl = JsonParserUtil.getUrl(contactJson.optString(ContactTable.id.name()));								
								if (getStatus().equals(UpdatesStatus.modified)) {
									SmartPagerApplication.getInstance().getImageLoader().clearCacheFor(photoUrl);
								}
							}
							builder.withValue(item.name(), photoUrl);
							break;
						default:
							builder.withValue(item.name(), contactJson.opt(item.name()));
						break;
					}
				}
				
				operations.add(builder.build());
			}
			Context context = SmartPagerApplication.getInstance().getApplicationContext();
            try {
                if(m_bIsNeedResetOldContacts && array.length() > 0)
                {
                    removeContacts();
                    m_bIsNeedResetOldContacts = false;
                }
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CONTACT_URI, null);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
	}

    /**
     * This method will delete all contacts from BD
     */
    private void removeContacts()
    {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation.newDelete(SmartPagerContentProvider.CONTENT_CONTACT_URI);
        operations.add(builder.build());
        Context context = SmartPagerApplication.getInstance().getApplicationContext();
        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY,  operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void setResetOldContacts (boolean value)
    {
        m_bIsNeedResetOldContacts = value;
    }
}
