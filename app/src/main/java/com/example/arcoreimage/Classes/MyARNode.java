package com.example.arcoreimage.Classes;

import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.concurrent.CompletableFuture;

/**
 * eigener AnchorNode für das platzieren des virtuellen Elements im Setup-Assistenten
 */
public class MyARNode extends AnchorNode {

    // AugmentedImage Variable für den QR-Code
    private AugmentedImage image;

    // ModelRenderable für das Rendern des Pfeils
    private static CompletableFuture<ModelRenderable> modelRenderableCompletableFuture;

    /**
     * vorbereiten und erstellen des 3D-Modells
     * @param context
     * @param uri , URL zum 3D-Modell ( dem Pfeil )
     */
    public MyARNode(Context context, String uri){
        if(modelRenderableCompletableFuture == null){
            modelRenderableCompletableFuture = ModelRenderable.builder()
                    .setRegistryId(uri)
                    .setSource(context, RenderableSource.builder().setSource(
                            context,
                            Uri.parse(uri),
                            RenderableSource.SourceType.GLB)
                            .setScale(0.04f)
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build())

                    .build();
        }
    }


    // Ausrichten des Pfeils, verknüpfen mit dem AnchorNode
    public void setImage(AugmentedImage image, String arrowDirection, float xCoordinate, float yCoordinate, float zCoordinate){
        this.image = image;
        if(!modelRenderableCompletableFuture.isDone()){
                CompletableFuture.allOf(modelRenderableCompletableFuture)
                        .thenAccept( (Void aVoid) ->{
                            setImage(image, arrowDirection, xCoordinate,yCoordinate, zCoordinate);


                        }).exceptionally(throwable -> {
                            return null;
                });
        }

        // AnchorNode an dem AugmentedImage setzen
        setAnchor(image.createAnchor(image.getCenterPose()));

        // Node erstellen
        Node lastNode = new Node();
        // Position des Nodes einstellen
        lastNode.setLocalPosition(new Vector3(xCoordinate, yCoordinate, zCoordinate));

        // verschiedene Rotationen des Nodes für das 3D-Modell erstellen
        // ausrichten der Pfeilspitze

        Quaternion arrowUnten= Quaternion.axisAngle(new Vector3(0,1f,0),0);
        Quaternion arrowRechts = Quaternion.axisAngle(new Vector3(0,1f,0),90f);
        Quaternion arrowOben = Quaternion.axisAngle(new Vector3(0,1f,0),180f);
        Quaternion arrowLinks = Quaternion.axisAngle(new Vector3(0,1f,0f),270f);

        Quaternion arrowVorne = Quaternion.axisAngle(new Vector3(0, 1f, 1f), 180f);
        Quaternion arrowHinten = Quaternion.axisAngle(new Vector3(0, 1f, -1f), 180f);

        switch (arrowDirection){
            case "unten": lastNode.setLocalRotation(arrowUnten);
                break;
            case "rechts": lastNode.setLocalRotation(arrowRechts);
                break;
            case "oben": lastNode.setLocalRotation(arrowOben);
                break;
            case "links": lastNode.setLocalRotation(arrowLinks);
                break;
            case "vorne": lastNode.setLocalRotation(arrowVorne);
                break;
            case "hinten": lastNode.setLocalRotation(arrowHinten);
                break;
            default:
                lastNode.setLocalRotation(arrowUnten);
                break;
        }


        // Node mit dem AnchorNode verbinden
        lastNode.setParent(this);
        // Node das 3D-Modell hinzufügen
        lastNode.setRenderable(modelRenderableCompletableFuture.getNow(null));
    }



    public AugmentedImage getImage() {
        return image;
    }


}
