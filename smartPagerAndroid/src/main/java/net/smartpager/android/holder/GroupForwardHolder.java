package net.smartpager.android.holder;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rey.material.widget.RadioButton;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class GroupForwardHolder implements IHolder {

	private TextView mTxtname;
	private TextView mTxtStatus;
	private ImageView mImgImage;
	private RadioButton mCheckBox;
	private String mSelectedID = "";
	
	@Override
	public IHolder createHolder(View view) {

		mTxtname = (TextView) view
				.findViewById(R.id.item_to_contact_name_textView);
		mTxtStatus = (TextView) view.findViewById(R.id.item_to_status_textView);
		mImgImage = (ImageView) view.findViewById(R.id.item_to_image_imageView);
		mCheckBox = (RadioButton) view.findViewById(R.id.item_to_checkbox);
		mCheckBox.setClickable(false);

		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setData(Object source) {
		final Recipient groupRecipient = (Recipient) source;	
		mCheckBox.setChecked(groupRecipient.getId().equalsIgnoreCase(mSelectedID));					
		mTxtname.setText(groupRecipient.getName());
		// set image
		mImgImage.setImageResource(R.drawable.contacts_ic_group_static);
		mImgImage.setBackgroundDrawable(null);
		mImgImage.setScaleType(ScaleType.FIT_CENTER);
		// hide status
		mTxtStatus.setVisibility(View.INVISIBLE);
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
