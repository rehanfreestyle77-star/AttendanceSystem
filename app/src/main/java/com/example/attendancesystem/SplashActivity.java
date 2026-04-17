package com.example.attendancesystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Upar wali Bar (ActionBar) chupana
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 3 Second (3000ms) ka delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Login Screen par jana
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Taaki Back dabane par Splash wapas na aaye
            }
        }, 3000);
    }
}