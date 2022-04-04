/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.TableModels;

import de.jmonitoring.base.buildings.BuildingProperties;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author togro
 */
public class BuildingTableModel extends AbstractTableModel {

    private ArrayList<BuildingProperties> buildings = new ArrayList<BuildingProperties>();

    public void addBuilding(BuildingProperties building) {
        buildings.add(building);
    }

    public void replaceBuilding(BuildingProperties oldProperties, BuildingProperties newPoperties) {
        int replaceLine = buildings.indexOf(oldProperties);  // index des zu ersetzenden Eintrags
        buildings.set(replaceLine, newPoperties);   // dort ersetzen
        fireTableRowsUpdated(replaceLine, replaceLine);
    }

    @Override
    public int getRowCount() {
        return buildings.size();
    }

    @Override
    public int getColumnCount() {
        return 10;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("BUILDING_ID");
            case 1:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("NAME");
            case 2:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("OBJECT_ID");
            case 3:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("DESCRIPTION");
            case 4:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("ZIP");
            case 5:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("CITY");
            case 6:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("STREET");
            case 7:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("CONTACT");
            case 8:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("PHONE");
            case 9:
                return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("NETWORK");
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return Integer.class;
            case 3:
                return String.class;
            case 4:
                return Integer.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            case 7:
                return String.class;
            case 8:
                return Long.class;
            case 9:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col != 0) {  // 0 ist die ID, evtl. Namen sperren?
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        BuildingProperties properties = (BuildingProperties) buildings.get(row);
        switch (col) {
            case 0:
                return (Integer) properties.getBuildingID();
            case 1:
                return properties.getBuildingName();
            case 2:
                return properties.getObjectID();
            case 3:
                return properties.getBuildingDescription();
            case 4:
                return properties.getPlz();
            case 5:
                return properties.getCity();
            case 6:
                return properties.getStreet();
            case 7:
                return properties.getContact();
            case 8:
                return properties.getPhone();
            case 9:
                return properties.getNetworking();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object object, int row, int col) {
        BuildingProperties selectedBuilding = (BuildingProperties) buildings.get(row);
        switch (col) {
            case 0:
                selectedBuilding.setBuildingID(((Integer) object));
                break;
            case 1:
                selectedBuilding.setBuildingName(((String) object).trim());
                break;
            case 2:
                selectedBuilding.setObjectID(((Integer) object));
                break;
            case 3:
                selectedBuilding.setBuildingDescription(((String) object).trim());
                break;
            case 4:
                selectedBuilding.setPlz(((Integer) object));
                break;
            case 5:
                selectedBuilding.setCity(((String) object).trim());
                break;
            case 6:
                selectedBuilding.setStreet(((String) object).trim());
                break;
            case 7:
                selectedBuilding.setContact(((String) object).trim());
                break;
            case 8:
                selectedBuilding.setPhone(((Long) object));
                break;
            case 9:
                selectedBuilding.setNetworking(((String) object).trim());
                break;
        }
        fireTableCellUpdated(row, col);
    }

    void removeAll() {
        while (buildings.size() > 0) {
            fireTableRowsDeleted(0, 0);
            buildings.remove(0);
        }
    }
}
