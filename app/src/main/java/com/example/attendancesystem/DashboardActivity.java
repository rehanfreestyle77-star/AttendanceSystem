package com.example.attendancesystem;

import android.net.Uri;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    CardView cardAdd, cardAttend, cardView, cardNotify, cardDelete, cardProgress, cardMonthlyReport, cardTimeTable;
    ImageButton btnMenu;
    TextView tvWelcome, tvDate, tvGreeting, tvTeacherSubject, tvAvatarInitialsText;
    LinearLayout lectureContainer;
    TextView tvNoLectures;
    FirebaseAuth auth;
    ImageView ivAvatarProfile;

    // Naya variable role ke liye
    String userRole = "Teacher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Intent se role le rahe hain
        userRole = getIntent().getStringExtra("userRole");
        if (userRole == null) userRole = "Teacher";

        auth = FirebaseAuth.getInstance();

        // Views Mapping
        cardAdd           = findViewById(R.id.cardAddStudent);
        cardAttend        = findViewById(R.id.cardTakeAttendance);
        cardView          = findViewById(R.id.cardViewRecord);
        cardMonthlyReport = findViewById(R.id.cardMonthlyReport);
        cardNotify        = findViewById(R.id.cardNotify);
        cardDelete        = findViewById(R.id.cardDeleteStudent);
        cardProgress      = findViewById(R.id.cardProgress);
        cardTimeTable     = findViewById(R.id.cardTimeTable);
        tvWelcome         = findViewById(R.id.tvWelcome);
        tvDate            = findViewById(R.id.tvDate);
        btnMenu           = findViewById(R.id.btnMenu);
        lectureContainer  = findViewById(R.id.lectureContainer);
        tvNoLectures      = findViewById(R.id.tvNoLectures);
        tvGreeting        = findViewById(R.id.tvGreeting);
        tvTeacherSubject  = findViewById(R.id.tvTeacherSubject);
        tvAvatarInitialsText = findViewById(R.id.tvAvatarInitialsText);
        ivAvatarProfile      = findViewById(R.id.tvAvatarInitials);

        tvGreeting.setText(getGreetingMessage());
        String today = new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(new Date());
        tvDate.setText(today);

        // Listeners
        cardAdd.setOnClickListener(v -> showAddOptionsDialog());
        cardAttend.setOnClickListener(v -> startActivity(new Intent(this, TakeAttendanceActivity.class)));
        cardView.setOnClickListener(v -> startActivity(new Intent(this, ViewAttendanceActivity.class)));
        cardMonthlyReport.setOnClickListener(v -> startActivity(new Intent(this, MonthlyReportActivity.class)));
        cardNotify.setOnClickListener(v -> startActivity(new Intent(this, NotifyParentsActivity.class)));
        cardDelete.setOnClickListener(v -> startActivity(new Intent(this, DeleteStudentActivity.class)));
        cardProgress.setOnClickListener(v -> startActivity(new Intent(this, ProgressActivity.class)));
        cardTimeTable.setOnClickListener(v -> startActivity(new Intent(this, TimeTableActivity.class)));

        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(DashboardActivity.this, v, Gravity.END);
            popup.getMenu().add(0, 1, 0, "👤  Profile");
            popup.getMenu().add(0, 2, 1, "🚪  Log Out");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) startActivity(new Intent(this, ProfileActivity.class));
                else { auth.signOut(); startActivity(new Intent(this, MainActivity.class)); finish(); }
                return true;
            });
            popup.show();
        });

        checkUserRole();
        updateProfileImage();
    }

    private void checkUserRole() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            if ("Principal".equals(userRole)) {
                // Principal UI
                cardAdd.setVisibility(View.VISIBLE);
                cardMonthlyReport.setVisibility(View.VISIBLE);
                cardDelete.setVisibility(View.VISIBLE);
                cardProgress.setVisibility(View.VISIBLE);
                cardTimeTable.setVisibility(View.VISIBLE);
                tvWelcome.setText("Welcome, Principal");
                if (tvAvatarInitialsText != null) tvAvatarInitialsText.setText("PR");
                if (tvTeacherSubject != null) tvTeacherSubject.setVisibility(View.GONE);
                tvNoLectures.setVisibility(View.VISIBLE);
                tvNoLectures.setText("Principal Dashboard Active");
            } else {
                // Teacher UI
                cardAdd.setVisibility(View.GONE);
                cardMonthlyReport.setVisibility(View.GONE);
                cardDelete.setVisibility(View.GONE);
                cardProgress.setVisibility(View.GONE);
                cardTimeTable.setVisibility(View.GONE);

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Teachers").child(user.getUid());
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String name = snapshot.child("name").getValue(String.class);
                            String sub = snapshot.child("subject").getValue(String.class);
                            tvWelcome.setText("Welcome, " + name);
                            if (tvAvatarInitialsText != null) tvAvatarInitialsText.setText(getInitials(name));
                            if (tvTeacherSubject != null) tvTeacherSubject.setText(sub);
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
                setupTeacherAlarms(user.getEmail());
            }
        }
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) return "T";
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) return String.valueOf(words[0].charAt(0)).toUpperCase();
        return (String.valueOf(words[0].charAt(0)) + String.valueOf(words[words.length - 1].charAt(0))).toUpperCase();
    }

    private void setupTeacherAlarms(String email) {
        if (email == null) return;
        String dayName = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
        String safeEmail = email.replace(".", "_");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TimeTable").child(safeEmail).child(dayName);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int count = 0;
                    lectureContainer.removeAllViews();
                    for (DataSnapshot lecture : snapshot.getChildren()) {
                        String subject = lecture.child("subject").getValue(String.class);
                        Integer hour = lecture.child("hour").getValue(Integer.class);
                        Integer minute = lecture.child("minute").getValue(Integer.class);
                        if (hour != null && minute != null) {
                            setAlarmForLecture(subject, hour, minute);
                            addLectureRow(subject, hour, minute);
                            count++;
                        }
                    }
                    tvNoLectures.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                } else {
                    tvNoLectures.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addLectureRow(String subject, int hour, int minute) {
        Calendar now = Calendar.getInstance();
        Calendar lecTime = Calendar.getInstance();
        lecTime.set(Calendar.HOUR_OF_DAY, hour);
        lecTime.set(Calendar.MINUTE, minute);

        String status;
        int dotColor;
        int badgeBg;
        int badgeText;

        long diff = lecTime.getTimeInMillis() - now.getTimeInMillis();
        if (diff < -3600000) {
            status = "Done";
            dotColor = Color.parseColor("#4ADE80");
            badgeBg  = Color.parseColor("#14532D");
            badgeText = Color.parseColor("#4ADE80");
        } else if (diff < 0) {
            status = "Now";
            dotColor = Color.parseColor("#60A5FA");
            badgeBg  = Color.parseColor("#1E3A5F");
            badgeText = Color.parseColor("#60A5FA");
        } else {
            status = "Upcoming";
            dotColor = Color.parseColor("#334155");
            badgeBg  = Color.TRANSPARENT;
            badgeText = Color.parseColor("#475569");
        }

        String timeStr = String.format(Locale.getDefault(), "%d:%02d %s",
                hour > 12 ? hour - 12 : (hour == 0 ? 12 : hour), minute, hour >= 12 ? "PM" : "AM");

        if (lectureContainer.getChildCount() > 0) {
            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.parseColor("#1E2D4A"));
            lectureContainer.addView(divider);
        }

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(0, dpToPx(8), 0, dpToPx(8));
        row.setLayoutParams(rowParams);

        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextSize(11);
        tvTime.setTextColor(Color.parseColor("#64748B"));
        tvTime.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(56), LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(tvTime);

        View dot = new View(this);
        int dotSize = dpToPx(8);
        dot.setLayoutParams(new LinearLayout.LayoutParams(dotSize, dotSize));
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        gd.setColor(dotColor);
        dot.setBackground(gd);
        row.addView(dot);

        TextView tvSub = new TextView(this);
        tvSub.setText(subject);
        tvSub.setTextSize(13);
        tvSub.setTextColor(Color.parseColor("#E2E8F0"));
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        subParams.setMarginStart(dpToPx(10));
        tvSub.setLayoutParams(subParams);
        row.addView(tvSub);

        TextView tvBadge = new TextView(this);
        tvBadge.setText(status);
        tvBadge.setTextSize(10);
        tvBadge.setTextColor(badgeText);
        tvBadge.setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2));
        if (badgeBg != Color.TRANSPARENT) {
            android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
            bg.setColor(badgeBg);
            bg.setCornerRadius(dpToPx(6));
            tvBadge.setBackground(bg);
        }
        row.addView(tvBadge);

        lectureContainer.addView(row);
    }

    private void setAlarmForLecture(String subject, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("subject", "Hurry up! " + subject + " lecture starts in 5 mins.");
        int uniqueId = (int) System.currentTimeMillis() + minute;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.add(Calendar.MINUTE, -5);
        if (c.getTimeInMillis() > System.currentTimeMillis()) {
            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
            }
        }
    }

    private void showAddOptionsDialog() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_add_options);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.findViewById(R.id.optionAddStudent).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, AddStudentActivity.class));
        });
        dialog.findViewById(R.id.optionAddTeacher).setOnClickListener(v -> {
            dialog.dismiss();
            if ("Principal".equals(userRole)) startActivity(new Intent(this, AddTeacherActivity.class));
            else Toast.makeText(this, "Only Principal can add Teachers", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private int dpToPx(int dp) { return Math.round(dp * getResources().getDisplayMetrics().density); }

    private String getGreetingMessage() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good Morning!";
        if (hour < 17) return "Good Afternoon!";
        return "Good Evening!";
    }

    private void updateProfileImage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            SharedPreferences sh = getSharedPreferences("UserProfile", MODE_PRIVATE);
            String savedUriString = sh.getString("profile_image_" + user.getUid(), "");
            if (ivAvatarProfile != null && !savedUriString.isEmpty()) {
                try {
                    ivAvatarProfile.setImageURI(Uri.parse(savedUriString));
                    ivAvatarProfile.setVisibility(View.VISIBLE);
                    if (tvAvatarInitialsText != null) tvAvatarInitialsText.setVisibility(View.GONE);
                } catch (Exception e) { ivAvatarProfile.setVisibility(View.GONE); }
            }
        }
    }

    private void checkForUpdate() { /* Apka original logic yahan paste karein */ }
}