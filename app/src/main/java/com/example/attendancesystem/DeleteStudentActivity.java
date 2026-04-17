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
import java.util.List;

public class DeleteStudentActivity extends AppCompatActivity {

    Spinner spinnerClass;
    Button btnLoad;
    ListView listViewDelete;
    DatabaseReference databaseStudents;
    List<Student> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_student);

        databaseStudents = FirebaseDatabase.getInstance().getReference("Students");

        spinnerClass = findViewById(R.id.spinnerClassDelete);
        btnLoad = findViewById(R.id.btnLoadDelete);
        listViewDelete = findViewById(R.id.listViewDelete);
        studentList = new ArrayList<>();

        // Class Spinner Setup
        String[] classes = {"Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10"};
        ArrayAdapter<String> classAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classes);
        spinnerClass.setAdapter(classAdapter);

        // Load Button Logic
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadStudentsForDeletion();
            }
        });
    }

    private void loadStudentsForDeletion() {
        final String selectedClass = spinnerClass.getSelectedItem().toString();

        databaseStudents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Student student = postSnapshot.getValue(Student.class);
                    if (student != null && student.getStudentClass().equals(selectedClass)) {
                        studentList.add(student);
                    }
                }
                // Adapter set karna
                DeleteStudentAdapter adapter = new DeleteStudentAdapter(DeleteStudentActivity.this, studentList);
                listViewDelete.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DeleteStudentActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}