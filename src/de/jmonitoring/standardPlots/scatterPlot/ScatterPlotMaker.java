/*
 * ScatterPlotMaker.java
 *
 * Created on 16. Juli 2007, 17:02
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.scatterPlot;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.ui.Layer;

import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.AnnotationEditor.AnnotationContainer;
import de.jmonitoring.utils.AnnotationEditor.AnnotationElement;
import de.jmonitoring.utils.AnnotationEditor.AnnotationHandler;
import de.jmonitoring.utils.DataPointObject;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.UnitLabelBuilder;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class ScatterPlotMaker {

    static final long serialVersionUID = 2460191724788190799L;
    private final XYPlot plot = new XYPlot();
    private List looks;
    private DateInterval dateInterval;
    private SensorProperties domainSensorProps;
    private String remark = "";
    private GenerateScatterDataSet sSet;
    private final ScatterChartDescriber describer;

    public ScatterPlotMaker(ScatterChartDescriber desc, DatasetWorkerFactory workerFactory) {
        describer = desc;
        String xAxisLabel;
        String yAxisLabel = "";
        String powerLabel = "";
        String titleSep = "";
        String domainLabel;
        domainSensorProps = describer.getDomainSensorProps();
        looks = describer.getchartCollection();
        dateInterval = describer.getDateInterval();
        List seriesCollection = describer.getchartCollection();


        sSet = new GenerateScatterDataSet(describer, workerFactory);
        remark = sSet.getRemark();
        StoppableThread thisThread = (StoppableThread) Thread.currentThread();
        if (!thisThread.running) {
            return;
        }

        int index = 0;
        String title = "";
        int plot_id = 0;
        for (Iterator it = seriesCollection.iterator(); it.hasNext();) {
            ScatterSeriesLooks currentLooks = (ScatterSeriesLooks) it.next();

            if (currentLooks != null) {
                title += titleSep + SensorInformation.getDisplayName(currentLooks.getSensorID());
                titleSep = " ,";
                UnitLabelBuilder ulb = new UnitLabelBuilder();
                if (currentLooks.getPowerWanted()) {
                    powerLabel = new UnitTransfer().getPowerUnitFromConsumption(currentLooks.getUnit());
                    yAxisLabel = ulb.buildLabel(yAxisLabel, powerLabel);
                } else {
                    yAxisLabel = ulb.buildLabel(yAxisLabel, currentLooks.getUnit());
                }
                plot.setDataset(plot_id, sSet.getDataSet()[index]);
                XYLineAndShapeRenderer renderer = getDataSetRenderer(currentLooks);

                CustomXYToolTipGenerator toolTipGenerator = new CustomXYToolTipGenerator();
                toolTipGenerator.setLabelMap(sSet.getLabelMap());
                // Tooltippgenerator
                renderer.setBaseToolTipGenerator(toolTipGenerator);

                plot.setRenderer(plot_id, renderer);

                plot_id++;
                index++;
            }
        }

        // y-Achse
        // Linke Achse bauen oder aus describer holen
        if (describer.getLeftRangeAxis() != null) {
            plot.setRangeAxis(describer.getLeftRangeAxis());
        } else {
            NumberAxis y1Axis = new NumberAxis(yAxisLabel);
            y1Axis.setRange(sSet.getValueRange()[0], sSet.getValueRange()[1]);
            plot.setRangeAxis(y1Axis);
        }

        // x-Achse (Domain)
        if (describer.getUsePower()) {
            domainLabel = new UnitTransfer().getPowerUnitFromConsumption(domainSensorProps.getSensorUnit().getUnit());
        } else {
            domainLabel = domainSensorProps.getSensorUnit().getUnit();
        }
        // Domänenachse setzen wenn der desriber einen enthält, ansonst die oben definierte PeriodAxis
        if (describer.getDomainAxis() != null) {
            plot.setDomainAxis(describer.getDomainAxis());
        } else {
            xAxisLabel = "[ " + domainLabel + " ]";
            NumberAxis domainAxis = new NumberAxis(xAxisLabel);
            plot.setDomainAxis(domainAxis);
        }

        plot.setRangePannable(true);
        plot.setDomainPannable(true);

        // System.out.println( "describer.getPlotTitle(): " + describer.getPlotTitle() );
        // System.out.println( "recycled describer: " + this.describer.get recyleDescriber  );
        // Wenn recycled subscriber, dann lies die ausgewählten Werte aus
        
        // Titel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
        if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
            title += " " + java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/scatterPlot/Bundle").getString("AGAINST") + " " + SensorInformation.getDisplayName(domainSensorProps.getSensorID());
            describer.setPlotTitle(title);
        }


        // TODO temporäre Lösung, dass der subtitle IMMER überschrieben wird.
        // Subtitel setzen, noch keinen enthält oder ein anderer als der ursprüngliche Zeitraum gewählt wurde
//        if (describer.getPlotSubtitle() == null || describer.getPlotSubtitle().isEmpty() || describer.isUseCustomDateInterval()) {
        describer.setPlotSubtitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + dateInterval.getStartDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + dateInterval.getEndDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + buildAggregationSubtitle(describer));
//        }

        // eventuell Bereiche mit AreaAnnotations Markieren
        XYItemRenderer renderer = plot.getRenderer();

        AnnotationContainer container = AnnotationHandler.readAnnotation(describer.getAreaMarker());
        if (container != null) {
            for (AnnotationElement element : container.getAnnotationElements()) {
                List<XYAnnotation> elementParts = AnnotationHandler.getAnnotationForElement(element);
                for (XYAnnotation annotation : elementParts) {
                    renderer.addAnnotation(annotation, Layer.BACKGROUND);
                }
            }
        }
    }

    /**
     * Erzeugt den Renderer entsprechend dem übergebenen Look
     *
     * @param seriesLooks
     * @return renderer
     */
    private XYLineAndShapeRenderer getDataSetRenderer(ScatterSeriesLooks seriesLooks) {
        XYLineAndShapeRenderer xy_renderer = new XYLineAndShapeRenderer(false, true);
        Color c;
        c = new Color(seriesLooks.getPointsColor().getRed(), seriesLooks.getPointsColor().getGreen(), seriesLooks.getPointsColor().getBlue(), 255);
        xy_renderer.setSeriesPaint(0, c);
        xy_renderer.setSeriesShape(0, getShape(seriesLooks.getPointType(), seriesLooks.getPointSize()));
        return xy_renderer;
    }

    private Shape getShape(int type, int size) {
        float s;
        switch (type) {
            case 0:
                s = (size + 1f) / 2;
                return new Ellipse2D.Float(-s, -s, 2 * s, 2 * s);
            case 1:
                s = (size + 1f) / 2;
                return new Rectangle2D.Float(-s, -s, 2 * s, 2 * s);
            case 2:
                s = (size + 1f);
                double element = s * Math.sin(Math.PI / 4.);
                GeneralPath p = new GeneralPath();
                p.moveTo(0, -s);
                p.lineTo(element, element);
                p.lineTo(-element, element);
                p.lineTo(0, -s);
                return p;
        }
        return null;
    }

    /**
     * Gibt den Subtitel des Charts zurück
     *
     * @return Subtitel des Plots
     */
    public String getSubTitle() {
        return buildAggregationSubtitle(describer);
    }

    /**
     * Gibt die SensorProperties des Domainsensors zurück
     *
     * @return
     */
    public SensorProperties getDomainSensorProps() {
        return domainSensorProps;
    }

    /**
     * Gibt eine ArrayList mit den Serien-Eigenschaften zurück
     *
     * @return ArrayList der SerienLooks
     */
    public ArrayList getSeriesLooks() {
        return new ArrayList(looks);
    }

    /**
     * Liefert ein {@link DateInterval} mit Start- und Enddatum zurück
     *
     * @return dateInterval
     */
    public DateInterval getTimeInterval() {
        return this.dateInterval;
    }

    /**
     * Gibt den erzeugten Plot der Serien zurück
     *
     * @return plot
     */
    public XYPlot getPlotInfo() {
        return plot;
    }

    public String getRemark() {
        return remark;
    }

    public HashMap<DataPointObject, String> getLabelmap() {
        return sSet.getLabelMap();
    }

    private String buildAggregationSubtitle(ScatterChartDescriber describer) {
        switch ((int) describer.getAggregation()) {
            case (int) MoniSoftConstants.RAW_INTERVAL:
                return "(Rohwerte)";
            case (int) MoniSoftConstants.HOUR_INTERVAL:
                return "(Stundenwerte)";
            case (int) MoniSoftConstants.DAY_INTERVAL:
                return "(Tageswerte)";
            case (int) MoniSoftConstants.WEEK_INTERVAL:
                return "(Wochenwerte)";
            case (int) MoniSoftConstants.MONTH_INTERVAL:
                return "(Monatswerte)";
            case (int) MoniSoftConstants.YEAR_INTERVAL:
                return "(Jahreswerte)";
            default:
                return "(" + describer.getAggregation() + "-Minuten-Werte)";
        }
    }
}
