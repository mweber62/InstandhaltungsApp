package com.example.arcoreimage;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;


import android.view.View;
import android.widget.Button;

//Hauptmenu
public class MainActivity extends AppCompatActivity {

    // Layout-Elemente
    private Button startButton, settingsButton, informationButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // zuweisen der Layout-Elemente
        startButton = findViewById(R.id.startButton);
        settingsButton = findViewById(R.id.settingsButton);
        informationButton = findViewById(R.id.informationButton);

        // Kamera-Permission abfragen
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, 100);

        // OnClick für das Starten der App
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               switchToMachineScanning();
            }
        });

        // OnClick fuer das wechseln in das Einstellungs-Fenster
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSettings();
            }
        });

        // OnClick fuer das wechseln in das InformationsFenster
        informationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToInformation();
            }
        });

    }

    // Intent zum wechseln in die InformationsActivity
    private void switchToInformation() {
        Intent i = new Intent(this, InformationActivity.class);
        startActivity(i);
    }

    // Intent zum wechseln in die SettingsActivity
    private void switchToSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    // Intent zum wechseln in die MachineScanningActivity
    private void switchToMachineScanning() {
        Intent i = new Intent(this, MachineScanningActivity.class);
        startActivity(i);
    }


}