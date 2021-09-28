package com.example.arcoreimage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InformationActivity extends AppCompatActivity {

    private Button backToStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        backToStartButton = findViewById(R.id.backToStartButton);

        backToStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToStartAcitvity();
            }
        });
    }

    private void switchToStartAcitvity() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}