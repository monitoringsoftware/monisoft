package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.DataPointObject;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Generate a datset for scatter plots
 *
 * @author togro
 */
public class GenerateScatterDataSet extends GeneralDataSetGenerator {

    private XYSeriesCollection[] DatasetCollection = new XYSeriesCollection[25];
    private final StoppableThread thisThread = (StoppableThread) Thread.currentThread();
    public double rangeMin = Double.MAX_VALUE;
    public double rangeMax = -Double.MAX_VALUE;
    private final ScatterChartDescriber desc;
    private String remark = "";
    private HashMap<DataPointObject, String> labelMap = new HashMap<DataPointObject, String>();
    private final DatasetWorkerFactory workerFactory;

    /**
     * Create a new generator using the given parameters
     *
     * @param describer The describer to use
     * @param workerFactory The factory that gives us the {@link DatasetWorker}
     */
    public GenerateScatterDataSet(ScatterChartDescriber describer, DatasetWorkerFactory workerFactory) {
//        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_AUS_DATENBANK") + SensorInformation.getDisplayName(describer.getDomainSensorProps().getSensorID()) + "\n");
        desc = describer;
        this.workerFactory = workerFactory;
        DateInterval dateInterval = desc.getDateInterval();
        MeasurementTreeSet domainMap = generateDomainMap(desc);

        if (!thisThread.running) {
            return;
        }

        int index = 0;
        for (Iterator it = desc.getchartCollection().iterator(); it.hasNext();) {
            if (!thisThread.running) {
                break;
            }
            ScatterSeriesLooks currentLooks = (ScatterSeriesLooks) it.next();
            if (currentLooks != null) {
//                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_AUS_DATENBANK") + SensorInformation.getDisplayName(currentLooks.getSensorID()) + "\n");
                DatasetCollection[index] = new XYSeriesCollection(generateXYSeries(currentLooks, domainMap, dateInterval, getPeriodForTimeStamp(desc.getAggregation(), dateInterval.getStartDate().getTime())));
                index++;
            }
        }
    }

    /**
     * Generate the data for the domain axis
     *
     * @param describer
     * @return
     */
    private MeasurementTreeSet generateDomainMap(ScatterChartDescriber describer) {
        DatasetWorker dw = this.workerFactory.createFor(describer.getDomainSensorProps().getSensorID());
        dw.setVerbose(true);
        MeasurementTreeSet map = (MeasurementTreeSet) dw.getInterpolatedData(describer.getDateInterval(), getPeriodForTimeStamp(describer.getAggregation(), describer.getDateInterval().getStartDate().getTime()), CounterMode.getInterpolationMode(describer.getUsePower(), false, SensorInformation.getSensorProperties(describer.getDomainSensorProps().getSensorID()).isCounter(), SensorInformation.getSensorProperties(describer.getDomainSensorProps().getSensorID()).isUsage(), MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));
        return map;
    }

    /**
     * Generate the final datset for the scatter plot
     *
     * @param seriesLook The loos of the series
     * @param domainMap The data for the domain axis
     * @param dateInterval The date interval to in question
     * @param regPeriod The interpolation interval
     * @return The dataset
     */
    private XYSeries generateXYSeries(ScatterSeriesLooks seriesLook, MeasurementTreeSet domainMap, DateInterval dateInterval, RegularTimePeriod regPeriod) {
        String seriesName;

        if (seriesLook.getLegendString() == null) {
            seriesName = SensorInformation.getDisplayName(seriesLook.getSensorID()) + " " + getSuffix(desc.getAggregation(), seriesLook.getPowerWanted(), false, false, SensorInformation.getSensorProperties(seriesLook.getSensorID()).isUsage());
        } else {
            seriesName = seriesLook.getLegendString();
        }

        XYSeries series = new XYSeries(seriesName);

//        XYSeries series = new XYSeries(SensorInformation.getDisplayName(seriesLook.getSensorID()) + getSuffix(desc.getAggregation(), seriesLook.getPowerWanted(), false, false));
        int invalidValueCount = 0;

        DatasetWorker dw = this.workerFactory.createFor(seriesLook.getSensorID());
        dw.setVerbose(true);
        // Tageszeitbeschränkung?
        if (seriesLook.getUseWeekDayConstraints()) {
            dw.setDayFilterCode(seriesLook.getWeekDayConstraintCode());
        }
        // Wenn Uhrzeitbeschränkung gewünscht die Werte dazu holen (Index der Combobox entspricht der Uhrzeit)
        if (seriesLook.getUseTimeConstraints()) {
            dw.setTimeFilter(seriesLook.getStartTimeConstraint(), seriesLook.getEndTimeConstraint());
        }
        if (seriesLook.isUseDateConstraints()) {
            dw.setDateConstraints(seriesLook.getStartDateConstraints(), seriesLook.getEndDateConstraints());
        }

        dw.setValueFilter(seriesLook.getValueFilter());
        MeasurementTreeSet rangeMap = dw.getInterpolatedData(dateInterval, regPeriod, CounterMode.getInterpolationMode(seriesLook.getPowerWanted(), false, SensorInformation.getSensorProperties(seriesLook.getSensorID()).isCounter(), SensorInformation.getSensorProperties(seriesLook.getSensorID()).isUsage(), MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));

        rangeMin = (dw.getMinValue() != null && dw.getMinValue() < rangeMin) ? dw.getMinValue() : rangeMin;
        rangeMax = (dw.getMaxValue() != null && dw.getMaxValue() > rangeMax) ? dw.getMaxValue() : rangeMax;

        // Zeitpunkte (keys) des DomainHashs durchlaufen und endgültige XYSeries zusammenbauen
        for (Measurement measurement : domainMap) {
            if ((measurement.getValue() != null) && (rangeMap.getValueForTime(measurement.getTime()) != null)) {
                series.add((measurement.getValue()), rangeMap.getValueForTime(measurement.getTime()), false);
                labelMap.put(new DataPointObject(measurement.getValue(), rangeMap.getValueForTime(measurement.getTime())), new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(new Date(measurement.getTime())));
            } else {
                invalidValueCount++; //Anzahl der ungüligen Werte
            }
        }

        remark += dw.getRemark(); // + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource_de_DE").getString("INSGESAMT") + " " + invalidValueCount + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource_de_DE").getString("LEERINTERVALL") + " " + domainMap.size() + ")";
        return series;
    }

    /**
     * Get the dataset
     *
     * @return the dataset
     */
    public XYSeriesCollection[] getDataSet() {
        return DatasetCollection;
    }

    /**
     * The raneg of values
     *
     * @return A 2-dim array of min and max
     */
    public double[] getValueRange() {
        double[] range = {rangeMin, rangeMax};
        return range;
    }

    public String getRemark() {
        return remark;
    }

    public HashMap<DataPointObject, String> getLabelMap() {
        return labelMap;
    }
}
