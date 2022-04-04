package de.jmonitoring.References;

import java.io.Serializable;

/**
 * Definiert einen Referenzwert zur Berechnung von spezifischen
 * Verbrauchsgrößen. Ein Referenzwert besteht aus einer Bezeichnung, einem Wert
 * und der dazugehörigen Einheit
 *
 * @author togro
 */
public class ReferenceValue implements Serializable {

    private String name;
    private double value;
    private Integer buildingID;
    private Integer zoneID;

    /**
     * Erzeugt einen neuen, leeren Referenzwert
     */
    public ReferenceValue() {
    }

    /**
     * Erzeugt einen neuen Referenzwert mit den übergebenen Parametern
     *
     * @param name
     * @param unitID
     * @param value
     * @param building
     * @param zone
     */
    public ReferenceValue(String name, double value, Integer building, Integer zone) {
        this.name = name;
        this.value = value;
        this.buildingID = building;
        this.zoneID = zone;

    }

    /**
     * Gibt das Gebäude das diesem Referenzwert zugeordnet ist zurück
     *
     * @return
     */
    public Integer getBuildingID() {
        return buildingID;
    }

    /**
     * Setzt das Gebäude das diesem Referenzwert zugeordnet ist
     *
     * @return
     */
    public void setBuildingID(Integer buildingID) {
        this.buildingID = buildingID;
    }

    /**
     * Gibt die Zone die diesem Referenzwert zugeordnet ist zurück
     *
     * @return
     */
    public Integer getZoneID() {
        return zoneID;
    }

    /**
     * Setzt die Zone die diesem Referenzwert zugeordnet ist
     *
     * @return
     */
    public void setZoneID(Integer zoneID) {
        this.zoneID = zoneID;
    }

    /**
     * Gibt die Bezeichnung des Referenzwertes zurück
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Gibt den Wert des Referenzwertes zurück
     *
     * @return
     */
    public double getValue() {
        return value;
    }

    /**
     * Setzt den Namen der Referenzgröße
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Setzt den Wert der Referenzgröße
     *
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }

    /**
     * Gibt den Namen des Referenzwertes zurück
     *
     * @return
     */
    @Override
    public String toString() {
        return name;
    }
}
