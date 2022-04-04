package de.jmonitoring.DataHandling;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.MoniSoftConstants.InvalidMONFileException;
import de.jmonitoring.base.sensors.SensorInformation;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class is responsible for importing MON-data-files
 *
 * @author togro
 */
public class MONDataImporter {

    private HashSet<String> failedList = new HashSet<String>(512);
    private int counter = -1;
    private DBConnector connector;
    private String fieldSeparator, decimalSeparator;
    private String legalSensorChars = "[-a-zA-Z0-9_.]+";
    private String timeFormat;
    private ArrayList<String> separators = new ArrayList<String>(Arrays.asList(",", ";"));
    private ArrayList<String> decimalChars = new ArrayList<String>(Arrays.asList(".", ","));
    private ArrayList<String> timeStampFormats = new ArrayList<String>(9); // yyyy-mm-dd HH:mm:ss

    /**
     * Create a new importer and use the given connection
     *
     * @param conn The database connction
     */
    public MONDataImporter(DBConnector conn) {
        connector = conn;
        // zulässige Zeitstempelformate belegen
        timeStampFormats.add("\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d"); // yyyy-mm-dd HH:mm:ss
        timeStampFormats.add("\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d"); // yyyy-mm-dd HH:mm
        timeStampFormats.add("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d"); // dd.mm.yyyy HH:mm:ss
        timeStampFormats.add("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d"); // dd.mm.yyyy HH:mm
    }

    /**
     * Invokles the import of the given file
     *
     * @param dataFile The file to import
     * @param encoding The encoding of the file
     * @return <code>true</code> if the import was successful
     */
    public boolean importMON(File dataFile, String encoding) {
        boolean success = false;

        if (dataFile != null) {
            try {
                File file = writeTmpFile(dataFile, encoding);
                counter = loadDataFromInfile(file);
                if (counter != -1) {
                    success = true;
                }
            } catch (FileNotFoundException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            } catch (IOException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            } catch (InvalidMONFileException ex) {
                Messages.showException(ex);
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("NO_MON") + "\n", true);
                Messages.showException(ex);
            }
        }
        return success;
    }

    /**
     * Try to guess the column order in the file
     *
     * @param file The import file
     * @param encoding The encoding of the file
     * @return A list of elements in the detected order
     */
    private ArrayList getColumnOrder(File file, String encoding) {
        ArrayList order = null;

        // Pattern mit möglichen Kombinationen füllen
        HashMap<Pattern, ArrayList<Object>> patternMap = new HashMap<Pattern, ArrayList<Object>>(9);

        Iterator<String> seperatorIT = separators.iterator();
        Iterator<String> decimalsIT; // = decimalChars.iterator();
        Iterator<String> timeIT;  // = timeStampFormats.iterator();
        String dcTest;
        String timeFMT, dC, sC;

        while (seperatorIT.hasNext()) { // alle potentiellen Feldtrenner durchlaufen
            sC = seperatorIT.next();
//            System.out.print("Feld: " + sC);
            decimalsIT = decimalChars.iterator();
            while (decimalsIT.hasNext()) { // alle potentiellen Dezimaltrenner durchlaufen
//                System.out.print(" Dez: " + sC);
                dC = decimalsIT.next();
                if (dC.equals(".")) { // für den reg. Audruck den Punkt escapen
                    dcTest = "\\.";
                } else {
                    dcTest = dC;
                }
                if (!dC.equals(sC)) { // auslassen falls dezimaltrenner und feldtrenner gleich sind -> sinnlos
                    timeIT = timeStampFormats.iterator();
                    while (timeIT.hasNext()) { // alle potentiellen Zeitformate durchlaufen
                        timeFMT = timeIT.next();
                        // set up the 6 possible combinations of the 3 columns
                        // list numerbs mean:
                        // 0: sensor
                        // 1: value
                        // 2: timestamp
                        patternMap.put(Pattern.compile("^" + legalSensorChars + sC + "\\d+(" + dcTest + "\\d+)?" + sC + timeFMT + "$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(0, 1, 2, sC, dC, timeFMT)));
                        patternMap.put(Pattern.compile("^" + legalSensorChars + sC + timeFMT + sC + "(\\d+(" + dcTest + "\\d+)?)?(;\\d+)?$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(0, 2, 1, sC, dC, timeFMT)));

                        patternMap.put(Pattern.compile("^\\d+(" + dcTest + "\\d+)?" + sC + legalSensorChars + sC + timeFMT + "$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(1, 0, 2, sC, dC, timeFMT)));
                        patternMap.put(Pattern.compile("^" + timeFMT + sC + legalSensorChars + sC + "(\\d+(" + dcTest + "\\d+)?)?(;\\d+)?$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(1, 2, 0, sC, dC, timeFMT)));
                        
                        patternMap.put(Pattern.compile("^" + timeFMT + sC + "\\d+(" + dcTest + "\\d+)?" + sC + legalSensorChars + "$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(2, 1, 0, sC, dC, timeFMT)));
                        patternMap.put(Pattern.compile("^\\d+(" + dcTest + "\\d+)?" + sC + timeFMT + sC + legalSensorChars + "$", Pattern.MULTILINE), new ArrayList<Object>(Arrays.asList(2, 0, 1, sC, dC, timeFMT)));
                    }
                }
            }
        }

        try {
            for (Pattern patternTemplate : patternMap.keySet()) {
                Scanner scanner = new Scanner(file, encoding);
                String found = scanner.findWithinHorizon(patternTemplate, 500000);
                // System.out.println( "found: " + found );
                if (found != null) {
                    order = (ArrayList) patternMap.get(patternTemplate);
                    System.out.println( "order: " + order );
                    fieldSeparator = (String) order.get(3);
                    decimalSeparator = (String) order.get(4);
                    timeFormat = (String) order.get(5);
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DETECTED") + " " + patternTemplate.toString() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("WITH_DIVIDER") + " " + fieldSeparator + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DECIMAL") + " " + decimalSeparator + "\n", true);
                }
            }
        } catch (FileNotFoundException ex) {
            Messages.showException(ex);
        }
        return order;
    }

    /**
     * Reads the given tempFile by using "load data local infile"
     *
     * @param tempFile The temporary import tempFile
     */
    public int loadDataFromInfile(File tempFile) {
        int count = -1;
        Connection myConn = null;
        Statement stmt = null;
        InputStream stream = null;
        try {
//            System.out.println("jdbc:mysql://" + connector.getServer() + "/" + connector.getDBName() + "?useUnicode=yes&characterEncoding=UTF-8" + "&user=" + connector.getUserName() + "&password=" + connector.getPwd() + "&useCompression=false");
            myConn = DriverManager.getConnection("jdbc:mysql://" + connector.getServer() + "/" + connector.getDBName() + "?useUnicode=yes&characterEncoding=UTF-8" + "&user=" + connector.getUserName() + "&password=" + connector.getPwd() + "&useCompression=false");
            stmt = myConn.createStatement();
            stream = new FileInputStream(tempFile);
            ((com.mysql.jdbc.Statement) stmt).setLocalInfileInputStream(stream);
            count = stmt.executeUpdate("load data local infile 'file' ignore into table " + MoniSoftConstants.HISTORY_TABLE + " fields terminated by ',' (T_Sensors_id_Sensors,Value,@Time) set TimeStamp=unix_timestamp(@Time)");
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (Exception ex) {
            count = -1;
            Messages.showException(ex);
        } finally {
            try {
                stream.close();
                stmt.close();
                myConn.close();
            } catch (SQLException ex) {
                Messages.showException(ex);
            } catch (IOException ex) {
                Messages.showException(ex);
            }
        }
        return count;
    }

    /**
     * Creates a temporary file from teh given one by replacing the sensor name
     * or key by the sensor id<br> The order of the 3 columns (value, sensoID,
     * timestamp) is detected and the resulting temporary file has alwas the
     * same column order.
     *
     * @param infile The import file
     * @param encoding The file encoding
     * @return Th eresulting temp-file
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     * @throws de.jmonitoring.base.MoniSoftConstants.InvalidMONFileException
     */
    private File writeTmpFile(File infile, String encoding) throws FileNotFoundException, IOException, InvalidMONFileException {
        // Reihenfolge der Spalten ermitteln
        ArrayList<Integer> order = getColumnOrder(infile, encoding); // Reihenfolge und Format der Spalten ermitteln
        String[] columns;
        File tempFile = null;
        String line, value, time, sensor, flag = "0";

        if (order == null) { // es konnten keine gültigen Spalten ermittelt werden
            throw new MoniSoftConstants.InvalidMONFileException();
        } else {
            Scanner scanner = new Scanner(infile, encoding);
            // Temporäre Datei erzeugen
            tempFile = File.createTempFile("mon", ".tmp");
            tempFile.deleteOnExit(); // tmp Datei löschen wenn Programm endet
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), encoding));
            int id;
            //Originaldatei zeilenweise lesen, id ermitteln und in tmp-Datei schreiben
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                columns = line.split(fieldSeparator);

                try {
                    sensor = columns[order.get(0)];
                    value = columns[order.get(1)].replace(",", "."); // kamma durch punkt ersetzen wenn nötig
                    time = timeFormatter(columns[order.get(2)]);
                    if ((columns.length == 4)) { // Exclusiv für Bad Aibling-> 4. Spalte mit Statusflag
                        flag = columns[3];       // Exclusiv für Bad Aibling-> 4. Spalte mit Statusflag
                    }                            // Exclusiv für Bad Aibling-> 4. Spalte mit Statusflag
                } catch (ArrayIndexOutOfBoundsException e) { // wenn der Index ungültig ist kann es keine Messdatenzeile sein
                    continue;
                }
                if (time == null) {
                    continue;
                }
                id = SensorInformation.getSensorIDFromNameORKey(sensor);

                if (id != -1) {
                    if (!value.isEmpty() && flag.equals("0")) {     // Exclusiv für Bad Aibling-> 4. Spalte mit Statusflag: Zeile ignorieren wenn flag != 0
                        out.write(id + "," + value + "," + time);
                        out.newLine();
                    }
                } else {
                    failedList.add(sensor);
                }
            }
            out.close();
            scanner.close();
        }
        return tempFile;
    }

    /**
     * Translates the timestamp to a common format
     *
     * @param time The time in valid time formats
     * @return timestamp in format yyyy-mm-dd HH:mm:ss
     */
    public String timeFormatter(String time) {
        String outputTime = "";
        Date d;
        SimpleDateFormat inputFmt = new SimpleDateFormat();
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (timeFormat.equals("\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d:\\d\\d")) { //yyyy-mm-dd HH:mm:ss
            return time; // schon implements richtigen Format, gleich zurückgeben
        }
        if (timeFormat.equals("\\d\\d\\d\\d-\\d\\d-\\d\\d\\s\\d\\d:\\d\\d")) { // yyyy-mm-dd HH:mm
            inputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        }
        if (timeFormat.equals("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d:\\d\\d")) { // dd.mm.yyyy HH:mm:ss
            inputFmt = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        }
        if (timeFormat.equals("\\d\\d\\.\\d\\d\\.\\d\\d\\d\\d\\s\\d\\d:\\d\\d")) {  // dd.mm.yyyy HH:mm
            inputFmt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            System.out.println( "inputDate: " + inputFmt.toLocalizedPattern() );
        }
        try {
            d = inputFmt.parse(time);
            outputTime = outputFmt.format(d);
        } catch (ParseException ex) {

            Messages.showException(ex);
            return null;
        }
        return outputTime;
    }

    /**
     * Return al list of sensors that coud not be imported (were not in the
     * sensor list)
     *
     * @return A list of failed sensors
     */
    public HashSet<String> getFailed() {
        return failedList;
    }

    /**
     * Return the list of imported valzues
     *
     * @return The number of imported values
     */
    public int getImportedCount() {
        return counter;
    }
}
