package net.smartpager.android.fragment.dialog;

import net.smartpager.android.consts.FragmentDialogTag;

public interface OnDialogDoneListener {
	public void onDialogDone(FragmentDialogTag tag, String data);
	public void onDialogNo(FragmentDialogTag tag, String data);
}
