/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.Components.DatePanel;
import de.jmonitoring.DataHandling.DataFilter.ValueFilter;
import de.jmonitoring.DataHandling.DataFilter.ValueFilterComponent;
import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import de.jmonitoring.base.DateEntriesChecker;
import de.jmonitoring.base.DescriberFactory;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.standardPlots.plotTabs.PlotBaseTab;
import de.jmonitoring.utils.AnnotationEditor.AnnotationDesigner;
import de.jmonitoring.utils.AnnotationEditor.AnnotationHandler;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.DeepCopyCollection;
import de.jmonitoring.utils.DnDListener.SensorSelectorDropListener;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.intervals.IntervalSelectorEntry;
import java.awt.Color;
import java.awt.dnd.DropTarget;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;

import javax.swing.JColorChooser;

/**
 *
 * @author dsl
 */
public class XYPlotTab extends PlotBaseTab {

    private Color[] XYSeriesColors = new Color[14];
    private ArrayList<ScatterSeriesLooks> XYSeriesLooksCollection = new ArrayList<ScatterSeriesLooks>();
    private int XY_ActiveSeries = 0;
    private final DatePanel xyDateChooserPanel;

    /**
     * Creates new form XYPanel
     */
    public XYPlotTab(MainApplication gui) {
        super(gui);
        this.xyDateChooserPanel = new DatePanel(gui(), true);
        initComponents();
        setSymbolChooser();
    }

    @Override
    protected String getTabName() {
        return "xyTab";
    }

    @Override
    public Result fillFrom(ChartDescriber chartDescriber) {
        if (!(chartDescriber instanceof ScatterChartDescriber)) {
            return Result.IGNORED;
        }
        XYSeriesLooksCollection = (ArrayList) new DeepCopyCollection().makeDeepCopy(chartDescriber.getchartCollection());
        XY_ActiveSeries = 0;
        xyToggleButton1.setSelected(true);
        xyDateChooserPanel.getStartDateChooser().setDate(chartDescriber.getDateInterval().getStartDate());
        xyDateChooserPanel.getEndDateChooser().setDate(chartDescriber.getDateInterval().getEndDate());
        loadXYPanelInfo(0, (ScatterChartDescriber) chartDescriber);
        //TabbedPanel.setSelectedComponent(xyTab); // den Scatter-Tab in den Vordergrund holen
        return Result.APPLIED;
    }

    @Override
    public void lockDates(boolean lock) {
        xyDateChooserPanel.getLockToggle().setSelected(lock);
    }

    @Override
    public void resetCollections(int index) {
        XYSeriesLooksCollection.add(index, null);
        XYSeriesColors[index] = MoniSoftConstants.ColorTable.get(index);
    }

    @Override
    public void clearSelections() {
        XY_xSensorSelector.removeAllItems();
        XY_ySensorSelector.removeAllItems();
        ValueConstraintSensorSelector0.removeAllItems();
        ValueConstraintSensorSelector1.removeAllItems();
        ValueConstraintSensorSelector2.removeAllItems();
        ValueConstraintSensorSelector3.removeAllItems();
    }

    @Override
    public void setSelectionsFrom(Models models) {
        XY_xSensorSelector.setModel(models.getSensorListComboBoxModel());
        XY_ySensorSelector.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector0.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector1.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector2.setModel(models.getSensorListComboBoxModel());
        ValueConstraintSensorSelector3.setModel(models.getSensorListComboBoxModel());
    }

    @Override
    public void clearData() {
        XYSeriesLooksCollection.clear();
        XY_ActiveSeries = 0;
    }

    @Override
    public DateInterval getSelectedInterval() {
        return xyDateChooserPanel.getInterval();
    }

    @Override
    public void setSelectedInterval(DateInterval newInterval) {
        xyDateChooserPanel.setInterval(newInterval);
    }

    @Override
    public void setIntervalSelector(Models models) {
        XY_AggSelector.setModel(models.getAggIntervalComboBoxModel());
        XY_AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(XY_AggSelector, (int) MoniSoftConstants.HOUR_INTERVAL));
    }

    /**
     * Takes the current setting of the Scatter-Aggregatioon-Chooser and sets
     * all series to it's value
     */
    private void setScatterAggregationToAllSlots() {
        for (ScatterSeriesLooks seriesLook : XYSeriesLooksCollection) {
            if (seriesLook != null) {
                seriesLook.setAggregation(((IntervalSelectorEntry) XY_AggSelector.getSelectedItem()).doubleValue());
            }
        }
    }

    private void loadXYPanelInfo(int seriesID) {
        loadXYPanelInfo(seriesID, null);
    }

    private void xyToggle(int slot) {
        int index = slot - 1;
        if (XY_ActiveSeries != index) {
            saveXYPanelInfo(XY_ActiveSeries);
            XY_ActiveSeries = index;
            loadXYPanelInfo(XY_ActiveSeries);
        }
    }

    private void setSymbolChooser() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new String[]{java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("DOT"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SQUARE"), java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("TRIANGLE")});
       XY_PointTypeSelector.setModel(model);
    }

    @Override
    public void fillAnnotationChooser() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String name : AnnotationHandler.getAnnotationAsList()) {
            model.addElement(name);
        }
        annotationChooser.setModel(model);
    }

    /**
     *
     * @param seriesID
     */
    private void saveXYPanelInfo(int seriesID) {
        SensorProperties selectedProps = (SensorProperties) XY_ySensorSelector.getSelectedItem();
        ScatterSeriesLooks seriesLook;

        // wenn Auswahlfeld leer beenden
        if (MoniSoftConstants.NO_SENSOR_SELECTED.equals(selectedProps.getSensorName())) {
            XYSeriesLooksCollection.set(seriesID, null);
            return;
        }

        seriesLook = new ScatterSeriesLooks(seriesID);
        seriesLook.setDomainSensor((SensorProperties) XY_xSensorSelector.getSelectedItem());
//        seriesLook.setAggregation(((IntervalSelectorEntry) XY_AggSelector.getSelectedItem()).doubleValue());
        seriesLook.setSensor(selectedProps.getSensorName());
        seriesLook.setLegendString(null);
        seriesLook.setSensorID(selectedProps.getSensorID());
        seriesLook.setUnit(selectedProps.getSensorUnit().getUnit());
        seriesLook.setPointsColor(XY_SeriesColorButton.getBackground());
        seriesLook.setPowerWanted(XY_SeriesPowerCheck.isSelected());
        seriesLook.setFactor(selectedProps.getFactor());
        seriesLook.setPointSize(XY_PointSizeSelector.getSelectedIndex());
        seriesLook.setPointType(XY_PointTypeSelector.getSelectedIndex());
        seriesLook.setUseTimeConstraints(XYTimeConstraintCheckBox.isSelected());
        seriesLook.setUseWeekDayConstraints(XYWeekDayConstraintCheckBox.isSelected());
        seriesLook.setUseDateConstraints(XYDateConstraintCheckBox.isSelected());
        seriesLook.setStartTimeConstraint(XYstartTimeConstraint.getSelectedIndex());
        seriesLook.setEndTimeConstraint(XYendTimeConstraint.getSelectedIndex());

        // eventuelle Datumsbeschränkungen aufnehmen
        if (XYDateConstraintCheckBox.isSelected() && jDateChooser1.getDate() != null && jDateChooser2.getDate() != null) {
            seriesLook.setStartDateConstraints(jDateChooser1.getDate().getTime());
            seriesLook.setEndDateConstraints(jDateChooser2.getDate().getTime());
        } else {
            XYDateConstraintCheckBox.setSelected(false);
        }

        byte dayContraints = 0;
        if (jCheckBox2.isSelected()) {
            dayContraints += 1;
        }
        if (jCheckBox3.isSelected()) {
            dayContraints += 2;
        }
        if (jCheckBox4.isSelected()) {
            dayContraints += 4;
        }
        if (jCheckBox5.isSelected()) {
            dayContraints += 8;
        }
        if (jCheckBox6.isSelected()) {
            dayContraints += 16;
        }
        if (jCheckBox7.isSelected()) {
            dayContraints += 32;
        }
        if (jCheckBox8.isSelected()) {
            dayContraints += 64;
        }
        seriesLook.setWeekDayConstraintCode(dayContraints);

        ValueFilter filter = new ValueFilter(true);
        if (f0Check.isSelected()) {
            if( vereinigungsmengeButton.isSelected() )
                // AZ: Es soll ein or-Filter eingebunden werden können - MONISOFT-21
                filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector0.getSelectedItem(), (String) ValueConstraintOperator0.getSelectedItem(), Double.valueOf(ValueConstraintValue0.getText().replace(",", ".")));
            else
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector0.getSelectedItem(), (String) ValueConstraintOperator0.getSelectedItem(), Double.valueOf(ValueConstraintValue0.getText().replace(",", ".")));
        }
        if (f1Check.isSelected()) {
            if( vereinigungsmengeButton.isSelected() )
                filter.addOrFilter((SensorProperties) ValueConstraintSensorSelector1.getSelectedItem(), (String) ValueConstraintOperator1.getSelectedItem(), Double.valueOf(ValueConstraintValue1.getText().replace(",", ".")));
            else
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector1.getSelectedItem(), (String) ValueConstraintOperator1.getSelectedItem(), Double.valueOf(ValueConstraintValue1.getText().replace(",", ".")));
        }
        if (f2Check.isSelected()) {
            if( vereinigungsmengeButton.isSelected() )
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector2.getSelectedItem(), (String) ValueConstraintOperator2.getSelectedItem(), Double.valueOf(ValueConstraintValue2.getText().replace(",", ".")));
            else
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector2.getSelectedItem(), (String) ValueConstraintOperator2.getSelectedItem(), Double.valueOf(ValueConstraintValue2.getText().replace(",", ".")));
        }
        if (f3Check.isSelected()) {
            if( vereinigungsmengeButton.isSelected() )
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector3.getSelectedItem(), (String) ValueConstraintOperator3.getSelectedItem(), Double.valueOf(ValueConstraintValue3.getText().replace(",", ".")));
            else
                filter.addAndFilter((SensorProperties) ValueConstraintSensorSelector3.getSelectedItem(), (String) ValueConstraintOperator3.getSelectedItem(), Double.valueOf(ValueConstraintValue3.getText().replace(",", ".")));
        }
        if (filter.getValueFilterString().isEmpty()) {
            filter = null;
        } else {
//            System.outStream.println(">>" + filter.getValueFilterString());
        }
        seriesLook.setValueFilter(filter);

        XYSeriesLooksCollection.set(seriesID, seriesLook);
    }

    private void reset_XYSeries() {
        for (int i = 0; i < 14; i++) {
            XYSeriesLooksCollection.set(i, null);
            //Farbenliste der Serien belegen mit Standardfarben
            XYSeriesColors[i] = MoniSoftConstants.ColorTable.get(i);
        }

        XY_ySensorSelector.setSelectedIndex(0);
        // Knopfstatus jeweils anpassen
        xyToggleButton1.setSelected(true);
        xyToggleButton2.setSelected(false);
        xyToggleButton3.setSelected(false);
        xyToggleButton4.setSelected(false);
        xyToggleButton5.setSelected(false);
        xyToggleButton6.setSelected(false);
        xyToggleButton7.setSelected(false);
    }

    /**
     *
     * @param seriesID
     */
    private void loadXYPanelInfo(int seriesID, ScatterChartDescriber desc) {
        // Einstellung für aktuelle Auswahl ermitteln
        ScatterSeriesLooks seriesLook = null;
        ScatterSeriesLooks currentLook;
        boolean isKnown = false;

        if (!XYSeriesLooksCollection.isEmpty()) {
            Iterator it = XYSeriesLooksCollection.iterator();
            while (it.hasNext()) {
                currentLook = (ScatterSeriesLooks) it.next();
                if ((currentLook != null) && (currentLook.getSeriesID() == seriesID)) {
                    isKnown = true;
                    seriesLook = currentLook;
                }
            }
        }
        XY_SeriesColorButton.setBackground(XYSeriesColors[seriesID]);
        if (isKnown) {
            XY_xSensorSelector.setSelectedItem(SensorInformation.getSensorProperties(seriesLook.getDomainSensor().getSensorID()));
//            XY_AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(XY_AggSelector, seriesLook.getAggregation()));
            XY_ySensorSelector.setSelectedItem(SensorInformation.getSensorProperties(seriesLook.getSensorID()));
            XY_SeriesColorButton.setBackground(seriesLook.getPointsColor());
            XY_SeriesPowerCheck.setSelected(seriesLook.getPowerWanted());
            XY_PointSizeSelector.setSelectedIndex(seriesLook.getPointSize());
            XY_PointTypeSelector.setSelectedIndex(seriesLook.getPointType());
            XYTimeConstraintCheckBox.setSelected(seriesLook.getUseTimeConstraints());
            XYWeekDayConstraintCheckBox.setSelected(seriesLook.getUseWeekDayConstraints());
            XYstartTimeConstraint.setSelectedIndex(seriesLook.getStartTimeConstraint());
            XYendTimeConstraint.setSelectedIndex(seriesLook.getEndTimeConstraint());

            // evtl. AreaMarker setzen
            if (desc != null) {
                if (desc.getAreaMarker() != null) {
                    jCheckBox1.setSelected(true);
//                    switch (desc.getAreaMarker()) {
//                        case AnnotationsCollection.DIN_1946_2:
//                            annotationChooser.setSelectedIndex(0);
//                            break;
//                        case AnnotationsCollection.DIN_EN_ISO_7730:
//                            annotationChooser.setSelectedIndex(1);
//                            break;
//                        case AnnotationsCollection.EN_15251:
//                            annotationChooser.setSelectedIndex(2);
//                            break;
//                    }
                } else { // kein Marker vorhanden
                    jCheckBox1.setSelected(false);
                }
            }

            // Datumsbeschränkungen setzen
            XYDateConstraintCheckBox.setSelected(seriesLook.isUseDateConstraints());
            if (seriesLook.isUseDateConstraints()) {
                if (seriesLook.getStartDateConstraints() > Long.MIN_VALUE) {
                    jDateChooser1.setDate(new Date(seriesLook.getStartDateConstraints()));
                }
                if (seriesLook.getEndDateConstraints() < Long.MAX_VALUE) {
                    jDateChooser2.setDate(new Date(seriesLook.getEndDateConstraints()));
                }
            }

            // Wochentagsbeschränkungen setzten
            byte dayConstraint = seriesLook.getWeekDayConstraintCode();
            jCheckBox2.setSelected(false);
            jCheckBox3.setSelected(false);
            jCheckBox4.setSelected(false);
            jCheckBox5.setSelected(false);
            jCheckBox6.setSelected(false);
            jCheckBox7.setSelected(false);
            jCheckBox8.setSelected(false);
            if (dayConstraint - 64 >= 0) { // TODO hübscher lösen!!
                dayConstraint -= 64;
                jCheckBox8.setSelected(true);
            }
            if (dayConstraint - 32 >= 0) {
                dayConstraint -= 32;
                jCheckBox7.setSelected(true);
            }
            if (dayConstraint - 16 >= 0) {
                dayConstraint -= 16;
                jCheckBox6.setSelected(true);
            }
            if (dayConstraint - 8 >= 0) {
                dayConstraint -= 8;
                jCheckBox5.setSelected(true);
            }
            if (dayConstraint - 4 >= 0) {
                dayConstraint -= 4;
                jCheckBox4.setSelected(true);
            }
            if (dayConstraint - 2 >= 0) {
                dayConstraint -= 2;
                jCheckBox3.setSelected(true);
            }
            if (dayConstraint - 1 >= 0) {
                jCheckBox2.setSelected(true);
            }

            // Wertebeschränkungen setzen
            // alles zurücksetzen
            // AZ: Kein Zurücksetzen der ausgewählten Markierungen - MONISOFT-20
            /*
            f0Check.setSelected(false);
            f1Check.setSelected(false);
            f2Check.setSelected(false);
            f3Check.setSelected(false);
            ValueConstraintOperator0.setSelectedIndex(0);
            ValueConstraintOperator1.setSelectedIndex(0);
            ValueConstraintOperator2.setSelectedIndex(0);
            ValueConstraintOperator3.setSelectedIndex(0);
            ValueConstraintSensorSelector0.setSelectedIndex(0);
            ValueConstraintSensorSelector1.setSelectedIndex(0);
            ValueConstraintSensorSelector2.setSelectedIndex(0);
            ValueConstraintSensorSelector3.setSelectedIndex(0);
            ValueConstraintValue0.setText(null);
            ValueConstraintValue1.setText(null);
            ValueConstraintValue2.setText(null);
            ValueConstraintValue3.setText(null);
            */
            if (seriesLook.getValueFilter() != null) {
                ArrayList<ValueFilterComponent> components = seriesLook.getValueFilter().getFilterComponents();
                for (int i = 0; i < components.size(); i++) {
                    switch (i) {
                        case 0:
                            f0Check.setSelected(true);
                            ValueConstraintOperator0.setSelectedItem(components.get(i).getOperand());
                            ValueConstraintSensorSelector0.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                            ValueConstraintValue0.setText(String.valueOf(components.get(i).getValue()));
                            break;
                        case 1:
                            f1Check.setSelected(true);
                            ValueConstraintOperator1.setSelectedItem(components.get(i).getOperand());
                            ValueConstraintSensorSelector1.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                            ValueConstraintValue1.setText(String.valueOf(components.get(i).getValue()));
                            break;
                        case 2:
                            f2Check.setSelected(true);
                            ValueConstraintOperator2.setSelectedItem(components.get(i).getOperand());
                            ValueConstraintSensorSelector2.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                            ValueConstraintValue2.setText(String.valueOf(components.get(i).getValue()));
                            break;
                        case 3:
                            f3Check.setSelected(true);
                            ValueConstraintOperator3.setSelectedItem(components.get(i).getOperand());
                            ValueConstraintSensorSelector3.setSelectedItem(SensorInformation.getSensorProperties(components.get(i).getSensorID()));
                            ValueConstraintValue3.setText(String.valueOf(components.get(i).getValue()));
                            break;
                    }
                }
            }

        } else {
            // wenn der Knopf noch nicht belegt war: Grundeinstellung vornehmen
            XY_ySensorSelector.setSelectedIndex(0);   // <keine> wählen
            XY_SeriesColorButton.setBackground(XYSeriesColors[XY_ActiveSeries]);  //TODO hier Vorbelegung abhängig vom Knopf (nicht alle rot)
            XY_SeriesPowerCheck.setSelected(false);
            XY_PointSizeSelector.setSelectedIndex(2);
            XY_PointTypeSelector.setSelectedIndex(0);
            XYTimeConstraintCheckBox.setSelected(false);
            XYWeekDayConstraintCheckBox.setSelected(false);
            XYstartTimeConstraint.setSelectedIndex(8);
            XYendTimeConstraint.setSelectedIndex(17);
            XYDateConstraintCheckBox.setSelected(false);
            jDateChooser1.setDate(null);
            jDateChooser2.setDate(null);
            // alle wochentagsbeschränkungen ausschalten
            jCheckBox2.setSelected(false);
            jCheckBox3.setSelected(false);
            jCheckBox4.setSelected(false);
            jCheckBox5.setSelected(false);
            jCheckBox6.setSelected(false);
            jCheckBox7.setSelected(false);
            jCheckBox8.setSelected(false);
            // ValueFilter
            // AZ: Kein Zurücksetzen der ausgewählten Markierungen - MONISOFT-20
            /*
            f0Check.setSelected(false);
            f1Check.setSelected(false);
            f2Check.setSelected(false);
            f3Check.setSelected(false);
            ValueConstraintOperator0.setSelectedIndex(0);
            ValueConstraintOperator1.setSelectedIndex(0);
            ValueConstraintOperator2.setSelectedIndex(0);
            ValueConstraintOperator3.setSelectedIndex(0);
            ValueConstraintSensorSelector0.setSelectedIndex(0);
            ValueConstraintSensorSelector1.setSelectedIndex(0);
            ValueConstraintSensorSelector2.setSelectedIndex(0);
            ValueConstraintSensorSelector3.setSelectedIndex(0);
            ValueConstraintValue0.setText(null);
            ValueConstraintValue1.setText(null);
            ValueConstraintValue2.setText(null);
            ValueConstraintValue3.setText(null);
            */
//            XY_AggSelector.setSelectedIndex(IntervalSelectorEntry.getIndexOfIntervalSelectorEntry(XY_AggSelector, MoniSoftConstants.HOUR_INTERVAL));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        WeekdayConstraintButtons = new javax.swing.ButtonGroup();
        xySlotButtonGroup = new javax.swing.ButtonGroup();
        schnittmengeGroup = new javax.swing.ButtonGroup();
        xyTab = new javax.swing.JPanel();
        XY_xAxisPanel = new javax.swing.JPanel();
        XY_xSensorSelector = new javax.swing.JComboBox();
        jLabel20 = new javax.swing.JLabel();
        XY_AggSelector = new javax.swing.JComboBox();
        jLabel21 = new javax.swing.JLabel();
        XY_DomainPowerCheck = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        xyToggleButton1 = new javax.swing.JToggleButton();
        xyToggleButton2 = new javax.swing.JToggleButton();
        xyToggleButton3 = new javax.swing.JToggleButton();
        xyToggleButton4 = new javax.swing.JToggleButton();
        xyToggleButton5 = new javax.swing.JToggleButton();
        xyToggleButton6 = new javax.swing.JToggleButton();
        xyToggleButton7 = new javax.swing.JToggleButton();
        XYSeriesReset = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        XY_ySensorSelector = new javax.swing.JComboBox();
        jLabel19 = new javax.swing.JLabel();
        XY_SeriesColorButton = new javax.swing.JButton();
        XY_PointTypeSelector = new javax.swing.JComboBox();
        XY_PointSizeSelector = new javax.swing.JComboBox();
        XY_SeriesPowerCheck = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        ValueConstraintSensorSelector0 = new javax.swing.JComboBox();
        ValueConstraintOperator0 = new javax.swing.JComboBox();
        ValueConstraintValue0 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f0Check = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        f1Check = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector1 = new javax.swing.JComboBox();
        ValueConstraintOperator1 = new javax.swing.JComboBox();
        ValueConstraintValue1 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f2Check = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector2 = new javax.swing.JComboBox();
        ValueConstraintOperator2 = new javax.swing.JComboBox();
        ValueConstraintValue2 = new de.jmonitoring.utils.textfields.DoubleTextField();
        f3Check = new javax.swing.JCheckBox();
        ValueConstraintSensorSelector3 = new javax.swing.JComboBox();
        ValueConstraintOperator3 = new javax.swing.JComboBox();
        ValueConstraintValue3 = new de.jmonitoring.utils.textfields.DoubleTextField();
        vereinigungsmengeButton = new javax.swing.JRadioButton();
        schnittmengeButton = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        XYTimeConstraintCheckBox = new javax.swing.JCheckBox();
        XYstartTimeConstraint = new javax.swing.JComboBox();
        XYendTimeConstraint = new javax.swing.JComboBox();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JSeparator();
        XYWeekDayConstraintCheckBox = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jCheckBox8 = new javax.swing.JCheckBox();
        XYDateConstraintCheckBox = new javax.swing.JCheckBox();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jSeparator14 = new javax.swing.JSeparator();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jCheckBox1 = new javax.swing.JCheckBox();
        annotationChooser = new javax.swing.JComboBox();
        editAnnotationButton = new javax.swing.JButton();
        XY_drawChartButton = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(200, 2147483647));
        setMinimumSize(new java.awt.Dimension(200, 36));
        setPreferredSize(new java.awt.Dimension(200, 450));
        setLayout(new java.awt.BorderLayout());

        xyTab.setDoubleBuffered(false);
        xyTab.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyTab.setMaximumSize(new java.awt.Dimension(156, 32767));
        xyTab.setPreferredSize(new java.awt.Dimension(156, 450));
        xyTab.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        xyTab.add(xyDateChooserPanel);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle"); // NOI18N
        XY_xAxisPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("MoniSoft.XY_xAxisPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        XY_xAxisPanel.setDoubleBuffered(false);
        XY_xAxisPanel.setFont(new java.awt.Font("Dialog", 0, 89)); // NOI18N
        XY_xAxisPanel.setPreferredSize(new java.awt.Dimension(467, 63));

        XY_xSensorSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_xSensorSelector.setMaximumRowCount(20);
        XY_xSensorSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        XY_xSensorSelector.setPreferredSize(new java.awt.Dimension(126, 17));
        XY_xSensorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XY_xSensorSelectorActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel20.setText(bundle.getString("MoniSoft.jLabel20.text")); // NOI18N

        XY_AggSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_AggSelector.setMaximumRowCount(15);
        XY_AggSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Originalmesswerte", "Stundenwerte", "Tageswerte", "Wochenwerte" }));
        XY_AggSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        XY_AggSelector.setPreferredSize(new java.awt.Dimension(126, 17));
        XY_AggSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XY_AggSelectorActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel21.setText(bundle.getString("MoniSoft.jLabel21.text")); // NOI18N

        XY_DomainPowerCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XY_DomainPowerCheck.setText(bundle.getString("MoniSoft.XY_DomainPowerCheck.text")); // NOI18N
        XY_DomainPowerCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        XY_DomainPowerCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        DropTarget dt_XY_xSensorSelector = new DropTarget(XY_xSensorSelector,new SensorSelectorDropListener());

        javax.swing.GroupLayout XY_xAxisPanelLayout = new javax.swing.GroupLayout(XY_xAxisPanel);
        XY_xAxisPanel.setLayout(XY_xAxisPanelLayout);
        XY_xAxisPanelLayout.setHorizontalGroup(
            XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(XY_xAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(XY_xSensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(XY_xAxisPanelLayout.createSequentialGroup()
                        .addComponent(XY_AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                        .addComponent(XY_DomainPowerCheck)))
                .addContainerGap())
        );
        XY_xAxisPanelLayout.setVerticalGroup(
            XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(XY_xAxisPanelLayout.createSequentialGroup()
                .addGroup(XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(XY_xSensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(XY_xAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(XY_AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(XY_DomainPowerCheck)))
        );

        xyTab.add(XY_xAxisPanel);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("MoniSoft.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel7.setPreferredSize(new java.awt.Dimension(467, 440));

        xySlotButtonGroup.add(xyToggleButton1);
        xyToggleButton1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton1.setSelected(true);
        xyToggleButton1.setText(bundle.getString("MoniSoft.xyToggleButton1.text")); // NOI18N
        xyToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton1ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton2);
        xyToggleButton2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton2.setText(bundle.getString("MoniSoft.xyToggleButton2.text")); // NOI18N
        xyToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton2ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton3);
        xyToggleButton3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton3.setText(bundle.getString("MoniSoft.xyToggleButton3.text")); // NOI18N
        xyToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton3ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton4);
        xyToggleButton4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton4.setText(bundle.getString("MoniSoft.xyToggleButton4.text")); // NOI18N
        xyToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton4ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton5);
        xyToggleButton5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton5.setText(bundle.getString("MoniSoft.xyToggleButton5.text")); // NOI18N
        xyToggleButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton5ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton6);
        xyToggleButton6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton6.setText(bundle.getString("MoniSoft.xyToggleButton6.text")); // NOI18N
        xyToggleButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton6ActionPerformed(evt);
            }
        });

        xySlotButtonGroup.add(xyToggleButton7);
        xyToggleButton7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xyToggleButton7.setText(bundle.getString("MoniSoft.xyToggleButton7.text")); // NOI18N
        xyToggleButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xyToggleButton7ActionPerformed(evt);
            }
        });

        XYSeriesReset.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYSeriesReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/eraser.png"))); // NOI18N
        XYSeriesReset.setMargin(new java.awt.Insets(2, 2, 2, 2));
        XYSeriesReset.setMaximumSize(new java.awt.Dimension(21, 21));
        XYSeriesReset.setMinimumSize(new java.awt.Dimension(21, 21));
        XYSeriesReset.setPreferredSize(new java.awt.Dimension(21, 21));
        XYSeriesReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XYSeriesResetActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        XY_ySensorSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_ySensorSelector.setMaximumRowCount(20);
        XY_ySensorSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        XY_ySensorSelector.setPreferredSize(new java.awt.Dimension(126, 17));
        XY_ySensorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XY_ySensorSelectorActionPerformed(evt);
            }
        });

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel19.setText(bundle.getString("MoniSoft.jLabel19.text")); // NOI18N

        XY_SeriesColorButton.setBackground(new java.awt.Color(255, 0, 51));
        XY_SeriesColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        XY_SeriesColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        XY_SeriesColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XY_SeriesColorButtonActionPerformed(evt);
            }
        });

        XY_PointTypeSelector.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XY_PointTypeSelector.setMinimumSize(new java.awt.Dimension(63, 13));
        XY_PointTypeSelector.setPreferredSize(new java.awt.Dimension(63, 13));

        XY_PointSizeSelector.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XY_PointSizeSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }));
        XY_PointSizeSelector.setSelectedIndex(2);
        XY_PointSizeSelector.setMinimumSize(new java.awt.Dimension(63, 13));
        XY_PointSizeSelector.setPreferredSize(new java.awt.Dimension(63, 13));

        XY_SeriesPowerCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XY_SeriesPowerCheck.setText(bundle.getString("MoniSoft.XY_SeriesPowerCheck.text")); // NOI18N
        XY_SeriesPowerCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        XY_SeriesPowerCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        DropTarget dt_XY_ySensorSelector = new DropTarget(XY_ySensorSelector,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(20, 20, 20)
                        .addComponent(XY_ySensorSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(XY_SeriesPowerCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(XY_SeriesColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(XY_PointTypeSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(XY_PointSizeSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(XY_ySensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(XY_PointSizeSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(XY_PointTypeSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(XY_SeriesColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(XY_SeriesPowerCheck))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel6.setPreferredSize(new java.awt.Dimension(400, 130));

        ValueConstraintSensorSelector0.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector0.setMaximumRowCount(15);
        ValueConstraintSensorSelector0.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector0.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator0.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator0.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=", "" }));
        ValueConstraintOperator0.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator0.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator0.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue0.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f0Check.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f0Check.setMinimumSize(new java.awt.Dimension(21, 17));
        f0Check.setPreferredSize(new java.awt.Dimension(21, 17));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel1.setText(bundle.getString("MoniSoft.jLabel1.text")); // NOI18N

        f1Check.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f1Check.setMinimumSize(new java.awt.Dimension(21, 17));
        f1Check.setPreferredSize(new java.awt.Dimension(21, 17));

        ValueConstraintSensorSelector1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector1.setMaximumRowCount(15);
        ValueConstraintSensorSelector1.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector1.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator1.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator1.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator1.setPreferredSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ValueConstraintOperator1ActionPerformed(evt);
            }
        });

        ValueConstraintValue1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f2Check.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f2Check.setMinimumSize(new java.awt.Dimension(21, 17));
        f2Check.setPreferredSize(new java.awt.Dimension(21, 17));

        ValueConstraintSensorSelector2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector2.setMaximumRowCount(15);
        ValueConstraintSensorSelector2.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector2.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator2.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator2.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator2.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        f3Check.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        f3Check.setMinimumSize(new java.awt.Dimension(21, 17));
        f3Check.setPreferredSize(new java.awt.Dimension(21, 17));
        f3Check.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                f3CheckActionPerformed(evt);
            }
        });

        ValueConstraintSensorSelector3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ValueConstraintSensorSelector3.setMaximumRowCount(15);
        ValueConstraintSensorSelector3.setMinimumSize(new java.awt.Dimension(126, 17));
        ValueConstraintSensorSelector3.setPreferredSize(new java.awt.Dimension(126, 17));

        ValueConstraintOperator3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        ValueConstraintOperator3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "==", "!=", "<", ">", ">=", "<=" }));
        ValueConstraintOperator3.setMaximumSize(new java.awt.Dimension(32767, 17));
        ValueConstraintOperator3.setMinimumSize(new java.awt.Dimension(72, 17));
        ValueConstraintOperator3.setPreferredSize(new java.awt.Dimension(72, 17));

        ValueConstraintValue3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        schnittmengeGroup.add(vereinigungsmengeButton);
        vereinigungsmengeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        vereinigungsmengeButton.setText(bundle.getString("MoniSoft.union.text")); // NOI18N

        schnittmengeGroup.add(schnittmengeButton);
        schnittmengeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        schnittmengeButton.setSelected(true);
        schnittmengeButton.setText(bundle.getString("MoniSoft.intersection.text")); // NOI18N

        DropTarget dt_ValueConstraintSensorSelector0 = new DropTarget(ValueConstraintSensorSelector0,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector1 = new DropTarget(ValueConstraintSensorSelector1,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector2 = new DropTarget(ValueConstraintSensorSelector2,new SensorSelectorDropListener());
        DropTarget dt_ValueConstraintSensorSelector3 = new DropTarget(ValueConstraintSensorSelector3,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(f0Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintSensorSelector0, 0, 254, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintOperator0, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintValue0, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel1)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(f1Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintSensorSelector1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintOperator1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintValue1, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(f2Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintSensorSelector2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintOperator2, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintValue2, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addComponent(f3Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintSensorSelector3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintOperator3, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ValueConstraintValue3, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(16, 16, 16))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(vereinigungsmengeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(schnittmengeButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f0Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator0, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector0, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f1Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f2Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ValueConstraintValue3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(f3Check, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ValueConstraintOperator3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ValueConstraintSensorSelector3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vereinigungsmengeButton)
                    .addComponent(schnittmengeButton)))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        XYTimeConstraintCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYTimeConstraintCheckBox.setText(bundle.getString("MoniSoft.XYTimeConstraintCheckBox.text")); // NOI18N

        XYstartTimeConstraint.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYstartTimeConstraint.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        XYstartTimeConstraint.setSelectedIndex(8);
        XYstartTimeConstraint.setMinimumSize(new java.awt.Dimension(61, 17));
        XYstartTimeConstraint.setPreferredSize(new java.awt.Dimension(61, 17));

        XYendTimeConstraint.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYendTimeConstraint.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        XYendTimeConstraint.setSelectedIndex(17);
        XYendTimeConstraint.setMinimumSize(new java.awt.Dimension(61, 17));
        XYendTimeConstraint.setPreferredSize(new java.awt.Dimension(61, 17));

        jLabel52.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel52.setText(bundle.getString("MoniSoft.jLabel52.text")); // NOI18N

        jLabel53.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel53.setText(bundle.getString("MoniSoft.jLabel53.text")); // NOI18N

        XYWeekDayConstraintCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYWeekDayConstraintCheckBox.setText(bundle.getString("MoniSoft.XYWeekDayConstraintCheckBox.text")); // NOI18N

        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox2.setText(bundle.getString("MoniSoft.jCheckBox2.text")); // NOI18N

        jCheckBox3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox3.setText(bundle.getString("MoniSoft.jCheckBox3.text")); // NOI18N

        jCheckBox4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox4.setText(bundle.getString("MoniSoft.jCheckBox4.text")); // NOI18N

        jCheckBox5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox5.setText(bundle.getString("MoniSoft.jCheckBox5.text")); // NOI18N

        jCheckBox6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox6.setText(bundle.getString("MoniSoft.jCheckBox6.text")); // NOI18N

        jCheckBox7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox7.setText(bundle.getString("MoniSoft.jCheckBox7.text")); // NOI18N

        jCheckBox8.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox8.setText(bundle.getString("MoniSoft.jCheckBox8.text")); // NOI18N

        XYDateConstraintCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XYDateConstraintCheckBox.setText(bundle.getString("MoniSoft.XYDateConstraintCheckBox.text")); // NOI18N

        jDateChooser1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jDateChooser1.setPreferredSize(new java.awt.Dimension(90, 17));

        jDateChooser2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jDateChooser2.setPreferredSize(new java.awt.Dimension(90, 17));

        jLabel22.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel22.setText(bundle.getString("MoniSoft.jLabel22.text")); // NOI18N

        jLabel23.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel23.setText(bundle.getString("MoniSoft.jLabel23.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 43, Short.MAX_VALUE)
                        .addComponent(jCheckBox2)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox3)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox4)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox5)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox6)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox7)
                        .addGap(18, 18, 18)
                        .addComponent(jCheckBox8)
                        .addGap(7, 7, 7))
                    .addComponent(jSeparator14)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(XYTimeConstraintCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(XYstartTimeConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel52, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(XYendTimeConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel53, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jSeparator4)
                    .addComponent(XYWeekDayConstraintCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(XYDateConstraintCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29)
                                .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(XYTimeConstraintCheckBox)
                    .addComponent(jLabel53)
                    .addComponent(XYendTimeConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel52)
                    .addComponent(XYstartTimeConstraint, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(XYWeekDayConstraintCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(jCheckBox3)
                    .addComponent(jCheckBox4)
                    .addComponent(jCheckBox5)
                    .addComponent(jCheckBox6)
                    .addComponent(jCheckBox7)
                    .addComponent(jCheckBox8))
                .addGap(11, 11, 11)
                .addComponent(jSeparator14, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(XYDateConstraintCheckBox)
                        .addComponent(jLabel22)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel23))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(xyToggleButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(xyToggleButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(XYSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(xyToggleButton1)
                        .addComponent(xyToggleButton2)
                        .addComponent(xyToggleButton3)
                        .addComponent(xyToggleButton4))
                    .addComponent(xyToggleButton5)
                    .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(xyToggleButton6)
                        .addComponent(xyToggleButton7)
                        .addComponent(XYSeriesReset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        xyTab.add(jPanel7);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("MoniSoft.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel8.setPreferredSize(new java.awt.Dimension(467, 74));

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jCheckBox1.setText(bundle.getString("MoniSoft.jCheckBox1.text")); // NOI18N
        jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));

        annotationChooser.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        annotationChooser.setMinimumSize(new java.awt.Dimension(56, 17));
        annotationChooser.setPreferredSize(new java.awt.Dimension(56, 17));

        editAnnotationButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        editAnnotationButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/pencil.png"))); // NOI18N
        editAnnotationButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        editAnnotationButton.setMaximumSize(new java.awt.Dimension(21, 21));
        editAnnotationButton.setMinimumSize(new java.awt.Dimension(21, 21));
        editAnnotationButton.setPreferredSize(new java.awt.Dimension(21, 21));
        editAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editAnnotationButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(editAnnotationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(annotationChooser, javax.swing.GroupLayout.Alignment.TRAILING, 0, 431, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBox1)
                    .addComponent(editAnnotationButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(annotationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        xyTab.add(jPanel8);

        XY_drawChartButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_drawChartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart--pencil.png"))); // NOI18N
        XY_drawChartButton.setText(bundle.getString("MoniSoft.XY_drawChartButton.text")); // NOI18N
        XY_drawChartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                XY_drawChartButtonActionPerformed(evt);
            }
        });
        xyTab.add(XY_drawChartButton);

        add(xyTab, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void XY_xSensorSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XY_xSensorSelectorActionPerformed
        if (!MoniSoft.getInstance().isGUIActive()) {
            return;
        }

        SensorProperties props = (SensorProperties) XY_xSensorSelector.getSelectedItem();
        if (props != null && !props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
            XY_DomainPowerCheck.setEnabled(true);
        } else {
            XY_DomainPowerCheck.setEnabled(false);
            XY_DomainPowerCheck.setSelected(false);
        }               
    }//GEN-LAST:event_XY_xSensorSelectorActionPerformed

    private void XY_AggSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XY_AggSelectorActionPerformed
        setScatterAggregationToAllSlots();
    }//GEN-LAST:event_XY_AggSelectorActionPerformed

    private void xyToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton1ActionPerformed
        xyToggle(1);
    }//GEN-LAST:event_xyToggleButton1ActionPerformed

    private void xyToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton2ActionPerformed
        xyToggle(2);
    }//GEN-LAST:event_xyToggleButton2ActionPerformed

    private void xyToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton3ActionPerformed
        xyToggle(3);
    }//GEN-LAST:event_xyToggleButton3ActionPerformed

    private void xyToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton4ActionPerformed
        xyToggle(4);
    }//GEN-LAST:event_xyToggleButton4ActionPerformed

    private void xyToggleButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton5ActionPerformed
        xyToggle(5);
    }//GEN-LAST:event_xyToggleButton5ActionPerformed

    private void xyToggleButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton6ActionPerformed
        xyToggle(6);
    }//GEN-LAST:event_xyToggleButton6ActionPerformed

    private void xyToggleButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xyToggleButton7ActionPerformed
        xyToggle(7);
    }//GEN-LAST:event_xyToggleButton7ActionPerformed

    private void XYSeriesResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XYSeriesResetActionPerformed
        reset_XYSeries();
    }//GEN-LAST:event_XYSeriesResetActionPerformed

    private void XY_ySensorSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XY_ySensorSelectorActionPerformed
        if (!MoniSoft.getInstance().isGUIActive()) {
            return;
        }

        SensorProperties props = (SensorProperties) XY_ySensorSelector.getSelectedItem();
        if (props != null && !props.getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED) && (props.isCounter() || props.isUsage())) {
            XY_SeriesPowerCheck.setEnabled(true);
        } else {
            XY_SeriesPowerCheck.setEnabled(false);
            XY_SeriesPowerCheck.setSelected(false);
        }
    }//GEN-LAST:event_XY_ySensorSelectorActionPerformed

    private void XY_SeriesColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XY_SeriesColorButtonActionPerformed
        Color col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SELECT_COLOR"), XY_SeriesColorButton.getBackground());
        if (col != null) {
            XY_SeriesColorButton.setBackground(col);
            XYSeriesColors[XY_ActiveSeries] = col;
        }
    }//GEN-LAST:event_XY_SeriesColorButtonActionPerformed

    private void ValueConstraintOperator1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ValueConstraintOperator1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ValueConstraintOperator1ActionPerformed

    private void f3CheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_f3CheckActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_f3CheckActionPerformed

    private void XY_drawChartButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_XY_drawChartButtonActionPerformed
        // den Stand des aktuelle Knopfes holen
        saveXYPanelInfo(XY_ActiveSeries);
        // get state of aggregation chooser and set to all sereies
        setScatterAggregationToAllSlots();

        if (!new DateEntriesChecker().hasValidEntries(xyDateChooserPanel.getStartDateChooser(), xyDateChooserPanel.getEndDateChooser(), XYSeriesLooksCollection)) {
            return;
        }

        XY_drawChartButton.setEnabled(false);

        final String areaMarker = getAreaMarker();
        final DateInterval interval = new DateInterval(xyDateChooserPanel.getStartDate(), xyDateChooserPanel.getEndDate());
        gui().drawUsingDescriber(new XYDescriberFactory(areaMarker, interval, XYSeriesLooksCollection, XY_DomainPowerCheck.isSelected()), interval, XYSeriesLooksCollection);
        XY_drawChartButton.setEnabled(true);        
        
        // AZ: nach dem Durchlauf CounterChangeErrorDialog auf false setzen - MONISOFT-8
        Interpolator.saveCancelDecision = false; 
        Interpolator.chartType = Interpolator.XYPLOT_TAB;
    }//GEN-LAST:event_XY_drawChartButtonActionPerformed

    private void editAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editAnnotationButtonActionPerformed
        AnnotationDesigner f = new AnnotationDesigner();
        this.gui().getDesktop().add(f);
        f.setVisible(true);
    }//GEN-LAST:event_editAnnotationButtonActionPerformed

    private static final class XYDescriberFactory implements DescriberFactory {

        private final String areaMarker;
        private final DateInterval interval;
        private final ArrayList<ScatterSeriesLooks> xYSeriesLooksCollection;
        private final boolean domainPower;

        private XYDescriberFactory(String areaMarker, DateInterval interval, ArrayList<ScatterSeriesLooks> XYSeriesLooksCollection, boolean domainPower) {
            super();
            this.areaMarker = areaMarker;
            this.interval = interval;
            this.xYSeriesLooksCollection = XYSeriesLooksCollection;
            this.domainPower = domainPower;
        }

        @Override
        public ChartDescriber createChartDescriber() {
            return new ScatterChartDescriber("Scatterplot", interval, xYSeriesLooksCollection, domainPower, areaMarker);
        }
    }

    private String getAreaMarker() {
        String areaMarker = null;
        if (jCheckBox1.isSelected()) {
            areaMarker = (String) annotationChooser.getSelectedItem();
        }
        return areaMarker;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox ValueConstraintOperator0;
    private javax.swing.JComboBox ValueConstraintOperator1;
    private javax.swing.JComboBox ValueConstraintOperator2;
    private javax.swing.JComboBox ValueConstraintOperator3;
    private javax.swing.JComboBox ValueConstraintSensorSelector0;
    private javax.swing.JComboBox ValueConstraintSensorSelector1;
    private javax.swing.JComboBox ValueConstraintSensorSelector2;
    private javax.swing.JComboBox ValueConstraintSensorSelector3;
    private javax.swing.JTextField ValueConstraintValue0;
    private javax.swing.JTextField ValueConstraintValue1;
    private javax.swing.JTextField ValueConstraintValue2;
    private javax.swing.JTextField ValueConstraintValue3;
    private javax.swing.ButtonGroup WeekdayConstraintButtons;
    private javax.swing.JCheckBox XYDateConstraintCheckBox;
    private javax.swing.JButton XYSeriesReset;
    private javax.swing.JCheckBox XYTimeConstraintCheckBox;
    private javax.swing.JCheckBox XYWeekDayConstraintCheckBox;
    static javax.swing.JComboBox XY_AggSelector;
    private javax.swing.JCheckBox XY_DomainPowerCheck;
    private javax.swing.JComboBox XY_PointSizeSelector;
    private javax.swing.JComboBox XY_PointTypeSelector;
    static javax.swing.JButton XY_SeriesColorButton;
    private javax.swing.JCheckBox XY_SeriesPowerCheck;
    private javax.swing.JButton XY_drawChartButton;
    private javax.swing.JPanel XY_xAxisPanel;
    static javax.swing.JComboBox XY_xSensorSelector;
    static javax.swing.JComboBox XY_ySensorSelector;
    private javax.swing.JComboBox XYendTimeConstraint;
    private javax.swing.JComboBox XYstartTimeConstraint;
    private javax.swing.JComboBox annotationChooser;
    private javax.swing.JButton editAnnotationButton;
    private javax.swing.JCheckBox f0Check;
    private javax.swing.JCheckBox f1Check;
    private javax.swing.JCheckBox f2Check;
    private javax.swing.JCheckBox f3Check;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBox8;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JSeparator jSeparator14;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JRadioButton schnittmengeButton;
    private javax.swing.ButtonGroup schnittmengeGroup;
    private javax.swing.JRadioButton vereinigungsmengeButton;
    private javax.swing.ButtonGroup xySlotButtonGroup;
    private javax.swing.JPanel xyTab;
    private javax.swing.JToggleButton xyToggleButton1;
    private javax.swing.JToggleButton xyToggleButton2;
    private javax.swing.JToggleButton xyToggleButton3;
    private javax.swing.JToggleButton xyToggleButton4;
    private javax.swing.JToggleButton xyToggleButton5;
    private javax.swing.JToggleButton xyToggleButton6;
    private javax.swing.JToggleButton xyToggleButton7;
    // End of variables declaration//GEN-END:variables
}
