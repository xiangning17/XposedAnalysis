package com.xiangning.methodtrack;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedHook extends XC_MethodHook implements IXposedHookLoadPackage {

    private static final String TAG = "XposedHook";

    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
//        Application application = (Application) param.thisObject;
//        setupMethodHook(application);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Log.i(TAG, "handleLoadPackage: pkg = " + loadPackageParam.packageName + ", process = " + loadPackageParam.processName);
        if (!"android".equals(loadPackageParam.packageName)) {
            return;
        }

        XposedHelpers.findAndHookMethod(
                "android.app.ContextImpl", loadPackageParam.classLoader,
                "checkPermission",
                String.class, int.class, int.class,
                this
        );

//        XposedHelpers.findAndHookMethod("android.app.Application",
//            loadPackageParam.classLoader, "attach", Context.class, this);
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if ("android.permission.STATUS_BAR".equals(param.args[0])) {
            Log.i(TAG, "grant status bar permission for uid: " + param.args[1]);
            param.setResult(PackageManager.PERMISSION_GRANTED);
        }
    }

    private void setupMethodHook(final Context context) {
        for (final TrackedMethod tracked : TrackedManager.getTrackedListForPkg(context, context.getPackageName())) {
            if (!tracked.isValid() || !tracked.getPackageName().equals(context.getPackageName())) {
                continue;
            }

            Log.i(TAG, "handleLoadPackage! [" + context.getPackageName() + "] " + tracked);
            Class<?> clazz;

            try {
                clazz = XposedHelpers.findClass(tracked.getClassName(), context.getClassLoader());
            } catch (XposedHelpers.ClassNotFoundError e) {
                Log.w(TAG, "handleLoadPackage class not found: " + tracked.getClassName(), e);
                continue;
            }

            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    saveHookMethodLog(context, "[" + tracked.getPackageName() + "] "
                            + tracked.getClassName() + "." + tracked.getMethodName()
                            + "(" + TextUtils.join(", ",
                        param.args != null ? param.args : new Object[0]) + ")",
                        new Throwable());
                }
            };

            if (TextUtils.isEmpty(tracked.getMethodName())) {
                for (Constructor<?> constructor : clazz.getConstructors()) {
                    if (tracked.matchMethodParamTypes(constructor.getParameterTypes())) {
                        constructor.setAccessible(true);
                        XposedBridge.hookMethod(constructor, hook);
                    }
                }
            } else {
                for (Method declaredMethod : clazz.getDeclaredMethods()) {
                    if (TextUtils.equals(declaredMethod.getName(), tracked.getMethodName())
                        && tracked.matchMethodParamTypes(declaredMethod.getParameterTypes())) {
                        declaredMethod.setAccessible(true);
                        XposedBridge.hookMethod(declaredMethod, hook);
                    }
                }
            }

        }
    }

    private void saveHookMethodLog(@NonNull Context context, String info, Throwable throwable) {
        Uri uri = Uri.parse("content://" + TrackedProvider.AUTHORITY);
        context.getContentResolver().call(uri, TrackedProvider.METHOD_SAVE_LOG,
                TrackLogger.formatLog("MethodHook", info, throwable), null);
    }

}
