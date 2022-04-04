package de.jmonitoring.base;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import de.jmonitoring.standardPlots.common.ChartLoader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.utils.ApplicationFolderManager;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import de.jmonitoring.utils.GRAWriter;
import de.jmonitoring.utils.commandLine.MoniSoftCommandline;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.networking.SSHTunnel;
import java.io.FileOutputStream;

/**
 * This is core application class<P>It creates its own instance as GUI or CLI,
 * dependant of the given boolean flag.<br>Here the MainGUI gets initialized as
 * well as loggers, application and project properties.<p> Some global flags are
 * also set from here an can be retrievied from the relevant methods.
 *
 *
 * @author togro
 */
public class MoniSoft {

    private static final long serialVersionUID = 1L;
    public final float recommendedLastDBVersion = 2.9f;
    public final float recommendedDBVersion = 2.11f;
    public final boolean ISTRIAL = false;
    private DateInterval dataDateRange = new DateInterval();
    private Properties applicationProps;
    private Properties projectProps;
    // TODO: Move this field to the MainGUI (or another GUI related class)
    private boolean GUIActive = false;
    private DBConnector dbConnector;
    private static MoniSoft instance = null;
    private static SSHTunnel ssht;
    private MoniSoftCommandline commandLine;
    private boolean showGUI;
    private boolean skipFavorites;
    private MainGUI gui;
    private static ch.qos.logback.classic.Logger logger = null;

    /**
     * Constructor of the MoniSoft application<p>
     *
     * @param gui Truie if thsi should be a GUI session, false for CLI
     */
    private MoniSoft(boolean gui) {
        showGUI = gui;
    }

    /**
     * This method creates a new MoniSof instance.<p> Here the decision is taken
     * if MoniSoft should create a GUI or will only be used by the CLI by
     * setting the global
     * <code>showGUI</code> variable.
     *
     * @param withGui
     * @return
     */
    public static MoniSoft createMonisoft(boolean withGui) {
        if (null == instance) {
            instance = new MoniSoft(withGui);
        }
        return instance;
    }

    /**
     * Initializes the GUI if MoniSoft was not started as CLI<p> Also
     * initializes the global logger and the {@link MessageDisplayer} and the
     * connection status.
     */
    public void initApplication() {
        initializeLogger("monisoft.log");
        dbConnector = new DBConnector();
        if (showGUI) {
            this.gui = new MainGUI(this);
            dbConnector.connectionStatus = this.gui;
            Messages.messageDisplayer = this.gui;
            return;
        }
        // if we are here this is a CLI session
        dbConnector.connectionStatus = new NonGUIConnectionStatus();
        Messages.messageDisplayer = new ConsoleMessageDisplayer();
    }

    /**
     * Invokes the startup method of the GUI using the given spalsh screen
     *
     * @param mSplash
     */
    public void initializeGUI(MonisoftSplash mSplash) {
        this.gui.initializeGUI(mSplash);
    }

    /**
     * Retrieves a falg indicationg if this is a demo version
     *
     * @return <code>true</code> if this is a DEMO version
     */
    public boolean isTrial() {
        return ISTRIAL;
    }

    /**
     * Sets the system wiede application properties to the given properties
     *
     * @param props The properties
     */
    public void setApplicationProperties(Properties props) {
        applicationProps = props;
        // check if the base folder exists, if not use the users home folder (as per default)
        File f = new File(applicationProps.getProperty("DefaultSaveFolder"));
        if (!f.exists()) {
            if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SETTINGS_FOLDER_NOT_EXIST") + "\n\n" + f.toString() + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOT_EXIST") + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CRATE_BASE_FOLDER_Q"), "Ordner anlegen?", JOptionPane.YES_NO_OPTION)) {
                ApplicationFolderManager.createBaseFolder();
            } else {
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("USING_HOME_AS_BASE_FOLDER"), "Info", JOptionPane.INFORMATION_MESSAGE);
                applicationProps.setProperty("DefaultSaveFolder", System.getProperty("user.home") + System.getProperty("file.separator"));
            }
        }
    }

    /**
     * Sets the global SSH tunnel variable
     *
     * @param sshTunnel The tunnel to use
     */
    public static void setSSHTunnel(SSHTunnel sshTunnel) {
        ssht = sshTunnel;
    }

    // TODO: Try to make this non-public
    /**
     * Tells the {@link DBConnector} to disconnet from the database and sets the
     * GUI to not connected mode<p> Also closes the ssh connection if applicable
     */
    public void disconnectFromDB() {
        dbConnector.disconnectFromDB();
        if (ssht != null && ssht.getSession() != null) {
            ssht.getSession().disconnect();
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SSH_CLOSED") + "\n", true);
        }
        this.gui.setConnected(false);
    }

    /**
     * Should favorites be loaded at startup?
     *
     * @return True is favorites should be skipped
     */
    public boolean isSkipFavorites() {
        return skipFavorites;
    }

    /**
     * Sets the flag indikatcing is favorites should be skipped at startup
     *
     * @param skipFavorites
     */
    public void setSkipFavorites(boolean skipFavorites) {
        this.skipFavorites = skipFavorites;
    }

    /**
     * Sets the global {@link MoniSoftCommandline} to the given CLI
     *
     * @param cl The {@link MoniSoftCommandline} to be used
     */
    public void setCommandLine(MoniSoftCommandline cl) {
        commandLine = cl;
    }

    /**
     * Returns the CLI
     *
     * @return The currecnt CLI object
     */
    public MoniSoftCommandline getCommandLine() {
        return commandLine;
    }

    /**
     * Returns the system wide logger
     *
     * @return The logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Ist the application in GUI or CLI mode?
     *
     * @return True if this is a GUI session
     */
    public boolean isGUI() {
        return showGUI;
    }

    /**
     * Invokes the creation of a new {@link ChartLoader} with the given
     * {@link ChartDescriber} from the database and a time period
     *
     * @param gra The name of the {@link ChartDescriber}
     * @param interval The time period to be used
     */
    public void loadStoredChartFromDatabase(String gra, DateInterval interval) {
        this.gui.setRecycleDescriber(false);
        String graSel = gra;

        // es wurde nichts gewählt
        if (graSel == null || graSel.isEmpty() || graSel.equals(MoniSoftConstants.NO_CHARTDESCRIPTION_AVAILABLE) || graSel.equals(MoniSoftConstants.NO_CHARTDESCRIPTION_SELECTED)) {
            return;
        }

        InputStreamReader isr = null;
        try {
            isr = new InputStreamReader(new GRAWriter().readGRAfromDB(graSel), "UTF8");
            new ChartLoader(this.gui, this.gui.getDateRange()).loadStoredChart(isr, interval);
        } catch (UnsupportedEncodingException e) {
            JOptionPane.showMessageDialog(this.gui, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("THE_FILE") + "\n\n" + applicationProps.getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER + System.getProperty("file.separator") + graSel + ".gra\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANNOT_BE_FOUND") + "\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PLEASE_CHECK_SETTINGS"));
            Messages.showException(e);
        } catch (Exception e) {
            Messages.showException(e);
        } finally {
            close(isr);
        }
    }

    /**
     * Invokes the creation of a new {@link ChartLoader} with the given
     * {@link ChartDescriber} from a GRA-XML file and a time period
     *
     * @param gra The name of the {@link ChartDescriber}
     * @param interval The time period to be used
     */
    public void loadStoredChartFromFile(String gra, DateInterval interval) {
        this.gui.setRecycleDescriber(false);
        String graSel;
        graSel = gra;
        // es wurde nichts gewählt
        if (graSel == null || graSel.isEmpty() || graSel.equals(MoniSoftConstants.NO_CHARTDESCRIPTION_AVAILABLE) || graSel.equals(MoniSoftConstants.NO_CHARTDESCRIPTION_SELECTED)) {
            return;
        }

        FileInputStream fis = null;
        InputStreamReader isr = null;

        try {
            if (isGUI()) { // Wenn von der GUI aufgerufen muss der Dateiname erst noch gebaut werden
                fis = new FileInputStream(applicationProps.getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER + System.getProperty("file.separator") + graSel + ".gra");
            } else {
                fis = new FileInputStream(graSel);
            }
            isr = new InputStreamReader(fis, "UTF8");
            new ChartLoader(this.gui, this.gui.getDateRange()).loadStoredChart(isr, interval);
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(this.gui, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("THE_FILE") + "\n\n" + applicationProps.getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER + System.getProperty("file.separator") + graSel + ".gra\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANNOT_BE_FOUND") + "\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PLEASE_CHECK_SETTINGS"));

            Messages.showException(e);
        } catch (Exception e) {
            Messages.showException(e);
        } finally {
            close(isr);
            close(fis);
        }
    }

    /*
     * Invokes the creation of favorites
     */
    public void loadFavorites() {
        Thread thread = new MoniSoft.loadFavoritesThread();
        thread.start();
        try {
            thread.join(20000);
        } catch (InterruptedException ex) {
            Messages.showException(ex);
        }
    }

    public DBConnector getDBConnector() {
        return dbConnector;
    }

    public void setDBConnector(DBConnector c) {
        dbConnector = c;
    }

    public Properties getApplicationProperties() {
        return applicationProps;
    }

    public Properties getProjectProperties() {
        return projectProps;
    }

    public void setProjectProperties(Properties props) {
        projectProps = props;
    }

    public static MoniSoft getInstance() {
        return instance;
    }

    public String getVersion() {
        return MoniSoftConstants.getVersion();
    }

    public boolean isGUIActive() {
        return GUIActive;
    }

    public void setGUIActive(boolean active) {
        GUIActive = active;
    }

    public DateInterval getDataDateRange() {
        return dataDateRange;
    }

    /**
     * Loads the project settings or, if not existant, creats them for the first
     * time
     */
    public void loadProjectProperties() {
        String propsFile = applicationProps.getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PROJECT_PROPS_FILE;

        // create and load default properties
        Properties defaultProps = new Properties();
        defaultProps.setProperty("ShowFavoriteCharts", "0");
        defaultProps.setProperty("ShowCompareTable", "0");
        defaultProps.setProperty("FavoriteCharts", "");
        defaultProps.setProperty("FavoriteCollection", "");
        defaultProps.setProperty("LookBackDays", "7");

        // Anwendungseinstellungen mit default-Werten vorbelegen
        projectProps = new Properties(defaultProps);

        // now load properties from last invocation
        FileInputStream in = null;
        try {
            in = new FileInputStream(propsFile);
            projectProps.load(in);
        } catch (IOException e) {
            logger.error("Could not read property file: " + propsFile + ". Creating it");
            Messages.showException(e);
            createProjectFile(propsFile, defaultProps);
        } finally {
            close(in);
        }
    }

    /**
     * Creates a new project property file
     *
     * @param file Name of the file
     * @param props The properties to be stored in the file
     */
    private void createProjectFile(String file, Properties props) {
        FileOutputStream out;
        try {
            out = new FileOutputStream(file);
            props.store(out, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("--- PROJEKTSPEZIFISCHE EINSTELLUNGEN FÜR {0} ---"), new Object[]{MoniSoft.getInstance().getDBConnector().getDBName()}));
        } catch (FileNotFoundException ex) {
            Messages.showException(ex);
            logger.error("Cannot create project properties file");
        } catch (IOException ex) {
            Messages.showException(ex);
            logger.error("Cannot create project properties file");
        }
    }

    /**
     * A convinience clas for the CLI where calls to GUI connection elements are
     * absorbed
     */
    private final class NonGUIConnectionStatus implements ConnectionStatus {

        @Override
        public void setConnected(boolean isConnected) {
        }

        @Override
        public void enableIdleLED() {
        }

        @Override
        public void enableDisconnectedLED() {
        }

        @Override
        public void enableConnectedLED() {
        }

        @Override
        public void setLabel(String text) {
        }
    }

    /**
     * This thread class invokes the loading of favorites a startup
     */
    private class loadFavoritesThread extends Thread {

        @Override
        public void run() {
            String charts = getProjectProperties().getProperty("FavoriteCharts");
            ArrayList<String> chartList = new ArrayList<String>(Arrays.asList(charts.split(",")));
            Integer lookBack = Integer.valueOf(getProjectProperties().getProperty("LookBackDays"));
            for (String chart : chartList) {
                chart = chart.replace("\"", "");
                loadStoredChartFromFile(chart, DateTimeCalculator.getTimeSpanBeforeToday(lookBack, lookBack - 1, new Date()));
            }

        }
    }

    /**
     * Initialize the logger.<p> The root logger gets its STDOU appender
     * detatched and the new appender is set to the given file.
     *
     * @param logFile The name of the log file
     */
    private void initializeLogger(String logFile) {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Remove the default appender to STDOUT form the root logger
        Logger rootLogger = (ch.qos.logback.classic.Logger) loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAppender("console");

        // Set output format
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %msg%n");
        encoder.start();

        // create new file appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setName("MAINAPPENDER");
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(applicationProps.getProperty("DefaultSaveFolder") + logFile);
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
    }

    public void setDummyLogger() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("");
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Remove the default appender to STDOUT form the root logger
        Logger rootLogger = (ch.qos.logback.classic.Logger) loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.detachAppender("console");
        
        // Set output format
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %msg%n");
        encoder.start();
        
        ConsoleAppender appender = new ConsoleAppender();
        appender.setName("MAINAPPENDER");
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
    }
    
    /**
     * Convinience class to close the given {@link Closeable} Object
     *
     * @param closeable The Object to be closed
     */
    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                Messages.showException(e);
            }
        }
    }
}
