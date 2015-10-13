package net.smartpager.android.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.model.ImageSource;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

@SuppressLint("NewApi")
public class MessageRecipientHolder implements IHolder {

	public interface OnDeleteRecipientClickListener{
		public void onDeleteRecipientClick(Recipient deleteRecipient);
		public void onDeleteImageClick(ImageSource imageSource);
	}
	
	OnDeleteRecipientClickListener mClickListener;
	TextView nameTextView;
	ImageButton deleteButton;
	View statusView;	
	
	@Override
	public IHolder createHolder(View view) {
		nameTextView = (TextView) view.findViewById(R.id.recipient_name_textView);
		deleteButton = (ImageButton) view.findViewById(R.id.recipient_delete_button);
		statusView = (View) view.findViewById(R.id.recipient_status_view);
		return this;
	}

	@Override
	public void setData(Object source) {
		final Recipient contactRecipient = (Recipient) source;
				
		nameTextView.setText(contactRecipient.getName());

		String statusName = contactRecipient.getStatus();
		statusName = statusName.replace(" ", "_");
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mClickListener.onDeleteRecipientClick(contactRecipient);
				
			}
		});
		if (TextUtils.isEmpty(statusName)) {
			statusView.setBackgroundResource(R.drawable.shape_contact_status);
		} else {
			switch (ContactStatus.valueOf(statusName)) {
				case ONLINE:
					statusView.setBackgroundResource(R.drawable.shape_contact_status);
				break;
				default:
					statusView.setBackgroundResource(R.drawable.shape_contact_status_offline);
				break;
			}
		}
	}

	@Override
	public void setListeners(Object... listeners) {
		mClickListener = (OnDeleteRecipientClickListener) listeners[0];

	}

	@Override
	public ViewGroup getInflateView(Context context) {
		return new LinearLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_page_recipient;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
