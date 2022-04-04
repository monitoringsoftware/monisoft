/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author togro
 */
public class ProcessStatus {

    public static final String PROGRESS = "Progress";
    private PropertyChangeSupport propertyChangeSupport;
    private int progress = 0;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

    public void setProgress(int progress) {
        int oldProgress = progress;
        this.progress = progress;
        firePropertyChange(PROGRESS, oldProgress, progress);
    }

    public int getProgress() {
        return progress;
    }
}
