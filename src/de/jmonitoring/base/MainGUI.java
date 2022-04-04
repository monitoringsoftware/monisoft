package de.jmonitoring.base;

import ch.qos.logback.classic.Level;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.standardPlots.plotTabs.PlotTabManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ToolTipManager;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import com.lowagie.text.pdf.DefaultFontMapper;

import de.jmonitoring.standardPlots.carpetPlot.CarpetChartDescriber;
import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.standardPlots.comparePlot.CompareChartDescriber;
import de.jmonitoring.standardPlots.ogivePlot.OgiveChartDescriber;
import de.jmonitoring.standardPlots.scatterPlot.ScatterChartDescriber;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChartDescriber;
import de.jmonitoring.utils.AnnotationEditor.AnnotationDesigner;
import de.jmonitoring.ApplicationProperties.AppPrefsDialog;
import de.jmonitoring.Components.BuildingEditorDialog;
import de.jmonitoring.Components.BuildingProfile;
import de.jmonitoring.Components.BuildingTablePanel;
import de.jmonitoring.Components.CSVFormatSelector;
import de.jmonitoring.Components.CSVImportDialog;
import de.jmonitoring.Cluster.ClusterEditor;
import de.jmonitoring.Cluster.ClusterMatrixPanel;
import de.jmonitoring.Components.CompareTableFrame;
import de.jmonitoring.Components.CompareValueMappingEditor;
import de.jmonitoring.Components.ConsolePopup;
import de.jmonitoring.Components.ConsumptionFrame;
import de.jmonitoring.DataHandling.CounterChange.CounterChangeDialog;
import de.jmonitoring.Components.ChartDescriberDateRangeDialog;
import de.jmonitoring.Consistency.DataCheckFrame;
import de.jmonitoring.Components.DateRangeProvider;
import de.jmonitoring.Components.DebugConsole;
import de.jmonitoring.Components.DeleteDataDialog;
import de.jmonitoring.Components.DemoInfo;
import de.jmonitoring.Components.ExportDialog;
import de.jmonitoring.DataHandling.FactorChange.FactorChangeDialog;
import de.jmonitoring.Components.FavoriteDialog;
import de.jmonitoring.Components.InfoDialog;
import de.jmonitoring.Components.ManualEntryDialog;
import de.jmonitoring.Components.MonthlyConsumtionPanel;
import de.jmonitoring.Components.MonthlyUsageDialog;
import de.jmonitoring.Components.PasswordDialog;
import de.jmonitoring.Components.ProgressBarPanel;
import de.jmonitoring.References.ReferenceDefinitionDialog;
import de.jmonitoring.References.ReferenceValueTablePanel;
import de.jmonitoring.Components.SectionChart;
import de.jmonitoring.SensorCategoryHandling.SensorCategoryEditor;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionEditor;
import de.jmonitoring.Components.SensorEditorDialog;
import de.jmonitoring.Components.SensorTablePanel;
import de.jmonitoring.Cluster.SortByReferenceFrame;
import de.jmonitoring.Components.StatisticsFrame;
import de.jmonitoring.Components.TreeSelectorDialog;
import de.jmonitoring.utils.UnitCalulation.UnitDefinitionDialog;
import de.jmonitoring.WeatherCalculation.WeatherAssignmentEditor;
import de.jmonitoring.DatabaseGeneration.DBCreator;
import de.jmonitoring.DBOperations.DBMaintenance;
import de.jmonitoring.DatabaseGeneration.DBUpdater;
import de.jmonitoring.DBOperations.ListFiller;
import de.jmonitoring.DBOperations.ReferenceImporter;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.DataHandling.EventReconstructor;
import de.jmonitoring.DataHandling.MonthlyUsageCalculator;
import de.jmonitoring.Graphexport.PDFGenerator;
import de.jmonitoring.Graphexport.SVGGenerator;
import de.jmonitoring.standardPlots.carpetPlot.CarpetChart;
import de.jmonitoring.standardPlots.comparePlot.CompareChart;
import de.jmonitoring.standardPlots.maintenancePlot.MaintenanceChart;
import de.jmonitoring.standardPlots.ogivePlot.OgivePlotChart;
import de.jmonitoring.standardPlots.scatterPlot.ScatterPlotChart;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChart;
import de.jmonitoring.standardPlots.carpetPlot.CarpetPlotTab;
import de.jmonitoring.standardPlots.comparePlot.ComparePlotTab;
import de.jmonitoring.standardPlots.maintenancePlot.MaintenancePlotTab;
import de.jmonitoring.standardPlots.ogivePlot.OgivePlotTab;
import de.jmonitoring.standardPlots.plotTabs.StandardPlotTab;
import de.jmonitoring.standardPlots.plotTabs.StandardPlotTab.Result;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesPlotTab;
import de.jmonitoring.standardPlots.scatterPlot.XYPlotTab;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import de.jmonitoring.WeatherCalculation.ClimateFactorReader;
import de.jmonitoring.utils.CloseApplicationAdapter;
import de.jmonitoring.utils.DeepCopyCollection;
import de.jmonitoring.utils.GRAWriter;
import de.jmonitoring.utils.SensorListexporter.SensorListExporter;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.WeatherCalculation.DegreeDayParameterDialog;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.utils.SensorListexporter.ExportSensorListFileChooser;
import de.jmonitoring.DataHandling.MONReaderThread;
import de.jmonitoring.Consistency.QualityPlotThread;
import de.jmonitoring.utils.WebBrowser;
import de.jmonitoring.utils.commandLine.MoniSoftCommandline;
import de.jmonitoring.utils.filenamefilter.GRA_FilenameFilter;
import de.jmonitoring.utils.filenamefilter.XLS_FilenameFilter;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.swing.EDT;
import java.awt.Rectangle;

/**
 * This class represents the GUI (JFrame) of MoniSoft.<p> It defines the screen
 * layout as well as the menu entries and their functionality.
 *
 * @author togro
 */
public class MainGUI extends JFrame implements MainApplication, ConnectionStatus, MessageDisplayer, DesktopManager, ConsoleManager {

    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();
    private int IFRAME_X_ORIGIN = 20;
    private int IFRAME_Y_ORIGIN = 20;
    private ProgressBarPanel progressBarPanel = new ProgressBarPanel();
    private ChartDescriberDateRangeDialog dateRangeDialog = new ChartDescriberDateRangeDialog(this, true);
    private final MoniSoft application;
    private final PlotTabManager plotTabManager;
    private boolean recyleDescriber = false;
    private ChartDescriber recycledChartDescriber;
    private boolean dateLock = false;
    private final CloseApplicationAdapter closeApplicationAdapter;
    // TODO: Move these fields to class MaintainanceTasks
    private boolean lock_Backupreconstruction = false;
    private final DebugConsole debugConsole = new DebugConsole();
    private SensorCategoryEditor keyEditor = null;
    private SensorCollectionEditor collectionEditor = null;

    /**
     * Creates new form MainGUI
     */
    public MainGUI(MoniSoft application) {
        super();
        EDT.always();
        this.application = application;
        this.closeApplicationAdapter = new CloseApplicationAdapter(getMainFrame());
        this.plotTabManager = new PlotTabManager();
        registerPlots();
        initComponents();
        disconnectButton.setEnabled(false);  //  zur Sicherheit falls in GUI-editor enabled (daran hängt die ganze Bedienfläche per bind)
        StatusPanel.add(progressBarPanel, BorderLayout.CENTER);
    }

    /**
     * Flag indicating if a certain chart describer is reclycled (resued) or is
     * defined from a new query
     *
     * @param b
     */
    public void setRecycleDescriber(boolean b) {
        this.recyleDescriber = b;
    }

    @Override
    public boolean isLogCalculation() {
        return calcProtocolCheckBox.isSelected();
    }

    @Override
    public Frame getMainFrame() {
        return this;
    }

    @Override
    public void enableConnectedLED() {
        setConnectLED(Color.GREEN);
    }

    @Override
    public void enableDisconnectedLED() {
        setConnectLED(Color.RED);
    }

    @Override
    public void enableIdleLED() {
        setConnectLED(Color.ORANGE);
    }

    @Override
    public void setLabel(String text) {
        connectLabel.setText(text);
    }

    /**
     * Belegt die Panels mit den aus den Describern gewonnenen Einstellungen
     * (bei Auswahl einer gespeicherten Grafik)
     *
     * @param d
     */
    @Override
    public void fillPanelFromDescriber(ChartDescriber d) {
        recycledChartDescriber = (ChartDescriber) new DeepCopyCollection().makeDeepCopy(d);

        for (StandardPlotTab plotTab : plotTabManager) {
            Result action = plotTab.fillFrom(recycledChartDescriber);
            if (Result.APPLIED == action) {
                this.plotTabManager.showPlotTab(plotTab);
                TabbedPanel.setIconAt(TabbedPanel.getSelectedIndex(), new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chain.png")));
                setRecycleDescriber(true);
                return;
            }
        }

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("UNKNOWN_CHARTDESCRIBER") + "\n", true);
    }

    @Override
    public void drawUsingDescriber(DescriberFactory describerFactory, DateInterval interval, ArrayList<? extends SeriesLooks> looksCollection) {
        ChartDescriber describer = describerFactory.createChartDescriber();

        // should a desriber be recyled? 
        if (isRecycleDescriber()) {
            recycledChartDescriber.setChartCollection(looksCollection);
            recycledChartDescriber.setDateInterval(interval);

            // TODO do the same for scatterplots?
            if (describer instanceof TimeSeriesChartDescriber) {
                recycledChartDescriber.setValueFilter(describer.getValueFilter());
            }

            drawChart(recycledChartDescriber);
        } else {
            drawChart(describer);
        }
        setRecycleDescriber(false);
    }

    @Override
    public void showMessageDialog(final String message) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(getMainFrame(), message);
            }
        });
    }

    @Override
    public void writeToConsole(final String text, final boolean force) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isLogCalculation() || force) {
                        updateConsole(text);
                    }
                } catch (Exception e) {
                    writeToDebugConsole(e);
                }
            }
        });
    }

    @Override
    public void writeToDebugConsole(final Exception e) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                debugConsole.appendToConsole(e);
                logger.debug(e.getMessage());
            }
        });
    }

    public void lock_BackuptReconstruction(boolean lock) {
        lock_Backupreconstruction = lock;
    }

    public void initializeGUI(MonisoftSplash mSplash) {
        setTitle("MoniSoft " + this.application.getVersion());
        addWindowListener(closeApplicationAdapter);

        Image im = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/de/jmonitoring/icons/charts01.png"));
        setIconImage(im);

        // OS abhängige Einstellungen
        String osName = System.getProperty("os.name");

        mSplash.showMessage("Detecting OS ...", 4);

        if (osName.equals("Linux")) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Dimension screenSize = toolkit.getScreenSize();
            setSize(screenSize);
            System.setProperty("sun.java2d.opengl", "true");
        } else if (osName.contains("Windows")) {
            System.setProperty("sun.java2d.d3d", "true");
//                    System.setProperty("sun.java2d.translaccel", "true");
//                    System.setProperty("sun.java2d.ddforcevram", "true");
//                    System.setProperty("sun.java2d.accthreshold", "0");
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // wird über den Windowlistener abgefangen

        mSplash.showMessage("Starting application ...", 5);

        ToolTipManager.sharedInstance().unregisterComponent(getDesktop());
        adjustSplitPaneHeight();

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setDragMode();
        setVisible(true);
        showPasswordDialog();
    }

    public DateRangeProvider getDateRange() {
        return this.dateRangeDialog;
    }

    @Override
    public void showPasswordDialog() {
        PasswordDialog passwdDialog = new PasswordDialog(this, true, false, "", this.application.getDBConnector());
        if (!this.application.isTrial()) {
//            passwdDialog.setFields("Annex53", "localhost", "3306", "demo_database", false); // ANNEX-DEMO
//            passwdDialog.setSSHFields("moni-gast", "129.13.252.53", "22", "172.22.87.200", "3306", "3306", "0"); // ANNEX-DEMO

//            passwdDialog.setFields("moni-gast", "localhost", "3306", "demo_database", false); // STANDARD-DEMO
//            passwdDialog.setSSHFields("moni-gast", "129.13.252.53", "22", "172.22.87.200", "3306", "3306", "1");  // STANDARD-DEMO

            Properties applicationProps = this.application.getApplicationProperties();
            if (applicationProps.getProperty("DefaultUser").isEmpty() && applicationProps.getProperty("DefaultDB").isEmpty() && (applicationProps.getProperty("DefaultServer").equals("localhost") || applicationProps.getProperty("DefaultServer").isEmpty())) {
                passwdDialog.setFields(applicationProps.getProperty("LAST_USER"), applicationProps.getProperty("LAST_SERVER"), applicationProps.getProperty("LAST_PORT"), applicationProps.getProperty("LAST_DB"), false);
            } else {
                passwdDialog.setFields(applicationProps.getProperty("DefaultUser"), applicationProps.getProperty("DefaultServer"), applicationProps.getProperty("DefaultServerPort"), applicationProps.getProperty("DefaultDB"), false);
            }
            passwdDialog.setSSHFields(applicationProps.getProperty("SSHUser"), applicationProps.getProperty("SSHServerIP"), applicationProps.getProperty("SSHServerPort"), applicationProps.getProperty("SSHTunnelIP"), applicationProps.getProperty("SSHTunnelPort"), applicationProps.getProperty("SSHLocalPort"), applicationProps.getProperty("UseSSHTunnel"));
        }
        passwdDialog.setLocationRelativeTo(this);
        passwdDialog.setVisible(true);
        closeApplicationAdapter.setConnector(this.application.getDBConnector());
        MoniSoft.setSSHTunnel(passwdDialog.getTunnelSession());
        passwdDialog.dispose();
    }

    @Override
    public ProgressBarPanel getProgressBarpanel() {
        return progressBarPanel;
    }

    @Override
    public void updateSensorSelectors(boolean set) {
        this.application.setGUIActive(false);
        // Die Auswahllisten leeren
        for (StandardPlotTab plotTab : plotTabManager) {
            plotTab.clearSelections();
        }
        // Füllen mit allen Messpunkten wenn gewünscht
        if (set) {
            Models models = new Models();
            for (StandardPlotTab plot : plotTabManager) {
                plot.setSelectionsFrom(models);
                plot.fillAnnotationChooser();
            }
        }
        this.application.setGUIActive(true);
        doLayout();
    }

    /**
     * Methode wird beim Start aufgerufen und setzt die Bedienelemente der
     * Oberfläche in den "connected" oder "not connected" Zustand
     *
     * @param isConnected
     */
    @Override
    public void setConnected(final boolean isConnected) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                disconnectItem.setEnabled(isConnected);
                connectItem.setEnabled(!isConnected);
                connectToDBButton.setEnabled(!isConnected);
                disconnectButton.setEnabled(isConnected);

                MainGUI.this.application.setGUIActive(isConnected);

                if (!isConnected) { // on disconnection
                    SensorInformation.clearSensorList();
                    BuildingInformation.clearBuildingList();
                    //            UnitList.clear();
                    for (StandardPlotTab plot : plotTabManager) {
                        plot.clearData();
                    }
                    updateSensorSelectors(isConnected);
                    setSavedChartCombobox(isConnected);
                } else { // on connection
                    MainGUI.this.application.loadProjectProperties();

                    // Belegen der Sensorauswahllisten mit den geholten Sensoren
                    updateSensorSelectors(isConnected);
                    // Die Auswahllisten für das Aggregationsintervall belegen
                    setIntervalSelectors();
                    // SeriesCollections mit null-Werten vorbelegen sowie Farben vorbelegen
                    resetSeriesCollections();
                    // Liste gespeicherter Messpunkte belegen
                    setSavedChartCombobox(isConnected);

                    // Prüfen ob 0-LogID existiert
                    DBMaintenance dbm = new DBMaintenance();
                    if (!dbm.hasDefaultLogEntry()) {
                        dbm.fixLog();
                    }

                    // TODO: This either belongs into MoniSoft or MainGUI
                    // Laden der automatischen Grafiken
                    if (!MainGUI.this.application.isSkipFavorites() && MainGUI.this.application.getProjectProperties().getProperty("ShowFavoriteCharts").equals("1")) {
                        MainGUI.this.application.loadFavorites();
                    }

                    // Laden der automatischen Grafiken
                    if (!MainGUI.this.application.isSkipFavorites() && MainGUI.this.application.getProjectProperties().getProperty("ShowCompareTable").equals("1")) {
                        loadCompareTableFavorite();
                    }

                    // Bei Demo Hinweis einblenden
                    if (MainGUI.this.application.isTrial()) {
                        DemoInfo demo = new DemoInfo();
                        desktopPanel.add(demo);
                        demo.setVisible(true);
                    }
                }
            }
        });
//        GUIActive = isConnected;
    }

    /**
     *
     */
    @Override
    public void setIntervalSelectors() {
        Models models = new Models();
        for (StandardPlotTab plot : plotTabManager) {
            plot.setIntervalSelector(models);
        }
    }

    @Override
    public void cascadeWindows() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                JInternalFrame[] frames = desktopPanel.getAllFrames();
                int x = 10;
                int y = 10;
                for (int i = 0; i < frames.length; i++) {
                    try {
                        frames[i].setIcon(false);
                    } catch (PropertyVetoException ex) {
                        Messages.showException(ex);
                    }
                    frames[i].setLocation(x, y);
                    x += 10;
                    y += 30;
                    frames[i].toFront();
                }
                desktopPanel.repaint();
            }
        });
    }

    @Override
    public void iconifyWindows(final boolean iconify) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                JInternalFrame[] frames = desktopPanel.getAllFrames();
                for (int i = 0; i < frames.length; i++) {
                    try {
                        frames[i].setIcon(iconify);
                    } catch (PropertyVetoException ex) {
                        logger.error("Error setting icon.", ex);
                    }
                }
                desktopPanel.repaint();
            }
        });
    }

    @Override
    public void closeAllWindows() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                int n = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CLOSE_ALL_WINDOWS"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUESTION"), JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    desktopPanel.removeAll();
                    desktopPanel.repaint();
                    IFRAME_X_ORIGIN = 20;
                    IFRAME_Y_ORIGIN = 20;
                }
            }
        });
    }

    @Override
    public void tileAllWindows() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                iconifyWindows(false);
                JInternalFrame[] frames = desktopPanel.getAllFrames();
                Rectangle dBounds = desktopPanel.getBounds();
                int cols = (int) Math.sqrt(frames.length);
                int rows = (int) (Math.ceil(((double) frames.length) / cols));
                int lastRow = frames.length - cols * (rows - 1);
                int width, height;
                JInternalFrame frame;

                if (lastRow == 0) {
                    rows--;
                    height = dBounds.height / rows;
                } else {
                    height = dBounds.height / rows;
                    if (lastRow < cols) {
                        rows--;
                        width = dBounds.width / lastRow;
                        for (int i = 0; i < lastRow; i++) {
                            frame = frames[cols * rows + i];

                            frame.setBounds(i * width, rows * height, width, height);
                        }
                    }
                }

                width = dBounds.width / cols;
                for (int j = 0; j < rows; j++) {
                    for (int i = 0; i < cols; i++) {
                        frame = frames[i + j * cols];
                        frame.setBounds(i * width, j * height, width, height);
                    }
                }
                desktopPanel.repaint();
            }
        });
    }

    @Override
    public void setDragMode(final int dragMode) {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                desktopPanel.setDragMode(dragMode);
            }
        });
    }

    @Override
    public void lockDates(boolean lock) {
        EDT.always();
        for (StandardPlotTab plot : plotTabManager) {
            plot.lockDates(lock);
        }
        dateLock = lock;
    }

    public void showCompareTable() {
        EDT.always();
        CompareTableFrame tf = new CompareTableFrame(this);
        desktopPanel.add(tf);
        tf.setVisible(true);
        tf.moveToFront();
    }

    @Override
    public void disposeIFrame(JInternalFrame iframe) {
        EDT.always();
        iframe.dispose();
        desktopPanel.revalidate();
    }

    public JTextArea getConsole() {
        return consoleTextArea;
    }

    @Override
    public JFreeChart getActiveChart() {
        return getCurrentChart();
    }

    @Override
    public CtrlChartPanel getActiveCtrlChartPanel() {
        return getActiveChartPanel();
    }

    @Override
    public JDesktopPane getDesktop() {
        return desktopPanel;
    }

    public void setConnectLED(final Color c) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                connectLED.setBackground(c);
            }
        });
    }

    public boolean verbose() {
        return calcProtocolCheckBox.isSelected();
    }

    @Override
    public void setSavedChartCombobox(final boolean set) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                if (set) {
                    // Combobox gespeicherter Grafiken belegen (Lesen aus den GRA Dateien)
                    Properties applicationProps = MoniSoft.getInstance().getApplicationProperties();
                    File userdir = new File(applicationProps.getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER);
                    final String entries[] = userdir.list(new GRA_FilenameFilter()); // Liste der im Verzeichnis gespeicherten GRA-Dateien
                    String[] init = {MoniSoftConstants.NO_CHARTDESCRIPTION_AVAILABLE};
                    DefaultComboBoxModel model = new DefaultComboBoxModel(init);
                    if (entries != null && entries.length > 0) {
                        for (int i = 0; i < entries.length; i++) {
                            entries[i] = entries[i].replace(".gra", ""); // das suffix zur darstellung entfernen
                        }
                        TreeSet<String> list = new TreeSet<String>(Arrays.asList(entries));
                        model = new DefaultComboBoxModel(list.toArray());
                        model.insertElementAt(MoniSoftConstants.NO_CHARTDESCRIPTION_SELECTED, 0);
                        storedChartsCombobox.setModel(model);
                        storedChartsCombobox.setSelectedItem(MoniSoftConstants.NO_CHARTDESCRIPTION_SELECTED);
                    } else {
                        storedChartsCombobox.setModel(model);
                        storedChartsCombobox.setSelectedItem(MoniSoftConstants.NO_CHARTDESCRIPTION_AVAILABLE);
                    }
                } else {
                    storedChartsCombobox.removeAllItems();
                }
            }
        });
    }

    @Override
    public void clearConsole() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                consoleTextArea.setText("");
            }
        });
    }

    @Override
    public void copyConsoleToClipBoard() {
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                consoleTextArea.copy();
            }
        });
    }

    public void showQualityView() {
        QualityPlotThread qt = new QualityPlotThread(getDesktop(), IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN, this);
        qt.start();
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
    }

    // NOTE: Don't call directly, but via MaintainancePlot.showIn(...)
    @Override
    public void showMaintenanceChart(DateInterval dateInterval, int sensorID, boolean markNegative) {
        JInternalFrame f = new MaintenanceChart(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft.MAINTENANCECHART"), dateInterval, sensorID, this, markNegative);
        desktopPanel.add(f);
        f.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        f.moveToFront();
    }

    /**
     * Löst das Erzeugen der Grafikerstellung abhängig vom übergebenen Describer
     * aus und zeichnet den InternalFrame
     *
     * @param describer
     */
    @Override
    public JFreeChart drawChart(final ChartDescriber describer) {
        EDT.always();
        JFreeChart chart = null;
        if (isGUI()) {
            TabbedPanel.setIconAt(TabbedPanel.getSelectedIndex(), null);
        }
        JInternalFrame f = null;
//        recyleDescriber = false;

        MoniSoftCommandline commandLine = MoniSoft.getInstance().getCommandLine();

        if (describer instanceof TimeSeriesChartDescriber) {
            f = new TimeSeriesChart((TimeSeriesChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            // if called from command line save as PNG
            if (!isGUI()) {
                chart = ((TimeSeriesChart) f).getChart();
                try {
                    ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else if (describer instanceof ScatterChartDescriber) {
            f = new ScatterPlotChart((ScatterChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            // if called from command line save as PNG
            if (!isGUI()) {
                chart = ((ScatterPlotChart) f).getChart();
                try {
                    ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else if (describer instanceof OgiveChartDescriber) {
            f = new OgivePlotChart((OgiveChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            // if called from command line save as PNG
            if (!isGUI()) {
                chart = ((OgivePlotChart) f).getChart();
                try {
                    ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else if (describer instanceof CompareChartDescriber) {
            f = new CompareChart((CompareChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            // if called from command line save as PNG
            if (!isGUI()) {
                chart = ((CompareChart) f).getChart();
                try {
                    ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        } else if (describer instanceof CarpetChartDescriber) {
            f = new CarpetChart((CarpetChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            // if called from command line save as PNG
            if (!isGUI()) {
                chart = ((CarpetChart) f).getChart();
                try {
                    ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }

        // only when caled from GUI - place chart on desktop
        if (isGUI()) {
            f.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
            f.setSize(describer.getWidth(), describer.getHeight());
            shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
            desktopPanel.add(f);
            f.moveToFront();
        }
        return chart;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        StatusPanel = new javax.swing.JPanel();
        connectionInformationPanel = new javax.swing.JPanel();
        connectLED = new javax.swing.JPanel();
        connectLabel = new javax.swing.JLabel();
        mainSplitPane = new javax.swing.JSplitPane();
        TabbedPanel = this.plotTabManager.asTabbedPane();
        consoleSplitPane = new javax.swing.JSplitPane();
        desktopPanel = new javax.swing.JDesktopPane();

        autoLabel = new javax.swing.JLabel();
        consoleScrollPane = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        toolBarPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        connectToDBButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton19 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        storedChartsCombobox = new javax.swing.JComboBox();
        jButton10 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        openTreeSelectorButton = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        PDF_Button = new javax.swing.JButton();
        PNG_Button = new javax.swing.JButton();
        SVG_Button = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        MenuBar1 = new javax.swing.JMenuBar();
        DBMenu = new javax.swing.JMenu();
        connectItem = new javax.swing.JMenuItem();
        disconnectItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        consistencyMenuItem = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuItem11 = new javax.swing.JMenuItem();
        FileMenu = new javax.swing.JMenu();
        exportMenuItem = new javax.swing.JMenuItem();
        jMenuItem34 = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        manualMenuItem = new javax.swing.JMenuItem();
        csvImportMenu = new javax.swing.JMenuItem();
        readMonFileMenu = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        deleteDataMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        chechDataMenuItem = new javax.swing.JMenuItem();
        jSeparator21 = new javax.swing.JPopupMenu.Separator();
        jMenuItem30 = new javax.swing.JMenuItem();
        SensorMenu = new javax.swing.JMenu();
        ParameterMenuItem = new javax.swing.JMenuItem();
        NewSensorMenu = new javax.swing.JMenuItem();
        NewSensorMenu1 = new javax.swing.JMenuItem();
        jMenuItem31 = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        qualityMenuItem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        eventReorganizeMenuItem = new javax.swing.JMenuItem();
        factorChangeMenuItem = new javax.swing.JMenuItem();
        counterChangeMenuItem = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jMenuItem12 = new javax.swing.JMenuItem();
        keyWordEditorMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem32 = new javax.swing.JMenuItem();
        BuildingCompMenu = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem33 = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JSeparator();
        compareClassMenuItam = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        jMenuItem24 = new javax.swing.JMenuItem();
        jMenuItem25 = new javax.swing.JMenuItem();
        jSeparator16 = new javax.swing.JPopupMenu.Separator();
        jMenuItem28 = new javax.swing.JMenuItem();
        jMenuItem26 = new javax.swing.JMenuItem();
        jSeparator17 = new javax.swing.JPopupMenu.Separator();
        jMenuItem17 = new javax.swing.JMenuItem();
        CalculationsMenu = new javax.swing.JMenu();
        jMenuItem14 = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jSeparator18 = new javax.swing.JPopupMenu.Separator();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem29 = new javax.swing.JMenuItem();
        jSeparator19 = new javax.swing.JPopupMenu.Separator();
        jMenuItem27 = new javax.swing.JMenuItem();
        jMenuItem13 = new javax.swing.JMenuItem();
        jSeparator20 = new javax.swing.JPopupMenu.Separator();
        jMenuItem35 = new javax.swing.JMenuItem();
        WindowMenu = new javax.swing.JMenu();
        closeAllItem = new javax.swing.JMenuItem();
        cascadeItem = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem37 = new javax.swing.JMenuItem();
        ExtrasMenu = new javax.swing.JMenu();
        ApplicationPropsMenu = new javax.swing.JMenuItem();
        jMenuItem23 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        calcProtocolCheckBox = new javax.swing.JCheckBoxMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem38 = new javax.swing.JMenuItem();
        HelpMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem21 = new javax.swing.JMenuItem();
        jMenuItem18 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        WeatherMenu = new javax.swing.JMenu();
        jMenuItem19 = new javax.swing.JMenuItem();
        jMenuItem22 = new javax.swing.JMenuItem();
        jMenuItem20 = new javax.swing.JMenuItem();
        testTriggerMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle"); // NOI18N
        setTitle(bundle.getString("MoniSoft.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(800, 600));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mainFrameResized(evt);
            }
        });

        StatusPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        StatusPanel.setDoubleBuffered(false);
        StatusPanel.setMaximumSize(new java.awt.Dimension(105, 25));
        StatusPanel.setMinimumSize(new java.awt.Dimension(105, 104));
        StatusPanel.setPreferredSize(new java.awt.Dimension(105, 25));
        StatusPanel.setLayout(new java.awt.BorderLayout());

        connectionInformationPanel.setMaximumSize(new java.awt.Dimension(400, 28));
        connectionInformationPanel.setMinimumSize(new java.awt.Dimension(400, 28));

        connectLED.setBackground(new java.awt.Color(255, 0, 0));
        connectLED.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        connectLED.setForeground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        connectLED.setAlignmentY(0.7F);

        javax.swing.GroupLayout connectLEDLayout = new javax.swing.GroupLayout(connectLED);
        connectLED.setLayout(connectLEDLayout);
        connectLEDLayout.setHorizontalGroup(
            connectLEDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 10, Short.MAX_VALUE)
        );
        connectLEDLayout.setVerticalGroup(
            connectLEDLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 9, Short.MAX_VALUE)
        );

        connectLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        connectLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        connectLabel.setText(bundle.getString("MoniSoft.connectLabel.text")); // NOI18N
        connectLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout connectionInformationPanelLayout = new javax.swing.GroupLayout(connectionInformationPanel);
        connectionInformationPanel.setLayout(connectionInformationPanelLayout);
        connectionInformationPanelLayout.setHorizontalGroup(
            connectionInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connectionInformationPanelLayout.createSequentialGroup()
                .addContainerGap(23, Short.MAX_VALUE)
                .addComponent(connectLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connectLED, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7))
        );
        connectionInformationPanelLayout.setVerticalGroup(
            connectionInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionInformationPanelLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(connectionInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(connectLED, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectLabel, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        StatusPanel.add(connectionInformationPanel, java.awt.BorderLayout.EAST);

        getContentPane().add(StatusPanel, java.awt.BorderLayout.SOUTH);

        mainSplitPane.setDividerLocation(490);
        mainSplitPane.setDividerSize(8);
        mainSplitPane.setOneTouchExpandable(true);

        TabbedPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        TabbedPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        TabbedPanel.setToolTipText(bundle.getString("MoniSoft.TabbedPanel.toolTipText")); // NOI18N
        TabbedPanel.setFocusable(false);
        TabbedPanel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        TabbedPanel.setMinimumSize(new java.awt.Dimension(450, 43));
        TabbedPanel.setOpaque(true);
        TabbedPanel.setPreferredSize(new java.awt.Dimension(450, 100));
        TabbedPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                TabbedPanelStateChanged(evt);
            }
        });
        mainSplitPane.setLeftComponent(TabbedPanel);

        consoleSplitPane.setDividerLocation(600);
        consoleSplitPane.setDividerSize(8);
        consoleSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        consoleSplitPane.setOneTouchExpandable(true);

        desktopPanel.setToolTipText(""); // NOI18N
        desktopPanel.setAutoscrolls(true);
        desktopPanel.setComponentPopupMenu(new de.jmonitoring.Components.DesktopPanePopup(this));
        desktopPanel.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        desktopPanel.setFocusable(false);

        autoLabel.setForeground(new java.awt.Color(255, 255, 255));
        autoLabel.setText(bundle.getString("MoniSoft.autoLabel.text")); // NOI18N
        autoLabel.setBounds(0, 0, 390, 15);
        desktopPanel.add(autoLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        consoleSplitPane.setLeftComponent(desktopPanel);

        consoleTextArea.setEditable(false);
        consoleTextArea.setBackground(javax.swing.UIManager.getDefaults().getColor("ToolBar.borderColor"));
        consoleTextArea.setColumns(20);
        consoleTextArea.setFont(new java.awt.Font("Courier New", 0, 11)); // NOI18N
        consoleTextArea.setRows(5);
        consoleTextArea.setComponentPopupMenu(new ConsolePopup(this));
        consoleScrollPane.setViewportView(consoleTextArea);

        consoleSplitPane.setRightComponent(consoleScrollPane);

        mainSplitPane.setRightComponent(consoleSplitPane);

        getContentPane().add(mainSplitPane, java.awt.BorderLayout.CENTER);

        toolBarPanel.setDoubleBuffered(false);
        toolBarPanel.setMinimumSize(new java.awt.Dimension(30, 25));

        jToolBar1.setFloatable(false);
        jToolBar1.setOpaque(false);

        connectToDBButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/connect.png"))); // NOI18N
        connectToDBButton.setToolTipText(bundle.getString("MoniSoft.connectToDBButton.toolTipText")); // NOI18N
        connectToDBButton.setFocusable(false);
        connectToDBButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        connectToDBButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        connectToDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectToDBButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(connectToDBButton);

        disconnectButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disconnect.png"))); // NOI18N
        disconnectButton.setToolTipText(bundle.getString("MoniSoft.disconnectButton.toolTipText")); // NOI18N
        disconnectButton.setFocusable(false);
        disconnectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        disconnectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        disconnectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(disconnectButton);

        jButton8.setBorderPainted(false);
        jButton8.setEnabled(false);
        jButton8.setFocusable(false);
        jButton8.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton8.setPreferredSize(new java.awt.Dimension(20, 12));
        jButton8.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton8);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk_download.png"))); // NOI18N
        jButton3.setToolTipText(bundle.getString("MoniSoft.jButton3.toolTipText")); // NOI18N
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton3);

        jButton9.setBorderPainted(false);
        jButton9.setEnabled(false);
        jButton9.setFocusable(false);
        jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton9.setPreferredSize(new java.awt.Dimension(20, 12));
        jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton9);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_cascade.png"))); // NOI18N
        jButton4.setToolTipText(bundle.getString("MoniSoft.jButton4.toolTipText")); // NOI18N
        jButton4.setFocusable(false);
        jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_delete.png"))); // NOI18N
        jButton5.setToolTipText(bundle.getString("MoniSoft.jButton5.toolTipText")); // NOI18N
        jButton5.setFocusable(false);
        jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton5);

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_side_contract.png"))); // NOI18N
        jButton6.setToolTipText(bundle.getString("MoniSoft.jButton6.toolTipText")); // NOI18N
        jButton6.setFocusable(false);
        jButton6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_side_expand.png"))); // NOI18N
        jButton7.setToolTipText(bundle.getString("MoniSoft.jButton7.toolTipText")); // NOI18N
        jButton7.setFocusable(false);
        jButton7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton7);

        jButton19.setBorderPainted(false);
        jButton19.setEnabled(false);
        jButton19.setFocusable(false);
        jButton19.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton19.setPreferredSize(new java.awt.Dimension(20, 12));
        jButton19.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton19);

        jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel10.setMaximumSize(new java.awt.Dimension(420, 2147483647));
        jPanel10.setMinimumSize(new java.awt.Dimension(420, 25));
        jPanel10.setPreferredSize(new java.awt.Dimension(420, 28));
        jPanel10.setLayout(new java.awt.BorderLayout());

        storedChartsCombobox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        storedChartsCombobox.setMaximumRowCount(50);
        storedChartsCombobox.setToolTipText(bundle.getString("MoniSoft.storedChartsCombobox.toolTipText")); // NOI18N
        storedChartsCombobox.setMaximumSize(new java.awt.Dimension(300, 18));
        storedChartsCombobox.setMinimumSize(new java.awt.Dimension(300, 18));
        storedChartsCombobox.setPreferredSize(new java.awt.Dimension(300, 18));
        storedChartsCombobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storedChartsComboboxActionPerformed(evt);
            }
        });
        jPanel10.add(storedChartsCombobox, java.awt.BorderLayout.CENTER);

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/wrench.png"))); // NOI18N
        jButton10.setToolTipText(bundle.getString("MoniSoft.jButton10.toolTipText")); // NOI18N
        jButton10.setBorderPainted(false);
        jButton10.setContentAreaFilled(false);
        jButton10.setFocusable(false);
        jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton10.setMaximumSize(new java.awt.Dimension(28, 21));
        jButton10.setMinimumSize(new java.awt.Dimension(28, 21));
        jButton10.setPreferredSize(new java.awt.Dimension(28, 21));
        jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton10, java.awt.BorderLayout.LINE_END);

        jToolBar1.add(jPanel10);

        jButton18.setBorderPainted(false);
        jButton18.setEnabled(false);
        jButton18.setFocusable(false);
        jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton18.setPreferredSize(new java.awt.Dimension(20, 12));
        jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton18);

        openTreeSelectorButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        openTreeSelectorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/node-select-all.png"))); // NOI18N
        openTreeSelectorButton.setText(bundle.getString("MoniSoft.openTreeSelectorButton.text")); // NOI18N
        openTreeSelectorButton.setToolTipText(bundle.getString("MoniSoft.openTreeSelectorButton.toolTipText")); // NOI18N
        openTreeSelectorButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        openTreeSelectorButton.setFocusable(false);
        openTreeSelectorButton.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        openTreeSelectorButton.setMaximumSize(new java.awt.Dimension(160, 30));
        openTreeSelectorButton.setMinimumSize(new java.awt.Dimension(160, 30));
        openTreeSelectorButton.setPreferredSize(new java.awt.Dimension(160, 30));
        openTreeSelectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openTreeSelectorButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openTreeSelectorButton);

        jButton21.setBorderPainted(false);
        jButton21.setEnabled(false);
        jButton21.setFocusable(false);
        jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton21.setPreferredSize(new java.awt.Dimension(20, 12));
        jButton21.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton21);

        PDF_Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/document-pdf.png"))); // NOI18N
        PDF_Button.setToolTipText(bundle.getString("MoniSoft.PDF_Button.toolTipText")); // NOI18N
        PDF_Button.setFocusable(false);
        PDF_Button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PDF_Button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        PDF_Button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        PDF_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PDF_ButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(PDF_Button);

        PNG_Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/photo-png.png"))); // NOI18N
        PNG_Button.setToolTipText(bundle.getString("MoniSoft.PNG_Button.toolTipText")); // NOI18N
        PNG_Button.setFocusable(false);
        PNG_Button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        PNG_Button.setIconTextGap(0);
        PNG_Button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        PNG_Button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        PNG_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PNG_ButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(PNG_Button);

        SVG_Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/svg-logo.png"))); // NOI18N
        SVG_Button.setToolTipText(bundle.getString("MoniSoft.SVG_Button.toolTipText")); // NOI18N
        SVG_Button.setFocusable(false);
        SVG_Button.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        SVG_Button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        SVG_Button.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        SVG_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SVG_ButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(SVG_Button);

        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jButton11.setBorderPainted(false);
        jButton11.setContentAreaFilled(false);
        jButton11.setFocusPainted(false);
        jButton11.setIconTextGap(0);
        jButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11help(evt);
            }
        });

        javax.swing.GroupLayout toolBarPanelLayout = new javax.swing.GroupLayout(toolBarPanel);
        toolBarPanel.setLayout(toolBarPanelLayout);
        toolBarPanelLayout.setHorizontalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolBarPanelLayout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 937, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jButton11)
                .addContainerGap())
        );
        toolBarPanelLayout.setVerticalGroup(
            toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolBarPanelLayout.createSequentialGroup()
                .addGroup(toolBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton11)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        getContentPane().add(toolBarPanel, java.awt.BorderLayout.NORTH);

        MenuBar1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        DBMenu.setText(bundle.getString("MoniSoft.DBMenu.text")); // NOI18N
        DBMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        connectItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        connectItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/connect.png"))); // NOI18N
        connectItem.setText(bundle.getString("MoniSoft.connectItem.text")); // NOI18N
        connectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectItemActionPerformed(evt);
            }
        });
        DBMenu.add(connectItem);

        disconnectItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        disconnectItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disconnect.png"))); // NOI18N
        disconnectItem.setText(bundle.getString("MoniSoft.disconnectItem.text")); // NOI18N
        disconnectItem.setEnabled(false);
        disconnectItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectItemActionPerformed(evt);
            }
        });
        DBMenu.add(disconnectItem);
        DBMenu.add(jSeparator3);

        consistencyMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        consistencyMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/database_refresh.png"))); // NOI18N
        consistencyMenuItem.setText(bundle.getString("MoniSoft.consistencyMenuItem.text")); // NOI18N
        consistencyMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consistencyMenuItemActionPerformed(evt);
            }
        });
        DBMenu.add(consistencyMenuItem);

        jMenuItem7.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/database_save.png"))); // NOI18N
        jMenuItem7.setText(bundle.getString("MoniSoft.jMenuItem7.text")); // NOI18N
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7backupMenuItemPressed(evt);
            }
        });
        DBMenu.add(jMenuItem7);
        DBMenu.add(jSeparator7);

        jMenuItem10.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/asterisk_yellow.png"))); // NOI18N
        jMenuItem10.setText(bundle.getString("MoniSoft.jMenuItem10.text")); // NOI18N
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10newProjectActionPerformed(evt);
            }
        });
        DBMenu.add(jMenuItem10);
        DBMenu.add(jSeparator8);

        jMenuItem11.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/control-power.png"))); // NOI18N
        jMenuItem11.setText(bundle.getString("MoniSoft.jMenuItem11.text")); // NOI18N
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        DBMenu.add(jMenuItem11);

        MenuBar1.add(DBMenu);

        FileMenu.setText(bundle.getString("MoniSoft.FileMenu.text")); // NOI18N
        FileMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        exportMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        exportMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_go.png"))); // NOI18N
        exportMenuItem.setText(bundle.getString("MoniSoft.exportMenuItem.text")); // NOI18N
        exportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(exportMenuItem);

        jMenuItem34.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem34.setEnabled(false);
        jMenuItem34.setLabel(bundle.getString("MoniSoft.jMenuItem34.label")); // NOI18N
        jMenuItem34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem34ActionPerformed(evt);
            }
        });
        FileMenu.add(jMenuItem34);
        FileMenu.add(jSeparator12);

        manualMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        manualMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/pencil.png"))); // NOI18N
        manualMenuItem.setText(bundle.getString("MoniSoft.manualMenuItem.text")); // NOI18N
        manualMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(manualMenuItem);

        csvImportMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        csvImportMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/table-import.png"))); // NOI18N
        csvImportMenu.setText(bundle.getString("MoniSoft.csvImportMenu.text")); // NOI18N
        csvImportMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                csvImportMenuActionPerformed(evt);
            }
        });
        FileMenu.add(csvImportMenu);

        readMonFileMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        readMonFileMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/system-monitor.png"))); // NOI18N
        readMonFileMenu.setText(bundle.getString("MoniSoft.readMonFileMenu.text")); // NOI18N
        readMonFileMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readMonFileMenureadDataFileMenuActionPerformed(evt);
            }
        });
        FileMenu.add(readMonFileMenu);
        FileMenu.add(jSeparator11);

        deleteDataMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        deleteDataMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/database-delete.png"))); // NOI18N
        deleteDataMenuItem.setText(bundle.getString("MoniSoft.deleteDataMenuItem.text")); // NOI18N
        deleteDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteDataMenuItem(evt);
            }
        });
        FileMenu.add(deleteDataMenuItem);
        FileMenu.add(jSeparator14);

        chechDataMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        chechDataMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/tick.png"))); // NOI18N
        chechDataMenuItem.setText(bundle.getString("CHECK_DATA")); // NOI18N
        chechDataMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chechDataMenuItemActionPerformed(evt);
            }
        });
        FileMenu.add(chechDataMenuItem);
        FileMenu.add(jSeparator21);

        jMenuItem30.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem30.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/weather_cloudy_arrow.png"))); // NOI18N
        jMenuItem30.setText(bundle.getString("MoniSoft.jMenuItem30.text")); // NOI18N
        jMenuItem30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem30ActionPerformed(evt);
            }
        });
        FileMenu.add(jMenuItem30);

        MenuBar1.add(FileMenu);

        SensorMenu.setText(bundle.getString("MoniSoft.SensorMenu.text")); // NOI18N
        SensorMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        ParameterMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        ParameterMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_form.png"))); // NOI18N
        ParameterMenuItem.setText(bundle.getString("MoniSoft.ParameterMenuItem.text")); // NOI18N
        ParameterMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParameterMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(ParameterMenuItem);

        NewSensorMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        NewSensorMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_form_add.png"))); // NOI18N
        NewSensorMenu.setText(bundle.getString("MoniSoft.NewSensorMenu.text")); // NOI18N
        NewSensorMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewSensorMenunewSensorMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(NewSensorMenu);
        NewSensorMenu.setEnabled(!this.application.isTrial());

        NewSensorMenu1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        NewSensorMenu1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_form_add.png"))); // NOI18N
        NewSensorMenu1.setText(bundle.getString("MoniSoft.NewSensorMenu1.text")); // NOI18N
        NewSensorMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewSensorMenu1newSensorMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(NewSensorMenu1);
        NewSensorMenu1.setEnabled(!this.application.isTrial());

        jMenuItem31.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem31.setText(bundle.getString("MoniSoft.jMenuItem31.text")); // NOI18N
        jMenuItem31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem31ActionPerformed(evt);
            }
        });
        SensorMenu.add(jMenuItem31);
        SensorMenu.add(jSeparator15);

        qualityMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        qualityMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/rosette_blue.png"))); // NOI18N
        qualityMenuItem.setText(bundle.getString("MoniSoft.qualityMenuItem.text")); // NOI18N
        qualityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qualityMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(qualityMenuItem);
        SensorMenu.add(jSeparator5);

        eventReorganizeMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        eventReorganizeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow-merge-000-left.png"))); // NOI18N
        eventReorganizeMenuItem.setText(bundle.getString("MoniSoft.eventReorganizeMenuItem.text")); // NOI18N
        eventReorganizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventReorganizeMenuItemeventReconstructActionPerformed(evt);
            }
        });
        SensorMenu.add(eventReorganizeMenuItem);
        eventReorganizeMenuItem.setEnabled(!this.application.isTrial());

        factorChangeMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        factorChangeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow-repeat-once.png"))); // NOI18N
        factorChangeMenuItem.setText(bundle.getString("MoniSoft.factorChangeMenuItem.text")); // NOI18N
        factorChangeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                factorChangeMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(factorChangeMenuItem);

        counterChangeMenuItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        counterChangeMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/counter.png"))); // NOI18N
        counterChangeMenuItem.setText(bundle.getString("MoniSoft.counterChangeMenuItem.text")); // NOI18N
        counterChangeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                counterChangeMenuItemActionPerformed(evt);
            }
        });
        SensorMenu.add(counterChangeMenuItem);
        SensorMenu.add(jSeparator9);

        jMenuItem12.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/package.png"))); // NOI18N
        jMenuItem12.setText(bundle.getString("MoniSoft.jMenuItem12.text")); // NOI18N
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        SensorMenu.add(jMenuItem12);

        keyWordEditorMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        keyWordEditorMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/node-select-all.png"))); // NOI18N
        keyWordEditorMenu.setText(bundle.getString("MoniSoft.keyWordEditorMenu.text")); // NOI18N
        keyWordEditorMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyWordEditorMenushowCategoryEditor(evt);
            }
        });
        SensorMenu.add(keyWordEditorMenu);
        SensorMenu.add(jSeparator1);

        jMenuItem32.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem32.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/ruler.png"))); // NOI18N
        jMenuItem32.setText(bundle.getString("MoniSoft.jMenuItem32.text")); // NOI18N
        jMenuItem32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem32ActionPerformed(evt);
            }
        });
        SensorMenu.add(jMenuItem32);

        MenuBar1.add(SensorMenu);

        BuildingCompMenu.setText(bundle.getString("MoniSoft.BuildingCompMenu.text")); // NOI18N
        BuildingCompMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jMenuItem3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/home.png"))); // NOI18N
        jMenuItem3.setText(bundle.getString("MoniSoft.jMenuItem3.text")); // NOI18N
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem3);

        jMenuItem33.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem33.setText(bundle.getString("MoniSoft.jMenuItem33.text")); // NOI18N
        jMenuItem33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem33ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem33);
        BuildingCompMenu.add(jSeparator13);

        compareClassMenuItam.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        compareClassMenuItam.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart_pie.png"))); // NOI18N
        compareClassMenuItam.setText(bundle.getString("MoniSoft.compareClassMenuItam.text")); // NOI18N
        compareClassMenuItam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareClassMenuItamActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(compareClassMenuItam);

        jMenuItem9.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/table.png"))); // NOI18N
        jMenuItem9.setText(bundle.getString("MoniSoft.jMenuItem9.text")); // NOI18N
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem9);

        jMenuItem24.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem24.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/table--plus.png"))); // NOI18N
        jMenuItem24.setText(bundle.getString("MoniSoft.jMenuItem24.text")); // NOI18N
        jMenuItem24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem24ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem24);

        jMenuItem25.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem25.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/table--arrow.png"))); // NOI18N
        jMenuItem25.setText(bundle.getString("MoniSoft.jMenuItem25.text")); // NOI18N
        jMenuItem25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem25ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem25);
        BuildingCompMenu.add(jSeparator16);

        jMenuItem28.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem28.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/block.png"))); // NOI18N
        jMenuItem28.setText(bundle.getString("MoniSoft.jMenuItem28.text")); // NOI18N
        jMenuItem28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem28ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem28);

        jMenuItem26.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem26.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/sort-price.png"))); // NOI18N
        jMenuItem26.setText(bundle.getString("MoniSoft.jMenuItem26.text")); // NOI18N
        jMenuItem26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem26ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem26);
        BuildingCompMenu.add(jSeparator17);

        jMenuItem17.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/applicaton-home.png"))); // NOI18N
        jMenuItem17.setText(bundle.getString("MoniSoft.jMenuItem17.text")); // NOI18N
        jMenuItem17.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem17ActionPerformed(evt);
            }
        });
        BuildingCompMenu.add(jMenuItem17);

        MenuBar1.add(BuildingCompMenu);

        CalculationsMenu.setText(bundle.getString("MoniSoft.CalculationsMenu.text")); // NOI18N
        CalculationsMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jMenuItem14.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/balance-unbalance.png"))); // NOI18N
        jMenuItem14.setText(bundle.getString("MoniSoft.jMenuItem14.text")); // NOI18N
        jMenuItem14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem14ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem14);

        jMenuItem8.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart-up-color.png"))); // NOI18N
        jMenuItem8.setText(bundle.getString("MoniSoft.jMenuItem8.text")); // NOI18N
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem8);
        CalculationsMenu.add(jSeparator18);

        jMenuItem16.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart.png"))); // NOI18N
        jMenuItem16.setText(bundle.getString("MoniSoft.jMenuItem16.text")); // NOI18N
        jMenuItem16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem16calculateMonthyUsages(evt);
            }
        });
        CalculationsMenu.add(jMenuItem16);

        jMenuItem29.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem29.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart--pencil.png"))); // NOI18N
        jMenuItem29.setText(bundle.getString("MoniSoft.jMenuItem29.text")); // NOI18N
        jMenuItem29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem29ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem29);
        CalculationsMenu.add(jSeparator19);

        jMenuItem27.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem27.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/border-all.png"))); // NOI18N
        jMenuItem27.setText(bundle.getString("MoniSoft.jMenuItem27.text")); // NOI18N
        jMenuItem27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem27ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem27);

        jMenuItem13.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/grid.png"))); // NOI18N
        jMenuItem13.setText(bundle.getString("MoniSoft.jMenuItem13.text")); // NOI18N
        jMenuItem13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem13ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem13);
        CalculationsMenu.add(jSeparator20);

        jMenuItem35.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem35.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/sum.png"))); // NOI18N
        jMenuItem35.setText(bundle.getString("MoniSoft.jMenuItem35.text")); // NOI18N
        jMenuItem35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem35ActionPerformed(evt);
            }
        });
        CalculationsMenu.add(jMenuItem35);

        MenuBar1.add(CalculationsMenu);

        WindowMenu.setText(bundle.getString("MoniSoft.WindowMenu.text")); // NOI18N
        WindowMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        WindowMenu.setRequestFocusEnabled(false);

        closeAllItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        closeAllItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_delete.png"))); // NOI18N
        closeAllItem.setText(bundle.getString("MoniSoft.closeAllItem.text")); // NOI18N
        closeAllItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeAllItemActionPerformed(evt);
            }
        });
        WindowMenu.add(closeAllItem);

        cascadeItem.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        cascadeItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_cascade.png"))); // NOI18N
        cascadeItem.setText(bundle.getString("MoniSoft.cascadeItem.text")); // NOI18N
        cascadeItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cascadeItemActionPerformed(evt);
            }
        });
        WindowMenu.add(cascadeItem);

        jMenuItem15.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/application_tile_vertical.png"))); // NOI18N
        jMenuItem15.setText(bundle.getString("MoniSoft.jMenuItem15.text")); // NOI18N
        jMenuItem15.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem15ActionPerformed(evt);
            }
        });
        WindowMenu.add(jMenuItem15);

        jMenuItem37.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem37.setText(bundle.getString("MainFrame.MINIMIZEALL")); // NOI18N
        jMenuItem37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem37ActionPerformed(evt);
            }
        });
        WindowMenu.add(jMenuItem37);

        MenuBar1.add(WindowMenu);

        ExtrasMenu.setText(bundle.getString("MoniSoft.ExtrasMenu.text")); // NOI18N
        ExtrasMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        ApplicationPropsMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        ApplicationPropsMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/wrench.png"))); // NOI18N
        ApplicationPropsMenu.setText(bundle.getString("MoniSoft.ApplicationPropsMenu.text")); // NOI18N
        ApplicationPropsMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ApplicationPropsMenuActionPerformed(evt);
            }
        });
        ExtrasMenu.add(ApplicationPropsMenu);

        jMenuItem23.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/star.png"))); // NOI18N
        jMenuItem23.setText(bundle.getString("MoniSoft.jMenuItem23.text")); // NOI18N
        jMenuItem23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem23ActionPerformed(evt);
            }
        });
        ExtrasMenu.add(jMenuItem23);
        ExtrasMenu.add(jSeparator2);

        jMenuItem2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/bug.png"))); // NOI18N
        jMenuItem2.setText(bundle.getString("MoniSoft.jMenuItem2.text")); // NOI18N
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        ExtrasMenu.add(jMenuItem2);
        ExtrasMenu.add(jSeparator6);

        calcProtocolCheckBox.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        calcProtocolCheckBox.setText(bundle.getString("MoniSoft.calcProtocolCheckBox.text")); // NOI18N
        calcProtocolCheckBox.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/report--pencil.png"))); // NOI18N
        calcProtocolCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                calcProtocolCheckBoxActionPerformed(evt);
            }
        });
        ExtrasMenu.add(calcProtocolCheckBox);
        ExtrasMenu.add(jSeparator10);

        jMenuItem6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/database_lightning.png"))); // NOI18N
        jMenuItem6.setText(bundle.getString("MoniSoft.jMenuItem6.text")); // NOI18N
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6updateMenuActionPerformed(evt);
            }
        });
        ExtrasMenu.add(jMenuItem6);
        if (this.application.isTrial()) jMenuItem6.setEnabled(false);

        jMenuItem38.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem38.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart_line.png"))); // NOI18N
        jMenuItem38.setText(bundle.getString("ANNOTATION_EDITOR_MENU_ITEM")); // NOI18N
        jMenuItem38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem38ActionPerformed(evt);
            }
        });
        ExtrasMenu.add(jMenuItem38);

        MenuBar1.add(ExtrasMenu);

        HelpMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        HelpMenu.setLabel(bundle.getString("MoniSoft.HelpMenu.label")); // NOI18N

        jMenuItem1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/information-frame.png"))); // NOI18N
        jMenuItem1.setText(bundle.getString("MoniSoft.jMenuItem1.text")); // NOI18N
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        HelpMenu.add(jMenuItem1);

        jMenuItem21.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/bug_edit.png"))); // NOI18N
        jMenuItem21.setText(bundle.getString("MoniSoft.jMenuItem21.text")); // NOI18N
        jMenuItem21.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem21ActionPerformed(evt);
            }
        });
        HelpMenu.add(jMenuItem21);
        jMenuItem21.setEnabled(!this.application.isTrial());

        jMenuItem18.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jMenuItem18.setText(bundle.getString("MoniSoft.jMenuItem18.text")); // NOI18N
        jMenuItem18.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem18ActionPerformed(evt);
            }
        });
        HelpMenu.add(jMenuItem18);

        jMenu3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/hammer-screwdriver.png"))); // NOI18N
        jMenu3.setText(bundle.getString("MoniSoft.jMenu3.text")); // NOI18N
        jMenu3.setEnabled(false);
        jMenu3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        WeatherMenu.setText(bundle.getString("MoniSoft.WeatherMenu.text")); // NOI18N
        WeatherMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N

        jMenuItem19.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow-switch.png"))); // NOI18N
        jMenuItem19.setText(bundle.getString("MoniSoft.jMenuItem19.text")); // NOI18N
        jMenuItem19.setEnabled(false);
        jMenuItem19.setPreferredSize(new java.awt.Dimension(210, 19));
        jMenuItem19.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem19ActionPerformed(evt);
            }
        });
        WeatherMenu.add(jMenuItem19);

        jMenuItem22.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/weather_cloudy_sum.png"))); // NOI18N
        jMenuItem22.setText(bundle.getString("MoniSoft.jMenuItem22.text")); // NOI18N
        jMenuItem22.setEnabled(false);
        jMenuItem22.setPreferredSize(new java.awt.Dimension(210, 19));
        jMenuItem22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem22ActionPerformed(evt);
            }
        });
        WeatherMenu.add(jMenuItem22);

        jMenu3.add(WeatherMenu);

        jMenuItem20.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jMenuItem20.setText(bundle.getString("MoniSoft.jMenuItem20.text")); // NOI18N
        jMenuItem20.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem20ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem20);

        testTriggerMenu.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        testTriggerMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/broom.png"))); // NOI18N
        testTriggerMenu.setLabel(bundle.getString("MoniSoft.jMenuItem4.label")); // NOI18N
        testTriggerMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testTriggerMenuActionPerformed(evt);
            }
        });
        jMenu3.add(testTriggerMenu);

        HelpMenu.add(jMenu3);

        MenuBar1.add(HelpMenu);

        setJMenuBar(MenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void connectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectItemActionPerformed
        showPasswordDialog();
    }//GEN-LAST:event_connectItemActionPerformed

    private void disconnectItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectItemActionPerformed
        this.application.disconnectFromDB();
    }//GEN-LAST:event_disconnectItemActionPerformed

    private void consistencyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consistencyMenuItemActionPerformed
        if (this.application.isTrial()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CONSISTENCY_CHECK") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOT_IN_DEMO") + "\n", true);
            return;
        }
        Thread consistencyThread = new Thread(new MaintainanceTasks.checkDBConsistency());
        consistencyThread.start();
    }//GEN-LAST:event_consistencyMenuItemActionPerformed

    private void jMenuItem7backupMenuItemPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7backupMenuItemPressed
        if (isLocked_Backupreconstruction()) {
            Messages.showMessage("Ein anderes Backup ist bereits im Gange" + "\n", true);
            return;
        }

        if (this.application.isTrial()) {
            Messages.showMessage("Backup" + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOT_IN_DEMO") + "\n", true);
            return;
        }
        new MaintainanceTasks(this).doBackup();
    }//GEN-LAST:event_jMenuItem7backupMenuItemPressed

    private void jMenuItem10newProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10newProjectActionPerformed
        if (this.application.isTrial()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CREATING_PROJECTS") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOT_IN_DEMO") + "\n", true);
            return;
        }
        Thread npt = new Thread(
                new MaintainanceTasks.NewProjectThread(this.application.getDBConnector(), this));
        npt.start();
    }//GEN-LAST:event_jMenuItem10newProjectActionPerformed

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem11ActionPerformed
        closeApplicationAdapter.windowClosing(null);
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void exportMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportMenuItemActionPerformed
        showExportDialog();
    }//GEN-LAST:event_exportMenuItemActionPerformed

    private void jMenuItem34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem34ActionPerformed
        //        DataListFrame iFrame = new DataListFrame();
        //        iFrame.setSize(550, 650);
        //        iFrame.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        //        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        //        iFrame.doLayout();
        //        iFrame.pack();
        //        iFrame.setVisible(true);
        //        desktopPanel.add(iFrame);
        //        iFrame.moveToFront();
    }//GEN-LAST:event_jMenuItem34ActionPerformed

    private void manualMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualMenuItemActionPerformed
        ManualEntryDialog dialog = new ManualEntryDialog(this, true);
        dialog.setTitle("Manuelle Dateneingabe");
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_manualMenuItemActionPerformed

    private void csvImportMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_csvImportMenuActionPerformed
        CSVImportDialog dialog = new CSVImportDialog(null, false);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_csvImportMenuActionPerformed

    private void readMonFileMenureadDataFileMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readMonFileMenureadDataFileMenuActionPerformed
        if (this.application.isTrial()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DATA_IMPORT") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NOT_IN_DEMO") + "\n", true);
            return;
        }

        Thread drt = new Thread(new MONReaderThread("UTF-8"));
        drt.start();
    }//GEN-LAST:event_readMonFileMenureadDataFileMenuActionPerformed

    private void deleteDataMenuItem(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteDataMenuItem
        DeleteDataDialog d = new DeleteDataDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }//GEN-LAST:event_deleteDataMenuItem

    private void ParameterMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParameterMenuItemActionPerformed
        JInternalFrame iFrame = new JInternalFrame(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LIST_OF_SENSORS"), true, false, true, true);
        iFrame.setSize(900, 700);
        iFrame.setLocation(20, 20);
        //        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        iFrame.getContentPane().add(new SensorTablePanel(this));
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
    }//GEN-LAST:event_ParameterMenuItemActionPerformed

    private void NewSensorMenunewSensorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewSensorMenunewSensorMenuItemActionPerformed
        SensorEditorDialog dialog = new SensorEditorDialog(this, true, null, true, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_NewSensorMenunewSensorMenuItemActionPerformed

    private void NewSensorMenu1newSensorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewSensorMenu1newSensorMenuItemActionPerformed
        // Einlesen einer Messpunktliste

        File sensorFile;
        DBCreator creator = new DBCreator(getMainFrame());
        JFileChooser fc = new JFileChooser(getApplicationProperties().getProperty("DefaultSaveFolder"));
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "CSV-Datei (*.csv)";
            }
        });

        // Ask if existing Sensors should be changed
        if (fc.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("OPEN")) == JFileChooser.APPROVE_OPTION) {
            sensorFile = fc.getSelectedFile();
            int result = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("Should existing sensors be changed?"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUESTION"), JOptionPane.YES_NO_CANCEL_OPTION);
            boolean overwrite = false;

            switch (result) {
                case JOptionPane.YES_OPTION:
                    overwrite = true;
                    break;
                case JOptionPane.NO_OPTION:
                    overwrite = false;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("OP_CANCELLED"));
                    return;
            }

            // Getting CSV-Format Options
            CSVFormatSelector fs = new CSVFormatSelector(this, true);
            fs.setLocationRelativeTo(this);
            fs.setVisible(true);
            char delimiter = fs.getDelimiter();
            String encoding = fs.getEncoding();

            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("IMPORT_ONGOING") + "\n", true);
            if (creator.fillSensorTable(sensorFile, encoding, delimiter, overwrite) == DBCreator.SUCCESS) {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("IMPORT_SUCCESS") + "\n", true);
                SensorInformation.setSensorList(new ListFiller().readSensorList());
            } else {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("IMPORT_ERROR") + "\n", true);
            }
        }
    }//GEN-LAST:event_NewSensorMenu1newSensorMenuItemActionPerformed

    private void jMenuItem31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem31ActionPerformed
        // Export sensor list
        CSVFormatSelector fs = new CSVFormatSelector(this, true);
        fs.setLocationRelativeTo(this);
        fs.setVisible(true);
        SensorListExporter.export(new ExportSensorListFileChooser().getFile(this), fs.getDelimiter(), fs.getEncoding(), SensorInformation.getSensorList());
    }//GEN-LAST:event_jMenuItem31ActionPerformed

    private void qualityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qualityMenuItemActionPerformed
        showQualityView();
    }//GEN-LAST:event_qualityMenuItemActionPerformed

    private void eventReorganizeMenuItemeventReconstructActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventReorganizeMenuItemeventReconstructActionPerformed
        if (EventReconstructor.isEventReconstructionLocked()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("OTHER_RECONSTRUCTION_ONGOING") + "\n", true);
            return;
        }

        if (JOptionPane.showConfirmDialog(null, "Bei der Umstrukturierung der Event-Daten werden diese in einer Platz sparenden From gespeichert,\nin der aufeinander folgende Ereignisse ohne Statusänderung zusammen gefasst werden.\nÜberflüssige Einträge werden gelöscht.\n\nSoll mit der Umstrukturierung fortgefahren werden?", "Bestätigung", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            EventReconstructor er = new EventReconstructor(this);
            er.startReconstructionAll(new Object());
        }
    }//GEN-LAST:event_eventReorganizeMenuItemeventReconstructActionPerformed

    private void factorChangeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_factorChangeMenuItemActionPerformed
        FactorChangeDialog fd = new FactorChangeDialog(this, true, SensorInformation.getSensorList());
        fd.setLocationRelativeTo(this);
        fd.setVisible(true);
    }//GEN-LAST:event_factorChangeMenuItemActionPerformed

    private void counterChangeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_counterChangeMenuItemActionPerformed
        CounterChangeDialog counterChangeDialog = new CounterChangeDialog(this, dateLock);
        counterChangeDialog.setLocationRelativeTo(this);
        counterChangeDialog.setVisible(true);
    }//GEN-LAST:event_counterChangeMenuItemActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem12ActionPerformed
        collectionEditor = new SensorCollectionEditor(this, true, this);
        collectionEditor.setLocationRelativeTo(this);
        collectionEditor.setVisible(true);
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void keyWordEditorMenushowCategoryEditor(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyWordEditorMenushowCategoryEditor
        keyEditor = new SensorCategoryEditor(this, true);
        keyEditor.setLocationRelativeTo(this);
        keyEditor.setVisible(true);
    }//GEN-LAST:event_keyWordEditorMenushowCategoryEditor

    private void jMenuItem32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem32ActionPerformed
        UnitDefinitionDialog d = new UnitDefinitionDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        d.toFront();
    }//GEN-LAST:event_jMenuItem32ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        JInternalFrame iFrame = new JInternalFrame(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LIST_OF_BUILDINGS"), true, false, true, true);
        iFrame.setSize(900, 700);
        iFrame.setLocation(30, 20);
        //    shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        BuildingTablePanel panel = new BuildingTablePanel(this);
        iFrame.getContentPane().add(panel);
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem33ActionPerformed
        BuildingEditorDialog editor = new BuildingEditorDialog(this, true, null, true, true);
        editor.setLocationRelativeTo(this);
        editor.setFields(new BuildingProperties());
        editor.setVisible(true);
    }//GEN-LAST:event_jMenuItem33ActionPerformed

    private void compareClassMenuItamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compareClassMenuItamActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        CompareValueMappingEditor d = new CompareValueMappingEditor(this, false);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }//GEN-LAST:event_compareClassMenuItamActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JInternalFrame iFrame = new JInternalFrame(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LIST_OF_REFERENCES"), true, false, true, true);
        iFrame.setSize(900, 700);
        iFrame.setLocation(30, 20);
        ReferenceValueTablePanel panel = new ReferenceValueTablePanel(this);
        iFrame.getContentPane().add(panel);
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem24ActionPerformed
        ReferenceDefinitionDialog d = new ReferenceDefinitionDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        d.toFront();
    }//GEN-LAST:event_jMenuItem24ActionPerformed

    private void jMenuItem25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem25ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ReferenceImporter refImp = new ReferenceImporter();
        refImp.importReferences();
    }//GEN-LAST:event_jMenuItem25ActionPerformed

    private void jMenuItem28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem28ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ClusterEditor d = new ClusterEditor(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        d.toFront();
    }//GEN-LAST:event_jMenuItem28ActionPerformed

    private void jMenuItem26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem26ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SortByReferenceFrame sf = new SortByReferenceFrame();
        desktopPanel.add(sf);
        sf.setPreferredSize(new Dimension(400, 300));
        sf.setVisible(true);
        sf.moveToFront();
    }//GEN-LAST:event_jMenuItem26ActionPerformed

    private void jMenuItem17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem17ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        BuildingProfile bp = new BuildingProfile(this);
        displayInternalFrameWith("MoniSoft.BUILDINGPROFILE", bp);
        bp.setActive(true);
    }//GEN-LAST:event_jMenuItem17ActionPerformed

    private void jMenuItem14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem14ActionPerformed
        showCompareTable();
    }//GEN-LAST:event_jMenuItem14ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        ConsumptionFrame f = new ConsumptionFrame(this);
        f.setVisible(true);
        desktopPanel.add(f);
        f.moveToFront();
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem16calculateMonthyUsages(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem16calculateMonthyUsages
        displayInternalFrameWith("MoniSoft.MONTHLYUSAGETABLE", new MonthlyConsumtionPanel(this));
    }//GEN-LAST:event_jMenuItem16calculateMonthyUsages

    private void jMenuItem29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem29ActionPerformed
        if (MonthlyUsageCalculator.isLocked()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MONTHLY_CALC_ONGOING") + "\n", true);
            return;
        }

        MonthlyUsageDialog d = new MonthlyUsageDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);

        if (d.isGoOn()) {
            MonthlyUsageCalculator muc = new MonthlyUsageCalculator(this);
            muc.startWriteAllMonthlyUsage(null, d.getSelectedYear(), d.isOverwrite(), new Object());
        }
    }//GEN-LAST:event_jMenuItem29ActionPerformed

    private void jMenuItem27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem27ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        SectionChart sf = new SectionChart(this);
        desktopPanel.add(sf);
        sf.setVisible(true);
        sf.moveToFront();
    }//GEN-LAST:event_jMenuItem27ActionPerformed

    private void jMenuItem13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem13ActionPerformed
        if (BuildingInformation.getBuildingList().isEmpty()) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_BUILDINGS_DEFINED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HINT"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JInternalFrame iFrame = new JInternalFrame("Clustermatrix", true, true, true, true);
        iFrame.setSize(1000, 800);
        iFrame.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        iFrame.getContentPane().add(new ClusterMatrixPanel(this));
        iFrame.doLayout();
        iFrame.pack();
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
        try {
            iFrame.setSelected(true);
        } catch (PropertyVetoException ex) {
            logger.error("Error selecting internal frame", ex);
        }
    }//GEN-LAST:event_jMenuItem13ActionPerformed

    private void jMenuItem35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem35ActionPerformed
        StatisticsFrame iFrame = new StatisticsFrame(this);
        iFrame.setSize(510, 400);
        iFrame.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        iFrame.doLayout();
        iFrame.pack();
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
    }//GEN-LAST:event_jMenuItem35ActionPerformed

    private void jMenuItem19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem19ActionPerformed
        JInternalFrame iFrame = new JInternalFrame("Zuordnung Wetterdaten", true, true, true, true);
        iFrame.setSize(600, 320);
        iFrame.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        iFrame.getContentPane().add(new WeatherAssignmentEditor(this));
        iFrame.doLayout();
        iFrame.pack();
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
        try {
            iFrame.setSelected(true);
        } catch (PropertyVetoException ex) {
            logger.error("Error selecting internal frame", ex);
        }
    }//GEN-LAST:event_jMenuItem19ActionPerformed

    private void jMenuItem22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem22ActionPerformed
//        DegreeDayCalculation ddc = new DegreeDayCalculation(null, null, null, new DatasetWorkerFactory(this), 1);
//        for (int year = 2008; year <= 2012; year++) {
//            Double HGTSum = 0.0;
//            Double GTZSum = 0.0;
//            Long missingDays = 0L;
//
//            for (int month = 0; month <= 11; month++) {
//                DegreeDay hgt = ddc.getHGTFromRawData(year, month);
//                if (hgt == null || hgt.getDegreedays() == null) {
//                    HGTSum = null;
//                    GTZSum = null;
//                    break;
//                } else {
//                    HGTSum += hgt.getDegreedays();
//                    GTZSum += ddc.getGTZFromRawData(year, month).getDegreedays(); // if hgt could be calculated, gtz must also be possible because only the base is changing
//                    missingDays += ddc.getMissingDays();
//                }
//            }
//            if (HGTSum == null || GTZSum == null) {
//                System.out.println("Nicht berechenbar für" + " " + year);
//            } else {
//                System.out.println(year + ": " + HGTSum + " " + GTZSum + " Missing " + missingDays);
//            }
//        }
    }//GEN-LAST:event_jMenuItem22ActionPerformed

    private void jMenuItem30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem30ActionPerformed
        if (ClimateFactorReader.isClimateImportLocked()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CLIMATE_IMPORT_ONGOING") + "\n", true);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new XLS_FilenameFilter());
        chooser.setMultiSelectionEnabled(false);
        chooser.setVisible(true);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            if (file.exists()) {
                ClimateFactorReader reader = new ClimateFactorReader(file, this);
                reader.importFactors();
            } else {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FILE_NOT_FOUND") + "\n", true);
            }
        }
    }//GEN-LAST:event_jMenuItem30ActionPerformed

    private void closeAllItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeAllItemActionPerformed
        closeAllWindows();
    }//GEN-LAST:event_closeAllItemActionPerformed

    private void cascadeItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cascadeItemActionPerformed
        cascadeWindows();
    }//GEN-LAST:event_cascadeItemActionPerformed

    private void jMenuItem15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem15ActionPerformed
        tileAllWindows();
    }//GEN-LAST:event_jMenuItem15ActionPerformed

    private void ApplicationPropsMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ApplicationPropsMenuActionPerformed
        AppPrefsDialog appPrefsDialog = new AppPrefsDialog(this, this);
        appPrefsDialog.setLocationRelativeTo(this);
        appPrefsDialog.setVisible(true);
    }//GEN-LAST:event_ApplicationPropsMenuActionPerformed

    private void jMenuItem23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem23ActionPerformed
        FavoriteDialog d = new FavoriteDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        d.toFront();
    }//GEN-LAST:event_jMenuItem23ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        debugConsole.setVisible(true);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem6updateMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6updateMenuActionPerformed
        if (this.application.isTrial()) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_DB_UPDATE_IN_DEMO") + "\n", true);
            return;
        }

        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DB_UPDATE_ONGOING"), true);
        logger.info("Startet database update");
        if (new DBUpdater().update()) {
            logger.info("Database update completed successfully");
            Messages.showMessage("erfolgreich beendet" + "\n", true);
        } else {
            logger.error("Database update failed or canceled by user");
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("UPDATE_CANCELLED_OR_FAIL") + "\n", true);
        }
    }//GEN-LAST:event_jMenuItem6updateMenuActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        InfoDialog info = new InfoDialog(this, false, this.application.getVersion());
        info.setLocationRelativeTo(this);
        info.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem21ActionPerformed
        new WebBrowser("web.fbta.uni-karlsruhe.de/mantis/login_page.php").launch();
    }//GEN-LAST:event_jMenuItem21ActionPerformed

    private void jMenuItem18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem18ActionPerformed
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(0);
    }//GEN-LAST:event_jMenuItem18ActionPerformed

    private void jMenuItem20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem20ActionPerformed
        String graSel = storedChartsCombobox.getSelectedItem().toString();
        try {
            new GRAWriter().writeGRAFiletoDB(graSel, new FileInputStream(getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + this.application.getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER + System.getProperty("file.separator") + graSel + ".gra"));
        } catch (FileNotFoundException ex) {
            Messages.showException(ex);
        }
    }//GEN-LAST:event_jMenuItem20ActionPerformed

    private void testTriggerMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testTriggerMenuActionPerformed
        //    NewUnitDialog nud = new NewUnitDialog(this, true, "Hz", "xE01");
        //    nud.setVisible(true);
        //    nud.toFront();


        //    ClimateFactorReader cfr = new ClimateFactorReader();
        //    for (ClimateFactor cf : cfr.getClimateFactorsForPostCode(14467)) {
        //        System.out.println(cf.getFactor() + " " + cf.getStartdate().toString());
        //    }


        DegreeDayParameterDialog d = new DegreeDayParameterDialog(this, true);
        d.setLocationRelativeTo(this);
        d.setVisible(true);


        //    CN_Nametranslator.readNames();
        //    CN_Nametranslator.translate();
        //    CN_Nametranslator.write();

        //    AnnotationDesigner f = new AnnotationDesigner();
        //    desktopPanel.add(f);
        //    f.setVisible(true);
    }//GEN-LAST:event_testTriggerMenuActionPerformed

    private void connectToDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectToDBButtonActionPerformed
        showPasswordDialog();
    }//GEN-LAST:event_connectToDBButtonActionPerformed

    private void disconnectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectButtonActionPerformed
        this.application.disconnectFromDB();
    }//GEN-LAST:event_disconnectButtonActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        showExportDialog();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        cascadeWindows();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        closeAllWindows();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        mainSplitPane.setDividerLocation(1);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        mainSplitPane.setDividerLocation(490);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void storedChartsComboboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storedChartsComboboxActionPerformed
        if (this.application.isGUIActive() && storedChartsCombobox.getSelectedItem() != null) {
            this.application.loadStoredChartFromFile(storedChartsCombobox.getSelectedItem().toString(), null);
        }
    }//GEN-LAST:event_storedChartsComboboxActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        dateRangeDialog.setLocationRelativeTo(jButton10);
        dateRangeDialog.setVisible(true);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void openTreeSelectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openTreeSelectorButtonActionPerformed
        TreeSelectorDialog d = new TreeSelectorDialog(this, false);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }//GEN-LAST:event_openTreeSelectorButtonActionPerformed

    private void PDF_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PDF_ButtonActionPerformed
        JFreeChart theChart = getCurrentChart();

        if (theChart == null) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_CHART_SELECTED_NO_EXPORT") + "\n", true);
            return;
        }

        JFileChooser chooser = new JFileChooser(getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + this.application.getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
        final String suffix = ".pdf";
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE"));
        chooser.setDialogTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE_PDF"));
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(suffix);
            }

            @Override
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PDF_FILES");
            }
        });

        int state = chooser.showSaveDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            try {
                File file = chooser.getSelectedFile();
                // Falls nicht mit entsprechender Endung -> anhängen

                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    String s = chooser.getSelectedFile().getAbsolutePath() + ".pdf";
                    file = new File(s);
                }
                PDFGenerator pdfgen = new PDFGenerator();
                pdfgen.saveChartAsPDF(file, theChart, getActiveChartPanel().getWidth(), getActiveChartPanel().getHeight(), new DefaultFontMapper());

            } catch (IOException e) {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PDF_ERROR"), true);
                Messages.showException(e);

            }
        }
    }//GEN-LAST:event_PDF_ButtonActionPerformed

    private void PNG_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PNG_ButtonActionPerformed
        JFreeChart theChart = getCurrentChart();

        if (theChart == null) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_CHART_SELECTED_NO_EXPORT") + "\n", true);
            return;
        }
        JFileChooser chooser = new JFileChooser(
                getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + this.application.getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
        final String suffix = ".png";
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE"));
        chooser.setDialogTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE_PNG"));
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(suffix);
            }

            @Override
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PNG-Files");
            }
        });

        int state = chooser.showSaveDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // Falls nicht mit entsprechender Endung -> anhängen
            if (!file.getName().toLowerCase().endsWith(".png")) {
                String s = chooser.getSelectedFile().getAbsolutePath() + ".png";
                file = new File(s);
            }
            try {
                ChartUtilities.saveChartAsPNG(file, theChart, getActiveChartPanel().getWidth(), getActiveChartPanel().getHeight());
            } catch (IOException e) {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("PNG_ERROR"), true);
                Messages.showException(e);
            }
        }
    }//GEN-LAST:event_PNG_ButtonActionPerformed

    private void SVG_ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SVG_ButtonActionPerformed
        JFreeChart theChart = getCurrentChart();

        if (theChart == null) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_CHART_SELECTED_NO_EXPORT") + "\n", true);
            return;
        }

        JFileChooser chooser = new JFileChooser(getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + this.application.getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
        final String suffix = ".svg";
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.setApproveButtonText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE"));
        chooser.setDialogTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SAVE_SVG"));
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(suffix);
            }

            @Override
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SVG_FILES");
            }
        });

        int state = chooser.showSaveDialog(null);

        if (state == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            // Falls nicht mit entsprechender Endung -> anhängen

            if (!file.getName().toLowerCase().endsWith(".svg")) {
                String s = chooser.getSelectedFile().getAbsolutePath() + ".svg";
                file = new File(s);
            }
            SVGGenerator svggen = new SVGGenerator();
            svggen.saveChartAsSVG(file, theChart, getActiveChartPanel().getWidth(), getActiveChartPanel().getHeight());

        }
    }//GEN-LAST:event_SVG_ButtonActionPerformed

    private void mainFrameResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mainFrameResized
        consoleSplitPane.setDividerLocation(mainSplitPane.getHeight() - 110);
    }//GEN-LAST:event_mainFrameResized

    private void TabbedPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_TabbedPanelStateChanged
        StandardPlotTab previousPlot = this.plotTabManager.getShownPlotTab(); // lasttab ist der Tab von dem wir kommen
        int currentIndex = ((JTabbedPane) evt.getSource()).getSelectedIndex(); // currenttab ist der Tab der jetzt aktiv ist
        StandardPlotTab shownPlot = this.plotTabManager.getPlotTabAt(currentIndex);
        this.plotTabManager.clearIconFor(previousPlot);
        this.plotTabManager.showPlotTab(shownPlot);

        DateInterval previousInterval = previousPlot.getSelectedInterval();
        if (dateLock
                && (null != previousInterval)
                && (null != previousInterval.getStartDate())) {
            shownPlot.setSelectedInterval(previousInterval);
        }

        setRecycleDescriber(false);
    }//GEN-LAST:event_TabbedPanelStateChanged

    private void jMenuItem37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem37ActionPerformed
        iconifyWindows(true);
    }//GEN-LAST:event_jMenuItem37ActionPerformed

    private void jMenuItem38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem38ActionPerformed
        AnnotationDesigner f = new AnnotationDesigner();
        desktopPanel.add(f);
        f.setVisible(true);
    }//GEN-LAST:event_jMenuItem38ActionPerformed

    private void chechDataMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chechDataMenuItemActionPerformed
        DataCheckFrame frame = new DataCheckFrame(this);
        desktopPanel.add(frame);
        frame.setVisible(true);
        frame.toFront();
    }//GEN-LAST:event_chechDataMenuItemActionPerformed

    private void calcProtocolCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_calcProtocolCheckBoxActionPerformed
        Level level;
        if (calcProtocolCheckBox.isSelected()) {
            level = Level.TRACE;
        } else {
            level = Level.INFO;
        }
        MoniSoft.getInstance().getLogger().setLevel(level);
    }//GEN-LAST:event_calcProtocolCheckBoxActionPerformed

    private void jButton11help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.MAIN_FRAME.getPage());
    }//GEN-LAST:event_jButton11help
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem ApplicationPropsMenu;
    private javax.swing.JMenu BuildingCompMenu;
    private javax.swing.JMenu CalculationsMenu;
    private javax.swing.JMenu DBMenu;
    private javax.swing.JMenu ExtrasMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenu HelpMenu;
    private javax.swing.JMenuBar MenuBar1;
    private javax.swing.JMenuItem NewSensorMenu;
    private javax.swing.JMenuItem NewSensorMenu1;
    javax.swing.JButton PDF_Button;
    javax.swing.JButton PNG_Button;
    private javax.swing.JMenuItem ParameterMenuItem;
    javax.swing.JButton SVG_Button;
    public static javax.swing.JMenu SensorMenu;
    private javax.swing.JPanel StatusPanel;
    private javax.swing.JTabbedPane TabbedPanel;
    private javax.swing.JMenu WeatherMenu;
    private javax.swing.JMenu WindowMenu;
    private javax.swing.JLabel autoLabel;
    public static javax.swing.JCheckBoxMenuItem calcProtocolCheckBox;
    private javax.swing.JMenuItem cascadeItem;
    private javax.swing.JMenuItem chechDataMenuItem;
    private javax.swing.JMenuItem closeAllItem;
    private javax.swing.JMenuItem compareClassMenuItam;
    static javax.swing.JMenuItem connectItem;
    private javax.swing.JPanel connectLED;
    public static javax.swing.JLabel connectLabel;
    javax.swing.JButton connectToDBButton;
    private javax.swing.JPanel connectionInformationPanel;
    public static javax.swing.JMenuItem consistencyMenuItem;
    private static javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JSplitPane consoleSplitPane;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JMenuItem counterChangeMenuItem;
    private static javax.swing.JMenuItem csvImportMenu;
    private javax.swing.JMenuItem deleteDataMenuItem;
    private javax.swing.JDesktopPane desktopPanel;
    javax.swing.JButton disconnectButton;
    static javax.swing.JMenuItem disconnectItem;
    private javax.swing.JMenuItem eventReorganizeMenuItem;
    static javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenuItem factorChangeMenuItem;
    javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton21;
    javax.swing.JButton jButton3;
    javax.swing.JButton jButton4;
    javax.swing.JButton jButton5;
    javax.swing.JButton jButton6;
    javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem14;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem19;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem20;
    private javax.swing.JMenuItem jMenuItem21;
    private javax.swing.JMenuItem jMenuItem22;
    private javax.swing.JMenuItem jMenuItem23;
    private javax.swing.JMenuItem jMenuItem24;
    private javax.swing.JMenuItem jMenuItem25;
    private javax.swing.JMenuItem jMenuItem26;
    private javax.swing.JMenuItem jMenuItem27;
    private javax.swing.JMenuItem jMenuItem28;
    private javax.swing.JMenuItem jMenuItem29;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem30;
    private javax.swing.JMenuItem jMenuItem31;
    private javax.swing.JMenuItem jMenuItem32;
    private javax.swing.JMenuItem jMenuItem33;
    private javax.swing.JMenuItem jMenuItem34;
    private javax.swing.JMenuItem jMenuItem35;
    private javax.swing.JMenuItem jMenuItem37;
    private javax.swing.JMenuItem jMenuItem38;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator16;
    private javax.swing.JPopupMenu.Separator jSeparator17;
    private javax.swing.JPopupMenu.Separator jSeparator18;
    private javax.swing.JPopupMenu.Separator jSeparator19;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator20;
    private javax.swing.JPopupMenu.Separator jSeparator21;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JMenuItem keyWordEditorMenu;
    private javax.swing.JSplitPane mainSplitPane;
    javax.swing.JMenuItem manualMenuItem;
    private javax.swing.JButton openTreeSelectorButton;
    private javax.swing.JMenuItem qualityMenuItem;
    private javax.swing.JMenuItem readMonFileMenu;
    private javax.swing.JComboBox storedChartsCombobox;
    private javax.swing.JMenuItem testTriggerMenu;
    private javax.swing.JPanel toolBarPanel;
    // End of variables declaration//GEN-END:variables

    private void adjustSplitPaneHeight() {
        this.consoleSplitPane.setDividerLocation(this.mainSplitPane.getHeight() - 100);
    }

    private boolean isRecycleDescriber() {
        return recyleDescriber;
    }
    // TODO: Replace all other direct access with this method

    private Properties getApplicationProperties() {
        return this.application.getApplicationProperties();
    }

    /**
     * Schreibt den übergebenen String mit der Übergebenen Farbe in die Konsole
     *
     * @param s
     * @param c //
     */
    private synchronized void updateConsole(final Object... args) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                String s = "";
                Color c = Color.BLACK;

                for (Object o : args) {
                    switch (count) {
                        case 0:
                            s = (String) o;
                            break;
                        case 1:
                            c = (Color) o;
                            break;
                    }
                    count++;
                }
                getConsole().setForeground(c);
                getConsole().append(s);
                getConsole().setCaretPosition(getConsole().getDocument().getLength());
            }
        });
    }

    private void registerPlot(StandardPlotTab newPlot) {
        this.plotTabManager.addPlotTab(newPlot);
    }

    private boolean isLocked_Backupreconstruction() {
        return lock_Backupreconstruction;
    }

    private void showExportDialog() {
        Models models = new Models();
        // AZ: set to not modal - MONISOFT-8
        ExportDialog exportDialog = new ExportDialog(SensorInformation.getSensorList(), models.getAggIntervalComboBoxModel(), this, false, new DatasetWorkerFactory(this));
        exportDialog.setLocationRelativeTo(this);
        exportDialog.setVisible(true);
    }

    /**
     * Setzte den DragMode des Desktops nach den Anwendungseinstellungen
     */
    private void setDragMode() {
        // TODO: Implement better access to application properties
        if (MoniSoft.getInstance().getApplicationProperties().getProperty("OutlineDragMode").equals("1")) {
            desktopPanel.setDragMode(JDesktopPane.LIVE_DRAG_MODE);
        } else {
            desktopPanel.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
        }
    }

    private void loadCompareTableFavorite() {
        CompareTableFrame frame = new CompareTableFrame("Special interest", this);
        frame.startCalc(true);
        desktopPanel.add(frame);
        frame.setVisible(true);
        frame.moveToFront();
    }

    /**
     * SeriesCollections mit null-Werten vorbelegen sowie Standard-Farben
     * vorbelegen
     */
    private void resetSeriesCollections() {
        for (int i = 0; i < 14; i++) {
            for (StandardPlotTab plot : plotTabManager) {
                plot.resetCollections(i);
            }
        }
    }

    private void shiftFrameOrigin(int x, int y) {
        IFRAME_X_ORIGIN = x + MoniSoftConstants.IFRAME_ORIGIN_OFFSET;
        IFRAME_Y_ORIGIN = y + MoniSoftConstants.IFRAME_ORIGIN_OFFSET;
        if (IFRAME_Y_ORIGIN > 400) {
            IFRAME_X_ORIGIN = 20;
            IFRAME_Y_ORIGIN = 20;
        }
    }

    // TODO: If you want to change this method, there is functionality misplaced
    private boolean isGUI() {
        return true;
    }

    private CtrlChartPanel getActiveChartPanel() {
        JInternalFrame activeFrame = (JInternalFrame) desktopPanel.getSelectedFrame();
        if (desktopPanel.getSelectedFrame() == null) {
            return null;
        }
        CtrlChartPanel activeChartPanel;
        if (activeFrame.getContentPane().getComponent(0).getClass() == JPanel.class) {
            JPanel activePanel = (JPanel) activeFrame.getContentPane().getComponent(0);
            activeChartPanel = (CtrlChartPanel) activePanel.getComponent(0);
        } else {
            activeChartPanel = (CtrlChartPanel) activeFrame.getContentPane().getComponent(0);
        }
        return activeChartPanel;
    }

    private JFreeChart getCurrentChart() {
        if (desktopPanel.getSelectedFrame() == null) {
            System.out.println("NULL");
            return null;
        }
        ChartPanel activeChartPanel;
        try {
            JInternalFrame activeFrame = (JInternalFrame) desktopPanel.getSelectedFrame();

            if (activeFrame.getContentPane().getComponent(0).getClass() == JPanel.class) {
                JPanel activePanel = (JPanel) activeFrame.getContentPane().getComponent(0);
                activeChartPanel = (ChartPanel) activePanel.getComponent(0);
            } else {
                activeChartPanel = (ChartPanel) activeFrame.getContentPane().getComponent(0);
            }

            JFreeChart activeChart = activeChartPanel.getChart();

            return activeChart;

        } catch (Exception e) {
            Messages.showException(e);
            return null;
        }
    }

    private void displayInternalFrameWith(String titleKey, JPanel panel) {
        JInternalFrame iFrame = new JInternalFrame(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString(titleKey), true, true, true, true);
        iFrame.setLocation(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        shiftFrameOrigin(IFRAME_X_ORIGIN, IFRAME_Y_ORIGIN);
        iFrame.getContentPane().add(panel);
        iFrame.doLayout();
        iFrame.pack();
        iFrame.setVisible(true);
        desktopPanel.add(iFrame);
        iFrame.moveToFront();
        try {
            iFrame.setSelected(true);
        } catch (PropertyVetoException ex) {
            logger.error("Error selecting internal frame", ex);
        }
    }

    private void registerPlots() {
        registerPlot(new TimeSeriesPlotTab(this));
        registerPlot(new XYPlotTab(this));
        registerPlot(new CarpetPlotTab(this));
        registerPlot(new OgivePlotTab(this));
        registerPlot(new MaintenancePlotTab(this));
        registerPlot(new ComparePlotTab(this));
    }
}
