package de.jmonitoring.DataHandling;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.jfree.chart.plot.ValueMarker;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import de.jmonitoring.Components.DeleteDataDialog;

import de.jmonitoring.Components.MaintenanceNaviPanel.ProgressListener;
import de.jmonitoring.DBOperations.DBConnector;
import static de.jmonitoring.DataHandling.DatabaseQuery.ONE_HOUR;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.intervals.DateInterval;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

/**
 * This class holds methods for the manipultion of the database concerning data
 * entries
 *
 * @author togro
 */
public class DataHandler {

    private Integer sensorID;
    private ArrayList<String> remarks = new ArrayList<String>();
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Creates a new handler for the given sensor
     *
     * @param sensorID The sensor which to operate on
     */
    public DataHandler(Integer sensorID) {
        super();
        this.sensorID = sensorID;
    }

    /**
     * Delete a single value at the given timestamp
     *
     * @param date The timestamp
     */
    public synchronized boolean deleteSingleValue(long date) {
        boolean success = false;
//        dateID = getTimeID(date);
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from T_History where T_Sensors_id_Sensors=" + sensorID + " and TimeStamp=" + date / 1000L);
            success = true;
            logger.info("Deleted a single value for sensor " + sensorID + " at " + new Date(date).toString());
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return success;
    }

    /**
     * Delete all values in the given time range
     *
     * @param startDate The start time
     * @param endDate The end time
     * @return The number of deleted data points
     */
    public synchronized Integer deleteValuesForTimeRange(DateInterval d) {
        String fromString = "";
        String toString = "";
        String table;
        String timeField;
        Connection myConn = null;
        Statement stmt = null;
        Date startDate = null;
        Date endDate = null;
        if (d != null) {
            startDate = d.getStartDate();
            endDate = d.getEndDate();
        }
        SimpleDateFormat format = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);

        // Unterscheiden ob Event oder normaler Wert
        if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
            table = MoniSoftConstants.EVENT_TABLE;
            timeField = "TimeStart";
            if (startDate != null) { // startdate und endDate = null bedeutet: alle vorhandenen Daten
                fromString = " and " + timeField + " >= '" + format.format(startDate) + "'";
            }
            if (endDate != null) {
                endDate.setTime(endDate.getTime() + - 1L); // auf 23:59:59 des letzen Tages setzen
                toString = " and " + timeField + " <= '" + format.format(endDate) + "'";
            }
        } else {
            table = MoniSoftConstants.HISTORY_TABLE;
            timeField = "TimeStamp";
            if (startDate != null) { // startdate und endDate = null bedeutet: alle vorhandenen Daten
                fromString = " and " + timeField + " >= " + startDate.getTime() / 1000L;
            }
            if (endDate != null) {
                endDate.setTime(endDate.getTime() + - 1L); // auf 23:59:59 des letzen Tages setzen
                toString = " and " + timeField + " <= " + endDate.getTime() / 1000L;
            }
        }

        final String query = "delete quick ignore from " + table + " where T_Sensors_id_Sensors = " + sensorID + fromString + toString;
        int count = 0;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            count = stmt.executeUpdate(query);
            logger.info("Deleted " + count + " data points for sensor " + sensorID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return count;
    }
    
    public synchronized Integer invalidateValuesForTimeRange(DateInterval d, Integer mode) {
        String fromString = "";
        String toString = "";
        String table;
        String timeField;
        Connection myConn = null;
        ResultSet rs = null;
        Statement stmt = null;
        Date startDate = null;
        Date endDate = null;
        if (d != null) {
            startDate = d.getStartDate();
            endDate = d.getEndDate();
        }
        SimpleDateFormat format = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);

        // Unterscheiden ob Event oder normaler Wert
        if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
            table = MoniSoftConstants.EVENT_TABLE;
            timeField = "TimeStart";
            if (startDate != null) { // startdate und endDate = null bedeutet: alle vorhandenen Daten
                fromString = " and " + timeField + " >= '" + format.format(startDate) + "'";
            }
            if (endDate != null) {
                endDate.setTime(endDate.getTime() + - 1L); // auf 23:59:59 des letzen Tages setzen
                toString = " and " + timeField + " <= '" + format.format(endDate) + "'";
            }
        } else {
            table = MoniSoftConstants.HISTORY_TABLE;
            timeField = "TimeStamp";
            if (startDate != null) { // startdate und endDate = null bedeutet: alle vorhandenen Daten
                fromString = " and " + timeField + " >= " + startDate.getTime() / 1000L;
            }
            if (endDate != null) {
                endDate.setTime(endDate.getTime() + - 1L); // auf 23:59:59 des letzen Tages setzen
                toString = " and " + timeField + " <= " + endDate.getTime() / 1000L;
            }
        }

        final String selectQuery = "select T_Log_id_Log, T_Sensors_id_Sensors, Value, TimeStamp from " + table + " where T_Sensors_id_Sensors = " + sensorID + fromString + toString;
        int count = 0;
        
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
        
            rs = stmt.executeQuery( selectQuery );
        
            while( rs.next() )
            {
                count++;
                
                logger.info( "1: " + rs.getInt(1) );
                
                if( mode == DeleteDataDialog.MODE_INVALID)
                {
                    Integer timestamp = rs.getInt(4);
                    markIgnore( timestamp * 1000L );
                }                
                else if( mode == DeleteDataDialog.MODE_VALID)
                {
                    Integer tLogid = rs.getInt(1);
                    if( tLogid > 0 )
                        removeMarkIgnore( tLogid );
                }
            }
            
            logger.info("Invalidated " + count + " data points for sensor " + sensorID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETE_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return count;
    }

    /**
     * Marks the data entry at the given timestamp as ignoreable
     *
     * @param time The timestamp
     */
    public synchronized void markIgnore(long time ) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        //String timeString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(new Date(time));
        String timeString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(new Date());
//        dateID = getTimeID(timeStart);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("insert into T_Log set EventType=" + MoniSoftConstants.LOG_INVALID + ",Time='" + timeString + "',Description='Wert ungültig (" + SensorInformation.getDisplayName(sensorID) + ")', User='" + MoniSoft.getInstance().getDBConnector().getUserName() + "'", Statement.RETURN_GENERATED_KEYS); // Neuer Eintrag in Log-Tabelle
            rs = stmt.getGeneratedKeys();
            rs.next();
            // Eintrag in History mit Log-Index bestücken
            stmt.executeUpdate("update T_History set T_Log_id_Log=" + rs.getInt(1) + " where TimeStamp=" + time / 1000L + " and " + "T_Sensors_id_Sensors=" + sensorID + " limit 1");
            logger.info("Added ignore mark for sensor " + sensorID + " at " + new Date(time));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }
    
    /**
     * Removes the marks at the given timestamp as ignoreable
     *
     * @param time The timestamp
     */
    public synchronized void removeMarkIgnore(Integer tLogId) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();            
            // Lösche den Eintrag aus der T_Log-Tabelle
            stmt.executeUpdate( "delete from T_Log where id_Log = " + tLogId );                        
            // Setze die T_Log_id aus der t_history-Tabelle wieder auf 0
            stmt.executeUpdate( "update T_History set T_Log_id_Log = 0 where T_Log_id_Log = " + tLogId );            
            logger.info("Removed ignore mark for sensor " + sensorID + " Logid: " + tLogId );            
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Marks all data entrys invalid that have the timestamps of the given
     * markers
     *
     * @param markerList A list of markers. Their values are the timestamps for
     * which to delete
     * @param progressListener A {@link ProgressListener}
     */
    public synchronized void markIgnoreAll(ArrayList<ValueMarker> markerList, ProgressListener progressListener) {
        progressListener.startProgress(0, markerList.size(), "Mark invalid");
        int count = 0;
        Connection myConn = null;
        Statement stmt = null;
        PreparedStatement pstm_history = null;
        PreparedStatement select_history = null;
        ResultSet rs = null;
        String sep = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
        String timeString = dateFormat.format(new Date());
        ArrayList<Long> timeVector = new ArrayList<Long>(1024);
        StringBuilder sb = new StringBuilder(2500);
        try {
            myConn = DBConnector.openConnection();

            pstm_history = myConn.prepareStatement("update T_History set T_Log_id_Log = ? where T_Sensors_id_Sensors = " + sensorID + " and TimeStamp = ? limit 1");
            stmt = myConn.createStatement();

            select_history = myConn.prepareStatement("select id from T_History where T_Sensors_id_Sensors = " + sensorID + " and TimeStamp = ? limit 1");
            for (Long time : timeVector) {
                select_history.setLong(1, time / 1000L);
                rs = select_history.executeQuery();
            }

            for (ValueMarker marker : markerList) {
                timeVector.add((long) marker.getValue());
                sb.append(sep).append("(").append(MoniSoftConstants.LOG_INVALID).append(",'").append(timeString).append("'," + "'" + "Invalid (").append(SensorInformation.getDisplayName(sensorID)).append(")'," + "'").append(MoniSoft.getInstance().getDBConnector().getUserName()).append("'" + ")");
                sep = ",";
            }

            stmt.executeUpdate("insert into T_Log (EventType,Time,Description,User) VALUES " + sb.toString(), Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();
            while (rs.next()) {
                count++;
                progressListener.setProgress(count, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SET_INVALID") + " (" + count + "/" + markerList.size() + ")");
                pstm_history.setInt(1, rs.getInt(1));
                pstm_history.setLong(2, timeVector.get(0) / 1000L);

                pstm_history.executeUpdate();
                timeVector.remove(0);
            }
            logger.info("Marked " + count + " values of sensor " + sensorID + " as invalid");
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(null, pstm_history, null);
            DBConnector.closeConnection(null, select_history, null);
            DBConnector.closeConnection(myConn, stmt, rs);
            progressListener.endProgess();
        }
    }

    /**
     * Marks the datapoint at the given timestamp as valid (removes ignore flag)
     *
     * @param setTime The timstamp
     */
    public synchronized void markValid(long setTime) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Long time = setTime / 1000L;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            // LogID des Eintrags ermitteln
            rs = stmt.executeQuery("select T_Log_id_Log from T_History where TimeStamp=" + time + " and T_Sensors_id_Sensors=" + sensorID + " limit 1");
            rs.next();
            int logID = rs.getInt(1);
            int count = stmt.executeUpdate("update T_History set T_Log_id_Log = 0 where TimeSTamp=" + time + " and T_Log_id_Log=" + logID + " limit 1");
            stmt.executeUpdate("delete from T_Log where id_Log=" + logID + " limit 1");
            logger.info("Marked " + count + " values of sensor " + sensorID + " as valid");
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Marks all data entrys valid that have the timestamps of the given markers
     *
     * @param markerList A list of markers. Their values are the timestamps for
     * which to handle
     */
    public synchronized void markValidAll(ArrayList<ValueMarker> markerList) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        long time;
        StringBuilder idBuffer = new StringBuilder(2500);
        String sep = "";
        StringBuilder timeBuffer = new StringBuilder(2500);

        try {
            myConn = DBConnector.openConnection();
            for (ValueMarker marker : markerList) {
                time = ((long) marker.getValue()) / 1000L;
                timeBuffer.append(sep);
                timeBuffer.append(time);
                sep = ",";
            }
            sep = "";
            stmt = myConn.createStatement();
            // LogIDs der Einträge ermitteln
            rs = stmt.executeQuery("select T_Log_id_Log from T_History where TimeStamp in (" + timeBuffer.toString() + ") and T_Sensors_id_Sensors=" + sensorID);
            while (rs.next()) {
                idBuffer.append(sep);
                idBuffer.append(rs.getInt(1));
                sep = ",";
            }
            int count = stmt.executeUpdate("update T_History set T_Log_id_Log = 0 where T_Log_id_Log in (" + idBuffer.toString() + ")");
            stmt.executeUpdate("delete from T_Log where id_Log in (" + idBuffer.toString() + ")");
            logger.info("Marked " + count + " values of sensor " + sensorID + " as valid");
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Inserts a value to the database at the given time
     *
     * @param date The time
     * @param value The value
     * @return <code>true</code> if the operation was successful
     */
    public synchronized boolean insertManualValue(Date date, double value) {
        String nowString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(new Date());
        ResultSet rs = null;
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            // Log-Eintrag
            int log = stmt.executeUpdate("insert into T_Log set EventType=" + MoniSoftConstants.LOG_MANUAL + ",Time='" + nowString + "',User='" + MoniSoft.getInstance().getDBConnector().getUserName() + "',Description='Manueller Eintrag'", Statement.RETURN_GENERATED_KEYS);
            consume(log);

            rs = stmt.getGeneratedKeys();
            rs.next();

            int logID = rs.getInt(1);
            int n;
            if (valueExistsAt(date)) {
                n = stmt.executeUpdate("update T_History set Value=" + value + ", T_Log_id_Log=" + logID + " where TimeStamp=" + date.getTime() / 1000L + " and T_Sensors_id_Sensors=" + sensorID);
            } else {
                n = stmt.executeUpdate("insert into T_History set Value=" + value + ",T_Sensors_id_Sensors=" + sensorID + ",TimeStamp=" + date.getTime() / 1000L + ", T_Log_id_Log=" + logID);
            }
            consume(n);
            logger.info("Updated or added manual value(s) " + value + " for " + sensorID + " at " + date.toString());
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (Exception ex) {
            Messages.showException(ex);
            return false;
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return true;
    }

    /**
     * Determine if there is a value at the given time
     *
     * @param date The time
     * @return <code>true</code> if this sensor has a value at the specified
     * time
     */
    public synchronized boolean valueExistsAt(Date date) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select Value from T_History where TimeStamp=" + date.getTime() / 1000L + " and T_Sensors_id_Sensors=" + sensorID + " limit 1");
            if (rs.next()) {
                return true;
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return false;
    }

    /**
     * Updates the timeSpan field of the event-table for the given sensorID and
     * start timeStart
     *
     * @param timeStart
     * @param timeSpan
     * @return true if there where no errors
     */
    public synchronized boolean updateEvent(Date timeStart, long timeSpan) {
        boolean success = false;
        Connection myConn = null;
        PreparedStatement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("update " + MoniSoftConstants.EVENT_TABLE + " set TimeSpan= ? where T_Sensors_id_Sensors= ? and TimeStart = ?");
            stmt.setLong(1, timeSpan);
            stmt.setInt(2, sensorID);
            stmt.setString(3, new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(timeStart));
            stmt.executeUpdate();
            success = true;
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return success;
    }

    /**
     * Returns the the oldest event in the event table
     *
     * @return the event or <code>null</code> if there is no event in the table
     */
    public synchronized EventMeasurement getOldestEventInEventtable() {
        EventMeasurement measurement = null;
        Long timeStart;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select TimeStart, TimeSpan, State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors= ? order by TimeStart desc limit 1");
            stmt.setInt(1, sensorID);
            rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp stamp = rs.getTimestamp(1);
                timeStart = stamp.getTime();
                Long timeSpan = rs.getLong(2);
                Integer state = rs.getInt(3);
                if (rs.wasNull()) {
                    state = null;
                }
                measurement = new EventMeasurement(timeStart, timeSpan, state);
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return measurement;
    }   
    
    /**
     * Returns all events from the event table
     *
     * <code>null</code>
     * @param searchDate The date for that we search ist predecessor
     * @return The {@link Measurement}. <code>null</code> if there is none
     */
    public synchronized List<EventMeasurement> getAllEvents() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        List<EventMeasurement> eventmeasurementList = new ArrayList<EventMeasurement>();
        try
        {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            rs = stmt.executeQuery("select TimeStart, TimeSpan, State from T_Events where T_Sensors_id_Sensors=" + sensorID + " and State is not null order by TimeStart");

            while (rs.next())
            {
                EventMeasurement eventMeasurement = new EventMeasurement();
                eventMeasurement.setTimeStart(rs.getTimestamp(1).getTime());
                eventMeasurement.setDuration(rs.getLong(2));
                eventMeasurement.setValue(rs.getInt(3));
                
                eventmeasurementList.add( eventMeasurement );
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return eventmeasurementList;
    }
    
    /**
     * Returns all events from the event table
     *
     * <code>null</code>
     * @param searchDate The date for that we search ist predecessor
     * @return The {@link Measurement}. <code>null</code> if there is none
     */
    public synchronized void deleteEventsForSensor( int sensorID ) {
        Connection myConn = null;
        Statement stmt = null;        
        int result = -1;
        try
        {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            result = stmt.executeUpdate("delete from T_Events where T_Sensors_id_Sensors=" + sensorID );
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
            // System.out.println( "deleteEventsForSensor: " + sensorID + " result: " + result );
        }        
    }
    
     /**
     * Returns the previous database entry immediately before the given time
     *
     * @param earliestDate Oldest date that should be returned can be
     * <code>null</code>
     * @param searchDate The date for that we search ist predecessor
     * @return The {@link Measurement}. <code>null</code> if there is none
     */
    public synchronized Measurement getPreviousDBEntry(Date earliestDate, Date searchDate) {
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
        
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Long dateLong;
        String where = "";        
        
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
                if (earliestDate != null) {
                    where = " and TimeStart >= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(earliestDate) + "'";
                }
                rs = stmt.executeQuery( "select TimeStart, State from T_Events where T_Sensors_id_Sensors=" + sensorID + where + " and TimeStart < '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(searchDate) + "' and State is not null order by TimeStart desc limit 1" );
                if (rs.next()) {
                    dateLong = rs.getTimestamp(1).getTime();
                    int state = rs.getInt(2);
                    return new Measurement(dateLong, Double.valueOf(state));
                }
            } else { // kein event
                if (earliestDate != null) {
                    where = " and TimeStamp >= " + earliestDate.getTime() / 1000L;
                }
                String searchDateString = "" + (searchDate.getTime() / 1000L);
                rs = stmt.executeQuery("select hist.TimeStamp*1000 as t,hist.value as v,hist.T_Log_id_Log as log from " + MoniSoftConstants.SENSOR_TABLE + " as sens, " + MoniSoftConstants.HISTORY_TABLE + " as hist where sens.id_Sensors=" + sensorID + where + " and sens.id_Sensors=hist.T_Sensors_id_Sensors and hist.TimeStamp < " + searchDateString + " group by hist.TimeStamp order by hist.TimeStamp desc limit 1");
                if (rs.next()) {
                    int log_id = rs.getInt(rs.findColumn("log"));
                    if (log_id == 0 || getLogEventType(log_id) != MoniSoftConstants.LOG_INVALID) {
                        dateLong = rs.getLong(1);
                        double f = rs.getDouble(2);                        
                        // MONISOFT-22: Verschiebe den Zeitraum und berücksichtige die Sommerzeit, falls eingestellt                        
                        long time = dateLong + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() * 1000L;                    
                        if( SensorInformation.getSensorProperties(sensorID).isSummerTime() )
                        {      
                            if( DateInterval.isInSummertime( time ) )
                                time -= ONE_HOUR;
                            
                            return new Measurement(time, f);
                        }
                        else
                            return new Measurement(time, f);
                    }
                }
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return null;
    }

    /**
     * Returns the next database entry immediately after the given time
     *
     * @param earliestDate Youngest date that should be returned can be
     * <code>null</code>
     * @param searchDate The date for that we search ist successor
     * @returnThe {@link Measurement}. <code>null</code> if there is none
     */
    public synchronized Measurement getNextDBEntry(Date latestDate, Date searchDate) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String where = "";
        Long dateLong;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
                if (latestDate != null) {
                    where = " and TimeStart <= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat).format(latestDate) + "'";
                }
                rs = stmt.executeQuery("select TimeStart, TimeSpan, State from T_Events where T_Sensors_id_Sensors=" + sensorID + where + " and TimeStart > '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat).format(searchDate) + "' and State is not null order by TimeStart limit 1");
                if (rs.next()) {
                    dateLong = rs.getTimestamp(1).getTime();
                    long span = rs.getLong(2);
                    int state = rs.getInt(3);                    
                    return new Measurement(dateLong, Double.valueOf(state));
                }
            } else { // kein event
                if (latestDate != null) {
                    where = " and TimeStamp <= " + latestDate.getTime() / 1000L;
                }
                rs = stmt.executeQuery("select TimeStamp*1000,value,T_Log_id_Log from " + MoniSoftConstants.HISTORY_TABLE + " where T_Sensors_id_Sensors=" + sensorID + where + " and TimeStamp > " + searchDate.getTime() / 1000L + " order by TimeStamp limit 1");
                if (rs.next()) {
                    int log_id = rs.getInt(3);
                    if (log_id == 0 || getLogEventType(log_id) != MoniSoftConstants.LOG_INVALID) {                        
                        dateLong = rs.getLong(1);
                        double f = rs.getDouble(2);
                        // System.out.println( "New Measurement: " + dateLong + " Value: " + f );
                        // MONISOFT-22: Verschiebe den Zeitraum und berücksichtige die Sommerzeit, falls eingestellt                        
                        long time = dateLong + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() * 1000L;                    
                        if( SensorInformation.getSensorProperties(sensorID).isSummerTime() )
                        {      
                            if( DateInterval.isInSummertime( time ) )
                                time += ONE_HOUR;
                            
                            return new Measurement(time, f);
                        }
                        else
                            return new Measurement(time, f);

                    }
                }
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return null;
    }

    /**
     * Returns the state for that was dominant in the given time raange
     *
     * @param start The start time
     * @param end The end time
     * @return A {@link IntervalValue} containing the state
     */
    public IntervalValue getSignificantEventStateForInterval(Date start, Date end) {
        return getEventStateForInterval(start, end, EventMode.EVENT_SIGNIFICANT_MODE);
    }

    /**
     * Returns the state for the given time range<p> Two modes can be used:<p>
     * <code>EventMode.EVENT_SIGNIFICANT_MODE</code> - This mode returns the
     * state (0 or 1) that was dominant for most of the time in the given time
     * range.<p>
     * <code>EventMode.EVENT_MEAN_MODE</code> - This mode returns the means
     * value of all event states in the given time range (e.g. 0.5 if both
     * states were equally distributed)<p>
     *
     * @param start The start time
     * @param end The end time
     * @param eventMode The mode to use
     * @return A {@link IntervalValue} containing the state
     */
    public synchronized IntervalValue getEventStateForInterval(Date start, Date end, EventMode eventMode) {
        Double state = null;
        Double returnValue = null;
        Double lastState = null, thisState;
        Date firstTime = start;
        Long thisTimeSpan;

        SimpleDateFormat MySQLDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
        HashMap<Double, Long> map = new HashMap<Double, Long>(1024);

        String startDateString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(start);
        String endDateString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(end);
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Date thisTimeStart;
//        System.out.print("return " + startDateString + " " + endDateString + " ");

        try {
            myConn = DBConnector.openConnection();
            // den letzen Wert vor dem Intervall holen
            lastState = getEventStateAtTimestamp(start);

//            System.out.print("letzter: " + lastState +  " ");

            // alle Events im Intervall holen
            stmt = myConn.createStatement();
//            System.out.println("select TimeStart, State, TimeSpan from T_Events where T_Sensors_id_Sensors = " + sensorID + " and TimeStart >= '" + startDateString + "' and TimeStart <= '" + endDateString + "' order by TimeStart");
            rs = stmt.executeQuery("select TimeStart, State, TimeSpan from T_Events where T_Sensors_id_Sensors = " + sensorID + " and TimeStart >= '" + startDateString + "' and TimeStart <= '" + endDateString + "' order by TimeStart");
            while (rs.next()) {
                if (firstTime == start) { // wenn erster Durchlauf die Zeit des ersten events setzen
                    firstTime = MySQLDateTimeFormat.parse(rs.getString(1));
                }

                thisTimeStart = MySQLDateTimeFormat.parse(rs.getString(1));
                thisState = rs.getDouble(2);
                thisTimeSpan = rs.getLong(3);

                // Falls das Interval über die hintere Intervallgrenze hinausgeht abschneiden
                if (thisTimeStart.getTime() + thisTimeSpan > end.getTime()) {
//                    System.out.println("Stutze!");
                    thisTimeSpan = end.getTime() - thisTimeStart.getTime();
                }

//                System.out.print(rs.getString(1) + " " + thisState + " " + thisTimeSpan + "\n");
                if (map.containsKey(thisState)) { // wenn bereits ein Event mit diesem Wert vorhanden ist, die Dauer addieren
                    map.put(thisState, thisTimeSpan + map.get(thisState));
                } else {   // ansonsten neuen map-Eintrag  anlegen
                    map.put(thisState, thisTimeSpan);
                }
            }

            // wenn der letzte Status (vor dem Intervall) nicht null ist, den map Eintrag mit diesem Status mit der Zeitspanne beaufschlagen (falls vorhanden, sonst anlegen)
            if (lastState != null) {
                if (map.containsKey(lastState)) { // wenn bereits ein Event mit diesem Wert vorhanden ist, die Dauer addieren
                    map.put(lastState, (firstTime.getTime() - start.getTime()) + map.get(lastState));
                } else {   // ansonsten neuen map-Eintrag  anlegen
                    map.put(lastState, (firstTime.getTime() - start.getTime()));
                }
            }


            if (eventMode == EventMode.EVENT_SIGNIFICANT_MODE) { // es soll der signifikante Wert des Events für das Intervall berechnet werden
                // map-Eintrag mit der längsten Dauer suchen
                long longest = -1;
                Long timeValue;

                for (Double key : map.keySet()) {
                    timeValue = map.get(key);
//                    System.out.print(startDateString + " - " + endDateString + " Status '" + key + "' hat Anteil " + timeValue + "\n");
                    if (timeValue > longest) {
                        state = key; // wenn die Zeitspanne die längste ist, den Status dafür übernehmen.
                        longest = timeValue;
                    }

                }
                returnValue = state;
            } else { // es soll nicht das signifikante Event sondern der Mittelwert berechnet werden.
                Long span = end.getTime() - start.getTime(); // Dauer des GesamtIntervalls
                Double sum = 0d;
                // key ist der Satus-Wert
                //TODO BUG: Maybe this should be: if (!map.keySet().size() > 0) {
                // ATTENTION: The map probably already has at least one entry: lastState
                if (map.keySet().size() > 1) { // wenn Elemente implements Intervall vornhanden sind
                    for (Double stateKey : map.keySet()) { // TODO Falscher Wert wenn ganzes INtervall von Status belegt!!!!
                        sum += stateKey * ((double) map.get(stateKey) / (double) span); // zeitlich gewichteter Mittelwert (zeitlicher Anteil am Intervall
                    }
                } else {
                    sum = lastState;
                }
                returnValue = sum;
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (ParseException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return IntervalValue.forValue(returnValue);
    }

    /**
     * Determine the duration of the event status at the given time
     *
     * @param date The time in question
     * @return The duration
     */
    public synchronized Long getEventDurationAtTimestamp(Date date) {
        return performQueryForTimestamp(date, new EvaluateAs<Long>() {
            @Override
            public Long extractFrom(Date time, Double state, Long timeSpan) {
                return timeSpan;
            }
        });
    }

    /**
     * Return the event state that was present aat the given time
     *
     * @param timeStart The time
     * @return The state
     */
    public synchronized Double getEventStateAtTimestamp(final Date date) {
        return performQueryForTimestamp(date, new EvaluateAs<Double>() {
            @Override
            public Double extractFrom(Date time, Double state, Long timeSpan) {
                // ist der Wert noch gültig am Anfang des Intervalls? Wenn nicht -> null
                if (!((time.getTime() + timeSpan) >= date.getTime())) {
                    return null;
                }
                return state;
            }
        });
    }

    /**
     * This method enables the return of a result in the given type
     *
     * @param <TYPE> The type in which the result should be returned
     */
    protected static interface EvaluateAs<TYPE> {

        public TYPE extractFrom(Date time, Double state, Long timeSpan);
    }

    /**
     * Return the state, its start time and duration that was present at the
     * given time
     *
     * @param <TYPE> The type in which tio return the value
     * @param date The date
     * @param evaluation The evaluator
     * @return The state value in the specified type
     */
    protected <TYPE> TYPE performQueryForTimestamp(Date date, EvaluateAs<TYPE> evaluation) {
        String dateString = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(date);
        SimpleDateFormat MySQLDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("SELECT TimeStart, State, TimeSpan from T_Events where T_Sensors_id_Sensors = ? and TimeStart <= ? order by TimeStart desc limit 1");
            stmt.setInt(1, sensorID);
            stmt.setString(2, dateString);
            rs = stmt.executeQuery();
            if (rs.next()) {
                Date time = MySQLDateTimeFormat.parse(rs.getString(1));
                Double state = rs.getDouble(2);
                Long timeSpan = rs.getLong(3);
                return evaluation.extractFrom(time, state, timeSpan);
            }

        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return null;
    }

    /**
     * Determine a {@link IntervalValue} for an interval which has a
     * counter change
     *
     * @param v1 The last value before the old change value
     * @param v2 The next value after the new change value
     * @param startDate The interval start
     * @param endDateThe interval end
     * @return
     */
    public IntervalValue getCounterChange(Double v1, Double v2, Date startDate, Date endDate) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        IntervalValue value = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
//            System.out.println("Prüfe Zählerwechsel zwischen " + startDate.toString() + " und " + endDate.toString());
            rs = stmt.executeQuery("select LastValue, FirstValue, Time from " + MoniSoftConstants.COUNTERCHANGE_TABLE + " where " + MoniSoftConstants.COUNTERCHANGE_SENSORID + "=" + sensorID + " and " + MoniSoftConstants.COUNTERCHANGE_TIME + " >= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(startDate) + "' and " + MoniSoftConstants.COUNTERCHANGE_TIME + " <= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(endDate) + "'");
//            System.out.println("select LastValue, FirstValue, Time from " + MoniSoftConstants.COUNTERCHANGE_TABLE + " where " + MoniSoftConstants.COUNTERCHANGE_SENSORID + "=" + sensorID + " and " + MoniSoftConstants.COUNTERCHANGE_TIME + " >= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(startDate) + "' and " + MoniSoftConstants.COUNTERCHANGE_TIME + " <= '" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(endDate) + "'");
            if (rs.next()) { // es wurde ein Zählerwechsel gefunden
                Double change1 = rs.getDouble(1);
                Double change2 = rs.getDouble(2);
                Date changeTime = rs.getTimestamp(3);
//                System.out.println("Found: " + change1 + " " + change2 + "\t" + changeTime.toString() + "\t" + v1 + "\t" + v2);
                if (change1 != null && change2 != null && change1 >= v1 && change2 <= v2) {
                    // alles ok: Gesamtdifferenz für das Intervall berechnen
                    value = IntervalValue.withCounterChangeParameters(change1, v1, v2, change2);
                    //value = IntervalValue.forValue((change1 - v1) + (v2 - change2));
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("COUNTER_CHANGE") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FOR") + " " + SensorInformation.getDisplayName(sensorID) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("ON") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(changeTime) + "\n", true);
                    remarks.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("COUNTER_CHANGE") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("ON") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(changeTime) + "\n");
                } else { // Wechselwerte eingetragen aber unplausibel
                    value = new IntervalValue(null, false);
                }
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return value;
    }

    /**
     * Return the log type of the given logID
     *
     * @param id The logID
     * @return The type
     */
    public synchronized int getLogEventType(Integer id) {
        int type = -1;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select EventType from T_Log where id_Log=" + id + " limit 1");
            if (rs.next()) {
                type = rs.getInt(1);
            }

        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return type;
    }

    /**
     * Return the sensor id of this {@link DataHandler}
     *
     * @return The sensorID
     */
    public int getSensorID() {
        return sensorID;
    }

    /**
     * Set the sensor id for this {@link DataHandler}
     *
     * @param The sensorID
     */
    public void setSensorID(int id) {
        sensorID = id;
    }

    /**
     * Retrieve any remarks generated my this {@link DataHandler}
     *
     * @return The list of remarks
     */
    public ArrayList<String> getRemarks() {
        return remarks;
    }

    /**
     * Invalidates a Object
     *
     * @param n The object
     */
    private void consume(Integer n) {
        n = null;
    }
}
