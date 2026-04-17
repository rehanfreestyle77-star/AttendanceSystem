package com.example.attendancesystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class DeleteStudentAdapter extends ArrayAdapter<Student> {

    private Activity context;
    private List<Student> studentList;

    public DeleteStudentAdapter(Activity context, List<Student> studentList) {
        super(context, R.layout.item_delete_student, studentList);
        this.context = context;
        this.studentList = studentList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.item_delete_student, null, true);

        TextView tvName = listViewItem.findViewById(R.id.tvDelName);
        TextView tvRoll = listViewItem.findViewById(R.id.tvDelRoll);
        ImageView btnDelete = listViewItem.findViewById(R.id.btnDeleteAction);

        Student student = studentList.get(position);

        tvName.setText(student.getName());
        tvRoll.setText("Roll No: " + student.getRollNo());

        // Delete Button Click Logic
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Student")
                        .setMessage("Are you sure you want to delete " + student.getName() + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteStudent(student);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        return listViewItem;
    }

    private void deleteStudent(Student student) {
        DatabaseReference dbStudent = FirebaseDatabase.getInstance().getReference("Students").child(student.getStudentId());
        dbStudent.removeValue();
        Toast.makeText(context, "Student Deleted", Toast.LENGTH_SHORT).show();
    }
}