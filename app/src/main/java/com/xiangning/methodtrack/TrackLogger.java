package com.xiangning.methodtrack;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class TrackLogger {

    private static final String TAG = "TrackLogger";

    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS ");

    public static void log(String info) {
        log("", info, null);
    }

    public static void log(String info, Throwable throwable) {
        log("", info, throwable);
    }

    public static void log(String tag, String info, Throwable throwable) {
        // 先用log打印一次
        Log.w(TAG, tag + ": " + info, throwable);
        // 输出到文件
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFile(), true));
            writer.write(sDateFormat.format(System.currentTimeMillis()));
            writer.write(formatLog(tag, info, throwable));
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            Log.e(TAG, "write error!", e);
        }
    }

    public static String formatLog(String tag, String info, Throwable throwable) {
        StringWriter writer = new StringWriter();
        if (tag != null) {
            writer.write(tag + ": ");
        }
        if (info != null) {
            writer.write(info);
        }
        if (throwable != null) {
            writer.write("\n");
            writer.write(Log.getStackTraceString(throwable));
        }
        return writer.toString();
    }

    private static File getLogFile() {
        File file = new File("/sdcard/MethodTrack/log/"
            + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log");
        if (!file.exists() && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return file;
    }
}
