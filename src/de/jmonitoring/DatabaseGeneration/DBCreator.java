package de.jmonitoring.DatabaseGeneration;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ibatis.common.resources.Resources;
import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import de.jmonitoring.Components.NewUnitDialog;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.References.ReferenceDescription;
import java.util.Arrays;

/**
 * This class contains methods to create the structure of a MoniSoft database
 * and to populate it with default data, as well as with sensor and building
 * lists read from csv files.
 *
 * @author togro
 */
public class DBCreator {

    public static final int SUCCESS = 0;
    public static final int FILE_NOT_FOUND = 1;
    public static final int IOERROR = 2;
    public static final int DBERROR = 3;
    public static final int IS_EMPTY = 0;
    public static final int HAS_STRUCTURE = 1;
    public static final int HAS_VALID_STRUCTURE = 2;
    private CsvPreference csvPreference; // = new CsvPreference('"', ';', "\r\n");
    private final Frame parent;

    /**
     * Creates o new instance with the given parent
     *
     * @param parent
     */
    public DBCreator(Frame parent) {
        super();
        this.parent = parent;
    }

    /**
     * Creates the MoniSoft table structure on an empty databases scheme<p> Uses
     * the file
     * <code>create_structure.sql</code> from this package
     *
     * @return true if the operation was successfull
     */
    public boolean createStructure() {
        boolean success;
        Connection myConn = null;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.ERZEUGE DATENBANKSTRUKTUR") + "\n", true);
        try {
            myConn = DBConnector.openConnection();
            ScriptRunner sr = new ScriptRunner(myConn, false, true);
            Resources.setCharset(Charset.forName("UTF-8"));
            sr.runScript(Resources.getResourceAsReader("de/jmonitoring/DatabaseGeneration/create_structure.sql"));
            success = true;
        } catch (Exception ex) {
            success = false;
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, null, null);
        }
        return success;
    }

    /**
     * Fills the unit table by using the file
     * <code>Units.sql</code> from this package
     *
     * @return true if the operation was successfull
     */
    public boolean fillUnitTable() {
        boolean success;
        Connection myConn = null;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.FÜLLE TABELLEN") + "\n", true);
        try {
            myConn = DBConnector.openConnection();
            ScriptRunner sr = new ScriptRunner(myConn, false, true);
            Resources.setCharset(Charset.forName("UTF-8"));
            sr.runScript(Resources.getResourceAsReader("de/jmonitoring/DatabaseGeneration/Units.sql"));
            success = true;
            UnitInformation.setUnitList(new ListFiller().readUnitList());
        } catch (Exception ex) {
            success = false;
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, null, null);
        }
        return success;
    }

//    /**
//     * Erzeugt eine neue, leere Datenbank sofern die Rechte dafür ausreichen.
//     *
//     * @param login
//     * @param pwd
//     * @return
//     */
//    public boolean createDB(String login, String pwd) {
//        boolean success;
//        try {
//            ScriptRunner sr = new ScriptRunner("com.mysql.jdbc.Driver", "jdbc:mysql://172.22.87.200", login, pwd, false, true);
//            Resources.setCharset(Charset.forName("UTF-8"));
//            sr.runScript(Resources.getResourceAsReader("de/jmonitoring/DatabaseGeneration/create_db.sql"));
//            Messages.showMessage(sr.toString());
//            success = true;
//        } catch (Exception ex) {
//            success = false;
//            Messages.showException(ex);
//            Messages.showException(ex);
//        } finally {
//            try {
//            } catch (Exception ex) {
//                Messages.showException(ex);
//                Messages.showException(ex);
//            }
//        }
//        return success;
//    }
    /**
     * Populates the sensor table.<p> A csv-file is read and the column values
     * mapped to their field names.<br>
     *
     *
     * @param file the csv file
     * @param encoding the character encocding
     * @param delimiter the field delimiter
     * @param overwrite if true existing sensors will be updated
     * @return the status of the operation
     */
    public int fillSensorTable(File file, String encoding, char delimiter, boolean overwrite) {
        csvPreference = new CsvPreference('"', delimiter, "\r\n");
        ArrayList<Map<String, String>> mapList = readCSVFileToMap(file, encoding);
        if (mapList.isEmpty()) {
            return FILE_NOT_FOUND;
        }

        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            for (String line : createSensorInserts(mapList, overwrite, stmt) ) {
                try {
                    stmt.executeUpdate(line);
                } catch (MySQLIntegrityConstraintViolationException ex) {
                    System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.SENSOREXISTS"));
                }
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException e) {
            Messages.showException(e);
        } catch (NullPointerException e) {
            Messages.showException(e);
            return DBERROR;
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return SUCCESS;
    }

    /**
     * Liest eine CSV-Datei ein und schreibt die Zeile mit dem Spaltenkopf als
     * Schlüssel in eine Map. Jede Zeile(Map) wird in eine ArrayList
     * aufgenommen.
     *
     * @param file Zu lesende CSV-Datei
     * @return <code>ArrayList<Map<String, String>></code> der Zeilen
     */
    private ArrayList<Map<String, String>> readCSVFileToMap(File file, String encoding) {
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
            Messages.showMessage("Die Anzahl der Spalten ist nicht einheitlich Breche ab.\n", true);
            Messages.showException(ex);
            return null;
        } finally {
            try {
                fi.close();
            } catch (IOException ex) {
                Messages.showException(ex);
            }
        }
    }

    /**
     * Creates the SQL commands to fill in the building table
     *
     * @param mapList the list of the field/value mappings of the csv file
     * @return an {@link ArrayList} of the SQl-insert lines
     */
    private ArrayList<String> createBuildingInserts(ArrayList<Map<String, String>> mapList) {
        ArrayList<String> insertList = new ArrayList<String>(100); // enthält die Strings mit den SQL-Inserts
        Iterator<Map<String, String>> it = mapList.iterator();
        Map<String, String> map;
        String name = "";
        String street = "";
        String plz = "";
        String contact = "";
        String phone = "";
        String networking = "";
        String city = "";
        String desc = "";
        while (it.hasNext()) {
            map = it.next();
            Iterator<String> mapIterator = map.keySet().iterator();
            while (mapIterator.hasNext()) {
                mapIterator.next();
                name = map.get(MoniSoftConstants.CSV_BUILDING_NAME);
                street = map.get(MoniSoftConstants.CSV_BUILDING_STREET);
                plz = map.get(MoniSoftConstants.CSV_BUILDING_PLZ);
                contact = map.get(MoniSoftConstants.CSV_BUILDING_CONTACT);
                phone = map.get(MoniSoftConstants.CSV_BUILDING_PHONE);
                networking = map.get(MoniSoftConstants.CSV_BUILDING_NETWORK);
                city = map.get(MoniSoftConstants.CSV_BUILDING_CITY);
                desc = map.get(MoniSoftConstants.CSV_BUILDING_DESCRIPTION);
            }
            if (name == null || name.isEmpty()) { // Gebäude ohne Namen sind keine.... ;-)
                continue;
            }

            // SQL-String bauen
            StringBuilder insertString = new StringBuilder("");
            insertString.append("insert into " + MoniSoftConstants.BUILDING_TABLE + " set ");
            insertString.append(MoniSoftConstants.BUILDING_NAME + "='").append(name).append("'");
            insertString.append("," + MoniSoftConstants.BUILDING_STREET + "='").append(street).append("'");
            insertString.append("," + MoniSoftConstants.BUILDING_CONTACT + "='").append(contact).append("'");
            insertString.append("," + MoniSoftConstants.BUILDING_PLZ + "=").append(plz.isEmpty() ? "null" : plz);
            insertString.append("," + MoniSoftConstants.BUILDING_PHONE + "=").append(phone.isEmpty() ? "null" : phone);
            insertString.append("," + MoniSoftConstants.BUILDING_NETWORKING + "='").append(networking).append("'");
            insertString.append("," + MoniSoftConstants.BUILDING_CITY + "='").append(city).append("'");
            insertString.append("," + MoniSoftConstants.BUILDING_DESCRIPTION + "='").append(desc).append("'");
            insertList.add(insertString.toString());
        }

        return insertList;
    }

    /**
     * Creates the SQL commands to fill in the reference table
     *
     * @param mapList the list of the field/value mappings of the csv file
     * @return an {@link ArrayList} of the SQl-insert lines
     */
    private ArrayList<String> createReferenceInserts(ArrayList<Map<String, String>> mapList) {
        ArrayList<String> insertList = new ArrayList<String>(100); // enthält die Strings mit den SQL-Inserts
        Iterator<Map<String, String>> it = mapList.iterator();
        Map<String, String> map;

        ArrayList<ReferenceDescription> defaultReferences = MoniSoftConstants.getDefaultReferences(); // Standardbezugsgrößen nach DIN 277 holen, benötigt werden hier nur die Schlüssel
        while (it.hasNext()) { // die einzelen "Zeilen" = Gebäude durchlaufen
            map = it.next();
            for (ReferenceDescription defaultReference : defaultReferences) { // die Standardreferenzen durchlaufen
                try {
                    // Übespringen wenn kein Wert für diese Bezugsgröße -> NumberFormatException
                    insertList.add("insert into " + MoniSoftConstants.REFERENCEMAP_TABLE + " set " + MoniSoftConstants.REFERENCEMAP_NAME + "='" + defaultReference.getName() + "', " + MoniSoftConstants.REFERENCEMAP_VALUE + "=" + map.get(defaultReference.getName()) + "," + MoniSoftConstants.REFERENCEMAP_BUILDING_ID + "=" + BuildingInformation.getBuildingIDFromName(map.get(MoniSoftConstants.CSV_BUILDING_NAME)));//  + "," + MoniSoftConstants.REFERENCEMAP_UNIT_ID + "=" + defaultReference.getUnitID());
                } catch (NumberFormatException e) {
                    Messages.showException(e);
                }
            }
        }

        return insertList;
    }

    /**
     * Creates the SQL commands to fill in the sensor table
     *
     * @param mapList the list of the field/value mappings of the csv file
     * @return an {@link ArrayList} of the SQl-insert lines
     */
    private ArrayList<String> createSensorInserts(ArrayList<Map<String, String>> mapList, boolean overwrite, Statement stmt) {
        ArrayList<String> insertList = new ArrayList<String>(2000);
        String name = null, keyName = null, desc = "", unit = null, factorString = null;
        String interval = null, minWT = null, minWE = null, maxWT = null, maxWE = null, validWT = null, validWE = null;
        String isCounter = null, isEvent = null, isManual = null, isUsage = null;
        String building = null;
        String counterNo = null;
        String medium = "";
        String virtual = null;
        String utcPlusXString = "";
        String summertimeString = "";
        Integer utcPlusX = 0;
        Boolean summertime = false;

        String locale = detectLocale(mapList);
                
        if (locale == null) {
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("NO_LANGUAGE"));
            return null;
        }        

        for (Map<String, String> map : mapList) {
            Iterator<String> mapIterator = map.keySet().iterator();
            while (mapIterator.hasNext()) {
                mapIterator.next();
                try {
                    name = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MESSPUNKTKUERZEL")).trim().replace(" ", "_");                  // TODO map für user definierbar machen
                    keyName = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MESSPUNKTSCHLUESSEL")).trim().replace(" ", "_");
                    factorString = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.FACTOR")).trim();
                    interval = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.INTERVALL")).trim();
                    isEvent = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.ZUSTAND")).trim();
                    isCounter = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.ZAEHLER")).trim();
                    isManual = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MANUELL")).trim();
                    isUsage = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.USAGE")).trim();                    
                } catch (NullPointerException e) {
                    Messages.showException(e);
                    Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.IN DER IMPORTDATEI FEHLT EINE ZWINGENDE SPALTE"));
                    return null;
                }

                try {
                    unit = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.EINHEIT")).trim();
                    desc = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.BESCHREIBUNG")).trim();
                    minWT = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MINWERKTAG")).trim();
                    maxWT = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MAXWERKTAG")).trim();
                    minWE = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MINWOCHENENDE")).trim();
                    maxWE = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MAXWOCHENENDE")).trim();
                    validWT = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.VALIDWERKTAG")).trim();
                    validWE = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.VALIDWOCHENENDE")).trim();
                    building = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.GEBAEUDE")).trim();
                    counterNo = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.ZAEHLERNUMMER")).trim();
                    medium = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.MEDIUM")).trim();
                    virtual = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.VIRTUAL")).trim();                    
                    utcPlusXString = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.UTCPLUSX")).trim();
                    if( utcPlusXString != null )
                        utcPlusX = new Integer( utcPlusXString );
                    summertimeString = map.get(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + locale).getString("DBCreator.SUMMERTIME")).trim();
                    if( summertimeString != null )
                        summertime = new Boolean( summertimeString );
                    
                } catch (NullPointerException e) {
                    System.out.println( "NullPointerException createSensorInserts: " + e.getMessage() );
                    e.printStackTrace();
                }
            }

            // Wen der Name oder der Schlüssel leer sind auslassen
            if (name == null || name.isEmpty() || keyName == null | keyName.isEmpty()) {
                continue;
            }            
            
            // Wen der Name oder der Schlüssel leer sind auslassen
            if (name == null || name.isEmpty() || keyName == null | keyName.isEmpty()) {
                continue;
            }
            
            StringBuilder insertString = new StringBuilder("");
            
            // ist der Sensor keyName bereits in der Tabelle ist,
            // wenn ja dann überschreibe den Datensatz mit einem Update
            // sonst führe einen Insert aus
            try {
                ResultSet rs = stmt.executeQuery( "select count(*) from " + MoniSoftConstants.SENSOR_TABLE + " where " + 
                                MoniSoftConstants.SENSOR_KEY + " = '" + keyName + "'" );
                if( rs != null )
                {
                    rs.next();
                    int count = rs.getInt( 1 );
                    
                    if( count == 1 )
                    {
                        // Der Update soll nur gemacht werden, wenn der User das wünscht (overwrite == true)
                        if( overwrite )
                        {
                            // Überschreibe den vorhandenen Sensornamen für einen Sensorkey (Update)
                            insertString.append("update ").append(MoniSoftConstants.SENSOR_TABLE).append(" set ");
                            insertString.append(MoniSoftConstants.SENSOR_NAME).append("='").append(name).append("'");
                            insertString.append(",").append(MoniSoftConstants.SENSOR_DESCRIPTION).append("='").append(desc).append("'");
                            insertString.append(",").append(MoniSoftConstants.SENSOR_UNIT_ID).append("=").append(getUnitID(unit, name));
                            insertString.append(",").append(MoniSoftConstants.MIN_WORKDAY).append("=").append(minWT == null || minWT.isEmpty() ? "null" : minWT);
                            insertString.append(",").append(MoniSoftConstants.MAX_WORKDAY).append("=").append(maxWT == null || maxWT.isEmpty() ? "null" : maxWT);
                            insertString.append(",").append(MoniSoftConstants.MIN_WEEKEND).append("=").append(minWE == null || minWE.isEmpty() ? "null" : minWE);
                            insertString.append(",").append(MoniSoftConstants.MAX_WEEKEND).append("=").append(maxWE == null || maxWE.isEmpty() ? "null" : maxWE);
                            insertString.append(",").append(MoniSoftConstants.MAX_WEEKEND_CHANGETIME).append("=").append(validWE == null || validWE.isEmpty() ? "null" : validWE);
                            insertString.append(",").append(MoniSoftConstants.MAX_WORKDAY_CHANGETIME).append("=").append(validWT == null || validWT.isEmpty() ? "null" : validWT);
                            insertString.append(",").append(MoniSoftConstants.IS_EVENT).append("=").append(isEvent != null && isEvent.equals("1"));
                            insertString.append(",").append(MoniSoftConstants.IS_COUNTER).append("=").append(isCounter != null && isCounter.equals("1"));
                            insertString.append(",").append(MoniSoftConstants.IS_MANUAL).append("=").append(isManual != null && isManual.equals("1"));
                            insertString.append(",").append(MoniSoftConstants.IS_USAGE).append("=").append(isUsage != null && isUsage.equals("1"));
                            insertString.append(",").append(MoniSoftConstants.SENSOR_BUILDING_ID).append("=").append(BuildingInformation.getBuildingIDFromName(building) == null ? "null" : BuildingInformation.getBuildingIDFromName(building));
                            insertString.append(",").append(MoniSoftConstants.SENSOR_FACTOR).append("=").append(Double.valueOf(factorString.replace(",", ".")));
                            insertString.append(",`").append(MoniSoftConstants.SENSOR_INTERVAL).append("`=").append(interval == null || interval.isEmpty() ? "0" : interval);
                            insertString.append(",").append(MoniSoftConstants.COUNTER_NO).append("=").append(counterNo != null ? "'" + counterNo + "'" : "null");
                            insertString.append(",").append(MoniSoftConstants.SENSOR_MEDIUM).append("=").append(medium != null ? "'" + medium + "'" : "null");
                            insertString.append(",").append(MoniSoftConstants.VIRT_DEF).append("=").append(virtual != null ? "'" + virtual + "'" : "null");
                            insertString.append(",").append(MoniSoftConstants.SENSOR_UTC_PLUX_X).append("=").append(utcPlusX);
                            insertString.append(",").append(MoniSoftConstants.SENSOR_SUMMERTIME).append("=").append(summertime);
                            insertString.append(" where ").append(MoniSoftConstants.SENSOR_KEY).append("='").append(keyName).append("'");                            
                        }
                    }
                    else
                    {
                        // Füge den neuen Sensor ein (Insert)
                        insertString.append("insert into ").append(MoniSoftConstants.SENSOR_TABLE).append(" set ");
                        insertString.append(MoniSoftConstants.SENSOR_NAME).append("='").append(name).append("'");
                        insertString.append(",").append(MoniSoftConstants.SENSOR_KEY).append("='").append(keyName).append("'");
                        insertString.append(",").append(MoniSoftConstants.SENSOR_DESCRIPTION).append("='").append(desc).append("'");
                        insertString.append(",").append(MoniSoftConstants.SENSOR_UNIT_ID).append("=").append(getUnitID(unit, name));
                        insertString.append(",").append(MoniSoftConstants.MIN_WORKDAY).append("=").append(minWT == null || minWT.isEmpty() ? "null" : minWT);
                        insertString.append(",").append(MoniSoftConstants.MAX_WORKDAY).append("=").append(maxWT == null || maxWT.isEmpty() ? "null" : maxWT);
                        insertString.append(",").append(MoniSoftConstants.MIN_WEEKEND).append("=").append(minWE == null || minWE.isEmpty() ? "null" : minWE);
                        insertString.append(",").append(MoniSoftConstants.MAX_WEEKEND).append("=").append(maxWE == null || maxWE.isEmpty() ? "null" : maxWE);
                        insertString.append(",").append(MoniSoftConstants.MAX_WEEKEND_CHANGETIME).append("=").append(validWE == null || validWE.isEmpty() ? "null" : validWE);
                        insertString.append(",").append(MoniSoftConstants.MAX_WORKDAY_CHANGETIME).append("=").append(validWT == null || validWT.isEmpty() ? "null" : validWT);
                        insertString.append(",").append(MoniSoftConstants.IS_EVENT).append("=").append(isEvent != null && isEvent.equals("1"));
                        insertString.append(",").append(MoniSoftConstants.IS_COUNTER).append("=").append(isCounter != null && isCounter.equals("1"));
                        insertString.append(",").append(MoniSoftConstants.IS_MANUAL).append("=").append(isManual != null && isManual.equals("1"));
                        insertString.append(",").append(MoniSoftConstants.IS_USAGE).append("=").append(isUsage != null && isUsage.equals("1"));
                        insertString.append(",").append(MoniSoftConstants.SENSOR_BUILDING_ID).append("=").append(BuildingInformation.getBuildingIDFromName(building) == null ? "null" : BuildingInformation.getBuildingIDFromName(building));
                        insertString.append(",").append(MoniSoftConstants.SENSOR_FACTOR).append("=").append(Double.valueOf(factorString.replace(",", ".")));
                        insertString.append(",`").append(MoniSoftConstants.SENSOR_INTERVAL).append("`=").append(interval == null || interval.isEmpty() ? "0" : interval);
                        insertString.append(",").append(MoniSoftConstants.COUNTER_NO).append("=").append(counterNo != null ? "'" + counterNo + "'" : "null");
                        insertString.append(",").append(MoniSoftConstants.SENSOR_MEDIUM).append("=").append(medium != null ? "'" + medium + "'" : "null");
                        insertString.append(",").append(MoniSoftConstants.VIRT_DEF).append("=").append(virtual != null ? "'" + virtual + "'" : "null");
                        insertString.append(",").append(MoniSoftConstants.SENSOR_UTC_PLUX_X).append("=").append(utcPlusX);
                        insertString.append(",").append(MoniSoftConstants.SENSOR_SUMMERTIME).append("=").append(summertime);
                        
                    }
                    
                }
            } catch (SQLException ex) {
                System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.SENSOREXISTS"));
            }

            if( insertString.length() > 0 )
                insertList.add(insertString.toString());
        }

        return insertList;
    }

    private String detectLocale(ArrayList<Map<String, String>> mapList) {
        String en = "_en_US";
        String de = "_de_DE";
        String locale = null;
        Map<String, String> map = mapList.get(0);
        for (String field : map.keySet()) {
            if (field.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + de).getString("DBCreator.MESSPUNKTKUERZEL"))) {
                locale = de;
            } else if (field.equals(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle" + en).getString("DBCreator.MESSPUNKTKUERZEL"))) {
                locale = en;
            }
        }

        return locale;
    }

    /**
     * Reads a csv-file and write the building table<p> Also fills reference
     * values in the file
     *
     * @param file the file to be read
     * @param encoding file encoding
     * @return true if the operation was successfull
     */
    public int fillBuildingTable(File file, String encoding, char delimiter) {
        csvPreference = new CsvPreference('"', delimiter, "\r\n");
        ArrayList<Map<String, String>> mapList = readCSVFileToMap(file, encoding); //

        if (mapList.isEmpty()) { // Die Datei enthält keine Einträge oder existiert nicht
            return FILE_NOT_FOUND;
        }

        Connection myConn = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            // SQL-Inserts aus den Gebäudedaten bauen und am Ende ausführen
            Iterator<String> it = createBuildingInserts(mapList).iterator();
            while (it.hasNext()) {
                try {
                    stmt.executeUpdate(it.next()); // Gebäude in die DB eintragen
                } catch (MySQLIntegrityConstraintViolationException ex) {
                    System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.GEBÄUDE SCHON VORHANDEN"));
                }
            }

            BuildingInformation.setBuildingList(new ListFiller().readBuildingList());

            // Die Bezugsgrössen aus der Datei holen und in die Referenztabelle eintragen
            Iterator<String> refIt = createReferenceInserts(mapList).iterator();
            while (refIt.hasNext()) {
                stmt.executeUpdate(refIt.next()); // Referenzen in die DB eintragen
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBCreator.NOPERMISSION"));
        } catch (SQLException e) {
            Messages.showException(e);
            return DBERROR;
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        return SUCCESS;
    }

    /**
     * Get the unit id from the Unit table. If there is no such unit in the
     * table ask the user if it should be created
     *
     * @param unit the unit name
     * @return the id of the unit
     */
    private Integer getUnitID(String unit, String sensor) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Integer id;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            // BINARY makes query case sensitive
            rs = stmt.executeQuery("select " + MoniSoftConstants.UNIT_ID + " from " + MoniSoftConstants.UNIT_TABLE + " where " + MoniSoftConstants.UNIT + "=BINARY'" + unit + "' limit 1");
            if (rs.next()) {
                id = rs.getInt(1);
            } else {
                NewUnitDialog d = new NewUnitDialog(this.parent, true, unit, sensor);
                d.setLocationRelativeTo(this.parent);
                d.setVisible(true);
                id = d.getUnitID();
            }
            return id;
        } catch (Exception ex) {
            Messages.showException(ex);
            return null;
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Check if the current databases scheme has vaild MoniSoft tables<p>
     *
     * @return the status of the scheme:<br> <code>IS_EMPTY</code> - there arte
     * no tables in the scheme<br> <code>HAS_STRUCTURE</code> - there is a valid
     * table structure for MoniSoft in the scheme<br>
     * <code>HAS_VALID_STRUCTURE</code> - there are tables in the scheme but it
     * is no MoniSoft structure<br>
     */
    public int hasStructure() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int state = IS_EMPTY;
        ArrayList tableList = new ArrayList(Arrays.asList(MoniSoftConstants.DB_TABLES.split(","))); // contains a list if table names

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("show tables");
            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();
            while (rs.next()) {
                tableList.remove(rs.getString(1)); // remove all table names from the list wich were found in the database
            }


            if (tableList.isEmpty()) { // if the list is empty all tables were found
                state = HAS_VALID_STRUCTURE;
            } else if (count > 0) {
                state = HAS_STRUCTURE;  // there are tables but some mandatory tables are missing
            } //  IS_EMPTY per definition
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return state;
    }
}
