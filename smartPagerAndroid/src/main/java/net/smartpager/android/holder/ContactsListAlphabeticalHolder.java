package net.smartpager.android.holder;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.smartpager.android.R;
import net.smartpager.android.model.AlphabeticalCaption;

import biz.mobidev.framework.adapters.holderadapter.IHolder;

public class ContactsListAlphabeticalHolder implements IHolder {

	private TextView mCaptionTextView;

	@Override
	public IHolder createHolder(View view) {
		mCaptionTextView = (TextView) view.findViewById(R.id.alphabet_caption_textview);
		return this;
	}

	@Override
	public void setData(Object source) {		
		AlphabeticalCaption caption = (AlphabeticalCaption) source;
		mCaptionTextView.setText(caption.getName());
	}

	@Override
	public ViewGroup getInflateView(Context context) {		
		return new LinearLayout(context);
	}

	@Override
	public int getLayoutResorsID() {
		return R.layout.item_alphabetical_list;
	}

	@Override
	public void preSetData(Bundle params) {

	}

	@Override
	public void setListeners(Object... listeners) {
		
	}

}
