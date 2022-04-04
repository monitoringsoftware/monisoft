package de.jmonitoring.DBOperations;

import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.NoOperationGUI;
import de.jmonitoring.utils.FileCompressor;
import de.jmonitoring.utils.StoppableThread;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;

/**
 * This class featires methods for database backup
 *
 * @author togro
 */
public class DatabaseBackup {

    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();
    private boolean compress;
    private DBConnector conn;
    private MoniSoftProgressBar progressBar = null;
    private final MainApplication gui;

    /**
     * Constructor taking in the calling GUI (or CLI)
     *
     * @param gui
     */
    public DatabaseBackup(MainApplication gui) {
        super();
        this.gui = gui;
    }

    /**
     * Invokes the backup for the given connection
     *
     * @param connector The {@link DBConnector}
     * @param compress <code>true</code> if the backup file should be compressed
     */
    public void runBackup(DBConnector connector, boolean compress) {
        boolean fromGUI = true;
        if (gui instanceof NoOperationGUI) { // we come form the command line
            fromGUI = false;
        }
        if (!fromGUI || confirmBackup() == JOptionPane.YES_OPTION) {
            if (!testForMysqlDump()) {
                if (fromGUI) {
                    JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("NO_MYSQL_DUMP") + "\n");
                }
                return;
            }
            this.compress = compress;
            if (connector != null) {
                conn = connector;
            } else {
                conn = MoniSoft.getInstance().getDBConnector();
            }

            if (fromGUI) {
                progressBar = this.gui.getProgressBarpanel().addProgressBar(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DB_BACKUP"));
                progressBar.addProgressCancelButtonActionListener(action);
                progressBar.setIndeterminate(true);
                progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("CREATING_BACKUP"));
            }

            doWork.start();
            if (!fromGUI) { // if we come from the gui we have to wait for the process to finish.otherwise the backup would just end when the application finishes
                try {
                    doWork.join();
                } catch (InterruptedException ex) {
                    Messages.showException(ex);
                }
            }
        }
    }
    /**
     * Listens to the cancel button and invokes cancellation
     */
    ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("CANCELLED") + "\n", true);
            if (progressBar != null) {
                progressBar.remove();
            }
            doWork.running = false;
            doWork.interrupt();
        }
    };

    /**
     * Displays a confirmation dialog
     *
     * @return The result of the dialog input
     */
    private int confirmBackup() {
        return JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("LONG_RUNNING_DB") + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("CONTINUE"), java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("STRAT_BACHUP_Q"), JOptionPane.OK_CANCEL_OPTION);
    }

    public boolean testForMysqlDump() {
        boolean installed = false;
        Runtime rt = Runtime.getRuntime();
        try {
            String command = MoniSoft.getInstance().getApplicationProperties().getProperty("MysqldumpLocation");
            ProcessBuilder builder = new ProcessBuilder(new String[]{command, "--version",});
            Process proc = builder.start();

            int processComplete = proc.waitFor();
            if (processComplete == 0) {
                installed = true;
            } else {
                installed = false;
            }
        } catch (Exception ex) {
            installed = false;
        }

        return installed;
    }
    /**
     * The thread that does the backup work
     */
    StoppableThread doWork = new StoppableThread(new Runnable() {
        @Override
        public void run() {
            ((StoppableThread) Thread.currentThread()).running = true;
            String line = null;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
            String filename = format.format(new Date()) + "_" + conn.getDBName() + ".sql";
//            String dumpCommand = "mysqldump -h " + conn.getServerIP() + " -P " + conn.getServerPort() + " -u " + conn.getUserName() + " --password=" + conn.getPwd() + " " + conn.getDBName() + " -r " + MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + filename;

            ProcessBuilder builder = new ProcessBuilder(
                    new String[]{
                        "mysqldump",
                        "-v",
                        "-h" + conn.getServerIP(),
                        "-P" + conn.getServerPort(),
                        "-u" + conn.getUserName(),
                        "--password=" + conn.getPwd(),
                        conn.getDBName()
                    });

            builder.redirectErrorStream(true);
//            Runtime rt = Runtime.getRuntime();
            Process proc = null;
            try {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("RUNNING") + " \"" + MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + filename + "\" " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("WAIT") + "\n", true);
//                proc = rt.exec(dumpCommand);
                proc = builder.start();
                InputStream processStdout = proc.getInputStream();
                OutputStream processStdin = proc.getOutputStream();

                BufferedInputStream reader = new BufferedInputStream(processStdout);
                FileOutputStream fileOut = new FileOutputStream(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + filename);

                byte[] buffer = new byte[1024];
                int length = 0;
                while ((length = reader.read(buffer)) != -1) {
                    fileOut.write(buffer, 0, length);
                }

                processStdout.close();
                processStdin.close();
                fileOut.close();

                int processComplete = proc.waitFor();
                if (processComplete == 0) {
                    if (compress) {
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("SUCCESS"), true);
                        if (progressBar != null) {
                            progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("COMPRESSING"));
                        }
                        new FileCompressor().compressFile(new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + filename), true);
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("COMPLETED") + "\n", true);
                        logger.info("Backup completed");
                    } else {
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("BACKUPSUCCESS") + "\n", true);
                    }
                } else {
                    InputStream error = proc.getErrorStream();
                    InputStreamReader errorReader = new InputStreamReader(error, "UTF-8");
                    BufferedReader ebr = new BufferedReader(errorReader);
                    while ((line = ebr.readLine()) != null) {
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("ERROR") + "\n" + line + "\n", true);
                        logger.error(line);
                    }
                }
            } catch (InterruptedException e) {
                ((StoppableThread) Thread.currentThread()).running = false;
                proc.destroy();
                Messages.showException(e);
            } catch (IOException e) {
                Messages.showException(e);
            } catch (Exception e) {
                Messages.showException(e);
            } finally {
                if (progressBar != null) {
                    progressBar.remove();
                }
            }
        }
    });
}
