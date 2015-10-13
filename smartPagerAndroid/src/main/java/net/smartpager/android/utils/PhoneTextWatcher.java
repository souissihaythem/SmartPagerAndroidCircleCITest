package net.smartpager.android.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class PhoneTextWatcher implements TextWatcher {

	private TextView mTextView;
	
	public PhoneTextWatcher(){  
	}  

	public PhoneTextWatcher(TextView textView){  
		this.mTextView = textView;
	}  
	
	@Override
	public void afterTextChanged(Editable text) {
		mTextView.setText(TelephoneUtils.formatIgnoreLength(text.toString()));  
	}

	@Override
	public void beforeTextChanged(CharSequence s,
            final int start,
            final int count,
            final int after) {
		
	}

	@Override
	public void onTextChanged(final CharSequence s,
            final int start,
            final int before,
            final int count) {
		
	}  

	
}
 