package de.jmonitoring.standardPlots.ogivePlot;

import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.Messages;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * Create a plot from the given parameters
 *
 * @author togro
 */
public class OgivePlotMaker {

    private XYPlot plot = new XYPlot();
    private GenerateOgiveDataSet sSet;
    private String title = "";

    /**
     * Creates a new instance of DLPlotMaker
     *
     * @param gui
     */
    public OgivePlotMaker(OgiveChartDescriber describer, DatasetWorkerFactory workerFactory) {
        List seriesCollection = describer.getchartCollection();
        DateInterval dateInterval = describer.getDateInterval();
        XYLineAndShapeRenderer renderer;
        sSet = new GenerateOgiveDataSet(seriesCollection, dateInterval, workerFactory);

        Messages.showMessage("Generiere Achsen und Renderer\n", true);

        int index = 0;
        String displayUnit = "";
        for (Iterator it = seriesCollection.iterator(); it.hasNext();) {
            OgiveSeriesLooks currentLooks = (OgiveSeriesLooks) it.next();
            if (currentLooks != null) {
                plot.setDataset(index, sSet.getDataSet()[index]);
                renderer = getDataSetRenderer(currentLooks);
                title = currentLooks.getSensor();

                if (currentLooks.getPowerWanted()) {
                    displayUnit = new UnitTransfer().getPowerUnitFromConsumption(currentLooks.getUnit());
                } else {
                    displayUnit = currentLooks.getUnit();
                }
                NumberAxis valueAxis = new NumberAxis(displayUnit);
                NumberAxis hourAxis = new NumberAxis("Stunden");
                hourAxis.setRange(0d, dateInterval.getSpanInHours());
                NumberAxis percAxis = new NumberAxis("%");
                percAxis.setRange(0, 100);
                percAxis.setTickUnit(new NumberTickUnit(10));
                if (currentLooks.getflipAxis()) {
                    plot.setDomainAxis(valueAxis);
                    plot.setRangeAxis(hourAxis);
                    plot.setRangeAxis(1, percAxis);
                } else {
                    plot.setDomainAxis(0, hourAxis);
                    plot.setDomainAxis(1, percAxis);
                    plot.setRangeAxis(valueAxis);
                }
                plot.setRenderer(renderer);
                plot.setDomainCrosshairVisible(true);
                index++;
            }
        }

        // Titel setzen, wenn der Describer bereits einen enthält diesen verwenden, ansonsten den Standardtitel
        if (describer.getPlotTitle() == null || describer.getPlotTitle().isEmpty()) {
            describer.setPlotTitle(title);
        }

        describer.setPlotSubtitle("Schieber am unteren Rand ziehen um hier Werte anzuzeigen");
    }

    private XYLineAndShapeRenderer getDataSetRenderer(OgiveSeriesLooks seriesLooks) {
        XYLineAndShapeRenderer xy_renderer = new XYLineAndShapeRenderer(true, false);
        return xy_renderer;
    }

    public int getmissingHours() {
        return sSet.getInvalidHours();
    }

    public XYPlot getPlotInfo() {
        return plot;
    }
}
