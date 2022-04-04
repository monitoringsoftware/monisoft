package de.jmonitoring.base;

import de.jmonitoring.DatabaseGeneration.NewProjectDialog;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.DBMaintenance;
import de.jmonitoring.DBOperations.DatabaseBackup;

/**
 * This class offers methods for database maintenance
 * @author togro
 */
public class MaintainanceTasks {

    private final MainApplication gui;

    /**
     * A Constuctor taking in the calling GUi
     * @param gui 
     */
    public MaintainanceTasks(MainApplication gui) {
        super();
        this.gui = gui;
    }

    /**
     * Check the consistency of the database
     */
    public static class checkDBConsistency implements Runnable {

        @Override
        public void run() {
            new DBMaintenance().consistencyCheck();
        }
    }

    /**
     * A thread that inkoves the project creation and show the {@link NewProjectDialog}
     */
    public static class NewProjectThread implements Runnable {

        private final DBConnector database;
        private final MainApplication gui;

        public NewProjectThread(DBConnector database, MainApplication gui) {
            this.database = database;
            this.gui = gui;
        }

        @Override
        public void run() {
            NewProjectDialog np = new NewProjectDialog(gui, true);
            np.setConnector(this.database);
            np.showChoiceDialog();
        }
    }

    /**
     * Runs the backup of the database
     */
    public void doBackup() {
        DatabaseBackup backup = new DatabaseBackup(this.gui);
        backup.runBackup(null, true);
    }
}
