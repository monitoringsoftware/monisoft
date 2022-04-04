/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.base.sensors;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.WeatherCalculation.WeatherManager;
import de.jmonitoring.DataHandling.CounterChange.CounterChangeHandler;
import de.jmonitoring.DataHandling.DataHandler;
import de.jmonitoring.DataHandling.FactorChange.FactorHandler;
import de.jmonitoring.DataHandling.MonthlyUsageCalculator;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to fetch information on a sensor
 *
 * @author togro
 */
public class SensorInformation {

    private static ArrayList<SensorProperties> sensorList = null;
    private static String finalDefinition = "";
    public static final int NUM_DELETED_FACTORS = 0;
    public static final int NUM_DELETED_CHANGES = 1;
    public static final int NUM_DELETED_MONTHLY = 2;
    public static final int NUM_DELETED_COLLECTIONS = 3;
    public static final int NUM_DELETED_WEATHER = 4;
    public static final int NUM_DELETED_DATA = 5;
    private static ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Liest für einen Messwert aus der Datenbank alle Faktoren aus und die Zeit
     * ab der sie gelten
     *
     * @param sensorID
     * @return <code>TreeMap<Long, Double></code> der Faktoren und deren
     * Gültigkeitsbeginn
     */
    public static TreeMap<Long, Double> getFactorList(int sensorID) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        TreeMap<Long, Double> factorMap = new TreeMap<Long, Double>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.FACTOR_TIME + "," + MoniSoftConstants.FACTOR_VALUE + " from " + MoniSoftConstants.FACTORS_TABLE + " where " + MoniSoftConstants.FACTOR_SENSOR_ID + " = " + sensorID + " order by " + MoniSoftConstants.FACTOR_TIME);
            while (rs.next()) {
                factorMap.put(new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).parse(rs.getString(1)).getTime(), rs.getDouble(2));
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (ParseException pe) {
            Messages.showException(pe);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return factorMap;
    }

    /**
     * Liest für einen Messwert aus der Datenbank alle Einträge für Faktoren aus
     */
    public static ArrayList<String> getFactorListEntrys(int sensorID) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> entrys = new ArrayList<String>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select date_format(" + MoniSoftConstants.FACTOR_TIME + ",'%d.%m.%Y %T')," + MoniSoftConstants.FACTOR_VALUE + " from " + MoniSoftConstants.FACTORS_TABLE + " where " + MoniSoftConstants.FACTOR_SENSOR_ID + " = " + sensorID + " order by " + MoniSoftConstants.FACTOR_TIME);
            while (rs.next()) {
                entrys.add(rs.getString(1) + ", " + rs.getDouble(2));
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return entrys;
    }

//    /**
//     * Liefert die SENSOR_ID des übergebenen Messpunkts, -1 wenn es keinen
//     * Sensor mit em entsprechenden Namen gibt
//     *
//     * @param sensor
//     */
//    public static int getSensorIDFromName(String sensorString) {
//        String sensor = sensorString.split("\u2015")[0].split("@")[0];
//        String building = null;
//        try {
//            building = sensorString.split("\u2015")[0].split("@")[1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            // no error if there i no @
//        }
//        int id = -1;
//        for (SensorProperties props : sensorList) {
//            if (props != null && props.getSensorName().equals(sensor) && ((building == null || building.isEmpty() || props.getBuildingName().equals(building) || building == null))) {
//                id = props.getSensorID();
//                break;
//            }
//        }
//        return id;
//    }
//    /**
//     * Liefert die SENSOR_ID des übergebenen Messpunkts, -1 wenn es keinen
//     * Sensor mit em entsprechenden Key gibt
//     *
//     * @param sensor
//     */
//    public static int getSensorIDFromKey(String keyString) {
//        String sensor = keyString.split("\u2015")[0].split("@")[0];
//        String building = null;
//        try {
//            building = keyString.split("\u2015")[0].split("@")[1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//        }
//        
//        int id = -1;
//        for (SensorProperties props : sensorList) {
//            if (props != null && props.getKeyName().equals(sensor) && ((building == null || building.isEmpty() || props.getBuildingName().equals(building) || building == null))) {
//                id = props.getSensorID();
//                break;
//            }
//        }
//        return id;
//    }
    /**
     * Liefert die SENSOR_ID des übergebenen Messpunkts, -1 wenn es keinen
     * Sensor mit em entsprechenden Key gibt
     *
     * @param sensor
     */
    public static int getSensorIDFromNameORKey(String sensorString) {
        String sensor = sensorString.split("\u2015")[0].split("@")[0];
        String building = null;
        try {
            building = sensorString.split("\u2015")[0].split("@")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            // no error if there i no @
        }
        int id = -1;
        for (SensorProperties props : sensorList) {
            if (props != null && (props.getSensorName().equals(sensor) || props.getKeyName().equals(sensor)) && ((building == null || building.isEmpty() || props.getBuildingName().equals(building) || building == null))) {
                id = props.getSensorID();
                break;
            }
        }
        return id;

//        
//        String sensor = sensorString.split("\u2015")[0];
//        sensor = sensor.split("@")[0];
//        int id = -1;
//        for (SensorProperties props : sensorList) {
//            if (props != null && !sensor.isEmpty() && (props.getKeyName().equals(sensor) || props.getSensorName().equals(sensor))) {
//                id = props.getSensorID();
//            }
//        }
//        return id;
    }

    public static boolean isSensor(Integer id) {
        if (SensorInformation.getSensorProperties(id) == null) { // es gibt keinen Messpunkt mit dieser ID
            return false;
        } else {
            return true;
        }
    }

    public static String getDisplayName(SensorProperties props) {
        String buildingPart = "";
        if (BuildingInformation.getBuildingList().size() > 1 && MoniSoft.getInstance().getApplicationProperties().getProperty("AddBuildingName").equals("1")) {
            buildingPart = "@" + props.getBuildingName();
        }
        if (props != null) {
            if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseSensorIDForDisplay").equals("1")) {
                return props.getKeyName() + buildingPart;
            } else {
                return props.getSensorName() + buildingPart;
            }
        } else {
            return null;
        }
    }

    public static String getDisplayName(int sensorID) {
        SensorProperties props = getSensorProperties(sensorID);
        return getDisplayName(props);
    }

    public static SensorProperties getSensorFromDisplayName(String name) {
        SensorProperties resultProps = null;
        // split at @
        String sensorPart = name.split("@")[0];
        String buildingPart = "";
        try {
            buildingPart = name.split("@")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        for (SensorProperties props : sensorList) {
            if (props.getSensorName().equals(sensorPart) && (buildingPart.isEmpty() || props.getBuildingName().equals(buildingPart))) {
                resultProps = props;
                break;
            }
        }
        return resultProps;
    }

    /**
     * Liefert die <link>SensorProperties</link> des übergebenen Messpunkts für
     * MesspunkID
     *
     * @param sensor
     */
    public static SensorProperties getSensorProperties(int id) {
        SensorProperties resultProps = null;
        for (SensorProperties props : sensorList) {
            if (props.getSensorID() == id) {
                resultProps = props;
                break;
            }
        }
        return resultProps;
    }

    public static void writeAllSensorProperties() {
        for (SensorProperties props : sensorList) {
            if (!props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
                writeSensorProperty(props.getSensorID());
            }
        }
    }

    /**
     * Schreibt die (aktuellen) SensorProps der übergebenen SensorID in die DB
     *
     * @param sensorID
     */
    public static void writeSensorProperty(int sensorID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        SensorProperties props = getSensorProperties(sensorID);
        writeSensorProperty(props.getSensorID(), props);
    }

    /**
     * Schreibt die übergebenen sensorProps für die angegebene sensorID
     *
     * @param sensorID
     * @param props
     */
    public static void writeSensorProperty(int sensorID, SensorProperties props) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        HashMap<String, Object> propertyMap = props.getPropertyList();

        String updateString = "update " + MoniSoftConstants.SENSOR_TABLE + " set ";
        String whereString = " where " + MoniSoftConstants.SENSOR_ID + "=" + sensorID;
        StringBuilder setString = new StringBuilder("");
        // Alle properties durchlaufen
        Object value;
        String separator = "";
        for (String key : propertyMap.keySet()) {
            if (!key.equals(MoniSoftConstants.SENSOR_ID) && !key.equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
                System.out.println("Keyx " + key);
                value = propertyMap.get(key);
                if (value != null && value.getClass().equals(String.class)) { // pack strings in ''
                    value = "'" + value + "'";
                }
                setString.append(separator);
                setString.append(MoniSoftConstants.SENSOR_TABLE);
                setString.append(".");
                setString.append(key);
                setString.append("=");
                setString.append(value);

                separator = ",";
            }
        }

        if (!setString.toString().isEmpty()) {
            try {
                myConn = DBConnector.openConnection();
                stmt = myConn.createStatement();
                stmt.executeUpdate(updateString + setString.toString() + whereString);
                logger.info(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("AKTUALISIERE MESSPUNKT {0} ({1})"), new Object[]{sensorID, props.getSensorName()}));
            } catch (MySQLSyntaxErrorException ex) {
                Messages.showException(ex);
                Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN AN DEN MESSPUNKTEN VORZUNEHMEN."));
            } catch (SQLException ex) {
                Messages.showException(ex);
            } catch (Exception ex) {
                Messages.showException(ex);
            } finally {
                DBConnector.closeConnection(myConn, stmt, null);
            }
        }
    }

    /**
     * Ermittelt die Gültigkeisspanne in Millisekunden abhängig ob Wochentag
     * oder Wochenende. NULL-Werte werden als 0 zurückgeliefert.
     *
     * @param sensorID
     * @param date
     * @return Zeitspanne in Millisekunden
     */
    public static Long getValidTimeSpanForDateInMillis(int sensorID, Date date) {
        Long validTime;
        Long[] validTimes = SensorInformation.getSensorProperties(sensorID).getMaxChangeTimes();
        if (DateTimeCalculator.isWorkDayLong(date.getTime())) {
            validTime = validTimes[0]; // Wochentags
        } else {
            validTime = validTimes[1];  // Wochenende
        }

        if (validTime == null) {
            validTime = 0L;
        }

        return validTime * 60000; // Sekunden in Millisekunden
    }

    /**
     * Liefert die zur Berechnug eines virtuellen Messpunktes nötigen Messpunkte
     *
     * @param definition Die Formel zur Berechnung des virtuellen Messpunktes
     * @return Liste der IDs der beteiligten Messpunkte
     */
    public static TreeSet<Integer> getVirtualComponents(String definition, String virtualName) {
        TreeSet<Integer> componentList = new TreeSet<Integer>();
        if (finalDefinition.isEmpty()) { // noch keine Definiation vorhanden
            finalDefinition = definition; // ansosnsten die Vorhandene holen "initialisieren"
        }

        try {
            Pattern p = Pattern.compile("\\[[^\\[\\]]*\\]");
            Matcher m = p.matcher(definition);
            String s;
            while (m.find()) {
                s = definition.substring(m.start() + 1, m.end() - 1);
                if (s.equals(virtualName)) {
                    Messages.showMessage("Circular reference in virtual definition! A sensor must not appear in it's own virtual formula!", true);
                    return null;
                }
                // System.out.println("s " + s + "    " + virtualName);
                componentList.add(getSensorIDFromNameORKey(s));
                if (getSensorIDFromNameORKey(s) == -1) {
                    if (virtualName == null) {
                        Messages.showOptionPane(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DER MESSPUNKT '{0}' IST NICHT BEKANNT.BITTE ÜBERPRÜFEN."), new Object[]{s}));
                    } else {
                        Messages.showOptionPane(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DER MESSPUNKT IST NICHT BEKANNT"), new Object[]{s, virtualName, definition}));
                    }
                    return null;
                } else { // Alle Komponenten existieren
                    if (getSensorProperties(getSensorIDFromNameORKey(s)).isVirtual()) { // sind darunter evtl. andere virtuelle Sensoren?
                        String innerDef = getSensorProperties(getSensorIDFromNameORKey(s)).getVirtualDefinition();
                        finalDefinition = finalDefinition.replace("[" + s + "]", innerDef);
                        TreeSet<Integer> innerList = getVirtualComponents(innerDef, virtualName);
                        componentList.addAll(innerList);
                        componentList.remove(getSensorIDFromNameORKey(s));
                    }
                }
            }
        } catch (StackOverflowError e) {
            Messages.showMessage("Circular reference in virtual definition! A sensor must not appear in it's own virtual formula!", true);
            return null;
        }
        return componentList;
    }
    
    public static String replaceVirtualDefinitionIds(String definition) {
        String tempDefinition = definition;
        if (finalDefinition.isEmpty()) { // noch keine Definiation vorhanden
            finalDefinition = definition; // ansonsten die Vorhandene holen "initialisieren"
        }

        for (SensorProperties props : sensorList)
        {   
            if (props != null && props.getSensorName() != null && definition != null )
            {
                String sensorNameWithBraces = "[" + props.getSensorName() + "]";
                
                if( definition.contains( sensorNameWithBraces ) )
                {
                    // Der Sensorname ist enthalten
                    tempDefinition = tempDefinition.replace( sensorNameWithBraces, "[" + props.getSensorID() + "]" );
                }
            }
        }
        return tempDefinition;
    }

    public static String getFinalDefinition() {
        return finalDefinition;
    }

    public static void resetFinalDefinition() {
        finalDefinition = "";
    }

//    /**
//     *
//     * @param sensorID
//     * @return
//     */
//    public static TreeSet<String> getAssociatedBuildings(int sensorID) {
//        TreeSet<String> list = new TreeSet<String>();
////        // SensorCollections die diesen Messpunkt aufführen holen (aus SensorCollectionhandler)
////        TreeSet<String> collectionList = SensorCollectionHandler.getCollectionsNameIncludingSensor(sensorID, SensorCollectionHandler.COMPARE_COLLECTION);
////        for (String collection : collectionList) {
////            list.addAll(SensorCollectionHandler.getInvolvedBuildingNames(collection)); // Name der Gebäude welche die jeweilige SensorCollection verwenden
////        }
////
//        return list;
//    }
    /**
     *
     * @param props
     * @return
     */
    public static boolean createNewSensor(SensorProperties props) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return false;
        }
        boolean success = false;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int id;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.SENSOR_TABLE + " set " + MoniSoftConstants.SENSOR_NAME + "= ?", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, props.getSensorName());
            // id des neuen Eintrags ermitteln
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            rs.next();
            logger.info("Added new sensor " + props.getSensorName());
            id = rs.getInt(1);
            writeSensorProperty(id, props);
            // Liste neu einlesen
            sensorList = new ListFiller().readSensorList();
            success = true;
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN NICHT DIE NÖTIGEN RECHTE UM NEUE MESSPUNKTE ANZULEGEN."));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN NICHT DIE NÖTIGEN RECHTE UM NEUE MESSPUNKTE ANZULEGEN."));
        } catch (Exception e) {
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLER BEI DATENBANKABFRAGE (CREATENEWSENSOR)"), new Object[]{e.getMessage()}), true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return success;
    }

    /**
     *
     * @param id
     * @param gui
     * @param component
     * @return
     */
    public static HashMap<Integer, Integer> deleteSensor(int id, boolean deleteData, MainApplication gui) {
        Connection myConn = null;
        Statement stmt = null;
        HashMap<Integer, Integer> deletedMap = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            if (deleteData) {
                deletedMap = deleteAllSensorOccurences(id, gui);
            }
            stmt.executeUpdate("delete from " + MoniSoftConstants.SENSOR_TABLE + " where " + MoniSoftConstants.SENSOR_ID + "=" + id);
            logger.info("Deletd sensor " + id + " from sensor list");
        } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return deletedMap;
    }

    /**
     *
     * @param sensorID
     * @param gui
     */
    public static HashMap<Integer, Integer> deleteAllSensorOccurences(int sensorID, MainApplication gui) {
        Connection myConn = null;
        Statement stmt = null;
        HashMap<Integer, Integer> deletion_resultMap = new HashMap<Integer, Integer>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            // delete from factor-table
            FactorHandler fh = new FactorHandler(sensorID);
            Integer deletedFactors = fh.removeAllFactors();

            // delete from counter change table
            CounterChangeHandler ch = new CounterChangeHandler();
            Integer deletedCounterChanges = ch.removeCounterChanges(sensorID);

            // delete from monthly-table
            Integer deletedMonthly = new MonthlyUsageCalculator(gui).deleteAllMonthlyUsages(sensorID);

            //delete from sensor collections
            Integer deletedCollections = SensorCollectionHandler.deleteSensorFromAllCollections(sensorID);

            // remove from weather definitions
            Integer deletedWeather = WeatherManager.removeSensor(sensorID);

            // delete data of sensor
            DataHandler dh = new DataHandler(sensorID);
            Integer deletedData = dh.deleteValuesForTimeRange(null); // null means "all values"
            logger.info("Deleted " + deletedData + " raw data points from sensor " + " " + sensorID);

            // assigning results to containter
            deletion_resultMap.put(NUM_DELETED_FACTORS, deletedFactors);
            deletion_resultMap.put(NUM_DELETED_CHANGES, deletedCounterChanges);
            deletion_resultMap.put(NUM_DELETED_MONTHLY, deletedMonthly);
            deletion_resultMap.put(NUM_DELETED_COLLECTIONS, deletedCollections);
            deletion_resultMap.put(NUM_DELETED_WEATHER, deletedWeather);
            deletion_resultMap.put(NUM_DELETED_DATA, deletedData);
        } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return deletion_resultMap;
    }

    /**
     *
     * @return
     */
    public static ArrayList<SensorProperties> getSensorList() {
        return sensorList;
    }

    /**
     *
     * @param list
     */
    public static void setSensorList(ArrayList<SensorProperties> list) {
        sensorList = list;
    }

    /**
     *
     * Gibt eine Liste der in der DB aufgeführten Zähler zurück
     *
     * @return Sensorenliste
     */
    public static TreeSet<SensorProperties> getCounterList() {
        TreeSet<SensorProperties> list = new TreeSet<SensorProperties>();
        SensorProperties props;
        Iterator<SensorProperties> it = sensorList.iterator();
        while (it.hasNext()) {
            props = it.next();
            if (props.isCounter()) {
                list.add(props);
            }
        }
        return new TreeSet<SensorProperties>(list);
    }

    /**
     *
     * Gibt eine Liste der in der DB aufgeführten Verbraucher zurück (also
     * Messpunkte die als Verbrauch markiert sind aber kein Zähler sind)
     *
     * @return Sensorenliste
     */
    public static HashSet<SensorProperties> getUsageList() {
        HashSet<SensorProperties> list = new HashSet<SensorProperties>();
        SensorProperties props;
        Iterator<SensorProperties> it = sensorList.iterator();
        while (it.hasNext()) {
            props = it.next();
            if (props.isUsage()) {
                list.add(props);
            }
        }
        return new HashSet<SensorProperties>(list);
    }

    /**
     *
     * Gibt eine Liste der in der DB aufgeführten Events zurück
     *
     * @return Sensorenliste
     */
    public static HashSet<SensorProperties> getEventList() {
        HashSet<SensorProperties> list = new HashSet<SensorProperties>();
        SensorProperties props;
        Iterator<SensorProperties> it = sensorList.iterator();
        while (it.hasNext()) {
            props = it.next();
            if (props.isEvent()) {
                list.add(props);
            }
        }
        return new HashSet<SensorProperties>(list);
    }

    /**
     * Leert die Sensorliste
     */
    public static void clearSensorList() {
        if (sensorList != null) {
            sensorList.clear();
        }
    }

    /**
     *
     * @param building
     * @param category
     * @return
     */
    public static Integer getSensorIDForConsumptionCategory(Integer building, String category) {
        Integer id = null;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String entry = "";
        String[] idList;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.SENSORCOLLECTION_LIST + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ? and " + MoniSoftConstants.SENSORCOLLECTION_CREATOR + "= ?");
            stmt.setString(1, category);
            stmt.setInt(2, SensorCollectionHandler.COMPARE_COLLECTION);
            rs = stmt.executeQuery();

            while (rs.next()) {
                entry = rs.getString(1);
            }

            if (entry.isEmpty()) {
                return null;
            }
            idList = entry.split(",");

            for (int i = 0; i < idList.length; i++) {
                if (SensorInformation.getSensorProperties(Integer.valueOf(idList[i])).getBuildingID().equals(building)) {
                    id = Integer.valueOf(idList[i]);
                }
            }

        } catch (Exception e) {
            Messages.showException(e);
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLER BEI DATENBANKABFRAGE (GETSENSORIDFORCONSUMPTIONCATEGORY)"), new Object[]{e.getMessage()}), true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return id;
    }

    public static boolean sensorNameIsFreeForBuilding(String name, BuildingProperties building) {
        boolean isFree = true;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            myConn = DBConnector.openConnection();
            if (building == null) {
                stmt = myConn.prepareStatement("select count(" + MoniSoftConstants.SENSOR_ID + ") from " + MoniSoftConstants.SENSOR_TABLE + " where " + MoniSoftConstants.SENSOR_NAME + "= ? and " + MoniSoftConstants.SENSOR_BUILDING_ID + " is null");
                stmt.setString(1, name);
            } else {
                stmt = myConn.prepareStatement("select count(" + MoniSoftConstants.SENSOR_ID + ") from " + MoniSoftConstants.SENSOR_TABLE + " where " + MoniSoftConstants.SENSOR_NAME + "= ? and " + MoniSoftConstants.SENSOR_BUILDING_ID + "= ?");
                stmt.setString(1, name);
                stmt.setInt(2, building.getBuildingID());
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    isFree = false; // this name / building combination exists... not free
                }
            }
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLER BEI DATENBANKABFRAGE (GETSENSORIDFORCONSUMPTIONCATEGORY)"), new Object[]{e.getMessage()}), true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return isFree;
    }

    public static boolean sensorKeyIsFreeForBuilding(String key, BuildingProperties building) {
        boolean isFree = true;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            if (building == null) {
                stmt = myConn.prepareStatement("select count(" + MoniSoftConstants.SENSOR_ID + ") from " + MoniSoftConstants.SENSOR_TABLE + " where " + MoniSoftConstants.SENSOR_KEY + "= ? and " + MoniSoftConstants.SENSOR_BUILDING_ID + " is null");
                stmt.setString(1, key);
            } else {
                stmt = myConn.prepareStatement("select count(" + MoniSoftConstants.SENSOR_ID + ") from " + MoniSoftConstants.SENSOR_TABLE + " where " + MoniSoftConstants.SENSOR_KEY + "= ? and " + MoniSoftConstants.SENSOR_BUILDING_ID + "= ?");
                stmt.setString(1, key);
                stmt.setInt(2, building.getBuildingID());
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                if (rs.getInt(1) > 0) {
                    isFree = false; // this key / building combination exists... not free
                }
            }
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLER BEI DATENBANKABFRAGE (GETSENSORIDFORCONSUMPTIONCATEGORY)"), new Object[]{e.getMessage()}), true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return isFree;
    }
}
