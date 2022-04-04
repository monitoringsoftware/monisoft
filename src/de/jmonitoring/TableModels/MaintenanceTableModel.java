/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmonitoring.TableModels;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author togro
 */
public class MaintenanceTableModel extends AbstractTableModel { //implements TableModel {
  private Object[][] data;

        /**
         * Creates a new demo table model. 
         * 
         * @param rows  the row count.
         */
        public MaintenanceTableModel(int rows) {
            this.data = new Object[rows][5];
        }

        /**
         * Returns the number of columns.
         * 
         * @return 7.
         */
    @Override
        public int getColumnCount() {
            return 5;
        }

        /**
         * Returns the row count.
         * 
         * @return 1.
         */
    @Override
        public int getRowCount() {
            return 1;
        }

        /**
         * Returns the value at the specified cell in the table.
         * 
         * @param row  the row index.
         * @param column  the column index.
         * 
         * @return The value.
         */
    @Override
        public Object getValueAt(int row, int column) {
            return this.data[row][column];
        }

        /**
         * Returns the cell edit status.
         * 
         * @return true for value column.
         */
    @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 1) {
                return true;
            }
            return false;
        }

        /**
         * Sets the value at the specified cell.
         * 
         * @param value  the value.
         * @param row  the row index.
         * @param column  the column index.
         */
    @Override
        public void setValueAt(Object value, int row, int column) {
            this.data[row][column] = value;
            fireTableCellUpdated(row, column);
        }

        /**
         * Returns the column name.
         * 
         * @param column  the column index.
         * 
         * @return The column name.
         */
    @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MaintenanceTableModel.TIME");
                case 1:
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MaintenanceTableModel.VALUE");
                case 2:
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MaintenanceTableModel.DIFFERENCE");
                case 3:
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MaintenanceTableModel.UNIT");
                case 4:
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MaintenanceTableModel.ATTRIBUTE");
            }
            return null;
        }
    
}
