package com.example.svarp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class TalkToSvarp extends AppCompatActivity {

    private View rippleView;
    private Button btnStop;
    private TextView heading, transcript, helper;

    private SpeechRecognizer speechRecognizer;
    private Intent speechIntent;
    private boolean isListening = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk_to_svarp);

        ImageView btnMic   = findViewById(R.id.btn_mic);
        ImageView btnClose = findViewById(R.id.BtnClose);
        rippleView         = findViewById(R.id.ripple_view);
        btnStop            = findViewById(R.id.btn_stop);
        heading            = findViewById(R.id.heading);
        transcript         = findViewById(R.id.transcript);
        helper             = findViewById(R.id.helper);

        // ── Speech recognizer setup ───────────────────────────────────────────────
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                java.util.Locale.getDefault());
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                stopListening();
            }

            @Override
            public void onResults(Bundle results) {
                updateTranscript(results);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                updateTranscript(partialResults);
            }

            @Override public void onEvent(int eventType, Bundle params) {}
        });

        // ── Mic button ────────────────────────────────────────────────────────────
        btnMic.setOnClickListener(v -> startListening());

        // ── Stop button — detect symptoms and go to assessment ────────────────────
        btnStop.setOnClickListener(v -> {
            stopListening();

            String spoken = transcript.getText().toString().toLowerCase();
            ArrayList<String> detectedSymptoms = new ArrayList<>();

            // Match spoken keywords to symptom names
            if (spoken.contains("fever")       || spoken.contains("बुखार"))               detectedSymptoms.add("Fever");
            if (spoken.contains("cough")       || spoken.contains("खांसी"))               detectedSymptoms.add("Cough");
            if (spoken.contains("headache")    || spoken.contains("सिरदर्द"))             detectedSymptoms.add("Headache");
            if (spoken.contains("fatigue")     || spoken.contains("tired")
                    || spoken.contains("थकान"))                detectedSymptoms.add("Fatigue");
            if (spoken.contains("sore throat") || spoken.contains("throat")
                    || spoken.contains("गले"))                 detectedSymptoms.add("Sore Throat");
            if (spoken.contains("vomit")       || spoken.contains("उल्टी"))               detectedSymptoms.add("Vomiting");
            if (spoken.contains("body ache")   || spoken.contains("muscle")
                    || spoken.contains("बदन दर्द"))            detectedSymptoms.add("Body Ache");
            if (spoken.contains("dizzy")       || spoken.contains("dizziness")
                    || spoken.contains("चक्कर"))               detectedSymptoms.add("Dizziness");
            if (spoken.contains("rash")        || spoken.contains("चकत्ते"))              detectedSymptoms.add("Skin Rash");
            if (spoken.contains("eye")         || spoken.contains("आँख"))                 detectedSymptoms.add("Eye Discomfort");
            if (spoken.contains("tooth")       || spoken.contains("दांत"))               detectedSymptoms.add("Toothache");
            if (spoken.contains("chest")       || spoken.contains("सीने"))               detectedSymptoms.add("Chest Pain");
            if (spoken.contains("breath")      || spoken.contains("breathing")
                    || spoken.contains("सांस"))                detectedSymptoms.add("Shortness of Breath");
            if (spoken.contains("nausea")      || spoken.contains("nauseous")
                    || spoken.contains("जी मिचलाना"))          detectedSymptoms.add("Nausea");
            if (spoken.contains("weak")        || spoken.contains("weakness")
                    || spoken.contains("कमज़ोरी"))             detectedSymptoms.add("Weakness");
            if (spoken.contains("weight loss") || spoken.contains("वज़न"))               detectedSymptoms.add("Weight Loss");
            if (spoken.contains("blood in stool")  || spoken.contains("मल में खून"))     detectedSymptoms.add("Blood in Stool");
            if (spoken.contains("blood in urine")  || spoken.contains("पेशाब में खून"))  detectedSymptoms.add("Blood in Urine");
            if (spoken.contains("diarrhea")    || spoken.contains("loose")
                    || spoken.contains("दस्त"))                detectedSymptoms.add("Diarrhea");
            if (spoken.contains("stomach")     || spoken.contains("abdomen")
                    || spoken.contains("पेट"))                 detectedSymptoms.add("Stomach Ache");

            // Pass symptoms the same way Select_Symptoms does
            Intent intent = new Intent(TalkToSvarp.this, assessment_res.class);
            intent.putStringArrayListExtra("selected_symptoms", detectedSymptoms);
            startActivity(intent);
        });

        btnClose.setOnClickListener(v -> finish());
    }

    // ── Start listening ───────────────────────────────────────────────────────────
    @SuppressLint("SetTextI18n")
    private void startListening() {
        if (isListening) return;
        isListening = true;
        heading.setText("I'm listening");
        transcript.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        helper.setVisibility(View.VISIBLE);
        rippleView.setVisibility(View.VISIBLE);
        speechRecognizer.startListening(speechIntent);
    }

    // ── Stop listening ────────────────────────────────────────────────────────────
    private void stopListening() {
        if (!isListening) return;
        isListening = false;
        rippleView.setVisibility(View.GONE);
        speechRecognizer.stopListening();
    }

    // ── Detect script language ────────────────────────────────────────────────────
    private String detectLanguage(String text) {
        for (char c : text.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.DEVANAGARI) {
                return "hindi";
            }
        }
        return "english";
    }

    // ── Update transcript and show live hint ──────────────────────────────────────
    private void updateTranscript(Bundle bundle) {
        ArrayList<String> results =
                bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (results != null && !results.isEmpty()) {
            String spokenText = results.get(0);
            transcript.setText(spokenText);

            String language = detectLanguage(spokenText);
            if (language.equals("hindi")) {
                helper.setText("बोलते रहें — जब हो जाए तो Stop दबाएं");
            } else {
                helper.setText("Keep speaking — press Stop when you're done");
            }
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────────
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}