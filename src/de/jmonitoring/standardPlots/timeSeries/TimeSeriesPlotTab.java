/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.timeSeries;

import ch.qos.logback.classic.Logger;
import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.Components.DatePanel;
import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DataFilter.ValueFilter;
import de.jmonitoring.DataHandling.DataFilter.ValueFilterComponent;
import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.DateEntriesChecker;
import de.jmonitoring.base.DescriberFactory;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.standardPlots.plotTabs.PlotBaseTab;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.DeepCopyCollection;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.utils.DnDListener.SensorSelectorDropListener;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.intervals.IntervalSelectorEntry;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author dsl
 */
public class TimeSeriesPlotTab extends PlotBaseTab {

    private ArrayList<TimeSeriesLooks> TimeSeriesLooksCollection = new ArrayList<TimeSeriesLooks>(20);
    private Color[] TimeSeriesMainColors = new Color[14];
    private Color[] TimeSeriesSecondaryColors = new Color[14];
    private int locked_y1AggregationIndex = 0;
    private int locked_y2AggregationIndex = 0;
    private int TS_y1ActiveSeriesID = 0;
    private int TS_y2ActiveSeriesID = 7;
    private final DatePanel tsDateChooserPanel;
    private int activeAxis = 0;

    /**
     * Creates new form TimeSeriesPlot
     */
    public TimeSeriesPlotTab(MainApplication gui) {
        super(gui);
        this.tsDateChooserPanel = new DatePanel(gui(), true);
        initComponents();

        DefaultComboBoxModel model = getReferenceSelectorModel();
        y1TimeReferenceSelector.setModel(model);
        y2TimeReferenceSelector.setModel(model);

        setStrokeChooser();
        setSymbolChooser();
    }

    @Override
    protected String getTabName() {
        return "TimeBasedTab"; //NO18N
    }

    @Override
    public void setIntervalSelector(Models models) {
        y1AggSelector.setModel(models.getAggIntervalComboBoxModel());
        y2AggSelector.setModel(models.getAggIntervalComboBoxModel());
        y1AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y1AggSelector, (int) MoniSoftConstants.HOUR_INTERVAL));
        y2AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y2AggSelector, (int) MoniSoftConstants.HOUR_INTERVAL));
        locked_y1AggregationIndex = IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y1AggSelector, (int) MoniSoftConstants.HOUR_INTERVAL);
        locked_y2AggregationIndex = IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y2AggSelector, (int) MoniSoftConstants.HOUR_INTERVAL);
    }

    @Override
    public void lockDates(boolean lock) {
        tsDateChooserPanel.getLockToggle().setSelected(lock);
    }

    @Override
    public void clearSelections() {
        y1SensorSelector.removeAllItems();
        y2SensorSelector.removeAllItems();
        ValueConstraintSensorSelector4.removeAllItems();
        ValueConstraintSensorSelector5.removeAllItems();
        ValueConstraintSensorSelector6.removeAllItems();
        ValueConstraintSensorSelector7.removeAllItems();
    }

    @Override
    public void setSelectionsFrom(Models models) {
        y1SensorSelector.setModel(models.getSensorListComboBoxModel());
        y1SensorSelector.revalidate();
        y2SensorSelector.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector4.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector5.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector6.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector7.setModel(models.getSensorListComboBoxModel());
    }

    @Override
    public void clearData() {
        TimeSeriesLooksCollection.clear();
        locked_y1AggregationIndex = 0;
        locked_y2AggregationIndex = 0;
        TS_y1ActiveSeriesID = 0;
        TS_y2ActiveSeriesID = 7;
    }

    @Override
    public DateInterval getSelectedInterval() {
        return tsDateChooserPanel.getInterval();
    }

    @Override
    public void setSelectedInterval(DateInterval newInterval) {
        tsDateChooserPanel.setInterval(newInterval);
    }

    @Override
    public void resetCollections(int index) {
        TimeSeriesMainColors[index] = MoniSoftConstants.ColorTable.get(index);
        TimeSeriesSecondaryColors[index] = MoniSoftConstants.ColorTable.get(index);
        TimeSeriesLooksCollection.add(index, null);
    }

    @Override
    public Result fillFrom(ChartDescriber chartDescriber) {
        if (!(chartDescriber instanceof TimeSeriesChartDescriber)) {
            return Result.IGNORED;
        }

        TimeSeriesLooksCollection = (ArrayList) new DeepCopyCollection().makeDeepCopy(chartDescriber.getchartCollection());

        boolean stacked = false;
        for (TimeSeriesLooks looks : TimeSeriesLooksCollection) {
            if (looks != null && looks.getStacked()) {
                stacked = true;
            }
        }

        if (stacked) {
            y1LinesToggleButton.setSelected(false);
            y1LinesToggleButton.setEnabled(false);
            AxisTabPanel.setEnabled(false);
        } else {
            AxisTabPanel.setEnabled(true);
            y1LinesToggleButton.setEnabled(true);
        }

        // set slot panel to first entries
        TS_y1ActiveSeriesID = 0;
        TS_y2ActiveSeriesID = 7;
        y1ToggleButton1.setSelected(true);
        y2ToggleButton1.setSelected(true);

        // set date fields to describers date
        tsDateChooserPanel.getStartDateChooser().setDate(chartDescriber.getDateInterval().getStartDate());
        tsDateChooserPanel.getEndDateChooser().setDate(chartDescriber.getDateInterval().getEndDate());

        loadTimeSeriesPanelInfo(0, 0);
        loadTimeSeriesPanelInfo(7, 1);
        loadValueFilters((TimeSeriesChartDescriber) chartDescriber);
        return Result.APPLIED;
    }

    /**
     *
     * @param seriesIndex
     */
    private void setTimeSeriesButtonColors(int seriesIndex) {
        if (seriesIndex < 7) {
            y1_Lines_LinesColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y1_Bars_FillColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y1_Bars_BorderColorButton.setBackground(TimeSeriesSecondaryColors[seriesIndex]);
            y1_Area_FillColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y1_Area_BorderColorButton.setBackground(TimeSeriesSecondaryColors[seriesIndex]);
        } else {
            y2_Lines_LinesColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y2_Bars_FillColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y2_Bars_BorderColorButton.setBackground(TimeSeriesSecondaryColors[seriesIndex]);
            y2_Area_FillColorButton.setBackground(TimeSeriesMainColors[seriesIndex]);
            y2_Area_BorderColorButton.setBackground(TimeSeriesSecondaryColors[seriesIndex]);
        }

    }

    /**
     *
     * @param seriesID
     * @param axis
     */
    private void saveTimeSeriesPanelInfo(int seriesID, int axis) {
        // Index des gewaehlten Sensors ermitteln und in SensorList nachschlagen
        SensorProperties selectedProps = null;        
        
        switch (axis) {
            case 0:
                selectedProps = (SensorProperties) y1SensorSelector.getSelectedItem();
                break;
            case 1:
                selectedProps = (SensorProperties) y2SensorSelector.getSelectedItem();
                break;
        }

        TimeSeriesLooks seriesLook = new TimeSeriesLooks(seriesID, axis);
        
        // wenn Auswahlfeld leer beenden
        if (MoniSoftConstants.NO_SENSOR_SELECTED.equals(selectedProps.getSensorName())) {
            TimeSeriesLooksCollection.set(seriesID, null);
            return;
        }

        // weitere Optionen je nach Darstellungstyp setzen (je nach y-Achse)
        switch (axis) {
            case 0: {
                // Allgemein
                seriesLook.setSensor(selectedProps.getSensorName()); // Name extrahieren und speichern
                seriesLook.setLegendString(null);
                seriesLook.setSensorID(selectedProps.getSensorID());
                seriesLook.setUnit(selectedProps.getSensorUnit().getUnit());
                seriesLook.setFactor(selectedProps.getFactor());
                seriesLook.setAggregation(((IntervalSelectorEntry) y1AggSelector.getSelectedItem()).doubleValue());
                seriesLook.setPowerWanted(y1Counter2.isSelected());
                seriesLook.setCounterWanted(y1Counter3.isSelected());                
                seriesLook.setStacked(y1StackedCheckBox.isSelected());
                seriesLook.setReference(y1ReferenceSelector.getSelectedItem() instanceof ReferenceValue ? (ReferenceValue) y1ReferenceSelector.getSelectedItem() : null);
                seriesLook.setTimeReferenceFromString((String) y1TimeReferenceSelector.getSelectedItem());

                // Einstellugnen sichern
                seriesLook.setLines_drawLine(y1_Lines_DrawLinesCheck.isSelected());
                seriesLook.setLines_drawSymbols(y1_Lines_DrawSymbolsCheck.isSelected());
                seriesLook.setLines_lineColor(y1_Lines_LinesColorButton.getBackground());
                seriesLook.setLines_symbolSize(y1_Lines_SymbolSizeComboBox.getSelectedIndex()); // Wert 1 hat index 0
                seriesLook.setLines_symbolType(y1_Lines_SymbolTypeComboBox.getSelectedIndex());
                seriesLook.setLines_lineSize(y1_Lines_LineSizeComboBox.getSelectedIndex());                
                seriesLook.setLines_lineType(y1_Lines_LineStrokeChooser.getSelectedIndex());
                seriesLook.setBars_DrawFilling(y1_Bars_DrawFillCheck.isSelected());
                seriesLook.setBars_DrawBorder(y1_Bars_DrawBorderCheck.isSelected());
                seriesLook.setBars_fillColor(y1_Bars_FillColorButton.getBackground());
                seriesLook.setBars_borderColor(y1_Bars_BorderColorButton.getBackground());
                seriesLook.setBars_alpha(y1_Bars_alphaSlider.getValue());
                seriesLook.setBars_borderSize(y1_Bars_BorderSizeCombobox.getSelectedIndex());
                seriesLook.setBars_borderType(y1_Bars_BorderTypeCombobox.getSelectedIndex());

                seriesLook.setArea_DrawFilling(y1_Area_DrawFillCheck.isSelected());
                seriesLook.setArea_DrawBorder(y1_Area_DrawBorderCheck.isSelected());
                seriesLook.setArea_fillColor(y1_Area_FillColorButton.getBackground());
                seriesLook.setArea_borderColor(y1_Area_BorderColorButton.getBackground());
                seriesLook.setArea_alpha(y1_Area_alphaSlider.getValue());
                seriesLook.setArea_borderSize(y1_Area_BorderSizeCombobox.getSelectedIndex());
                seriesLook.setArea_borderType(y1_Area_BorderTypeCombobox.getSelectedIndex());

                // je nach Darstellungstyp
                if (y1LinesToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_LINES);
                } else if (y1BarsToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_BARS);
                } else if (y1AreaToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_AREA);
                }
            }
            break;
            case 1: {
                seriesLook.setSensor(selectedProps.getSensorName()); // Name extrahieren und speichern
                seriesLook.setLegendString(null);
                seriesLook.setSensorID(selectedProps.getSensorID());
                seriesLook.setUnit(selectedProps.getSensorUnit().getUnit());
                seriesLook.setFactor(selectedProps.getFactor());
                seriesLook.setAggregation(((IntervalSelectorEntry) y2AggSelector.getSelectedItem()).doubleValue());
                seriesLook.setPowerWanted(y2Counter2.isSelected());
                seriesLook.setCounterWanted(y2Counter3.isSelected());
                seriesLook.setReference(y2ReferenceSelector.getSelectedItem() instanceof ReferenceValue ? (ReferenceValue) y2ReferenceSelector.getSelectedItem() : null);
                seriesLook.setTimeReferenceFromString((String) y2TimeReferenceSelector.getSelectedItem());

                seriesLook.setLines_drawLine(y2_Lines_DrawLinesCheck.isSelected());
                seriesLook.setLines_drawSymbols(y2_Lines_DrawSymbolsCheck.isSelected());
                seriesLook.setLines_lineColor(y2_Lines_LinesColorButton.getBackground());
                seriesLook.setLines_symbolSize(y2_Lines_SymbolSizeComboBox.getSelectedIndex()); // Wert 1 hat index 0
                seriesLook.setLines_symbolType(y2_Lines_SymbolTypeComboBox.getSelectedIndex());
                seriesLook.setLines_lineSize(y2_Lines_LineSizeComboBox.getSelectedIndex());                
                seriesLook.setLines_lineType(y2_Lines_LineStrokeChooser.getSelectedIndex());

                seriesLook.setBars_DrawFilling(y2_Bars_DrawFillCheck.isSelected());
                seriesLook.setBars_DrawBorder(y2_Bars_DrawBorderCheck.isSelected());
                seriesLook.setBars_fillColor(y2_Bars_FillColorButton.getBackground());
                seriesLook.setBars_borderColor(y2_Bars_BorderColorButton.getBackground());
                seriesLook.setBars_alpha(y2_Bars_alphaSlider.getValue());
                seriesLook.setBars_borderSize(y2_Bars_BorderSizeComboBox.getSelectedIndex());
                seriesLook.setBars_borderType(y2_Bars_BorderTypeComboBox.getSelectedIndex());

                seriesLook.setArea_DrawFilling(y2_Area_DrawFillCheck.isSelected());
                seriesLook.setArea_DrawBorder(y2_Area_DrawBorderCheck.isSelected());
                seriesLook.setArea_fillColor(y2_Area_FillColorButton.getBackground());
                seriesLook.setArea_borderColor(y2_Area_BorderColorButton.getBackground());
                seriesLook.setArea_alpha(y2_Area_alphaSlider.getValue());
                seriesLook.setArea_borderSize(y2_Area_BorderSizeComboBox.getSelectedIndex());
                seriesLook.setArea_borderType(y2_Area_BorderTypeComboBox.getSelectedIndex());

                if (y2LinesToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_LINES);
                } else if (y2BarsToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_BARS);
                } else if (y2AreaToggleButton.isSelected()) {
                    seriesLook.setDrawType(MoniSoftConstants.TS_AREA);
                }
            }
            break;
            default:
                return;

        }
        TimeSeriesLooksCollection.set(seriesID, seriesLook);        
    }

    private void y2Toggle(int slot) {
        int index = slot - 1;
        // wenn vorher ein anderer Knopf gedrückt war, dessen Werte speichern und die f�r diesen Knopf laden
        if (TS_y2ActiveSeriesID != index) {
            saveTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
            TS_y2ActiveSeriesID = index;
            loadTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);  // und Werte zur�ckschreiben
        }
    }

    private void y1Toggle(int slot) {
        int index = slot - 1;
        // wenn vorher ein anderer Knopf gedrückt war, dessen Werte speichern und die für diesen Knopf laden
        if (TS_y1ActiveSeriesID != index) {
            saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
            Integer drawType = null;
            if (TimeSeriesLooksCollection.get(TS_y1ActiveSeriesID) != null) {
                drawType = TimeSeriesLooksCollection.get(TS_y1ActiveSeriesID).getDrawType();
            }

            TS_y1ActiveSeriesID = index;
            loadTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);  // und Werte zur�ckschreiben

            // make sure that if stacked is ON all draw types auf the y1-Series are the same
            if (y1StackedCheckBox.isSelected()) {
                y1LinesToggleButton.setSelected(false);
                if (drawType != null) {
                    switch (drawType) {
                        case MoniSoftConstants.TS_BARS:
                            y1BarsToggleButton.setSelected(true);
                            break;
                        case MoniSoftConstants.TS_AREA:
                            y1AreaToggleButton.setSelected(true);
                            break;
                        default:
                            y1BarsToggleButton.setSelected(true);
                    }
                } else {
                    y1BarsToggleButton.setSelected(true);
                }
            }
        }
    }

    private void set_y1_Defaults() {
        y1LinesToggleButton.setSelected(true);
        y1BarsToggleButton.setSelected(false);
        y1AreaToggleButton.setSelected(false);
        y1Counter1.setSelected(false);
        y1SensorSelector.setSelectedIndex(0);   // <keine> wählen
        y1AggSelector.setSelectedIndex(locked_y1AggregationIndex);      //den gelockten Zustand nehmen
        y1_Lines_LinesColorButton.setBackground(TimeSeriesMainColors[TS_y1ActiveSeriesID]);
        y1_Lines_DrawLinesCheck.setSelected(true);
        y1_Lines_DrawSymbolsCheck.setSelected(false);
        y1_Lines_LineSizeComboBox.setSelectedIndex(0);
        y1_Lines_SymbolSizeComboBox.setSelectedIndex(2);
        y1_Lines_LineStrokeChooser.setSelectedIndex(0);
        y1_Lines_SymbolTypeComboBox.setSelectedIndex(0);
        y1_Bars_FillColorButton.setBackground(TimeSeriesMainColors[TS_y1ActiveSeriesID]);
        y1_Bars_BorderColorButton.setBackground(TimeSeriesSecondaryColors[TS_y1ActiveSeriesID]);
        y1_Bars_BorderSizeCombobox.setSelectedIndex(0);
        y1_Bars_BorderTypeCombobox.setSelectedIndex(0);
        y1_Bars_alphaSlider.setValue(255);
        y1_Bars_DrawFillCheck.setSelected(true);
        y1_Bars_DrawBorderCheck.setSelected(false);
        y1_Area_FillColorButton.setBackground(TimeSeriesMainColors[TS_y1ActiveSeriesID]);
        y1_Area_BorderColorButton.setBackground(TimeSeriesSecondaryColors[TS_y1ActiveSeriesID]);
        y1_Area_BorderSizeCombobox.setSelectedIndex(0);
        y1_Area_BorderTypeCombobox.setSelectedIndex(0);
        y1_Area_alphaSlider.setValue(255);
        y1_Area_DrawFillCheck.setSelected(true);
        y1_Area_DrawBorderCheck.setSelected(false);
        // AZ: Kein Zurücksetzen der ausgewählten Markierungen - MONISOFT-20
        /*
        f0Check1.setSelected(false);
        f1Check1.setSelected(false);
        f2Check1.setSelected(false);
        f3Check1.setSelected(false);
        ValueConstraintOperator4.setSelectedIndex(0);
        ValueConstraintOperator5.setSelectedIndex(0);
        ValueConstraintOperator6.setSelectedIndex(0);
        ValueConstraintOperator7.setSelectedIndex(0);
        ValueConstraintSensorSelector4.setSelectedIndex(0);
        ValueConstraintSensorSelector5.setSelectedIndex(0);
        ValueConstraintSensorSelector6.setSelectedIndex(0);
        ValueConstraintSensorSelector7.setSelectedIndex(0);
        ValueConstraintValue4.setText(null);
        ValueConstraintValue5.setText(null);
        ValueConstraintValue6.setText(null);
        ValueConstraintValue7.setText(null);
        */
        CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
        cardLayout.show(y1DefinitionPanel, "y1LinesCard"); //NO18N
    }

    private void set_y2_Defaults() {
        y2LinesToggleButton.setSelected(true);
        y2BarsToggleButton.setSelected(false);
        y2AreaToggleButton.setSelected(false);
        y2Counter1.setSelected(false);
        y2SensorSelector.setSelectedIndex(0);   // <keine> wählen
        y2AggSelector.setSelectedIndex(locked_y2AggregationIndex);      //den gelockten Zustand nehmen
        y2_Lines_LinesColorButton.setBackground(TimeSeriesMainColors[TS_y2ActiveSeriesID]);
        y2_Lines_DrawLinesCheck.setSelected(true);
        y2_Lines_DrawSymbolsCheck.setSelected(false);
        y2_Lines_LineSizeComboBox.setSelectedIndex(0);
        y2_Lines_SymbolSizeComboBox.setSelectedIndex(2);
        y2_Lines_LineStrokeChooser.setSelectedIndex(0);
        y2_Lines_SymbolTypeComboBox.setSelectedIndex(0);
        y2_Bars_FillColorButton.setBackground(TimeSeriesMainColors[TS_y2ActiveSeriesID]);
        y2_Bars_BorderColorButton.setBackground(TimeSeriesSecondaryColors[TS_y2ActiveSeriesID]);
        y2_Bars_BorderSizeComboBox.setSelectedIndex(0);
        y2_Bars_BorderTypeComboBox.setSelectedIndex(0);
        y2_Bars_alphaSlider.setValue(255);
        y2_Bars_DrawFillCheck.setSelected(true);
        y2_Bars_DrawBorderCheck.setSelected(false);
        y2_Area_FillColorButton.setBackground(TimeSeriesMainColors[TS_y2ActiveSeriesID]);
        y2_Area_BorderColorButton.setBackground(TimeSeriesSecondaryColors[TS_y2ActiveSeriesID]);
        y2_Area_BorderSizeComboBox.setSelectedIndex(0);
        y2_Area_BorderTypeComboBox.setSelectedIndex(0);
        y2_Area_alphaSlider.setValue(255);
        y2_Area_DrawFillCheck.setSelected(true);
        y2_Area_DrawBorderCheck.setSelected(false);
        CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
        cardLayout.show(y2DefinitionPanel, "y1LinesCard"); //NO18N
    }

    /**
     *
     * @param seriesID
     * @param axis
     */
    private void loadTimeSeriesPanelInfo(int seriesID, int axis) {
        // Einstellung für aktuelle Auswahl ermitteln
        TimeSeriesLooks seriesLook = null;
        TimeSeriesLooks currentLook = null;
        boolean isKnown = false;        

        if (!TimeSeriesLooksCollection.isEmpty()) {
            Iterator it = TimeSeriesLooksCollection.iterator();
            while (it.hasNext()) {
                currentLook = (TimeSeriesLooks) it.next();

                if( currentLook == null )
                    System.out.println( "currentLook is null!!!!! seriesID: " + seriesID + " axis: " + axis );
                else
                    System.out.println( "isKnown? currentLook.getSeriesID(): " + currentLook.getSeriesID() + " seriesID: " + seriesID );
                
                
                if ((currentLook != null) && (currentLook.getSeriesID() == seriesID) && (currentLook.getyAxis() == axis)) {
                    isKnown = true;
                    System.out.println( "seriesLook Known currentLook.getSeriesID(): " + currentLook.getSeriesID() );
                    seriesLook = currentLook;
                }
            }
        }

        // Elemente des Panels setzen
        switch (axis) {
            case 0:
                setTimeSeriesButtonColors(TS_y1ActiveSeriesID);
                if (isKnown) {
                    y1SensorSelector.setSelectedItem(SensorInformation.getSensorProperties(seriesLook.getSensorID()));
                    y1AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y1AggSelector, seriesLook.getAggregation()));

                    y1Counter1.setSelected(true);
                    y1Counter2.setSelected(seriesLook.getPowerWanted());
                    y1Counter3.setSelected(seriesLook.getCounterWanted());                    
                    y1StackedCheckBox.setSelected(seriesLook.getStacked());
                    y1SensorSelectorTouched(null); // Messpunktwähler "berühren" damit der Referenzwähler aktualisiert wird
                    if (seriesLook.getReference() == null) {
                        try {
                            y1ReferenceSelector.setSelectedIndex(0);
                        } catch (IllegalArgumentException e) {
                            // Abfangen wenn Selector gar nicht belegt ist
                        }
                    } else {
                        int itemcount = y1ReferenceSelector.getItemCount();
                        for (int i = 0; i < itemcount; i++) {
                            if (y1ReferenceSelector.getItemAt(i).toString().equals(seriesLook.getReference().toString())) {
                                y1ReferenceSelector.setSelectedIndex(i);
                                continue;
                            }
                        }
                    }

                    if (seriesLook.getTimeReferenceString() == null) {
                        try {
                            y1TimeReferenceSelector.setSelectedIndex(0);
                        } catch (IllegalArgumentException e) {
                            // Abfangen wenn Selector gar nicht belegt ist
                        }
                    } else {
                        y1TimeReferenceSelector.setSelectedItem(seriesLook.getTimeReferenceString());
                    }

                    sety1Looks(seriesLook);
                } else {
                    set_y1_Defaults();
                }
                break;
            case 1:
                setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
                if (isKnown) {
                    y2SensorSelector.setSelectedItem(SensorInformation.getSensorProperties(seriesLook.getSensorID()));
                    y2AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(y2AggSelector, seriesLook.getAggregation()));

                    y2Counter1.setSelected(true);
                    y2Counter2.setSelected(seriesLook.getPowerWanted());
                    y2Counter3.setSelected(seriesLook.getCounterWanted());
                    y2SensorSelectorTouched(null); // Messpunktwähler "berühren" damit der Referenzwähler aktualisiert wird

                    if (seriesLook.getReference() == null) {
                        try {
                            y2ReferenceSelector.setSelectedIndex(0);
                        } catch (IllegalArgumentException e) {
                            // Abfangen wenn Selector gar nicht belegt ist
                        }
                    } else {
                        int itemcount = y2ReferenceSelector.getItemCount();
                        for (int i = 0; i < itemcount; i++) {
                            if (y2ReferenceSelector.getItemAt(i).toString().equals(seriesLook.getReference().toString())) {
                                y2ReferenceSelector.setSelectedIndex(i);
                                continue;
                            }
                        }
                    }

                    if (seriesLook.getTimeReferenceString() == null) {
                        try {
                            y2TimeReferenceSelector.setSelectedIndex(0);
                        } catch (IllegalArgumentException e) {
                            // Abfangen wenn Selector gar nicht belegt ist
                        }
                    } else {
                        y2TimeReferenceSelector.setSelectedItem(seriesLook.getTimeReferenceString());
                    }
                    sety2Looks(seriesLook);
                } else {
                    // wenn der Knopf noch nicht belegt war Grundeinstellung vornehmen
                    set_y2_Defaults();
                }
                break;
            default:
        }
    }

    private void reset_y1Series() {
        for (int i = 0; i < 7; i++) {
            TimeSeriesLooksCollection.set(i, null);
            //Farbenliste der Serien belegen mit Standardfarben
            TimeSeriesMainColors[i] = MoniSoftConstants.ColorTable.get(i);
            TimeSeriesSecondaryColors[i] = MoniSoftConstants.ColorTable.get(i);
        }

        y1SensorSelector.setSelectedIndex(0);
        y1ToggleButton1.setSelected(true);
        y1ToggleButton2.setSelected(false);
        y1ToggleButton3.setSelected(false);
        y1ToggleButton4.setSelected(false);
        y1ToggleButton5.setSelected(false);
        y1ToggleButton6.setSelected(false);
        y1ToggleButton7.setSelected(false);
    }

    private void reset_y2Series() {
        for (int i = 8; i < 14; i++) {
            TimeSeriesLooksCollection.set(i, null);
            //Farbenliste der Serien belegen mit Standardfarben
            TimeSeriesMainColors[i] = MoniSoftConstants.ColorTable.get(i);
            TimeSeriesSecondaryColors[i] = MoniSoftConstants.ColorTable.get(i);
        }

        y2SensorSelector.setSelectedIndex(0);
        y2ToggleButton1.setSelected(true);
        y2ToggleButton2.setSelected(false);
        y2ToggleButton3.setSelected(false);
        y2ToggleButton4.setSelected(false);
        y2ToggleButton5.setSelected(false);
        y2ToggleButton6.setSelected(false);
        y2ToggleButton7.setSelected(false);
    }

    public void sety1Looks(TimeSeriesLooks seriesLook) {
        switch (seriesLook.getDrawType()) {
            case MoniSoftConstants.TS_LINES: {
                y1LinesToggleButton.setSelected(true);   // Zeichenmodus
                y1BarsToggleButton.setSelected(false);
                y1AreaToggleButton.setSelected(false);
                if (seriesLook.getLines_drawLine()) {   // Linien zeigen
                    y1_Lines_DrawLinesCheck.setSelected(true);
                } else {
                    y1_Lines_DrawLinesCheck.setSelected(false);
                }

                if (seriesLook.getLines_drawSymbols()) {   // Symbole zeigen
                    y1_Lines_DrawSymbolsCheck.setSelected(true);
                } else {
                    y1_Lines_DrawSymbolsCheck.setSelected(false);
                }

                y1_Lines_LinesColorButton.setBackground(seriesLook.getLines_lineColor());
                y1_Lines_SymbolTypeComboBox.setSelectedIndex(seriesLook.getLines_symbolType());
                y1_Lines_SymbolSizeComboBox.setSelectedIndex(seriesLook.getLines_symbolSize()); // Wert 1 hat index 0
                System.out.println( "y1_Lines_LineStrokeChooser.setSelectedIndex: lineType: " + seriesLook.getLines_lineType() );
                y1_Lines_LineStrokeChooser.setSelectedIndex(seriesLook.getLines_lineType());
                y1_Lines_LineSizeComboBox.setSelectedIndex(seriesLook.getLines_lineSize());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
                cardLayout.show(y1DefinitionPanel, "y1LinesCard");//NO18N
            }

            break;
            case MoniSoftConstants.TS_BARS: {
                y1LinesToggleButton.setSelected(false);
                y1BarsToggleButton.setSelected(true);
                y1AreaToggleButton.setSelected(false);
                y1_Bars_alphaSlider.setValue(seriesLook.getBars_alpha());
                if (seriesLook.getBars_DrawFilling()) {   // Linien zeigen
                    y1_Bars_DrawFillCheck.setSelected(true);
                } else {
                    y1_Bars_DrawFillCheck.setSelected(false);
                }

                if (seriesLook.getBars_DrawBorder()) {   // Symbole zeigen
                    y1_Bars_DrawBorderCheck.setSelected(true);
                } else {
                    y1_Bars_DrawBorderCheck.setSelected(false);
                }

                y1_Bars_FillColorButton.setBackground(seriesLook.getBars_fillColor());
                y1_Bars_BorderColorButton.setBackground(seriesLook.getBars_borderColor());
                y1_Bars_BorderSizeCombobox.setSelectedIndex(seriesLook.getBars_borderSize());
                y1_Bars_BorderTypeCombobox.setSelectedIndex(seriesLook.getBars_borderType());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
                cardLayout.show(y1DefinitionPanel, "y1BarsCard");//NO18N
            }

            break;
            case MoniSoftConstants.TS_AREA: {
                y1LinesToggleButton.setSelected(false);
                y1BarsToggleButton.setSelected(false);
                y1AreaToggleButton.setSelected(true);
                y1_Area_alphaSlider.setValue(seriesLook.getArea_alpha());
                if (seriesLook.getArea_DrawFilling()) {   // Linien zeigen
                    y1_Area_DrawFillCheck.setSelected(true);
                } else {
                    y1_Area_DrawFillCheck.setSelected(false);
                }

                if (seriesLook.getArea_DrawBorder()) {   // Symbole zeigen
                    y1_Area_DrawBorderCheck.setSelected(true);
                } else {
                    y1_Area_DrawBorderCheck.setSelected(false);
                }

                y1_Area_FillColorButton.setBackground(seriesLook.getArea_fillColor());
                y1_Area_BorderColorButton.setBackground(seriesLook.getArea_borderColor());
                y1_Area_BorderSizeCombobox.setSelectedIndex(seriesLook.getArea_borderSize());
                y1_Area_BorderTypeCombobox.setSelectedIndex(seriesLook.getArea_borderType());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
                cardLayout.show(y1DefinitionPanel, "y1AreaCard");//NO18N
            }
            break;
            default:
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")//NO18N
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        y2CounterRadioGroup = new javax.swing.ButtonGroup();
        y1CounterRadioGroup = new javax.swing.ButtonGroup();
        y1SlotButtonGroup = new javax.swing.ButtonGroup();
        y2SlotButtonGroup = new javax.swing.ButtonGroup();
        schnittmengeGroup = new javax.swing.ButtonGroup();
        TimeBasedTab = new javax.swing.JPanel();
        AxisTabPanel = new javax.swing.JTabbedPane();
        LeftAxisPanel = new javax.swing.JPanel();
        y1ToggleButton1 = new javax.swing.JToggleButton();
        y1ToggleButton2 = new javax.swing.JToggleButton();
        y1ToggleButton3 = new javax.swing.JToggleButton();
        y1ToggleButton4 = new javax.swing.JToggleButton();
        y1ToggleButton5 = new javax.swing.JToggleButton();
        y1ToggleButton6 = new javax.swing.JToggleButton();
        y1ToggleButton7 = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        y1SensorSelector = new javax.swing.JComboBox();
        y1AggSelector = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        y1LockAggregationToggle = new javax.swing.JToggleButton();
        y1Counter1 = new javax.swing.JRadioButton();
        y1Counter2 = new javax.swing.JRadioButton();
        y1Counter3 = new javax.swing.JRadioButton();
        jPanel27 = new javax.swing.JPanel();
        y1LinesToggleButton = new javax.swing.JToggleButton();
        y1BarsToggleButton = new javax.swing.JToggleButton();
        y1AreaToggleButton = new javax.swing.JToggleButton();
        y1DefinitionPanel = new javax.swing.JPanel();
        y1LinesPanel = new javax.swing.JPanel();
        y1_Lines_DrawLinesCheck = new javax.swing.JCheckBox();
        y1_Lines_DrawSymbolsCheck = new javax.swing.JCheckBox();
        y1_Lines_LineStrokeChooser = new javax.swing.JComboBox();
        y1_Lines_SymbolTypeComboBox = new javax.swing.JComboBox();
        y1_Lines_LineSizeComboBox = new javax.swing.JComboBox();
        y1_Lines_SymbolSizeComboBox = new javax.swing.JComboBox();
        y1_Lines_LinesColorButton = new javax.swing.JButton();
        y1BarsPanel = new javax.swing.JPanel();
        y1_Bars_DrawFillCheck = new javax.swing.JCheckBox();
        y1_Bars_DrawBorderCheck = new javax.swing.JCheckBox();
        y1_Bars_BorderColorButton = new javax.swing.JButton();
        y1_Bars_FillColorButton = new javax.swing.JButton();
        y1_Bars_BorderTypeCombobox = new javax.swing.JComboBox();
        y1_Bars_BorderSizeCombobox = new javax.swing.JComboBox();
        y1_Bars_alphaSlider = new javax.swing.JSlider();
        jLabel7 = new javax.swing.JLabel();
        y1AreaPanel = new javax.swing.JPanel();
        y1_Area_DrawFillCheck = new javax.swing.JCheckBox();
        y1_Area_DrawBorderCheck = new javax.swing.JCheckBox();
        y1_Area_BorderColorButton = new javax.swing.JButton();
        y1_Area_FillColorButton = new javax.swing.JButton();
        y1_Area_BorderTypeCombobox = new javax.swing.JComboBox();
        y1_Area_BorderSizeCombobox = new javax.swing.JComboBox();
        y1_Area_alphaSlider = new javax.swing.JSlider();
        jLabel27 = new javax.swing.JLabel();
        y1ReferenceSelector = new javax.swing.JComboBox();
        jLabel16 = new javax.swing.JLabel();
        y1TimeReferenceSelector = new javax.swing.JComboBox();
        y1TimeSeriesReset = new javax.swing.JButton();
        y1StackedCheckBox = new javax.swing.JCheckBox();
        RightAxisPanel = new javax.swing.JPanel();
        y2ToggleButton1 = new javax.swing.JToggleButton();
        y2ToggleButton2 = new javax.swing.JToggleButton();
        y2ToggleButton3 = new javax.swing.JToggleButton();
        y2ToggleButton4 = new javax.swing.JToggleButton();
        y2ToggleButton5 = new javax.swing.JToggleButton();
        y2ToggleButton6 = new javax.swing.JToggleButton();
        y2ToggleButton7 = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        y2SensorSelector = new javax.swing.JComboBox();
        y2AggSelector = new javax.swing.JComboBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        y2LockAggregationToggle = new javax.swing.JToggleButton();
        y2Counter1 = new javax.swing.JRadioButton();
        y2Counter2 = new javax.swing.JRadioButton();
        y2Counter3 = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        y2LinesToggleButton = new javax.swing.JToggleButton();
        y2BarsToggleButton = new javax.swing.JToggleButton();
        y2AreaToggleButton = new javax.swing.JToggleButton();
        y2DefinitionPanel = new javax.swing.JPanel();
        y2LinesPanel = new javax.swing.JPanel();
        y2_Lines_DrawLinesCheck = new javax.swing.JCheckBox();
        y2_Lines_DrawSymbolsCheck = new javax.swing.JCheckBox();
        y2_Lines_LineStrokeChooser = new javax.swing.JComboBox();
        y2_Lines_SymbolTypeComboBox = new javax.swing.JComboBox();
        y2_Lines_LineSizeComboBox = new javax.swing.JComboBox();
        y2_Lines_SymbolSizeComboBox = new javax.swing.JComboBox();
        y2_Lines_LinesColorButton = new javax.swing.JButton();
        y2BarsPanel = new javax.swing.JPanel();
        y2_Bars_DrawFillCheck = new javax.swing.JCheckBox();
        y2_Bars_DrawBorderCheck = new javax.swing.JCheckBox();
        y2_Bars_BorderColorButton = new javax.swing.JButton();
        y2_Bars_FillColorButton = new javax.swing.JButton();
        y2_Bars_BorderTypeComboBox = new javax.swing.JComboBox();
        y2_Bars_BorderSizeComboBox = new javax.swing.JComboBox();
        y2_Bars_alphaSlider = new javax.swing.JSlider();
        jLabel10 = new javax.swing.JLabel();
        y2AreaPanel = new javax.swing.JPanel();
        y2_Area_DrawFillCheck = new javax.swing.JCheckBox();
        y2_Area_DrawBorderCheck = new javax.swing.JCheckBox();
        y2_Area_BorderColorButton = new javax.swing.JButton();
        y2_Area_FillColorButton = new javax.swing.JButton();
        y2_Area_BorderTypeComboBox = new javax.swing.JComboBox();
        y2_Area_BorderSizeComboBox = new javax.swing.JComboBox();
        y2_Area_alphaSlider = new javax.swing.JSlider();
        jLabel11 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        y2ReferenceSelector = new javax.swing.JComboBox();
        y2TimeReferenceSelector = new javax.swing.JComboBox();
        y2TimeSeriesReset = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        ValueConstraintSensorSelector4 = new javax.swing.JComboBox();
        ValueConstraintOperator4 = new javax.swing.JComboBox();
        ValueConstraintValue4 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f0Check1 = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        f1Check1 = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector5 = new javax.swing.JComboBox();
        ValueConstraintOperator5 = new javax.swing.JComboBox();
        ValueConstraintValue5 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f2Check1 = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector6 = new javax.swing.JComboBox();
        ValueConstraintOperator6 = new javax.swing.JComboBox();
        ValueConstraintValue6 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f3Check1 = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector7 = new javax.swing.JComboBox();
        ValueConstraintOperator7 = new javax.swing.JComboBox();
        ValueConstraintValue7 = new de.jmonitoring.utils.textfields.DoubleTextField();
        markMissingCheck = new javax.swing.JCheckBox();
        vereinigungsmengeButton = new javax.swing.JRadioButton();
        schnittmengeButton = new javax.swing.JRadioButton();
        TimeSeriesdrawChartButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        TimeBasedTab.setDoubleBuffered(false);
        TimeBasedTab.setEnabled(false);
        TimeBasedTab.setMinimumSize(new java.awt.Dimension(484, 340));
        TimeBasedTab.setPreferredSize(new java.awt.Dimension(484, 342));
        TimeBasedTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        TimeBasedTab.add(tsDateChooserPanel);

        AxisTabPanel.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        AxisTabPanel.setMinimumSize(new java.awt.Dimension(467, 330));
        AxisTabPanel.setPreferredSize(new java.awt.Dimension(467, 332));
        AxisTabPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                AxisTabPanelStateChanged(evt);
            }
        });

        LeftAxisPanel.setDoubleBuffered(false);
        LeftAxisPanel.setMinimumSize(new java.awt.Dimension(450, 281));

        y1SlotButtonGroup.add(y1ToggleButton1);
        y1ToggleButton1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton1.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle"); // NOI18N
        y1ToggleButton1.setText(bundle.getString("MoniSoft.y1ToggleButton1.text")); // NOI18N
        y1ToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton1ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton2);
        y1ToggleButton2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton2.setText(bundle.getString("MoniSoft.y1ToggleButton2.text")); // NOI18N
        y1ToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton2ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton3);
        y1ToggleButton3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton3.setText(bundle.getString("MoniSoft.y1ToggleButton3.text")); // NOI18N
        y1ToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton3ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton4);
        y1ToggleButton4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton4.setText(bundle.getString("MoniSoft.y1ToggleButton4.text")); // NOI18N
        y1ToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton4ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton5);
        y1ToggleButton5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton5.setText(bundle.getString("MoniSoft.y1ToggleButton5.text")); // NOI18N
        y1ToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton5ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton6);
        y1ToggleButton6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton6.setText(bundle.getString("MoniSoft.y1ToggleButton6.text")); // NOI18N
        y1ToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton6ActionPerformed(evt);
            }
        });

        y1SlotButtonGroup.add(y1ToggleButton7);
        y1ToggleButton7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1ToggleButton7.setText(bundle.getString("MoniSoft.y1ToggleButton7.text")); // NOI18N
        y1ToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1ToggleButton7ActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setDoubleBuffered(false);

        y1SensorSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1SensorSelector.setMaximumRowCount(20);
        y1SensorSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y1SensorSelector.setPreferredSize(new java.awt.Dimension(126, 17));
        y1SensorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1SensorSelectorTouched(evt);
            }
        });

        y1AggSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1AggSelector.setMaximumRowCount(15);
        y1AggSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Originalmesswerte", "Stundenwerte", "Tageswerte", "Wochenwerte", "Monatswerte" }));
        y1AggSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y1AggSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel6.setText(bundle.getString("MoniSoft.jLabel6.text")); // NOI18N

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel9.setText(bundle.getString("MoniSoft.jLabel9.text")); // NOI18N

        y1LockAggregationToggle.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        y1LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock-unlock.png"))); // NOI18N
        y1LockAggregationToggle.setToolTipText(bundle.getString("MoniSoft.y1LockAggregationToggle.toolTipText")); // NOI18N
        y1LockAggregationToggle.setBorder(null);
        y1LockAggregationToggle.setBorderPainted(false);
        y1LockAggregationToggle.setContentAreaFilled(false);
        y1LockAggregationToggle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock.png"))); // NOI18N
        y1LockAggregationToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1LockAggregationToggleActionPerformed(evt);
            }
        });

        y1CounterRadioGroup.add(y1Counter1);
        y1Counter1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1Counter1.setText(bundle.getString("MoniSoft.y1Counter1.text")); // NOI18N
        y1Counter1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1Counter1.setEnabled(false);
        y1Counter1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1CounterRadioGroup.add(y1Counter2);
        y1Counter2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1Counter2.setText(bundle.getString("MoniSoft.y1Counter2.text")); // NOI18N
        y1Counter2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1Counter2.setEnabled(false);
        y1Counter2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1CounterRadioGroup.add(y1Counter3);
        y1Counter3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1Counter3.setText(bundle.getString("MoniSoft.y1Counter3.text")); // NOI18N
        y1Counter3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1Counter3.setEnabled(false);
        y1Counter3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1LinesToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1LinesToggleButton.setSelected(true);
        y1LinesToggleButton.setText(bundle.getString("MoniSoft.y1LinesToggleButton.text")); // NOI18N
        y1LinesToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y1LinesToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y1LinesToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y1LinesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1LinesToggleButtonActionPerformed(evt);
            }
        });

        y1BarsToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1BarsToggleButton.setText(bundle.getString("MoniSoft.y1BarsToggleButton.text")); // NOI18N
        y1BarsToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y1BarsToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y1BarsToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y1BarsToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1BarsToggleButtonActionPerformed(evt);
            }
        });

        y1AreaToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1AreaToggleButton.setText(bundle.getString("MoniSoft.y1AreaToggleButton.text")); // NOI18N
        y1AreaToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y1AreaToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y1AreaToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y1AreaToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1AreaToggleButtonActionPerformed(evt);
            }
        });

        y1DefinitionPanel.setLayout(new java.awt.CardLayout());

        y1LinesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y1LinesPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        y1LinesPanel.setDoubleBuffered(false);

        y1_Lines_DrawLinesCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_DrawLinesCheck.setSelected(true);
        y1_Lines_DrawLinesCheck.setText(bundle.getString("MoniSoft.y1_Lines_DrawLinesCheck.text")); // NOI18N
        y1_Lines_DrawLinesCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Lines_DrawLinesCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Lines_DrawSymbolsCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_DrawSymbolsCheck.setText(bundle.getString("MoniSoft.y1_Lines_DrawSymbolsCheck.text")); // NOI18N
        y1_Lines_DrawSymbolsCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Lines_DrawSymbolsCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Lines_LineStrokeChooser.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_LineStrokeChooser.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Lines_LineStrokeChooser.setPreferredSize(new java.awt.Dimension(63, 13));
        y1_Lines_LineStrokeChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Lines_LineStrokeChooserActionPerformed(evt);
            }
        });

        y1_Lines_SymbolTypeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_SymbolTypeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Lines_SymbolTypeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Lines_LineSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_LineSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y1_Lines_LineSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Lines_LineSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Lines_SymbolSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Lines_SymbolSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        y1_Lines_SymbolSizeComboBox.setSelectedIndex(2);
        y1_Lines_SymbolSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Lines_SymbolSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Lines_LinesColorButton.setBackground(new java.awt.Color(219, 15, 15));
        y1_Lines_LinesColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y1_Lines_LinesColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y1_Lines_LinesColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Lines_LinesColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout y1LinesPanelLayout = new javax.swing.GroupLayout(y1LinesPanel);
        y1LinesPanel.setLayout(y1LinesPanelLayout);
        y1LinesPanelLayout.setHorizontalGroup(
            y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y1LinesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(y1_Lines_LinesColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1_Lines_DrawLinesCheck)
                    .addComponent(y1_Lines_DrawSymbolsCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(y1_Lines_SymbolTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y1_Lines_LineStrokeChooser, 0, 107, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1_Lines_LineSizeComboBox, 0, 42, Short.MAX_VALUE)
                    .addComponent(y1_Lines_SymbolSizeComboBox, 0, 43, Short.MAX_VALUE))
                .addContainerGap())
        );
        y1LinesPanelLayout.setVerticalGroup(
            y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, y1LinesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(y1_Lines_LinesColorButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, y1LinesPanelLayout.createSequentialGroup()
                        .addComponent(y1_Lines_DrawLinesCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y1_Lines_DrawSymbolsCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE))
                    .addGroup(y1LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(y1LinesPanelLayout.createSequentialGroup()
                            .addComponent(y1_Lines_LineSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1_Lines_SymbolSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(y1LinesPanelLayout.createSequentialGroup()
                            .addComponent(y1_Lines_LineStrokeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1_Lines_SymbolTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(13, 13, 13))
        );

        y1DefinitionPanel.add(y1LinesPanel, "y1LinesCard");

        y1BarsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y1BarsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y1_Bars_DrawFillCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Bars_DrawFillCheck.setSelected(true);
        y1_Bars_DrawFillCheck.setText(bundle.getString("MoniSoft.y1_Bars_DrawFillCheck.text")); // NOI18N
        y1_Bars_DrawFillCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Bars_DrawFillCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Bars_DrawBorderCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Bars_DrawBorderCheck.setText(bundle.getString("MoniSoft.y1_Bars_DrawBorderCheck.text")); // NOI18N
        y1_Bars_DrawBorderCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Bars_DrawBorderCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Bars_BorderColorButton.setBackground(new java.awt.Color(255, 0, 51));
        y1_Bars_BorderColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y1_Bars_BorderColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y1_Bars_BorderColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Bars_BorderColorButtonActionPerformed(evt);
            }
        });

        y1_Bars_FillColorButton.setBackground(new java.awt.Color(255, 0, 51));
        y1_Bars_FillColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y1_Bars_FillColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y1_Bars_FillColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Bars_FillColorButtonActionPerformed(evt);
            }
        });

        y1_Bars_BorderTypeCombobox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Bars_BorderTypeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Durchgezogen", "Gepunktet", "Gestrichelt" }));
        y1_Bars_BorderTypeCombobox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Bars_BorderTypeCombobox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Bars_BorderSizeCombobox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Bars_BorderSizeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y1_Bars_BorderSizeCombobox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Bars_BorderSizeCombobox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Bars_alphaSlider.setMaximum(255);
        y1_Bars_alphaSlider.setValue(255);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel7.setText(bundle.getString("MoniSoft.jLabel7.text")); // NOI18N

        javax.swing.GroupLayout y1BarsPanelLayout = new javax.swing.GroupLayout(y1BarsPanel);
        y1BarsPanel.setLayout(y1BarsPanelLayout);
        y1BarsPanelLayout.setHorizontalGroup(
            y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y1BarsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1_Bars_DrawFillCheck)
                    .addComponent(jLabel7)
                    .addComponent(y1_Bars_DrawBorderCheck))
                .addGap(8, 8, 8)
                .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(y1_Bars_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, y1BarsPanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(y1BarsPanelLayout.createSequentialGroup()
                                .addComponent(y1_Bars_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y1_Bars_BorderTypeCombobox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y1_Bars_BorderSizeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(y1_Bars_FillColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        y1BarsPanelLayout.setVerticalGroup(
            y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y1BarsPanelLayout.createSequentialGroup()
                .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(y1_Bars_DrawFillCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1_Bars_FillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(y1_Bars_BorderSizeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y1_Bars_BorderTypeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(y1_Bars_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y1_Bars_DrawBorderCheck)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y1BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(y1_Bars_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        y1DefinitionPanel.add(y1BarsPanel, "y1BarsCard");

        y1AreaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y1AreaPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y1_Area_DrawFillCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Area_DrawFillCheck.setSelected(true);
        y1_Area_DrawFillCheck.setText(bundle.getString("MoniSoft.y1_Area_DrawFillCheck.text")); // NOI18N
        y1_Area_DrawFillCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Area_DrawFillCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Area_DrawBorderCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Area_DrawBorderCheck.setText(bundle.getString("MoniSoft.y1_Area_DrawBorderCheck.text")); // NOI18N
        y1_Area_DrawBorderCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y1_Area_DrawBorderCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y1_Area_BorderColorButton.setBackground(new java.awt.Color(255, 0, 51));
        y1_Area_BorderColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y1_Area_BorderColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y1_Area_BorderColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Area_BorderColorButtonActionPerformed(evt);
            }
        });

        y1_Area_FillColorButton.setBackground(new java.awt.Color(255, 0, 51));
        y1_Area_FillColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y1_Area_FillColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y1_Area_FillColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_Area_FillColorButtonActionPerformed(evt);
            }
        });

        y1_Area_BorderTypeCombobox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Area_BorderTypeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Durchgezogen", "Gepunktet", "Gestrichelt" }));
        y1_Area_BorderTypeCombobox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Area_BorderTypeCombobox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Area_BorderSizeCombobox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1_Area_BorderSizeCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y1_Area_BorderSizeCombobox.setMinimumSize(new java.awt.Dimension(63, 13));
        y1_Area_BorderSizeCombobox.setPreferredSize(new java.awt.Dimension(63, 13));

        y1_Area_alphaSlider.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        y1_Area_alphaSlider.setMaximum(255);
        y1_Area_alphaSlider.setValue(255);

        jLabel27.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel27.setText(bundle.getString("MoniSoft.jLabel27.text")); // NOI18N

        javax.swing.GroupLayout y1AreaPanelLayout = new javax.swing.GroupLayout(y1AreaPanel);
        y1AreaPanel.setLayout(y1AreaPanelLayout);
        y1AreaPanelLayout.setHorizontalGroup(
            y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y1AreaPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1_Area_DrawFillCheck)
                    .addComponent(jLabel27)
                    .addComponent(y1_Area_DrawBorderCheck))
                .addGap(18, 18, 18)
                .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y1AreaPanelLayout.createSequentialGroup()
                        .addComponent(y1_Area_alphaSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(y1AreaPanelLayout.createSequentialGroup()
                        .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(y1AreaPanelLayout.createSequentialGroup()
                                .addComponent(y1_Area_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y1_Area_BorderTypeCombobox, 0, 104, Short.MAX_VALUE))
                            .addComponent(y1_Area_FillColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y1_Area_BorderSizeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12))))
        );
        y1AreaPanelLayout.setVerticalGroup(
            y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y1AreaPanelLayout.createSequentialGroup()
                .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(y1_Area_DrawFillCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1_Area_FillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y1_Area_DrawBorderCheck)
                    .addComponent(y1_Area_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1_Area_BorderSizeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1_Area_BorderTypeCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(y1AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel27)
                    .addComponent(y1_Area_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        y1DefinitionPanel.add(y1AreaPanel, "y1AreaCard");

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1AreaToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(y1BarsToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y1LinesToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y1DefinitionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel27Layout.createSequentialGroup()
                        .addComponent(y1LinesToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(y1BarsToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(y1AreaToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel27Layout.createSequentialGroup()
                        .addComponent(y1DefinitionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        y1ReferenceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1ReferenceSelector.setMaximumRowCount(15);
        y1ReferenceSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y1ReferenceSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel16.setText(bundle.getString("MoniSoft.jLabel16.text")); // NOI18N

        y1TimeReferenceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1TimeReferenceSelector.setMaximumRowCount(15);
        y1TimeReferenceSelector.setToolTipText(bundle.getString("MoniSoft.y1TimeReferenceSelector.toolTipText")); // NOI18N
        y1TimeReferenceSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y1TimeReferenceSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        DropTarget dt_y1SensorSelector = new DropTarget(y1SensorSelector,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(y1AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y1LockAggregationToggle)
                        .addGap(191, 191, 191))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(y1Counter3)
                        .addGap(107, 107, 107))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(y1SensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(y1Counter1)
                        .addGap(39, 39, 39)
                        .addComponent(y1Counter2, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGap(98, 98, 98)
                            .addComponent(jLabel16)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1TimeReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(9, 9, 9))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addGap(6, 6, 6)
                            .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(51, 51, 51))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(y1SensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel6)
                        .addComponent(y1AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(y1LockAggregationToggle))
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y1Counter1)
                    .addComponent(y1Counter2, javax.swing.GroupLayout.DEFAULT_SIZE, 20, Short.MAX_VALUE)
                    .addComponent(y1Counter3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel16)
                    .addComponent(y1ReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y1TimeReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
        );

        y1TimeSeriesReset.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1TimeSeriesReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/eraser.png"))); // NOI18N
        y1TimeSeriesReset.setToolTipText(bundle.getString("MoniSoft.y1TimeSeriesReset.toolTipText")); // NOI18N
        y1TimeSeriesReset.setMargin(new java.awt.Insets(2, 2, 2, 2));
        y1TimeSeriesReset.setMaximumSize(new java.awt.Dimension(21, 21));
        y1TimeSeriesReset.setMinimumSize(new java.awt.Dimension(21, 21));
        y1TimeSeriesReset.setPreferredSize(new java.awt.Dimension(21, 21));
        y1TimeSeriesReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1TimeSeriesResetActionPerformed(evt);
            }
        });

        y1StackedCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y1StackedCheckBox.setText(bundle.getString("MoniSoft.y1StackedCheckBox.text")); // NOI18N
        y1StackedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1StackedCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LeftAxisPanelLayout = new javax.swing.GroupLayout(LeftAxisPanel);
        LeftAxisPanel.setLayout(LeftAxisPanelLayout);
        LeftAxisPanelLayout.setHorizontalGroup(
            LeftAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LeftAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LeftAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1StackedCheckBox)
                    .addGroup(LeftAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, 0, 399, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, LeftAxisPanelLayout.createSequentialGroup()
                            .addComponent(y1ToggleButton1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton4)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1ToggleButton7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(y1TimeSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(51, 51, 51))
        );
        LeftAxisPanelLayout.setVerticalGroup(
            LeftAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LeftAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LeftAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(y1ToggleButton1)
                    .addComponent(y1ToggleButton2)
                    .addComponent(y1ToggleButton3)
                    .addComponent(y1ToggleButton4)
                    .addComponent(y1ToggleButton5)
                    .addComponent(y1ToggleButton6)
                    .addComponent(y1ToggleButton7)
                    .addComponent(y1TimeSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y1StackedCheckBox)
                .addContainerGap(13, Short.MAX_VALUE))
        );

        AxisTabPanel.addTab(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft.LeftAxisPanel.TabConstraints.tabTitle"), LeftAxisPanel);

        y2SlotButtonGroup.add(y2ToggleButton1);
        y2ToggleButton1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton1.setSelected(true);
        y2ToggleButton1.setText(bundle.getString("MoniSoft.y2ToggleButton1.text")); // NOI18N
        y2ToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton1ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton2);
        y2ToggleButton2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton2.setText(bundle.getString("MoniSoft.y2ToggleButton2.text")); // NOI18N
        y2ToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton2ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton3);
        y2ToggleButton3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton3.setText(bundle.getString("MoniSoft.y2ToggleButton3.text")); // NOI18N
        y2ToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton3ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton4);
        y2ToggleButton4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton4.setText(bundle.getString("MoniSoft.y2ToggleButton4.text")); // NOI18N
        y2ToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton4ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton5);
        y2ToggleButton5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton5.setText(bundle.getString("MoniSoft.y2ToggleButton5.text")); // NOI18N
        y2ToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton5ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton6);
        y2ToggleButton6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton6.setText(bundle.getString("MoniSoft.y2ToggleButton6.text")); // NOI18N
        y2ToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton6ActionPerformed(evt);
            }
        });

        y2SlotButtonGroup.add(y2ToggleButton7);
        y2ToggleButton7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2ToggleButton7.setText(bundle.getString("MoniSoft.y2ToggleButton7.text")); // NOI18N
        y2ToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2ToggleButton7ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel2.setDoubleBuffered(false);

        y2SensorSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2SensorSelector.setMaximumRowCount(20);
        y2SensorSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y2SensorSelector.setPreferredSize(new java.awt.Dimension(126, 17));
        y2SensorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2SensorSelectorTouched(evt);
            }
        });

        y2AggSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2AggSelector.setMaximumRowCount(15);
        y2AggSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Originalmesswerte", "Stundenwerte", "Tageswerte", "Wochenwerte", "Monatswerte", "Jahreööööswerte" }));
        y2AggSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y2AggSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel12.setText(bundle.getString("MoniSoft.jLabel12.text")); // NOI18N

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel13.setText(bundle.getString("MoniSoft.jLabel13.text")); // NOI18N

        y2LockAggregationToggle.setFont(new java.awt.Font("Dialog", 1, 8)); // NOI18N
        y2LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock-unlock.png"))); // NOI18N
        y2LockAggregationToggle.setToolTipText(bundle.getString("MoniSoft.y2LockAggregationToggle.toolTipText")); // NOI18N
        y2LockAggregationToggle.setBorder(null);
        y2LockAggregationToggle.setBorderPainted(false);
        y2LockAggregationToggle.setContentAreaFilled(false);
        y2LockAggregationToggle.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock.png"))); // NOI18N
        y2LockAggregationToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2LockAggregationToggleActionPerformed(evt);
            }
        });

        y2CounterRadioGroup.add(y2Counter1);
        y2Counter1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2Counter1.setSelected(true);
        y2Counter1.setText(bundle.getString("MoniSoft.y2Counter1.text")); // NOI18N
        y2Counter1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2Counter1.setEnabled(false);
        y2Counter1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2CounterRadioGroup.add(y2Counter2);
        y2Counter2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2Counter2.setText(bundle.getString("MoniSoft.y2Counter2.text")); // NOI18N
        y2Counter2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2Counter2.setEnabled(false);
        y2Counter2.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2CounterRadioGroup.add(y2Counter3);
        y2Counter3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2Counter3.setText(bundle.getString("MoniSoft.y2Counter3.text")); // NOI18N
        y2Counter3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2Counter3.setEnabled(false);
        y2Counter3.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2LinesToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2LinesToggleButton.setSelected(true);
        y2LinesToggleButton.setText(bundle.getString("MoniSoft.y2LinesToggleButton.text")); // NOI18N
        y2LinesToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y2LinesToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y2LinesToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y2LinesToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2LinesToggleButtonActionPerformed(evt);
            }
        });

        y2BarsToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2BarsToggleButton.setText(bundle.getString("MoniSoft.y2BarsToggleButton.text")); // NOI18N
        y2BarsToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y2BarsToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y2BarsToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y2BarsToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2BarsToggleButtonActionPerformed(evt);
            }
        });

        y2AreaToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2AreaToggleButton.setText(bundle.getString("MoniSoft.y2AreaToggleButton.text")); // NOI18N
        y2AreaToggleButton.setMaximumSize(new java.awt.Dimension(58, 17));
        y2AreaToggleButton.setMinimumSize(new java.awt.Dimension(58, 17));
        y2AreaToggleButton.setPreferredSize(new java.awt.Dimension(58, 17));
        y2AreaToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2AreaToggleButtonActionPerformed(evt);
            }
        });

        y2DefinitionPanel.setLayout(new java.awt.CardLayout());

        y2LinesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y2LinesPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y2_Lines_DrawLinesCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_DrawLinesCheck.setSelected(true);
        y2_Lines_DrawLinesCheck.setText(bundle.getString("MoniSoft.y2_Lines_DrawLinesCheck.text")); // NOI18N
        y2_Lines_DrawLinesCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Lines_DrawLinesCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Lines_DrawSymbolsCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_DrawSymbolsCheck.setText(bundle.getString("MoniSoft.y2_Lines_DrawSymbolsCheck.text")); // NOI18N
        y2_Lines_DrawSymbolsCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Lines_DrawSymbolsCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Lines_LineStrokeChooser.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_LineStrokeChooser.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Lines_LineStrokeChooser.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Lines_SymbolTypeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_SymbolTypeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Lines_SymbolTypeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Lines_LineSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_LineSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y2_Lines_LineSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Lines_LineSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Lines_SymbolSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Lines_SymbolSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        y2_Lines_SymbolSizeComboBox.setSelectedIndex(2);
        y2_Lines_SymbolSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Lines_SymbolSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Lines_LinesColorButton.setBackground(new java.awt.Color(255, 203, 70));
        y2_Lines_LinesColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y2_Lines_LinesColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y2_Lines_LinesColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_Lines_LinesColorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout y2LinesPanelLayout = new javax.swing.GroupLayout(y2LinesPanel);
        y2LinesPanel.setLayout(y2LinesPanelLayout);
        y2LinesPanelLayout.setHorizontalGroup(
            y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y2LinesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(y2_Lines_LinesColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y2_Lines_DrawSymbolsCheck, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                    .addComponent(y2_Lines_DrawLinesCheck))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(y2_Lines_LineStrokeChooser, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y2_Lines_SymbolTypeComboBox, 0, 108, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(y2_Lines_SymbolSizeComboBox, 0, 0, Short.MAX_VALUE)
                    .addComponent(y2_Lines_LineSizeComboBox, 0, 44, Short.MAX_VALUE))
                .addContainerGap())
        );
        y2LinesPanelLayout.setVerticalGroup(
            y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y2LinesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y2LinesPanelLayout.createSequentialGroup()
                        .addComponent(y2_Lines_LineSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2_Lines_SymbolSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(y2LinesPanelLayout.createSequentialGroup()
                        .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(y2_Lines_LineStrokeChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(y2_Lines_DrawLinesCheck))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(y2LinesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(y2_Lines_DrawSymbolsCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(y2_Lines_SymbolTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(y2_Lines_LinesColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        y2DefinitionPanel.add(y2LinesPanel, "y2LinesCard");

        y2BarsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y2BarsPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y2_Bars_DrawFillCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Bars_DrawFillCheck.setSelected(true);
        y2_Bars_DrawFillCheck.setText(bundle.getString("MoniSoft.y2_Bars_DrawFillCheck.text")); // NOI18N
        y2_Bars_DrawFillCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Bars_DrawFillCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Bars_DrawBorderCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Bars_DrawBorderCheck.setText(bundle.getString("MoniSoft.y2_Bars_DrawBorderCheck.text")); // NOI18N
        y2_Bars_DrawBorderCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Bars_DrawBorderCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Bars_BorderColorButton.setBackground(new java.awt.Color(255, 203, 70));
        y2_Bars_BorderColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y2_Bars_BorderColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y2_Bars_BorderColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_Bars_BorderColorButtonActionPerformed(evt);
            }
        });

        y2_Bars_FillColorButton.setBackground(new java.awt.Color(255, 203, 70));
        y2_Bars_FillColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y2_Bars_FillColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y2_Bars_FillColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_Bars_FillColorButtonActionPerformed(evt);
            }
        });

        y2_Bars_BorderTypeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Bars_BorderTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Durchgezogen", "Gepunktet", "Gestrichelt" }));
        y2_Bars_BorderTypeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Bars_BorderTypeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Bars_BorderSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Bars_BorderSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y2_Bars_BorderSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Bars_BorderSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Bars_alphaSlider.setMaximum(255);
        y2_Bars_alphaSlider.setValue(255);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel10.setText(bundle.getString("MoniSoft.jLabel10.text")); // NOI18N

        javax.swing.GroupLayout y2BarsPanelLayout = new javax.swing.GroupLayout(y2BarsPanel);
        y2BarsPanel.setLayout(y2BarsPanelLayout);
        y2BarsPanelLayout.setHorizontalGroup(
            y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y2BarsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y2BarsPanelLayout.createSequentialGroup()
                        .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(y2_Bars_DrawFillCheck)
                            .addComponent(jLabel10)
                            .addComponent(y2_Bars_DrawBorderCheck))
                        .addGap(16, 16, 16)
                        .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(y2BarsPanelLayout.createSequentialGroup()
                                .addComponent(y2_Bars_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2_Bars_BorderTypeComboBox, 0, 115, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2_Bars_BorderSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(y2_Bars_FillColorButton, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)))
                    .addGroup(y2BarsPanelLayout.createSequentialGroup()
                        .addGap(60, 60, 60)
                        .addComponent(y2_Bars_alphaSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)))
                .addContainerGap())
        );
        y2BarsPanelLayout.setVerticalGroup(
            y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y2BarsPanelLayout.createSequentialGroup()
                .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(y2_Bars_FillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2_Bars_DrawFillCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(y2_Bars_BorderSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y2_Bars_BorderTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(y2_Bars_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(y2_Bars_DrawBorderCheck)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2BarsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(y2_Bars_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        y2DefinitionPanel.add(y2BarsPanel, "y2BarsCard");

        y2AreaPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("MoniSoft.y2AreaPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y2_Area_DrawFillCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Area_DrawFillCheck.setSelected(true);
        y2_Area_DrawFillCheck.setText(bundle.getString("MoniSoft.y2_Area_DrawFillCheck.text")); // NOI18N
        y2_Area_DrawFillCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Area_DrawFillCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Area_DrawBorderCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Area_DrawBorderCheck.setText(bundle.getString("MoniSoft.y2_Area_DrawBorderCheck.text")); // NOI18N
        y2_Area_DrawBorderCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        y2_Area_DrawBorderCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        y2_Area_BorderColorButton.setBackground(new java.awt.Color(255, 203, 70));
        y2_Area_BorderColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y2_Area_BorderColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y2_Area_BorderColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_Area_BorderColorButtonActionPerformed(evt);
            }
        });

        y2_Area_FillColorButton.setBackground(new java.awt.Color(255, 203, 70));
        y2_Area_FillColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        y2_Area_FillColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        y2_Area_FillColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_Area_FillColorButtonActionPerformed(evt);
            }
        });

        y2_Area_BorderTypeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Area_BorderTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Durchgezogen", "Gepunktet", "Gestrichelt" }));
        y2_Area_BorderTypeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Area_BorderTypeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Area_BorderSizeComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2_Area_BorderSizeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        y2_Area_BorderSizeComboBox.setMinimumSize(new java.awt.Dimension(63, 13));
        y2_Area_BorderSizeComboBox.setPreferredSize(new java.awt.Dimension(63, 13));

        y2_Area_alphaSlider.setFont(new java.awt.Font("Dialog", 1, 9)); // NOI18N
        y2_Area_alphaSlider.setMaximum(255);
        y2_Area_alphaSlider.setValue(255);

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel11.setText(bundle.getString("MoniSoft.jLabel11.text")); // NOI18N

        javax.swing.GroupLayout y2AreaPanelLayout = new javax.swing.GroupLayout(y2AreaPanel);
        y2AreaPanel.setLayout(y2AreaPanelLayout);
        y2AreaPanelLayout.setHorizontalGroup(
            y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, y2AreaPanelLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y2_Area_DrawFillCheck)
                    .addComponent(jLabel11)
                    .addComponent(y2_Area_DrawBorderCheck))
                .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(y2AreaPanelLayout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(y2_Area_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, y2AreaPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(y2_Area_FillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, y2AreaPanelLayout.createSequentialGroup()
                                .addComponent(y2_Area_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2_Area_BorderTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2_Area_BorderSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        y2AreaPanelLayout.setVerticalGroup(
            y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(y2AreaPanelLayout.createSequentialGroup()
                .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(y2_Area_FillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2_Area_DrawFillCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y2_Area_DrawBorderCheck)
                    .addComponent(y2_Area_BorderColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2_Area_BorderTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2_Area_BorderSizeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(y2AreaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(y2_Area_alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        y2DefinitionPanel.add(y2AreaPanel, "y2AreaCard");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(y2AreaToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y2BarsToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(y2LinesToggleButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(y2DefinitionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(y2LinesToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(y2BarsToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(y2AreaToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(y2DefinitionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel26.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel26.setText(bundle.getString("MoniSoft.jLabel26.text")); // NOI18N

        y2ReferenceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2ReferenceSelector.setMaximumRowCount(15);
        y2ReferenceSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y2ReferenceSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        y2TimeReferenceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2TimeReferenceSelector.setMaximumRowCount(15);
        y2TimeReferenceSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        y2TimeReferenceSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        DropTarget dt_y2Sensorselector = new DropTarget(y2SensorSelector,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(y2Counter1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2Counter2, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(y2Counter3, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel12)
                                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(y2AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(y2LockAggregationToggle))
                                    .addComponent(y2SensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 306, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(97, 97, 97)
                        .addComponent(jLabel26)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ReferenceSelector, 0, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2TimeReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(y2SensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(y2AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2LockAggregationToggle)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y2Counter1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2Counter2, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2Counter3, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel26)
                    .addComponent(y2ReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(y2TimeReferenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        y2TimeSeriesReset.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        y2TimeSeriesReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/eraser.png"))); // NOI18N
        y2TimeSeriesReset.setToolTipText(bundle.getString("MoniSoft.y2TimeSeriesReset.toolTipText")); // NOI18N
        y2TimeSeriesReset.setMargin(new java.awt.Insets(2, 2, 2, 2));
        y2TimeSeriesReset.setMaximumSize(new java.awt.Dimension(21, 21));
        y2TimeSeriesReset.setMinimumSize(new java.awt.Dimension(21, 21));
        y2TimeSeriesReset.setPreferredSize(new java.awt.Dimension(21, 21));
        y2TimeSeriesReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2TimeSeriesResetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout RightAxisPanelLayout = new javax.swing.GroupLayout(RightAxisPanel);
        RightAxisPanel.setLayout(RightAxisPanelLayout);
        RightAxisPanelLayout.setHorizontalGroup(
            RightAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RightAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RightAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, RightAxisPanelLayout.createSequentialGroup()
                        .addComponent(y2ToggleButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2ToggleButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2TimeSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(120, 120, 120))
        );
        RightAxisPanelLayout.setVerticalGroup(
            RightAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RightAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RightAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(y2ToggleButton1)
                    .addComponent(y2ToggleButton2)
                    .addComponent(y2ToggleButton3)
                    .addComponent(y2ToggleButton4)
                    .addComponent(y2ToggleButton5)
                    .addComponent(y2ToggleButton6)
                    .addComponent(y2ToggleButton7)
                    .addComponent(y2TimeSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        AxisTabPanel.addTab(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MoniSoft.RightAxisPanel.TabConstraints.tabTitle"), RightAxisPanel);

        TimeBasedTab.add(AxisTabPanel);

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("MoniSoft.jPanel9.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel9.setPreferredSize(new java.awt.Dimension(467, 160));

        ValueConstraintSensorSelector4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector4.setMaximumRowCount(15);
        ValueConstraintSensorSelector4.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector4.setPreferredSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ValueConstraintSensorSelector4ActionPerformed(evt);
            }
        });

        ValueConstraintOperator4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=", "" }));
        ValueConstraintOperator4.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator4.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator4.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f0Check1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f0Check1.setMinimumSize(new java.awt.Dimension(21, 17));
        f0Check1.setPreferredSize(new java.awt.Dimension(21, 17));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel2.setText(bundle.getString("MoniSoft.jLabel2.text")); // NOI18N

        f1Check1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f1Check1.setMinimumSize(new java.awt.Dimension(21, 17));
        f1Check1.setPreferredSize(new java.awt.Dimension(21, 17));

        ValueConstraintSensorSelector5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector5.setMaximumRowCount(15);
        ValueConstraintSensorSelector5.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector5.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator5.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator5.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator5.setPreferredSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ValueConstraintOperator5ActionPerformed(evt);
            }
        });

        ValueConstraintValue5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f2Check1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f2Check1.setMinimumSize(new java.awt.Dimension(21, 17));
        f2Check1.setPreferredSize(new java.awt.Dimension(21, 17));

        ValueConstraintSensorSelector6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector6.setMaximumRowCount(15);
        ValueConstraintSensorSelector6.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector6.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator6.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator6.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator6.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f3Check1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f3Check1.setMinimumSize(new java.awt.Dimension(21, 17));
        f3Check1.setPreferredSize(new java.awt.Dimension(21, 17));

        ValueConstraintSensorSelector7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector7.setMaximumRowCount(15);
        ValueConstraintSensorSelector7.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector7.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator7.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator7.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator7.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        markMissingCheck.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        markMissingCheck.setText(bundle.getString("MoniSoft.markMissingCheck.text")); // NOI18N
        markMissingCheck.setToolTipText(bundle.getString("MoniSoft.markMissingCheck.toolTipText")); // NOI18N
        markMissingCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        markMissingCheck.setMaximumSize(new java.awt.Dimension(118, 17));
        markMissingCheck.setMinimumSize(new java.awt.Dimension(118, 17));
        markMissingCheck.setPreferredSize(new java.awt.Dimension(118, 17));

        schnittmengeGroup.add(vereinigungsmengeButton);
        vereinigungsmengeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        vereinigungsmengeButton.setText(bundle.getString("MoniSoft.union.text")); // NOI18N

        schnittmengeGroup.add(schnittmengeButton);
        schnittmengeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        schnittmengeButton.setSelected(true);
        schnittmengeButton.setText(bundle.getString("MoniSoft.intersection.text")); // NOI18N

        DropTarget dt_ValueConstraintSensorSelector4 = new DropTarget(ValueConstraintSensorSelector4,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector5 = new DropTarget(ValueConstraintSensorSelector5,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector6 = new DropTarget(ValueConstraintSensorSelector6,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector7 = new DropTarget(ValueConstraintSensorSelector7,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(f0Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintSensorSelector4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintOperator4, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintValue4, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(f1Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintSensorSelector5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintOperator5, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintValue5, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addComponent(f2Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintSensorSelector6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintOperator6, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintValue6, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                                .addComponent(f3Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintSensorSelector7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintOperator7, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(ValueConstraintValue7, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel2)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(markMissingCheck, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vereinigungsmengeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(schnittmengeButton)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(4, 4, 4)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f0Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f1Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator5, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f2Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator6, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f3Check1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator7, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(markMissingCheck, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(vereinigungsmengeButton)
                    .addComponent(schnittmengeButton))
                .addGap(4, 4, 4))
        );

        TimeBasedTab.add(jPanel9);

        TimeSeriesdrawChartButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        TimeSeriesdrawChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart--pencil.png"))); // NOI18N
        TimeSeriesdrawChartButton.setText(bundle.getString("MoniSoft.TimeSeriesdrawChartButton.text")); // NOI18N
        TimeSeriesdrawChartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TimeSeriesdrawChartButtonActionPerformed(evt);
            }
        });
        TimeBasedTab.add(TimeSeriesdrawChartButton);

        add(TimeBasedTab, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void y1ToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton1ActionPerformed
        y1Toggle(1);
    }//GEN-LAST:event_y1ToggleButton1ActionPerformed

    private void y1ToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton2ActionPerformed
        y1Toggle(2);
    }//GEN-LAST:event_y1ToggleButton2ActionPerformed

    private void y1ToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton3ActionPerformed
        y1Toggle(3);
    }//GEN-LAST:event_y1ToggleButton3ActionPerformed

    private void y1ToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton4ActionPerformed
        y1Toggle(4);
    }//GEN-LAST:event_y1ToggleButton4ActionPerformed

    private void y1ToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton5ActionPerformed
        y1Toggle(5);
    }//GEN-LAST:event_y1ToggleButton5ActionPerformed

    private void y1ToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton6ActionPerformed
        y1Toggle(6);
    }//GEN-LAST:event_y1ToggleButton6ActionPerformed

    private void y1ToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1ToggleButton7ActionPerformed
        y1Toggle(7);
    }//GEN-LAST:event_y1ToggleButton7ActionPerformed

    private void y1SensorSelectorTouched(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1SensorSelectorTouched
        if (!MoniSoft.getInstance().isGUIActive()) {
            return;
        }
        
        SensorProperties props = (SensorProperties) y1SensorSelector.getSelectedItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle"); // NOI18N
        
        System.out.println( "TS_y1ActiveSeriesID: " + TS_y1ActiveSeriesID );
        System.out.println( "TimeSeriesLooksCollection.get(TS_y1ActiveSeriesID): " + TimeSeriesLooksCollection.get(TS_y1ActiveSeriesID) );
                
        if (TimeSeriesLooksCollection.get(TS_y1ActiveSeriesID) == null)
        {
            if (props != null && !props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
                // Zähler            
                y1Counter1.setSelected(true);
            } else if( props != null ) {
                // Standardmäßig ist der y1Counter2 aktiviert            
                y1Counter2.setSelected(true);
            }            
        }
        
        // Wenn ein Zähler Leitungs/Verbrauch/Zählerstandswähler aktivieren
        if( props == null || props.getSensorName() == null || props.getSensorID() < 1 )
        {
            y1Counter1.setEnabled(false);            
            y1Counter2.setEnabled(false);            
            y1Counter3.setEnabled(false);
        }
        else if (!props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
            // Zähler            
            y1Counter1.setEnabled(true);
            y1Counter1.setText(bundle.getString("MoniSoft.y2Counter1.text")); // NOI18N            
            y1Counter2.setEnabled(true);
            y1Counter2.setText(bundle.getString("MoniSoft.y2Counter2.text")); // NOI18N
            y1Counter3.setEnabled(true);
            y1Counter3.setText(bundle.getString("MoniSoft.y2Counter3.text")); // NOI18N            
        } else {
            // Kein Zähler
            // Standardmäßig ist der y1Counter2 aktiviert            
            y1Counter1.setEnabled(true);
            y1Counter1.setText(bundle.getString("MoniSoft.y2Counter1.textNichtZaehler")); // NOI18N            
            y1Counter2.setEnabled(true);
            y1Counter2.setText(bundle.getString("MoniSoft.y2Counter2.textNichtZaehler")); // NOI18N
            y1Counter3.setEnabled(true);
            y1Counter3.setText(bundle.getString("MoniSoft.y2Counter3.textNichtZaehler")); // NOI18N
        }

        // populate reference selector if a building is allocated
        if (props.getBuildingID() != null) {
            // populate reference selector
            DefaultComboBoxModel m = (DefaultComboBoxModel) y1ReferenceSelector.getModel();
            m.removeAllElements();
            m.addElement(MoniSoftConstants.NO_SENSOR_SELECTED);
            if (BuildingInformation.getBuildingReferences(props.getBuildingID()) != null) {
                for (ReferenceValue ref : BuildingInformation.getBuildingReferences(props.getBuildingID())) {
                    m.addElement(ref);
                }
            }
        }
    }//GEN-LAST:event_y1SensorSelectorTouched

    private void y1LockAggregationToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1LockAggregationToggleActionPerformed
        TimeSeriesLooks currentLook;
        if (y1LockAggregationToggle.isSelected()) { // gelockt
            saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
            y1LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock.png")));//NO18N
            if (!TimeSeriesLooksCollection.isEmpty()) {
                Iterator it = TimeSeriesLooksCollection.iterator();
                while (it.hasNext()) {
                    currentLook = (TimeSeriesLooks) it.next();
                    if (currentLook != null && currentLook.getyAxis() == 0) {
                        currentLook.setAggregation(((IntervalSelectorEntry) y1AggSelector.getSelectedItem()).doubleValue());
                    }
                }
            }
            y1AggSelector.setEnabled(false);
            locked_y1AggregationIndex = y1AggSelector.getSelectedIndex();
        } else { // frei gegeben
            y1LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock-unlock.png")));//NO18N
            y1AggSelector.setEnabled(true);
        }
    }//GEN-LAST:event_y1LockAggregationToggleActionPerformed

    private void y1LinesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1LinesToggleButtonActionPerformed
        // andere Button ausschalten
        y1LinesToggleButton.setSelected(true);
        y1BarsToggleButton.setSelected(false);
        y1AreaToggleButton.setSelected(false);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
        cardLayout.show(y1DefinitionPanel, "y1LinesCard");//NO18N
        // Stacked-Checkbox ausschalten
        y1StackedCheckBox.setSelected(false);
    }//GEN-LAST:event_y1LinesToggleButtonActionPerformed

    private void y1BarsToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1BarsToggleButtonActionPerformed
        // andere Button ausschalten
        y1LinesToggleButton.setSelected(false);
        y1BarsToggleButton.setSelected(true);
        y1AreaToggleButton.setSelected(false);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
        cardLayout.show(y1DefinitionPanel, "y1BarsCard");//NO18N
        // Stacked-Checkbox einschalten
    }//GEN-LAST:event_y1BarsToggleButtonActionPerformed

    private void y1AreaToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1AreaToggleButtonActionPerformed
        // andere Buttons ausschalten
        y1LinesToggleButton.setSelected(false);
        y1BarsToggleButton.setSelected(false);
        y1AreaToggleButton.setSelected(true);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y1DefinitionPanel.getLayout();
        cardLayout.show(y1DefinitionPanel, "y1AreaCard");//NO18N
        // Stacked-Checkbox einschalten
    }//GEN-LAST:event_y1AreaToggleButtonActionPerformed

    private void y1_Lines_LinesColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Lines_LinesColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y1_Lines_LinesColorButton.getBackground());
        if (col != null) {
            y1_Lines_LinesColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y1ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y1ActiveSeriesID);
        }
    }//GEN-LAST:event_y1_Lines_LinesColorButtonActionPerformed

    private void y1_Bars_BorderColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Bars_BorderColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y1_Bars_BorderColorButton.getBackground());
        if (col != null) {
            y1_Bars_BorderColorButton.setBackground(col);
            TimeSeriesSecondaryColors[TS_y1ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y1ActiveSeriesID);
        }
    }//GEN-LAST:event_y1_Bars_BorderColorButtonActionPerformed

    private void y1_Bars_FillColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Bars_FillColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y1_Bars_FillColorButton.getBackground());
        if (col != null) {
            y1_Bars_FillColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y1ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y1ActiveSeriesID);

        }
    }//GEN-LAST:event_y1_Bars_FillColorButtonActionPerformed

    private void y1_Area_BorderColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Area_BorderColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y1_Area_BorderColorButton.getBackground());
        if (col != null) {
            y1_Area_BorderColorButton.setBackground(col);
            TimeSeriesSecondaryColors[TS_y1ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y1ActiveSeriesID);
        }
    }//GEN-LAST:event_y1_Area_BorderColorButtonActionPerformed

    private void y1_Area_FillColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Area_FillColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y1_Area_FillColorButton.getBackground());
        if (col != null) {
            y1_Area_FillColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y1ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y1ActiveSeriesID);
        }
    }//GEN-LAST:event_y1_Area_FillColorButtonActionPerformed

    private void y1TimeSeriesResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1TimeSeriesResetActionPerformed
        reset_y1Series();
    }//GEN-LAST:event_y1TimeSeriesResetActionPerformed

    private void y1StackedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1StackedCheckBoxActionPerformed
        // stacked was clicked
        // je nach Checkboxstatus alle Slots auf stacked setzen
        saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
        if (!TimeSeriesLooksCollection.isEmpty()) { // wenn es gewählte Messpunte gibt
            for (TimeSeriesLooks currentLook : TimeSeriesLooksCollection) {
                if (currentLook != null) {
                    if (currentLook.getSeriesID() >= 7) { // if this is a series from the secondary axis make it invalid
                        TimeSeriesLooksCollection.set(currentLook.getSeriesID(), null);
                        loadTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
                    } else {
                        if (y1StackedCheckBox.isSelected()) {
                            currentLook.setStacked(true);
                        } else {
                            currentLook.setStacked(false);
                        }
                    }
                }
            }
        }

        // wenn gewählt die Darstellungsart (Balken/Fläche) für alle slots übernehmen
        if (y1StackedCheckBox.isSelected()) {
            // if lines are selected switch to bars
            if (y1LinesToggleButton.isSelected()) {
                y1BarsToggleButton.setSelected(true);
            }
            y1LinesToggleButton.setSelected(false);
            y1LinesToggleButton.setEnabled(false);

            for (TimeSeriesLooks looks : TimeSeriesLooksCollection) {
                if (looks != null) {
                    if (y1BarsToggleButton.isSelected()) {
                        looks.setDrawType(MoniSoftConstants.TS_BARS);
                    } else {
                        looks.setDrawType(MoniSoftConstants.TS_AREA);
                    }
                }
            }
            AxisTabPanel.setEnabled(false);
        } else {
            y1LinesToggleButton.setEnabled(true);
            AxisTabPanel.setEnabled(true);
        }
        // TODO wenn gewählt müssen alle Sensoren für diese Achse vom gleichen Darstellungstyp sein (Mischung aus StapelBalken und StapelFlächen unmöglich)
        // daher wenn "selected": alle Werte dieser Achse auf den Momentanen Darstellungstyp setzten
    }//GEN-LAST:event_y1StackedCheckBoxActionPerformed

    private void y2ToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton1ActionPerformed
        y2Toggle(8);
    }//GEN-LAST:event_y2ToggleButton1ActionPerformed

    private void y2ToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton2ActionPerformed
        y2Toggle(9);
    }//GEN-LAST:event_y2ToggleButton2ActionPerformed

    private void y2ToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton3ActionPerformed
        y2Toggle(10);
    }//GEN-LAST:event_y2ToggleButton3ActionPerformed

    private void y2ToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton4ActionPerformed
        y2Toggle(11);
    }//GEN-LAST:event_y2ToggleButton4ActionPerformed

    private void y2ToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton5ActionPerformed
        y2Toggle(12);
    }//GEN-LAST:event_y2ToggleButton5ActionPerformed

    private void y2ToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton6ActionPerformed
        y2Toggle(13);
    }//GEN-LAST:event_y2ToggleButton6ActionPerformed

    private void y2ToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2ToggleButton7ActionPerformed
        y2Toggle(14);
    }//GEN-LAST:event_y2ToggleButton7ActionPerformed

    private void y2SensorSelectorTouched(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2SensorSelectorTouched
        if (!MoniSoft.getInstance().isGUIActive()) {
            return;
        }
        
        SensorProperties props = (SensorProperties) y2SensorSelector.getSelectedItem();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle"); // NOI18N

        System.out.println( "TS_y2ActiveSeriesID: " + TS_y2ActiveSeriesID );
        System.out.println( "TimeSeriesLooksCollection.get(TS_y2ActiveSeriesID): " + TimeSeriesLooksCollection.get(TS_y2ActiveSeriesID) );
                
        if (TimeSeriesLooksCollection.get(TS_y2ActiveSeriesID) == null)
        {
            if (props != null && !props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
                // Zähler            
                y2Counter1.setSelected(true);
            } else if( props != null ) {
                // Standardmäßig ist der y1Counter2 aktiviert            
                y2Counter2.setSelected(true);
            }            
        }

        // Wenn ein Zähler Leitungs/Verbrauch/Zählerstandswähler aktivieren
        if( props == null || props.getSensorName() == null || props.getSensorID() < 1 )
        {
            y2Counter1.setEnabled(false);            
            y2Counter2.setEnabled(false);            
            y2Counter3.setEnabled(false);
        }        
        else if (!props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
            // Zähler
            y2Counter1.setEnabled(true);
            y2Counter1.setText(bundle.getString("MoniSoft.y2Counter1.text"));
            y2Counter2.setEnabled(true);
            y2Counter2.setText(bundle.getString("MoniSoft.y2Counter2.text"));
            y2Counter3.setEnabled(true);
            y2Counter3.setText(bundle.getString("MoniSoft.y2Counter3.text"));
        } else {
            y2Counter1.setEnabled(true);
            y2Counter1.setText(bundle.getString("MoniSoft.y2Counter1.textNichtZaehler")); // NOI18N            
            y2Counter2.setEnabled(true);
            y2Counter2.setText(bundle.getString("MoniSoft.y2Counter2.textNichtZaehler")); // NOI18N            
            y2Counter3.setEnabled(true);
            y2Counter3.setText(bundle.getString("MoniSoft.y2Counter3.textNichtZaehler")); // NOI18N            
        }

        if (props.getBuildingID() != null) {
            // populate reference selector
            DefaultComboBoxModel m = (DefaultComboBoxModel) y2ReferenceSelector.getModel();
            m.removeAllElements();
            m.addElement(MoniSoftConstants.NO_SENSOR_SELECTED);
            for (ReferenceValue ref : BuildingInformation.getBuildingReferences(props.getBuildingID())) {
                m.addElement(ref);
            }
        }
    }//GEN-LAST:event_y2SensorSelectorTouched

    private void y2LockAggregationToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2LockAggregationToggleActionPerformed
        TimeSeriesLooks currentLook;
        if (y2LockAggregationToggle.isSelected()) {
            saveTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
            y2LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock.png")));//NO18N
            if (!TimeSeriesLooksCollection.isEmpty()) {
                Iterator it = TimeSeriesLooksCollection.iterator();
                while (it.hasNext()) {
                    currentLook = (TimeSeriesLooks) it.next();
                    if (currentLook != null && currentLook.getyAxis() == 1) {
                        currentLook.setAggregation(((IntervalSelectorEntry) y2AggSelector.getSelectedItem()).doubleValue());
                    }
                }
            }
            y2AggSelector.setEnabled(false);
            locked_y2AggregationIndex = y2AggSelector.getSelectedIndex();
        } else {    // frei gegeben
            y2LockAggregationToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/lock-unlock.png")));//NO18N
            y2AggSelector.setEnabled(true);
        }
    }//GEN-LAST:event_y2LockAggregationToggleActionPerformed

    private void y2LinesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2LinesToggleButtonActionPerformed
        // andere Button ausschalten
        y2LinesToggleButton.setSelected(true);
        y2BarsToggleButton.setSelected(false);
        y2AreaToggleButton.setSelected(false);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
        cardLayout.show(y2DefinitionPanel, "y2LinesCard");//NO18N
    }//GEN-LAST:event_y2LinesToggleButtonActionPerformed

    private void y2BarsToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2BarsToggleButtonActionPerformed
        // andere Button ausschalten
        y2LinesToggleButton.setSelected(false);
        y2BarsToggleButton.setSelected(true);
        y2AreaToggleButton.setSelected(false);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
        cardLayout.show(y2DefinitionPanel, "y2BarsCard");//NO18N
    }//GEN-LAST:event_y2BarsToggleButtonActionPerformed

    private void y2AreaToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2AreaToggleButtonActionPerformed
        // andere Button ausschalten
        y2LinesToggleButton.setSelected(false);
        y2BarsToggleButton.setSelected(false);
        y2AreaToggleButton.setSelected(true);
        // Zugehöriges Panel setzen
        CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
        cardLayout.show(y2DefinitionPanel, "y2AreaCard");//NO18N
    }//GEN-LAST:event_y2AreaToggleButtonActionPerformed

    private void y2_Lines_LinesColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_Lines_LinesColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y2_Lines_LinesColorButton.getBackground());
        if (col != null) {
            y2_Lines_LinesColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y2ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
        }
    }//GEN-LAST:event_y2_Lines_LinesColorButtonActionPerformed

    private void y2_Bars_BorderColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_Bars_BorderColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y2_Bars_BorderColorButton.getBackground());
        if (col != null) {
            y2_Bars_BorderColorButton.setBackground(col);
            TimeSeriesSecondaryColors[TS_y2ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
        }
    }//GEN-LAST:event_y2_Bars_BorderColorButtonActionPerformed

    private void y2_Bars_FillColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_Bars_FillColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y2_Bars_FillColorButton.getBackground());
        if (col != null) {
            y2_Bars_FillColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y2ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
        }
    }//GEN-LAST:event_y2_Bars_FillColorButtonActionPerformed

    private void y2_Area_BorderColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_Area_BorderColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y2_Area_BorderColorButton.getBackground());
        if (col != null) {
            y2_Area_BorderColorButton.setBackground(col);
            TimeSeriesSecondaryColors[TS_y2ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
        }
    }//GEN-LAST:event_y2_Area_BorderColorButtonActionPerformed

    private void y2_Area_FillColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_Area_FillColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), y2_Area_FillColorButton.getBackground());
        if (col != null) {
            y2_Area_FillColorButton.setBackground(col);
            TimeSeriesMainColors[TS_y2ActiveSeriesID] = col;
            setTimeSeriesButtonColors(TS_y2ActiveSeriesID);
        }
    }//GEN-LAST:event_y2_Area_FillColorButtonActionPerformed

    private void y2TimeSeriesResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2TimeSeriesResetActionPerformed
        reset_y2Series();
    }//GEN-LAST:event_y2TimeSeriesResetActionPerformed

    private void AxisTabPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_AxisTabPanelStateChanged
        if (MoniSoft.getInstance().isGUIActive()) {
            int selectedTab = AxisTabPanel.getSelectedIndex();
            activeAxis = selectedTab;
            switch (selectedTab) {
                case 1:
                    saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
                    loadTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
                    break;

                case 0:
                    saveTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
                    loadTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
                    break;
            }
        }
    }//GEN-LAST:event_AxisTabPanelStateChanged

    private void ValueConstraintOperator5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ValueConstraintOperator5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValueConstraintOperator5ActionPerformed

    private void TimeSeriesdrawChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TimeSeriesdrawChartButtonActionPerformed
        // wenn vorher ein anderer Knopf gedrückt war, dessen Werte speichern und die für diesen Knopf laden
        // AZ: es sollte nur die aktive Oberflächen gespeichert werden        
        if( activeAxis == 1 )
            saveTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);
        else if( activeAxis == 0 )
            saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
            
        // saveTimeSeriesPanelInfo(TS_y1ActiveSeriesID, 0);
        // saveTimeSeriesPanelInfo(TS_y2ActiveSeriesID, 1);

        if (!new DateEntriesChecker().hasValidEntries(tsDateChooserPanel.getStartDateChooser(), tsDateChooserPanel.getEndDateChooser(), TimeSeriesLooksCollection)) {
            return;
        }
        TimeSeriesdrawChartButton.setEnabled(false);
        // Wertefilter holen und setzen
        final ValueFilter filter = getValueFilter();
        final DateInterval interval = new DateInterval(tsDateChooserPanel.getStartDate(), tsDateChooserPanel.getEndDate());
        gui().drawUsingDescriber(new TimeSeriesDescriberFactory(filter, interval, TimeSeriesLooksCollection), interval, TimeSeriesLooksCollection);
        TimeSeriesdrawChartButton.setEnabled(true);
        
        // AZ: nach dem Durchlauf CounterChangeErrorDialog auf false setzen - MONISOFT-8
        Interpolator.saveCancelDecision = false;
        Interpolator.chartType = Interpolator.TIMESERIES_TAB;        
    }

    private void sety2Looks(TimeSeriesLooks seriesLook) {
        switch (seriesLook.getDrawType()) {
            case MoniSoftConstants.TS_LINES: {
                y2LinesToggleButton.setSelected(true);   // Zeichenmodus
                y2BarsToggleButton.setSelected(false);
                y2AreaToggleButton.setSelected(false);
                if (seriesLook.getLines_drawLine()) {   // Linien zeigen
                    y2_Lines_DrawLinesCheck.setSelected(true);
                } else {
                    y2_Lines_DrawLinesCheck.setSelected(false);
                }

                if (seriesLook.getLines_drawSymbols()) {   // Symbole zeigen
                    y2_Lines_DrawSymbolsCheck.setSelected(true);
                } else {
                    y2_Lines_DrawSymbolsCheck.setSelected(false);
                }

                y2_Lines_LinesColorButton.setBackground(seriesLook.getLines_lineColor());
                y2_Lines_SymbolTypeComboBox.setSelectedIndex(seriesLook.getLines_symbolType());
                y2_Lines_SymbolSizeComboBox.setSelectedIndex(seriesLook.getLines_symbolSize()); // Wert 1 hat index 0
                y2_Lines_LineStrokeChooser.setSelectedIndex(seriesLook.getLines_lineType());
                y2_Lines_LineSizeComboBox.setSelectedIndex(seriesLook.getLines_lineSize());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
                cardLayout.show(y2DefinitionPanel, "y2LinesCard");//NO18N
            }

            break;
            case MoniSoftConstants.TS_BARS: {
                y2LinesToggleButton.setSelected(false);
                y2BarsToggleButton.setSelected(true);
                y2AreaToggleButton.setSelected(false);
                y2_Bars_alphaSlider.setValue(seriesLook.getBars_alpha());
                if (seriesLook.getBars_DrawFilling()) {   // Linien zeigen
                    y2_Bars_DrawFillCheck.setSelected(true);
                } else {
                    y2_Bars_DrawFillCheck.setSelected(false);
                }

                if (seriesLook.getBars_DrawBorder()) {   // Symbole zeigen
                    y2_Bars_DrawBorderCheck.setSelected(true);
                } else {
                    y2_Bars_DrawBorderCheck.setSelected(false);
                }

                y2_Bars_FillColorButton.setBackground(seriesLook.getBars_fillColor());
                y2_Bars_BorderColorButton.setBackground(seriesLook.getBars_borderColor());
                y2_Bars_BorderSizeComboBox.setSelectedIndex(seriesLook.getBars_borderSize());
                y2_Bars_BorderTypeComboBox.setSelectedIndex(seriesLook.getBars_borderType());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
                cardLayout.show(y2DefinitionPanel, "y2BarsCard");//NO18N
            }

            break;
            case MoniSoftConstants.TS_AREA: {
                y2LinesToggleButton.setSelected(false);
                y2BarsToggleButton.setSelected(false);
                y2AreaToggleButton.setSelected(true);
                y2_Area_alphaSlider.setValue(seriesLook.getArea_alpha());
                if (seriesLook.getArea_DrawFilling()) {   // Linien zeigen
                    y2_Area_DrawFillCheck.setSelected(true);
                } else {
                    y2_Area_DrawFillCheck.setSelected(false);
                }

                if (seriesLook.getArea_DrawBorder()) {   // Symbole zeigen
                    y2_Area_DrawBorderCheck.setSelected(true);
                } else {
                    y2_Area_DrawBorderCheck.setSelected(false);
                }

                y2_Area_FillColorButton.setBackground(seriesLook.getArea_fillColor());
                y2_Area_BorderColorButton.setBackground(seriesLook.getArea_borderColor());
                y2_Area_BorderSizeComboBox.setSelectedIndex(seriesLook.getBars_borderSize());
                y2_Area_BorderTypeComboBox.setSelectedIndex(seriesLook.getBars_borderType());
                // zugehöriges Card-Panel setzen
                CardLayout cardLayout = (CardLayout) y2DefinitionPanel.getLayout();
                cardLayout.show(y2DefinitionPanel, "y2AreaCard");//NO18N
            }
            break;
        }
    }

    private void loadValueFilters(TimeSeriesChartDescriber chartDesriber) {
        //                  // empty all value filters 
        // AZ: Kein Zurücksetzen der Einstellungen - MONISOFT-20
        /*
        f0Check1.setSelected(false);
        f1Check1.setSelected(false);
        f2Check1.setSelected(false);
        f3Check1.setSelected(false);
        ValueConstraintOperator4.setSelectedIndex(0);
        ValueConstraintOperator5.setSelectedIndex(0);
        ValueConstraintOperator6.setSelectedIndex(0);
        ValueConstraintOperator7.setSelectedIndex(0);
        ValueConstraintSensorSelector4.setSelectedIndex(0);
        ValueConstraintSensorSelector5.setSelectedIndex(0);
        ValueConstraintSensorSelector6.setSelectedIndex(0);
        ValueConstraintSensorSelector7.setSelectedIndex(0);
        ValueConstraintValue4.setText(null);
        ValueConstraintValue5.setText(null);
        ValueConstraintValue6.setText(null);
        ValueConstraintValue7.setText(null);
                */
        // if there are filter fill them in
        if (chartDesriber.getValueFilter() != null) {
            ArrayList<ValueFilterComponent> components = chartDesriber.getValueFilter().getFilterComponents();
            for (int i = 0; i < components.size(); i++) {
                switch (i) {
                    case 0:
                        f0Check1.setSelected(true);
                        ValueConstraintOperator4.setSelectedItem(components.get(i).getOperand());
                        ValueConstraintSensorSelector4.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                        ValueConstraintValue4.setText(String.valueOf(components.get(i).getValue()));
                        break;
                    case 1:
                        f1Check1.setSelected(true);
                        ValueConstraintOperator5.setSelectedItem(components.get(i).getOperand());
                        ValueConstraintSensorSelector5.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                        ValueConstraintValue5.setText(String.valueOf(components.get(i).getValue()));
                        break;
                    case 2:
                        f2Check1.setSelected(true);
                        ValueConstraintOperator5.setSelectedItem(components.get(i).getOperand());
                        ValueConstraintSensorSelector5.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                        ValueConstraintValue5.setText(String.valueOf(components.get(i).getValue()));
                        break;
                    case 3:
                        f3Check1.setSelected(true);
                        ValueConstraintOperator5.setSelectedItem(components.get(i).getOperand());
                        ValueConstraintSensorSelector5.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                        ValueConstraintValue5.setText(String.valueOf(components.get(i).getValue()));
                        break;
                }
            }
        }
    }

    @Override
    public void fillAnnotationChooser() {
        // do nothing
    }

    private DefaultComboBoxModel getReferenceSelectorModel() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("<KEINE>"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("YEAR"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("QUARTER"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MONTH"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("WEEK"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DAY"));//NO18N
        model.addElement(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("HOUR"));//NO18N
        return model;
    }

    private void setStrokeChooser() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("CONTINUOUS"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DOTTED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DASHED")});
        y1_Lines_LineStrokeChooser.setModel(model);
        y2_Lines_LineStrokeChooser.setModel(model);
    }

    private void setSymbolChooser() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DOT"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SQUARE"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TRIANGLE")});
        y1_Lines_SymbolTypeComboBox.setModel(model);
        y2_Lines_SymbolTypeComboBox.setModel(model);
    }

    private static final class TimeSeriesDescriberFactory implements DescriberFactory {

        private final ValueFilter filter;
        private final DateInterval interval;
        private final ArrayList<TimeSeriesLooks> timeSeriesLooksCollection;

        private TimeSeriesDescriberFactory(ValueFilter filter, DateInterval interval, ArrayList<TimeSeriesLooks> TimeSeriesLooksCollection) {
            this.filter = filter;
            this.interval = interval;
            timeSeriesLooksCollection = TimeSeriesLooksCollection;
        }

        @Override
        public ChartDescriber createChartDescriber() {
            TimeSeriesChartDescriber describer = new TimeSeriesChartDescriber(java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TIME_BASED_PLOT"), interval, timeSeriesLooksCollection);
            describer.setValueFilter(filter);
            describer.setMarkMissing(markMissingCheck.isSelected());
            return describer;
        }
    }

    private ValueFilter getValueFilter() {
        ValueFilter filter = new ValueFilter(false);
        if (f0Check1.isSelected()) {
            if (filter.testFilter(ValueConstraintSensorSelector4.getSelectedItem(), ValueConstraintOperator4.getSelectedItem(), ValueConstraintValue4.getText().replace(",", "."))) {//NO18N
                // AZ: Es soll ein or-Filter eingebunden werden können - MONISOFT-21
                if( vereinigungsmengeButton.isSelected() )
                    filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector4.getSelectedItem(), (String) ValueConstraintOperator4.getSelectedItem(), Double.valueOf(ValueConstraintValue4.getText().replace(",", ".")));//NO18N
                else
                    filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector4.getSelectedItem(), (String) ValueConstraintOperator4.getSelectedItem(), Double.valueOf(ValueConstraintValue4.getText().replace(",", ".")));//NO18N
            } else {
                filter = null;
            }
        }
        if (f1Check1.isSelected() && filter != null) {
            if (filter.testFilter(ValueConstraintSensorSelector5.getSelectedItem(), ValueConstraintOperator5.getSelectedItem(), ValueConstraintValue5.getText().replace(",", "."))) {//NO18N
                if( vereinigungsmengeButton.isSelected() )
                    filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector5.getSelectedItem(), (String) ValueConstraintOperator5.getSelectedItem(), Double.valueOf(ValueConstraintValue5.getText().replace(",", ".")));//NO18N
                else
                    filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector5.getSelectedItem(), (String) ValueConstraintOperator5.getSelectedItem(), Double.valueOf(ValueConstraintValue5.getText().replace(",", ".")));//NO18N
            } else {
                filter = null;
            }
        }
        if (f2Check1.isSelected() && filter != null) {
            if (filter.testFilter(ValueConstraintSensorSelector6.getSelectedItem(), ValueConstraintOperator6.getSelectedItem(), ValueConstraintValue6.getText().replace(",", "."))) {//NO18N
                if( vereinigungsmengeButton.isSelected() )
                    filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector6.getSelectedItem(), (String) ValueConstraintOperator6.getSelectedItem(), Double.valueOf(ValueConstraintValue6.getText().replace(",", ".")));//NO18N
                else
                    filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector6.getSelectedItem(), (String) ValueConstraintOperator6.getSelectedItem(), Double.valueOf(ValueConstraintValue6.getText().replace(",", ".")));//NO18N
            } else {
                filter = null;
            }
        }
        if (f3Check1.isSelected() && filter != null) {
            if (filter.testFilter(ValueConstraintSensorSelector7.getSelectedItem(), ValueConstraintOperator7.getSelectedItem(), ValueConstraintValue7.getText().replace(",", "."))) {//NO18N
                if( vereinigungsmengeButton.isSelected() )
                    filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector7.getSelectedItem(), (String) ValueConstraintOperator7.getSelectedItem(), Double.valueOf(ValueConstraintValue7.getText().replace(",", ".")));//NO18N    
                else
                    filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector7.getSelectedItem(), (String) ValueConstraintOperator7.getSelectedItem(), Double.valueOf(ValueConstraintValue7.getText().replace(",", ".")));//NO18N
            } else {
                filter = null;
            }
        }

        if (filter == null) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("INVALID_VALUE_FILTER") + "\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("NO_FILTER_APPLIED"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("MESSAGE"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (filter.getValueFilterString().isEmpty()) {
                filter = null;
            }
        }
        return filter;
	}//GEN-LAST:event_TimeSeriesdrawChartButtonActionPerformed

    private void y1_Lines_LineStrokeChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_Lines_LineStrokeChooserActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_y1_Lines_LineStrokeChooserActionPerformed

    private void ValueConstraintSensorSelector4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ValueConstraintSensorSelector4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValueConstraintSensorSelector4ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane AxisTabPanel;
    private javax.swing.JPanel LeftAxisPanel;
    private javax.swing.JPanel RightAxisPanel;
    private javax.swing.JPanel TimeBasedTab;
    private javax.swing.JButton TimeSeriesdrawChartButton;
    private javax.swing.JComboBox ValueConstraintOperator4;
    private javax.swing.JComboBox ValueConstraintOperator5;
    private javax.swing.JComboBox ValueConstraintOperator6;
    private javax.swing.JComboBox ValueConstraintOperator7;
    private javax.swing.JComboBox ValueConstraintSensorSelector4;
    private javax.swing.JComboBox ValueConstraintSensorSelector5;
    private javax.swing.JComboBox ValueConstraintSensorSelector6;
    private javax.swing.JComboBox ValueConstraintSensorSelector7;
    private javax.swing.JTextField ValueConstraintValue4;
    private javax.swing.JTextField ValueConstraintValue5;
    private javax.swing.JTextField ValueConstraintValue6;
    private javax.swing.JTextField ValueConstraintValue7;
    private javax.swing.JCheckBox f0Check1;
    private javax.swing.JCheckBox f1Check1;
    private javax.swing.JCheckBox f2Check1;
    private javax.swing.JCheckBox f3Check1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel9;
    public static javax.swing.JCheckBox markMissingCheck;
    private javax.swing.JRadioButton schnittmengeButton;
    private javax.swing.ButtonGroup schnittmengeGroup;
    private javax.swing.JRadioButton vereinigungsmengeButton;
    private javax.swing.JComboBox y1AggSelector;
    private javax.swing.JPanel y1AreaPanel;
    private javax.swing.JToggleButton y1AreaToggleButton;
    private javax.swing.JPanel y1BarsPanel;
    private javax.swing.JToggleButton y1BarsToggleButton;
    private javax.swing.JRadioButton y1Counter1;
    private javax.swing.JRadioButton y1Counter2;
    private javax.swing.JRadioButton y1Counter3;
    private javax.swing.ButtonGroup y1CounterRadioGroup;
    private javax.swing.JPanel y1DefinitionPanel;
    private javax.swing.JPanel y1LinesPanel;
    private javax.swing.JToggleButton y1LinesToggleButton;
    private javax.swing.JToggleButton y1LockAggregationToggle;
    private javax.swing.JComboBox y1ReferenceSelector;
    private javax.swing.JComboBox y1SensorSelector;
    private javax.swing.ButtonGroup y1SlotButtonGroup;
    private javax.swing.JCheckBox y1StackedCheckBox;
    private javax.swing.JComboBox y1TimeReferenceSelector;
    private javax.swing.JButton y1TimeSeriesReset;
    private javax.swing.JToggleButton y1ToggleButton1;
    private javax.swing.JToggleButton y1ToggleButton2;
    private javax.swing.JToggleButton y1ToggleButton3;
    private javax.swing.JToggleButton y1ToggleButton4;
    private javax.swing.JToggleButton y1ToggleButton5;
    private javax.swing.JToggleButton y1ToggleButton6;
    private javax.swing.JToggleButton y1ToggleButton7;
    static javax.swing.JButton y1_Area_BorderColorButton;
    private javax.swing.JComboBox y1_Area_BorderSizeCombobox;
    private javax.swing.JComboBox y1_Area_BorderTypeCombobox;
    private javax.swing.JCheckBox y1_Area_DrawBorderCheck;
    private javax.swing.JCheckBox y1_Area_DrawFillCheck;
    static javax.swing.JButton y1_Area_FillColorButton;
    private javax.swing.JSlider y1_Area_alphaSlider;
    static javax.swing.JButton y1_Bars_BorderColorButton;
    private javax.swing.JComboBox y1_Bars_BorderSizeCombobox;
    private javax.swing.JComboBox y1_Bars_BorderTypeCombobox;
    private javax.swing.JCheckBox y1_Bars_DrawBorderCheck;
    private javax.swing.JCheckBox y1_Bars_DrawFillCheck;
    static javax.swing.JButton y1_Bars_FillColorButton;
    private javax.swing.JSlider y1_Bars_alphaSlider;
    private javax.swing.JCheckBox y1_Lines_DrawLinesCheck;
    private javax.swing.JCheckBox y1_Lines_DrawSymbolsCheck;
    private javax.swing.JComboBox y1_Lines_LineSizeComboBox;
    private javax.swing.JComboBox y1_Lines_LineStrokeChooser;
    private javax.swing.JButton y1_Lines_LinesColorButton;
    private javax.swing.JComboBox y1_Lines_SymbolSizeComboBox;
    private javax.swing.JComboBox y1_Lines_SymbolTypeComboBox;
    private javax.swing.JComboBox y2AggSelector;
    private javax.swing.JPanel y2AreaPanel;
    private javax.swing.JToggleButton y2AreaToggleButton;
    private javax.swing.JPanel y2BarsPanel;
    private javax.swing.JToggleButton y2BarsToggleButton;
    private javax.swing.JRadioButton y2Counter1;
    private javax.swing.JRadioButton y2Counter2;
    private javax.swing.JRadioButton y2Counter3;
    private javax.swing.ButtonGroup y2CounterRadioGroup;
    private javax.swing.JPanel y2DefinitionPanel;
    private javax.swing.JPanel y2LinesPanel;
    private javax.swing.JToggleButton y2LinesToggleButton;
    private javax.swing.JToggleButton y2LockAggregationToggle;
    private javax.swing.JComboBox y2ReferenceSelector;
    private javax.swing.JComboBox y2SensorSelector;
    private javax.swing.ButtonGroup y2SlotButtonGroup;
    private javax.swing.JComboBox y2TimeReferenceSelector;
    private javax.swing.JButton y2TimeSeriesReset;
    private javax.swing.JToggleButton y2ToggleButton1;
    private javax.swing.JToggleButton y2ToggleButton2;
    private javax.swing.JToggleButton y2ToggleButton3;
    private javax.swing.JToggleButton y2ToggleButton4;
    private javax.swing.JToggleButton y2ToggleButton5;
    private javax.swing.JToggleButton y2ToggleButton6;
    private javax.swing.JToggleButton y2ToggleButton7;
    static javax.swing.JButton y2_Area_BorderColorButton;
    private javax.swing.JComboBox y2_Area_BorderSizeComboBox;
    private javax.swing.JComboBox y2_Area_BorderTypeComboBox;
    private javax.swing.JCheckBox y2_Area_DrawBorderCheck;
    private javax.swing.JCheckBox y2_Area_DrawFillCheck;
    static javax.swing.JButton y2_Area_FillColorButton;
    private javax.swing.JSlider y2_Area_alphaSlider;
    private javax.swing.JButton y2_Bars_BorderColorButton;
    private javax.swing.JComboBox y2_Bars_BorderSizeComboBox;
    private javax.swing.JComboBox y2_Bars_BorderTypeComboBox;
    private javax.swing.JCheckBox y2_Bars_DrawBorderCheck;
    private javax.swing.JCheckBox y2_Bars_DrawFillCheck;
    private javax.swing.JButton y2_Bars_FillColorButton;
    private javax.swing.JSlider y2_Bars_alphaSlider;
    private javax.swing.JCheckBox y2_Lines_DrawLinesCheck;
    private javax.swing.JCheckBox y2_Lines_DrawSymbolsCheck;
    private javax.swing.JComboBox y2_Lines_LineSizeComboBox;
    private javax.swing.JComboBox y2_Lines_LineStrokeChooser;
    private javax.swing.JButton y2_Lines_LinesColorButton;
    private javax.swing.JComboBox y2_Lines_SymbolSizeComboBox;
    private javax.swing.JComboBox y2_Lines_SymbolTypeComboBox;
    // End of variables declaration//GEN-END:variables
}
