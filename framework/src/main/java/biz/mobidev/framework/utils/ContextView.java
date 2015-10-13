package biz.mobidev.framework.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

public class ContextView {
	private View mRootView;
	private Object mTargetOject;
	public ContextView(Activity activity) {
		this.mRootView = activity.getWindow().getDecorView();
		mTargetOject =activity;
	}
	
	public ContextView(Dialog dialog) {
		this.mRootView = dialog.getWindow().getDecorView();
		mTargetOject = dialog;
	}
	
	public Context getContext()
	{
		return mRootView.getContext();
	}
	public View getRootView() {
		return mRootView;
	}
	
	public Object getTargetObject() {
		return mTargetOject;
	}
}
