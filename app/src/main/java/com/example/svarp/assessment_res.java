package com.example.svarp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class assessment_res extends AppCompatActivity {

    private TextView txtKeyTitle, txtKeyFindings;
    private TextView actionText1, actionText2, actionText3;
    private RecyclerView riskRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_res);

        initViews();
        setupNavigation();

        ArrayList<String> selectedSymptomNames = getIntent().getStringArrayListExtra("selected_symptoms");
        runAssessment(selectedSymptomNames);
    }

    private void initViews() {
        txtKeyTitle   = findViewById(R.id.txtKeyTitle);
        txtKeyFindings = findViewById(R.id.txtKeyFindings);
        actionText1   = findViewById(R.id.actionText1);
        actionText2   = findViewById(R.id.actionText2);
        actionText3   = findViewById(R.id.actionText3);
        riskRecycler  = findViewById(R.id.riskLevel);
    }

    private void setupNavigation() {
        Button btnHome = findViewById(R.id.btnHome);
        Button btnCare = findViewById(R.id.btnCare);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, Main_Screen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnCare.setOnClickListener(v ->
                startActivity(new Intent(this, care_guidance.class))
        );

        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void runAssessment(ArrayList<String> selectedSymptomNames) {
        // Read saved language preference
        SharedPreferences prefs = getSharedPreferences(LanguageAdapter.PREFS_NAME, MODE_PRIVATE);
        String lang = prefs.getString(LanguageAdapter.KEY_LANGUAGE, LanguageAdapter.LANG_ENGLISH);
        boolean isHindi = LanguageAdapter.LANG_HINDI.equals(lang);

        // Create engine with correct language
        HealthDecisionEngine engine = new HealthDecisionEngine(isHindi);

        // Convert symptom names to enum
        List<HealthDecisionEngine.Symptom> symptoms = convertToSymptoms(selectedSymptomNames);

        // Analyze
        HealthDecisionEngine.HealthAssessment result = engine.analyzeInput(symptoms);

        // Update UI
        updateRiskLevel(result.riskLevel);
        updateKeyFindings(result, isHindi);
        updateActionSteps(result.actionSteps);
    }

    private List<HealthDecisionEngine.Symptom> convertToSymptoms(ArrayList<String> names) {
        List<HealthDecisionEngine.Symptom> symptoms = new ArrayList<>();
        if (names == null) return symptoms;

        for (String name : names) {
            switch (name.toLowerCase().trim()) {
                case "fever":                  symptoms.add(HealthDecisionEngine.Symptom.FEVER); break;
                case "cough":                  symptoms.add(HealthDecisionEngine.Symptom.COUGH); break;
                case "headache":               symptoms.add(HealthDecisionEngine.Symptom.HEADACHE); break;
                case "fatigue":                symptoms.add(HealthDecisionEngine.Symptom.FATIGUE); break;
                case "sore throat":            symptoms.add(HealthDecisionEngine.Symptom.SORE_THROAT); break;
                case "vomiting": case "vomit": symptoms.add(HealthDecisionEngine.Symptom.VOMITING); break;
                case "body ache":              symptoms.add(HealthDecisionEngine.Symptom.BODY_ACHE); break;
                case "dizziness":              symptoms.add(HealthDecisionEngine.Symptom.DIZZINESS); break;
                case "skin rash":              symptoms.add(HealthDecisionEngine.Symptom.SKIN_RASH); break;
                case "eye discomfort":         symptoms.add(HealthDecisionEngine.Symptom.EYE_DISCOMFORT); break;
                case "toothache":              symptoms.add(HealthDecisionEngine.Symptom.TOOTHACHE); break;
                case "chest pain":             symptoms.add(HealthDecisionEngine.Symptom.CHEST_PAIN); break;
                case "shortness of breath": case "breathing":
                    symptoms.add(HealthDecisionEngine.Symptom.SHORTNESS_OF_BREATH); break;
                case "nausea":                 symptoms.add(HealthDecisionEngine.Symptom.NAUSEA); break;
                case "weakness":               symptoms.add(HealthDecisionEngine.Symptom.WEAKNESS); break;
                case "weight loss":            symptoms.add(HealthDecisionEngine.Symptom.WEIGHT_LOSS); break;
                case "blood in stool": case "blood stool":
                    symptoms.add(HealthDecisionEngine.Symptom.BLOOD_IN_STOOL); break;
                case "blood in urine": case "peeblood":
                    symptoms.add(HealthDecisionEngine.Symptom.BLOOD_IN_URINE); break;
                case "diarrhea":               symptoms.add(HealthDecisionEngine.Symptom.DIARRHEA); break;
                case "stomach ache": case "stomach pain":
                    symptoms.add(HealthDecisionEngine.Symptom.STOMACH_ACHE); break;
            }
        }
        return symptoms;
    }

    private void updateRiskLevel(HealthDecisionEngine.RiskLevel risk) {
        riskRecycler.setLayoutManager(new LinearLayoutManager(this));
        riskRecycler.setNestedScrollingEnabled(false);
        RiskAdapter adapter = new RiskAdapter(risk.name());
        riskRecycler.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void updateKeyFindings(HealthDecisionEngine.HealthAssessment result, boolean isHindi) {
        if (result.isEmergency) {
            String prefix = isHindi ? "⚠️ ज़रूरी: " : "⚠️ URGENT: ";
            txtKeyTitle.setText(prefix + result.condition);
            txtKeyTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            String prefix = isHindi ? "आकलन: " : "Assessment: ";
            txtKeyTitle.setText(prefix + result.condition);
        }
        txtKeyFindings.setText(result.explanation);
    }

    @SuppressLint("SetTextI18n")
    private void updateActionSteps(List<String> actions) {
        actionText1.setText("");
        actionText2.setText("");
        actionText3.setText("");

        if (!actions.isEmpty()) actionText1.setText("1️⃣ " + actions.get(0));
        if (actions.size() >= 2) actionText2.setText("2️⃣ " + actions.get(1));
        if (actions.size() >= 3) actionText3.setText("3️⃣ " + actions.get(2));

        if (actions.size() > 3) {
            StringBuilder extra = new StringBuilder(actionText3.getText().toString());
            for (int i = 3; i < actions.size(); i++) {
                extra.append("\n\n").append((i + 1)).append("️⃣ ").append(actions.get(i));
            }
            actionText3.setText(extra.toString());
        }
    }
}