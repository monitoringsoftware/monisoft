/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.comparePlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import java.util.List;


/**
 *
 * @author togro
 */
public class CompareChartDescriber extends ChartDescriber<CompareSeriesLooks> {

    private List timePeriods;
    private int categoryInterval;

    public CompareChartDescriber(String title, List periods, int category, List<CompareSeriesLooks> collection) {
    	super(title, collection);
        timePeriods = periods;
        categoryInterval = category;
    }

    public int getCategory() {
        return categoryInterval;
    }

    public List getTimePeriods() {
        return timePeriods;
    }

//    @Override
//    public DateInterval getDateInterval() {
//        return getDateInterval();
//    }
}
