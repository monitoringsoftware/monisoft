package de.jmonitoring.base;

/**
 * This interface includes methods for the control of status leds and
 * connecttion information in the status bar of the main GUI.
 *
 * @author togro
 */
public interface ConnectionStatus {

    /**
     * Sets the status of the appliaction to connected
     *
     * @param isConnected
     */
    public void setConnected(boolean isConnected);

    /**
     * Sets the connect LED to connected
     */
    public void enableConnectedLED();

    /**
     * Sets the connect LED to disconnected
     */
    public void enableDisconnectedLED();

    /**
     * Sets the connect LED to idle
     */
    public void enableIdleLED();

    /**
     * Sets the connetion label
     *
     * @param text The text to be displayed
     */
    public void setLabel(String text);
}
