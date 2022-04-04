/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

/**
 *
 * @author togro
 */
public class DegreeDay {

    private Integer year;
    private Integer month;
    private String name;
    private Double degreedays;
    private boolean isGTZ;

    public DegreeDay() {
    }

    public DegreeDay(Integer year, Integer month, String name, Double degreedays, boolean isGTZ) {
        this.year = year;
        this.month = month;
        this.name = name;
        this.degreedays = degreedays;
        this.isGTZ = isGTZ;
    }

    public Double getDegreedays() {
        return degreedays;
    }

    public void setDegreedays(Double degreedays) {
        this.degreedays = degreedays;
    }

    public boolean isIsGTZ() {
        return isGTZ;
    }

    public void setIsGTZ(boolean isGTZ) {
        this.isGTZ = isGTZ;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
