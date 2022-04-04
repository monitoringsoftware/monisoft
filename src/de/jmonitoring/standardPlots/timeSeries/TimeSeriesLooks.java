package de.jmonitoring.standardPlots.timeSeries;

import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.awt.Color;
import java.io.Serializable;

/**
 * A class extending the base {@link SeriesLooks} and adds methods needed to define the appearance of a timeSeries.<p>
 * The existing field names should not be edited or deleted, as this will cause problems with existing {@link ChartDescriber}s
 * 
 * @author togro
 */
public class TimeSeriesLooks extends SeriesLooks implements Serializable {

    /**
     * How the series should be plotted. Valus are:<br>
     * {@link MoniSoftConstants}.TS_LINES<br>
     * {@link MoniSoftConstants}.TS_BARS<br>
     * {@link MoniSoftConstants}.TS_AREA<br>
     */
    private int DrawType;
    private int yAxis; // The axis on which the sereis should be plotted
    private Color Lines_lineColor; // The color of the line for TS_LINES
    private Color Lines_symbolColor; // The color of the symbols for TS_LINES
    private int Lines_lineType; // The type of the line for TS_LINES
    private int Lines_symbolType; // The type of the symbols for TS_LINES
    private int Lines_lineSize; // The width of the line for TS_LINES
    private int Lines_symbolSize; //The size of the symbols for TS_LINES
    private boolean Lines_drawLine; // Flag indicating if the line is plotted for TS_LINES
    private boolean Lines_drawSymbols; // Flag indicating if the symbols is plotted for TS_LINES
    private boolean Bars_drawFilling;
    private boolean Bars_drawBorder;
    private Color Bars_fillColor;
    private Color Bars_borderColor;
    private int Bars_alpha;
    private int Bars_borderSize;
    private int Bars_borderType;
    private boolean Area_drawFilling;
    private boolean Area_drawBorder;
    private Color Area_fillColor;
    private Color Area_borderColor;
    private int Area_alpha;
    private int Area_borderSize;
    private int Area_borderType;
    private boolean counterWanted;
    private boolean stacked;

    /**
     * Creates a new instance of TimeSeriesLooks with the given id and for the specified axis
     */
    public TimeSeriesLooks(int ident, int axis) {
        super(ident); // ident is the number of the solt button pusehd in the GUI
        yAxis = axis; // the axis on which this series will be plotted
    }

    public void setCounterWanted(boolean counterWanted) {
        this.counterWanted = counterWanted;
    }

    public boolean getCounterWanted() {
        return counterWanted;
    }

    public void setDrawType(int Type) {
        this.DrawType = Type;
    }

    public int getDrawType() {
        return DrawType;
    }

    public void setyAxis(int axis) {
        this.yAxis = axis;
    }

    public int getyAxis() {
        return yAxis;
    }

    public void setLines_lineColor(Color c) {
        this.Lines_lineColor = c;
    }

    public Color getLines_lineColor() {
        return Lines_lineColor;
    }

    public void setLines_symbolColor(Color c) {
        this.Lines_symbolColor = c;
    }

    public Color getLines_symbolColor() {
        return Lines_symbolColor;
    }

    public void setLines_lineType(int Type) {
        this.Lines_lineType = Type;
    }

    public int getLines_lineType() {
        return Lines_lineType;
    }

    public void setLines_symbolType(int Type) {
        this.Lines_symbolType = Type;
    }

    public int getLines_symbolType() {
        return Lines_symbolType;
    }

    public void setLines_lineSize(int Size) {
        this.Lines_lineSize = Size;
    }

    public int getLines_lineSize() {
        return Lines_lineSize;
    }

    public void setLines_symbolSize(int Size) {
        this.Lines_symbolSize = Size;
    }

    public int getLines_symbolSize() {
        return Lines_symbolSize;
    }

    public void setLines_drawLine(boolean Draw) {
        this.Lines_drawLine = Draw;
    }

    public boolean getLines_drawLine() {
        return Lines_drawLine;
    }

    public void setLines_drawSymbols(boolean Draw) {
        this.Lines_drawSymbols = Draw;
    }

    public boolean getLines_drawSymbols() {
        return Lines_drawSymbols;
    }

    public void setBars_DrawFilling(boolean Draw) {
        this.Bars_drawFilling = Draw;
    }

    public boolean getBars_DrawFilling() {
        return Bars_drawFilling;
    }

    public void setBars_DrawBorder(boolean Draw) {
        this.Bars_drawBorder = Draw;
    }

    public boolean getBars_DrawBorder() {
        return Bars_drawBorder;
    }

    public void setBars_fillColor(Color c) {
        this.Bars_fillColor = c;
    }

    public Color getBars_fillColor() {
        return new Color(Bars_fillColor.getRGB());
    }

    public void setBars_borderColor(Color c) {
        this.Bars_borderColor = c;
    }

    public Color getBars_borderColor() {
        return new Color(Bars_borderColor.getRGB());
    }

    public void setBars_alpha(int alpha) {
        this.Bars_alpha = alpha;
    }

    public int getBars_alpha() {
        return Bars_alpha;
    }

    public void setBars_borderType(int Type) {
        this.Bars_borderType = Type;
    }

    public int getBars_borderType() {
        return Bars_borderType;
    }

    public void setBars_borderSize(int Size) {
        this.Bars_borderSize = Size;
    }

    public int getBars_borderSize() {
        return Bars_borderSize;
    }

    public boolean getStacked() {
        return stacked;
    }

    public void setStacked(boolean stacked) {
        this.stacked = stacked;
    }

    public void setArea_DrawFilling(boolean Draw) {
        this.Area_drawFilling = Draw;
    }

    public boolean getArea_DrawFilling() {
        return Area_drawFilling;
    }

    public void setArea_DrawBorder(boolean Draw) {
        this.Area_drawBorder = Draw;
    }

    public boolean getArea_DrawBorder() {
        return Area_drawBorder;
    }

    public void setArea_fillColor(Color c) {
        this.Area_fillColor = c;
    }

    public Color getArea_fillColor() {
        return new Color(Area_fillColor.getRGB());
    }

    public void setArea_borderColor(Color c) {
        this.Area_borderColor = c;
    }

    public Color getArea_borderColor() {
        return new Color(Area_borderColor.getRGB());
    }

    public void setArea_alpha(int alpha) {
        this.Area_alpha = alpha;
    }

    public int getArea_alpha() {
        return Area_alpha;
    }

    public void setArea_borderType(int Type) {
        this.Area_borderType = Type;
    }

    public int getArea_borderType() {
        return Area_borderType;
    }

    public void setArea_borderSize(int Size) {
        this.Area_borderSize = Size;
    }

    public int getArea_borderSize() {
        return Area_borderSize;
    }
}
