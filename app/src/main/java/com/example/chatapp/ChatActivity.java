package com.example.chatapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.models.Messages;
import com.example.chatapp.models.Profile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverID, messageReceiverName, messageReceiverImage;
    private static int ATTACHMENT_REQUEST = 5247;
    private String formattedAttachmentType;
    private static String TAG = "Chat Activity";

    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private Profile currentUser;
    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    private ImageButton SendMessageButton, SendFilesButton;
    private EditText MessageInputText;

    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter messageAdapter;
    private RecyclerView userMessageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeFields();
        loadProfileCache();
        configureAttachmentButton();
    }

    private void initializeFields() {
        ChatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_IMAGE);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        SendMessageButton = findViewById(R.id.send_message_btn);
        SendFilesButton = findViewById(R.id.send_files_btn);
        MessageInputText = findViewById(R.id.input_message);

        userMessageList = findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfileActivity();
            }
        });
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    private void loadProfileCache() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(getString(R.string.shared_pref_profile_key), null);
        Gson gson = new Gson();
        currentUser = gson.fromJson(json, Profile.class);
        Log.d(TAG, "Updated profile from cache!");
    }

    private FirebaseRecyclerAdapter<Messages, RecyclerView.ViewHolder> getConfiguredAdapter() {
        Query query = databaseRef
                .child(getString(R.string.dbnode_messages))
                .child(currentUser.uid)
                .child(messageReceiverID);

        FirebaseRecyclerOptions<Messages> options = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(query, Messages.class)
                .build();

        FirebaseRecyclerAdapter<Messages, RecyclerView.ViewHolder> adapter = new FirebaseRecyclerAdapter
                <Messages, RecyclerView.ViewHolder>(options) {

            private static final int SENDER_MESSAGE_VH = 1;
            private static final int RECIPIENT_MESSAGE_VH = 2;

            @Override
            public int getItemViewType(int position) {
                Messages message = getItem(position);
                Log.d(TAG, "Item retrieved.");
                return message.isSentBy(currentUser.uid) ? SENDER_MESSAGE_VH : RECIPIENT_MESSAGE_VH;
            }

            @Override
            protected void onBindViewHolder
                    (@NonNull RecyclerView.ViewHolder holder, int position, @NonNull Messages model) {
                switch (holder.getItemViewType()) {
                    case SENDER_MESSAGE_VH:
                        SenderMessageHolder senderMessageHolder = (SenderMessageHolder) holder;

                        senderMessageHolder.senderProfileImage.setVisibility(View.GONE);
                        senderMessageHolder.senderImage.setVisibility(View.GONE);
                        senderMessageHolder.senderText.setVisibility(View.GONE);

                        if (!currentUser.imageUrl.isEmpty()) {
                            senderMessageHolder.senderProfileImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(currentUser.imageUrl)
                                    .into(senderMessageHolder.senderProfileImage);
                        }

                        if (model.type.equals(getString(R.string.storage_type_image))) {
                            Log.d(TAG, "Got new image");
                            if (!model.message.isEmpty()) {
                                senderMessageHolder.senderImage.setVisibility(View.VISIBLE);
                                Picasso.get().load(model.message).into(senderMessageHolder.senderImage);
                            }
                        } else if (model.type.equals(getString(R.string.storage_type_zip))){
                            Log.d(TAG, "Got new zip");
                            senderMessageHolder.senderText.setVisibility(View.VISIBLE);
                            senderMessageHolder.senderText.setText(model.message);

                            //TODO: On Click Implementation
                        } else if (model.type.equals(getString(R.string.storage_type_text))) {
                            Log.d(TAG, "Got new text");
                            senderMessageHolder.senderText.setVisibility(View.VISIBLE);
                            senderMessageHolder.senderText.setText(model.message);
                        }
                        break;

                    case RECIPIENT_MESSAGE_VH:
                        RecipientMessageHolder recipientMessageHolder = (RecipientMessageHolder) holder;

                        recipientMessageHolder.recipientProfileImage.setVisibility(View.GONE);
                        recipientMessageHolder.recipientImage.setVisibility(View.GONE);
                        recipientMessageHolder.recipientText.setVisibility(View.GONE);

                        if (!messageReceiverImage.isEmpty()) {
                            recipientMessageHolder.recipientProfileImage.setVisibility(View.VISIBLE);
                            Picasso.get().load(messageReceiverImage)
                                    .into(recipientMessageHolder.recipientProfileImage);
                        }

                        if (model.type.equals(getString(R.string.storage_type_image))) {
                            Log.d(TAG, "GOT NEW IMAGE TYPE");
                            if (!model.message.isEmpty()) {
                                recipientMessageHolder.recipientImage.setVisibility(View.VISIBLE);
                                Picasso.get().load(model.message).into(recipientMessageHolder.recipientImage);
                            }
                        } else if (model.type.equals(getString(R.string.storage_type_zip))){
                            Log.d(TAG, "Got new zip type");
                            recipientMessageHolder.recipientText.setVisibility(View.VISIBLE);
                            recipientMessageHolder.recipientText.setText(model.message);

                            //TODO: On Click Implementation
                        } else if (model.type.equals(getString(R.string.storage_type_text))) {
                            Log.d(TAG, "Got new Text");
                            recipientMessageHolder.recipientText.setVisibility(View.VISIBLE);
                            recipientMessageHolder.recipientText.setText(model.message);
                        }
                        break;

                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                switch (viewType) {
                    case SENDER_MESSAGE_VH:
                        View senderView = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.chat_sender_vh, viewGroup, false);
                        return new SenderMessageHolder(senderView);

                    case RECIPIENT_MESSAGE_VH:
                        View recipientView = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.chat_recipient_vh, viewGroup, false);
                        return new RecipientMessageHolder(recipientView);

                    default:
                        return null;
                }
            }
        };

        return adapter;
    }

    private static class SenderMessageHolder extends RecyclerView.ViewHolder {
        private ImageView senderProfileImage;
        private TextView senderText;
        private ImageView senderImage;

        private SenderMessageHolder(@NonNull View itemView) {
            super(itemView);
            senderProfileImage = itemView.findViewById(R.id.sender_profile_image);
            senderText = itemView.findViewById(R.id.sender_message_text);
            senderImage = itemView.findViewById(R.id.sender_message_image);
        }
    }

    private static class RecipientMessageHolder extends RecyclerView.ViewHolder {
        private ImageView recipientProfileImage;
        private TextView recipientText;
        private ImageView recipientImage;

        private RecipientMessageHolder(@NonNull View itemView) {
            super(itemView);
            recipientProfileImage = itemView.findViewById(R.id.recipient_profile_image);
            recipientText = itemView.findViewById(R.id.recipient_message_text);
            recipientImage = itemView.findViewById(R.id.recipient_message_image);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        messageAdapter.stopListening();
    }

    private void configureAttachmentButton() {
        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "Zip File"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select attachment type");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        switch (i) {
                            case 0:
                                Log.d(TAG, "Setting intent type " + i);
                                intent.setType(getString(R.string.storage_type_image));
                                String[] imageTypes = {"image/png", "image/jpeg"};
                                intent.putExtra(Intent.EXTRA_MIME_TYPES, imageTypes);
                                break;
                            case 1:
                                Log.d(TAG, "Setting intent type " + i);
                                intent.setType(getString(R.string.storage_type_zip));
                                break;
                            default:
                                break;
                        }
                        startActivityForResult(intent, ATTACHMENT_REQUEST);
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ATTACHMENT_REQUEST
                && resultCode == RESULT_OK
                && data != null &&
                data.getData() != null) {
            Uri fileUri = data.getData();
            uploadFileToStorage(fileUri);
        }
    }

    private void uploadFileToStorage(Uri fileUri) {
        String fileType = getContentResolver().getType(fileUri);
        Log.d(TAG, "File type retrieved: " + fileType);

        if (fileType != null) {
            switch (fileType) {
                case "image/png":
                case "image/jpeg":
                    Log.d(TAG, "Image File selected.");
                    formattedAttachmentType = getString(R.string.storage_type_image);
                    storageRef = storageRef.child(getString(R.string.storage_child_image));
                    break;
                case "application/zip":
                    Log.d(TAG, "Zip File selected");
                    formattedAttachmentType = getString(R.string.storage_type_zip);
                    storageRef = storageRef.child(getString(R.string.storage_child_doc));
                    break;
                default:
                    break;
            }
        }

        final ProgressDialog loadingProgress = new ProgressDialog(this);
        loadingProgress.setTitle("Sending File");
        loadingProgress.setMessage("Please wait, we are sending that file...");
        loadingProgress.setCanceledOnTouchOutside(false);
        loadingProgress.show();

        storageRef.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                loadingProgress.dismiss();
                if (task.isSuccessful()) {
                    Log.d(TAG, "File uploaded successfully");

                    task.getResult().getMetadata().getReference().getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d(TAG, "Successfully retrieved download url.");
                            updateFirebaseMessageNode(uri.toString(), formattedAttachmentType);
                        }
                    });
                } else {
                    Log.e(TAG, "Failed to upload file to storage!");
                }
            }
        });
    }

    private void updateFirebaseMessageNode(String content, String formattedType) {
        Map<String, String> dateAndTime = getCurrentDateAndTime();
        DatabaseReference senderDatabaseRef = databaseRef.child(getString(R.string.dbnode_messages))
                .child(currentUser.uid)
                .child(messageReceiverID);
        String messageID = senderDatabaseRef.push().getKey();

        Messages messages = new Messages(currentUser.uid, content, formattedType, messageReceiverID, messageID,
                dateAndTime.get("Time"), dateAndTime.get("Date"), formattedType);

        Map<String, Object> postValues = messages.toMap();
        Map<String, Object> childUpdates = new HashMap<>();

        childUpdates.put(currentUser.uid + "/" + messageReceiverID + "/" + messageID, postValues);
        childUpdates.put(messageReceiverID + "/" + currentUser.uid + "/" + messageID, postValues);

        Log.d(TAG, "Child Updates: " + childUpdates.toString());

        databaseRef.child(getString(R.string.dbnode_messages))
                .updateChildren(childUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Successfully updated children in db.");
                } else {
                    Log.e(TAG, "Failed to update children. " + task.getException().getStackTrace());
                }
            }
        });
    }

    private Map<String, String> getCurrentDateAndTime() {
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");
        Calendar calendar = Calendar.getInstance();

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        dateFormat.setTimeZone(timeZone);
        String date = dateFormat.format(calendar.getTime());

        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        timeFormat.setTimeZone(timeZone);
        String time = timeFormat.format(calendar.getTime());

        Map<String, String> dateAndTime = new HashMap<>();
        dateAndTime.put("Date", date);
        dateAndTime.put("Time", time);
        return dateAndTime;
    }

    private void displayLastSeen() {
        databaseRef.child("Users").child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();
                            if (state.equals(getString(R.string.status_offline))) {
                                userLastSeen.setText("Last seen: " + time + ", " + date);
                            } else {
                                userLastSeen.setText(state);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        userName.setText(messageReceiverName);
        if (!messageReceiverImage.isEmpty()) {
            Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        }

        messageAdapter = getConfiguredAdapter();
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
        messageAdapter.registerAdapterDataObserver(getObserver());
        messageAdapter.startListening();
        displayLastSeen();
    }

    private RecyclerView.AdapterDataObserver getObserver() {
        RecyclerView.AdapterDataObserver observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
//                super.onItemRangeInserted(positionStart, itemCount);
//                int messageCount = messageAdapter.getItemCount();
//                Log.d(TAG, "Message Count " + messageCount);
//
//                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
//
//                if (lastVisiblePosition == -1 ||
//                        (positionStart >= (messageCount - 1)) &&
//                                lastVisiblePosition == (positionStart -1)) {
//                    userMessageList.smoothScrollToPosition(positionStart);
//                }
                userMessageList.smoothScrollToPosition(positionStart);
            }
        };
        return observer;
    }

    /*
    @Override
    protected void onPause() {
        super.onPause();
        databaseRef.removeEventListener(seenListener);
    }
    */

    private void sendMessage() {
        String messageText = MessageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            return;
        } else {
            MessageInputText.getText().clear();
        }

        updateFirebaseMessageNode(messageText, getString(R.string.storage_type_text));
    }

    private void showProfileActivity() {
        Intent miniGameIntent = new Intent(ChatActivity.this, ProfileActivity.class);
        startActivity(miniGameIntent);
    }
}
