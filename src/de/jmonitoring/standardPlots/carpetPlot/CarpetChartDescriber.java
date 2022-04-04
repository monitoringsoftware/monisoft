/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmonitoring.standardPlots.carpetPlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;

import de.jmonitoring.standardPlots.carpetPlot.CarpetSeriesLooks;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class CarpetChartDescriber extends ChartDescriber<CarpetSeriesLooks> {

    public CarpetChartDescriber(String title, DateInterval dateInterval, List<CarpetSeriesLooks> collection) {
    	super(title, collection);
        setDateInterval(dateInterval);
    }

//    @Override
//    public DateInterval getDateInterval() {
//        return getDateInterval();
//    }
}
