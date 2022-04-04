/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.maintenancePlot;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.Components.MaintenanceNaviPanel;
import de.jmonitoring.TableModels.MaintenanceTableModel;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.cellEditors.NumberCellEditor;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.tablecellrenderer.DoubleCellRenderer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.ui.*;

/**
 *
 * @author togro
 */
public class MaintenanceChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private DateInterval dateInterval;
    private MaintenancePlot myPlot;
    private StoppableThread stoppThread;
    private JFreeChart chart;
    private long keepTime = -1;
    private Integer sensorID;
    private final MainApplication gui;
    private boolean markNegative;
    BasicStroke stroke = new BasicStroke(1f);

    public MaintenanceChart(String title, DateInterval dateInterval, int sensorID, MainApplication gui, boolean markNegative) {
        super(title, true, true, true, true);
        this.sensorID = sensorID;
        this.dateInterval = dateInterval;
        this.gui = gui;
        this.markNegative = markNegative;
        JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif"))); // NOI18N
        JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL"));// NOI18N
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.removeAll();
                mainPanel.setBackground(Color.WHITE);
                mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL_CLEANUP"), JLabel.CENTER));// NOI18N
                mainPanel.revalidate();
                stoppThread.running = false;
                disposeMe();
            }
        });
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(waitLabel);
        mainPanel.add(progress, BorderLayout.SOUTH);
        mainPanel.add(cancelButton, BorderLayout.NORTH);
        getContentPane().add(mainPanel);
        setSize(810, 430);
        setDoubleBuffered(false);
        setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
        this.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                disposeMe();
            }
        });
        setVisible(true);

        // Berechnug starten
        start();
    }

    private void start() {
        stoppThread = new StoppableThread(this);
        stoppThread.start();
    }

    @Override
    public void run() {
        MaintenanceNaviPanel maintenancePanel = null;
        stoppThread.running = true;
        myPlot = new MaintenancePlot(dateInterval, sensorID, progress);
        if (stoppThread.running) {
            chart = new JFreeChart(SensorInformation.getDisplayName(sensorID), JFreeChart.DEFAULT_TITLE_FONT, myPlot.getPlot(), true);
            chart.setAntiAlias(false);
            chart.setNotify(true);

            if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {// NOI18N
                drawChartStamp();
            }

            chart.removeLegend();
            chart.addProgressListener(new ChartProgressListener() {
                @Override
                public void chartProgress(ChartProgressEvent event) {

                    if (event.getType() != ChartProgressEvent.DRAWING_FINISHED) {
                        return;
                    }

                    JPanel activePanel = (JPanel) getContentPane().getComponent(0);
                    ChartPanel activeChartPanel = (ChartPanel) activePanel.getComponent(0);
                    XYPlot plot = (XYPlot) activeChartPanel.getChart().getPlot();

                    long x = (long) plot.getDomainCrosshairValue();
                    if (x == keepTime) { // wenn sich der status des Crosshairs nicht geändert hat abbrechen
                        return;
                    }
                    keepTime = x;

                    Date date = new Date();
                    date.setTime(x);

                    JPanel dashPanel = (JPanel) activePanel.getComponent(1);
                    JScrollPane spane = (JScrollPane) dashPanel.getComponent(0);
                    JTable tab = (JTable) spane.getViewport().getComponent(0);
                    MaintenanceTableModel tm = (MaintenanceTableModel) tab.getModel();
                    TimeSeriesCollection seriesCollection = (TimeSeriesCollection) plot.getDataset();
                    TimeSeries series = (TimeSeries) seriesCollection.getSeries(0);
                    plot.getRangeAxis().getLabel();
                    TimeSeriesDataItem item = series.getDataItem(new Second(date));
                    Integer index = series.getIndex(new Second(date));
                    ArrayList<ValueMarker> markerList = new ArrayList<ValueMarker>();
                    Number y;

                    try {
                        markerList.addAll(plot.getDomainMarkers(1, Layer.BACKGROUND));
                    } catch (Exception e) {
                    }

                    int i = index;
                    TimeSeriesDataItem preitem = null;
                    Long fms;
                    boolean keep;
                    while (i > 0) {
                        keep = true;
                        i--;
                        fms = series.getDataItem(i).getPeriod().getFirstMillisecond();
                        for (ValueMarker marker : markerList) {
                            if (fms == marker.getValue()) {
                                keep = false;
                            }
                        }
                        if (keep) {
                            preitem = series.getDataItem(i);
                            break;
                        }
                    }

                    if ((item != null)) {
                        y = item.getValue();
                        tm.setValueAt(date.getTime(), 0, 0);
                        tm.setValueAt(y, 0, 1);
                        tm.setValueAt(plot.getRangeAxis().getLabel(), 0, 3);
                        if (preitem != null) {
                            tm.setValueAt((Double) y - (Double) preitem.getValue(), 0, 2);
                        } else {
                            tm.setValueAt(null, 0, 2);
                        }
                    }
                }
            });

            CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, false, this.gui);
            ctrlChartPanel.setMaximumDrawHeight(2000);
            ctrlChartPanel.setMaximumDrawWidth(2000);
            ctrlChartPanel.setMouseWheelEnabled(true);
            ctrlChartPanel.removeNotify();

            // Panels bauen
            mainPanel.removeAll();
            mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);

            final MaintenanceTableModel mModel = new MaintenanceTableModel(1);
            mModel.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getColumn() == 1) {
                        XYPlot plot = myPlot.getPlot();
                        TimeSeriesCollection seriesCollection = (TimeSeriesCollection) plot.getDataset();
                        TimeSeries series = (TimeSeries) seriesCollection.getSeries(0);
                        Date date = new Date((long) plot.getDomainCrosshairValue());
                        Second period = new Second(date);
                        if (((Double) mModel.getValueAt(0, 1)).equals((Double) series.getValue(period))) {
                            series.update(new Second(date), (Double) mModel.getValueAt(0, 1));
                        }
                    }
                }
            });

            JTable mtable = new JTable(mModel);
            TableCellRenderer renderer1 = new DateCellRenderer(new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat));
            TableCellRenderer renderer2 = new DoubleCellRenderer("0.000");// NOI18N
            mtable.getColumnModel().getColumn(0).setCellRenderer(renderer1);
            mtable.getColumnModel().getColumn(1).setCellRenderer(renderer2);
            mtable.getColumnModel().getColumn(2).setCellRenderer(renderer2);
            mtable.getColumnModel().getColumn(1).setCellEditor(new NumberCellEditor());
            JScrollPane tablePane = new JScrollPane(mtable);
            tablePane.setPreferredSize(new Dimension(400, 35));
            JPanel dashBoard = new JPanel(new BorderLayout());
            dashBoard.add(tablePane, BorderLayout.SOUTH);
            maintenancePanel = new MaintenanceNaviPanel(this.gui);
            dashBoard.add(maintenancePanel, BorderLayout.CENTER);
            dashBoard.setBorder(new LineBorder(Color.GRAY));
            mainPanel.add(dashBoard, BorderLayout.SOUTH);
            AxisControl leftScalePanel = new AxisControl(AxisControl.LEFT, this.gui);
            mainPanel.add(leftScalePanel, BorderLayout.WEST);
            this.revalidate();
        }
        if (markNegative && maintenancePanel != null) {
            markAllNegativeCounterValues(maintenancePanel);
        }
    }

    private void drawChartStamp() {
        // Bildstempel
        TextTitle tt = new TextTitle(new Date().toString() + "   " + MoniSoft.getInstance().getDBConnector().getUserName() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ON") + " " + MoniSoft.getInstance().getDBConnector().getDBName(), new Font("Dialog", Font.PLAIN, 9));// NOI18N
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
            myPlot = null;
        }
        chart = null;
        stoppThread = null;
        chart = null;
        this.gui.disposeIFrame(this);
    }

    private void markAllNegativeCounterValues(MaintenanceNaviPanel maintenancePanel) {
        TimeSeriesCollection seriesCollection = (TimeSeriesCollection) myPlot.getPlot().getDataset();
        TimeSeries series = (TimeSeries) seriesCollection.getSeries(0);
        
        Double keepValue = null;
        boolean markOn = false;
        Double currentValue;
        for (int i = 0; i <= series.getItemCount() - 1; i++) {
            currentValue = series.getValue(i).doubleValue();
            if (keepValue == null) {
                keepValue = currentValue;
                continue;
            }
            
            // AZ: Erkenne Ausreißer nach unten markiere sie - MONISOFT-7         
            if( keepValue > currentValue && 
                i < series.getItemCount() -1 &&
                keepValue > series.getValue(i+1).doubleValue() )
            {
                maintenancePanel.setIgnoreMarker(series.getTimePeriod(i-1).getFirstMillisecond(), myPlot.getPlot());
                keepValue = currentValue;
                markOn = false;
            }
            else if (keepValue > currentValue) {
                System.out.println("Ignore " + currentValue + " i = " + i );
                maintenancePanel.setIgnoreMarker(series.getTimePeriod(i).getFirstMillisecond(), myPlot.getPlot());
                markOn = true;
            } else {
                keepValue = currentValue;
                markOn = false;
            }
        }
    }
}
