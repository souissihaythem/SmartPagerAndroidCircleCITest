package net.smartpager.android.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.database.ContactGroupTable.ItemType;

public class RecipientsCursorAdapter extends SimpleCursorAdapter {

	public RecipientsCursorAdapter(Context context, Cursor c, boolean autoRequery) {
		// super(context, c, autoRequery);
		super(context, R.layout.item_recipient, c, new String[] { GroupContactItem.name.name(),
				GroupContactItem.contact_lastname_or_group_type.name(),
				GroupContactItem.contact_smart_pager_id_or_group_id.name() }, new int[] {
				GroupContactItem.name.ordinal(), GroupContactItem.contact_lastname_or_group_type.ordinal(),
				GroupContactItem.contact_smart_pager_id_or_group_id.ordinal() },
				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public CharSequence convertToString(Cursor mCursor) {
		switch (ItemType.values()[mCursor.getInt(GroupContactItem.isGroup.ordinal())]) {
			case isGroup:
				return mCursor.getString(GroupContactItem.name.ordinal());
			default:
				return mCursor.getString(GroupContactItem.name.ordinal()) + " "
						+ mCursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void bindView(View view, Context context, Cursor cursor) {		
		TextView nameTextView = (TextView) view.findViewById(R.id.item_to_contact_name_textView);
		TextView descriptionTextView = (TextView) view.findViewById(R.id.item_to_description_textView);
		TextView statusView = (TextView) view.findViewById(R.id.item_to_status_textView);
		ImageView imageView = (ImageView) view.findViewById(R.id.item_to_image_imageView);
		String name = "";
		switch (ItemType.values()[cursor.getInt(GroupContactItem.isGroup.ordinal())]) {
			case isContact:
				statusView.setVisibility(View.VISIBLE);
				name = cursor.getString(GroupContactItem.name.ordinal()) + " "
						+ cursor.getString(GroupContactItem.contact_lastname_or_group_type.ordinal());
				nameTextView.setText(name);
				descriptionTextView.setText(cursor.getString(GroupContactItem.contact_title_or_group_empty.ordinal()));
				// set image
				String imageUri = cursor.getString(GroupContactItem.contact_url_or_group_empty.ordinal());
				SmartPagerApplication.getInstance().getImageLoader()
						.displayImage(imageUri, imageView, R.drawable.chat_avatar_no_image);
				// set status
				String statusName = cursor.getString(GroupContactItem.contact_status_or_group_empty.ordinal());
				statusName = statusName.replace(" ", "_");
				switch (ContactStatus.valueOf(statusName)) {
					case ONLINE:
						statusView.setBackgroundResource(R.drawable.shape_contact_status);
					break; 
					default:
						statusView.setBackgroundResource(R.drawable.shape_contact_status_offline);
					break;
				}
			break;
			case isGroup:
				// set name
				name = cursor.getString(GroupContactItem.name.ordinal());
				nameTextView.setText(name);
				// set image
				imageView.setImageResource(R.drawable.contacts_ic_group_static);
				imageView.setBackgroundDrawable(null);
				imageView.setScaleType(ScaleType.FIT_CENTER);
				// hide status
				statusView.setVisibility(View.INVISIBLE);
			break;
			default:
			break;
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		ViewGroup view = new RelativeLayout(context);
		String inflater = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater vi = (LayoutInflater) context.getSystemService(inflater);
		vi.inflate(R.layout.item_recipient, view, true);
		return view;
	}
}
