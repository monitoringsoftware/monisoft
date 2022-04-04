package de.jmonitoring.DataHandling.CounterChange;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DataHandling.FactorChange.FactorHandler;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class manages the database entries for counter changes
 *
 * @author togro
 */
public class CounterChangeHandler {

    private final ch.qos.logback.classic.Logger logger;

    /**
     * Create a new instance of CounterChangeHandler
     */
    public CounterChangeHandler() {
        logger = MoniSoft.getInstance().getLogger();
    }

    /**
     * Adds a counter change or, if another counter chnage exists at the time,
     * updates the database
     *
     * @param change The {@link CounterChange}
     * @param sensorID The sensor
     */
    public boolean addOrUpdateCounterChange(CounterChange change, Integer sensorID) {
        boolean success = false;
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return false;
        }
        Connection myConn = null;
        Statement stmt = null;
        FactorHandler fh = new FactorHandler(sensorID);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            int i = stmt.executeUpdate("replace into " + MoniSoftConstants.COUNTERCHANGE_TABLE + " set LastValue=" + change.getOldValue() + ",FirstValue=" + change.getNewValue() + ",Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(change.getTime()) + "', T_Sensors_id_Sensors=" + sensorID);

            if (i == 1) {
                success = true;
                // der Wandlungsfaktor wird, sofern verändert in die Liste der Faktoren aufgenommen
                fh.addOrUpdateFactor(change.getTime(), change.getFactor(), true);
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("COUNTERCHANGE_FOR_SENSOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") eingetragen für " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(change.getTime()) + "\n", true);
                logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("COUNTERCHANGE_FOR_SENSOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") eingetragen für " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(change.getTime()));
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return success;
    }

    /**
     * Delete the counter chnage at the given time from the database
     *
     * @param time The time at which the counter change is
     * @param sensorID The sensor
     */
    public void removeCounterChange(Date time, Integer sensorID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        FactorHandler fh = new FactorHandler(sensorID);

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.COUNTERCHANGE_TABLE + " where Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "' and T_Sensors_id_Sensors=" + sensorID);
            fh.removeFactor(time);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("COUNTERCHANGE_FOR_SENSOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("DELETED_FOR") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(time) + "\n", true);
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("COUNTERCHANGE_FOR_SENSOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("DELETED_FOR") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(time));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    /**
     * Delete all counter chnages of the given sensor from the database
     *
     * @param sensorID The sensor
     * @return The number of deleted changes
     */
    public Integer removeCounterChanges(Integer sensorID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Integer i = 0;
        Connection myConn = null;
        Statement stmt = null;
        FactorHandler fh = new FactorHandler(sensorID);

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            i = stmt.executeUpdate("delete from " + MoniSoftConstants.COUNTERCHANGE_TABLE + " where T_Sensors_id_Sensors=" + sensorID);
            fh.removeAllFactors();
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("ALL_CHANGES_FOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("DELETED"));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return i;
    }

    /**
     * Return a {@link CounterChange} if one is found between to points in time
     *
     * @param sensorID The sensor
     * @param from The begin of the search interval
     * @param to The end of the search interval
     * @return The set of found {@link CounterChange}s
     */
    public ArrayList<CounterChange> getCounterChangesFor(Integer sensorID, long from, long to) {
        ArrayList<CounterChange> changes = getCounterChanges(sensorID);
        ArrayList<CounterChange> result = new ArrayList<CounterChange>();
        for (CounterChange change : changes) {
            long timeOfChange = change.getTime().getTime();
            if (timeOfChange >= from && timeOfChange <= to) {
                result.add(change);
            }
        }
        return result;
    }

    /**
     * Return a {@link CounterChange} if one is found between to points in time
     *
     * @param changes The counterChnages if already known
     * @param from The begin of the search interval
     * @param to The end of the search interval
     * @return The set of found {@link CounterChange}s
     */
    public ArrayList<CounterChange> getCounterChangesFor(ArrayList<CounterChange> changes, long from, long to) {
        ArrayList<CounterChange> result = new ArrayList<CounterChange>();
        for (CounterChange change : changes) {
            long timeOfChange = change.getTime().getTime();
            if (timeOfChange >= from && timeOfChange <= to) {
                result.add(change);
            }
        }
        return result;
    }

    /**
     * Retrieves a list af all counter chnages for the given sensor from the
     * database
     *
     * @return A set of {@link CounterChange}s
     */
    public ArrayList<CounterChange> getCounterChanges(Integer sensorID) {
        ArrayList<CounterChange> ccHash = new ArrayList<CounterChange>();
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Timestamp time;
        Double lastValue, firstValue, factor;
        FactorHandler fh = new FactorHandler(sensorID);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select * from " + MoniSoftConstants.COUNTERCHANGE_TABLE + " where " + MoniSoftConstants.COUNTERCHANGE_SENSORID + "=" + sensorID + " order by " + MoniSoftConstants.COUNTERCHANGE_TIME);
            while (rs.next()) {
                time = rs.getTimestamp(MoniSoftConstants.COUNTERCHANGE_TIME);
                lastValue = rs.getDouble(MoniSoftConstants.COUNTERCHANGE_LASTVALUE);
                firstValue = rs.getDouble(MoniSoftConstants.COUNTERCHANGE_FIRSTVALUE);
                if (fh.getFactor(time) == null) {
                    factor = 1.0;
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("THE_CHANGE_FOR") + "  " + SensorInformation.getDisplayName(sensorID) + " " + "am" + " " + time.toString() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("NO_FACTOR_ASSUME_ZERO"), true);
                } else {
                    factor = fh.getFactor(time);
                }
                ccHash.add(new CounterChange(time, lastValue, firstValue, factor, sensorID));
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return ccHash;
    }

    /**
     * Retrieve a list of all counter chaages in the databse.<p> Creates a Map
     * with the corresponding sensor id as key
     *
     * @return A map of all {@link CounterChange}s
     */
    public HashMap<Integer, HashSet<CounterChange>> getAllCounterChanges() {
        HashMap<Integer, HashSet<CounterChange>> map = new HashMap<Integer, HashSet<CounterChange>>();
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Timestamp time;
        Double lastValue, firstValue, factor;
        FactorHandler fh;
        Integer sensorID;
        HashSet<CounterChange> internalList;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select * from " + MoniSoftConstants.COUNTERCHANGE_TABLE);
            while (rs.next()) {
                time = rs.getTimestamp(MoniSoftConstants.COUNTERCHANGE_TIME);
                lastValue = rs.getDouble(MoniSoftConstants.COUNTERCHANGE_LASTVALUE);
                lastValue = rs.wasNull() ? null : lastValue;
                firstValue = rs.getDouble(MoniSoftConstants.COUNTERCHANGE_FIRSTVALUE);
                firstValue = rs.wasNull() ? null : firstValue;
                sensorID = rs.getInt(MoniSoftConstants.COUNTERCHANGE_SENSORID);
                fh = new FactorHandler(sensorID);
                if (fh.getFactor(time) == null) {
                    factor = 1.0;
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("THE_CHANGE_FOR") + "  " + SensorInformation.getDisplayName(sensorID) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("AT") + " " + time.toString() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/CounterChange/Bundle").getString("NO_FACTOR_ASSUME_ZERO") + "\n", true);
                } else {
                    factor = fh.getFactor(time);
                }
                internalList = map.get(sensorID) != null ? map.get(sensorID) : new HashSet<CounterChange>();
                internalList.add(new CounterChange(time, lastValue, firstValue, factor, sensorID));
                map.put(sensorID, internalList);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return map;
    }
}
