package de.jmonitoring.base;

import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.jfree.chart.JFreeChart;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.Components.ProgressBarPanel;
import de.jmonitoring.standardPlots.carpetPlot.CarpetChart;
import de.jmonitoring.standardPlots.carpetPlot.CarpetChartDescriber;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import de.jmonitoring.standardPlots.comparePlot.CompareChart;
import de.jmonitoring.standardPlots.comparePlot.CompareChartDescriber;
import de.jmonitoring.standardPlots.ogivePlot.OgiveChartDescriber;
import de.jmonitoring.standardPlots.ogivePlot.OgivePlotChart;
import de.jmonitoring.standardPlots.scatterPlot.ScatterChartDescriber;
import de.jmonitoring.standardPlots.scatterPlot.ScatterPlotChart;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChart;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChartDescriber;
import de.jmonitoring.utils.DeepCopyCollection;
import de.jmonitoring.utils.commandLine.MoniSoftCommandline;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.swing.EDT;
import java.io.File;
import org.jfree.chart.ChartUtilities;

/**
 * This calls is used by the commandline (CLI) part of MoniSoft where some
 * methods (mostly thos that influence GUI output) must not called (make no
 * sense) from certain internal methods
 *
 * @author togro
 */
public final class NoOperationGUI implements MainApplication {

    public NoOperationGUI() {
        super();
    }

    @Override
    public boolean isLogCalculation() {
        return false;
    }

    /**
     * There is no progress bar panel on the CLI
     *
     * @return
     */
    @Override
    public ProgressBarPanel getProgressBarpanel() {
        return null;
    }

    /**
     * No frames on th CLI
     *
     * @return
     */
    @Override
    public Frame getMainFrame() {
        return null;
    }

    /**
     * No destktop on the CLI
     *
     * @return
     */
    @Override
    public JDesktopPane getDesktop() {
        return null;
    }

    /**
     * Invoces the chart creation with the given describer. The plott get a
     * reference of {@link NoOperationGUI} that they know how to handle GUI
     * elements
     *
     * @param describer The decriber that defines the chart
     * @return
     */
    @Override
    public JFreeChart drawChart(ChartDescriber describer) {
        EDT.always();
        JFreeChart chart = null;
        JInternalFrame f;

        MoniSoftCommandline commandLine = MoniSoft.getInstance().getCommandLine();

        if (describer instanceof TimeSeriesChartDescriber) {
            f = new TimeSeriesChart((TimeSeriesChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            chart = ((TimeSeriesChart) f).getChart();
            try {
                ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (describer instanceof ScatterChartDescriber) {
            f = new ScatterPlotChart((ScatterChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            chart = ((ScatterPlotChart) f).getChart();
            try {
                ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (describer instanceof OgiveChartDescriber) {
            f = new OgivePlotChart((OgiveChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            chart = ((OgivePlotChart) f).getChart();
            try {
                ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (describer instanceof CompareChartDescriber) {
            f = new CompareChart((CompareChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            chart = ((CompareChart) f).getChart();
            try {
                ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

        } else if (describer instanceof CarpetChartDescriber) {
            f = new CarpetChart((CarpetChartDescriber) new DeepCopyCollection().makeDeepCopy(describer), this);

            chart = ((CarpetChart) f).getChart();
            try {
                ChartUtilities.saveChartAsPNG(new File(commandLine.getOutputFileName()), chart, describer.getWidth(), describer.getHeight());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

        return chart;
    }

    /**
     * No frames on th CLI
     *
     * @return
     */
    @Override
    public void disposeIFrame(JInternalFrame carpetChart) {
    }

    /**
     * No active chart on th CLI
     *
     * @return
     */
    @Override
    public JFreeChart getActiveChart() {
        return null;
    }

    /**
     * No active chartpanel on th CLI
     *
     * @return
     */
    @Override
    public CtrlChartPanel getActiveCtrlChartPanel() {
        return null;
    }

    @Override
    public void drawUsingDescriber(DescriberFactory describerFactory, DateInterval interval, ArrayList<? extends SeriesLooks> looksCollection) {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void lockDates(boolean locked) {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void fillPanelFromDescriber(ChartDescriber d) {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void updateSensorSelectors(boolean set) {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void setIntervalSelectors() {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void setSavedChartCombobox(boolean set) {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void showPasswordDialog() {
    }

    /**
     * Not applicable from CLI
     *
     * @param locked
     */
    @Override
    public void showMaintenanceChart(DateInterval dateInterval, int sensorID, boolean markNegative) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
