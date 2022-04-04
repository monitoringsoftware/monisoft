package de.jmonitoring.base;

import de.jmonitoring.Components.DateRangeProvider;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.jfree.chart.JFreeChart;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.Components.ProgressBarPanel;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * Interface that defines the common methods for the GUI and the command line
 *
 * @author togro
 */
public interface MainApplication {

    /**
     * Returns the main frame of the application
     *
     * @return
     */
    public Frame getMainFrame();

    /**
     * Returns the desktop area
     *
     * @return
     */
    public JDesktopPane getDesktop();

    /**
     * Returns the panel containing the progress bars
     *
     * @return
     */
    public ProgressBarPanel getProgressBarpanel();

    /**
     * Returns the currently selected internal frame of the desktop area
     *
     * @return
     */
    public JFreeChart getActiveChart();

    /**
     *
     * @returns the {@link CtrlChartPanel} that is in the currently selected
     * frame
     */
    public CtrlChartPanel getActiveCtrlChartPanel();

    /**
     * Invokes the chart generation
     *
     * @param chartDescriber Describer defining the chart to be build
     * @return
     */
    public JFreeChart drawChart(ChartDescriber chartDescriber);

    /**
     * Fills the operation panels (selectors etc. form the settings of an
     * existing chart
     *
     * @param d The chartDecriber
     */
    public void fillPanelFromDescriber(ChartDescriber d);

    /**
     * Invokes the chart generation with the given describer and time interval
     *
     * @param describerFactory
     * @param interval
     * @param looksCollection
     */
    public void drawUsingDescriber(DescriberFactory describerFactory, DateInterval interval, ArrayList<? extends SeriesLooks> looksCollection);

    /**
     * Displays the maintenace chart for the given time interval
     *
     * @param dateInterval The time interval
     * @param sensorID The sensorID of the sensor
     * @param markNegativeCounter If true all suspicious counter values will be preliminarily marked
     */
    public void showMaintenanceChart(DateInterval dateInterval, int sensorID, boolean markNegativeCounter);

    /**
     * Displays the password dialog
     */
    public void showPasswordDialog();

    /**
     * Flag indicationg if the user wants calculations to be traced (logged)
     *
     * @return
     */
    public boolean isLogCalculation();

    /**
     * Closes the given windows in the desktop area
     *
     * @param frame
     */
    public void disposeIFrame(JInternalFrame frame);

    /**
     * Flag indicating if the user has locked the date selectors for the
     * standard plots
     *
     * @param locked
     */
    public void lockDates(boolean locked);

    /**
     * Updates all sensorselectors
     *
     * @param set
     */
    public void updateSensorSelectors(boolean set);

    /**
     * Updated all time period selectors
     */
    public void setIntervalSelectors();

    /**
     * Updates the selector for stored chart descriptions
     *
     * @param set
     */
    public void setSavedChartCombobox(boolean set);
}
