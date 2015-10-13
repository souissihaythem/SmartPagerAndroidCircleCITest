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

public class InviteContRecievedFragmentDialog extends DialogFragment {

	public interface InviteContRecievedDialogListener {
		public void onConfirmContactRequestClick();
		public void onCancelContactRequestClick();
	}
	private InviteContRecievedDialogListener mListener;
	private ImageView mPhotoImageView;

	public static InviteContRecievedFragmentDialog newInstance(String name, String imageUri, boolean cancelable) {
		
		InviteContRecievedFragmentDialog fd = new InviteContRecievedFragmentDialog();
		
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.contactFullName.name(), name);
		bundle.putString(BundleKey.imageUri.name(), imageUri);
		
		fd.setArguments(bundle);
		fd.setCancelable(cancelable);
		return fd;
	}
	
	private void getProfilePicture(String imageUri) {		
		SmartPagerApplication.getInstance().getImageLoader()
				.displayFullImage(imageUri, mPhotoImageView, R.drawable.chat_avatar_no_image);
	}
	
	public void setDialogListener(InviteContRecievedDialogListener listener){
		mListener = listener;
	}
	
	@Override
	public void onAttach(Activity activity) {
		
		if (activity instanceof InviteContRecievedDialogListener) {
			mListener = (InviteContRecievedDialogListener) activity;
		}
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		
		View rootView = inflater.inflate(R.layout.dialog_layout_invite_cont_rcvd, container, false);
		
		mPhotoImageView = (ImageView) rootView.findViewById(R.id.photo_imageView);
		getProfilePicture(getArguments().getString(BundleKey.imageUri.name()));
		
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText(getArguments().getString(BundleKey.contactFullName.name()));
		
		TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
		messageTextView.setText(R.string.dialog_msg_has_invited_you);
		
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setText(R.string.dialog_btn_cancel);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onCancelContactRequestClick();
				}
				dismiss(); 
			}
		});
		
		Button btnNegative = (Button) rootView.findViewById(R.id.dialog_no_button);
		btnNegative.setText(R.string.dialog_btn_confirm);
		btnNegative.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onConfirmContactRequestClick();
				}
				dismiss(); 
			}
		});
		
		return rootView;
	}
}
