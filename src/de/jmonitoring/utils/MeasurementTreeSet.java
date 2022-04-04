/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.utils;

import de.jmonitoring.DataHandling.Interpolators.MinMax;
import java.util.SortedSet;
import java.util.TreeSet;

import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.Unit;
import java.text.SimpleDateFormat;
import java.util.TreeMap;

/**
 * A set of measurements
 *
 * @author togro
 */
public class MeasurementTreeSet extends TreeSet<Measurement> {

    private Integer id;
    private final Unit unit;
    private Measurement measurementBeforeInterval = null;
    private Measurement measurementAfterInterval = null;
    private Double minimum = Double.MAX_VALUE;
    private Double maximum = -Double.MAX_VALUE;
    private TreeMap<Long, MinMax> minMaxMap = new TreeMap<Long, MinMax>();
    private Double peakMin = null;
    private Double peakMax = null;

    public MeasurementTreeSet() {
        this(new TreeSet<Measurement>(), new Unit());
    }

    public MeasurementTreeSet(SortedSet<Measurement> s, Unit unit) {
        super(s);
        this.unit = unit;
        this.minimum = Double.MAX_VALUE;
        this.maximum = -Double.MAX_VALUE;
    }

    public MeasurementTreeSet(SortedSet<Measurement> s, Unit unit, Double min, Double max) {
        super(s);
        this.unit = unit;
        this.minimum = min;
        this.maximum = max;
    }

    public static MeasurementTreeSet copyOf(MeasurementTreeSet original) {
        MeasurementTreeSet newTreeSet = new MeasurementTreeSet(original, original.getUnit(), original.getMinimumOfIntervals(), original.getMaximumofIntervals());
        newTreeSet.setPeakMin(original.getPeakMin());
        newTreeSet.setPeakMax(original.getPeakMax());
        newTreeSet.setIntervalMinMaxMap(original.getIntervalMinMaxMap());
        return newTreeSet;
    }

    public static MeasurementTreeSet emptyFor(int sensorID) {
        return empty(SensorInformation.getSensorProperties(sensorID).getSensorUnit());
    }

    // TODO: Check if callers can be mapped to emptyFor(sensorID)
    public static MeasurementTreeSet empty(Unit unit) {
        return new MeasurementTreeSet(new TreeSet<Measurement>(), unit);
    }

    public synchronized boolean containsValueForTime(Long time) {
        boolean b = false;
        for (Measurement m : this) {
            if (m.getTime().equals(time)) {
                return true;
            }
        }
        return b;
    }

    public synchronized Measurement getMeasurementForTime(Long time) {
        for (Measurement m : this) {
            if (m.getTime().equals(time)) {
                return m;
            }
        }
        return null;
    }

    public synchronized Double getValueForTime(Long time) {
        Double value = null;

        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );                    
        // System.out.println( "getValueForTime: value is null for time: " + simpleDateFormat.format( time )  );
        
        for (Measurement m : this) {
            // System.out.println( "" + simpleDateFormat.format( m.getTime() ) + " " + m.getValue() );
            if (m.getTime().equals(time)) {
                return m.getValue();
            }
        }
        
        /*
        if( value == null )
        {            
            System.out.println( "null!! getValueForTime: value is null for time: " + simpleDateFormat.format( time )  );
        }
        */
        return value;
    }

    public Unit getUnit() {
        return unit;
    }

    /**
     * Extrahieren aller Messwerte innerhalb des Aggregationsintervalls
     */
    public MeasurementTreeSet extractMeasurementsBetween(Measurement startM, Measurement endM) {
        boolean hasEnd = (ceiling(endM) != null);
        return new MeasurementTreeSet(subSet(startM, true, endM, hasEnd), getUnit());
    }

    public Measurement getMeasurementBeforeInterval() {
        return measurementBeforeInterval;
    }

    public void setMeasurementBeforeInterval(Measurement measurementBeforeInterval) {
        this.measurementBeforeInterval = measurementBeforeInterval;
    }

    public Measurement getMeasurementAfterInterval() {
        return measurementAfterInterval;
    }

    public void setMeasurementAfterInterval(Measurement measurementAfterInterval) {
        this.measurementAfterInterval = measurementAfterInterval;
    }

    public Double getMinimumOfIntervals() {
        if (minimum == null || minimum.equals(Double.MAX_VALUE)) {
            return null;
        }
        return minimum;
    }

    public Double getMaximumofIntervals() {
        if (maximum == null || maximum.equals(-Double.MAX_VALUE)) {
            return null;
        }
        return maximum;
    }

    public Double getPeakMin() {
        return peakMin;
    }

    public void setPeakMin(Double peakMin) {
        this.peakMin = peakMin;
    }

    public Double getPeakMax() {
        return peakMax;
    }

    public void setPeakMax(Double peakMax) {
        this.peakMax = peakMax;
    }

    public void probeValue(Double value) {
        if (value == null) {
            return;
        }

        if (value > maximum) {
            maximum = value;
        }

        if (value < minimum) {
            minimum = value;
        }
    }

    public void addIntervalMinMax(Long time, MinMax minmax) {
        minMaxMap.put(time, minmax);
        updatePeaks(minmax);
    }

    public TreeMap<Long, MinMax> getIntervalMinMaxMap() {
        return minMaxMap;
    }

    public void setIntervalMinMaxMap(TreeMap<Long, MinMax> minMaxMap) {
        this.minMaxMap = minMaxMap;
    }

    public MinMax getPeakMinMax() {
        return new MinMax(peakMin, peakMax);
    }

    private void updatePeaks(MinMax minmax) {
        if (minmax == null) {
            return;
        }

        if (peakMin == null || minmax.getMinimum() != null && minmax.getMinimum() < peakMin) {
            peakMin = minmax.getMinimum();
        }
        if (peakMax == null || minmax.getMaximum() != null && minmax.getMaximum() > peakMax) {
            peakMax = minmax.getMaximum();
        }
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
