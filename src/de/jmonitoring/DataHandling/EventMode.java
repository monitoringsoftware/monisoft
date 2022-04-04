package de.jmonitoring.DataHandling;

/**
 * Ths enum holds the two event modes<p> -
 * <code>EVENT_MEAN_MODE</code>: Calculates a mean interval of the event
 * states<br> -
 * <code>EVENT_SIGNIFICANT_MODE</code>: Return the state that was prevailing for
 * most of the time
 *
 * @author togro
 */
public enum EventMode {

    EVENT_MEAN_MODE,
    EVENT_SIGNIFICANT_MODE;
}
