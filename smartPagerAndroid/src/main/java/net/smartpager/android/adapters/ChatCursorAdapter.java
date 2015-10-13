package net.smartpager.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.smartpager.android.database.DatabaseHelper.MessageTable;
import net.smartpager.android.holder.chat.ChatInboxItemHolder1;
import net.smartpager.android.holder.chat.ChatInboxItemHolder2;
import net.smartpager.android.holder.chat.ChatInboxItemHolder3;
import net.smartpager.android.holder.chat.ChatInboxItemHolder4;
import net.smartpager.android.holder.chat.ChatInboxItemHolder5;
import net.smartpager.android.holder.chat.ChatItemClickListener;
import net.smartpager.android.holder.chat.ChatItemHolder;
import net.smartpager.android.holder.chat.ChatSendItemHolder;
import net.smartpager.android.model.PlayInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;
import biz.mobidev.framework.utils.Log;

public class ChatCursorAdapter extends CursorAdapter implements ChatItemClickListener{

	LayoutInflater mInflater;
	public ChatItemClickListener mChatItemClickListener;
    private boolean[] m_arrMessageRecipientsShown = null;
	private Map<String, Integer> mRecipientPositionMap;
	private ArrayList<Class<? extends IHolderCursor>> holders;
	private Bundle params = null;

	public ChatCursorAdapter(Context context, Cursor c, boolean autoRequery, Map<String, Integer> recipientPosMap) {
		super(context, c, autoRequery);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		holders = new ArrayList<Class<? extends IHolderCursor>>();
		holders.add(ChatSendItemHolder.class);
		holders.add(ChatInboxItemHolder1.class);
		holders.add(ChatInboxItemHolder2.class);
		holders.add(ChatInboxItemHolder3.class);
		holders.add(ChatInboxItemHolder4.class);
		holders.add(ChatInboxItemHolder5.class);
		mRecipientPositionMap = recipientPosMap;
	}

	public void setPresetData(Bundle bundle) {
		params = bundle;
	}

	public void setChatItemClickListener(ChatItemClickListener chatItemClickListener) {
		this.mChatItemClickListener = chatItemClickListener;
	}

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        m_arrMessageRecipientsShown = new boolean[newCursor.getCount()];
        return super.swapCursor(newCursor);
    }

    @Override
	public void bindView(View view, Context content, Cursor cursor) {
		IHolderCursor holderCursor = (IHolderCursor) view.getTag();
        ((ChatItemHolder)holderCursor).setCurrListPosition(cursor.getPosition());
        ((ChatItemHolder)holderCursor).setWasRecipientsShown(m_arrMessageRecipientsShown[cursor.getPosition()]);
		holderCursor.preSetData(params);
		holderCursor.setData(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int position = getItemViewType(getCursor().getPosition());
		IHolderCursor itemHolder = null;
		try {
			itemHolder = holders.get(position).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		ViewGroup view = itemHolder.inflateLayout(mInflater, context);
		itemHolder.setListeners(mChatItemClickListener, this);
		view.setTag(itemHolder.createHolder(view));
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		if (getCursor().getPosition() != position) {
			getCursor().moveToPosition(position);
		}
		String pagerId = getCursor().getString(MessageTable.fromSmartPagerId.ordinal());
		
		if(!mRecipientPositionMap.containsKey(pagerId)){
//			int pos = mRecipientPositionMap.size() %(holders.size()-1);
			int pos = 1 + (mRecipientPositionMap.size()-1) %(holders.size()-1);
			mRecipientPositionMap.put(pagerId, pos);
		}
		return mRecipientPositionMap.get(pagerId);
	}

	@Override
	public int getViewTypeCount() {
		return holders.size();
	}

    @Override
    public void onImageClick(int messageId) {

    }

    @Override
    public void onCallBackClick(String number, int messageId) {

    }

    @Override
    public void onContactClick(String contactId) {

    }

    @Override
    public void onChatItemSwitchPlayMode(PlayInfo playInfo) {

    }

    @Override
    public void onRejectAcceptanceReq(long msgId) {

    }

    @Override
    public void onAcceptAcceptanceReq(long msgId) {

    }

    @Override
    public void onResponseOptionClick(String respText) {

    }

    @Override
    public void onRecipientsClick(int listPosition, boolean isShown) {
        m_arrMessageRecipientsShown[listPosition] = isShown;
    }
}
