package de.jmonitoring.Components;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 *
 * @author togro
 */
public class MemoryUsage extends JPanel {

    /** Time series for total memory used. */
    private TimeSeries total;
    /** Time series for free memory. */
    private TimeSeries free;

    /**
     * Creates a new application.
     *
     * @param maxAge  the maximum age (in milliseconds).
     */
    public MemoryUsage(int maxAge) {

        super(new BorderLayout());

        // create two series that automatically discard data more than 30
        // seconds old...
        this.total = new TimeSeries(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("MemoryUsage.GESAMTSPEICHER"));
        this.total.setMaximumItemAge(maxAge);
        this.free = new TimeSeries(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("MemoryUsage.FREIER SPEICHER"));
        this.free.setMaximumItemAge(maxAge);
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.total);
        dataset.addSeries(this.free);

        DateAxis domain = new DateAxis(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("MemoryUsage.ZEIT"));
        NumberAxis range = new NumberAxis(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("MemoryUsage.SPEICHER"));
        domain.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        range.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        domain.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));
        range.setLabelFont(new Font("SansSerif", Font.PLAIN, 14));

        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.green);
        renderer.setSeriesStroke(0, new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        renderer.setSeriesStroke(1, new BasicStroke(3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        XYPlot plot = new XYPlot(dataset, domain, range, renderer);
        domain.setAutoRange(true);
        domain.setLowerMargin(0.0);
        domain.setUpperMargin(0.0);
        domain.setTickLabelsVisible(true);

        range.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        JFreeChart chart = new JFreeChart(null, new Font("SansSerif", Font.BOLD, 12), plot, true);

        ChartUtilities.applyCurrentTheme(chart);

        ChartPanel chartPanel = new ChartPanel(chart, true);
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4),
                BorderFactory.createLineBorder(Color.black)));
        add(chartPanel);

    }

    /**
     * Adds an observation to the 'total memory' time series.
     *
     * @param y  the total memory used.
     */
    private void addTotalObservation(double y) {
        this.total.add(new Millisecond(), y);
    }

    /**
     * Adds an observation to the 'free memory' time series.
     *
     * @param y  the free memory.
     */
    private void addFreeObservation(double y) {
        this.free.add(new Millisecond(), y);
    }

    /**
     * The data generator.
     */
    class DataGenerator extends Timer implements ActionListener {

        /**
         * Constructor.
         *
         * @param interval  the interval (in milliseconds)
         */
        DataGenerator(int interval) {
            super(interval, null);
            addActionListener(this);
        }

        /**
         * Adds a new free/total memory reading to the dataset.
         *
         * @param event  the action event.
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            long f = Runtime.getRuntime().freeMemory();
            long t = Runtime.getRuntime().totalMemory();
            addTotalObservation(t);
            addFreeObservation(f);
        }
    }
}
