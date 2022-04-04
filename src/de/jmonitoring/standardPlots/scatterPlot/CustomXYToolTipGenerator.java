package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.utils.DataPointObject;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import org.jfree.chart.labels.AbstractXYItemLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.PublicCloneable;

/**
 * A tooltip generator defining tooltips for data points in the scatter plots
 *
 * @author togro
 */
public class CustomXYToolTipGenerator extends AbstractXYItemLabelGenerator implements XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {

    private static final long serialVersionUID = -3564164459039540784L;
    /**
     * The default tooltip format.
     */
    public static final String DEFAULT_TOOL_TIP_FORMAT = "{0}: ({1}, {2})";
    private HashMap<DataPointObject, String> labelMap;

    /**
     * Returns a tool tip generator that formats the x-values as dates and the
     * y-values as numbers.
     *
     * @return A tool tip generator (never <code>null</code>).
     */
    public static CustomXYToolTipGenerator getTimeSeriesInstance() {
        return new CustomXYToolTipGenerator(DEFAULT_TOOL_TIP_FORMAT,
                DateFormat.getInstance(), NumberFormat.getInstance());
    }

    /**
     * Creates a tool tip generator using default number formatters.
     */
    public CustomXYToolTipGenerator() {
        this(DEFAULT_TOOL_TIP_FORMAT, NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance());
    }

    /**
     * Creates a tool tip generator using the specified number formatters.
     *
     * @param formatString the item label format string (<code>null</code> not
     * permitted).
     * @param xFormat the format object for the x values (<code>null</code> not
     * permitted).
     * @param yFormat the format object for the y values (<code>null</code> not
     * permitted).
     */
    public CustomXYToolTipGenerator(String formatString,
            NumberFormat xFormat, NumberFormat yFormat) {

        super(formatString, xFormat, yFormat);

    }

    /**
     * Creates a tool tip generator using the specified number formatters.
     *
     * @param formatString the label format string (<code>null</code> not
     * permitted).
     * @param xFormat the format object for the x values (<code>null</code> not
     * permitted).
     * @param yFormat the format object for the y values (<code>null</code> not
     * permitted).
     */
    public CustomXYToolTipGenerator(String formatString, DateFormat xFormat,
            NumberFormat yFormat) {

        super(formatString, xFormat, yFormat);

    }

    /**
     * Creates a tool tip generator using the specified formatters (a number
     * formatter for the x-values and a date formatter for the y-values).
     *
     * @param formatString the item label format string (<code>null</code> not
     * permitted).
     * @param xFormat the format object for the x values (<code>null</code>
     * permitted).
     * @param yFormat the format object for the y values (<code>null</code> not
     * permitted).
     *
     * @since 1.0.4
     */
    public CustomXYToolTipGenerator(String formatString,
            NumberFormat xFormat, DateFormat yFormat) {

        super(formatString, xFormat, yFormat);
    }

    /**
     * Creates a tool tip generator using the specified date formatters.
     *
     * @param formatString the label format string (<code>null</code> not
     * permitted).
     * @param xFormat the format object for the x values (<code>null</code> not
     * permitted).
     * @param yFormat the format object for the y values (<code>null</code> not
     * permitted).
     */
    public CustomXYToolTipGenerator(String formatString,
            DateFormat xFormat, DateFormat yFormat) {

        super(formatString, xFormat, yFormat);

    }

    /**
     * Generates the tool tip text for an item in a dataset.
     *
     * @param dataset the dataset (<code>null</code> not permitted).
     * @param series the series index (zero-based).
     * @param item the item index (zero-based).
     *
     * @return The tooltip text (possibly <code>null</code>).
     */
    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        DataPointObject d = new DataPointObject(dataset.getXValue(series, item), dataset.getYValue(series, item));
        String label = labelMap.get(d);
        return "<html><body>" + generateLabelString(dataset, series, item) + " <b>" + label + "</body></html>";
    }

    /**
     * Generates the tool tip text for an item in a dataset.
     *
     * @param dataset the dataset (<code>null</code> not permitted).
     * @param series the series index (zero-based).
     * @param item the item index (zero-based).
     *
     * @return The tooltip text (possibly <code>null</code>).
     */
    public void setLabelMap(HashMap<DataPointObject, String> map) {
        this.labelMap = map;
    }

    /**
     * Generates the tool tip text for an item in a dataset.
     *
     * @param dataset the dataset (<code>null</code> not permitted).
     * @param series the series index (zero-based).
     * @param item the item index (zero-based).
     *
     * @return The tooltip text (possibly <code>null</code>).
     */
    public String getLabelOfPoint(double x, double y) {
        DataPointObject d = new DataPointObject(x, y);
        String label = labelMap.get(d);
        return label;
    }

    /**
     * Tests this object for equality with an arbitrary object.
     *
     * @param obj the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CustomXYToolTipGenerator)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns an independent copy of the generator.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if cloning is not supported.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
