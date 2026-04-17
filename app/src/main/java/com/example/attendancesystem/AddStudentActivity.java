package com.example.attendancesystem;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AddStudentActivity extends AppCompatActivity {

    EditText etName, etRollNo, etPhone;
    Spinner spinnerClass;
    Button btnSave, btnUploadCSV; // Naya button 'btnUploadCSV' add kiya
    DatabaseReference databaseStudents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        databaseStudents = FirebaseDatabase.getInstance().getReference("Students");

        etName = findViewById(R.id.etName);
        etRollNo = findViewById(R.id.etRollNo);
        etPhone = findViewById(R.id.etPhone);
        spinnerClass = findViewById(R.id.spinnerClass);
        btnSave = findViewById(R.id.btnSaveStudent);

        // CSV Upload Button ko link kiya (XML me id 'btnUploadCSV' honi chahiye)
        btnUploadCSV = findViewById(R.id.btnUploadCSV);

        // Naya Code (Black Text ke liye)
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, classes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(adapter);

        // Manual Save Button Logic
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addStudent();
            }
        });

        // CSV Upload Button Logic
        btnUploadCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCSVFile();
            }
        });
    }

    // ================= MANUAL UPLOAD LOGIC =================
    private void addStudent() {
        String name = etName.getText().toString().trim();
        String rollNo = etRollNo.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String sClass = spinnerClass.getSelectedItem().toString();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(rollNo) && !TextUtils.isEmpty(phone)) {
            String id = databaseStudents.push().getKey();

            // Student object banaya
            Student student = new Student(id, rollNo, name, sClass, phone);

            if (id != null) {
                databaseStudents.child(id).setValue(student);
                Toast.makeText(this, "Student Added Successfully!", Toast.LENGTH_LONG).show();

                // Boxes khali karo
                etName.setText("");
                etRollNo.setText("");
                etPhone.setText("");
            }
        } else {
            Toast.makeText(this, "Please enter all details (Name, Roll No, Phone)", Toast.LENGTH_LONG).show();
        }
    }

    // ================= EXCEL/CSV BULK UPLOAD LOGIC =================
    private void selectCSVFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimetypes = {"text/csv", "text/comma-separated-values", "application/csv"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                uploadCSVToFirebase(uri);
            }
        }
    }

    private void uploadCSVToFirebase(Uri uri) {
        Toast.makeText(this, "Reading File... Please Wait", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            int count = 0;
            boolean isFirstRow = true; // Heading chhodne ke liye

            while ((line = reader.readLine()) != null) {
                // Agar pehli line heading hai (e.g., Roll, Name, Phone, Class), toh usse skip karo
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                // CSV mein data comma se alag hota hai
                String[] columns = line.split(",");

                if (columns.length >= 4) { // Make sure 4 columns hain
                    String rollNo = columns[0].trim();
                    String name = columns[1].trim();
                    String phone = columns[2].trim();
                    String sClass = columns[3].trim();

                    // Firebase ke liye unique ID
                    String id = databaseStudents.push().getKey();

                    if (id != null) {
                        Student student = new Student(id, rollNo, name, sClass, phone);
                        databaseStudents.child(id).setValue(student);
                        count++;
                    }
                }
            }
            reader.close();
            Toast.makeText(this, "Success! " + count + " Students Added.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error reading CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}