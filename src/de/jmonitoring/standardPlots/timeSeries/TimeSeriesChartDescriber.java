/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;

import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class TimeSeriesChartDescriber extends ChartDescriber<TimeSeriesLooks> {

    public TimeSeriesChartDescriber(String title, DateInterval dateInterval, List<TimeSeriesLooks> collection) {
    	super(title, collection);
        setDateInterval(dateInterval);
    }
}
