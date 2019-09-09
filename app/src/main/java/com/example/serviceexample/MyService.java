package com.example.serviceexample;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;



public class MyService extends Service {
    private SQSPoller sqsPoller;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sqsPoller = new SQSPoller();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sqsPoller.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sqsPoller.stop();
    }
}
