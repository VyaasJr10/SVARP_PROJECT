package com.example.svarp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Select_Symptoms extends AppCompatActivity {

    private static final int GRID_SPAN_COUNT = 2;
    private Button btn_report;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_symptoms);

        btn_report = findViewById(R.id.btn_report);

        applyWindowInsets();
        setupBackButton();
        setupReportButton();
        setupRecyclerView();
    }

    private void applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });
    }

    private void setupBackButton() {
        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void setupReportButton() {
        btn_report.setOnClickListener(v -> {
            Intent intent = new Intent(Select_Symptoms.this, assessment_res.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.symptomList);

        btn_report.setVisibility(Button.GONE);

        String[] symptoms = {
                "Fever",
                "Cough",
                "Headache",
                "Fatigue",
                "Sore throat",
                "Vomiting",
                "Body Ache",
                "Dizziness",
                "Skin rash",
                "Eye discomfort",
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

        recyclerView.setLayoutManager(
                new GridLayoutManager(this, GRID_SPAN_COUNT)
        );

        @SuppressLint("SetTextI18n") SymptomAdapter adapter = new SymptomAdapter(
                symptoms,
                icons,
                selectedCount -> {
                    if (selectedCount > 0) {
                        btn_report.setVisibility(Button.VISIBLE);
                        btn_report.setText(
                                "Continue (" + selectedCount + " Selected)"
                        );
                    } else {
                        btn_report.setVisibility(Button.GONE);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }
}
