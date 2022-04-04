/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmonitoring.utils;

/**
 * A thread that will be stopped if its <code>running</code> field is <code>false</code>
 * @author togro
 */
public class StoppableThread extends Thread {
    public boolean running = false;
    
    public void stopIt() {
        running = false;
    }

    public StoppableThread() {
    }

    
    
    public StoppableThread(Runnable target) {
        super(target);
    }
}
