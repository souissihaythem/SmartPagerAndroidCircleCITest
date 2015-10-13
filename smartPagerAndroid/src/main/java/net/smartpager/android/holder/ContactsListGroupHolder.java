package net.smartpager.android.holder;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import com.squareup.picasso.Picasso;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.fragment.interfaces.ContactsListButtonsListener;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class ContactsListGroupHolder implements IHolder {

	private TextView mNameTextView;
	private Button mPageButton;
	private ImageView mGroupImage;
	private ContactsListButtonsListener mListener;

	@Override
	public IHolder createHolder(View view) {
		
		mNameTextView = (TextView) view.findViewById(R.id.item_message_contact_name_textView);
		mPageButton = (Button) view.findViewById(R.id.item_message_page_button);
		mGroupImage = (ImageView) view.findViewById(R.id.item_message_status_imageView);

		return this;
	}

	@Override
	public void setData(Object source) {
		
		final Recipient recipient = (Recipient) source;

		final String name = recipient.getName();
		mNameTextView.setText(name);

		if (recipient.getGroupType() != null && recipient.getGroupType().equalsIgnoreCase("SCHEDULE")) {
			mGroupImage.setImageResource(R.drawable.contacts_ic_schedule_group_static);
		} else {
			mGroupImage.setImageResource(R.drawable.contacts_ic_group_static);
		}
		
		mPageButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				mListener.onPageButtonClick(recipient);
			}
		});
	}

	@Override
	public void setListeners(Object... listeners) {
		
		if (listeners != null) {
			mListener = (ContactsListButtonsListener) listeners[0];
		}
	}

	@Override
	public ViewGroup getInflateView(Context context) {
		
		return new RelativeLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_group_list;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
