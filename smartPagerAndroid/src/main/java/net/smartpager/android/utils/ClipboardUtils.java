package net.smartpager.android.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class ClipboardUtils {

	// cleanup clip-board content
	public static void cleanUpClipboard(Context context) {
		ClipboardManager cbm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData data = ClipData.newPlainText("", "");
		cbm.setPrimaryClip(data);
	}

	public static void disableCopyPasteOperations(EditText editText) {
		editText.setCustomSelectionActionModeCallback(new ActionMode.Callback() {

			public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
				return false;
			}

			public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
				return false;
			}

			public void onDestroyActionMode(ActionMode actionMode) {}
		});

		editText.setLongClickable(false);
		editText.setTextIsSelectable(false);
	}

}
