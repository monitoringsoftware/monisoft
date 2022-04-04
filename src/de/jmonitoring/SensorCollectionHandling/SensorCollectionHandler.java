package de.jmonitoring.SensorCollectionHandling;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;

import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 * This class handles the creation and maintainance of {@link SensorCollection}s
 * in the database.
 *
 * @author togro
 */
public class SensorCollectionHandler {

    public final static int SIMPLE_COLLECTION = 0;
    public final static int COMPARE_COLLECTION = 1;
    private static HashMap<Integer, HashSet<Integer>> collectionSensors;
    private static ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Write a new {@link SensorCollection} to the database
     *
     * @param name Name of the collection
     * @param idList List of all sensor IDs belonging to this collection
     * @param creator Creator of the collection (compare collection or simple
     * collection)
     * @return A list of generated indexes in the table
     */
    public synchronized static Integer insertCollection(String name, HashSet<Integer> idList, int creator, boolean climateCorrectable, boolean refresh) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Integer createdID = null;
        ArrayList<Integer> buildingList = new ArrayList<Integer>(16);
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder list = new StringBuilder("");
        String sep = "";
        // loop all sensors
        for (Integer entry : idList) {
            list.append(sep).append(entry.toString());
            sep = ",";
            // get buildingID of sensor
            Integer buildingID = SensorInformation.getSensorProperties(entry).getBuildingID();
            if (buildingID != null) {
                buildingList.add(buildingID);
            }
        }
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " set " + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ?," + MoniSoftConstants.SENSORCOLLECTION_CREATOR + "= ?," + MoniSoftConstants.SENSORCOLLECTION_LIST + "= ?", Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, name);
            stmt.setInt(2, creator);
            stmt.setString(3, list.toString());
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                createdID = rs.getInt(1);
            }

            if (createdID != null && creator == SensorCollectionHandler.COMPARE_COLLECTION) { // wenn es einen Eintrag gab und es eine Vergleichsgruppe ist
                insertCollectionIDToBuildings(buildingList, createdID);
                stmt.executeUpdate("update " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " set climatecorrection =" + climateCorrectable + " where id=" + createdID);
            }
            logger.info("Added sensor collection '" + name + "' with " + idList.size() + " sensors");
        } catch (MySQLSyntaxErrorException ex) {
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        if (refresh) {
            refreshCollections();
        }
        return createdID;
    }

    /**
     * Removes the given collection ID from a building
     *
     * @param buildings The ID of the bulding
     * @param collectionID The ID of the collection to be removed
     */
    private synchronized static void removeCollectionIDFromBuilding(Integer collectionID, Integer building) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        StringBuilder idStringBuilder;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "= ?");
            stmt.setInt(1, building);
            updateStmt = myConn.prepareStatement("update " + MoniSoftConstants.BUILDING_TABLE + " set " + MoniSoftConstants.BUILDING_COLLECTIONIDS + "= ? where " + MoniSoftConstants.BUILDING_ID + "= ?");
            rs = stmt.executeQuery();
            if (rs.next()) {
                String s = rs.getString(1);
                ArrayList<String> ids = new ArrayList<String>();
                if (s != null) {
                    idStringBuilder = new StringBuilder(s); // the string with the IDs
                    ids = new ArrayList<String>(Arrays.asList(idStringBuilder.toString().split(",")));
                } 
                idStringBuilder = new StringBuilder();
                for (String id : ids) {
                    if (!idStringBuilder.toString().isEmpty()) {
                        idStringBuilder.append(",");
                    }

                    if (!id.isEmpty() && !collectionID.equals(Integer.valueOf(id))) {
                        idStringBuilder.append(id);
                    }
                }

                idStringBuilder = new StringBuilder(idStringBuilder.toString().replace(",,", ","));

                if (idStringBuilder.toString().endsWith(",")) {
                    idStringBuilder.deleteCharAt(idStringBuilder.length() - 1);
                }

                updateStmt.setString(1, idStringBuilder.toString());
                updateStmt.setInt(2, building);
                updateStmt.executeUpdate();
            }
            logger.info("Removed sensor collection with id " + collectionID + " from building " + BuildingInformation.getBuildingNameFromID(building));
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            DBConnector.closeConnection(null, updateStmt, null);
        }
    }

    /**
     * Removes the given collection ID from all buildings
     *
     * @param collectionID The ID of the collection to be removed
     */
    private synchronized static void removeCollectionIDFromAllBuildings(Integer collectionID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        Statement updateStmt = null;
        ResultSet rs = null;
//        StringBuilder idStringBuilder;
//        Integer building;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            updateStmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.BUILDING_ID + "," + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE);
            while (rs.next()) { // loop all buildings
                removeCollectionIDFromBuilding(collectionID, rs.getInt(1));
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            DBConnector.closeConnection(myConn, updateStmt, null);
        }
    }

    /**
     * Deletes the collection with the given name from the database
     *
     * @param name The name of the {@link SensorCollection} to be deleted
     * @param refresh if <ocde>true</code> the internal list of collections will
     * be refreshed
     */
    public synchronized static void removeCollection(String name, boolean refresh) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        PreparedStatement stmt = null;

        Integer collectionID = getCollectionID(name);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("delete from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ?");
            stmt.setString(1, name);
            stmt.executeUpdate();
            logger.info("Removed sensor collection with id " + collectionID);
            removeCollectionIDFromAllBuildings(collectionID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        if (refresh) {
            refreshCollections();
        }
    }

    /**
     * Deletes the collection with the given ID from the database
     *
     * @param collectionID The ID of the {@link SensorCollection} to be deleted
     * @param refresh if <ocde>true</code> the internal list of collections will
     * be refreshed
     */
    public synchronized static void removeCollection(int collectionID, boolean refresh) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_ID + "=" + collectionID + "");
            logger.info("Removed sensor collection with id " + collectionID);
            removeCollectionIDFromAllBuildings(collectionID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        if (refresh) {
            refreshCollections();
        }
    }

    /**
     * Refresh the list of internal {@link SensorCollection}s to reflect the
     * state in the database
     */
    public synchronized static void refreshCollections() {
        setCollectionSensors(new ListFiller().readCollectionSensors());
    }

    /**
     * Invoke the update of a collection in the database
     *
     * @param collectionID The id of the {@link SensorCollection}
     * @param sensorIDList The list of sensor IDs for this collection
     * @param isClimateCorrectable <code>true</code> if this is a collection
     * that shoulb be climate corrected
     * @param refresh If <code>true</code> the interal list of collections will
     * be refreshed
     */
    public synchronized static void updateCollection(Integer collectionID, HashSet<Integer> sensorIDList, boolean isClimateCorrectable, boolean refresh) {
        updateCollection(collectionID, SensorCollectionHandler.getCollectionName(collectionID), sensorIDList, isClimateCorrectable, refresh);
    }

    /**
     * Invoke the update of a collection in the database
     *
     * @param collectionID The id of the {@link SensorCollection}
     * @param name The (new) name of the collection
     * @param sensorIDList The list of sensor IDs for this collection
     * @param isClimateCorrectable <code>true</code> if this is a collection
     * that shoulb be climate corrected
     * @param refresh If <code>true</code> the interal list of collections will
     * be refreshed
     */
    public synchronized static void updateCollection(Integer collectionID, String name, HashSet<Integer> sensorIDList, boolean isClimateCorrectable, boolean refresh) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }

        Connection myConn = null;
        PreparedStatement stmt = null;
        StringBuilder listString = new StringBuilder("");
        String sep = "";

        boolean isCompareCollection = isCompareCollection(collectionID);
        for (Integer sensorID : sensorIDList) {
            if (isCompareCollection) {
                updateBuilding(sensorID, collectionID);
            }
            listString.append(sep).append(sensorID.toString());
            sep = ",";
        }


        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("update " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " set " + MoniSoftConstants.SENSORCOLLECTION_LIST + "= ?," + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ?, climatecorrection= ? where " + MoniSoftConstants.SENSORCOLLECTION_ID + "= ?");
            stmt.setString(1, listString.toString());
            stmt.setString(2, name);
            stmt.setBoolean(3, isClimateCorrectable);
            stmt.setInt(4, collectionID);
            stmt.executeUpdate();
            logger.info("Updated sensor collection with id " + collectionID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        if (refresh) {
            refreshCollections();
        }
        if (isCompareCollection) {
            trimBuildingCollections(collectionID);
        }
    }

    /**
     * Return the type of the collection
     *
     * @param collectionID The ID of the collection to query
     * @return <code>true</code> if the collection is a compare collection
     */
    private static boolean isCompareCollection(Integer collectionID) {
        boolean b = false;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.SENSORCOLLECTION_CREATOR + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_ID + "= ?");
            stmt.setInt(1, collectionID);
            rs = stmt.executeQuery();
            rs.next();
            b = rs.getBoolean(1);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return b;
    }

    /**
     * Insert a collection to all involved buildings
     *
     * @param buildings The list of buildings
     * @param collectionID The ID of the collection to insert
     */
    private synchronized static void insertCollectionIDToBuildings(ArrayList<Integer> buildings, Integer collectionID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        PreparedStatement stmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;
        StringBuilder idString;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "= ?");
            updateStmt = myConn.prepareStatement("update " + MoniSoftConstants.BUILDING_TABLE + " set " + MoniSoftConstants.BUILDING_COLLECTIONIDS + "= ? where " + MoniSoftConstants.BUILDING_ID + "= ?");
            // loop all given buildings
            for (Integer building : buildings) {
                stmt.setInt(1, building);
                // get the already existing collectionIDs for this building
                rs = stmt.executeQuery();
                if (rs.next()) {
                    String s = rs.getString(1);
                    if (s != null) {
                        idString = new StringBuilder(s); // the string with the IDs

                        ArrayList<String> ids = new ArrayList<String>(Arrays.asList(idString.toString().split(",")));
                        if (collectionID != null && collectionID != 0 && !ids.contains(collectionID.toString())) { // if not already in listString or invalid or empty
                            if (!idString.toString().isEmpty()) {
                                idString.append(",");
                            }

                            // add the new collection to the listString of existing collections
                            idString.append(Integer.valueOf(collectionID));
                            // write the new listString of collectionIDs of the building
                            updateStmt.setString(1, idString.toString());
                            updateStmt.setInt(2, building);
                            updateStmt.executeUpdate();
                            logger.info("Added collection with id " + collectionID + " to building with id " + BuildingInformation.getBuildingNameFromID(building));
                        }
                    }
                }
            }
//            ListFiller.readBuildingList();
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
            DBConnector.closeConnection(null, updateStmt, null);
        }
    }

    /**
     * Removes all occurances of a SensorCollection from the building table that
     * includes no sensor of that building.
     *
     * @param collectionID
     */
    private synchronized static void trimBuildingCollections(Integer collectionID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }

        // loop all buildings
        ArrayList<BuildingProperties> buildingList = BuildingInformation.getBuildingList();
        Integer building;
        for (BuildingProperties buildingProps : buildingList) {
            building = buildingProps.getBuildingID();
            Integer buildingOfSensor;

            HashSet<Integer> sensors = SensorCollectionHandler.getSensorsForCollection(collectionID);
            // loop all sensors in the current collection
            boolean buildingAppearsInCollection = false;

            for (Integer sensorID : sensors) {
                if (SensorInformation.getSensorProperties(sensorID) != null) {
                    buildingOfSensor = SensorInformation.getSensorProperties(sensorID).getBuildingID(); // get building of sensor
                    if (building.equals(buildingOfSensor)) { // a sensor of the given building is in the collection
                        buildingAppearsInCollection = true;
                        break;
                    }
                }
            }

            if (!buildingAppearsInCollection) {
                removeCollectionIDFromBuilding(collectionID, building);
            }
        }
    }

    /**
     * Deletes all sensors of the given building from all SensorCollections
     *
     * @param buildingID
     */
    public synchronized static Integer removeSensorsOfBuildingFromAllCollections(int buildingID) {
        Integer numberRemoved = 0;
        HashSet<Integer> collectionList = BuildingInformation.getUsageCategoryIDs(buildingID);
        for (Integer collectionID : collectionList) {
            numberRemoved += removeSensorsOfBuildingFromCollection(buildingID, collectionID);
        }
        return numberRemoved;
    }

    /**
     * Deletes all sensors of the given building from the given sensorCollection
     *
     * @param buildingID
     * @param collectionID
     */
    public synchronized static Integer removeSensorsOfBuildingFromCollection(int buildingID, int collectionID) {
        Integer count = 0;
        HashSet<Integer> sensorList = SensorCollectionHandler.getSensorsForCollection(collectionID);
        Iterator<Integer> it = sensorList.iterator();
        Integer sensorID;
        while (it.hasNext()) {
            sensorID = it.next();
            if (SensorInformation.getSensorProperties(sensorID).getBuildingID().equals(buildingID)) {
                it.remove();
                count++;
            }
        }
        updateCollection(collectionID, sensorList, isClimateCorrectionCollection(getCollectionName(collectionID)), true);
        return count;
    }

    /**
     * Deletes all collection of a creator from the database
     *
     * @param creator The creator
     * (<code>SensorCollectionHandler.COMPARE_COLLECTION</code> * * * * * * * *
     * or <code>SensorCollectionHandler.SIMPLE_COLLECTION</code>)
     */
    public synchronized static void removeAllCollectionsOfCreator(int creator) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_CREATOR + "=" + creator + "");
            logger.info("Deleted all sensor collections of creator " + creator);
            if (creator == SensorCollectionHandler.COMPARE_COLLECTION) { // wenn es eine SensorCollection zum Gebäudevergleich ist, diese auch in der Gebäudetabelle löschen
                removeAllBuildingCollections();
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        refreshCollections();
    }

    /**
     * Remove all collections from all buildings
     */
    private synchronized static void removeAllBuildingCollections() {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("update " + MoniSoftConstants.BUILDING_TABLE + " set " + MoniSoftConstants.BUILDING_COLLECTIONIDS + "=''");
            logger.info("Removed all sensor collections form all buildings");
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    /**
     * Liefert alle Messpunktsammlungen eines Erzeugers aus der DB
     *
     * @param creator The creator
     * (<code>SensorCollectionHandler.COMPARE_COLLECTION</code> * * * * * * * *
     * or <code>SensorCollectionHandler.SIMPLE_COLLECTION</code>)
     * @return HashMap<String, ArrayList<Integer>> Mit Collection-Namen als
     * Schlüssel und ihren MesspunktIDs als ArrayList
     */
    public synchronized static Map<String, ArrayList<Integer>> getCollectionsOfCreator(Integer creator) {
        Map<String, ArrayList<Integer>> map = new TreeMap<String, ArrayList<Integer>>();
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String name, listString;
        List<String> list;
        ArrayList<Integer> idList;

        String whereCreator = "";
        if (creator != null) {
            whereCreator = " where " + MoniSoftConstants.SENSORCOLLECTION_CREATOR + "=" + creator;
        }

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.SENSORCOLLECTION_NAME + "," + MoniSoftConstants.SENSORCOLLECTION_LIST + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + whereCreator + " order by " + MoniSoftConstants.SENSORCOLLECTION_NAME);
            while (rs.next()) {
                
                name = rs.getString(1);
                listString = rs.getString(2);
                list = Arrays.asList(listString.split(","));
                idList = new ArrayList<Integer>(32);                
                for (String entry : list) {
                    if (!entry.isEmpty() && Integer.valueOf(entry) != null) {
                        idList.add(Integer.valueOf(entry));                        
                    }
                }
                map.put(name, idList);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return map;
    }

    /**
     * Returns the name of the given SensorCollection with the given
     * collectionID or
     * <code>null</code> if it does not exist
     *
     * @param collectionID The collectionID of the Sensorcollection
     * @return The name
     */
    public synchronized static String getCollectionName(int collectionID) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String name = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.SENSORCOLLECTION_NAME + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_ID + "=" + collectionID + " limit 1");
            while (rs.next()) {
                name = rs.getString(1);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return name;
    }

    /**
     * Returns the ID of the given SensorCollection with the given name or
     * <code>null</code> if it does not exist
     *
     * @param name The name of the Sensorcollection
     * @return The collectionID
     */
    public synchronized static Integer getCollectionID(String name) {
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Integer id = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select " + MoniSoftConstants.SENSORCOLLECTION_ID + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ?");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            while (rs.next()) {
                id = rs.getInt(1);
                break;
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return id;
    }

    /**
     * Return a map of all SensorCollections and their included sensors
     *
     * @return A {@link HashMap} of all {@link SensorCollections}s with the
     * collection ID as key and a list of sensor IDs as values
     */
    public static HashMap<Integer, HashSet<Integer>> getCollectionSensors() {
        return collectionSensors;
    }

    /**
     * Set the map of all SensorCollections to the given {@link HashMap}
     *
     * @param collectionSensors The HashMap of the SensorCollections
     */
    public static synchronized void setCollectionSensors(HashMap<Integer, HashSet<Integer>> collectionSensors) {
        SensorCollectionHandler.collectionSensors = collectionSensors;
    }

    /**
     * Return a list of sensors contained in the given collection
     *
     * @param collectionID The ID of the collection to query
     * @return A set of included sensor IDs
     */
    public static synchronized HashSet<Integer> getSensorsForCollection(Integer collectionID) {
        return collectionSensors.get(collectionID);
    }

    /**
     * Return a list of sensors contained in the given collection
     *
     * @param collectionID The name of the collection to query
     * @return A set of included sensor IDs
     */
    public static synchronized HashSet<Integer> getSensorsForCollection(String name) {
        return collectionSensors.get(getCollectionID(name));
    }

    /**
     * Liefert die Namen aller SensorCollections eines Erzeugers als
     * <code>ComboBoxModel</code>
     *
     * @param creator
     * @return <code>DefaultComboBoxModel</code> mit allen Collection-Namen
     */
    public synchronized static DefaultComboBoxModel getSensorCollectionNamesAsComboBoxModel(int creator, boolean addEmptyEntry, boolean addAllEntry) {
        DefaultComboBoxModel model = new DefaultComboBoxModel(getCollectionsOfCreator(creator).keySet().toArray());
        if (addEmptyEntry) {
            model.insertElementAt(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("SensorCollectionHandler.PLEASE_CHOOSE"), 0);
        }
        if (addAllEntry) {
            model.insertElementAt(MoniSoftConstants.ALL, 0);
        }
        return model;
    }

    /**
     * Get a list of names of all sensor collections of a certain creator
     *
     * @param creator The creator
     * @return <code>HashSet</code> of all sensor names
     */
    public synchronized static HashSet<String> getAllSensorCollectionNames(int creator) {
        HashSet<String> names = new HashSet<String>(getCollectionsOfCreator(creator).keySet());
        return names;
    }

    /**
     * Build a
     * <code>DefaultListModel</code> of sensors included in the given
     * <code>SensorCollection</code>
     *
     * @param name Name of the sensor collection
     * @return <code>DefaultListModel</code> of all included sensors
     */
    public synchronized static DefaultListModel getSensorCollectionAsListModel(String name) {
        DefaultListModel model = new DefaultListModel();
        HashSet<Integer> list = getSensorsForCollection(getCollectionID(name)); // Liste aller an der Collection beteiligten SensorIDs holen
        if (list != null) {
            for (Integer entry : list) {
                model.addElement(SensorInformation.getSensorProperties(entry)); // Properties dieser Sensoren in ListModelschreiben (nutzt toString der SensorProperties zur Anzeige)
            }
        }
        return model;
    }

    /**
     * Return all names of buildings that are involved in the collection with
     * the given name
     *
     * @param name Name of SensorCollection
     * @return A list of all involved building names
     */
    public synchronized static TreeSet<String> getInvolvedBuildingNames(String collectionName) {
        TreeSet<String> buildings = new TreeSet<String>(); // dublettenlose List
        Integer collectionID = getCollectionID(collectionName); // id der Collection ermitteln

        if (collectionID != null) { // wenn es diese Sensorcollection gibt
            String buildingName, collections[];
            Connection myConn = null;
            Statement stmt = null;
            ResultSet rs = null;
            String allCategories;
            try {
                myConn = DBConnector.openConnection();
                stmt = myConn.createStatement();
                rs = stmt.executeQuery("select " + MoniSoftConstants.BUILDING_NAME + "," + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE); // Alle Gebäude lesen
                while (rs.next()) {
                    buildingName = rs.getString(1);
                    allCategories = rs.getString(2);
                    if (allCategories != null) {
                        collections = allCategories.split(","); // Array aller gelisteten CollectionIDs
                        for (String part : collections) {
                            if (!part.isEmpty() && collectionID.equals(Integer.valueOf(part))) { // Die gewünschte Collection wird gelistet
                                buildings.add(buildingName);             // dann aufnehmen
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                Messages.showException(ex);
            } finally {
                DBConnector.closeConnection(myConn, stmt, rs);
            }
        }

        return buildings;
    }

    /**
     * Liefert die IDs der SensorCollections in denen ein Messpunkt vorkommt
     *
     * @param sensorID The sensor
     * @return <code>HashSet<String></code> mit den Namen der SensorCollections
     */
    public synchronized static TreeSet<Integer> getCollectionsIDsIncludingSensor(int sensorID, Integer creator) {
        TreeSet<Integer> resultList = new TreeSet<Integer>();
        ArrayList<Integer> idList;
        Map<String, ArrayList<Integer>> allCollections = getCollectionsOfCreator(creator); // List aller Collections mit ihren Messpunkten
        for (String collection : allCollections.keySet()) {
            idList = allCollections.get(collection);
            for (Integer id : idList) {
                if (id == sensorID) {
                    getCollectionID(collection);
                    resultList.add(getCollectionID(collection));
                }
            }
        }
        return resultList;
    }

    /**
     * Liefert die ID des Messpunktes zurück der für das jeweilige Gebäude die
     * übergebene Verbrauchsgröße darstellt
     *
     * @param buildingID The building
     * @param categoryName The name of the consumption category
     * @return
     */
    public synchronized static Integer getCategorySensorForBuilding(int buildingID, String name) {
        Integer sensorID = null;
        HashSet<Integer> list = getSensorsForCollection(getCollectionID(name));
        for (Integer entry : list) {
            if (SensorInformation.getSensorProperties(entry).getBuildingID() == buildingID) {
                sensorID = entry;
                break; // wenn gefunden rausspringen
            }
        }
        return sensorID;
    }

    /**
     * Removes the given sensor from all collections
     *
     * @param sensorID The ID of the sensor to be deleted
     * @return The number of collection from wich the sensor was deleted
     */
    public synchronized static Integer deleteSensorFromAllCollections(int sensorID) {
        Integer i = 0;
        // loop all collections and call delete method
        for (Integer collectionID : getCollectionsIDsIncludingSensor(sensorID, null)) {
            if (deleteSensorFromCollection(sensorID, collectionID) > 0) {
                i++;
            }
        }

        return i;
    }

    /**
     *
     * @param sensorID The ID of the sensor to be deleted
     * @param collectionID The ID of the collection to delete from
     * @return The number of deletions
     */
    public synchronized static Integer deleteSensorFromCollection(int sensorID, Integer collectionID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Integer i = 0;
        HashSet<Integer> sensorIDs = collectionSensors.get(collectionID);
        // delete ID from listString
        Iterator<Integer> it = sensorIDs.iterator();
        Integer id;
        while (it.hasNext()) {
            id = it.next();
            if (id.equals(sensorID)) {
                it.remove();
                i++;
            }
        }
        updateCollection(collectionID, sensorIDs, false, true);
        return i;
    }

    /**
     * Test is the given collection is a climate correctable collection
     *
     * @param name The name of the collection to check
     * @return <code>true</code> if the collection is climate correctable
     */
    public synchronized static boolean isClimateCorrectionCollection(String name) {
        boolean b = false;
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select climatecorrection from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_NAME + "= ?");
            stmt.setString(1, name);
            rs = stmt.executeQuery(); // Alle Zonen lesen
            if (rs.next()) {
                b = rs.getBoolean(1);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return b;
    }

    /**
     * Adds a collection to the building that this sensor belongs to
     *
     * @param sensorID The ID of the sensor
     * @param collectionID The ID of the collection
     */
    private static void updateBuilding(Integer sensorID, Integer collectionID) {
        ArrayList<Integer> buildingList;
        Integer buildingID;
        buildingList = new ArrayList<Integer>();
        buildingID = SensorInformation.getSensorProperties(sensorID).getBuildingID();
        if (buildingID != null) {
            buildingList.add(buildingID);
        }
        insertCollectionIDToBuildings(buildingList, collectionID);
    }
}
