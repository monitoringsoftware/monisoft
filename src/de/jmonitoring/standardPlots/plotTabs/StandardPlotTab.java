/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.plotTabs;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.Component;

/**
 * This interface is only implemented by widget classes.
 * Clients have to consider EDT threading rules.
 * @author dsl
 */
public interface StandardPlotTab {

    public static enum Result {
        APPLIED,
        IGNORED;
    }

    public Result fillFrom(ChartDescriber describer);

    public Component asComponent();

    public void setIntervalSelector(Models models);

    public void resetCollections(int index);

    public void lockDates(boolean lock);

    public void clearSelections();

    public void setSelectionsFrom(Models models);

    public void clearData();

    public DateInterval getSelectedInterval();

    public void setSelectedInterval(DateInterval newInterval);

    public String getTitle();
    
    public void fillAnnotationChooser();
}
