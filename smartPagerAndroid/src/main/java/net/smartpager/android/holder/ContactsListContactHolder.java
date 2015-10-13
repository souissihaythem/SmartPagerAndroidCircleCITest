package net.smartpager.android.holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.fragment.interfaces.ContactsListButtonsListener;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class ContactsListContactHolder implements IHolder {
	
	private TextView mNameTextView;
	private TextView mDescriptionTextView;
	private TextView mStatusView;
	private ImageView mImageView;
	private Button mCallButton;
	private Button mPageButton;
	private ContactsListButtonsListener mListener;
	
	@Override
	public IHolder createHolder(View view) {
		
		mNameTextView = (TextView) view.findViewById(R.id.item_message_contact_name_textView);
		mDescriptionTextView = (TextView) view.findViewById(R.id.item_massage_them_textView);
		mStatusView = (TextView) view.findViewById(R.id.item_contact_status_textView);
		mImageView = (ImageView) view.findViewById(R.id.item_message_status_imageView);
		mCallButton = (Button) view.findViewById(R.id.contact_item_call_button);
		mPageButton= (Button) view.findViewById(R.id.contact_item_page_button);
		
		return this;
	}

	@Override
	public void setData(Object source) {
		
		final Recipient recipient = (Recipient) source;
		
		mNameTextView.setText(recipient.getName());
		mDescriptionTextView.setText(recipient.getTitle());
		
//		if (recipient.getName().contains("aaaa aaaa")){
//			Log.e("*************", recipient.getName(), recipient.getImageUrl(), TextUtils.isEmpty(recipient.getImageUrl()));
//			
//		}

		mImageView.setImageResource(R.drawable.chat_avatar_no_image);
		final String imageUri = recipient.getImageUrl();

		Picasso.with(SmartPagerApplication.getInstance().getApplicationContext())
				.cancelRequest(mImageView);
		if (imageUri.length() > 0) {
			Picasso.with(SmartPagerApplication.getInstance().getApplicationContext())
					.load(imageUri)
					.into(mImageView);
		}

		String statusName = recipient.getStatus();
		statusName = statusName.replace(" ", "_");
		switch (ContactStatus.valueOf(statusName)) {
			case ONLINE:
				mStatusView.setBackgroundResource(R.drawable.shape_contact_status);
			break;
			default:
				mStatusView.setBackgroundResource(R.drawable.shape_contact_status_offline);
			break;
		}
		
		final String phoneNumber = recipient.getPhone();
		
		if (SmartPagerApplication.getInstance().getPreferences().getAllowCallFromContacts() == false) {
			mCallButton.setVisibility(View.GONE);
		} else {
			mCallButton.setVisibility(View.VISIBLE);
		}
		
		mCallButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mListener.onCallButtonClick(phoneNumber);
			}
		});

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
		return R.layout.item_contact_list;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
