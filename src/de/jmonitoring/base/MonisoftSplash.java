package de.jmonitoring.base;

/**
 * Interface defining methos for the splash screen
 *
 * @author togro
 */
public interface MonisoftSplash {

    /**
     * Setup splash screen
     */
    public void initialize();

    /**
     * Display message on the spalsh screen
     *
     * @param text Message to be displayed
     * @param step Step that the progressbar of the spash screen should extend
     */
    public void showMessage(String text, int step);
}
