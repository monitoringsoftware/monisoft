package de.jmonitoring.standardPlots.common;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DataFilter.ValueFilter;
import de.jmonitoring.References.ReferenceValue;
import java.io.Serializable;

/**
 * A class representing the common appearance of a series<p>
 *
 * The existing field names should not be edited or deleted, as this will cause
 * problems with existing {@link ChartDescriber}s
 *
 * @author togro
 */
public class SeriesLooks implements Serializable {

    private int id;  // the series id
    private String unit = ""; // the unit of the underlying sensor //NOI18N
    private String sensor = ""; // the name of the sensor //NOI18N
    private int sensorID; // the id of the sensor
    private boolean powerWanted; // a flag indicating if the sereis should be shown as consumption or power (if applicable)
    private CounterMode counterMode; 
    private double factor; // the faktor for value calculation
    private double aggregation; // the aggregation interval
    private ValueFilter valueFilter;  // a filter for filtering by values
    private ReferenceValue ref; // a reference value for calculating specific consumptions
    private Long timeReference; // a base time wich is used in conjunction with the reference value
    private String refUnit = ""; // the unit of the reference value //NOI18N
    private boolean useTimeConstraints; // flag indicating if a time (e.g. only values from 7:00 to 18:00) constraint should be used
    private boolean useWeekDayConstraints; // flag indicating if a time weekday constraint (e.g. only Mo - Fr) should be used
    private boolean useDateConstraints;// flag indicating if a date constraint should be used (e.g. a subinterval of the whole interval)
    private int startTimeConstraint; // the start time in hours if a time constraint is used (e.g. 7 if values are wanted from 7:00 to 18:00)
    private int endTimeConstraint;  // the end time in hours if a time constraint is used (e.g. 18 if values are wanted from 7:00 to 18:00)
    private byte weekDayConstraintCode;  // code defining the days to be filtered
    private long startDateConstraints = Long.MIN_VALUE; // the start value of a date constraint
    private long endDateConstraints = Long.MAX_VALUE; // the end value of a date constraint
    private String legendString = null; // the text for the legend label shown for this series    
    
    /**
     * Creates a new instance with the given id
     *
     * @param ident
     */
    public SeriesLooks(int ident) {
        id = ident;
    }

    public void setSensor(String sens) {
        sensor = sens;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensorID(int id) {
        sensorID = id;
    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSeriesID(int ident) {
        id = ident;
    }

    public int getSeriesID() {
        return id;
    }

    public void setAggregation(Double Agg) {
        this.aggregation = Agg;
    }

    public Double getAggregation() {
        return aggregation;
    }

    public void setFactor(double f) {
        factor = f;
    }

    public double getFactor() {
        return factor;
    }

    public void setPowerWanted(boolean pW) {
        powerWanted = pW;
    }

    public boolean getPowerWanted() {
        return powerWanted;
    }

    public void setUnit(String u) {
        unit = u;
    }

    public String getUnit() {
        return unit;
    }

    public ValueFilter getValueFilter() {
        return valueFilter;
    }

    public void setValueFilter(ValueFilter valueFilter) {
        this.valueFilter = valueFilter;
    }

    public ReferenceValue getReference() {
        return ref;
    }

    public void setReference(ReferenceValue r) {
        ref = r;
    }

    public Long getTimeReference() {
        return timeReference;
    }

    public void setTimeReference(long TimeReference) {
        this.timeReference = TimeReference;
    }

    public String getTimeReferenceUnit() {
        return refUnit;
    }

    public void setUseTimeConstraints(boolean b) {
        useTimeConstraints = b;
    }

    public boolean getUseTimeConstraints() {
        return useTimeConstraints;
    }

    public void setUseWeekDayConstraints(boolean b) {
        useWeekDayConstraints = b;
    }

    public boolean getUseWeekDayConstraints() {
        return useWeekDayConstraints;
    }

    public void setStartTimeConstraint(int index) {
        startTimeConstraint = index;
    }

    public int getStartTimeConstraint() {
        return startTimeConstraint;
    }

    public void setEndTimeConstraint(int index) {
        endTimeConstraint = index;
    }

    public int getEndTimeConstraint() {
        return endTimeConstraint;
    }

    public void setWeekDayConstraintCode(byte con) {
        weekDayConstraintCode = con;
    }

    public byte getWeekDayConstraintCode() {
        return weekDayConstraintCode;
    }

    public String getLegendString() {
        return legendString;
    }

    public void setLegendString(String legendString) {
        this.legendString = legendString;
    }

    public long getEndDateConstraints() {
        return endDateConstraints;
    }

    public void setEndDateConstraints(long endDateConstraints) {
        this.endDateConstraints = endDateConstraints;
    }

    public long getStartDateConstraints() {
        return startDateConstraints;
    }

    public void setStartDateConstraints(long startDateConstraints) {
        this.startDateConstraints = startDateConstraints;
    }

    public boolean isUseDateConstraints() {
        return useDateConstraints;
    }

    public void setUseDateConstraints(boolean useDateConstraints) {
        this.useDateConstraints = useDateConstraints;
    }

    /**
     * Sets the duration and the unit label of a time reference ("per Year").
     *
     * @param selection
     */
    public void setTimeReferenceFromString(String selection) {
        timeReference = null;
        refUnit = ""; //NOI18N
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("YEAR"))) {//NOI18N
            timeReference = 31536000000L;
            refUnit = "a"; //NOI18N
        }
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUARTER"))) {//NOI18N
            timeReference = 7884000000L;
            refUnit = "Q"; //NOI18N
        }
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MONTH"))) {//NOI18N
            timeReference = 2592000000L;
            refUnit = "M"; //NOI18N
        }
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("WEEK"))) {//NOI18N
            timeReference = 604800000L;
            refUnit = "w"; //NOI18N
        }
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DAY"))) {//NOI18N
            timeReference = 86400000L;
            refUnit = "d"; //NOI18N
        }
        if (selection.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HOUR"))) {//NOI18N
            timeReference = 3600000L;
            refUnit = "h"; //NOI18N
        }
    }

    /**
     * Returns the text belonging to the duration value of the
     * <code>timeReference</code>
     *
     * @return
     */
    public String getTimeReferenceString() {
        String name = null;
        if (timeReference != null) {
            if (timeReference == 3600000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HOUR");//NOI18N
            }
            if (timeReference == 86400000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DAY");//NOI18N
            }
            if (timeReference == 604800000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("WEEK");//NOI18N
            }
            if (timeReference == 2592000000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MONTH");//NOI18N
            }
            if (timeReference == 7884000000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUARTER");//NOI18N
            }
            if (timeReference == 31536000000L) {
                name = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("YEAR");//NOI18N
            }
        }
        return name;
    }

    /**
     * @return the counterMode
     */
    public CounterMode getCounterMode() {
        return counterMode;
    }

    /**
     * @param counterMode the counterMode to set
     */
    public void setCounterMode(CounterMode counterMode) {
        this.counterMode = counterMode;
    }
}
