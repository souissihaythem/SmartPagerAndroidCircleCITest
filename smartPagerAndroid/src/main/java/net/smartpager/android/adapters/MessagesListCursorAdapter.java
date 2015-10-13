package net.smartpager.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.DatabaseHelper;
import net.smartpager.android.holder.MessagesListItemHolder;

import java.util.ArrayList;

import biz.mobidev.framework.adapters.holderadapter.IHolderCursor;

public class MessagesListCursorAdapter extends CursorAdapter {

	private ArrayList<Class<? extends IHolderCursor>> mHolders;
	private LayoutInflater mInflater;
	private Context context;

	public MessagesListCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.context = context;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHolders = new ArrayList<Class<? extends IHolderCursor>>();
		mHolders.add(MessagesListItemHolder.class);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		IHolderCursor holderCursor = (IHolderCursor) view.getTag();
		holderCursor.setData(cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int position = getItemViewType(getCursor().getPosition());
		IHolderCursor itemHolder = null;
		try {
			itemHolder = mHolders.get(0).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		ViewGroup view = itemHolder.inflateLayout(mInflater, context);
		view.setTag(itemHolder.createHolder(view));
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		//getCursor().moveToPosition(position);
		//return getCursor().getInt(GroupContactItem.isGroup.ordinal());

		Cursor cursor = (Cursor) getItem(position);
		String threadId = cursor.getString(DatabaseHelper.MessageTable.threadID.ordinal());
		// set item title
		if (SmartPagerApplication.getInstance().isThreadMuted(context.getApplicationContext(), Integer.parseInt(threadId))) {
			return 1;
		}
		else{
			return 0;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		//return GroupContactItem.values().length;
		return 2;
	}
}