package de.jmonitoring.standardPlots.ogivePlot;

import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.awt.Color;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to
 * define the appearance of a ogive plot.<p>
 *
 * @author togro
 */
public class OgiveSeriesLooks extends SeriesLooks implements Serializable {

    private int timeBase;
    private Color lineColor;
    private boolean flipped;
    private boolean reverse;

    /**
     * Creates a new instance of OgiveSeriesLooks with the given id
     */
    public OgiveSeriesLooks(int ident) {
        super(ident);
    }

    /**
     * Set the line color of the series
     *
     * @param c
     */
    public void setLineColor(Color c) {
        this.lineColor = c;
    }

    /**
     * Get the line color of the series
     *
     * @return the color
     */
    public Color getLineColor() {
        return new Color(lineColor.getRGB());
    }

    /**
     * Set the resolution for the calculation in minutes
     *
     * @param timeBase
     */
    public void setTimeBase(int timeBase) {
        this.timeBase = timeBase;
    }

    /**
     * Get the resolution of the calculation
     *
     * @return the resolution in minutes
     */
    public int getTimeBase() {
        return timeBase;
    }

    /**
     * Get the mode for the axis orientation
     *
     * @return <code>true</code> if flipped
     */
    public boolean getflipAxis() {
        return flipped;
    }

    /**
     * Set the filp mode of the axis, defining the orientation
     *
     * @param flipped
     */
    public void setflipAxis(boolean flipped) {
        this.flipped = flipped;
    }

    /**
     * Get the sorting order for the values
     *
     * @return  <code>true</code> if the order is reversed
     */
    public boolean getReverse() {
        return reverse;
    }

    /**
     * Determine if the values should be displayed in reversed order
     *
     * @param reverse
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
}
