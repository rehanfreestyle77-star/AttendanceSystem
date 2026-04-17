package com.example.attendancesystem;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.List;

public class ProgressAdapter extends ArrayAdapter<ProgressActivity.StudentProgress> {

    private Activity context;
    private List<ProgressActivity.StudentProgress> progressList;

    public ProgressAdapter(Activity context, List<ProgressActivity.StudentProgress> progressList) {
        super(context, R.layout.item_progress, progressList);
        this.context = context;
        this.progressList = progressList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.item_progress, null, true);

        TextView tvName = listViewItem.findViewById(R.id.tvName);
        TextView tvRoll = listViewItem.findViewById(R.id.tvRoll);
        TextView tvPercentage = listViewItem.findViewById(R.id.tvPercentage);
        TextView tvStats = listViewItem.findViewById(R.id.tvStats);
        ProgressBar progressBar = listViewItem.findViewById(R.id.progressBar);

        ProgressActivity.StudentProgress currentItem = progressList.get(position);

        tvName.setText(currentItem.name);
        tvRoll.setText("Roll No: " + currentItem.rollNo);
        tvPercentage.setText(currentItem.percentage + "%");
        tvStats.setText("Present: " + currentItem.presentCount + " | Total Lectures: " + currentItem.totalCount);

        // Progress Bar Update
        progressBar.setProgress(currentItem.percentage);

        // COLOR LOGIC:
        // > 75% = Green
        // 50% - 75% = Orange
        // < 50% = Red
        if (currentItem.percentage >= 75) {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))); // Green
            tvPercentage.setTextColor(Color.parseColor("#4CAF50"));
        } else if (currentItem.percentage >= 50) {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FF9800"))); // Orange
            tvPercentage.setTextColor(Color.parseColor("#FF9800"));
        } else {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))); // Red
            tvPercentage.setTextColor(Color.parseColor("#F44336"));
        }

        return listViewItem;
    }
}