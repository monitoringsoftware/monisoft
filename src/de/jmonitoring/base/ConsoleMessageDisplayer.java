package de.jmonitoring.base;

/**
 * A message displayer for the CLI that directs all messages to STDOUT
 *
 * @author togro
 */
public class ConsoleMessageDisplayer implements MessageDisplayer {

    public ConsoleMessageDisplayer() {
        super();
    }

    /**
     * Print messages wich are shown in Dialogs
     *
     * @param message
     */
    @Override
    public void showMessageDialog(String message) {
        System.out.println(message);
    }

    /**
     * Handle messages written to the console
     *
     * @param text The message
     * @param force Flag indication if the text should be printed even if
     * verbose mode is off
     */
    @Override
    public void writeToConsole(String text, boolean force) {
        if (!force) {
            return;
        }
        System.out.print(text);
    }

    /**
     * Handle exceptions written to the debug console
     *
     * @param e The exception to be handled
     */
    @Override
    public void writeToDebugConsole(Exception e) {
        System.out.println(e.getMessage());
    }
}
