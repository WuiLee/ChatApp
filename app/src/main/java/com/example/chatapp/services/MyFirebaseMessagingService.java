package com.example.chatapp.services;

import android.support.annotation.NonNull;
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
