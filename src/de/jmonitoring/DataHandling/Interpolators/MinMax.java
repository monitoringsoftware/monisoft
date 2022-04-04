package de.jmonitoring.DataHandling.Interpolators;

/**
 * A Object holding two values for a minumum and maximum. A new value can
 * simply<br> be added with probeValue and is automatically assigend to the
 * correspondant<br> value.
 *
 * @author togro
 */
public class MinMax {

    Double minimum = null;
    Double maximum = null;

    /**
     * Create a new Object with the given max an min values
     *
     * @param minimum
     * @param maximum
     */
    public MinMax(Double minimum, Double maximum) {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    /**
     * Create an empty Object
     */
    public MinMax() {
        this(null, null);
    }

    public Double getMinimum() {
        return minimum;
    }

    public void setMinimum(Double minimum) {
        this.minimum = minimum;
    }

    public Double getMaximum() {
        return maximum;
    }

    public void setMaximum(Double maximum) {
        this.maximum = maximum;
    }

    /**
     * Assigns the given value to the minimum and/or maximum if aplicable
     *
     * @param value
     */
    public void probeValue(Double value) {
        if (value == null) {
            return;
        }

        if (maximum == null || value > maximum) {
            maximum = value;
        }

        if (minimum == null || value < minimum) {
            minimum = value;
        }
    }
}
