package net.smartpager.android.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;

public class ContactRequestAcceptedFragmentDialog extends DialogFragment {

	public interface ContactRequestAcceptedDialogListener {
		public void onContactRequestAcceptedClick();
	}
	private ContactRequestAcceptedDialogListener mListener;
	private ImageView mPhotoImageView;
	private String mImageUri;

	public static ContactRequestAcceptedFragmentDialog newInstance(String name, String imageUri, boolean cancelable) {
		
		ContactRequestAcceptedFragmentDialog fd = new ContactRequestAcceptedFragmentDialog();
		
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.contactFullName.name(), name);
		bundle.putString(BundleKey.imageUri.name(), imageUri);
		fd.mImageUri = imageUri;
		
		fd.setArguments(bundle);
		fd.setCancelable(cancelable);
		return fd;
	}
	
	private void getProfilePicture(String imageUri) {
		
		SmartPagerApplication.getInstance().getImageLoader()
				.displayImage(imageUri, mPhotoImageView, R.drawable.chat_avatar_no_image);
	}
	
	@Override
	public void onAttach(Activity activity) {
		
		if (activity instanceof ContactRequestAcceptedDialogListener) {
			mListener = (ContactRequestAcceptedDialogListener) activity;
		}
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}
	
	@Override
	public void onResume() {

//		getProfilePicture(getArguments().getString(BundleKey.imageUri.name()));
		super.onResume();
		getProfilePicture(mImageUri);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		
		View rootView = inflater.inflate(R.layout.dialog_layout_contact_request_accepted, container, false);
		
		mPhotoImageView = (ImageView) rootView.findViewById(R.id.photo_imageView);
		
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText(getArguments().getString(BundleKey.contactFullName.name()));
		
		TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
		messageTextView.setText(R.string.dialog_msg_has_accepted_contact);
		
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setText(R.string.dialog_btn_ok);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onContactRequestAcceptedClick();
				}
				dismiss(); 
			}
		});
		
		return rootView;
	}
}
