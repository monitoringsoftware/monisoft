/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.TableModels;

import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.References.ReferenceValue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author togro
 */
public class ReferenceEditorTableModel extends AbstractTableModel { //implements TableModel {

    private ArrayList<HashMap<String, ArrayList<Double>>> dataList = new ArrayList<HashMap<String, ArrayList<Double>>>();
    private Vector<String> header;
    private String[] displayHeader;
    private Vector<Integer> columnUnitIDs;

    public ReferenceEditorTableModel(Vector<String> header, Vector<Integer> columnUnitIDs) {
        this.header = header;
        this.columnUnitIDs = columnUnitIDs;

        buildDisplayHeader();
    }

    public void addRow(int buildingID) {
        // Zum Gebäude alle Referenzgrößen holen. Hier tauchen nur die Referenzgrößen auf die für dieses Gebäude definiert sind
        HashMap<String, Double> referenceNames = new HashMap<String, Double>();
        if (BuildingInformation.getBuildingReferences(buildingID) != null) {
            for (ReferenceValue reference : BuildingInformation.getBuildingReferences(buildingID)) {
                referenceNames.put(reference.getName(), reference.getValue());
            }
        }
        
        ArrayList<Double> valueList = new ArrayList<Double>();
        // anhand des Tabellenheaders die werte zuordnen
        Double value;
        for (int i = 1; i < header.size(); i++) { // beginnend ab 1 da 0 der Gebäudename ist
            value = referenceNames.get(header.get(i));
            valueList.add(value);
        }

        HashMap<String, ArrayList<Double>> rowMap = new HashMap<String, ArrayList<Double>>();
        rowMap.put(BuildingInformation.getBuildingNameFromID(buildingID), valueList);

        dataList.add(rowMap);
    }

    public void addColumn(String colname, Integer unitID) {
        header.add(colname);
        columnUnitIDs.add(unitID);
        buildDisplayHeader();
        addColumnToRows();
        fireTableStructureChanged();
    }

    private void addColumnToRows() {
        ArrayList<HashMap<String, ArrayList<Double>>> newDataList = new ArrayList<HashMap<String, ArrayList<Double>>>();
        HashMap<String, ArrayList<Double>> newMap;;
        ArrayList<Double> newValues;
        String key;

        for (HashMap<String, ArrayList<Double>> dataRow : dataList) { // Alle Zeile durchlafen
            Iterator<String> it = dataRow.keySet().iterator(); // Werte der Zeilen
            key = it.next();
            newValues = dataRow.get(key);
            newValues.add(null);
            newMap = new HashMap<String, ArrayList<Double>>();
            newMap.put(key, newValues);
            newDataList.add(newMap);
        }

        dataList = newDataList;

    }

    private void buildDisplayHeader() {
        // Spaltenköpfe bauen mit der entsprechenden Eiheit
        displayHeader = new String[header.size()];
        displayHeader[0] = header.get(0);
        for (int i = 1; i < header.size(); i++) { // beginnend ab 1 da 0 der Gebäudename ist
            displayHeader[i] = "<html><body><center><b>" + header.get(i) + "</b><br>[" + UnitInformation.getUnitNameFromID(columnUnitIDs.get(i - 1)) + "]</center></body></html>";
        }
    }

    public void deleteRow(int building) {
        // TODO
    }

    /**
     * Returns the number of columns.
     *
     * @return
     */
    @Override
    public int getColumnCount() {
        return header.size();
    }

    /**
     * Returns the row count.
     *
     * @return
     */
    @Override
    public int getRowCount() {
        return dataList.size();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        if (col == 0) {
            return String.class;
        } else {
            return Double.class;
        }
    }

    /**
     * Returns the value at the specified cell in the table.
     *
     * @param row the row index.
     * @param column the column index.
     *
     * @return The value.
     */
    @Override
    public Object getValueAt(int row, int column) {
        HashMap<String, ArrayList<Double>> theRow = dataList.get(row);
        if (column == 0) { // in der ersten spalte den Gebäudenamen zurückgeben
            Iterator<String> it = theRow.keySet().iterator();
            return it.next();
        } else { // in allen anderen den double Wert der Referenzgröße
            ArrayList<Double> valueList = theRow.values().iterator().next();
            return valueList.get(column - 1);
        }
    }

    /**
     * Returns the cell edit status.
     *
     * @return true for value column.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        if (col > 0) {
            return true;
        }
        return false;
    }

    /**
     * Sets the value at the specified cell.
     *
     * @param value the value.
     * @param row the row index.
     * @param column the column index.
     */
    @Override
    public void setValueAt(Object value, int row, int column) {
        // Anhand von zeile das Betreffende Gebäude bestimmen
        HashMap<String, ArrayList<Double>> theRow = dataList.get(row);
        ArrayList<Double> valueList = theRow.values().iterator().next();
        valueList.set(column - 1, (Double) value);

        fireTableCellUpdated(row, column);
    }

    /**
     * Returns the column name.
     *
     * @param column the column index.
     *
     * @return The column name.
     */
    @Override
    public String getColumnName(int column) {
        return displayHeader[column];
    }
}
