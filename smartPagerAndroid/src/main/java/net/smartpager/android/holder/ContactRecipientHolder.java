package net.smartpager.android.holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.activity.ContactDetailsActivity;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.database.ContactGroupTable.GroupContactItem;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class ContactRecipientHolder implements IHolder {

	private TextView mTxtName;
	private TextView mTxtStatus;
	private ImageView mImgImage;

	@Override
	public IHolder createHolder(View view) {

		mTxtName = (TextView) view.findViewById(R.id.chat_recipient_first_name_textView);
		mTxtStatus = (TextView) view.findViewById(R.id.chat_recipient_status_textView);
		mImgImage = (ImageView) view.findViewById(R.id.chat_recipient_image_imageView);

		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setData(Object source) {
		final Recipient contactRecipient = (Recipient) source;
		String displayName;
		if (!contactRecipient.getName().equals("null null")) {
			displayName = contactRecipient.getName();
		} else {
			displayName = contactRecipient.getSmartPagerId();
		}
		mTxtName.setText(displayName);
		// set image
		if (contactRecipient.isGroup()) {
			// set image
			mImgImage.setImageResource(R.drawable.contacts_ic_group_static);
			mImgImage.setBackgroundDrawable(null);
			mImgImage.setScaleType(ScaleType.FIT_CENTER);
			// hide status
			mTxtStatus.setVisibility(View.INVISIBLE);
		} else {
			// show status
			mTxtStatus.setVisibility(View.VISIBLE);
			// set image
			SmartPagerApplication.getInstance().getImageLoader()
					.displayImage(contactRecipient.getImageUrl(), mImgImage, R.drawable.chat_avatar_no_image);
		}

		// set status
		String statusName = contactRecipient.getStatus();
		if (!TextUtils.isEmpty(statusName)) {
			statusName = statusName.replace(" ", "_");
			switch (ContactStatus.valueOf(statusName)) {
				case ONLINE:
					mTxtStatus.setBackgroundResource(R.drawable.shape_contact_status);
				break;
				default:
					mTxtStatus.setBackgroundResource(R.drawable.shape_contact_status_offline);
				break;
			}
		}
		// on Image click
		mImgImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = SmartPagerApplication.getInstance().getApplicationContext();
				String id = contactRecipient.getId();
				String smartPagerId = contactRecipient.getSmartPagerId();
				Intent intent = new Intent(context, ContactDetailsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(GroupContactItem.id.name(), id);
				intent.putExtra(GroupContactItem.contact_smart_pager_id_or_group_id.name(), smartPagerId);
				context.startActivity(intent);
			}
		});
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
		
		return R.layout.item_chat_recipient;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
