package net.smartpager.android;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmitriy on 1/9/14.
 */
public final class SPContentProviderHelper {
    public static final void applyBatches (Context context, List<ContentProviderOperation.Builder> builders) throws RemoteException, OperationApplicationException {
        ContentResolver contentResolver = context.getContentResolver();
        if(builders != null)
        {
            ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
            for(ContentProviderOperation.Builder builder : builders)
            {
                operations.add(builder.build());
                contentResolver.applyBatch(SmartPagerContentProvider.AUTHORITY, operations);
            }
        }
    }

    public static final void applyBatches (Context context, List<ContentProviderOperation.Builder> builders, Uri... notifyUris) throws RemoteException, OperationApplicationException {
        applyBatches(context, builders);
        ContentResolver contentResolver = context.getContentResolver();
        if(notifyUris != null)
        {
            for(Uri notifyUri : notifyUris)
            {
                contentResolver.notifyChange(notifyUri, null);
            }
        }
    }
}
