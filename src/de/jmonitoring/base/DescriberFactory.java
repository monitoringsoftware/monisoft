package de.jmonitoring.base;

import de.jmonitoring.standardPlots.common.ChartDescriber;

/**
 * An interface used by standard plots to build a {@link ChartDecscriber}
 *
 * @author togro
 */
public interface DescriberFactory {

    /**
     * Create a {@link ChartDecscriber}
     *
     * @return
     */
    ChartDescriber createChartDescriber();
}
