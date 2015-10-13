package net.smartpager.android.web.response;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.text.TextUtils;

import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.ResponseParts;
import net.smartpager.android.consts.UpdatesStatus;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.DatabaseHelper.ImageUrlTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.DatabaseHelper.RecipientTable;
import net.smartpager.android.utils.JsonParserUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Abstract representation of server response for messages responses. 
 * Includes methods for parsing images, recipients and  message status 
 * @author Roman
 * @see GetInboxResponse
 * @see GetSentMessagesResponse
 */
public abstract class AbstractMessageResponse extends AbstractResponse {

	private static final long serialVersionUID = 6497495211465302592L;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	public static final String INDEX_TEXT = "&index=";

	public static Date formatDate(String str, Date defaultDate) {
		try {
			if (TextUtils.isEmpty(str)) {
				return defaultDate;
			}
			return sdf.parse(str);
		} catch (Exception e) {}
		return defaultDate;
	}

	public AbstractMessageResponse(String responceContent) {
		super(responceContent);
	}

	public AbstractMessageResponse(String responceContent, UpdatesStatus status) {
		super(responceContent, status);
	}

	/**
	 * Prepares message images URLs for inserting to database.   
	 * @param contactJson
	 * @return
	 */
	protected ArrayList<ContentProviderOperation> parseImages(JSONObject contactJson) {
		ArrayList<ContentProviderOperation> imageOperations = new ArrayList<ContentProviderOperation>();

		JSONArray array = contactJson.optJSONArray(ResponseParts.imageUrl.name());
		if (array != null) {
			for (int i = 0; i != array.length(); i++) {
				Builder builder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_IMAGE_URL_URI);
				for (ImageUrlTable item : ImageUrlTable.values()) {
					switch (item) {
						case _id:
						break;
						case messageId:
							builder.withValue(item.name(), contactJson.opt(MessageTable.id.name()));
						break;
						case imageIndex:
							String url = array.optString(i);
                            if(url.contains(INDEX_TEXT))
                            {
                                int indexStart = url.indexOf(INDEX_TEXT) + INDEX_TEXT.length();
                                int indexEnd = url.indexOf((int) '&', indexStart);
                                String index = url.substring(indexStart, indexEnd);
                                builder.withValue(item.name(), index);
                            }
						break;

						default:
							builder.withValue(item.name(), array.opt(i));
						break;
					}
				}
				imageOperations.add(builder.build());
			}
		}
		return imageOperations;

	}

	protected void putMessageStatus(JSONObject contactJson, Builder builder, MessageTable item, String defaultValue) {
		// Status should be any of:
		// Pending, Read, Replied, Delivered, Received, Sent, Accepted, Rejected, Failed, Delayed, Called
		String status = JsonParserUtil.getCapitalizeString(contactJson.optString(item.name()));
		if ( status != null ) {
			switch (MessageStatus.valueOf(status)) {
				case Read:
				case Replied:
				case Delivered:
				case Received:
				case Sent:
				case Accepted:
				case Rejected:
				case Called:
					builder.withValue(MessageTable.messageStatus.name(), status);
					break;
				default:
					// Pending, Failed, Delayed
					builder.withValue(MessageTable.messageStatus.name(), defaultValue);
				break;
			}
		}
		
		if ( status == null ) {
			status = defaultValue;
		}
		
		builder.withValue(item.name(), status);
	}
	
	protected ArrayList<ContentProviderOperation> parseRecipient(JSONObject contactJson) {
		ArrayList<ContentProviderOperation> recipientOperations = new ArrayList<ContentProviderOperation>();

		JSONArray array = contactJson.optJSONArray(ResponseParts.recipients.name());
		if (array != null) {
			for (int i = 0; i != array.length(); i++) {
				long maxTimeUpdate = Constants.DEFAULT_DATE.getTime();
				Builder builder = ContentProviderOperation.newInsert(SmartPagerContentProvider.CONTENT_RECIPIENTS_URI);
				JSONObject recipientJson = array.optJSONObject(i);
				boolean isPennding = false;
				for (RecipientTable item : RecipientTable.values()) {
					switch (item) {
						case _id:
						break;
						case messageId:
							builder.withValue(item.name(), contactJson.opt(MessageTable.id.name()));
						break;
						case lastUpdate:
                            Date updateDate = formatDate(contactJson.optString("timeSent"), Constants.DEFAULT_DATE);
                            long updateTime = updateDate.getTime();
                            if (updateTime > maxTimeUpdate) {
                                maxTimeUpdate = updateTime;
                            }
                            builder.withValue(item.name(), updateTime);
						break;
						case status:
							String status = JsonParserUtil.getCapitalizeString(recipientJson.optString(item.name()));
							if(status.equalsIgnoreCase(MessageStatus.Pending.name())){
								isPennding = true;
							}
                            if(status.equalsIgnoreCase(MessageStatus.Failed.name()) || status.equalsIgnoreCase(MessageStatus.Delayed.name()))
                                status = MessageStatus.Sent.name();
    						builder.withValue(item.name(), status);
							break;
						case repliedTime:
						case deliveredTime:
						case readTime:
							Date currentDate = formatDate(contactJson.optString(item.name()), Constants.DEFAULT_DATE);
							long time = currentDate.getTime();
							if (time > maxTimeUpdate) {
								maxTimeUpdate = time;
							}
							builder.withValue(item.name(), time);
						break;
						default:
							builder.withValue(item.name(), recipientJson.opt(item.name()));
						break;
					}
				}
				if(isPennding){
					maxTimeUpdate = Calendar.getInstance().getTimeInMillis();
				}
				builder.withValue(MessageTable.lastUpdate.name(), maxTimeUpdate);
				recipientOperations.add(builder.build());
			}
		}
		return recipientOperations;
	}

}
