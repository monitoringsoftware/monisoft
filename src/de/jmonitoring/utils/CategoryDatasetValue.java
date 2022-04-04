/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.utils;

/**
 *
 * @author togro
 */
public class CategoryDatasetValue implements Comparable {

    private Double value;
    private Comparable<String> row;
    private Comparable<String> column; // die category auf der x-Achse

    public CategoryDatasetValue(Double value, Comparable<String> row, Comparable<String> column) {
        this.value = value;
        this.row = row;
        this.column = column;
    }

    public Comparable<String> getColumn() {
        return column;
    }

    public void setColumn(Comparable<String> column) {
        this.column = column;
    }

    public Comparable<String> getRow() {
        return row;
    }

    public void setRow(Comparable<String> row) {
        this.row = row;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CategoryDatasetValue other = (CategoryDatasetValue) obj;
        if (this.value == null || !this.value.equals(other.value)) {
            return false;
        }
        if (this.column == null || !this.column.equals(other.column)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.value != null ? this.value.hashCode() : 0);
        hash = 89 * hash + (this.column != null ? this.column.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof CategoryDatasetValue) {
            CategoryDatasetValue extern = (CategoryDatasetValue) o;
            if (this.getValue() > extern.getValue()) {
                return 1;
            } else if (this.getValue() < extern.getValue()) {
                return -1;
            }

            String c = (String) this.getColumn();
            String cext = (String) extern.getColumn();

            int i = c.compareTo(cext);
            return i;

        }
        return 0;
    }
}
