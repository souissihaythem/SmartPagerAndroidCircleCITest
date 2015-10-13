package net.smartpager.android.holder;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.rey.material.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.holder.ContactHolder.OnRecipientCheckedChangeListener;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class GroupHolder implements IHolder {

    View itemBG;
	private TextView mTxtname;
	private TextView mStatusView;
	private ImageView mImageView;
	private CheckBox mCheckBox;

	OnRecipientCheckedChangeListener mCheckedListener;
	
	@Override
	public IHolder createHolder(View view) {
        itemBG = view;
		mTxtname = (TextView) view
				.findViewById(R.id.item_to_contact_name_textView);
		mStatusView = (TextView) view.findViewById(R.id.item_to_status_textView);
		mImageView = (ImageView) view.findViewById(R.id.item_to_image_imageView);
		mCheckBox = (CheckBox) view.findViewById(R.id.item_to_checkbox);

		return this;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setData(Object source) {
		final Recipient groupRecipient = (Recipient) source;	
		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				groupRecipient.setSelected(isChecked);
				if (mCheckedListener != null) {
					mCheckedListener.onRecipientCheckedChange(groupRecipient);
				}
			}
		});
		mCheckBox.setChecked(groupRecipient.isSelected());
        itemBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCheckBox.setChecked(!mCheckBox.isChecked());
            }
        });
		mTxtname.setText(groupRecipient.getName());
		// set image
		if (groupRecipient.getGroupType() != null && groupRecipient.getGroupType().equalsIgnoreCase("SCHEDULE")) {
			mImageView.setImageResource(R.drawable.contacts_ic_schedule_group_static);
		} else {
			mImageView.setImageResource(R.drawable.contacts_ic_group_static);
		}
		mImageView.setBackgroundDrawable(null);
		mImageView.setScaleType(ScaleType.FIT_CENTER);
		// hide status
		mStatusView.setVisibility(View.INVISIBLE);
	}

	@Override
	public void setListeners(Object... listeners) {
		if (listeners != null){
			mCheckedListener = (OnRecipientCheckedChangeListener) listeners[0];
		}

	}

	@Override
	public ViewGroup getInflateView(Context context) {
		return new RelativeLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_contact_to_message;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
