package de.jmonitoring.base;

/**
 * Mehtods used to handle the console of the GUI
 *
 * @author togro
 */
public interface ConsoleManager {

    /**
     * Copies the currently selected text of the console to the clipboard
     */
    void copyConsoleToClipBoard();

    /**
     * Clears the console
     */
    void clearConsole();
}
