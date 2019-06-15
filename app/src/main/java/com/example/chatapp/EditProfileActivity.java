package com.example.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class EditProfileActivity extends AppCompatActivity {

    private Button UpdateProfileSetting;
    private EditText userName, userID, identity, userEmail, userPhoneNo, courseID, gender;

    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("My Profile");

        InitializeFields();

        UpdateProfileSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();
    }

    private void InitializeFields() {

        UpdateProfileSetting = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.edit_user_name);
        userID = (EditText) findViewById(R.id.edit_user_id);
        identity = (EditText) findViewById(R.id.edit_user_identity);
        userEmail = (EditText) findViewById(R.id.edit_email_address);
        userPhoneNo = (EditText) findViewById(R.id.edit_phone_number);
        courseID = (EditText) findViewById(R.id.edit_course_id);
        gender = (EditText) findViewById(R.id.edit_gender);

    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setUserID = userID.getText().toString();
        String setIdentity = identity.getText().toString();
        String setUserEmail = userEmail.getText().toString();
        String setPhoneNo = userPhoneNo.getText().toString();
        String setCourseID = courseID.getText().toString();
        String setGender = gender.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please enter your name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserID)){
            Toast.makeText(this, "Please enter your id...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setIdentity)){
            Toast.makeText(this, "Please enter your position...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setUserEmail)){
            Toast.makeText(this, "Please enter your email address...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setPhoneNo)){
            Toast.makeText(this, "Please enter your phoneNo...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setCourseID)){
            Toast.makeText(this, "Please enter your courseId/departmentId...", Toast.LENGTH_SHORT).show();
        }
        else{
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("name", setUserName);
            profileMap.put("id", setUserID);
            profileMap.put("identity", setIdentity);
            profileMap.put("email", setUserEmail);
            profileMap.put("phone", setPhoneNo);
            profileMap.put("course", setCourseID);
            profileMap.put("gender", setGender);

            RootRef.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(EditProfileActivity.this, "Profile Updated Successful...", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message = task.getException().toString();
                                Toast.makeText(EditProfileActivity.this, "Error" + message, Toast.LENGTH_SHORT).show();
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
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("image")))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserID = dataSnapshot.child("id").getValue().toString();
                            String retrieveIdentity = dataSnapshot.child("identity").getValue().toString();
                            String retrieveUserEmail = dataSnapshot.child("email").getValue().toString();
                            String retrievePhoneNo = dataSnapshot.child("phone").getValue().toString();
                            String retrieveCourseID = dataSnapshot.child("course").getValue().toString();
                            String retrieveGender = dataSnapshot.child("gender").getValue().toString();

                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userID.setText(retrieveUserID);
                            identity.setText(retrieveIdentity);
                            userEmail.setText(retrieveUserEmail);
                            userPhoneNo.setText(retrievePhoneNo);
                            courseID.setText(retrieveCourseID);
                            gender.setText((retrieveGender));
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveUserID = dataSnapshot.child("id").getValue().toString();
                            String retrieveIdentity = dataSnapshot.child("identity").getValue().toString();
                            String retrieveUserEmail = dataSnapshot.child("email").getValue().toString();
                            String retrievePhoneNo = dataSnapshot.child("phone").getValue().toString();
                            String retrieveCourseID = dataSnapshot.child("course").getValue().toString();

                            userName.setText(retrieveUserName);
                            userID.setText(retrieveUserID);
                            identity.setText(retrieveIdentity);
                            userEmail.setText(retrieveUserEmail);
                            userPhoneNo.setText(retrievePhoneNo);
                            courseID.setText(retrieveCourseID);
                        }
                        else {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(EditProfileActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
