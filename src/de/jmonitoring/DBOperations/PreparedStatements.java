package de.jmonitoring.DBOperations;

import de.jmonitoring.base.MoniSoftConstants;

/**
 * A collection of {@link PreparedStatements}
 * <p>TODO: move other statements in the application to here.
 *
 * @author togro
 */
public abstract class PreparedStatements {

    public final static String SELECT_BULDING_NAME_FROM_ID = "select " + MoniSoftConstants.BUILDING_NAME + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_ID + "=? limit 1";
    public final static String SELECT_BUILDING_ID_FROM_NAME = "select " + MoniSoftConstants.BUILDING_ID + " from " + MoniSoftConstants.BUILDING_TABLE + " where " + MoniSoftConstants.BUILDING_NAME + "=? limit 1";
    public final static String INSERT_NEW_BUILDING_NAME = " insert into " + MoniSoftConstants.BUILDING_TABLE + " set " + MoniSoftConstants.BUILDING_NAME + "=?";
    //SensorTable
    // AZ: Änderung Die Tabelle Sensor wird in Zukunft alphabetisch nach der Spalte Sensor sortiert - MONISOFT-1
    public final static String SELECT_ALL_FROM_SENSORTABLE = "select * from " + MoniSoftConstants.SENSOR_TABLE + " ORDER BY Sensor";
}
