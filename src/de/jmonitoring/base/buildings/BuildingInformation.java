/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.base.buildings;

import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.base.sensors.SensorInformation;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DBOperations.PreparedStatements;
import de.jmonitoring.Cluster.ClusterInformation;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceValue;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 *
 * @author togro
 */
public class BuildingInformation {

    private static ArrayList<BuildingProperties> buildingList = new ArrayList<BuildingProperties>();
    private static HashMap<Integer, ArrayList<ReferenceValue>> buildingReferencesMap = new HashMap<Integer, ArrayList<ReferenceValue>>();
    private static final String ACCESS_DENIED = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN.");
    private static final String ERROR = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLER BEI DATENBANKABFRAGE");
    public static final int NUM_DELETED_SENSOR_ALLOCATIONS = 0;
    public static final int NUM_DELETED_REFERENCES = 1;
    public static final int NUM_DELETED_COMPARECOLLECTIONS = 2;
    public static final int NUM_DELETED_CLUSTERS = 3;
    private static ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    public BuildingInformation() {
    }

    /**
     * Liefert die <link>BuildingProperties</link> des übergebenen Gebäudes für
     * die Gebäude-ID
     *
     * @param sensor
     */
    public static BuildingProperties getBuildingProperties(int id) {
        BuildingProperties props = new BuildingProperties();
        Iterator<BuildingProperties> it = getBuildingList().iterator();
        while (it.hasNext()) {
            props = it.next();
            if (props.getBuildingID() == id) {
                break;
            } else {
                props = null;
            }
        }
        return props;
    }

    public static void writeAllBuildingProperties() {
        Iterator<BuildingProperties> it = getBuildingList().iterator();
        BuildingProperties props;
        int id;
        while (it.hasNext()) {
            props = it.next();
            id = props.getBuildingID();
            writeBuildingProperty(id);
        }

        ListFiller filler = new ListFiller();
        buildingList = filler.readBuildingList();
        buildingReferencesMap = filler.readBuildingReferencesMap();
    }

    public static void writeBuildingProperty(int id) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        BuildingProperties props = getBuildingProperties(id);
        writeBuildingProperty(id, props);
    }

    // Schreibt die übergebenen BuildingProperties in die Gebäudetabelle
    @SuppressWarnings("unchecked")
    public static void writeBuildingProperty(int id, BuildingProperties props) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        HashMap<String, Object> propertyMap = props.getPropertyList();

        String updateString = "update " + MoniSoftConstants.BUILDING_TABLE + " set ";
        String whereString = " where " + MoniSoftConstants.BUILDING_ID + "=" + id;
        StringBuilder setString = new StringBuilder("");

        // Alle properties durchlaufen
        Iterator<String> it = propertyMap.keySet().iterator();
        String key;
        Object value;
        Object outvalue;

        String separator = "";
        while (it.hasNext()) {
            key = it.next();
            if (!key.equals(MoniSoftConstants.BUILDING_ID)) {
                value = propertyMap.get(key);
                outvalue = value;
                if (value != null && value.getClass().equals(String.class)) {
                    outvalue = "'" + value + "'";
                } else if (key.equals(MoniSoftConstants.BUILDING_COLLECTIONIDS)) {
                    StringBuilder sb = new StringBuilder(256);
                    String delim = "";
                    for (Integer entry : (ArrayList<Integer>) value) {
                        sb.append(delim);
                        sb.append(entry);
                        delim = ",";
                    }
                    outvalue = "'" + sb.toString() + "'";
                }
                setString.append(separator);
                setString.append(MoniSoftConstants.BUILDING_TABLE);
                setString.append(".");
                setString.append(key);
                setString.append("=");
                setString.append(outvalue);
                separator = ",";
            }
        }
        if (!setString.toString().isEmpty()) {
            try {
                myConn = DBConnector.openConnection();
                stmt = myConn.createStatement();
//                System.out.println(updateString + setString.toString() + whereString);
                stmt.executeUpdate(updateString + setString.toString() + whereString);
                logger.info("Updated building " + props.getBuildingName());
            } catch (MySQLSyntaxErrorException ex) {
                Messages.showException(ex);
                logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
                Messages.showOptionPane(ACCESS_DENIED);
            } catch (SQLException ex) {
                Messages.showException(ex);
            } finally {
                DBConnector.closeConnection(myConn, stmt, null);
            }
        }
    }

    /**
     *
     * @param unit
     * @return
     */
    public static String getBuildingNameFromID(int id) {
        String building = null;
        if (getBuildingList() != null) {

            for (BuildingProperties props : getBuildingList()) {
                if (props.getBuildingID() == id) {
                    building = props.getBuildingName();
                    break;
                }
            }
        }
        return building;
    }

    /**
     *
     * @param name
     * @return
     */
    public static Integer getBuildingIDFromName(String name) {
        Integer id = null;
        if (!name.trim().isEmpty() || getBuildingList() == null) { // Keine Name vorhanden - gleich beenden
            for (BuildingProperties props : getBuildingList()) {
                if (props.getBuildingName().equals(name.trim())) {
                    id = props.getBuildingID();
                    break;
                }
            }
        }
        return id;
    }

    /**
     * Legt eine neues Gebäude an und schreibt danach dessen Properties in die
     * DB.
     *
     * @param props Properties des zu erzeugenden Gebäudes
     * @return id des erzeugten Gebäudes oder <code>null</code> im Fehlerfall
     */
    public static Integer createNewBuilding(BuildingProperties props) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return 0;
        }
        Connection myConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Integer id = null;
        try {
            myConn = DBConnector.openConnection();
            pstmt = myConn.prepareStatement(PreparedStatements.INSERT_NEW_BUILDING_NAME, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, props.getBuildingName()); //TODO hier noch '' ???
            pstmt.executeUpdate();
            logger.info("Created new building '" + props.getBuildingName() + "'");

            rs = pstmt.getGeneratedKeys();
            // id des neuen Eintrags ermitteln
            if (rs.next()) { // wenn der Eintrag erfolgreich war
                id = rs.getInt(1);
                writeBuildingProperty(id, props); // schreiben der Properties
                //Liste neu einlesen
                ListFiller filler = new ListFiller();
                buildingList = filler.readBuildingList();
                buildingReferencesMap = filler.readBuildingReferencesMap();
            }

        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(ACCESS_DENIED);
        } catch (Exception e) {
            Messages.showMessage(ERROR + "(createNewBuilding): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, pstmt, rs);
        }

        return id;
    }

    // gibt eine Liste der belegten Gebäudeflächen zurück
    public static HashMap<String, Double> getAreasOfBuilding(int id) {
        HashMap<String, Double> map = new HashMap<String, Double>(20);
        return map;
    }

    /**
     *
     * @return
     */
    public static ArrayList<BuildingProperties> getBuildingList() {
        return buildingList;
    }

    /**
     *
     * @param list
     */
    public static void setBuildingList(ArrayList<BuildingProperties> list) {
        buildingList = list;
    }

    /**
     * Returns a HashMap of all buildings and their references. The building-ID
     * is used as key
     *
     * @return
     */
    public static HashMap<Integer, ArrayList<ReferenceValue>> getBuildingReferencesMap() {
        return buildingReferencesMap;
    }

    public static void setBuildingReferencesMap(HashMap<Integer, ArrayList<ReferenceValue>> buildingReferencesMap) {
        BuildingInformation.buildingReferencesMap = buildingReferencesMap;
    }

    /**
     *
     */
    public static void clearBuildingList() {
        if (buildingList != null) {
            buildingList.clear();
        }
    }

    /**
     * Liefert allee Gebäudenamen als Array
     *
     * @return
     */
    public static TreeSet<String> getBuildingNames() {
        TreeSet<String> list = new TreeSet<String>();
        for (BuildingProperties props : getBuildingList()) {
            list.add(props.getBuildingName());
        }
        return list;
    }

    /**
     * Liefert alle zu einem Gebäude gehörenden Verbrauchskategorien
     * (SensorCollections) aus und gibt deren Namen zurück
     *
     * @param buildingID
     * @return Eine <code>ArrayList</code> mit den Namen der SensorCollections
     */
    public static HashSet<String> getUsageCategoryNames(int buildingID) {
        HashSet<String> categoryList = new HashSet<String>(50);
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<Integer> colIDs = new ArrayList<Integer>(50);

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "=" + buildingID);
            String list;
            while (rs.next()) {
                list = rs.getString(1);
                if (list == null || list.length() == 0) {
                    continue;
                }
                for (String s : list.split(",")) {
                    colIDs.add(Integer.valueOf(s)); // id numerisch umwandlen
                }
            }

            // Nun alle IDs durchlaufen und Namen holen
            for (Integer id : colIDs) {
                stmt = myConn.createStatement();
                rs = stmt.executeQuery("select " + MoniSoftConstants.SENSORCOLLECTION_NAME + " from " + MoniSoftConstants.SENSORCOLLECTION_TABLE + " where " + MoniSoftConstants.SENSORCOLLECTION_ID + "=" + id + " and " + MoniSoftConstants.SENSORCOLLECTION_CREATOR + "=" + SensorCollectionHandler.COMPARE_COLLECTION);
                String s;
                while (rs.next()) {
                    s = rs.getString(1);
                    categoryList.add(rs.getString(1));
                }
            }
        } catch (Exception e) {
            Messages.showMessage(ERROR + " (getUsageCategories): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return categoryList;
    }

    public static HashSet<Integer> getUsageCategoryIDs(int buildingID) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        HashSet<Integer> colIDs = new HashSet<Integer>(50);

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.BUILDING_COLLECTIONIDS + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "=" + buildingID);
            String list;
            while (rs.next()) {
                list = rs.getString(1);
                if (list == null || list.length() == 0) {
                    continue;
                }
                for (String s : list.split(",")) {
                    colIDs.add(Integer.valueOf(s)); // id numerisch umwandlen
                }
            }
        } catch (Exception e) {
            Messages.showMessage(ERROR + " (getUsageCategories): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return colIDs;
    }

    /**
     * Return all {@link ReferenceValue}s a building has. If the building is not
     * known an empty list will be returned
     *
     * @param buildingID of the building
     * @return
     */
    public static ArrayList<ReferenceValue> getBuildingReferences(int buildingID) {
        if (buildingReferencesMap.containsKey(buildingID)) {
            return buildingReferencesMap.get(buildingID);
        }

        return new ArrayList<ReferenceValue>(); // if the key is not know return a empty list
    }

    /**
     * Gibt den ReferenceValue mit dem entsprechenden Namen für ein Gebäude
     * zurück
     *
     * @param buildingID id des Gebäudes
     * @param name Name der gewünschen Referenzgröße
     * @return
     */
    public static ReferenceValue getBuildingreference(int buildingID, String name) {
        ReferenceValue reference = null;
        for (ReferenceValue ref : getBuildingReferences(buildingID)) {
            if (ref.getName().equals(name)) {
                reference = ref;
                break; // gesucht und gefunden -> es much nicht weitergesucht werden
            }
        }

        return reference;
    }

    /**
     * Löscht alle Bezugsgrößen einen Gebäudes und schreibt die übergebenen
     * neuen Bezugsgrößen
     *
     * @param list Liste der Bezugsgrößen
     * @param buildingID id des Gebäudes
     */
    public static void updateBuildingReferences(ArrayList<ReferenceValue> list, int buildingID) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        PreparedStatement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("delete from " + MoniSoftConstants.REFERENCEMAP_TABLE + " where " + MoniSoftConstants.REFERENCEMAP_BUILDING_ID + "=?");
            stmt.setInt(1, buildingID);
            stmt.executeUpdate();
            logger.info("Deleted all references for building " + BuildingInformation.getBuildingNameFromID(buildingID));
            stmt = myConn.prepareStatement("insert into " + MoniSoftConstants.REFERENCEMAP_TABLE + " set " + MoniSoftConstants.REFERENCEMAP_NAME + "= ?," + MoniSoftConstants.REFERENCEMAP_VALUE + "=?," + MoniSoftConstants.REFERENCEMAP_BUILDING_ID + "=?");
            for (ReferenceValue reference : list) {
                stmt.setString(1, reference.getName());
                stmt.setDouble(2, reference.getValue());
                stmt.setInt(3, buildingID);
                stmt.executeUpdate();
            }
            logger.info("Added " + list.size() + " references for building " + BuildingInformation.getBuildingNameFromID(buildingID));

            setBuildingReferencesMap(new ListFiller().readBuildingReferencesMap());
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(ACCESS_DENIED);
        } catch (Exception e) {
            Messages.showMessage(ERROR + " (updateBuildingReferences): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    public static void writeBuildingImage(int buildingID, File imageFile) {
        Connection myConn = null;
        String updateString;
        PreparedStatement pstmt = null;
        try {
            myConn = DBConnector.openConnection();
            if (imageFile == null) {
                updateString = "update " + MoniSoftConstants.BUILDING_TABLE + " set image = null where " + MoniSoftConstants.BUILDING_ID + "= ?";
                pstmt = myConn.prepareStatement(updateString);
                pstmt.setInt(1, buildingID);
            } else {
                FileInputStream fis = new FileInputStream(imageFile);
                updateString = "update " + MoniSoftConstants.BUILDING_TABLE + " set image= ? where " + MoniSoftConstants.BUILDING_ID + "= ?";
                pstmt = myConn.prepareStatement(updateString);
                pstmt.setInt(2, buildingID);
                pstmt.setBinaryStream(1, fis);
            }
            logger.info("Changed image for building" + BuildingInformation.getBuildingNameFromID(buildingID));
        } catch (FileNotFoundException ex) {
            Messages.showException(ex);
            Messages.showMessage("File not found" + "\n", true);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(ACCESS_DENIED);
        } catch (Exception e) {
            Messages.showMessage(ERROR + " (writeBuildingImage): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, pstmt, null);
        }
    }

    public static BufferedImage readBuildingImage(int buildingID) {
        BufferedImage image = null;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select image from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "=" + buildingID);
            while (rs.next()) {
                InputStream is = rs.getBinaryStream(1);
                image = is == null ? null : ImageIO.read(is);
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
        } catch (Exception e) {
            Messages.showMessage(ERROR + " (readBuildingImage): " + e.getMessage() + "\n", true);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return image;

    }

    /**
     * Returns all sensors that are associated with this building
     *
     * @param buildingID
     * @return
     */
    public static ArrayList<Integer> getBuildingSensors(int buildingID) {
        ArrayList<Integer> sensorList = new ArrayList<Integer>();

        // Loop all Sensors
        for (SensorProperties props : SensorInformation.getSensorList()) {
            if (props.getBuildingID() != null && props.getBuildingID() == buildingID) {
                sensorList.add(props.getSensorID());
            }
        }

        return sensorList;
    }

    /**
     *
     * @param id
     * @param component
     * @return
     */
    public static HashMap<Integer, Integer> deleteBuilding(int id) {
        Connection myConn = null;
        Statement stmt = null;
        HashMap<Integer, Integer> deletedMap = new HashMap<Integer, Integer>();
        String name = BuildingInformation.getBuildingNameFromID(id);
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            deletedMap = deleteAllBuildingOccurences(id);
            stmt.executeUpdate("delete from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "=" + id);
            logger.info("Gebäude '" + name + "' (" + id + ") aus Gebäudeliste gelöscht");
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

    public static HashMap<Integer, Integer> deleteAllBuildingOccurences(int buildingID) {
        Connection myConn = null;
        Statement stmt = null;
        HashMap<Integer, Integer> deletion_resultMap = new HashMap<Integer, Integer>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            // delete from T_Referenceces
            Integer removedReferences = removeReferencesOfBuilding(buildingID);

            // delete from T_SensorCollections
            Integer removedSensorCollectionLinks = removeBuildingFromSensorCollections(buildingID);

            // delete from T_Sensors
            Integer removedSensorLinks = removeBuildingIDromSensors(buildingID);

            // delete from T_Clusters
            Integer removedCluserOccurences = removeBuildingFromClusters(buildingID);

            // assigning results to containter
            deletion_resultMap.put(NUM_DELETED_SENSOR_ALLOCATIONS, removedSensorLinks);
            deletion_resultMap.put(NUM_DELETED_REFERENCES, removedReferences);
            deletion_resultMap.put(NUM_DELETED_COMPARECOLLECTIONS, removedSensorCollectionLinks);
            deletion_resultMap.put(NUM_DELETED_CLUSTERS, removedCluserOccurences);

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
     * Deletes this building from all SensorProperties that are linked to it and
     * writes the chnages to the database.
     *
     * @param buildingID
     * @return
     */
    public static Integer removeBuildingIDromSensors(int buildingID) {
        Integer i = 0;
        for (int id : getBuildingSensors(buildingID)) {
            SensorInformation.getSensorProperties(id).setBuildingID(null);
            SensorInformation.writeSensorProperty(id);
            i++;
        }

        return i;
    }

    /**
     * Delete all references of a building
     *
     * @param buildingID
     */
    public static Integer removeReferencesOfBuilding(int buildingID) {
        Connection myConn = null;
        Statement stmt = null;
        Integer count = 0;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            count = stmt.executeUpdate("delete from " + MoniSoftConstants.REFERENCEMAP_TABLE + " where " + MoniSoftConstants.REFERENCEMAP_BUILDING_ID + "=" + buildingID);
            logger.info("Deleted all references of building " + buildingID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBMaintenance.SIE HABEN NICHT DIE NÖTIGEN RECHTE UM VERÄNDERUNGEN VORZUNEHMEN."));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return count;
    }

    public static Integer removeBuildingFromSensorCollections(int buildingID) {
        Integer count = SensorCollectionHandler.removeSensorsOfBuildingFromAllCollections(buildingID);

        return count;
    }

    public static Integer removeBuildingFromClusters(int buildingID) {
        return ClusterInformation.removeBuildingFromAllClusters(buildingID);
    }
}
