/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.comparePlot;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.StoppableThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import javax.swing.*;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * Create a InternalFrame with a compare plot
 *
 * @author togro
 */
public class CompareChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private StoppableThread stoppThread;
    private JFreeChart chart;
    private ComparePlotMaker myPlot;
    private final CompareChartDescriber desc;
    private final MainApplication gui;

    public CompareChart(CompareChartDescriber describer, MainApplication gui) {
        super(describer.getInternalFrameTitle(), true, true, true, true);
        this.desc = describer;
        this.gui = gui;

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
            this.setTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft.CompareTab.TabConstraints.tabTitle") + " " + describer.getchartCollection().get(0).getSensor());// NOI18N
            this.setDoubleBuffered(false);
            this.setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
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
        myPlot = new ComparePlotMaker(desc, new DatasetWorkerFactory(this.gui)); // TODO Aggregationsintervall anpassen
//        myPlot = new ComparePlotMaker(collection, this.timePeriods, category, ""); // TODO Aggregationsintervall anpassen

        chart = new JFreeChart(desc.getPlotTitle(), JFreeChart.DEFAULT_TITLE_FONT, myPlot.getPlotInfo(), true);
        chart.setAntiAlias(false);
        chart.setNotify(true);

        if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {// NOI18N
            drawChartStamp();
        }

        if (MoniSoft.getInstance().isGUI()) {
            final CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, false, desc, this.gui);
//
            ctrlChartPanel.setMaximumDrawHeight(2000);
            ctrlChartPanel.setMaximumDrawWidth(2000);
            File file = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);// NOI18N
            if (file.exists()) {
                ctrlChartPanel.setDefaultDirectoryForSaveAs(file);
            } else {
                ctrlChartPanel.setDefaultDirectoryForSaveAs(null);
            }
            ctrlChartPanel.setMouseWheelEnabled(true);

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
//        desc.setLegendTitle(legend);

        if (desc.isShowLegend() == null || desc.isShowLegend()) {
            chart.addSubtitle(legend);
        }

        chart.removeLegend();
    }

    private void disposeMe() {
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
