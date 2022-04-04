/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.comparePlot;

import java.util.List;

import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;

import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.SparselyLabeledCategoryAxis;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.intervals.MonthInterval;
import de.jmonitoring.utils.intervals.WeekInterval;
import de.jmonitoring.utils.intervals.YearInterval;

/**
 *
 * @author togro
 */
public class ComparePlotMaker {

    private CategoryPlot plot;
    private GenerateCompareDataSet compSet;
    private CompareSeriesLooks series;
    String titlePeriod = ""; //NOI18N
    String titleCategory = ""; //NOI18N

    public ComparePlotMaker(CompareChartDescriber describer, DatasetWorkerFactory workerFactory) {
        List<CompareSeriesLooks> seriesCollection = describer.getchartCollection();
        List timePeriods = describer.getTimePeriods();
        int category = describer.getCategory();
        String title;
        series = seriesCollection.get(0); // nur eine Serie in Liste (zur Zeit - immer nur ein MessP auswählbar)
        compSet = new GenerateCompareDataSet(timePeriods, series, category, series.getPowerWanted(), workerFactory);

        if (timePeriods.get(0) instanceof DateInterval) {
            titlePeriod = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("COMPARE_DAY") + " "; //NOI18N
        } else if (timePeriods.get(0) instanceof WeekInterval) {
            titlePeriod = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("COMPARE_WEEK") + " ";//NOI18N
        } else if (timePeriods.get(0) instanceof MonthInterval) {
            titlePeriod = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("COMPARE_MONTH") + " ";//NOI18N
        } else if (timePeriods.get(0) instanceof YearInterval) {
            titlePeriod = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("COMPARE_YEAR") + " ";//NOI18N
        }

        switch (category) {
            case MoniSoftConstants.HOUR_CATEGORY:
                titleCategory = " (" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HOURLY");//NOI18N
                break;
            case MoniSoftConstants.DAY_CATEGORY:
                titleCategory = " (" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DAILY");//NOI18N
                break;
            case MoniSoftConstants.WEEK_CATEGORY:
                titleCategory = " (" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("WEEKLY");//NOI18N
                break;
            case MoniSoftConstants.MONTH_CATEGORY:
                titleCategory = " (" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MONTHLY");//NOI18N
                break;
            case MoniSoftConstants.YEAR_CATEGORY:
                titleCategory = " (" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("YEARLY");//NOI18N
                break;
        }


        // Titel
        if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
            title = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft.CompareTab.TabConstraints.tabTitle") + " " + SensorInformation.getDisplayName(series.getSensorID());  // Voreinstellung wenn noch kein Titel in describer festgelegt
            describer.setPlotTitle(title);
        } else {
            title = describer.getPlotTitle();
        }

//        AreaRenderer renderer = new AreaRenderer();
        LineAndShapeRenderer renderer = new LineAndShapeRenderer();
        NumberAxis rangeAxis = new NumberAxis();
        if (series.getPowerWanted()) {
            rangeAxis.setLabel(new UnitTransfer().getPowerUnitFromConsumption(seriesCollection.get(0).getUnit()));
        } else {
            rangeAxis.setLabel(seriesCollection.get(0).getUnit());
        }
        SparselyLabeledCategoryAxis domainAxis = new SparselyLabeledCategoryAxis(40);
        domainAxis.setCategoryMargin(0);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        plot = new CategoryPlot(compSet.getDataset(), domainAxis, rangeAxis, renderer);
        plot.setDomainGridlinesVisible(true);
        plot.setForegroundAlpha(0.8f);
    }

    public CategoryPlot getPlotInfo() {
        return plot;
    }
}
