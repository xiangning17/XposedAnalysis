package com.xiangning.methodtrack;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TrackedProvider extends ContentProvider {

    public static final String AUTHORITY = "com.xiangning.tracked";
    public static final String METHOD_GET_TRACKED_LIST_FOR_PKG = "METHOD_GET_TRACKED_LIST_FOR_PKG";
    public static final String METHOD_SAVE_LOG = "METHOD_SAVE_LOG";

    public TrackedProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Nullable
    @Override
    public Bundle call(@NonNull String method, @Nullable String arg, @Nullable Bundle extras) {
        if (METHOD_GET_TRACKED_LIST_FOR_PKG.equals(method)) {
            ArrayList<TrackedMethod> trackedListForPkg = new ArrayList<>();
            String callerPkg = arg;
            for (TrackedMethod tracked : TrackedManager.getInstance(getContext()).getTrackedList()) {
                if (TextUtils.equals(tracked.getPackageName(), callerPkg)) {
                    trackedListForPkg.add(tracked);
                }
            }

            Bundle result = new Bundle();
            result.putString(TrackedManager.PREF_KEY_TRACKED_LIST, TrackedManager.toJson(trackedListForPkg));
            return result;
        } else if (METHOD_SAVE_LOG.equals(method)) {
            TrackLogger.log(arg);
        }

        return null;
    }
}
