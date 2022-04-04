package de.jmonitoring.standardPlots.plotTabs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTabbedPane;


/**
 * Manages the plot tabe shown in the GUI for standard plot panels
 * @author togro
 */
public class PlotTabManager implements Iterable<StandardPlotTab> {

    private final List<StandardPlotTab> plots;
    private StandardPlotTab shownPlot;
    private final JTabbedPane tabs;

    public PlotTabManager() {
        super();
        this.plots = new ArrayList<StandardPlotTab>();
        this.tabs = new JTabbedPane();
    }

    public void addPlotTab(StandardPlotTab newPlot) {
        this.plots.add(newPlot);
        this.tabs.addTab(newPlot.getTitle(), newPlot.asComponent());
        if (this.shownPlot == null) {
        	this.shownPlot = newPlot;
        }
    }

    public JTabbedPane asTabbedPane() {
        return this.tabs;
    }

    public void showPlotTab(StandardPlotTab toShow) {
        this.tabs.setSelectedComponent(toShow.asComponent());
        this.shownPlot = toShow;
    }

    @Override
    public Iterator<StandardPlotTab> iterator() {
        return this.plots.iterator();
    }

    public StandardPlotTab getShownPlotTab() {
        return this.shownPlot;
    }

    public StandardPlotTab getPlotTabAt(int index) {
        return this.plots.get(index);
    }

    public void clearIconFor(StandardPlotTab previousPlot) {
        int index = this.plots.indexOf(previousPlot);
        this.tabs.setIconAt(index, null);
    }
}
