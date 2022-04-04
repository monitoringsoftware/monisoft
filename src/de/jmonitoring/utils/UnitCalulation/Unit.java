package de.jmonitoring.utils.UnitCalulation;

import java.io.Serializable;

/**
 * This calls defines a unit for sensors
 *
 * @author togro
 */
public class Unit implements Serializable {

    private String unit = null;
    private String description = null;

    /**
     * Constructor for empty Unit
     */
    public Unit() {
    }

    /*
     *  Constructor for Unit with given name
     * 
     * @param unit The name
     */
    public Unit(String unit) {
        this.unit = unit;
    }

    /**
     * Constructor for Unit with given name and description
     *
     * @param unit The name
     * @param description The description
     */
    public Unit(String unit, String description) {
        this.unit = unit;
        this.description = description;
    }

    /**
     * return the unit description
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * set the unit description
     *
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * return the unit name
     *
     * @return The name
     */
    public String getUnit() {
        return unit;
    }

    /**
     * set the unit name
     *
     * @param unit The name
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return unit;
    }
}
