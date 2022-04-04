package de.jmonitoring.DBOperations;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.Cluster.ClusterInformation;
import de.jmonitoring.base.ConnectionStatus;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.ApplicationFolderManager;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.sql.*;
import java.util.Date;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * This class includes the methods used to connect to a datanse and disconnt
 * from it.<p>
 *
 * @author togro
 */
public class DBConnector {

    private String uName;
    private String dbName;
    private String dbServer;
    private String passwd;
    private boolean useGui;
    // XXX: Horrible hack
    public static ConnectionStatus connectionStatus;
    private static ch.qos.logback.classic.Logger logger;

    /**
     * Create a new DBConnector
     */
    public DBConnector() {
        super();
        logger = MoniSoft.getInstance().getLogger();
    }

    /**
     * Connects to the desired database
     *
     * @param server The db server
     * @param database The database name (=project)
     * @param userName username
     * @param passWord password
     * @param connectOnly connect only without reading sensorlist etc.
     * @param useGUI <code>true</code> if this is called from a GUI session
     * @return conn Datenbankverbindung
     */
    public boolean connectToDB(String server, String database, String userName, String passWord, boolean connectOnly, boolean useGUI) {
        passwd = passWord;
        useGui = useGUI;
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.LADE DATENBANKTREIBER"), true);
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Messages.showMessage("OK\n", true);
        } catch (Exception e) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.FEHLER") + "\n", true);
            if (useGui) {
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.DATENBANKTREIBER KONNTE NICHT EINGEBUNDEN WERDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.WARNUNG"), JOptionPane.ERROR_MESSAGE);
            }
            logger.error(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.DATENBANKTREIBER KONNTE NICHT EINGEBUNDEN WERDEN"));
            Messages.showException(e);
        }

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.CONNECTTO") + " '" + database + "' ....", true);
        try {
//            Messages.showMessage("jdbc:mysql://" + server + "/" + database + "?useUnicode=yes&characterEncoding=UTF-8" + "&user=" + userName + "&password=" + passWord +"&useCompression=true&autoReconnect=true&useSSL=true",true);
            setupDriver("jdbc:mysql://" + server + "/" + database + "?useUnicode=yes&characterEncoding=UTF-8" + "&user=" + userName + "&password=" + passWord); // + "&useCompression=true&autoReconnect=true&useSSL=true");
            DriverManager.getConnection("jdbc:apache:commons:dbcp:monisoft");

            Messages.showMessage("OK\n", true);
            connectionStatus.setLabel(ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.CONNECTEDTO") + " '" + database + "' " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.AS") + " " + userName);

            uName = userName;
            dbName = database;
            dbServer = server;
            if (!connectOnly) {
                PrepareApplicationThread t = new PrepareApplicationThread();
                t.setName("PrepareApplicationThread");
                t.start();
                t.join(10000);
            }
            connectionStatus.enableConnectedLED();
        } catch (Exception e) {
            connectionStatus.setConnected(false);
            connectionStatus.enableDisconnectedLED();
            Messages.showMessage(ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.FEHLER") + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
            return false;
        }
        connectionStatus.enableIdleLED();
        return true;
    }

    /**
     * Return the version of the connected database
     *
     * @return
     */
    public float getDBVersion() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs;
        float version = -1f;
        try {
            myConn = DriverManager.getConnection("jdbc:apache:commons:dbcp:monisoft");
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select " + MoniSoftConstants.DB_VERSION + " from " + MoniSoftConstants.CONFIG_TABLE);
            version = rs.next() ? rs.getFloat(1) : 1f;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            try {
                stmt.close();
                myConn.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
            return version;
        }
    }

    /**
     * Disconnects from the database
     *
     * @return <code>true</code> if successful
     */
    public boolean disconnectFromDB() {
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.TRENNE DATENBANKVERBINDUNG") + "... ", true);
        try {
            shutdownDriver();
            BuildingInformation.clearBuildingList();
            UnitInformation.clearUnitList();
            ReferenceInformation.clearReferenceList();
            SensorInformation.clearSensorList();
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.SESSIONENDE"));
//            MoniSoft.getInstance().appendLog(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.SESSIONENDE"));
            connectionStatus.setLabel(ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.KEINE DATENBANKVERBINDUNG"));
            connectionStatus.enableDisconnectedLED();
            uName = "";
            Messages.showMessage("OK\n", true);
        } catch (Exception e) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.FEHLER") + "\n", true);
            logger.error(e.getMessage());
            Messages.showException(e);
            return false;
        }
        return true;
    }

    /**
     * Return the state of the connction
     *
     * @return <code>true</code> if we are connected
     */
    public boolean isConnected() {
        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
            if (driver.getConnectionPool("monisoft").getNumActive() == -1) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Returns the time range in wich the database holds data
     *
     * @return The date interval in a {@link DateInterval}
     */
    public DateInterval getDateRange() {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        DateInterval dateRange = null;
        try {
            myConn = DriverManager.getConnection("jdbc:apache:commons:dbcp:monisoft");
            connectionStatus.enableConnectedLED();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select min(" + MoniSoftConstants.HISTORY_TIMESTAMP + ")*1000,max(" + MoniSoftConstants.HISTORY_TIMESTAMP + ")*1000 from " + MoniSoftConstants.HISTORY_TABLE);
            rs.next();
            dateRange = new DateInterval(new Date(rs.getLong(1)), new Date(rs.getLong(2)));
        } catch (SQLException e) {
            logger.error(e.getMessage());
            Messages.showException(e);
        } finally {
            try {
                rs.close();
                stmt.close();
                myConn.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
        }

        return dateRange;
    }

    /**
     * Setup driver for connection pooling
     *
     * @param connectURI The URI to connect to
     * @throws Exception
     */
    private void setupDriver(String connectURI) throws Exception {
        //
        // First, we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        ObjectPool connectionPool = new GenericObjectPool(null);

        //
        // Next, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, null);

        //
        // Now we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true); // dead store is ok!

        //
        // Finally, we create the PoolingDriver itself...
        //
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");

        //
        // ...and register our pool with it.
        //
        driver.registerPool("monisoft", connectionPool);

        //
        // Now we can just use the connect string "jdbc:apache:commons:dbcp:example"
        // to access our pool of Connections.
        //
    }

    /**
     * Unload the driver
     *
     * @throws Exception
     */
    public void shutdownDriver() throws Exception {
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.closePool("monisoft");
    }

    /**
     * Return the name (=project) of the connected database
     *
     * @return The project name
     */
    public String getDBName() {
        return dbName;
    }

    /**
     * return the currently conected user
     *
     * @return The username
     */
    public String getUserName() {
        return uName;
    }

    /**
     * Return the server we are connected to
     *
     * @return The server
     */
    public String getServer() {
        return dbServer;
    }

    /**
     * Split the server IP from the URI and returns it
     *
     * @return The IP part of the URI
     */
    public String getServerIP() {
        String[] s = dbServer.split(":");
        return s[0];
    }

    /**
     * Split the server port number from the URI and returns it
     *
     * @return The port numer part of the URI
     */
    public int getServerPort() {
        String[] s = dbServer.split(":");
        return Integer.valueOf(s[1]);
    }

    /**
     * Return the password
     *
     * @return The passsword
     */
    public String getPwd() {
        return passwd;
    }

    /**
     * This thread reads information from the database to prepare the
     * application and fill it with the project
     */
    private class PrepareApplicationThread extends StoppableThread {

        @Override
        public void run() {
            running = true;
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.DBVERSION") + " " + getDBVersion() + "\n", true); // DB version ausgeben
            ListFiller filler = new ListFiller();
            UnitInformation.setUnitList(filler.readUnitList());
            ReferenceInformation.setReferenceList(filler.readReferenceList());
            BuildingInformation.setBuildingList(filler.readBuildingList());
            BuildingInformation.setBuildingReferencesMap(filler.readBuildingReferencesMap());
            SensorInformation.setSensorList(filler.readSensorList());
            ClusterInformation.setGlobalClusterMap(filler.readClusterList());
            SensorCollectionHandler.setCollectionSensors(filler.readCollectionSensors());
            connectionStatus.setConnected(true);
            connectionStatus.enableIdleLED();

            // Prüfen, ob ein Projektordner existiert, falls nicht anlegen
            if (useGui && !ApplicationFolderManager.projectFolderExists()) {
                ApplicationFolderManager.createProjectFolder();
            }

            // Prüfen, ob unter dem Projektordner die Unterordner existieren, falls nicht anlegen
            if (useGui && !ApplicationFolderManager.folderStructureExists()) {
                ApplicationFolderManager.createFolderStructure();
            }

            // Loggen
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector.STARTINGSESSION") + " (" + dbName + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("DBConnector:ON") + " " + dbServer + ")");
        }
    }

    /**
     * Close the given connection and result sets
     *
     * @param conn The {@link Connection} to close
     * @param stmt A {@link  Statement} to close
     * @param rs A {@link  ResultSet} to close
     */
    public static void closeConnection(Connection conn, Statement stmt, ResultSet rs) {
        try {
            connectionStatus.enableIdleLED();
        } catch (Exception e) {
            Messages.showException(e);
        }

        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException sqle) {
                Messages.showException(sqle);
            }
        }
        if (null != stmt) {
            try {
                stmt.close();
            } catch (SQLException sqle) {
                Messages.showException(sqle);
            }
        }
        if (null != conn) {
            try {
                conn.close();
            } catch (SQLException sqle) {
                Messages.showException(sqle);
            }
        }
    }

    /**
     * Opens a connection from the pool and returns it
     *
     * @return The connection
     * @throws SQLException
     */
    public static Connection openConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:monisoft");
        connectionStatus.enableConnectedLED();
        return conn;
    }
}
