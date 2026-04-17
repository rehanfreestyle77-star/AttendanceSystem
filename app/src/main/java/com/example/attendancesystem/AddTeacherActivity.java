package com.example.attendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddTeacherActivity extends AppCompatActivity {

    EditText etName, etEmail, etPhone, etPassword;
    Spinner spinnerSubject; // Naya Spinner
    Button btnSave;

    FirebaseAuth auth;
    DatabaseReference dbTeachers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        auth = FirebaseAuth.getInstance();
        dbTeachers = FirebaseDatabase.getInstance().getReference("Teachers");

        etName = findViewById(R.id.etTeacherName);
        etEmail = findViewById(R.id.etTeacherEmail);
        etPhone = findViewById(R.id.etTeacherPhone);
        etPassword = findViewById(R.id.etTeacherPassword);
        spinnerSubject = findViewById(R.id.spinnerTeacherSubject); // Link kiya
        btnSave = findViewById(R.id.btnSaveTeacher);

        // Subjects ki list set ki (Isme aur add kar sakte hain)
        String[] subjects = {"Maths", "English", "Science", "History", "Hindi", "Computer", "Geography", "Biology"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerTeacher();
            }
        });
    }

    private void registerTeacher() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String subject = spinnerSubject.getSelectedItem().toString(); // Subject uthaya

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setText("Creating Account...");
        btnSave.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String userId = auth.getCurrentUser().getUid();

                    HashMap<String, String> teacherMap = new HashMap<>();
                    teacherMap.put("name", name);
                    teacherMap.put("email", email);
                    teacherMap.put("phone", phone);
                    teacherMap.put("subject", subject); // Firebase me save kiya
                    teacherMap.put("role", "Teacher");

                    dbTeachers.child(userId).setValue(teacherMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddTeacherActivity.this, "Teacher Added Successfully!", Toast.LENGTH_LONG).show();
                                auth.signOut();
                                Toast.makeText(AddTeacherActivity.this, "Please log in again as Principal", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(AddTeacherActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

                } else {
                    btnSave.setText("Create Teacher Account");
                    btnSave.setEnabled(true);
                    Toast.makeText(AddTeacherActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}