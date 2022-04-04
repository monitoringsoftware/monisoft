/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JProgressBar;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.TableXYDataset;

/**
 * Generiert ein TimeTableXYDataset Verwendet von TimeSeriesPlotMaker für
 * StackedBarCharts
 *
 * @author togro
 */
public class GenerateTimeTableXYDataset extends GeneralDataSetGenerator {

    private TimeTableXYDataset[] timeTableXYDatasetCollection = {new TimeTableXYDataset(), new TimeTableXYDataset()}; // hat genau zwei Elemete haben: für jede Achse ein TimeTableXYDataset
    private ArrayList markerList = new ArrayList(20);
    private ArrayList filterMarkerList = new ArrayList(20);
    private JProgressBar progressBar;
    private final StoppableThread t = (StoppableThread) Thread.currentThread();
    private final TimeSeriesChartDescriber describer;
    private String remark = "";
    private final DatasetWorkerFactory workerFactory;

    /**
     *
     * @param seriesCollection
     * @param start
     * @param end
     */
    public GenerateTimeTableXYDataset(ArrayList seriesCollection, DateInterval dateInterval, JProgressBar progress, TimeSeriesChartDescriber desc, DatasetWorkerFactory workerFactory) {
    	this.workerFactory = workerFactory;
        progressBar = progress;
        describer = desc;
        int counter = 0;
        for (Iterator it = seriesCollection.iterator(); it.hasNext();) {
            TimeSeriesLooks currentLooks = (TimeSeriesLooks) it.next();
            if (currentLooks != null) {
                counter++;
            }
        }
        progressBar = progress;
        if (progressBar != null) {
            progressBar.setStringPainted(true);
            progressBar.setMinimum(0);
            progressBar.setMaximum(counter);
        }

        int index = 0;
        for (Iterator it = seriesCollection.iterator(); it.hasNext();) {
            if (!t.running) {
                break;
            }
            TimeSeriesLooks currentLooks = (TimeSeriesLooks) it.next();
            if (currentLooks != null) {
                progressBar.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_AUS_DATENBANK") + SensorInformation.getDisplayName(currentLooks.getSensorID()));
//                Messages.showMessage("Lese aus Datenbank: " + currentLooks.getSensor() + "\n");
                generateTimeTable(currentLooks, dateInterval);
                index++;
                if (progressBar != null) {
                    progressBar.setValue(index);
                }
            }
        }
    }

    private void generateTimeTable(TimeSeriesLooks seriesLooks, DateInterval dateInterval) {

        // Belegen der Darstellungsoiptionen
        int sensorID = seriesLooks.getSensorID();
        String seriesName;

        if (seriesLooks.getLegendString() == null) {
            seriesName = SensorInformation.getDisplayName(sensorID);
        } else {
            seriesName = seriesLooks.getLegendString();
        }

        DatasetWorker dw = this.workerFactory.createFor(sensorID);
        dw.setVerbose(true);

        dw.setReference(seriesLooks.getReference());
        dw.setTimeReference(seriesLooks.getTimeReference());

        dw.setValueFilter(describer.getValueFilter());

        CounterMode counterMode = CounterMode.getInterpolationMode(seriesLooks.getPowerWanted(), seriesLooks.getCounterWanted(), 
                SensorInformation.getSensorProperties(sensorID).isCounter(), 
                SensorInformation.getSensorProperties(sensorID).isUsage(),MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), 
                MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1"));        
                
        MeasurementTreeSet map = dw.getInterpolatedData(dateInterval, 
                getPeriodForTimeStamp(seriesLooks.getAggregation(), dateInterval.getStartDate().getTime()), counterMode );
        
//        Iterator<Long> it = map.keySet().iterator();
        markerList = dw.getMarkers();        
        filterMarkerList = dw.getFilterMarkers();
        remark += dw.getRemark();

        for (Measurement measurement : map) {
            try {
                timeTableXYDatasetCollection[0].add(getPeriodForTimeStamp(seriesLooks.getAggregation(), measurement.getTime()), measurement.getValue(), seriesName, false); // 0 bedeutet Achse links
            } catch (SeriesException e) { // Abfangen der Exception bei doppelten Daten
                Messages.showException(e);
            }
        }
    }

    /**
     *
     *
     * @return
     */
    public TableXYDataset[] getDataSet() {
        return timeTableXYDatasetCollection;
    }

    /**
     *
     * @return
     */
    public ArrayList getMissingmarker() {
        return markerList;
    }

    /**
     *
     * @return
     */
    public ArrayList getFilterMarker() {
        return filterMarkerList;
    }

    /**
     *
     * @return
     */
    public String getRemark() {
        return remark;
    }
}
