package de.jmonitoring.standardPlots.common;

import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.intervals.CustomMinutePeriod;
import de.jmonitoring.utils.intervals.CustomSecondPeriod;
import java.util.Date;
import org.jfree.data.time.*;

/**
 * Defining methods used by all dataset generators
 *
 * @author togro
 */
public abstract class GeneralDataSetGenerator {

    /**
     * Return the {@link RegularTimePeriod} object defined by the given
     * parameters
     *
     * @param interval The interval type TODO: replace by enum or int
     * @param time The time which should be converted
     * @return
     */
    public static RegularTimePeriod getPeriodForTimeStamp(double interval, Long time) {
        RegularTimePeriod regPeriod = null;
        if (interval < 0) {
            switch ((int) interval) {
                case (int) MoniSoftConstants.RAW_INTERVAL:
                    regPeriod = null;
                    break;
                case (int) MoniSoftConstants.HOUR_INTERVAL:
                    regPeriod = new Hour(new Date(time));
                    break;
                case (int) MoniSoftConstants.DAY_INTERVAL:
                    regPeriod = new Day(new Date(time));
                    break;
                case (int) MoniSoftConstants.WEEK_INTERVAL:
                    regPeriod = new Week(new Date(time));
                    break;
                case (int) MoniSoftConstants.MONTH_INTERVAL:
                    regPeriod = new Month(new Date(time));
                    break;
                case (int) MoniSoftConstants.YEAR_INTERVAL:
                    regPeriod = new Year(new Date(time));
                    break;
//                default:
//                    regPeriod = new CustomMinutePeriod((byte) interval, new Date(time));
            }
        } else if (interval < 1) {
            regPeriod = new CustomSecondPeriod((byte) (interval * 60), new Date(time));
        } else {
            regPeriod = new CustomMinutePeriod((byte) interval, new Date(time));
        }
        return regPeriod;
    }

    /**
     * Return the interval suffix defined by the given parameters
     *
     * @param aggType The aggregation interval
     * @param showAsPower Flag that indicates if the user wants to calculate
     * power
     * @param showAsCounter Flag that indicates if the user wants to show
     * counter values
     * @param isCounter Flag taht indicates if the sensor is a counter
     * @return The suffix
     */
    public static String getSuffix(double aggInterval, boolean showAsPower, boolean showAsCounter, boolean isCounter, boolean hasUsageUnit) {
        String suffix = "";
        // 1. Teil

        if (aggInterval < 0) {
            switch ((int) aggInterval) {
                case (int) MoniSoftConstants.RAW_INTERVAL:
                    suffix = "(" + 10 + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MIN");
                    break;
                case (int) MoniSoftConstants.HOUR_INTERVAL:
                    suffix = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("STUNDEN");
                    break;
                case (int) MoniSoftConstants.DAY_INTERVAL:
                    suffix = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("TAGES");
                    break;
                case (int) MoniSoftConstants.WEEK_INTERVAL:
                    suffix = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("WOCHEN");
                    break;
                case (int) MoniSoftConstants.MONTH_INTERVAL:
                    suffix = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MONATS");
                    break;
                case (int) MoniSoftConstants.YEAR_INTERVAL:
                    suffix = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("JAHRES");
                    break;
//                default:
//                    suffix = " (" + aggInterval + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MIN");
            }
        } else if (aggInterval < 1) {
            suffix = "x Sekunden";
        } else {
            suffix = "(" + aggInterval + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MIN");
        }

        // 2. Teil
        if ((isCounter || hasUsageUnit) && showAsPower) {
            suffix += java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LEISTUNG");
        } else if ((isCounter || hasUsageUnit) && showAsCounter) {
            suffix += java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("ZÄHLERSTAND");
        } else if ((isCounter || hasUsageUnit)) {
            suffix += java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("VERBRAUCH");
        } else {
            if (suffix.contains(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MIN"))) {
                suffix += java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("-MITTEL)");
            } else {
                suffix += java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("MITTEL)");
            }
        }
        return suffix;
    }
}
