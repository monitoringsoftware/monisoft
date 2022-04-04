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
public class mysqldump_FileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.getName().toLowerCase().startsWith(java.util.ResourceBundle.getBundle("de/jmonitoring/utils/filenamefilter/Bundle").getString("MYSQLDUMP")) || f.isDirectory();

    }

    @Override
    public String getDescription() {
        return java.util.ResourceBundle.getBundle("de/jmonitoring/utils/filenamefilter/Bundle").getString("ANWENDUNG MYSQLDUMP");
    }
}
