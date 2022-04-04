package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.standardPlots.scatterPlot.ScatterPlotMaker;
import de.jmonitoring.utils.StoppableThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 * Create a InternalFrame with a scatter plot
 *
 * @author togro
 */
public class ScatterPlotChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private StoppableThread stoppThread;
    private final ScatterChartDescriber describer;
    private JFreeChart chart;
    private ScatterPlotMaker myPlot;
    private final MainApplication gui;

    public ScatterPlotChart(ScatterChartDescriber d, MainApplication gui) {
        super(d.getInternalFrameTitle(), true, true, true, true);
        describer = d;
        this.gui = gui;

        if (MoniSoft.getInstance().isGUI()) {
            JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));
            JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainPanel.removeAll();
                    mainPanel.setBackground(Color.WHITE);
                    mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CANCEL_CLEANUP"), JLabel.CENTER));
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
        myPlot = new ScatterPlotMaker(describer, new DatasetWorkerFactory(this.gui)); // TODO Aggregationsintervall anpassen

        if (stoppThread.running) {
            chart = new JFreeChart(describer.getPlotTitle(), JFreeChart.DEFAULT_TITLE_FONT, myPlot.getPlotInfo(), true);

            TextTitle subtitle = new TextTitle(describer.getPlotSubtitle());
            subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));
            chart.addSubtitle(subtitle);

            chart.setAntiAlias(false);
            chart.setNotify(true);
            if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {
                drawChartStamp();
            }

            printChartRemark();

            if (MoniSoft.getInstance().isGUI()) {
                CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, true, describer, this.gui);
                ctrlChartPanel.setMouseWheelEnabled(true);
                ctrlChartPanel.setMaximumDrawHeight(2000);
                ctrlChartPanel.setMaximumDrawWidth(2000);
                ctrlChartPanel.setTooltipLabelMap(myPlot.getLabelmap());
                File file = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
                if (file.exists()) {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(file);
                } else {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(null);
                }

                mainPanel.removeAll();
                mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);
                AxisControl leftScalePanel = new AxisControl(AxisControl.LEFT, this.gui);
                mainPanel.add(leftScalePanel, BorderLayout.WEST);

                // Im Moment nur eine y-Achse
//            AxisControl rightScalePanel = new AxisControl(AxisControl.RIGHT);
//            mainPanel.add(rightScalePanel, BorderLayout.EAST);

                this.revalidate();
            }
        }
    }

    private void printChartRemark() {
        TextTitle tt = new TextTitle(myPlot.getRemark(), new Font("Dialog", Font.PLAIN, 9));
        tt.setPosition(RectangleEdge.BOTTOM);
        tt.setPaint(Color.GRAY);
        tt.setHorizontalAlignment(HorizontalAlignment.LEFT);
        tt.setTextAlignment(HorizontalAlignment.LEFT);
        tt.setMargin(0.0, 5.0, 1.0, 1.0);
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


        //Legende
        LegendTitle legend = new LegendTitle(myPlot.getPlotInfo());
        legend.setPosition(RectangleEdge.BOTTOM);
        legend.setBackgroundPaint(Color.WHITE);
        legend.setFrame(new BlockBorder(Color.GRAY));
//        describor.setLegendTitle(legend);

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
