package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.MinMax;
import java.util.List;

/**
 * A IntervalMeasurement takes a (calculated) value for a interval as well
 * as<br> it's maximum and minumum values in that interval.<p>
 *
 * @author togro
 */
public class IntervalMeasurement {

    private Double min; // kleinster Wert im Intervall
    private Double max;// größter Wert im Intervall
    private Double value;// Wert des Intervalls (Mittel, Summe, etc)
    private Long missing; // nicht abgedeckte Zeit in millisekunden
    private Long time;
    private MinMax minmax;
    private final List<String> remarks;

    /**
     * Crate a new measurement with the given parameters
     *
     * @param value maesurement value in the interval
     * @param min lowest value in the interval
     * @param max highest value in the interval
     * @param miss duration in milliseconds which is not covered by valid
     * measurements
     * @param time timestamp (begin) of the interval
     * @param remarks List of remarks that were genaraterd during calculation of
     * the interval value
     */
    public IntervalMeasurement(Double value, MinMax minmax, Long miss, Long time, List<String> remarks) {
        this.min = minmax.getMinimum();
        this.max = minmax.getMaximum();
        this.value = value;
        this.missing = miss;
        this.time = time;
        this.minmax = minmax;
        this.remarks = remarks;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getMissing() {
        return missing;
    }

    public void setMissing(Long missing) {
        this.missing = missing;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public List<String> getRemarks() {
        return remarks;
    }

    public MinMax getMinmax() {
        return minmax;
    }

    public void setMinmax(MinMax minmax) {
        this.minmax = minmax;
    }
}
