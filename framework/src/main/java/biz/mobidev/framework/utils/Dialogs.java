package biz.mobidev.framework.utils;

import android.app.AlertDialog;
import android.content.Context;

public class Dialogs {
	public static AlertDialog GetSimplDialog(Context context, String title, String message, String buttonName){
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context)
		.setTitle(title).setMessage(message)
		.setNeutralButton(buttonName, null);
		return dialogBuilder.create();
	}
	

}
