package de.jmonitoring.base;

import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.References.ReferenceDescription;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * This class contains common constants used throughout the application
 *
 * @author togro
 */
public class MoniSoftConstants {

    /**
     * Returns the name of the current version
     *
     * @return
     */
    public static String getVersion() {
        return "16-11-2017";
    }
    /**
     * Format dd.MM.yyy
     */
    public final static String HumanDateFormat = "dd.MM.yyyy";
    /**
     * Format dd.MM.yyyy HH:mm:s
     */
    public final static String HumanDateTimeFormat = "dd.MM.yyyy HH:mm:ss";
    /**
     * Format yyyy-MM-dd
     */
    public final static String MySQLDateFormat = "yyyy-MM-dd";
    /**
     * Format yyyy-MM-dd HH:mm:ss
     */
    public final static String MySQLDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    /**
     * Format HH:mm:ss
     */
    public final static String HumanTimeFormat = "HH:mm:ss";
    public final static int WORKDAY = 0;
    public final static int WEEKEND = 1;
    public final static int MINIMUM = 0;
    public final static int MAXIMUM = 1;    // Namen der Tabelle in der Datenbank
    public final static String SENSOR_TABLE = "T_Sensors";
    public final static String UNIT_TABLE = "T_Units";
    public final static String HISTORY_TABLE = "T_History";
    public final static String EVENT_TABLE = "T_Events";
    public final static String COUNTERCHANGE_TABLE = "T_CounterChanges";
    public final static String FACTORS_TABLE = "T_Factors";
    public final static String REFERENCEHIERARCHY_TABLE = "T_ReferenceHierarchy";
    public final static String REFERENCEMAP_TABLE = "T_References";
    public final static String REFERENCES_TABLE = "T_ReferenceNames";
    public final static String CLUSTER_TABLE = "T_Clusters";
    public final static String LOG_TABLE = "T_Log";
    public final static String BUILDING_TABLE = "T_Building";
    public final static String CONFIG_TABLE = "T_Config";
    public final static String CATEGORY_TABLE = "T_Categories";
    public final static String SENSORCOLLECTION_TABLE = "T_SensorCollections";
    public final static String MONTHLY_TABLE = "T_Monthly";
    public final static String WEATHERDEF_TABLE = "T_WeatherDefinition";
    public final static String GRAPHICS_TABLE = "T_Graphics";
    // Feldnamen in Historytabelle
    public final static String HISTORY_TIMESTAMP = "TimeStamp";
    public final static String HISTORY_SENSOR_ID = "T_Sensors_id_Sensors";
    public final static String HISTORY_VALUE = "Value";
    public final static String HISTORY_LOG_ID = "T_Log_id_Log";
    // Feldnamen in Sensortabelle
    public final static String SENSOR_ID = "id_Sensors";
    public final static String SENSOR_KEY = "SensorKey";
    public final static String SENSOR_UNIT_ID = "T_Units_id_Units";
    public final static String SENSOR_BUILDING_ID = "T_Building_id_Building";
    public final static String SENSOR_NAME = "Sensor";
    public final static String MIN_WEEKEND = "MinWE";
    public final static String MAX_WEEKEND = "MaxWE";
    public final static String MIN_WORKDAY = "MinWT";
    public final static String MAX_WORKDAY = "MaxWT";
    public final static String MAX_WEEKEND_CHANGETIME = "MaxChangeTimeWE";
    public final static String MAX_WORKDAY_CHANGETIME = "MaxChangeTimeWT";
    public final static String SENSOR_DESCRIPTION = "Description";
    public final static String SENSOR_INTERVAL = "Interval";
    public final static String IS_COUNTER = "isCounter";
    public final static String IS_USAGE = "isUsage";
    public final static String VIRT_DEF = "Virtual";
    // AZ: MONISOFT-22: Zeitzonen
    public final static String SENSOR_UTC_PLUX_X = "UtcPlusX";
    public final static String SENSOR_SUMMERTIME = "Summertime";
    
//    public final static String IS_PERIODIC = "Periodic";
    public final static String IS_MANUAL = "Manual";
    public final static String IS_EVENT = "isEvent";
    public final static String SENSOR_FACTOR = "Factor";
    public final static String COUNTER_NO = "counterNo";
    public final static String SENSOR_MEDIUM = "medium";
    public final static String SENSOR_CONSTANT = "Constant";
    public final static String IS_RESETCOUNTER = "isResetCounter";
//    public final static String MAX_UPDATETIME = "MaxUpdateTime";
    // Feldnamen in Unit-Tabelle
    public final static String UNIT = "Unit";
    public final static String UNIT_ID = "id_Units";
    // Feldnamen in T_Config
    public final static String DB_VERSION = "DBVersion";
    // Feldnamen in T_Counterchanges
    public final static String COUNTERCHANGE_SENSORID = "T_Sensors_id_Sensors";
    public final static String COUNTERCHANGE_TIME = "Time";
    public final static String COUNTERCHANGE_LASTVALUE = "LastValue";
    public final static String COUNTERCHANGE_FIRSTVALUE = "FirstValue";
    // Feldnamen in T_ReferenceHierarchy
    public final static String REFERENCE_VALUE = "Value";
    public final static String REFERENCE_ID = "id_ReferenceHierarchy";
    public final static String REFERENCE_NAME = "ReferenceName";
    public final static String REFERENCE_UNIT_ID = "T_Units_id_Units";
    // Feldnamen in T_ReferenceNames
    public final static String REFERENCENAME_NAME = "RefName";
    public final static String REFERENCENAME_UNIT_ID = "unitID";
    public final static String REFERENCENAME_DESCRIPTION = "Description";
    public final static String REFERENCENAME_ID = "id";
    // Feldnamen in T_References
    public final static String REFERENCEMAP_REFERENCE_ID = "T_ReferenceHierarchy_id_ReferenceHierarchy";
    public final static String REFERENCEMAP_SENSOR_ID = "T_Sensors_id_Sensors";
    public final static String REFERENCEMAP_NAME = "Name";
    public final static String REFERENCEMAP_BUILDING_ID = "T_Building_id_Building";
    public final static String REFERENCEMAP_VALUE = "Value";
    public final static String REFERENCEMAP_UNIT_ID = "T_Units_id_Units";
    // Feldnamen in T_Building
    public final static String BUILDING_NAME = "BuildingName";
    public final static String BUILDING_ID = "id_Building";
    public final static String BUILDING_STREET = "Strasse";
    public final static String BUILDING_PLZ = "PLZ";
    public final static String BUILDING_CONTACT = "Ansprechpartner";
    public final static String BUILDING_PHONE = "Telefon";
    public final static String BUILDING_NETWORKING = "Netzwerkdaten";
    public final static String BUILDING_CITY = "Ort";
    public final static String BUILDING_DESCRIPTION = "Beschreibung";
    public final static String BUILDING_COLLECTIONIDS = "SensorCollectionIDs";
    public final static String BUILDING_ACTIVE = "active";
    // Feldnamen in T_Factors
    public final static String FACTOR_TIME = "Time";
    public final static String FACTOR_VALUE = "Value";
    public final static String FACTOR_SENSOR_ID = "T_Sensors_id_Sensors";
    // Feldnamen in T_Categories
    public final static String LFT = "lft";
    public final static String RGT = "rgt";
    public final static String NODE = "catnode";
    public final static String SET = "catset";
    // Feldnamen in T_SensorCollections
    public final static String SENSORCOLLECTION_NAME = "colname";
    public final static String SENSORCOLLECTION_LIST = "sensors";
    public final static String SENSORCOLLECTION_CREATOR = "creator";
    public final static String SENSORCOLLECTION_ID = "id";
    // Feldnamen in T_Clusters
    public final static String CLUSTER_NAME = "ClusterName";
    public final static String CLUSTER_KAT = "ClusterKat";
    public final static String CLUSTER_BUILDINGS = "Buildings";
    public final static String CLUSTER_ID = "id_Clusters";
    // Feldnamen in T_Monthly
    public final static String MONTHLY_MONTH = "Month";
    public final static String MONTHLY_YEAR = "Year";
    public final static String MONTHLY_VALUE = "Value";
    public final static String MONTHLY_LOG = "T_Log_id_Log";
    public final static String MONTHLY_SENSOR = "T_Sensors_id_Sensors";
    // Intervallkategorien (vorläufig)
    public final static int HOUR_CATEGORY = 0;
    public final static int DAY_CATEGORY = 1;
    public final static int WEEK_CATEGORY = 3;
    public final static int MONTH_CATEGORY = 2;
    public final static int YEAR_CATEGORY = 4;
    public final static int LOG_INVALID = 0;
    // LOG EventTypes
    public final static int LOG_MANUAL = 1;
    public final static int LOG_FACTORCHANGE = 2;
    public final static int LOG_COUNTERCHANGE = 3;
    public final static int LOG_DEFAULT = 4;
    //
    public final static int LEFT = 0;
    public final static int RIGHT = 1;
    //
    public final static int START = 1;
    public final static int END = 0;
    //
    public final static String NO_SENSOR_SELECTED = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("<KEINE>");
    public final static String ALL = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ALLSENSORS");
    //
    public final static double RANGE_BOUNDARY = 999999999f;
    //
    public final static double RAW_INTERVAL = -1d;
    public final static double HOUR_INTERVAL = -2d;
    public final static double DAY_INTERVAL = -3d;
    public final static double WEEK_INTERVAL = -4d;
    public final static double MONTH_INTERVAL = -5d;
    public final static double YEAR_INTERVAL = -6d;
    //
    public final static int TS_LINES = 0;
    public final static int TS_BARS = 1;
    public final static int TS_POINTS = 2;
    public final static int TS_AREA = 3;
    //
    public final static int MAX_ALLOWED_OFFSET = 60; // in Minuten
    //
    public final static int IFRAME_ORIGIN_OFFSET = 30;
    //
    public final static String userPrefs = "userPrefs";
    //
    public final static String chooseFile = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("<DATEI WÄHLEN>");
    //
    public final static String chooseSensor = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("<MESSPUNKT WÄHLEN>");
    // Zeichensätze
    public final static String ISO8859 = "ISO-8859-1";
    public final static String UTF8 = "UTF-8";
    // Erforderliche Tabellen
    public final static String DB_TABLES = "T_Building,T_Config,T_CounterChanges,T_Events,T_Factors,T_History,T_Log,T_Sensors,T_Units";
    // Datumsberechung
    public final static long ONE_DAY_IN_SECONDS = 86400;
    public final static long ONE_DAY_IN_MILLISECONDS = ONE_DAY_IN_SECONDS * 1000L;
    // Namen der Anwendungsordner
    public final static String GRA_FOLDER = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CHARTBESCHREIBUNGEN");
    public final static String SER_FOLDER = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MESSPUNKTSAMMLUNGEN");
    public final static String PIC_FOLDER = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("EXPORTIERTE_GRAFIKEN");
    public final static String DATA_FOLDER = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("EXPORTIERTE_DATEN");
    public final static String PROJECT_PROPS_FILE = "projectProperties";
    // gespeicehrte Grfaiken
    public final static String NO_CHARTDESCRIPTION_AVAILABLE = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_DESCR");
    public final static String NO_CHARTDESCRIPTION_SELECTED = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_DESCR");
    // Spaltennamen für Gebäude beim Lesen einer Gebäudeliste
    public final static String CSV_BUILDING_NAME = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NAME");
    public final static String CSV_BUILDING_STREET = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("STRASSE");
    public final static String CSV_BUILDING_PLZ = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PLZ");
    public final static String CSV_BUILDING_CITY = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ORT");
    public final static String CSV_BUILDING_CONTACT = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ANSPRECHPARTNER");
    public final static String CSV_BUILDING_PHONE = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TELEFON");
    public final static String CSV_BUILDING_NETWORK = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NETZWERKDATEN");
    public final static String CSV_BUILDING_DESCRIPTION = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BESCHREIBUNG");
    // Standardfarben
    public final static List<Color> ColorTable = Collections.unmodifiableList(Arrays.asList(
            new Color(219, 15, 15),
            new Color(0, 0, 215),
            new Color(2, 196, 2),
            new Color(255, 153, 0),
            new Color(129, 12, 12),
            new Color(3, 46, 131),
            new Color(2, 120, 2),
            new Color(255, 203, 70),
            new Color(152, 98, 203),
            new Color(0, 187, 157),
            new Color(158, 154, 76),
            new Color(0, 152, 255),
            new Color(254, 241, 130),
            new Color(255, 97, 82)));
//    };
    // Color for SebsorCollectionPanel
    public final static Color SENSORCOLLECTION_PANEL_BACKGROUND = new Color(224, 169, 0);
    // CSV-Import
    public final static String UNIXTIMSTAMP = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("UNIX ZEITSTEMPEL");
    public final static String UNIXTIMEFORMAT = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("0000000000");
    // Wetterdefinitionen (in T_WeatherDefinition)
    public final static String WEATHER_OUTSIDE_TEMPERATURE = "OUTSIDE_TEMP";
    public final static String WEATHER_OUTSIDE_HUMIDITY = "OUTSIDE_RH";
    public final static String WEATHER_RAIN_STATUS = "RAIN_STATUS";
    public final static String WEATHER_RAIN_AMOUNT = "RAIN_AMOUNT";
    public final static String WEATHER_CARBON_DIOXIDE = "CARBON_DIOXIDE";
    public final static String WEATHER_WIND_DIRECTION = "WIND_DIRECTION";
    public final static String WEATHER_WIND_SPEED = "WIND_SPEED";
    public final static String WEATHER_GLOBAL_RADIATION = "GLOBAL_RADIATION";
    // Demo-Hint
    public final static String DEMO = "Funktion in Demo-Version nicht verfügbar." + "\n";
    // Plot types
    public final static Integer PLOTTYPE_OTHER = 0;
    public final static Integer PLOTTYPE_TIMESERIES = 1;
    public final static Integer PLOTTYPE_SCATTER = 2;
    public final static Integer PLOTTYPE_OGIVE = 3;
    public final static Integer PLOTTYPE_COMPARE = 4;
    public final static Integer PLOTTYPE_CARPET = 5;
    public final static Integer PLOTTYPE_WIND = 6;

    // Exceptions
    public static class InvalidMONFileException extends Exception {

        @Override
        public String getMessage() {
            return (java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DIE GEWÄHLTE DATEI IST KEINE GÜLTIGE DATEN-EINGADEDATEI"));
        }
    }

    /**
     * "Translates" the index of Comboboxes to the correct field seperator <br>
     * 0 = ","<br> 1 = ";"<br> 2 = "\t"<br> 3 = " "
     *
     * @param index
     * @return
     */
    public static String getFieldSeparator(int index) {
        switch (index) {
            case 0:
                return ",";
            case 1:
                return ";";
            case 2:
                return "\t";
            case 3:
                return " ";
        }
        // Standard falls ein falscher index
        return ";";
    }

    /**
     * Liefert das Format für Dezimalzahlen zurück. Die Anzahl der
     * Nachkommastellen legt der übergebene Formatstring fest<br> 0 =
     * Dezimaltrenner ist der Punkt<br> 1 = Dezimaltrenner ist das Komma<br>
     *
     * @param index
     * @param format
     * @return
     */
    public static DecimalFormat getDecimalFormat(int index, String format) {
        DecimalFormat decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("de", "DE"));  // standard Komma
        switch (index) {
            case 0:
                decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("en", "EN"));   // Punkt
                break;
            case 1:
                decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("de", "DE"));   // Komma
                break;
        }
        decFormat.applyPattern(format); // Format für die Ausgabe der Zahlenwerte
        return decFormat;
    }

    /**
     * Liefert eine Liste der verfügbaren Standard-Bezugsgrößen für Gebäude
     * (nach DIN 277 2005)
     *
     * @return
     */
    public static ArrayList<ReferenceDescription> getDefaultReferences() {
        // TODO Vom Nutzer bearbeitbar machen: aus DB holen
        ArrayList<ReferenceDescription> list = new ArrayList<ReferenceDescription>(36);
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BGF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BRUTTO-GRUNDFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NGF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NETTO-GRUNDFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF1"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("WOHNEN UND AUFENTHALT"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF2"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BÜROARBEIT"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF3"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PRODUKTION, HAND- UND MASCHINENARBEIT, EXPERIMENTE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF4"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LAGERN, VERTEILEN UND VERKAUFEN"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF5"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BILDUNG, UNTERRICHT UND KULTUR"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HNF6"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HEILEN UND PFLEGEN"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("EBF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ENERGIEBEZUGSFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TECHNISCHE FUNKTIONSFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NUTZFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("KGF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("KONSTRUKTIONSFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VF"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VERKEHRSFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MA"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ANZAHL ARBEITSPLÄTZE"), UnitInformation.getIDFromUnitName("MA")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BRI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BRUTTO-RAUMINHALT"), UnitInformation.getIDFromUnitName("m³")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NRI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NETTO-RAUMINHALT"), UnitInformation.getIDFromUnitName("m³")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("KRI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("KONSTRUKTIONS-RAUMINHALT"), UnitInformation.getIDFromUnitName("m³")));
        list.add(new ReferenceDescription(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FENSTERFL"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FENSTERFLÄCHE"), UnitInformation.getIDFromUnitName("m²")));
        return list;
    }
    private final static List<String> MONTHNAMES = Collections.unmodifiableList(Arrays.asList(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("JAN"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEB"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MÄR"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("APR"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MAI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("JUN"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("JUL"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("AUG"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SEP"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("OKT"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOV"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DEZ")));
    private final static List<String> DAYNAMES = Collections.unmodifiableList(Arrays.asList(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SO"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MO"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MI"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DO"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FR"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SA")));

    /**
     * Returns the month abbratavion (JAN/FEB, etc.) for the given month<p> 0 =
     * january<br>11 = december
     *
     * @param index
     * @return the name of the month
     */
    public static String getMonthFor(Integer index) {
        return MONTHNAMES.get(index);
    }

    /**
     * Returns the day abbratavion (MON/TUE, etc.) for the given week day<p> 0 =
     * sunday<br>6 = saturday
     *
     * @param index
     * @return the name of the weekday
     */
    public static String getDayNameFor(Integer index) {
        return DAYNAMES.get(index);
    }
}
