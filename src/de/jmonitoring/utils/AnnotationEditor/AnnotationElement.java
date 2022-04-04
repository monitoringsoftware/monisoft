package de.jmonitoring.utils.AnnotationEditor;

import java.awt.BasicStroke;
import java.awt.Color;

/**
 * A class representing one element of an annotation.<p>
 * It holds all the colors and strokes which define the look of the element. An element can be open or closed. If it is open ist represents a line, otherwise a filled polygon.
 * @author togro
 */
public class AnnotationElement {

    private Color fillColor;
    private Color lineColor;
    private int fillAlpha;
    private int lineAlpha;
    private BasicStroke stroke;
    private Double[][] points;
    private String name;
    private boolean closed = false;

    /**
     * Cretae a new element with the given (internal only) name, colors for filling and outline, and outlinestroke.
     * @param name internal name
     * @param fillColor color for the filling
     * @param lineColor color for the outline
     * @param stroke the stroke for the outline
     */
    public AnnotationElement(String name, Color fillColor, Color lineColor, BasicStroke stroke) {
        this.name = name;
        this.fillColor = fillColor;
        this.lineColor = lineColor;
        this.stroke = stroke;
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color lineColor) {
        this.lineColor = lineColor;
    }

    public int getFillAlpha() {
        return fillAlpha;
    }

    public void setFillAlpha(int fillAlpha) {
        this.fillAlpha = fillAlpha;
    }

    public int getLineAlpha() {
        return lineAlpha;
    }

    public void setLineAlpha(int lineAlpha) {
        this.lineAlpha = lineAlpha;
    }

    public BasicStroke getStroke() {
        return stroke;
    }

    public void setStroke(BasicStroke stroke) {
        this.stroke = stroke;
    }

    public Double[][] getPoints() {
        return points;
    }

    public void setPoints(Double[][] points) {
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
