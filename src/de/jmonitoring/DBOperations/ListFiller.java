package de.jmonitoring.DBOperations;

import java.sql.PreparedStatement;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.Cluster.Cluster;
import java.sql.Connection;
import de.jmonitoring.References.ReferenceDescription;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.utils.UnitCalulation.Unit;
import de.jmonitoring.utils.swing.EDT;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * This class is responsible for querying the database entries and fill all the
 * lists necessary for operation:<p> SensorList<br> BuildingList<br>
 * UnitList<br>Reference values<br>Sensor collections
 *
 * @author togro
 */
public class ListFiller {

    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Reads from T_Buildings, creates {@link BuildingProperties} for each
     * building and fills the global list of buildings
     *
     * @return A List of {@link BuildingProperties}
     */
    public ArrayList<BuildingProperties> readBuildingList() {
        EDT.never("readBuildingList");
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<BuildingProperties> buildingList = new ArrayList<BuildingProperties>(100);
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.LESE GEBÄUDELISTE"), true);
        BuildingProperties props;
        ArrayList<Integer> idList;
        String sensorCollectionIDs;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            // get list of all columns in sensor table
            stmt = myConn.createStatement();
            Set<String> columnNames = new HashSet<String>();
            stmt.executeQuery("show columns from " + MoniSoftConstants.BUILDING_TABLE);
            rs = stmt.getResultSet();
            while (rs.next()) {
                columnNames.add(rs.getString(1));
            }

            rs = stmt.executeQuery("select * from " + MoniSoftConstants.BUILDING_TABLE);
            while (rs.next()) {
                props = new BuildingProperties();
                if (columnNames.contains(MoniSoftConstants.BUILDING_NAME)) {
                    props.setBuildingName(rs.getString(MoniSoftConstants.BUILDING_NAME));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_ID)) {
                    props.setBuildingID(returnInteger(rs, MoniSoftConstants.BUILDING_ID));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_STREET)) {
                    props.setStreet(rs.getString(MoniSoftConstants.BUILDING_STREET));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_CONTACT)) {
                    props.setPlz(rs.getInt(MoniSoftConstants.BUILDING_PLZ));
                    props.setContact(rs.getString(MoniSoftConstants.BUILDING_CONTACT));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_CITY)) {
                    props.setCity(rs.getString(MoniSoftConstants.BUILDING_CITY));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_PHONE)) {
                    props.setPhone(returnLong(rs, MoniSoftConstants.BUILDING_PHONE));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_NETWORKING)) {
                    props.setNetworking(rs.getString(MoniSoftConstants.BUILDING_NETWORKING));
                }
                if (columnNames.contains(MoniSoftConstants.BUILDING_DESCRIPTION)) {
                    props.setBuildingDescription(rs.getString(MoniSoftConstants.BUILDING_DESCRIPTION));
                }
                // Die SensorCollections des Gebäudes holen und numerisch Umwandeln
                idList = new ArrayList<Integer>(36);
                sensorCollectionIDs = rs.getString(MoniSoftConstants.BUILDING_COLLECTIONIDS);
                if (sensorCollectionIDs != null) {
                    for (String entry : Arrays.asList(sensorCollectionIDs.split(","))) {
                        if (!entry.isEmpty() && Integer.valueOf(entry) != null) {
                            idList.add(Integer.valueOf(entry));
                        }
                    }
                }
//                props.setSensorCollecions(idList);
//                props.setObjectID(rs.getInt(MoniSoftConstants.BUILDING_OBJECT));

                buildingList.add(props);
            }
            Messages.showMessage(" " + (buildingList.size()) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.BUILDINGSREAD") + "\n", true);
        } catch (SQLException e) {
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            return buildingList;
        }
    }

    /**
     * Reads from T_Sensors, creates {@link SensorProperties} for each sensor
     * and fills the global list of sensors
     *
     * @return A List of {@link SensorProperties}
     */
    public ArrayList<SensorProperties> readSensorList() {
        EDT.never("readSensorList");
        Connection myConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ArrayList<SensorProperties> sensorList = new ArrayList<SensorProperties>(1024);
        int id;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.LESE MESSPUNKTLISTE"), true);
        SensorProperties props;

        try {
            myConn = DBConnector.openConnection();

            // get list of all columns in sensor table
            Statement s = myConn.createStatement();
            Set<String> columnNames = new HashSet<String>();
            s.executeQuery("show columns from " + MoniSoftConstants.SENSOR_TABLE);
            rs = s.getResultSet();
            while (rs.next()) {
                columnNames.add(rs.getString(1));
            }

            pstmt = myConn.prepareStatement(PreparedStatements.SELECT_ALL_FROM_SENSORTABLE);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                id = rs.getInt(MoniSoftConstants.SENSOR_ID);
                props = new SensorProperties(id);
                if (columnNames.contains(MoniSoftConstants.SENSOR_NAME)) {
                    props.setSensorName(rs.getString(MoniSoftConstants.SENSOR_NAME));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_DESCRIPTION)) {
                    props.setSensorDescription(rs.getString(MoniSoftConstants.SENSOR_DESCRIPTION));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_UNIT_ID)) {
                    props.setSensorUnit(UnitInformation.getUnitFormID(rs.getInt(MoniSoftConstants.SENSOR_UNIT_ID)));
                }
                if (columnNames.contains(MoniSoftConstants.IS_MANUAL)) {
                    props.setManual(rs.getBoolean(MoniSoftConstants.IS_MANUAL));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_FACTOR)) {
                    props.setFactor(returnDouble(rs, MoniSoftConstants.SENSOR_FACTOR));
                }
                if (columnNames.contains(MoniSoftConstants.IS_COUNTER)) {
                    props.setCounter(rs.getBoolean(MoniSoftConstants.IS_COUNTER));
                }
                if (columnNames.contains(MoniSoftConstants.MIN_WORKDAY)) {
                    props.setWTLimits(returnInteger(rs, MoniSoftConstants.MIN_WORKDAY), MoniSoftConstants.MINIMUM);
                }
                if (columnNames.contains(MoniSoftConstants.MAX_WORKDAY)) {
                    props.setWTLimits(returnInteger(rs, MoniSoftConstants.MAX_WORKDAY), MoniSoftConstants.MAXIMUM);
                }
                if (columnNames.contains(MoniSoftConstants.MIN_WEEKEND)) {
                    props.setWELimits(returnInteger(rs, MoniSoftConstants.MIN_WEEKEND), MoniSoftConstants.MINIMUM);
                }
                if (columnNames.contains(MoniSoftConstants.MAX_WEEKEND)) {
                    props.setWELimits(returnInteger(rs, MoniSoftConstants.MAX_WEEKEND), MoniSoftConstants.MAXIMUM);
                }
                if (columnNames.contains(MoniSoftConstants.MAX_WORKDAY_CHANGETIME)) {
                    props.setMaxChangeTimes(returnLong(rs, MoniSoftConstants.MAX_WORKDAY_CHANGETIME), MoniSoftConstants.WORKDAY);
                }
                if (columnNames.contains(MoniSoftConstants.MAX_WEEKEND_CHANGETIME)) {
                    props.setMaxChangeTimes(returnLong(rs, MoniSoftConstants.MAX_WEEKEND_CHANGETIME), MoniSoftConstants.WEEKEND);
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_INTERVAL)) {
                    props.setInterval(returnInteger(rs, MoniSoftConstants.SENSOR_INTERVAL));
                }
                if (columnNames.contains(MoniSoftConstants.IS_EVENT)) {
                    props.setEvent(rs.getBoolean(MoniSoftConstants.IS_EVENT));
                }
                if (columnNames.contains(MoniSoftConstants.VIRT_DEF)) {
                    props.setVirtualDefinition(rs.getString(MoniSoftConstants.VIRT_DEF));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_KEY)) {
                    props.setKeyName(rs.getString(MoniSoftConstants.SENSOR_KEY));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_BUILDING_ID)) {
                    props.setBuildingID(returnInteger(rs, MoniSoftConstants.SENSOR_BUILDING_ID)); //(rs.getObject(MoniSoftConstants.SENSOR_BUILDING_ID)));
                }
                if (columnNames.contains(MoniSoftConstants.IS_RESETCOUNTER)) {
                    props.setResetCounter(rs.getBoolean(MoniSoftConstants.IS_RESETCOUNTER));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_MEDIUM)) {
                    props.setMedium(rs.getString(MoniSoftConstants.SENSOR_MEDIUM));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_CONSTANT)) {
                    props.setConstant(returnDouble(rs, MoniSoftConstants.SENSOR_CONSTANT));
                }
                if (columnNames.contains(MoniSoftConstants.IS_USAGE)) {
                    props.setUsage(rs.getBoolean(MoniSoftConstants.IS_USAGE));
                }
                // AZ: MONISOFT-22: Zeitzonen
                if (columnNames.contains(MoniSoftConstants.SENSOR_UTC_PLUX_X)) {
                    props.setUtcPlusX(returnLong(rs, MoniSoftConstants.SENSOR_UTC_PLUX_X));
                }
                if (columnNames.contains(MoniSoftConstants.SENSOR_SUMMERTIME)) {
                    props.setSummerTime(rs.getBoolean(MoniSoftConstants.SENSOR_SUMMERTIME));
                }
                sensorList.add(props);
            }
            Messages.showMessage(" " + sensorList.size() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.SENSORSREAD") + "\n", true);
        } catch (Exception e) {
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, pstmt, rs);
            return sensorList;
        }
    }

    /**
     * Reads reference value from the database and puts them into a map with
     * the<br> buildingID as key and an ArrayList of all {@link ReferenceValue}
     * as content
     *
     * @return A map of reference values for all buildings
     */
    public HashMap<Integer, ArrayList<ReferenceValue>> readBuildingReferencesMap() {
        EDT.never("readBuildingReferencesMap");
        HashMap<Integer, ArrayList<ReferenceValue>> map = new HashMap<Integer, ArrayList<ReferenceValue>>();

        ArrayList<BuildingProperties> buildings = BuildingInformation.getBuildingList();
        Integer buildingID;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String name;
        Double value;

        // fill map for all buildings with empty lists
        for (BuildingProperties building : buildings) {
            map.put(building.getBuildingID(), new ArrayList<ReferenceValue>(20));
        }

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            rs = stmt.executeQuery("select " + MoniSoftConstants.REFERENCEMAP_NAME + "," + MoniSoftConstants.REFERENCEMAP_VALUE + "," + MoniSoftConstants.REFERENCEMAP_BUILDING_ID + " from " + MoniSoftConstants.REFERENCEMAP_TABLE);
            while (rs.next()) {
                name = rs.getString(1);
                value = rs.getDouble(2);
                buildingID = rs.getInt(3);
                if (name != null && !name.isEmpty() && value != null && value != 0 && buildingID != null) {
                    map.get(buildingID).add(new ReferenceValue(name, value, null, null));
                }
            }
        } catch (Exception e) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return map;
    }

    /**
     * Reads units from T_Units table and puts them into a map with the<br>
     * unitID as key and the name of the unit as content
     *
     * @return A map of {@link Unit}s
     */
    public HashMap<Integer, Unit> readUnitList() {
        EDT.never("readUnitList");
        HashMap<Integer, Unit> unitMap = new HashMap<Integer, Unit>(100);
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.LESE EINHEITENLISTE"), true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT " + MoniSoftConstants.UNIT + "," + MoniSoftConstants.UNIT_ID + " from " + MoniSoftConstants.UNIT_TABLE);
            while (rs.next()) {
                unitMap.put(rs.getInt(2), new Unit(rs.getString(1)));
            }
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.OK") + "\n", true);
        } catch (Exception e) {
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            return unitMap;
        }
    }

    /**
     * Reads the refernce descriptions from the database
     *
     * @return A set of all {@link ReferenceDescription}s
     */
    public TreeSet<ReferenceDescription> readReferenceList() {
        EDT.never("readReferenceList");
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        TreeSet<ReferenceDescription> referenceList = new TreeSet<ReferenceDescription>();
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.LESE LISTE DER BEZUGSGRÖSSEN"), true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT " + MoniSoftConstants.REFERENCENAME_NAME + "," + MoniSoftConstants.REFERENCENAME_UNIT_ID + "," + MoniSoftConstants.REFERENCENAME_DESCRIPTION + " from " + MoniSoftConstants.REFERENCES_TABLE);
            while (rs.next()) {
                referenceList.add(new ReferenceDescription(rs.getString(1), rs.getString(3), rs.getInt(2)));
            }
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.OK") + "\n", true);
        } catch (Exception e) {
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            return referenceList;
        }
    }

    /**
     * Reads a list of clusters from the database.
     *
     * @return A map of all {@link Cluster}s with their name as key
     */
    public TreeMap<String, Cluster> readClusterList() {
        EDT.never("readClusterList");
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        TreeMap<String, Cluster> list = new TreeMap<String, Cluster>();
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.LESE LISTE DER CLUSTER"), true);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("SELECT " + MoniSoftConstants.CLUSTER_NAME + "," + MoniSoftConstants.CLUSTER_KAT + "," + MoniSoftConstants.CLUSTER_BUILDINGS + " from " + MoniSoftConstants.CLUSTER_TABLE);

            String name;
            Integer group;
            Cluster cluster;
            String buildingString;

            while (rs.next()) {
                name = rs.getString(1);
                group = rs.getInt(2);
                buildingString = rs.getString(3);

                // Wenn Cluster schon aufgenommen diesen holen, sonst neu erstellen
                if (list.containsKey(name)) {
                    cluster = list.get(name);
                } else {
                    cluster = new Cluster(name);
                    list.put(name, cluster);
                }

                // Die Gruppe der aktuellen Zeile hinzufügen (jede Zeile ist quasi eine Gruppe
                cluster.setGroup(group);

                // Set aus Gebäudestring generieren
                TreeSet<Integer> buildingSet = buildingIDSet(buildingString);

                // Alle Gebäude durchlaufen und eintragen
                for (Integer building : buildingSet) {
                    cluster.addBuilding(group, building);
                }
            }

            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.OK") + "\n", true);
        } catch (Exception e) {
            Messages.showMessage(" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ListFiller.ERROR") + " " + e.getMessage() + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            return list;
        }
    }

    /**
     * Reads a list of sensor collections from the database.
     *
     * @return A map of all sensor collections with their id as key and a
     * {@link HashSet} of the assigned sensorIDs
     */
    public HashMap<Integer, HashSet<Integer>> readCollectionSensors() {
        EDT.never("readCollectionSensors");
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String[] list;
        Integer collectionID;
        HashMap<Integer, HashSet<Integer>> collectionSensors = new HashMap<Integer, HashSet<Integer>>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.SENSORCOLLECTION_ID + "," + MoniSoftConstants.SENSORCOLLECTION_LIST + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE);
            while (rs.next()) {
                HashSet<Integer> idList = new HashSet<Integer>(32);
                collectionID = rs.getInt(1);
                list = rs.getString(2).split(",");
                for (String entry : list) {
                    if (!entry.isEmpty()) { // Leere Strings ausfischen
                        idList.add(Integer.valueOf(entry));
                    }
                }
                collectionSensors.put(collectionID, idList);
            }
        } catch (SQLException ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return collectionSensors;
    }

    /**
     * Builds a list of {@link Integer} buildingIDs from the given
     * {@link String} of comma seperated buildingIDs
     *
     * @param buildingString The list of buildingIDs as {@link String}
     * @return A set of buildingIDs
     */
    private TreeSet<Integer> buildingIDSet(String buildingString) {
        TreeSet<Integer> set = new TreeSet<Integer>();
        String[] s = buildingString.split(",");
        for (int i = 0; i < s.length; i++) {
            if (!s[i].equals("")) {
                set.add(Integer.valueOf(s[i]));
            }
        }
        return set;
    }

    /**
     * Returns the {@link Integer} value of a column of the given
     * {@link ResultSet} or
     * <code>null</code> if the database field is empty.<br> The builtin
     * getInt() method of {@link ResultSet} would return 0 instead.
     *
     * @param rs The {@link ResultSet}
     * @param columName The cname of the column in question
     * @return The {@link Integer} value
     * @throws SQLException
     */
    private Integer returnInteger(ResultSet rs, String columName) throws SQLException {
        Integer i = new Integer(rs.getInt(columName));
        if (rs.wasNull()) {
            i = null;
        }
        return i;
    }

    /**
     * Returns the {@link Long} value of a column of the given {@link ResultSet}
     * or
     * <code>null</code> if the database field is empty.<br> The builtin
     * getLong() method of {@link ResultSet} would return 0 instead.
     *
     * @param rs The {@link ResultSet}
     * @param columName The cname of the column in question
     * @return The {@link Long} value
     * @throws SQLException
     */
    private Long returnLong(ResultSet rs, String columName) throws SQLException {
        Long l = new Long(rs.getLong(columName));
        if (rs.wasNull()) {
            l = null;
        }
        return l;
    }

    /**
     * Returns the {@link Double} value of a column of the given
     * {@link ResultSet} or
     * <code>null</code> if the database field is empty.<br> The builtin
     * getLong() method of {@link ResultSet} would return 0.0 instead.
     *
     * @param rs The {@link ResultSet}
     * @param columName The cname of the column in question
     * @return The {@link Double} value
     * @throws SQLException
     */
    private Double returnDouble(ResultSet rs, String columName) throws SQLException {
        Double d = new Double(rs.getDouble(columName));
        if (rs.wasNull()) {
            d = null;
        }
        return d;
    }
}
