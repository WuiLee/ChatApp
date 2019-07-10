package com.example.chatapp.forms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFormActivity extends AppCompatActivity {

    private static String TAG = "EditProfileFormActivity";

    private Toolbar mToolbar;

    private Button submitButton;
    private EditText userName, userID, identity, userPhoneNo, courseID;
    private TextView[] compulsoryFields;
    private CircleImageView userProfileImage;
    private Uri selectedPhotoUri;

    private FirebaseAuth mAuth;
    private String userUid;
    private DatabaseReference RootRef;
    private FirebaseInstanceId firebaseInstance;
    private StorageReference storageReference;

    private Profile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_form);

        mAuth = FirebaseAuth.getInstance();
        userUid = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
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
                submitForm();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 0 &&
                resultCode == Activity.RESULT_OK &&
                data != null) {
            selectedPhotoUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedPhotoUri);
                userProfileImage.setImageBitmap(bitmap);
            } catch (Exception e){
                Log.d(TAG, "Failed selecting image: " + e.getLocalizedMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPhoto();
            }
        });

        compulsoryFields = new TextView[] {userName, userID, identity, userPhoneNo, courseID};

        configureFields();
    }

    private void selectPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
    }

    private void configureFields() {
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
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

            if (!userProfile.imageUrl.isEmpty()) {
                Picasso.get().load(userProfile.imageUrl).into(userProfileImage);
            }
        }
    }

    private void submitForm() {
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
                            final String authUserId = userUid;
                            String userToken = task.getResult().getToken();
                            String name = userName.getText().toString();
                            String userId = userID.getText().toString();
                            String userIdentity = identity.getText().toString();
                            String phoneNo = userPhoneNo.getText().toString();
                            String courseId = courseID.getText().toString();

                            final Profile profile = new Profile
                                    (authUserId, userId, userToken, courseId, userIdentity, name, phoneNo, "");

                            if (selectedPhotoUri != null) {
                                final String imageId = UUID.randomUUID().toString();
                                final String profileImageReference = "/Profile Images/" + imageId;
                                storageReference.child(profileImageReference).putFile(selectedPhotoUri)
                                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Log.d(TAG, "Successfully added photo to storage.");

                                                storageReference.child(profileImageReference).getDownloadUrl()
                                                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(final Uri uri) {
                                                                Log.d(TAG, "Successfully retrieved download Uri: " + uri.toString());
                                                                RootRef.child("Users").child(authUserId)
                                                                        .child(getString(R.string.dbnode_users_imageUrl)).setValue(uri.toString())
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                EditProfileFormActivity.this.userProfile.imageUrl = uri.toString();
                                                                                Log.d(TAG, "Successfully updated image url on database");
                                                                                updateProfileCache(userProfile);
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }
                                        });
                            }

                            RootRef.child("Users").child(authUserId).setValue(profile)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(
                                                        EditProfileFormActivity.this,
                                                        EditProfileFormActivity.this.getResources().getString(R.string.register_form_completed),
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                                updateProfileCache(profile);
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

    private void updateProfileCache(Profile profile) {
        this.userProfile = profile;
        SharedPreferences sharedPreferences = getSharedPreferences
                (getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(profile);
        editor.putString
                (getString(R.string.shared_pref_profile_key), json);
        editor.apply();
        Log.d(TAG, "Successfully updated profile cache");
    }

    private void showMainActivity() {
        Intent mainIntent = new Intent(EditProfileFormActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
