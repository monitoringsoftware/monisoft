package de.jmonitoring.DBOperations;

import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.References.ReferenceDescription;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.utils.filenamefilter.CSV_FilenameFilter;
import java.io.*;
import java.util.*;
import javax.swing.JFileChooser;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This class manages the import of a CSV-File of reference values.<p>
 *
 * @author togro
 */
public class ReferenceImporter {

    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();
    private File importFile;
    private CsvPreference csvPreference;

    /**
     * Constructor
     */
    public ReferenceImporter() {
    }

    /**
     * Invokes the file selection and impirt
     */
    public void importReferences() {
        char c = ',';
        csvPreference = new CsvPreference('"', c, "\r\n");
        importFile = getFile(); // Importdatei holen
        if (importFile != null) {
            importCSV(importFile);
        }
    }

    /**
     * Shows file chooser and return the selected file or
     * <code>null</code> if canceled
     *
     * @return The file
     */
    private File getFile() {
        File file = null;
        JFileChooser chooser = new JFileChooser(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder"));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new CSV_FilenameFilter());
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            file = chooser.getSelectedFile();
        }

        return file;
    }

    /**
     * Loops the read lines, creates the {@link ReferenceValue}s for each entry
     * and invokes the database update of the buildings references as well as
     * the global liost of reference values
     *
     * @param importFile
     */
    private void importCSV(File importFile) {
        ArrayList<Map<String, String>> rowList = readCSVFileToMap(importFile, "utf-8", csvPreference);
        HashSet<String> foundNames = new HashSet<String>();
        HashSet<String> missingNames = new HashSet<String>();

        TreeSet<ReferenceDescription> referenceList = ReferenceInformation.getReferenceList();

        // Die enthaltenen Bezugsgrößen ermitteln (nur erste Zeile)
        Map<String, String> row = rowList.get(0);
        boolean found;
        for (String bzg : row.keySet()) {
            found = false;
            for (ReferenceDescription refDef : referenceList) {
                if (bzg.equals(refDef.getName())) { // eine bekannte Bezugsgrösse ist in der Spalte
                    found = true;
                    foundNames.add(bzg);
                    break;
                }
            }
            if (!found) {
                missingNames.add(bzg); // TODO fragen was damit zu tun ist.....
            }
        }

        // Zeilen der Datei durchlaufen
        boolean first = true;
        String building;
        Double value;
        for (Map<String, String> currentRow : rowList) {
            if (first) { // erste Zeile überspringen
                first = false;
                continue;
            }
            ArrayList<ReferenceValue> buildingReferences = new ArrayList<ReferenceValue>();
            building = currentRow.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ReferenceImporter.GEBAEUDE"));
            for (String column : currentRow.keySet()) {
                if (!column.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ReferenceImporter.GEBAEUDE"))) {
                    try {
                        value = Double.valueOf(currentRow.get(column));
                        ReferenceValue reference = new ReferenceValue(column, value, BuildingInformation.getBuildingIDFromName(building), null);
                        buildingReferences.add(reference);
                    } catch (NumberFormatException e) {
                        // TODO silent
                    }
                }
            }
            // eine Zeile (Gebäude gelesen, gesammelte bezugsgrößen eintragen
            BuildingInformation.updateBuildingReferences(buildingReferences, BuildingInformation.getBuildingIDFromName(building));
        }

        new ListFiller().readReferenceList();
    }

    /**
     * Reads the csv-file and writes the lines with their column headers as key
     * to a Map<br> These line maps are then added to a ArrayList
     *
     * @param file The csv-file
     * @return <code>ArrayList</code> of line-maps
     */
    private ArrayList<Map<String, String>> readCSVFileToMap(File file, String encoding, CsvPreference csvPreference) {
        ArrayList<Map<String, String>> mapList = new ArrayList<Map<String, String>>(1024);
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            BufferedReader b = new BufferedReader(new InputStreamReader(fi, encoding));
            ICsvMapReader inFile = new CsvMapReader(b, csvPreference);
            final String[] header = inFile.getCSVHeader(false);
            Map<String, String> map;
            while ((map = inFile.read(header)) != null) {
                mapList.add(map);
            }
            return mapList;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
            return null;
        } finally {
            try {
                fi.close();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
                Messages.showException(ex);
            }
        }
    }
}
