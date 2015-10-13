package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.database.DatabaseHelper.PagingGroupTable;
import net.smartpager.android.web.command.GetPagingGroupsCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Response on {@linkplain GetPagingGroupsCommand} <br>
 * <b>Response JSON</b> <br>
 * <tt>{<br>
 * &nbsp;     success: &lt;boolean&gt;, <br>
 * &nbsp;     errorCode: &lt;long: 0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;, <br>
 * &nbsp;     errorMessage: &lt;string&gt;,<br>
 * &nbsp;     pagingGroups: [ <br>
 * &nbsp; &nbsp;     	{ <br>
 * &nbsp; &nbsp; &nbsp;     		id: &lt;long&gt;, <br>
 * &nbsp; &nbsp; &nbsp;     		name: &lt;string&gt;, <br>
 * &nbsp; &nbsp; &nbsp;     		pagerNumber: &lt;string(11 digits)&gt;, <br>
 * &nbsp; &nbsp; &nbsp;     		type&lt;string[�ABSOLUTESCHEDULE�, �SCHEDULE�, �ESCALATION� , �BROADCAST� , �RSS SCHEDULE�]&gt;<br>
 * &nbsp; &nbsp;       }, <br>
 * &nbsp; &nbsp;     	... <br>
 * &nbsp;     ]<br>
 * }</tt><br>
 * 
 * @author Roman
 */
public class GetPagingGroupsResponse extends AbstractResponse{


	private static final long serialVersionUID = 1636046588363157959L;

	public GetPagingGroupsResponse(String responceContent) {
		super(responceContent);
	}

	public GetPagingGroupsResponse(String responceContent, UpdatesStatus status) {
		super(responceContent, status);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		JSONArray array = jsonObject.optJSONArray(ResponseParts.pagingGroups.name());
		if (array != null) {

			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
			for (int i = 0; i != array.length(); i++) {
				JSONObject contactJson = array.optJSONObject(i);
				
				Uri uri = SmartPagerContentProvider.CONTENT_PAGINGROUP_URI;
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
				
				for (PagingGroupTable item : PagingGroupTable.values()) {
					switch (item) {
						case _id:

						break;
						case id:
							switch (getStatus()) {
							case added:
								builder.withValue(item.name(), contactJson.opt(item.name()));			
								break;
							case modified:
								builder.withSelection(item.name() + " = ? ", new String[]{
									contactJson.optString(item.name())});				
								break;		
							default:					
								break;
							}
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
				context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY,  operations);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_PAGINGROUP_URI, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}
		
	}

}
