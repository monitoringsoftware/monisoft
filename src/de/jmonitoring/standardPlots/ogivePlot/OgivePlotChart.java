/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.standardPlots.ogivePlot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameEvent;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYDrawableAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Drawable;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

import de.jmonitoring.Components.AxisControl;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.CircleDrawer;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import javax.swing.event.InternalFrameAdapter;

/**
 * A class creating a internal frame with an ogive chart
 *
 * @author togro
 */
public class OgivePlotChart extends JInternalFrame implements Runnable {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JProgressBar progress = new JProgressBar();
    private DateInterval dateInterval;
    private List<OgiveSeriesLooks> collection;
    private StoppableThread stoppThread;
    private int missingHours;
    private OgivePlotMaker myPlot;
    private JFreeChart chart;
    private final OgiveChartDescriber describer;
    private final MainApplication gui;

    /**
     * Create a new frame with a chart defined by the given chartdescriber
     * @param d The describer
     * @param gui The calling GUI
     */
    public OgivePlotChart(OgiveChartDescriber d, MainApplication gui) {
        super(d.getInternalFrameTitle(), true, true, true, true); //    public OgivePlotChart(String title, DateInterval dateInterval, ArrayList collection) {
        describer = d;
        this.gui = gui;
        this.dateInterval = describer.getDateInterval();
        this.collection = describer.getchartCollection();

        if (MoniSoft.getInstance().isGUI()) {
            JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));
            JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.CANCEL"));
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainPanel.removeAll();
                    mainPanel.setBackground(Color.WHITE);
                    mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.CLEANUP"), JLabel.CENTER));
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

    /**
     * Start calculation
     */
    private void start() {
        // Berechnug starten
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

    /**
     * Thsi does the work
     */
    @Override
    public void run() {
        stoppThread.running = true;
        myPlot = new OgivePlotMaker(describer, new DatasetWorkerFactory(this.gui));
        missingHours = myPlot.getmissingHours();
        if (stoppThread.running) {
            chart = new JFreeChart(describer.getPlotTitle(), JFreeChart.DEFAULT_TITLE_FONT, myPlot.getPlotInfo(), true);

            // Untertitel
            TextTitle subtitle = new TextTitle(describer.getPlotSubtitle());
            subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));
            chart.addSubtitle(subtitle);

            chart.setAntiAlias(false);
            chart.setNotify(true);

            if (MoniSoft.getInstance().getApplicationProperties().getProperty("DrawChartStamp").equals("1")) {
                drawChartStamp();
            }

            if (MoniSoft.getInstance().isGUI()) {
                CtrlChartPanel ctrlChartPanel = new CtrlChartPanel(chart, false, describer, this.gui);
                ctrlChartPanel.setMaximumDrawHeight(2000);
                ctrlChartPanel.setMaximumDrawWidth(2000);
                File file = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PIC_FOLDER);
                if (file.exists()) {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(file);
                } else {
                    ctrlChartPanel.setDefaultDirectoryForSaveAs(null);
                }

                JPanel dashBoard = new JPanel(new BorderLayout());
                dashBoard.setBackground(Color.WHITE);
                JSlider slider = new JSlider(0, 100, 0);
                slider.setBackground(Color.WHITE);
                slider.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent event) {
                        JSlider source = (JSlider) event.getSource();
                        int sliderValue = source.getValue();

                        JInternalFrame activeFrame = OgivePlotChart.this.gui.getDesktop().getSelectedFrame();
                        JPanel activePanel = (JPanel) activeFrame.getContentPane().getComponent(0);
                        ChartPanel activeChartPanel = (ChartPanel) activePanel.getComponent(0);
                        XYPlot plot = (XYPlot) activeChartPanel.getChart().getPlot();
                        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
                        Range range = domainAxis.getRange();
                        double c = domainAxis.getLowerBound() + (sliderValue / 100.0) * range.getLength();
                        DecimalFormat decf = new DecimalFormat("0.0");
                        XYSeriesCollection seriesCollection = (XYSeriesCollection) plot.getDataset();
                        XYSeries series = seriesCollection.getSeries(0);
                        double[][] itemList = series.toArray();

                        int index = 0;
                        while (itemList[0][index] <= c && index < itemList[0].length - 1) {
                            index++;
                        }

                        String operator;
                        if (collection.get(0).getReverse()) {
                            operator = ">=";
                        } else {
                            operator = "<=";
                        }

                        TextTitle subtitle;
                        if (collection.get(0).getflipAxis()) {
                            subtitle = new TextTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.VALUES_ARE") + " " + decf.format(itemList[1][index]) + "h " + operator + " " + decf.format(itemList[0][index]) + " " + plot.getDomainAxis().getLabel() + " (" + missingHours + "h " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.OF") + " " + dateInterval.getSpanInHours() + "h " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.NOT_RECORDED") + ")");
                        } else {
                            subtitle = new TextTitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.VALUES_ARE") + "  " + decf.format(itemList[0][index]) + "h " + operator + " " + decf.format(itemList[1][index]) + " " + plot.getRangeAxis().getLabel() + " (" + missingHours + "h " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.OF") + " " + dateInterval.getSpanInHours() + "h " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.NOT_RECORDED") + ")");
                        }
                        subtitle.setFont(new Font("Dialog", Font.PLAIN, 10));
                        activeChartPanel.getChart().removeSubtitle(activeChartPanel.getChart().getSubtitle(0));
                        activeChartPanel.getChart().addSubtitle(0, subtitle);
                        plot.clearAnnotations();
                        plot.setDomainCrosshairVisible(true);
                        plot.setRangeCrosshairVisible(true);
                        plot.setDomainCrosshairValue(itemList[0][index]);
                        plot.setRangeCrosshairValue(itemList[1][index]);

                        CircleDrawer cd = new CircleDrawer(Color.red, new BasicStroke(1.0f), null);
                        XYAnnotation bestBid = new XYDrawableAnnotation(itemList[0][index], itemList[1][index], 10.0, 10.0, (Drawable) cd);
                        plot.addAnnotation(bestBid);
                    }
                });

                dashBoard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                dashBoard.add(slider, BorderLayout.SOUTH);

                AxisControl leftScalePanel = new AxisControl(AxisControl.LEFT, this.gui);

                mainPanel.removeAll();
                mainPanel.add(ctrlChartPanel, BorderLayout.CENTER);
                mainPanel.add(dashBoard, BorderLayout.SOUTH);
                mainPanel.add(leftScalePanel, BorderLayout.WEST);

                this.revalidate();
            }
        }
    }

    /**
     * Build and show the chart stamp
     */
    private void drawChartStamp() {
        // Bildstempel
        TextTitle tt = new TextTitle(new Date().toString() + "   " + MoniSoft.getInstance().getDBConnector().getUserName() + " " + " an" + " " + MoniSoft.getInstance().getDBConnector().getDBName(), new Font("Dialog", Font.PLAIN, 9));
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

        if (describer.isShowLegend() == null || describer.isShowLegend()) {
            chart.addSubtitle(legend);
        }

        chart.removeLegend();
    }

    /**
     * Closes ths frame
     */
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
     * return the created chart
     *
     * @return The chart
     */
    public JFreeChart getChart() {
        return chart;
    }
}
