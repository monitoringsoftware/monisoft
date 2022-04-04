/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmonitoring.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author az
 */
public class TestUtils {
    
    public static void main( String[] args )
    {
        System.out.println( "Test" );
        DateFormat dateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );
                
        Long ms = new Long( 1404764388000L );
        // long ms = new Long( 1408964083491L );
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis( ms );
        
        System.out.println( "Time: " + dateFormat.format( cal.getTime() ) );
        System.out.println( "Ms: " + cal.getTime().getTime() );
    }
}
