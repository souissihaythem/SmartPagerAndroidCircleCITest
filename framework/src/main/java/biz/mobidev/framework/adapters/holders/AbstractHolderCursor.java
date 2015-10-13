package biz.mobidev.framework.adapters.holders;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface AbstractHolderCursor {

	/**
	 * Callback method for inflate item layout. Must return inflated ViewGroup object.
	 * <br>
	 * Called when need inflate new view to adapter.
	 * <br>
	 * <i>Call order 1</i>
	 * @param inflater
	 * @param context
	 * @return Inflated view
	 */
	public ViewGroup inflateLayout(LayoutInflater inflater, Context  context);
	/**
	 * Callback method for init views from item
	 * @param view
	 * @return
	 */
	public AbstractHolderCursor createHolder(View view);
	/**
	 * Callback method calls before method setData bundle may contains adapter mode variables
	 * @param params
	 */
	public void preSetData(Bundle params);
	/**
	 * Callback method for set data to views from cursor
	 * @param cursor
	 */
	public void setData(Cursor cursor);
	/**
	 * 
	 * @param listeners
	 */
	public void setListeners(Object... listeners);
}
