package de.jmonitoring.DataHandling;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.DBMaintenance;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.utils.UnitCalulation.Unit;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import org.jfree.data.time.Month;

/**
 * This class hold method for the query of database values
 *
 * @author togro
 */
public class DatabaseQuery {

    private int sensorID;
    private Integer buildingIDOfSensor;
    private boolean verbose = false;
    private LinkedList<Integer> missingMonthsList;
    private final Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    public static long ONE_HOUR = 3600 * 1000L;
    
    /**
     * Create a Instance for the given sensor
     *
     * @param id The sensorID
     */
    public DatabaseQuery(int id) {
        sensorID = id;
        buildingIDOfSensor = SensorInformation.getSensorProperties(sensorID).getBuildingID();
    }

    /**
     * A default query on the T_History table for the given time range.
     *
     * @param dateInterval The time range
     * @param ignoreLog If <code>true</code> even measurements that are marked
     * as invalid will be read
     * @return The {@link MeasurementTreeSet} containing all
     * {@link Measurement}s
     */
    public MeasurementTreeSet simpleQueryResult(DateInterval dateInterval, boolean ignoreLog) {
        MeasurementTreeSet measurementSet = new MeasurementTreeSet();
        if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
            return measurementSet;
        }
        Connection myConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        int log;
        try {
            DBMaintenance dbm = new DBMaintenance();
            if (!dbm.hasDefaultLogEntry()) {
                dbm.fixLog();
            }

            myConn = DBConnector.openConnection();
            long startSecond = dateInterval.getStartDate().getTime() / 1000L;
            
            // MONISOFT-22: Verschiebe das Zeitintervall nach vorne wenn der Startzeitpunkt in der MESZ liegt
            if( SensorInformation.getSensorProperties(sensorID).isSummerTime() &&   
                DateInterval.isInSummertime( dateInterval.getStartDate().getTime() ) )
                startSecond -= ONE_HOUR / 1000;
            
            long endSecond = dateInterval.getEndDate().getTime() / 1000L;
            
            // MONISOFT-22: Verschiebe das Zeitintervall wenn der Endzeitpunkt in der MESZ liegt
            if( SensorInformation.getSensorProperties(sensorID).isSummerTime() &&   
                DateInterval.isInSummertime( dateInterval.getEndDate().getTime() ) )
                endSecond -= ONE_HOUR / 1000;

            if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseIgnoreValue").equals("1")) { // ignore certain values that are error-values by definition
                
                String ignoreValue = MoniSoft.getInstance().getApplicationProperties().getProperty("IgnoreValue");
                
                // AZ: Ermittle alle zu ignorierenden Werte (Trenne nach Komma auf) - MONISOFT-10
                if( ignoreValue != null )
                {
                    List<String> ignoreValueList = Arrays.asList(ignoreValue.split(","));                    

                    if( ignoreValueList != null && ignoreValueList.size() > 0 )
                    {             
                        String sqlIgnoreText = "";

                        for( int i = 0; i < ignoreValueList.size(); i++ )
                        {
                            try
                            {
                                Double.valueOf(ignoreValueList.get(i) );                                
                                
                                // AZ: Kein Umwandlungsfehler. Hier liegt ein gültiger Kommazahlenwert vor
                                sqlIgnoreText += " and hist.value <> ? ";                                                                
                            }
                            catch( Exception e )
                            {
                                Messages.showMessage("Warning during read ignoreValue " + ignoreValueList.get(i) + ": " + e.getMessage() + "\n", true);                                
                            }                            
                        }
                        
                        pstmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join "
                                + "T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors= ? and hist.TimeStamp >= ? and hist.TimeStamp <= ? " + sqlIgnoreText );
                        
                        int tempValidValues = 0;
                        
                        for( int i = 0; i < ignoreValueList.size(); i++ )
                        {
                            try
                            {
                                Double ignoreValueDouble = Double.valueOf(ignoreValueList.get(i));
                                pstmt.setDouble(4+tempValidValues, ignoreValueDouble );
                                tempValidValues++;
                            }
                            catch( Exception e )
                            {                                
                                
                            }
                        }
                    }
                }
            } else {
                pstmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors= ? and hist.TimeStamp >= ? and hist.TimeStamp <= ?");
            }
            pstmt.setInt(1, sensorID);
            pstmt.setLong(2, startSecond);
            pstmt.setLong(3, endSecond);
            rs = pstmt.executeQuery();
            rs.last();
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DataBaseQuery.SENSOR") + " '" + SensorInformation.getDisplayName(sensorID) + "': " + rs.getRow() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DataBaseQuery.READ") + "\n", verbose);
            rs.beforeFirst();
            
            System.out.println( "Sensor: " + sensorID + " utc plus x: " + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() );
            
            while (rs.next()) {
                log = rs.getInt(3); // Die Art des Log-Events
                if (ignoreLog || log != MoniSoftConstants.LOG_INVALID) { // Hinzufügen, wenn Messwert nicht ungültig ist.....                    
                    // System.out.println( "Messwert gültig: " + rs.getLong(1) * 1000L + " Value: " + rs.getDouble(2) );
                    // AZ: MONISOFT-22 Addiere UTC plus x-Zeit                    
                    // System.out.println( "Zeit vorher: " + rs.getDouble(1) + " Zeit nachher: " + rs.getDouble(1) + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() * 1000 + " Messwert: " + rs.getDouble(2) );
                    
                    long time = ( rs.getLong(1) + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() ) * 1000L;
                    
                    // AZ: Prüfe, ob der Sensor Sommerzeit-relevant ist.                    
                    if( SensorInformation.getSensorProperties(sensorID).isSummerTime() )
                    {   
                        if( DateInterval.isInSummertime( time ) )
                            time += ONE_HOUR;                            
                        
                        // System.out.println( "Measurement: time: " + time + " value: " + rs.getDouble(2) );
                        measurementSet.add(new Measurement( time, rs.getDouble(2)));
                    }
                    else
                    {
                        // System.out.println( "nicht Sommerzeit-relevant" );
                        measurementSet.add(new Measurement( time, rs.getDouble(2)));
                    }
                }
                else
                {
                    System.out.println( "Messwert ungültig: " + rs.getLong(1) * 1000L + " Value: " + rs.getDouble(2) );
                }
            }
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showMessage("Error during database query [SimpleQuery]: " + ex.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, pstmt, rs);
        }
        return measurementSet;
    }
    
    public MeasurementTreeSet simpleQueryResultForDate(Date startDate, Date endDate, boolean ignoreLog) {
        MeasurementTreeSet measurementSet = new MeasurementTreeSet();
        if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
            return measurementSet;
        }
        Connection myConn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        int log;
        try {
            DBMaintenance dbm = new DBMaintenance();
            if (!dbm.hasDefaultLogEntry()) {
                dbm.fixLog();
            }

            myConn = DBConnector.openConnection();
            long startSecond = startDate.getTime() / 1000L;
            // long endSecond = endDate.getTime() / 1000L;
            long endSecond = endDate.getTime() / 1000L;

            if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseIgnoreValue").equals("1")) { // ignore certain values that are error-values by definition
                
                String ignoreValue = MoniSoft.getInstance().getApplicationProperties().getProperty("IgnoreValue");
                
                // AZ: Ermittle alle zu ignorierenden Werte (Trenne nach Komma auf) - MONISOFT                          
                if( ignoreValue != null )
                {
                    List<String> ignoreValueList = Arrays.asList(ignoreValue.split(","));                    

                    if( ignoreValueList != null && ignoreValueList.size() > 0 )
                    {             
                        String sqlIgnoreText = "";

                        for( int i = 0; i < ignoreValueList.size(); i++ )
                        {
                            try
                            {
                                Double.valueOf(ignoreValueList.get(i) );                                
                                
                                // AZ: Kein Umwandlungsfehler. Hier liegt ein gültiger Kommazahlenwert vor
                                sqlIgnoreText += " and hist.value <> ? ";                                                                
                            }
                            catch( Exception e )
                            {
                                Messages.showMessage("Warning during read ignoreValue " + ignoreValueList.get(i) + ": " + e.getMessage() + "\n", true);                                
                            }                            
                        }
                        
                        pstmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join "
                                + "T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors= ? and hist.TimeStamp >= ? and hist.TimeStamp <= ? " + sqlIgnoreText );
                        
                        int tempValidValues = 0;
                        
                        for( int i = 0; i < ignoreValueList.size(); i++ )
                        {
                            try
                            {
                                Double ignoreValueDouble = Double.valueOf(ignoreValueList.get(i));
                                pstmt.setDouble(4+tempValidValues, ignoreValueDouble );
                                tempValidValues++;
                            }
                            catch( Exception e )
                            {                                
                                
                            }
                        }
                    }
                }
            } else {
                pstmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors= ? and hist.TimeStamp >= ? and hist.TimeStamp <= ?");
            }
            pstmt.setInt(1, sensorID);
            pstmt.setLong(2, startSecond);
            pstmt.setLong(3, endSecond);
            rs = pstmt.executeQuery();
            rs.last();
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DataBaseQuery.SENSOR") + " '" + SensorInformation.getDisplayName(sensorID) + "': " + rs.getRow() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("DataBaseQuery.READ") + "\n", verbose);
            rs.beforeFirst();
            while (rs.next()) {
                log = rs.getInt(3); // Die Art des Log-Events
                if (ignoreLog || log != MoniSoftConstants.LOG_INVALID) { // Hinzufügen, wenn Messwert nicht ungültig ist.....                    
                    // System.out.println( "Messwert gültig: " + rs.getLong(1) * 1000L + " Value: " + rs.getDouble(2) );
                    measurementSet.add(new Measurement(rs.getLong(1) * 1000L, rs.getDouble(2)));
                }
                else
                {
                    System.out.println( "Messwert ungültig: " + rs.getLong(1) * 1000L + " Value: " + rs.getDouble(2) );
                }
            }
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showMessage("Error during database query [SimpleQuery]: " + ex.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, pstmt, rs);
        }
        return measurementSet;
    }

    /**
     * Returns a map of monthly value for the given year
     *
     * @param year The year
     * @return A {@link TreeMap} of values with the month as key and the
     * corresponding value as content
     */
    public TreeMap<Month, Double> getMonthlyValuesForYear(int year) {
        TreeMap<Month, Double> map = new TreeMap<Month, Double>();
        return map;
    }

    /**
     * Returns a value for the given year
     *
     * @param year The year
     * @param reference The reference to use
     * @param targetUnit The traget unit
     * @param climateFactor The climatefactor
     * * @param useYearReference If <code>true</code> the consumption will be
     * calculated on a "per year" basis. Missing months will be considered to
     * have a consumption equal to the mean of the months with valid values
     * @return The yearly value
     */
    public Double getValueForYear(int year, String reference, Unit targetUnit, Double climateFactor, boolean useYearReference) {
        return getValueFor(1, year, reference, targetUnit, climateFactor, true, useYearReference);
    }

    public int getSensorID() {
        return sensorID;
    }

    public void setSensorID(int id) {
        sensorID = id;
    }

    public List<Integer> getMissingMonths() {
        return missingMonthsList;
    }

    public void setVerbose(boolean b) {
        verbose = b;
    }

    /**
     * Calculates a (specific) value für the given year and month.<p> This
     * method uses data stored in T_Monthly database
     *
     * @param year The year (not <code>null</code>)
     * @param reference (<code>null</code> valid)
     * @param targetUnit ( <code>null</code> valid, unit will be kept
     * @param climateFactor (<code>null</code> possible, factor will be 1)
     * @param fullYear If <code>true</code> calculate for 12 month from the
     * given month, otherwise return value for this month
     * @param useYearReference If <code>true</code> the consumption will be
     * calculated on a "per year" basis. Missing months will be considered to
     * have a consumption equal to the mean of the months with valid values
     * @return The calculated value
     */
    public Double getValueFor(int month, int year, String reference, Unit targetUnit, Double climateFactor, boolean fullYear, boolean useYearReference) {
//        System.out.println("******************************");
        String compareString = getCompareString(month, year, fullYear);

        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Double value = null;
        BigDecimal entry;
        Double refValue = 1d; // reference value is 1 if no valid reference is given
        Double climateCorrectionFactor = climateFactor == null ? 1 : climateFactor;

        // Alle Referenzen für das aktuelle Gebäude holen
        ArrayList<ReferenceValue> refList = BuildingInformation.getBuildingReferences(buildingIDOfSensor);
        for (ReferenceValue ref : refList) {
            if (ref.getName().equals(reference) && ref.getValue() > 0d) {
                refValue = ref.getValue(); // if a valid reference was given set it
            }
        }

        missingMonthsList = new LinkedList<Integer>(Arrays.asList(months)); // set the list of all months of a year
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
//            System.out.println("select " + MoniSoftConstants.MONTHLY_MONTH + "," + MoniSoftConstants.MONTHLY_VALUE + " from " + MoniSoftConstants.MONTHLY_TABLE + " where " + MoniSoftConstants.MONTHLY_SENSOR + "=" + sensorID + " and concat(year,\"-\",month,\"#\") in " + compareString);
            rs = stmt.executeQuery("select " + MoniSoftConstants.MONTHLY_MONTH + "," + MoniSoftConstants.MONTHLY_VALUE + " from " + MoniSoftConstants.MONTHLY_TABLE + " where " + MoniSoftConstants.MONTHLY_SENSOR + "=" + sensorID + " and concat(year,\"-\",month,\"#\") in " + compareString);
            while (rs.next()) {
                value = value == null ? 0 : value; // Zu Beginn Wert noch leer
                entry = rs.getBigDecimal(2);
                if (entry != null) {
                    missingMonthsList.remove((Integer) rs.getInt(1)); // if we have a value: remove month from the missing list
                    value += entry.doubleValue();
                }
            }
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showMessage("Error during database query [getValueForYear]: " + ex.getMessage() + "\n", true);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
//        System.out.println("missing months: " + missingMonthsList.size());
        value = value == null ? 0 : value; // if we have illegal values set to 0
//        System.out.println("Raw value :" + value);
        // apply climate Correcction
        value = value * climateCorrectionFactor;
//        System.out.println("climate corrected value :" + value);
        // unit transfer
//        System.out.println("target unit " + targetUnit + " sensor " + SensorInformation.getSensorProperties(sensorID).getSensorUnit().getUnit());
        value = UnitTransfer.transfer(SensorInformation.getSensorProperties(sensorID).getSensorUnit(), targetUnit, value);
//        System.out.println("in target unit :" + value);
        // apply reference value 
        if (value == null) { // if the value is noll here it could not be transferred to the target unit
            return null;
        }
        value = value / refValue;
//        System.out.println("reference applied :" + value + " (" + refValue + ") " + reference);
        //apply time (year) reference ("per year")
        if (useYearReference) {
            Integer numberOfValidMonths = 12 - missingMonthsList.size();
//            System.out.println("month to use  :" + numberOfValidMonths);
            value = value / numberOfValidMonths;
//            System.out.println("year ref applid :" + value);
        }
//        System.out.println("RESULTING VALUE: " + value);
        return value;
    }

    /**
     * Builds a string of the year and month used to get the relevant entries
     * from the database:<p> e.g. for the months 03/2011 to 02/2012 (12
     * months):<br> "(2011-3#, 2011-4#,2011-5#, 2011-6#,2011-7#,
     * 2011-8#,2011-9#, 2011-10#,2011-11#, 2011-12#,2012-1#, 2012-2#)"
     *
     * @param month The month
     * @param year The year
     * @param fullYear If <code>true</code> a string for 12 month will be
     * generated oterwise only for one month
     * @return The string
     */
    private String getCompareString(int month, int year, boolean fullYear) {
        int maxCount;
        if (fullYear) {
            maxCount = 12;
        } else {
            maxCount = 1;
        }
        String s = "(";
        String sep = "";
        int m = month - 1;
        int y = year;
        for (int i = 0; i < maxCount; i++) {
            m++;
            if (m > 12) {
                m = 1;
                y++;
            }
            s += sep + "\"" + y + "-" + m + "#" + "\"";
            sep = ",";
        }
        s += ")";
        return s;
    }
}
