package de.jmonitoring.utils.intervals;

import ch.qos.logback.classic.util.ContextInitializer;
import de.jmonitoring.base.Messages;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *  Behälter für ein Start- und ein End-Datum vom Typ {@link Date}
 * 
 * @author togro
 */
public class DateInterval implements Cloneable, Serializable {

    /** Das Startdatum */
    private Date startDate;
    /** Das Enddatum */
    private Date endDate;

    /**
     * Erzeugt ein {@link DateInterval}-Objekt aus den übergabenen {@link Date}-Objekten
     * 
     * @param start
     * @param end
     */
    public DateInterval(Date start, Date end) {
        startDate = trimDate(start);
        endDate = extendDate(end);
    }

    /**
     * Erzeugt ein leeres {@link DateInterval}-Objekt
     */
    public DateInterval() {
    }

    // Monisoft-22: Das DateInterval soll nicht beschnitten werden
    public void setStartDateComplete(Date start)
    {
        startDate = start;
    }

    public void setEndDateComplete(Date end)
    {
        endDate = end;
    }

    /**
     * Setzt das Startdatum
     * 
     * @param start
     */
    public void setStartDate(Date start) {
        if (start != null) {
            startDate = trimDate(start);
        } else {
            startDate = null;
        }
    }

    /**
     * Setzt das Enddatum
     * 
     * @param end
     */
    public void setEndDate(Date end) {
        if (end != null) {
            endDate = extendDate(end);
        } else {
            endDate = null;
        }
    }

    /**
     * Setzt das Startdatum.<br>
     * Das Datum wird als String übergeben und mit <code>format</code> geparst.
     * 
     * @param start Das Datum als {@link String} oder <code>null</code> wenn der String nicht geparst werden kann
     * @param format das {@link SimpleDateFormat} mit dem der Datum-String umgewandelt wird
     */
    public void setStringStartDate(String start, SimpleDateFormat format) {
        try {
            startDate = trimDate(format.parse(start));
        } catch (ParseException ex) {
            startDate = null;
        }
    }

    /**
     * Setzt das Enddatum.<br>
     * Das Datum wird als String übergeben und mit <code>format</code> geparst.
     * 
     * @param end Das Datum als {@link String} oder <code>null</code> wenn der String nicht geparst werden kann
     * @param format das {@link SimpleDateFormat} mit dem der Datum-String umgewandelt wird
     */
    public void setStringEndDate(String end, SimpleDateFormat format) {
        try {
            endDate = extendDate(format.parse(end));
        } catch (ParseException ex) {
            endDate = null;
        }
    }

    /**
     * Gibt das Startdatum des Zeitintervalls zurück
     * 
     * @return 
     */
    public Date getStartDate() {
        if (startDate != null) {
            return new Date(startDate.getTime());
        }
        return null;
    }

    /**
     * Gibt das Enddatum des Zeitintervalls zurück
     * 
     * @return
     */
    public Date getEndDate() {
        if (endDate != null) {
            return new Date(endDate.getTime());
        }
        return null;
    }

    /**
     *  Gibt das Startdatum des Zeitintervalls als String im Format <code>format</code> zurück
     * 
     * @return startDate
     */
    public String getStartDateString(SimpleDateFormat format) {
        return format.format(startDate);
    }

    /**
     * Gibt das Enddatum des Zeitintervalls als String im Format <code>format</code> zurück
     * 
     * @return endDate
     */
    public String getEndDateString(SimpleDateFormat format) {
        return format.format(endDate);
    }

    /**
     * Liefert die Zeitspanne des Intervalls in Millisekunden
     * @return
     */
    public Long getSpanInMillis() {
        return getEndDate().getTime() + 1000L - getStartDate().getTime();
    }

    /**
     * Liefert die Zeitspanne des Intervalls in Stunden
     * @return
     */
    public Long getSpanInHours() {
        return (getEndDate().getTime() + 1000L - getStartDate().getTime()) / (1000L * 60L * 60L);
    }

        /**
     * Liefert die Zeitspanne des Intervalls in Stunden
     * @return
     */
    public Long getSpanInDays() {
        return (getEndDate().getTime() + 1000L - getStartDate().getTime()) / (1000L * 60L * 60L * 24);
    }

    /**
     * Erweitert das Datum mit '00:00:00'
     *
     * @return endDate
     */
    private Date trimDate(Date date) {
        if (date == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0).getTime();
    }

    /**
     * Erweitert das Datum mit '23:59:59'
     *
     * @return endDate
     */
    private Date extendDate(Date date) {
        if (date == null) {
            return null;
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
//        cal.add(Calendar.DATE, 1); // + 1 Tag ????????
//        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0).getTime();
        return new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 23, 59, 59).getTime();        
    }

    @Override
    public DateInterval clone() {
        try {
            return (DateInterval) super.clone();
        } catch (CloneNotSupportedException cnse) {
            Messages.showException(cnse);

            return null;
        }
    }
    
    /**
     * MONISOFT-22 Returns if the date is in summer or wintertime
     * 
     * @return true if a time is inside a summertime else returns false
     */
    public static boolean isInSummertime( long ms ) {
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
        
        Calendar calDate = Calendar.getInstance();
        calDate.setTimeInMillis( ms );
        
        Date date = calDate.getTime();

        TimeZone mesz = TimeZone.getTimeZone( "Europe/Berlin" );

        /*
        System.out.println( "default: " + mesz );
        System.out.println( "getDSTSavings: " + mesz.getDSTSavings() );
        System.out.println( "short: " + mesz.getDisplayName( mesz.inDaylightTime(date), TimeZone.SHORT ) );
        */
        System.out.println( "inDaylightTime: " + simpleDateFormat.format( date ) + " = " + mesz.inDaylightTime(date) );
        
        if( mesz.inDaylightTime(date) )
        {
            // System.out.println( "true " + simpleDateFormat.format( date ) );
            return true;
        }
        else
        {
            // System.out.println( "false " + simpleDateFormat.format( date ) );
            return false;
        }
        
        /*
        System.out.println( "default: " + TimeZone.getDefault() );
        System.out.println( "getDSTSavings: " + TimeZone.getDefault().getDSTSavings() );
        System.out.println( "short: " + TimeZone.getDefault().getDisplayName( TimeZone.getDefault().inDaylightTime(date), TimeZone.SHORT ) );
        */
        
        /*
            31. März 1996, 2:00 MEZ – 27. Oktober 1996, 3:00 MESZ
            30. März 1997, 2:00 MEZ – 26. Oktober 1997, 3:00 MESZ
            29. März 1998, 2:00 MEZ – 25. Oktober 1998, 3:00 MESZ
            28. März 1999, 2:00 MEZ – 31. Oktober 1999, 3:00 MESZ
            26. März 2000, 2:00 MEZ – 29. Oktober 2000, 3:00 MESZ
            25. März 2001, 2:00 MEZ – 28. Oktober 2001, 3:00 MESZ
            31. März 2002, 2:00 MEZ – 27. Oktober 2002, 3:00 MESZ
            30. März 2003, 2:00 MEZ – 26. Oktober 2003, 3:00 MESZ
            28. März 2004, 2:00 MEZ – 31. Oktober 2004, 3:00 MESZ
            27. März 2005, 2:00 MEZ – 30. Oktober 2005, 3:00 MESZ
            26. März 2006, 2:00 MEZ – 29. Oktober 2006, 3:00 MESZ
            25. März 2007, 2:00 MEZ – 28. Oktober 2007, 3:00 MESZ
            30. März 2008, 2:00 MEZ – 26. Oktober 2008, 3:00 MESZ
            29. März 2009, 2:00 MEZ – 25. Oktober 2009, 3:00 MESZ
            28. März 2010, 2:00 MEZ – 31. Oktober 2010, 3:00 MESZ
            27. März 2011, 2:00 MEZ – 30. Oktober 2011, 3:00 MESZ
            25. März 2012, 2:00 MEZ – 28. Oktober 2012, 3:00 MESZ
            31. März 2013, 2:00 MEZ – 27. Oktober 2013, 3:00 MESZ
            30. März 2014, 2:00 MEZ – 26. Oktober 2014, 3:00 MESZ
            29. März 2015, 2:00 MEZ – 25. Oktober 2015, 3:00 MESZ
            27. März 2016, 2:00 MEZ – 30. Oktober 2016, 3:00 MESZ
        
            Begin Sommerzeit 2017 	26. März 2017 	Sonntag 	12 	883
            Begin Sommerzeit 2018 	25. März 2018 	Sonntag 	12 	1247
            Begin Sommerzeit 2019 	31. März 2019 	Sonntag 	13 	1618
            Begin Sommerzeit 2020 	29. März 2020 	Sonntag 	13 	1982
            Begin Sommerzeit 2021 	28. März 2021 	Sonntag 	12 	2346
            Begin Sommerzeit 2022 	27. März 2022 	Sonntag 	12 	2710
            Begin Sommerzeit 2023 	26. März 2023 	Sonntag 	12 	3074
            Begin Sommerzeit 2024 	31. März 2024 	Sonntag 	13 	3445
        */
        
        // Start der Sommerzeit 4.7.2013
        // Ende der Sommerzeit 10.7.2013
        /*
        Calendar start = Calendar.getInstance();
        start.set( Calendar.YEAR, 2013 );
        start.set( Calendar.MONTH, 6 );
        start.set( Calendar.DATE, 4 );
        start.set( Calendar.HOUR_OF_DAY, 0 );
        start.set( Calendar.MINUTE, 0 );
        start.set( Calendar.SECOND, 0 );
        start.set( Calendar.MILLISECOND, 0 );

        Calendar end = Calendar.getInstance();
        end.set( Calendar.YEAR, 2013 );
        end.set( Calendar.MONTH, 6 );
        end.set( Calendar.DATE, 10 );
        end.set( Calendar.HOUR_OF_DAY, 0 );
        end.set( Calendar.MINUTE, 0 );
        end.set( Calendar.SECOND, 0 );
        end.set( Calendar.MILLISECOND, 0 );

        // System.out.println( "start " + simpleDateFormat.format( start.getTime() ) );
        // System.out.println( "end   " + simpleDateFormat.format( end.getTime() ) );
        
        if( date.after( start.getTime() ) && date.before( end.getTime() ) )
        {
            // System.out.println( "true " + simpleDateFormat.format( date ) );
            return true;
        }
        else
        {
            // System.out.println( "false " + simpleDateFormat.format( date ) );
            return false;
        }
        */
    }   
}
