package com.example.attendancesystem;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import java.util.List;

public class StudentAdapter extends ArrayAdapter<Student> {

    private Activity context;
    private List<Student> studentList;

    public StudentAdapter(Activity context, List<Student> studentList) {
        super(context, R.layout.item_student, studentList);
        this.context = context;
        this.studentList = studentList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.item_student, null, true);

        TextView tvName = listViewItem.findViewById(R.id.tvStudentName);
        TextView tvRollNo = listViewItem.findViewById(R.id.tvRollNo);
        CheckBox cbPresent = listViewItem.findViewById(R.id.cbPresent);

        final Student student = studentList.get(position);

        tvName.setText(student.getName());
        tvRollNo.setText("Roll No: " + student.getRollNo());

        // Checkbox State maintain karna
        cbPresent.setOnCheckedChangeListener(null); // Listener temporarily hatao taaki confusion na ho
        cbPresent.setChecked(student.isPresent);

        // Jab Teacher tick kare, to value update karo
        cbPresent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                student.isPresent = isChecked; // True ya False set karega
            }
        });

        return listViewItem;
    }
}