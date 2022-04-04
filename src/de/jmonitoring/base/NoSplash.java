/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.base;

/**
 * A non functional splash screen dummy for the CLI intercapting calls to it
 *
 * @author togro
 */
public class NoSplash implements MonisoftSplash {

    /**
     * Create a non functional splash screen
     */
    public NoSplash() {
        super();
    }

    /**
     * Not applicable with CLI
     */
    @Override
    public void initialize() {
        // does nothing
    }

    /**
     * Not applicable with CLI
     */
    @Override
    public void showMessage(String text, int step) {
        // does nothing
    }
}
