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
 * Repräsentiert ein regelmässiges, beliebiges Zeitintervall in Sekunden.<br>
 * Gültige Werte liegen im Bereich 1 bis 59 Sekunden.
 *  
 * @author togro
 */
public class CustomSecondPeriod extends RegularTimePeriod implements Serializable {

    /** Die Zeitspanne */
    private byte duration;
    /** The day. */
    private Day day;
    /** The hour. */
    private byte hour;
    /** The minute. */
    private byte minute;
    /** The second. */
    private byte second;
    /** The first millisecond. */
    private long firstMillisecond;
    /** The last millisecond. */
    private long lastMillisecond;
    /** Useful constant for the first minute in a day. */
    public static final int FIRST_SECOND_IN_HOUR = 0;
    /** Useful constant for the last minute in a day. */
    public static final int LAST_SECOND_IN_HOUR = 59;

    /**
     * Erzeugt ein neues, leeres Zeitintervall
     * 
     */
    public CustomSecondPeriod() {
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem aktuellen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Sekunden (gültig zwischen 1 und 59)
     */
    public CustomSecondPeriod(byte duration) {
        this(duration, new Date());
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Sekunden  (gültig zwischen 1 und 59)
     * @param time
     */
    public CustomSecondPeriod(byte duration, Date time) {
        this(duration, time, RegularTimePeriod.DEFAULT_TIME_ZONE);
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt
     * 
     * @param duration Die Dauer des Intervalls in Sekunden  (gültig zwischen 1 und 59)
     * @param second
     * @param minute
     */
    public CustomSecondPeriod(byte duration, int second, Minute minute) {
        if (minute == null) {
            throw new IllegalArgumentException("Null 'minute' argument.");
        }
        if ((duration < 1) || (duration > 59)) {
            throw new IllegalArgumentException("Illegal 'duration' argument.");
        }
        this.duration = duration;
        this.second = (byte) second;
        this.minute = (byte) minute.getMinute();
        this.hour = (byte) minute.getHourValue();
        this.day = minute.getDay();
        peg(GregorianCalendar.getInstance());
    }

    /**
     * Erzeugt ein neues Zeitintervall basierend auf dem angegebenen Zeitpunkt und der entsprechenden Zeitzone
     * 
     * @param duration Die Dauer des Intervalls in Sekunden  (gültig zwischen 1 und 59)
     * @param time
     * @param zone
     */
    public CustomSecondPeriod(byte duration, Date time, TimeZone zone) {
        if (time == null) {
            throw new IllegalArgumentException("Null 'time' argument.");
        }
        if (zone == null) {
            throw new IllegalArgumentException("Null 'zone' argument.");
        }
        if ((duration < 1) || (duration > 59)) {
            throw new IllegalArgumentException("Illegal 'duration' argument.");
        }
        Calendar calendar = GregorianCalendar.getInstance(zone);
        calendar.setTime(time);
        this.second = (byte) calendar.get(Calendar.SECOND);
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
    public CustomSecondPeriod(byte duration, int minute, int hour, int day, int month, int year) {
        this(duration, new Minute(minute, hour, day, month, year).getStart());
    }

    /**
     * Liefert das vorherige Zeitintervall
     * 
     * @return {@link CustomSecondPeriod} voheriges Intervall
     */
    @Override
    public RegularTimePeriod previous() {
        CustomSecondPeriod result;
        int previousSecond;

        previousSecond = this.second - this.duration;

        if (previousSecond >= FIRST_SECOND_IN_HOUR) {    // vorherige Sekunde fällt in die gleiche Minute
            result = new CustomSecondPeriod(this.duration, previousSecond, getMinute());
        } else {                                          // vorherige Sekunde fällt in die vorherige Minute
            Minute m = (Minute) getMinute().previous();
            if (m != null) {
                result = new CustomSecondPeriod(this.duration, previousSecond + 60, getMinute());
            } else {
                result = null;
            }
        }
        return result;
    }

    /**
     * Liefert das nächste Zeitintervall
     * 
     * @return {@link CustomSecondPeriod} nächstes Intervall
     */
    @Override
    public CustomSecondPeriod next() {
        CustomSecondPeriod result = null;
        byte nextSecond = (byte) (this.second + this.duration);
        if (nextSecond <= LAST_SECOND_IN_HOUR) {    // falls die nächste Sekunde in die gleiche Minute fällt
            result = new CustomSecondPeriod(this.duration, nextSecond, getMinute());
            this.second = nextSecond;
        } else {                                    // falls die neue Sekunde in die nächste Minute fällt
            Minute nextMinute = (Minute) getMinute().next();
            if (nextMinute != null) {
//                result = new CustomSecondPeriod(this.duration, nextSecond - 60, getHour());
                result = new CustomSecondPeriod(this.duration, nextSecond - 60, nextMinute);
                this.second = (byte) (nextSecond - 60);
//                System.out.println("in nächster Minute " + this.second);
            } else {
                result = null;
            }
        }
//        System.out.println("Danach: " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(new Date(result.getFirstMillisecond())));
        return result;
    }

    @Override
    public long getSerialIndex() {
//        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
//        return hourIndex * 60L + this.second;
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        return hourIndex * 60L + this.minute + this.second;
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
    public Minute getMinute() {
        return new Minute(this.minute, getHour());
    }

    /**
     * Returns the second.
     *
     * @return The second.
     */
    public int getSecond() {
        return this.second;
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
        calendar.set(year, month, day, this.hour, this.minute, this.second);
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
        calendar.set(year, month, day, this.hour, this.minute + this.duration - 1, this.second);
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

        // CASE 1 : Comparing to another CustomSecondPeriod object
        // -------------------------------------------
        if (o1 instanceof CustomSecondPeriod) {
            CustomSecondPeriod m = (CustomSecondPeriod) o1;
            result = getDay().compareTo(m.getDay());
            result += getHour().compareTo(m.getHour());
            result += getMinute().compareTo(m.getMinute());
            if (result == 0) { // Gleicher Tag, gleiche Stunde, 
                result = this.second - m.getSecond();
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
