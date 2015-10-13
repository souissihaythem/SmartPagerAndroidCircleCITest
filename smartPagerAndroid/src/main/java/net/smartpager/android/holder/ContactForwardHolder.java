package net.smartpager.android.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.rey.material.widget.RadioButton;
import com.squareup.picasso.Picasso;

import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

@SuppressLint("NewApi")
public class ContactForwardHolder implements IHolder {

	TextView nameTextView;
	TextView descriptionTextView;
	TextView statusView;
	ImageView imageView;
	RadioButton checkBox;
	String mSelectedID="";

	@Override
	public IHolder createHolder(View view) {

		nameTextView = (TextView) view.findViewById(R.id.item_to_contact_name_textView);
		descriptionTextView = (TextView) view.findViewById(R.id.item_to_description_textView);
		statusView = (TextView) view.findViewById(R.id.item_to_status_textView);
		imageView = (ImageView) view.findViewById(R.id.item_to_image_imageView);
		checkBox = (RadioButton) view.findViewById(R.id.item_to_checkbox);
		checkBox.setClickable(false);

		return this;
	}

	@Override
	public void setData(Object source) {
		final Recipient contactRecipient = (Recipient) source;
		// checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		//
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// contactRecipient.setSelected(isChecked);
		// }
		// });
		checkBox.setChecked(contactRecipient.getId().equalsIgnoreCase(mSelectedID));
		nameTextView.setText(contactRecipient.getName());
		descriptionTextView.setText(contactRecipient.getTitle());
		// set image
//		SmartPagerApplication.getInstance().getImageLoader()
//				.displayImage(contactRecipient.getImageUrl(), imageView, R.drawable.chat_avatar_no_image);
//		imageView.setImageUrl(contactRecipient.getImageUrl(), R.drawable.chat_avatar_no_image);

		imageView.setImageResource(R.drawable.chat_avatar_no_image);

		String imageUrl = contactRecipient.getImageUrl();
		Picasso.with(SmartPagerApplication.getInstance().getApplicationContext())
				.cancelRequest(imageView);
		if (imageUrl.length() > 0) {
			Picasso.with(SmartPagerApplication.getInstance().getApplicationContext())
					.load(imageUrl)
					.into(imageView);
		}
		
		// set status
		String statusName = contactRecipient.getStatus();
		statusName = statusName.replace(" ", "_");
		switch (ContactStatus.valueOf(statusName)) {
			case ONLINE:
				statusView.setBackgroundResource(R.drawable.shape_contact_status);
			break;
			default:
				statusView.setBackgroundResource(R.drawable.shape_contact_status_offline);
			break;
		}
	}

	@Override
	public void setListeners(Object... listeners) {

	}

	@Override
	public ViewGroup getInflateView(Context context) {
		return new RelativeLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_contact_to_forward;
	}

	@Override
	public void preSetData(Bundle params) {
		if (params != null && params.containsKey(BundleKey.recipientID.name())) {
			mSelectedID = params.getString(BundleKey.recipientID.name());
		}
	}

}
