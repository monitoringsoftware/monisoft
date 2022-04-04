/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

/**
 *
 * @author togro
 */
public class WeatherManager {

    
    /**
     * Liefert zu einem Wettermesspunkt den zugehörigen Sensor. Zulässige Werte
     * für den Typ sind:<br>
     * <code>
     *  &nbsp;MoniSoftConstants.WEATHER_OUTSIDE_TEMPERATURE<br>
     *  &nbsp;MoniSoftConstants.WEATHER_OUTSIDE_HUMIDITY<br>
     *  &nbsp;MoniSoftConstants.WEATHER_RAIN_STATUS<br>
     *  &nbsp;MoniSoftConstants.WEATHER_RAIN_AMOUNT<br>
     *  &nbsp;MoniSoftConstants.WEATHER_CARBON_DIOXIDE<br>
     *  &nbsp;MoniSoftConstants.WEATHER_WIND_DIRECTION<br>
     *  &nbsp;MoniSoftConstants.WEATHER_WIND_SPEED<br>
     *  &nbsp;MoniSoftConstants.WEATHER_GLOBAL_RADIATION
     * </code>
     *
     * @param type Wetterdatentyp
     * @return Der dem Wetterdatentyp zugeordnete Messpunkt
     */
    public static SensorProperties getAssociatedSensor(String type, Integer buildingID) {
        SensorProperties props = null;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select T_Sensors_id_Sensors from " + MoniSoftConstants.WEATHERDEF_TABLE + " where category= ? and T_Building_id_Building = ?");
            stmt.setString(1, type);
            stmt.setInt(2, buildingID);
            rs = stmt.executeQuery();
            if (rs.next()) {
                props = SensorInformation.getSensorProperties(rs.getInt(1));
            }
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return props;
    }

    /**
     * Schreibt den zu einem Wettermesspunkt zugehörigen Sensor in die
     * Datenbank. Wenn der übergebene Messpunkt
     * <code>null</code> ist oder keine Instanz von
     * <code>SensorProperties</code> ist wird
     * <code>null</code> in die Datenbank eingetragen. <p>Zulässige Werte für
     * den Typ sind:<br>
     * <code>
     *  &nbsp;MoniSoftConstants.WEATHER_OUTSIDE_TEMPERATURE<br>
     *  &nbsp;MoniSoftConstants.WEATHER_OUTSIDE_HUMIDITY<br>
     *  &nbsp;MoniSoftConstants.WEATHER_RAIN_STATUS<br>
     *  &nbsp;MoniSoftConstants.WEATHER_RAIN_AMOUNT<br>
     *  &nbsp;MoniSoftConstants.WEATHER_CARBON_DIOXIDE<br>
     *  &nbsp;MoniSoftConstants.WEATHER_WIND_DIRECTION<br>
     *  &nbsp;MoniSoftConstants.WEATHER_WIND_SPEED<br>
     *  &nbsp;MoniSoftConstants.WEATHER_GLOBAL_RADIATION
     * </code>
     *
     * @param type Wetterdatentyp
     * @param props <code>SensorProperties</code> des Messpunkts
     */
    public static void writeAssociatedSensor(String type, SensorProperties props, Integer buildingID) {
        Connection myConn = null;
        PreparedStatement stmt = null;
        Integer id;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.WEATHERDEF_TABLE + " set T_Sensors_id_Sensors= ?, T_Building_id_Building= ?, category= ? on duplicate key update T_Sensors_id_Sensors= ?");
            if (props != null) {
                id = props.getSensorID();
                stmt.setInt(1, id);
                stmt.setInt(4, id);
            } else {
                id = null;
                stmt.setNull(1, Types.INTEGER);
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(2, buildingID);
            stmt.setString(3, type);

            stmt.executeUpdate();
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("WeatherManager.MISSINGPERMISSIONS"));
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    /**
     *
     * @param sensorID
     */
    public static Integer removeSensor(Integer sensorID) {
        Integer i = 0;
        Integer replaceID = null;
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
//            System.out.println("update " + MoniSoftConstants.WEATHERDEF_TABLE + " set T_Sensors_id_Sensors=" + replaceID + " where T_Sensors_id_Sensors=" + sensorID);
            i = stmt.executeUpdate("update " + MoniSoftConstants.WEATHERDEF_TABLE + " set T_Sensors_id_Sensors=" + replaceID + " where T_Sensors_id_Sensors=" + sensorID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("WeatherManager.MISSINGPERMISSIONS"));
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return i;
    }
}
