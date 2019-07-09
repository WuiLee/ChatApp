package com.example.chatapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.ChatActivity;
import com.example.chatapp.models.Contacts;
import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.EventListener;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private static String TAG = "ChatsFragment";

    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter;
    private String chatterID;
    private ValueEventListener listener;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatsRef, Contacts.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder
                    (@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                chatterID = getRef(position).getKey();
                Log.d(TAG, "On Bind VH called");
                usersRef.child(chatterID).addValueEventListener(getConfiguredListener(holder, chatterID));
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                Log.d(TAG, "On Create VH called");
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display_layout, viewGroup, false);
                return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    private ValueEventListener getConfiguredListener
            (final ChatsViewHolder holder, final String usersIDs) {
        if (listener == null) {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        final String retName = dataSnapshot.child("name").getValue().toString();
                        final String imageUrl = dataSnapshot.child(getString(R.string.dbnode_users_imageUrl))
                                .getValue().toString();

                        if (!imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl)
                                    .placeholder(R.drawable.profile_image)
                                    .into(holder.profileImage);
                        }

                        holder.userName.setText(retName);

                        if (dataSnapshot.child("userState").hasChild("state")){
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                holder.userStatus.setText("online");
                            }
                            else if (state.equals("offline")){
                                holder.userStatus.setText("Last Seen: " + date + " " + time);
                            }
                        }
                        else{
                            holder.userStatus.setText("offline");
                        }


                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", usersIDs);
                                chatIntent.putExtra("visit_user_name", retName);
                                chatIntent.putExtra("visit_image", imageUrl);
                                startActivity(chatIntent);
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
        }
        Log.d(TAG,"Returning a listener.");
        return listener;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listener != null) {
            Log.d(TAG, "Listener not null, removing Listener");
            usersRef.child(chatterID).removeEventListener(listener);
            listener = null;
        } else {
            Log.d(TAG, "Listener is null!");
        }
        adapter.stopListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userStatus, userName;
        ImageView onlineIcon;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.user_profile_ID);
            userName = itemView.findViewById(R.id.users_profile_name);
            onlineIcon = (ImageView) itemView.findViewById(R.id.user_online_status);
        }
    }
}
