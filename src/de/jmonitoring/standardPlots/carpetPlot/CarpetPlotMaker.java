/*
 * CarpetPlotMaker.java
 *
 * Created on 19. August 2007, 21:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.carpetPlot;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.List;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.Range;
import org.jfree.data.time.Day;
import org.jfree.ui.RectangleAnchor;

import com.lowagie.text.Font;

import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.CustomPaintScale;
import de.jmonitoring.utils.DateBandAxis;
import de.jmonitoring.utils.UnitLabelBuilder;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author Thomas
 */
public class CarpetPlotMaker {

    private XYPlot plot;
    private CustomPaintScale paintScale;
    private double rangeMin, rangeMax;
    private NumberAxis scaleAxis = new NumberAxis();
    private CarpetSeriesLooks series;

    /**
     * Creates a new instance of CarpetPlotMaker
     */
    public CarpetPlotMaker(CarpetChartDescriber describer, DatasetWorkerFactory workerFactory) {
        List<CarpetSeriesLooks> seriesCollection = describer.getchartCollection();
        DateInterval dateInterval = describer.getDateInterval();
        series = seriesCollection.get(0); // Zur Zeit nur ein Sensor verwendet
        GenerateCarpetDataset sSet = new GenerateCarpetDataset(series, dateInterval, workerFactory.createFor(series.getSensorID()));
        String displayUnit = "";
        ValueAxis yAxis = new NumberAxis("Uhrzeit");
        yAxis.setRange(new Range(0., 24.), true, true); // Skala 0-24h
        yAxis.setFixedAutoRange(24f); // limitieren auf 24
        DateAxis xAxis = new DateAxis("Datum");
        String title = "";

        // Zeitachse mit verschiedenen Bändern hinzufügen
        ValueAxis domainAxis = new DateBandAxis(dateInterval, Day.class, false, false, true, true, 9);

        // Skala der x-Achse auf setzen
        xAxis.setRange(dateInterval.getStartDate(), dateInterval.getEndDate());

        XYBlockRenderer renderer = new XYBlockRenderer();
        // Je nachdem ob Grenzwerte manuell vorgegeben werden (Werte in series belegt) diese oder die aus der SQL-Abfrage ermittelten nehmen
        // Minimum
        if (series.getScaleMin() != -MoniSoftConstants.RANGE_BOUNDARY) {
            rangeMin = series.getScaleMin();
        } else {
            rangeMin = (double) sSet.getMinValue();
            System.out.println("min : " + rangeMin);
            series.setScaleMin(rangeMin);
        }
        // Maximum
        if (series.getScaleMax() != MoniSoftConstants.RANGE_BOUNDARY) {
            rangeMax = series.getScaleMax();
        } else {
            rangeMax = (double) sSet.getMaxValue();
            System.out.println("max : " + rangeMax);
            series.setScaleMax(rangeMax);
        }

        paintScale = new CustomPaintScale(rangeMin, rangeMax, false);
        renderer.setPaintScale(paintScale.getScale());
        renderer.setBlockWidth(86400000); // ein tag
        renderer.setBlockHeight(1); // eine Stunde
//        renderer.setBaseToolTipGenerator(new CustomXYToolTipGenerator());
        renderer.setBlockAnchor(RectangleAnchor.BOTTOM_LEFT);
        renderer.setSeriesPaint(0, Color.black);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());


        // wenn Achse im describer diese nehmen ansonsten die oben erstellte
        if (describer.getLeftRangeAxis() != null) {
            yAxis = describer.getLeftRangeAxis();
        }
        plot = new XYPlot(sSet.getDataSet(), domainAxis, yAxis, renderer);

        // Domänenachse setzen wenn der desriber einen enthält, ansonst die oben definierte PeriodAxis
        if (describer.getDomainAxis() != null) {
            domainAxis = adjustDomainAxis(describer.getDomainAxis(), domainAxis);
        }
        plot.setDomainAxis(domainAxis);

        // Bei Carpetplots gibt es keine Gitterlinien
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);

        plot.setRangePannable(true);
        plot.setDomainPannable(true);

        // Referenzlabels wenn nötig
        String referencePart = (series.getReference() != null ? UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(series.getReference().getName())) + series.getReference().getName() : "");
        String timeReferencePart = series.getTimeReferenceUnit().isEmpty() ? "" : series.getTimeReferenceUnit(); // Einheit des Zeitbezugs;

        // Skala formatieren
        UnitLabelBuilder ulb = new UnitLabelBuilder();
        if (series.getPowerWanted()) {
//            displayUnit = getPowerUnitFromConsumption(series.getUnitFromName());
            displayUnit = ulb.buildLabel(displayUnit, new UnitTransfer().getPowerUnitFromConsumption(series.getUnit()));
        } else {
//            displayUnit = series.getUnitFromName();
            displayUnit = ulb.buildLabel(displayUnit, series.getUnit());
        }
        displayUnit = ulb.addReference(displayUnit, referencePart, timeReferencePart);

        scaleAxis.setTickLabelFont(new java.awt.Font("Dialog", Font.NORMAL, 9));
        scaleAxis.setLabelFont(new java.awt.Font("Dialog", Font.NORMAL, 9));
        scaleAxis.setLabel("Farbschlüssel " + displayUnit);
        scaleAxis.setRange(getMin(), getMax());

        // Titel setzen wenn nicht schon im describer
        if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
            describer.setPlotTitle(SensorInformation.getDisplayName(series.getSensorID()));
        }

        // Subtitel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
//        if (describer.getPlotSubtitle() == null || describer.getPlotSubtitle().isEmpty() || describer.isUseCustomDateInterval()) {
        describer.setPlotSubtitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + describer.getDateInterval().getStartDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + describer.getDateInterval().getEndDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)));
//        }

    }




    /**
     * Adjust the shown interval on the recycled describers axis according to
     * the new timespan of the data (getting it from the automatically generated
     * desriber).<br> All other axis properties remain untouched)
     *
     * @param describerAxis
     * @param generatedAxis
     * @return
     */
    private ValueAxis adjustDomainAxis(ValueAxis describerAxis, ValueAxis generatedAxis) {
        if (describerAxis.getLowerBound() != generatedAxis.getLowerBound()) {
            describerAxis.setLowerBound(generatedAxis.getLowerBound());
        }

        if (describerAxis.getUpperBound() != generatedAxis.getUpperBound()) {
            describerAxis.setUpperBound(generatedAxis.getUpperBound());
        }

        return describerAxis;
    }

    public NumberAxis getScaleAxis() {
        return scaleAxis;
    }

    public XYPlot getPlotInfo() {
        return plot;
    }

    public LookupPaintScale getPaintScale() {
        return paintScale.getScale();
    }

    public double getMin() {
        return rangeMin;
    }

    public double getMax() {
        return rangeMax;
    }
}
