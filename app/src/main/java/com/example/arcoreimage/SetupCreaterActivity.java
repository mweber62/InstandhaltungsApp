package com.example.arcoreimage;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.arcoreimage.Classes.Instruction;
import com.example.arcoreimage.Classes.MachineInfo;
import com.example.arcoreimage.Classes.MyARNode;
import com.example.arcoreimage.Classes.SetupAssistent;
import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.microedition.khronos.opengles.GL10;

public class SetupCreaterActivity extends AppCompatActivity implements Scene.OnUpdateListener {

    // TAG für das Loggen
    public String TAG = "SetupCreaterAcitvity";

    // AR Variablen
    private ArSceneView arView;
    private ArFragment arFragment;
    private Session session;

    // Layout Elements
    private LinearLayout seekLayout;
    private SeekBar seekX, seekY, seekZ;
    private EditText description, setupNameTextField;
    private Button confirmSetupStep, finishSetup;
    private Spinner arrowDirectionSpinner;
    private TextView scanQR;







    // Var for AR Place virtuel Element and create new Instruction
    private Node node;
    //private MyARNode myNode;
    private Anchor mainAnchor = null;
    private String MODEl_URL = "https://github.com/mwbr62/3d-models/blob/main/arrow_rotated.glb?raw=true";
    private boolean shouldConfiguarteSession;
    private boolean firstScan;
    private float xCoord, yCoord, zCoord;
    private float xCoordHit, yCoordHit, zCoordHit;
    private float mainNodeX, mainNodeY, mainNodeZ;
    private List<Instruction> instructionList;
    private String sAID;
    private String maschineID;
    private String arrowDirection;
    private String setupName;

    // Var for MQTT
    private String host;
    private String clientID = "ExampleTestClient";
    private String topic = "sendNewSetup";
    private MqttAndroidClient mqttAndroidClient;
    private MachineInfo mInfo;


    // TODO: AugmentedImage Coord - Node Coord für entgültige Coord??
    // TODO: Speichern der Infos in die DB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_creater);

        // Informationen von der letzten Activity uebernehmen
        Intent intent = getIntent();
        mInfo = (MachineInfo) intent.getSerializableExtra("machineInfo");

        // link var to LayoutElements
        arFragment = (ArFragment)getSupportFragmentManager()
                .findFragmentById(R.id.sceneform_fragment);
        seekX = (SeekBar) findViewById(R.id.seekBarX);
        seekY = (SeekBar)findViewById(R.id.seekBarY);
        seekZ = (SeekBar)findViewById(R.id.seekBarZ);
        seekLayout = findViewById(R.id.seekLayout);
        description = findViewById(R.id.descriptionSetupStep);
        confirmSetupStep = findViewById(R.id.confirmSetupStep);
        arrowDirectionSpinner = findViewById(R.id.arrowDirectionSpinner);
        finishSetup = findViewById(R.id.finishSetup);
        setupNameTextField = findViewById(R.id.setupName);
        scanQR = findViewById(R.id.instructionText2);

        // erstellen einer Liste für die Arbeitsschritte
        instructionList = new ArrayList<Instruction>();

        // standard Pfeilausrichtung initalisieren
        arrowDirection = "vorne";

        // Maschinen-ID Variable aus dem MachineInfo Objekt erstellen
        maschineID = mInfo.MachineId;

        // noch nicht gescannt
        firstScan = false;

        // OnClickListener für das bestaetigen eines Arbeitsschritts
        confirmSetupStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                //calculate the coordinates (augmentedImage.Coord - node.Coord)
                /*float xCoord =  node.getWorldPosition().x - mainNodeX ;
                float yCoord =  node.getWorldPosition().y - mainNodeY;
                float zCoord =  node.getWorldPosition().z - mainNodeZ;

                 */
                /*
                // berechnen der Koordinaten
                // Koordinate des HitTest - Koordinate des QR-Codes
                float lastXCoord =  xCoord - mainNodeX;
                float lastYCoord =  yCoord - mainNodeY;
                float lastZCoord =  zCoord - mainNodeZ;

                 */

                float lastXCoord =  node.getWorldPosition().x - mainNodeX;
                float lastYCoord =  node.getWorldPosition().y - mainNodeY;
                float lastZCoord =  node.getWorldPosition().z - mainNodeZ;






                // create new Instruction with all the new Informations, add into the instructionList, remove virtual element and reset descrption box
                Instruction i = new Instruction(sAID, maschineID, description.getText().toString(), arrowDirection, lastXCoord, lastYCoord, lastZCoord);
                instructionList.add(i);
                arFragment.getArSceneView().getScene().removeChild(node);

                // zuruecksetzen der Informationen und Progressbars
                node = null;
                //myNode = null;
                seekX.setProgress(50);
                seekY.setProgress(50);
                seekZ.setProgress(50);
                description.setText("");




            }
        });

        // erstellen des Spinner-Inhaltes (DropDown-Menue Inhalts)
        String[] items = new String[6];
        items[0] = "vorne";
        items[1] = "unten";
        items[2] = "rechts";
        items[3] = "oben";
        items[4] = "links";
        items[5] = "hinten";


        //SpinnerAdapter erstellen
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items){
            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                tv.setTextColor(Color.BLACK);

                return view;
            }
        };

        arrowDirectionSpinner.setAdapter(adapter);

        arrowDirectionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            //        items[0] = "vorne";
            //        items[1] = "unten";
            //        items[2] = "rechts";
            //        items[3] = "oben";
            //        items[4] = "links";
            //        items[5] = "hinten";

            /**
             * beim auswaehlen einer Pfeilausrichtung muss der Pfeil in die jweilige Richtung rotiert werden
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(node != null) {
                    rotateNode(node, items[position]);
                    arrowDirection = items[position];
                    /*if(myNode!=null){
                        arFragment.getArSceneView().getScene().removeChild(myNode);
                        myNode = new MyARNode(SetupCreaterActivity.this, MODEl_URL);
                        myNode.setImage(imageAnchor, arrowDirection, xCoord, yCoord, zCoord);
                        arFragment.getArSceneView().getScene().addChild(myNode);
                    }

                     */

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        // Angaben fuer den MQTTHost und generieren einer ClientID
        clientID = clientID + System.currentTimeMillis();
        host = "ws://broker.emqx.io:8083";

        // Erstellen eines Clients
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientID);
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
            // wenn eine Nachricht zum Thema "sendSAID" ankommt, wird die
            // Nachricht in eine Zahl umgewandelt und um 5 erhoeht. Dadurch wird eine
            // neue einzigartige sAID generiert
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.i(TAG, "Incoming messaged: " + new String(message.getPayload()));
                if(topic.equals("sendSAID")){
                    int number = Integer.parseInt(message.toString());
                    number = number +5;
                    sAID = "" + number;
                    Log.i(TAG, "SAID: "  + sAID);
                }


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        //MQTTConnectionOptions erstellen und bei Success das Topic subscriben
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



                    subscribeTopic("sendSAID");
                    publishMessage("giveSAID", "askForSAID");
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

        // ProgressBars und alle anderen Layouts ausblenden, bis HitTest ausgeführt wurde und somit ein
        // 3D-Modell platziert wurde
        seekLayout.setVisibility(View.GONE);
        seekX.setMax(100);
        seekY.setMax(100);
        seekZ.setMax(100);
        // Progress der Progressbars auf 50 setzen damit verschieben in + und - Bereich moeglich ist
        seekX.setProgress(50);
        seekY.setProgress(50);
        seekZ.setProgress(50);








        // erstellen und rendern des 3D-Modells (Pfeils)
        CompletableFuture<ModelRenderable> modelRenderableCompletableFuture = ModelRenderable.builder()
                .setRegistryId(MODEl_URL)
                .setSource(this, RenderableSource.builder().setSource(
                        this,
                        Uri.parse(MODEl_URL),
                        RenderableSource.SourceType.GLB)
                        .setScale(0.04f)
                        .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                        .build())

                .build();



        // OnSeekBarChangeListener für XBar
        seekX.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            // Wenn der Progress sich aendert dann berechne die XKoordinate des Pfeils neu,
            // entferne den alten Pfeil und setze einen neuen Pfeil mit der neuen XPosition
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alteredXValue;

                //berechnen der neuen XKoordinate
                int value = 2 * (progress - 50);
                alteredXValue = xCoordHit + (value * 0.01f);

                // erstellen des neuen Pfeils
                if(node != null){
                    arFragment.getArSceneView().getScene().removeChild(node);
                    node = new Node();
                    node.setLocalPosition(new Vector3(alteredXValue, yCoord,zCoord));
                    node.setParent(arFragment.getArSceneView().getScene());

                    node.setRenderable(modelRenderableCompletableFuture.getNow(null));
                    rotateNode(node, arrowDirection);
                    arFragment.getArSceneView().getScene().addChild(node);
                }

                /*if(myNode!=null){
                    arFragment.getArSceneView().getScene().removeChild(myNode);
                    myNode = new MyARNode(SetupCreaterActivity.this, MODEl_URL);
                    myNode.setImage(imageAnchor, arrowDirection, alteredXValue, yCoord, zCoord);
                    arFragment.getArSceneView().getScene().addChild(myNode);
                }*/

                // neue XKoordinate uebernehmen
                xCoord = alteredXValue;
                //description.setText("X: " + (xCoord - mainNodeX) + ", Y: " + (yCoord-mainNodeY) + ", Z: " + (zCoord-mainNodeZ));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // gleiche Vorgehensweise wie bei SeekX
        seekY.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alteredYValue;
                int value = 2 * (progress - 50);

                alteredYValue = yCoordHit + (value * 0.01f);

                if(node !=null){
                    arFragment.getArSceneView().getScene().removeChild(node);
                    //Toast.makeText(SetupCreaterActivity.this, "Size of Scene" + arFragment.getArSceneView().getScene().getChildren().size(),Toast.LENGTH_SHORT).show();


                    node = new Node();
                    node.setLocalPosition(new Vector3(xCoord, alteredYValue,zCoord));

                    node.setParent(arFragment.getArSceneView().getScene());
                    node.setRenderable(modelRenderableCompletableFuture.getNow(null));
                    rotateNode(node, arrowDirection);
                    arFragment.getArSceneView().getScene().addChild(node);
                }
                //description.setText("X: " + xCoord + ", Y: " + alteredYValue + ", Z: " + zCoord);
                /*
                if(myNode!=null){
                    arFragment.getArSceneView().getScene().removeChild(myNode);
                    myNode = new MyARNode(SetupCreaterActivity.this, MODEl_URL);
                    myNode.setImage(imageAnchor, arrowDirection, xCoord, alteredYValue, zCoord);
                    arFragment.getArSceneView().getScene().addChild(myNode);
                }

                 */



                yCoord = alteredYValue;


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // gleiche vorgehensweise wie bei SeekX und SeekY
        seekZ.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float alteredZValue;
                int value = 2 * (progress - 50);

                alteredZValue = zCoordHit + (value * 0.01f);


                if(node != null){

                    arFragment.getArSceneView().getScene().removeChild(node);
                    node = new Node();
                    node.setLocalPosition(new Vector3(xCoord, yCoord,alteredZValue));
                    node.setParent(arFragment.getArSceneView().getScene());
                    node.setRenderable(modelRenderableCompletableFuture.getNow(null));
                    rotateNode(node, arrowDirection);
                    arFragment.getArSceneView().getScene().addChild(node);


                }
                //description.setText("X: " + xCoord + ", Y: " + yCoord + ", Z: " + alteredZValue);

               /* if(myNode!=null){
                    arFragment.getArSceneView().getScene().removeChild(myNode);
                    myNode = new MyARNode(SetupCreaterActivity.this, MODEl_URL);
                    myNode.setImage(imageAnchor, arrowDirection, xCoord, yCoord, alteredZValue);
                    arFragment.getArSceneView().getScene().addChild(myNode);
                }

                */

                zCoord = alteredZValue;




            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // OnClickListener fuer das Abschließen des Setups
        finishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Der SetupName muss vorhanden sein
                if(setupNameTextField.getText().length()>0){

                    // erstellen der letzten Instruktion "fertig"
                    Instruction instruction = new Instruction(sAID, maschineID, "fertig", "vorne", 0,0,0);
                    instructionList.add(instruction);

                    // GSON Builder um die Informationen in das JSON Format umzuwandeln
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();

                    // erstellen des neuen Setups, umwandeln in das JSON-Format und publishen ueber das topic "sendNewSetup"
                    SetupAssistent sA = new SetupAssistent(setupNameTextField.getText().toString(), sAID, maschineID );
                    String setups = gson.toJson(sA);
                    publishMessage(setups, "sendNewSetup");


                    // erstellen der Liste von Arbeitsschritten, umwandeln in JSON-Format und publishen ueber das Thema
                    // "sendNewInstructionList"
                    String instructions = " {\"instructionList\": " +  gson.toJson(instructionList) + "}";
                    publishMessage(instructions, "sendNewInstructionList");

                    //Lokales hinzufügen des neues Setups und der neuen InstruktionsListe in das MachineInfo-Objekt
                    mInfo.setupAssistentList.add(sA);
                    for(int i = 0; i < instructionList.size(); i++){
                        mInfo.instructionList.add(instructionList.get(i));
                    }



                    // mit abschließen des Setups, wechseln in die MachineInfo-Activity
                    switchToMachineInfoActivity();
                }else{
                    Toast.makeText(SetupCreaterActivity.this, "SetupName angeben!", Toast.LENGTH_LONG).show();
                }

            }
        });

        // einstellen des HitTest zum platzieren eines Pfeils
        arFragment.setOnTapArPlaneListener(new BaseArFragment.OnTapArPlaneListener() {
            @Override
            public void onTapPlane(HitResult hitResult, Plane plane, MotionEvent motionEvent) {



                if(mainAnchor != null) {
                    // erstellen eines Anchors und hinzufügen der HitNode
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode hitNode = new AnchorNode(anchor);

                    // abspeichern der X,Y und Z Koordinaten des HitTest
                    xCoordHit = hitNode.getWorldPosition().x;
                    yCoordHit = hitNode.getWorldPosition().y;
                    zCoordHit = hitNode.getWorldPosition().z;
                    xCoord = xCoordHit;
                    yCoord = yCoordHit;
                    zCoord = zCoordHit;


                    // neuen Node erstellen an dieser Position, 3D-Modell hinzufügen, rotieren und der Szene hinzufügen
                    node = new Node();
                    node.setLocalPosition(new Vector3(xCoord, yCoord, zCoord));
                    node.setRenderable(modelRenderableCompletableFuture.getNow(null));
                    rotateNode(node, "vorne");
                    arFragment.getArSceneView().getScene().addChild(node);
                    /*
                    myNode = new MyARNode(SetupCreaterActivity.this, MODEl_URL);
                    myNode.setImage(imageAnchor, arrowDirection, xCoord, yCoord, zCoord);
                    arFragment.getArSceneView().getScene().addChild(myNode);

                     */

                    // Layout für Progressbar, Beschreibung und Pfeilausrichtung einblenden
                    seekLayout.setVisibility(View.VISIBLE);

                }else{
                    Toast.makeText(SetupCreaterActivity.this, "Sie müssen den QR-Code erst abscannen!", Toast.LENGTH_LONG).show();
                }
            }
        });
        arView = arFragment.getArSceneView();

        // Abfragen der Permissions für die Kamera
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        setupSession();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(SetupCreaterActivity.this, "Permission need to display Camera", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        initSceneView();
    }
    // OnUpdateListener hinzufügen
    private void initSceneView() {
        arView.getScene().addOnUpdateListener(this);

    }
    // Session einrichten
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
    // Session konfigurieren, UpdateModus einstellen
    private void configSession() {
        Config config = new Config(session);
        if(!buildDatabase(config)){
            Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show();

        }
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
    }
    // AugmentedImage Datenbank erstellen
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
    // passenden QR-Code aus dem assest-Ordner laden (QR-Code der Maschine)
    private Bitmap loadImage() {
        try {
            InputStream is = getAssets().open("4785_450.jpeg");
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public void onUpdate(FrameTime frameTime) {

        Frame frame = arView.getArFrame();

        // AugmentedImages die getrackt wurden erfassen und abspeichern
        Collection<AugmentedImage> updateAugmentedImage = frame.getUpdatedTrackables(AugmentedImage.class);

        // AugmentedImages mit den QR-Codes abgleichen
        for (AugmentedImage image : updateAugmentedImage){
            if(image.getTrackingState() == TrackingState.TRACKING){
                if(image.getName().equals("qrCode")) {

                    // Beim Scannen des QR-Codes den Benutzer darueber benachrichtigen
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
                        scanQR.setVisibility(View.GONE);

                        // erster Scan abgeschlossen
                        firstScan=true;

                        // Anchor an dieser Position des AugmentedImage erstellen
                        mainAnchor = image.createAnchor(image.getCenterPose());
                        AnchorNode mainNode = new AnchorNode(mainAnchor);

                        // Koordinaten dieser Position abspeichern
                        mainNodeX = mainNode.getWorldPosition().x;
                        mainNodeY = mainNode.getWorldPosition().y;
                        mainNodeZ = mainNode.getWorldPosition().z;



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
                        Toast.makeText(SetupCreaterActivity.this, "Permission need to display Camera", Toast.LENGTH_SHORT).show();
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

    private void rotateNode(Node node, String arrowDirection){
        Quaternion arrowUnten= Quaternion.axisAngle(new Vector3(0,1f,0),0);
        Quaternion arrowRechts = Quaternion.axisAngle(new Vector3(0,1f,0),90f);
        Quaternion arrowOben = Quaternion.axisAngle(new Vector3(0,1f,0),180f);
        Quaternion arrowLinks = Quaternion.axisAngle(new Vector3(0,1f,0f),270f);

        Quaternion arrowVorne = Quaternion.axisAngle(new Vector3(0, 1f, 1f), 180f);
        Quaternion arrowHinten = Quaternion.axisAngle(new Vector3(0, 1f, -1f), 180f);

        switch (arrowDirection){
            case "unten": node.setLocalRotation(arrowHinten);
                break;
            case "rechts": node.setLocalRotation(arrowRechts);
                break;
            case "oben": node.setLocalRotation(arrowVorne);
                break;
            case "links": node.setLocalRotation(arrowLinks);
                break;
            case "vorne": node.setLocalRotation(arrowUnten);
                break;
            case "hinten": node.setLocalRotation(arrowOben);
                break;
            default:
                node.setLocalRotation(arrowUnten);
                break;
        }
    }

    private void subscribeTopic(String topic) {
        try{
            mqttAndroidClient.subscribe(topic, 1, null, new IMqttActionListener() {
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

    public void publishMessage(String payload,String topic) {
        try {

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            mqttAndroidClient.publish(topic, message,null, new IMqttActionListener() {
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

    private void switchToMachineInfoActivity() {

        Intent switchActivityIntent = new Intent(this, MachineInfoActivity.class);
        switchActivityIntent.putExtra("machineInfo", mInfo);
        startActivity(switchActivityIntent);

    }

}