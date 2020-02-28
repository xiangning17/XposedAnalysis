package com.xiangning.methodtrack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 守护服务，避免进程被结束
 */
public class DemonService extends Service {
    public DemonService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
