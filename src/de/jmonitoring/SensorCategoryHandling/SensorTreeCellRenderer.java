/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.SensorCategoryHandling;

import de.jmonitoring.base.sensors.SensorInformation;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 *
 * @author togro
 */
public class SensorTreeCellRenderer extends DefaultTreeCellRenderer {

    private ImageIcon yellowIcon;
    private ImageIcon redIcon;
    private ImageIcon folderIcon;
    private ImageIcon defaultLeaficon;
    private ImageIcon greenIcon;
    private ImageIcon defaultSensorIcon;
    private ImageIcon manualIcon;
    private ImageIcon eventIcon;
    private ImageIcon counterIcon;

    public SensorTreeCellRenderer() {
        openIcon = new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/Folder_Open.png"));
        closedIcon = new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/Folder_Closed.png"));
        greenIcon = new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/sensor-icon-green.png"));
        yellowIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/sensor-icon-yellow.png"));
        redIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/sensor-icon-red.png"));
        defaultLeaficon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/sensor-icon-blank.png"));

        defaultSensorIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/defaultsensor_icon.png"));
        manualIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/manual_icon.png"));
        eventIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/event_icon.png"));
        counterIcon = new ImageIcon(getClass().getResource("/de/jmonitoring/icons/consumption_icon.png"));

        setOpenIcon(openIcon);
        setClosedIcon(closedIcon);
        setLeafIcon(defaultLeaficon);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        
        String str = value.toString();
        if (str != null) {
            str = str.split("\u2015")[0];
            str = str.split("@")[0];
        }

        
        if (leaf) {
            setFont(new Font("Dialog", Font.PLAIN, 10));
            if (SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(str)) != null) { // Abfangen falls keine Messpunkt
                if (SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(str)).isEvent()) {
                    setIcon(eventIcon);
                } else if (SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(str)).isCounter()) {
                    setIcon(counterIcon);
                } else if (SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(str)).isManual()) {
                    setIcon(manualIcon);
                } else {
                    if (leaf) {
                        setIcon(defaultSensorIcon);
                    }
                }
            } else {
                if (leaf) {
                    setIcon(defaultSensorIcon);
                }
            }
        } else { // Kein Blatt, sondern Kategorie
            setFont(new Font("Dialog", Font.BOLD, 10));
        }

        return this;
    }
}
