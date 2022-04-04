package de.jmonitoring.DataHandling;

import de.jmonitoring.ApplicationProperties.AppPrefsDialog;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.utils.filenamefilter.MON_FilenameFilter;
import java.io.File;
import java.util.Iterator;
import javax.swing.JFileChooser;

/**
 * This thread capsluates the importing work for MON files
 * @author togro
 */
public class MONReaderThread extends Thread {

    private final String encoding;

    public MONReaderThread(String encoding) {
        this.encoding = encoding;
    }

    
    /**
     * This method inkoves the MON-import
     */
    @Override
    public void run() {
        JFileChooser fileChooser = new JFileChooser(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder"));
        fileChooser.setFileFilter(new MON_FilenameFilter());
        fileChooser.setMultiSelectionEnabled(true);

        // Holen des letzten Ordners in den Anwendungseinstellungen
        if (!MoniSoft.getInstance().getApplicationProperties().getProperty("LastMONImport").isEmpty()) {
            File folder = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("LastMONImport"));
            if (folder.exists()) {
                fileChooser.setCurrentDirectory(folder);
            }
        }

        int sum = 0;
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File[] filelist = fileChooser.getSelectedFiles();

            // Speichern des Ordners in den Anwendungseinstellungen
            MoniSoft.getInstance().getApplicationProperties().setProperty("LastMONImport", filelist[0].getParent());
            try {
                AppPrefsDialog.saveProperties(false, false);
            } catch (Exception ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }

            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IMPORTING") + " " + filelist.length + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FILES") + " ...\n", true);
            for (int i = 0; i < filelist.length; i++) {
                try {
                    sleep(5);
                } catch (InterruptedException ex) {
                    Messages.showException(ex);
                }
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("READING_FILE") + " " + filelist[i].getName() + " ...\n", true);
                MONDataImporter reader = new MONDataImporter(MoniSoft.getInstance().getDBConnector());
                if (reader.importMON(filelist[i], encoding)) {
                    Iterator<String> it = reader.getFailed().iterator();
                    while (it.hasNext()) {
                        Messages.showMessage("\t" + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SENSOR") + " '" + it.next() + "' " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SENSOR_UNKNOWN") + ".\n", true);
                    }
                }

                Messages.showMessage("... " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("COMPLETED") + "," + " " + reader.getImportedCount() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SETS_IMPORTED") + "\n", true);
                sum += reader.getImportedCount();
            }

            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IMPORT_COMPLETED") + " " + sum + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SETS") + " " + filelist.length + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FILESCOUNT") + "\n\n", true);
        }
    }
}
