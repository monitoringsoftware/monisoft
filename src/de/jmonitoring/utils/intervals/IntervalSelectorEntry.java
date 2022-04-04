/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.intervals;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author togro
 */
public class IntervalSelectorEntry extends Number implements Comparable<Integer> {

    private double value;

    public IntervalSelectorEntry(Double value) {
        this.value = value;
    }

//    @Override
    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;

    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public int compareTo(Integer o) {
        double thisVal = this.value;
        double anotherVal = o.doubleValue();
        return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));

    }

    @Override
    public String toString() {
        String s = "";

        if (value < 0d) {
            switch (intValue()) {
                case -1:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("ROHDATEN");
                    break;
                case -2:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("STUNDENWERTE");
                    break;
                case -3:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("TAGESWERTE");
                    break;
                case -4:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("WOCHENWERTE");
                    break;
                case -5:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("MONATSWERTE");
                    break;
                case -6:
                    s = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("JAHRESWERTE");
                    break;
//                default:
//                    s = intValue() + "-Minutenwerte";
            }
        } else if (value < 1d) {
            int seconds = (int) (60 * value);
            s = seconds + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("SEKUNDEN");
        } else {
            s = intValue() + "-" + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/intervals/Bundle").getString("MINUTENWERTE");
        }
        return s;
    }

    public static int getIndexOfIntervalSelectorEntry(JComboBox selector, double interval) {
        int index = 0;
        DefaultComboBoxModel cm = (DefaultComboBoxModel) selector.getModel();

        for (int i = 0; i < cm.getSize(); i++) {
            if (((IntervalSelectorEntry) cm.getElementAt(i)).value == interval) {
                index = i;
                break;
            }
        }
        return index;
    }
}
