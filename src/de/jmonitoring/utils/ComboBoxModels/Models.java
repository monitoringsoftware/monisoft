/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.ComboBoxModels;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.intervals.IntervalSelectorEntry;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;

/**
 * Some combobox models for variuos purposes
 *
 * @author togro
 */
public class Models {

    public DefaultComboBoxModel getAggIntervalComboBoxModel() {
        DefaultComboBoxModel cm = new DefaultComboBoxModel();
        String[] intervalStrings = MoniSoft.getInstance().getApplicationProperties().getProperty("IntervalList").split(",");

        double interval;

        for (int i = 0; i < intervalStrings.length; i++) {
            try {
                if (!intervalStrings[i].isEmpty()) {
                    interval = Double.valueOf(intervalStrings[i]);
                    cm.addElement(new IntervalSelectorEntry(interval));
                }
            } catch (NumberFormatException e) {
                Messages.showException(e);
                Messages.showException(e);
            }
        }

        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.HOUR_INTERVAL));
        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.DAY_INTERVAL));
        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.WEEK_INTERVAL));
        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.MONTH_INTERVAL));
        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.YEAR_INTERVAL));

        return cm;
    }

    public DefaultComboBoxModel getCarpetAggIntervalComboBoxModel() {
        DefaultComboBoxModel cm = new DefaultComboBoxModel();
        String[] intervalStrings = MoniSoft.getInstance().getApplicationProperties().getProperty("IntervalList").split(",");


        double interval;

        for (int i = 0; i < intervalStrings.length; i++) {
            try {
                if (!intervalStrings[i].isEmpty()) {
                    interval = Double.valueOf(intervalStrings[i]);
                    cm.addElement(new IntervalSelectorEntry(interval));
                }
            } catch (NumberFormatException e) {
                Messages.showException(e);
                Messages.showException(e);
            }
        }
        cm.addElement(new IntervalSelectorEntry(MoniSoftConstants.HOUR_INTERVAL));

        return cm;
    }

    public DefaultComboBoxModel getSensorListComboBoxModel() {
        Vector<SensorProperties> propList = new Vector<SensorProperties>();
        propList.add(new SensorProperties(0, MoniSoftConstants.NO_SENSOR_SELECTED, MoniSoftConstants.NO_SENSOR_SELECTED));
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            propList.add(SensorInformation.getSensorList().get(i));
        }
        return new DefaultComboBoxModel(propList);
    }

    public DefaultComboBoxModel getCounterListComboBoxModel() {
        Vector<SensorProperties> propList = new Vector<SensorProperties>();
        propList.add(new SensorProperties(0, MoniSoftConstants.NO_SENSOR_SELECTED, MoniSoftConstants.NO_SENSOR_SELECTED));

        SensorProperties props;
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            props = SensorInformation.getSensorList().get(i);
            if (props.isCounter()) {
                propList.add(props);
            }
        }
        return new DefaultComboBoxModel(propList);
    }

    public DefaultComboBoxModel getUsageListComboBoxModel() {
        Vector<SensorProperties> propList = new Vector<SensorProperties>();
        propList.add(new SensorProperties(0, MoniSoftConstants.NO_SENSOR_SELECTED, MoniSoftConstants.NO_SENSOR_SELECTED));

        SensorProperties props;
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            props = SensorInformation.getSensorList().get(i);
            if (props != null && !props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
                propList.add(props);
            }
        }
        return new DefaultComboBoxModel(propList);
    }

    public DefaultListModel getSensorListListModel() {
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            model.addElement(SensorInformation.getSensorList().get(i));
        }
        return model;
    }
}
