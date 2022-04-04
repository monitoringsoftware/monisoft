package de.jmonitoring.standardPlots.carpetPlot;

import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to
 * define the appearance of a carpet plot.<p>
 *
 * @author togro
 */
public class CarpetSeriesLooks extends SeriesLooks implements Serializable {

    private double scaleMin = -999999999d;
    private double scaleMax = -999999999d;

    /**
     * Creates a new instance of CarpetSeriesLooks with the given id
     */
    public CarpetSeriesLooks(int id) {
        super(id);
    }

    /**
     * Set the minimum value for the color scale
     *
     * @param value
     */
    public void setScaleMin(double value) {
        scaleMin = value;
    }

    /**
     * Returns the minimum value for the color scale
     *
     * @return the minimum
     */
    public double getScaleMin() {
        return scaleMin;
    }

    /**
     * Set the maximum value for the color scale
     *
     * @param value
     */
    public void setScaleMax(double value) {
        scaleMax = value;
    }

    /**
     * Returns the maximum value for the color scale
     *
     * @return the maximum
     */
    public double getScaleMax() {
        return scaleMax;
    }
}
