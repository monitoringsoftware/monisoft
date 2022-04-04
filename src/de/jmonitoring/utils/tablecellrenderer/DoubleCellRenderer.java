/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.tablecellrenderer;

import java.awt.Component;
import java.text.DecimalFormat;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author togro
 */
public class DoubleCellRenderer extends DefaultTableCellRenderer {

    private DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN);



    public DoubleCellRenderer(String pattern) {
        this.formatter.applyPattern(pattern);
        setHorizontalAlignment(JLabel.RIGHT);
    }

    public DoubleCellRenderer(DecimalFormat formatter) {
        this.formatter = formatter;
        setHorizontalAlignment(JLabel.RIGHT);
    }

    @Override
    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(((Double) value).doubleValue()));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        return cell;
    }
}
