/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.SensorListexporter;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * File chooser used to select the file to store a sensor list
 *
 * @author togro
 */
public class ExportSensorListFileChooser {

    public File getFile(MainApplication gui) {
        File file = null;
        try {
            JFileChooser fileChooser = new JFileChooser(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder"));
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("CSV files") + " (*.csv)";
                }
            });

            int returnVal = fileChooser.showSaveDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                if (!file.toString().endsWith(".csv")) {
                    file = new File(file.toString() + ".csv");
                }

                if (file.exists() && JOptionPane.showConfirmDialog(gui.getMainFrame(), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("FILE_EXISTS_OVERWRITE"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUESTION"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    file = null;
                }

            }
        } catch (Exception e) {
            Messages.showException(e);
        }

        return file;
    }
}
