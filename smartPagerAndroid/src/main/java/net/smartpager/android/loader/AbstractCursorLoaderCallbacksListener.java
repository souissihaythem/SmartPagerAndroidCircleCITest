package net.smartpager.android.loader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

/**
 * Created by dmitriy on 2/24/14.
 */
public abstract class AbstractCursorLoaderCallbacksListener implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final int CALLBACK_LISTENER_PRIORITY_NONE = -1;
    public static final int CALLBACK_LISTENER_PRIORITY_RELEASE = 0;
    public static final int CALLBACK_LISTENER_PRIORITY_TEST = 1;
    protected LoaderManager.LoaderCallbacks<Cursor> m_nextListener = null;
    protected int m_listenerPriority = CALLBACK_LISTENER_PRIORITY_NONE;

    public final LoaderManager.LoaderCallbacks<Cursor> setNext (LoaderManager.LoaderCallbacks<Cursor> next)
    {
        m_nextListener = next;
        return  m_nextListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_NONE || m_listenerPriority == CALLBACK_LISTENER_PRIORITY_RELEASE)
            return onCreate(i, bundle);
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_TEST && m_nextListener != null)
            return m_nextListener.onCreateLoader(i, bundle);
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_NONE || m_listenerPriority == CALLBACK_LISTENER_PRIORITY_RELEASE)
            onReset(cursorLoader);
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_TEST && m_nextListener != null)
            m_nextListener.onLoaderReset(cursorLoader);
    }

    @Override
    public final void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_NONE || m_listenerPriority == CALLBACK_LISTENER_PRIORITY_RELEASE)
            onFinish(cursorLoader, cursor);
        if(m_listenerPriority == CALLBACK_LISTENER_PRIORITY_TEST && m_nextListener != null)
            m_nextListener.onLoadFinished(cursorLoader, cursor);
    }

    public abstract Loader<Cursor> onCreate (int i, Bundle bundle);
    public abstract void onReset (Loader<Cursor> cursorLoader);
    public abstract void onFinish (Loader<Cursor> cursorLoader, Cursor cursor);
}
