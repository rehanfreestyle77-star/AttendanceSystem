package com.example.attendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        checkForUpdate();

        // Agar pehle se login hai
        if (auth.getCurrentUser() != null) {
            fetchRoleAndNavigate(auth.getCurrentUser().getUid());
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(MainActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnLogin.setText("Logging in...");
                btnLogin.setEnabled(false);

                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // LOGIN SUCCESS: Ab role check karenge
                                    fetchRoleAndNavigate(auth.getCurrentUser().getUid());
                                } else {
                                    btnLogin.setText("LOGIN");
                                    btnLogin.setEnabled(true);
                                    Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
    }

    // --- SIMPLE ROLE CHECK LOGIC ---
    private void fetchRoleAndNavigate(String uid) {
        FirebaseDatabase.getInstance().getReference("Teachers").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String role = "Teacher"; // Default role
                        if (snapshot.exists() && snapshot.hasChild("role")) {
                            role = snapshot.child("role").getValue(String.class);
                        }

                        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                        intent.putExtra("userRole", role); // Dashboard ko batayenge kaun hai
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Error aaye toh bhi normal dashboard bhej do
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    }
                });
    }

    private void checkForUpdate() {
        DatabaseReference updateRef = FirebaseDatabase.getInstance().getReference("AppVersion");
        updateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Long latestVersionCodeLong = snapshot.child("code").getValue(Long.class);
                    String apkUrl = snapshot.child("url").getValue(String.class);
                    if (latestVersionCodeLong == null || apkUrl == null) return;
                    int latestVersionCode = latestVersionCodeLong.intValue();
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        int currentVersionCode = pInfo.versionCode;
                        if (latestVersionCode > currentVersionCode) {
                            showUpdateDialog(apkUrl);
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void showUpdateDialog(String url) {
        if (isFinishing()) return;
        new AlertDialog.Builder(this)
                .setTitle("Update Available 🚀")
                .setMessage("A new version of the app is available. Please update now.")
                .setCancelable(false)
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    }
                })
                .show();
    }
}