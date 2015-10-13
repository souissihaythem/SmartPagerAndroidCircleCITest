package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
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
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.model.Message;
import net.smartpager.android.model.SPair;
import net.smartpager.android.notification.Notificator;
import net.smartpager.android.service.FileDownloadService;
import net.smartpager.android.service.FileDownloadService.DownloadFileAction;
import net.smartpager.android.utils.JsonParserUtil;
import net.smartpager.android.web.command.GetInboxCommand;
import net.smartpager.android.web.command.MarkMessageDeliveredCommand;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biz.mobidev.framework.utils.Log;

/**
 * Response on {@linkplain GetInboxCommand}<br>
 * <b>Response JSON</b><br>
 * <tt>[<br>
 * &nbsp;    success: &lt;true/false&gt;,<br>
 * &nbsp;    errorCode: &lt;0=OK, 401=access denied, 404=service not found, 500=internal server error&gt;,<br>
 * &nbsp;   errorMessage: &lt;string-value&gt;,<br>
 * &nbsp;    inboxMessages: [<br>
 * &nbsp; &nbsp;         [<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               id: &lt;message id&gt;,<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               threadID: &lt;thread id&gt;,<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               critical: &lt;boolean&gt;,<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               requestAcceptance: &lt;boolean&gt;,<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               fromNUmber: &lt;string-value&gt;,   &nbsp; &nbsp; &nbsp; &nbsp;<i> // capitalization error in sim</i><br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               callbackNumber: &lt;string-value&gt;,<br>
 * &nbsp; &nbsp; &nbsp; &nbsp;               imageUrl: [ &nbsp; &nbsp; &nbsp; &nbsp;<i>   // if there are image urls...</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                   {&lt;string-Url-0&gt;},<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                   ...<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;              ],<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;              recordUrl: &lt;string-value&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;              text: &lt;string-value&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             subjectText: &lt;string-value&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             timeSent: &lt;datetime&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             deliveredTime: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             repliedTime: &lt;datetime&gt;, &nbsp; &nbsp; &nbsp; &nbsp;<i>// if exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             readTime: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             timeRecieved: &lt;datetime&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             subjectIconURL: &lt;string&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if subject Icon exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             subjectColor: &lt;string&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if subject color exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             fromColor: &lt;string&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if fromColor exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             bodyColor: &lt;string&gt;,  &nbsp; &nbsp; &nbsp; &nbsp;<i> // if bodyColor exists</i><br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             fromContactId: &lt;long&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             fromSmartPagerId: &lt;string&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             status: &lt;string&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             recipients: [<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;               { smartPagerID: &lt;string&gt;, contactId: &lt;long&gt;, status: &lt;string&gt;, deliveredTime: &lt;datetime&gt;, repliedTime: &lt;datetime&gt;, readTime: &lt;datetime&gt; },<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;                   ...<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;              ],<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             responseTemplate: &lt;string&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             fromGroupId: &lt;long&gt;,<br>
 *  &nbsp; &nbsp; &nbsp; &nbsp;             toGroupId: &lt;long&gt;<br>
 *  &nbsp; &nbsp;         ],<br>
 *  &nbsp; &nbsp;        ...<br>
 *  &nbsp;    ]<br>
 * ]</tt><br>
 *
 * @author Roman
 *
 */
public class GetInboxResponse extends AbstractMessageResponse {

    private static final long serialVersionUID = 1522290221610550843L;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    MarkMessageDeliveredCommand mMarkMessageDeliveredCommand;
    // private List<SPair<Integer, String>> mIds;
    private List<SPair<Integer, Message>> mIds;
    
    private boolean mHasNewMessages = false;

    public GetInboxResponse(String responceContent, UpdatesStatus status) {
        super(responceContent, status);
    }

    public GetInboxResponse(String responceContent) {
        super(responceContent);
    }

    public static Date formatDate(String str, Date defaultDate) {
        try {
            if (str.length() == 0) return defaultDate;
            return sdf.parse(str);
        } catch (Exception e) {}
        return defaultDate;
    }

    @Override
    public void processResponse(JSONObject jsonObject) {
        // mIds = new ArrayList<SPair<Integer, String>>();
        mIds = new ArrayList<SPair<Integer, Message>>();
        ArrayList<String> fromContactIdList = new ArrayList<String>();
        Integer messageThreadId = -1;
        String messageId = "";

        JSONArray array = jsonObject.optJSONArray(ResponseParts.inboxMessages.name());
        if (array != null) {
            ArrayList<ContentProviderOperation> imagesOperations = new ArrayList<ContentProviderOperation>();
            ArrayList<ContentProviderOperation> recipientOperations = new ArrayList<ContentProviderOperation>();
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            ArrayList<ContentProviderOperation> groups = new ArrayList<ContentProviderOperation>();
            Context context = SmartPagerApplication.getInstance().getApplicationContext();
            boolean critical = false;
            for (int i = 0; i != array.length(); i++) {
                JSONObject contactJson = array.optJSONObject(i);
                long maxTimeUpdate = Constants.DEFAULT_DATE.getTime();
                
                String mStatus = contactJson.optString("status").toLowerCase();
                mStatus = mStatus.substring(0, 1).toUpperCase() + mStatus.substring(1);
                
                if (MessageStatus.valueOf(mStatus).ordinal() < MessageStatus.Read.ordinal()) {
                	mHasNewMessages = true;
                }

                Uri uri = SmartPagerContentProvider.CONTENT_MESSAGE_URI;
                Builder builder = ContentProviderOperation.newInsert(uri);

                switch (getStatus()) {
                    case added:
                    case none:
                        builder = ContentProviderOperation.newInsert(uri);
                        break;
                    case modified:
                        builder = ContentProviderOperation.newUpdate(uri);
                        break;
                    default:
                        break;
                }

                messageThreadId = contactJson.optInt(MessageTable.threadID.name());
                // mIds.add(new SPair<Integer, String>(messageThreadId, contactJson.optString(MessageTable.id.name())));
                mIds.add(new SPair<Integer, Message>(messageThreadId, new Message(contactJson.optString(MessageTable.id
                        .name()), contactJson.optBoolean(MessageTable.requestAcceptance.name()))));

                // skip processing non-critical messages in 'Pager OFF' mode
                if (!isCurrentStatusOnline() && getStatus() != UpdatesStatus.none) {
                    if (!contactJson.optBoolean(MessageTable.critical.name())) {
                        continue;
                    }
                }
                
                StringBuilder sb = new StringBuilder();
                JSONArray pdfUrls = contactJson.optJSONArray("pdfUrls");
                if (pdfUrls != null) {
                	for (int k = 0; k < pdfUrls.length(); k++) {
						sb.append(pdfUrls.optString(k));
                    	sb.append(",");
                    }
                    sb.setLength(sb.length() - 1);
                    String pdfString = sb.toString();
                    builder.withValue(MessageTable.pdfUrls.name(), pdfString);
                } else {
                	builder.withValue(MessageTable.pdfUrls.name(), "");
                }

                for (MessageTable item : MessageTable.values()) {
                    switch (item) {
                        case _id:

                            break;
                        case id:
                            messageId = contactJson.optString(item.name());
                            switch (getStatus()) {
                                case added:
                                case none:
                                    if (mMarkMessageDeliveredCommand == null) {
                                        mMarkMessageDeliveredCommand = new MarkMessageDeliveredCommand();
                                    }

                                    mMarkMessageDeliveredCommand.addMessageId(messageId);
                                	builder.withValue(item.name(), messageId);
                                    
                                    break;
                                case modified:
                                    builder.withSelection(item.name() + " = ? ", new String[] { messageId });
                                    break;
                                default:
                                    break;
                            }
                            break;
                        case messageStatus:
                            break;
                        case status:
                            putMessageStatus(contactJson, builder, item, MessageStatus.Received.name());
                            break;
                        case lastUpdate:

                            break;
                        case message_order:
                            builder.withValue(item.name(), contactJson.opt(ResponseParts.order.name()));
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
                        case subjectText:
                        	
                        	String subject = null;
                        	if (contactJson.isNull(item.name())) {
                        		subject = "(no subject)";
                        	} else {
                        		subject = item.name();
                        	}
                        	
                        	builder.withValue(item.name(), contactJson.optString(subject, "(no subject)"));
                        	break;
                        case repliedTime:
                        case timeSent:
                        case deliveredTime:
                        case readTime:
                            Date currentDate = formatDate(contactJson.optString(item.name()), Constants.DEFAULT_DATE);
                            long time = currentDate.getTime();
                            if (time > maxTimeUpdate && item != MessageTable.readTime) {
                                maxTimeUpdate = time;
                            }
                            builder.withValue(item.name(), time);

                            break;
                        case messageType:
                            builder.withValue(item.name(), MessageType.inbox.ordinal());
                            break;
                        case fromContactId:
                            fromContactIdList.add(contactJson.optString(item.name()));
                            builder.withValue(item.name(), contactJson.opt(item.name()));
                            break;
                        case requestAcceptance:
                            boolean requested = contactJson.optBoolean(item.name());
                            if (requested) {
                                builder.withValue(item.name(), 1);
                                String status = JsonParserUtil.getCapitalizeString(contactJson
                                        .optString(MessageTable.status.name()));
                                if ((getStatus() == UpdatesStatus.added || getStatus() == UpdatesStatus.none)
                                        && !status.equalsIgnoreCase(MessageStatus.Received.name())
                                        && !status.equalsIgnoreCase(MessageStatus.Accepted.name())) {
                                    builder.withValue(MessageTable.requestAcceptanceShow.name(), 1);
                                }
                            } else {
                                builder.withValue(item.name(), 0);
                            }
                            break;
                        case requestAcceptanceShow:
                            break;
                        case replyAllowed:
                            boolean isAllowed = contactJson.optBoolean(item.name());
                            builder.withValue(item.name(), isAllowed ? 1 : 0);
                            break;
                        case critical:
                            critical = contactJson.optBoolean(item.name());
                            builder.withValue(item.name(), critical ? 1 : 0);
                            break;
                        case responseTemplate:
                            String respTempl = contactJson.optString(item.name());
                            builder.withValue(item.name(), respTempl);
                            break;
//                        case fromGroupId:
//                            JSONArray groupData = contactJson.optJSONArray(ResponseParts.groups.name());
//                            for (int groupDataI = 0; groupDataI != groupData.length(); groupDataI++) {
//                                JSONObject dataJson = groupData.optJSONObject(groupDataI);
//                                String groupID = dataJson.optString(ResponseParts.id.name());
//                                builder.withValue(item.name(), groupID);
//                            }
//                            break;
                        case pdfUrls:
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
                
                // TODO for accepted feature - 28-05-2013
                // now we are reading status not for whole message - only for current user
                String status = null;
                Long lastUpdate = null;
                JSONArray recipients = contactJson.optJSONArray(ResponseParts.recipients.name());
                for (int j = 0; j != recipients.length(); j++) {
                    JSONObject recipientJson = recipients.optJSONObject(j);
                    String recipientID = recipientJson.optString(ResponseParts.smartPagerID.name());
                    if (SmartPagerApplication.getInstance().getPreferences().getSmartPagerID().equalsIgnoreCase(recipientID)) {
                        status = recipientJson.optString(MessageTable.status.name());
                        lastUpdate = recipientJson.optLong(MessageTable.readTime.name());
                    }
                }
                if ( status == null || status.equals("") ) {
                    status = MessageStatus.Received.name();
                }
                else if ( status.equalsIgnoreCase(MessageStatus.Failed.name()) || status.equalsIgnoreCase(MessageStatus.Delayed.name()) ) {
                    status = MessageStatus.Sent.name();
                }
                else {
                    status = JsonParserUtil.getCapitalizeString(status);
                }
                if ( lastUpdate == null ) {
                    lastUpdate = System.currentTimeMillis();
                }
                builder.withValue(MessageTable.status.name(), status);
                // builder.withValue(MessageTable.lastUpdate.name(), lastUpdate);
                // -----------------------------------------------
                builder.withValue(MessageTable.lastUpdate.name(), maxTimeUpdate);
                recipientOperations.addAll(parseRecipient(contactJson));
                imagesOperations.addAll(parseImages(contactJson));
                operations.add(builder.build());
            }

            try {
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, recipientOperations);
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, imagesOperations);
                context.getContentResolver().applyBatch(SmartPagerContentProvider.AUTHORITY, groups);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_FULLINFO_URI,
                        null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, null);
                context.getContentResolver().notifyChange(SmartPagerContentProvider.CONTENT_THREAD_GROUP_CORRESPONDENCE_URI, null);
                if (mMarkMessageDeliveredCommand != null) {
                    this.addPostComand(mMarkMessageDeliveredCommand);
                }
                if (array.length() > 0) {
                    notifyNewInbox(fromContactIdList, critical, mIds);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (OperationApplicationException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyNewInbox(ArrayList<String> fromContactIdList, boolean critical,
                                List<SPair<Integer, Message>> mIds) {
    	
    	int threadID = mIds.get(0).first;
    	int messageID = Integer.parseInt(mIds.get(0).second.getId());
    	String alertType;
    	
    	if (threadID != messageID) {
    		alertType = "casual";
    	} else {
    		if (critical) {
    			alertType = "critical";
    		} else {
    			alertType = "normal";
    		}
    	}

        boolean needToNotify = isCurrentStatusOnline() || critical;
        if (needToNotify && this.getStatus() == UpdatesStatus.added) {
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(BundleKey.fromContactIdList.name(), fromContactIdList);
            bundle.putBoolean(BundleKey.optUrgent.name(), critical);
            bundle.putInt(MessageTable.threadID.name(), threadID);
            bundle.putString(MessageTable.id.name(), String.valueOf(messageID));
            bundle.putString(BundleKey.alertType.name(), alertType);
            // Log.e("====== SEND CRITICAL 1 =======", critical);
            // Notificator.notifyInboxRepeat(bundle, REPEAT_ALARM_INTERVAL_MINUTES);            
            if (mHasNewMessages) {
            	Notificator.notifyInbox(bundle);
            }
        }
    }

    private boolean isCurrentStatusOnline() {
        return SmartPagerApplication.getInstance().getPreferences().getStatus().equals(ContactStatus.ONLINE.name());
    }

    private void addMarkMessageDeliveredCommand(String messageId) {
        MarkMessageDeliveredCommand command = new MarkMessageDeliveredCommand();
        command.setMessageId(messageId);
        this.addPostComand(command);
    }

    // public List<SPair<Integer, String>> getIds() {
    // return mIds;
    // }

    public List<SPair<Integer, Message>> getIds() {
        return mIds;
    }

}
