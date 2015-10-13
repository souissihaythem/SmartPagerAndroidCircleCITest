package net.smartpager.android.fragment.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;

public class ContactRequestSentFragmentDialog extends DialogFragment {

	public interface ContactRequestSentDialogListener {
		public void onAddAnotherClick();
		public void onDoneClick();
	}
	private ContactRequestSentDialogListener mListener; 

	public static ContactRequestSentFragmentDialog newInstance(String name, boolean cancelable) {
		
		ContactRequestSentFragmentDialog fd = new ContactRequestSentFragmentDialog();
		
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.contactFullName.name(), name);
		
		fd.setArguments(bundle);
		fd.setCancelable(cancelable);
		
		return fd;
	}
	
	@Override
	public void onAttach(Activity activity) {
		
		if (activity instanceof ContactRequestSentDialogListener) {
			mListener = (ContactRequestSentDialogListener) activity;
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
		
		View rootView = inflater.inflate(R.layout.dialog_layout_contact_req_was_sent, container, false);
		
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText(R.string.dialog_title_request);
		
		TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
		messageTextView.setText(R.string.dialog_msg_has_been_sent);
		
		TextView nameTextView = (TextView) rootView.findViewById(R.id.dialog_name_textView);
		nameTextView.setText(getArguments().getString(BundleKey.contactFullName.name()));
		
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setText(R.string.dialog_btn_add_another);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onAddAnotherClick();
				}
				dismiss(); 
			}
		});
		
		Button btnNegative = (Button) rootView.findViewById(R.id.dialog_no_button);
		btnNegative.setText(R.string.dialog_btn_done);
		btnNegative.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onDoneClick();
				}
				dismiss(); 
			}
		});
		
		return rootView;
	}
}
