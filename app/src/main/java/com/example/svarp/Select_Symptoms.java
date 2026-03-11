package com.example.svarp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;

public class Select_Symptoms extends AppCompatActivity {

    private static final int GRID_SPAN_COUNT = 2;
    private Button btn_report;
    private SymptomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_symptoms);

        btn_report = findViewById(R.id.btn_report);

        applyWindowInsets();
        setupBackButton();
        setupRecyclerView();
        setupReportButton();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void setupReportButton() {
        btn_report.setOnClickListener(v -> {
            String[] symptomsArray = adapter.getSelectedSymptoms();
            ArrayList<String> selectedSymptoms = new ArrayList<>(Arrays.asList(symptomsArray));
            Intent intent = new Intent(Select_Symptoms.this, assessment_res.class);
            intent.putStringArrayListExtra("selected_symptoms", selectedSymptoms);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.symptomList);
        btn_report.setVisibility(Button.GONE);

        // Check saved language
        SharedPreferences prefs = getSharedPreferences(LanguageAdapter.PREFS_NAME, MODE_PRIVATE);
        String lang = prefs.getString(LanguageAdapter.KEY_LANGUAGE, LanguageAdapter.LANG_ENGLISH);
        boolean isHindi = LanguageAdapter.LANG_HINDI.equals(lang);

        // English symptom names — these are the keys used for engine mapping
        String[] englishSymptoms = {
                "Fever",
                "Cough",
                "Headache",
                "Fatigue",
                "Sore Throat",
                "Vomiting",
                "Body Ache",
                "Dizziness",
                "Skin Rash",
                "Eye Discomfort",
                "Toothache",
                "Chest Pain",
                "Shortness of Breath",
                "Nausea",
                "Weakness",
                "Weight Loss",
                "Blood in Stool",
                "Blood in Urine",
                "Diarrhea",
                "Stomach Ache"
        };

        // Hindi symptom names (same order)
        String[] hindiSymptoms = {
                "बुखार",
                "खांसी",
                "सिरदर्द",
                "थकान",
                "गले में दर्द",
                "उल्टी",
                "बदन दर्द",
                "चक्कर आना",
                "त्वचा पर चकत्ते",
                "आँखों में तकलीफ",
                "दांत दर्द",
                "सीने में दर्द",
                "सांस लेने में तकलीफ",
                "जी मिचलाना",
                "कमज़ोरी",
                "वज़न कम होना",
                "मल में खून",
                "पेशाब में खून",
                "दस्त",
                "पेट दर्द"
        };

        // Always pass English names to the adapter internally for engine mapping
        // but display Hindi names if Hindi is selected
        String[] displaySymptoms = isHindi ? hindiSymptoms : englishSymptoms;

        int[] icons = {
                R.drawable.fever,
                R.drawable.cough,
                R.drawable.headache,
                R.drawable.fatigue,
                R.drawable.sore_throat,
                R.drawable.vomit,
                R.drawable.body_ache,
                R.drawable.dizziness,
                R.drawable.skin_rash,
                R.drawable.eye_discomfort,
                R.drawable.toothache,
                R.drawable.chest_pain,
                R.drawable.breathing,
                R.drawable.nausea,
                R.drawable.weakness,
                R.drawable.weight_loss,
                R.drawable.blood_stool,
                R.drawable.peeblood,
                R.drawable.diarrhea,
                R.drawable.stomach_ache
        };

        recyclerView.setLayoutManager(new GridLayoutManager(this, GRID_SPAN_COUNT));

        // Pass English names to adapter so engine mapping always works
        // but show Hindi display names on screen
        adapter = new SymptomAdapter(
                englishSymptoms,  // used internally for engine
                displaySymptoms,  // shown on screen
                icons,
                selectedCount -> {
                    if (selectedCount > 0) {
                        btn_report.setVisibility(Button.VISIBLE);
                        if (isHindi) {
                            btn_report.setText("आगे बढ़ें (" + selectedCount + " चुने गए)");
                        } else {
                            btn_report.setText("Continue (" + selectedCount + " Selected)");
                        }
                    } else {
                        btn_report.setVisibility(Button.GONE);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }
}