package com.example.chatapp.services;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMessaging";

    @Override
    // Called when the FCM server deletes pending messages.
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    // Called when a message is received.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String dataPayload = "";
        try {
            dataPayload = remoteMessage.getData().toString();
        } catch (NullPointerException e) {
            Log.e(TAG, "Null Pointer Exception: " + e.getMessage());
        }

        Log.d(TAG, "Notification payload: " + dataPayload);

        String dataType = remoteMessage.getData().get(getString(R.string.fcm_data_type));
        try {
            if (dataType.equals(getString(R.string.fcm_data_type_direct_message))) {
                Log.d(TAG, "Incoming new message");
                String messageId = remoteMessage.getData().get(getString(R.string.fcm_data_message_id));
                String title = remoteMessage.getData().get(getString(R.string.fcm_data_title));
                String messageBody = remoteMessage.getData().get(getString(R.string.fcm_data_message_body));
                sendMessageNotification(messageId, title, messageBody);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Null Pointer Exception: " + e.getMessage());
        }

    }

    private void sendMessageNotification(String messageId, String title, String messageBody) {
        Log.d(TAG, "Building a notification");
        int notificationId = buildNotificationId(messageId);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                getString(R.string.notification_channel_id));

//        Intent i = new Intent(this, MainActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent p = PendingIntent.getActivity(
//                this,
//                0,
//                i,
//                0);
        builder.setSmallIcon(R.drawable.profile_icon)
        .setColor(Color.BLUE)
        .setContentTitle(title)
        .setContentText(messageBody);

        NotificationManager n = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        n.notify(notificationId, builder.build());
    }

    private int buildNotificationId(String messageId) {
        Log.d(TAG, "Building a notification id");

        int notificationId = 0;
        for (int i = 0; i < 8; i++) {
            notificationId += messageId.charAt(0);
        }

        Log.d(TAG, "Message id: " + messageId);
        Log.d(TAG, "Built notification id: " + notificationId);
        return notificationId;
    }

    // Called when an upstream message is sent to the server.
    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    // Called when a new token for the default Firebase project is generated.
    // This is invoked after app install when a token is first generated, and again if the token changes.
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG, "Refreshed token: " + s);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(getString(R.string.dbnode_users))
                .child(getString(R.string.dbnode_users_uid))
                .child(getString(R.string.dbnode_users_userToken))
                .setValue(s)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Successfully stored new token");
                        } else {
                            Log.w(TAG, "Failed to store new token");
                        }
                    }
                });
    }

    // Called when there was an error sending an upstream message.
    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
    }
}
