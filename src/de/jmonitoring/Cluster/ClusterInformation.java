/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Cluster;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.Cluster.Cluster;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class can be used to gather information on clusters
 *
 * @author togro
 */
public class ClusterInformation {

    private static TreeMap<String, Cluster> clusterMap = new TreeMap<String, Cluster>();

    public static void writeCluster(Cluster cluster) {
        Connection myConn = null;
        Statement stmt = null;
        String query = "replace into " + MoniSoftConstants.CLUSTER_TABLE + " (" + MoniSoftConstants.CLUSTER_NAME + "," + MoniSoftConstants.CLUSTER_KAT + "," + MoniSoftConstants.CLUSTER_BUILDINGS + ") values ";

        // Alle Gruppen dieses Clusters holen ( jede Gruppe ist eine Zeile inder DB) und durchlaufen
        HashMap<Integer, TreeSet<Integer>> clusterGroups = cluster.getClusterGroups();
        String addString = "";
        TreeSet<Integer> buildings;
        String insertSep = "";

        for (Integer group : clusterGroups.keySet()) {
            buildings = cluster.getBuildingsForGroup(group);
            String sep = "";
            String buildingString = "";
            for (Integer building : buildings) {
                buildingString += sep + building.toString();
                sep = ",";
            }

            addString += insertSep + "('" + cluster.getName() + "'," + group + ",'" + buildingString + "')";
            insertSep = ",";
        }

        query += addString;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate(query);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ClusterInformation.CLUSTER") + " '" + cluster.getName() + "' " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ClusterInformation.WITH") + " " + cluster.getGroupCount() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ClusterInformation.GROUPS_WRITTEN") + ".\n", true);
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        // globale Liste aktualisieren
        setGlobalClusterMap(new ListFiller().readClusterList());
    }

    /**
     * Löscht den übergebenen Cluster und alle seine Gruppen aus der DB
     */
    public static void deleteCluster(String name) {
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.CLUSTER_TABLE + " where " + MoniSoftConstants.CLUSTER_NAME + "='" + name + "'");
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        // globale Liste aktualisieren
        setGlobalClusterMap(new ListFiller().readClusterList());
    }

    public static boolean clusterExists(String cluster) {
        boolean exists = false;
        for (String name : clusterMap.keySet()) {
            if (name.toLowerCase().equals(cluster.toLowerCase())) {
                exists = true;
            }
        }
        return exists;
    }

    public static boolean clusterExists(Cluster cluster) {
        boolean exists = false;
        for (String name : clusterMap.keySet()) {
            if (name.equals(cluster.getName())) {
                exists = true;
            }
        }
        return exists;
    }

    public static Cluster getCluster(String name) {
        for (Cluster cluster : clusterMap.values()) {
            if (cluster.getName().equals(name)) {
                return cluster;
            }
        }
        return null;
    }

    public static ArrayList<String> getClusterNamesForBuilding(Integer buildingID) {
        ArrayList<String> list = new ArrayList<String>();
        TreeSet<Integer> buildingSet;
        for (Cluster cluster : clusterMap.values()) {
            // Clustergruppen durchlaufen
            for (int group = 1; group <= cluster.getGroupCount(); group++) {
                buildingSet = cluster.getBuildingsForGroup(group);
                if (buildingSet != null && buildingSet.contains(buildingID)) {
                    list.add(cluster.getName());
                }
            }
        }

        return list;
    }

    public static ArrayList<Cluster> getClustersForBuilding(Integer buildingID) {
        ArrayList<Cluster> list = new ArrayList<Cluster>();
        TreeSet<Integer> buildingSet;
        for (Cluster cluster : clusterMap.values()) {
            // Clustergruppen durchlaufen
            for (int group = 1; group <= cluster.getGroupCount(); group++) {
                buildingSet = cluster.getBuildingsForGroup(group);
                if (buildingSet != null && buildingSet.contains(buildingID)) {
                    list.add(cluster);
                }
            }
        }

        return list;
    }

    public static void addClusterToGlobalMap(Cluster cluster) {
        clusterMap.put(cluster.getName(), cluster);
    }

    public static void removeClusterFromGlobalMap(Cluster cluster) {
        clusterMap.remove(cluster.getName());
    }

    public static void setGlobalClusterMap(TreeMap<String, Cluster> map) {
        clusterMap = map;
    }

    public static TreeMap<String, Cluster> getGlobalClusterMap() {
        return clusterMap;
    }

    public static void writeAllClusters() {
        for (Cluster cluster : clusterMap.values()) {
            writeCluster(cluster);
        }
    }

    public static void removeBuildingFromCluster(int buildingId, int clusterID) {
    }

    public static Integer removeBuildingFromAllClusters(int buildingId) {
        Integer count = 0;
        for (Cluster cluster : getClustersForBuilding(buildingId)) {
            for (int group = 1; group <= cluster.getGroupCount(); group++) {
                cluster.removeBuildingFromClusterGroup(group, buildingId);
                count++;
            }
            ClusterInformation.writeCluster(cluster);
        }

        return count;
    }
}
