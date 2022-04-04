package de.jmonitoring.standardPlots.plotTabs;

import de.jmonitoring.base.MainApplication;
import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.JPanel;

/**
 * A common panel used for all tabs for the standard charts in the GUI
 *
 * @author togro
 */
public abstract class PlotBaseTab extends JPanel implements StandardPlotTab {

    private final MainApplication gui;

    public PlotBaseTab(MainApplication gui) {
        super();
        this.gui = gui;
    }

    protected MainApplication gui() {
        return this.gui;
    }

    @Override
    public final String getTitle() {
        return ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft." + getTabName() + ".TabConstraints.tabTitle");
    }

    protected abstract String getTabName();

    @Override
    public Component asComponent() {
        return this;
    }
}