package de.jmonitoring.DataHandling;

/**
 * This class represents a measurement value
 *
 * @author togro
 */
public class IntervalValue {

    private final Double value;
    private final boolean wasCancelled;

    /**
     * Create a new instance of IntervalValue with the given value and
     * cancellation state
     *
     * @param value
     * @param wasCancelled
     */
    protected IntervalValue(Double value, boolean wasCancelled) {
        super();
        this.value = value;
        this.wasCancelled = wasCancelled;
    }

    /**
     * Return "normal" interval value
     *
     * @param value The value for the interval
     * @return The value
     */
    public static IntervalValue forValue(Double value) {
        return new IntervalValue(value, false);
    }

    /**
     * Return a instance with cancelled mode on as signal
     *
     * @return The value
     */
    public static IntervalValue cancel() {
        return new IntervalValue(null, true);
    }

    /**
     * Calculate the intervla value from counter chnage parameters
     *
     * @param highValue
     * @param v1
     * @param v2
     * @param lowValue
     * @return
     */
    public static IntervalValue withCounterChangeParameters(Double highValue, Double v1, Double v2, Double lowValue) {
        return forValue((highValue - v1) + (v2 - lowValue));
    }

    /**
     * Return the interval value or
     * <code>Double.MIN_VALUE</code> if cancelled
     *
     * @return The value
     */
    public Double getValue() {
        if (isCancelled()) {
            return Double.MIN_VALUE;
        }
        return value;
    }

    /**
     * Return if this is a cancelled interval value
     *
     * @return
     */
    public boolean isCancelled() {
        return this.wasCancelled;
    }

    /**
     * Returns the given value if it is greater that the existing one
     *
     * @param maximum
     * @return The higher value
     */
    public double ifGreaterThan(double maximum) {
        return Math.max(getValue(), maximum);
    }

    /**
     * Returns the given value if it is lesser that the existing one
     *
     * @param minimum
     * @return The lower value
     */
    public double ifLowerThan(double minimum) {
        return Math.min(getValue(), minimum);
    }

    @Override
    public String toString() {
        if (isCancelled()) {
            return "CANCELLED";
        }
        return String.valueOf(getValue());
    }
}
