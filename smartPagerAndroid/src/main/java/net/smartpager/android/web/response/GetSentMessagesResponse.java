package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.service.FileDownloadService;
import net.smartpager.android.service.FileDownloadService.DownloadFileAction;
import net.smartpager.android.web.command.GetSentMessagesCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
/**
 * Response on {@linkplain GetSentMessagesCommand}<br>
 * <b>Response JSON</b><br>
*<tt>[<br>
* &nbsp;    success: &lt;true/false&gt;,<br>
* &nbsp;    errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
* &nbsp;    errorMessage: &lt;string-value&gt;,<br>
* &nbsp;    sentMessages: [<br>
* &nbsp; &nbsp;         [<br>
* &nbsp; &nbsp; &nbsp;               id: &lt;message id&gt;,<br>
* &nbsp; &nbsp; &nbsp;               threadID: &lt;thread id&gt;,<br>
* &nbsp; &nbsp; &nbsp;               fromNUmber: &lt;string-value&gt;, &nbsp; &nbsp; &nbsp;<i>// capitalization error in sim</i><br>
* &nbsp; &nbsp; &nbsp;               callbackNumber: &lt;string-value&gt;,<br>
* &nbsp; &nbsp; &nbsp;               imageUrl: [  &nbsp; &nbsp; &nbsp;<i> // if there are image urls...</i><br>
* &nbsp; &nbsp; &nbsp; &nbsp;                    {&lt;string-Url-0&gt;},<br>
* &nbsp; &nbsp; &nbsp; &nbsp;                    ...<br>
* &nbsp; &nbsp; &nbsp;               ],<br>
* &nbsp; &nbsp; &nbsp;               recordUrl: &lt;string-value&gt;,<br>
* &nbsp; &nbsp; &nbsp;               text: &lt;string-value&gt;,<br>
* &nbsp; &nbsp; &nbsp;               subjectText: &lt;string-value&gt;,<br>
* &nbsp; &nbsp; &nbsp;               timeSent: &lt;datetime&gt;,<br>
* &nbsp; &nbsp; &nbsp;               deliveredTime: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp;<i>// if exists</i><br>
* &nbsp; &nbsp; &nbsp;               repliedTime: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp;<i>// if exists</i><br>
* &nbsp; &nbsp; &nbsp;               readTime: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp;<i>// if exists</i><br>
* &nbsp; &nbsp; &nbsp;               timeRecieved: &lt;datetime&gt;, &nbsp; &nbsp; &nbsp;<i>// if exists</i><br>
* &nbsp; &nbsp; &nbsp;               subjectIconURL: &lt;string&gt;, &nbsp; &nbsp; &nbsp;<i>// if subject Icon exists</i><br>
* &nbsp; &nbsp; &nbsp;               subjectColor: &lt;string&gt;, &nbsp; &nbsp; &nbsp;<i>// if subject color exists</i><br>
* &nbsp; &nbsp; &nbsp;               fromColor: &lt;string&gt;,  &nbsp; &nbsp; &nbsp;<i>// if fromColor exists</i><br>
* &nbsp; &nbsp; &nbsp;               bodyColor: &lt;string&gt;,  &nbsp; &nbsp; &nbsp;<i>// if bodyColor exists</i><br>
* &nbsp; &nbsp; &nbsp;               fromContactId: &lt;long&gt;,<br>
* &nbsp; &nbsp; &nbsp;               fromSmartPagerId: &lt;string&gt;,<br>
* &nbsp; &nbsp; &nbsp;               status: &lt;string&gt;,<br>
* &nbsp; &nbsp; &nbsp;               recipients: [<br>
* &nbsp; &nbsp; &nbsp; &nbsp;                    { 	<br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	smartPagerID: &lt;string&gt;, <br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	contactId: &lt;long&gt;, <br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	status: &lt;string&gt;, <br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	deliveredTime: &lt;datetime&gt;, <br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	repliedTime: &lt;datetime&gt;, <br>
* &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                    	readTime: &lt;datetime&gt; <br>
* &nbsp; &nbsp; &nbsp; &nbsp;                    },<br>
* &nbsp; &nbsp; &nbsp; &nbsp;                    ...<br>
* &nbsp; &nbsp; &nbsp;               ],<br>
* &nbsp; &nbsp; &nbsp;               responseTemplate: &lt;string&gt;,<br>
* &nbsp; &nbsp; &nbsp;              fromGroupId: &lt;long&gt;,<br>
  &nbsp; &nbsp; &nbsp;              toGroupId: &lt;long&gt;<br>
* &nbsp; &nbsp;          ],<br>
* &nbsp; &nbsp;          ...<br>
* &nbsp;     ]<br>
*]</tt><br>
 * @author Roman
 */
public class GetSentMessagesResponse extends AbstractMessageResponse {

	private static final long serialVersionUID = 6006882770184999893L;

	public GetSentMessagesResponse(String responceContent) {
		super(responceContent);
	}

	public GetSentMessagesResponse(String responceContent, UpdatesStatus status) {
		super(responceContent, status);
	}

	@Override
	public void processResponse(JSONObject jsonObject) {
		JSONArray array = jsonObject.optJSONArray(ResponseParts.sentMessages.name());
		if (array != null) {
			ArrayList<ContentProviderOperation> imagesOperations = new ArrayList<ContentProviderOperation>();
			ArrayList<ContentProviderOperation> recipientOperations = new ArrayList<ContentProviderOperation>();
			ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            ArrayList<ContentProviderOperation> groups = new ArrayList<ContentProviderOperation>();
			Context context = SmartPagerApplication.getInstance().getApplicationContext();
			for (int i = 0; i != array.length(); i++) {
				JSONObject contactJson = array.optJSONObject(i);
                int messageThreadId = contactJson.optInt(MessageTable.threadID.name());
				long maxTimeUpdate = Constants.DEFAULT_DATE.getTime();

				Uri uri = SmartPagerContentProvider.CONTENT_MESSAGE_URI;
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

				for (MessageTable item : MessageTable.values()) {
					switch (item) {
						case _id:

						break;
						case id:
							switch (getStatus()) {
								case added:
									builder.withValue(item.name(), contactJson.opt(item.name()));
								break;
								case modified:
									builder.withSelection(item.name() + " = ? ",
											new String[] { contactJson.optString(item.name()) });
								break;
								default:
								break;
							}
						break;
						case lastUpdate:
						break;
						case messageStatus:
							break;
						case status:
							putMessageStatus(contactJson, builder, item, MessageStatus.Sent.name());
							break;
						case message_order:
							builder.withValue(item.name(), contactJson.opt("order"));
						break;
						case recordUrl:
							String url = contactJson.optString(item.name());
							if (!TextUtils.isEmpty(url)) {
								Intent intent = new Intent(context, FileDownloadService.class);
								intent.setAction(DownloadFileAction.WavLoadAndConvertTo3gpEncode.name());
								intent.putExtra(BundleKey.downloadFile.name(), url);
								context.startService(intent);
								builder.withValue(item.name(), url);
							}
						break;
						case repliedTime:
						case timeSent:
						case deliveredTime:
						case readTime:
							Date currentDate = formatDate(contactJson.optString(item.name()), Constants.DEFAULT_DATE);
							long time = currentDate.getTime();
							if ((time > maxTimeUpdate) && (item != MessageTable.readTime)&&(item != MessageTable.deliveredTime)&&(item != MessageTable.repliedTime)) {
								maxTimeUpdate = time;
							}
							builder.withValue(item.name(), time);
						break;
						case messageType:
							builder.withValue(item.name(), MessageType.sent.ordinal());
						break;
                        case replyAllowed:
                            boolean isAllowed = contactJson.optBoolean(item.name());
                            builder.withValue(item.name(), isAllowed ? 1 : 0);
                            break;
						case requestAcceptanceShow:
							builder.withValue(item.name(), 0);
						break;
						case requestAcceptance:
							if (!contactJson.isNull(item.name())) {
								builder.withValue(item.name(), contactJson.opt(item.name()));
							}
						break;
						default:
							builder.withValue(item.name(), contactJson.opt(item.name()));
						break;
					}
				}
                if (contactJson.has(ResponseParts.groups.name())) {
                    Builder groupsBuilder;
                    JSONArray groupsData = contactJson.optJSONArray(ResponseParts.groups.name());
                    for (int groupDataI = 0; groupDataI != groupsData.length(); groupDataI++) {
                        JSONObject dataJson = groupsData.optJSONObject(groupDataI);
                        String groupID = dataJson.optString(ResponseParts.id.name());
                        groupsBuilder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_THREAD_GROUP_CORRESPONDENCE_URI);
                        groupsBuilder.withValue(DatabaseHelper.ThreadGroupCorrespondence.threadID.name(), messageThreadId);
                        groupsBuilder.withValue(DatabaseHelper.ThreadGroupCorrespondence.groupID.name(), groupID);
                        groups.add(groupsBuilder.build());
                    }
                }
				builder.withValue(MessageTable.lastUpdate.name(), maxTimeUpdate);
				recipientOperations.addAll(parseRecipient(contactJson));
				operations.add(builder.build());
				imagesOperations.addAll(parseImages(contactJson));
			}

			try {
				context.getContentResolver().applyBatch(
						SmartPagerContentProvider.AUTHORITY, operations);
				context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, recipientOperations);
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, groups);
//				context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, imagesOperations);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI, null);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_FULLINFO_URI,
						null);
				context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_THREAD_GROUP_CORRESPONDENCE_URI, null);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}
		}

	}

}
