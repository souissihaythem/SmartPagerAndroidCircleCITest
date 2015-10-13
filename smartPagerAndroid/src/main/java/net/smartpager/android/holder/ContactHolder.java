package net.smartpager.android.holder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.rey.material.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.database.ContactCursorTable.ContactStatus;
import net.smartpager.android.model.Recipient;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

@SuppressLint("NewApi")
public class ContactHolder implements IHolder {

	public interface OnRecipientCheckedChangeListener{
		public void onRecipientCheckedChange(Recipient recipient);
	}

    View itemBG;
	TextView nameTextView;
	TextView descriptionTextView;
	TextView statusView;
	ImageView imageView;
	CheckBox checkBox;

	OnRecipientCheckedChangeListener checkedListener;
		
	@Override
	public IHolder createHolder(View view) {
        itemBG = view;
		nameTextView = (TextView) view
				.findViewById(R.id.item_to_contact_name_textView);
		descriptionTextView = (TextView) view
				.findViewById(R.id.item_to_description_textView);
		statusView = (TextView) view.findViewById(R.id.item_to_status_textView);
		imageView = (ImageView) view.findViewById(R.id.item_to_image_imageView);
		checkBox = (CheckBox) view.findViewById(R.id.item_to_checkbox);

		return this;
	}

	@Override
	public void setData(Object source) {
		final Recipient contactRecipient = (Recipient) source;
		checkBox.setClickable(!contactRecipient.isDisabled());			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				contactRecipient.setSelected(isChecked);
				if (checkedListener != null) {
					checkedListener.onRecipientCheckedChange(contactRecipient);
				}
			}
		});
		checkBox.setChecked(contactRecipient.isSelected());
        itemBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });
		nameTextView.setText(contactRecipient.getName());
		descriptionTextView.setText(contactRecipient.getTitle());
		// set image
		SmartPagerApplication
			.getInstance()
			.getImageLoader()
			.displayImage(contactRecipient.getImageUrl(), imageView, R.drawable.chat_avatar_no_image);
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
		if (listeners != null){
			checkedListener = (OnRecipientCheckedChangeListener) listeners[0];
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
