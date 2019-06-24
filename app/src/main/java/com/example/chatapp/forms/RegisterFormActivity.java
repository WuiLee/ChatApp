package com.example.chatapp.forms;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterFormActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button RegisterAccountForm;
    private EditText userName, userID, identity, userPhoneNo, courseID;
    private TextView[] compulsoryFields;
    private CircleImageView userProfileImage;

    private FirebaseAuth mAuth;
    private String userUid;
    private DatabaseReference RootRef;
    private FirebaseInstanceId firebaseInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        firebaseInstance = FirebaseInstanceId.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_form_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Account");

        InitializeFields();
//        RetrieveUserInfo();

        RegisterAccountForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SubmitForm();
            }
        });
    }

    private void InitializeFields() {
        // Explicit type casting is redundant here since you have already declared the variable with its type
        RegisterAccountForm = (Button) findViewById(R.id.register_form_button);
        userName = (EditText) findViewById(R.id.enter_user_name);
        userID = (EditText) findViewById(R.id.enter_user_id);
        identity = (EditText) findViewById(R.id.enter_user_identity);
        userPhoneNo = (EditText) findViewById(R.id.enter_phone_number);
        courseID = (EditText) findViewById(R.id.enter_course_id);

        userProfileImage = (CircleImageView) findViewById(R.id.profile_image);

        compulsoryFields = new TextView[]{userName, userID, identity, userPhoneNo, courseID};
    }

    private void SubmitForm() {

        // Not a good practice to name your variables similar to setter methods
        // Rename them if time allows
//        String setUserName = userName.getText().toString();
//        String setUserID = userID.getText().toString();
//        String setIdentity = identity.getText().toString();
//        String setPhoneNo = userPhoneNo.getText().toString();
//        String setCourseID = courseID.getText().toString();

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
                                                        RegisterFormActivity.this,
                                                        RegisterFormActivity.this.getResources().getString(R.string.register_form_completed),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                SendUserToMainActivity();
                                            } else {
                                                Toast.makeText(
                                                        RegisterFormActivity.this,
                                                        task.getException().toString(),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(
                                    RegisterFormActivity.this,
                                    task.getException().toString(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });

//        if (TextUtils.isEmpty(setUserName)) {
//            Toast.makeText(this, "Please enter your name...", Toast.LENGTH_SHORT).show();
//        }
//        if (TextUtils.isEmpty(setUserID)) {
//            Toast.makeText(this, "Please enter your id...", Toast.LENGTH_SHORT).show();
//        }
//        if (TextUtils.isEmpty(setIdentity)) {
//            Toast.makeText(this, "Please enter your position...", Toast.LENGTH_SHORT).show();
//        }
//
//        if (TextUtils.isEmpty(setPhoneNo)) {
//            Toast.makeText(this, "Please enter your phoneNo...", Toast.LENGTH_SHORT).show();
//        }
//        if (TextUtils.isEmpty(setCourseID)) {
//            Toast.makeText(this, "Please enter your courseId/departmentId...", Toast.LENGTH_SHORT).show();
//        } else {
//            HashMap<String, String> profileMap = new HashMap<>();
//            profileMap.put("uid", currentUserID);
//            profileMap.put("name", setUserName);
//            profileMap.put("id", setUserID);
//            profileMap.put("identity", setIdentity);
//            profileMap.put("phone", setPhoneNo);
//            profileMap.put("course", setCourseID);
//            RootRef.child("Users").child(currentUserID).setValue(profileMap)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                SendUserToMainActivity();
//                                Toast.makeText(RegisterFormActivity.this, "Form Submitted Successfully", Toast.LENGTH_SHORT).show();
//                            } else {
//                                String message = task.getException().toString();
//                                Toast.makeText(RegisterFormActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    });
//        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterFormActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

//    private void RetrieveUserInfo() {
//        RootRef.child("Users").child(userUid)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))) {
//                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
//                            String retrieveUserID = dataSnapshot.child("id").getValue().toString();
//                            String retrieveIdentity = dataSnapshot.child("identity").getValue().toString();
//                            String retrievePhoneNo = dataSnapshot.child("phone").getValue().toString();
//                            String retrieveCourseID = dataSnapshot.child("course").getValue().toString();
//
//                            userName.setText(retrieveUserName);
//                            userID.setText(retrieveUserID);
//                            identity.setText(retrieveIdentity);
//                            userPhoneNo.setText(retrievePhoneNo);
//                            courseID.setText(retrieveCourseID);
//
//                            if ((dataSnapshot.hasChild("image"))) {
//                                String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
//                            }
//
//                        } else {
//                            Toast.makeText(RegisterFormActivity.this, "Please set & update your profile information...", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                    }
//                });
//    }
}
