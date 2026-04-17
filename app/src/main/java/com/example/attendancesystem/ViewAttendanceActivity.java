package com.example.attendancesystem;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView; // Lottie Import
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ViewAttendanceActivity extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;

    Button btnDateSelector, btnLoadReport, btnExport;
    Spinner spinnerClass, spinnerSubject;
    ListView listViewReport;
    TextView tvReportSummary;

    // Lottie Animation Variable
    LottieAnimationView lottieLoading;

    DatabaseReference databaseAttendance;
    String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        databaseAttendance = FirebaseDatabase.getInstance().getReference("Attendance");

        // IDs Connect kiye
        btnDateSelector = findViewById(R.id.btnDateSelector);
        btnLoadReport = findViewById(R.id.btnLoadReport);
        btnExport = findViewById(R.id.btnExport);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        listViewReport = findViewById(R.id.listViewReport);
        tvReportSummary = findViewById(R.id.tvReportSummary);

        // Lottie Connect kiya
        lottieLoading = findViewById(R.id.lottieLoading);

        // Default Date
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        btnDateSelector.setText("Date: " + selectedDate);
        tvReportSummary.setText("Select filters and load report");

        // Spinners Setup
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(classAdapter);

        String[] subjects = {"Maths", "English", "Science", "Hindi", "History"};
        ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubject.setAdapter(subjectAdapter);

        // Listeners
        btnDateSelector.setOnClickListener(v -> showDatePicker());

        // Load Report with Animation
        btnLoadReport.setOnClickListener(v -> loadStudentListForSubject());

        // Export with Permission Check
        btnExport.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportAllSubjectsToCSV();
            } else {
                checkPermissionAndExport();
            }
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
            btnDateSelector.setText("Date: " + selectedDate);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void checkPermissionAndExport() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        } else {
            exportAllSubjectsToCSV();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            exportAllSubjectsToCSV();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    // *** MAIN LOGIC WITH ANIMATION ***
    private void loadStudentListForSubject() {
        final String selectedClass = spinnerClass.getSelectedItem().toString();
        final String selectedSubject = spinnerSubject.getSelectedItem().toString();

        DatabaseReference reportRef = databaseAttendance.child(selectedDate).child(selectedClass).child(selectedSubject);
        tvReportSummary.setText("Report: " + selectedClass + " | " + selectedSubject);

        // 1. ANIMATION START (Show Loading, Hide List)
        lottieLoading.setVisibility(View.VISIBLE);
        listViewReport.setVisibility(View.GONE);

        reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Student> studentList = new ArrayList<>();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                        String name = studentSnapshot.child("name").getValue(String.class);
                        String status = studentSnapshot.child("status").getValue(String.class);

                        Student student = new Student();
                        student.name = name;
                        student.status = status;
                        studentList.add(student);
                    }
                }

                RecordAdapter adapter = new RecordAdapter(ViewAttendanceActivity.this, studentList);
                listViewReport.setAdapter(adapter);

                // 2. ANIMATION STOP (Hide Loading, Show List)
                lottieLoading.setVisibility(View.GONE);
                listViewReport.setVisibility(View.VISIBLE);

                if(studentList.isEmpty()) Toast.makeText(ViewAttendanceActivity.this, "No data found.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Error aane par bhi loading band karo
                lottieLoading.setVisibility(View.GONE);
                Toast.makeText(ViewAttendanceActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void exportAllSubjectsToCSV() {
        // Export logic same as before (No changes needed here for animation)
        final String selectedClass = spinnerClass.getSelectedItem().toString();
        DatabaseReference reportRef = databaseAttendance.child(selectedDate).child(selectedClass);

        reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(ViewAttendanceActivity.this, "No Data to Export.", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Map<String, Map<String, String>> studentData = new HashMap<>();
                    Set<String> subjects = new HashSet<>();

                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subjectName = subjectSnapshot.getKey();
                        subjects.add(subjectName);
                        for (DataSnapshot rollNoSnapshot : subjectSnapshot.getChildren()) {
                            String rollNo = rollNoSnapshot.getKey();
                            String name = rollNoSnapshot.child("name").getValue(String.class);
                            String status = rollNoSnapshot.child("status").getValue(String.class);

                            studentData.putIfAbsent(rollNo, new HashMap<>());
                            studentData.get(rollNo).put("Name", name);
                            studentData.get(rollNo).put(subjectName, status);
                        }
                    }

                    StringBuilder csvData = new StringBuilder();
                    csvData.append("Roll No,Name");
                    List<String> sortedSubjects = new ArrayList<>(subjects);
                    for (String sub : sortedSubjects) csvData.append(",").append(sub);
                    csvData.append("\n");

                    for (Map.Entry<String, Map<String, String>> entry : studentData.entrySet()) {
                        String rollNo = entry.getKey();
                        Map<String, String> records = entry.getValue();
                        csvData.append(rollNo).append(",").append(records.getOrDefault("Name", "N/A"));
                        for (String sub : sortedSubjects) csvData.append(",").append(records.getOrDefault(sub, "-"));
                        csvData.append("\n");
                    }

                    String fileName = "Attendance_" + selectedClass.replace(" ", "") + "_" + selectedDate + ".csv";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                        if (uri != null) {
                            OutputStream outputStream = getContentResolver().openOutputStream(uri);
                            outputStream.write(csvData.toString().getBytes());
                            outputStream.close();
                            Toast.makeText(ViewAttendanceActivity.this, "Excel Saved in Downloads!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        File file = new File(folder, fileName);
                        FileWriter writer = new FileWriter(file);
                        writer.append(csvData.toString());
                        writer.flush();
                        writer.close();
                        Toast.makeText(ViewAttendanceActivity.this, "Saved to Downloads!", Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Toast.makeText(ViewAttendanceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}