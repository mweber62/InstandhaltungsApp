package com.example.arcoreimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.arcoreimage.Classes.MachineInfo;

/**
 * Anzeigen der Maschinen-Informationen
 */
public class MachineInfoActivity extends AppCompatActivity  {

    // TAG für das Loggen
    private String TAG = "MachineInfoActivty";

    // Layout-Elemente
    private Button setupAssistentButton, setupCreaterButton;
    private TextView machineId, manufacturer, machineName, machineType, communicationInterace, isProductFileRequired, isMaterialRequired, validFileName, validFileExtension, tcpServerAddress;
    private MachineInfo mInfo;
    private Spinner setupSpinner;

    // Variable zum speichern des ausgewählten Setups
    private String selectedSAID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_info);

        // Layout-Elemente zuweisen
        machineId = findViewById(R.id.machineIDTextView);
        manufacturer = findViewById(R.id.manufacturerTextView);
        machineName = findViewById(R.id.machineNameTextView);
        machineType = findViewById(R.id.machineTypeTextView);
        communicationInterace = findViewById(R.id.communicationInterfaceTextView);
        isProductFileRequired = findViewById(R.id.isProductionFileRequiredTextView);
        isMaterialRequired = findViewById(R.id.isMaterialTextView);
        validFileName = findViewById(R.id.validFileNameTextView);
        validFileExtension = findViewById(R.id.validFileExtensionTextView);
        tcpServerAddress = findViewById(R.id.tcpServerAdressTextView);
        setupSpinner = findViewById(R.id.setupSpinner);
        setupCreaterButton = findViewById(R.id.setupCreaterButton);
        setupAssistentButton = findViewById(R.id.setupAssistentButton);

        // Informationen aus der Scan-Activity übernehmen
        Intent intent = getIntent();
        mInfo = (MachineInfo) intent.getSerializableExtra("machineInfo");

        // Einsetzen der übergebenen Informationen in die Layout-Elemente
        machineId.setText(mInfo.getMachineId());
        manufacturer.setText(mInfo.getManufacturer());
        machineName.setText(mInfo.getMachineName());
        machineType.setText(mInfo.getMachineType());
        communicationInterace.setText(mInfo.getCommunicationInterface());
        isProductFileRequired.setText(mInfo.getIsProductionFileRequired());
        isMaterialRequired.setText(mInfo.getIsMaterialRequired());
        validFileName.setText(mInfo.getValidFileName());
        validFileExtension.setText(mInfo.getValidFileExtension());
        tcpServerAddress.setText(mInfo.getTcpServerAddress());


        // Erstellen des SetupSpinners ( DropDown-Menue fuer das Auswaehlen des Setups)
        String[] items = new String[mInfo.setupAssistentList.size() + 1];
        Log.i(TAG, mInfo.setupAssistentList.size() + "");
        items[0] = "Setup-Assistenten auswählen";
        for (int i = 0; i < mInfo.setupAssistentList.size();i++){
            items[i + 1] = mInfo.setupAssistentList.get(i).getName();
            Log.i(TAG, mInfo.setupAssistentList.get(i).getName());

        }

        // ArrayAdapter für den SetupSpinner
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items){
            @Override
            public boolean isEnabled(int position) {
                //erste Position ist nur ein Hint, deswegen return false
                if(position == 0){
                    return false;
                }else{
                    return true;
                }
            }

            // Ausgrauen für Position 0, da es sich hierbei nur um einen Hint handelt
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0 ){
                    tv.setTextColor(Color.GRAY);
                }else{
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Adapter hinzufügen und OnItemSelectedListener einstellen
        setupSpinner.setAdapter(adapter);
        setupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position != 0){
                    // entnehmen der sAID des asugewählten Setups
                    selectedSAID = mInfo.setupAssistentList.get(position -1).getsAID();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        // OnclickListener für setupCreater ( Erstellen eines Setups)
        setupCreaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToSetupCreator();
            }
        });

        //OnClickListener für SetupAssistenButton (Starten des Setups)
        setupAssistentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchToARActivity();
            }
        });
    }

    /**
     * beim Starten eines Setups in die ARActivity wechseln,
     * Maschinen-Info übergeben und die das ausgewaehlte Setup mit angeben
     */
    private void switchToARActivity() {
        Intent arActivityIntent = new Intent(this, ARActivity.class);
        arActivityIntent.putExtra("machineInfo", mInfo);
        arActivityIntent.putExtra("sAID", selectedSAID);
        startActivity(arActivityIntent);

    }
    // wechseln in die SetupCreatorActivity und uebergeben der MaschinenInformationen
    private void switchToSetupCreator() {
        Intent arActivityIntent = new Intent(this, SetupCreaterActivity.class);
        arActivityIntent.putExtra("machineInfo", mInfo);
        startActivity(arActivityIntent);

    }
}