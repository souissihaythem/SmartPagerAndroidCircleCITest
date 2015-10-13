package net.smartpager.android.fragment.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.consts.FragmentDialogTag;

public class QuickResponseEditFragmentDialog extends DialogFragment implements android.content.DialogInterface.OnClickListener {

	private OnDialogDoneListener mDialogDoneListener;

	public static QuickResponseEditFragmentDialog newInstance(String title, String message, String responseMsg, boolean cancelable, OnDialogDoneListener listener) {
		QuickResponseEditFragmentDialog fragmentDialog = newInstance(title, message, responseMsg, cancelable);
		fragmentDialog.mDialogDoneListener = listener;
		return fragmentDialog;
	}
	
	public static QuickResponseEditFragmentDialog newInstance(String title, String message, String responseMsg, boolean cancelable) {
		QuickResponseEditFragmentDialog fragmentDialog = new QuickResponseEditFragmentDialog();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.dialogTitle.name(), title);
		bundle.putString(BundleKey.dialogMessage.name(), message);
		bundle.putString(BundleKey.response.name(), responseMsg);
		fragmentDialog.setArguments(bundle);
		fragmentDialog.setCancelable(cancelable);
		return fragmentDialog;
	}
	
	@Override
	public void onAttach(Activity activity) {
	   if (mDialogDoneListener == null && activity instanceof OnDialogDoneListener) {
		   mDialogDoneListener = (OnDialogDoneListener) activity;
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
		
		View rootView = inflater.inflate(R.layout.dialog_quick_resp_edit, container, false);
		
		TextView titleTextView = (TextView) rootView.findViewById(R.id.dialog_title_textView);
		titleTextView.setText( getArguments().getString(BundleKey.dialogTitle.name()));

//        String msg = getArguments().getString(BundleKey.dialogTitle.name());
//        TextView messageTextView = (TextView) rootView.findViewById(R.id.dialog_message_textView);
//        int visibility = (TextUtils.isEmpty(msg) ? View.GONE : View.VISIBLE);
//        messageTextView.setVisibility(visibility);
//        if(!TextUtils.isEmpty(msg))
//           messageTextView.setText(msg);
		
		final EditText editText = (EditText) rootView.findViewById(R.id.dialog_response_edittext);
		editText.setText( getArguments().getString(BundleKey.response.name()));
		editText.setSelection(editText.getText().length());
		
		Button btnPositive = (Button) rootView.findViewById(R.id.dialog_ok_button);
		btnPositive.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialogDoneListener != null) {
					mDialogDoneListener.onDialogDone( FragmentDialogTag.valueOf(getTag()), editText.getText().toString());
				}
				dismiss(); 
			}
		});
		
		Button btnNegative = (Button) rootView.findViewById(R.id.dialog_no_button);
		btnNegative.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDialogDoneListener != null) {
					mDialogDoneListener.onDialogNo(FragmentDialogTag.valueOf(getTag()), "");
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
