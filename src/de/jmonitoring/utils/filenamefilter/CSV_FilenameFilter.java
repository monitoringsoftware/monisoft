/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.filenamefilter;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author togro
 */
public class CSV_FilenameFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.getName().toLowerCase().endsWith("." + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/filenamefilter/Bundle").getString("CSV")) || f.isDirectory();

    }

    @Override
    public String getDescription() {
        return java.util.ResourceBundle.getBundle("de/jmonitoring/utils/filenamefilter/Bundle").getString("CSV DATEIEN");
    }
}
