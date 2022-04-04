/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;

/**
 *
 * @author togro
 */
public class MyColumnFactory extends ColumnFactory {

    @Override
    protected int getRowCount(JXTable table) {
        return (table.getModel().getRowCount() / 4) < 1 ? table.getModel().getRowCount() / 4 : 1;
    }
}
