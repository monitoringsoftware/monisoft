package de.jmonitoring.standardPlots.comparePlot;

import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to
 * define the appearance of a carpet plot. (currently none)<p>
 *
 * @author togro
 */
public class CompareSeriesLooks extends SeriesLooks implements Serializable {

    /**
     * Creates a new instance of CompareSeriesLooks with the given id
     */
    public CompareSeriesLooks(int ident) {
        super(ident);
    }
}
