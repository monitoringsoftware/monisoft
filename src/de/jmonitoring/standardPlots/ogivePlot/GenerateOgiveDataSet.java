package de.jmonitoring.standardPlots.ogivePlot;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.stat.Frequency;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.Hour;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * Generate a datset for ogive charts
 *
 * @author togro
 */
public class GenerateOgiveDataSet extends GeneralDataSetGenerator {

    private XYSeriesCollection[] DatasetCollection = new XYSeriesCollection[20];
    private int missingHours = 0;
    private final DatasetWorkerFactory workerFactory;

    /**
     * Create a generator
     *
     * @param seriesCollection The looks to use
     * @param dateInterval The date interval to be shown
     * @param workerFactory The factory that gives us the {@link DatasetWorker}
     */
    public GenerateOgiveDataSet(List<OgiveSeriesLooks> seriesCollection, DateInterval dateInterval, DatasetWorkerFactory workerFactory) {
        this.workerFactory = workerFactory;
        int index = 0;
        for (Iterator<OgiveSeriesLooks> it = seriesCollection.iterator(); it.hasNext();) {

            OgiveSeriesLooks currentLooks = it.next();
            if (currentLooks != null) {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_AUS_DATENBANK") + SensorInformation.getDisplayName(currentLooks.getSensorID()) + "\n", true);
                DatasetCollection[index] = new XYSeriesCollection(generateOgiveSeries(currentLooks, dateInterval));
                index++;
            }
        }
    }

    /**
     * Generates and returns the dataset
     *
     * @param seriesLooks The serieslooks of the line
     * @param dateInterval The date interval to use
     * @return
     */
    private XYSeries generateOgiveSeries(OgiveSeriesLooks seriesLooks, DateInterval dateInterval) {
        XYSeries ogiveSeries = new XYSeries(SensorInformation.getDisplayName((seriesLooks.getSensorID())), true, false);
        DatasetWorker dw = this.workerFactory.createFor(seriesLooks.getSensorID());
        dw.setVerbose(true);
        MeasurementTreeSet map = dw.getInterpolatedData(dateInterval, new Hour(dateInterval.getStartDate()), CounterMode.getInterpolationMode(seriesLooks.getPowerWanted(), false, SensorInformation.getSensorProperties(seriesLooks.getSensorID()).isCounter(), SensorInformation.getSensorProperties(seriesLooks.getSensorID()).isUsage(), MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));
        double min = dw.getMinValue();
        double max = dw.getMaxValue();

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("BERECHNE_SUMMENHAEUFIGKEIT") + "\n", true);
        // kummuliere Summenhäufigkeit ermitteln
        Frequency f = new Frequency();
        for (Measurement measurement : map) {
            if (measurement != null && measurement.getValue() != null) {
                f.addValue(measurement.getValue());
            } else {
                missingHours++;
            }
        }
        double resolution = 100d; // TODO Anpassbar machen...
        double step = (max - min) / resolution;          // Wertebereich in <resolution> Teile aufteilen
        long frequency = 0L;
        Number num;
//        System.out.println("Min: " + min + " Max:" + max);
        for (double value = min; value < max; value += step) {
//            frequency = f.getCumFreq(Math.round(value * 1000000d));    // Anzahl der Werte KLEINER oder gleich value
            frequency = f.getCumFreq(value);
//            System.out.println("Value " + value + " freq: " +frequency);
            try {
                if (seriesLooks.getReverse()) {
                    num = (Number) (f.getSumFreq() - frequency);
                } else {
                    num = (Number) frequency;
                }


                if (seriesLooks.getflipAxis()) {
                    ogiveSeries.addOrUpdate(value, num); // Stunden auf y-Achse
                } else {
                    ogiveSeries.addOrUpdate(num, value); // Stunden auf x-Achse
                }
            } catch (SeriesException e) {
            }
        }
        return ogiveSeries;
    }

    /**
     * Return the datset
     *
     * @return The dataset
     */
    public XYSeriesCollection[] getDataSet() {
        return DatasetCollection;
    }

    /**
     * Return the number of hours with no data
     * @return The number of hours
     */
    public int getInvalidHours() {
        return missingHours;
    }
}
