/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.common;

import java.io.Serializable;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;

import de.jmonitoring.DataHandling.DataFilter.ValueFilter;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * This class represents a chart description which contains settings common to
 * all standard plots.<br> It is used by the system to store the information in
 * a chart and serialize it to a xml file (*.gra) files.
 *
 * @author togro
 */
public abstract class ChartDescriber<SL extends SeriesLooks> implements Serializable {

    private String chartTitle;
    public DateInterval chartDateInterval;
    private List<SL> chartCollection;
    private String plotTitle = ""; //NOI18N
    private Range y1Range = new Range(Double.NaN, Double.NaN);
    private Range y2Range = new Range(Double.NaN, Double.NaN);
    private Range xRange = new Range(Double.NaN, Double.NaN);
    private Integer width = 500;
    private Integer height = 350;
    private ValueAxis domainAxis;
    private ValueAxis leftRangeAxis;
    private ValueAxis rightRangeAxis;
    private String plotSubTitle;
    private ValueFilter valueFilter;
    private boolean markMissing = false;
    private Boolean showLegend = null;
    private Boolean showremarks = null;
    private TextTitle chartRemarkTitle = null;
    private boolean customDateInterval = false;

    /**
     * Constructor for chartdecribers. The title is the main title of the chart
     * and the chartCollection holds a list of the {@link SeriesLooks} to be
     * displayed.
     *
     * @param title The chart title
     * @param chartCollection List of the {@link SeriesLooks}
     */
    public ChartDescriber(String title, List<SL> chartCollection) {
        super();
        this.chartTitle = title;
        this.chartCollection = chartCollection;
    }

    /**
     * Returns the title which is displayed in the internal frame of the chart
     *
     * @return
     */
    public String getInternalFrameTitle() {
        return chartTitle;
    }

    /**
     * Returns the date interval for which data is shown in the chart
     *
     * @return
     */
    public DateInterval getDateInterval() {
        return chartDateInterval;
    }

    /**
     * Returns a list of {@link SeriesLooks} to be shown in the chart
     *
     * @return
     */
    public List<SL> getchartCollection() {
        return chartCollection;
    }

    /**
     * Returns the height of the chart or defaults to 350 pixels if the height
     * is not already set
     *
     * @return
     */
    public Integer getHeight() {
        if (height == null) {
            height = 350;
        }
        return height;
    }

    /**
     * Sets the height of the chart
     * @param height 
     */
    public void setHeight(Integer height) {
        this.height = height;
    }

    /**
     * Returns the width of the chart or defaults to 500 pixels if the width
     * is not already set
     *
     * @return
     */
    public Integer getWidth() {
        if (width == null) {
            width = 500;
        }
        return width;
    }

     /**
     * Sets the width of the chart
     * @param height 
     */
    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getPlotTitle() {
        return plotTitle;
    }

    public void setPlotTitle(String plotTitle) {
        this.plotTitle = plotTitle;
    }

    public void setChartCollection(List<SL> collection) {
        this.chartCollection = collection;
    }

    public void setDateInterval(DateInterval chartDateInterval) {
        this.chartDateInterval = chartDateInterval;
    }

    public void setChartTitle(String chartTitle) {
        this.chartTitle = chartTitle;
    }

    public Range getxRange() {
        return xRange;
    }

    public void setxRange(Range xRange) {
        this.xRange = xRange;
    }

    public Range getY1Range() {
        return y1Range;
    }

    public void setY1Range(Range y1Range) {
        this.y1Range = y1Range;
    }

    public Range getY2Range() {
        return y2Range;
    }

    public void setY2Range(Range y2Range) {
        this.y2Range = y2Range;
    }

    public ValueAxis getDomainAxis() {
        return domainAxis;
    }

    public void setDomainAxis(ValueAxis axis) {
        this.domainAxis = axis;
    }

    public ValueAxis getLeftRangeAxis() {
        return leftRangeAxis;
    }

    public void setLeftRangeAxis(ValueAxis leftRangeAxis) {
        this.leftRangeAxis = leftRangeAxis;
    }

    public ValueAxis getRightRangeAxis() {
        return rightRangeAxis;
    }

    public void setRightRangeAxis(ValueAxis rightRangeAxis) {
        this.rightRangeAxis = rightRangeAxis;
    }

    public String getPlotSubtitle() {
        return plotSubTitle;
    }

    public void setPlotSubtitle(String subtitle) {
        this.plotSubTitle = subtitle;
    }

    public ValueFilter getValueFilter() {
        return valueFilter;
    }

    public void setValueFilter(ValueFilter valueFilter) {
        this.valueFilter = valueFilter;
    }

    public boolean isMarkMissing() {
        return markMissing;
    }

    public void setMarkMissing(boolean markMissing) {
        this.markMissing = markMissing;
    }

    public Boolean isShowLegend() {
        return showLegend;
    }

    public void setShowLegend(boolean showLegend) {
        this.showLegend = showLegend;
    }

    public Boolean isShowRemarks() {
        return showremarks;
    }

    public void setShowRemarks(boolean showRemarks) {
        this.showremarks = showRemarks;
    }

    public TextTitle getChartRemarkTitle() {
        return chartRemarkTitle;
    }

    public void setChartRemarkTitle(TextTitle chartRemarkTitle) {
        this.chartRemarkTitle = chartRemarkTitle;
    }

    public boolean isUseCustomDateInterval() {
        return customDateInterval;
    }

    public void setUseCustomDateInterval(boolean customDateInterval) {
        this.customDateInterval = customDateInterval;
    }
}
