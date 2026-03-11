package com.example.svarp;

import android.content.Intent;
import android.content.SharedPreferences;
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

    // ── English notices ───────────────────────────────────────────────────────────
    private final String[] noticesEn = {
            "Stay hydrated today.",
            "Wash hands frequently.",
            "Take short breaks from screen.",
            "Get 7-8 hours sleep.",
            "Consult doctor if symptoms persist."
    };

    // ── Hindi notices ─────────────────────────────────────────────────────────────
    private final String[] noticesHi = {
            "आज खूब पानी पिएं।",
            "बार-बार हाथ धोएं।",
            "स्क्रीन से थोड़ा ब्रेक लें।",
            "7-8 घंटे की नींद लें।",
            "लक्षण बने रहें तो डॉक्टर से मिलें।"
    };

    private boolean doubleBackToExitPressedOnce = false;
    private Handler noticeHandler;
    private Runnable noticeRunnable;
    private int lastIndex = -1;

    // Views we need to update for language
    private TextView head1, sub1, head2, sub2, head4, sub4, head5, sub5, syncText;
    private MaterialButton btnMic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        // ── Find views ────────────────────────────────────────────────────────────
        btnMic                          = findViewById(R.id.btn_mic);
        ImageView appSetting            = findViewById(R.id.appSetting);
        ConstraintLayout cardSymptoms   = findViewById(R.id.cardSymptoms);
        ConstraintLayout cardHistory    = findViewById(R.id.cardHistory);
        head1  = findViewById(R.id.head1);
        sub1   = findViewById(R.id.sub1);
        head2  = findViewById(R.id.head2);
        sub2   = findViewById(R.id.sub2);
        head4  = findViewById(R.id.head4);
        sub4   = findViewById(R.id.sub4);
        head5  = findViewById(R.id.head5);
        sub5   = findViewById(R.id.sub5);
        syncText = findViewById(R.id.sync);

        // ── Navigation ────────────────────────────────────────────────────────────
        btnMic.setOnClickListener(v ->
                startActivity(new Intent(this, TalkToSvarp.class)));

        appSetting.setOnClickListener(v ->
                startActivity(new Intent(this, setting.class)));

        cardSymptoms.setOnClickListener(v ->
                startActivity(new Intent(this, Select_Symptoms.class)));

        cardHistory.setOnClickListener(v ->
                startActivity(new Intent(this, history.class)));

        // ── Double back to exit ───────────────────────────────────────────────────
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        boolean isHindi = isHindiSelected();
                        if (doubleBackToExitPressedOnce) {
                            finish();
                        } else {
                            doubleBackToExitPressedOnce = true;
                            String msg = isHindi
                                    ? "बाहर निकलने के लिए फिर से दबाएं"
                                    : "Press back again to exit";
                            Toast.makeText(Main_Screen.this, msg, Toast.LENGTH_SHORT).show();
                            new Handler(Looper.getMainLooper()).postDelayed(
                                    () -> doubleBackToExitPressedOnce = false, 2000);
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Called every time we come back to this screen — including after language change
        applyLanguage();
        startNoticeRotation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNoticeRotation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNoticeRotation();
    }

    // ── Read language preference ──────────────────────────────────────────────────
    private boolean isHindiSelected() {
        SharedPreferences prefs = getSharedPreferences(
                LanguageAdapter.PREFS_NAME, MODE_PRIVATE);
        return LanguageAdapter.LANG_HINDI.equals(
                prefs.getString(LanguageAdapter.KEY_LANGUAGE, LanguageAdapter.LANG_ENGLISH));
    }

    // ── Apply language to all visible text ───────────────────────────────────────
    private void applyLanguage() {
        boolean isHindi = isHindiSelected();

        if (isHindi) {
            head1.setText("मैं कैसे मदद कर सकता हूँ?");
            sub1.setText("जल्दी आकलन के लिए अपने लक्षण बताएं");
            btnMic.setText("वॉइस चैट शुरू करें");

            head2.setText("लक्षण चुनें");
            sub2.setText("अपने मौजूदा लक्षण चुनने के लिए टैप करें");

            head4.setText("स्वास्थ्य इतिहास");
            sub4.setText("अपने पिछले आकलन देखें");

            head5.setText("सार्वजनिक स्वास्थ्य सूचना");
            syncText.setText("अंतिम सिंक: 8:22 PM");
        } else {
            head1.setText(getString(R.string.greeting));
            sub1.setText(getString(R.string.tell_symptoms));
            btnMic.setText(getString(R.string.talk));

            head2.setText(getString(R.string.select_Symptoms));
            sub2.setText(getString(R.string.choose_symptoms));

            head4.setText(getString(R.string.history));
            sub4.setText(getString(R.string.view_history));

            head5.setText(getString(R.string.notice));
            syncText.setText(getString(R.string.sync));
        }
    }

    // ── Notice rotation ───────────────────────────────────────────────────────────
    private void startNoticeRotation() {
        stopNoticeRotation(); // clear any existing handler first
        noticeHandler = new Handler(Looper.getMainLooper());
        noticeRunnable = new Runnable() {
            @Override
            public void run() {
                boolean isHindi = isHindiSelected();
                String[] notices = isHindi ? noticesHi : noticesEn;

                int randomIndex;
                do {
                    randomIndex = new Random().nextInt(notices.length);
                } while (randomIndex == lastIndex);

                lastIndex = randomIndex;
                sub5.setText(notices[randomIndex]);

                noticeHandler.postDelayed(this, 5000);
            }
        };
        noticeHandler.post(noticeRunnable);
    }

    private void stopNoticeRotation() {
        if (noticeHandler != null && noticeRunnable != null) {
            noticeHandler.removeCallbacks(noticeRunnable);
            noticeHandler = null;
            noticeRunnable = null;
        }
    }
}