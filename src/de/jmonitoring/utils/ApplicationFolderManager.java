/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import java.io.File;
import javax.swing.JOptionPane;

/**
 *
 * @author togro
 */
public class ApplicationFolderManager {

    public static boolean baseFolderExists() {
        boolean exists = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder")).exists();

        if (!exists) {
            createBaseFolder();
        }

        return exists;
    }

    public static boolean folderStructureExists() {
        boolean b1 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER).exists();
        boolean b2 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.SER_FOLDER).exists();
        boolean b3 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER).exists();
        boolean b4 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.DATA_FOLDER).exists();
        return b1 && b2 && b3 && b4;
    }

    public static boolean projectFolderExists() {
        return new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName()).exists();
    }

    /**
     * Erzeugt den Basisordner der in den Anwendungseinstellungen hinterlegt ist
     * @return
     */
    public static boolean createBaseFolder() {
        boolean success = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder")).mkdir();
        if (!success) {
             JOptionPane.showMessageDialog(null, "Der Basisordner kann an dieser Stelle nicht erzeugt werden.\n\nBitte Überprüfen Sie die Berechtigungen.");
        }
        return success;
    }

    /**
     * Erzeugt den Ordner, der alle Daten des jeweiligen Projektes enthält
     * @return
     */
    public static boolean createProjectFolder() {
        boolean success = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName()).mkdir();
        if (!success) {
             JOptionPane.showMessageDialog(null, "Der Projektordner kann an dieser Stelle nicht erzeugt werden.\n\nBitte Überprüfen Sie die Berechtigungen.");
        }
        return success;
    }

    /**
     *  Erzeugt die Ordnerstruktur unterhalb des Projekte-Ordners 
     * @return
     */
    public static boolean createFolderStructure() {

        boolean b1 = true;
        boolean b2 = true;
        boolean b3 = true;
        boolean b4 = true;
        if (!new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER).exists()) {
            b1 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER).mkdir();
        }
        if (!new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.SER_FOLDER).exists()) {
            b2 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.SER_FOLDER).mkdir();
        }
        if (!new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER).exists()) {
            b3 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER).mkdir();
        }
        if (!new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.DATA_FOLDER).exists()) {
            b4 = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.DATA_FOLDER).mkdir();
        }

        if (!(b1 && b2 && b3 && b4)) {
             JOptionPane.showMessageDialog(null, "Die Unterordner können an dieser Stelle nicht erzeugt werden.\n\nBitte Überprüfen Sie die Berechtigungen.");
        }
        return b1 && b2 && b3 && b4;
    }
}
