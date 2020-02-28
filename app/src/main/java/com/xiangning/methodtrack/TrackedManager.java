package com.xiangning.methodtrack;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TrackedManager {

    public static final String TAG = "TrackedManager";

    public static final String PREF_FILE_TRACKED = "tracked_method";
    public static final String PREF_KEY_TRACKED_LIST = "tracked_list";

    private static TrackedManager sInstance;
    private static Gson sGson = new Gson();

    public static TrackedManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TrackedManager.class) {
                if (sInstance == null) {
                    sInstance = new TrackedManager(context);
                }
            }
        }

        // 更新context
        sInstance.setContext(context);
        return sInstance;
    }

    public static String toJson(List<TrackedMethod> trackedMethodList) {
        return sGson.toJson(trackedMethodList);
    }

    public static List<TrackedMethod> fromJson(String json) {
        List<TrackedMethod> list = sGson.fromJson(json,
            new TypeToken<List<TrackedMethod>>() {}.getType());
        return list != null ? list : Collections.<TrackedMethod>emptyList();
    }

    public static List<TrackedMethod> getTrackedListForPkg(Context context, String pkg) {
        List<TrackedMethod> trackedList = new ArrayList<>();
        Uri uri = Uri.parse("content://" + TrackedProvider.AUTHORITY);
        Bundle result = context.getContentResolver()
            .call(uri, TrackedProvider.METHOD_GET_TRACKED_LIST_FOR_PKG, pkg, null);
        if (result != null && result.containsKey(PREF_KEY_TRACKED_LIST)) {
            // Log.w(TAG, "getTrackedListForPkg: " + result.getString(PREF_KEY_TRACKED_LIST));
            trackedList.addAll(fromJson(result.getString(PREF_KEY_TRACKED_LIST)));
        }

        return trackedList;
    }


    private boolean mIsAppContext;
    private Context mContext;
    private List<TrackedMethod> mTrackedMethodList;


    private TrackedManager(Context context) {
        setContext(context);
        mTrackedMethodList = new ArrayList<>(loadFromSettings());
    }

    private void setContext(@NonNull Context context) {
        if (!mIsAppContext) {
            mContext = context;
            if (context.getApplicationContext() != null) {
                mContext = context.getApplicationContext();
                mIsAppContext = true;
            }
        }
    }

    public List<TrackedMethod> getTrackedList() {
        // Log.d(TAG, "getTrackedList: " + mTrackedMethodList);
        return mTrackedMethodList;
    }

    public void addTrackedMethod(@NonNull TrackedMethod tracked) {
        updateTrackedMethod(-1, tracked);
    }

    public void updateTrackedMethod(int index, @NonNull TrackedMethod tracked) {
        if (index < 0) {
            mTrackedMethodList.add(tracked);
        } else if (index < mTrackedMethodList.size()) {
            mTrackedMethodList.set(index, tracked);
        } else {
            throw new IndexOutOfBoundsException(String.format("%d out of size %d", index, mTrackedMethodList.size()));
        }

        updateSettings();
    }

    public void removeTrackedMethod(TrackedMethod tracked) {
        mTrackedMethodList.remove(tracked);
        updateSettings();
    }

    private List<TrackedMethod> loadFromSettings() {
        String settings = mContext.getSharedPreferences(PREF_FILE_TRACKED, Context.MODE_PRIVATE)
            .getString(PREF_KEY_TRACKED_LIST, null);
        List<TrackedMethod> list = fromJson(settings);
        Log.d(TAG, "loadFromSettings: " + list);
        return list;
    }

    private void updateSettings() {
        Log.d(TAG, "updateSettings: " + mTrackedMethodList);
        mContext.getSharedPreferences(PREF_FILE_TRACKED, Context.MODE_PRIVATE)
            .edit()
            .putString(PREF_KEY_TRACKED_LIST, toJson(mTrackedMethodList))
            .apply();
    }

}
