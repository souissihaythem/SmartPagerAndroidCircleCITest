package net.smartpager.android.holder;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.consts.QueryMessageThreadTable;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ChatsTable.MessageType;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.DatabaseHelper.RecipientTable;
import net.smartpager.android.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;

public class MessagesListItemHolder implements IHolderCursor {
	
	private int contactOffset = MessageTable.values().length;

	private Context mContext;
	private TextView mNameTextView;
	private TextView mSubjectTextView;
	private TextView mBodyTextView;
	private TextView mStatusTextView;
	private TextView mActionAgeTextView;
	private TextView mActionAgeCategoryTextView;
	private TextView mTimeTextView;
	private ImageView mImageImageView;
	private ImageView mRecordImageView;
	private ImageView mRepliedImageView;
    private ImageView mGroupIconImageView;
	private LinearLayout mReadStatusView;

	@Override
	public ViewGroup inflateLayout(LayoutInflater inflater, Context context) {
		ViewGroup view = new RelativeLayout(context);
		mContext = context;
		inflater.inflate(R.layout.item_message_list, view, true);
		return view;
	}

	@Override
	public IHolderCursor createHolder(View view) {
		
		mNameTextView = (TextView) view.findViewById(R.id.item_message_contact_name_textView);
		mSubjectTextView = (TextView) view.findViewById(R.id.item_massage_them_textView);
		mBodyTextView = (TextView) view.findViewById(R.id.item_message_body_textView);
		mStatusTextView = (TextView) view.findViewById(R.id.item_message_action_textView1);
		mActionAgeTextView = (TextView) view.findViewById(R.id.item_message_action_time_textView);
		mActionAgeCategoryTextView = (TextView) view.findViewById(R.id.item_message_interval_textView);
		mTimeTextView = (TextView) view.findViewById(R.id.item_message_time_textView);
		mImageImageView = (ImageView) view.findViewById(R.id.item_message_attach_image_imageView);
		mRecordImageView = (ImageView) view.findViewById(R.id.item_message_attach_voice_imageView);
		mRepliedImageView = (ImageView) view.findViewById(R.id.item_message_status_imageView);

		mReadStatusView = (LinearLayout) view.findViewById(R.id.lin_layout_msg_status);
		mGroupIconImageView = (ImageView) view.findViewById(R.id.item_message_group_icon_imageView);
		return this;
	}

	@Override
	public void preSetData(Bundle params) {

	}

	@Override
	public void setData(Cursor cursor) {
		String status = new String();
		Date date = new Date();
		Date statusDate = new Date();
		
		// If message was sent from device
		if (cursor.getInt(MessageTable.messageType.ordinal()) == MessageType.sent.ordinal()) {
			status = MessageStatus.Sent.toString();
			date.setTime(cursor.getLong(MessageTable.timeSent.ordinal()));
			statusDate = date;
		}
		
		// If message was received by device
		if (cursor.getInt(MessageTable.messageType.ordinal()) == MessageType.inbox.ordinal()) {
			
			// If the readTime == 0, assert the message has been received but not read
			if ((cursor.getLong(MessageTable.readTime.ordinal()) == Constants.DEFAULT_DATE.getTime())) {
				status = MessageStatus.Received.toString();
				// Since this function loops through all the messages to create the notion of a thread,
				// there is a delay that occurs before setting the deliveredTime > 0.  To combat this,
				// we use the value of date as it currently stands, and only set the time when deliveredTime 
				// has been set.
				if ((cursor.getLong(MessageTable.deliveredTime.ordinal()) > Constants.DEFAULT_DATE.getTime())) {
					date.setTime(cursor.getLong(MessageTable.deliveredTime.ordinal()));
					statusDate = date;
				}
				
			} 
			// If the readTime != 0, assert the message has been read
			else {
				status = MessageStatus.Read.toString();
				date.setTime(cursor.getLong(MessageTable.deliveredTime.ordinal()));
				statusDate.setTime(cursor.getLong(MessageTable.readTime.ordinal()));
			}
		}
		
		List<String> unreadStatuses = new ArrayList<String>();
		for (MessageStatus mMessageStatus : MessageStatus.values()) {
			if (mMessageStatus.ordinal() < MessageStatus.Read.ordinal()) {
				unreadStatuses.add(mMessageStatus.toString());
			}
		}
		
		int color = 0;		
		if (unreadStatuses.contains(cursor.getString(MessageTable.messageStatus.ordinal()))
				&& (cursor.getInt(MessageTable.messageType.ordinal()) == MessageType.inbox.ordinal())) {
			
			int critical = cursor.getInt(cursor.getColumnIndex(MessageTable.critical.name()));
			int repliedResource = (critical == 0)
					? R.drawable.messages_ic_red_code_static
					: R.drawable.ic_exclamation;
			mRepliedImageView.setImageResource(repliedResource);
			
			mRepliedImageView.setVisibility(View.VISIBLE);
			color = mContext.getResources().getColor(R.color.red_msg_read_status);
			mReadStatusView.setBackgroundResource(R.drawable.layerlist_msg_right_bck_unread);
		} else {
			color = mContext.getResources().getColor(R.color.primary_text_default_material_dark);
			mRepliedImageView.setVisibility(View.INVISIBLE);
			mReadStatusView.setBackgroundResource(R.drawable.shape_message_item_time_background);
		}
		if (cursor.getInt(MessageTable.messageType.ordinal()) == MessageType.sent.ordinal()) {
			mRepliedImageView.setImageResource(R.drawable.messages_ic_arrow_two_static);
			mRepliedImageView.setVisibility(View.VISIBLE);
		}
		if (SmartPagerApplication.getInstance().isThreadMuted(mContext, cursor.getInt(MessageTable.threadID.ordinal()))) {
			mRepliedImageView.setImageResource(R.drawable.messages_icon_muted);
			mRepliedImageView.setVisibility(View.VISIBLE);
		}

		mStatusTextView.setTextColor(color);
		mActionAgeTextView.setTextColor(color);
		mActionAgeCategoryTextView.setTextColor(color);

		setActionAge(statusDate.getTime(), mActionAgeTextView, mActionAgeCategoryTextView);

		mTimeTextView.setText(DateTimeUtils.format(date.getTime()));

		mStatusTextView.setText(status);

		int columnImagesCount = MessageTable.values().length + ContactTable.values().length + 1;		
		if (cursor.getInt(columnImagesCount) > 0) {
			mImageImageView.setVisibility(View.VISIBLE);
		} else {
			mImageImageView.setVisibility(View.GONE);
		}

        String groupName = cursor.getString(cursor.getColumnIndex(QueryMessageThreadTable.groupName.name()));
		int groupOffset = 2;
		String displayName = "";
		
		if ( groupName != null ) {
			//Log.d("MessagesListItemHolder", "Cursor has for group: " + groupName + " has: " + DatabaseUtils.dumpCursorToString(cursor));
		}
		
		if ( cursor.getInt(MessageTable.messageType.ordinal()) == MessageType.inbox.ordinal() ) {
			String firstName="";
			String lastName="";
			
			if ( groupName != null ) {
				firstName = cursor.getString(cursor.getColumnCount()-2);
				lastName = cursor.getString(cursor.getColumnCount()-1);
				
				displayName = firstName + " " + lastName;
			}
			else {
				if ( !cursor.isNull(contactOffset + ContactTable.firstName.ordinal()) ) {
					firstName = cursor.getString(contactOffset + ContactTable.firstName.ordinal());
				}
				if ( !cursor.isNull(contactOffset + ContactTable.lastName.ordinal()) ) {
					lastName = cursor.getString(contactOffset + ContactTable.lastName.ordinal());
				}
				if ( TextUtils.isEmpty(lastName) && TextUtils.isEmpty(firstName) ) {
					displayName = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
				} 
				else {
					displayName = String.format("%s %s", firstName, lastName);
				}
			}
			
			if ( TextUtils.isEmpty(lastName) && TextUtils.isEmpty(firstName) ) {
				displayName = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
			} 
			else {
				displayName = String.format("%s %s", firstName, lastName);
			}
		}
		else {
			String firstName = null;
			String lastName = null;
			
			if ( groupName == null ) {
				//Log.d("MessagesListItemHolder", "Outbox has cursor: " + DatabaseUtils.dumpCursorToString(cursor));
				firstName = cursor.getString(cursor.getColumnCount() - 2 - groupOffset);
				lastName = cursor.getString(cursor.getColumnCount() - 1 - groupOffset);
			}
			else {
				//Log.d("MessagesListItemHolder", "Outbox with groupName: " + groupName + " has cursor: " + DatabaseUtils.dumpCursorToString(cursor));
				firstName = cursor.getString(cursor.getColumnCount() - 2);
				lastName = cursor.getString(cursor.getColumnCount() - 1);
				
				if ( TextUtils.isEmpty(lastName) && TextUtils.isEmpty(firstName) ) {
					firstName = cursor.getString(contactOffset + ContactTable.firstName.ordinal());
					lastName = cursor.getString(contactOffset + ContactTable.lastName.ordinal());
				} 
			}

			if ( TextUtils.isEmpty(lastName) && TextUtils.isEmpty(firstName) ) {
				displayName = cursor.getString(MessageTable.fromSmartPagerId.ordinal());
			} 
			else {
				displayName = String.format("%s %s", firstName, lastName);
			}
		}

		int recipientCount = cursor.getInt(cursor.getColumnCount() - 4 - groupOffset) - 1;
		if ( recipientCount > 1 ) {
			displayName += " +" + recipientCount;
		}

		mGroupIconImageView.setVisibility(groupName == null ? View.GONE : View.VISIBLE);
		mSubjectTextView.setText(cursor.getString(MessageTable.subjectText.ordinal()));
		
        /*String subjectColor = cursor.getString(MessageTable.subjectColor.ordinal());
        if (subjectColor != null && !subjectColor.equalsIgnoreCase("null") ) {
    		mSubjectTextView.setTextColor(Color.parseColor(String.format("#%s",
                    subjectColor)));
        }*/
        
		SmartPagerApplication.getInstance().getImageLoader().displayImage(cursor.getString(MessageTable.subjectIconURL.ordinal()), mSubjectTextView);

		// subjectTextView.setText(cursor.getString(MessageTable.id.ordinal()));
        if ( groupName == null ) {
		    mNameTextView.setText(displayName);
		    mBodyTextView.setText(cursor.getString(MessageTable.text.ordinal()));
        }
        else {
            mNameTextView.setText(groupName);
            mBodyTextView.setText(displayName + ": " + cursor.getString(MessageTable.text.ordinal()));
        }
        
        /*String bodyColor = cursor.getString(MessageTable.bodyColor.ordinal());
        if ( bodyColor != null && !bodyColor.equalsIgnoreCase("null") ) {
    		mBodyTextView.setTextColor(Color.parseColor(String.format("#%s",
				cursor.getString(MessageTable.bodyColor.ordinal()))));
        }*/

		String recordUrl = cursor.getString(MessageTable.recordUrl.ordinal());
		if ( TextUtils.isEmpty(recordUrl) ) {
			mRecordImageView.setVisibility(View.GONE);
		} 
		else {
			mRecordImageView.setVisibility(View.VISIBLE);
		}

		//String subjectdUrl = cursor.getString(MessageTable.subjectIconURL.ordinal());
		//if (!TextUtils.isEmpty(subjectdUrl)) {
		//	
		//}
	}

	private long setActionAge(long time, TextView ageTextView, TextView ageCategoryTextView) {
		Calendar calendar = Calendar.getInstance();

		long diffInMis = Math.abs(time - calendar.getTimeInMillis());
		String category = "days";
		long diff = TimeUnit.MILLISECONDS.toDays(diffInMis);
		if (diff == 0) {
			category = "hours";
			diff = TimeUnit.MILLISECONDS.toHours(diffInMis);
			if (diff == 0) {
				category = "min";
				diff = TimeUnit.MILLISECONDS.toMinutes(diffInMis);
				if (diff == 0) {
					category = "sec";
					diff = TimeUnit.MILLISECONDS.toSeconds(diffInMis);
				}
			}
		}
		ageTextView.setText(String.valueOf(diff));
		ageCategoryTextView.setText(String.format("%s ago", category));
		return diff;
	}

	@Override
	public void setListeners(Object... listeners) {

	}

}
