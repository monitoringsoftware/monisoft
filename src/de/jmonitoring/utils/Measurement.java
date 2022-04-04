package de.jmonitoring.utils;

/**
 * A Measurment takes the value and the time of a measurement.
 *
 * @author togro
 */
public class Measurement implements Comparable {

    private String virtualDefinition;
    private Long time;
    private Double value;

    public Measurement(Long time, Double value) {
        this.time = time;
        this.value = value;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    /**
     * Measurments are equal when they have the same time and value
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Measurement other = (Measurement) obj;
        if (this.time == null || !this.time.equals(other.time)) {
            return false;
        }
        if (this.value == null || !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.time != null ? this.time.hashCode() : 0);
        hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    /**
     * Measurements are sorted by time when compared
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Object o) {
        if (this.time.equals(((Measurement) o).getTime())) {
            return 0;
        } else if (this.time > ((Measurement) o).getTime()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Measurement time: " + time + ", value: " + value;
    }

    /**
     * @return the virtualDefinition
     */
    public String getVirtualDefinition() {
        return virtualDefinition;
    }

    /**
     * @param virtualDefinition the virtualDefinition to set
     */
    public void setVirtualDefinition(String virtualDefinition) {
        this.virtualDefinition = virtualDefinition;
    }
}
