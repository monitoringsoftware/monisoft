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
public class MonthInterval implements Cloneable,Serializable {

//    public final static int MONTH = 0;
//    public final static int YEAR = 1;
    private int startMonth,  endMonth;
    private int startYear,  endYear;

    public MonthInterval(int startMonth, int startYear, int endMonth, int endYear) {
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public MonthInterval() {
    }

    public void setStartMonth(int month, int year) {
        this.startMonth = month;
        this.startYear = year;
    }

    public void setEndMonth(int month, int year) {
        this.startMonth = month;
        this.startYear = year;
    }

    public int[] getStartMonth() {
        int[] week = {this.startMonth, startYear};
        return week;
    }

    public int[] getEndMonth() {
        int[] month = {this.endMonth, endYear};
        return month;
    }

    /**
     *  Gibt das Startdatum des Zeitintervalls zurück
     * 
     * @return startDate
     */
    public Date getStartDate() {
        GregorianCalendar date = new GregorianCalendar();
        date.clear();
        date.set(Calendar.MONTH, startMonth);
        date.set(Calendar.YEAR, startYear);
        return date.getTime();
    }

    /**
     * Gibt das Enddatum des Zeitintervalls zurück
     * 
     * @return endDate
     */
    public Date getEndDate() {
        GregorianCalendar date = new GregorianCalendar();
        date.clear();
        date.set(Calendar.MONTH, endMonth);
        date.set(Calendar.YEAR, endYear);
        date.add(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        date.add(Calendar.SECOND,-1);
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
        date.set(Calendar.MONTH, startMonth);
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
        date.set(Calendar.MONTH, endMonth);
        date.set(Calendar.YEAR, endYear);
        date.add(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));
        date.add(Calendar.SECOND,-1);
        return format.format(date.getTime());
    }
}
