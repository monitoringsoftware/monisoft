package de.jmonitoring.base;

import de.jmonitoring.standardPlots.common.SeriesLooks;
import de.jmonitoring.standardPlots.windPlot.WindChartDescriber;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChartDescriber;
import de.jmonitoring.standardPlots.scatterPlot.ScatterChartDescriber;
import de.jmonitoring.standardPlots.ogivePlot.OgiveChartDescriber;
import de.jmonitoring.standardPlots.comparePlot.CompareChartDescriber;
import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.standardPlots.carpetPlot.CarpetChartDescriber;
import de.jmonitoring.standardPlots.common.ChartLoader;
import de.jmonitoring.standardPlots.scatterPlot.ScatterSeriesLooks;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesLooks;
import de.jmonitoring.Components.ChartPropertyDialog;
import de.jmonitoring.Components.ExportDialog;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.DataPointObject;
import de.jmonitoring.utils.JFreeChartPatches.SamplingXYLineAndShapeRenderer;
import de.jmonitoring.utils.intervals.IntervalSelectorEntry;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.optimization.fitting.PolynomialFitter;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.function.Function2D;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.function.PolynomialFunction2D;
import org.jfree.data.function.PowerFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.statistics.Statistics;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.*;

/**
 * This is the main {@link ChartPanel} for the JfreeChart charts. Is is an
 * extension to the builtin {@link ChartPanel}.<p> It defines the popup menus
 * and the actions of its items.
 *
 * @author togro
 */
public class CtrlChartPanel extends ChartPanel {

    private ChartEntity selectedEntity;
    private XYItemRenderer keepRenderer;
    private Paint keepPaint;
    private XYDataset dset;
    private boolean seriesSelectable;
    // AZ: neuer Eintrag im Kontextmenue - MONISOFT-12
    private final JMenuItem copyToExportMenu = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("KOPIERE_IN_EXPORT"));
    private final JMenuItem showValuesMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ZEIGE WERTE"));
    private final JMenuItem saveDescriborMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("GRAFIKBESCHREIBUNG SPEICHERN") + "...");
    private final JMenuItem saveSensorCollectionMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MESSPUNKTSAMMLUNG SPEICHERN") + "...");
    private final JMenuItem changeSeriesNameMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LEGENDENEINTRAG ÄNDERN") + "...");
    private final JMenuItem chartPropertyMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("GRAFIKEIGENSCHAFTEN"));
    private final JMenu regressionMenu = new JMenu("Regression");
    private final JMenuItem powerRegressionMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("POWER"));
    private final JMenuItem linearRegressionMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("LINEAR"));
    private final JMenuItem polyRegressionMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("POLYNOMIAL") + "...");
    private final JMenuItem deleteRegressionMenuItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("REMOVE_REGRESSION"));
    private Rectangle currentRect = null;
    private Rectangle rectToDraw = null;
    private Rectangle previousRectDrawn = new Rectangle();
    private HashMap<DataPointObject, String> labelmap = new HashMap<DataPointObject, String>();
    private final static float dash1[] = {10.0f};
    private final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
    private final ChartDescriber chartDescriber;
    private XYPlot plot = null;
    private Integer selectedSeries = null;
    private ArrayList<Integer> actualSeriesIDs = new ArrayList<Integer>();
    private Integer plotType = MoniSoftConstants.PLOTTYPE_OTHER;
    private final MainApplication gui;

    /**
     * Constructor for a given chart. Series are selectable.
     *
     * @param chart The chart to be shown
     * @param gui The GUI which calls us
     */
    public CtrlChartPanel(JFreeChart chart, MainApplication gui) {
        this(chart, true, gui);
    }

    /**
     * Constructor for a given chart.
     *
     * @param chart The chart to be shown
     * @param selectable true if series should be selectable
     * @param gui The GUI which calls us
     */
    public CtrlChartPanel(JFreeChart chart, boolean selectable, MainApplication gui) {
        this(chart, selectable, null, gui);
    }

    /**
     * Main constructor this object
     *
     * @param chart The chart to be shown
     * @param selectabletrue if series should be selectable
     * @param describer A chartdescriber by which this chart is made up by
     * @param gui The GUI which calls us
     */
    public CtrlChartPanel(JFreeChart chart, boolean selectable, ChartDescriber describer, MainApplication gui) {
        super(chart);
        chartDescriber = describer;
        seriesSelectable = selectable;
        this.gui = gui;

        if (chartDescriber != null) {
            List<SeriesLooks> collection = chartDescriber.getchartCollection();
            for (SeriesLooks looks : collection) {
                if (looks != null) {
                    actualSeriesIDs.add(looks.getSeriesID());
                }
            }

            // detect plot type
            if (chartDescriber instanceof ScatterChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_SCATTER;
            } else if (chartDescriber instanceof TimeSeriesChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_TIMESERIES;
            } else if (chartDescriber instanceof OgiveChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_OGIVE;
            } else if (chartDescriber instanceof CarpetChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_CARPET;
            } else if (chartDescriber instanceof CompareChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_COMPARE;
            } else if (chartDescriber instanceof WindChartDescriber) {
                plotType = MoniSoftConstants.PLOTTYPE_WIND;
            }
        }

        // menu item for saving sensor collection
        saveSensorCollectionMenuItem.addMouseListener(saveSensorCollectionMouseAdapter());
        //  menu item for saving chart describer
        saveDescriborMenuItem.addMouseListener(saveChartDescriberMouseListener());
        // AZ: Monisoft-12
        copyToExportMenu.addMouseListener(copyToExportMenuAdapter());        
        // menu item for showning value list
        showValuesMenuItem.addMouseListener(showValuesMouseAdapter());
        // menu item for changing series name
        changeSeriesNameMenuItem.addMouseListener(changeSeriesNameMouseAdapter());
        // menu item for chart settings
        chartPropertyMenuItem.addMouseListener(showChartSettingsMouseAdapter());
        // menu item for linear regression
        linearRegressionMenuItem.addMouseListener(linearRegressionMouseAdapter());
        // menu item for power regression
        powerRegressionMenuItem.addMouseListener(powerRegressionMouseAdapter());
        // menu item for deleting regression
        deleteRegressionMenuItem.addMouseListener(deleteRegressionsMouseAdapter());
        // menu item for polynomial regression
        polyRegressionMenuItem.addMouseListener(polynomialRegressionMouseAdapter());
        buildPopupMenu();
    }

    /**
     * Collects setting from chart an invokes saving of describer
     */
    private void saveChartDescribor() {
        getPopupMenu().setVisible(false);
        chartDescriber.setPlotTitle(getChart().getTitle().getText());
        chartDescriber.setPlotSubtitle(((TextTitle) getChart().getSubtitle(0)).getText());

        chartDescriber.setDomainAxis(((XYPlot) getChart().getPlot()).getDomainAxis());
        if ((XYPlot) getChart().getPlot() instanceof CombinedDomainXYPlot) {
            chartDescriber.setLeftRangeAxis(((XYPlot) ((CombinedDomainXYPlot) getChart().getPlot()).getSubplots().get(0)).getRangeAxis(0));
            chartDescriber.setRightRangeAxis(((XYPlot) ((CombinedDomainXYPlot) getChart().getPlot()).getSubplots().get(0)).getRangeAxis(1));
        } else {
            chartDescriber.setLeftRangeAxis(((XYPlot) getChart().getPlot()).getRangeAxis(0));
            chartDescriber.setRightRangeAxis(((XYPlot) getChart().getPlot()).getRangeAxis(1));
        }

        // Fensterabmessungen holen und in den Describer setzen Abfangen von fehlenden Größeneinträgen beim lesen.....
        Component c = getParent();
        while (!(c instanceof JInternalFrame)) { // so lange durchgehen bis InternalFrame erreicht.....
            c = c.getParent();
        }
        chartDescriber.setWidth(c.getWidth());
        chartDescriber.setHeight(c.getHeight());

        saveDescribor();
    }

    /**
     * Changes the name of the currently selected series
     */
    private void changeSeriesName() {
        getPopupMenu().setVisible(false);
        String s = queryLegendName(dset);
        if (s == null || s.isEmpty()) {
            return;
        }
        if (dset instanceof TimeSeriesCollection) {
            ((TimeSeriesCollection) dset).getSeries(0).setKey(s); // Ändern des SeriesKeys (ändert die darstellung der aktuellen Grafik
            ((TimeSeriesLooks) chartDescriber.getchartCollection().get(actualSeriesIDs.get(selectedSeries))).setLegendString(s); // LegendString im Look speichern, dmait sie in den describer übwernommen und mit diesem ggf. mitgespeichert wir
        } else if (dset instanceof XYSeriesCollection) {
            ((XYSeriesCollection) dset).getSeries(0).setKey(s);
            ((ScatterSeriesLooks) chartDescriber.getchartCollection().get(actualSeriesIDs.get(selectedSeries))).setLegendString(s);
        } else if (dset instanceof DefaultXYZDataset) {
            DefaultXYZDataset xyzsc = (DefaultXYZDataset) dset;
            xyzsc.getSeriesKey(0);
            // Wie namen ändern? getSeries gibt es nicht....
        } else if (dset instanceof TimeTableXYDataset) {
//                    for (int i = 0; i < ((TimeTableXYDataset) dset).getSeriesCount(); i++) {
//                        System.out.println(((TimeTableXYDataset) dset).getSeriesKey(i));
//                    }
            System.out.println(((TableXYDataset) dset).getSeriesKey(selectedSeries));

            ((TimeSeriesLooks) chartDescriber.getchartCollection().get(actualSeriesIDs.get(selectedSeries))).setLegendString(s);
        }
        getChart().fireChartChanged();
    }

    // Select region with ctrl-shift-drag
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isShiftDown() && e.isControlDown()) {
            int x = e.getX();
            int y = e.getY();
            currentRect = new Rectangle(x, y, 0, 0);
            updateDrawableRect(getWidth(), getHeight());
            repaint();
        } else {
            super.mousePressed(e);
        }
    }

    // Select region with ctrl-shift-drag
    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.isShiftDown() && e.isControlDown()) {
            updateSize(e);
        } else {
            super.mouseDragged(e);
        }
    }

    // Select region with ctrl-shift-drag
    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isShiftDown() && e.isControlDown()) {
            updateSize(e);
//            System.out.println("Jetzt was machen mit " + currentRect.getBounds().toString());
//            Point2D p; = translateScreenToJava2D(currentRect.getLocation());
//            Rectangle2D plotArea = getScreenDataArea();
//            ValueAxis domainAxis = null, rangeAxis = null;
//            RectangleEdge rangeAxisEdge = null, domainAxisEdge= null;
//            if (plot != null) {
//                domainAxis = plot.getDomainAxis();
//                domainAxisEdge = plot.getDomainAxisEdge();
//                rangeAxis = plot.getRangeAxis();
//                rangeAxisEdge = plot.getRangeAxisEdge();
////                double chartX = domainAxis.java2DToValue(p.getX(), plotArea, domainAxisEdge);
////                double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
////                System.out.println("Chart: x = " + chartX + ", y = " + chartY);
//            }
//            ChartRenderingInfo rInfo = getChartRenderingInfo();
//            EntityCollection c = rInfo.getEntityCollection();
//            int item, series;
//            Double x, y;
//            XYItemEntity entity;
//
////
//
//
//            // loop all entyties
//            for (int i = 0; i < c.getEntityCount(); i++) {
//                if (c.getEntity(i) instanceof XYItemEntity) {
//                    item = ((XYItemEntity) c.getEntity(i)).getItem();
//                    series = ((XYItemEntity) c.getEntity(i)).getSeriesIndex();
//                    entity = (XYItemEntity) c.getEntity(i);
//                    x = entity.getDataset().getXValue(series, item);
//                    y = entity.getDataset().getYValue(series, item);
//                    if (domainAxis != null && rangeAxis != null) {
//                        p = translateJava2DToScreen()
//                        double chartX = domainAxis.java2DToValue(p.getX(), plotArea, domainAxisEdge);
//                        double chartY = rangeAxis.java2DToValue(p.getY(), plotArea, rangeAxisEdge);
//
//                        System.out.println("Chart: x = " + chartX + ", y = " + chartY);
//                    }
//                    //                    System.out.println(((XYItemEntity) c.getEntity(i)).getDataset().getX(series, item) + " " + ((XYItemEntity) c.getEntity(i)).getDataset().getY(series, item));
//                    if (currentRect.contains(x, y)) {
//                        System.out.println("> " + x + ", " + y);
//                    }
//                }
//            }
        } else {
            super.mouseReleased(e);
        }
    }
    // Select region with ctrl-shift-drag

    void updateSize(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        currentRect.setSize(x - currentRect.x, y - currentRect.y);
        updateDrawableRect(getWidth(), getHeight());
        Rectangle totalRepaint = rectToDraw.union(previousRectDrawn);
        repaint(totalRepaint.x, totalRepaint.y, totalRepaint.width, totalRepaint.height);
    }
    // Select region with ctrl-shift-drag

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paints the background and image

        //If currentRect exists, paint a box on top.
        if (currentRect != null) {
            //Draw a rectangle on top of the image.
            g.setXORMode(Color.white); //Color of line varies
            //depending on image colors
            g.setColor(Color.BLACK);
            g.drawRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1, rectToDraw.height - 1);
            g.setColor(Color.orange);
            g.fillRect(rectToDraw.x, rectToDraw.y, rectToDraw.width - 1, rectToDraw.height - 1);
        }
    }

    /**
     * Ask for new legend entry
     *
     * @param dataset
     * @return
     */
    private String queryLegendName(XYDataset dataset) {
        return JOptionPane.showInputDialog(getTopLevelAncestor(), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NEUER LEGENDENEINTRAG"), dataset.getSeriesKey(0));
    }

    /**
     * Mark a series when clicked
     *
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        selectSeries(e);
    }

    /**
     * Returns the id of the given
     * <code>XYDataset</code>
     *
     * @param dataset The datset to query
     * @return The id of this dataset
     */
    private Integer getDatasetID(XYDataset dataset) {
        Integer id = null;
        for (int i = 0; i < plot.getDatasetCount(); i++) { // alle datasets dieses plots durchlaufen
            if (plot.getDataset(i).equals(dataset)) {
                id = i;
            }
        }
        return id;
    }

    /**
     * Highlights the selected series with magenta color
     *
     * @param series The series to be marked
     */
    private void markSeries(int series) {
        copyToExportMenu.setEnabled(true);
        showValuesMenuItem.setEnabled(true);
        changeSeriesNameMenuItem.setEnabled(true);
        regressionMenu.setEnabled(true);
        if (plot.getRendererForDataset(dset) instanceof StackedXYBarRenderer) {
            StackedXYBarRenderer renderer = (StackedXYBarRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(series);
            renderer.setSeriesPaint(series, Color.MAGENTA);
            keepRenderer = (XYItemRenderer) renderer;
        } else if (plot.getRendererForDataset(dset) instanceof XYBarRenderer) {
            XYBarRenderer renderer = (XYBarRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(0);
            renderer.setSeriesPaint(0, Color.MAGENTA);
            keepRenderer = (XYItemRenderer) renderer;
        } else if (plot.getRendererForDataset(dset) instanceof XYAreaRenderer) {
            XYAreaRenderer renderer = (XYAreaRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(0);
            renderer.setSeriesPaint(0, Color.MAGENTA);
            keepRenderer = (XYItemRenderer) renderer;
        } else if (plot.getRendererForDataset(dset) instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(0);
            renderer.setSeriesPaint(0, Color.MAGENTA);
            renderer.setLegendTextPaint(0, Color.RED);
            keepRenderer = (XYItemRenderer) renderer;
        } else if (plot.getRendererForDataset(dset) instanceof SamplingXYLineAndShapeRenderer) {
            SamplingXYLineAndShapeRenderer renderer = (SamplingXYLineAndShapeRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(0);
            renderer.setSeriesPaint(0, Color.MAGENTA);
            renderer.setLegendTextPaint(0, Color.RED);
            keepRenderer = (XYItemRenderer) renderer;
        } else if (plot.getRendererForDataset(dset) instanceof XYBlockRenderer) {
            XYBlockRenderer renderer = (XYBlockRenderer) plot.getRendererForDataset(dset);
            keepPaint = renderer.getSeriesPaint(0);
            renderer.setSeriesPaint(0, Color.MAGENTA);
            keepRenderer = (XYBlockRenderer) renderer;
        } else {
            copyToExportMenu.setEnabled(true);
            showValuesMenuItem.setEnabled(false);
            changeSeriesNameMenuItem.setEnabled(false);
            regressionMenu.setEnabled(false);
            dset = null;
        }
    }

    /**
     * Method for ctrl-shift-drag rectangle marking
     *
     * @param compWidth
     * @param compHeight
     */
    private void updateDrawableRect(int compWidth, int compHeight) {
        int x = currentRect.x;
        int y = currentRect.y;
        int width = currentRect.width;
        int height = currentRect.height;

        //Make the width and height positive, if necessary.
        if (width < 0) {
            width = 0 - width;
            x = x - width + 1;
            if (x < 0) {
                width += x;
                x = 0;
            }
        }
        if (height < 0) {
            height = 0 - height;
            y = y - height + 1;
            if (y < 0) {
                height += y;
                y = 0;
            }
        }

        //The rectangle shouldn't extend past the drawing area.
        if ((x + width) > compWidth) {
            width = compWidth - x;
        }

        if ((y + height) > compHeight) {
            height = compHeight - y;
        }

        //Update rectToDraw after saving old value.
        if (rectToDraw != null) {
            previousRectDrawn.setBounds(rectToDraw.x, rectToDraw.y, rectToDraw.width, rectToDraw.height);
            rectToDraw.setBounds(x, y, width, height);
        } else {
            rectToDraw = new Rectangle(x, y, width, height);
        }
    }

    /**
     * Returns the chartsdescriber of this chartpanel
     *
     * @return
     */
    public ChartDescriber getChartdescriber() {
        return chartDescriber;
    }

    /**
     * Shows the {@link ChartPropertyDialog} for this chart
     */
    private void showChartPropertyDialog() {
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, true, getChart(), this);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /**
     * Saves the {@link ChartDesriber} for this chart to a file
     */
    private void saveDescribor() {
        ChartLoader handler = new ChartLoader(gui, null);
        handler.writeChartDescriber(chartDescriber);
    }

    /**
     * Saves all sensors in this chart as a sensorcollection
     */
    private void saveSensorCollection() {
        String name = JOptionPane.showInputDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BITTE GEBEN SIE EINEN NAMEN FÜR DIE MESSPUNKTSAMMLUNG EIN") + "\n");
        HashSet<Integer> idList = new HashSet<Integer>();
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
            for (Object looks : chartDescriber.getchartCollection()) {
                if (looks instanceof ScatterSeriesLooks) {
                    idList.add(((ScatterSeriesLooks) looks).getDomainSensor().getSensorID());
                }
                if (looks != null) {
                    idList.add(((SeriesLooks) looks).getSensorID());
                }
            }
            if (idList.size() > 0) { // sind überhaupt Messpunkte enthalten
                Integer collectionID = SensorCollectionHandler.getCollectionID(name);
                if (collectionID == null) { // existiert die collection schon?
                    SensorCollectionHandler.insertCollection(name, idList, SensorCollectionHandler.SIMPLE_COLLECTION, false, true);
                } else { // wenn ja: fragen
                    if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("EXISTS") + "\n'" + name + "'\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.ASK_OVERWRITE"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.OVERWRITE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                        SensorCollectionHandler.updateCollection(collectionID, idList, false, true);
                    }
                }
            }
        }
    }

    /**
     * Sets the map of tooltip labels to the given map
     *
     * @param map The map to replace the legend items
     */
    public void setTooltipLabelMap(HashMap<DataPointObject, String> map) {
        labelmap = map;
    }

    /**
     * Adds a regression line to the chart
     *
     * @param regressionData
     */
    private void drawRegression(XYDataset regressionData) {
        // store current series and renderer in maps
        ArrayList<XYDataset> dataSetMap = new ArrayList<XYDataset>();
        ArrayList<XYItemRenderer> rendererMap = new ArrayList<XYItemRenderer>();
        int count = plot.getDatasetCount();
        XYDataset ds;
        XYItemRenderer renderer;
        for (int i = 0; i < count; i++) {
            ds = plot.getDataset(i);
            dataSetMap.add(ds);
            renderer = plot.getRenderer(i);
            rendererMap.add(renderer);
        }

        // add series for regression
        plot.setDataset(0, regressionData);
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        r.setSeriesVisibleInLegend(0, false);
        Color c = ((Color) keepPaint).darker();
        r.setSeriesPaint(0, c);
        r.setSeriesStroke(0, new BasicStroke(2.0f));
        plot.setRenderer(0, r);


        // draw the "old" series behind the regression line
        for (int i = 0; i < count; i++) {
            plot.setDataset(i + 1, dataSetMap.get(i));
            plot.setRenderer(i + 1, rendererMap.get(i));
        }
    }

    /**
     * Calculates a linear regression for the selected series
     */
    private void drawLinearRegression() {
        double[] OLSCoefficients = Regression.getOLSRegression(dset, 0);
        Function2D curve = new LineFunction2D(OLSCoefficients[0], OLSCoefficients[1]);
        XYDataset regressionData = DatasetUtilities.sampleFunction2D(curve, getDatasetDomainRange()[0], getDatasetDomainRange()[1], 100, "Fit linear"); // + dset.getSeriesKey(0));
        DecimalFormat df = new DecimalFormat("0.##");
        Messages.showMessage("Regression: y = " + df.format(OLSCoefficients[1]) + " * x" + getCombinedOperator("+", OLSCoefficients[0], df) + "\t" + "Correlation:" + " " + new DecimalFormat("0.#####").format(calcCorrelation()) + "\n", true);
        drawRegression(regressionData);
    }

    /**
     * Helper for linear regression calculation of the correlationCoefficient
     *
     * @return
     */
    private double calcCorrelation() {
        Double correlationCoefficient = null;
        int n = dset.getItemCount(0);
        ArrayList<Double> xArray = new ArrayList<Double>();
        ArrayList<Double> yArray = new ArrayList<Double>();

        for (int i = 0; i < n; i++) {
            xArray.add(dset.getXValue(0, i));
            yArray.add(dset.getYValue(0, i));
        }
        correlationCoefficient = Statistics.getCorrelation(xArray.toArray(new Double[xArray.size()]), yArray.toArray(new Double[yArray.size()]));

        return correlationCoefficient;
    }

    /**
     * Calculate power regression
     */
    private void drawPowerRegression() {
        double[] powerCoefficients = getMyPowerRegression();
        if (!Double.isNaN(powerCoefficients[0]) && !Double.isNaN(powerCoefficients[1])) {
            Function2D curve = new PowerFunction2D(powerCoefficients[0], powerCoefficients[1]);
            XYDataset regressionData = DatasetUtilities.sampleFunction2D(curve, getDatasetDomainRange()[0], getDatasetDomainRange()[1], 100, "Fit power"); // + dset.getSeriesKey(0));
            drawRegression(regressionData);
            DecimalFormat df = new DecimalFormat("0.##");
            Messages.showMessage("Regression: y = " + df.format(powerCoefficients[0]) + " * x ^ " + df.format(powerCoefficients[1]) + "\n", true);
        } else {
            System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("POWERREGRESSION_NOT_CALC"));
        }

    }

    /**
     * Calculate polynomial regression
     */
    public void drawPolyRegression(int order) {
//        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer();
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PolynomialFitter fitter = new PolynomialFitter(order, optimizer);
        int n = dset.getItemCount(0);
        for (int i = 0; i < n; i++) {
            fitter.addObservedPoint(dset.getXValue(0, i), dset.getYValue(0, i));
        }

        try {
            double[] coeffs = fitter.fit();
            Function2D curve = new PolynomialFunction2D(coeffs);
            XYDataset regressionData = DatasetUtilities.sampleFunction2D(curve, getDatasetDomainRange()[0], getDatasetDomainRange()[1], 100, "Fit polynomial"); // (" + order + ")-" + dset.getSeriesKey(0));
            drawRegression(regressionData);

            ArrayList<String> displayCoeffs = new ArrayList<String>();
            displayCoeffs.add("");
            displayCoeffs.add("*x");
            for (int i = 2; i <= 15; i++) {
                displayCoeffs.add("*x^" + i);
            }

            DecimalFormat df = new DecimalFormat("0.##");
            String s = "y = ";
            String coef = "";
            for (int i = 0; i < coeffs.length; i++) {
                if (i > 0) {
                    coef = getCombinedOperator("+", coeffs[i], df);
                } else {
                    coef = df.format(coeffs[i]);
                }
                s += coef + displayCoeffs.get(i);
            }
            Messages.showMessage("Regression: " + s + "\n", true);

        } catch (ConvergenceException e) {
            Messages.showException(e);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("REGRESSION_ABORT_NOCONVERGENCE") + "\n", true);
        }
    }

    /**
     * Helper for calculation power regression
     *
     * @return An array of coeffecients
     */
    private double[] getMyPowerRegression() {
        int n = dset.getItemCount(0);
        int skip = 0;
        if (n < 2) {
            throw new IllegalArgumentException("Not enough data.");
        }

        double sumX = 0;
        double sumY = 0;
        double sumXX = 0;
        double sumXY = 0;
        for (int i = 0; i < n; i++) {
            double x = Math.log(dset.getXValue(0, i));
            double y = Math.log(dset.getYValue(0, i));
            if (!Double.isNaN(x) && !Double.isNaN(y) && !Double.isInfinite(x) && !Double.isInfinite(y)) {
                sumX += x;
                sumY += y;
                double xx = x * x;
                sumXX += xx;
                double xy = x * y;
                sumXY += xy;
            } else {
                skip++;
            }
        }
        n = n - skip;

        double sxx = sumXX - (sumX * sumX) / n;
        double sxy = sumXY - (sumX * sumY) / n;
        double xbar = sumX / n;
        double ybar = sumY / n;

        double[] result = new double[2];
        result[1] = sxy / sxx;
        result[0] = Math.pow(Math.exp(1.0), ybar - result[1] * xbar);
        return result;
    }

    /**
     * Checks if the value after the +/- operator is negative and if it is
     * returns the combined operator/value pair<p> Example: "y = 5x + -2"<br> To
     * get a cleaner String we would call : "y = 5x" + getCombinedOperator("+",
     * -2, format)<br> The resulting String would be "y = 5x - 2"
     *
     * @param operator
     * @param value
     * @param decimalFormat
     * @return
     */
    private String getCombinedOperator(String operator, Double value, DecimalFormat decimalFormat) {
        String combinedOperator = "";
        if (operator.equals("+") && value < 0d) {
            combinedOperator = " - " + decimalFormat.format(Math.abs(value));
        } else if (operator.equals("+")) {
            combinedOperator = " + " + decimalFormat.format(value);
        }

        if (operator.equals("-") && value < 0d) {
            combinedOperator = " + " + decimalFormat.format(Math.abs(value));
        } else if (operator.equals("-")) {
            combinedOperator = " - " + decimalFormat.format(value);;
        }
        return combinedOperator;
    }

    /**
     * Deletes all regression lines from the chart
     */
    private void deleteRegressions() {
        int count = plot.getDatasetCount();
        XYDataset ds;
        XYItemRenderer renderer;

        // Create a map with all sets and renderers which are no regression
        String key;
        for (int i = 0; i < count; i++) {
            ds = plot.getDataset(i);
            renderer = plot.getRenderer(i);
            key = (String) ds.getSeriesKey(0);
            if (key.startsWith("Fit")) {
                renderer.setSeriesVisible(0, false);
            }
        }
    }

    /**
     * Calculate the value range on the domain axis
     *
     * @return An array with the minimum and maximum value
     */
    private Double[] getDatasetDomainRange() {
        Double[] range = new Double[2];

        int n = dset.getItemCount(0);
        Double minX = null;
        Double maxX = null;
        Double x, y;
        for (int i = 0; i < n; i++) {
            x = dset.getXValue(0, i);
            y = dset.getYValue(0, i);

            if (maxX == null || x > maxX) {
                maxX = x;
            }
            if (minX == null || x < minX) {
                minX = x;
            }
        }
        range[0] = minX;
        range[1] = maxX;

        return range;
    }

    /**
     * Select the series at the position of the given {@link MouseEvent}
     *
     * @param e The {@link MouseEvent}
     */
    private void selectSeries(MouseEvent e) {
        if (seriesSelectable) {
            int x = e.getX();
            int y = e.getY();
            ChartRenderingInfo rend = getChartRenderingInfo();
            EntityCollection c = rend.getEntityCollection();
            selectedEntity = c.getEntity(x, y);

            plot = null;

            XYPlot wholePlot = (XYPlot) getChart().getPlot();
            if (wholePlot instanceof CombinedDomainXYPlot) {
                CombinedDomainXYPlot p = (CombinedDomainXYPlot) wholePlot.getRootPlot();
                List plots = p.getSubplots();
                Iterator<XYPlot> it = plots.iterator();
                while (it.hasNext()) {
                    XYPlot tmpPlot = it.next();
                    if (!(tmpPlot.getRangeAxis() instanceof SymbolAxis)) {
                        plot = tmpPlot;
                    }
                }
            } else {
                plot = wholePlot;
            }

            try {
                if (keepRenderer != null && keepRenderer instanceof StackedXYBarRenderer) {
                    if (selectedSeries != null) {
                        ((StackedXYBarRenderer) keepRenderer).setSeriesPaint(selectedSeries, keepPaint);
                    }
                } else if (keepRenderer != null && keepRenderer instanceof XYBarRenderer) {
                    ((XYBarRenderer) keepRenderer).setSeriesPaint(0, keepPaint);
                } else if (keepRenderer != null && keepRenderer instanceof XYAreaRenderer) {
                    ((XYAreaRenderer) keepRenderer).setSeriesPaint(0, keepPaint);
                } else if (keepRenderer != null && keepRenderer instanceof XYLineAndShapeRenderer) {
                    ((XYLineAndShapeRenderer) keepRenderer).setSeriesPaint(0, keepPaint);
                    ((XYLineAndShapeRenderer) keepRenderer).setLegendTextPaint(0, Color.BLACK);
                } else if (keepRenderer != null && keepRenderer instanceof SamplingXYLineAndShapeRenderer) {
                    ((SamplingXYLineAndShapeRenderer) keepRenderer).setSeriesPaint(0, keepPaint);
                    ((SamplingXYLineAndShapeRenderer) keepRenderer).setLegendTextPaint(0, Color.BLACK);
                } else if (keepRenderer != null && keepRenderer instanceof XYBlockRenderer) {
                    ((XYBlockRenderer) keepRenderer).setSeriesPaint(0, keepPaint);
                }
                if (selectedEntity != null && selectedEntity instanceof XYItemEntity) { // Es wurde eine Messreihe gewählt
                    XYItemEntity entity = (XYItemEntity) selectedEntity;
                    dset = entity.getDataset(); // dataset dieser entity holen

                    if (dset instanceof TimeTableXYDataset) {
                        selectedSeries = entity.getSeriesIndex();
                        markSeries(selectedSeries);
                    } else {
                        selectedSeries = getDatasetID(dset);
                        markSeries(0);
                    }
                } else if (selectedEntity != null && selectedEntity instanceof LegendItemEntity) { // Es wurde aus der Legende gewählt
                    LegendItemEntity legendEntity = (LegendItemEntity) selectedEntity;
                    dset = (XYDataset) legendEntity.getDataset();
                    selectedSeries = getDatasetID(dset);
                    markSeries(0);
                } else {                                    // es wurde nichts (leere Fläche) ausgewählt
                    copyToExportMenu.setEnabled(true);
                    showValuesMenuItem.setEnabled(false);
                    changeSeriesNameMenuItem.setEnabled(false);
                    regressionMenu.setEnabled(false);
                    dset = null;
                    selectedSeries = null;
                }
            } catch (Exception ev) {
                Messages.showException(ev);
            }
        }
    }

    /**
     * {@link MouseAdapter} for show values menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter showValuesMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                getPopupMenu().setVisible(false);
                Thread t = new generateTableDataThread();
                t.start();
            }
        };
    }
    
    
    // AZ: Monisoft-12
    private MouseAdapter copyToExportMenuAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                getPopupMenu().setVisible(false);
                Models models = new Models();
                ExportDialog exportDialog = new ExportDialog(SensorInformation.getSensorList(), models.getAggIntervalComboBoxModel(), gui.getMainFrame(), false, new DatasetWorkerFactory(gui));
                exportDialog.setLocationRelativeTo(gui.getMainFrame());
                exportDialog.setVisible(true);

                Calendar startDateCalendar = new GregorianCalendar();
                startDateCalendar.setTime( chartDescriber.chartDateInterval.getStartDate() );
                startDateCalendar.set( Calendar.HOUR_OF_DAY, 0 );
                startDateCalendar.set( Calendar.MINUTE, 0 );
                startDateCalendar.set( Calendar.SECOND, 0 );
                startDateCalendar.set( Calendar.MILLISECOND, 0 );

                Calendar endDateCalendar = new GregorianCalendar();
                endDateCalendar.setTime( chartDescriber.chartDateInterval.getEndDate() );
                endDateCalendar.set( Calendar.HOUR_OF_DAY, 0 );
                endDateCalendar.set( Calendar.MINUTE, 0 );
                endDateCalendar.set( Calendar.SECOND, 0 );
                endDateCalendar.set( Calendar.MILLISECOND, 0 );
                
                // Zeitraum kopieren
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy hh:kk:ss" );
                
                System.out.println( "StartDate: " + startDateCalendar.getTime() );
                System.out.println( "EndDate: " + endDateCalendar.getTime() );
                
                exportDialog.startDateChooser.setDate( startDateCalendar.getTime() );
                exportDialog.endDateChooser.setDate( endDateCalendar.getTime() );                

                // Alle verfügbaren Messpunkte bereits in die Liste eintragen
                DefaultListModel exportedList = (DefaultListModel) exportDialog.listToBeExported.getModel();
                
                for( Object o : chartDescriber.getchartCollection() )
                {                    
                    SeriesLooks sl = (SeriesLooks) o;
                    
                    for (SensorProperties sensorProperties : SensorInformation.getSensorList() )
                    {
                        if( sensorProperties != null && sl != null &&
                            sensorProperties.getSensorID() == sl.getSensorID() )
                        {                          
                            exportedList.addElement( sensorProperties );
                        }
                    }
                }
                
                // Intervall vorbefüllen durch kleinsten vorhandenen Eintrag                
                // exportDialog.exportIntervalChooser.setSelectedIndex( kleinstesInterval );
            }
        };
    }

    /**
     * {@link MouseAdapter} for change series name menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter changeSeriesNameMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                changeSeriesName();
            }
        };
    }

    /**
     * {@link MouseAdapter} for chart setting dialog menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter showChartSettingsMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                getPopupMenu().setVisible(false);
                showChartPropertyDialog();
            }
        };
    }

    /**
     * {@link MouseAdapter} for linear regression menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter linearRegressionMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawLinearRegression();
            }
        };
    }

    /**
     * {@link MouseAdapter} for power regression menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter powerRegressionMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                drawPowerRegression();
            }
        };
    }

    /**
     * {@link MouseAdapter} for delete regression menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter deleteRegressionsMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                deleteRegressions();
            }
        };
    }

    /**
     * {@link MouseAdapter} for polynomial regression menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter polynomialRegressionMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                try {
                    Integer order = Integer.parseInt(JOptionPane.showInputDialog(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("POLYNOM_DEGREE") + " (2 - 15) "));
                    if (order < 2) {
                        order = 2;
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SET_DEGREE_2") + "\n", true);
                    }
                    if (order > 15) {
                        order = 15;
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SET_DEGREE_15") + "\n", true);
                    }
                    drawPolyRegression(order);
                } catch (NumberFormatException ex) {
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DEGREE_INVALID") + "\n", true);
                }
            }
        };
    }

    /**
     * {@link MouseAdapter} for save sensorCollection menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter saveSensorCollectionMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                getPopupMenu().setVisible(false);
                saveSensorCollection();
            }
        };
    }

    /**
     * {@link MouseAdapter} for save {@link ChartDescriber} menu item
     *
     * @return The {@link MouseAdapter}
     */
    private MouseAdapter saveChartDescriberMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveChartDescribor();
            }
        };
    }

    /**
     * Populate the popupmenu for the chart
     */
    private void buildPopupMenu() {
        getPopupMenu().add(copyToExportMenu);
        getPopupMenu().add(showValuesMenuItem);        
        // add regression menu if we have a scatter plot
        if (plotType == MoniSoftConstants.PLOTTYPE_SCATTER) {
            regressionMenu.add(linearRegressionMenuItem);
            regressionMenu.add(powerRegressionMenuItem);
            regressionMenu.add(polyRegressionMenuItem);
            regressionMenu.add(deleteRegressionMenuItem);
            getPopupMenu().add(regressionMenu);
        }

        getPopupMenu().add(changeSeriesNameMenuItem);
        copyToExportMenu.setEnabled(true);
        showValuesMenuItem.setEnabled(false);
        changeSeriesNameMenuItem.setEnabled(false);
        regressionMenu.setEnabled(false);
        getPopupMenu().addSeparator();
        getPopupMenu().add(saveSensorCollectionMenuItem);
        getPopupMenu().addSeparator();
        getPopupMenu().add(saveDescriborMenuItem);
        getPopupMenu().remove(0); // Die eingebauten "Eigenschaften aus dem Menü nehmen
        getPopupMenu().add(chartPropertyMenuItem, 0); // und durch den eigenen ersetzen
    }

    /**
     * This thread generates a list of values for the selected series and
     * displays a {@link JInternalFrame} with it
     */
    class generateTableDataThread extends Thread {

        StringBuilder sb = new StringBuilder(500000);
        String valueString, colorString = "";

        @Override
        public void run() {
            if (dset == null) {
                return;
            }
            JEditorPane editor = new JEditorPane("text/plain", java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("ERSTELLE LISTE ..."));
            editor.setEditable(false);

            JScrollPane scroller = new JScrollPane();
            scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scroller.getVerticalScrollBar().setValue(scroller.getVerticalScrollBar().getMinimum());
            scroller.setViewportView(editor);
            JInternalFrame f = new JInternalFrame(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.VALUELIST") + " " + dset.getSeriesKey(0), true, true, true);
            f.add(scroller);
            f.setSize(400, 500);
            f.setVisible(true);
            CtrlChartPanel.this.gui.getDesktop().add(f);
            f.toFront();

            String lineSep = System.getProperty("line.separator"); // NOI18N
            SimpleDateFormat dateFm = new SimpleDateFormat(MoniSoftConstants.HumanDateFormat);
            SimpleDateFormat timeFm = new SimpleDateFormat(MoniSoftConstants.HumanTimeFormat);
            Date d = new Date();
            if (dset instanceof XYDataset) {
                String sep = ";";
                DecimalFormat decFormat = MoniSoftConstants.getDecimalFormat(Integer.parseInt(MoniSoft.getInstance().getApplicationProperties().getProperty("ExportDecimalSeparator")), MoniSoft.getInstance().getApplicationProperties().getProperty("ExportNumberFormat"));

                if (dset instanceof TimeSeriesCollection) {
                    TimeSeriesCollection tsc = (TimeSeriesCollection) dset;
                    TimeSeries ts = tsc.getSeries(0);
                    RegularTimePeriod regTP;
                    String text = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.DATE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.TIME") + sep + tsc.getSeriesKey(0) + System.getProperty("line.separator");
                    sb.append(text);

                    Iterator it = ts.getTimePeriods().iterator();
                    Number value;
                    while (it.hasNext()) {
                        regTP = (RegularTimePeriod) it.next();
                        value = ts.getValue(regTP);
                        if (value == null) {
                            valueString = "";
                        } else {
                            valueString = decFormat.format((Double) value);
                        }
                        d.setTime(regTP.getFirstMillisecond());
                        sb.append(colorString).append(dateFm.format(d)).append(sep).append(timeFm.format(d)).append(sep).append(valueString).append(lineSep);
                    }
                } else if (dset instanceof TimeTableXYDataset) {
                    TimeTableXYDataset ttds = (TimeTableXYDataset) dset;
                    String text = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.DATE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.TIME") + sep + ttds.getSeriesKey(0) + System.getProperty("line.separator");
                    sb.append(text);
                    for (int ii = 0; ii < ttds.getItemCount(0); ii++) {
                        if (ttds.getStartY(0, ii) == null) {
                            valueString = "";
                        } else {
                            valueString = decFormat.format((Double) ttds.getStartY(0, ii));
                        }
                        d.setTime(ttds.getStartX(0, ii).longValue());
                        sb.append(colorString).append(dateFm.format(d)).append(sep).append(timeFm.format(d)).append(sep).append(valueString).append(lineSep);
                    }
                } else if (dset instanceof XYSeriesCollection) { // Scatterplots
                    String label = "";
                    XYSeriesCollection xysc = (XYSeriesCollection) dset;
                    Double x, y;
                    String text = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.DATE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.TIME") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.XVALUE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.YVALUE") + System.getProperty("line.separator");
                    sb.append(text);
                    for (int ii = 0; ii < xysc.getItemCount(0); ii++) {
                        x = (Double) xysc.getX(0, ii);
                        y = (Double) xysc.getY(0, ii);
                        if (y == null) {
                            valueString = "";
                        } else {
                            valueString = decFormat.format(y);
                        }
                        label = labelmap.get(new DataPointObject(x, y)) == null ? "" : labelmap.get(new DataPointObject(x, y));
                        label = label.replace(" ", sep);
                        sb.append(label).append(sep).append(colorString).append(decFormat.format(x)).append(sep).append(valueString).append(lineSep);
                    }
                } else if (dset instanceof DefaultXYZDataset) {
                    String label = "";
                    DefaultXYZDataset xysc = (DefaultXYZDataset) dset;
                    Double x, y;
                    String text = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.DATE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.TIME") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.XVALUE") + sep + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CtrlChartPanel.YVALUE") + System.getProperty("line.separator");
                    sb.append(text);
                    for (int ii = 0; ii < xysc.getItemCount(0); ii++) {
                        x = (Double) xysc.getX(0, ii);
                        y = (Double) xysc.getY(0, ii);
                        if (y == null) {
                            valueString = "";
                        } else {
                            valueString = decFormat.format(y);
                        }
                        // XXX: check if it is ok to reference a possibly null y
                        label = labelmap.get(new DataPointObject(x, y)) == null ? "" : labelmap.get(new DataPointObject(x, y));
                        label = label.replace(" ", sep);
                        sb.append(label).append(sep).append(colorString).append(decFormat.format(x)).append(sep).append(valueString).append(lineSep);
                    }
                }

                try {
                    editor.setText(sb.toString());
                } catch (Exception ex) {
                    Messages.showException(ex);
                }
            }
        }
    }
}
