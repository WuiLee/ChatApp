package com.example.chatapp;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chatapp.MiniGame.ArcadeMenuActivity;
import com.example.chatapp.forms.AddPostFormActivity;
import com.example.chatapp.forms.EditProfileFormActivity;
import com.example.chatapp.forms.LoginFormActivity;
import com.example.chatapp.fragments.TabsAccessorAdapter;
import com.example.chatapp.models.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Profile userProfile;

    // User Interface
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadUserProfile();
        createNotificationChannel();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("iChat");

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);
        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfileCache();
    }

    private void updateFCMUserToken() {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Successfully retrieved instance id result.");
                    String token = task.getResult().getToken();
                    Log.d(TAG, "Token: " + token);
                    databaseReference.child(getString(R.string.dbnode_users))
                            .child(userProfile.uid)
                            .child(getString(R.string.dbnode_users_userToken))
                            .setValue(token)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Successfully updated token.");
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearProfileCache();
        updateUserOnlineStatus(getString(R.string.status_offline));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.main_add_post_option:
                showAddPostForm();
                break;
            case R.id.main_view_friends_option:
                showFriendList();
                break;
            case R.id.main_create_group_option:
                showCreateGroupForm();
                break;
            case R.id.main_view_profile_option:
                showProfileMenu();
                break;
            case R.id.main_arcade_option:
                showArcadeMenu();
                break;
            case R.id.main_logout_option:
                clearProfileCache();
                updateUserOnlineStatus(getString(R.string.status_offline));
                firebaseAuth.signOut();
                showLoginForm();
        }

        return true;
    }

    private void loadProfileCache() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(getString(R.string.shared_pref_profile_key), null);
        Gson gson = new Gson();
        userProfile = gson.fromJson(json, Profile.class);
    }

    private void loadUserProfile() {
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            databaseReference.child(getString(R.string.dbnode_users)).child(uid)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(getString(R.string.dbnode_users_uid)).getValue() != null) {
                                userProfile = dataSnapshot.getValue(Profile.class);
                                cacheProfile();
                                updateUserOnlineStatus(getString(R.string.status_online));
                                updateFCMUserToken();
                            } else {
                                showEditProfileForm();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    private void clearProfileCache() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    private void cacheProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userProfile);
        Log.d(TAG, json);
        editor.putString
                (getString(R.string.shared_pref_currentUserUid_key), userProfile.uid);
        editor.putString
                (getString(R.string.shared_pref_profile_key), json);
        editor.apply();
        Log.d(TAG, "Cached Profile");
    }

    private void updateUserOnlineStatus(final String state) {
        if (userProfile == null) {
            return;
        }

        String onlineDate, onlineTime;

        // User last online date
        TimeZone timeZone = TimeZone.getTimeZone("GMT+8");

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, yyyy");
        dateFormat.setTimeZone(timeZone);
        onlineDate = dateFormat.format(calendar.getTime());

        // User last online time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        timeFormat.setTimeZone(timeZone);
        onlineTime = timeFormat.format(calendar.getTime());

        HashMap<String, Object> onlineStatus = new HashMap<>();
        onlineStatus.put("date", onlineDate);
        onlineStatus.put("time", onlineTime);
        onlineStatus.put("state", state);

        databaseReference.child(getString(R.string.dbnode_users))
                .child(userProfile.uid)
                .child(getString(R.string.dbnode_users_userState))
                .updateChildren(onlineStatus)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Updated user online status: " + state);
                        } else {
                            Log.e(TAG, "User online status cannot be updated: " +
                                    task.getException());
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel n = new NotificationChannel(
                getString(R.string.notification_channel_id),
                name,
                importance
        );
        n.setDescription(description);
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(n);
    }

    private void showCreateGroupForm() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g FYP Group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name", Toast.LENGTH_SHORT).show();
                }
                else{
                    createGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }

    private void createGroup(final String groupName) {
        databaseReference.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName + " is Created Successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showEditProfileForm() {
        Intent profileIntent = new Intent(MainActivity.this, EditProfileFormActivity.class) ;
        startActivity(profileIntent);
        finish();
    }

    private void showLoginForm() {
        Intent loginIntent = new Intent(MainActivity.this, LoginFormActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void showProfileMenu() {
        Intent settingsIntent = new Intent(MainActivity.this, ProfileMenuActivity.class);
        startActivity(settingsIntent);
    }

    private void showFriendList() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FriendListActivity.class);
        startActivity(findFriendsIntent);
    }

    private void showArcadeMenu() {
        Intent miniGameIntent = new Intent(MainActivity.this, ArcadeMenuActivity.class);
        startActivity(miniGameIntent);
    }

    private void showAddPostForm() {
        String identity = userProfile.identity;

        if (identity.equals(getString(R.string.dbnode_users_identity_lecturer))) {
            Intent addPostActivity = new Intent(MainActivity.this, AddPostFormActivity.class);
            startActivity(addPostActivity);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Alert")
                    .setMessage("Only lecturers can post announcements.");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
