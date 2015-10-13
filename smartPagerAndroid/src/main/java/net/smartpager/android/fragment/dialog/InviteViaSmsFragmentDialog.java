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

public class InviteViaSmsFragmentDialog extends DialogFragment {

	public interface InviteViaSmsDialogListener {
		public void onSendSmsInviteClick();
		public void onCancelSmsInviteClick();
	}
	private InviteViaSmsDialogListener mListener; 

	public static InviteViaSmsFragmentDialog newInstance(boolean cancelable) {
		
		InviteViaSmsFragmentDialog fd = new InviteViaSmsFragmentDialog();
		fd.setCancelable(cancelable);
		return fd;
	}
	
	@Override
	public void onAttach(Activity activity) {
		
		if (activity instanceof InviteViaSmsDialogListener) {
			mListener = (InviteViaSmsDialogListener) activity;
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
		
		View rootView = inflater.inflate(R.layout.dialog_layout_invite_via_sms, container, false);
		
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText(R.string.add_a_new_contact);
		
		TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
		messageTextView.setText(R.string.dialog_msg_you_can_send_invite);
		
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setText(R.string.dialog_btn_cancel);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onCancelSmsInviteClick();
				}
				dismiss(); 
			}
		});
		
		Button btnNegative = (Button) rootView.findViewById(R.id.dialog_no_button);
		btnNegative.setText(R.string.dialog_btn_send_invite);
		btnNegative.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onSendSmsInviteClick();
				}
				dismiss(); 
			}
		});
		
		return rootView;
	}
}
