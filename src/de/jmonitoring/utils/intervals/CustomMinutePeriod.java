/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.intervals;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;

/**
 * Repräsentiert ein regelmässiges, beliebiges Zeitintervall in Minuten.<br>
 * Gültige Werte liegen im Bereich 1 bis 59 Minuten.
 *  
 * @author togro
 */
public class CustomMinutePeriod extends RegularTimePeriod implements Serializable {

    /** Die Zeitspanne */
    private byte duration;
    /** The day. */
    private Day day;
    /** The hour. */
    private byte hour;
    /** The minute. */
    private byte minute;
    /** The first millisecond. */
    private long firstMillisecond;
    /** The last millisecond. */
    private long lastMillisecond;
    /** Useful constant for the first minute in a day. */
    public static final int FIRST_MINUTE_IN_HOUR = 0;
    /** Useful constant for the last minute in a day. */
    public static final int LAST_MINUTE_IN_HOUR = 59;

    /**
     * Erzeugt ein neues, leeres Zeitintervall
     * 
     */
    public CustomMinutePeriod() {
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem aktuellen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Minuten (gültig zwischen 1 und 59)
     */
    public CustomMinutePeriod(byte duration) {
        this(duration, new Date());
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Minuten  (gültig zwischen 1 und 59)
     * @param time
     */
    public CustomMinutePeriod(byte duration, Date time) {
        this(duration, time, RegularTimePeriod.DEFAULT_TIME_ZONE);
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Minuten  (gültig zwischen 1 und 59)
     * @param minute
     * @param hour
     */
    public CustomMinutePeriod(byte duration, int minute, Hour hour) {
        if (hour == null) {
            throw new IllegalArgumentException("Null 'hour' argument.");
        }
        if ((duration < 1) || (duration > 59)) {
            throw new IllegalArgumentException("Illegal 'duration' argument.");
        }
        this.duration = duration;
        this.minute = (byte) minute;
        this.hour = (byte) hour.getHour();
        this.day = hour.getDay();
        peg(GregorianCalendar.getInstance());
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt und der entsprechenden Zeitzone
     * 
     * @param duration Die Dauer des Intervalls in Minuten  (gültig zwischen 1 und 59)
     * @param time
     * @param zone
     */
    public CustomMinutePeriod(byte duration, Date time, TimeZone zone) {
        if (time == null) {
            throw new IllegalArgumentException("Null 'time' argument.");
        }
        if (zone == null) {
            throw new IllegalArgumentException("Null 'zone' argument.");
        }
        if ((duration < 1) || (duration > 59)) {
            throw new IllegalArgumentException("Illegal 'duration' argument.");
        }
        Calendar calendar = Calendar.getInstance(zone);
        calendar.setTime(time);

        this.minute = (byte) calendar.get(Calendar.MINUTE);
        this.hour = (byte) calendar.get(Calendar.HOUR_OF_DAY);
        this.day = new Day(time, zone);
        this.duration = duration;
        peg(calendar);
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Minuten  (gültig zwischen 1 und 59)
     * @param minute
     * @param hour
     * @param day
     * @param month
     * @param year
     */
    public CustomMinutePeriod(byte duration, int minute, int hour, int day, int month, int year) {
        this(duration, new Minute(minute, hour, day, month, year).getStart());
    }

    /**
     * Liefert das vorherige Zeitintervall
     * 
     * @return {@link CustomMinutePeriod} voheriges Intervall
     */
    @Override
    public RegularTimePeriod previous() {
        CustomMinutePeriod result;
        int previousMinute;

        previousMinute = this.minute - this.duration;

        if (previousMinute >= FIRST_MINUTE_IN_HOUR) {    // vorherige Minute fällt in die gleiche Stunde
            result = new CustomMinutePeriod(this.duration, previousMinute, getHour());
        } else {                                          // vorherige Minute fällt in die vorherige Stunde
            Hour h = (Hour) getHour().previous();
            if (h != null) {
                result = new CustomMinutePeriod(this.duration, previousMinute + 60, h);
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Liefert das nächste Zeitintervall
     * 
     * @return {@link CustomMinutePeriod} nächstes Intervall
     */
    @Override
    public CustomMinutePeriod next() {
        CustomMinutePeriod result = null;
        byte nextMinute = (byte) (this.minute + this.duration);
        if (nextMinute <= LAST_MINUTE_IN_HOUR) {    // falls die nächste Minute in die gleiche Stunde fällt
            result = new CustomMinutePeriod(this.duration, nextMinute, getHour());
            this.minute = nextMinute;
        } else {                                    // falls die neue Minute in die nächste Stunde fällt
            Hour nextHour = (Hour) getHour().next();
            if (nextHour != null) {
                result = new CustomMinutePeriod(this.duration, nextMinute - 60, nextHour);
                this.minute = (byte) (nextMinute - 60);
//                System.out.println("in nächster Stunde " + this.minute);
            } else {
                result = null;
            }
        }
//        System.out.println("Danach: " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(new Date(result.getFirstMillisecond())));
        return result;
    }

    @Override
    public long getSerialIndex() {
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        return hourIndex * 60L + this.minute;
    }

    @Override
    public void peg(Calendar calendar) {
        this.firstMillisecond = getFirstMillisecond(calendar);
        this.lastMillisecond = getLastMillisecond(calendar);
    }

    /**
     * Returns the day.
     * 
     * @return The day.
     */
    public Day getDay() {
        return this.day;
    }

    /**
     * Returns the hour.
     *
     * @return The hour (never <code>null</code>).
     */
    public Hour getHour() {
        return new Hour(this.hour, this.day);
    }

    /**
     * Returns the hour.
     * 
     * @return The hour.
     */
    public int getHourValue() {
        return this.hour;
    }

    /**
     * Returns the minute.
     *
     * @return The minute.
     */
    public int getMinute() {
        return this.minute;
    }

    /**
     * Returns the first millisecond of the time period.  This will be determined 
     * relative to the time zone specified in the constructor, or in the 
     * calendar instance passed in the most recent call to the 
     * {@link #peg(Calendar)} method.
     *
     * @return The first millisecond of the day.
     * 
     * @see #getLastMillisecond()
     */
    @Override
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }

    /** 
     * Recalculates the start date/time and end date/time for this time period 
     * relative to the supplied calendar (which incorporates a time zone).
     * 
     * @param calendar  the calendar (<code>null</code> not permitted).
     */
    @Override
    public long getFirstMillisecond(Calendar calendar) {
        int year = this.day.getYear();
        int month = this.day.getMonth() - 1;
        int day = this.day.getDayOfMonth();

        calendar.clear();
        calendar.set(year, month, day, this.hour, this.minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns the last millisecond of the day.  This will be 
     * determined relative to the time zone specified in the constructor, or
     * in the calendar instance passed in the most recent call to the 
     * {@link #peg(Calendar)} method.
     *
     * @return The last millisecond of the day.
     * 
     * @see #getFirstMillisecond()
     */
    @Override
    public long getLastMillisecond() {
        return this.lastMillisecond;
    }

    /**
     * Returns the last millisecond of the time period, evaluated using the supplied
     * calendar (which determines the time zone).
     *
     * @param calendar  calendar to use (<code>null</code> not permitted).
     *
     * @return The end of the day as milliseconds since 01-01-1970.
     *
     * @throws NullPointerException if <code>calendar</code> is 
     *     <code>null</code>.
     */
    @Override
    public long getLastMillisecond(Calendar calendar) {
        int year = this.day.getYear();
        int month = this.day.getMonth() - 1;
        int day = this.day.getDayOfMonth();

        calendar.clear();

//        if (this.minute + this.duration > LAST_MINUTE_IN_HOUR) {
//            Hour nextHour = (Hour) getHour().next();
//            calendar.set(year, month, day, this.hour, this.minute + this.duration - 1, 59);
//        } else {
        calendar.set(year, month, day, this.hour, this.minute + this.duration - 1, 59);
//        }
        calendar.set(Calendar.MILLISECOND, 999);

        //return calendar.getTimeInMillis();  // this won't work for JDK 1.3
        return calendar.getTime().getTime();
    }

    /**
     * Returns an integer indicating the order of this Day object relative to
     * the specified object:
     *
     * negative == before, zero == same, positive == after.
     *
     * @param o1  the object to compare.
     *
     * @return negative == before, zero == same, positive == after.
     */
    @Override
    public int compareTo(Object o1) {
        int result;

        // CASE 1 : Comparing to another Minute object
        // -------------------------------------------
        if (o1 instanceof CustomMinutePeriod) {
            CustomMinutePeriod m = (CustomMinutePeriod) o1;
            result = getHour().compareTo(m.getHour());
            if (result == 0) {
                result = this.minute - m.getMinute();
            }
        } // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof RegularTimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        } // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else {
            // consider time periods to be ordered after general objects
            result = 1;
        }

        return result;
    }
}
