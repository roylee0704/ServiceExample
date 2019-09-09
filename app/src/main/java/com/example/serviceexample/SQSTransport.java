package com.example.serviceexample;

import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQSTransport {
    private static String accessKey = "ACCESS";
    private static String secretKey = "SECRET";
    private static final String TAG = "SQSTransport";

    private static final ExecutorService deleteThreadPool = Executors.newFixedThreadPool(1);

    private static AmazonSQSClient getClient() {
        StaticCredentialsProvider creds = new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        return new AmazonSQSClient(creds);
    }


    private static String recvQueueURL(String queueName) {
        return "https://sqs.ap-southeast-1.amazonaws.com/439299810195/"+ queueName;
    }


    public static List<String> receiveMessages(String queueName) throws Exception {
        final AmazonSQSClient client = getClient();

        Log.d(TAG, "Queue URL:"+recvQueueURL(queueName));

        ReceiveMessageRequest request = new ReceiveMessageRequest(recvQueueURL(queueName));
        request.setWaitTimeSeconds(20);    // long polling period.
        request.setMaxNumberOfMessages(1);
        ReceiveMessageResult result = client.receiveMessage(request);

        final List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<>();

        ArrayList<String> messages = new ArrayList<String>();
        for (Message m : result.getMessages()) {
            deleteEntries.add(new DeleteMessageBatchRequestEntry(m.getMessageId(), m.getReceiptHandle()));
            Log.d(TAG, m.getBody());
            messages.add(m.getBody());
        }

        if (!deleteEntries.isEmpty()) {
            deleteThreadPool.submit(() -> {
                try {
                    DeleteMessageBatchRequest deleteRequest = new DeleteMessageBatchRequest(recvQueueURL(queueName))
                            .withEntries(deleteEntries);
                    client.deleteMessageBatch(deleteRequest);
                } catch (Exception e) {
                    Log.e(TAG, "failed to delete messages: " + e.getMessage());
                }
            });
        }

        return messages;
    }

}
