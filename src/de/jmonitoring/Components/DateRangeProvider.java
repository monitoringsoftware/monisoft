/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Components;

import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author dsl
 */
public interface DateRangeProvider {

    DateInterval getCustomdateInterval();

    boolean useCustomPeriod();
}
