/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Consistency;

import de.jmonitoring.DataHandling.DatabaseQuery;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.intervals.DateInterval;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author togro
 */
public class ValueTests {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(MoniSoftConstants.HumanDateFormat);
    private final DecimalFormat decimalFormat = new DecimalFormat("0.###");
    private final DataCheckFrame.Worker worker;
    private HashMap<Integer, LimitExceedance> exceedanceCollection = new HashMap<Integer, LimitExceedance>();

    public ValueTests(DataCheckFrame.Worker worker) {
        this.worker = worker;
    }

    public void checkLimits(Date day, int sensorID) {
    }

    /**
     * Check regularity of all sensors except:<br> <ul><li>manual
     * sensors</li><li>virtual sensors</li><li>event sensors</li></ul>
     *
     * @param dateInterval the time span to be checked
     * @return the gathered text messages with the results
     */
    public String checkAllRegularity(DateInterval dateInterval) {
        StringBuilder regSb = new StringBuilder(1024);
        StringBuilder limitSb = new StringBuilder(1024);
        int count = 1;
        for (SensorProperties props : SensorInformation.getSensorList()) {
            if (wasCanceled()) {
                return regSb.toString();
            }
            tellProgress(count++);
            String[] result = new String[2];
            if (!props.isVirtual() && !props.isEvent() && !props.isManual()) { // skip virtual, manual and event sensors
                if (props.getInterval() > 0) { //  check if the sensor coems in intervals or irregular
                    result = (checkIntervalRegularity(dateInterval, props.getSensorID(), 0));
                } else if ((props.getMaxChangeTimes()[0] != null && props.getMaxChangeTimes()[0] > 0) || (props.getMaxChangeTimes()[1] != null && props.getMaxChangeTimes()[1] > 0)) {
                    result = checkChangeRegularity(dateInterval, props.getSensorID());
                }
            }

            if (result[0] != null) {
                regSb.append(result[0]);
            }
            if (result[1] != null) {
                limitSb.append(result[1]);
            }
        }

        String regularityTitle = "Regularity tests";
        String limitTitle = "Value range tests";
        String limitsOK = "No exceedances found";
        String regularityOK = "No irregularity found";

        if (worker != null) {
            regularityTitle = "<font color=\"#000000\" face=\"courier\" size=\"3\"><b>" + regularityTitle + "</b></font><br>";
            limitTitle = "<p><font color=\"#000000\" face=\"courier\" size=\"3\"><b>" + limitTitle + "</b></font><br>";
            limitsOK = "<font color=\"#000000\" face=\"courier\" size=\"2\">" + limitsOK + "</font><br>";
            regularityOK = "<font color=\"#000000\" face=\"courier\" size=\"2\">" + regularityOK + "</font><br>";
        } else {
            regularityTitle = regularityTitle + "\n";
            limitTitle = "\n" + limitTitle + "\n";
            limitsOK = limitsOK + "\n";
            regularityOK = regularityOK + "\n";
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(regularityTitle);
        if (!regSb.toString().isEmpty()) {
            resultBuilder.append(regSb.toString());
        } else {
            resultBuilder.append(regularityOK);
        }

        resultBuilder.append(limitTitle);
        if (!limitSb.toString().isEmpty()) {
            resultBuilder.append(limitSb.toString());
        } else {
            resultBuilder.append(limitsOK);
        }

        return resultBuilder.toString();


    }

    /**
     * Check regularity of the sensors in the given collection except:<br>
     * <ul><li>manual sensors</li><li>virtual sensors</li><li>event
     * sensors</li></ul>
     *
     * @param dateInterval the time span to be checked
     * @param sensorCollection the collection to be checked
     * @return the gathered text messages with the results
     */
    public String checkRegularityOf(DateInterval dateInterval, String sensorCollection) {
        HashSet<Integer> sensors = SensorCollectionHandler.getSensorsForCollection(sensorCollection);

        StringBuilder regSb = new StringBuilder(1024);
        StringBuilder limitSb = new StringBuilder(1024);
        int count = 1;
        for (Integer sensorID : sensors) {
            if (wasCanceled()) {
                return regSb.toString();
            }
            tellProgress(count++);
            String[] result = new String[2];
            SensorProperties props = SensorInformation.getSensorProperties(sensorID);
            tellProgress(count++);
            if (!props.isVirtual() && !props.isEvent() && !props.isManual()) { // skip virtual, manual and event sensors
                if (props.getInterval() > 0) { //  check if the sensor coems in intervals or irregular
                    result = checkIntervalRegularity(dateInterval, props.getSensorID(), 0);
                } else if ((props.getMaxChangeTimes()[0] != null && props.getMaxChangeTimes()[0] > 0) || (props.getMaxChangeTimes()[1] != null && props.getMaxChangeTimes()[1] > 0)) {
                    result = checkChangeRegularity(dateInterval, props.getSensorID());
                }
            }

            if (result[0] != null) {
                regSb.append(result[0]);
            }
            if (result[1] != null) {
                limitSb.append(result[1]);
            }
        }

        String regularityTitle = "Regularity tests";
        String limitTitle = "Value range tests";
        String limitsOK = "No exceedances found";
        String regularityOK = "No irregularity found";

        if (worker != null) {
            regularityTitle = "<font color=\"#000000\" face=\"courier\" size=\"3\"><b>" + regularityTitle + "</b></font><br>";
            limitTitle = "<p><font color=\"#000000\" face=\"courier\" size=\"3\"><b>" + limitTitle + "</b></font><br>";
            limitsOK = "<font color=\"#000000\" face=\"courier\" size=\"2\">" + limitsOK + "</font><br>";
            regularityOK = "<font color=\"#000000\" face=\"courier\" size=\"2\">" + regularityOK + "</font><br>";
        } else {
            regularityTitle = regularityTitle + "\n";
            limitTitle = "\n" + limitTitle + "\n";
            limitsOK = limitsOK + "\n";
            regularityOK = regularityOK + "\n";
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append(regularityTitle);
        if (!regSb.toString().isEmpty()) {
            resultBuilder.append(regSb.toString());
        } else {
            resultBuilder.append(regularityOK);
        }

        resultBuilder.append(limitTitle);
        if (!limitSb.toString().isEmpty()) {
            resultBuilder.append(limitSb.toString());
        } else {
            resultBuilder.append(limitsOK);
        }



        return resultBuilder.toString();
    }

    /**
     * /**
     * Check regularity of all sensors except:<br> <ul><li>manual
     * sensors</li><li>virtual sensors</li><li>event sensors</li></ul>
     *
     * @param day the day to be checked
     * @return the gathered text messages with the results
     */
    public String checkAllRegularity(Date day) {
        return checkAllRegularity(new DateInterval(day, day));
    }

    /**
     * Überprüft für den übergebenen Tag ob regelmässige Messpunkte ihr
     * Intervall einhalten
     *
     * @param day Zur überprüfender Tag
     * @param sensorID ID des zu prüfenden Messpunkts
     * @param limit Limit in Minuten das noch ohne Meldung hinnehmbar ist
     */
    private String[] checkIntervalRegularity(DateInterval dateInterval, int sensorID, int limit) { // TDO limit einbauen (aus Anwendungseinstellungen)
        Integer interval = SensorInformation.getSensorProperties(sensorID).getInterval() * 60 * 1000; // millisekunden
        String[] emptyResult = {"", ""};

        if (interval == null || interval == 0) { // keine Überprüfung möglich / erforderlich
            return emptyResult;
        }

        return testData(dateInterval, interval, sensorID);
    }

    /**
     * Überprüft bei Werteänderung auflaufende Messpunkte ob sie mindestens in
     * ihrem maximal zulässigen Intervall (MaxChangeTime) kommen
     *
     * @param day Zu überprüfender Tag
     * @param sensorID ID des zu prüfenden Messpunkts
     */
    private String[] checkChangeRegularity(DateInterval dateInterval, int sensorID) {
        String[] emptyResult = {"", ""};
        Long changeTime_WT = SensorInformation.getSensorProperties(sensorID).getMaxChangeTimes()[0];
        Long changeTime_WE = SensorInformation.getSensorProperties(sensorID).getMaxChangeTimes()[1];
        Long changeTime;


        // Wert von Wochentag oder Wochenende verwenden
        if (DateTimeCalculator.isWorkday(dateInterval.getStartDate())) { // TODO natürlich nicht nur den Starttag nehmen!!!!
            if (changeTime_WT == null) {
                return emptyResult;
            } else {
                changeTime = changeTime_WT * 60L * 1000L;// millisekunden
            }
        } else {
            if (changeTime_WE == null) {
                return emptyResult;
            } else {
                changeTime = changeTime_WE * 60L * 1000L;// millisekunden
            }
        }

        return testData(dateInterval, changeTime, sensorID);
    }

    /**
     * Prüft den übergebene Messpunkt am fraglichen Tag mit dem angegebenen
     * Zeitlimit (in Millisekunden)
     *
     * @param day
     * @param targetInterval
     * @param sensorID
     */
    protected String[] testData(DateInterval dateInterval, long targetInterval, int sensorID) {
        String messageRegularity = "";
        String messageLimits = "";
        String[] emptyResult = {"", ""};


        DatabaseQuery dq = new DatabaseQuery(sensorID);
        MeasurementTreeSet dataSet = dq.simpleQueryResult(new DateInterval(dateInterval.getStartDate(), dateInterval.getEndDate()), true);
        long diffMax = 0;
        long diff;
        boolean empty = true;
        boolean valid = true;
        Double value;
        Long warningLimit = calcWarningLimit();
        Long last = dateInterval.getStartDate().getTime(); // set last tie to start of interval
        Long time;

        LimitExceedance exceedance;
        if (exceedanceCollection.containsKey(sensorID)) {
            exceedance = exceedanceCollection.get(sensorID);
        } else {
            exceedance = new LimitExceedance();
        }

        for (Measurement measurement : dataSet) {
            if (wasCanceled()) {
                return emptyResult;
            }

            time = measurement.getTime();
            value = measurement.getValue();
            exceedance = isLimitExceeded(time, value, sensorID, exceedance);
            exceedanceCollection.put(sensorID, exceedance);

            if (SensorInformation.getSensorProperties(sensorID).isCounter() && (measurement.getValue() == 0)) { // Bei Zählern die 0-Werte ignorieren......
                continue;
            }
            empty = false; // we are in the loop - the dataset is not empty
            diff = (time - last); // in first loop run these are the same (=0)
            if (diff > targetInterval) {
                valid = false;
                diffMax = diff > diffMax ? diff : diffMax;
            }
            last = time;
        }
        messageLimits = buildLimitMessage(exceedance, sensorID);
        messageRegularity = buildRegularityMessage(empty, sensorID, dateInterval, messageRegularity, valid, diffMax, targetInterval, warningLimit);

        String[] result = {messageRegularity, messageLimits};
        return result;
    }

    private boolean wasCanceled() {
        if (worker != null) {
            return worker.isCancelled();
        }
        return false;
    }

    private void tellProgress(int i) {
        if (worker != null) {
            this.worker.publishValue(i);
        }
    }

    private String buildRegularityMessage(boolean empty, int sensorID, DateInterval dateInterval, String message, boolean valid, long diffMax, long targetInterval, Long warningLimit) {
        String s;
        if (empty) {
            s = "(" + SensorInformation.getDisplayName(sensorID) + ")\t" + "No data between " + " " + dateFormat.format(dateInterval.getStartDate()) + " - " + dateFormat.format(dateInterval.getEndDate());
            if (worker != null) {
                message = "<font color=\"#FF6666\" face=\"courier\" size=\"2\"><b>" + s + "</b><font><br>";
            } else {
                message = s + "\n";
            }
        } else if (!valid) {
            Long exceedance = (diffMax - targetInterval);
            if (exceedance > warningLimit) {
                s = "(" + SensorInformation.getDisplayName(sensorID) + ")\t" + "Interval" + " (" + (targetInterval / 60000f) + " " + "Min" + ") " + "at least once exceeded in" + " " + dateFormat.format(dateInterval.getStartDate()) + " - " + dateFormat.format(dateInterval.getEndDate()) + " (max." + decimalFormat.format(((diffMax) / 60000f)) + " " + "Min" + ")";
                if (worker != null) {
                    message = "<font color=\"#FF6666\" face=\"courier\" size=\"2\">" + s + "<font><br>";
                } else {
                    message = s + "\n";
                }
            }
        } else {
            if (worker != null) {
                s = "(" + SensorInformation.getDisplayName(sensorID) + ")\t OK";
                message = "<font color=\"#04B431\" face=\"courier\" size=\"2\">" + s + "<font><br>";
            }
        }
        return message;
    }

    private String buildLimitMessage(LimitExceedance exceedance, Integer sensorID) {
        String s = "";

        String lowerLimitString = exceedance.getLowerLimit() == null ? "-" : exceedance.getLowerLimit().toString();
        String upperLimitString = exceedance.getUpperLimit() == null ? "-" : exceedance.getUpperLimit().toString();

        if (exceedance.getNumberOfExceedancesUpper() > 0 || exceedance.getNumberOfExceedancesLower() > 0) {
            s = "(" + SensorInformation.getDisplayName(sensorID) + ")\t" + "Limit violation: Allowed [" + lowerLimitString + ":" + upperLimitString + "]";
        }

        if (exceedance.getNumberOfExceedancesUpper() > 0) {
            s += "\tMAX exceedance: " + exceedance.getNumberOfExceedancesUpper() + "x, maximum by " + exceedance.getMaxExceedanceUpper();
        }
        if (exceedance.getNumberOfExceedancesLower() > 0) {
            s += "\tMIN exceedance: " + exceedance.getNumberOfExceedancesLower() + "x, maximum by " + exceedance.getMaxExceedanceLower();
        }

        if (!s.isEmpty()) {
            if (worker != null) {
                s = "<font color=\"#FF6666\" face=\"courier\" size=\"2\">" + s + "<font><br>";
            } else {
                s += "\n";
            }
        }

        return s;
    }

    private LimitExceedance isLimitExceeded(Long time, Double value, int sensorID, LimitExceedance exceedance) {
        if (value == null) {
            return exceedance;
        }

        Integer[] limits = getLimits(time, sensorID);
        Integer upper = limits[1];
        Integer lower = limits[0];

        exceedance.setUpperLimit(upper);
        exceedance.setLowerLimit(lower);

        if (SensorInformation.getSensorProperties(sensorID).isCounter() || SensorInformation.getSensorProperties(sensorID).isUsage()) {
            // TODO calc its power? for what interval?
            return exceedance;
        }

        if (SensorInformation.getSensorProperties(sensorID).isManual()) {
            return exceedance;
        }

        if (upper != null && value > upper) {
            if (value - upper > exceedance.getMaxExceedanceUpper()) {
                exceedance.setMaxExceedanceUpper(value - upper);
            }
            exceedance.setNumberOfExceedancesUpper(exceedance.getNumberOfExceedancesUpper() + 1);
        }

        if (lower != null && value < lower) {
            if (lower - value > exceedance.getMaxExceedanceLower()) {
                exceedance.setMaxExceedanceLower(lower - value);
            }
            exceedance.setNumberOfExceedancesLower(exceedance.getNumberOfExceedancesLower() + 1);
        }

        return exceedance;
    }

    private Long calcWarningLimit() {
        Long warningLimit;
        try {
            warningLimit = Long.valueOf(MoniSoft.getInstance().getApplicationProperties().getProperty("IntervalWarningTolerance"));
            warningLimit = warningLimit * 60000; // in ms
        } catch (NumberFormatException e) {
            Messages.showException(e);
            warningLimit = 0L;
        }
        return warningLimit;
    }

    private Integer[] getLimits(Long time, int sensorID) {
        Integer upper;
        Integer lower;
        if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
            lower = 0;
            upper = 1;
        } else {
            if (DateTimeCalculator.isWeekendLong(time)) {
                lower = SensorInformation.getSensorProperties(sensorID).getWELimits()[0];
                upper = SensorInformation.getSensorProperties(sensorID).getWELimits()[1];
            } else {
                lower = SensorInformation.getSensorProperties(sensorID).getWTLimits()[0];
                upper = SensorInformation.getSensorProperties(sensorID).getWTLimits()[1];
            }
        }
        Integer[] limits = {lower, upper};

        return limits;
    }
}