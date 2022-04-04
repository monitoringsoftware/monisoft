/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.utils.DateCalculation;

import de.jmonitoring.utils.intervals.DateInterval;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author togro
 */
public class DateTimeCalculator {

    /**
     * Ermittelt für den übergebenen Zeitpunkt ob es sich um einen Werktag
     * handelt
     *
     * @param time
     * @return
     */
    public static boolean isWorkDayLong(long time) {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        int day = date.get(Calendar.DAY_OF_WEEK);

        if (day > 0 && day < 6) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isWeekendLong(long time) {
        return !isWorkDayLong(time);
    }

    public static boolean isWeekend(Date day) {
        boolean b = false;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(day);
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            b = true;
        }
        return b;
    }

    public static boolean isWorkday(Date day) {
        return !isWeekend(day);
    }

    public static DateInterval getToday() {
        GregorianCalendar gc = new GregorianCalendar(); // TODO auf heute setzen (Klammer leeren)

//        gc.set(Calendar.HOUR_OF_DAY, 23);
//        gc.set(Calendar.MINUTE, 59);
//        gc.set(Calendar.SECOND, 59);
//        Date endDate = new Date(gc.getTime().getTime());
//
//        gc.set(Calendar.HOUR_OF_DAY, 0);
//        gc.set(Calendar.MINUTE, 0);
//        gc.set(Calendar.SECOND, 0);
//        Date startDate = new Date(gc.getTime().getTime());

        return new DateInterval(gc.getTime(), gc.getTime());
    }

    public static DateInterval getYesterday() {
        GregorianCalendar gc = new GregorianCalendar(); // TODO auf heute setzen (Klammer leeren)
        gc.add(Calendar.DATE, -1);

//        gc.set(Calendar.HOUR_OF_DAY, 23);
//        gc.set(Calendar.MINUTE, 59);
//        gc.set(Calendar.SECOND, 59);
//        Date endDate = new Date(gc.getTime().getTime());
//
//        gc.set(Calendar.HOUR_OF_DAY, 0);
//        gc.set(Calendar.MINUTE, 0);
//        gc.set(Calendar.SECOND, 0);
//        Date startDate = new Date(gc.getTime().getTime());

//        return new DateInterval(startDate, endDate);
        return new DateInterval(gc.getTime(), gc.getTime());
    }

    /**
     * Berechnet eine Zeitspanne ausgehend vom übergebenen Datum
     *
     * @param offsetDays
     * @param durationDays
     * @param today
     * @return
     */
    public static DateInterval getTimeSpanBeforeToday(int offsetDays, int durationDays, Date today) { // 0 = Today, 1 = yesterday
        GregorianCalendar gc = new GregorianCalendar();

        if (today != null) {
            gc.setTime(today);
        }

        gc.add(Calendar.DATE, -offsetDays);
        Date startDate = new Date(gc.getTime().getTime());

        gc.add(Calendar.DATE, durationDays);
        Date endDate = new Date(gc.getTime().getTime());
        return new DateInterval(startDate, endDate);
    }

    /**
     * Ermittelt ob das Übergebene Jahr ein Schaltjahr ist
     *
     * @param year
     * @return
     */
    public static boolean isLeapYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            return true;
        }
        return false;



    }

    public Long getMySQLDate(String dateTime, String format) {
        Long timeLong = null;
        return timeLong;
    }
}
