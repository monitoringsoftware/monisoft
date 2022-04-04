/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.windPlot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class WindChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private DateInterval dateInterval;
    private List<WindSeriesLooks> collection;
    private StoppableThread stoppThread;
    private JFreeChart chart;
    private WindPlotMaker myPlot;
    private final WindChartDescriber describer;
    private final MainApplication gui;

    public WindChart(WindChartDescriber d, MainApplication gui) {
        super(d.getInternalFrameTitle(), true, true, true, true);
        describer = d;
        this.gui = gui;
        this.dateInterval = describer.getDateInterval();
        this.collection = describer.getchartCollection();
        if (MoniSoft.getInstance().isGUI()) {
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
            setSize(500, 350);
            this.setTitle("WindPlot " + SensorInformation.getDisplayName(collection.get(0).getSensorID()));// NOI18N
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
//                MoniSoft.getInstance().fillPanelFromDescriber(describer,false);
                }

                @Override
                public void internalFrameDeactivated(InternalFrameEvent e) {
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
        SimpleDateFormat HumanDateFormat = new SimpleDateFormat(MoniSoftConstants.HumanDateFormat);

        // Aus der Collection und dem Zeitraum einen Plot erstellen
        myPlot = new WindPlotMaker(describer);

        chart = new JFreeChart(describer.getPlotTitle(), JFreeChart.DEFAULT_TITLE_FONT, myPlot.getPlotInfo(), true);
        TextTitle subtitle = new TextTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + dateInterval.getStartDateString(HumanDateFormat) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + dateInterval.getEndDateString(HumanDateFormat));// NOI18N
        subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));// NOI18N
        chart.addSubtitle(subtitle);
        chart.setAntiAlias(false);
        chart.setNotify(true);

        if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {// NOI18N
            drawChartStamp();
        }

        chart.removeLegend();

        if (MoniSoft.getInstance().isGUI()) {
            final CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, false, describer, this.gui);
            ctrlChartPanel.setMouseWheelEnabled(true);
            ctrlChartPanel.setMaximumDrawHeight(2000);
            ctrlChartPanel.setMaximumDrawWidth(2000);
            File file = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);// NOI18N
            if (file.exists()) {
                ctrlChartPanel.setDefaultDirectoryForSaveAs(file);
            } else {
                ctrlChartPanel.setDefaultDirectoryForSaveAs(null);
            }

            mainPanel.removeAll();
            mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);
            AxisControl leftScalePanel = new AxisControl(AxisControl.LEFT, this.gui);
            mainPanel.add(leftScalePanel, BorderLayout.WEST);
            this.revalidate();
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
        LegendTitle legend = new LegendTitle(myPlot.getPlotInfo());
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setBackgroundPaint(Color.WHITE);
        legend.setFrame(new BlockBorder(Color.GRAY));
//        describer.setLegendTitle(legend);

        if (describer.isShowLegend() == null || describer.isShowLegend()) {
            chart.addSubtitle(legend);
        }

        chart.removeLegend();
    }

    private void disposeMe() {
        if (myPlot != null) {
            myPlot.getPlotInfo().setDataset(null);
        }
        chart = null;
        stoppThread = null;
        collection = null;
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
