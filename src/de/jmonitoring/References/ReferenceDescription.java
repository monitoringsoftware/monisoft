package de.jmonitoring.References;

/**
 * This class represents the definition af reference values<p> Each reference
 * has a name, a unit and an optional description.<p>
 * Example:<br><i>
 * - name: BGF<br>
 * - description: Bruttogeschossfläche<br>
 * - unit: m³<br></i>
 *
 * @author togro
 */
public class ReferenceDescription implements Comparable {

    private String name;
    private String description;
    private Integer unitID;

    /**
     * Create an empty reference description
     */
    public ReferenceDescription() {
    }

    /**
     * Create a reference description using the given parameters
     *
     * @param name The name of the {@link ReferenceDescription}
     * @param description The description
     * @param unit The assigned {@link Unit}
     */
    public ReferenceDescription(String name, String description, Integer unit) {
        this.name = name;
        this.description = description;
        this.unitID = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getUnitID() {
        return unitID;
    }

    public void setUnitID(Integer unitID) {
        this.unitID = unitID;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ReferenceDescription) {
            ReferenceDescription extern = (ReferenceDescription) o;
            return this.getName().compareTo(extern.getName());
        }
        return 0;
    }
}
