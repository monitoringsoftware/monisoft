/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.DataHandling.IntervalMeasurement;
import java.util.ArrayList;

import org.jfree.data.time.TimePeriod;

/**
 * This interface defines common methods for all interpolator types
 *
 * @author togro
 */
public interface InterpolatorInterface {

    IntervalMeasurement calculateInterval();

    TimePeriod getInterval();

    void setInterval(TimePeriod interval);

    void setDurationOfValidity(Long valididty);

    ArrayList<String> getRemarks();
}
