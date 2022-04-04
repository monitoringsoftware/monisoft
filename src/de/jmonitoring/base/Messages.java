package de.jmonitoring.base;

/**
 * Show up messages by a {@link MessageDisplayer}
 *
 * @author togro
 */
public class Messages {

    public static MessageDisplayer messageDisplayer;

    public static synchronized void showOptionPane(String message) {
        messageDisplayer.showMessageDialog(message);
    }

    /**
     * Appends the message to the MoniSoft-Console without adding a
     * <code>\n</code> If
     * <code>force</code> is true the message will be printed even if protocol
     * output is disabled
     *
     * @param message The message
     * @param force fore output even in non verbose mode
     */
    public static synchronized void showMessage(String message, boolean force) {
        messageDisplayer.writeToConsole(message, force);
    }

    public static synchronized void showException(Exception e) {
        messageDisplayer.writeToDebugConsole(e);
    }
}
