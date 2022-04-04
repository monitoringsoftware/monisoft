package de.jmonitoring.standardPlots.common;

import de.jmonitoring.base.sensors.SensorProperties;
import java.io.InputStreamReader;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import de.jmonitoring.standardPlots.carpetPlot.CarpetChartDescriber;
import de.jmonitoring.standardPlots.comparePlot.CompareChartDescriber;
import de.jmonitoring.standardPlots.ogivePlot.OgiveChartDescriber;
import de.jmonitoring.standardPlots.scatterPlot.ScatterChartDescriber;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesChartDescriber;
import de.jmonitoring.standardPlots.windPlot.WindChartDescriber;
import de.jmonitoring.Components.DateRangeProvider;
import de.jmonitoring.DataHandling.DataFilter.ValueFilterComponent;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.standardPlots.carpetPlot.CarpetSeriesLooks;
import de.jmonitoring.standardPlots.comparePlot.CompareSeriesLooks;
import de.jmonitoring.standardPlots.ogivePlot.OgiveSeriesLooks;
import de.jmonitoring.standardPlots.scatterPlot.ScatterSeriesLooks;
import de.jmonitoring.standardPlots.timeSeries.TimeSeriesLooks;
import de.jmonitoring.standardPlots.windPlot.WindSeriesLooks;
import de.jmonitoring.utils.UnitCalulation.Unit;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.swing.EDT;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * Class used to load and save chart describers
 *
 * @author togro
 */
public class ChartLoader {

    private final MainApplication gui;
    private final DateRangeProvider dateRangeProvider;

    public ChartLoader(MainApplication gui) {
        this(gui, null);
    }

    public ChartLoader(MainApplication gui, DateRangeProvider dateRangeProvider) {
        this.gui = gui;
        this.dateRangeProvider = dateRangeProvider;
    }

    /**
     * Load the stored xml file from the given stream and draw the chart with a
     * given date interval
     *
     * @param isr The input stream
     * @param customInterval The custom date interval
     */
    public void loadStoredChart(InputStreamReader isr, DateInterval customInterval) {
        XStream xs = new XStream();
        xs.alias("OgiveChartDescriber", OgiveChartDescriber.class);
        xs.alias("OgiveSeriesLooks", OgiveSeriesLooks.class);
        xs.alias("CarpetChartDescriber", CarpetChartDescriber.class);
        xs.alias("CarpetSeriesLooks", CarpetSeriesLooks.class);
        xs.alias("TimeSeriesChartDescriber", TimeSeriesChartDescriber.class);
        xs.alias("TimesSeriesLooks", TimeSeriesLooks.class);
        xs.alias("ScatterPlotDescriber", ScatterChartDescriber.class);
        xs.alias("ScatterChartDescriber", ScatterChartDescriber.class);
        xs.alias("ScatterSeriesLooks", ScatterSeriesLooks.class);
        xs.alias("CompareChartDescriber", CompareChartDescriber.class);
        xs.alias("CompareSeriesLooks", CompareSeriesLooks.class);
        xs.alias("ValueAxis", ValueAxis.class);
        xs.alias("NumberAxis", NumberAxis.class);
        xs.alias("ValueFilterComponent", ValueFilterComponent.class);
        xs.aliasField("Aggregation", TimeSeriesLooks.class, "aggregation");
        xs.aliasField("Aggregation", CarpetSeriesLooks.class, "aggregation");
        xs.aliasField("Aggregation", ScatterSeriesLooks.class, "aggregation");
        xs.aliasField("Aggregation", OgiveSeriesLooks.class, "aggregation");
        xs.aliasField("Aggregation", CompareSeriesLooks.class, "aggregation");
        xs.aliasField("Aggregation", WindSeriesLooks.class, "aggregation");
        xs.alias("Sensor_lfd", Integer.class);
        xs.aliasField("timeSeriesChartCollection", TimeSeriesChartDescriber.class, "chartCollection");
        xs.aliasField("carpetChartCollection", CarpetChartDescriber.class, "chartCollection");
        xs.aliasField("scatterChartCollection", ScatterChartDescriber.class, "chartCollection");
        xs.aliasField("ogiveChartCollection", OgiveChartDescriber.class, "chartCollection");
        xs.aliasField("compareChartCollection", CompareChartDescriber.class, "chartCollection");
        xs.aliasField("windChartCollection", WindChartDescriber.class, "chartCollection");
        xs.omitField(Unit.class, "isUsageUnit");
        xs.omitField(ChartDescriber.class, "legendTitle");
        xs.omitField(SensorProperties.class, "Sensor_lfd");

        String[] s = {"yyyy-MM-dd HH:mm:ss z"};
        xs.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", s));

        Object o = new Object();
        try {
            o = xs.fromXML(isr);
        } catch (Exception e) {
            Messages.showException(e);
        }

        DateInterval interval = customInterval;
        if (interval == null) {
            if (dateRangeProvider.useCustomPeriod()) {
                interval = dateRangeProvider.getCustomdateInterval();
            }
        }

        if (o instanceof ChartDescriber) {
            ChartDescriber desc = (ChartDescriber) o;
            if (dateRangeProvider.useCustomPeriod() || customInterval != null) {
                desc.setDateInterval(interval);
                desc.setUseCustomDateInterval(true);
            }

            // translate old marker entries from decribers which only stored the index of the combobox-entry of the annotation
            if (desc instanceof ScatterChartDescriber) {
                String marker = translateAnnotation(((ScatterChartDescriber) desc).getAreaMarker());
                ((ScatterChartDescriber) desc).setAreaMarker(marker);
            }
            drawChart(desc);
        }
    }

    /**
     * Saves the ChartDesriber for this chart to a file
     *
     * @param describer The describer to be saved
     */
    public void writeChartDescriber(ChartDescriber describer) {
        XStream xstream = new XStream();
        xstream.alias("OgiveChartDescriber", OgiveChartDescriber.class);
        xstream.alias("OgiveSeriesLooks", OgiveSeriesLooks.class);
        xstream.alias("CarpetChartDescriber", CarpetChartDescriber.class);
        xstream.alias("CarpetSeriesLooks", CarpetSeriesLooks.class);
        xstream.alias("TimeSeriesChartDescriber", TimeSeriesChartDescriber.class);
        xstream.alias("TimesSeriesLooks", TimeSeriesLooks.class);
        xstream.alias("ScatterPlotDescriber", ScatterChartDescriber.class);
        xstream.alias("ScatterSeriesLooks", ScatterSeriesLooks.class);
        xstream.alias("ValueAxis", ValueAxis.class);
        xstream.alias("NumberAxis", NumberAxis.class);
        xstream.alias("ValueFilterComponent", ValueFilterComponent.class);

        String[] s = {};
        xstream.registerConverter(new DateConverter("yyyy-MM-dd HH:mm:ss", s));

        try {
            JFileChooser fileChooser = new JFileChooser(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().toLowerCase().endsWith(".gra") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("GRAFIKBESCHREIBUNGEN (*.GRA)");
                }
            });

            int returnVal = fileChooser.showSaveDialog(this.gui.getMainFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // Dateinamen holen
                File file = fileChooser.getSelectedFile();
                if (!file.toString().endsWith(".gra")) {
                    file = new File(file.toString() + ".gra");
                }

                if (!file.exists() || JOptionPane.showConfirmDialog(this.gui.getMainFrame(), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DIE GRAFIKBESCHREIBUNG EXISTIERT BEREITS!ÜBERSCHREIBEN?"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("FRAGE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    // Describer schreiben
                    FileOutputStream fs = new FileOutputStream(file);
                    Writer out = new OutputStreamWriter(fs, "UTF8");
                    BufferedWriter buf = new BufferedWriter(out);
                    xstream.toXML(describer, buf);
                    fs.close();
                    this.gui.setSavedChartCombobox(true);
                }

            }
        } catch (Exception e) {
            Messages.showException(e);
        }
    }

    /**
     * Draw the chart defined by the given describer
     *
     * @param description The chart describer
     */
    private void drawChart(final ChartDescriber description) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                ChartLoader.this.gui.drawChart(description);
            }
        });
    }

    /**
     * Temporary method to restore behaviour of old annotation entries in
     * scatterchartdescribers where the annotation was stored as index of the
     * combobox entry
     *
     * @param areaMarker
     * @return The translated sring
     */
    private String translateAnnotation(String areaMarker) {
        String marker;
        HashMap<String, String> oldMarkerOrder = new HashMap<String, String>();
        oldMarkerOrder.put("0", "Komfortbereich nach DIN ISO 1946 TEIL 2");
        oldMarkerOrder.put("1", "Komfortbereich nach ISO 7730");
        oldMarkerOrder.put("2", "Komfortbereich nach DIN EN 15251");
        oldMarkerOrder.put("3", "Komfortbereich DIN ISO 1946 mit Bielefelder Urteil (\"32/6\"-Regel)");
        oldMarkerOrder.put("4", "Komfortgrenze nach Bielefelder Urteil (\"32/6\"-Regel)");

        marker = oldMarkerOrder.get(areaMarker);
        return marker;
    }
}
