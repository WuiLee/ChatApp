package com.example.chatapp.forms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.models.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFormActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button submitButton;
    private EditText userName, userID, identity, userPhoneNo, courseID;
    private TextView[] compulsoryFields;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private String userUid;
    private DatabaseReference RootRef;
    private FirebaseInstanceId firebaseInstance;

    private Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_form);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        firebaseInstance = FirebaseInstanceId.getInstance();

        mToolbar = findViewById(R.id.register_form_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Account");

        initializeFields();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitForm();
            }
        });
    }

    private void initializeFields() {
        // Explicit type casting is redundant here since you have already declared the variable with its type
        submitButton = findViewById(R.id.register_form_button);

        userName = findViewById(R.id.enter_user_name);
        userID = findViewById(R.id.enter_user_id);
        identity = findViewById(R.id.enter_user_identity);
        userPhoneNo = findViewById(R.id.enter_phone_number);
        courseID = findViewById(R.id.enter_course_id);

        userProfileImage = findViewById(R.id.profile_image);

        compulsoryFields = new TextView[] {userName, userID, identity, userPhoneNo, courseID};

        configureFields();
    }

    private void configureFields() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_file_key), Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(getString(R.string.shared_pref_profile_key), null);
        Gson gson = new Gson();
        userProfile = gson.fromJson(json, Profile.class);

        if (userProfile != null) {
            userName.setText(userProfile.name);
            userID.setText(userProfile.userId);
            identity.setText(userProfile.identity);
            identity.setEnabled(false);
            userPhoneNo.setText(userProfile.phoneNumber);
            courseID.setText(userProfile.courseId);
        }
    }

    private void SubmitForm() {
        for (TextView field: compulsoryFields) {
            if (TextUtils.isEmpty(field.getText().toString())) {
                Toast.makeText(
                        this,
                        this.getResources().getString(R.string.register_form_not_completed),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
        }

        // Getting registration token for FCM client app instance
        firebaseInstance.getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {

                            String authUserId = userUid;
                            String userToken = task.getResult().getToken();
                            String name = userName.getText().toString();
                            String userId = userID.getText().toString();
                            String userIdentity = identity.getText().toString();
                            String phoneNo = userPhoneNo.getText().toString();
                            String courseId = courseID.getText().toString();

                            HashMap<String, String> formHashMap = new HashMap<>();
                            formHashMap.put(getString(R.string.dbnode_users_userToken), userToken);
                            formHashMap.put(getString(R.string.dbnode_users_uid), authUserId);
                            formHashMap.put(getString(R.string.dbnode_users_name), name);
                            formHashMap.put(getString(R.string.dbnode_users_userId), userId);
                            formHashMap.put(getString(R.string.dbnode_users_identity), userIdentity);
                            formHashMap.put(getString(R.string.dbnode_users_phoneNo), phoneNo);
                            formHashMap.put(getString(R.string.dbnode_users_courseId), courseId);

                            RootRef.child("Users").child(authUserId).setValue(formHashMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(
                                                        EditProfileFormActivity.this,
                                                        EditProfileFormActivity.this.getResources().getString(R.string.register_form_completed),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                showMainActivity();
                                            } else {
                                                Toast.makeText(
                                                        EditProfileFormActivity.this,
                                                        task.getException().toString(),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(
                                    EditProfileFormActivity.this,
                                    task.getException().toString(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
    }

    private void showMainActivity() {
        Intent mainIntent = new Intent(EditProfileFormActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
