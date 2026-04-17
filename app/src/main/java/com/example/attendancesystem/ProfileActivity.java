package com.example.attendancesystem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvEmail, tvSubject;
    ImageView ivProfilePhoto;
    RelativeLayout rlPhotoEdit;
    Button btnResetPass;
    FirebaseAuth auth;
    DatabaseReference dbRef;

    // Gallery launcher with UNIQUE UID Saving logic
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        try {
                            // 1. Permission lock karna
                            final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(imageUri, takeFlags);

                            // 2. UI update
                            ivProfilePhoto.setImageURI(imageUri);

                            // 3. UNIQUE SAVE: UID ke saath save karein taaki Principal/Teacher mix na ho
                            SharedPreferences sh = getSharedPreferences("UserProfile", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sh.edit();

                            // Key badal di: "profile_image_" + UID
                            editor.putString("profile_image_" + user.getUid(), imageUri.toString());
                            editor.apply();

                            Toast.makeText(this, "Your Profile Photo Saved!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error saving photo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        tvName = findViewById(R.id.tvProfileName);
        tvEmail = findViewById(R.id.tvProfileEmail);
        tvSubject = findViewById(R.id.tvProfileSubject);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        rlPhotoEdit = findViewById(R.id.rlPhotoEdit);
        btnResetPass = findViewById(R.id.btnChangePassword);

        if (user != null) {
            String uid = user.getUid();
            tvEmail.setText(user.getEmail());

            // UNIQUE LOAD: Sirf is user ki saved photo uthao
            SharedPreferences sh = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String savedUri = sh.getString("profile_image_" + uid, "");
            if (!savedUri.isEmpty()) {
                try {
                    ivProfilePhoto.setImageURI(Uri.parse(savedUri));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Firebase data load
            dbRef = FirebaseDatabase.getInstance().getReference("Teachers").child(uid);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        tvName.setText(snapshot.child("name").getValue(String.class));
                        tvSubject.setText(snapshot.child("subject").getValue(String.class));
                    } else {
                        tvName.setText("Principal");
                        tvSubject.setText("Admin Console");
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });

            rlPhotoEdit.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                galleryLauncher.launch(intent);
            });

            btnResetPass.setOnClickListener(v -> {
                auth.sendPasswordResetEmail(user.getEmail()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }
}