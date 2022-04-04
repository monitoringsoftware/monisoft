package de.jmonitoring.ApplicationProperties;

import de.jmonitoring.base.MoniSoftConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class reads application properties from the configuration file and loads
 * them into a {@link Properties} object.
 *
 * @author togro
 */
public class ApplicationPropertyReader {

    /**
     * Reader for the Application properties that define the behaviour of the
     * application. Reads properties from file or uses the default settings.
     */
    public ApplicationPropertyReader() {
    }

    /**
     * Tries to load stored application properties from the userPrefs file. If
     * not successfull the default props will be used.
     *
     * @return The (default) application properties, never <code>NULL</code>
     * @throws IOException
     */
    public Properties getApplicationProperties() throws IOException {
        String userPrefsFile = System.getProperty("user.home") + System.getProperty("file.separator") + MoniSoftConstants.userPrefs;  // set default folder for projects to users home folder
        FileInputStream in;

        // create and load default properties
        Properties defaultProps = new Properties();
        defaultProps.setProperty("DefaultSaveFolder", System.getProperty("user.home") + System.getProperty("file.separator"));
        defaultProps.setProperty("ExportFieldSeparator", Integer.toString(0));
        defaultProps.setProperty("ExportDecimalSeparator", Integer.toString(0));
        defaultProps.setProperty("ExportNumberFormat", "0.###");
        defaultProps.setProperty("DefaultUser", "");
        defaultProps.setProperty("DefaultDB", "");
        defaultProps.setProperty("DefaultServer", "localhost");
        defaultProps.setProperty("DefaultServerPort", "3306");
        defaultProps.setProperty("CoverTolerance", "90");
        defaultProps.setProperty("UseCoverTolerance", "0");
        defaultProps.setProperty("UseAntiAliasing", "0");
        defaultProps.setProperty("OutlineDragMode", "0");
        defaultProps.setProperty("DrawChartStamp", "1");
        defaultProps.setProperty("UseSensorIDForDisplay", "0");
        defaultProps.setProperty("UseEdgeTolerance", "0");
        defaultProps.setProperty("AutomaticCounterChange", "0");
        defaultProps.setProperty("CalcPartlyConsumptions", "0");
        defaultProps.setProperty("EdgeTolerance", "10");
        defaultProps.setProperty("UseLeaveEvents", "1");
        defaultProps.setProperty("LeaveEvents", "0");
        defaultProps.setProperty("FileEncoding", MoniSoftConstants.ISO8859);
        defaultProps.setProperty("UseIgnoreValue", "0");
        defaultProps.setProperty("IgnoreValue", "-99");
        defaultProps.setProperty("IntervalList", "10");
        defaultProps.setProperty("UseSSHTunnel", "0");
        defaultProps.setProperty("SSHTunnelIP", "");
        defaultProps.setProperty("SSHTunnelPort", "3306");
        defaultProps.setProperty("SSHServerIP", "");
        defaultProps.setProperty("SSHServerPort", "22");
        defaultProps.setProperty("SSHLocalPort", "3306");
        defaultProps.setProperty("SSHUser", "");
        defaultProps.setProperty("LAST_PORT", "");
        defaultProps.setProperty("LAST_SERVER", "");
        defaultProps.setProperty("LAST_DB", "");
        defaultProps.setProperty("LAST_USER", "");
        defaultProps.setProperty("LastCSVImport", "");
        defaultProps.setProperty("LastMONImport", "");
        defaultProps.setProperty("IntervalWarningTolerance", "5");
        defaultProps.setProperty("AbberationLimits", "20,30,50,-20,-30,-50");
        defaultProps.setProperty("Locale", "de_DE");
        defaultProps.setProperty("MysqldumpLocation", "");
        defaultProps.setProperty("AddBuildingName", "1");

        // preset to those default values
        Properties applicationProps = new Properties(defaultProps);

        // now load properties from file if possible
        File file = new File(userPrefsFile);
        if (file.exists()) {
            in = new FileInputStream(file);
            applicationProps.load(in);
            in.close();
        }
        return applicationProps;
    }
}
