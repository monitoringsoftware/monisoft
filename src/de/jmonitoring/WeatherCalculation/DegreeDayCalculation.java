/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.Messages;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author togro
 */
public class DegreeDayCalculation implements Runnable {

    private Double heatingBaseTemperature; // Heizgrenztemperatur
    private Double coolingBaseTemperature; // Kühlgrenztemperatur
    private Double insideBaseTemperature;  // Innenraumtemperatur
    private final SensorProperties outSideTempSensor;
    private StoppableThread stoppThread; // = new StoppableThread(this);
    private MeasurementTreeSet dataSet;
    private DateInterval dateInterval;
    private Long missingDays = 0L; // Zahl der fehlenden Tage
    private Integer missingDayTolerance = 15; // Anzahle der Tage die maximal fehlen dürfen um sie durch Schätzung zu ersetzen
    private final DatasetWorkerFactory datasetWorkerFactory;

    public DegreeDayCalculation(Double heatingbaseTemperature, Double coolingBaseTemperature, Double insideBaseTemperature, DatasetWorkerFactory datasetWorkerFactory, Integer buildingID) {
        this.datasetWorkerFactory = datasetWorkerFactory;
        this.heatingBaseTemperature = heatingbaseTemperature != null ? heatingbaseTemperature : 15d;
        this.coolingBaseTemperature = coolingBaseTemperature != null ? coolingBaseTemperature : 20d; // TODO welche Vorgabe?
        this.insideBaseTemperature = insideBaseTemperature != null ? insideBaseTemperature : 20d;
        outSideTempSensor = WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_OUTSIDE_TEMPERATURE, buildingID);
    }

    public DegreeDay getHGTFromRawData(Integer year, Integer month) {
        if (outSideTempSensor == null) {
            return null;
        }

        getData(year, month);
        DegreeDay degreeDay = new DegreeDay(year, month, "", calculate(heatingBaseTemperature), false);

        return degreeDay;
    }

    public DegreeDay getGTZFromRawData(Integer year, Integer month) {
        if (outSideTempSensor == null) {
            return null;
        }

        getData(year, month);
        DegreeDay degreeDay = new DegreeDay(year, month, "", calculate(insideBaseTemperature), true);

        return degreeDay;
    }

    private DateInterval makeDateInterval(Integer year, Integer month) {
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        DateInterval interval = new DateInterval(new GregorianCalendar(year, month, 1).getTime(), new GregorianCalendar(year, month, lastDay).getTime());
        return interval;
    }

    private Double calculate(Double baseTemperature) {
        Double degreeDays = 0d;
        Double dailymeanTemp;
        Double monthlyMeanTemp = 0d;
        missingDays = 0L;
        for (Measurement measurement : dataSet) {
            if (measurement != null && measurement.getValue() != null) {
                dailymeanTemp = measurement.getValue();
            } else {
                dailymeanTemp = Double.MAX_VALUE;
            }

            if (dailymeanTemp < heatingBaseTemperature) { // Tagesmittel kleiner als Heizgrenze -> Heiztag
                degreeDays += (baseTemperature - dailymeanTemp);
                monthlyMeanTemp += dailymeanTemp;
            } else if (dailymeanTemp.equals(Double.MAX_VALUE))  {
                missingDays++;
            } else {
                monthlyMeanTemp += dailymeanTemp;
            }
        }

        if (missingDays > 0 && missingDays <= missingDayTolerance) { // Wenn fehlende Tage tolerabel diese durch Schätzungen aus der Monatsmitteltemp der übrigen Tage ersetzen
            monthlyMeanTemp = monthlyMeanTemp / (dataSet.size() - missingDays);
            if (monthlyMeanTemp < heatingBaseTemperature) {
//                System.out.println("Unkorrigiert: " + degreeDays + " (" + monthlyMeanTemp + " " + dataSet.size() + " " + missingDays + ")");
                degreeDays += (baseTemperature - monthlyMeanTemp) * missingDays;
//                System.out.println("Korrigiert: " + degreeDays);
            }
        } else if (missingDays > 0) {
            degreeDays = null;
        }

        return degreeDays;
    }

    public Long getMissingDays() {
        return missingDays;
    }

    public Double getHeatingBaseTemperature() {
        return heatingBaseTemperature;
    }

    public void setHeatingBaseTemperature(Double baseTemperature) {
        this.heatingBaseTemperature = baseTemperature;
    }

    public Double getCoolingBaseTemperature() {
        return coolingBaseTemperature;
    }

    public void setCoolingBaseTemperature(Double coolingBaseTemperature) {
        this.coolingBaseTemperature = coolingBaseTemperature;
    }

    public Double getInsideTemperature() {
        return insideBaseTemperature;
    }

    public void setInsideTemperature(Double insideTemperature) {
        this.insideBaseTemperature = insideTemperature;
    }

    private void getData(Integer year, Integer month) {
        this.dateInterval = makeDateInterval(year, month);
        stoppThread = new StoppableThread(this);
        try {
            stoppThread.start();
            stoppThread.join(10000);
        } catch (InterruptedException ex) {
            Messages.showException(ex);
        }
    }

    @Override
    public void run() {
        stoppThread.running = true;
        DatasetWorker dw = this.datasetWorkerFactory.createFor(outSideTempSensor.getSensorID());
        dataSet = (MeasurementTreeSet) dw.getInterpolatedData(dateInterval, GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.DAY_INTERVAL, dateInterval.getStartDate().getTime()), CounterMode.NOCOUNTER);
        dw.getMissingTime();
    }
}
