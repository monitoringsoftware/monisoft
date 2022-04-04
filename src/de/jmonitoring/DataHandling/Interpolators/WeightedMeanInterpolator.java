package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.DataHandling.IntervalMeasurement;
import de.jmonitoring.base.MoniSoft;
import java.util.ArrayList;
import java.util.Iterator;

import org.jfree.data.time.TimePeriod;

import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;

/**
 * This implementation of the {@link InterpolatorInterface} is used to
 * calculated intervals by weighted means.<p> It is suitable for sensors like
 * temperatures, humidities etc.
 *
 * @author togro
 */
public class WeightedMeanInterpolator implements InterpolatorInterface {

    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();
    private TimePeriod interval;
    private MeasurementTreeSet intervalMeasurements;
    private Long durationOfValidity;
    private Double coverTolerance;
    private ArrayList<String> remarks = new ArrayList<String>();

    /**
     * Create a new interpolator
     *
     * @param interval The interval for which to calculate a value
     * @param intervalMeasurements The measurements relevsnt for this interval
     * @param coverTolerance The covertolerance if applicable
     * @param valid The duration of validity of thsi sensor
     */
    public WeightedMeanInterpolator(TimePeriod interval, MeasurementTreeSet intervalMeasurements, Double coverTolerance, Long valid) {
        this.interval = interval;
        this.intervalMeasurements = MeasurementTreeSet.copyOf(intervalMeasurements);
        this.intervalMeasurements.setMeasurementBeforeInterval(intervalMeasurements.getMeasurementBeforeInterval());
        this.intervalMeasurements.setMeasurementAfterInterval(intervalMeasurements.getMeasurementAfterInterval());
        this.durationOfValidity = valid;
        this.coverTolerance = coverTolerance;
    }

    /**
     * Start calculation of the interval
     *
     * @return A {@link IntervalMeasurement} holding the result *      * or <code>null</code> if no result could be calculated
     */
    @Override
    public synchronized IntervalMeasurement calculateInterval() {
        logger.trace("*** New interval " + interval.getStart() + " " + interval.getEnd());
        long endTime = interval.getEnd().getTime() + 1L; // TODO +1 überdenken
        long startTime = interval.getStart().getTime();

        if (isEmptyInterval()) {
            logger.trace("Interval is empty and no value before it");
            return new IntervalMeasurement(null, new MinMax(), endTime - startTime, startTime, getRemarks());
        }

        // Ab hier gibt es  Werte IM Intervall ODER auf den Intervallgrenzen (mindestens einen).
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

        int n = intervalMeasurements.size(); // has to be before the next loop because intervalMeasurements will change its size in it

        // Vorbelegen der Werte für den ersten Durchlauf (von hinten mit den jüngsten Daten)
        Double subIntervalEndValue = null;
        Long subIntervalEndTime = null;
        if (n > 0) {
            Measurement runningMeasurement = intervalMeasurements.pollLast(); // get value of and remove last entry
            subIntervalEndValue = runningMeasurement.getValue();
            subIntervalEndTime = runningMeasurement.getTime();
        }
        Double weightsum = 0d;
        Double intervalSum = 0d;
        Long intervalSpan = endTime - startTime;
        Double valueSum = subIntervalEndValue;
        MinMax minmax = new MinMax();

        // loop all remaing measuremnts in interval (backwards in time)
        for (Iterator<Measurement> it = intervalMeasurements.descendingIterator(); it.hasNext();) {
            Measurement measurement = it.next();

            Long currentTime = measurement.getTime();
            Double currentValue = measurement.getValue();

            minmax.probeValue(currentValue);

            Long subIntervalTimeSpan = subIntervalEndTime - currentTime;         // Zeit zwischen letztem Wert und dem momentanen Wert

            if (durationOfValidity != null && durationOfValidity > 0 && subIntervalTimeSpan > durationOfValidity) {                 // begrenzen auf die maximale Gültigkeitsdauer
                logger.trace("Setze subIntervalTimeSpan von " + subIntervalTimeSpan + " auf " + durationOfValidity);
                subIntervalTimeSpan = durationOfValidity;
            }

            valueSum += currentValue;  // Summer aller Werte erhöhen
            Double weight = (double) subIntervalTimeSpan / (double) intervalSpan;    // Zeitlicher Anteil des Subintervalls am Gesamtintervall

            Object[] logObjects1 = {weight, subIntervalTimeSpan / 1000, intervalSpan / 1000};
            logger.trace("Gewicht: {} Zeitanteil {} Sekunden von {} Sekunden", logObjects1);

            weightsum += weight;                                            // Summe der Gewichte
            intervalSum += weight * (currentValue + subIntervalEndValue) / 2d;

            Object[] logObjects2 = {weight, currentValue, subIntervalEndValue, intervalSum, weightsum};
            logger.trace("Mittelung:\tGewicht {}\tWert {}\tSUIEnd {}\tSumme {} GewichteSumme {}", logObjects2);

            subIntervalEndTime = currentTime;
            subIntervalEndValue = currentValue;

            it.remove();
        }

        weightsum = weightsum > 1.0 ? 1.0 : weightsum; // limit weightsum to exacty one (sometimes it gets numerically fractional greater than 1)

        long missingTime = (long) ((endTime - startTime) * (1 - weightsum));

        if (weightsum.equals(0d)) {
            return new IntervalMeasurement(null, minmax, missingTime, startTime, getRemarks());
        }

        // build interval value
        Double intervalValue;
        if (coverTolerance == null) { // no covertolerance must be met - every interval that has any values is valid
            if (1.0 - weightsum > 0.0000001) {
                logger.trace("NO covertolerance needed but there are gaps");
                intervalValue = intervalSum + ((1.0 - weightsum) * valueSum / (double) n);
            } else {
                logger.trace("Interval fully covered");
                intervalValue = intervalSum;
            }
        } else { // a covertolerance must be met
            logger.trace("weightsum: {}", weightsum);
            if (weightsum < coverTolerance) {
                logger.trace("Covertolerance {} exceeded (weightsum: {}) -  Intervall = NULL", coverTolerance, weightsum);
                intervalValue = null;
            } else if (1.0 - weightsum > 0.0000001) {
                logger.trace("Covertolerance complied with but there are gaps");
                intervalValue = intervalSum + ((1.0 - weightsum) * valueSum / (double) n);
            } else {
                logger.trace("Interval fully coverered");
                intervalValue = intervalSum;
            }
        }
        logger.trace("INTERVALVALUE: {}", intervalValue);
        return new IntervalMeasurement(intervalValue, minmax, missingTime, startTime, getRemarks());
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
     * Set the duration of validity
     *
     * @param validity The new validity duration in ms
     */
    @Override
    public void setDurationOfValidity(Long validity) {
        this.durationOfValidity = validity;
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
     * Checks if the interval has intervalMeasurements in it or one valid
     * measurment before it
     *
     * @return <code>true</code> if there are measurements in the interval or one before it
     */
    private synchronized boolean isEmptyInterval() {
        if (intervalMeasurements.isEmpty() && intervalMeasurements.getMeasurementBeforeInterval() == null) {
            return true;
        }
        return false;
    }

    /**
     * Calculates a value for the starttime of the interval<p> If there is no
     * measurement before the starttime of the interval or there already is a
     * value for the starttime
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
            logger.trace("Valid previous value and interval value");
            return new Measurement(startTime, InterpolatorHelper.interpolate(startTime, intervalMeasurements.getMeasurementBeforeInterval().getTime(), intervalMeasurements.first().getTime(), intervalMeasurements.getMeasurementBeforeInterval().getValue(), intervalMeasurements.first().getValue()));
        }

        logger.trace("No value calculatable: STARTborder NULL");
        return null;
    }

    /**
     * Calculates a value for the endtime of the interval<p> If there is no
     * measurement after the endtime of the interval or there already is a value
     * for the endtime
     * <code>null</code> is returned.
     *
     * @param endTime The time of the interval end
     * @return A interpolated counter value for the interval endtime or
     * <code>null</code> if there already exists a value or if no value can be
     * calculated
     */
    private Measurement calEndBorderMeasurement(Long endTime) {
        if (intervalMeasurements.isEmpty()) {
            if (intervalMeasurements.getMeasurementBeforeInterval() != null && intervalMeasurements.getMeasurementAfterInterval() != null) {
                logger.trace("No values in interval. Use values before and after it to calculate ENDborder");
                return new Measurement(endTime, InterpolatorHelper.interpolate(endTime, intervalMeasurements.getMeasurementBeforeInterval().getTime(), intervalMeasurements.getMeasurementAfterInterval().getTime(), intervalMeasurements.getMeasurementBeforeInterval().getValue(), intervalMeasurements.getMeasurementAfterInterval().getValue()));
            }
            logger.trace("No value calculatable: STARTborder NULL");
            return null;
        }

        if (intervalMeasurements.last().getTime().equals(endTime)) {
            logger.trace("ENDborder already has a value ({})", intervalMeasurements.last().getValue());
            return null;
        }

        if (intervalMeasurements.getMeasurementAfterInterval() != null) {
            logger.trace("Valid next value and interval value");
            return new Measurement(endTime, InterpolatorHelper.interpolate(endTime, intervalMeasurements.last().getTime(), intervalMeasurements.getMeasurementAfterInterval().getTime(), intervalMeasurements.last().getValue(), intervalMeasurements.getMeasurementAfterInterval().getValue()));
        }

        logger.trace("No value calculatable: ENDborder NULL");
        return null;
    }
}
