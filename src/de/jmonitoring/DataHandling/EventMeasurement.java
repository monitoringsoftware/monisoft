package de.jmonitoring.DataHandling;

/**
 * This class represents an event measurement.<p> Each masurement is defined by
 * its start time, its state and the duration until this state changes or gets
 * invalid.
 *
 * @author togro
 */
public class EventMeasurement implements Comparable {

    private Long timeStart;
    private Long duration;
    private Integer value;

    /**
     * Create measurement with the given paramter set
     *
     * @param timeStart
     * @param duration
     * @param value
     */
    public EventMeasurement(Long timeStart, Long duration, Integer value) {
        this.timeStart = timeStart;
        this.duration = duration;
        this.value = value;
    }

    /**
     * Create an empty measurement
     */
    public EventMeasurement() {
        this(null, null, null);
    }

    /**
     * Return the starting time of the state
     *
     * @return The starting time
     */
    public Long getTimeStart() {
        return timeStart;
    }

    /**
     * Set the start time
     *
     * @param timeStart The time
     */
    public void setTimeStart(Long timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * Return the duration time of the state
     *
     * @return The duration time
     */
    public Long getDuration() {
        return duration;
    }

    /**
     * Set the duration time
     *
     * @param duration The duration
     */
    public void setDuration(Long duration) {
        this.duration = duration;
    }

    /**
     * return the state value
     *
     * @return The value
     */
    public Integer getValue() {
        return value;
    }

    /**
     * Set the state value
     *
     * @param value The value
     */
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Method for comparing, ordering event measurements<p> Measurments are
     * equal when they have the same time and value
     *
     * @param obj The Object to compare with
     * @return <code>true</code> if the given objects have the same time
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EventMeasurement other = (EventMeasurement) obj;
        if (this.timeStart == null || !this.timeStart.equals(other.timeStart)) {
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
        hash = 97 * hash + (this.timeStart != null ? this.timeStart.hashCode() : 0);
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
        if (this.timeStart.equals(((EventMeasurement) o).getTimeStart())) {
            return 0;
        } else if (this.timeStart > ((EventMeasurement) o).getTimeStart()) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "Measurement time: " + timeStart + ", value: " + value + "for ";
    }
}
