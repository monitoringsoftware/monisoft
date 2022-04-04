package de.jmonitoring.DataHandling.CounterChange;

import java.util.Date;

/**
 * This class represents a counter change with al its necessary parameters
 *
 * @author togro
 */
public class CounterChange {

    private Date time;
    private Double oldValue;
    private Double newValue;
    private Double factor;
    private Integer sensorID;

    /**
     * Create a empty change
     */
    public CounterChange() {
    }

    /**
     * Create a chnage with the given parameters
     *
     * @param time The time of the change
     * @param oldValue The last value of the old meter
     * @param newValue The first value of the new meter
     * @param factor The (new) factor
     * @param sensorID The sensor
     */
    public CounterChange(Date time, Double oldValue, Double newValue, Double factor, Integer sensorID) {
        this.time = time;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.factor = factor;
        this.sensorID = sensorID;
    }

    public Double getFactor() {
        return factor;
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }

    public Double getNewValue() {
        return newValue;
    }

    public void setNewValue(Double newValue) {
        this.newValue = newValue;
    }

    public Double getOldValue() {
        return oldValue;
    }

    public void setOldValue(Double oldValue) {
        this.oldValue = oldValue;
    }

    public Integer getSensorID() {
        return sensorID;
    }

    public void setSensorID(Integer sensorID) {
        this.sensorID = sensorID;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * Test if this counter change has valid parameters
     *
     * @return
     */
    public boolean isValidChange() {
        if (oldValue != null && newValue != null && factor != null && sensorID != null && time != null) {
            return true;
        }
        return false;
    }
}
