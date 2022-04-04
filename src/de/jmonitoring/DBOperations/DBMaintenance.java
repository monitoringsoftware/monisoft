package de.jmonitoring.DBOperations;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class contains methods to check database consistency
 *
 * @author togro
 */
public class DBMaintenance {

    private static ch.qos.logback.classic.Logger logger;

    /**
     * Constructor
     */
    public DBMaintenance() {
        logger = MoniSoft.getInstance().getLogger();
    }

    /**
     * Optimize db
     */
    public void optimizeDB() {
//        removeUnusedDatetimes(); // TODO reimplement
    }

    /**
     * Check database consistency
     */
    public void consistencyCheck() {
        boolean error = false;
        String successString = java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.OHNE FEHLER");
        int invalidEntrys = 0;

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.RUNNING") + "\n", true);

        Messages.showMessage("1/4) " + "Default-Log-Test ...." + "\n", true);
        if (!hasDefaultLogEntry()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.NOLOGID") + "\n", true);
            if (fixLog()) {
                Messages.showMessage("     " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.CORRECTED") + "\n", true);
            } else {
                Messages.showMessage("     " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.NOCORRECTION") + "\n", true);
            }
            error = true;
        }

        Messages.showMessage("2/4) " + "LogID-Test ...." + "\n", true);
        invalidEntrys = checkLogIDs();
        if (invalidEntrys > 0) {
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.INVALIDLOGIDS") + "\n", new Object[]{invalidEntrys}), true);
            // TODO FIX:  Diese LogIDs auf 0 setzten
            error = true;
        }

        Messages.showMessage("3/4) " + "TimeStamp-Test ...." + "\n", true);
        invalidEntrys = checkTimeStampRange();
        if (invalidEntrys > 0) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("WARNING") + invalidEntrys + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("INVALID_DATES") + "\n", true);
            // TODO FIX:  Einträge löschen ?
            error = true;
        }

        Messages.showMessage("4/4) " + "SensorID-Test ...." + "\n", true);
        invalidEntrys = checkInvalidSensorIDs();
        if (invalidEntrys > 0) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("WARNING") + invalidEntrys + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("INVALID_ENTRY") + "\n", true);
            // TODO: IDS auflisten??? Nutzer sollte manuell eingreifen
            error = true;
        }

        if (error) {
            successString = java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.MIT WARNUNGEN");
        }

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.CONSISTENYCHECK") + " " + successString + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.BEENDET") + "\n", true);
//        removeUnusedDatetimes();
    }

    /**
     * Optimize T_History
     */
    public void optimizeHistory() {
        Connection myConn = null;
        Statement stmt = null;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.OPTIMIERE HISTORY-TABELLE"), true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("optimize table " + MoniSoftConstants.HISTORY_TABLE);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.BEENDET") + "\n", true);
    }

    /**
     * Optimize T_Events
     */
    public void optimizeEvents() {
        Connection myConn = null;
        Statement stmt = null;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.OPTIMIERE EVENT-TABELLE"), true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("optimize table " + MoniSoftConstants.EVENT_TABLE);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.BEENDET") + "\n", true);
    }

    /**
     * Check if the current database has any tables
     *
     * @return <code>true</code> if there are tables
     */
    public boolean isEmpty() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            stmt.executeQuery("show tables");
            rs = stmt.getResultSet();
            while (rs.next()) {
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return true;
    }

    /**
     * Checks if the default log entry is present.<p> For correct operation it
     * is mandatory that a entry with 0 log id exists
     *
     * @return <code>true</code> if the entry exists
     */
    public boolean hasDefaultLogEntry() {
        boolean hasEntry = false;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select * from T_Log where id_Log=0");
            if (rs.first()) {
                hasEntry = true;
            }
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return hasEntry;
    }

    /**
     * Check for invalid log ids
     *
     * @return The number of invalid log ids
     */
    public int checkLogIDs() {
        int entryCount = 0;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT count(*) FROM T_History where  T_Sensors_id_Sensors > 0 and T_Sensors_id_Sensors = T_Log_id_log");
            rs.next();
            entryCount = rs.getInt(1);
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return entryCount;
    }

    /**
     * Check for dates of unplausible nature (befor 2000 and after 2050)
     *
     * @return The number of unplausible dates
     */
    public int checkTimeStampRange() {
        int entryCount = 0;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT count(*) FROM T_History where TimeStamp < unix_timestamp('2000-01-01') or TimeStamp > unix_timestamp('2050-01-01')");
            rs.next();
            entryCount = rs.getInt(1);
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return entryCount;
    }

    /**
     * Check for enties in T_History that do not belong to a sensor in the
     * sensor list
     *
     * @return The number of orphant entries
     */
    public int checkInvalidSensorIDs() {
        int entryCount = 0;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT count(*) FROM T_History where not T_Sensors_id_Sensors IN (select id_Sensors from T_Sensors)");
            rs.next();
            entryCount = rs.getInt(1);
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return entryCount;
    }

    /**
     * Fixes the log tabe if the default 0-entry is missing
     *
     * @return <code>true</code> if the fix was successful
     */
    public boolean fixLog() {
        boolean success = false;
        Connection myConn = null;
        Statement stmt = null;
        int result = 0;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            result += stmt.executeUpdate("INSERT INTO T_Log (id_Log,Description,Time,EventType,Value,User) VALUES (0,'default','0000-00-00',4,0,'default')");
            result += stmt.executeUpdate("UPDATE IGNORE T_Log SET id_Log = 0 WHERE EventType=4");
            if (result >= 1) {
                success = true;
                try { // Abfangen wenn von Kommandozeile
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("CORRECTED_MISSING_ID") + "\n", true);
                    logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.FEHLENDE DEFAULT-LOG-ID KORRIGIERT"));
                } catch (Exception e) {
                }
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return success;
    }
}
