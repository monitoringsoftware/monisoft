package de.jmonitoring.standardPlots.timeSeries;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.DataHandling.IntervalMeasurement;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JProgressBar;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author togro
 */
public class GenerateTimeSeriesDataSet extends GeneralDataSetGenerator {

    private TimeSeriesCollection[] DatasetCollection = new TimeSeriesCollection[20];
    private ArrayList missingMarkerList = new ArrayList(20);
    private ArrayList filterMarkerList = new ArrayList(20);
    private JProgressBar progressBar;
    private final StoppableThread stoppableThread = (StoppableThread) Thread.currentThread();
    private String remark = "";
    private TreeMap<Long, IntervalMeasurement> minMaxMap;
    private TimeSeriesChartDescriber describer;
    private final DatasetWorkerFactory workerFactory;

    /**
     * Erzeugt aus der übergebenen Collection
     *
     * @param seriesCollection
     * @param start
     * @param end
     * @param progess
     */
    public GenerateTimeSeriesDataSet(ArrayList seriesCollection, DateInterval dateInterval, JProgressBar progress, TimeSeriesChartDescriber desc, DatasetWorkerFactory workerFactory) {
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
        for (Iterator<TimeSeriesLooks> it = seriesCollection.iterator(); it.hasNext();) {
            if (!stoppableThread.running) {
                System.out.println("break");
                break;
            }
            TimeSeriesLooks currentLooks = it.next();
            if (currentLooks != null) {
                //Messages.showMessage("Lese aus Datenbank: " + SensorInformation.getDisplayName(currentLooks.getSensor()) + "\n");
                progressBar.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_AUS_DATENBANK") + SensorInformation.getDisplayName(currentLooks.getSensorID()));
                TimeSeriesCollection tc = new TimeSeriesCollection(generateTimeSeries(dateInterval, currentLooks));
//                tc.addSeries(getMinSeries(currentLooks));
//                tc.addSeries(getMaxSeries(currentLooks));
                DatasetCollection[index] = tc;
//                DatasetCollection[index] = new TimeSeriesCollection(generateTimeSeries(dateInterval, currentLooks));
                DatasetCollection[index].setXPosition(TimePeriodAnchor.MIDDLE);
                index++;
                if (progressBar != null) {
                    progressBar.setValue(index);
                }
            }
        }
    }

    /**
     *
     * @param dateInterval
     * @param seriesLooks
     * @return
     */
    private TimeSeries generateTimeSeries(DateInterval dateInterval, TimeSeriesLooks seriesLooks) {
        int sensorID = seriesLooks.getSensorID();
        String suffix = getSuffix(seriesLooks.getAggregation(), seriesLooks.getPowerWanted(), seriesLooks.getCounterWanted(), SensorInformation.getSensorProperties(sensorID).isCounter(), SensorInformation.getSensorProperties(sensorID).isUsage()); // Name für die Serie bauen

        TimeSeries timeSeries;
        DatasetWorker dw = this.workerFactory.createFor(sensorID);
        dw.setVerbose(true);

        if (seriesLooks.getLegendString() == null) {
            timeSeries = new TimeSeries(SensorInformation.getDisplayName(sensorID) + " " + suffix);
        } else {
            timeSeries = new TimeSeries(seriesLooks.getLegendString());
        }

        dw.setReference(seriesLooks.getReference());
        dw.setTimeReference(seriesLooks.getTimeReference());

        dw.setValueFilter(describer.getValueFilter());

        boolean foreach = false;
        
        if (SensorInformation.getSensorProperties(sensorID).isVirtual() )
        {
            String virtualDefinition = SensorInformation.getSensorProperties(sensorID).getVirtualDefinition();
            if( virtualDefinition != null && virtualDefinition.contains( "FOREACH" ) )
            {
                foreach = true;                
            }
        }
        
        CounterMode counterMode = CounterMode.getInterpolationMode(seriesLooks.getPowerWanted(), seriesLooks.getCounterWanted(), SensorInformation.getSensorProperties(sensorID).isCounter(), SensorInformation.getSensorProperties(sensorID).isUsage(),MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1"));

        MeasurementTreeSet map = dw.getInterpolatedData(dateInterval, getPeriodForTimeStamp(seriesLooks.getAggregation(), dateInterval.getStartDate().getTime()), counterMode);
//        minMaxMap = dw.getMinMaxData();
        if (map == null) {
            return null;
        }
        missingMarkerList = dw.getMarkers();
        
        // TODO: Prüfung, ob es ein virtueller Zähler
        
        for (int i = 0; i < missingMarkerList.size(); i++) {
            IntervalMarker marker = (IntervalMarker) missingMarkerList.get(i);
            
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
            String startValueString = simpleDateFormat.format( marker.getStartValue() );
            String endValueString = simpleDateFormat.format( marker.getEndValue() );
            
            // System.out.println( "Marker: StartValue: " + startValueString + " EndValue: " + endValueString + " Label: " + marker.getLabel() );
            // TODO: Finde, ob es zu einem Zeitpunkt doch einen Wert gibt            
        }

        
        filterMarkerList = dw.getFilterMarkers();

        // System.out.println( "map-size: " + map.size() );
        
//        Iterator it = map.entrySet().iterator();
        for (Measurement measurement : map) {
//            Map.Entry<Long, Double> entry = (Map.Entry<Long, Double>) it.next();

            try {
//                System.out.println("Time: " + measurement.getTime() +  " " + new Date(measurement.getTime()));
                RegularTimePeriod regularTimePeriod = getPeriodForTimeStamp(seriesLooks.getAggregation(), measurement.getTime());
                
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
                
                // System.out.println( "measurement.getTime(): " + measurement.getTime() + " " + simpleDateFormat.format( measurement.getTime() ) );
                
                
                // System.out.println( "regularTimePeriod: " + regularTimePeriod );
                
                timeSeries.add(regularTimePeriod, measurement.getValue(), false); // Kein notify -> schneller
            } catch (SeriesException e) { // Abfangen wenn Zeitpunkt schon vorhanden
                Messages.showException(e);
            }
        }
//        Iterator<Long> it = map.keySet().iterator();
//        while (it.hasNext()) {
//            time = it.next();
//            try {
//                timeSeries.add(getPeriodForTimeStamp(seriesLooks.getAggregation(), time), map.get(time), false); // Kein notify -> schneller
//            } catch (SeriesException e) { // Abfangen wenn Zeitpunkt schon vorhanden
//                Messages.showException(e);
//            }
//        }

        if( !remark.contains( dw.getRemark() ) )
        {
            // AZ: die gleiche Markierung soll nicht noch einmal rein - MONISOFT-24
            remark += dw.getRemark();
        }       
        
        return timeSeries;
    }

    private TimeSeries getMinSeries(SeriesLooks seriesLooks) {
        TimeSeries ts = new TimeSeries("Min");
        for (Long l : minMaxMap.keySet()) {
            ts.add(getPeriodForTimeStamp(seriesLooks.getAggregation(), l), minMaxMap.get(l).getMin());
        }
        return ts;
    }

    private TimeSeries getMaxSeries(SeriesLooks seriesLooks) {
        TimeSeries ts = new TimeSeries("Max");
        for (Long l : minMaxMap.keySet()) {
            ts.add(getPeriodForTimeStamp(seriesLooks.getAggregation(), l), minMaxMap.get(l).getMax());
        }
        return ts;
    }

    /**
     *
     * @return
     */
    public TimeSeriesCollection[] getDataSet() {
        return DatasetCollection;
    }

    /**
     *
     * @return
     */
    public ArrayList getMissingmarker() {
        return missingMarkerList;
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
