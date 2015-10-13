package net.smartpager.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.DatabaseHelper.QuickResponseTable;

public class AutoResponseCursorAdapter extends CursorAdapter {

	private int mSelectedIndex = -1;
	
	public void setSelectedIndex(int index) {
		mSelectedIndex = index;
	}
	
	public AutoResponseCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final TextView messageTextView = (TextView) view.findViewById(R.id.item_auto_response_text_view);
		int columnIndex = cursor.getColumnIndex( QuickResponseTable.messageText.name() );
		String messageText = cursor.getString(columnIndex);
		messageTextView.setText(messageText);
		RadioButton radioButton = (RadioButton) view.findViewById(R.id.item_auto_response_radiobutton);
		
		if (mSelectedIndex == cursor.getPosition()) {
			radioButton.setChecked(true);
		} else {
			radioButton.setChecked(false);
		}
		
		radioButton.setChecked(messageText.equals(SmartPagerApplication.getInstance().getPreferences().getAutoResponse()));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewGroup view = new RelativeLayout(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater) context.getSystemService(inflater);
		vi.inflate(R.layout.item_auto_response, view, true);
		return view;
	}
}

