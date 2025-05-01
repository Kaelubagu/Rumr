package com.example.rumrapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeTxt, userIdTxt, boxTxt;
    private ImageView logoImg;
    private Button enterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        welcomeTxt = findViewById(R.id.textView2);
        userIdTxt   = findViewById(R.id.textView3);
        logoImg     = findViewById(R.id.imageView);
        boxTxt      = findViewById(R.id.textView);
        enterBtn    = findViewById(R.id.button);

        enterBtn.setOnClickListener(v -> animateOut());

    }
    // animation funnnn!
    private void animateOut() {
        // staggered fade & slide upward
        long duration = 300;
        welcomeTxt.animate()
                .alpha(0f)
                .translationYBy(-50)
                .setDuration(duration)
                .start();

        userIdTxt.animate()
                .alpha(0f)
                .translationYBy(-50)
                .setStartDelay(50)
                .setDuration(duration)
                .start();

        logoImg.animate()
                .alpha(0f)
                .scaleX(0.5f).scaleY(0.5f)
                .setStartDelay(100)
                .setDuration(duration)
                .start();

        boxTxt.animate()
                .alpha(0f)
                .translationYBy(100)
                .setStartDelay(150)
                .setDuration(duration)
                .withEndAction(() -> {

                    welcomeTxt.setVisibility(View.GONE);
                    userIdTxt.setVisibility(View.GONE);
                    logoImg.setVisibility(View.GONE);
                    boxTxt.setVisibility(View.GONE);

                    Intent go = new Intent(MainActivity.this, ChatroomActivity.class);
                    startActivity(go);
                    finish();
                })
                .start();

        enterBtn.animate()
                .alpha(0f)
                .setStartDelay(200)
                .setDuration(duration)
                .withEndAction(() -> enterBtn.setVisibility(View.GONE))
                .start();
    }
}