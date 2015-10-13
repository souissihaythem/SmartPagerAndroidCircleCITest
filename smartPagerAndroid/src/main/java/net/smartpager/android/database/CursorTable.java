package net.smartpager.android.database;

import android.net.Uri;
import android.support.v4.content.CursorLoader;

public abstract class CursorTable {

	public abstract Uri getUri();

	public abstract CursorLoader getLoader();
	public abstract CursorLoader getFilterLoader(String... filterparams);
//	public abstract Object[] getEnumFields();
//
//	public abstract ContentValues getFullValues(Bundle bundle);
//	
//	protected String[] getFullProjection() {
//		Object[] values = getEnumFields();
//		int length = values.length;
//		String[] projection = new String[length];
//		for (int i = 0; i != length; i++) {
//			projection[i] = values[i].toString();
//		}
//		return projection;
//	}
//
//	protected int delete(Context context, String queryCondition, String[] arguments) {
//		ContentResolver resolver = context.getContentResolver();
//		return resolver.delete(getUri(), queryCondition, arguments);
//	}
//
//	public int deleteRecord(Context context, int id) {
//		String queryCondition = "_id = ?";
//		return delete(context, queryCondition, new String[] { String.valueOf(id) });
//	}
//
//	public int update(Context context, Bundle bundle, int id) {
//		ContentResolver resolver = context.getContentResolver();
//		String queryCondition = "_id = ?";
//		return resolver.update(getUri(), getFullValues(bundle), queryCondition, new String[] { String.valueOf(id) });
//	}
}
