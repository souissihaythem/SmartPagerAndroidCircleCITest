package net.smartpager.android.web.command;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.PushAction;
import net.smartpager.android.consts.WebAction;
import net.smartpager.android.consts.WebSendMessageParams;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ChatsTable;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.database.DatabaseHelper.ImageUrlTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.DatabaseHelper.RecipientTable;
import net.smartpager.android.model.SPair;
import net.smartpager.android.web.response.AbstractMessageResponse;
import net.smartpager.android.web.response.AbstractResponse;
import net.smartpager.android.web.response.SendMessageResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import biz.mobidev.framework.utils.Log;

/**
 * 
 * The send message function on the mobile device will compile the message contents and the user credentials and send
 * them to the REST API. This is done via POST request to the �/rest/sendMessage� URL.<br>
 * Below is the JSON schema used to transmit this data:<br>
 * <b>Request JSON</b><br>
 * <tt>[<br>
 * &nbsp;      uberPassword: &lt;SHA-256&gt;,<br>
 * &nbsp;      smartPagerID: &lt;username&gt;,<br>
 * &nbsp;      message: [<br>
 * &nbsp;           text: &lt;message text&gt;,<br>
 * &nbsp;           recipients: [<br>
 * &nbsp; &nbsp;                { <br>
 * &nbsp; &nbsp;                	smartPagerID: &lt;string(optional)&gt;,<br> 
 * &nbsp; &nbsp;                	contactId: &lt;long(optional)&gt;, <br>
 * &nbsp; &nbsp;                	groupId: &lt;long(optional)&gt; }&lt;object(one and only one of smartPagerID, contactId or groupId must be set)&gt;,<br>
 * &nbsp; &nbsp;                ...<br>
 * &nbsp;           ]&lt;array(must contain at least one row)&gt;,<br>
 * &nbsp;           imageUrl: [&lt;string(url)&gt;, ...] &lt;array(optional)&gt;,<br>
 * &nbsp;           subjectText: &lt;string(subject)&gt;<br>
 * &nbsp;           threadID: &lt;long&gt;,<br>
 * &nbsp;           critical: &lt;boolean&gt;, &nbsp; &nbsp; <i>#in order to send critical messages the sender user must have the send critical message permission</i><br>
 * &nbsp;           requestAcceptance: &lt;boolean&gt;,<br>
 * &nbsp;           callbackNumber: &lt;string(optional,11 digits)&gt;,<br>
 * &nbsp;           recordUrl: &lt;string(optional,URL)&gt;,<br>
 * &nbsp;           notificationEmail: &lt;string(optional,email)&gt;,<br>
 * &nbsp;           notificationSMS: &lt;string(optional,11 digits)&gt;<br>
 * &nbsp;      ]<br>
 * ]</tt><br>
 * <tt><u>Request Remarks</u>
 * <ol><li>(As of this writing: 4 October, 2012): The uberPassword hash is now SHA-256 for all calls to the REST API</li>
 * <li>The smartPagerID is the username of the user. This is what is used when they login. In general, since REST does not maintain session, the username and uberPassword will accompany each request sent by the mobile software.</li>
 * <li>Required Fields: smartPagerID, uberPassword, if messages: message.recipients</li>
 * <li>'recipients' is a List of JSONObjects. Each object will contain a smartPagerID and/or a contactId</li>
 * <li>'imageUrl' is a List of Strings. Each String is a URL to an image</li></ol>
 * 
 * @author Roman
 * @see SendMessageResponse
 */
public class SendMessageCommand extends AbstractUserCommand {

	private static final long serialVersionUID = 9124191390780537831L;
	MarkMessageRepliedCommand mMarkMessageRepliedCommand;
	private String mText;
	private String mSubjectText;
	private String mToContactId;
	private List<String> mToSmartPagerId;
	private List<String> mContactsIds;
	private List<String> mToGroupId;
	private int mThreadID;
	private String mOrder;
	private String mMessageStatusID;
	private String mFromNumber;
	private String mCallBackNumber;
	private List<String> mImageUrl;
    private List<String> mImageUrlToSend;
	private String mRecordUrlToSend;
    private String mRecordUrl;
	private String mArchived;
	private String mResponseTemplate;
	private Boolean mIsFastAdd = false;
	private Boolean mCritical;
	private Boolean mRequestAcceptance;

	@Override
	public HttpEntity getSubEntity(JSONObject root) throws Exception {
		JSONObject message = new JSONObject();
		message.put(WebSendMessageParams.text.name(), mText);
		message.put(WebSendMessageParams.subjectText.name(), mSubjectText);
		message.put(WebSendMessageParams.toContactId.name(), mToContactId);
		if (mToSmartPagerId != null || mToGroupId != null) {
			JSONArray rec = new JSONArray();
			for (String i : mToSmartPagerId) {
				JSONObject o = new JSONObject();
				o.put(WebSendMessageParams.contactId.name(), i);
				rec.put(o);
			}
			if (mToGroupId != null && !mToGroupId.isEmpty()) {
				for (String i : mToGroupId) {
					JSONObject o = new JSONObject();
					o.put(WebSendMessageParams.groupId.name(), i);
					rec.put(o);
				}
			}
			message.put(WebSendMessageParams.recipients.name(), rec);
		}
		if (mThreadID != 0) {
			message.put(WebSendMessageParams.threadID.name(), mThreadID);
		}
		if (mOrder != null) {
			message.put(WebSendMessageParams.order.name(), mOrder);
		}
		if (mMessageStatusID != null) {
			message.put(WebSendMessageParams.messageStatusID.name(), mMessageStatusID);
		}
		if (mFromNumber != null) {
			message.put(WebSendMessageParams.fromNumber.name(), mFromNumber);
		}
		if (mCallBackNumber != null) {
			message.put(WebSendMessageParams.callbackNumber.name(), mCallBackNumber);
		} else {
			message.put(WebSendMessageParams.callbackNumber.name(), "");
		}

//		if (mImageUrl == null) {
        mImageUrlToSend = new ArrayList<String>();
//		}

        mImageUrlToSend.addAll(parsePutFileResults(mPreCommandResultList));

		if (mImageUrlToSend.size() != 0) {
			JSONArray ar = new JSONArray();
			for (String i : mImageUrlToSend) {
				ar.put(i);
			}
			message.put(WebSendMessageParams.imageUrl.name(), ar);
		}
		if (mRecordUrlToSend != null) {
			message.put(WebSendMessageParams.recordUrl.name(), mRecordUrlToSend);
		}
		if (mArchived != null) {
			message.put(WebSendMessageParams.archived.name(), mArchived);
		}
		if (mCritical != null) {
			message.put(WebSendMessageParams.critical.name(), mCritical);
		}
		if (mRequestAcceptance != null) {
			message.put(WebSendMessageParams.requestAcceptance.name(), mRequestAcceptance);
		}
		if (!TextUtils.isEmpty(mResponseTemplate)) {
			message.put(WebSendMessageParams.responseTemplate.name(), mResponseTemplate);
		}

		root.put(WebSendMessageParams.message.name(), message);
		return new StringEntity(root.toString());
	}

	@Override
	public AbstractResponse getResponse(String string) {
		AbstractResponse response = new SendMessageResponse(string);
		if (response.isSuccess()) {
			try {
				JSONObject jsonObject;

				jsonObject = response.getJsonObject();

				ArrayList<Integer> ids = new ArrayList<Integer>();
				ids.add(jsonObject.optInt("messageId"));
                if(!mIsFastAdd)
                {
                    GetSentMessagesCommand command = new GetSentMessagesCommand();
                    command.setIds(ids);
    				response.addPostComand(command);
                }
                ContentResolver resolver = SmartPagerApplication.getInstance().getContentResolver();
                int threadID = jsonObject.optInt("threadID");
//				if (mIsFastAdd) {
                ContentValues values = new ContentValues();
                values.put(MessageTable.id.name(), jsonObject.optInt("messageId"));
                values.put(MessageTable.threadID.name(), threadID);
                values.put(MessageTable.text.name(), mText);
                values.put(MessageTable.subjectText.name(), mSubjectText);
                values.put(MessageTable.fromNumber.name(), mFromNumber);
                List<AbstractCommand> preCommantList = getPreCommandList();
                //Here we need to get the audio local URL(if any) from the PutFileCommand and use it for the local messages presentation
                if(preCommantList != null && preCommantList.size() > 0)
                {
                    AbstractCommand abstractCommand = null;
                    for(int i = 0; i < preCommantList.size(); i++)
                    {
                        abstractCommand = preCommantList.get(i);
                        if (abstractCommand instanceof PutFileCommand) {
                            try {
                                if (((PutFileCommand)abstractCommand).getMimeType().equals(PutFileCommand.AUDIO_MPEG))
                                    mRecordUrl = ((PutFileCommand) abstractCommand).getFileName();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                values.put(MessageTable.recordUrl.name(), (mRecordUrl != null && !"".equals(mRecordUrl)) ? mRecordUrl : mRecordUrlToSend);
                values.put(MessageTable.critical.name(), mCritical);
                values.put(MessageTable.fromSmartPagerId.name(), SmartPagerApplication.getInstance().getPreferences().getSmartPagerID());
                values.put(MessageTable.requestAcceptance.name(), mRequestAcceptance);
                values.put(MessageTable.status.name(), MessageStatus.Sent.name());
                values.put(MessageTable.messageStatus.name(), MessageStatus.Replied.name());
                values.put(MessageTable.lastUpdate.name(), Calendar.getInstance().getTimeInMillis());
                values.put(MessageTable.timeSent.name(), Calendar.getInstance().getTimeInMillis());
                resolver.insert(SmartPagerContentProvider.CONTENT_MESSAGE_URI, values);

                ArrayList<ContentProviderOperation> imageOperations = parseImages(parseURLList(mImageUrl),
                        jsonObject.optInt("messageId"));
                if (!imageOperations.isEmpty()) {
                    resolver.applyBatch(SmartPagerContentProvider.AUTHORITY, imageOperations);
                }

                if (mContactsIds != null) {
                    ArrayList<ContentProviderOperation> recipientsOperations = parseRecipients(jsonObject);
                    if (!recipientsOperations.isEmpty()) {
                        resolver.applyBatch(SmartPagerContentProvider.AUTHORITY, recipientsOperations);
                    }
                }
                Builder groupsBuilder;
                if (mToGroupId != null && mToGroupId.size() > 0) {
                    ArrayList<ContentProviderOperation> groups = new ArrayList<ContentProviderOperation>();
                    for (String groupID : mToGroupId) {
                        groupsBuilder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_THREAD_GROUP_CORRESPONDENCE_URI);
                        groupsBuilder.withValue(DatabaseHelper.ThreadGroupCorrespondence.threadID.name(), threadID);
                        groupsBuilder.withValue(DatabaseHelper.ThreadGroupCorrespondence.groupID.name(), groupID);
                        groups.add(groupsBuilder.build());
                    }
                    if (!groups.isEmpty()) {
                        resolver.applyBatch(SmartPagerContentProvider.AUTHORITY, groups);
                        resolver.notifyChange(SmartPagerContentProvider.CONTENT_THREAD_GROUP_CORRESPONDENCE_URI, null);
                    }
                }

                resolver.notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_URI, null);
                resolver.notifyChange(SmartPagerContentProvider.CONTENT_CHATS_URI, null);
                resolver.notifyChange(SmartPagerContentProvider.CONTENT_MESSAGE_THREADS_URI, null);
                resolver.notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI, null);
                resolver.notifyChange(SmartPagerContentProvider.CONTENT_RECIPIENTS_FULLINFO_URI, null);
                resolver.notifyChange(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI, null);
//				}

			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
//			MarkMessageReplied(response);
		}
		return response;
	}

	private ArrayList<ContentProviderOperation> parseRecipients(JSONObject jsonObject) {
		ArrayList<ContentProviderOperation> recipientsOperations = new ArrayList<ContentProviderOperation>();
		
		if (mContactsIds != null && !mContactsIds.isEmpty()) {
			for (int i = 0; i < mContactsIds.size(); i++) {
				Builder builder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI);
				for (RecipientTable item : RecipientTable.values()) {
					switch (item) {
						case messageId:
							builder.withValue(item.name(), jsonObject.optInt("messageId"));
						break;
						case contactId:
							builder.withValue(item.name(), mContactsIds.get(i));
							break;
						case status:
							builder.withValue(item.name(), MessageStatus.Pending.name());
							break;
						case smartPagerID:
							builder.withValue(item.name(), mToSmartPagerId.get(i));
							break;
						case lastUpdate:
							builder.withValue(MessageTable.lastUpdate.name(), Calendar.getInstance().getTimeInMillis());
							break;
						default:
							break;
					}
				}
				recipientsOperations.add(builder.build());
			}
		}
		return recipientsOperations;
	}

	protected ArrayList<ContentProviderOperation> parseImages(List<String> imageUrls, int messageId) {
		ArrayList<ContentProviderOperation> imageOperations = new ArrayList<ContentProviderOperation>();
		if (imageUrls != null && !imageUrls.isEmpty()) {
			for (String url : imageUrls) {
				Builder builder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI);
				for (ImageUrlTable item : ImageUrlTable.values()) {
					switch (item) {
						case _id:
						break;
						case messageId:
							builder.withValue(item.name(), messageId);
						break;
						case imageIndex:
                            if(url.contains(AbstractMessageResponse.INDEX_TEXT))
                            {
                                int indexStart = url.indexOf(AbstractMessageResponse.INDEX_TEXT)
                                        + AbstractMessageResponse.INDEX_TEXT.length();
                                int indexEnd = url.indexOf((int) '&', indexStart);
                                String index = url.substring(indexStart, indexEnd);
                                builder.withValue(item.name(), index);
                            }
						break;

						default:
							builder.withValue(item.name(), url);
						break;
					}
				}
				imageOperations.add(builder.build());
			}
		}
		return imageOperations;

	}

	private void MarkMessageReplied(AbstractResponse response) {
		if (mMarkMessageRepliedCommand != null) {
			response.addPostComand(mMarkMessageRepliedCommand);
		}
	}

	@Override
	public WebAction getWebAction() {
		return WebAction.sendMessage;
	}

	@Override
	public void setArguments(Bundle bundle) {
		
		SPair<ArrayList<String>, ArrayList<String>> recipIds = 
				new SPair<ArrayList<String>, ArrayList<String>>(
						new ArrayList<String>(), 
						new ArrayList<String>()
				);
		
		if (bundle != null) {
			mText = bundle.getString(WebSendMessageParams.text.name());
			mSubjectText = bundle.getString(WebSendMessageParams.subjectText.name());
			mToContactId = bundle.getString(WebSendMessageParams.toContactId.name());
//			mToSmartPagerId = bundle.getStringArrayList(WebSendMessageParams.recipients.name());
			mToGroupId = bundle.getStringArrayList(WebSendMessageParams.recipientsGroup.name());
			mThreadID = bundle.getInt(WebSendMessageParams.threadID.name());
			mOrder = bundle.getString(WebSendMessageParams.order.name());
			mMessageStatusID = bundle.getString(WebSendMessageParams.messageStatusID.name());
			mFromNumber = bundle.getString(WebSendMessageParams.fromNumber.name());
			mCallBackNumber = bundle.getString(WebSendMessageParams.callbackNumber.name());
			mImageUrl = bundle.getStringArrayList(WebSendMessageParams.imagesUriToSend.name());
			mRecordUrlToSend = bundle.getString(WebSendMessageParams.recordUriToSend.name());
//            mRecordUrl = bundle.getString(WebSendMessageParams.recordUriToSend.name());
			mArchived = bundle.getString(WebSendMessageParams.archived.name());
			mCritical = bundle.getBoolean(WebSendMessageParams.critical.name());
			mRequestAcceptance = bundle.getBoolean(WebSendMessageParams.requestAcceptance.name());
			mResponseTemplate = bundle.getString(WebSendMessageParams.responseTemplate.name());
			mIsFastAdd = bundle.getBoolean(WebSendMessageParams.isFastAdd.name(), false);
			
			if (mIsFastAdd) {
				recipIds = (SPair<ArrayList<String>, ArrayList<String>>) bundle.getSerializable(WebSendMessageParams.recipients.name());
				mToSmartPagerId = recipIds.second;
				mContactsIds = recipIds.second;
			} else {
				mToSmartPagerId = bundle.getStringArrayList(WebSendMessageParams.recipients.name());
			}
			
			String messageToMarkReplyId = bundle.getString(WebSendMessageParams.id.name());
			if (!TextUtils.isEmpty(messageToMarkReplyId)) {
				mMarkMessageRepliedCommand = new MarkMessageRepliedCommand();
				mMarkMessageRepliedCommand.setInitiatedByUser(false);
				mMarkMessageRepliedCommand.setMessageId(messageToMarkReplyId);
			}
			ArrayList<String> imagesUriToSend = bundle.getStringArrayList(WebSendMessageParams.imagesUriToSend.name());
			if (imagesUriToSend != null) {
				for (String uri : imagesUriToSend) {
					PutFileCommand comand = new PutFileCommand();
					comand.setFileName(uri);
					comand.setMimeType(PutFileCommand.IMAGE_JPEG);
					this.addPreCommand(comand);
				}
			}
			String audioUriToSend = bundle.getString(WebSendMessageParams.recordUriToSend.name());
			if (!TextUtils.isEmpty(audioUriToSend)) {
				PutFileCommand comand = new PutFileCommand();
				comand.setFileName(audioUriToSend);
				comand.setMimeType(PutFileCommand.AUDIO_MPEG);
				this.addPreCommand(comand);
			}
		}
	}

	/**
	 * Parses {@linkplain PutFileCommand} pre-commands results to obtain images and audio records URLs
	 * 
	 * @return
	 */
	private ArrayList<String> parsePutFileResults(List<String> jsonObjectsList) {
		ArrayList<String> imagesUriList = new ArrayList<String>();
		for (String result : jsonObjectsList) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(result);
                boolean hasKey = jsonObject.has("url");
                if(!hasKey)
                    continue;
				String url = jsonObject.optString("url");
				boolean isAudio = false;
				if (mRecordUrlToSend != null) {
					isAudio = isAudioFile(mRecordUrlToSend);
				}
				if (isAudio) {
					mRecordUrlToSend = url;
				} else {
                    if(!"".equals(url))
    					imagesUriList.add(url);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return imagesUriList;
	}

    private boolean isAudioFile (String url)
    {
        return (url.indexOf(".3gp") != -1) || (url.indexOf(".wav") != -1)
                || (url.indexOf(".mp3") != -1);
    }

    private ArrayList<String> parseURLList(List<String> itemsList) {
        ArrayList<String> imagesUriList = new ArrayList<String>();
        Uri itemURI = null;
        for (String currURL : itemsList) {
            itemURI = Uri.parse(currURL);
            String url = currURL;//itemURI.getLastPathSegment();
            boolean isAudio = isAudioFile(url);
            if (isAudio) {
                mRecordUrlToSend = url;
            } else {
                if (!"".equals(url))
                    imagesUriList.add(url);
            }
        }
        return imagesUriList;
    }

}
