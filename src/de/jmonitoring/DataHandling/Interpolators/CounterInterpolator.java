package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.DataHandling.CounterChange.CounterChange;
import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.IntervalMeasurement;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import org.jfree.data.time.TimePeriod;

/**
 * This interpolator is responsible for calculating interval values for counters
 *
 * @author togro
 */
public class CounterInterpolator implements InterpolatorInterface {

    private TimePeriod interval;
    private MeasurementTreeSet intervalMeasurements;
    private CounterMode counterMode;
    private ArrayList<CounterChange> changes;
    private ArrayList<String> remarks = new ArrayList<String>();
    private ch.qos.logback.classic.Logger logger = null;
    private Long edgeTolerance;
    private Long durationOfValidity;
    private boolean intervalWasEmpty;
    private ArrayList<CounterStep> steps;

    /**
     * This implementation of the {@link InterpolatorInterface} is used to
     * calculated intervals by calculation differences of counter values.<p> It
     * is suitable for sensors counters and will also be used by usage sensors
     * where a virtual counter value is calculated by {@link Interpolator}
     *
     * @param counterChanges A list of counter changes in the complete date
     * interval
     * @param interval The interpolation interval
     * @param intervalMeasurements The measurements in the interval
     * @param mode The {@link CounterMode}
     * @param edgeTolerance The tolerance used at the interval borders
     * @param valid The duration of validity
     * @param steps A list of negative counter steps
     */
    public CounterInterpolator(ArrayList<CounterChange> counterChanges, TimePeriod interval, MeasurementTreeSet intervalMeasurements, CounterMode mode, Long edgeTolerance, Long valid, ArrayList<CounterStep> steps) {
        super();
        this.changes = counterChanges;
        this.steps = steps;
        this.interval = interval;
        this.intervalMeasurements = MeasurementTreeSet.copyOf(intervalMeasurements);
        this.intervalMeasurements.setMeasurementBeforeInterval(intervalMeasurements.getMeasurementBeforeInterval());
        this.intervalMeasurements.setMeasurementAfterInterval(intervalMeasurements.getMeasurementAfterInterval());
        this.counterMode = mode;
        this.edgeTolerance = edgeTolerance;
        this.durationOfValidity = valid;
        try {
            this.logger = MoniSoft.getInstance().getLogger();
        } catch (NullPointerException e) {
            // nothin to do
        }
    }

    /**
     * Start calculation of the interval
     *
     * @return A {@link IntervalMeasurement} holding the result * * * * *      * or <code>null</code> if no result could be calculated
     */
    @Override
    public IntervalMeasurement calculateInterval() {
        logger.trace("*** New interval " + interval.getStart() + " " + interval.getEnd());
        if (isEmptyInterval()) {
            logger.trace("Interval is empty and no value before it");
            return null;
        }
        long endTime = interval.getEnd().getTime() +1; // TODO consider +1 ?
        long startTime = interval.getStart().getTime();

        if (intervalMeasurements.isEmpty()) {
            logger.trace("Interval is empty but threre is a value before it");
            intervalWasEmpty = true;
        } else {
            intervalWasEmpty = false;
        }

        // try to interpolate values at the interval borders
        Measurement tmpStartMeasurement = calcStartBorderMeasurement(startTime);
        if (tmpStartMeasurement != null) {
            intervalMeasurements.add(tmpStartMeasurement);
            logger.trace("Calculated a value for the interval start: {}", tmpStartMeasurement.getValue());
        }
        Measurement tmpEndMeasurement = calEndBorderMeasurement(endTime);
        if (tmpEndMeasurement != null) {
            intervalMeasurements.add(tmpEndMeasurement);
            logger.trace("Calculated a value for the interval end: {}", tmpEndMeasurement.getValue());

        }

        long missingTime;
        if (intervalMeasurements.size() < 1) {
            logger.trace("Could not calculate value for both borders. Cannot calculate a difference");
            missingTime = endTime - startTime;
            return new IntervalMeasurement(null, new MinMax(), missingTime, startTime, getRemarks());
        } else {
            missingTime = (endTime - startTime) - (intervalMeasurements.last().getTime() - intervalMeasurements.first().getTime());
            if (counterMode == CounterMode.COUNTERCONSUMPTION || counterMode == CounterMode.COUNTERPOWER) {
                return calculateUsageOrPowerFromDelta(missingTime, startTime, endTime);
            } else { //Counter value wanted
                return new IntervalMeasurement(intervalMeasurements.first().getValue(), new MinMax(intervalMeasurements.first().getValue(), intervalMeasurements.last().getValue()), missingTime, startTime, getRemarks());
            }
        }
    }

    /**
     * Checks if the interval has intervalMeasurements in it or one valid
     * measurment before it
     *
     * @return True if there are measurements in the interval or one before it
     */
    public boolean isEmptyInterval() {
        if (intervalMeasurements.isEmpty() && intervalMeasurements.getMeasurementBeforeInterval() == null) {
            return true;
        }
        return false;
    }

    /**
     * Calculates a counter value for the starttime of the interval<p> If there
     * is no measurement before the starttime of the interval or there already
     * is a value for the starttime
     * <code>null</code> is returned.
     *
     * @param startTime The time of the interval start
     * @return A interpolated counter value for the interval starttime or
     * <code>null</code> if there already exists a value or if no value can be
     * calculated
     */
    private Measurement calcStartBorderMeasurement(Long startTime) {
        if (intervalMeasurements.isEmpty()) {
            if (intervalMeasurements.getMeasurementBeforeInterval() != null && intervalMeasurements.getMeasurementAfterInterval() != null) {
                logger.trace("No values in interval. Use values before and after it to calculate STARTborder");
                return new Measurement(startTime, InterpolatorHelper.interpolate(startTime, intervalMeasurements.getMeasurementBeforeInterval().getTime(), intervalMeasurements.getMeasurementAfterInterval().getTime(), intervalMeasurements.getMeasurementBeforeInterval().getValue(), intervalMeasurements.getMeasurementAfterInterval().getValue()));
            }
            logger.trace("No value calculatable: STARTborder NULL");
            return null;
        }

        if (intervalMeasurements.first().getTime().equals(startTime)) {
            logger.trace("STARTborder already has a value ({})", intervalMeasurements.first().getValue());
            return null;
        }

        if (intervalMeasurements.getMeasurementBeforeInterval() != null) {
            if (checkStartTolerance(startTime)) {
                logger.trace("Valid previous value and valid interval value");
                return new Measurement(startTime, InterpolatorHelper.interpolate(startTime, intervalMeasurements.getMeasurementBeforeInterval().getTime(), intervalMeasurements.first().getTime(), intervalMeasurements.getMeasurementBeforeInterval().getValue(), intervalMeasurements.first().getValue()));
            } else if (counterMode.isShowPartUsage()) {
                logger.trace("Valid previous value but no internal value");
                return new Measurement(startTime, intervalMeasurements.getMeasurementBeforeInterval().getValue());
            }
        } else if (counterMode.isShowPartUsage()) {
            logger.trace("Partly usage allowed. No valid previous value but a interval value. Set STARTborder to that value ({})", intervalMeasurements.first().getValue());
            return new Measurement(startTime, intervalMeasurements.first().getValue());
        }
        logger.trace("No value calculatable: STARTborder NULL");
        return null;
    }

    /**
     * Calculates a counter value for the endtime of the interval<p> If there is
     * no measurement after the endtime of the interval or there already is a
     * value for the endtime
     * <code>null</code> is returned.
     *
     * @param endTime The time of the interval end
     * @return A interpolated counter value for the interval endtime or
     * <code>null</code> if there already exists a value or if no value can be
     * calculated
     */
    private Measurement calEndBorderMeasurement(Long endTime) {
        // if interval was empty in the beginning. try to calculate a value by interpolating the values around the end border
        if (intervalWasEmpty) {
            if (intervalMeasurements.getMeasurementBeforeInterval() != null && intervalMeasurements.getMeasurementAfterInterval() != null) {
                logger.trace("No values in interval. Use values before and after it to calculate ENDborder");
                return new Measurement(endTime, InterpolatorHelper.interpolate(endTime, intervalMeasurements.getMeasurementBeforeInterval().getTime(), intervalMeasurements.getMeasurementAfterInterval().getTime(), intervalMeasurements.getMeasurementBeforeInterval().getValue(), intervalMeasurements.getMeasurementAfterInterval().getValue()));
            }
            logger.trace("No value calculatable: ENDborder NULL");
            return null;
        }

        // at this point there are values in the interval from the database (not calculated and assigned to the start)
        if (intervalMeasurements.last().getTime().equals(endTime)) {
            logger.trace("ENDborder already has a value ({})", intervalMeasurements.last().getValue());
            return null;
        }

        if (intervalMeasurements.getMeasurementAfterInterval() != null) {
            if (checkEndTolerance(endTime)) {
                logger.trace("Valid next value and valid interval value");
                return new Measurement(endTime, InterpolatorHelper.interpolate(endTime, intervalMeasurements.last().getTime(), intervalMeasurements.getMeasurementAfterInterval().getTime(), intervalMeasurements.last().getValue(), intervalMeasurements.getMeasurementAfterInterval().getValue()));
            }
            if (counterMode.isShowPartUsage()) {
                logger.trace("Partly usage allowed. Valid next value but invalid internal value. Set ENDborder to that value ({})", intervalMeasurements.getMeasurementAfterInterval().getValue());
                return new Measurement(endTime, intervalMeasurements.getMeasurementAfterInterval().getValue());
            }
            return null;
        } else if (counterMode.isShowPartUsage()) {
            logger.trace("Partly usage allowed. No valid next value but a interval value. Set ENDborder to that value");
            return new Measurement(endTime, intervalMeasurements.last().getValue());
        }

        logger.trace("No value after interval. Cannot calculate.  ENDborder NULL");
        return null;
    }

    /**
     * Return the interval
     *
     * @return The interval
     */
    @Override
    public TimePeriod getInterval() {
        return interval;
    }

    /**
     * Set the interval
     *
     * @param interval The new interval
     */
    @Override
    public void setInterval(TimePeriod interval) {
        this.interval = interval;
    }

    /**
     * Return any remarks
     *
     * @return A list of generated remarks
     */
    @Override
    public ArrayList<String> getRemarks() {
        return remarks;
    }

    /**
     * Calculates the usage or power from the difference of two counter values
     *
     * @param missingTime
     * @param startTime The interval start time
     * @param endTime The interval end time
     * @return The result as {@link IntervalMeasurement}
     */
    private IntervalMeasurement calculateUsageOrPowerFromDelta(long missingTime, long startTime, long endTime) {
        // Usage or power wanted
        Double consumption;
        consumption = calculateDifferenceOfMeasurements(startTime, endTime); //; last().getValue(),- intervalMeasurements.first().getValue(); // Calculate usage from first and last counter value
        MinMax minmax = calculateMinMax();
        if (counterMode == CounterMode.COUNTERCONSUMPTION) { // Verbrauch
            return new IntervalMeasurement(consumption, minmax, missingTime, startTime, getRemarks()); // TODO min und max und missingTime?????
        } else {  // Leistung: TODO: calcStartBorder 10% ??
            if( intervalMeasurements == null || intervalMeasurements.getValueForTime(endTime) == null || intervalMeasurements.getValueForTime(startTime) == null )
            {
                // System.out.println( "null" );
            }
            if (intervalMeasurements.getValueForTime(endTime) - intervalMeasurements.getValueForTime(startTime) == 0) {
                logger.trace("Power is 0.");
                return new IntervalMeasurement(0d, minmax, missingTime, startTime, getRemarks());
            } else if (((endTime - startTime) - missingTime) > 0) {
                Double f = 3600000d / (double) ((endTime - startTime) - missingTime); // TODO Stunde /durch xxxx ????? gilt nur für Wh, kWh, MWh etc.... !!!!!!!!!!!!!!!!!!
                logger.trace("Calculating power.");
                return new IntervalMeasurement(f * consumption, minmax, missingTime, startTime, getRemarks()); // TODO min und max und missingTime?????
            } else {
                logger.trace("Calculating power not possible");
                return null;
            }
        }
    }

    /**
     * Calculate the difference of two values at the times given
     *
     * @param startTime Time of the first (lower) value
     * @param endTime Time of the second (higher) value
     * @return
     */
    public Double calculateDifferenceOfMeasurements(long startTime, long endTime) {
        if (!intervalMeasurements.isEmpty() && intervalMeasurements.getValueForTime(endTime) != null && intervalMeasurements.getValueForTime(startTime) != null) {
            Double diff = intervalMeasurements.getValueForTime(endTime) - intervalMeasurements.getValueForTime(startTime);
            if (changes.size() > 0) {
                Double sum = 0d;
                Double s1 = intervalMeasurements.getValueForTime(startTime);
                for (CounterChange change : changes) {
                    remarks.add("Counter change at " + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(change.getTime()) + "\n");
                    Double s2 = change.getOldValue();
                    sum += s2 - s1;
                    s1 = change.getNewValue();
                }
                sum += intervalMeasurements.getValueForTime(endTime) - s1;
                diff = sum;
                logger.trace("diff was neg " + new Date(startTime) + " " + new Date(endTime) + " >>>>> " + diff);
            } else if (diff < 0 || isStepInInterval(startTime, endTime)) {
                return null;
            }

            Object[] paramArray = {diff, intervalMeasurements.getValueForTime(endTime), intervalMeasurements.getValueForTime(startTime)}; // for logging
            logger.trace("Calculating difference {} = {} - {}", paramArray);
            return diff;
        }

        logger.trace("Consumption not calculatable. Not enough measurements.");
        return Double.NaN;
    }

    private boolean isStepInInterval(long startTime, long endTime) {
        boolean b = false;
        for (CounterStep step : steps) {
            if ((startTime >= step.getOldMeasurement().getTime() && startTime <= step.getNewMeasurement().getTime()) || (endTime >= step.getOldMeasurement().getTime() && endTime <= step.getNewMeasurement().getTime())) {
                return true;
            }
        }
        return b;
    }

    /**
     * Calculates the minimum and maximum of values in the interval
     *
     * @return The {@link MinMax} values
     */
    private MinMax calculateMinMax() {
        MinMax minmax = new MinMax();

        Long time1 = null;
        Long time2;
        Double value1 = null;
        Double value2;
        Double valueDiff;
        Long timeDiff;

        boolean first = true;
        for (Measurement m : intervalMeasurements) {
            if (first) {
                time1 = m.getTime();
                value1 = m.getValue();
                first = false;
                continue;
            }

            time2 = m.getTime();
            value2 = m.getValue();

            valueDiff = value2 - value1;
            timeDiff = time2 - time1;

            time1 = time2;
            value1 = value2;

            if (counterMode == CounterMode.COUNTERCONSUMPTION) {
                minmax.probeValue(valueDiff);
            } else {
                Double f = 3600000d / timeDiff;
                minmax.probeValue(valueDiff * f);
            }
        }
        return minmax;
    }

    /**
     * Checks the last measurement in the interval (if any) to be valid for
     * end-border-calculation using a tolerance
     *
     * @param endTime the time of the end border of the interval
     * @param toleratedDuration the tolerated gap between time of measurement
     * and border time
     * @return
     */
    private boolean checkEndTolerance(long endTime) { //, Long toleratedDuration) {
        if (!intervalMeasurements.last().getTime().equals(endTime)) {   // tolerance met if there is a value at the interval limit or the duration of validity is null (= infinite)
            if (intervalMeasurements.isEmpty() || (durationOfValidity != null && (endTime - intervalMeasurements.last().getTime()) - durationOfValidity > edgeTolerance)) {
                logger.trace("tolerance violation at end. Number of values: " + intervalMeasurements.size() + " gap: " + (endTime - intervalMeasurements.last().getTime()) / 1000 / 60 / 60 + " h. Allowed: " + edgeTolerance / 1000 / 60 / 60 + " h");
                return false;
            }
        }
        return true;
    }

    /**
     * Checks the first measurement in the interval (if any) to be valid for
     * start-border-calculation using a tolerance
     *
     * @param startTime the time of the start border of the interval
     * @param toleratedDuration the tolerated gap between time of measurement
     * and border time
     * @return
     */
    private boolean checkStartTolerance(long startTime) { //, Long toleratedDuration) {
        if (!intervalMeasurements.first().getTime().equals(startTime)) { // tolerance met if there is a value at the interval limit
            if (intervalMeasurements.isEmpty() || intervalMeasurements.first().getTime() - startTime > edgeTolerance) { // no validity because it only affects the future
                logger.trace("tolerance violation at start. Number of values: " + intervalMeasurements.size() + " gap: " + (intervalMeasurements.first().getTime() - startTime) / 1000 / 60 / 60 + " h. Allowed: " + edgeTolerance / 1000 / 60 / 60 + " h");
                return false;
            }
        }
        return true;
    }

    @Override
    public void setDurationOfValidity(Long validity) {
        this.durationOfValidity = validity;
    }

    /**
     * Set the edgeTolerance
     *
     * @param tolerance The tolrence in ms
     */
    public void setEdgeTolerance(Long tolerance) {
        edgeTolerance = tolerance;
    }

    /**
     * Return the state of the usage mode
     *
     * @return <code>true</code> if the calculation should also return a result
     * iven if the interval was not covered sufficiently
     */
    public boolean isShowPartUsage() {
        return counterMode.isShowPartUsage();
    }

    /**
     * Return the final results of the interpolation
     *
     * @return The results as {@link MeasurementTreeSet}
     */
    public MeasurementTreeSet getIntervalMeasurements() {
        return intervalMeasurements;
    }

    /**
     * Sets the logger to the given one
     *
     * @param logger The new logger
     */
    public void setLogger(ch.qos.logback.classic.Logger logger) {
        this.logger = logger;
    }
}
