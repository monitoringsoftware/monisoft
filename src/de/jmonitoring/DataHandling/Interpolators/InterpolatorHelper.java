package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import java.util.Date;
import org.jfree.data.time.TimePeriod;

/**
 * Helper class for common calculations of the {@link Interpolator}
 *
 * @author togro
 */
public class InterpolatorHelper {

    private static ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    public InterpolatorHelper() {
    }

    /**
     * Tries to find the last measurement immediately before the interval
     * start<p> if there is no such mesurement within the allowed tolerances the
     * returned value is
     * <code>null</code>
     *
     * @param completeMap A map of all measurements
     * @param startMeasurement A (fake) measurement at the interval start
     * @param valid The duration of validity in ms
     * @param edgeTolerance The egdeTorenace in ms (should be null if no
     * counter)
     * @return The last measurement directly before the interval start
     */
    public static synchronized Measurement getPreviousKeepValue(MeasurementTreeSet completeMap, Measurement startMeasurement, Long valid, Long edgeTolerance) {
        Measurement lowerCandidate = completeMap.lower(startMeasurement);
        if (lowerCandidate == null) {
            logger.trace("No value before interval found");
            return null;
        }

        Object[] logArray1 = {lowerCandidate.getValue(), new Date(lowerCandidate.getTime())};
        logger.trace("Candidate value before interval: {} at {}", logArray1);

        long timeGap = startMeasurement.getTime() - lowerCandidate.getTime();
        Long useValid;
        if (valid == null) {
            useValid = Long.MAX_VALUE;
            logger.trace("Value is valid forever");
        } else {
            useValid = valid;
            Object[] logArray2 = {valid / 1000 / 60, timeGap};
            logger.trace("Value is invalid after {} minutes. Found gap is {} minutes", logArray2);
        }

        if (edgeTolerance == null) { // it is not a counter
            if (timeGap <= useValid) {
                logger.trace("No counter! Gap is tolerated");
                return new Measurement(lowerCandidate.getTime(), lowerCandidate.getValue());
            }
            logger.trace("No counter! Gap is NOT tolerated with tolerance {}", edgeTolerance);
        } else {
            if (edgeTolerance == Long.MAX_VALUE || timeGap - useValid <= edgeTolerance) { // it is a counter
                logger.trace("Counter! Gap is tolerated");
                return new Measurement(lowerCandidate.getTime(), lowerCandidate.getValue());
            }
            logger.trace("Counter! Gap is NOT tolerated with tolerance {}", edgeTolerance);
        }
        return null;
    }

    /**
     * Tries to find the next measurement immediately after the interval end<p>
     * if there is no such measurment within the allowed tolerances the returned
     * value is
     * <code>null</code>
     *
     * @param completeMap A map of all measurements
     * @param endMeasurement
     * @param valid The duration of validity in ms
     * @param edgeTolerance The egdeTorenace in ms (should be null if no
     * counter)
     * @return
     */
    public static synchronized Measurement getNextKeepValue(MeasurementTreeSet completeMap, Measurement endMeasurement, Long valid, Long edgeTolerance) {
        Measurement upperCandidate = completeMap.higher(endMeasurement);
        if (upperCandidate == null) {
            logger.trace("No value after interval found");
            return null;
        }

        Object[] logArray1 = {upperCandidate.getValue(), new Date(upperCandidate.getTime())};
        logger.trace("Candidate value after interval: {} at {}", logArray1);

        long timeGap = upperCandidate.getTime() - endMeasurement.getTime();
        Long useValid;
        if (valid == null) {
            useValid = Long.MAX_VALUE;
            logger.trace("Value is valid forever");
        } else {
            useValid = valid;
            Object[] logArray2 = {valid / 1000 / 60, timeGap};
            logger.trace("Value is invalid after {} minutes. Found gap is {} minutes", logArray2);
        }

        if (edgeTolerance == null) { // it is not a counter
            if (timeGap <= useValid) {
                logger.trace("No counter! Gap is tolerated");
                return new Measurement(upperCandidate.getTime(), upperCandidate.getValue());
            }
            logger.trace("No counter! Gap is NOT tolerated with tolerance {}", edgeTolerance);
        } else { // it is a counter
            if (edgeTolerance == Long.MAX_VALUE || timeGap - useValid <= edgeTolerance) {
                logger.trace("Counter! Gap is tolerated");
                return new Measurement(upperCandidate.getTime(), upperCandidate.getValue());
            }
            logger.trace("Counter! Gap is NOT tolerated with tolerance {}", edgeTolerance);
        }
        return null;
    }

    /**
     * Calculates the decimal value of the cover tolerance from the given
     * percent value<p> If there is no tolerance wished or its value is invalid
     * the returned value is NULL, which means there is no tolerance allowed<p>
     * 0% => 0.0<br> 50% => 0.5<br> 100% => 1.0<<br>
     *
     * @return The decimal value of the cover tolerance
     */
    public static synchronized Double getCoverTolerance() {
        if (!MoniSoft.getInstance().getApplicationProperties().getProperty("UseCoverTolerance").equals("1")) {
            return null;
        }

        Double percent;
        try {
            percent = Double.valueOf(MoniSoft.getInstance().getApplicationProperties().getProperty("CoverTolerance"));
        } catch (NullPointerException e) {
            logger.trace("Invalid coverage value: {}" + MoniSoft.getInstance().getApplicationProperties().getProperty("CoverTolerance"));
            return null;
        }

        // calculate the tolerance in decimals form the given percent value
        if ((percent > 0 && percent <= 100)) {
            return percent / 100d;
        } else {
            logger.trace("Invalid coverage value: {}.Must be > 0 <= 100" + percent);
            return null; // invalid range: ignoriere (no tolerance used)
        }
    }

    /**
     * Returns the timspan around the interval edge in which a counter value
     * must be to be valid for calculation.<p> Uses the value of the
     * <code>SensorProperties</code> (in minutes) and return the value in
     * ms.<br> If the value is not specified the returned value is
     * <code>Long.MAX_VALUE</code> which means there is no restriction.
     *
     * @param interval the current aggregation interval
     * @return - the lenght of the tolerance area around the interval border in
     * ms
     */
    public synchronized static Long getEdgeTolerance(TimePeriod interval) {
        if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseEdgeTolerance").equals("1")) {
            double edgeTolerancePercent = Long.valueOf(MoniSoft.getInstance().getApplicationProperties().getProperty("EdgeTolerance")) / 100d; // Prozent in Dezimalzahl umwandeln
            return Math.round((interval.getEnd().getTime() - interval.getStart().getTime()) * edgeTolerancePercent); // Dauer der Toleranz abhängig vom Intervall berechnen
        }
        return Long.MAX_VALUE;
    }

    /**
     * Calculates the duration of validity in ms for the given sensor.<p> Uses
     * the value of the
     * <code>SensorProperties</code> (in minutes) and returns the value in
     * ms.<br> If the duration is not specified the returned value is null which
     * means the value is valid forever. If the duration is 0 the value expires
     * immediately.
     *
     * @param sensorID the sensor
     * @return the duration of validity in ms
     */
    public synchronized static Long getDurationOfValidity(int sensorID) {
        SensorProperties props = SensorInformation.getSensorProperties(sensorID);
        if (props.getMaxChangeTimes()[0] == null || props.getMaxChangeTimes()[0] < 0) {
            return null;
        }

        return props.getMaxChangeTimes()[0] * 60000L; // convert minutes to ms
    }

    /**
     * Calculates a 'virtual', ascending counter form the given usage datset.<p>
     * The new datset starts at 0 and is calculated by accumulating the usage
     * values.
     *
     * @param dataSet The usage dataset
     * @return The new dataset of 'virtual' counter readings
     */
    public synchronized static MeasurementTreeSet generateCounterValues(MeasurementTreeSet dataSet) {
        MeasurementTreeSet newSet = MeasurementTreeSet.empty(dataSet.getUnit());
        Double valueSum = 0d;
        for (Measurement measurement : dataSet) {
            if (measurement.getValue() != null) {
                valueSum += Math.abs(measurement.getValue());
                newSet.add(new Measurement(measurement.getTime(), valueSum));
            }
        }

        return newSet;
    }

    /**
     * Calculates a 'virtual', ascending counter form the given resetting
     * counter datset.<p>
     * The new datset starts at the first counter value
     *
     * @param dataSet The dataset of a resetting counter
     * @return The new dataset of 'virtual' counter readings
     */
    public synchronized static MeasurementTreeSet generateAscendingCounterValues(MeasurementTreeSet dataSet) {
        MeasurementTreeSet newSet = MeasurementTreeSet.empty(dataSet.getUnit());
        Double valueDiff = 0d;
        Double lastValue = Double.NaN;
        Double ascendingValue = Double.NaN;
        for (Measurement measurement : dataSet) {
            if (ascendingValue.isNaN()) { // erster Durchlauf, Wert direkt übernehmen
                newSet.add(new Measurement(measurement.getTime(), measurement.getValue()));
                ascendingValue = measurement.getValue();
                lastValue = ascendingValue;
                continue;
            }

            valueDiff = measurement.getValue() - lastValue;
            lastValue = measurement.getValue();
            
            if (valueDiff >= 0) {
                ascendingValue = ascendingValue + valueDiff;
            } else {
                ascendingValue = ascendingValue + measurement.getValue();
            }
            newSet.add(new Measurement(measurement.getTime(), ascendingValue));
        }

        return newSet;
    }

    /**
     * Linear interpolation at (x,y)=(x<sub>i</sub>,y<sub>i</sub>) based on the
     * points (x1,y1)=(x<sub>i-1</sub>,y<sub>i-1</sub>) and
     * (x2,y2)=(x<sub>i+1</sub>,y<sub>i+1</sub>)
     *
     * @param x1 Time of 'left' data point
     * @param x2 Time of 'right' data point
     * @param x Time of interpolated point
     * @param y1 Value of 'left' data point
     * @param y2 Value of 'right' data point
     * @return The coordinates of the interpolated point (y)
     */
    public static synchronized Double interpolate(long x, long x1, long x2, double y1, double y2) {
//        DecimalFormat decimalFormat = new DecimalFormat("0.###");
//        System.out.println("interplate " + decimalFormat.format(((y2 - y1) / (double) (x2 - x1)) * (double) (x - x1) + y1) + " an " + new Date(x) + " mit " + new Date(x1) + "\t" + new Date(x2) + "\t\tWerte: " + decimalFormat.format(y1) + " " + decimalFormat.format(y2));
        return ((y2 - y1) / (double) (x2 - x1)) * (double) (x - x1) + y1;
    }
}
