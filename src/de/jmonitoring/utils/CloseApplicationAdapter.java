/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;

/**
 *
 * @author togro
 */
public class CloseApplicationAdapter extends WindowAdapter {

    private DBConnector connector;
	private final Frame parent;

    public CloseApplicationAdapter(Frame parent) {
        super();
		this.parent = parent;
    }

    public void setConnector(DBConnector c) {
        connector = c;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        if (connector != null && connector.isConnected()) {
            int ret = JOptionPane.showConfirmDialog(parent, ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("CloseApplicationAdapter.OPENCONNECTION") + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("CloseApplicationAdapter.DISCONNECT"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/Bundle").getString("CloseApplicationAdapter.CLOSEAPPLICATION"), JOptionPane.YES_NO_OPTION);
            switch (ret) {
                case JOptionPane.YES_OPTION:
                    try {
                        connector.disconnectFromDB();
                    } catch (Exception ex) {
                        Messages.showException(ex);
                    }
                    System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
}
