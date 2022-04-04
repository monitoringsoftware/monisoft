/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.ogivePlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;

import de.jmonitoring.utils.intervals.DateInterval;

/**
 * A {@link ChartDescriber} for ogive charts
 *
 * @author togro
 */
public class OgiveChartDescriber extends ChartDescriber<OgiveSeriesLooks> {

    public OgiveChartDescriber(String title, DateInterval dateInterval, List<OgiveSeriesLooks> collection) {
        super(title, collection);
        setDateInterval(dateInterval);
    }
}
