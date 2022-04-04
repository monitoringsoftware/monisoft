/*
 * CarpetPlotMaker.java
 *
 * Created on 19. August 2007, 21:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.windPlot;

import java.util.List;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PolarPlot;

import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author Thomas
 */
public class WindPlotMaker {

    PolarPlot plot;
    double rangeMin, rangeMax;
    NumberAxis rangeAxis = new NumberAxis();
    WindSeriesLooks series;

    /** Creates a new instance of CarpetPlotMaker */
    public WindPlotMaker(WindChartDescriber describer) {
        List<WindSeriesLooks> seriesCollection = describer.getchartCollection();
        DateInterval dateInterval = describer.getDateInterval();
        series = seriesCollection.get(0); // Zur Zeit nur ein Sensor verwendet
//        GenerateWindDataset sSet = new GenerateWindDataset(series, dateInterval, series.getAggregation());
        DateAxis xAxis = new DateAxis("Datum");
        String title = "";

        xAxis.setRange(dateInterval.getStartDate(), dateInterval.getEndDate());


        plot = new PolarPlot();

        // Titel setzten
        if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
            describer.setPlotTitle(SensorInformation.getDisplayName(series.getSensorID()));
        } else {
            title = describer.getPlotTitle();
        }
    }

    public PolarPlot getPlotInfo() {
        return plot;
    }

    public double getMin() {
        return rangeMin;
    }

    public double getMax() {
        return rangeMax;
    }
}
