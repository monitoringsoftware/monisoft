/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.TableModels;

import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author togro
 */
public class SensorTableModel extends AbstractTableModel {

    ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<SensorProperties> sensors = new ArrayList<SensorProperties>(1024);

    public SensorTableModel() {
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("ID"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("NAME"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("BUILDING"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("UNIT"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("FACTOR"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("DESCRIPTION"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("KEY"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("EVENT"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MANUAL"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MIN_WE"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MAX_WE"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MIN_WT"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MAX_WT"));
        columnNames.add("<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("VALIDITY_WE") + "<br>[" + ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MINUTES") + "]</center></html>");
        columnNames.add("<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("VALIDITY_WT") + "<br>" + "[" + ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MINUTES") + "]" + "</center></html>");
        columnNames.add("<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("INTERVAL") + "<br>[" + ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MINUTES") + "]</center></html>");
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("VIRTUAL"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("COUNTER"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("RESET_COUNTER"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MEDIA"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("CONSUMPTION"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("UTC_PLUS"));
        columnNames.add(java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("SUMMER_TIME"));
    }

    public void addSensor(SensorProperties sensor) {
        sensors.add(sensor);
    }

    public void replaceSensor(SensorProperties oldProperties, SensorProperties newPoperties) {
        int replaceLine = sensors.indexOf(oldProperties);  // index des zu ersetzenden Eintrags
        sensors.set(replaceLine, newPoperties);   // dort ersetzen 
        fireTableRowsUpdated(replaceLine, replaceLine);
    }

    @Override
    public int getRowCount() {
        return sensors.size();
    }

    @Override
    public int getColumnCount() {
        return 23;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames.get(col);
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return Integer.class;
            case 1:
                return String.class;
            case 2:
                return JComboBox.class;
            case 3:
                return JComboBox.class;
            case 4:
                return Double.class;
            case 5:
                return String.class;
            case 6:
                return String.class;
            case 7:
                return Boolean.class;
            case 8:
                return Boolean.class;
            case 9:
                return Integer.class;
            case 10:
                return Integer.class;
            case 11:
                return Integer.class;
            case 12:
                return Integer.class;
            case 13:
                return Long.class;
            case 14:
                return Long.class;
            case 15:
                return Integer.class;
            case 16:
                return String.class;
            case 17:
                return Boolean.class;
            case 18:
                return Boolean.class;
            case 19:
                return String.class;
            case 20:
                return Boolean.class;
            case 21:
                return Long.class;
            case 22:
                return Boolean.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
//        if (getColumnName(col).equals(columnNames.get(0)) || getColumnName(col).equals(columnNames.get(6))) {
//            return false;
//        } else {
        return true;
//        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        SensorProperties properties = sensors.get(row);
        switch (col) {
            case 0:
                return (Integer) properties.getSensorID();
            case 1:
                return properties.getSensorName();
            case 2:
                return properties.getBuildingName();
            case 3:
                return properties.getSensorUnit() == null ? "" : properties.getSensorUnit().getUnit();
            case 4:
                return properties.getFactor();
            case 5:
                return properties.getSensorDescription();
            case 6:
                return properties.getKeyName();
            case 7:
                return properties.isEvent() ? Boolean.TRUE : Boolean.FALSE;
            case 8:
                return properties.isManual() ? Boolean.TRUE : Boolean.FALSE;
            case 9:
                return properties.getWELimits()[0] == null ? null : properties.getWELimits()[0];
            case 10:
                return properties.getWELimits()[1] == null ? null : properties.getWELimits()[1];
            case 11:
                return properties.getWTLimits()[0] == null ? null : properties.getWTLimits()[0];
            case 12:
                return properties.getWTLimits()[1] == null ? null : properties.getWTLimits()[1];
            case 13:
                return properties.getMaxChangeTimes()[MoniSoftConstants.WEEKEND] == null ? null : properties.getMaxChangeTimes()[MoniSoftConstants.WEEKEND];
            case 14:
                return properties.getMaxChangeTimes()[MoniSoftConstants.WORKDAY] == null ? null : properties.getMaxChangeTimes()[MoniSoftConstants.WORKDAY];
            case 15:
                return properties.getInterval() == null ? null : properties.getInterval();
            case 16:
                return properties.getVirtualDefinition();
            case 17:
                return properties.isCounter() ? Boolean.TRUE : Boolean.FALSE;
            case 18:
                return properties.isResetCounter() ? Boolean.TRUE : Boolean.FALSE;
            case 19:
                return properties.getMedium() != null ? properties.getMedium() : "";
            case 20:
                return properties.isUsage() ? Boolean.TRUE : Boolean.FALSE;
            case 21:
                return properties.getUtcPlusX() != null ? properties.getUtcPlusX() : 0;
            case 22:
                return properties.isSummerTime() ? Boolean.TRUE : Boolean.FALSE;
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object object, int row, int col) {
        SensorProperties selectedSensor = sensors.get(row);
        switch (col) {
            case 0:
                selectedSensor.setSensorID(((Integer) object).intValue());
                break;
            case 1:
                selectedSensor.setSensorName(((String) object).trim());
                break;
            case 2:
                selectedSensor.setBuildingName(((BuildingProperties) object).getBuildingName());
                break;
            case 3:
                selectedSensor.setSensorUnit(UnitInformation.getUnitFromName((String) object));
                break;
            case 4:
                selectedSensor.setFactor((Double) object);
                break;
            case 5:
                selectedSensor.setSensorDescription(((String) object).trim());
                break;
            case 6:
                selectedSensor.setKeyName(((String) object).trim());
                break;
            case 7:
                selectedSensor.setEvent((Boolean) object);
                break;
            case 8:
                selectedSensor.setManual((Boolean) object);
                break;
            case 9:
                selectedSensor.setWELimits((Integer) object, MoniSoftConstants.MINIMUM);
                break;
            case 10:
                selectedSensor.setWELimits((Integer) object, MoniSoftConstants.MAXIMUM);
                break;
            case 11:
                selectedSensor.setWTLimits((Integer) object, MoniSoftConstants.MINIMUM);
                break;
            case 12:
                selectedSensor.setWTLimits((Integer) object, MoniSoftConstants.MAXIMUM);
                break;
            case 13:
                selectedSensor.setMaxChangeTimes((Long) object, MoniSoftConstants.WEEKEND);
                break;
            case 14:
                selectedSensor.setMaxChangeTimes((Long) object, MoniSoftConstants.WORKDAY);
                break;
            case 15:
                selectedSensor.setInterval((Integer) object);
                break;
            case 16:
                selectedSensor.setVirtualDefinition(((String) object).trim());
                break;
            case 17:
                selectedSensor.setCounter((Boolean) object);
                break;
            case 18:
                selectedSensor.setResetCounter((Boolean) object);
                break;
            case 19:
                selectedSensor.setMedium(((String) object).trim());
                break;
            case 20:
                selectedSensor.setUsage((Boolean) object);
                break;
            case 21:
                selectedSensor.setUtcPlusX((Long) object);
                break;
            case 22:
                selectedSensor.setSummerTime((Boolean) object);
                break;
        }
        fireTableCellUpdated(row, col);
    }

    public void removeAll() {
        while (sensors.size() > 0) {
            fireTableRowsDeleted(0, 0);
            sensors.remove(0);
        }
    }

    public void removeRow(int row) {
        sensors.remove(row);
        fireTableRowsDeleted(row, row);
    }
}
