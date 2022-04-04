/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.SensorCategoryHandling;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.MoniSoft;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;

/**
 *
 * @author togro
 */
public class NestedSetTransfer {

    private static ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    public static void writeNestedSetToDB(TreeMap<Integer, NestedSetElement> map, String categorySet) {
        if (map.isEmpty()) {
            return;
        }

        Connection myConn = null;
        PreparedStatement stmt = null;

        Iterator<Integer> it = map.keySet().iterator();
        String entry;

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("NestedSetTransfer.WRITING") + " ...\n", true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("delete from " + MoniSoftConstants.CATEGORY_TABLE + " where " + MoniSoftConstants.SET + "= ?");
            stmt.setString(1, categorySet);
            stmt.executeUpdate();
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.CATEGORY_TABLE + " set " + MoniSoftConstants.NODE + "= ?," + MoniSoftConstants.LFT + "= ?," + MoniSoftConstants.RGT + "= ? ," + MoniSoftConstants.SET + "= ?");

            while (it.hasNext()) {
                NestedSetElement element = map.get(it.next());
                entry = convertSensorNodeToID(element);
                stmt.setString(1, entry);
                stmt.setInt(2, element.getLeft());
                stmt.setInt(3, element.getRight());
                stmt.setString(4, categorySet);
                stmt.executeUpdate();
            }
            logger.info("Modified sensor categories.");
        } catch (MySQLSyntaxErrorException ex) {
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    public static TreeMap<Integer, NestedSetElement> readNestedSetFromDB(String categorySet) {
        TreeMap<Integer, NestedSetElement> map = new TreeMap<Integer, NestedSetElement>();
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int lft, rgt;
        String node;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.LFT + "," + MoniSoftConstants.RGT + "," + MoniSoftConstants.NODE + " from " + MoniSoftConstants.CATEGORY_TABLE + " where " + MoniSoftConstants.SET + "= ?");
            stmt.setString(1, categorySet);
            rs = stmt.executeQuery();
            while (rs.next()) {
                lft = rs.getInt(1);
                rgt = rs.getInt(2);
                node = rs.getString(3);
                map.put(lft, new NestedSetElement(convertEntryToNodeName(node), lft, rgt));
            }

            if (map.keySet().isEmpty()) {
                writeAnchorCategory();
                map.put(1, new NestedSetElement(convertEntryToNodeName(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("NestedSetTransfer.CATEGORIES")), 1, 2));
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return map;
    }

    private static String convertSensorNodeToID(NestedSetElement e) {
        String entry;
        String name = e.getName().split("\u2015")[0];
        // Prüfen, ob es sich um einen Messpunkt handelt
        if (SensorInformation.getSensorIDFromNameORKey(name) == -1 && SensorInformation.getSensorIDFromNameORKey(name) == -1) {
            // Nein! Es ist eine Kategorie: komplett zurückgeben
            entry = "|" + e.getName();
        } else {
            // Ja! Die ID als String liefern. Berücksichtigt ob es ein key oder der Name des Messpunktes ist
            if (SensorInformation.getSensorIDFromNameORKey(name) == -1) {
                entry = Integer.toString(SensorInformation.getSensorIDFromNameORKey(name));
            } else {
                entry = Integer.toString(SensorInformation.getSensorIDFromNameORKey(name));
            }
        }
        return entry;
    }

    private static String convertEntryToNodeName(String entry) {
        String node;
        try {
            int sensorID = Integer.parseInt(entry);
            // Prüfen, ob es einen Messpunkt mit dieser ID gibt:
            if (SensorInformation.isSensor(sensorID)) {
                node = SensorInformation.getDisplayName(sensorID) + "\u2015" + SensorInformation.getSensorProperties(sensorID).getSensorDescription();
            } else {
                // es ist kein Messpunkt -> Kategorie zurückgeben
                node = entry + " " + NestedSet.POSSIBLY_DELETED;
            }
        } catch (NumberFormatException e) { // Bei Exception war keine Zahl in der DB -> also keine ID
            node = entry.replaceFirst("\\|", "");
        }
        return node;
    }

    private static void writeAnchorCategory() {
        Connection myConn = null;
        PreparedStatement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.CATEGORY_TABLE + " (" + MoniSoftConstants.LFT + "," + MoniSoftConstants.RGT + "," + MoniSoftConstants.NODE + "," + MoniSoftConstants.SET + ") values (1,2,?,'KAT')");
            stmt.setString(1, java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("NestedSetTransfer.CATEGORIES"));
            stmt.executeUpdate();
        } catch (MySQLSyntaxErrorException ex) {
            logger.error(ex.getMessage());
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }
}
