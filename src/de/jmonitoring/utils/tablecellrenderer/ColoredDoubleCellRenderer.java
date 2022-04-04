package de.jmonitoring.utils.tablecellrenderer;

import java.awt.Color;
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
public class ColoredDoubleCellRenderer extends DefaultTableCellRenderer {

    private DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.GERMAN);
    private Integer[] abberationPercentages;

    public ColoredDoubleCellRenderer(String pattern, Integer[] abberationPercentages) {
        this.formatter.applyPattern(pattern);
        this.abberationPercentages = abberationPercentages;
        setHorizontalAlignment(JLabel.RIGHT);
    }

    public ColoredDoubleCellRenderer(DecimalFormat formatter) {
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
        Double v = (Double) value;
        if (!isSelected) {
            if (v != null) {
                setBackground(Color.WHITE);
                if (v >= abberationPercentages[0]) {
                    setBackground(Color.YELLOW);
                }
                if (v >= abberationPercentages[1]) {
                    setBackground(Color.ORANGE);
                }
                if (v >= abberationPercentages[2]) {
                    setBackground(Color.RED);
                }
                if (v <= abberationPercentages[4]) {
                    setBackground(new Color(205, 255, 205));
                }
                if (v <= abberationPercentages[4]) {
                    setBackground(new Color(51, 204, 0));
                }
                if (v <= abberationPercentages[5]) {
                    setBackground(new Color(0, 151, 53));
                }
            } else {
                setBackground(Color.red);
            }
        }
        return cell;
    }
}
