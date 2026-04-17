package com.example.attendancesystem;

import android.content.ContentValues;
import android.graphics.Color;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MonthlyReportActivity extends AppCompatActivity {

    Spinner spinnerMonth, spinnerYear, spinnerClass, spinnerSubject;
    Button btnLoad, btnExport;
    ListView listViewMonthly;
    DatabaseReference dbAttendance;

    Map<String, Integer> presentCountMap = new HashMap<>();
    Map<String, Integer> absentCountMap = new HashMap<>();
    Map<String, String> nameMap = new HashMap<>();
    ArrayList<String> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        dbAttendance = FirebaseDatabase.getInstance().getReference("Attendance");

        spinnerMonth = findViewById(R.id.spinnerMonth);
        spinnerYear = findViewById(R.id.spinnerYear);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        listViewMonthly = findViewById(R.id.listViewMonthly);
        btnLoad = findViewById(R.id.btnLoadMonthly);
        btnExport = findViewById(R.id.btnExportMonthly);

        setupSpinners();

        btnLoad.setOnClickListener(v -> calculateMonthlyReport());
        btnExport.setOnClickListener(v -> exportToExcel());
    }

    private void setupSpinners() {
        // --- BLACK TEXT SETUP ---

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
        setBlackSpinner(spinnerMonth, months);

        String[] years = {"2024", "2025", "2026"};
        setBlackSpinner(spinnerYear, years);

        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        setBlackSpinner(spinnerClass, classes);

        String[] subjects = {"Maths", "English", "Science", "Hindi", "History"};
        setBlackSpinner(spinnerSubject, subjects);
    }

    // Helper method taaki baar baar code na likhna pade
    private void setBlackSpinner(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void calculateMonthlyReport() {
        presentCountMap.clear();
        absentCountMap.clear();
        nameMap.clear();
        reportList.clear();

        String month = spinnerMonth.getSelectedItem().toString();
        String year = spinnerYear.getSelectedItem().toString();
        String sClass = spinnerClass.getSelectedItem().toString();
        String sSubject = spinnerSubject.getSelectedItem().toString();

        Toast.makeText(this, "Calculating... Please wait", Toast.LENGTH_SHORT).show();

        for (int i = 1; i <= 31; i++) {
            String day = String.format("%02d", i);
            String dateKey = day + "-" + month + "-" + year;

            dbAttendance.child(dateKey).child(sClass).child(sSubject)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot studentData : snapshot.getChildren()) {
                                    String roll = studentData.getKey();
                                    String name = studentData.child("name").getValue(String.class);
                                    String status = studentData.child("status").getValue(String.class);

                                    nameMap.put(roll, name);
                                    presentCountMap.putIfAbsent(roll, 0);
                                    absentCountMap.putIfAbsent(roll, 0);

                                    if ("Present".equals(status)) {
                                        presentCountMap.put(roll, presentCountMap.get(roll) + 1);
                                    } else {
                                        absentCountMap.put(roll, absentCountMap.get(roll) + 1);
                                    }
                                }
                            }
                            updateListView();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }
    }

    private void updateListView() {
        reportList.clear();
        for (String roll : nameMap.keySet()) {
            String name = nameMap.get(roll);
            int p = presentCountMap.getOrDefault(roll, 0);
            int a = absentCountMap.getOrDefault(roll, 0);
            int total = p + a;
            int percent = (total > 0) ? (p * 100 / total) : 0;

            String entry = name + " (Roll: " + roll + ")\n" +
                    "Present: " + p + " | Absent: " + a + " (" + percent + "%)";
            reportList.add(entry);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reportList);
        listViewMonthly.setAdapter(adapter);
    }

    private void exportToExcel() {
        if (reportList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            StringBuilder csvData = new StringBuilder();
            csvData.append("Roll No,Name,Present Days,Absent Days,Total Days,Percentage\n");

            for (String roll : nameMap.keySet()) {
                String name = nameMap.get(roll);
                int p = presentCountMap.getOrDefault(roll, 0);
                int a = absentCountMap.getOrDefault(roll, 0);
                int total = p + a;
                int percent = (total > 0) ? (p * 100 / total) : 0;

                csvData.append(roll).append(",")
                        .append(name).append(",")
                        .append(p).append(",")
                        .append(a).append(",")
                        .append(total).append(",")
                        .append(percent).append("%\n");
            }

            String fileName = "MonthlyReport_" + spinnerMonth.getSelectedItem() + "_" + spinnerYear.getSelectedItem() + ".csv";

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
                    Toast.makeText(this, "Excel Saved in Downloads!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Use newer Android version for auto-save", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}