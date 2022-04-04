package de.jmonitoring.utils.AnnotationEditor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.annotations.XYPolygonAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * A {@link JPanel}-class holding a chart which is used to display end edit
 * annotions.<p>It also holds all methods to manipulate the shown annotations
 * and data points.
 *
 * @author togro
 */
public class AnnotationDesignerChart extends javax.swing.JPanel {

    private final ChartPanel chartPanel; // the chartPanel holding the chart
    private final AnnotationDesigner parent; // the {@link AnnotationDesigner} holding this chart
    private XYPlot plot; // the plot which is shown in the chartPanel
    private boolean locked = false; // a flag indicating if the chart is locked (when the polygon is closed)
    private final DecimalFormat df = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
    private final Color openColor = new Color(197, 0, 0); //  color for open polygon
    private final Color closedColor = new Color(10, 182, 5); //  color closed polygon

    /**
     * Creates new form AnnotationDesignerPanel this the given
     * {@link AnnotationDesigner} as parent
     */
    public AnnotationDesignerChart(final AnnotationDesigner parent) {
        initComponents();
        setLayout(new BorderLayout());
        final JFreeChart chart = createEmptyChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setPopupMenu(null);
        chartPanel.setMouseZoomable(true);
        chartPanel.setMaximumDrawHeight(2000);
        chartPanel.setMaximumDrawWidth(2000);
        this.parent = parent;

        chartPanel.addChartMouseListener(new ChartMouseListener() {
            @Override
            /**
             * If a mousebutton was clicked on the chart add a new point at the
             * pointer position
             */
            public void chartMouseClicked(ChartMouseEvent event) {
                if (parent.isPreview()) {
                    return;
                }
                Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) chart.getPlot();
                Double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                Double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());
                setClosed(addDataPoint(chartX, chartY));
                setX(chartX);
                setY(chartY);
            }

            @Override
            /**
             * If the mouse pointer is moved over the chart, detect if it is
             * near an existing point and if so hightlight that point
             */
            public void chartMouseMoved(ChartMouseEvent event) {
                Point2D p = chartPanel.translateScreenToJava2D(event.getTrigger().getPoint());
                Rectangle2D plotArea = chartPanel.getScreenDataArea();
                XYPlot plot = (XYPlot) chart.getPlot();
                Double chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea, plot.getDomainAxisEdge());
                Double chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea, plot.getRangeAxisEdge());

                setX(chartX);
                setY(chartY);

                // detect if we are in the near an existsing point and highlight it
                // only if not in locked mode
                if (!locked) {
                    Double smallesDistance = Double.MAX_VALUE;
                    Double xRange = plot.getDomainAxis().getRange().getUpperBound() - plot.getDomainAxis().getRange().getLowerBound();
                    Double yRange = plot.getRangeAxis().getRange().getUpperBound() - plot.getRangeAxis().getRange().getLowerBound();
                    Double limit = yRange / 150;
                    XYSeries series = getSeries();
                    double[][] points = series.toArray();
                    int indexOfsmallest = -1;
                    for (int i = 0; i < series.getItemCount(); i++) {
                        Double x2 = points[0][i];
                        Double y2 = points[1][i];
                        Double distance = Math.sqrt((x2 - chartX) * (x2 - chartX) + (y2 - chartY) * (y2 - chartY));
                        if (distance < smallesDistance) {
                            indexOfsmallest = i;
                            smallesDistance = distance;
                        }
                    }
                    if (smallesDistance < limit) {
                        highLightPoint(points[0][indexOfsmallest], points[1][indexOfsmallest], xRange / 70, yRange / 70);
                    } else {
                        clearHighLight();
                    }
                }
            }
        });

        add(chartPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(500, 500));
    }

    /**
     * Tells the parent {@link AnnotationDesigner} if the polygon was closed or
     * not
     *
     * @param b
     */
    public void setClosed(boolean b) {
        this.parent.setAnnotationClosed(b);
    }

    /**
     * Zoom to a level where all elements, whether as data points or as
     * {@link XYAnnotation}, are visible
     */
    public void zoomAll() {
        double xMin = Double.MAX_VALUE;
        double xMax = -Double.MAX_VALUE;
        double yMin = Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;


        // loop the annotations
        for (Object o : getRenderer().getAnnotations()) {
            if (o instanceof XYPolygonAnnotation) {
                XYPolygonAnnotation pa = (XYPolygonAnnotation) o;
                double[] pCoords = pa.getPolygonCoordinates();
                for (int i = 0; i < pCoords.length; i = i + 2) {
                    double x = pCoords[i];
                    double y = pCoords[i + 1];
                    xMin = probeMinimum(xMin, x);
                    xMax = probeMaximum(xMax, x);
                    yMin = probeMinimum(yMin, y);
                    yMax = probeMaximum(yMax, y);
                }

            } else {
                MoniSoftLineAnnotation la = (MoniSoftLineAnnotation) o;
                Double[] lCoords = la.getCoordinates();
                for (int i = 0; i < lCoords.length; i = i + 2) {
                    double x = lCoords[i];
                    double y = lCoords[i + 1];
                    xMin = probeMinimum(xMin, x);
                    xMax = probeMaximum(xMax, x);
                    yMin = probeMinimum(yMin, y);
                    yMax = probeMaximum(yMax, y);
                }
            }
        }

        // loop the datapoits
        Double[][] dataPoints = getDataPoints();
        for (int i = 0; i < dataPoints.length; i++) {
            double x = dataPoints[i][0];
            double y = dataPoints[i][1];
            xMin = probeMinimum(xMin, x);
            xMax = probeMaximum(xMax, x);
            yMin = probeMinimum(yMin, y);
            yMax = probeMaximum(yMax, y);
        }

        if (xMin == Double.MAX_VALUE || xMax == -Double.MAX_VALUE) {
            xMin = 0;
            xMax = 20;
        }
        if (yMin == Double.MAX_VALUE || yMax == -Double.MAX_VALUE) {
            yMin = 0;
            yMax = 20;
        }

        double yOffset = (yMax - yMin) * 0.1;
        double xOffset = (xMax - xMin) * 0.1;

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(false);
        rangeAxis.setUpperBound(yMax + yOffset);
        rangeAxis.setLowerBound(yMin - yOffset);
        rangeAxis.setAutoTickUnitSelection(true);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(false);
        domainAxis.setUpperBound(xMax + xOffset);
        domainAxis.setLowerBound(xMin - xOffset);
        domainAxis.setAutoTickUnitSelection(true);
    }

    /**
     * Compares to values and returns the less one
     *
     * @param v1
     * @param v2
     * @return the lower of the two values
     */
    private double probeMinimum(double v1, double v2) {
        if (v1 > v2) {
            return v2;
        }
        return v1;
    }

    /**
     * Compares to values and returns the greater one
     *
     * @param v1
     * @param v2
     * @return the higher of the two values
     */
    private double probeMaximum(double v1, double v2) {
        if (v1 < v2) {
            return v2;
        }
        return v1;
    }

    /**
     * Shows the given {@link XYAnnotation} in the chart.
     *
     * @param annotation
     */
    public void plotAnnotation(XYAnnotation annotation) {
        getRenderer().addAnnotation(annotation);
    }

    /**
     * Removes all annotations from the plot
     */
    public void clearAnnotations() {
        getRenderer().removeAnnotations();
    }

    /**
     * Clears the data series and removes all point markers the plot
     */
    public void clearData() {
        XYSeries series = getSeries();
        series.clear();
        setRendererColor(openColor);
        series.fireSeriesChanged();
        locked = false;
        clearHighLight();
        initAxis();
    }

    /**
     * Initializes the axes
     */
    public void initAxis() {
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("Dialog", 0, 8));
        rangeAxis.setAutoRange(true);
        rangeAxis.setRange(0, 20);
        rangeAxis.setAutoTickUnitSelection(true);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Dialog", 0, 8));
        domainAxis.setAutoRange(true);
        domainAxis.setRange(0, 20);
        domainAxis.setAutoTickUnitSelection(true);
    }

    /**
     * Sets scaling of both axes to auto
     */
    public void adjust() {
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setAutoRange(true);
        rangeAxis.setAutoTickUnitSelection(true);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setAutoRange(true);
        domainAxis.setAutoTickUnitSelection(true);
    }

    /**
     * Removes the last point of the data series
     */
    public void undo() {
        XYSeries series = getSeries();
        if (series.getItemCount() == 0) {
            return;
        }
        setRendererColor(openColor);
        series.remove(series.getItemCount() - 1);
        series.fireSeriesChanged();
        locked = false; // if a point was removed the polygon can no longer be closed
    }

    /**
     * Puts the crosshairs on the given point<p> If the position was changed
     * also update the parents selection in the coordinate list
     *
     * @param x
     * @param y
     */
    public void markPoint(Double x, Double y) {
        boolean same = false;
        if (x != plot.getDomainCrosshairValue() && y != plot.getRangeCrosshairValue()) {
            same = true;
        }
        plot.setDomainCrosshairValue(x);
        plot.setRangeCrosshairValue(y);

        if (!same) {
            Integer index = getIndexOfPoint(x, y);
            if (index != null) {
                parent.setListSelection(index);
            }
        }
    }

    /**
     * Toggles crosshair visibility
     *
     * @param show
     */
    public void showCrosshairs(boolean show) {
        plot.setDomainCrosshairVisible(show);
        plot.setRangeCrosshairVisible(show);
    }

    /**
     * Returns the data series of the current plot
     *
     * @return
     */
    private XYSeries getSeries() {
        XYSeriesCollection dataset = (XYSeriesCollection) plot.getDataset();
        return dataset.getSeries(0);
    }

    /**
     * Adds the given point to the data series.<p> Checks if the point is
     * already in the series and if so locks the plot and sets the color for a
     * closed polygon.<br> The parent is called to add the new point.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean addDataPoint(Double x, Double y) {
        if (locked) {
            return true;
        }
        Double[] oldPoint = isDuplicate(x, y);
        if (oldPoint != null) {
            x = oldPoint[0];
            y = oldPoint[1];

            setRendererColor(closedColor);
            clearHighLight();
            locked = true;

        }
        XYSeries series = getSeries();
        Double roundedX = Double.valueOf(df.format(x));
        Double roundedY = Double.valueOf(df.format(y));
        series.add(roundedX, roundedY);
        series.fireSeriesChanged();
        this.parent.addToList(roundedX, roundedY);

        return locked;
    }

    /**
     * Updates the point with the given index to the given coordinates.
     *
     * @param index
     * @param x
     * @param y
     */
    public void update(int index, Double x, Double y) {
        XYSeries series = getSeries();
        int size = series.getItemCount();
        double[][] points = series.toArray();
        points[0][index] = x;
        points[1][index] = y;
        series.clear();
        for (int i = 0; i < size; i++) {
            series.add(points[0][i], points[1][i]);
        }
    }

    /**
     * Returns the point of the current data series.
     *
     * @return the data points
     */
    public Double[][] getDataPoints() {
        XYSeries series = getSeries();
        Double[][] points = new Double[series.getItemCount()][2];
        for (int i = 0; i < series.getItemCount(); i++) {
            points[i][0] = series.getDataItem(i).getXValue();
            points[i][1] = series.getDataItem(i).getYValue();
        }
        return points;
    }

    /**
     * Checks if the given point is near a existsing point in the series by
     * looping all points.<br> If the point is closer to an existing point than
     * a calculated limit is returns the coordinates of that existing point so
     * that the line will 'lock' to that point.<br> If given point is not close
     * to a existsing point
     * <code>null</code> is returned.
     *
     * @param x1
     * @param y1
     * @return the coordinates of an existing point within certain limits * * *
     * or <code>null</code> if there is no point close enough.
     */
    private Double[] isDuplicate(Double x1, Double y1) {
        Double[] point = new Double[2];
        XYSeries series = getSeries();
        double[][] points = series.toArray();
        Double smallesDistance = Double.MAX_VALUE;
        Double range = plot.getRangeAxis().getRange().getUpperBound() - plot.getRangeAxis().getRange().getLowerBound();
        Double limit = range / 150;
        for (int i = 0; i < series.getItemCount(); i++) {
            Double x2 = points[0][i];
            Double y2 = points[1][i];
            Double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
            if (distance < smallesDistance) {
                point[0] = x2;
                point[1] = y2;
                smallesDistance = distance;
            }
        }
        if (smallesDistance < limit) {
            return point;
        }
        return null;
    }

    /**
     * Hightlights the given point by drawing a rectangle {@link
     * XYBoxAnnotation} of the given size around it.
     *
     * @param x
     * @param y
     * @param xSize
     * @param ySize
     */
    private void highLightPoint(Double x, Double y, Double xSize, Double ySize) {
        XYBoxAnnotation bestBid = new XYBoxAnnotation(x - xSize, y - ySize, x + xSize, y + ySize, new BasicStroke(1.0f), Color.BLACK);
        plot.addAnnotation(bestBid);
    }

    /**
     * Removes all hightlights
     */
    private void clearHighLight() {
        plot.clearAnnotations();
    }

    /**
     * Return the index of the given point.
     *
     * @param x
     * @param y
     * @return the index of the point or <code>null</code> if there is no such
     * point
     */
    private Integer getIndexOfPoint(Double x, Double y) {
        XYSeries series = getSeries();
        int size = series.getItemCount();
        double[][] points = series.toArray();
        for (int i = 0; i < size; i++) {
            if (points[0][i] == x && points[1][i] == y) {
                return i;
            }
        }
        return null;
    }

    /**
     * Creates the a new empty plot and puts it to the chartPanel.
     *
     * @return
     */
    private JFreeChart createEmptyChart() {
        XYSeries data = new XYSeries("", false, true);
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(data);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "", // title
                "", "", // axis labels
                dataset, // dataset
                PlotOrientation.VERTICAL,
                false, // legend?
                false, // tooltips?
                false // URLs?
                );

        chart.setBackgroundPaint(Color.white);
        plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        float dash[] = {4.0f};
        plot.setRangeCrosshairStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        plot.setDomainCrosshairStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainZeroBaselineVisible(true);
        plot.setRangeZeroBaselineVisible(true);

        initAxis();
        initRenderer();

        return chart;
    }

    /**
     * Get the renderer of the plot
     *
     * @return the renderer
     */
    private XYLineAndShapeRenderer getRenderer() {
        return (XYLineAndShapeRenderer) plot.getRenderer();
    }
    /**
     * A Progesslistener which detects if the rendering is finished and marks
     * the point at the mouse position Currently unused
     */
    private final ChartProgressListener chartProgessListener = new ChartProgressListener() {
        @Override
        public void chartProgress(ChartProgressEvent event) {            
            if (event.getType() == ChartProgressEvent.DRAWING_FINISHED && locked) {
                markPoint(plot.getDomainCrosshairValue(), plot.getRangeCrosshairValue());
            }
        }
    };

    /**
     * Sets the color for the renderer
     *
     * @param c
     */
    private void setRendererColor(Color c) {
        XYLineAndShapeRenderer renderer = getRenderer();
        renderer.setSeriesPaint(0, c);
    }

    /**
     * Initialises the renderer
     */
    private void initRenderer() {
        Shape shape = new Ellipse2D.Double(-3, -3, 6, 6);
        XYLineAndShapeRenderer renderer = getRenderer();
        renderer.setSeriesShape(0, shape);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, openColor);
        float dash[] = {8.0f};
        renderer.setSeriesStroke(0, new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
    }

    /**
     * Sets the live x coordinate of the parent {@link AnnotationDesigner}
     *
     * @param d
     */
    private void setX(Double d) {
        this.parent.setLiveX(d);
    }

    /**
     * Sets the live y coordinate of the parent {@link AnnotationDesigner}
     *
     * @param d
     */
    private void setY(Double d) {
        this.parent.setLiveY(d);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
