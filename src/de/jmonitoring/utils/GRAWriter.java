/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class wrties and read chart decribers to the database
 *
 * @author togro
 */
public class GRAWriter {

    public void writeGRAFiletoDB(String name, FileInputStream fis) {
        Connection myConn;;
        PreparedStatement ps;

        try {
            myConn = DBConnector.openConnection();
            myConn.setAutoCommit(false);
            ps = myConn.prepareStatement("insert into T_Graphics (Name,Description) values (?,?)");
            ps.setString(1, name);
            ps.setBinaryStream(2, fis);
            ps.executeUpdate();
            myConn.commit();
            ps.close();
            fis.close();
            myConn.close();
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("PERMISSION_DENIED"));
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
        }
    }

    public void writeChartDescriptiontoDB(String name, String content) {
        Connection myConn;
        PreparedStatement ps;

        try {
            myConn = DBConnector.openConnection();
            myConn.setAutoCommit(false);
            ps = myConn.prepareStatement("insert into T_Graphics (Name,Description) values (?,?)");
            ps.setString(1, name);
            ps.setString(2, content);
            ps.executeUpdate();
            myConn.commit();
            ps.close();
            myConn.close();
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("PERMISSION_DENIED"));
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
        }
    }

    public InputStream readGRAfromDB(String name) {
        InputStream stream = null;
        Connection myConn;
        PreparedStatement ps;
        ResultSet rs;

        try {
            myConn = DBConnector.openConnection();
            myConn.setAutoCommit(false);
            ps = myConn.prepareStatement("select Description from T_Graphics where Name=?");
            ps.setString(1, name);
            rs = ps.executeQuery();
            while (rs.next()) {
                stream = rs.getBinaryStream(1);
            }

            ps.close();
            myConn.close();
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
        }

        return stream;
    }
}
