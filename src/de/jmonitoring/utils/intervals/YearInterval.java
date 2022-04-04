/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.intervals;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author togro
 */
public class YearInterval implements Cloneable,Serializable {

    private int startYear;
    private int endYear;

    public YearInterval(int startYear, int endYear) {
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public void setStartYear(int year) {
        this.startYear = year;
    }

    public void setEndYear(int year) {
        this.endYear = year;
    }

    public int getstartYear() {
        return this.startYear;
    }

    public int getEndYear() {
        return this.endYear;
    }

    /**
     *  Gibt das Startdatum des Zeitintervalls als String im Format <code>format</code> zurück
     * 
     * @return startDate
     */
    public String getStartDateString(SimpleDateFormat format) {
        GregorianCalendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, startYear);
        return format.format(date.getTime());
    }

    /**
     * Gibt das Enddatum des Zeitintervalls als String im Format <code>format</code> zurück
     * 
     * @return endDate
     */
    public String getEndDateString(SimpleDateFormat format) {
        GregorianCalendar date = new GregorianCalendar();
        date.set(Calendar.YEAR, startYear);
        return format.format(date.getTime());
    }
}
