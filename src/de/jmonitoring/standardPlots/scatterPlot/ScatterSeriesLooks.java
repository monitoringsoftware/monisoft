package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.awt.Color;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to
 * define the appearance of a scatter plot.<p>
 *
 * @author togro
 */
public class ScatterSeriesLooks extends SeriesLooks implements Serializable {

    private Color pointsColor;
    private int pointSize;
    private int pointType;
    private SensorProperties domainSensor;

    /**
     * Creates a new instance of ScatterSeriesLooks with the given id
     */
    public ScatterSeriesLooks(int ident) {
        super(ident);
    }

    /**
     * Get the {@link SensorProperties} of the sensor used for the domain axis
     *
     * @return the sensor
     */
    public SensorProperties getDomainSensor() {
        return domainSensor;
    }

    /**
     * Set the {@link SensorProperties} of the sensor used for the domain axis
     *
     * @param domainSensor
     */
    public void setDomainSensor(SensorProperties domainSensor) {
        this.domainSensor = domainSensor;
    }

    /**
     * Set the color for the points of this series
     *
     * @param c
     */
    public void setPointsColor(Color c) {
        pointsColor = c;
    }

    /**
     * Get the color for the points of this series
     *
     * @return the color
     */
    public Color getPointsColor() {
        return new Color(pointsColor.getRGB());
    }

    /**
     * Set the size for the points of this series
     *
     * @param size
     */
    public void setPointSize(int size) {
        pointSize = size;
    }

    /**
     * Get the size for the points of this series
     *
     * @return the size
     */
    public int getPointSize() {
        return pointSize;
    }

    /**
     * Set the point type of the series
     *
     * @param type
     */
    public void setPointType(int type) {
        pointType = type;
    }

    /**
     * Get the point type of the series
     *
     * @return
     */
    public int getPointType() {
        return pointType;
    }
}
