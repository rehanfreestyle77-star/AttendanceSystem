package com.example.attendancesystem;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotifyParentsActivity extends AppCompatActivity {

    // Variables declare kiye
    Button btnDate;
    Spinner spinnerClass, spinnerSubject;
    ListView listViewAbsent;

    DatabaseReference dbAttendance;
    String selectedDate;

    ArrayList<String> absentList; // Names dikhane ke liye
    ArrayList<String> phoneList;  // Numbers store karne ke liye

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify_parents);

        // Firebase ka reference liya
        dbAttendance = FirebaseDatabase.getInstance().getReference("Attendance");

        // Design se IDs connect ki
        btnDate = findViewById(R.id.btnDate);
        spinnerClass = findViewById(R.id.spinnerClass);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        listViewAbsent = findViewById(R.id.listViewAbsent);

        // Aaj ki date set ki
        selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        btnDate.setText("Date: " + selectedDate);

        // 1. Class Spinner mein data bhara
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> adapterClass = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(adapterClass);

        // 2. Subject Spinner mein data bhara
        String[] subjects = {"Maths", "English", "Science", "Hindi", "History"};
        ArrayAdapter<String> adapterSub = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, subjects);
        spinnerSubject.setAdapter(adapterSub);

        // 3. Date Picker (Calendar kholne ke liye)
        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(NotifyParentsActivity.this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year);
                btnDate.setText("Date: " + selectedDate);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 4. Load Button dabaane par students dhoondo
        findViewById(R.id.btnLoad).setOnClickListener(v -> loadAbsentStudents());

        // 5. List par click karne par WhatsApp khule
        listViewAbsent.setOnItemClickListener((parent, view, position, id) -> {
            String studentName = absentList.get(position);
            String phone = phoneList.get(position);

            if (phone != null && phone.length() >= 10) {
                sendWhatsApp(studentName, phone);
            } else {
                Toast.makeText(NotifyParentsActivity.this, "Phone Number not found!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Database se Absent students laane ka logic
    private void loadAbsentStudents() {
        String sClass = spinnerClass.getSelectedItem().toString();
        String sSubject = spinnerSubject.getSelectedItem().toString();

        dbAttendance.child(selectedDate).child(sClass).child(sSubject)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        absentList = new ArrayList<>();
                        phoneList = new ArrayList<>();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            String status = data.child("status").getValue(String.class);

                            // Sirf ABSENT walo ko list mein dalo
                            if ("Absent".equals(status)) {
                                String name = data.child("name").getValue(String.class);
                                String phone = data.child("phone").getValue(String.class);

                                absentList.add(name + " (Tap to Notify)");
                                phoneList.add(phone);
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(NotifyParentsActivity.this, android.R.layout.simple_list_item_1, absentList);
                        listViewAbsent.setAdapter(adapter);

                        if(absentList.isEmpty()) {
                            Toast.makeText(NotifyParentsActivity.this, "Everyone Present or No Data!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(NotifyParentsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // WhatsApp par message bhejne ka logic
    private void sendWhatsApp(String name, String phone) {
        try {
            if (!phone.startsWith("+91")) {
                phone = "+91" + phone; // India code lagaya
            }

            String cleanName = name.replace(" (Tap to Notify)", "");
            String subject = spinnerSubject.getSelectedItem().toString();

            // Message Content
            String msg = "Hello Parent, Your child " + cleanName + " is marked ABSENT today for " + subject + " class. Please provide a reason.";

            // WhatsApp Intent
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phone + "&text=" + msg));
            startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
    }
}