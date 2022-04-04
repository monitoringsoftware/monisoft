package de.jmonitoring.DataHandling;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DataHandling.Interpolators.MinMax;
import de.jmonitoring.base.MainGUI;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import org.jfree.data.time.Month;
import org.jfree.data.time.RegularTimePeriod;

/**
 * This class in responsible for calculationg monthly consumption values from
 * raw data
 *
 * @author togro
 */
public class MonthlyUsageCalculator {

    private DateInterval interval;
    private RegularTimePeriod monthPeriod; // Betrachtungsmoat (mit Jahr)
    private MeasurementTreeSet dataSet;
    private Long missingTime = 0L;
    private Object syncObject_writeAll;
    private MoniSoftProgressBar progressBar;
    private final MainApplication gui;
    private static boolean isLocked;

    /**
     * Create new instance with the given GUI
     *
     * @param gui
     */
    public MonthlyUsageCalculator(MainApplication gui) {
        super();
        this.gui = gui;
    }

    /**
     * Return the locked status of this instance
     *
     * @return <oce>true</code> if a calculation is already working
     */
    public static boolean isLocked() {
        return isLocked;
    }

    /**
     * Calculates the monthly consumption / mean for the given sensor, month and
     * year<p> Data of T_History is used, values will be interpolated at the
     * interval borders
     *
     * @param month Month (1-12)
     * @param year Year
     * @param sensor id of sensor
     * @return
     */
    public IntervalMeasurement getInterpolatedMonthlyUsage(Integer month, Integer year, int sensor) {
        monthPeriod = new Month(month, year);
        interval = new DateInterval(new Date(monthPeriod.getFirstMillisecond()), new Date(monthPeriod.getLastMillisecond()));
        Thread t = new RawDataGetterThread(sensor);
        t.setName("MonthlyUsageCalcThread");
        t.start(); // dataSet füllen
        try {
            t.join(); // warten bis thread beendet
            if (dataSet == null || dataSet.first().getValue() == null) {
                Messages.showMessage(SensorInformation.getDisplayName(sensor) + ": " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("NO_VALID_VALUE") + " " + MoniSoftConstants.getMonthFor(month - 1) + " " + year + "\n", true);
//                System.out.println("Trage null ein für " + SensorInformation.getDisplayName(sensor) + " für " + month + " " + year);
            }
            trimDataMap(dataSet, interval);
            return new IntervalMeasurement(dataSet.first().getValue(), new MinMax(), missingTime, interval.getStartDate().getTime(), Collections.<String>emptyList());

        } catch (InterruptedException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
            return null;
        }
    }

    /**
     * Calculates the monthly consumption / mean for the given sensor, month and
     * year<p> Data is taken from T_Monthly
     *
     * @param month Month (1-12)
     * @param year Year
     * @param sensor id of sensor
     * @return
     */
    public Double getStoredMonthlyUsage(Integer month, Integer year, int sensor) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Double readVal = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.execute("select value from T_Monthly where year=" + year + " and month=" + month + " and T_Sensors_id_Sensors=" + sensor);
            rs = stmt.getResultSet();
            if (rs.next()) {
                readVal = rs.getDouble(1);
                if (rs.wasNull()) {
                    readVal = null;
                }
            }

        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
            Messages.showMessage("Error querying database [MonthlyUsageCalculator.getStoredMonthlyUsage]: " + ex.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return readVal;
    }

    /**
     * Bearbeitet den übergebenen Messpunkt für einen Monat oder alle Monate
     * eines Jahres und initiiert den Eintrag in die DB
     *
     * @param month Kalendermonat (1-12). Wenn <code>null</code> wird jeder
     * Monat des Kalenderjahres bearbeitet
     * @param year Kalenderjahr
     * @param sensor id des zu schreibenden Messpunkts
     */
    public void writeMonthlyUsage(Integer month, Integer year, int sensor) {
        final boolean fromGUI = (gui instanceof MainGUI);
        IntervalMeasurement measurement;
        if (month == null) { // Für jeden Monat des Jahres berechnen
            for (int i = 1; i <= 12; i++) {
                if (fromGUI) {
                    progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("PROCESSING_SENSOR") + " " + SensorInformation.getDisplayName(sensor) + " (" + i + "/" + year + ")");
                }
                measurement = getInterpolatedMonthlyUsage(i, year, sensor);
                writeToDB(i, year, measurement == null ? null : measurement.getValue(), sensor, false);
            }
        } else { // nur für den übergebenen Monat berechnen
            if (fromGUI) {
                progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("PROCESSING_SENSOR") + " " + SensorInformation.getDisplayName(sensor) + " (" + month + "/" + year + ")");
            }
            measurement = getInterpolatedMonthlyUsage(month, year, sensor);
            writeToDB(month, year, measurement == null ? null : measurement.getValue(), sensor, false);
        }
    }

    /**
     * Bearbeitet alle Messpunkte für einen Monat oder alle Montae eines Jahres
     * und initiiert den Eintrag in die DB
     *
     * @param month Kalendermonat (1-12). Wenn <code>null</code> wird jeder
     * Monat des Kalenderjahres bearbeitet
     * @param year Kalenderjahr
     */
    private void writeAllMonthlyUsageWorker(Integer month, Integer year) {
//        System.out.println("run");
        final boolean fromGUI = (gui instanceof MainGUI);
        ArrayList<SensorProperties> sensorList = SensorInformation.getSensorList();
//        Iterator<SensorProperties> it = SensorInformation.getSensorList().iterator();
        int id;
        int count = 0;
        if (fromGUI) {
            progressBar.setMinMax(0, sensorList.size());
        }

        for (SensorProperties props : sensorList) {
            if (!((StoppableThread) Thread.currentThread()).running) {
                if (fromGUI) {
                    progressBar.remove();
                }
                return;
            }

            id = props.getSensorID();
            if (fromGUI) {
                progressBar.setValue(count++);
            }

            // wenn es ein Zähler oder verbrauch ist: Werte berechnen
            if (!props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
                writeMonthlyUsage(month, year, id);
            }
        }
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FINISHED_MONTHLY") + "\n", true);
        if (fromGUI) {
            progressBar.remove();
        }
    }

    /**
     * Deletes all enries in T_Monthly for the given sensor
     *
     * @param sensor The sensor id to be deleted
     * @return The number of deleted entries
     */
    public Integer deleteAllMonthlyUsages(int sensor) {
        Integer i = 0;
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
//            System.out.println("delete from " + MoniSoftConstants.MONTHLY_TABLE + " where " + MoniSoftConstants.MONTHLY_SENSOR + "=" + sensor);
            i = stmt.executeUpdate("delete from " + MoniSoftConstants.MONTHLY_TABLE + " where " + MoniSoftConstants.MONTHLY_SENSOR + "=" + sensor);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
            Messages.showMessage("Error querying database [deleteAllMonthlyUsages]: " + ex.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return i;
    }

    /**
     * Schreibt einen Monatswert in die Monatsdatenbank. Per Mehrfachindex in
     * der DB-Definition können keine Werte überschrieben werden wenn insert
     * benutzt wird
     *
     * @param month Kalendermonat (1-12)
     * @param year Kalenderjahr
     * @param value Zu schreibender Wert
     * @param sensor id des zu schreibenden Messpunkts
     */
    public void writeToDB(Integer month, Integer year, Double value, int sensor, boolean overwrite) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        if (month != null && year != null) {
            try {
                myConn = DBConnector.openConnection();
                stmt = myConn.createStatement();
                stmt.execute("select value from T_Monthly where year=" + year + " and month=" + month + " and T_Sensors_id_Sensors=" + sensor); // prüfen ob für diesen Messpunkt schon ein Wert existiert
                rs = stmt.getResultSet();
                if (!rs.next()) {
                    // es gibt noch keinen Wert - neu eintragen
//                    System.out.println("insert into T_Monthly set month=" + month + ",year=" + year + ",value=" + value + ",T_Sensors_id_Sensors=" + sensor);
                    stmt.executeUpdate("insert into T_Monthly set month=" + month + ",year=" + year + ",value=" + value + ",T_Sensors_id_Sensors=" + sensor + ",T_Log_id_log=0");
                } else { // es gibt einen Wert - updaten
                    rs.getDouble(1);
                    if (overwrite || rs.wasNull()) {
//                        System.out.println(" Es gibt schon einen anderen Wert für:  month=" + month + ",year=" + year + ",value=" + value + ",T_Sensors_id_Sensors=" + sensor);
                        stmt.executeUpdate("update T_Monthly set value=" + value + " where year=" + year + " and month=" + month + " and T_Sensors_id_Sensors=" + sensor);
                    }
                }
            } catch (MySQLSyntaxErrorException ex) {
                Messages.showException(ex);
                Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            } catch (Exception ex) {
                Messages.showException(ex);
                Messages.showException(ex);
                Messages.showMessage("Error querying database [MonthlyUsageCalculator]: " + ex.getMessage() + "\n", true);
            } finally {
                DBConnector.closeConnection(myConn, stmt, rs);
            }
        }
    }

    /**
     * Invokes the database query for raw data and get a value form one month
     */
    class RawDataGetterThread extends StoppableThread {

        int sensorID;

        public RawDataGetterThread(int id) {
            sensorID = id;
        }

        @Override
        public void run() {
            running = true;
            DatasetWorker dw = new DatasetWorkerFactory(MonthlyUsageCalculator.this.gui).createFor(sensorID);
            dw.setVerbose(false);
            CounterMode countermode = CounterMode.getInterpolationMode(false, false, SensorInformation.getSensorProperties(sensorID).isCounter(), SensorInformation.getSensorProperties(sensorID).isUsage(), MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1"));
            dataSet = dw.getInterpolatedData(interval, monthPeriod, countermode);

            missingTime = dw.getMissingTime();
        }
    }

    /**
     * Invokes the entry of calculated values to T_Monthly
     *
     * @param month The month
     * @param year The year
     * @param overwrite If <code>true</code> existsing data will be overwritten
     * @param o A synchronization object
     */
    public void startWriteAllMonthlyUsage(Integer month, Integer year, boolean overwrite, Object o) {
        final boolean fromGUI = (gui instanceof MainGUI);

        if (isLocked) {
            return;
        }
        isLocked = true;
        syncObject_writeAll = o;
        if (fromGUI) {
            progressBar = this.gui.getProgressBarpanel().addProgressBar(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CALC_MONTHLY"));
        }

        final StoppableThread writeAllMonthlyUsageThread = new StoppableThread(new workingTask(month, year));

        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fromGUI) {
                    progressBar.remove();
                }
                writeAllMonthlyUsageThread.running = false;
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("MONTHLY_CANCELLED") + "\n", true);
            }
        };
        if (fromGUI) {
            progressBar.addProgressCancelButtonActionListener(action);
        }
        writeAllMonthlyUsageThread.start();
        if (!fromGUI) { // if we come from the gui we have to wait for the process to finish.otherwise the backup would just end when the application finishes
            try {
                writeAllMonthlyUsageThread.join();
            } catch (Exception ex) {
                Messages.showException(ex);
            }
        }
    }

    /**
     * This class starts the calculation wor in a new thread
     */
    public class workingTask implements Runnable {

        private final Integer month;
        private final Integer year;

        public workingTask(Integer m, Integer y) {
            month = m;
            year = y;
        }

        @Override
        public void run() {
            synchronized (syncObject_writeAll) {
                ((StoppableThread) Thread.currentThread()).running = true;
                try {
                    // do the work
                    writeAllMonthlyUsageWorker(month, year);
                    syncObject_writeAll.notify();
                    isLocked = false;
                } catch (Exception e) {
                    Messages.showException(e);
                }
            }
        }
    }

    /**
     * Trim dataset to given time period
     *
     * @param dataSet The dataset
     * @param dateInterval The date interval
     */
    private void trimDataMap(MeasurementTreeSet dataSet, DateInterval dateInterval) {
        if ((dataSet != null)) {
            for (Iterator<Measurement> it = dataSet.iterator(); it.hasNext();) {
                Measurement measurement = it.next();
                if (measurement.getTime() < dateInterval.getStartDate().getTime() || measurement.getTime() > dateInterval.getEndDate().getTime()) {
                    it.remove();
                }
            }
        }
    }
}
