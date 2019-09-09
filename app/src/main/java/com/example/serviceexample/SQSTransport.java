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
import com.amazonaws.util.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SQSTransport {
    private static String accessKey = "ACCESS";
    private static String secretKey = "SECRET";
    private static final String TAG = "SQSTransport";

    private static final ExecutorService sendThreadPool = Executors.newFixedThreadPool(3);
    private static final ExecutorService deleteThreadPool = Executors.newFixedThreadPool(1);

    private static AmazonSQSClient getClient() {
        StaticCredentialsProvider creds = new StaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey));
        return new AmazonSQSClient(creds);
    }


    private static String recvQueueURL(String pairing) {
        return "https://sqs.ap-southeast-1.amazonaws.com/439299810195/gobike-biker-notif";
    }

//    public static void sendMessage(Pairing pairing, NetworkMessage message) throws TransportException {
//        sendThreadPool.submit(() -> {
//            try {
//                sendMessageJob(pairing, message);
//            } catch (TransportException e) {
//                e.printStackTrace();
//            }
//        });
//    }

//    private static void sendMessageJob(Pairing pairing, NetworkMessage message) throws TransportException {
//        AmazonSQSClient cl    ient = getClient();
//        client.sendMessage(recvQueueURL(pairing), Base64.encodeAsString(message.bytes()));
//    }

    public static List<byte[]> receiveMessages(final String pairing) throws Exception {
        final AmazonSQSClient client = getClient();
        ReceiveMessageRequest request = new ReceiveMessageRequest(recvQueueURL(pairing));
        request.setWaitTimeSeconds(20);
        request.setMaxNumberOfMessages(10);
        ReceiveMessageResult result = client.receiveMessage(request);

        final List<DeleteMessageBatchRequestEntry> deleteEntries = new ArrayList<>();

        ArrayList<byte[]> messages = new ArrayList<byte[]>();
        for (Message m : result.getMessages()) {
            deleteEntries.add(new DeleteMessageBatchRequestEntry(m.getMessageId(), m.getReceiptHandle()));
            try {
                messages.add(Base64.decode(m.getBody()));
            } catch (Exception e) {
                Log.e(TAG, "failed to decode message: " + e.getMessage());
            }
        }

        if (!deleteEntries.isEmpty()) {
            deleteThreadPool.submit(() -> {
                try {
                    DeleteMessageBatchRequest deleteRequest = new DeleteMessageBatchRequest(recvQueueURL(pairing))
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
