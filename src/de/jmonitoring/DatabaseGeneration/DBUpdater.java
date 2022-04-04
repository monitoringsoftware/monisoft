package de.jmonitoring.DatabaseGeneration;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import java.sql.Connection;
import java.sql.Statement;
import java.text.DecimalFormat;
import javax.swing.JOptionPane;

/**
 * This class contains the update mathods wich invokes an update of the database.<p>
 * The file in which the update SQl commands must be is <code>update.sql</code> in this package.
 * 
 * The recommended DB-Version is set in <code>MoniSoft.java</code> in the base package.<br>
 * The DB-Version of the database is stored in table T_Config.
 * 
 * @author togro
 */
public class DBUpdater {

    public DBUpdater() {
    }

    
    /**
     * Invokes an update to the database depending on the recommended DB Version
     * @return true if the update was successfull
     */
    public boolean update() {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return false;
        }
        boolean success;
        DecimalFormat format = new DecimalFormat("0.0");
        DecimalFormat formatNeu = new DecimalFormat("0.00");
        String updateFile;
        String from; 
        String to;
        float version = MoniSoft.getInstance().getDBConnector().getDBVersion();
        // Update
        if (version < 1.2f) {
            updateFile = "update_to_12.sql";
            from = "< 1.2";
            to = "1.2";
        } else if (version == 2.11f) {
            JOptionPane.showMessageDialog(null, "Die Datenbank ist auf dem neuesten Stand (" + version + ") und benötigt kein Update.");
            return true;
        } else if (version >= 1.2f && version < 2.9f) {
            updateFile = "update.sql";
            from = format.format(version);
            to = format.format(MoniSoft.getInstance().recommendedLastDBVersion);
        } else if (version == 2.9f) {
            // AZ: check the priviliges for the MySql-User
            if( !priviligesOk() )
                return false;
            updateFile = "update_to_2_11.sql";
            from = format.format(version);
            to = formatNeu.format(MoniSoft.getInstance().recommendedDBVersion);            
        } else {
            JOptionPane.showMessageDialog(null, "Unbekannte Datenbankversion (" + version + "). Breche ab.");
            return true;
        }

        if (JOptionPane.showConfirmDialog(null, "<html><body>Die bestehende Datenbank wird von Version" + " " + from + " " + "auf" + " " + to + " " + "aktualisiert.<p>&nbsp;</p>Die Funktion sollte nur nach expliziter Aufforderung verwendet werden!<br>Zur Sicherheit bitte ein Backup der vorhandenen Daten anlegen!<p>Dies kann einige Minuten dauern!<p>&nbsp;</p><b>MoniSoft wird während der Aktualisierung nicht reagieren.<br>Bitte beenden Sie die Anwendung nicht bis der Vorgang abgeschlossen ist!</b><p>&nbsp;</p>Fortfahren?</html></body>", "Frage", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
            success = false;
        } else {
            Connection myConn = null;
            try {
                myConn = DBConnector.openConnection();
                ScriptRunner sr = new ScriptRunner(myConn, false, false);
                sr.runScript(Resources.getResourceAsReader("de/jmonitoring/DatabaseGeneration/" + updateFile));
                success = true;
                MoniSoft.getInstance().getLogger().info("Datenbankupdate auf Version" + " " + to);
            } catch (Exception ex) {
                success = false;
                Messages.showException(ex);                
            } finally {
                DBConnector.closeConnection(myConn, null, null);
            }
        }
        return success;
    }
    
    /**
     * Rechteprüfung, durch Ausführung eines ALTER TABLE-Skriptes
     * @return true if the actual db-user has the privilige to alter the t_sensors-table
     */
    public boolean priviligesOk()
    {        
        boolean success = false;
        Connection myConn = null;
        try {
            myConn = DBConnector.openConnection();
            Statement stmt = null;
            stmt = myConn.createStatement();
            stmt.executeUpdate("ALTER TABLE `T_Sensors` ADD COLUMN `test` int(11) DEFAULT '0'");
            stmt.executeUpdate("ALTER TABLE `T_Sensors` DROP COLUMN `test`");
            success = true;
            MoniSoft.getInstance().getLogger().info("Rechteprüfung erfolgreich durchgeführt");
        } catch (MySQLSyntaxErrorException ex) {
            success = false;
            Messages.showException(ex);
            String meldung = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("DBUpdate.privilegeError");            
            JOptionPane.showMessageDialog(null, meldung + " " + ex.getMessage() );
        } catch (Exception ex) {
            success = false;
            Messages.showException(ex);            
        } finally {
            DBConnector.closeConnection(myConn, null, null);
        }
        return success;
    }
}
