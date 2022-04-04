package de.jmonitoring.DataHandling;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.NoOperationGUI;

/**
 * A factory class generating a {@link DatasetWorker} for a sensor
 *
 * @author togro
 */
public class DatasetWorkerFactory {

    private final MainApplication gui;

    /**
     * Take the given GUI (or nonGUI CLI)
     *
     * @param gui
     * @see NoOperationGUI
     */
    public DatasetWorkerFactory(MainApplication gui) {
        this.gui = gui;
    }

    /**
     * Return a {@link DatasetWorker} for the given sensor
     *
     * @param sensorId The ID of the sensor
     * @return The {@link DatasetWorker}
     */
    public DatasetWorker createFor(int sensorId) {
        return new DatasetWorker(sensorId, this.gui);
    }
}
