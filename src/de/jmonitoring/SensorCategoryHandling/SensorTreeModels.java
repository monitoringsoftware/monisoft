/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.SensorCategoryHandling;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.Messages;
import java.sql.Connection;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.UnitCalulation.Unit;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author togro
 */
public class SensorTreeModels {

    private static final char seperatorChar = '\u2015';

    /**
     * Generiert ein TreeModel mit dem Alphabet als Kategorie
     *
     * @return
     */
    public static TreeModel alphabeticTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("Alphabetical"));
        // einen Hash mit Knoten für das Alphabet bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();
        // Alphabet durchlaufen
        char anfang = '0';
        char ende = 'Z';
        String first, name, entry;

        while (anfang <= ende) {
            CategoryTreeNode catNode = new CategoryTreeNode(String.valueOf(anfang));
            treeMap.put(String.valueOf(anfang), catNode);
            anfang++;
        }

        for (SensorProperties props : SensorInformation.getSensorList()) {
            name = SensorInformation.getDisplayName(props);
            first = name.substring(0, 1).toUpperCase();
            entry = name + seperatorChar + props.getSensorDescription();

            treeMap.get(first).add(new SensorTreeNode(entry));
        }

        addNumberOftEntrys(treeMap, root);

        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein TreeModel mit den Einheiten als Kategorie
     *
     * @return
     */
    public static TreeModel unitTreeModel() {
        String unit, name, entry;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("UNITS"));
        // einen Hash mit Knoten für die Einheiten bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();

        // Einheiten einlesen
        for (Unit u : UnitInformation.getUnitList().values()) {
            treeMap.put(u.getUnit(), new CategoryTreeNode(u.getUnit()));
        }

        // die Messpunkte zuordnen
        for (SensorProperties props : SensorInformation.getSensorList()) {
            name = SensorInformation.getDisplayName(props);
            unit = props.getSensorUnit().getUnit();
            entry = name + seperatorChar + props.getSensorDescription();
            treeMap.get(unit).add(new SensorTreeNode(entry));
        }

        addNumberOftEntrys(treeMap, root);

        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein TreeModel mit dem Medium als Kategorie
     *
     * @return
     */
    public static TreeModel mediumTreeModel() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Object o;
        DefaultMutableTreeNode n;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("MEDIA"));
        // einen Hash mit Knoten für die Einheiten bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            // Einheiten einlesen
            rs = stmt.executeQuery("select medium from T_Sensors");
            String medium = "";
            while (rs.next()) {
                medium = rs.getString(1);
                if (medium != null) {
                    treeMap.put(medium, new CategoryTreeNode(medium));
                }
            }

            // die Messpunkte zuordnen
            rs = stmt.executeQuery("select medium,sensor from T_Sensors");
            String name, entry;
            while (rs.next()) {
                medium = rs.getString(1);
                name = rs.getString(2);
                if (medium != null) {
                    entry = name + seperatorChar + SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(name)).getSensorDescription();
                    treeMap.get(medium).add(new SensorTreeNode(entry));
                }

            }

            addNumberOftEntrys(treeMap, root);
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);

        }
        if (root.getChildCount() == 0) {
            root = new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("NO_MEDIA_DEFINED"));
        }
        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein TreeModel mit den Typen als Kategorie
     *
     * @return
     */
    public static TreeModel typeTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("TYPES"));
        // einen Hash mit Knoten für die Einheiten bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();


        treeMap.put(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("COUNTER"), new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("COUNTER")));
        treeMap.put(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("EVENT"), new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("EVENT")));
        treeMap.put(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("STANDARD"), new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("STANDARD")));
        treeMap.put(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("MANUAL"), new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("MANUAL")));
        treeMap.put(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("VIRTUAL"), new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("VIRTUAL")));

        String name;
        String entry;
        boolean standard;

        // die Messpunkte zuordnen
        for (SensorProperties props : SensorInformation.getSensorList()) {
            name = SensorInformation.getDisplayName(props);
            entry = name + seperatorChar + props.getSensorDescription();
            standard = true;

            if (props.isCounter()) {
                treeMap.get(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("COUNTER")).add(new SensorTreeNode(entry));
                standard = false;
            }
            if (props.isEvent()) {
                treeMap.get(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("EVENT")).add(new SensorTreeNode(entry));
                standard = false;
            }
            if (props.isManual()) {
                treeMap.get(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("MANUAL")).add(new SensorTreeNode(entry));
            }
            if (props.isVirtual() && !props.getVirtualDefinition().isEmpty()) {
                treeMap.get(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("VIRTUAL")).add(new SensorTreeNode(entry));
            }
            if (standard) {
                treeMap.get(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("STANDARD")).add(new SensorTreeNode(entry));
            }

        }

        addNumberOftEntrys(treeMap, root);

        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein flaches TreeModel
     *
     * @return
     */
    public static TreeModel flatTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("ALL_SENSORS"));

        for (SensorProperties props : SensorInformation.getSensorList()) {
            String name = SensorInformation.getDisplayName(props);
            root.add(new SensorTreeNode(name + seperatorChar + props.getSensorDescription()));
        }

        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein flaches, sortiertes TreeModel
     *
     * @return
     */
    public static TreeModel flatSortedTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("ALL_SENSORS"));
        TreeMap<String, SensorProperties> treeMap = new TreeMap<String, SensorProperties>();

        for (SensorProperties props : SensorInformation.getSensorList()) {
            String name = SensorInformation.getDisplayName(props);
            treeMap.put(name, props);
        }

        for (String sensorName : treeMap.keySet()) {
            SensorProperties p = treeMap.get(sensorName);
            root.add(new SensorTreeNode(sensorName + seperatorChar + p.getSensorDescription()));
        }

        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein TreeModel mit den Gebäuden als Kategorie
     *
     * @return
     */
    public static TreeModel buildingTreeModel() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("BUILDING"));
        String noBuilding = java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("WITHOUT_BUILDING");
        // einen Hash mit Knoten für die Gebäude bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();


        for (BuildingProperties props : BuildingInformation.getBuildingList()) {
            treeMap.put(props.getBuildingName(), new CategoryTreeNode(props.getBuildingName()));
        }

        // create node of sensors with no building 
        treeMap.put(noBuilding, new CategoryTreeNode(noBuilding));

        for (SensorProperties props : SensorInformation.getSensorList()) {
            String name = SensorInformation.getDisplayName(props);
            String entry = name + seperatorChar + props.getSensorDescription();
            String building = props.getBuildingName();

            if (building == null || building.isEmpty()) {
                treeMap.get(noBuilding).add(new SensorTreeNode(entry));
            } else {
                treeMap.get(building).add(new SensorTreeNode(entry));
            }
        }

        addNumberOftEntrys(treeMap, root);

        if (root.getChildCount() == 0) {
            root = new CategoryTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("NO_BUILDINGS"));
        }
        return new DefaultTreeModel(root);
    }

    /**
     * Generiert ein TreeModel mit den Nuterdefinierten Vergleichskategorien als
     * Kategorie
     *
     * @return
     */
    public static TreeModel compareCategoryModel() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Object o;
        DefaultMutableTreeNode n;

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCategoryHandling/Bundle").getString("BUILDING"));
        // einen Hash mit Knoten für die Einheiten bilden
        TreeMap<String, CategoryTreeNode> treeMap = new TreeMap<String, CategoryTreeNode>();

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            // Einheiten einlesen
            rs = stmt.executeQuery("select BuildingName from T_Building");
            String building;
            while (rs.next()) {
                building = rs.getString(1);
                treeMap.put(building, new CategoryTreeNode(building));
            }

            // die Messpunkte zuordnen
            rs = stmt.executeQuery("select b.BuildingName,s.sensor from T_Sensors s, T_Building b where b.id_Building=s.T_Building_id_Building");
            String name, entry;
            while (rs.next()) {
                building = rs.getString(1);
                name = rs.getString(2);
                entry = name + seperatorChar + SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(name)).getSensorDescription();
                treeMap.get(building).add(new SensorTreeNode(entry));
            }

            addNumberOftEntrys(treeMap, root);

        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return new DefaultTreeModel(root);
    }

    private static DefaultMutableTreeNode addNumberOftEntrys(TreeMap<String, CategoryTreeNode> treeMap, DefaultMutableTreeNode root) {
        Iterator<String> it = treeMap.keySet().iterator();
        while (it.hasNext()) {
            DefaultMutableTreeNode n = treeMap.get(it.next());
            Object o = n.getUserObject();

            if (n.getChildCount() > 0) {
                n.setUserObject(o + " (" + n.getChildCount() + ")");
                root.add(n);
            }
        }

        return root;
    }
}
