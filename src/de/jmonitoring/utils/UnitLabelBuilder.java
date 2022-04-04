/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author togro
 */
public class UnitLabelBuilder {

    /**
     * Erzeugt ein Einheitenlabel in eckigen Klammern. Schon vorhandene Einheiten werden ignoriert und nicht erneut aufgeführt
     * @param unitlabel
     * @param newUnit
     * @return
     */
    public String buildLabel(String unitlabel, String newUnit) {
        String label = unitlabel;
        String sep = "";
        // Klammern entfernen
        label = label.replace("[", "");
        label = label.replace("]", "");

        // an den Kommas aufteilen und Inhalt in Set schreiben
        HashSet<String> unitSet = new HashSet<String>(Arrays.asList(label.split(",")));

        if (unitSet.contains(newUnit)) { // Einheit kam schon vor, einfach das alte Label zurückgeben
            label = unitlabel;
        } else {
            unitSet.add(newUnit);
            label = "";
            for (String unit : unitSet) {
                if (!unit.isEmpty()) {
                    label += sep + unit;
                    sep = ",";
                }
            }
            label = "[" + label + "]";
        }

        return label;
    }

    public String addReference(String unitlabel, String reference, String timereference) {
        String label = unitlabel;
        String r = "", t = "";
        String timeSep = "";


        if (reference != null && !reference.isEmpty()) {
            r = reference;
        }

        if (timereference != null && !timereference.isEmpty()) {    // Es gibt eine Zeitrefernez
            t = timereference;
            if (!r.isEmpty()) {
                timeSep = " ";
            }
        }


        // Referenz hinzufügen
        if (!r.isEmpty() || !t.isEmpty()) {
            label = label.replace("[", "");
            label = label.replace("]", "");
            label += " / " + r + timeSep + t;
            label = "[" + label + "]";
        }

        return label;
    }
}
