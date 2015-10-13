package net.smartpager.android.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import net.smartpager.android.SmartPagerApplication;
import net.smartpager.android.consts.BundleKey;
import net.smartpager.android.database.CursorTable;

import biz.mobidev.framework.utils.Log;

public abstract class AbstractFragmentList extends Fragment implements LoaderCallbacks<Cursor> {
	protected ListView mList;
	protected BaseAdapter mCursorAdapter;
	protected CursorTable mCursorTable;
    protected boolean m_bWasLoaderStarted = false;

	protected abstract BaseAdapter createAdapter();

	protected abstract CursorTable createTable();

	protected abstract int getFragmentLayout();

	protected abstract int getListViewID();

	protected abstract void initView(View rootView);

	protected abstract int getLoaderID();

	@Override
	public void onAttach(Activity activity) {
		mCursorTable = createTable();
		super.onAttach(activity);
	}

	@Override
	public void onStart() {
		// to refresh data
        if(!m_bWasLoaderStarted)
            SmartPagerApplication.getInstance().restartCursorLoader(getActivity(), getLoaderID(), null, this);
//    		getActivity().getSupportLoaderManager().restartLoader(getLoaderID(), null, this);
        m_bWasLoaderStarted = false;
		super.onResume();
	}

	/*
	 * @Override public void onResume() { getActivity().registerReceiver(mRefreshReceiver, filter); super.onResume(); }
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(getFragmentLayout(), container, false);
		mList = (ListView) rootView.findViewById(getListViewID());
		initView(rootView);
		mCursorAdapter = createAdapter();
		mList.setAdapter(mCursorAdapter);
        SmartPagerApplication.getInstance().initCursorLoader(getActivity(), getLoaderID(), null, this);
//		getActivity().getSupportLoaderManager().initLoader(getLoaderID(), null, this);
        m_bWasLoaderStarted = true;
		return rootView;
	}

	public void filter(String filterString) {
		// filterString
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.filter.name(), filterString);
        SmartPagerApplication.getInstance().restartCursorLoader(getActivity(), getLoaderID(), bundle, this);
//		getActivity().getSupportLoaderManager().restartLoader(getLoaderID(), bundle, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		if (id == getLoaderID()) {
			if (bundle != null && bundle.containsKey(BundleKey.filter.name())) {
                return mCursorTable.getFilterLoader(bundle.getString(BundleKey.filter.name()));
			}
			return mCursorTable.getLoader();
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		if (arg0.getId() == getLoaderID() && cursor != null && !cursor.isClosed()) {
			//((CursorAdapter) mList.getAdapter()).swapCursor(cursor);
			((CursorAdapter) mCursorAdapter).swapCursor(cursor);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
	}
}
