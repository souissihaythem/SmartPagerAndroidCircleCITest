package net.smartpager.android.holder;


import android.content.Context;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.holder.MessageRecipientHolder.OnDeleteRecipientClickListener;
import net.smartpager.android.model.ImageSource;

import java.io.IOException;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class ImageHolder implements IHolder {

	ImageView mImageView;
	Button mRemoveButton;
	ImageSource mImageSource;
	OnDeleteRecipientClickListener mClickListener;
	@Override
	public IHolder createHolder(View view) {
		mImageView = (ImageView) view.findViewById(R.id.item_imageView);
		mRemoveButton = (Button)view.findViewById(R.id.item_image_remove_button);
		mRemoveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mClickListener.onDeleteImageClick(mImageSource);
				
			}
		});
		return this;
	}

	@Override
	public void setData(Object source) {
		mImageSource = (ImageSource) source;
		SmartPagerApplication.getInstance().getImageLoader().displayImage(mImageSource.getPreviewImageUri().toString(), mImageView, R.drawable.def_pic_no_image);
//		.03
	}

	public static int getOrientation(String str) {
		ExifInterface exif;
		int tag = -1;
		try {
			exif = new ExifInterface(str);
			String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
			tag = Integer.parseInt(exifOrientation);
		} catch (IOException e) {
			return -1;
		} // Since API Level 5
		switch (tag) {
			case ExifInterface.ORIENTATION_NORMAL:
				return 0;
			case ExifInterface.ORIENTATION_ROTATE_180:
				return 180;
			case ExifInterface.ORIENTATION_ROTATE_270:
				return 270;
			case ExifInterface.ORIENTATION_ROTATE_90:
				return 90;
			default:
				return -1;

		}
	}
	
	@Override
	public void setListeners(Object... listeners) {
		mClickListener = (OnDeleteRecipientClickListener) listeners[0];

	}

	@Override
	public ViewGroup getInflateView(Context context) {
		return new RelativeLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_image;
	}

	@Override
	public void preSetData(Bundle params) {

	}

}
