package com.example.arcoreimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arcoreimage.Classes.MachineInfo;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;

public class MachineScanningActivity extends AppCompatActivity {

    // TAG für das Loggen
    private final String TAG = "MachineScanningActivity";

    // Layout-Elemente
    private Button  testButton;
    private TextView qrTextView, machineIDLabel;

    // MQTT Variablen
    private String host;
    private String clientID = "ExampleTestClient";
    private String topic = "sendMInfo";
    private MqttAndroidClient mqttAndroidClient;
    private boolean messagePublished = false;

    // Objekt für das halten der erhaltenen Informationen ueber die Maschine
    private MachineInfo mInfo;

    // Variablen für den QR-Scanner
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_machine_scanning);

        // Zuweisen der Layout-Elemente
        testButton = findViewById(R.id.testButton);
        qrTextView = findViewById(R.id.qrTextView);
        machineIDLabel = findViewById(R.id.mIDLbael);
        surfaceView = findViewById(R.id.scanner_view);

        // MaschinenLabel ausblenden
        machineIDLabel.setVisibility(View.GONE);


        // QR-Scanner einrichten
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setRequestedPreviewSize(640, 480).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                // Ueberpruefen der Permissions fuer Kamerazugriff
                if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                try {
                    cameraSource.start(holder);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            // wenn der QR-Code gelesen wird, schreibe Inhalt in das QRTextFeld und
            // zeige das es an
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrcode = detections.getDetectedItems();
                if(qrcode.size() != 0){
                    qrTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            machineIDLabel.setVisibility(View.VISIBLE);
                            qrTextView.setText(qrcode.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });








        // generieren von einer Client-ID und angabe zu dem MQTT Host
        clientID = clientID + System.currentTimeMillis();
        host = "ws://broker.emqx.io:8083";

        // erstellen eines mqttClients
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientID);

        // beim erhalten von einer Nachricht aus dem JSON-Format ein MaschineInfo Objekt erstellen
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if(reconnect){
                    Log.i(TAG, "reconnected to Host");
                }else{
                    Log.i(TAG, "connected to Host");
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connected to Host was lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "Incoming messaged: " + new String(message.getPayload()));

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                mInfo = gson.fromJson(new String(message.getPayload()), MachineInfo.class);

                if(messagePublished == true){
                    switchToMachineInfoActivity();
                }



            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        // erstellen von MqttConnectionOptionen
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try{
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);



                    subscribeTopic();
                    Log.i(TAG, "MQTT connected sucessfully");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "MQTT connection failed");
                }
            });
        }catch(MqttException e){
            e.printStackTrace();
        }



        // OnClickButton fuer das publishen der Nachricht
        // sollte QR-Code nicht gescannt worden sein, Informiere Benutzer
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(qrTextView.getText().toString() != "")
                    publishMessage(qrTextView.getText().toString());
                    messagePublished = true;

                if(qrTextView.getText().toString().equals("Bitte scannen Sie den QR-Code"))
                    Toast.makeText(MachineScanningActivity.this, "Fehler! Bitte erneut versuchen!", Toast.LENGTH_SHORT).show();
            }
        });

    }


    // Methode zum subscriben eines Topics
    private void subscribeTopic() {
        try{
            mqttAndroidClient.subscribe("sendMInfo", 1, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });
        }catch(MqttException e){
            e.printStackTrace();
        }


    }

    // Methode zum publishen einer Nachricht
    public void publishMessage(String payload) {
        try {

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            mqttAndroidClient.publish("sendMID", message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!") ;
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!") ;
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    // wechseln in die MachineInfoActivity und uebergeben des erstellen MachinenInfo Obejkts
    private void switchToMachineInfoActivity() {

        Intent switchActivityIntent = new Intent(this, MachineInfoActivity.class);
        switchActivityIntent.putExtra("machineInfo", mInfo);
        startActivity(switchActivityIntent);

    }

}