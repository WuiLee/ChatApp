package com.example.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity{

    private Button UpdateAccountSettings, TimetableSettings, EnrolmentSettings, EmergencyContact;
    private EditText userName, userStatus, userID, courseID;
    private CircleImageView userProfileImage;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private static final int GalleryPick = 1;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        TimetableSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToTimetableActivity();
            }
        });

        EnrolmentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToEnrolmentActivity();
            }
        });



        RetrieveUserInfo();

        /*userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });*/
    }

    private void InitializeFields() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        TimetableSettings = (Button) findViewById(R.id.timetable_page_button);
        EnrolmentSettings = (Button) findViewById(R.id.enrollment_page_button);
        EmergencyContact = (Button) findViewById(R.id.useful_contact_button);

        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userID = (EditText) findViewById(R.id.set_student_id);
        courseID = (EditText) findViewById(R.id.set_course_id);

        SettingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
        }

    }*/

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        String setUserID = userID.getText().toString();
        String setCourseID = courseID.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "PLease enter your name...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "PLease enter your status...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setUserID)){
            Toast.makeText(this, "PLease enter your ID...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(setCourseID)){
            Toast.makeText(this, "PLease enter your courseID...", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("status", setStatus);
            profileMap.put("id", setUserID);
            profileMap.put("course", setCourseID);
            RootRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successful...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
    private void RetrieveUserInfo() {
        RootRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveUserID = dataSnapshot.child("id").getValue().toString();
                            String retrieveCourseID = dataSnapshot.child("course").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            userID.setText(retrieveUserID);
                            courseID.setText(retrieveCourseID);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveUserID = dataSnapshot.child("id").getValue().toString();
                            String retrieveCourseID = dataSnapshot.child("course").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            userID.setText(retrieveUserID);
                            courseID.setText(retrieveCourseID);
                        }
                        else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void SendUserToTimetableActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, TimetableActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToEnrolmentActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, EnrolmentActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}