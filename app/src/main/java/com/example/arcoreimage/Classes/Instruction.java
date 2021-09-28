package com.example.arcoreimage.Classes;

import java.io.Serializable;

/**
 * Serializable für das Umwandeln von JSON-Format in Instruction
 */
public class Instruction implements Serializable {

    // ID in der Datenbank
    private int id;

    // Setup-Assisten-ID, ID eines Setups, einzigartig
    private String sAID;

    // Machinen-ID, einzigartig
    private String machineID;

    // Beschreibung dieses Arbeitsschritts
    private String description;

    // X, Y und Z Koordinaten für das Augmented Reality Element, hier ein Pfeil
    private float x;
    private float y;
    private float z;

    // Pfeilausrichtung
    private String arrowDirection;


    public Instruction(String sAID, String machineID, String description, String arrowDirection, float xCoordinate, float yCoordinate, float zCoordinate){
        this.sAID = sAID;
        this.machineID = machineID;
        this.description = description;
        this.arrowDirection = arrowDirection;
        this.x = xCoordinate;
        this.y = yCoordinate;
        this.z = zCoordinate;

    }

    /**
     * Getter und Setter
     */
    public String getsAID() {
        return sAID;
    }

    public void setsAID(String sAID) {
        this.sAID = sAID;
    }

    public String getMachineID() {
        return machineID;
    }

    public void setMachineID(String machineID) {
        this.machineID = machineID;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public String getArrowDirection() {
        return arrowDirection;
    }

    public void setArrowDirection(String arrowDirection) {
        this.arrowDirection = arrowDirection;
    }
}
