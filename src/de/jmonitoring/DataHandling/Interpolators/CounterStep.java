package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.utils.Measurement;

/**
 * Represents a negative step in counter values
 *
 * @author togro
 */
public class CounterStep {

    private Measurement oldMeasaurement;
    private Measurement newMeasaurement;

    /**
     * A ne setep with empty parameters
     */
    public CounterStep() {
        this(null, null);
    }

    /**
     * Create a step which is defiend by the given measurements
     *
     * @param oldMeasaurement The (higher) measurement
     * @param newMeasaurement The (lower) measuremnt
     */
    public CounterStep(Measurement oldMeasaurement, Measurement newMeasaurement) {
        this.oldMeasaurement = oldMeasaurement;
        this.newMeasaurement = newMeasaurement;
    }

    public Measurement getOldMeasurement() {
        return oldMeasaurement;
    }

    public void setOldMeasaurement(Measurement oldMeasaurement) {
        this.oldMeasaurement = oldMeasaurement;
    }

    public Measurement getNewMeasurement() {
        return newMeasaurement;
    }

    public void setNewMeasaurement(Measurement newMeasaurement) {
        this.newMeasaurement = newMeasaurement;
    }
}
