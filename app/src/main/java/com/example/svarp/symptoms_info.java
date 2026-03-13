package com.example.svarp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class symptoms_info extends AppCompatActivity {

    private TextView txtSymptoms;
    private TextView txtInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_symptoms_info);

        ImageView btnBack = findViewById(R.id.btnBack);
        txtSymptoms = findViewById(R.id.txtSymptoms);
        txtInfo = findViewById(R.id.txtInfo);

        btnBack.setOnClickListener(v -> finish());

        // Receive symptoms from care_guidance
        ArrayList<String> symptoms =
                getIntent().getStringArrayListExtra("selected_symptoms");

        if (symptoms != null && !symptoms.isEmpty()) {

            StringBuilder builder = new StringBuilder();

            for (String s : symptoms) {
                builder.append("• ").append(s).append("\n");
            }

            txtSymptoms.setText(builder.toString());
            txtInfo.setText(generateInfo(symptoms));
        }
    }

    /**
     * Generates simple explanation text for selected symptoms
     */
    private String generateInfo(ArrayList<String> symptoms){

        StringBuilder info = new StringBuilder();

        for(String s : symptoms){

            switch(s){

                case "Fever":
                    info.append("Fever: A temporary increase in body temperature, often due to infection.\n\n");
                    break;

                case "Cough":
                    info.append("Cough: A reflex to clear the throat or airways. Often caused by infections or irritation.\n\n");
                    break;

                case "Headache":
                    info.append("Headache: Pain in the head that can occur due to stress, dehydration, illness, or fatigue.\n\n");
                    break;

                case "Fatigue":
                    info.append("Fatigue: Extreme tiredness that may result from illness, lack of sleep, or stress.\n\n");
                    break;

                case "Sore Throat":
                    info.append("Sore Throat: Pain or irritation in the throat, often caused by viral infections.\n\n");
                    break;

                case "Vomiting":
                    info.append("Vomiting: The forceful expulsion of stomach contents, often caused by infection or food poisoning.\n\n");
                    break;

                case "Body Ache":
                    info.append("Body Ache: Muscle pain that may occur during infections like flu or due to physical strain.\n\n");
                    break;

                case "Dizziness":
                    info.append("Dizziness: A feeling of lightheadedness that may result from dehydration or low blood pressure.\n\n");
                    break;

                case "Skin Rash":
                    info.append("Skin Rash: Changes in skin color or texture caused by allergies, infections, or irritation.\n\n");
                    break;

                case "Eye Discomfort":
                    info.append("Eye Discomfort: May include itching, redness, or irritation due to strain or infection.\n\n");
                    break;

                case "Toothache":
                    info.append("Toothache: Pain in or around a tooth caused by decay, infection, or gum disease.\n\n");
                    break;

                case "Chest Pain":
                    info.append("Chest Pain: Can range from mild discomfort to serious conditions like heart problems. Seek medical advice if severe.\n\n");
                    break;

                case "Shortness of Breath":
                    info.append("Shortness of Breath: Difficulty breathing that may indicate respiratory or heart issues.\n\n");
                    break;

                case "Nausea":
                    info.append("Nausea: A feeling of sickness with the urge to vomit, often related to stomach problems.\n\n");
                    break;

                case "Weakness":
                    info.append("Weakness: Reduced strength that may result from illness, fatigue, or nutritional deficiency.\n\n");
                    break;

                case "Weight Loss":
                    info.append("Weight Loss: Unintentional weight loss may indicate metabolic issues or chronic illness.\n\n");
                    break;

                case "Blood in Stool":
                    info.append("Blood in Stool: May indicate digestive tract problems and should be checked by a doctor.\n\n");
                    break;

                case "Blood in Urine":
                    info.append("Blood in Urine: Could indicate urinary tract infection or kidney problems.\n\n");
                    break;

                case "Diarrhea":
                    info.append("Diarrhea: Frequent loose stools often caused by infection, food intolerance, or digestive issues.\n\n");
                    break;

                case "Stomach Ache":
                    info.append("Stomach Ache: Pain in the abdomen that may result from indigestion, infection, or cramps.\n\n");
                    break;
            }
        }

        if(info.length() == 0){
            info.append("Monitor your symptoms and consult a healthcare professional if they persist.");
        }

        return info.toString();
    }
}