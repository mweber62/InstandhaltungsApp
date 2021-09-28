package com.example.arcoreimage.Classes;

import java.io.Serializable;

public class SetupAssistent implements Serializable {

    // ID in der Datenbank
    private int _id;

    // Name des Setups
    private String name;

    // einzigartige Setup-ID ( Setup-Assistent-ID)
    private String sAID;

    // einzigartige Maschinen-ID
    private String machineID;


    public SetupAssistent( String name, String sAID, String machineID){
        this.sAID = sAID;
        this.machineID = machineID;
        this.name = name;


    }

    /**
     * GETTER und SETTER
     */
    public int get_id() {
        return _id;
    }

    public void set_id(int id) {
        this._id = id;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}