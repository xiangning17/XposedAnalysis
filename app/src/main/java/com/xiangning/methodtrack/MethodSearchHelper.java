package com.xiangning.methodtrack;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

public final class MethodSearchHelper {

    private static final String TAG = "MethodSearchHelper";

    private static List<String> sPackageList;

    private static List<String> sPreloadClasses;

    private static LruCache<String, DexClasses> sPkgClassesCache = new LruCache<>(3);

    private static class DexClasses {
        ClassLoader classLoader;
        List<String> classes;

        public DexClasses(ClassLoader classLoader, List<String> classes) {
            this.classLoader = classLoader;
            this.classes = classes;
        }
    }

    static {
        List<String> classes = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/system/etc/preloaded-classes"));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("[") && !line.startsWith("#")) {
                    classes.add(line);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "static initializer: load preload class failed!", e);
        }
        sPreloadClasses = classes;
        Log.d(TAG, "static initializer: preload classes size = " + classes.size());
    }

    public static List<String> findPackages(@NonNull Context context, String keywords) {
        if (sPackageList == null) {
            List<ApplicationInfo> applicationInfoList = context.getPackageManager().getInstalledApplications(0);
            sPackageList = new ArrayList<>(applicationInfoList.size());
            for (ApplicationInfo info : applicationInfoList) {
                sPackageList.add(info.packageName);
            }

        }

        List<String> list = new ArrayList<>();
        for (String pkg : sPackageList) {
            if (pkg.contains(keywords)) {
                list.add(pkg);
            }
        }

        return list;
    }

    public static List<String> findClasses(@NonNull Context context, String pkg, String keywords) {
        List<String> list = new ArrayList<>();

        // 在预置类中查找
        for (String cls : sPreloadClasses) {
            if (cls.contains(keywords)) {
                list.add(cls);
            }
        }

        DexClasses dexClasses = sPkgClassesCache.get(pkg);
        if (dexClasses == null) {
            dexClasses = loadPkgClasses(context, pkg);
            if (dexClasses != null) {
                sPkgClassesCache.put(pkg, dexClasses);
            }
        }

        // 在应用自身的类中查找
        for (String cls : dexClasses.classes) {
            if (cls.contains(keywords)) {
                list.add(cls);
            }
        }

        return list;
    }

    public static List<Method> findMethods(String pkg, String cls, String keywords) {
        List<Method> list = new ArrayList<>();

        DexClasses dexClasses = sPkgClassesCache.get(pkg);
        if (dexClasses != null) {
            Class clazz = null;
            try {
                clazz = dexClasses.classLoader.loadClass(cls);
                for (Method method : clazz.getDeclaredMethods()) {
                    if ("*".equals(keywords) || method.getName().contains(keywords)) {
                        list.add(method);
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "findMethods: class not found! " + cls, e);
            }
        }
        return list;
    }

    private static DexClasses loadPkgClasses(Context context, String pkg) {
        ApplicationInfo applicationInfo = null;
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "loadPkgClasses: package not found!" + pkg);
            return null;
        }

        List<String> classes = new ArrayList<>();

        String[] sourceDirs = new String[]{applicationInfo.sourceDir};

        ClassLoader classLoader = MethodSearchHelper.class.getClassLoader();
        for (String path : sourceDirs) {
            DexFile dexfile = null;
            try {
                dexfile = new DexFile(path);
                Enumeration<String> entries = dexfile.entries();
                while (entries.hasMoreElements()) {
                    classes.add(entries.nextElement());
                }
                classLoader = new PathClassLoader(path, classLoader);
            } catch (IOException e) {
                Log.e(TAG, "loadPkgClasses error!", e);
            } finally {
                try {
                    if (dexfile != null) {
                        dexfile.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "loadPkgClasses error!", e);
                }
            }
        }

        return new DexClasses(classLoader, classes);
    }


}
