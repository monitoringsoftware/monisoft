/*
 * SensorProperties.java
 *
 * Created on 15. Juni 2007, 13:56
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.base.sensors;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.UnitCalulation.Unit;
import java.io.Serializable;
import java.util.HashMap;

/**
 * This class defines a sensor with all its parameters
 *
 * @author togro
 */
public class SensorProperties implements Cloneable, Serializable, Comparable {

    private int Sensor_id;
    private String SensorName = "";
    private String SensorDescription = "";
    private Unit SensorUnit = null;
    private double Factor = 1d;
    private boolean isManual = false;
    private boolean isCounter = false;
    private boolean isEvent = false;
    private Integer minWE = null;
    private Integer maxWE = null;
    private Integer minWT = null;
    private Integer maxWT = null;
    private Long changeTimeWE = null;
    private Long changeTimeWT = null;
    private Integer interval = null;
    private String keyName = "";
    private String virtualDefinition = "";
    private Integer buildingID = null;
    private String buildingName = null;
    private boolean isResetCounter = false;
    private boolean isUsage = false;
    private String medium = "";
    private Double constant = null;
    private HashMap<String, Object> list = new HashMap<String, Object>(36);
    // these are no longer used but must remain here for compatibility with older chartdescription where they are serialized
    private Integer maxUpdateTime = null;
    private boolean periodic = false;
    // AZ: MONISOFT-22: Zeitzonen
    private Long utcPlusX = null;
    private boolean summerTime = false;

    /**
     *
     */
    public SensorProperties() {
    }

    public SensorProperties(int id) {
        this.Sensor_id = id;
    }

    public SensorProperties(int id, String Name, String key) {
        this.Sensor_id = id;
        this.SensorName = Name;
        this.keyName = key;
    }

    private void updateList() {
        // Liste für iterierbare Rückgabe bauen
        list.put(MoniSoftConstants.SENSOR_NAME, this.SensorName);
        list.put(MoniSoftConstants.SENSOR_ID, this.Sensor_id);
        list.put(MoniSoftConstants.SENSOR_DESCRIPTION, this.SensorDescription);
        list.put(MoniSoftConstants.SENSOR_FACTOR, this.Factor);
        list.put(MoniSoftConstants.IS_EVENT, this.isEvent);
        list.put(MoniSoftConstants.SENSOR_INTERVAL, this.interval);
        list.put(MoniSoftConstants.MIN_WEEKEND, this.minWE);
        list.put(MoniSoftConstants.MAX_WEEKEND, this.maxWE);
        list.put(MoniSoftConstants.MIN_WORKDAY, this.minWT);
        list.put(MoniSoftConstants.MAX_WORKDAY, this.maxWT);
        list.put(MoniSoftConstants.MAX_WEEKEND_CHANGETIME, this.changeTimeWE);
        list.put(MoniSoftConstants.MAX_WORKDAY_CHANGETIME, this.changeTimeWT);
        list.put(MoniSoftConstants.SENSOR_KEY, this.keyName);
        list.put(MoniSoftConstants.IS_MANUAL, this.isManual);
        list.put(MoniSoftConstants.IS_COUNTER, this.isCounter);
        list.put(MoniSoftConstants.IS_RESETCOUNTER, this.isResetCounter);
        list.put(MoniSoftConstants.IS_USAGE, this.isUsage);
        list.put(MoniSoftConstants.VIRT_DEF, this.virtualDefinition);
        list.put(MoniSoftConstants.SENSOR_BUILDING_ID, this.buildingID);
        list.put(MoniSoftConstants.SENSOR_MEDIUM, this.medium);
        list.put(MoniSoftConstants.SENSOR_CONSTANT, this.constant);
        list.put(MoniSoftConstants.SENSOR_UNIT_ID, UnitInformation.getIDFromUnitName(SensorUnit.getUnit()));
            // AZ: MONISOFT-22: Zeitzonen
        long utcPlusX = 0;
        if( this.getUtcPlusX() != null )
            utcPlusX = this.getUtcPlusX();
        list.put(MoniSoftConstants.SENSOR_UTC_PLUX_X, utcPlusX);
        list.put(MoniSoftConstants.SENSOR_SUMMERTIME, this.isSummerTime());
    }

    public int getSensorID() {
        return this.Sensor_id;
    }

    public void setSensorID(int id) {
        this.Sensor_id = id;
    }

    public String getSensorName() {
        return this.SensorName;
    }

    public void setSensorName(String name) {
        this.SensorName = name;
    }

    public String getSensorDescription() {
        return this.SensorDescription;
    }

    public void setSensorDescription(String description) {
        this.SensorDescription = description;
    }

    public Unit getSensorUnit() {
        return SensorUnit;
    }

    public void setSensorUnit(Unit unit) {
        this.SensorUnit = unit;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String name) {
        this.buildingName = name;
        this.buildingID = BuildingInformation.getBuildingIDFromName(name);
    }

    public Integer getBuildingID() {
        return this.buildingID;
    }

    public void setBuildingID(Integer buildingID) {
        if (buildingID != null) {
            this.buildingID = buildingID;
            BuildingProperties bProps = BuildingInformation.getBuildingProperties(buildingID);
            buildingName = (buildingID == null || bProps == null) ? "" : bProps.getBuildingName();
        } else {
            buildingID = null;
            buildingName = "";
        }
    }

    public boolean isManual() {
        return this.isManual;
    }

    public void setManual(boolean b) {
        this.isManual = b;
    }

    public boolean isCounter() {
        return this.isCounter;
    }

    public void setCounter(boolean b) {
        this.isCounter = b;
    }

    public boolean isEvent() {
        return this.isEvent;
    }

    public void setEvent(boolean b) {
        this.isEvent = b;
    }

    public boolean isVirtual() {
        return this.virtualDefinition == null || this.virtualDefinition.isEmpty() ? false : true;
    }

    public void setVirtualDefinition(String def) {
        this.virtualDefinition = def;
    }

    public String getVirtualDefinition() {
        return this.virtualDefinition;
    }

    public double getFactor() {
        return this.Factor;
    }

    public void setFactor(double factor) {
        this.Factor = factor;
    }

    public Integer[] getWELimits() {
        Integer[] tupel = {minWE, maxWE};
        return tupel;
    }

    public void setWELimits(Integer value, int minORmax) {
        switch (minORmax) {
            case MoniSoftConstants.MINIMUM:
                this.minWE = value;
                break;
            case MoniSoftConstants.MAXIMUM:
                this.maxWE = value;
                break;
        }
    }

    public Integer[] getWTLimits() {
//        System.out.println("R "+ minWT + " " + maxWT);
        Integer[] tupel = {minWT, maxWT};
        return tupel;
    }

    public void setWTLimits(Integer value, int minORmax) {
        switch (minORmax) {
            case MoniSoftConstants.MINIMUM:
                this.minWT = value;
                break;
            case MoniSoftConstants.MAXIMUM:
                this.maxWT = value;
                break;
        }
    }

    public Long[] getMaxChangeTimes() {
        Long[] tupel = {changeTimeWT, changeTimeWE};
        return tupel;
    }

    public void setMaxChangeTimes(Long value, int weORwt) {
        switch (weORwt) {
            case MoniSoftConstants.WEEKEND:
                this.changeTimeWE = value;
                break;
            case MoniSoftConstants.WORKDAY:
                this.changeTimeWT = value;
                break;
        }
    }

    public Integer getInterval() {
        return this.interval;
    }

    public void setInterval(Integer interval) {
        if (interval == null) {
            interval = 0;
        }
        this.interval = interval;
    }

    public String getKeyName() {
        return this.keyName;
    }

    public void setKeyName(String k) {
        this.keyName = k;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public Double getConstant() {
        return constant;
    }

    public void setConstant(Double constant) {
        this.constant = constant;
    }

    /**
     * Returns an itarable list of all sensor properties
     *
     * @return
     */
    public HashMap<String, Object> getPropertyList() {
        updateList(); // before return list, make sure it is up-to-date
        return list;
    }

    public boolean isResetCounter() {
        return isResetCounter;
    }

    public void setResetCounter(boolean isResetCounter) {
        this.isResetCounter = isResetCounter;
    }

    public boolean isUsage() {
        return isUsage;
    }

    public void setUsage(boolean isUsage) {
        this.isUsage = isUsage;
    }

    @Override
    public String toString() {
        String buildingPart = "";
        if (BuildingInformation.getBuildingList().size() > 1 && MoniSoft.getInstance().getApplicationProperties().getProperty("AddBuildingName").equals("1") && !this.SensorName.equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            String building = buildingName == null ? "" : buildingName;
            buildingPart = "@" + building;
        }

        String descriptionPart = "";
        if (!this.SensorName.equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            descriptionPart = "\u2015" + this.SensorDescription;
        }

        if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseSensorIDForDisplay").equals("0")) {
            return this.SensorName + buildingPart + descriptionPart;
        } else {
            return this.keyName + buildingPart + descriptionPart;
        }
    }

    @Override
    public SensorProperties clone() {
        try {
            return (SensorProperties) super.clone();
        } catch (CloneNotSupportedException cnse) {
            Messages.showException(cnse);
            return null;
        }
    }
    
    @Override
    public int compareTo( Object o ) {
        
        if( o instanceof SensorProperties )
        {
            SensorProperties sp = (SensorProperties) o;
        
            if( sp == null || sp.getSensorName() == null )
                return 0;
            else
                return getSensorName().compareTo( sp.getSensorName() );
        }
        
        return 0;
    }

    /**
     * @return the utcPlusX
     */
    public Long getUtcPlusX() {
        return utcPlusX;
    }

    /**
     * @param utcPlusX the utcPlusX to set
     */
    public void setUtcPlusX(Long utcPlusX) {
        this.utcPlusX = utcPlusX;
    }

    /**
     * @return the summerTime
     */
    public boolean isSummerTime() {
        return summerTime;
    }

    /**
     * @param summerTime the summerTime to set
     */
    public void setSummerTime(boolean summerTime) {
        this.summerTime = summerTime;
    }
}
