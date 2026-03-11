package com.example.svarp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class assessment_res extends AppCompatActivity {

    private TextView txtKeyTitle, txtKeyFindings;
    private TextView actionText1, actionText2, actionText3;
    private RecyclerView riskRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_res);

        // Initialize views
        initViews();

        // Setup navigation
        setupNavigation();

        // Get input from previous activities
        String voiceInput = getIntent().getStringExtra("voice_text");
        ArrayList<String> selectedSymptoms = getIntent().getStringArrayListExtra("selected_symptoms");

        // Run health assessment
        runAssessment(voiceInput, selectedSymptoms);
    }

    private void initViews() {
        txtKeyTitle = findViewById(R.id.txtKeyTitle);
        txtKeyFindings = findViewById(R.id.txtKeyFindings);
        actionText1 = findViewById(R.id.actionText1);
        actionText2 = findViewById(R.id.actionText2);
        actionText3 = findViewById(R.id.actionText3);
        riskRecycler = findViewById(R.id.riskLevel);
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

    private void runAssessment(String voiceInput, ArrayList<String> selectedSymptoms) {
        // Create decision engine
        HealthDecisionEngine engine = new HealthDecisionEngine();

        // Convert to list (handle null)
        List<String> symptoms = (selectedSymptoms != null) ? selectedSymptoms : new ArrayList<>();

        // Analyze
        HealthDecisionEngine.HealthAssessment result = engine.analyzeInput(
                voiceInput != null ? voiceInput : "",
                symptoms
        );

        // Update UI
        updateRiskLevel(result.riskLevel);
        updateKeyFindings(result);
        updateActionSteps(result.actionSteps);
    }

    private void updateRiskLevel(String risk) {
        riskRecycler.setLayoutManager(new LinearLayoutManager(this));
        riskRecycler.setNestedScrollingEnabled(false);
        RiskAdapter adapter = new RiskAdapter(risk);
        riskRecycler.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void updateKeyFindings(HealthDecisionEngine.HealthAssessment result) {
        // Dynamic title based on severity
        if (result.isEmergency) {
            txtKeyTitle.setText("⚠️ URGENT: " + result.condition);
            txtKeyTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            txtKeyTitle.setText("Assessment: " + result.condition);
        }

        // Human-readable explanation
        txtKeyFindings.setText(result.explanation);
    }

    @SuppressLint("SetTextI18n")
    private void updateActionSteps(List<String> actions) {
        // Clear all first
        actionText1.setText("");
        actionText2.setText("");
        actionText3.setText("");

        // Fill available actions (up to 3)
        if (!actions.isEmpty()) actionText1.setText("1️⃣ " + actions.get(0));
        if (actions.size() >= 2) actionText2.setText("2️⃣ " + actions.get(1));
        if (actions.size() >= 3) actionText3.setText("3️⃣ " + actions.get(2));

        // If we have more than 3, append to the third one
        if (actions.size() > 3) {
            StringBuilder extra = new StringBuilder(actionText3.getText().toString());
            for (int i = 3; i < actions.size(); i++) {
                extra.append("\n\n").append((i + 1)).append("️⃣ ").append(actions.get(i));
            }
            actionText3.setText(extra.toString());
        }
    }
}