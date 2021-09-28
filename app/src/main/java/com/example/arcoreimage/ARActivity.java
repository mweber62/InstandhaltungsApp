package com.example.arcoreimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.arcoreimage.Classes.Instruction;
import com.example.arcoreimage.Classes.MachineInfo;
import com.example.arcoreimage.Classes.MyARNode;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.ux.ArFragment;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ARActivity extends AppCompatActivity implements Scene.OnUpdateListener{

    // TAG für das Loggen
    private String TAG = "ARActivity";

    // Layout Elemente
    private Button nextStep, lastStep;
    private TextView instructionText;
    private ArSceneView arView;

    // Augmented Variablen
    private Session session;
    private boolean shouldConfiguarteSession = false;
    private String MODEl_URL = "https://github.com/mwbr62/3d-models/blob/main/arrow_rotated.glb?raw=true";

    // Setup Variablen
    private MyARNode node;
    boolean readyToPlace = true;
    private MachineInfo mInfo;
    private String currentSAID;
    private boolean firstScan = false;
    private boolean setupBeenden = false;
    private List<Instruction> instructionList;
    private int currentInstruction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        // Layout-Elemente zuweisen
        arView = findViewById(R.id.arView);
        nextStep = findViewById(R.id.nextStep);
        lastStep = findViewById(R.id.lastStep);
        instructionText = findViewById(R.id.instructionText);

        // Plane-Netz ausschalten
        arView.getPlaneRenderer().setVisible(false);

        // Informationen aus der letzten Activity entgegen nehmen
        Intent intent = getIntent();
        mInfo = (MachineInfo) intent.getSerializableExtra("machineInfo");
        currentSAID = intent.getStringExtra("sAID");



        // erstellen einer InstruktionListe und befüllen mit den richtigen Arbeitsschritten
        instructionList = new ArrayList<>();

        for(int i = 0; i < mInfo.getInstructionList().size(); i++){
            if(mInfo.getInstructionList().get(i).getsAID().equals(currentSAID)){
                instructionList.add(mInfo.getInstructionList().get(i));
            }
        }

        currentInstruction = 0;


        // OnClick-Listener für den next-Step Button (den nächsten Arbeitsschritt anzeigen
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //neues Element kann platziert werden
                readyToPlace = true;

                if(currentInstruction + 1 <= instructionList.size()){
                    currentInstruction++;
                }

                if(currentInstruction + 1 == instructionList.size()){
                    setupBeenden = true;

                }else{
                    /*
                    String currentInstructionArrowDirection = instructionList.get(currentInstruction).getArrowDirection();
                    float currentInstructionXCoordinate = instructionList.get(currentInstruction).getX();
                    float currentInstructionYCoordinate = instructionList.get(currentInstruction).getY();
                    float currentInstructionZCoordinate = instructionList.get(currentInstruction).getZ();

                     */
                    String currentInstructionMessage = instructionList.get(currentInstruction).getDescription();
                    instructionText.setText(currentInstructionMessage);
                }
                if(setupBeenden){
                    finishedSetup();
                }
            }
        });

        // LastStep OnClickListener ( den vorherigen Arbeitsschritt anzeigen)
        lastStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // neues Element kann platziert werden
                readyToPlace = true;

                // auf den vorherigen Arbeitsschritt wechseln
                currentInstruction--;
                if (currentInstruction - 1 < -1)
                    currentInstruction = 0;

                /*
                String currentInstructionArrowDirection = instructionList.get(currentInstruction).getArrowDirection();
                float currentInstructionXCoordinate = instructionList.get(currentInstruction).getX();
                float currentInstructionYCoordinate = instructionList.get(currentInstruction).getY();
                float currentInstructionZCoordinate = instructionList.get(currentInstruction).getZ();

                 */
                String currentInstructionMessage = instructionList.get(currentInstruction).getDescription();
                instructionText.setText(currentInstructionMessage);





            }
        });


        // Abfrage von den Permissions für diese Acitivty (Kamera Permission)
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ARActivity.this, "Permission need to display Camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        initSceneView();
    }

    private void initSceneView() {
        arView.getScene().addOnUpdateListener(this);
    }

    // Überprüfe die ARCore Anforderungen
    private void setupSession() {

        if(session == null){
            try {
                session = new Session(this);

            } catch (UnavailableArcoreNotInstalledException e) {
                e.printStackTrace();
            } catch (UnavailableApkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableSdkTooOldException e) {
                e.printStackTrace();
            } catch (UnavailableDeviceNotCompatibleException e) {
                e.printStackTrace();
            }
            shouldConfiguarteSession= true;
        }
        if(shouldConfiguarteSession)
        {
            configSession();
            shouldConfiguarteSession = true;

            arView.setupSession(session);
        }

        try {
            session.resume();
            arView.resume();
        }catch (CameraNotAvailableException e){
            e.printStackTrace();
            session=null;
            return;
        }
    }

    // Erstellen der Config für die Session, UpdateMode einstellen
    private void configSession() {
        Config config = new Config(session);
        if(!buildDatabase(config)){
            Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show();

        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);


    }
    // Die AugmentedImage Datenbank erstellen
    private boolean buildDatabase(Config config) {

        AugmentedImageDatabase augmentedImageDatabase;
        Bitmap bitmap = loadImage();
        if(bitmap == null)
            return false;

        augmentedImageDatabase = new AugmentedImageDatabase(session);
        augmentedImageDatabase.addImage("qrCode" , bitmap);
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    // Laden des passenden QR-Codes aus dem assets-Ordner (QR-Code zu der aktuellen Maschine)
    private Bitmap loadImage() {
        try {
            InputStream is = getAssets().open(mInfo.getMachineId() + "_450.jpeg");
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    @Override
    public void onUpdate(FrameTime frameTime) {

        Frame frame = arView.getArFrame();

        // Erfassen und abspeichern von AugmentedImage beim Tracken der Szene
        Collection<AugmentedImage> updateAugmentedImage = frame.getUpdatedTrackables(AugmentedImage.class);

        // Erstellen der Informationen für das Anzeigen des derzeitigen Arbeitsschritts
        String currentInstructionArrowDirection = instructionList.get(currentInstruction).getArrowDirection();
        String currentInstructionMessage = instructionList.get(currentInstruction).getDescription();
        float currentInstructionXCoordinate = instructionList.get(currentInstruction).getX();
        float currentInstructionYCoordinate = instructionList.get(currentInstruction).getY();
        float currentInstructionZCoordinate = instructionList.get(currentInstruction).getZ();


        // Abgleichen der erfassten AugmentedImages mit den QR-Code
        for (AugmentedImage image : updateAugmentedImage){
            if(image.getTrackingState() == TrackingState.TRACKING){
                if(image.getName().equals("qrCode")) {


                    if(readyToPlace==true){
                        if(node != null)
                            arView.getScene().removeChild(node);

                        // erstellen des AnchorNodes
                        node = new MyARNode(this, MODEl_URL);

                        // setzen der Beschreibung
                        instructionText.setText(currentInstructionMessage);

                        // sollte es sich um den ersten Scann des QR-Codes handeln
                        // Benachrichtige den Benutzer über Erolgreiches Scannen des QR-Codes
                        if(firstScan == false){
                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing!
                                }
                            });
                            alert.setMessage("QR-Code erfolgreich gescanned");
                            alert.create().show();
                            firstScan=true;
                        }

                        // Setze das 3D-Modell an den Node
                        node.setImage(image, currentInstructionArrowDirection, currentInstructionXCoordinate, currentInstructionYCoordinate, currentInstructionZCoordinate);

                        // füge den Node der Szene hinzu
                        arView.getScene().addChild(node);

                        // einfügen neuer 3D-Modelle verweigern
                        readyToPlace = false;
                    }

                }
            }
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ARActivity.this, "Permission need to display Camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(session != null){
            arView.pause();
            session.pause();
        }
    }


    /**
     * sobald der letzte Schritt erreicht wurde benachrichtige den Benutzer
     */
    public void finishedSetup(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setPositiveButton("anderes Setup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchToMachineInfoActivity();
            }
        });
        alert.setNegativeButton("Hauptmenu", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchToMainMenuActivity();
            }
        });
        alert.setMessage("Weiteres Setup starten oder zurück ins Hauptmenu?");
        alert.create().show();
    }

    /**
     * wechsel in das Hauptmenu zurück
     */
    private void switchToMainMenuActivity() {
        Intent mainMenuActivity = new Intent(this, MainActivity.class);
        startActivity(mainMenuActivity);
    }

    /**
     * wechsel in die Maschinen-Informations-Acitivty und übergebe das Maschine-Info Objekt
     */
    private void switchToMachineInfoActivity() {
        Intent machineInfoActivity = new Intent(this, MachineInfoActivity.class);
        machineInfoActivity.putExtra("machineInfo", mInfo);
        startActivity(machineInfoActivity);
    }


}