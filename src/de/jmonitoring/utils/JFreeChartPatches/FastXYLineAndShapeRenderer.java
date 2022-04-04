/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.JFreeChartPatches;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author togro
 */
public class FastXYLineAndShapeRenderer extends XYLineAndShapeRenderer implements Cloneable, PublicCloneable, Serializable {
        /**
     * The set with the pixel that have been used for rednering.
     * All bits in the set are set to false when a new series is rendered.
     */
	private final BitSet renderedPixels = new BitSet();

    /**
     * The width and height in Java2D units that a data point should occupy.
     */
    private int shapeSize;

    /**
     * The width in Java2D units that a line should occupy.
     */
    private int lineWidth = 1;

    /**
     * Creates a new renderer.
     */
    public FastXYLineAndShapeRenderer() {
        this(true, true);
    }

    public FastXYLineAndShapeRenderer(boolean lines, boolean shapes) {
        this(lines, shapes, 1);
    }

    public FastXYLineAndShapeRenderer(boolean lines, boolean shapes,int shapeSize) {
        super(lines, shapes);
        this.shapeSize = shapeSize;
    }


    /**
     * Sets the size that a shape occupies when drawn in the data area.
     * @param shapeSize  the size.
     *
     */
    public void setShapeSize(int shapeSize) {
        this.shapeSize = shapeSize;
        fireChangeEvent();
    }

    /**
     * Returns the pixel size.
     * @return The pixel size.
     *
     */
    public int getShapeSize() {
        return this.shapeSize;
    }

    /**
     * Sets the pixel size.
     * @param shapeSize  the size.
     *
     */
    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
        fireChangeEvent();
    }

    /**
     * Returns the pixel size.
     * @return The pixel size.
     *
     */
    public int getLineWidth() {
        return this.lineWidth;
    }

    /**
     * Returns the number of passes through the data that the renderer requires
     * in order to draw the chart.  Most charts will require a single pass, but
     * some require two passes.
     *
     * @return The pass count.
     */
    public int getPassCount() {
        return 1;
    }

    /**
     * Records the state for the renderer.  This is used to preserve state
     * information between calls to the drawItem() method for a single chart
     * drawing.
     */
    public static class State extends XYItemRendererState {

        /** The path for the current series. */
        GeneralPath seriesPath;

        /**
         * A second path that draws vertical intervals to cover any extreme
         * values.
         */
        GeneralPath intervalPath;

        /**
         * The minimum change in the x-value needed to trigger an update to
         * the seriesPath.
         */
        //double dX = 1.0;

        /** The last x-coordinate visited by the seriesPath. */
        //double lastX = Double.NEGATIVE_INFINITY;

        //double lastY = Double.NEGATIVE_INFINITY;

        /** The initial y-coordinate for the current x-coordinate. */
        //double openY = 0.0;

        /** The highest y-coordinate for the current x-coordinate. */
        //double highY = 0.0;

        /** The lowest y-coordinate for the current x-coordinate. */
        //double lowY = 0.0;

        /** The final y-coordinate for the current x-coordinate. */
        //double closeY = 0.0;

	    boolean seriesDrawn = false;
        /**
         * Creates a new state instance.
         *
         * @param info  the plot rendering info.
         */
        public State(PlotRenderingInfo info) {
            super(info);
            setProcessVisibleItemsOnly(false);
        }

        /**
         * This method is called by the {@link XYPlot} at the start of each
         * series pass.  We reset the state for the current series.
         *
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param firstItem  the first item index for this pass.
         * @param lastItem  the last item index for this pass.
         * @param pass  the current pass index.
         * @param passCount  the number of passes.
         */
        public void startSeriesPass(XYDataset dataset, int series,
                int firstItem, int lastItem, int pass, int passCount) {
            this.seriesPath.reset();
            this.intervalPath.reset();
            this.seriesDrawn = false;
            super.startSeriesPass(dataset, series, firstItem, lastItem, pass,
                    passCount);
        }

    }

    /**
     * Initialises the renderer.
     * <P>
     * This method will be called before the first item is rendered, giving the
     * renderer an opportunity to initialise any state information it wants to
     * maintain.  The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return The renderer state.
     */
    public XYItemRendererState initialise(Graphics2D g2,
            Rectangle2D dataArea, XYPlot plot, XYDataset data,
            PlotRenderingInfo info) {

        State state = new State(info);
        double dpi = 72;
    //        Integer dpiVal = (Integer) g2.getRenderingHint(HintKey.DPI);
    //        if (dpiVal != null) {
    //            dpi = dpiVal.intValue();
    //        }
        state.seriesPath = new GeneralPath();
        state.intervalPath = new GeneralPath();
        //state.dX = 72.0 / dpi;
        return state;
    }

    /**
     * Draws the visual representation of a single data item as shape and/or as a line.
     * This method immediately returns if the first item of a series has been rendered!
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {

        State s = (State) state;
		if (s.seriesDrawn) return;
        if (!getItemVisible(series, item)) {
            return;
        }
		s.seriesDrawn = true;
		if(getItemLineVisible(series, item)){
			drawSeriesLine(g2,
                       state,
                       dataArea,
                       info,
                       plot,
                       domainAxis,
                       rangeAxis,
                       dataset,
                       series,
                       crosshairState,
                       pass

			);
		}
		if(getItemShapeVisible(series, item)){
			drawSeriesShapes(g2,
                       state,
                       dataArea,
                       info,
                       plot,
                       domainAxis,
                       rangeAxis,
                       dataset,
                       series,
                       crosshairState,
                       pass

			);
		}
    }



    /**
     * Draws the visual representation of a series as shapes.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    protected void drawSeriesShapes(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         CrosshairState crosshairState,
                         int pass) {
		renderedPixels.clear();
        int xPixels = 0;
        int yPixels = 0;
        double xOrigin = dataArea.getX();
        double yOrigin = dataArea.getY();

        int itemCount = dataset.getItemCount(series);
        PlotOrientation orientation = plot.getOrientation();
       	xPixels = (int)(Math.rint(dataArea.getWidth()) / shapeSize);
        yPixels = (int)(Math.rint(dataArea.getHeight()) / shapeSize);
		ArrayList java2DValues = new ArrayList(itemCount);
		ArrayList dataValues = new ArrayList(itemCount);
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        //collect data to show
		double lowX = domainAxis.getLowerBound();
		double highX = domainAxis.getUpperBound();
		double lowY = rangeAxis.getLowerBound();
		double highY = rangeAxis.getUpperBound();
		for(int itemIndex = itemCount - 1; itemIndex >= 0; itemIndex--){
	        double x = dataset.getXValue(series, itemIndex);
            if(( x >= lowX ) && ( x <= highX )){ //Double.NaN wonŽt come beyond this point
		        double y = dataset.getYValue(series, itemIndex);
		        if( ( y >= lowY ) && ( y <= highY ) ){ //Double.NaN wonŽt come beyond this point
			        double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
			        double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
		            if (orientation == PlotOrientation.HORIZONTAL) {
		                double temp = transX;
		                transX = transY;
		                transY = temp;
		            }
                    int iTransX = (int)(Math.rint((transX - xOrigin)) / shapeSize);
                    int iTransY = (int)(Math.rint((transY - yOrigin)) / shapeSize);
	            	int itemPosition = iTransY * xPixels + iTransX;
	            	if(!renderedPixels.get(itemPosition)){
	            		renderedPixels.set(itemPosition);
	            		java2DValues.add(new double[]{transX, transY});
	            		dataValues.add(new double[]{x, y});
	            	}
		        }
            }
		}
		//draw items
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }
        boolean itemShapeFilled = getItemShapeFilled(series, 0);
        boolean drawOutlines = getDrawOutlines();
        Paint itemFillPaint = getUseFillPaint() ? getItemFillPaint(series, 0) : getItemPaint(series, 0);
        Paint itemOutlinePaint = getUseOutlinePaint() ? getItemOutlinePaint(series, 0) : getItemPaint(series, 0);
        Stroke itemOutlineStroke = getItemOutlineStroke(series, 0);

		for(int i = java2DValues.size() - 1; i >= 0 ; i--){
			double[] points = (double[])java2DValues.get(i);
			double x = points[0];
			double y = points[1];
			Shape shape = getItemShape(series, i);
			shape = ShapeUtilities.createTranslatedShape(shape, x, y);

            if (itemShapeFilled) {
                g2.setPaint(itemFillPaint);
                g2.fill(shape);
            }
            if (drawOutlines) {
                g2.setPaint(itemOutlinePaint);
                g2.setStroke(itemOutlineStroke);
                g2.draw(shape);
            }
	        if (entities != null) {
	        	double[] datapoints = (double[])dataValues.get(i);
	            addEntity(entities, shape, dataset, series, 0, points[0], points[1]);
	        }
		}
        int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
        int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
        if(java2DValues.size() > 0){
	        updateCrosshairValues(crosshairState, ((double[])dataValues.get(0))[0], ((double[])dataValues.get(0))[1], domainAxisIndex,
	                rangeAxisIndex, ((double[])java2DValues.get(0))[0], ((double[])java2DValues.get(0))[1], orientation);
        }
    }

    /**
     * Draws the visual representation of a series as lines.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    protected void drawSeriesLine(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         CrosshairState crosshairState,
                         int pass) {

        State s = (State) state;
        int itemCount = dataset.getItemCount(series);
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
		double lowestVisibleX = domainAxis.getLowerBound();
		double highestVisibleX = domainAxis.getUpperBound();
        double dX = (highestVisibleX - lowestVisibleX) / dataArea.getWidth() * lineWidth;
		double lowestVisibleY = rangeAxis.getLowerBound();
		double highestVisibleY = rangeAxis.getUpperBound();

        double lastX = Double.NEGATIVE_INFINITY;
        double lastY = Double.NEGATIVE_INFINITY;
        double highY = 0.0;
        double lowY = 0.0;
        double closeY = 0.0;
		boolean lastIntervalDone = false;
		boolean currentPointVisible = false;
		boolean lastPointVisible = false;
		boolean lastPointGood = false;
		boolean lastPointInInterval = false;
		for(int itemIndex = 0; itemIndex < itemCount; itemIndex++){
	        double x = dataset.getXValue(series, itemIndex);
	        double y = dataset.getYValue(series, itemIndex);
	        if(!Double.isNaN(x) && !Double.isNaN(y)){
	        	if((Math.abs(x - lastX) > dX)){
	                //in any case, add the interval that we are about to leave to the intervalPath
	                float intervalStartX = 0.0f;
	                float intervalEndX = 0.0f;
	                float intervalStartY = 0.0f;
	                float intervalEndY = 0.0f;
	                float currentX = 0.0f;
	                float currentY = 0.0f;
	                float lastFX = 0.0f;
	                float lastFY = 0.0f;

	                //first set some variables
	                if(orientation == PlotOrientation.VERTICAL){
		                intervalStartX = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
		                intervalEndX = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
		                intervalStartY = (float)rangeAxis.valueToJava2D(lowY, dataArea, yAxisLocation);
		                intervalEndY = (float)rangeAxis.valueToJava2D(highY, dataArea, yAxisLocation);
		                currentX = (float)domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
		                lastFX = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
		                currentY = (float)rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
		                lastFY = (float)rangeAxis.valueToJava2D(closeY, dataArea, yAxisLocation);
	                }
	                else{
		                intervalStartX = (float)rangeAxis.valueToJava2D(lowY, dataArea, yAxisLocation);
		                intervalEndX = (float)rangeAxis.valueToJava2D(highY, dataArea, yAxisLocation);
		                intervalStartY = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
		                intervalEndY = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
		                currentX = (float)rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
		                lastFX = (float)rangeAxis.valueToJava2D(closeY, dataArea, yAxisLocation);
		                currentY = (float)domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
		                lastFY = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
	                }
	                if (lowY < highY) {
		                s.intervalPath.moveTo(intervalStartX, intervalStartY);
		                s.intervalPath.lineTo(intervalEndX, intervalEndY);
	                    lastIntervalDone = true;
	                }

	                //now the series path
                    currentPointVisible = ((x >= lowestVisibleX) && (x <= highestVisibleX) && (y >= lowestVisibleY) && (y <= highestVisibleY));
					if(!lastPointGood){//last point not valid --
						if(currentPointVisible){//--> if the current position is visible move seriesPath cursor to the current position
		                    s.seriesPath.moveTo(currentX, currentY);
						}
					}
					else{//last point valid
						//if the last point was visible and not part of an interval,
						//we have already moved the seriesPath cursor to the last point, either with or without drawingh a line
						//thus we only need to draw a line to the current position
						if(lastPointVisible && !lastPointInInterval){
							s.seriesPath.lineTo(currentX, currentY);
						}
						//if the last point was not visible or part of an interval, we have just stored the y values of the last point
						//and not yet moved the seriesPath cursor. Thus, we need to move the cursor to the last point without drawing
						//and draw a line to the current position.
						else{
							s.seriesPath.moveTo(lastFX, lastFY);
							s.seriesPath.lineTo(currentX, currentY);
						}
					}
					lastPointVisible = currentPointVisible;
	                lastX = x;
	                lastY = y;
	                highY = y;
	                lowY = y;
	                closeY = y;
		        	lastPointInInterval = false;
		        }
				else{
	                lastIntervalDone = false;
		        	lastPointInInterval = true;
	                highY = Math.max(highY, y);
	                lowY = Math.min(lowY, y);
	                closeY = y;
				}
            	lastPointGood = true;
		    }
	        else{
	        	lastPointGood = false;
	        }
        }
        // if this is the last item, draw the path ...
        // draw path, but first check whether we need to complete an interval
        if(!lastIntervalDone){
            if (lowY < highY) {
                float intervalStartX = 0.0f;
                float intervalEndX = 0.0f;
                float intervalStartY = 0.0f;
                float intervalEndY = 0.0f;
                if(orientation == PlotOrientation.VERTICAL){
                    intervalStartX = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
                    intervalEndX = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
                    intervalStartY = (float)rangeAxis.valueToJava2D(lowY, dataArea, yAxisLocation);
                    intervalEndY = (float)rangeAxis.valueToJava2D(highY, dataArea, yAxisLocation);
                }
                else{
                    intervalStartX = (float)rangeAxis.valueToJava2D(lowY, dataArea, yAxisLocation);
                    intervalEndX = (float)rangeAxis.valueToJava2D(highY, dataArea, yAxisLocation);
                    intervalStartY = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
                    intervalEndY = (float)domainAxis.valueToJava2D(lastX, dataArea, xAxisLocation);
                }
                s.intervalPath.moveTo(intervalStartX, intervalStartY);
                s.intervalPath.lineTo(intervalEndX, intervalEndY);
            }
        }
        PathIterator pi = s.seriesPath.getPathIterator(null);
        g2.setStroke(getItemStroke(series, 0));
        g2.setPaint(getItemPaint(series, 0));
        g2.draw(s.seriesPath);
        g2.draw(s.intervalPath);
    }


    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the clone cannot be created.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FastXYLineAndShapeRenderer)) {
            return false;
        }
        FastXYLineAndShapeRenderer that = (FastXYLineAndShapeRenderer)obj;
        if(this.lineWidth != that.lineWidth){
        	return false;
        }
        if(this.shapeSize != that.shapeSize){
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.lineWidth = stream.readInt();
        this.shapeSize = stream.readInt();
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(this.lineWidth);
        stream.writeInt(this.shapeSize);
    }

}
