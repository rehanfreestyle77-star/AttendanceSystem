package com.example.attendancesystem;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TakeAttendanceActivity extends AppCompatActivity {

    ListView listViewStudents;
    Button btnSubmit, btnLoad;
    Spinner spinnerSubject, spinnerClass;

    // Lottie Variable
    LottieAnimationView lottieSuccess;

    List<Student> studentList;
    DatabaseReference databaseStudents;
    DatabaseReference databaseAttendance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_attendance);

        // Firebase References
        databaseStudents = FirebaseDatabase.getInstance().getReference("Students");
        databaseAttendance = FirebaseDatabase.getInstance().getReference("Attendance");

        // UI IDs
        listViewStudents = findViewById(R.id.listViewStudents);
        btnSubmit = findViewById(R.id.btnSubmitAttendance);
        btnLoad = findViewById(R.id.btnLoadStudents);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        spinnerClass = findViewById(R.id.spinnerClass);

        // Lottie Connect
        lottieSuccess = findViewById(R.id.lottieSuccess);

        studentList = new ArrayList<>();

        // --- YAHAN CHANGES KIYE HAIN (Black Text setup) ---

        // 1. Class Spinner
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, classes);
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);

        // 2. Subject Spinner
        String[] subjects = {"Select Subject", "Maths", "English", "Science", "History", "Hindi"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, subjects);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(subjectAdapter);

        // ----------------------------------------------------

        // Load Button Listener
        btnLoad.setOnClickListener(v -> loadStudentsByClass());

        // Submit Button Listener
        btnSubmit.setOnClickListener(v -> saveAttendance());
    }

    private void loadStudentsByClass() {
        final String selectedClass = spinnerClass.getSelectedItem().toString();

        databaseStudents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Student student = postSnapshot.getValue(Student.class);
                    // Filter by Class
                    if (student != null && student.getStudentClass().equals(selectedClass)) {
                        studentList.add(student);
                    }
                }
                StudentAdapter adapter = new StudentAdapter(TakeAttendanceActivity.this, studentList);
                listViewStudents.setAdapter(adapter);
                Toast.makeText(TakeAttendanceActivity.this, "Loaded " + studentList.size() + " students", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void saveAttendance() {
        String selectedSubject = spinnerSubject.getSelectedItem().toString();
        String selectedClass = spinnerClass.getSelectedItem().toString();

        if (selectedSubject.equals("Select Subject")) {
            Toast.makeText(this, "Please select a Subject first!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentList.size() == 0) {
            Toast.makeText(this, "No students loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Button disable karo taaki double click na ho
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Saving...");

        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());

        // Loop chalakar save karo
        for (Student student : studentList) {
            String status = student.isPresent ? "Present" : "Absent";
            DatabaseReference ref = databaseAttendance.child(currentDate).child(selectedClass).child(selectedSubject).child(student.getRollNo());
            ref.child("name").setValue(student.getName());
            ref.child("status").setValue(status);
            ref.child("phone").setValue(student.getPhone());
        }

        // *** ANIMATION START ***
        lottieSuccess.setVisibility(View.VISIBLE); // Dikhao
        lottieSuccess.playAnimation(); // Play karo

        Toast.makeText(this, "Attendance Saved Successfully!", Toast.LENGTH_SHORT).show();

        // 4 Second ka Delay (4000ms)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish(); // Activity Band
            }
        }, 4000);
    }
}