package de.jmonitoring.References;

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
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author togro
 */
public class ReferenceInformation {

    private static TreeSet<ReferenceDescription> referenceList = null;

    /**
     * Liefert den Namen der Bezugsgrösse aus ihrer ID
     *
     * @param id ID der Bezugsgrösse
     * @return Name der Bezugsgrösse
     */
    public static String getReferenceFromID(int id) {
        String unit = "n/a";
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.REFERENCENAME_NAME + " from " + MoniSoftConstants.REFERENCES_TABLE + " where " + MoniSoftConstants.REFERENCENAME_ID + "=" + id);
            rs.next();
            unit = rs.getString(MoniSoftConstants.UNIT);
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
        return unit;
    }

    /**
     * Liefert die ID der Bezugsgrösse aus ihrem Namen. Liest direkt aus der DB.
     *
     * @param unit Name der Bezugsgrösse
     * @return ID der Bezugsgrösse,
     * <code>null</code> wenn nicht vorhanden
     */
    public static Integer getIDFromReferenceName(String reference) {
        Integer id = null;
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.REFERENCENAME_ID + " from " + MoniSoftConstants.REFERENCES_TABLE + " where " + MoniSoftConstants.REFERENCENAME_NAME + "= BINARY '" + reference + "'");
            if (rs.next()) {
                id = rs.getInt(MoniSoftConstants.UNIT_ID);
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return id;
    }

    public static void refreshReferences(ArrayList<ReferenceDescription> references) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> alreadyHandledReferenceNames = new ArrayList<String>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            for (ReferenceDescription reference : references) {
                // Einfügen oder aktualisieren
                stmt.executeUpdate("insert into " + MoniSoftConstants.REFERENCES_TABLE + " set " + MoniSoftConstants.REFERENCENAME_NAME + "='" + reference.getName() + "'," + MoniSoftConstants.REFERENCENAME_DESCRIPTION + "='" + reference.getDescription() + "'," + MoniSoftConstants.REFERENCENAME_UNIT_ID + "=" + reference.getUnitID() + " ON DUPLICATE KEY UPDATE " + MoniSoftConstants.REFERENCENAME_DESCRIPTION + "='" + reference.getDescription() + "'," + MoniSoftConstants.REFERENCENAME_UNIT_ID + "=" + reference.getUnitID(), Statement.RETURN_GENERATED_KEYS);
                alreadyHandledReferenceNames.add(reference.getName());
            }
            for (Iterator<ReferenceDescription> it = referenceList.iterator(); it.hasNext();) {
                ReferenceDescription reference = it.next();
                if (!references.contains(reference) && !alreadyHandledReferenceNames.contains(reference.getName())) { // wenn diese (alte) Bezugsgröße nicht mehr vorhanden ist löschen
                    deleteReference(reference);
                }
            }
            alreadyHandledReferenceNames.clear();

            // globale Liste aktualisieren
            setReferenceList(new ListFiller().readReferenceList());

        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    public static void deleteReference(ReferenceDescription reference) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.REFERENCES_TABLE + " where " + MoniSoftConstants.REFERENCENAME_NAME + "='" + reference.getName() + "'");
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        // globale Liste aktualisieren
        setReferenceList(new ListFiller().readReferenceList());
    }

    public static void deleteAllReferences() {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        Connection myConn = null;
        Statement stmt = null;

        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            stmt.executeUpdate("delete from " + MoniSoftConstants.REFERENCES_TABLE);
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }

        // globale Liste aktualisieren
        setReferenceList(new ListFiller().readReferenceList());
    }

    /**
     * Liefert eine Liste aller Bezugsgössen
     *
     * @return
     * <code>ArrayList<ReferenceDescription></code> mit den Bezugsgrössen
     */
    public static TreeSet<ReferenceDescription> getReferenceList() {
        return referenceList;
    }

    /*
     * Ermittelt die Einheiten-ID zum übergebenen Bezugsgrößennamen
     */
    public static Integer getUnitIDForReference(String refName) {
        Integer id = null;

        for (ReferenceDescription def : referenceList) {
            if (def.getName().equals(refName)) {
                id = def.getUnitID();
            }
        }

        return id;
    }

    /**
     * Leert die liste
     */
    public static void clearReferenceList() {
        if (referenceList != null) {
            referenceList.clear();
        }
    }

    /**
     * Setzt die Liste der Bezugsgrössen
     *
     * @param list
     * <Code>ArrayList<ReferenceDescription></code> mit den Bezugsgrössen
     */
    public static void setReferenceList(TreeSet<ReferenceDescription> list) {
        referenceList = list;
    }
}
