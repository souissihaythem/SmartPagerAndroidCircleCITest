package net.smartpager.android.fragment.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import com.rey.material.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.utils.PhoneTextWatcher;

public class AddContactFragmentDialog extends DialogFragment implements OnClickListener {

	public interface AddContactDialogListener {
		public void onAddNowClick(String number);
	}

	private EditText numberEditText;
	private TextView numberFormattedTextView;
	private AddContactDialogListener clickListener;

	public static AddContactFragmentDialog newInstance(AddContactDialogListener clickListener) {
		AddContactFragmentDialog addContactFragmentDialog = new AddContactFragmentDialog();
		addContactFragmentDialog.clickListener = clickListener;
		return addContactFragmentDialog;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setCancelable(true);
		setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.dialog_layout_add_contact, container, false);

		numberEditText = (EditText) rootView.findViewById(R.id.new_contact_number_editText);
		numberFormattedTextView = (TextView) rootView.findViewById(R.id.new_contact_number_formatted_textview);
		numberFormattedTextView.setOnClickListener(onPhoneTextClickListener);
		numberEditText.addTextChangedListener(new PhoneTextWatcher(numberFormattedTextView));
		Button btnPositive = (Button) rootView.findViewById(R.id.contact_accept_button);
		btnPositive.setOnClickListener(this);
		return rootView;
	}

	private OnClickListener onPhoneTextClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) SmartPagerApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(numberEditText, 0);
		}
	};
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onResume() {
		numberEditText.postDelayed(new Runnable() {
			
			@Override
			public void run() {

				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(numberEditText, InputMethodManager.SHOW_FORCED); // .SHOW_FORCED);
				
			}
		}, 100);
		super.onResume();
	}
	
	@Override
	public void onClick(View v) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(numberEditText.getWindowToken(), 0); // .SHOW_FORCED);
		clickListener.onAddNowClick("1"+numberEditText.getText().toString());
		dismiss();
	}
}
