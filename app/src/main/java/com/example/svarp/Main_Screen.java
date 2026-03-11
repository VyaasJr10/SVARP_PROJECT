package com.example.svarp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class Main_Screen extends AppCompatActivity {

    private final String[] notices = {
            "Stay hydrated today.",
            "Wash hands frequently.",
            "Take short breaks from screen.",
            "Get 7-8 hours sleep.",
            "Consult doctor if symptoms persist."
    };
    private boolean doubleBackToExitPressedOnce = false;

    private Handler noticeHandler;
    private Runnable noticeRunnable;
    private int lastIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // Views
        MaterialButton btnMic = findViewById(R.id.btn_mic);
        ImageView appSetting = findViewById(R.id.appSetting);
        ConstraintLayout cardSymptoms = findViewById(R.id.cardSymptoms);
        ConstraintLayout cardHistory = findViewById(R.id.cardHistory);
        TextView noticeText = findViewById(R.id.sub5);


        btnMic.setOnClickListener(v ->
                startActivity(new Intent(this, TalkToSvarp.class))
        );

        appSetting.setOnClickListener(v ->
                startActivity(new Intent(this, setting.class))
        );

        cardSymptoms.setOnClickListener(v ->
                startActivity(new Intent(this, Select_Symptoms.class))
        );

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, history.class))
        );

        // -------------------------
        // Random Notice Logic
        // -------------------------

        noticeHandler = new Handler(Looper.getMainLooper());

        noticeRunnable = new Runnable() {
            @Override
            public void run() {

                int randomIndex;
                do {
                    randomIndex = new Random().nextInt(notices.length);
                } while (randomIndex == lastIndex);

                lastIndex = randomIndex;
                noticeText.setText(notices[randomIndex]);

                noticeHandler.postDelayed(this, 5000);
            }
        };

        noticeHandler.post(noticeRunnable);

        // -------------------------
        // Double Back To Exit (Modern)
        // -------------------------

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        if (doubleBackToExitPressedOnce) {
                            finish();
                        } else {
                            doubleBackToExitPressedOnce = true;
                            Toast.makeText(Main_Screen.this,
                                    "Press back again to exit",
                                    Toast.LENGTH_SHORT).show();

                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> doubleBackToExitPressedOnce = false,
                                    2000
                            );
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noticeHandler != null && noticeRunnable != null) {
            noticeHandler.removeCallbacks(noticeRunnable);
        }
    }
}
