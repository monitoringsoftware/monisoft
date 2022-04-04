/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.DnDListener;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionPanel;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.HashSet;

/**
 *
 * @author togro
 */
public class SensorCollectionEntryDropListener implements DropTargetListener {

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        boolean accept = false;
        if (dtde.getDropTargetContext().getComponent().getClass() == SensorCollectionPanel.class) {
            SensorCollectionPanel panel = (SensorCollectionPanel) dtde.getDropTargetContext().getDropTarget().getComponent();
            panel.setHeaderColor(Color.GREEN);
            try {
                int id = SensorInformation.getSensorIDFromNameORKey(dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString().split("\u2015")[0]);
                if (id != -1) { // es ist ein Messpunkt und nicht anderes
                    HashSet<SensorProperties> set = panel.getCompareValue().getPropertySet();
                    if (set.size() > 0) {
                        for (SensorProperties entry : set) {// prüfen, ob er schon zur Gruppe gehört oder ob dieses Gebäude schon vorkommt
                            if (entry.getSensorID() == SensorInformation.getSensorIDFromNameORKey(dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString().split("\u2015")[0])) {
                                panel.setHeaderColor(Color.RED);
                                dtde.rejectDrag();
                            } else {
                                accept = true;
                            }
                        }
                    } else {
                        accept = true;
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
                panel.setHeaderColor(Color.RED);
                dtde.rejectDrag();
            } catch (IOException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
                panel.setHeaderColor(Color.RED);
                dtde.rejectDrag();
            }

            if (!accept) {
                panel.setHeaderColor(Color.RED);
                dtde.rejectDrag();
            }
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
//        ZoneEditorPanel panel = (ZoneEditorPanel) dtde.getDropTargetContext().getDropTarget().getComponent();
//        panel.setHeaderColor(Color.red);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragExit(DropTargetEvent dtde) {
        SensorCollectionPanel panel = (SensorCollectionPanel) dtde.getDropTargetContext().getDropTarget().getComponent();
        panel.setHeaderColor(MoniSoftConstants.SENSORCOLLECTION_PANEL_BACKGROUND);
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
//        String target;
        if (dtde.getDropTargetContext().getComponent().getClass() == SensorCollectionPanel.class) {
            try {
                int id = SensorInformation.getSensorIDFromNameORKey(dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString().split("\u2015")[0]);
//                target = ((ZoneEditorPanel)dtde.getDropTargetContext().getComponent()).getTitle();
                ((SensorCollectionPanel) dtde.getDropTargetContext().getComponent()).addSensor(id);
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
