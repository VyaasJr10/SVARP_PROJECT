package com.example.svarp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ConstraintLayout card1 = findViewById(R.id.card1);
        ConstraintLayout card2 = findViewById(R.id.card2);
        ConstraintLayout card3 = findViewById(R.id.card3);


        card1.setOnClickListener(v ->
                startActivity(new Intent(this, language_selection.class))
        );
        card2.setOnClickListener(v ->
                startActivity(new Intent(this, about.class))
        );
        card3.setOnClickListener(v ->
                startActivity(new Intent(this, privacy.class))
        );
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}