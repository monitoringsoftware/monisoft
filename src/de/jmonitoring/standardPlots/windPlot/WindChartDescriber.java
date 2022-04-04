/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.windPlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;

import de.jmonitoring.standardPlots.windPlot.WindSeriesLooks;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class WindChartDescriber extends ChartDescriber<WindSeriesLooks> {

    public WindChartDescriber(String title, DateInterval dateInterval, List<WindSeriesLooks> collection) {
    	super(title, collection);
        setDateInterval(dateInterval);
    }
}
