package net.smartpager.android.fragment.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FragmentDialogTag;

public class AlertFragmentDialog extends DialogFragment implements android.content.DialogInterface.OnClickListener {

	private OnDialogDoneListener mDialogDoneListener;
	
	public interface AddContactClickListener {
		public void onAddNowClick(String number);
	}

	public static AlertFragmentDialog newInstance(String title, String message, boolean cancelable) {
		AlertFragmentDialog fragmentDialog = new AlertFragmentDialog();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.dialogTitle.name(), title);
		bundle.putString(BundleKey.dialogMessage.name(), message);
		fragmentDialog.setArguments(bundle);
		fragmentDialog.setCancelable(cancelable);
		return fragmentDialog;
	}
	
	public static AlertFragmentDialog newInstance(String title, String message) {
		return newInstance(title, message, true);
	}
	
	public static AlertFragmentDialog newErrorInstance(String message) {
		return newInstance(SmartPagerApplication.getInstance().getString(R.string.error), message, false);
	}
	
	@Override
	public void onAttach(Activity activity) {
	   if (activity instanceof OnDialogDoneListener){
		   mDialogDoneListener = (OnDialogDoneListener) activity;
	   }
	   super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, 0);
	}	

/*	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new Builder(getActivity());
		Bundle bundle = getArguments();
		builder.setTitle(bundle.getString(BundleKey.dialogTitle.name()));
		builder.setMessage(bundle.getString(BundleKey.dialogMessage.name()));
		builder.setPositiveButton("OK", this);
		return builder.create();
	}*/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
		View rootView = inflater.inflate(R.layout.dialog_layout, container, false);
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText( getArguments().getString(BundleKey.dialogTitle.name()));
		TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
		messageTextView.setText( getArguments().getString(BundleKey.dialogMessage.name()));
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialogDoneListener != null){
					mDialogDoneListener.onDialogDone(FragmentDialogTag.valueOf(getTag()), "");
				}
				dismiss(); 
			}
		});
		return rootView;
	}		
	
	@Override
	public void onClick(DialogInterface arg0, int arg1) {		
		dismiss();
	}

	
}
