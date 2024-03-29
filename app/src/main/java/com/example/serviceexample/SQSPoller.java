package com.example.serviceexample;

import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class SQSPoller {
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private static final String TAG = "SQSPoller";

    public SQSPoller() { }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "starting polling ");
                while (true) {
                    if (stopped.get()) {
                        Log.d(TAG, "stopped polling " );
                        return;
                    }
                    try {
                        Log.d(TAG, "read... ");
                        for (String message : SQSTransport.receiveMessages("gobike-biker-notif")) {
                            Log.d(TAG, "message received"+message);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            return;
                        }
                    }
                }
            }
        }).start();
    }

    public void stop() {
        stopped.set(true);
    }

}
