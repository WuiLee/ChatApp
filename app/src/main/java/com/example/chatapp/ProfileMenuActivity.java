package com.example.chatapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.chatapp.forms.EditProfileFormActivity;
import com.example.chatapp.models.Profile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileMenuActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;
    private static String TAG = "ProfileMenuActivity";

    private Button EditProfileSettings, TimetableSettings, EnrolmentSettings, EmergencyContact;
    private TextView userName, userID, courseID;
    private CircleImageView userProfileImage;

    private Profile userProfile;

    private Toolbar SettingsToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_menu);

        mAuth = FirebaseAuth.getInstance();

        initializeFields();

        EditProfileSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileForm();
            }
        });

        TimetableSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimetable();
            }
        });

        EnrolmentSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnrolmentList();
            }
        });

        EmergencyContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsefulContacts();
            }
        });
    }


    private void initializeFields() {
        EditProfileSettings = findViewById(R.id.edit_profile_button);
        TimetableSettings = findViewById(R.id.timetable_page_button);
        EnrolmentSettings = findViewById(R.id.enrollment_page_button);
        EmergencyContact = findViewById(R.id.useful_contact_button);

        userName = findViewById(R.id.view_user_name);
        userID = findViewById(R.id.view_student_id);
        courseID = findViewById(R.id.view_course_id);
        userProfileImage = findViewById(R.id.view_profile_image);

        SettingsToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("My Profile");

        configureFields();
    }

    private void configureFields() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_file_key), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString
                (getString(R.string.shared_pref_profile_key), null);
        Gson gson = new Gson();
        userProfile = gson.fromJson(json, Profile.class);

        if (userProfile != null) {
            Log.d(TAG, "Retrieved user profile from cache.");
            userName.setText(userProfile.name);
            userID.setText(userProfile.userId);
            courseID.setText(userProfile.courseId);
            Log.d(TAG, json);
            if (!userProfile.imageUrl.isEmpty()) {
                Picasso.get().load(userProfile.imageUrl).into(userProfileImage);
            }
        }
    }

    private void showEditProfileForm() {
        Intent mainIntent = new Intent(ProfileMenuActivity.this, EditProfileFormActivity.class);
        startActivity(mainIntent);
    }

    private void showTimetable() {
        Intent timetableIntent = new Intent(ProfileMenuActivity.this, WeekActivity.class);
        startActivity(timetableIntent);
    }

    private void showEnrolmentList() {
        Intent enrolmentIntent = new Intent(ProfileMenuActivity.this, EnrolmentActivity.class);
        startActivity(enrolmentIntent);
    }

    private void showUsefulContacts() {
        Intent contactIntent = new Intent(ProfileMenuActivity.this, UsefulContactList.class);
        startActivity(contactIntent);
    }
}