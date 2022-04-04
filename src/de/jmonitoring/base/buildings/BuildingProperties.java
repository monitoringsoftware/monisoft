/*
 * SensorProperties.java
 *
 * Created on 15. Juni 2007, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.base.buildings;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author togro
 */
public class BuildingProperties implements Cloneable, Serializable {

    private int building_id;
    private Integer plz;
    private Long phone;
    private String buildingName = "";
    private String contact = "";
    private String city = "";
    private String networking = "";
    private String street = "";
    private String buildingDescription = "";
    private Integer ObjectID = null;
    private  HashMap<String,Object> list = new HashMap<String,Object>();
//    private ArrayList <Integer> sensorCollecions = new ArrayList<Integer>(16);

    /**
     * Konstruktor:
     * Erzeugt ein Buildingset ohne Parameter
     */
    public BuildingProperties() {
    }

    public Integer getObjectID() {
        return ObjectID;
    }

    public void setObjectID(Integer ObjectID) {
        this.ObjectID = ObjectID;
    }

    private void updateList() {
        // Liste für iterierbare Rückgabe bauen
        list.put(MoniSoftConstants.BUILDING_NAME, this.buildingName);
        list.put(MoniSoftConstants.BUILDING_ID, this.building_id);
        list.put(MoniSoftConstants.BUILDING_CITY, this.city);
        list.put(MoniSoftConstants.BUILDING_CONTACT, this.contact);
        list.put(MoniSoftConstants.BUILDING_STREET, this.street);
        list.put(MoniSoftConstants.BUILDING_PLZ, this.plz);
        list.put(MoniSoftConstants.BUILDING_NETWORKING, this.networking);
        list.put(MoniSoftConstants.BUILDING_PHONE, this.phone);
        list.put(MoniSoftConstants.BUILDING_DESCRIPTION, this.buildingDescription);
//        list.put(MoniSoftConstants.BUILDING_COLLECTIONIDS, this.sensorCollecions);
    }


    public int getBuildingID() {
        return this.building_id;
    }

    public void setBuildingID(int id) {
        this.building_id = id;
    }

    public String getBuildingDescription() {
        return buildingDescription;
    }

    public void setBuildingDescription(String buildingDescription) {
        this.buildingDescription = buildingDescription;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getNetworking() {
        return networking;
    }

    public void setNetworking(String networking) {
        this.networking = networking;
    }

    public Long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public Integer getPlz() {
        return plz;
    }

    public void setPlz(Integer plz) {
        this.plz = plz;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuildingName() {
        return this.buildingName;
    }

    public void setBuildingName(String name) {
        this.buildingName = name;
    }
    
    /**
     * Returns all properties of the building in a HashMap
     * @return 
     */
    public HashMap<String,Object> getPropertyList() {
        updateList(); // Sicherstellen, dass die Liste aktuell ist
        return list;
    }

    /**
     * Returns the IDs of all SensorCollections which are associated with this building
     * @return 
     */
//    public ArrayList<Integer> getSensorCollecions() {
//        return sensorCollecions;
//    }
//
//    // sets the list of IDs of the SensorCollections which are associated with this building
//    public void setSensorCollecions(ArrayList<Integer> sensorCollecions) {
//        this.sensorCollecions = sensorCollecions;
//    }

    /**
     * Returns the buildings name
     * @return 
     */
    @Override
    public String toString() {
        return buildingName;
    }

    @Override
    public BuildingProperties clone() {
        try {
            return (BuildingProperties) super.clone();
        } catch (CloneNotSupportedException cnse) {
            Messages.showException(cnse);
            return null;
        }
    }
}
