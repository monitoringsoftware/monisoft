package de.jmonitoring.SensorCollectionHandling;

import de.jmonitoring.base.sensors.SensorProperties;
import java.util.HashSet;

/**
 * This class defines a sensor collection<p> A sensor collection is a named
 * collection of sensor ids that the user can define to quickly select a set of
 * sensors.<br>The sensors are stored in a {@link HashSet} as {@link SensorProperties}
 *
 * @author togro
 */
public class SensorCollection {

    private String name;
    private HashSet<SensorProperties> set = new HashSet<SensorProperties>(32);

    /**
     * Create a new {@link SensorCollection} with the given name
     *
     * @param name The name
     */
    public SensorCollection(String name) {
        this.name = name;
    }

    /**
     * Add a sensor to the list
     *
     * @param props The {@link SensorProperties} of the sensor
     */
    public void addSensor(SensorProperties props) {
        set.add(props);
    }

    /**
     * Remove a sensor to the list
     *
     * @param props The {@link SensorProperties} of the sensor to be deleted
     */
    public void removeSensor(SensorProperties props) {
        set.remove(props);
    }

    /**
     * Get the name of this {@link SensorCollection}
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of this {@link SensorCollection}
     *
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the set of all {@link SensorProperties} in this collection
     *
     * @return
     */
    public HashSet<SensorProperties> getPropertySet() {
        return set;
    }

    /**
     * Set the set of sensors in this collection to the given set of
     * {@link SensorProperties}
     *
     * @param set The new set
     */
    public void setSet(HashSet<SensorProperties> set) {
        this.set = set;
    }
}
