package com.example.attendancesystem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<Student> {

    private Activity context;
    private List<Student> studentList;

    public RecordAdapter(Activity context, List<Student> studentList) {
        super(context, R.layout.item_attendance_record, studentList);
        this.context = context;
        this.studentList = studentList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.item_attendance_record, null, true);

        TextView tvName = listViewItem.findViewById(R.id.tvRecordName);
        TextView tvStatus = listViewItem.findViewById(R.id.tvRecordStatus);
        ImageView btnWhatsApp = listViewItem.findViewById(R.id.btnWhatsApp); // Button dhoonda

        final Student student = studentList.get(position);

        tvName.setText(student.getName());
        tvStatus.setText(student.getStatus());

        // LOGIC: Agar Absent hai, to Red Color aur WhatsApp button dikhao
        if ("Absent".equals(student.getStatus())) {
            tvStatus.setTextColor(Color.parseColor("#F44336")); // Red
            btnWhatsApp.setVisibility(View.VISIBLE); // Button Dikhayo
        } else {
            tvStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            btnWhatsApp.setVisibility(View.GONE); // Button Chupao
        }

        // WhatsApp Click Listener
        btnWhatsApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatsApp(student.getName());
            }
        });

        return listViewItem;
    }

    private void openWhatsApp(String studentName) {
        try {
            // Message taiyar karna
            String message = "Hello, This is from School. Your child " + studentName + " is absent today. Please provide a reason.";

            // WhatsApp kholne ka code (Intent)
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://api.whatsapp.com/send?text=" + message));
            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp not installed!", Toast.LENGTH_SHORT).show();
        }
    }
}