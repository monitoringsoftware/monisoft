package de.jmonitoring.DataHandling.DataFilter;

import java.io.Serializable;

/**
 * Represents a single component of a multi-condition value filter
 *
 * @author togro
 */
public class ValueFilterComponent implements Serializable {

    private String operand;
    private Integer sensorID;
    private Double value;

    /**
     * Create a new instance with the given filter parameter
     *
     * @param operand The operand
     * @param sensorID The sensor ID
     * @param value The test value
     */
    public ValueFilterComponent(String operand, Integer sensorID, Double value) {
        this.operand = operand;
        this.sensorID = sensorID;
        this.value = value;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public Integer getSensorID() {
        return sensorID;
    }

    public void setSensorID(Integer sensorID) {
        this.sensorID = sensorID;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
