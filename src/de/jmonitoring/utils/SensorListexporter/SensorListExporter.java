package de.jmonitoring.utils.SensorListexporter;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorProperties;
import java.io.*;
import java.util.ArrayList;

/**
 * Thsi class builds a csv-file from the sensor list
 *
 * @author togro
 */
public class SensorListExporter {

    public SensorListExporter() {
    }

    /**
     * Invoke the export using the following parameters
     *
     * @param file The file to be vreated
     * @param delimiter The fidl delimiter
     * @param encoding the file encoding to use
     * @param properties The list of sensors to be exported
     */
    public static void export(File file, char delimiter, String encoding, ArrayList<SensorProperties> properties) {
        if (file == null) {
            return;
        }

        ArrayList<String> header = buildHeader();

        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
            // printing header
            String sep = "";
            for (String field : header) {
                out.write(sep + field);
                sep = String.valueOf(delimiter);
            }
            out.newLine();

            // building rows
            ArrayList<String> rows = new ArrayList<String>();
            StringBuilder line;
            for (SensorProperties property : properties) {
                line = buildRow(property, delimiter); // TODO Properties für Zaehlernummer!!!
                rows.add(line.toString());
            }

            for (String row : rows) {
//                System.out.println(row);
                out.write(row);
                out.newLine();
            }
            out.close();
        } catch (IOException ex) {
            Messages.showException(ex);
        }
    }

    private static ArrayList<String> buildHeader() {
        // building header
        ArrayList<String> header = new ArrayList<String>();
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MESSPUNKTKUERZEL"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MESSPUNKTSCHLUESSEL"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.EINHEIT"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.FACTOR"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.BESCHREIBUNG"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.INTERVALL"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.ZAEHLER"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.ZUSTAND"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MANUELL"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.USAGE"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MINWERKTAG"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MINWOCHENENDE"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MAXWERKTAG"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MAXWOCHENENDE"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.VALIDWERKTAG"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.VALIDWOCHENENDE"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.GEBAEUDE"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.MEDIUM"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.ZAEHLERNUMMER"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.VIRTUAL"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.UTCPLUSX"));
        header.add(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.SUMMERTIME"));
        return header;
    }

    private static StringBuilder buildRow(SensorProperties property, char delimiter) {
        StringBuilder line;
        line = new StringBuilder();
        line.append(property.getSensorName());
        line.append(delimiter).append(property.getKeyName());
        line.append(delimiter).append(property.getSensorUnit());
        line.append(delimiter).append(property.getFactor());
        line.append(delimiter).append(property.getSensorDescription() == null ? "" : "\"" + property.getSensorDescription() + "\"");
        line.append(delimiter).append((property.getInterval() == null) ? "0" : property.getInterval());
        line.append(delimiter).append(property.isCounter() ? "1" : "0");
        line.append(delimiter).append(property.isEvent() ? "1" : "0");
        line.append(delimiter).append(property.isManual() ? "1" : "0");
        line.append(delimiter).append(property.isUsage() ? "1" : "0");
        line.append(delimiter).append(property.getWTLimits()[MoniSoftConstants.MINIMUM] == null ? "" : property.getWTLimits()[MoniSoftConstants.MINIMUM]);
        line.append(delimiter).append(property.getWELimits()[MoniSoftConstants.MINIMUM] == null ? "" : property.getWELimits()[MoniSoftConstants.MINIMUM]);
        line.append(delimiter).append(property.getWTLimits()[MoniSoftConstants.MAXIMUM] == null ? "" : property.getWTLimits()[MoniSoftConstants.MAXIMUM]);
        line.append(delimiter).append(property.getWELimits()[MoniSoftConstants.MAXIMUM] == null ? "" : property.getWELimits()[MoniSoftConstants.MAXIMUM]);
        line.append(delimiter).append(property.getMaxChangeTimes()[MoniSoftConstants.WORKDAY] == null ? "" : property.getMaxChangeTimes()[MoniSoftConstants.WORKDAY]);
        line.append(delimiter).append(property.getMaxChangeTimes()[MoniSoftConstants.WEEKEND] == null ? "" : property.getMaxChangeTimes()[MoniSoftConstants.WEEKEND]);
        line.append(delimiter).append(property.getBuildingName() == null ? "" : property.getBuildingName());
        line.append(delimiter).append(property.getMedium() == null ? "" : property.getMedium());
        line.append(delimiter); // TODO Properties für Zaehlernummer!!!
        line.append(delimiter).append(property.getVirtualDefinition() == null ? "" : property.getVirtualDefinition()); // TODO Properties für Zaehlernummer!!!
        line.append(delimiter).append(property.getUtcPlusX() == null ? "" : property.getUtcPlusX());
        line.append(delimiter).append(property.isSummerTime() == true ? "true" : "false" );        
        return line;
    }
}