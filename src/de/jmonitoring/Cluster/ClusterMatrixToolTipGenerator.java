/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Cluster;

import java.text.DecimalFormat;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 *
 * @author togro
 */
public class ClusterMatrixToolTipGenerator implements XYToolTipGenerator {

    private DecimalFormat df = new DecimalFormat("0.0#");
    private String rangeLabel = "";
    private String domainLabel = "";

    public ClusterMatrixToolTipGenerator(String rangeLabel, String domainLabel) {
        super();
        this.domainLabel = domainLabel;
        this.rangeLabel = rangeLabel;
    }

    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        return "<html><body><b>" + dataset.getSeriesKey(series) + "</b>: " + dataset.getXValue(series, item) + " " + domainLabel + " mit " + df.format(dataset.getYValue(series, item)) + " " + rangeLabel;
    }
}
