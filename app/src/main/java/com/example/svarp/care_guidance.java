package com.example.svarp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;

public class care_guidance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ArrayList<String> selectedSymptoms =
                getIntent().getStringArrayListExtra("selected_symptoms");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_care_guidance);

        ConstraintLayout cardHelp = findViewById(R.id.cardHelp);
        cardHelp.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:108"));
            startActivity(intent);
        });

        ConstraintLayout cardReminder   = findViewById(R.id.dailyReminder);
        cardReminder.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, symptoms_info.class);
            intent.putStringArrayListExtra("selected_symptoms", selectedSymptoms);
            startActivity(intent);

        });

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}