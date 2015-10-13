package net.smartpager.android.holder.chat;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.URLSpan;
import android.text.util.Linkify;
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
import net.smartpager.android.SmartPagerContentProvider;
import net.smartpager.android.consts.Constants;
import net.smartpager.android.database.ChatsTable;
import net.smartpager.android.database.ChatsTable.MessageStatus;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper.ContactTable;
import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.database.DatabaseHelper.RecipientTable;
import net.smartpager.android.utils.DateTimeUtils;
import net.smartpager.android.utils.DefensiveURLSpan;
import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;

public class ChatSendItemHolder extends ChatItemHolder implements IHolderCursor, LoaderCallbacks<Cursor> {
	
	Context mContext;

    LinearLayout btnShowRecipients;
	RelativeLayout mContainerLayout;
	ImageView mRecordImageView;
	TextView mMessageCaptionTextView;
	TextView mMessageBodyTextView;
	TextView mSignatureTextView;
	TextView mDateTextView;
    TextView mBtnShowRecipientsText;
	View mLineView;
	
	TextView mDeliveredTextView;
	TextView mReadTextView;
	TextView mAcceptedTextView;

	private FragmentActivity mActivity;
	private int mMessageId = -1;
	private int mSpin = MessageTable.values().length;
    private int m_recipientsCounter;
    private int m_deliveredCounter;
    private int m_acceptedCounter;
    private int m_pendingCounter;
    private int m_readCounter;
    private int m_receivedCounter;
    private int m_rejectedCounter;
    private int m_repliedCounter;
    private int m_sentCounter;
    private ChatItemClickListener m_nCurrListener = null;

    public ChatSendItemHolder() {
		mMessageId = -1;
	}

	@Override
	public ViewGroup inflateLayout(LayoutInflater inflater, Context context) {
		ViewGroup view = new RelativeLayout(context);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_recipientsCounter > 1)
                {
                    m_bWasRecipientsShown = !m_bWasRecipientsShown;
                    updateRecipientsLine();
                    m_nCurrListener.onRecipientsClick(m_nCurrListPosition, m_bWasRecipientsShown);
                }
            }
        });
        mContext = context;
		mActivity = (FragmentActivity) context;
		inflater.inflate(R.layout.item_chat_list_my, view, true);
		return view;
	}

    private void updateRecipientsLine ()
    {
        if(m_recipientsCounter > 1)
        {
            if(btnShowRecipients != null)
                btnShowRecipients.setVisibility( !m_bWasRecipientsShown ? View.VISIBLE : View.GONE );
            if(mAcceptedTextView != null)
                mAcceptedTextView.setVisibility( !m_bWasRecipientsShown ? View.GONE : View.VISIBLE);
        }
    }

	@Override
	public IHolderCursor createHolder(View view) {
		super.createHolder(view);
		
		mContainerLayout = (RelativeLayout) view.findViewById(R.id.item_chat_message_container);
		mRecordImageView = (ImageView) view.findViewById(R.id.item_chat_record_image_view1);
		mMessageCaptionTextView = (TextView) view.findViewById(R.id.item_chat_message_caption_text_view);
		mMessageBodyTextView = (TextView) view.findViewById(R.id.item_chat_message_body_text_view);
		mSignatureTextView = (TextView) view.findViewById(R.id.item_chat_signature_text_view);
		mDateTextView = (TextView) view.findViewById(R.id.item_chat_date_text_view);
		mLineView = view.findViewById(R.id.item_chat_line);
        btnShowRecipients = (LinearLayout) view.findViewById(R.id.item_chat_bt_show_recipients);
        mBtnShowRecipientsText  = (TextView) btnShowRecipients.findViewById(R.id.bt_show_recipients_text);
		
		mDeliveredTextView = (TextView) view.findViewById(R.id.item_chat_delivered_text_view);
		mReadTextView = (TextView) view.findViewById(R.id.item_chat_read_text_view);
		mAcceptedTextView = (TextView) view.findViewById(R.id.item_chat_accepted_text_view);
        updateRecipientsLine();
		return this;
	}
	
	private void fixTextView(TextView tv) {
		try {
			SpannableString current=(SpannableString)tv.getText();
			URLSpan[] spans=
					current.getSpans(0, current.length(), URLSpan.class);

			for (URLSpan span : spans) {
				int start=current.getSpanStart(span);
				int end=current.getSpanEnd(span);

				current.removeSpan(span);
				current.setSpan(new DefensiveURLSpan(span.getURL(), mContext), start, end,
						0);
			}
		} catch (ClassCastException e) {
			Log.d(getClass().getName(), "ClassCastException: " + e);
		}
		
	}


	@Override
	public void preSetData(Bundle params) {
		super.preSetData(params);
        updateRecipientsLine();
	}

	@Override
	public void setData(Cursor cursor) {
		super.setData(cursor);
		mMessageBodyTextView.setText(cursor.getString(MessageTable.text.ordinal()));
		Linkify.addLinks(mMessageBodyTextView, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
		fixTextView(mMessageBodyTextView);
		mMessageId = cursor.getInt(MessageTable.id.ordinal());

		mActivity.getSupportLoaderManager().initLoader(mMessageId, null, this);

//		int idxContactId = 26; // Look for SQL query for more details

		mSignatureTextView.setText(cursor.getString(mSpin + ContactTable.firstName.ordinal()) + " "
				+ cursor.getString(mSpin + ContactTable.lastName.ordinal()));

		long milliseconds = cursor.getLong(MessageTable.timeSent.ordinal());
		if (milliseconds == Constants.DEFAULT_DATE.getTime()) {
			mDateTextView.setText("");
		} else {
			mDateTextView.setText(DateTimeUtils.format(milliseconds));
		}
        
        updateRecipientsLine();
	}

	@Override
	public void setListeners(Object... listeners) {
		super.setListeners(listeners);
        if(listeners.length > 1)
            m_nCurrListener = (ChatItemClickListener)listeners[1];
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(mActivity, SmartPagerContentProvider.CONTENT_RECIPIENTS_FULLINFO_URI, null,
				String.valueOf(mMessageId), null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) 
	{
		if ( loader.getId() == mMessageId ) {
			if ( cursor != null && cursor.moveToFirst() ) {
                resetCounters();
				int spin = GroupContactItem.values().length - 1;
				StringBuilder builder = new StringBuilder();
				boolean isFirst = true;
				
				do {

                    String myFullName = SmartPagerApplication.getInstance().getPreferences().getFirstName() + " " + SmartPagerApplication.getInstance().getPreferences().getLastName();

                    StringBuilder isThisMyUserBuilder = new StringBuilder();
                    isThisMyUserBuilder.append(cursor.getString(GroupContactItem.name.ordinal())).append(" ");
                    if (cursor.getInt(GroupContactItem.isGroup.ordinal()) == 0) {
                        isThisMyUserBuilder.append(cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal()))
                                .append(" ");
                    }

                    if (isThisMyUserBuilder.toString().trim().equalsIgnoreCase(myFullName.trim())) {
                        continue;
                    }



                    m_recipientsCounter++;
                    
					if (!isFirst) {
						builder.append("<br>");
					} 
					else {
						isFirst = false;
					}
					
					builder.append(cursor.getString(GroupContactItem.name.ordinal())).append(" ");
					if (cursor.getInt(GroupContactItem.isGroup.ordinal()) == 0) {
						builder.append(cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal()))
								.append(" ");
					}
					long lastUpdateMilliseconds = cursor.getLong(spin + RecipientTable.lastUpdate.ordinal());
					String status = cursor.getString(spin + RecipientTable.status.ordinal());
					
					// To combat issue where message can sometimes be set as 'Delivered' when it has already been 'Read'
					long readTimeMilliseconds = cursor.getLong(spin + RecipientTable.readTime.ordinal());
					if (readTimeMilliseconds > 0 && status.equalsIgnoreCase(MessageStatus.Delivered.name())) {
						status = MessageStatus.Read.name();
						lastUpdateMilliseconds = cursor.getLong(spin + RecipientTable.readTime.ordinal());
					}
					
					if ( status == null ) {
						//Log.d("ChatSendItemHolder", "Cursor has null for status at col(" + (spin + RecipientTable.status.ordinal()) + "): " + DatabaseUtils.dumpCursorToString(cursor));
					}
					
                    updateRecipientsStatusCounters(status);
                    if (status == null || status.equalsIgnoreCase(ChatsTable.MessageStatus.Failed.name()) || status.equalsIgnoreCase(ChatsTable.MessageStatus.Delayed.name()) ) {
                        status = ChatsTable.MessageStatus.Sent.name();
                    }
                    
					// switch (MessageStatus.valueOf(status)) {
					// case REPLIED:
					// // date.setTime(cursor.getLong(MessageTable.repliedTime.ordinal()));
					// repliedImageView.setVisibility(View.VISIBLE);
					// break;
					// case DELIVERED:
					// // date.setTime(cursor.getLong(MessageTable.deliveredTime.ordinal()));
					// repliedImageView.setVisibility(View.INVISIBLE);
					// break;
					// case READ:
					// // date.setTime(cursor.getLong(MessageTable.readTime.ordinal()));
					// repliedImageView.setVisibility(View.INVISIBLE);
					// break;
					// case PENDING:
					// //date.setTime(cursor.getLong(MessageTable.timeSent.ordinal()));
					// repliedImageView.setVisibility(View.INVISIBLE);
					// break;
					// default:
					// repliedImageView.setVisibility(View.INVISIBLE);
					// break;
					// }
					builder.append("<b>");
					String firstChar = status.substring(0, 1);
					status = firstChar + status.toLowerCase().substring(1);
					builder.append(status).append(" ");
					// builder.append(cursor.getString(spin + RecipientTable.lastUpdate.ordinal())).append("\n");
					builder.append(DateTimeUtils.format(lastUpdateMilliseconds));
					builder.append("</b>");

				} while (cursor.moveToNext());
				
				mAcceptedTextView.setText(Html.fromHtml(builder.toString()));
				// acceptedTextView.setVisibility(View.VISIBLE);
			} 
			else {
				// acceptedTextView.setVisibility(View.GONE);
			}
			
            btnShowRecipients.setVisibility(m_recipientsCounter > 1 ? View.VISIBLE : View.GONE);
            mAcceptedTextView.setVisibility(m_recipientsCounter > 1 ? View.GONE : View.VISIBLE);
            updateAcceptedTextView();
		} 
		else {
			// /acceptedTextView.setVisibility(View.GONE);
		}
		
        updateRecipientsLine();
	}

    private void resetCounters ()
    {
        m_recipientsCounter = 0;
        m_deliveredCounter = 0;
        m_acceptedCounter = 0;
        m_pendingCounter = 0;
        m_readCounter = 0;
        m_receivedCounter = 0;
        m_rejectedCounter = 0;
        m_repliedCounter = 0;
        m_sentCounter = 0;
    }

    private void updateAcceptedTextView ()
    {
        int currValue = 0;
        StringBuilder builder = new StringBuilder();
        for(ChatsTable.MessageStatus status : ChatsTable.MessageStatus.values())
        {
            currValue = 0;
            switch (status)
            {
                case Accepted:
                    currValue = m_acceptedCounter;
                    break;
                case Delivered:
                    currValue = m_deliveredCounter;
                    break;
                case Pending:
                    currValue = m_pendingCounter;
                    break;
                case Read:
                    currValue = m_readCounter;
                    break;
                case Received:
                    currValue = m_receivedCounter;
                    break;
                case Rejected:
                    currValue = m_rejectedCounter;
                    break;
                case Replied:
                    currValue = m_repliedCounter;
                    break;
                case Sent:
                    currValue = m_sentCounter;
                    break;
                case Failed:
                case Delayed:
                    continue;
                default:
                    break;
            }
            if(currValue > 0)
                builder.append(" ").append(status.name()).append(": ").append(currValue).append(",");
        }
        if(builder.length() > 0)
        {
            if(builder.charAt(builder.length() - 1) == ',')
                builder.deleteCharAt(builder.length() - 1);
            mBtnShowRecipientsText.setText(builder.toString());
        }
    }

    private void updateRecipientsStatusCounters (String status)
    {
        if(status == null)
            return;
        switch (ChatsTable.MessageStatus.valueOf(status.trim())) {
            case Accepted:
                m_acceptedCounter++;
                break;
            case Delivered:
                m_deliveredCounter++;
                break;
            case Pending:
                m_pendingCounter++;
                break;
            case Read:
                m_readCounter++;
                break;
            case Received:
                m_receivedCounter++;
                break;
            case Rejected:
                m_rejectedCounter++;
                break;
            case Replied:
                m_repliedCounter++;
                break;
            case Sent:
            case Failed:
            case Delayed:
                m_sentCounter++;
                break;
            default:
                break;
        }
    }

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
	}

}
