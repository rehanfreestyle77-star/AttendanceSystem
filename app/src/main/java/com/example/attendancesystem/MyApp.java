package com.example.attendancesystem;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Yeh line App ko Offline kaam karne ki taqat deti hai
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}