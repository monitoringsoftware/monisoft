package de.jmonitoring.utils.UnitCalulation;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A helper class for getting information from the unit system about units
 *
 * @author togro
 */
public class UnitInformation {

    private static HashMap<Integer, Unit> unitList = null;

    /**
     * Get the unit name from the unitID
     *
     * @param id id of unit
     * @return name of unit
     */
    public static String getUnitNameFromID(int id) {
        return unitList.get(id).getUnit();
    }

    /**
     * Get the unitID from its name
     *
     * @param unit The name of the unit
     * @return The unit id <code>null</code> if not existant
     */
    public static Integer getIDFromUnitName(String unit) {
        for (Integer i : unitList.keySet()) {
            if (unitList.get(i).getUnit().equals(unit)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Refersh the database with the units of the given {@link Unit} list.
     *
     * @param units The list of units
     */
    public static void refreshUnits(ArrayList<Unit> units) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> alreadyHandledUnitNames = new ArrayList<String>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            // loop units and refresh database
            for (Unit unit : units) {
                // refresh of update
                stmt.executeUpdate("insert ignore into " + MoniSoftConstants.UNIT_TABLE + " set " + MoniSoftConstants.UNIT + "='" + unit.getUnit() + "'", Statement.RETURN_GENERATED_KEYS);  // + MoniSoftConstants.UNIT_ISUSAGE + "=" + unit.isUsageUnit() + " ON DUPLICATE KEY UPDATE " + MoniSoftConstants.UNIT_ISUSAGE + "=" + unit.isUsageUnit(), Statement.RETURN_GENERATED_KEYS);
                alreadyHandledUnitNames.add(unit.getUnit());
            }

            // loop global unit list an look for units that are no longer existsing
            for (Iterator<Unit> it = unitList.values().iterator(); it.hasNext();) {
                Unit unit = it.next();
                if (!units.contains(unit) && !alreadyHandledUnitNames.contains(unit.getUnit())) { // if not existing delete
                    deleteUnit(unit);
                }
            }
            alreadyHandledUnitNames.clear();

            // refersh global unit list
            setUnitList(new ListFiller().readUnitList());

        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Inserts of opdates a single {@link Unit} in the database
     *
     * @param unit The {@link Unit}
     * @return The id of the new {@link Unit}
     */
    public static Integer insertOrUpdateUnit(Unit unit) {
        Integer id = null;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("insert ignore into " + MoniSoftConstants.UNIT_TABLE + " set " + MoniSoftConstants.UNIT + "='" + unit.getUnit() + "'", Statement.RETURN_GENERATED_KEYS); // ON DUPLICATE KEY UPDATE " + MoniSoftConstants.UNIT_ISUSAGE + "=" + unit.isUsageUnit(), Statement.RETURN_GENERATED_KEYS);
            rs = stmt.getGeneratedKeys();

            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        // refresh global unit list
        setUnitList(new ListFiller().readUnitList());

        return id;
    }

    /**
     * Delete the given {@link Unit} from the database
     *
     * @param unit The {@link Unit} to be deleted
     */
    public static void deleteUnit(Unit unit) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.UNIT_TABLE + " where " + MoniSoftConstants.UNIT + "='" + unit.getUnit() + "'");
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        // refresh global unit list
        setUnitList(new ListFiller().readUnitList());
    }

    /**
     * Get a list of all units
     *
     * @return A {@link HashMap} of units with the id as map key
     */
    public static HashMap<Integer, Unit> getUnitList() {
        return unitList;
    }

    /**
     * Set the global unit list to this list
     *
     * @param {@link HashMap} of units with the id as map key
     */
    public static void setUnitList(HashMap<Integer, Unit> list) {
        unitList = list;

    }

    /**
     * Empty the global uni list
     */
    public static void clearUnitList() {
        if (unitList != null) {
            unitList.clear();
        }
    }

    /**
     * Get the {@link Unit} from its name
     *
     * @param unitName The unit tname
     * @return {@link  Unit} with this name
     */
    public static Unit getUnitFromName(String unitName) {
        for (Unit u : unitList.values()) {
            if (u.getUnit().equals(unitName)) {
                return u;
            }
        }
        return null;
    }

    /**
     * get the {@link Unit} from its id
     *
     * @param unitID The units id
     * @return {@link  Unit} with this id
     */
    public static Unit getUnitFormID(int unitID) {
        return unitList.get(unitID);

    }
}
