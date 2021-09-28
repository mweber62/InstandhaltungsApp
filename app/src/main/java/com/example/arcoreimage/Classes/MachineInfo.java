package com.example.arcoreimage.Classes;

import com.example.arcoreimage.ARActivity;

import java.io.Serializable;
import java.util.List;

/**
 * Serializable für das Umwandeln von JSON-Format in MachineInfo.
 * Enthält jegliche Information zu einer Maschine
 */
public class MachineInfo implements Serializable {

    // ID in der Datenbank
    public int _id;

    // einzigartige Maschinen-ID
    public String MachineId;

    // Maschinen-Informationen
    public String Manufacturer;
    public String MachineName;
    public String MachineType;
    public String CommunicationInterface;
    public String IsProductionFileRequired;
    public String IsMaterialRequired;
    public String ValidFileName;
    public String ValidFileExtension;
    public String TcpServerAddress;

    // Liste alle Arbeitsschritte für diese Maschine
    public List<Instruction> instructionList;

    // Liste aller Setups für diese Maschine
    public List<SetupAssistent> setupAssistentList;


    /**
     * GETTER und SETTER
     */
    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getMachineId() {
        return MachineId;
    }

    public void setMachineId(String machineId) {
        MachineId = machineId;
    }

    public String getManufacturer() {
        return Manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        Manufacturer = manufacturer;
    }

    public String getMachineName() {
        return MachineName;
    }

    public void setMachineName(String machineName) {
        MachineName = machineName;
    }

    public String getMachineType() {
        return MachineType;
    }

    public void setMachineType(String machineType) {
        MachineType = machineType;
    }

    public String getCommunicationInterface() {
        return CommunicationInterface;
    }

    public void setCommunicationInterface(String communicationInterface) {
        CommunicationInterface = communicationInterface;
    }

    public String getIsProductionFileRequired() {
        return IsProductionFileRequired;
    }

    public void setIsProductionFileRequired(String isProductionFileRequired) {
        IsProductionFileRequired = isProductionFileRequired;
    }

    public String getIsMaterialRequired() {
        return IsMaterialRequired;
    }

    public void setIsMaterialRequired(String isMaterialRequired) {
        IsMaterialRequired = isMaterialRequired;
    }

    public String getValidFileName() {
        return ValidFileName;
    }

    public void setValidFileName(String validFileName) {
        ValidFileName = validFileName;
    }

    public String getValidFileExtension() {
        return ValidFileExtension;
    }

    public void setValidFileExtension(String validFileExtension) {
        ValidFileExtension = validFileExtension;
    }

    public String getTcpServerAddress() {
        return TcpServerAddress;
    }

    public void setTcpServerAddress(String tcpServerAdress) {
        TcpServerAddress = tcpServerAdress;
    }

    public List<Instruction> getInstructionList() {
        return instructionList;
    }

    public void setInstructionList(List<Instruction> instructionList) {
        this.instructionList = instructionList;
    }

    public List<SetupAssistent> getSetupAssistentList() {
        return setupAssistentList;
    }

    public void setSetupAssistentList(List<SetupAssistent> setupAssistentList) {
        this.setupAssistentList = setupAssistentList;
    }
}
