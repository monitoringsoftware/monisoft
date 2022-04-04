/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DataHandling.DataFilter;

import de.jmonitoring.base.sensors.SensorProperties;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class defines a value filter which can be used to filter data by the
 * value of other sensors
 *
 * @author togro
 */
public class ValueFilter implements Serializable {

    private String filterString = "";
    private String displayString = "";
    private String separator = "";
    private HashMap<Integer, String> variables = new HashMap<Integer, String>(16); // Zuordnung sensorID zu ihrem Variablennamen
    private ArrayList<ValueFilterComponent> filterComponents = new ArrayList<ValueFilterComponent>(16);
    private boolean remove;

    /**
     * Create a new instance of ValueFilter
     *
     * @param remove If <code>true</code> filterred value will be removed,
     * otherwise only marked
     */
    public ValueFilter(boolean remove) {
        this.remove = remove;
    }

    /**
     * Add a filter entry for a condition by AND
     *
     * @param sens The sensor
     * @param op The operator
     * @param val The test value
     */
    public void addAndFilter(SensorProperties sens, String op, Double val) {
        String varName = "#{V" + sens.getSensorID() + "}";
        String compareValue = Double.toString(val);        
        if (!filterString.isEmpty()) {
            separator = " && ";
        }
        filterString += separator + varName + " " + op + " " + compareValue;
        displayString += separator + sens.getSensorName() + " " + op + " " + compareValue;
        variables.put(sens.getSensorID(), varName);
        filterComponents.add(new ValueFilterComponent(op, sens.getSensorID(), val));
    }

    /**
     * Add a filter entry for a condition by OR
     *
     * @param sens The sensor
     * @param op The operator
     * @param val The test value
     */
    public void addOrFilter(SensorProperties sens, String op, Double val) {
        String varName = "#{V" + sens.getSensorID() + "}";
        String compareValue = Double.toString(val);
        if (!filterString.isEmpty()) {
            separator = " || ";
        }
        filterString += separator + varName + " " + op + " " + compareValue;
        displayString += separator + sens.getSensorName() + " " + op + " " + compareValue;
        variables.put(sens.getSensorID(), varName);
        filterComponents.add(new ValueFilterComponent(op, sens.getSensorID(), val));
    }

    /**
     * Return the generated filter string used for calculation
     *
     * @return The filter
     */
    public String getValueFilterString() {
        return filterString;
    }

    /**
     * Return the generated filter string for display (with variable names
     * replaced by sensor name)
     *
     * @return The filter
     */
    public String getVaulueFilterDisplayString() {
        return displayString;
    }

    /**
     * Return the used variables
     *
     * @return A map of the variables
     */
    public HashMap<Integer, String> getVariables() {
        return variables;
    }

    /**
     * Return a list of all {
     *
     * @list ValueFilterComponent}s used in thsi filter
     * @return
     */
    public ArrayList<ValueFilterComponent> getFilterComponents() {
        return filterComponents;
    }

    /**
     * Return the REMOVE status
     *
     * @return <code>true</code> if filtered values will be removed
     */
    public boolean isRemove() {
        return this.remove;
    }

    /**
     * Test if the given parameters make up a valid filter
     *
     * @param sensor The sensor
     * @param operator The operator
     * @param value The test value
     * @return<code>true</code> if the test was sucessful
     */
    public boolean testFilter(Object sensor, Object operator, Object value) {
        boolean valid = true;
//        System.out.println(">>>" + ((SensorProperties) sensor).getSensorID());
        if (sensor == null || ((SensorProperties) sensor).getSensorID() < 1) {
            valid = false;
        }
        if (operator == null || ((String) operator).isEmpty()) {
            valid = false;
        }
        if (value == null) {
            valid = false;
        } else {
            try {
                Double.valueOf((String) value);
            } catch (NumberFormatException e) {
                valid = false;
            }
        }

        return valid;
    }
}
