package de.jmonitoring.standardPlots.comparePlot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.intervals.MonthInterval;
import de.jmonitoring.utils.intervals.WeekInterval;
import de.jmonitoring.utils.intervals.YearInterval;

/**
 *
 * @author togro
 */
public class GenerateCompareDataSet {

    private DefaultCategoryDataset dset = new DefaultCategoryDataset();
    private final DatasetWorkerFactory workerFactory;

    public GenerateCompareDataSet(List periods, CompareSeriesLooks compareSeries, int categoryInterval, boolean showAsPower, DatasetWorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
        GregorianCalendar tmpDate = new GregorianCalendar();
        tmpDate.clear();
        // alle geforderten periods durchlaufen
        for (int i = 0; i < periods.size(); i++) {
            MeasurementTreeSet dataMap = getDataForPeriod(periods.get(i), compareSeries.getSensorID(), categoryInterval, showAsPower);
            for (Measurement measurement : dataMap) {
                tmpDate.setTimeInMillis(measurement.getTime());
                String category = "";
                // der entsprechenden category zuordnen
                switch (categoryInterval) {
                    case MoniSoftConstants.HOUR_CATEGORY:
                        if (periods.get(i) instanceof DateInterval) {
                            category = String.valueOf(tmpDate.get(Calendar.HOUR_OF_DAY)) + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("_UHR");
                        } else if (periods.get(i) instanceof WeekInterval) {
                            int h = (tmpDate.get(Calendar.DAY_OF_WEEK) - 1) * 24 + tmpDate.get(Calendar.HOUR_OF_DAY);
                            category = String.valueOf(h);
                        } else if (periods.get(i) instanceof MonthInterval) {
                            int h = (tmpDate.get(Calendar.DAY_OF_MONTH) - 1) * 24 + tmpDate.get(Calendar.HOUR_OF_DAY);
                            category = String.valueOf(h);
                        } else if (periods.get(i) instanceof YearInterval) {
                            int h = (tmpDate.get(Calendar.DAY_OF_YEAR) - 1) * 24 + tmpDate.get(Calendar.HOUR_OF_DAY);
                            category = String.valueOf(h);
                        }

                        break;
                    case MoniSoftConstants.DAY_CATEGORY:
                        if (periods.get(i) instanceof WeekInterval) {
                            category = MoniSoftConstants.getDayNameFor(tmpDate.get(Calendar.DAY_OF_WEEK) - 1);
                        } else if (periods.get(i) instanceof MonthInterval) {
                            category = String.valueOf(tmpDate.get(Calendar.DAY_OF_MONTH));
                        } else if (periods.get(i) instanceof YearInterval) {
                            category = String.valueOf(tmpDate.get(Calendar.DAY_OF_YEAR));
                        }

                        break;
                    case MoniSoftConstants.WEEK_CATEGORY:
                        category = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("KW_") + String.valueOf(tmpDate.get(Calendar.WEEK_OF_YEAR));
                        break;
                    case MoniSoftConstants.MONTH_CATEGORY:
                        category = MoniSoftConstants.getMonthFor(tmpDate.get(Calendar.MONTH));
                        break;
                    case MoniSoftConstants.YEAR_CATEGORY:
                        category = String.valueOf(tmpDate.get(Calendar.YEAR));
                        break;
                }
                // der entsprechenden Series zuordnen
                String series = nameSeries(tmpDate, periods.get(i).getClass());
                dset.addValue(measurement.getValue(), series, category);
            }
        }
    }

    /**
     * Liefert einen interpolierten Datensatz für einen Sensor über ein
     * Zeitintervall. Das Interpolationintervall orientiert sich an der
     * übergebenen category
     *
     * @param timePeriod
     * @param sensor
     * @param categoryInterval
     * @param showAsPower
     * @return
     */
    private MeasurementTreeSet getDataForPeriod(Object timePeriod, int sensorID, int categoryInt, boolean showAsPower) {
        SimpleDateFormat MySQLDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
        SimpleDateFormat MySQLDateFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);
        DateInterval dateInterval = new DateInterval();
        Date d = null;

        // AbfrageStrings für Start und Enddatum ermitteln
        if (timePeriod instanceof DateInterval) {
            DateInterval interval = (DateInterval) timePeriod;
            dateInterval.setStartDate(interval.getStartDate());
            d = null;
            try {
                d = MySQLDateTimeFormat.parse(interval.getEndDateString(MySQLDateFormat) + " 23:59:59");
            } catch (ParseException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }
            dateInterval.setEndDate(d);
        } else if (timePeriod instanceof WeekInterval) {
            WeekInterval interval = (WeekInterval) timePeriod;
            dateInterval.setStartDate(interval.getStartDate());
            try {
                d = MySQLDateTimeFormat.parse(interval.getEndDateString(MySQLDateFormat) + " 23:59:59");
            } catch (ParseException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }
            dateInterval.setEndDate(d);
        } else if (timePeriod instanceof MonthInterval) {
            MonthInterval interval = (MonthInterval) timePeriod;
            dateInterval.setStartDate(interval.getStartDate());
            try {
                d = MySQLDateTimeFormat.parse(interval.getEndDateString(MySQLDateFormat) + " 23:59:59");
            } catch (ParseException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }
            dateInterval.setEndDate(d);
        } else if (timePeriod instanceof YearInterval) {
            YearInterval interval = (YearInterval) timePeriod;
            GregorianCalendar SCal = new GregorianCalendar(interval.getstartYear(), 0, 1);
            GregorianCalendar eCal = new GregorianCalendar(interval.getEndYear(), 11, 31);
            dateInterval.setStartDate(SCal.getTime());
            dateInterval.setEndDate(eCal.getTime());
        } else {
        }

        // Interpolationszeitraum betimmen
        RegularTimePeriod categoryInterval;
        switch (categoryInt) {
            case MoniSoftConstants.HOUR_CATEGORY:
                categoryInterval = new Hour(dateInterval.getStartDate());
                break;
            case MoniSoftConstants.DAY_CATEGORY:
                categoryInterval = new Day(dateInterval.getStartDate());
                break;
            case MoniSoftConstants.WEEK_CATEGORY:
                categoryInterval = new Week(dateInterval.getStartDate());
                break;
            case MoniSoftConstants.MONTH_CATEGORY:
                categoryInterval = new Month(dateInterval.getStartDate());
                break;
            case MoniSoftConstants.YEAR_CATEGORY:
                categoryInterval = new Year(dateInterval.getStartDate());
                break;
            default:
                categoryInterval = new Day(dateInterval.getStartDate());
        }

        // Art des Sensors holen
        boolean isCounter = SensorInformation.getSensorProperties(sensorID).isCounter();

        DatasetWorker dw = this.workerFactory.createFor(sensorID);
        dw.setReference(null);
        dw.setVerbose(true);
        MeasurementTreeSet map = (MeasurementTreeSet) dw.getInterpolatedData(dateInterval, categoryInterval, CounterMode.getInterpolationMode(showAsPower, false, isCounter, SensorInformation.getSensorProperties(sensorID).isUsage(), MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));
        return map;
    }

    /**
     * Erzeugt den Namen der Series
     *
     * @param tmpDate
     * @return
     */
    private String nameSeries(GregorianCalendar tmpDate, Class seriesClass) {
        String series = "";
        if (seriesClass.equals(DateInterval.class)) {
            series = MoniSoftConstants.getDayNameFor(tmpDate.get(Calendar.DAY_OF_WEEK) - 1);
            series += " " + tmpDate.get(Calendar.DAY_OF_MONTH) + "." + (tmpDate.get(Calendar.MONTH) + 1) + "." + tmpDate.get(Calendar.YEAR);
        } else if (seriesClass.equals(WeekInterval.class)) {
            series = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("KW") + String.valueOf(tmpDate.get(Calendar.WEEK_OF_YEAR)) + "/" + String.valueOf(tmpDate.get(Calendar.YEAR));
        } else if (seriesClass.equals(MonthInterval.class)) {
            series = String.valueOf(tmpDate.get(Calendar.MONTH) + 1) + "/" + String.valueOf(tmpDate.get(Calendar.YEAR));
        } else if (seriesClass.equals(YearInterval.class)) {
            series = String.valueOf(tmpDate.get(Calendar.YEAR));
        }
        return series;
    }

    public DefaultCategoryDataset getDataset() {
        return dset;
    }
}
