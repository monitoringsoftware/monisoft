/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.StoppableThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;

/**
 * Create a InternalFrame with a time series plot
 *
 * @author togro
 */
public class TimeSeriesChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private StoppableThread stoppThread;
    private JFreeChart chart;
    private TimeSeriesPlotMaker myPlot;
    private CombinedDomainXYPlot combinedPlot;
    private final TimeSeriesChartDescriber describer;
    private final MainApplication gui;

    public TimeSeriesChart(TimeSeriesChartDescriber d, MainApplication gui) {
        super(d.getInternalFrameTitle(), true, true, true, true);
        describer = d;
        this.gui = gui;

        if (MoniSoft.getInstance().isGUI()) { // Wenn aus der Oberfläche gestartet
            JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));
            JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainPanel.removeAll();
                    mainPanel.setBackground(Color.WHITE);
                    mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCELLED"), JLabel.CENTER));
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
            setSize(500, 350);
            this.setDoubleBuffered(false);
            this.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
            this.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    disposeMe();
                }
            });
            setVisible(true);
        }

        // Berechnug starten
        start();
    }

    private void start() {
        stoppThread = new StoppableThread(this);
        stoppThread.start();
        if (!MoniSoft.getInstance().isGUI()) {
            try {
                stoppThread.join();
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public void run() {
        stoppThread.running = true;
        myPlot = new TimeSeriesPlotMaker(describer, progress, TimeSeriesChart.this.gui); // Plot erzeugen
        if (!(stoppThread == null) && stoppThread.running) {
//            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("IN_PROGRESS") + "\n",true);
            combinedPlot = new CombinedDomainXYPlot(myPlot.getDomainAxis());
            combinedPlot.setRangePannable(true);
            combinedPlot.setDomainPannable(true);
            // Beide Plots nicht null
            if (myPlot.getMainPlot() != null && myPlot.getEventPlot() != null) {
                if (MoniSoft.getInstance().isGUI() && !describer.isMarkMissing() && myPlot.getMainPlot().getDomainMarkers(Layer.FOREGROUND) != null) { // Falls nicht gewünscht DomainMarker loeschen
                    myPlot.getMainPlot().clearDomainMarkers();
                }
                combinedPlot.add(myPlot.getMainPlot(), 4);
                combinedPlot.add(myPlot.getEventPlot(), 1);
                combinedPlot.setGap(10.0);
            } else if (myPlot.getEventPlot() != null) { // nur MainPlot ist null
                combinedPlot.add(myPlot.getEventPlot());
            } else if (myPlot.getMainPlot() != null) {  // nur EventPlot ist null
                if (MoniSoft.getInstance().isGUI() && !describer.isMarkMissing() && myPlot.getMainPlot().getDomainMarkers(Layer.FOREGROUND) != null) { // Falls nicht gewünscht DomainMarker loeschen
                    ArrayList<IntervalMarker> list = new ArrayList(myPlot.getMainPlot().getDomainMarkers(Layer.FOREGROUND));
                    Iterator<IntervalMarker> it = list.iterator();
                    IntervalMarker marker;
                    while (it.hasNext()) {
                        marker = it.next();
                        if (((IntervalMarker) marker).getPaint() == Color.YELLOW) {
                            myPlot.getMainPlot().removeDomainMarker((IntervalMarker) marker);
                        }
                    }

//                    myPlot.getMainPlot().clearDomainMarkers();
                }
                combinedPlot.add(myPlot.getMainPlot());
            } else { // beide null -  nichts zu plotten
                disposeMe();
            }


            // Plot(s) in ein ChartPanel einbauen, Titel aus describer verwenden
            // Die Titel werden im Plotmaker erzeugt, und dem decriber zugewiesen
            chart = new JFreeChart(describer.getPlotTitle(), JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);

            // Untertitel
            TextTitle subtitle = (describer.getPlotSubtitle() == null) ? new TextTitle("") : new TextTitle(describer.getPlotSubtitle());
            subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));
            chart.addSubtitle(subtitle);

            chart.setAntiAlias(MoniSoft.getInstance().getApplicationProperties().getProperty("UseAntiAliasing").equals("1"));
            chart.setNotify(true);

            if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {
                drawChartStamp();
            }

            printChartRemark();

            if (MoniSoft.getInstance().isGUI()) {
                final CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, true, describer, this.gui);
                ctrlChartPanel.setMouseWheelEnabled(true);
                ctrlChartPanel.setMaximumDrawHeight(2000);
                ctrlChartPanel.setMaximumDrawWidth(2000);

                File file = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
                if (file.exists()) {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(file);
                } else {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(null);
                }

                mainPanel.removeAll();
                mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);
                if (myPlot.hasLeftYAxis) {
                    AxisControl leftScalePanel = new AxisControl(AxisControl.LEFT, this.gui);
                    mainPanel.add(leftScalePanel, BorderLayout.WEST);
                }
                if (myPlot.hasRightYAxis) {
                    AxisControl rightScalePanel = new AxisControl(AxisControl.RIGHT, this.gui);
                    mainPanel.add(rightScalePanel, BorderLayout.EAST);
                }
                this.revalidate();
                try {
                    this.setSelected(true);
                } catch (PropertyVetoException ex) {
                    Messages.showException(ex);
                    Messages.showException(ex);
                }
            }
        } else {
            disposeMe();
        }
    }

    private void printChartRemark() {
        TextTitle tt = new TextTitle(myPlot.getRemark(), new Font("Dialog", Font.PLAIN, 9));
        tt.setPosition(RectangleEdge.BOTTOM);
        tt.setPaint(Color.GRAY);
        tt.setHorizontalAlignment(HorizontalAlignment.LEFT);
        tt.setMargin(0.0, 5.0, 1.0, 1.0);
        tt.setTextAlignment(HorizontalAlignment.LEFT);
        describer.setChartRemarkTitle(tt);

        if (describer.isShowRemarks() == null || describer.isShowRemarks()) {
            chart.addSubtitle(tt);
        }
    }

    private void drawChartStamp() {
        // Bildstempel
        TextTitle tt = new TextTitle(new Date().toString() + "   " + MoniSoft.getInstance().getDBConnector().getUserName() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ON") + " " + MoniSoft.getInstance().getDBConnector().getDBName(), new Font("Dialog", Font.PLAIN, 9));
        tt.setPosition(RectangleEdge.BOTTOM);
        tt.setPaint(Color.GRAY);
        tt.setHorizontalAlignment(HorizontalAlignment.RIGHT);
        tt.setMargin(0.0, 0.0, 1.0, 1.0);
        chart.addSubtitle(tt);

        //Legende wenn ein Hauptplot vorhanden, sonst nur Beschriftung der y-Achse
        if (myPlot.getMainPlot() != null) {
            LegendTitle legend = new LegendTitle(myPlot.getMainPlot());
            legend.setPosition(RectangleEdge.BOTTOM);
            legend.setBackgroundPaint(Color.WHITE);
            legend.setFrame(new BlockBorder(Color.GRAY));
//            describer.setLegendTitle(legend);

            if (describer.isShowLegend() == null || describer.isShowLegend()) {                
                chart.addSubtitle(legend);
            }
        }
        chart.removeLegend();
    }

    private void disposeMe() {
        if (myPlot != null) {
            if (myPlot.getMainPlot() != null) {
                myPlot.getMainPlot().setDataset(null);
            }
            if (myPlot.getEventPlot() != null) {
                myPlot.getEventPlot().setDataset(null);
            }
        }
        stoppThread = null;
        chart = null;
        this.gui.disposeIFrame(this);
    }

    /**
     * Liefert den erzeugten Chart zurück
     *
     * @return
     */
    public JFreeChart getChart() {
        return chart;
    }
}
