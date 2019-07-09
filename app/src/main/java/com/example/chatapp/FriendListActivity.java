package com.example.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.models.Profile;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListActivity extends AppCompatActivity {

    private static String TAG = "Friend List Activity";

    private Toolbar mToolbar;
    private RecyclerView FindFriendsRecyclerList;
    private FirebaseRecyclerAdapter<Profile, FindFriendViewHolder> adapter;
    private DatabaseReference usersRef;
    private Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_firends);

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendsRecyclerList = findViewById(R.id.find_firends_recycle_list);
        FindFriendsRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_firends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }


    private void loadProfileCache() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(getString(R.string.shared_pref_profile_key), null);
        Gson gson = new Gson();
        userProfile = gson.fromJson(json, Profile.class);
        Log.d(TAG, "Updated profile from cache!");
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadProfileCache();

        Query query = usersRef
                .orderByChild(getString(R.string.dbnode_users_courseId))
                .equalTo(userProfile.courseId);

        FirebaseRecyclerOptions<Profile> options =
                new FirebaseRecyclerOptions.Builder<Profile>()
                .setQuery(query, Profile.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Profile, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder
                            (@NonNull final FindFriendViewHolder holder, final int position, @NonNull Profile model) {
                        holder.userName.setText(model.name);
                        holder.userId.setText(model.userId);
                        final String imageUrl = model.imageUrl;

                        if (!imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String selectedUid = getRef(position).getKey();
                                Intent profileIntent = new Intent (FriendListActivity.this, ProfileActivity.class);
                                profileIntent.putExtra(getString(R.string.selected_profile_uid), selectedUid);
                                profileIntent.putExtra(getString(R.string.selected_profile_image_url), imageUrl);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.user_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        FindFriendsRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{
        TextView userName;
        TextView userId;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.users_profile_name);
            userId = itemView.findViewById(R.id.user_profile_ID);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
