/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DBOperations;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * A identifier for MoniSoft databases. Used to find out which of many tables on a database server are MoniSoft databases
 * which of them and are accessible for a specific user.
 * 
 * @author togro
 */
public class DBIdentifier {

    private String server;
    private String user;
    private String password;

    /**
     * Creates a DBIdentifier which will use the given server and user credentials to look for databases
     * @param server
     * @param user
     * @param passwd 
     */
    public DBIdentifier(String server, String user, String passwd) {
        this.server = server;
        this.user = user;
        this.password = passwd;
    }

    /**
     * Returns a list of all MoniSoft databases on the given server the user has
     * the right to connect to
     * @return List of available projects /databases
     */
    public ArrayList<String> getAccessibleTables() {
        ArrayList<String> dbList = new ArrayList<String>();
        Connection conn = null;
        try {
            String db;
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection("jdbc:mysql://" + server + "/", user, password);
            password = "";
            Statement innerST = conn.createStatement();
            ResultSet innerRS;
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("show databases"); // only shows databases the user can "see"
            while (rs.next()) {                                 // .. loop the list
                db = rs.getString(1);
                innerST = conn.createStatement();
                innerRS = innerST.executeQuery("show tables from " + db);
                while (innerRS.next()) {
                    if (innerRS.getString(1).equals(MoniSoftConstants.HISTORY_TABLE)) { // check if there is a table T_History
                        dbList.add(db);
                    }
                }
            }
            st.close();
            innerST.close();
            conn.close();
        } catch (Exception e) {
            dbList = null;
            Messages.showException(e);
        }
        return dbList;
    }
}
