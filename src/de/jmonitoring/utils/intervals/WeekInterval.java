/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.intervals;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author togro
 */
public class WeekInterval implements Cloneable,Serializable {

    public final static int WEEK = 0;
    public final static int YEAR = 1;
    private int startWeek, endWeek;
    private int startYear, endYear;

    public WeekInterval(int startWeek, int startYear, int endWeek, int endYear) {
        this.startWeek = startWeek;
        this.endWeek = endWeek;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public WeekInterval() {
    }

    public void setStartWeek(int week, int year) {
        this.startWeek = week;
        this.startYear = year;
    }

    public void setEndWeek(int week, int year) {
        this.startWeek = week;
        this.startYear = year;
    }

    public int[] getStartWeek() {
        int[] week = {this.startWeek, startYear};
        return week;
    }

    public int[] getEndWeek() {
        int[] week = {this.endWeek, endYear};
        return week;
    }

    public Date getStartDate() {
        GregorianCalendar date = new GregorianCalendar();
        date.clear();
        date.set(Calendar.WEEK_OF_YEAR, startWeek);
        date.set(Calendar.YEAR, startYear);
        return date.getTime();
    }

    public Date getEndDate() {
        GregorianCalendar date = new GregorianCalendar();
        date.clear();
        date.set(Calendar.WEEK_OF_YEAR, endWeek);
        date.set(Calendar.YEAR, endYear);
        date.add(Calendar.DAY_OF_MONTH, 6);
        return date.getTime();
    }

    /**
     *  Gibt das Startdatum des Zeitintervalls als String im Format <code>format</code> zurück
     * 
     * @return startDate
     */
    public String getStartDateString(SimpleDateFormat format) {
        GregorianCalendar date = new GregorianCalendar();
        date.clear();
        date.set(Calendar.WEEK_OF_YEAR, startWeek);
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
        date.clear();
        date.set(Calendar.WEEK_OF_YEAR, endWeek);
        date.set(Calendar.YEAR, endYear);
        date.add(Calendar.DAY_OF_MONTH, 6);
        return format.format(date.getTime());
    }
}
