package de.jmonitoring.base;

import de.jmonitoring.ApplicationProperties.ApplicationPropertyReader;
import de.jmonitoring.utils.commandLine.MoniSoftCommandline;
import de.jmonitoring.utils.swing.EDT;
import java.awt.Font;
import java.awt.SplashScreen;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * Main MoniSoft class<p> Defines UI settings and initialites the startup
 * process<br> <p> Calls the CLI or GUI depending on any given parameters
 *
 * @author togro
 */
public class Main {

    private static boolean commandLineOnly(final String[] args) {
        return args.length > 0;
    }

    public static void main(final String args[]) throws Exception {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                Properties applicationProperties = null;
                try {                    
                    // MONISOFT-24
                    System.out.println( "Default-TimeZone des Systems: " + TimeZone.getDefault() );
                    
                    applicationProperties = new ApplicationPropertyReader().getApplicationProperties();
                } catch (Exception e) {
                    Messages.showException(e);
                }
                System.setProperty("file.encoding", "UTF8");
                if (applicationProperties != null) {

                    // get locale from app-properties
                    String localeString = applicationProperties.getProperty("Locale");
                    if (!localeString.isEmpty()) {
                        String[] localeParts = localeString.split("_");
                        try {
                            String language = localeParts[0];
                            String country = localeParts[1];
                            Locale.setDefault(new Locale(language, country));
                        } catch (IndexOutOfBoundsException e) {
                            System.out.println("Invalid Locale");
                        }
                    }
                } else {
                    Locale.setDefault(new Locale("de", "DE")); // dafault language
                }

                MonisoftSplash mSplash;
                if (commandLineOnly(args)) {
                    mSplash = new NoSplash();
                } else {
                    mSplash = new SwingBasedMonisoftSplash(SplashScreen.getSplashScreen());
                    mSplash.initialize();
                }

                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
                    UIManager.put("ScrollBar.width", new Integer(14));
                    UIManager.put("Button.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("List.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("ComboBox.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("CheckBox.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("Label.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("ColorChooser.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("InternalFrame.titleFont", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("TabbedPane.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("TitledBorder.font", new FontUIResource("Dialog", Font.PLAIN, 10));
                    UIManager.put("MenuItem.font", new FontUIResource("Dialog", Font.PLAIN, 12));
                    UIManager.put("Menu.font", new FontUIResource("Dialog", Font.PLAIN, 12));
                    UIManager.put("Slider.thumbWidth", 1);
                } catch (Exception e) {
                    Messages.showException(e);
                }

                mSplash.showMessage("Creating instance ...", 2);
                final MoniSoft instance = MoniSoft.createMonisoft(!commandLineOnly(args));
                mSplash.showMessage("Loading application settings ...", 3);
                try {
                    instance.setApplicationProperties(applicationProperties);   // laden der Anwendungseinstellungen
                } catch (Exception e) {
                    Messages.showException(e);
                }
                instance.initApplication();
                if (commandLineOnly(args)) {
                    MoniSoftCommandline cl = new MoniSoftCommandline(args);
                    instance.setCommandLine(cl);
                    cl.parseCommandLine();
                    System.exit(0);
                }

                // from here on: start MoniSoft as GUI
                instance.initializeGUI(mSplash);
            }
        });
    }
}
