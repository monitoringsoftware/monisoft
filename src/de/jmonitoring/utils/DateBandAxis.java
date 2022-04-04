/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import com.lowagie.text.Font;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.BasicStroke;
import java.awt.Color;
import java.text.SimpleDateFormat;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.data.time.*;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author togro
 */
public class DateBandAxis extends PeriodAxis {

    private PeriodAxisLabelInfo[] info;
    public DateBandAxis(DateInterval dateInterval, Class spanClass, boolean minutes, boolean hours, boolean days, boolean monthyear, int fontSize) {
        super("");
        setAutoRangeTimePeriodClass(Millisecond.class);
        setInfoLabel(minutes, hours, days, monthyear, fontSize);
        setLabelInfo(info);
        setMajorTickTimePeriodClass(spanClass);
        // test, maybe eliminated if problems occur
        if (dateInterval != null) {
            setLowerBound(dateInterval.getStartDate().getTime());
            setUpperBound(dateInterval.getEndDate().getTime());
        }
    }

    /**
     * Setzt die Bänder des InfoLabels
     *
     * @param minutes
     * @param hours
     * @param days
     * @param monthyear
     * @param fontSize
     */
    private void setInfoLabel(boolean minutes, boolean hours, boolean days, boolean monthyear, int fontSize) {
        int h = (hours) ? 1 : 0;
        int d = (days) ? 1 : 0;
        int mo = (monthyear) ? 1 : 0;
        int m = (minutes) ? 1 : 0;

        info = new PeriodAxisLabelInfo[m + h + d + mo];

        int lfd = 0;
        if (minutes) {
            info[lfd] = new PeriodAxisLabelInfo(Minute.class, new SimpleDateFormat("mm"), new RectangleInsets(0, 0, 0, 0), new java.awt.Font("Lucida Sans", Font.NORMAL, fontSize), Color.BLACK, true, new BasicStroke(1.0f), Color.LIGHT_GRAY);
            lfd++;
        }
        if (hours) {
            info[lfd] = new PeriodAxisLabelInfo(Hour.class, new SimpleDateFormat("HH"), new RectangleInsets(0, 0, 0, 0), new java.awt.Font("Lucida Sans", Font.NORMAL, fontSize), Color.BLACK, true, new BasicStroke(1.0f), Color.LIGHT_GRAY);
            lfd++;
        }
        if (days) {
            info[lfd] = new PeriodAxisLabelInfo(Day.class, new SimpleDateFormat("d E"), new RectangleInsets(0, 0, 0, 0), new java.awt.Font("Lucida Sans", Font.NORMAL, fontSize), Color.BLACK, true, new BasicStroke(1.0f), Color.LIGHT_GRAY);
            lfd++;
        }
        if (monthyear) {
            info[lfd] = new PeriodAxisLabelInfo(Month.class, new SimpleDateFormat("MMM yy"), new RectangleInsets(0, 0, 0, 0), new java.awt.Font("Lucida Sans", Font.BOLD, fontSize), Color.BLACK, true, new BasicStroke(1.0f), Color.LIGHT_GRAY);
            lfd++;
        }
    }
    
    public void setSpanClass(Class spanClass) {
        setMajorTickTimePeriodClass(spanClass);
    }
}
