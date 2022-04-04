/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DataHandling.DataHandler;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.intervals.DateInterval;
import java.sql.PreparedStatement;

/**
 *
 * @author togro
 */
public class EventTimeSeries implements Serializable {

    private final IntervalXYDataset set;

    public EventTimeSeries(ArrayList collection, DateInterval dateInterval) {
        super();
        set = generateEventSeries(collection, dateInterval);
    }

    private IntervalXYDataset generateEventSeries(ArrayList<TimeSeriesLooks> seriesLooks, DateInterval interval) {
        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();
        SimpleDateFormat MySQLDateFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DateInterval dInterval = interval.clone();
        Iterator<TimeSeriesLooks> it;
        TimeSeriesLooks currentLooks;
        int lfd = 0;
        Date stateStartTime, stateEndTime;
        long duration;
        Object stateObject;
        boolean state = false;
        long tolerance = 3 * 24 * 60 * 60 * 1000; // TODO wählbar machen

        try {
            myConn = DBConnector.openConnection();

            it = seriesLooks.iterator();
            while (it.hasNext()) {
                currentLooks = it.next();
                XYIntervalSeries trueSeries = new XYIntervalSeries(currentLooks.getSensorID());             // Serie 1
                XYIntervalSeries falseSeries = new XYIntervalSeries(currentLooks.getSensorID() + "%");      // Serie 2
                XYIntervalSeries missingSeries = new XYIntervalSeries(currentLooks.getSensorID() + "#");    // Serie 3
                addItem(lfd, missingSeries, new Second(dInterval.getStartDate()), new Second(dInterval.getEndDate())); // Komplettes INtervall als fehlend im  Hintergrund

                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("LESE_EVENT") + " " + SensorInformation.getDisplayName(currentLooks.getSensorID()) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("AUS_DATENBANK"), true);

                // Letzten Stand des Events holen (vor Abfrageintervall)
                DataHandler dh = new DataHandler(currentLooks.getSensorID());
                Measurement prevMeasurement = dh.getPreviousDBEntry(null, interval.getStartDate()); // Holt den letzten Status vor dem Abfrageintervall

                if (prevMeasurement != null) {
                    Long span = dh.getEventDurationAtTimestamp(new Date(prevMeasurement.getTime())); // Holt die Dauer dieses Zustands
                    if (span != null) {
                        state = prevMeasurement.getValue().intValue() == 0 ? false : true;

                        if (prevMeasurement.getTime() > interval.getStartDate().getTime()) { // wenn der endzeitpunkt dieses ersten events noch vor dem intervallstart liegt weglassen
                            if (state) {
                                addItem(lfd, trueSeries, new Second(interval.getStartDate()), new Second(new Date(prevMeasurement.getTime() + span - 1L)));
                            } else {
                                addItem(lfd, falseSeries, new Second(interval.getStartDate()), new Second(new Date(prevMeasurement.getTime() + span - 1L)));
                            }
                        }
                    }
                }

                stmt = myConn.prepareStatement("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " as e where e.TimeStart >= ? and e.TimeStart <= ? and e.T_Sensors_id_Sensors = ?");
                stmt.setString(1, dInterval.getStartDateString(MySQLDateFormat));
                stmt.setString(2, dInterval.getEndDateString(MySQLDateFormat) + " 23:59:59");
                stmt.setInt(3, currentLooks.getSensorID());
                rs = stmt.executeQuery();
                rs.last();
                Messages.showMessage(rs.getRow() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("EINTRAEGE_GELESEN"), true);
                rs.beforeFirst();
                while (rs.next()) {
                    stateStartTime = rs.getTimestamp(1);
                    duration = rs.getLong(2);
                    stateObject = rs.getObject(3);
                    if (stateObject != null) { // wenn es einen gültigen Wert gibt
                        state = (Integer) stateObject == 0 ? false : true;  // false wenn der Wert 0 ist ansonsten true, d.h. auch bei z.B. 5% Sonnenschutz
                        stateEndTime = new Date(stateStartTime.getTime() + duration - 1);
                        if (stateEndTime.getTime() > interval.getEndDate().getTime()) {
                            stateEndTime = interval.getEndDate();
                        }
                        if (state) {
                            addItem(lfd, trueSeries, new Second(stateStartTime), new Second(stateEndTime));
                        } else {
                            addItem(lfd, falseSeries, new Second(stateStartTime), new Second(stateEndTime));
                        }
                    } else {    // der Status ist 'null'
                        if (duration < tolerance) { // wenn die Lücke tolerabel ist
                            stateEndTime = new Date(stateStartTime.getTime() + duration - 1);
                            if (stateEndTime.getTime() > interval.getEndDate().getTime()) {
                                stateEndTime = interval.getEndDate();
                            }
                            if (state) { // enthält den letzten Status der nicht null war
                                addItem(lfd, trueSeries, new Second(stateStartTime), new Second(stateEndTime));
                            } else {
                                addItem(lfd, falseSeries, new Second(stateStartTime), new Second(stateEndTime));
                            }
                        }
                    }
                }
                dataset.addSeries(falseSeries);
                dataset.addSeries(trueSeries);
                dataset.addSeries(missingSeries);
                lfd++;
            }
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/common/resource").getString("FEHLER_BEI_DATENBANKABFRAGE") + e.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return dataset;
    }

    private void addItem(int sensorIndex, XYIntervalSeries s, RegularTimePeriod p0, RegularTimePeriod p1) {
        s.add(p0.getFirstMillisecond(), p0.getFirstMillisecond(), p1.getLastMillisecond(), sensorIndex, sensorIndex - 0.35, sensorIndex + 0.35);
    }

    public IntervalXYDataset getDataset() {
        return set;
    }
}
