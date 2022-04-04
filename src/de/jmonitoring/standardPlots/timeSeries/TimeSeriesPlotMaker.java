/*
 * TimeSeriesPlotMaker.java
 *
 * Created on 12. Juni 2007, 14:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.JProgressBar;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Day;

import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import de.jmonitoring.standardPlots.timeSeries.GenerateTimeSeriesDataSet;
import de.jmonitoring.standardPlots.timeSeries.GenerateTimeTableXYDataset;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.DateBandAxis;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.UnitLabelBuilder;
import de.jmonitoring.utils.JFreeChartPatches.SamplingXYLineAndShapeRenderer;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;
import org.jfree.ui.Layer;

/**
 *
 * @author togro
 */
public class TimeSeriesPlotMaker {

    static final int LINE_PLOT = 0;
    static final int BAR_PLOT = 1;
    static final int DOT_PLOT = 2;
    static final int AREA_PLOT = 3;
    static final int STEP_PLOT = 4;
    static final int DIFF_PLOT = 5;
    static final int CARPET_PLOT = 6;
    private XYPlot mainPlot = new XYPlot(); // Hauptplot
    private XYPlot eventPlot = new XYPlot(); // Plot der Events
    private String title = "";
    private final DateInterval dateInterval;
    static JProgressBar progressBar;
    public boolean hasLeftYAxis = false;
    public boolean hasRightYAxis = false;
    private ValueAxis domainAxis;
    private StoppableThread thisThread = (StoppableThread) Thread.currentThread();
    private Stack colorStack = new Stack();
    private String remark = "";
    private TimeSeriesChartDescriber describer;
    private ArrayList markerList = null;
    private IntervalMarker marker = null;
    private final MainApplication gui;

    public TimeSeriesPlotMaker(TimeSeriesChartDescriber desc, JProgressBar progress, MainApplication gui) {
        super();
        describer = desc;
        this.gui = gui;
        List seriesCollection = describer.getchartCollection();
        DateInterval interval = describer.getDateInterval();
        progressBar = progress;
        String y1AxisLabel = "";
        String y2AxisLabel = "";
        String yAxisLabel = ""; // bei gestapelten Diagrammen
        String powerLabel = "";
        int index = 0;
        XYItemRenderer renderer;
        String titleSep = "";
        String referencePart = "";
        String timeReferencePart = ""; // Einheit des Jahresbezugs;
        dateInterval = interval.clone();

        progressBar.setMinimum(0);
        progressBar.setMaximum(seriesCollection.size());
        ArrayList<String> labels = new ArrayList<String>();

        // Zeitachse mit verschiedenen Bändern erzeugen
//        System.out.println("von " + dateInterval.getStartDate() + " - " + dateInterval.getEndDate());
        domainAxis = new DateBandAxis(dateInterval, Day.class, false, true, true, true, 9);
        TimeSeriesLooks series;

        // Leere Einträge und Events aus der Collection filtern und letztere in eine eigene Collection legen
        ArrayList<TimeSeriesLooks> normalTimeSeriesCollection = new ArrayList<TimeSeriesLooks>();
        ArrayList<TimeSeriesLooks> eventSeriesCollection = new ArrayList<TimeSeriesLooks>();
        for (Iterator<TimeSeriesLooks> it = seriesCollection.iterator(); it.hasNext();) {
            series = it.next();
            if (series != null && SensorInformation.getSensorProperties(series.getSensorID()).isEvent()) {
                labels.add(series.getSensor());
                eventSeriesCollection.add(series);
            } else if (series != null) {
                normalTimeSeriesCollection.add(series);
            }
        }


        // Normale Messpunkte bearbeiten, falls welche gewählt sind
        if (!normalTimeSeriesCollection.isEmpty()) {
            // je nachdem ob es eine Stacked Bar/Area - Darstellung sein soll den entprechenden Datesatz generieren
            //
            // STACKED
            //
            if (isStackedSeries(normalTimeSeriesCollection)) {
                GenerateTimeTableXYDataset sSet = new GenerateTimeTableXYDataset(normalTimeSeriesCollection, dateInterval, progressBar, describer, new DatasetWorkerFactory(this.gui));
                if (!thisThread.running || sSet == null) {
                    mainPlot = null;
                    eventPlot = null;
                    return;
                }
                remark = sSet.getRemark();

                renderer = getdatasetRenderer((TimeSeriesLooks) normalTimeSeriesCollection.get(0));

//                Messages.showMessage("Generieren der Renderer...\n",true);
                // Farben setzen
                for (Iterator it = normalTimeSeriesCollection.iterator(); it.hasNext();) {
                    series = (TimeSeriesLooks) it.next();
                    if (series != null) {
                        // Wenn die Einheit der y-Achse noch leer ist diese belegen (bekommt nur den ersten Wert des Stapels zugewiesen)
                        if (yAxisLabel.isEmpty()) {
                            referencePart = (series.getReference() != null ? "/" + UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(series.getReference().getName())) : "");
                            timeReferencePart = series.getTimeReferenceUnit().isEmpty() ? "" : " " + series.getTimeReferenceUnit(); // Einheit des Jahresbezugs;
                            UnitLabelBuilder ulb = new UnitLabelBuilder();
                            if (series.getPowerWanted()) {
                                powerLabel = new UnitTransfer().getPowerUnitFromConsumption(series.getUnit()); // Achsenbeschriftung je nach gewünschter Darstellungsform bauen
                                yAxisLabel = ulb.buildLabel(yAxisLabel, powerLabel); //powerLabel;
                            } else {
                                yAxisLabel = ulb.buildLabel(yAxisLabel, series.getUnit()); //series.getUnitFromName();    // Einheiten der Messwerte zusammenfügen
                            }
                            yAxisLabel = ulb.addReference(yAxisLabel, referencePart, timeReferencePart);// "[" + yAxisLabel + "]";
                        }
                        title += titleSep + SensorInformation.getDisplayName(series.getSensorID());
                        titleSep = ", ";
                        if (series.getDrawType() == BAR_PLOT) {
                            renderer.setSeriesPaint(series.getSeriesID(), new Color(series.getBars_fillColor().getRed(), series.getBars_fillColor().getGreen(), series.getBars_fillColor().getBlue(), series.getBars_alpha()));
                            Color c = new Color(series.getBars_borderColor().getRed(), series.getBars_borderColor().getGreen(), series.getBars_borderColor().getBlue());
                            renderer.setSeriesOutlinePaint(series.getSeriesID(), c);  // Rahmen setzen
                        }
                    }
                }
                // Die Errormarker holen und setzten
                markerList = sSet.getMissingmarker();

                for (int i = 0; i < markerList.size(); i++) {
                    marker = (IntervalMarker) markerList.get(i);
                    marker.setPaint(Color.YELLOW);
                    marker.setAlpha(0.2f);
                    marker.setOutlineStroke(null);
                    mainPlot.addDomainMarker(marker);
                }

                // Linke Achse bauen oder aus describer holen
                if (describer.getLeftRangeAxis() != null) {
                    mainPlot.setRangeAxis(0, describer.getLeftRangeAxis());
                } else {
                    mainPlot.setRangeAxis(0, new NumberAxis(yAxisLabel));
                }
                // Rechte Achse bauen oder aus describer holen
                if (hasRightYAxis) {
                    if (describer.getRightRangeAxis() != null) {
                        mainPlot.setRangeAxis(1, describer.getRightRangeAxis());
                    } else {
                        mainPlot.setRangeAxis(1, new NumberAxis(y2AxisLabel));
                    }
                }
                // Domänenachse setzen wenn der describer einen enthält, ansonst die oben definierte PeriodAxis
                if (describer.getDomainAxis() != null) {
                    domainAxis = adjustDomainAxis(describer.getDomainAxis(), domainAxis);
                }
                mainPlot.setDomainAxis(domainAxis);

                // Titel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
                if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
                    describer.setPlotTitle(title);
                }
                // Subtitel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
//                if (describer.getPlotSubtitle() == null || describer.getPlotSubtitle().isEmpty() || describer.isUseCustomDateInterval()) {
                describer.setPlotSubtitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + describer.getDateInterval().getStartDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + describer.getDateInterval().getEndDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)));
//                }

                mainPlot.setDataset(0, sSet.getDataSet()[0]);
                mainPlot.setRenderer(renderer);
                mainPlot.setDomainGridlinesVisible(false);
                mainPlot.setDomainPannable(true);
                mainPlot.setRangePannable(true);


                // Die Errormarker holen und setzten
                markerList = sSet.getMissingmarker();
                for (int i = 0; i < markerList.size(); i++) {
                    marker = (IntervalMarker) markerList.get(i);
                    marker.setPaint(Color.YELLOW);
                    marker.setAlpha(0.2f);
                    marker.setOutlineStroke(null);
                    mainPlot.addDomainMarker(marker, Layer.FOREGROUND);
                }

                // Filtermarkerholen und setzen
                ArrayList filterMarkerList = sSet.getFilterMarker();
                if (filterMarkerList != null) {
                    IntervalMarker filterMarker;
                    for (int i = 0; i < filterMarkerList.size(); i++) {
                        filterMarker = (IntervalMarker) filterMarkerList.get(i);
                        filterMarker.setPaint(Color.RED);
                        filterMarker.setAlpha(0.2f);
                        filterMarker.setOutlineStroke(null);
                        mainPlot.addDomainMarker(filterMarker, Layer.BACKGROUND);
                    }
                }
                hasLeftYAxis = true;
                hasRightYAxis = false;
            } else {
                //
                //
                // bei "normaler" Darstellung
                //
                //
//                Messages.showMessage("C: " + dateInterval.getStartDate().toString() + "\n");
                GenerateTimeSeriesDataSet sSet = new GenerateTimeSeriesDataSet(normalTimeSeriesCollection, dateInterval, progressBar, describer, new DatasetWorkerFactory(this.gui));
                if (!thisThread.running || sSet == null) {
                    mainPlot = null;
                    eventPlot = null;
                    return;
                }
                remark = sSet.getRemark();
//                Messages.showMessage("Generieren der Renderer...\n",true);
                // Achsenabhängige Teile der normalSeriesCollection aufdröseln und entsprechende Parameter setzen
                for (Iterator it = normalTimeSeriesCollection.iterator(); it.hasNext();) {   // TODO Hier besser: Datensätze durchgehen und nicht seriesCollections (tut dann auch für stacked!)
                    series = (TimeSeriesLooks) it.next();
                    if (series != null) {                     // Nicht belegte Datensätze abweisen
                        title += titleSep + SensorInformation.getDisplayName(series.getSensorID());
                        titleSep = ", ";
                        referencePart = (series.getReference() != null ? UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(series.getReference().getName())) + series.getReference().getName() : "");
                        timeReferencePart = series.getTimeReferenceUnit().isEmpty() ? "" : series.getTimeReferenceUnit(); // Einheit des Jahresbezugs;

                        UnitLabelBuilder ulb = new UnitLabelBuilder();
                        if (series.getyAxis() == 0) {         // y1-Achse
                            // Label der Achse (Einheitenberschriftung) zusammenbauen
                            hasLeftYAxis = true;
                            if (series.getPowerWanted()) {
                                powerLabel = new UnitTransfer().getPowerUnitFromConsumption(series.getUnit()); // Achsenbeschriftung je nach gewünschter Darstellungsform bauen
                                y1AxisLabel = ulb.buildLabel(y1AxisLabel, powerLabel);
                            } else {
                                y1AxisLabel = ulb.buildLabel(y1AxisLabel, series.getUnit());
                            }

                            y1AxisLabel = ulb.addReference(y1AxisLabel, referencePart, timeReferencePart);

                            mainPlot.setDataset(index, sSet.getDataSet()[index]);            // Dataset auf die y1-Achse legen
                            renderer = getdatasetRenderer(series);                              // renderer für diese Messspunkt holen
                            mainPlot.setRenderer(index, renderer);                           // renderer für entspr. Datensatz setzen
                            mainPlot.mapDatasetToRangeAxis(index, 0);                        // Datensatz auf y1-Achse legen
                        } else if (series.getyAxis() == 1) {  //y2-Achse
                            hasRightYAxis = true;
                            if (series.getPowerWanted()) {
                                powerLabel = new UnitTransfer().getPowerUnitFromConsumption(series.getUnit()); // Achsenbeschriftung je nach gewünschter Darstellungsform bauen
                                y2AxisLabel = ulb.buildLabel(y2AxisLabel, powerLabel);
                            } else {
                                y2AxisLabel = ulb.buildLabel(y2AxisLabel, series.getUnit());
                            }
                            y2AxisLabel = ulb.addReference(y2AxisLabel, referencePart, timeReferencePart);

                            mainPlot.setDataset(index, sSet.getDataSet()[index]);            // Dataset auf die y2-Achse legen
                            renderer = getdatasetRenderer(series);                              // renderer für diese Messspunkt holen
                            mainPlot.setRenderer(index, renderer);                           // renderer für entspr. Datensatz setzen
                            mainPlot.mapDatasetToRangeAxis(index, 1);                        // Datensatz auf y2-Achse legen
                        }
                        index++;
                    }
                }

                // Linke Achse bauen oder aus describer holen
                if (describer.getLeftRangeAxis() != null) {
                    mainPlot.setRangeAxis(0, describer.getLeftRangeAxis());
                } else {
                    mainPlot.setRangeAxis(0, new NumberAxis(y1AxisLabel));
                }
                // Rechte Achse bauen oder aus describer holen
                if (hasRightYAxis) {
                    if (describer.getRightRangeAxis() != null) {
                        mainPlot.setRangeAxis(1, describer.getRightRangeAxis());
                    } else {
                        mainPlot.setRangeAxis(1, new NumberAxis(y2AxisLabel));
                    }
                }


                // Domänenachse setzen wenn der desriber einen enthält, ansonst die oben definierte PeriodAxis
                if (describer.getDomainAxis() != null) {
                    domainAxis = adjustDomainAxis(describer.getDomainAxis(), domainAxis);
                }
                mainPlot.setDomainAxis(domainAxis);

                // Titel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel (auch wenn ein custom intervall gewählt wurde)
                if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
                    describer.setPlotTitle(title);
                }
                // Subtitel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
//                if (describer.getPlotSubtitle() == null || describer.getPlotSubtitle().isEmpty() || describer.isUseCustomDateInterval()) {
                describer.setPlotSubtitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + describer.getDateInterval().getStartDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + describer.getDateInterval().getEndDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)));
//                }

                mainPlot.setRangeZeroBaselineVisible(true);
                mainPlot.setDomainPannable(true);
                mainPlot.setRangePannable(true);
                // Die Errormarker holen und setzten
                markerList = sSet.getMissingmarker();
                for (int i = 0; i < markerList.size(); i++) {
                    marker = (IntervalMarker) markerList.get(i);
                    marker.setPaint(Color.YELLOW);
                    marker.setAlpha(0.2f);
                    marker.setOutlineStroke(null);
                    mainPlot.addDomainMarker(marker, Layer.FOREGROUND);
                }

                // Filtermarkerholen und setzen
                ArrayList filterMarkerList = sSet.getFilterMarker();
                if (filterMarkerList != null) {
                    IntervalMarker filterMarker;
                    for (int i = 0; i < filterMarkerList.size(); i++) {
                        filterMarker = (IntervalMarker) filterMarkerList.get(i);
                        filterMarker.setPaint(Color.RED);
                        filterMarker.setAlpha(0.2f);
                        filterMarker.setOutlineStroke(null);
                        mainPlot.addDomainMarker(filterMarker, Layer.BACKGROUND);
                    }
                }
            }
        } else {
            normalTimeSeriesCollection = null;
            mainPlot = null;
        }

        // Plot der Events bauen wenn welche gewählt wurden
        if (!eventSeriesCollection.isEmpty()) {
            EventTimeSeries eventSeries = new EventTimeSeries(eventSeriesCollection, dateInterval);
            eventPlot.setDataset(eventSeries.getDataset());
            SymbolAxis yAxis = new SymbolAxis(null, labels.toArray(new String[labels.size()]));
            yAxis.setGridBandsVisible(false);
            eventPlot.setRangeAxis(yAxis);

            XYBarRenderer eventRenderer = new XYBarRenderer();
            eventRenderer.setBarPainter(new StandardXYBarPainter());
            eventRenderer.setShadowVisible(false);
            resetColors();


            for (TimeSeriesLooks looks : eventSeriesCollection) {
                title += titleSep + looks.getSensor();
                titleSep = ", ";
            }

            for (int i = 0; i < eventSeries.getDataset().getSeriesCount(); i++) {
                eventRenderer.setSeriesPaint(i, (Color) colorStack.peek());
                eventRenderer.setSeriesOutlinePaint(i, (Color) colorStack.pop());
                if (colorStack.empty()) {
                    resetColors();
                }
            }

            // wenn es keinen Hauptplot gibt parameter aus describer setzen
            if (normalTimeSeriesCollection == null) {
                // Titel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
                if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
                    describer.setPlotTitle(title);
                }
                // Subtitel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
                if (describer.getPlotSubtitle() == null || describer.getPlotSubtitle().isEmpty() || describer.isUseCustomDateInterval()) {
                    describer.setPlotSubtitle(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("VON") + " " + describer.getDateInterval().getStartDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("BIS") + " " + describer.getDateInterval().getEndDateString(new SimpleDateFormat(MoniSoftConstants.HumanDateFormat)));
                }

                // Domänenachse setzen wenn der desriber einen enthält, ansonst die oben definierte PeriodAxis
                if (describer.getDomainAxis() != null) {
                    domainAxis = describer.getDomainAxis();
                }
            }

            eventPlot.setDomainAxis(domainAxis);

            eventRenderer.setUseYInterval(true);
            eventPlot.setRenderer(eventRenderer);
            eventPlot.setBackgroundPaint(Color.WHITE);
            eventPlot.setDomainGridlinesVisible(false);
            eventPlot.setRangeGridlinesVisible(false);
            eventPlot.setDomainPannable(true);
            eventPlot.setRangePannable(true);

            hasLeftYAxis = true;
        } else {
            eventSeriesCollection = null;
            eventPlot = null;
        }
        
        // AZ: nach dem Durchlauf CounterChangeErrorDialog auf false setzen - MONISOFT-8
        Interpolator.saveCancelDecision = false;
        Interpolator.chartType = Interpolator.TIMESERIES_PLOT_TAB;
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

    private void resetColors() {
        colorStack.push(Color.MAGENTA);
        colorStack.push(Color.GREEN);
        colorStack.push(Color.RED);
    }

    // für eine Series den entsprechenden Renderer setzten - je nach Darstellungsart
    private AbstractXYItemRenderer getdatasetRenderer(TimeSeriesLooks series) {
        AbstractXYItemRenderer renderer = null;
//        StandardXYToolTipGenerator generator;
        Color c;
        switch (series.getDrawType()) {
            case LINE_PLOT:
                SamplingXYLineAndShapeRenderer LaS_renderer = new SamplingXYLineAndShapeRenderer(series.getLines_drawLine(), series.getLines_drawSymbols());
                LaS_renderer.setSeriesShape(0, getShape(series.getLines_symbolType(), series.getLines_symbolSize()));
                LaS_renderer.setSeriesStroke(0, getStroke(series.getLines_lineType(), series.getLines_lineSize()));
                LaS_renderer.setDrawSeriesLineAsPath(true);  // erhöht Performance aber serie verschwindet wenn Endpunkt aus dem Bild rutscht
                renderer = LaS_renderer;
                c = new Color(series.getLines_lineColor().getRed(), series.getLines_lineColor().getGreen(), series.getLines_lineColor().getBlue(), 255);
                renderer.setSeriesPaint(0, c);  // Zeichenfarbe setzen
//                generator = new StandardXYToolTipGenerator("{0}:  {1}={2}", new SimpleDateFormat("dd.MM.yyyy"), new DecimalFormat("0.0"));
//                renderer.setBaseToolTipGenerator(generator);
////                renderer.setDefaultEntityRadius(10);
                break;

            case BAR_PLOT: // Bei Balkengrafik unterscheiden ob stacked oder normal
                if (series.getStacked()) {
                    StackedXYBarRenderer stackedXYBarRenderer = new StackedXYBarRenderer(0.2);
                    stackedXYBarRenderer.setBarPainter(new StandardXYBarPainter());
                    stackedXYBarRenderer.setShadowVisible(false);
                    stackedXYBarRenderer.setDrawBarOutline(series.getBars_DrawBorder());                    
                    renderer = stackedXYBarRenderer;
                    // Farben für Serien werden oben gestetzt
                } else {
                    int alpha;
                    XYBarRenderer Bar_renderer = new XYBarRenderer();
                    Bar_renderer.setBarPainter(new StandardXYBarPainter());
                    Bar_renderer.setShadowVisible(false);
                    Bar_renderer.setDrawBarOutline(series.getBars_DrawBorder());
                    Bar_renderer.setSeriesOutlineStroke(0, getStroke(series.getBars_borderType(), series.getBars_borderSize()));
//                    Bar_renderer.setSeriesOutlinePaint(0, series.getBars_borderColor());
                    Bar_renderer.setMargin(0.2);
                    renderer = Bar_renderer;
                    if (series.getBars_DrawFilling()) {
                        alpha = series.getBars_alpha();
                    } else {
                        alpha = 0;
                    }
                    c = new Color(series.getBars_fillColor().getRed(), series.getBars_fillColor().getGreen(), series.getBars_fillColor().getBlue(), alpha);
                    renderer.setSeriesPaint(0, c);  // Füllfarbe setzen
                    c = new Color(series.getBars_borderColor().getRed(), series.getBars_borderColor().getGreen(), series.getBars_borderColor().getBlue());
                    renderer.setSeriesOutlinePaint(0, c);  // Rahmen setzen
                }
                break;

            case AREA_PLOT:
                if (series.getStacked()) {
                    StackedXYAreaRenderer2 stackedXYAreaRenderer = new StackedXYAreaRenderer2();
                    renderer = stackedXYAreaRenderer;
                } else {
                    int alpha;
                    XYAreaRenderer Area_renderer = new XYAreaRenderer();
                    Area_renderer.setOutline(series.getArea_DrawBorder());
                    Area_renderer.setSeriesOutlineStroke(0, getStroke(series.getArea_borderType(), series.getArea_borderSize()));
                    renderer = Area_renderer;
                    if (series.getArea_DrawFilling()) {
                        alpha = series.getArea_alpha();
                    } else {
                        alpha = 0;
                    }
                    c = new Color(series.getArea_fillColor().getRed(), series.getArea_fillColor().getGreen(), series.getArea_fillColor().getBlue(), alpha);
                    renderer.setSeriesPaint(0, c);      // Füllfarbe setzen
                    c = new Color(series.getArea_borderColor().getRed(), series.getArea_borderColor().getGreen(), series.getArea_borderColor().getBlue());
                    renderer.setSeriesOutlinePaint(0, c);  // Rahmen setzen
                }
                break;

            case STEP_PLOT:
                XYStepRenderer Step_renderer = new XYStepRenderer();
                renderer = Step_renderer;
                break;

            case DIFF_PLOT:
                XYDifferenceRenderer Diff_renderer = new XYDifferenceRenderer();
                renderer = Diff_renderer;
                break;
        }

        return renderer;
    }

    private Shape getShape(int type, int index) {
        int size = index + 1; //
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

    private Stroke getStroke(int type, int index) {
        int size = index + 1;
        switch (type) {
            case 0:
                return new BasicStroke(size);
            case 1:
                return new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{4f, 4f}, 0f);
            case 2:
                return new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 8f}, 0f);
        }
        return null;
    }

    /**
     * Ermitteln ob es sich bei der übergebenen SeriesCollection um eine
     * gestapelte handelt
     *
     * @param seriesCollection
     * @return
     */
    private boolean isStackedSeries(ArrayList seriesCollection) {
        TimeSeriesLooks series = (TimeSeriesLooks) seriesCollection.get(0); // Das erste Element der series holen
        return series.getStacked();
    }

    public XYPlot getMainPlot() {
        return mainPlot;
    }

    public XYPlot getEventPlot() {
        return eventPlot;
    }

//    public String getPlotTitle() {
//        return title;
//    }
    public boolean hasLeftYAxes() {
        return hasLeftYAxis;
    }

    public boolean hasRightAxes() {
        return hasRightYAxis;
    }

    public ValueAxis getDomainAxis() {
        return domainAxis;
    }

    /**
     * Liefert ein {@link DateInterval} mit Start- und Enddatum zurück
     *
     * @return dateInterval
     */
    public DateInterval getTimeInterval() {
        return this.dateInterval;
    }

    public String getRemark() {
        return remark;
    }
}
