package com.example.attendancesystem;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressActivity extends AppCompatActivity {

    Spinner spinnerClass;
    Button btnLoad;
    ListView listViewProgress;

    DatabaseReference dbStudents, dbAttendance;

    // Data store karne ke liye Model Class (Internal)
    public static class StudentProgress {
        String rollNo;
        String name;
        int presentCount = 0;
        int totalCount = 0;
        int percentage = 0;

        public StudentProgress(String rollNo, String name) {
            this.rollNo = rollNo;
            this.name = name;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Firebase References
        dbStudents = FirebaseDatabase.getInstance().getReference("Students");
        dbAttendance = FirebaseDatabase.getInstance().getReference("Attendance");

        spinnerClass = findViewById(R.id.spinnerClassFilter);
        btnLoad = findViewById(R.id.btnLoadProgress);
        listViewProgress = findViewById(R.id.listViewProgress);

        // Class Spinner Setup
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(classAdapter);

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateProgress();
            }
        });
    }

    private void calculateProgress() {
        final String selectedClass = spinnerClass.getSelectedItem().toString();

        // Step 1: Pehle us class ke saare Students fetch karo
        dbStudents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot studentSnapshot) {
                // Map use karenge taaki RollNo se data jaldi mile
                final Map<String, StudentProgress> studentMap = new HashMap<>();

                for (DataSnapshot snap : studentSnapshot.getChildren()) {
                    Student student = snap.getValue(Student.class);
                    if (student != null && student.getStudentClass().equals(selectedClass)) {
                        // Map mein student daal diya (Initial counts 0 hain)
                        studentMap.put(student.getRollNo(), new StudentProgress(student.getRollNo(), student.getName()));
                    }
                }

                if (studentMap.isEmpty()) {
                    Toast.makeText(ProgressActivity.this, "No students found in " + selectedClass, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Step 2: Ab Attendance Records fetch karo aur calculate karo
                dbAttendance.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {

                        // Loop through ALL Dates
                        for (DataSnapshot dateSnap : attendanceSnapshot.getChildren()) {

                            // Check karo agar us Date mein selected Class ka data hai
                            if (dateSnap.hasChild(selectedClass)) {
                                DataSnapshot classSnap = dateSnap.child(selectedClass);

                                // Loop through ALL Subjects in that Class
                                for (DataSnapshot subjectSnap : classSnap.getChildren()) {

                                    // Loop through ALL Students (Roll Nos)
                                    for (DataSnapshot studentSnap : subjectSnap.getChildren()) {
                                        String rollNo = studentSnap.getKey();
                                        String status = studentSnap.child("status").getValue(String.class);

                                        // Agar ye Roll No humare Student Map mein hai
                                        if (studentMap.containsKey(rollNo)) {
                                            StudentProgress progress = studentMap.get(rollNo);

                                            // Count Badhao
                                            progress.totalCount++;
                                            if ("Present".equals(status)) {
                                                progress.presentCount++;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Step 3: Percentage Calculate karo aur List taiyar karo
                        List<StudentProgress> finalDataList = new ArrayList<>();
                        for (StudentProgress s : studentMap.values()) {
                            if (s.totalCount > 0) {
                                s.percentage = (s.presentCount * 100) / s.totalCount;
                            } else {
                                s.percentage = 0; // Agar koi class hi nahi hui ab tak
                            }
                            finalDataList.add(s);
                        }

                        // Adapter set karo
                        ProgressAdapter adapter = new ProgressAdapter(ProgressActivity.this, finalDataList);
                        listViewProgress.setAdapter(adapter);
                        Toast.makeText(ProgressActivity.this, "Progress Calculated!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(ProgressActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}