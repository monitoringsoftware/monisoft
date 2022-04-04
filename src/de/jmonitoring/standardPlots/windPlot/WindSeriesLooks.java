package de.jmonitoring.standardPlots.windPlot;

import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to
 * define the appearance of a carpet plot. (currently none)<p>
 *
 * @author togro
 */
public class WindSeriesLooks extends SeriesLooks implements Serializable {

    /**
     * Creates a new instance of WindSeriesLooks with the given id
     */
    public WindSeriesLooks(int id) {
        super(id);
    }
}
