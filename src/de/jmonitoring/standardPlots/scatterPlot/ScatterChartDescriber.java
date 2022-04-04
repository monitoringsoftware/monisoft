/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;

import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * The describer used for scatter plots
 *
 * @author togro
 */
public class ScatterChartDescriber extends ChartDescriber<ScatterSeriesLooks> {

    private double aggregation;
    private boolean usePower;
    private SensorProperties domainProps;
    private String marker;

    /**
     * Generate a new descirber using
     *
     * @param title The chart title
     * @param dateInterval The date interval
     * @param collection A collection of series looks to be shown
     * @param useDomainPower Flag indicationg if power should be used on the
     * domain axis
     * @param areaMarker Any area markers to be put in the backgound of the
     * chart
     */
    public ScatterChartDescriber(String title, DateInterval dateInterval, List<ScatterSeriesLooks> collection, boolean useDomainPower, String areaMarker) {
        super(title, collection);
        setDateInterval(dateInterval);
        aggregation = collection.get(0).getAggregation();
        usePower = useDomainPower;
        domainProps = collection.get(0).getDomainSensor();
        marker = areaMarker;
    }

    public double getAggregation() {
        return aggregation;
    }

    public SensorProperties getDomainSensorProps() {
        return domainProps;
    }

    public boolean getUsePower() {
        return usePower;
    }

    public String getAreaMarker() {
        return marker;
    }

    public void setAreaMarker(String m) {
        marker = m;
    }
}
