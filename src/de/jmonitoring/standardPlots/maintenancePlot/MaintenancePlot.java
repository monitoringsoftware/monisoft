/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.maintenancePlot;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.DateBandAxis;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.XYShapeStepRenderer;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.swing.JProgressBar;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.time.*;
import org.jfree.ui.Layer;

/**
 *
 * @author togro
 */
public class MaintenancePlot {

    XYPlot plot = new XYPlot();
    SimpleDateFormat MySQLDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
    static JProgressBar progressBar;
    StoppableThread thisThread = (StoppableThread) Thread.currentThread();
    private final BasicStroke stroke = new BasicStroke(0.5f);

    public MaintenancePlot(DateInterval dateInterval, int sensorID, JProgressBar progress) {
        progressBar = progress;
        TimeSeries sensorSeries = new TimeSeries(SensorInformation.getDisplayName(sensorID));
        TimeSeries ignoreSeries = new TimeSeries(SensorInformation.getDisplayName(sensorID) + "_ignore");
        Messages.showMessage("Lese " + SensorInformation.getDisplayName(sensorID) + " aus Datenbank" + "\n", true);
        ArrayList markerArray = new ArrayList(500);
        Connection myConn = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            myConn = DBConnector.openConnection();

            if (SensorInformation.getSensorProperties(sensorID).isEvent()) { // Event
                stmt = myConn.prepareStatement("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = ? and TimeStart >= ? and TimeStart <= ? order by TimeStart");
                stmt.setInt(1, sensorID);
                stmt.setString(2, dateInterval.getStartDateString(new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat)));
                stmt.setString(3, dateInterval.getEndDateString(new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat)) + " 23:59:59");
                rs = stmt.executeQuery();
                rs.last();
                int maxRow = rs.getRow();
                rs.beforeFirst();
                progressBar.setMinimum(0);
                progressBar.setMaximum(maxRow);
                progressBar.setValue(0);
                while (rs.next() && thisThread.running) {
                    try {
                        sensorSeries.add(new Second(new Date(MySQLDateTimeFormat.parse(rs.getString(1)).getTime())), rs.getDouble(3), false);
                    } catch (SeriesException e) {
                    }
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            } else { // Normaler Messwert
                long startSecond = dateInterval.getStartDate().getTime() / 1000L;
                long endSecond = dateInterval.getEndDate().getTime() / 1000L;
                stmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors=? and hist.TimeStamp >= ? and hist.TimeStamp <= ?");
                stmt.setInt(1, sensorID);
                stmt.setLong(2, startSecond);
                stmt.setLong(3, endSecond);
                rs = stmt.executeQuery();
                rs.last();
                int maxRow = rs.getRow();
                rs.beforeFirst();
                progressBar.setMinimum(0);
                progressBar.setMaximum(maxRow);
                progressBar.setValue(0);
                while (rs.next() && thisThread.running) {
                    if (rs.getInt(3) == MoniSoftConstants.LOG_INVALID) {
                        markerArray.add(new ValueMarker(rs.getLong(1) * 1000, Color.RED, stroke, null, null, 0.5f)); // wenn ja: invalid marker setzten
                    }
                    try {
                        sensorSeries.add(new Second(new Date(rs.getLong(1) * 1000)), rs.getDouble(2), false);
                    } catch (SeriesException e) {
                    }
                    progressBar.setValue(progressBar.getValue() + 1);
                }
            }


            // Wurde inzwischen der Thread abgebrochen?
            if (!thisThread.running) {
                return;
            }
            Messages.showMessage("Generiere Plot" + "\n", true);
            TimeSeriesCollection dataset = new TimeSeriesCollection(sensorSeries);
            TimeSeriesCollection ignoreDataset = new TimeSeriesCollection(ignoreSeries);

            plot.setDataset(dataset);
            plot.setDataset(1, ignoreDataset);



            // Renderer setzen
            if (SensorInformation.getSensorProperties(sensorID).isEvent()) { // Messpunkt ist ein event
//                XYStepAreaRenderer stepper = new XYStepAreaRenderer();
                XYShapeStepRenderer stepper = new XYShapeStepRenderer();
                stepper.setSeriesLinesVisible(0, true);
                stepper.setSeriesShapesVisible(0, true);
                stepper.setSeriesPaint(0, new Color(61, 134, 44));
                stepper.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4.0, 4.0));
                stepper.setBaseToolTipGenerator(null);
                stepper.setDrawSeriesLineAsPath(true);
                stepper.setSeriesCreateEntities(0, false);
                plot.setRenderer(0, stepper);
            } else { // "Normale" Werte
                XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
                renderer.setSeriesLinesVisible(0, true);
                renderer.setSeriesShapesVisible(0, true);
                renderer.setDrawOutlines(false);
                renderer.setUseFillPaint(false);
                renderer.setSeriesPaint(0, new Color(61, 134, 44));
                renderer.setSeriesShape(0, new Ellipse2D.Double(-2, -2, 4.0, 4.0));
                renderer.setBaseToolTipGenerator(null);
                renderer.setDrawSeriesLineAsPath(true);
                renderer.setSeriesCreateEntities(0, false);
                plot.setRenderer(0, renderer);
            }

            // Ignore-Werte (leerer Renderer)
            XYLineAndShapeRenderer ignoreRenderer = new XYLineAndShapeRenderer();
            plot.setRenderer(1, ignoreRenderer);

            // Für alle Werte mit Log-Eintrag den Marker setzen
            Iterator<ValueMarker> it = markerArray.iterator();
            while (it.hasNext()) {
                plot.addDomainMarker(1, it.next(), Layer.BACKGROUND, false);
            }


            // Zeitachse mit verschiedenen Bändern hinzufügen
            ValueAxis domainAxis = new DateBandAxis(dateInterval, Day.class, false, true, true, true, 9);
            domainAxis.setTickMarksVisible(false);
            NumberAxis rangeAxis = new NumberAxis(SensorInformation.getSensorProperties(sensorID).getSensorUnit().getUnit());
            rangeAxis.setLabelAngle(Math.PI / 2);
            rangeAxis.setAutoTickUnitSelection(true);
            rangeAxis.setAutoRangeIncludesZero(false);
            plot.setDomainAxis(domainAxis);
            plot.setRangeAxis(rangeAxis);
            plot.setRangeZeroBaselineVisible(true);

            plot.setRangeGridlinesVisible(true);
            plot.setDomainGridlinesVisible(false);

            plot.setDomainPannable(true);
            plot.setRangePannable(true);

            // Crosshairs hinzufügen
            float[] dash = {15, 4};
            plot.setDomainCrosshairVisible(true);
            plot.setDomainCrosshairStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
            plot.setDomainCrosshairLockedOnData(true);
            plot.setRangeCrosshairVisible(true);
            plot.setRangeCrosshairStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
        } catch (Exception e) {
            Messages.showMessage("Fehler bei Datenbankabfrage: " + e.getMessage() + "\n", true);
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    public XYPlot getPlot() {
        return plot;
    }
}
