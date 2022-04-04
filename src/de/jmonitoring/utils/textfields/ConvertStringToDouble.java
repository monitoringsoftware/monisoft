/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.textfields;

/**
 *  * @author togro
 */
public class ConvertStringToDouble {

    /**
     * Wandelt einen String in den entsprechenden dezimalwert um. Kommas als Dezimaltrenner werden zuvor in '.' gewandelt.
     * @param s Der umzuwandlende String
     * @return Den <code>Double</code>-Wert des Strings oder null wenn der String keinen gültigen Wert enthält
     */
    public Double convert(String s) {
        Double d;
        // Komma im String durch Punkt ersetzen
        s = s.replace(",", ".");

        // Versuchen umzuwandlen
        try {
            d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        return d;
    }
}
