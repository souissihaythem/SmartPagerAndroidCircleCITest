package net.smartpager.android.utils;

import android.database.Cursor;

import net.smartpager.android.database.ContactGroupTable.GroupContactItem;

public class CursorTextUtils {

	public static String getTitleDepartmentCompostion(Cursor cursor) {
		
		String result = "";
		
		// get correct title string
		String title = cursor.getString(GroupContactItem.contact_title_or_group_empty.ordinal());
		if (title == null) {
			title = "";
		}
		
		// get correct department string
		String departments = cursor.getString(GroupContactItem.contact_depart_or_group_empty.ordinal());
		if (departments != null) {
			String[] depsArray = departments.split("\n");
			// if more then one department - change string
			if (depsArray.length > 1) {
				departments = "(Multiple Departments)";
			}
		} else {
			departments = "";
		}
		
		// get result string
		if (departments.equals("")) {
			result = title;
		} else {
			if (title.equals("")) {
				result = departments;
			} else {
				result = title + ", " + departments;
			}
		}
		
		return result;
	}

}
