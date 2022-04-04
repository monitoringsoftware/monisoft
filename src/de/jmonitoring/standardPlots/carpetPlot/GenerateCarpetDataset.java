/*
 * GenerateXYZDataset.java
 *
 * Created on 20. August 2007, 11:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.carpetPlot;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.standardPlots.carpetPlot.CarpetSeriesLooks;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.intervals.DateInterval;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.jfree.data.time.Day;
import org.jfree.data.xy.DefaultXYZDataset;

/**
 * Generiert ein DefaultXYZDataset, benutzt von Carpetplots
 *
 * @author togro
 */
public class GenerateCarpetDataset extends GeneralDataSetGenerator {

    private DefaultXYZDataset dataset = new DefaultXYZDataset();
    private final static double RANGE_BOUNDARY = 999999999f;
    private double maximum = RANGE_BOUNDARY;
    private double minimum = -RANGE_BOUNDARY;

    /**
     *
     * @param seriesLooks
     * @param dateInterval
     * @param aggInterval
     */
    public GenerateCarpetDataset(CarpetSeriesLooks seriesLooks, DateInterval dateInterval, DatasetWorker dw) {
        dw.setVerbose(true);
        dw.setReference(seriesLooks.getReference());
        dw.setTimeReference(seriesLooks.getTimeReference());
        MeasurementTreeSet map = (MeasurementTreeSet) dw.getInterpolatedData(dateInterval, getPeriodForTimeStamp(seriesLooks.getAggregation(), dateInterval.getStartDate().getTime()), CounterMode.getInterpolationMode(seriesLooks.getPowerWanted(), false, SensorInformation.getSensorProperties(seriesLooks.getSensorID()).isCounter(), SensorInformation.getSensorProperties(seriesLooks.getSensorID()).isUsage(),MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));
        maximum = dw.getMaxValue();
        minimum = dw.getMinValue();
        Day dayPeriod;
        Calendar cal = new GregorianCalendar();
        int size = map.size();
        double[] xValues = new double[size];
        double[] yValues = new double[size];
        double[] zValues = new double[size];
        int i = 0;
        for (Measurement measurement : map) {
            cal.setTimeInMillis(measurement.getTime());                   // Kalenderobjekt erzeugen (zum Rechnen)
            dayPeriod = new Day(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
            xValues[i] = dayPeriod.getFirstMillisecond();
            yValues[i] = cal.get(Calendar.MINUTE) / 60. + cal.get(Calendar.HOUR_OF_DAY) / 1.;
            if (measurement.getValue() != null) {
                zValues[i] = (Double) measurement.getValue();
            } else {
                zValues[i] = -999999999f;
                // TODO: Fehlerwert setzen ????
            }
            i++;
        }
        dataset.addSeries(SensorInformation.getDisplayName(seriesLooks.getSensorID()), new double[][]{xValues, yValues, zValues});
    }

    public DefaultXYZDataset getDataSet() {
        return dataset;
    }

    public double getMaxValue() {
        return maximum;
    }

    public double getMinValue() {
        return minimum;
    }
}
