package com.example.attendancesystem;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class TimeTableActivity extends AppCompatActivity {

    EditText etTeacherEmail, etSubject;
    Spinner spinnerDay;
    Button btnPickTime, btnSave;
    TextView tvTime;

    int selectedHour = -1, selectedMinute = -1;
    DatabaseReference dbTimeTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table);

        dbTimeTable = FirebaseDatabase.getInstance().getReference("TimeTable");

        // IDs
        etTeacherEmail = findViewById(R.id.etTeacherEmail);
        etSubject = findViewById(R.id.etSubjectName);
        spinnerDay = findViewById(R.id.spinnerDay);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveTimeTable); // XML me button ka text "Save Schedule" kar dena
        tvTime = findViewById(R.id.tvSelectedTime);

        // Day Spinner
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        spinnerDay.setAdapter(adapter);

        // Time Picker
        btnPickTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedHour = hour;
                selectedMinute = minute;
                tvTime.setText(String.format(Locale.getDefault(), "Lecture Time: %02d:%02d", hour, minute));
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        // SIRF SAVE HOGA - KOI WHATSAPP NAHI
        btnSave.setOnClickListener(v -> saveToFirebase());
    }

    private void saveToFirebase() {
        String email = etTeacherEmail.getText().toString().trim();
        String subject = etSubject.getText().toString().trim();
        String day = spinnerDay.getSelectedItem().toString();

        if (email.isEmpty() || subject.isEmpty() || selectedHour == -1) {
            Toast.makeText(this, "Please fill all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase ke liye '.' ko '_' banaya
        String safeEmail = email.replace(".", "_");

        HashMap<String, Object> map = new HashMap<>();
        map.put("subject", subject);
        map.put("hour", selectedHour);
        map.put("minute", selectedMinute);

        // Data Save: TimeTable -> TeacherEmail -> Day -> NewEntry
        dbTimeTable.child(safeEmail).child(day).push().setValue(map)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Schedule Set! Teacher will get auto-alarm.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}