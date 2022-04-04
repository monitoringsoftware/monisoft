/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.cellEditors;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

/**
 * A cell editor for numbers
 *
 * @author togro
 */
public class NumberCellEditor extends AbstractCellEditor implements TableCellEditor {

    private JFormattedTextField textField;
    private JTable tableUsed;
    private Object oldValue;

    public NumberCellEditor() {
        textField = new JFormattedTextField();
        textField.setEditable(true);
        textField.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Object getCellEditorValue() {

        String name = oldValue.getClass().getName();
        Object returnValue = null;
        try {
            if (name.equals("java.lang.Integer")) {
                returnValue = new java.lang.Integer(textField.getText());
            } else if (name.equals("java.lang.Double")) {
                returnValue = new java.lang.Double(textField.getText());
            } else if (name.equals("java.lang.Float")) {
                returnValue = new java.lang.Float(textField.getText());
            } else if (name.equals("java.lang.Long")) {
                returnValue = new java.lang.Long(textField.getText());
            } else if (name.equals("java.lang.Short")) {
                returnValue = new java.lang.Short(textField.getText());
            } else if (name.equals("java.lang.Byte")) {
                returnValue = new java.lang.Byte(textField.getText());
            } else if (name.equals("java.math.BigDecimal")) {
                returnValue = new java.math.BigDecimal(textField.getText());
            } else if (name.equals("java.math.BigInteger")) {
                returnValue = new java.math.BigInteger(textField.getText());
            }
        } catch (NumberFormatException e) {
        }
        return returnValue;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setValue(value);
        oldValue = value;
        this.tableUsed = table;
        String className = value.getClass().getName();
        System.out.println("Type: " + className);
        return textField;
    }
}
