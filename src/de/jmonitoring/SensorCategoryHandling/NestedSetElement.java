package de.jmonitoring.SensorCategoryHandling;

/**
 *
 * @author togro
 */
public class NestedSetElement {
    private int left;
    private int right;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NestedSetElement(String name,int left, int right) {
        this.left = left;
        this.right = right;
        this.name = name;
    }

    public NestedSetElement() {
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }
}
