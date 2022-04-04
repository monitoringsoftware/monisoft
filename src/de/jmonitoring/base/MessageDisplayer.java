package de.jmonitoring.base;

/**
 * An interface for displaying messages. Different implementation by CLI and GUI
 *
 * @author togro
 */
public interface MessageDisplayer {

    /**
     * Show a message in a dialog window (if CLI STDOUT)
     *
     * @param message The message to show
     */
    public void showMessageDialog(String message);

    /**
     * Write a message to the console (if CLI STDOUT)
     *
     * @param test The message to show
     * @param force if <code>treu</code> the message will even by displayed if
     * verbose mode is off
     */
    public void writeToConsole(String message, boolean force);

    /**
     * Write a exception message to the debug console (if CLI STDOUT)
     *
     * @param e The exception
     */
    public void writeToDebugConsole(Exception e);
}
