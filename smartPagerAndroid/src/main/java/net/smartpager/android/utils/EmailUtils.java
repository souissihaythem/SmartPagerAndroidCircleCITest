package net.smartpager.android.utils;

import android.content.Context;
import android.content.Intent;

public class EmailUtils {

	public static final String TYPE = "message/rfc822";
	public static final String E_MAIL = "enter email";

	public static void sendEmail(Context ctx, String subject, String body) {
		Intent i = new Intent(Intent.ACTION_SEND);
		// i.setType("text/plain"); //use this line for testing in the emulator
		i.setType(TYPE); // use from live device
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { E_MAIL });
		i.putExtra(Intent.EXTRA_SUBJECT, subject);
		i.putExtra(Intent.EXTRA_TEXT, body);
		ctx.startActivity(Intent.createChooser(i, "Select email application."));
	}

}
