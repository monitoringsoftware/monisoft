/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.DnDListener;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author togro
 */
public class SensorSelectorDropListener implements DropTargetListener {

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        boolean accept = false;
        if (dtde.getDropTargetContext().getComponent().getClass() == JComboBox.class) { // nur prüfen wenn es eine Combobox ist
            ComboBoxModel cm = ((JComboBox) dtde.getDropTargetContext().getDropTarget().getComponent()).getModel();
            int size = cm.getSize();
            try {
                int id = SensorInformation.getSensorIDFromNameORKey(dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString().split("\u2015")[0]);
                if (id != -1) {
                    SensorProperties dragedProperties = SensorInformation.getSensorProperties(id);
                    for (int i = 0; i < size; i++) {
                        if (cm.getElementAt(i).equals(dragedProperties)) {
                            accept = true;
                        }
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
                dtde.rejectDrag();
            } catch (IOException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
                dtde.rejectDrag();
            }
        }

        if (!accept) {
            dtde.rejectDrag();
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dtde.getDropTargetContext().getComponent() instanceof JComboBox) {
            try {
//            ((JComboBox) dtde.getDropTargetContext().getDropTarget().getComponent()).setSelectedIndex(5);
                int id = SensorInformation.getSensorIDFromNameORKey(dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString().split("\u2015")[0]);
                ((JComboBox) dtde.getDropTargetContext().getDropTarget().getComponent()).setSelectedItem(SensorInformation.getSensorProperties(id));
            } catch (UnsupportedFlavorException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            } catch (IOException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }
        }
    }
}
