/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.base.charts;

import de.jmonitoring.Consistency.QualitySelectorPanel;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.Consistency.QualityOverviewPotMaker;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author togro
 */
public class QualityOverviewFrame extends JInternalFrame implements ActionListener {

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private final JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));// NOI18N
    private DateInterval dateInterval;
    private QualitySelectorPanel entryPanel = new QualitySelectorPanel();
    private QualityPlotWorker workerThread;
    private final JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL"));// NOI18N
    private final String createButtonText = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CREATE");// NOI18N
    private final String newSelectionButtonText = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NEW_SELECTION");// NOI18N
    private Integer tolerance;
    private JFreeChart chart;
    private QualityOverviewPotMaker myPlot;
    private final JList sensorSelectionList = new JList();
    private final JScrollPane selectorScrollPane = new JScrollPane();
    private CtrlChartPanel ctrlChartPanel;
    private final MainApplication gui;

    public QualityOverviewFrame(String title, boolean resizable, boolean closable, boolean maximizable, boolean iconifiable, MainApplication gui) {
        super(title, resizable, closable, maximizable, iconifiable);
        this.gui = gui;
        setPreferredSize(new Dimension(874, 500));
        setMinimumSize(new Dimension(874, 500));
        cancelButton.addActionListener(this);
        fillSensorSelectionList();


        entryPanel.getStartButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { //"Erzeugen" gedrückt

                if (entryPanel.getStartButton().getText().equals(createButtonText)) {
                    // Messpunkte ausgewählt?
                    if (getSelectedSensors().isEmpty()) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN KEINE MESSPUNKTE AUSGEWÄHLT."));// NOI18N
                        return;
                    }

                    // sind Datumswerte eingetragen?
                    if (entryPanel.jDateChooser1.getDate() == null || entryPanel.jDateChooser2.getDate() == null) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN KEINEN ZEITRAUM AUSGEWÄHLT."));// NOI18N
                        return;
                    }

                    // ist das Enddatum > Startdatum?
                    if (entryPanel.jDateChooser2.getDate().getTime() < entryPanel.jDateChooser1.getDate().getTime()) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DAS STARTDATUM IST SPÄTER ALS DAS ENDDATUM."));// NOI18N
                        return;
                    }

                    switch (entryPanel.toleranceSelector.getSelectedIndex()) {
                        case 0:
                            tolerance = 60;
                            break;
                        case 1:
                            tolerance = 120;
                            break;
                        case 2:
                            tolerance = 180;
                            break;
                        case 3:
                            tolerance = 300;
                            break;
                        case 4:
                            tolerance = 720;
                            break;
                        case 5:
                            tolerance = 1440;
                            break;
                        case 6:
                            tolerance = 2880;
                            break;
                        case 7:
                            tolerance = 10080;
                            break;
                    }
                    dateInterval = new DateInterval(entryPanel.jDateChooser1.getDate(), entryPanel.jDateChooser2.getDate());
                    workerThread = new QualityPlotWorker();
                    workerThread.setName("workerThread"); // NOI18N
                    workerThread.start();
                } else {
                    mainPanel.remove(ctrlChartPanel);
                    mainPanel.remove(cancelButton);
                    mainPanel.add(selectorScrollPane, BorderLayout.CENTER);
                    entryPanel.getStartButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
                    entryPanel.getStartButton().setText(createButtonText);
                    mainPanel.revalidate();
                    setSize(getWidth() + 1, getHeight());
                    setSize(getWidth() - 1, getHeight());
                    revalidate();
                }
            }
        });

        mainPanel.setBackground(Color.WHITE);
        selectorScrollPane.getViewport().add(sensorSelectionList);
        mainPanel.add(selectorScrollPane, BorderLayout.CENTER);
        mainPanel.add(entryPanel, BorderLayout.SOUTH);
        getContentPane().add(mainPanel);
        setSize(850, 450);
        this.setDoubleBuffered(false);
        this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        this.addInternalFrameListener(new InternalFrameListener() {
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                disposeMe();
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameIconified(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameDeiconified(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameActivated(InternalFrameEvent e) {
            }

            @Override
            public void internalFrameDeactivated(InternalFrameEvent e) {
            }
        });
        setVisible(true);
    }

    private void fillSensorSelectionList() {
        sensorSelectionList.setModel(new Models().getSensorListListModel());
    }

    private ArrayList<SensorProperties> getSelectedSensors() {
        return new ArrayList(Arrays.asList(sensorSelectionList.getSelectedValues()));
    }

    class QualityPlotWorker extends StoppableThread {

        @Override
        public void run() {
            if (dateInterval.getStartDate() != null && dateInterval.getEndDate() != null) {
                running = true;
                mainPanel.remove(selectorScrollPane);
                mainPanel.setBackground(Color.WHITE);
                mainPanel.add(waitLabel, BorderLayout.CENTER);
                entryPanel.getStartButton().setEnabled(false);
                mainPanel.add(cancelButton, BorderLayout.NORTH);
                setSize(getWidth() + 1, getHeight());
                setSize(getWidth() - 1, getHeight());
                mainPanel.revalidate();
                ArrayList<SensorProperties> selectedList = getSelectedSensors();

                myPlot = new QualityOverviewPotMaker(dateInterval, selectedList, tolerance, entryPanel.getMode(), QualityOverviewFrame.this.gui);
                if (running && myPlot.getPlot() != null) {
                    chart = new JFreeChart(myPlot.getPlot());
                    chart.setAntiAlias(false);
                    chart.setNotify(true);

                    if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {// NOI18N
                        drawChartStamp();
                    }

                    chart.removeLegend();
                    ctrlChartPanel = new CtrlChartPanel(chart, false, QualityOverviewFrame.this.gui);
                    ctrlChartPanel.setMaximumDrawHeight(2000);
                    ctrlChartPanel.setMaximumDrawWidth(2000);
                    ctrlChartPanel.setMouseWheelEnabled(true);

                    ctrlChartPanel.addChartMouseListener(new ChartMouseListener() {
                        XYItemEntity entity;
                        XYPlot plot;
                        XYIntervalSeriesCollection dset;
                        XYIntervalSeries series;
                        XYBarRenderer renderer;
                        int selectedIndex = -1;
                        int missHL = -1;
                        int warningHL = -1;
                        int okHL = -1;
                        Paint lastOKColor;
                        Paint lastWarningColor;
                        Paint lastMissColor;
                        Color newOKColor = new Color(7, 120, 0);
                        Color newWarningColor = new Color(220, 110, 0);
                        Color newMissingColor = new Color(155, 0, 0);
                        String selectedSensor;

                        @Override
                        public void chartMouseClicked(ChartMouseEvent event) {
                            try {
                                entity = (XYItemEntity) event.getEntity();
                                plot = (XYPlot) event.getChart().getPlot();
                                dset = (XYIntervalSeriesCollection) plot.getDataset();
                                series = dset.getSeries(entity.getSeriesIndex());
                                Range range = plot.getDomainAxis().getRange();
                                DateInterval d = new DateInterval(new Date((long) range.getLowerBound()), new Date((long) range.getUpperBound()));
                                selectedSensor = series.getKey().toString();
                                selectedSensor = selectedSensor.replace("%", "");// NOI18N
                                selectedSensor = selectedSensor.replace("#", "");// NOI18N
                                QualityOverviewFrame.this.gui.showMaintenanceChart(d, Integer.parseInt(selectedSensor), false);
                            } catch (ClassCastException e) {
                            }
                        }

                        @Override
                        public void chartMouseMoved(ChartMouseEvent event) {

                            try {
                                if (okHL != -1) {
                                    renderer.setSeriesPaint(okHL, lastOKColor);
                                }
                                if (warningHL != -1) {
                                    renderer.setSeriesPaint(warningHL, lastWarningColor);
                                }
                                if (missHL != -1) {
                                    renderer.setSeriesPaint(missHL, lastMissColor);
                                }

//                                if (event.getEntity().getClass().equals(ChartEntity.class)) {
                                entity = (XYItemEntity) event.getEntity();
                                plot = (XYPlot) event.getChart().getPlot();
                                dset = (XYIntervalSeriesCollection) plot.getDataset();
                                series = dset.getSeries(entity.getSeriesIndex());
                                renderer = (XYBarRenderer) plot.getRenderer();
                                selectedIndex = entity.getSeriesIndex();


                                if (selectedIndex % 3 == 0) {        // Missing selektiert
                                    missHL = selectedIndex;
                                    warningHL = selectedIndex + 1;
                                    okHL = selectedIndex + 2;
                                } else if (selectedIndex % 3 == 1) { // Warning selektiert
                                    missHL = selectedIndex - 1;
                                    warningHL = selectedIndex;
                                    okHL = selectedIndex + 1;
                                } else {                             // OK selektiert
                                    missHL = selectedIndex - 2;
                                    warningHL = selectedIndex - 1;
                                    okHL = selectedIndex;
                                }

                                lastOKColor = renderer.getSeriesPaint(okHL);
                                lastWarningColor = renderer.getSeriesPaint(warningHL);
                                lastMissColor = renderer.getSeriesPaint(missHL);
                                renderer.setSeriesPaint(okHL, newOKColor);
                                renderer.setSeriesPaint(warningHL, newWarningColor);
                                renderer.setSeriesPaint(missHL, newMissingColor);
//                                }
                            } catch (NullPointerException e) {
                                // nix machen
                            } catch (ClassCastException e) {
                                // nix machen
                            }
                        }
                    });

                    // Panels bauen
                    mainPanel.remove(waitLabel);
                    mainPanel.remove(cancelButton);
                    mainPanel.setBackground(Color.WHITE);
                    mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);
                    entryPanel.getStartButton().setEnabled(true);
                    entryPanel.getStartButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow-turn-180.png"))); // NOI18N
                    entryPanel.getStartButton().setText(newSelectionButtonText);
                    setSize(getWidth() + 1, getHeight());
                    setSize(getWidth() - 1, getHeight());
                    mainPanel.revalidate();
                } else { // Abgebrochen
                    running = false;
                    workerThread = null;
                    mainPanel.remove(waitLabel);
                    mainPanel.remove(cancelButton);
                    mainPanel.add(selectorScrollPane, BorderLayout.CENTER);
                    entryPanel.getStartButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
                    entryPanel.getStartButton().setText(createButtonText);
                    entryPanel.getStartButton().setEnabled(true);
                    entryPanel.revalidate();
                    setSize(getWidth() + 1, getHeight());
                    setSize(getWidth() - 1, getHeight());
                    mainPanel.revalidate();
                }
            } else {
                mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FEHLERHAFTE DATUMSANGABEN"), JLabel.CENTER)); // NOI18N
                mainPanel.revalidate();
            }
        }
    }

    /**
     * Action when cancel buttonis pressed
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        workerThread.running = false;
        workerThread = null;
        mainPanel.remove(waitLabel);
        mainPanel.remove(cancelButton);
        mainPanel.add(selectorScrollPane, BorderLayout.CENTER);
        entryPanel.getStartButton().setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        entryPanel.getStartButton().setText(createButtonText);
        entryPanel.getStartButton().setEnabled(true);
        entryPanel.revalidate();
        setSize(getWidth() + 1, getHeight());
        setSize(getWidth() - 1, getHeight());
        mainPanel.revalidate();
    }

    private void drawChartStamp() {
        // Bildstempel
        TextTitle tt = new TextTitle(new Date().toString() + "   " + MoniSoft.getInstance().getDBConnector().getUserName() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QualityOverviewFrame.AN") + " " + MoniSoft.getInstance().getDBConnector().getDBName(), new Font("Dialog", Font.PLAIN, 9)); // NOI18N
        tt.setPosition(RectangleEdge.BOTTOM);
        tt.setPaint(Color.GRAY);
        tt.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        tt.setMargin(0.0, 0.0, 1.0, 1.0);
        chart.addSubtitle(tt);
        //Legende
        LegendTitle legend = new LegendTitle(myPlot.getPlot());
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setBackgroundPaint(Color.WHITE);
        legend.setFrame(new BlockBorder(Color.GRAY));
        chart.addSubtitle(legend);
        chart.removeLegend();
    }

    private void disposeMe() {
        if (myPlot != null) {
            myPlot.getPlot().setDataset(null);
            workerThread = null;
            myPlot = null;
        }
        if (chart != null) {
            chart = null;
        }
        this.gui.disposeIFrame(this);
    }
}
