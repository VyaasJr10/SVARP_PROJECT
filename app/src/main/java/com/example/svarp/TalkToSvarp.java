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

        ImageView btnMic = findViewById(R.id.btn_mic);
        ImageView btnClose = findViewById(R.id.BtnClose);
        rippleView = findViewById(R.id.ripple_view);
        btnStop = findViewById(R.id.btn_stop);
        heading = findViewById(R.id.heading);
        transcript = findViewById(R.id.transcript);
        helper = findViewById(R.id.helper);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
            }

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

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        btnMic.setOnClickListener(v -> startListening());

        btnStop.setOnClickListener(v -> {
            stopListening();
            Intent intent = new Intent(TalkToSvarp.this, assessment_res.class);
            intent.putExtra("voice_text", transcript.getText().toString());
            startActivity(intent);
        });

        btnClose.setOnClickListener(v -> finish());
    }

    @SuppressLint("SetTextI18n")
    private void startListening() {
        if (isListening) return;

        isListening = true;
        heading.setText("I'm listening....");
        transcript.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        helper.setVisibility(View.VISIBLE);
        rippleView.setVisibility(View.VISIBLE);

        speechRecognizer.startListening(speechIntent);
    }

    private void stopListening() {
        if (!isListening) return;

        isListening = false;
        rippleView.setVisibility(View.GONE);
        speechRecognizer.stopListening();
    }

    private void updateTranscript(Bundle bundle) {
        ArrayList<String> results =
                bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (results != null && !results.isEmpty()) {
            transcript.setText(results.get(0));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
