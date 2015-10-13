package net.smartpager.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;

public class QuickResponseCursorAdapter extends CursorAdapter {

	public interface QuickResponseChangeListener {
		void onEditQuickResponse(int id, String msgText);
		void onDeleteQuickResponse(int id);
	}
	
	private QuickResponseChangeListener mChangeListener;
	private boolean mEditableMode;
	
	public void setChangeListener(QuickResponseChangeListener listener) {
		mChangeListener = listener;
	}

	public QuickResponseCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}
	

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		TextView txtMsg = (TextView) view.findViewById(R.id.item_quick_response_text_view);
		txtMsg.setText(cursor.getString(QuickResponseTable.messageText.ordinal()));
		
		ImageView btnEdit = (ImageView) view.findViewById(R.id.item_quick_response_btn_edit);
		ImageView btnDelete = (ImageView) view.findViewById(R.id.item_quick_response_btn_delete);
		
		final int msgId = cursor.getInt(QuickResponseTable._id.ordinal());
		final String msgText = cursor.getString(QuickResponseTable.messageText.ordinal());
		
		if (mEditableMode) {
			
			btnEdit.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mChangeListener != null) {
						mChangeListener.onEditQuickResponse(msgId, msgText);
					}
				}
			});
			
			btnDelete.setBackgroundResource(R.drawable.delete_ic_response);
			btnDelete.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mChangeListener != null) {
						mChangeListener.onDeleteQuickResponse(msgId);
					}
				}
			});
			
		} else {
			
			btnEdit.setVisibility(View.GONE);
			btnDelete.setBackgroundResource(R.drawable.def_ic_movement);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewGroup view = new RelativeLayout(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater) context.getSystemService(inflater);
		vi.inflate(R.layout.item_quick_response, view, true);
		return view;
	}

	public void setEditableMode(boolean editable) {
		mEditableMode = editable;
	}
}