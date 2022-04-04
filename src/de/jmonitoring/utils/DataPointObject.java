package de.jmonitoring.utils;

import java.io.Serializable;

/**
 * A Object holding the x and y values for a data point.<p> Used to make a point
 * identifyable
 *
 * @author togro
 */
public class DataPointObject implements Serializable {

    private double x;
    private double y;

    public DataPointObject(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DataPointObject other = (DataPointObject) obj;
        if (this.x != other.x) {
            return false;
        }
        if (this.y != other.y) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }
}
