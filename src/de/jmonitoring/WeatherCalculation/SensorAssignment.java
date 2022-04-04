/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

import de.jmonitoring.base.sensors.SensorProperties;

/**
 *
 * @author togro
 */
public class SensorAssignment {

    private SensorProperties temperature = null;
    private SensorProperties humidity = null;
    private SensorProperties rainStatus = null;
    private SensorProperties rainAmount = null;
    private SensorProperties carbonDioxide = null;
    private SensorProperties windDirection = null;
    private SensorProperties windSpeed = null;
    private SensorProperties globalRadiation = null;
    private Integer buildingID = null;

    public SensorAssignment(Integer buildingID) {
        this.buildingID = buildingID;
    }

    public SensorProperties getTemperature() {
        return temperature;
    }

    public void setTemperature(SensorProperties temperature) {
        this.temperature = temperature;
    }

    public SensorProperties getHumidity() {
        return humidity;
    }

    public void setHumidity(SensorProperties humidity) {
        this.humidity = humidity;
    }

    public SensorProperties getRainStatus() {
        return rainStatus;
    }

    public void setRainStatus(SensorProperties rainStatus) {
        this.rainStatus = rainStatus;
    }

    public SensorProperties getRainAmount() {
        return rainAmount;
    }

    public void setRainAmount(SensorProperties rainAmount) {
        this.rainAmount = rainAmount;
    }

    public SensorProperties getCarbonDioxide() {
        return carbonDioxide;
    }

    public void setCarbonDioxide(SensorProperties carbonDioxide) {
        this.carbonDioxide = carbonDioxide;
    }

    public SensorProperties getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(SensorProperties windDirection) {
        this.windDirection = windDirection;
    }

    public SensorProperties getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(SensorProperties wind_speed) {
        this.windSpeed = wind_speed;
    }

    public SensorProperties getGlobalRadiation() {
        return globalRadiation;
    }

    public void setGlobalRadiation(SensorProperties globalRadiation) {
        this.globalRadiation = globalRadiation;
    }

    public Integer getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(Integer buildingID) {
        this.buildingID = buildingID;
    }
}
