/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.filenamefilter;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author togro
 */
public class GRA_FilenameFilter implements FilenameFilter {

    public GRA_FilenameFilter() {
    }

    @Override
    public boolean accept(File f, String s) {
        return s.toLowerCase().endsWith("." + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/filenamefilter/Bundle").getString("GRA"));
    }
}
