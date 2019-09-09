package com.example.serviceexample;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.Settings;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;

import androidx.annotation.Nullable;



public class MyService extends Service {
    private MediaPlayer player;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player = MediaPlayer.create(this, Settings.System.DEFAULT_RINGTONE_URI);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        player.setLooping(true);
        player.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        player.stop();
    }

    public void addItemSQS() {
       // Timber.i( "Pull: addItemSQS: ");

        AWSCredentials awsCredentials=new BasicAWSCredentials("ACCESS_KEY" ,"SECRET_KEY");

        AmazonSQSAsyncClient sqs = new AmazonSQSAsyncClient(awsCredentials);
        sqs.setRegion(Region.getRegion("ap-southeast-1"));
        SendMessageRequest req = new SendMessageRequest("https://sqs.ap-southeast-1.amazonaws.com/ACCOUNT_ID/QUEUE_NAME", "hello world");
        sqs.sendMessageAsync(req, new AsyncHandler<SendMessageRequest, SendMessageResult>() {
            @Override
            public void onSuccess(SendMessageRequest request, SendMessageResult sendMessageResult) {
                //Timber.i( "Pull: SQS result: " + sendMessageResult.getMessageId());
            }

            @Override
            public void onError(Exception e) {
             //   Timber.e( "Pull: SQS error: ", e);
            }
        });
    }
}
