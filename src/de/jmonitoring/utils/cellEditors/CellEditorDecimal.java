/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.utils.cellEditors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * A cell editor used to edit decimal format values in tables
 *
 * @author togro
 */
public class CellEditorDecimal extends DefaultCellEditor {

    private JFormattedTextField ftf;
    private NumberFormat format;
    private Integer scale = null;
    private boolean DEBUG = true;

    public CellEditorDecimal(int p_scale) {
        super(new JFormattedTextField());
        scale = new Integer(p_scale);
        init();
    }

    private void init() {
        ftf = (JFormattedTextField) getComponent();
        format = NumberFormat.getNumberInstance();
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setFormat(format);

        if (scale != null) {
            format.setMaximumFractionDigits(scale.intValue());
            format.setMinimumFractionDigits(scale.intValue());
        }

        ftf.setFormatterFactory(new DefaultFormatterFactory(formatter));
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) { //The text is invalid.
                    System.out.println("The entered text is invalid");
                } else {
                    try { //The text is valid,
                        ftf.commitEdit();//so use it.
                        ftf.postActionEvent(); //stop editing
                    } catch (java.text.ParseException exc) {
                    }
                }
            }
        });

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JFormattedTextField ftf = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
        ftf.setValue(value);
        return ftf;
    }

    @Override
    public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        Object o = ftf.getValue();

        if (o == null) {
            return null;
        } else if (o instanceof Long) {
            return new Double(((Long) o).doubleValue());
        } else if (o instanceof Number) {
            return (Double) o;
        } else {

            try {
                return format.parseObject(o.toString());
            } catch (ParseException exc) {
                return null;
            }
        }
    }
}