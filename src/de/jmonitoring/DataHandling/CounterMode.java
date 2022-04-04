package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.InterpolatorInterface;
import de.jmonitoring.DataHandling.Interpolators.CounterInterpolator;
import de.jmonitoring.DataHandling.Interpolators.InterpolatorHelper;
import de.jmonitoring.DataHandling.Interpolators.MinMax;
import de.jmonitoring.DataHandling.Interpolators.WeightedMeanInterpolator;
import java.util.Collections;

import org.jfree.data.time.TimePeriod;

import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;

/**
 * This enum conatains the counter modes used for interval calculation<p> The
 * overridden methods behave according the the needs of the respective mode.
 *
 * @author togro
 */
public enum CounterMode {

    NOCOUNTER {
        @Override
        public Double obtainCoverTolerance() {
            return InterpolatorHelper.getCoverTolerance();
        }

        @Override
        public Long obtainEdgeTolerance(TimePeriod aggregationInterval) {
            return null;
        }
    },
    COUNTERCONSUMPTION,
    COUNTERPOWER,
    COUNTERVALUE,
    RESET_COUNTERCONSUMPTION,
    RESET_COUNTERPOWER,
    RESET_COUNTERVALUE,    
    NO_COUNTER_KUMULIERTE_LEISTUNG,
    NO_COUNTER_ZEIGE_WERTE,
    NO_COUNTER_KUMULIERUNG;
    private static boolean autoCounterChange = false;
    private static boolean showPartUsage = true;

    public Double obtainCoverTolerance() {
        return 0d;
    }

    public Long obtainEdgeTolerance(TimePeriod aggregationInterval) {
        return InterpolatorHelper.getEdgeTolerance(aggregationInterval);
    }

    public static IntervalMeasurement computeLastMeasurement(Long startTime, Measurement lastValidMeasurement) {
        return new IntervalMeasurement(lastValidMeasurement.getValue(), new MinMax(), 0L, startTime, Collections.<String>emptyList());
    }

    public boolean isAutoCounterChange() {
        return autoCounterChange;
    }

    public void setAutoCounterChange(boolean b) {
        autoCounterChange = b;
    }

    public boolean isShowPartUsage() {
        return showPartUsage;
    }

    public static void setShowPartUsage(boolean showPartUsage) {
        CounterMode.showPartUsage = showPartUsage;
    }

    public static CounterMode getInterpolationMode(boolean showAsPower, boolean showAsCounter, boolean isCounter, boolean hasUsageUnit, boolean auto, boolean partly) {
        autoCounterChange = auto;
        showPartUsage = partly;
        // Zähler
        if (isCounter || hasUsageUnit) {
            if (showAsCounter) {
                return COUNTERVALUE;
            }
            if (showAsPower) {
                return COUNTERPOWER;
            }
            return COUNTERCONSUMPTION;
        }
        // Kein Zähler
        else if( !isCounter && showAsPower )
            return NO_COUNTER_ZEIGE_WERTE;
        else if( !isCounter && showAsCounter )
            return NO_COUNTER_KUMULIERUNG;
            // return NOCOUNTER;
        else
            return NO_COUNTER_KUMULIERTE_LEISTUNG;
    }
}
