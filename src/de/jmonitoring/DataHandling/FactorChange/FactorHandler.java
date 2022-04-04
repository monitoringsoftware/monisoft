package de.jmonitoring.DataHandling.FactorChange;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * This class handles the database management for factor chnages
 *
 * @author togro
 */
public class FactorHandler {

    private int sensorID;
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Create a new handler for the given sensor
     *
     * @param id The sensor
     */
    public FactorHandler(int id) {
        sensorID = id;
    }

    /**
     * Inserts a new factor change to the database. If a change is already
     * existing at the given time, the factor is updated.
     *
     * @param time The time of the factor change
     * @param newfactor The new factor
     * @param force If <code>true</code> an existing factor will be updated
     * without user query
     */
    public synchronized void addOrUpdateFactor(Date time, Double newfactor, boolean force) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        String query;
        String action;

        // Prüfen ob schon ein Faktor zu diesem Zeitpunkt existiert
        Double testfactor = getFactor(time);
        if (testfactor != null) { // es gibt bereits einen Faktor in der DB
            if (force || JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("EXIST1") + " " + testfactor + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("EXIST2") + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("OVERWRITEFACTOR"), "Faktorersetzung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                query = "update " + MoniSoftConstants.FACTORS_TABLE + " set Value=" + newfactor + " where T_Sensors_id_Sensors=" + sensorID + " and Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "'";
                action = java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGED");
            } else {
                return;
            }
        } else {
            query = "insert into " + MoniSoftConstants.FACTORS_TABLE + " set Value=" + newfactor + ",Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "',T_Sensors_id_Sensors=" + sensorID;
            action = java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("ADDED");
        }
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate(query);
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FACTOR_FOR_SENSOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + action + ": " + newfactor + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FROM") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(time));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    /**
     * Remove the factor at the given time
     *
     * @param time The time
     * @return The number of deleted entries
     */
    public synchronized Integer removeFactor(Date time) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Integer i = 0;
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
//            System.out.println("delete from " + MoniSoftConstants.FACTORS_TABLE + " where " + MoniSoftConstants.FACTOR_SENSOR_ID + "=" + sensorID + " and Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "'");
            i = stmt.executeUpdate("delete from " + MoniSoftConstants.FACTORS_TABLE + " where " + MoniSoftConstants.FACTOR_SENSOR_ID + "=" + sensorID + " and Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "'");
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FACTORS_DELETED_FOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETED_AT") + " " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(time));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return i;
    }

    /**
     * Delete all factors for the sensor of this handler
     *
     * @return The number of deleted entries
     */
    public synchronized Integer removeAllFactors() {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Integer i = 0;
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            i = stmt.executeUpdate("delete from " + MoniSoftConstants.FACTORS_TABLE + " where " + MoniSoftConstants.FACTOR_SENSOR_ID + "=" + sensorID);
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FACTORS_DELETED_FOR") + " " + SensorInformation.getDisplayName(sensorID) + " (" + sensorID + ") " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DELETED"));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return i;
    }

    /**
     * Queries the factor that is active for the currecnt sensor at the given
     * time
     *
     * @param time The time
     * @return The factor that is valid at the time
     */
    public synchronized Double getFactor(Date time) {
        Double value = null;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeQuery("select Value from " + MoniSoftConstants.FACTORS_TABLE + " where T_Sensors_id_Sensors= " + sensorID + " and Time='" + new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).format(time) + "'");
            rs = stmt.getResultSet();
            if (rs.next()) {
                value = rs.getDouble(1);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return value;
    }
}
