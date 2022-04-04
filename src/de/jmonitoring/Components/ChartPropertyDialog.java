/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ChartPropertyDialog.java
 *
 * Created on 27.08.2009, 16:55:59
 */
package de.jmonitoring.Components;

import com.toedter.calendar.JDateChooser;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.utils.DateBandAxis;
import de.jmonitoring.utils.textfields.IntegerTextField;
import de.jmonitoring.utils.textfields.DoubleTextField;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.border.LineBorder;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Week;
import org.jfree.data.time.Year;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

/**
 *
 * @author togro
 */
public class ChartPropertyDialog extends javax.swing.JDialog {

    public static final int SCALE = 0;
    public static final int TITLE = 1;
    public static final int TIMEAXIS = 2;
    public static final int DATASERIES = 3;
    public static final int GRID = 4;
    public static final int INFO = 5;
    private XYPlot xyplot = null;
    private CategoryPlot categoryPlot = null;
    private JFreeChart chart;
    private CombinedDomainXYPlot cbPlot = null;
    private CtrlChartPanel panel = null;
    private DecimalFormat format = new DecimalFormat("#0.###");
    HashMap<String, XYDataset> dataSetMap = null;
    HashMap<String, XYItemRenderer> rendererMap = new HashMap<String, XYItemRenderer>();
	private final MainApplication gui;
//    HashMap<String, Color> rendererMap = new HashMap<String, Color>();

    /**
     * Creates new form ChartPropertyDialog
     */
    public ChartPropertyDialog(MainApplication gui, boolean modal, JFreeChart c, CtrlChartPanel p) {
        super(gui.getMainFrame(), modal);
		this.gui = gui;
        initComponents();

        jDateChooser1.getJCalendar().setTodayButtonVisible(true);
        jDateChooser2.getJCalendar().setTodayButtonVisible(true);

        jList1.setCellRenderer(new ColorCellRenderer());

        // den plot holen für den das Dialogfeld angezeigt wird
        chart = c;
        panel = p;
        if (chart.getPlot() instanceof org.jfree.chart.plot.CombinedDomainXYPlot) {
            cbPlot = (CombinedDomainXYPlot) chart.getPlot();
            List plotList = cbPlot.getSubplots();
            xyplot = (XYPlot) plotList.get(0); // der erste plot des CombinedPlots
        } else if (chart.getPlot() instanceof org.jfree.chart.plot.XYPlot) {
            xyplot = (XYPlot) chart.getPlot();
        } else if (chart.getPlot() instanceof org.jfree.chart.plot.CategoryPlot) {
            categoryPlot = (CategoryPlot) chart.getPlot();
        } else {
            System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.IGNORED") + "  " + chart.getPlot().getClass().toString());
        }

        tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.WERTEACHSE"), scalePanel);
        tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.BESCHRIFTUNG"), titlePanel);

        selectAndAddDomainPanel();

        tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.DARSTELLUNGSREIHENFOLGE"), dataSetOrderPanel);

        // Das GridPanel ist abhängig von der Achse
        selectAndAddGridPanel();
        tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.GRAFIKINFO"), infoPanel);

        setScalePanel();
        setTitlePanel();
        setGridPanel();
        setDomainAxisPanel();
        setSeriesOrderPanel();
        setInfoPanel();
    }

    private void selectAndAddDomainPanel() {
        if (xyplot != null) {
            ValueAxis axis = xyplot.getDomainAxis();
            if (axis instanceof DateAxis || axis instanceof DateBandAxis || axis instanceof PeriodAxis) {
                tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.ZEITACHSE"), timeAxisPanel);
            } else {
                tabPanel.add(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.DOMÄNENACHSE"), domainPanel);
            }
        }
    }

    private void selectAndAddGridPanel() {
        int index = tabPanel.indexOfTab(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.GITTERNETZ"));

        if (index != -1) {
            tabPanel.remove(index);
        } else {
            index = tabPanel.getTabCount();
        }

        gridPanelCategory.setEnabled(false);
        gridPanelDate.setEnabled(false);
        gridPanelNumber.setEnabled(false);

        if (xyplot != null && xyplot.getDomainAxis() instanceof PeriodAxis) {
            gridPanelDate.setEnabled(true);
            tabPanel.add(gridPanelDate, index);
        } else if (xyplot != null && xyplot.getDomainAxis() instanceof ValueAxis) {
            gridPanelNumber.setEnabled(true);
            tabPanel.add(gridPanelNumber, index);
        } else if (categoryPlot != null) {
            gridPanelCategory.setEnabled(true);
            tabPanel.add(gridPanelCategory, index);
        }

        tabPanel.setTitleAt(index, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ChartPropertyDialog.GITTERNETZ"));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        gridPanelNumber = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        useRangeGridNumberCheckBox = new javax.swing.JCheckBox();
        useDomainGridNumberCheckBox = new javax.swing.JCheckBox();
        jLabel21 = new javax.swing.JLabel();
        jLabel22 = new javax.swing.JLabel();
        numberRangeIntervalTextField = new DoubleTextField();
        numberDomainIntervalTextField = new DoubleTextField();
        scalePanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        y1AutoCheckBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        y1Min = new de.jmonitoring.utils.textfields.DoubleTextField();
        y1Max = new de.jmonitoring.utils.textfields.DoubleTextField();
        jLabel1 = new javax.swing.JLabel();
        y1Interval = new de.jmonitoring.utils.textfields.DoubleTextField();
        jPanel4 = new javax.swing.JPanel();
        y2AutoCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        y2Min = new de.jmonitoring.utils.textfields.DoubleTextField();
        y2Max = new de.jmonitoring.utils.textfields.DoubleTextField();
        jLabel6 = new javax.swing.JLabel();
        y2Interval = new de.jmonitoring.utils.textfields.DoubleTextField();
        titlePanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        mainTitle = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        subTitle1 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        subTitle2 = new javax.swing.JTextField();
        fontSizeTextFieldMain = new IntegerTextField();
        fontSizeTextFieldSub1 = new IntegerTextField();
        fontSizeTextFieldSub2 = new IntegerTextField();
        jLabel31 = new javax.swing.JLabel();
        mainFontStyle = new javax.swing.JComboBox();
        jLabel33 = new javax.swing.JLabel();
        sub1FontStyle = new javax.swing.JComboBox();
        sub2FontStyle = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        xAxis = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        y1Axis = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        y2Axis = new javax.swing.JTextField();
        fontSizeTextFieldxAxis = new IntegerTextField();
        fontSizeTextFieldLYAxis = new IntegerTextField();
        fontSizeTextFieldRYAxis = new IntegerTextField();
        jLabel32 = new javax.swing.JLabel();
        xAxisFontStyle = new javax.swing.JComboBox();
        leftYAxisFontStyle = new javax.swing.JComboBox();
        rightYAxisFontStyle = new javax.swing.JComboBox();
        jLabel34 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        timeAxisPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        simpleAxisRadioButton = new javax.swing.JRadioButton();
        simpleIntervalTextField = new IntegerTextField();
        jLabel13 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        dateBandRadioButton = new javax.swing.JRadioButton();
        useMinutes = new javax.swing.JCheckBox();
        useHours = new javax.swing.JCheckBox();
        useDays = new javax.swing.JCheckBox();
        useMonthYear = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jPanel10 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        fontSizeTextField = new IntegerTextField();
        dataSetOrderPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jList1.setDragEnabled(true);
        jList1.setDropMode(DropMode.INSERT);
        //jList1.setPrototypeCellValue("WWWWWWWWWWWWWWWWWW");
        jList1.setTransferHandler(new ListMoveTransferHandler());
        jLabel17 = new javax.swing.JLabel();
        gridPanelDate = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        useRangeGridCheckBox = new javax.swing.JCheckBox();
        useGridDateCheckBox = new javax.swing.JCheckBox();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        intervalSelectorComboBox = new javax.swing.JComboBox();
        rangeIntervalTextField = new DoubleTextField();
        refreshButton = new javax.swing.JButton();
        infoPanel = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        widthTextField = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        heightTextField = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        jPanel16 = new javax.swing.JPanel();
        showLegendCheckBox = new javax.swing.JCheckBox();
        showChartRemarksCheckBox = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        useAntiAliasingCheckBox = new javax.swing.JCheckBox();
        gridPanelCategory = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jCheckBox11 = new javax.swing.JCheckBox();
        jCheckBox12 = new javax.swing.JCheckBox();
        jLabel24 = new javax.swing.JLabel();
        jTextField4 = new DoubleTextField();
        domainPanel = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        domainAutoCheckBox = new javax.swing.JCheckBox();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        domainMin = new de.jmonitoring.utils.textfields.DoubleTextField();
        domainMax = new de.jmonitoring.utils.textfields.DoubleTextField();
        jLabel30 = new javax.swing.JLabel();
        domainInterval = new de.jmonitoring.utils.textfields.DoubleTextField();
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        tabPanel = new javax.swing.JTabbedPane();

        gridPanelNumber.setEnabled(false);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel12.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        useRangeGridNumberCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useRangeGridNumberCheckBox.setText(bundle.getString("ChartPropertyDialog.useRangeGridNumberCheckBox.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelNumber, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), useRangeGridNumberCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useRangeGridNumberCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useRangeGridNumberCheckBoxActionPerformed(evt);
            }
        });

        useDomainGridNumberCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useDomainGridNumberCheckBox.setText(bundle.getString("ChartPropertyDialog.useDomainGridNumberCheckBox.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelNumber, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), useDomainGridNumberCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useDomainGridNumberCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDomainGridNumberCheckBoxActionPerformed(evt);
            }
        });

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel21.setText(bundle.getString("ChartPropertyDialog.jLabel21.text")); // NOI18N

        jLabel22.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel22.setText(bundle.getString("ChartPropertyDialog.jLabel22.text")); // NOI18N

        numberRangeIntervalTextField.setText(bundle.getString("ChartPropertyDialog.numberRangeIntervalTextField.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelNumber, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), numberRangeIntervalTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        numberRangeIntervalTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberRangeIntervalTextFieldActionPerformed(evt);
            }
        });

        numberDomainIntervalTextField.setText(bundle.getString("ChartPropertyDialog.numberDomainIntervalTextField.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelNumber, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), numberDomainIntervalTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        numberDomainIntervalTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numberDomainIntervalTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(useRangeGridNumberCheckBox)
                        .addGap(51, 51, 51)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberRangeIntervalTextField))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(useDomainGridNumberCheckBox)
                        .addGap(51, 51, 51)
                        .addComponent(jLabel21)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numberDomainIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(112, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useDomainGridNumberCheckBox)
                    .addComponent(jLabel21)
                    .addComponent(numberDomainIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useRangeGridNumberCheckBox)
                    .addComponent(jLabel22)
                    .addComponent(numberRangeIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridPanelNumberLayout = new javax.swing.GroupLayout(gridPanelNumber);
        gridPanelNumber.setLayout(gridPanelNumberLayout);
        gridPanelNumberLayout.setHorizontalGroup(
            gridPanelNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelNumberLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gridPanelNumberLayout.setVerticalGroup(
            gridPanelNumberLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelNumberLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(199, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y1AutoCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1AutoCheckBox.setText(bundle.getString("ChartPropertyDialog.y1AutoCheckBox.text")); // NOI18N
        y1AutoCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1AutoCheckBoxActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("ChartPropertyDialog.jLabel2.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle.getString("ChartPropertyDialog.jLabel3.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y1AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y1Min, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y1Min.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1MinActionPerformed(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y1AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y1Max, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y1Max.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1MaxActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("ChartPropertyDialog.jLabel1.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y1AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y1Interval, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y1Interval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1IntervalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(y1AutoCheckBox)
                .addGap(46, 46, 46)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(y1Max, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y1Interval, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(y1Min, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(228, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1AutoCheckBox)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(y1Min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(y1Interval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(y1Max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        y2AutoCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2AutoCheckBox.setText(bundle.getString("ChartPropertyDialog.y2AutoCheckBox.text")); // NOI18N
        y2AutoCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2AutoCheckBoxActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("ChartPropertyDialog.jLabel4.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("ChartPropertyDialog.jLabel5.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y2AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y2Min, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y2Min.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2MinActionPerformed(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y2AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y2Max, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y2Max.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2MaxActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel6.setText(bundle.getString("ChartPropertyDialog.jLabel6.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, y2AutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), y2Interval, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        y2Interval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2IntervalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(y2AutoCheckBox)
                .addGap(45, 45, 45)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(y2Min, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(53, 53, 53)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(y2Interval, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(y2Max, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(y2Min, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6)
                            .addComponent(y2Interval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(y2Max, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(y2AutoCheckBox))
                .addGap(4, 4, 4))
        );

        javax.swing.GroupLayout scalePanelLayout = new javax.swing.GroupLayout(scalePanel);
        scalePanel.setLayout(scalePanelLayout);
        scalePanelLayout.setHorizontalGroup(
            scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        scalePanelLayout.setVerticalGroup(
            scalePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel7.setText(bundle.getString("ChartPropertyDialog.jLabel7.text")); // NOI18N

        mainTitle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainTitleActionPerformed(evt);
            }
        });
        mainTitle.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                mainTitleFocusLost(evt);
            }
        });
        mainTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                mainTitleKeyReleased(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText(bundle.getString("ChartPropertyDialog.jLabel8.text")); // NOI18N

        subTitle1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subTitle1ActionPerformed(evt);
            }
        });
        subTitle1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                subTitle1FocusLost(evt);
            }
        });
        subTitle1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subTitle1KeyReleased(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel12.setText(bundle.getString("ChartPropertyDialog.jLabel12.text")); // NOI18N

        subTitle2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subTitle2ActionPerformed(evt);
            }
        });
        subTitle2.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                subTitle2FocusLost(evt);
            }
        });
        subTitle2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                subTitle2KeyReleased(evt);
            }
        });

        fontSizeTextFieldMain.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldMainActionPerformed(evt);
            }
        });

        fontSizeTextFieldSub1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldSub1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldSub1ActionPerformed(evt);
            }
        });

        fontSizeTextFieldSub2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldSub2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldSub2ActionPerformed(evt);
            }
        });

        jLabel31.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel31.setText(bundle.getString("ChartPropertyDialog.jLabel31.text")); // NOI18N

        mainFontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        mainFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        mainFontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        mainFontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        mainFontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mainFontStyleActionPerformed(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel33.setText(bundle.getString("ChartPropertyDialog.jLabel33.text")); // NOI18N

        sub1FontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        sub1FontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        sub1FontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        sub1FontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        sub1FontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sub1FontStyleActionPerformed(evt);
            }
        });

        sub2FontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        sub2FontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        sub2FontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        sub2FontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        sub2FontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sub2FontStyleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel12))
                .addGap(24, 24, 24)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(subTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, 358, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fontSizeTextFieldSub2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(sub2FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel31)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(subTitle1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                                    .addComponent(mainTitle, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fontSizeTextFieldSub1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fontSizeTextFieldMain, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(mainFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel33)
                            .addComponent(sub1FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(24, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(jLabel33))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(mainTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldMain, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(mainFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(subTitle1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldSub1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8)
                    .addComponent(sub1FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(subTitle2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldSub2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(sub2FontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fontSizeTextFieldMain, mainTitle});

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fontSizeTextFieldSub1, subTitle1});

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fontSizeTextFieldSub2, subTitle2});

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel9.setText(bundle.getString("ChartPropertyDialog.jLabel9.text")); // NOI18N

        xAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisActionPerformed(evt);
            }
        });
        xAxis.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                xAxisFocusLost(evt);
            }
        });
        xAxis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                xAxisKeyReleased(evt);
            }
        });

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel10.setText(bundle.getString("ChartPropertyDialog.jLabel10.text")); // NOI18N

        y1Axis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1AxisActionPerformed(evt);
            }
        });
        y1Axis.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                y1AxisFocusLost(evt);
            }
        });
        y1Axis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                y1AxisKeyReleased(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel11.setText(bundle.getString("ChartPropertyDialog.jLabel11.text")); // NOI18N

        y2Axis.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                y2AxisFocusLost(evt);
            }
        });
        y2Axis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                y2AxisKeyReleased(evt);
            }
        });

        fontSizeTextFieldxAxis.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldxAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldxAxisActionPerformed(evt);
            }
        });

        fontSizeTextFieldLYAxis.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldLYAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldLYAxisActionPerformed(evt);
            }
        });

        fontSizeTextFieldRYAxis.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextFieldRYAxis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldRYAxisActionPerformed(evt);
            }
        });

        jLabel32.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel32.setText(bundle.getString("ChartPropertyDialog.jLabel32.text")); // NOI18N

        xAxisFontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        xAxisFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        xAxisFontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        xAxisFontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        xAxisFontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xAxisFontStyleActionPerformed(evt);
            }
        });

        leftYAxisFontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        leftYAxisFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        leftYAxisFontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        leftYAxisFontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        leftYAxisFontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leftYAxisFontStyleActionPerformed(evt);
            }
        });

        rightYAxisFontStyle.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        rightYAxisFontStyle.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Plain", "Bold", "Italics" }));
        rightYAxisFontStyle.setMinimumSize(new java.awt.Dimension(71, 18));
        rightYAxisFontStyle.setPreferredSize(new java.awt.Dimension(71, 18));
        rightYAxisFontStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rightYAxisFontStyleActionPerformed(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel34.setText(bundle.getString("ChartPropertyDialog.jLabel34.text")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(51, 51, 51))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel6Layout.createSequentialGroup()
                                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(xAxis, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                            .addComponent(y1Axis)
                            .addComponent(y2Axis))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fontSizeTextFieldLYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fontSizeTextFieldRYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fontSizeTextFieldxAxis, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(xAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leftYAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightYAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel34))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel32)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(xAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldxAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(xAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y1Axis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldLYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(leftYAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(y2Axis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fontSizeTextFieldRYAxis, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rightYAxisFontStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {fontSizeTextFieldLYAxis, fontSizeTextFieldRYAxis, fontSizeTextFieldxAxis, xAxis, y1Axis, y2Axis});

        javax.swing.GroupLayout titlePanelLayout = new javax.swing.GroupLayout(titlePanel);
        titlePanel.setLayout(titlePanelLayout);
        titlePanelLayout.setHorizontalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        titlePanelLayout.setVerticalGroup(
            titlePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titlePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(54, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jPanel7.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));
        jPanel7.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));

        buttonGroup1.add(simpleAxisRadioButton);
        simpleAxisRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        simpleAxisRadioButton.setText(bundle.getString("ChartPropertyDialog.simpleAxisRadioButton.text")); // NOI18N
        simpleAxisRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpleAxisRadioButtonActionPerformed(evt);
            }
        });

        simpleIntervalTextField.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, simpleAxisRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), simpleIntervalTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        simpleIntervalTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpleIntervalTextFieldActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel13.setText(bundle.getString("ChartPropertyDialog.jLabel13.text")); // NOI18N

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Minuten", "Stunden", "Tage", "Monate", " " }));
        jComboBox1.setSelectedIndex(2);
        jComboBox1.setMinimumSize(new java.awt.Dimension(68, 20));
        jComboBox1.setPreferredSize(new java.awt.Dimension(68, 20));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, simpleAxisRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), jComboBox1, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(simpleAxisRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simpleIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(simpleAxisRadioButton)
                    .addComponent(jLabel13)
                    .addComponent(simpleIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        buttonGroup1.add(dateBandRadioButton);
        dateBandRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        dateBandRadioButton.setSelected(true);
        dateBandRadioButton.setText(bundle.getString("ChartPropertyDialog.dateBandRadioButton.text")); // NOI18N
        dateBandRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateBandRadioButtonActionPerformed(evt);
            }
        });

        useMinutes.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useMinutes.setText(bundle.getString("ChartPropertyDialog.useMinutes.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateBandRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useMinutes, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useMinutes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMinutesActionPerformed(evt);
            }
        });

        useHours.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useHours.setText(bundle.getString("ChartPropertyDialog.useHours.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateBandRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useHours, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useHours.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useHoursActionPerformed(evt);
            }
        });

        useDays.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useDays.setText(bundle.getString("ChartPropertyDialog.useDays.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateBandRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useDays, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useDaysActionPerformed(evt);
            }
        });

        useMonthYear.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useMonthYear.setText(bundle.getString("ChartPropertyDialog.useMonthYear.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, dateBandRadioButton, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useMonthYear, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useMonthYear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useMonthYearActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(dateBandRadioButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useMinutes)
                            .addComponent(useHours))
                        .addGap(43, 43, 43)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(useMonthYear)
                            .addComponent(useDays))))
                .addContainerGap(72, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dateBandRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useMinutes)
                    .addComponent(useDays))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useHours)
                    .addComponent(useMonthYear))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel9.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jDateChooser1.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        jDateChooser1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooser1PropertyChange(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel14.setText(bundle.getString("ChartPropertyDialog.jLabel14.text")); // NOI18N

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel15.setText(bundle.getString("ChartPropertyDialog.jLabel15.text")); // NOI18N

        jDateChooser2.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        jDateChooser2.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jDateChooser2PropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel14)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(78, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel15)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel10.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel16.setText(bundle.getString("ChartPropertyDialog.jLabel16.text")); // NOI18N

        fontSizeTextField.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fontSizeTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSizeTextFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fontSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fontSizeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addContainerGap())
        );

        javax.swing.GroupLayout timeAxisPanelLayout = new javax.swing.GroupLayout(timeAxisPanel);
        timeAxisPanel.setLayout(timeAxisPanelLayout);
        timeAxisPanelLayout.setHorizontalGroup(
            timeAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(timeAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        timeAxisPanelLayout.setVerticalGroup(
            timeAxisPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(timeAxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        jList1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jList1ValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel17.setText(bundle.getString("ChartPropertyDialog.jLabel17.text")); // NOI18N

        javax.swing.GroupLayout dataSetOrderPanelLayout = new javax.swing.GroupLayout(dataSetOrderPanel);
        dataSetOrderPanel.setLayout(dataSetOrderPanelLayout);
        dataSetOrderPanelLayout.setHorizontalGroup(
            dataSetOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSetOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataSetOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE)
                    .addComponent(jLabel17))
                .addContainerGap())
        );
        dataSetOrderPanelLayout.setVerticalGroup(
            dataSetOrderPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataSetOrderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                .addContainerGap())
        );

        gridPanelDate.setEnabled(false);

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel11.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        useRangeGridCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useRangeGridCheckBox.setText(bundle.getString("ChartPropertyDialog.useRangeGridCheckBox.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelDate, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), useRangeGridCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useRangeGridCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useRangeGridCheckBoxActionPerformed(evt);
            }
        });

        useGridDateCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useGridDateCheckBox.setText(bundle.getString("ChartPropertyDialog.useGridDateCheckBox.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelDate, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), useGridDateCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useGridDateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useGridDateCheckBoxActionPerformed(evt);
            }
        });

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel18.setText(bundle.getString("ChartPropertyDialog.jLabel18.text")); // NOI18N

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel19.setText(bundle.getString("ChartPropertyDialog.jLabel19.text")); // NOI18N

        intervalSelectorComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        intervalSelectorComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Minute", "Stunde", "Tag", "Woche", "Monat", "Jahr" }));
        intervalSelectorComboBox.setSelectedIndex(4);
        intervalSelectorComboBox.setPreferredSize(new java.awt.Dimension(71, 20));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelDate, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), intervalSelectorComboBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        rangeIntervalTextField.setText(bundle.getString("ChartPropertyDialog.rangeIntervalTextField.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelDate, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), rangeIntervalTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        rangeIntervalTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rangeIntervalTextFieldActionPerformed(evt);
            }
        });

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/tick.png"))); // NOI18N
        refreshButton.setPreferredSize(new java.awt.Dimension(18, 18));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelDate, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), refreshButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel11Layout.createSequentialGroup()
                .addContainerGap(137, Short.MAX_VALUE)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useRangeGridCheckBox)
                    .addComponent(useGridDateCheckBox))
                .addGap(23, 23, 23)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rangeIntervalTextField))
                    .addGroup(jPanel11Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(intervalSelectorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(87, 87, 87))
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useGridDateCheckBox)
                    .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(intervalSelectorComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useRangeGridCheckBox)
                    .addComponent(jLabel19)
                    .addComponent(rangeIntervalTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridPanelDateLayout = new javax.swing.GroupLayout(gridPanelDate);
        gridPanelDate.setLayout(gridPanelDateLayout);
        gridPanelDateLayout.setHorizontalGroup(
            gridPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelDateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gridPanelDateLayout.setVerticalGroup(
            gridPanelDateLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelDateLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(199, Short.MAX_VALUE))
        );

        infoPanel.setEnabled(false);

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel15.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jLabel23.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel23.setText(bundle.getString("ChartPropertyDialog.jLabel23.text")); // NOI18N

        widthTextField.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        widthTextField.setText(bundle.getString("ChartPropertyDialog.widthTextField.text")); // NOI18N
        widthTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthTextFieldActionPerformed(evt);
            }
        });

        jLabel25.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel25.setText(bundle.getString("ChartPropertyDialog.jLabel25.text")); // NOI18N

        jLabel27.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel27.setText(bundle.getString("ChartPropertyDialog.jLabel27.text")); // NOI18N

        heightTextField.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        heightTextField.setText(bundle.getString("ChartPropertyDialog.heightTextField.text")); // NOI18N
        heightTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                heightTextFieldActionPerformed(evt);
            }
        });

        jLabel26.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel26.setText(bundle.getString("ChartPropertyDialog.jLabel26.text")); // NOI18N

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel25))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(1, 1, 1)
                        .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel27)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel23)
                    .addComponent(widthTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel26)
                    .addComponent(heightTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel27))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel16.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        showLegendCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showLegendCheckBox.setText(bundle.getString("ChartPropertyDialog.showLegendCheckBox.text")); // NOI18N
        showLegendCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLegendCheckBoxActionPerformed(evt);
            }
        });

        showChartRemarksCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showChartRemarksCheckBox.setText(bundle.getString("ChartPropertyDialog.showChartRemarksCheckBox.text")); // NOI18N
        showChartRemarksCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showChartRemarksCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showLegendCheckBox)
                    .addComponent(showChartRemarksCheckBox))
                .addContainerGap(248, Short.MAX_VALUE))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(showLegendCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showChartRemarksCheckBox))
        );

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel17.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        useAntiAliasingCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useAntiAliasingCheckBox.setText(bundle.getString("ChartPropertyDialog.useAntiAliasingCheckBox.text")); // NOI18N
        useAntiAliasingCheckBox.setToolTipText(bundle.getString("ChartPropertyDialog.useAntiAliasingCheckBox.toolTipText")); // NOI18N
        useAntiAliasingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAntiAliasingCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useAntiAliasingCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(useAntiAliasingCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout infoPanelLayout = new javax.swing.GroupLayout(infoPanel);
        infoPanel.setLayout(infoPanelLayout);
        infoPanelLayout.setHorizontalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        infoPanelLayout.setVerticalGroup(
            infoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(infoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridPanelCategory.setEnabled(false);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel13.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jCheckBox11.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox11.setText(bundle.getString("ChartPropertyDialog.jCheckBox11.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelCategory, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), jCheckBox11, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jCheckBox11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox11ActionPerformed(evt);
            }
        });

        jCheckBox12.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox12.setText(bundle.getString("ChartPropertyDialog.jCheckBox12.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelCategory, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), jCheckBox12, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jCheckBox12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox12ActionPerformed(evt);
            }
        });

        jLabel24.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel24.setText(bundle.getString("ChartPropertyDialog.jLabel24.text")); // NOI18N

        jTextField4.setText(bundle.getString("ChartPropertyDialog.jTextField4.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, gridPanelCategory, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), jTextField4, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGap(135, 135, 135)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jCheckBox11)
                        .addGap(51, 51, 51)
                        .addComponent(jLabel24)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE))
                    .addComponent(jCheckBox12))
                .addContainerGap(112, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addComponent(jCheckBox12)
                .addGap(6, 6, 6)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox11)
                    .addComponent(jLabel24)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout gridPanelCategoryLayout = new javax.swing.GroupLayout(gridPanelCategory);
        gridPanelCategory.setLayout(gridPanelCategoryLayout);
        gridPanelCategoryLayout.setHorizontalGroup(
            gridPanelCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelCategoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        gridPanelCategoryLayout.setVerticalGroup(
            gridPanelCategoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(gridPanelCategoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(199, Short.MAX_VALUE))
        );

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ChartPropertyDialog.jPanel14.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        domainAutoCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        domainAutoCheckBox.setText(bundle.getString("ChartPropertyDialog.domainAutoCheckBox.text")); // NOI18N
        domainAutoCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainAutoCheckBoxActionPerformed(evt);
            }
        });

        jLabel28.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel28.setText(bundle.getString("ChartPropertyDialog.jLabel28.text")); // NOI18N

        jLabel29.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel29.setText(bundle.getString("ChartPropertyDialog.jLabel29.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, domainAutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), domainMin, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        domainMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainMinActionPerformed(evt);
            }
        });

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, domainAutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), domainMax, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        domainMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainMaxActionPerformed(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel30.setText(bundle.getString("ChartPropertyDialog.jLabel30.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, domainAutoCheckBox, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), domainInterval, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        domainInterval.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                domainIntervalActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addComponent(domainAutoCheckBox)
                .addGap(46, 46, 46)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(domainMax, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 53, Short.MAX_VALUE)
                        .addComponent(jLabel30)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(domainInterval, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addComponent(domainMin, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(228, Short.MAX_VALUE))))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(domainAutoCheckBox)
                    .addGroup(jPanel14Layout.createSequentialGroup()
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel28)
                            .addComponent(domainMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel30)
                            .addComponent(domainInterval, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel29)
                            .addComponent(domainMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout domainPanelLayout = new javax.swing.GroupLayout(domainPanel);
        domainPanel.setLayout(domainPanelLayout);
        domainPanelLayout.setHorizontalGroup(
            domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(domainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(76, 76, 76))
        );
        domainPanelLayout.setVerticalGroup(
            domainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(domainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(268, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(bundle.getString("ChartPropertyDialog.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(590, 450));
        setModal(true);
        setPreferredSize(new java.awt.Dimension(590, 450));
        setResizable(false);

        jPanel1.setPreferredSize(new java.awt.Dimension(590, 40));

        closeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        closeButton.setText(bundle.getString("ChartPropertyDialog.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(closeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 504, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(closeButton)
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        tabPanel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        getContentPane().add(tabPanel, java.awt.BorderLayout.CENTER);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void jList1ValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jList1ValueChanged
        for (int i = 0; i < jList1.getModel().getSize(); i++) {
            xyplot.setDataset(i, dataSetMap.get(jList1.getModel().getElementAt(i).toString()));
            //xyplot.getRenderer(i).setSeriesPaint(0, rendererMap.get(jList1.getModel().getElementAt(i).toString()));
            xyplot.setRenderer(i, rendererMap.get(jList1.getModel().getElementAt(i).toString()));
        }
    }//GEN-LAST:event_jList1ValueChanged

    private void simpleAxisRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpleAxisRadioButtonActionPerformed
        setSimpleAxis();
    }//GEN-LAST:event_simpleAxisRadioButtonActionPerformed

    private void dateBandRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateBandRadioButtonActionPerformed
        setDateBandAxis(); //null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
    }//GEN-LAST:event_dateBandRadioButtonActionPerformed

    private void fontSizeTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldActionPerformed
        if (simpleAxisRadioButton.isSelected()) {
            setSimpleAxis();
        } else {
            setDateBandAxis(); //null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
        }
    }//GEN-LAST:event_fontSizeTextFieldActionPerformed

    private void useMinutesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMinutesActionPerformed
        setDateBandAxis();//null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
    }//GEN-LAST:event_useMinutesActionPerformed

    private void useHoursActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useHoursActionPerformed
        setDateBandAxis();//null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
    }//GEN-LAST:event_useHoursActionPerformed

    private void useDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDaysActionPerformed
        setDateBandAxis();//null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
    }//GEN-LAST:event_useDaysActionPerformed

    private void useMonthYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useMonthYearActionPerformed
        setDateBandAxis();//null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
    }//GEN-LAST:event_useMonthYearActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (simpleAxisRadioButton.isSelected()) {
            setSimpleAxis();
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void simpleIntervalTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpleIntervalTextFieldActionPerformed
        if (simpleAxisRadioButton.isSelected()) {
            setSimpleAxis();
        }
    }//GEN-LAST:event_simpleIntervalTextFieldActionPerformed

    private void y1AutoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1AutoCheckBoxActionPerformed
        sety1Range();
    }//GEN-LAST:event_y1AutoCheckBoxActionPerformed

    private void y2AutoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2AutoCheckBoxActionPerformed
        sety2Range();
    }//GEN-LAST:event_y2AutoCheckBoxActionPerformed

    private void y1MinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1MinActionPerformed
        sety1Range();
    }//GEN-LAST:event_y1MinActionPerformed

    private void y1MaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1MaxActionPerformed
        sety1Range();
    }//GEN-LAST:event_y1MaxActionPerformed

    private void y1IntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1IntervalActionPerformed
        sety1Range();
    }//GEN-LAST:event_y1IntervalActionPerformed

    private void y2MinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2MinActionPerformed
        sety2Range();
    }//GEN-LAST:event_y2MinActionPerformed

    private void y2MaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2MaxActionPerformed
        sety2Range();
    }//GEN-LAST:event_y2MaxActionPerformed

    private void y2IntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2IntervalActionPerformed
        sety2Range();
    }//GEN-LAST:event_y2IntervalActionPerformed

    private void useGridDateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useGridDateCheckBoxActionPerformed
        setDomainGrid();
    }//GEN-LAST:event_useGridDateCheckBoxActionPerformed

    private void useRangeGridCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useRangeGridCheckBoxActionPerformed
        setRangeGrid();
    }//GEN-LAST:event_useRangeGridCheckBoxActionPerformed

    private void mainTitleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainTitleActionPerformed
        setMainTitle();
    }//GEN-LAST:event_mainTitleActionPerformed

    private void mainTitleFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_mainTitleFocusLost
        setMainTitle();
    }//GEN-LAST:event_mainTitleFocusLost

    private void subTitle1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subTitle1ActionPerformed
        setSubTitle(0, subTitle1.getText());
    }//GEN-LAST:event_subTitle1ActionPerformed

    private void subTitle1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_subTitle1FocusLost
        setSubTitle(0, subTitle1.getText());
    }//GEN-LAST:event_subTitle1FocusLost

    private void subTitle2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subTitle2ActionPerformed
        setSubTitle(1, subTitle2.getText());
    }//GEN-LAST:event_subTitle2ActionPerformed

    private void subTitle2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_subTitle2FocusLost
        setSubTitle(1, subTitle2.getText());
    }//GEN-LAST:event_subTitle2FocusLost

    private void xAxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisActionPerformed
        setXAxisLabel();
    }//GEN-LAST:event_xAxisActionPerformed

    private void xAxisFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xAxisFocusLost
        setXAxisLabel();
    }//GEN-LAST:event_xAxisFocusLost

    private void y1AxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1AxisActionPerformed
        setYAxisLabel(0, y1Axis.getText());
    }//GEN-LAST:event_y1AxisActionPerformed

    private void y1AxisFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_y1AxisFocusLost
        setYAxisLabel(0, y1Axis.getText());
    }//GEN-LAST:event_y1AxisFocusLost

    private void y2AxisFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_y2AxisFocusLost
        setYAxisLabel(1, y2Axis.getText());
    }//GEN-LAST:event_y2AxisFocusLost

    private void mainTitleKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_mainTitleKeyReleased
        setMainTitle();
    }//GEN-LAST:event_mainTitleKeyReleased

    private void subTitle1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subTitle1KeyReleased
        setSubTitle(0, subTitle1.getText());
    }//GEN-LAST:event_subTitle1KeyReleased

    private void subTitle2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_subTitle2KeyReleased
        setSubTitle(1, subTitle2.getText());
    }//GEN-LAST:event_subTitle2KeyReleased

    private void xAxisKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_xAxisKeyReleased
        setXAxisLabel();
    }//GEN-LAST:event_xAxisKeyReleased

    private void y1AxisKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_y1AxisKeyReleased
        setYAxisLabel(0, y1Axis.getText());
    }//GEN-LAST:event_y1AxisKeyReleased

    private void y2AxisKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_y2AxisKeyReleased
        setYAxisLabel(1, y2Axis.getText());
    }//GEN-LAST:event_y2AxisKeyReleased

    private void showLegendCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLegendCheckBoxActionPerformed
        LegendTitle legend = null;
        boolean hasLegend = false;

        // Legende aus oder einblenden. sie werden anhand ihrer position erkannt (bottom und left)
        if (chart != null) {
            for (Object o : chart.getSubtitles()) {
                if (o instanceof LegendTitle) {
                    hasLegend = true;
                    legend = (LegendTitle) o;
                }
            }

//            if (panel.getChartdescriber().getLegendTitle() != null) {
            if (!hasLegend) { // Wenn es keine Legende im chart gab (kam ausgeblendet aus GRA)  die aus dem Describer holen
                legend = new LegendTitle(chart.getPlot());
                legend.setPosition(RectangleEdge.BOTTOM);
                legend.setBackgroundPaint(Color.WHITE);
                legend.setFrame(new BlockBorder(Color.GRAY));
//                    t = panel.getChartdescriber().getLegendTitle();
                chart.addSubtitle(legend);
            }

            if (showLegendCheckBox.isSelected()) {
                legend.setVisible(false);
                if (panel.getChartdescriber() != null) {
                    panel.getChartdescriber().setShowLegend(false);
                }
            } else {
                legend.setVisible(true);
                if (panel.getChartdescriber() != null) {
                    panel.getChartdescriber().setShowLegend(true);
                }
            }
//            }
        }
    }//GEN-LAST:event_showLegendCheckBoxActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        setDomainGrid();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void rangeIntervalTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rangeIntervalTextFieldActionPerformed
        setRangeGrid();
    }//GEN-LAST:event_rangeIntervalTextFieldActionPerformed

    private void useRangeGridNumberCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useRangeGridNumberCheckBoxActionPerformed
        setRangeGrid();
    }//GEN-LAST:event_useRangeGridNumberCheckBoxActionPerformed

    private void useDomainGridNumberCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useDomainGridNumberCheckBoxActionPerformed
        setDomainGrid();
    }//GEN-LAST:event_useDomainGridNumberCheckBoxActionPerformed

    private void numberRangeIntervalTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberRangeIntervalTextFieldActionPerformed
        setRangeGrid();
    }//GEN-LAST:event_numberRangeIntervalTextFieldActionPerformed

    private void numberDomainIntervalTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numberDomainIntervalTextFieldActionPerformed
        setDomainGrid();
    }//GEN-LAST:event_numberDomainIntervalTextFieldActionPerformed

    private void jCheckBox11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox11ActionPerformed
        setRangeGrid();
    }//GEN-LAST:event_jCheckBox11ActionPerformed

    private void jCheckBox12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox12ActionPerformed
        setDomainGrid();
    }//GEN-LAST:event_jCheckBox12ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        setRangeGrid();
}//GEN-LAST:event_jTextField4ActionPerformed

    private void widthTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthTextFieldActionPerformed
        setChartSize();
    }//GEN-LAST:event_widthTextFieldActionPerformed

    private void heightTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_heightTextFieldActionPerformed
        setChartSize();
    }//GEN-LAST:event_heightTextFieldActionPerformed

    private void showChartRemarksCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showChartRemarksCheckBoxActionPerformed
        TextTitle t = null;
        boolean hasRemark = false;

        // Chartremarks aus oder einblenden. sie werden anhand ihrer position erkannt (bottom und left)
        if (chart != null) {
            for (Object o : chart.getSubtitles()) {
                if (o instanceof TextTitle) {
                    t = (TextTitle) o;
                    if (t.getPosition() == RectangleEdge.BOTTOM && t.getHorizontalAlignment() == HorizontalAlignment.LEFT) {
                        hasRemark = true;
                    }
                }
            }

            if (panel.getChartdescriber() != null && panel.getChartdescriber().getChartRemarkTitle() != null) {
                if (!hasRemark) { // Wenn es keinen Remark im chart gab (kam ausgeblendet aus GRA)  den aus dem Describer holen
                    t = panel.getChartdescriber().getChartRemarkTitle();
                    chart.addSubtitle(t);
                }

                if (showChartRemarksCheckBox.isSelected()) {
                    t.setVisible(false);
                    panel.getChartdescriber().setShowRemarks(false);
                } else {
                    t.setVisible(true);
                    panel.getChartdescriber().setShowRemarks(true);
                }
            }
        }


    }//GEN-LAST:event_showChartRemarksCheckBoxActionPerformed

    private void domainAutoCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainAutoCheckBoxActionPerformed
        setDomainRange();
    }//GEN-LAST:event_domainAutoCheckBoxActionPerformed

    private void domainMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainMinActionPerformed
        setDomainRange();
    }//GEN-LAST:event_domainMinActionPerformed

    private void domainMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainMaxActionPerformed
        setDomainRange();
    }//GEN-LAST:event_domainMaxActionPerformed

    private void domainIntervalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_domainIntervalActionPerformed
        setDomainRange();
    }//GEN-LAST:event_domainIntervalActionPerformed

    private void jDateChooser1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooser1PropertyChange
        if (jDateChooser1.getDate() != null) {
            if (simpleAxisRadioButton.isSelected()) {
                setSimpleAxis();
            } else {
                setDateBandAxis(); //null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
            }
        }
    }//GEN-LAST:event_jDateChooser1PropertyChange

    private void jDateChooser2PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jDateChooser2PropertyChange
        if (jDateChooser2.getDate() != null) {
            if (simpleAxisRadioButton.isSelected()) {
                setSimpleAxis();
            } else {
                setDateBandAxis();// null, Day.class, jCheckBox7.isSelected(), jCheckBox8.isSelected(), jCheckBox9.isSelected(), jCheckBox10.isSelected(), fontSizeTextField.getText());
            }
        }
    }//GEN-LAST:event_jDateChooser2PropertyChange

    private void fontSizeTextFieldMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldMainActionPerformed
        setMainTitle();
    }//GEN-LAST:event_fontSizeTextFieldMainActionPerformed

    private void fontSizeTextFieldSub1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldSub1ActionPerformed
        setSubTitle(0, subTitle1.getText());
    }//GEN-LAST:event_fontSizeTextFieldSub1ActionPerformed

    private void fontSizeTextFieldSub2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldSub2ActionPerformed
        setSubTitle(1, subTitle2.getText());
    }//GEN-LAST:event_fontSizeTextFieldSub2ActionPerformed

    private void fontSizeTextFieldxAxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldxAxisActionPerformed
        setXAxisLabel();
    }//GEN-LAST:event_fontSizeTextFieldxAxisActionPerformed

    private void fontSizeTextFieldLYAxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldLYAxisActionPerformed
        setYAxisLabel(0, y1Axis.getText());
    }//GEN-LAST:event_fontSizeTextFieldLYAxisActionPerformed

    private void fontSizeTextFieldRYAxisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSizeTextFieldRYAxisActionPerformed
        setYAxisLabel(1, y2Axis.getText());
    }//GEN-LAST:event_fontSizeTextFieldRYAxisActionPerformed

    private void mainFontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mainFontStyleActionPerformed
        setMainTitle();
    }//GEN-LAST:event_mainFontStyleActionPerformed

    private void sub1FontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sub1FontStyleActionPerformed

        setSubTitle(0, subTitle1.getText());
    }//GEN-LAST:event_sub1FontStyleActionPerformed

    private void sub2FontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sub2FontStyleActionPerformed
        setSubTitle(1, subTitle2.getText());
    }//GEN-LAST:event_sub2FontStyleActionPerformed

    private void xAxisFontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xAxisFontStyleActionPerformed
        setXAxisLabel();
    }//GEN-LAST:event_xAxisFontStyleActionPerformed

    private void leftYAxisFontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leftYAxisFontStyleActionPerformed
        setYAxisLabel(0, y1Axis.getText());
    }//GEN-LAST:event_leftYAxisFontStyleActionPerformed

    private void rightYAxisFontStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rightYAxisFontStyleActionPerformed
        setYAxisLabel(1, y2Axis.getText());
    }//GEN-LAST:event_rightYAxisFontStyleActionPerformed

    private void useAntiAliasingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAntiAliasingCheckBoxActionPerformed
        setAntiAliasing(useAntiAliasingCheckBox.isSelected());
    }//GEN-LAST:event_useAntiAliasingCheckBoxActionPerformed

    /**
     * Größe der Grafik setzen (des ChartPanel)
     */
    private void setAntiAliasing(boolean b) {
        if (chart != null) {
            chart.setAntiAlias(b);
        }
    }

    /**
     * Größe der Grafik setzen (des ChartPanel)
     */
    private void setChartSize() {
        int width = Integer.valueOf(widthTextField.getText().trim());
        int height = Integer.valueOf(heightTextField.getText().trim());
        panel.setPreferredSize(new Dimension(width, height));
        this.gui.getDesktop().getSelectedFrame().pack();
    }

    private void setYAxisLabel(int id, String s) {
        if (xyplot != null) {

            Integer fontStyle = 0;
            Integer fontSize = 10;
            Integer index = 0;
            switch (id) {
                case 0:
                    index = leftYAxisFontStyle.getSelectedIndex();
                    try {
                        fontSize = Integer.parseInt(fontSizeTextFieldLYAxis.getText());
                    } catch (NumberFormatException e) {
                        fontSize = 10;
                        fontSizeTextFieldLYAxis.setText(fontSize.toString());
                    }
                    break;
                case 1:
                    index = rightYAxisFontStyle.getSelectedIndex();
                    try {
                        fontSize = Integer.parseInt(fontSizeTextFieldRYAxis.getText());
                    } catch (NumberFormatException e) {
                        fontSize = 10;
                        fontSizeTextFieldRYAxis.setText(fontSize.toString());
                    }
            }

            switch (index) {
                case 0:
                    fontStyle = Font.PLAIN;
                    break;
                case 1:
                    fontStyle = Font.BOLD;
                    break;
                case 2:
                    fontStyle = Font.ITALIC;
                    break;
                default:
                    fontStyle = Font.PLAIN;
            }


            if (xyplot.getRangeAxis(id) != null) {
                xyplot.getRangeAxis(id).setLabel(s.trim());
                xyplot.getRangeAxis(id).setLabelFont(new Font(xyplot.getRangeAxis(id).getLabelFont().getFamily(), fontStyle, fontSize));
            }
        }
    }

    private void setXAxisLabel() {
        if (xyplot != null) {
            xyplot.getDomainAxis().setLabel(xAxis.getText().trim());
            Integer fontStyle = 0;
            Integer fontSize;
            switch (xAxisFontStyle.getSelectedIndex()) {
                case 0:
                    fontStyle = Font.PLAIN;
                    break;
                case 1:
                    fontStyle = Font.BOLD;
                    break;
                case 2:
                    fontStyle = Font.ITALIC;
                    break;
                default:
                    fontStyle = Font.PLAIN;
            }


            try {
                fontSize = Integer.parseInt(fontSizeTextFieldxAxis.getText());
            } catch (NumberFormatException e) {
                fontSize = 10;
                fontSizeTextFieldxAxis.setText(fontSize.toString());
            }

            xyplot.getDomainAxis().setLabelFont(new Font(xyplot.getDomainAxis().getLabelFont().getFamily(), fontStyle, fontSize));

        }
    }

    private void setSubTitle(int id, String t) {
        TextTitle subtitle;
        if (chart.getSubtitle(id) instanceof TextTitle) {
            subtitle = (TextTitle) chart.getSubtitle(id);
            subtitle.setText(t.trim());
        } else {
            return;
        }

        Integer index = 0;
        Integer fontSize = 10;
        Integer fontStyle;
        switch (id) {
            case 0:
                index = sub1FontStyle.getSelectedIndex();
                try {
                    fontSize = Integer.parseInt(fontSizeTextFieldSub1.getText());
                } catch (NumberFormatException e) {
                    fontSize = 10;
                    fontSizeTextFieldSub1.setText(fontSize.toString());
                }
                break;
            case 1:
                index = sub2FontStyle.getSelectedIndex();
                try {
                    fontSize = Integer.parseInt(fontSizeTextFieldSub2.getText());
                } catch (NumberFormatException e) {
                    fontSize = 10;
                    fontSizeTextFieldSub2.setText(fontSize.toString());
                }
        }

        switch (index) {
            case 0:
                fontStyle = Font.PLAIN;
                break;
            case 1:
                fontStyle = Font.BOLD;
                break;
            case 2:
                fontStyle = Font.ITALIC;
                break;
            default:
                fontStyle = Font.PLAIN;
        }

        subtitle.setFont(new java.awt.Font(subtitle.getFont().getFamily(), fontStyle, fontSize));


    }

    private void setMainTitle() {
        chart.setTitle(mainTitle.getText().trim());
        Integer fontSize;
        try {
            fontSize = Integer.parseInt(fontSizeTextFieldMain.getText());
        } catch (NumberFormatException e) {
            fontSize = 14;
            fontSizeTextFieldMain.setText(fontSize.toString());
        }
        Integer fontStyle;
        switch (mainFontStyle.getSelectedIndex()) {
            case 0:
                fontStyle = Font.PLAIN;
                break;
            case 1:
                fontStyle = Font.BOLD;
                break;
            case 2:
                fontStyle = Font.ITALIC;
                break;
            default:
                fontStyle = Font.PLAIN;
        }
        chart.getTitle().setFont(new java.awt.Font(chart.getTitle().getFont().getFamily(), fontStyle, fontSize));
    }

    private void setDomainGrid() {
        if (xyplot != null) {
            if (useGridDateCheckBox.isEnabled()) {
                ((PeriodAxis) xyplot.getDomainAxis()).setMajorTickTimePeriodClass(getClassOf(intervalSelectorComboBox.getSelectedIndex()));
                xyplot.setDomainGridlinesVisible(useGridDateCheckBox.isSelected());
            } else if (useDomainGridNumberCheckBox.isEnabled()) {
                if (xyplot.getDomainAxis() instanceof DateAxis) {
                    ((DateAxis) xyplot.getDomainAxis()).setTickUnit(new DateTickUnit(DateTickUnitType.DAY, Integer.valueOf(numberDomainIntervalTextField.getText())));
                } else {
                    ((NumberAxis) xyplot.getDomainAxis()).setTickUnit(new NumberTickUnit(Double.valueOf(numberDomainIntervalTextField.getText())));
                }
                xyplot.setDomainGridlinesVisible(useDomainGridNumberCheckBox.isSelected());
            }
        } else if (categoryPlot != null) {
            categoryPlot.setDomainGridlinesVisible(jCheckBox12.isSelected());
        }
        setScalePanel();
    }

    private void setRangeGrid() {
        if (xyplot != null) {
            if (useRangeGridCheckBox.isEnabled()) {
                xyplot.setRangeGridlinesVisible(useRangeGridCheckBox.isSelected());
                ((NumberAxis) xyplot.getRangeAxis()).setTickUnit(new NumberTickUnit(Double.valueOf(rangeIntervalTextField.getText())));
            } else if (useRangeGridNumberCheckBox.isEnabled()) {
                xyplot.setRangeGridlinesVisible(useRangeGridNumberCheckBox.isSelected());
                ((NumberAxis) xyplot.getRangeAxis()).setTickUnit(new NumberTickUnit(Double.valueOf(numberRangeIntervalTextField.getText())));
            }
        } else if (categoryPlot != null) {
            categoryPlot.setRangeGridlinesVisible(jCheckBox11.isSelected());
            ((NumberAxis) categoryPlot.getRangeAxis()).setTickUnit(new NumberTickUnit(Double.valueOf(jTextField4.getText())));
        }
        setScalePanel();
    }

    private void sety1Range() {
        if (xyplot.getRangeAxis(0) != null) {
            xyplot.getRangeAxis(0).setAutoRange(true);
            xyplot.getRangeAxis(0).setAutoTickUnitSelection(true);
            if (!y1AutoCheckBox.isSelected()) {
                if (!y1Min.getText().isEmpty()) {
                    xyplot.getRangeAxis(0).setLowerBound(Double.valueOf(commaSwitcher(y1Min.getText())));
                }
                if (!y1Max.getText().isEmpty()) {
                    xyplot.getRangeAxis(0).setUpperBound(Double.valueOf(commaSwitcher(y1Max.getText())));
                }

                // Tickintervall
                if (!y1Interval.getText().isEmpty()) {
                    ((NumberAxis) xyplot.getRangeAxis(0)).setTickUnit(new NumberTickUnit(Double.valueOf(commaSwitcher(y1Interval.getText().toString()))));
                }
            }
        }
        setGridPanel();
    }

    private void sety2Range() {
        if (xyplot.getRangeAxis(1) != null) {
            xyplot.getRangeAxis(1).setAutoRange(true);
            xyplot.getRangeAxis(1).setAutoTickUnitSelection(true);
            if (!y2AutoCheckBox.isSelected()) {
                if (!y2Min.getText().isEmpty()) {
                    xyplot.getRangeAxis(1).setLowerBound(Double.valueOf(commaSwitcher(y2Min.getText())));
                }
                if (!y2Max.getText().isEmpty()) {
                    xyplot.getRangeAxis(1).setUpperBound(Double.valueOf(commaSwitcher(y2Max.getText())));
                }

                // Tickintervall
                if (!y2Interval.getText().isEmpty()) {
                    ((NumberAxis) xyplot.getRangeAxis(1)).setTickUnit(new NumberTickUnit(Double.valueOf(commaSwitcher(y2Interval.getText().toString()))));
                }
            }
        }
        setGridPanel();
    }

    private void setDomainRange() {
        if (xyplot.getDomainAxis() != null) {
            xyplot.getDomainAxis().setAutoRange(true);
            xyplot.getDomainAxis().setAutoTickUnitSelection(true);
            if (!domainAutoCheckBox.isSelected()) {
                if (!domainMin.getText().isEmpty()) {
                    xyplot.getDomainAxis().setLowerBound(Double.valueOf(commaSwitcher(domainMin.getText())));
                }
                if (!domainMax.getText().isEmpty()) {
                    xyplot.getDomainAxis().setUpperBound(Double.valueOf(commaSwitcher(domainMax.getText())));
                }
                // Intervall
                if (!domainInterval.getText().isEmpty()) {
                    ((NumberAxis) xyplot.getDomainAxis()).setTickUnit(new NumberTickUnit(Double.valueOf(commaSwitcher(domainInterval.getText()))));
                }
            }
        }
    }

    private Class getClassOf(int index) {
        Class c;
        switch (index) {
            case 0:
                c = new Minute().getClass();
                break;
            case 1:
                c = new Hour().getClass();
                break;
            case 2:
                c = new Day().getClass();
                break;
            case 3:
                c = new Week().getClass();
                break;
            case 5:
                c = new Month().getClass();
                break;
            case 6:
                c = new Year().getClass();
                break;
            default:
                c = new Month().getClass();
        }
        return c;
    }

    private int getIndexOf(Class p) {
        int index = 0;
        if (Minute.class.isAssignableFrom(p)) {
            index = 0;
        } else if (Hour.class.isAssignableFrom(p)) {
            index = 1;
        } else if (Day.class.isAssignableFrom(p)) {
            index = 2;
        } else if (Week.class.isAssignableFrom(p)) {
            index = 3;
        } else if (Month.class.isAssignableFrom(p)) {
            index = 4;
        } else if (Year.class.isAssignableFrom(p)) {
            index = 5;
        }
        return index;
    }

    private void setSimpleAxis() {
        DateAxis axis = new DateAxis();

        // set dates from datechooser
        if (jDateChooser1.getDate() != null) {
            axis.setLowerBound(jDateChooser1.getDate().getTime());
        }
        if (jDateChooser2.getDate() != null) {
            axis.setUpperBound(jDateChooser2.getDate().getTime());
        }

        int interval = 1;
        int fontSize;
        DateTickUnitType type = DateTickUnitType.DAY;
        try {
            interval = Integer.parseInt(simpleIntervalTextField.getText());
        } catch (NumberFormatException e) {
        }

        switch (jComboBox1.getSelectedIndex()) {
            case 0:
                type = DateTickUnitType.MINUTE;
                break;
            case 1:
                type = DateTickUnitType.HOUR;
                break;
            case 2:
                type = DateTickUnitType.DAY;
                break;
            case 3:
                type = DateTickUnitType.MONTH;
                break;
        }

        axis.setTickUnit(new DateTickUnit(type, interval));

        try {
            fontSize = Integer.parseInt(fontSizeTextField.getText());
        } catch (NumberFormatException e) {
            fontSize = 9;
        }
        axis.setTickLabelFont(new java.awt.Font(axis.getTickLabelFont().getFontName(), axis.getTickLabelFont().getStyle(), fontSize));

        if (cbPlot != null) {  //  es ist ein CombinedPlot, dann muss diesem die neue Achse zugewiesen werden
            cbPlot.setDomainAxis(axis);
        } else {
            xyplot.setDomainAxis(axis);
        }
        selectAndAddGridPanel();
        setGridPanel();

    }

    private void setDateBandAxis() {
//    private void setDateBandAxis(DateInterval interval, Class spanClass, boolean minutes, boolean hours, boolean days, boolean monthyear, String fontSizeString) {
        int fontSize = 9;
        try {
            fontSize = Integer.parseInt(fontSizeTextField.getText());
        } catch (NumberFormatException e) {
        }
//        ValueAxis axis = new DateBandAxis(interval, spanClass, minutes, hours, days, monthyear, fontSize);
        ValueAxis axis = new DateBandAxis(null, getClassOf(intervalSelectorComboBox.getSelectedIndex()), useMinutes.isSelected(), useHours.isSelected(), useDays.isSelected(), useMonthYear.isSelected(), fontSize);

        // set dates from datechooser
        if (jDateChooser1.getDate() != null) {
            axis.setLowerBound(jDateChooser1.getDate().getTime());
        }
        if (jDateChooser2.getDate() != null) {
            axis.setUpperBound(jDateChooser2.getDate().getTime());
        }

        if (cbPlot != null) {  //  es ist ein CombinedPlot, dann muss diesem die neue Achse zugewiesen werden
            cbPlot.setDomainAxis(axis);
        } else {
            xyplot.setDomainAxis(axis);
        }
        selectAndAddGridPanel();
        setGridPanel();
    }

    /**
     * Ersetzt in einen String Kommas durch Punkte
     *
     * @param s
     * @return
     */
    private String commaSwitcher(String s) {
        s = s.replace(',', '.');
        return s;
    }

    /**
     * Ersetzt in einen String Punkte durch Kommas
     *
     * @param s
     * @return
     */
    private String pointSwitcher(String s) {
        s = s.replace('.', ',');
        return s;
    }

    /**
     * Setzt den gewählten Tab
     *
     * @param tab
     */
    public void setTab(int tab) {
        switch (tab) {
            case SCALE:
                setScalePanel();
                tabPanel.setSelectedIndex(0);
                break;
            case TITLE:
                setTitlePanel();
                tabPanel.setSelectedIndex(1);
                break;
            case TIMEAXIS:
                setDomainAxisPanel();
                tabPanel.setSelectedIndex(2);
                break;
            case DATASERIES:
                setSeriesOrderPanel();
                tabPanel.setSelectedIndex(3);
                break;
            case GRID:
                setGridPanel();
                tabPanel.setSelectedIndex(4);
                break;
            case INFO:
                setInfoPanel();
                tabPanel.setSelectedIndex(5);
                break;
        }
    }

    /**
     * Belegt das Zeitachses-Panel mit den aktuellen Werten des Plots
     */
    private void setDomainAxisPanel() {
        int simpleInterval = 1;
        // Vorbelegung
        useMinutes.setSelected(false);
        useHours.setSelected(true);
        useDays.setSelected(true);
        useMonthYear.setSelected(true);
        simpleIntervalTextField.setText(Integer.toString(1));
        if (xyplot != null) {
            ValueAxis axis = xyplot.getDomainAxis();
            if (axis instanceof DateAxis) { // a simple date axis
                int fontSize = axis.getTickLabelFont().getSize();
                fontSizeTextField.setText(Integer.toString(fontSize));
                simpleAxisRadioButton.setSelected(true);
                DateTickUnit unit = ((DateAxis) axis).getTickUnit();
                simpleInterval = unit.getMultiple();
                simpleIntervalTextField.setText(Integer.toString(simpleInterval));

                if (unit.getUnitType().equals(DateTickUnitType.MINUTE)) {
                    jComboBox1.setSelectedIndex(0);
                } else if (unit.getUnitType().equals(DateTickUnitType.HOUR)) {
                    jComboBox1.setSelectedIndex(1);
                } else if (unit.getUnitType().equals(DateTickUnitType.DAY)) {
                    jComboBox1.setSelectedIndex(2);
                } else if (unit.getUnitType().equals(DateTickUnitType.MONTH)) {
                    jComboBox1.setSelectedIndex(3);
                }

                // set the date fields
                setDateField(jDateChooser1, axis.getLowerBound());
                setDateField(jDateChooser2, axis.getUpperBound());

            } else if (axis instanceof DateBandAxis || axis instanceof PeriodAxis) { // a date band axis
                dateBandRadioButton.setSelected(true);
                PeriodAxisLabelInfo info;
                dateBandRadioButton.setSelected(true);
                useMinutes.setSelected(false);
                useHours.setSelected(false);
                useDays.setSelected(false);
                useMonthYear.setSelected(false);
                for (int i = 0; i < ((PeriodAxis) axis).getLabelInfo().length; i++) {
                    info = ((PeriodAxis) axis).getLabelInfo()[i];
                    fontSizeTextField.setText(Integer.toString(info.getLabelFont().getSize()));
                    if (info.getPeriodClass().equals(Minute.class)) {
                        useMinutes.setSelected(true);
                    }
                    if (info.getPeriodClass().equals(Hour.class)) {
                        useHours.setSelected(true);
                    }
                    if (info.getPeriodClass().equals(Day.class)) {
                        useDays.setSelected(true);
                    }
                    if (info.getPeriodClass().equals(Month.class)) {
                        useMonthYear.setSelected(true);
                    }
                }

                // set the date fields
                setDateField(jDateChooser1, axis.getLowerBound());
                setDateField(jDateChooser2, axis.getUpperBound());

            } else { // any another axis
                domainAutoCheckBox.setSelected(axis.isAutoRange());
                domainMin.setText(format.format(axis.getRange().getLowerBound()));
                domainMax.setText(format.format(axis.getRange().getUpperBound()));
//                domainInterval.setText(axis.get);
            }
        }
    }

    private void setDateField(JDateChooser dc, Double dateDouble) {
        dc.setDate(new Date(dateDouble.longValue()));
    }

    /**
     * Belegt das Beschriftungs-Panel mit den aktuellen Werten des Plots
     */
    private void setTitlePanel() {
        // erst alle Titel leeren
        subTitle1.setText("");
        subTitle2.setText("");
        mainTitle.setText("");

        mainTitle.setText(chart.getTitle().getText());
        mainTitle.setToolTipText(chart.getTitle().getText());
        mainTitle.setCaretPosition(0);

        // font of main title
        fontSizeTextFieldMain.setText(String.valueOf(chart.getTitle().getFont().getSize()));
        mainFontStyle.setSelectedIndex(chart.getTitle().getFont().getStyle());

        // Neu
        int subTitleCount = chart.getSubtitleCount();
        for (int i = 0; i < subTitleCount; i++) {
            if (chart.getSubtitle(i) instanceof TextTitle) {
                switch (i) {
                    case 0:
                        subTitle1.setText(((TextTitle) chart.getSubtitle(i)).getText());
                        subTitle1.setCaretPosition(0);
                        subTitle1.setToolTipText(((TextTitle) chart.getSubtitle(i)).getText());
                        fontSizeTextFieldSub1.setText(String.valueOf(((TextTitle) chart.getSubtitle(i)).getFont().getSize()));
                        sub1FontStyle.setSelectedIndex(((TextTitle) chart.getSubtitle(i)).getFont().getStyle());
                        break;
                    case 1:
                        subTitle2.setText(((TextTitle) chart.getSubtitle(i)).getText());
                        subTitle2.setToolTipText(((TextTitle) chart.getSubtitle(i)).getText());
                        subTitle2.setCaretPosition(0);
                        fontSizeTextFieldSub2.setText(String.valueOf(((TextTitle) chart.getSubtitle(i)).getFont().getSize()));
                        sub2FontStyle.setSelectedIndex(((TextTitle) chart.getSubtitle(i)).getFont().getStyle());
                        break;
                }
            }
        }

        if (xyplot != null) {
            y1Axis.setText((xyplot.getRangeAxis(0) == null) ? "" : xyplot.getRangeAxis(0).getLabel());
            y2Axis.setText((xyplot.getRangeAxis(1) == null) ? "" : xyplot.getRangeAxis(1).getLabel());
            xAxis.setText((xyplot.getDomainAxis() == null) ? "" : xyplot.getDomainAxis().getLabel());

            if (xyplot.getDomainAxis() != null) {
                fontSizeTextFieldxAxis.setText(String.valueOf(xyplot.getDomainAxis().getLabelFont().getSize()));
                xAxisFontStyle.setSelectedIndex(xyplot.getDomainAxis().getLabelFont().getStyle());
            }

            if (xyplot.getRangeAxis(0) != null) {
                fontSizeTextFieldLYAxis.setText(String.valueOf(xyplot.getRangeAxis(0).getLabelFont().getSize()));
                leftYAxisFontStyle.setSelectedIndex(xyplot.getRangeAxis(0).getLabelFont().getStyle());
            }

            if (xyplot.getRangeAxis(1) != null) {
                fontSizeTextFieldRYAxis.setText(String.valueOf(xyplot.getRangeAxis(1).getLabelFont().getSize()));
                rightYAxisFontStyle.setSelectedIndex(xyplot.getRangeAxis(1).getLabelFont().getStyle());
            }

        } else if (categoryPlot != null) {
            y1Axis.setText((categoryPlot.getRangeAxis(0) == null) ? "" : categoryPlot.getRangeAxis(0).getLabel());
            y2Axis.setText((categoryPlot.getRangeAxis(1) == null) ? "" : categoryPlot.getRangeAxis(1).getLabel());
            xAxis.setText((categoryPlot.getDomainAxis() == null) ? "" : categoryPlot.getDomainAxis().getLabel());

            if (categoryPlot.getDomainAxis() != null) {
                fontSizeTextFieldxAxis.setText(String.valueOf(categoryPlot.getDomainAxis().getLabelFont().getSize()));
                xAxisFontStyle.setSelectedIndex(categoryPlot.getDomainAxis().getLabelFont().getStyle());
            }
            if (categoryPlot.getRangeAxis(0) != null) {
                fontSizeTextFieldLYAxis.setText(String.valueOf(categoryPlot.getRangeAxis(0).getLabelFont().getSize()));
                leftYAxisFontStyle.setSelectedIndex(categoryPlot.getRangeAxis(0).getLabelFont().getStyle());
            }
            if (categoryPlot.getRangeAxis(1) != null) {
                fontSizeTextFieldRYAxis.setText(String.valueOf(categoryPlot.getRangeAxis(1).getLabelFont().getSize()));
                rightYAxisFontStyle.setSelectedIndex(categoryPlot.getRangeAxis(1).getLabelFont().getStyle());
            }




        }
    }

    /**
     * Belegt das Scale-Panel mit den aktuellen Werten des Plots
     */
    private void setScalePanel() {
        if (xyplot != null) {
            if (xyplot.getRangeAxis(0) != null) {
                y1AutoCheckBox.setEnabled(true);
                y1AutoCheckBox.setSelected(xyplot.getRangeAxis(0).isAutoRange());
            } else {
                y1AutoCheckBox.setEnabled(false);
            }

            if (xyplot.getRangeAxis(1) != null) {
                y2AutoCheckBox.setEnabled(true);
                y2AutoCheckBox.setSelected(xyplot.getRangeAxis(1).isAutoRange());
            } else {
                y2AutoCheckBox.setEnabled(false);
            }

            y1Min.setText((xyplot.getRangeAxis(0) == null) ? "" : format.format(xyplot.getRangeAxis(0).getRange().getLowerBound()));
            y1Max.setText((xyplot.getRangeAxis(0) == null) ? "" : format.format(xyplot.getRangeAxis(0).getRange().getUpperBound()));
            y2Min.setText((xyplot.getRangeAxis(1) == null) ? "" : format.format(xyplot.getRangeAxis(1).getRange().getLowerBound()));
            y2Max.setText((xyplot.getRangeAxis(1) == null) ? "" : format.format(xyplot.getRangeAxis(1).getRange().getUpperBound()));
            y1Interval.setText(pointSwitcher(Double.toString(((NumberAxis) xyplot.getRangeAxis(0)).getTickUnit().getSize())));
        } else if (categoryPlot != null) {
            if (categoryPlot.getRangeAxis(0) != null) {
                y1AutoCheckBox.setEnabled(categoryPlot.getRangeAxis(0).isAutoRange());
            }
            if (categoryPlot.getRangeAxis(1) != null) {
                y2AutoCheckBox.setEnabled(categoryPlot.getRangeAxis(1).isAutoRange());
            }
            y1Min.setText((categoryPlot.getRangeAxis(0) == null) ? "" : format.format(categoryPlot.getRangeAxis(0).getRange().getLowerBound()));
            y1Max.setText((categoryPlot.getRangeAxis(0) == null) ? "" : format.format(categoryPlot.getRangeAxis(0).getRange().getUpperBound()));
            y2Min.setText((categoryPlot.getRangeAxis(1) == null) ? "" : format.format(categoryPlot.getRangeAxis(1).getRange().getLowerBound()));
            y2Max.setText((categoryPlot.getRangeAxis(1) == null) ? "" : format.format(categoryPlot.getRangeAxis(1).getRange().getUpperBound()));
            y2Interval.setText(pointSwitcher(Double.toString(((NumberAxis) categoryPlot.getRangeAxis(0)).getTickUnit().getSize())));
        }
    }

    /**
     * Belegt das SeriesOrderPanel mit den aktuellen Datenreihen des Plots
     */
    private void setSeriesOrderPanel() {
        if (xyplot != null) {
            XYDataset ds;
            DefaultListModel dm = new DefaultListModel();
            int count = xyplot.getDatasetCount();

            dataSetMap = new HashMap<String, XYDataset>();
            XYItemRenderer renderer = null;
            xyplot.getRenderer();
            for (int i = 0; i < count; i++) {
                ds = xyplot.getDataset(i);
                renderer = xyplot.getRenderer(i);
                if (renderer.isSeriesVisible(0)) {
                    rendererMap.put((String) ds.getSeriesKey(0), renderer);
                    dataSetMap.put((String) ds.getSeriesKey(0), ds);
                    dm.addElement(new ListEntry((String) ds.getSeriesKey(0), renderer.getSeriesPaint(0)));
                }
            }

            if (dm.size() == 1) {
                tabPanel.setEnabledAt(3, false);
            }
            jList1.setModel(dm);
        }
    }

    /**
     * Belegt das Grid-Panel
     */
    private void setGridPanel() {
        DecimalFormat decFormat = new DecimalFormat("#0.#");
        if (xyplot != null) {
            useGridDateCheckBox.setSelected(xyplot.isDomainGridlinesVisible());
            useRangeGridCheckBox.setSelected(xyplot.isRangeGridlinesVisible());
            useRangeGridNumberCheckBox.setSelected(xyplot.isRangeGridlinesVisible());
            useDomainGridNumberCheckBox.setSelected(xyplot.isDomainGridlinesVisible());
            rangeIntervalTextField.setText(String.valueOf(((NumberAxis) xyplot.getRangeAxis()).getTickUnit().getSize()));
            numberRangeIntervalTextField.setText(String.valueOf(((NumberAxis) xyplot.getRangeAxis()).getTickUnit().getSize()));
            if (numberDomainIntervalTextField.isEnabled()) {
                if (xyplot.getDomainAxis() instanceof NumberAxis) {
                    numberDomainIntervalTextField.setText(decFormat.format(((NumberAxis) xyplot.getDomainAxis()).getTickUnit().getSize()));
                } else if (xyplot.getDomainAxis() instanceof DateAxis) {
                    numberDomainIntervalTextField.setText(decFormat.format(((DateAxis) xyplot.getDomainAxis()).getTickUnit().getSize() / (1000 * 60 * 60 * 24)));
                }
            } else if (intervalSelectorComboBox.isEnabled()) {
                intervalSelectorComboBox.setSelectedIndex(getIndexOf(((PeriodAxis) xyplot.getDomainAxis()).getMajorTickTimePeriodClass()));
            }
        } else if (categoryPlot != null) {
            jCheckBox11.setSelected(categoryPlot.isRangeGridlinesVisible());
            jCheckBox12.setSelected(categoryPlot.isDomainGridlinesVisible());
            jTextField4.setText(String.valueOf(((NumberAxis) categoryPlot.getRangeAxis()).getTickUnit().getSize()));
        }
    }

    /**
     * Belegt das Grid-Panel
     */
    private void setInfoPanel() {
        widthTextField.setText(String.valueOf(panel.getWidth()));
        heightTextField.setText(String.valueOf(panel.getHeight()));
        useAntiAliasingCheckBox.setSelected(chart.getAntiAlias());

        if (panel.getChartdescriber() != null) {
            showLegendCheckBox.setSelected(panel.getChartdescriber().isShowLegend() != null && !panel.getChartdescriber().isShowLegend());
            showChartRemarksCheckBox.setSelected(panel.getChartdescriber().isShowRemarks() != null && !panel.getChartdescriber().isShowRemarks());
        } else {
            showLegendCheckBox.setEnabled(false);
            showChartRemarksCheckBox.setEnabled(false);
        }
    }

    static class ListEntry {

        private String key;
        private Paint paint;

        public ListEntry(String k, Paint p) {
            key = k;
            paint = p;
        }

        public String getKey() {
            return key;
        }

        public Paint getPaint() {
            return paint;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    public void setMainTitleTextField(String title) {
        mainTitle.setText(title.trim());


    }

    public void setSubTitleTextField(String title) {
        subTitle1.setText(title.trim());




    }

    class ColorCellRenderer extends DefaultListCellRenderer {

        Color entryColor = new Color(255, 255, 204);
//    class ColorCellRenderer extends JLabel implements ListCellRenderer {

        public ColorCellRenderer() {
            setOpaque(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void hightlight() {
//            System.out.println("mark");
//            setBackground(Color.red);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setIconTextGap(5);
            setBackground(entryColor);
            setPreferredSize(new Dimension(getWidth(), 30));
            setBorder(new LineBorder(Color.BLACK));
            setText(value.toString());
            if (rendererMap.get(value.toString()) != null) {
                setIcon(new ColorIcon((Color) rendererMap.get(value.toString()).getSeriesPaint(0)));
            }
            return this;
        }
    }

    static class ColorIcon implements Icon {

        private int HEIGHT = 12;
        private int WIDTH = 12;
        private Color color;

        public ColorIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconHeight() {
            return HEIGHT;
        }

        @Override
        public int getIconWidth() {
            return WIDTH;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x + 2, y + 2, WIDTH - 2, HEIGHT - 2);

            g.setColor(Color.black);
            g.drawRect(x + 2, y + 2, WIDTH - 2, HEIGHT - 2);
        }
    }

    static class ListMoveDataFlavor extends DataFlavor {

        private final DefaultListModel model;

        public ListMoveDataFlavor(DefaultListModel model) {
            super(ListMoveTransferData.class, "List Data");
            this.model = model;
        }

        public DefaultListModel getModel() {
            return model;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((model == null) ? 0 : model.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ListMoveDataFlavor other = (ListMoveDataFlavor) obj;
            if (this.model != other.model && (this.model == null || !this.model.equals(other.model))) {
                return false;
            }
            return true;
        }

        @Override
        public boolean equals(DataFlavor that) {
            if (this == that) {
                return true;
            }
            if (!super.equals(that) || getClass() != that.getClass()) {
                return false;
            }
            return match(model, that);
        }

        /**
         * Tests whether the given data flavor is a {@link ListMoveDataFlavor}
         * and matches the given model.
         *
         * @param model the model
         * @param flavor the flavor
         * @return {@code true} if matches
         */
        public static boolean match(DefaultListModel model, DataFlavor flavor) {
            return flavor instanceof ListMoveDataFlavor && ((ListMoveDataFlavor) flavor).getModel() == model;
        }
    }

    /**
     * Model bound and index based transfer data.
     *
     * @author Sebastian Haufe
     */
    private static class ListMoveTransferData {

        private final DefaultListModel model;
        private final int[] indices;

        ListMoveTransferData(DefaultListModel model, int[] indices) {
            this.model = model;
            this.indices = indices;
        }

        int[] getIndices() {
            return indices;
        }

        public DefaultListModel getModel() {
            return model;
        }
    }

    /**
     * Model bound transferable implementation.
     *
     * @author Sebastian Haufe
     */
    static class ListMoveTransferable implements Transferable {

        private final ListMoveTransferData data;

        public ListMoveTransferable(ListMoveTransferData data) {
            this.data = data;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{new ListMoveDataFlavor(data.getModel())};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return ListMoveDataFlavor.match(data.getModel(), flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }
    }

    /**
     * List transfer handler.
     *
     * @author Sebastian Haufe
     */
    static class ListMoveTransferHandler extends TransferHandler {

        /**
         * Serial version UID
         */
        private static final long serialVersionUID = 6703461043403098490L;

        @Override
        public int getSourceActions(JComponent c) {
            final JList list = (JList) c;
            return list.getModel() instanceof DefaultListModel ? MOVE : NONE;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            if (!(comp instanceof JList) || !(((JList) comp).getModel() instanceof DefaultListModel)) {
                return false;
            }

            final DefaultListModel model =
                    (DefaultListModel) ((JList) comp).getModel();
            for (DataFlavor f : transferFlavors) {
                if (ListMoveDataFlavor.match(model, f)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            final JList list = (JList) c;
            final int[] selectedIndices = list.getSelectedIndices();
            return new ListMoveTransferable(new ListMoveTransferData(
                    (DefaultListModel) list.getModel(), selectedIndices));
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport info) {
            final Component comp = info.getComponent();
            if (!info.isDrop() || !(comp instanceof JList)) {
                return false;
            }
            final JList list = (JList) comp;
            final ListModel lm = list.getModel();
            if (!(lm instanceof DefaultListModel)) {
                return false;
            }

            final DefaultListModel listModel = (DefaultListModel) lm;
            final DataFlavor flavor = new ListMoveDataFlavor(listModel);
            if (!info.isDataFlavorSupported(flavor)) {
                return false;
            }

            final Transferable transferable = info.getTransferable();
            try {
                final ListMoveTransferData data = (ListMoveTransferData) transferable.getTransferData(flavor);

                // get the initial insertion index
                final JList.DropLocation dropLocation = list.getDropLocation();
                int insertAt = dropLocation.getIndex();

                // get the indices sorted (we use them in reverse order, below)
                final int[] indices = data.getIndices();
                Arrays.sort(indices);

                // remove old elements from model, store them on stack
                final Stack<Object> elements = new Stack<Object>();
                int shift = 0;
                for (int i = indices.length - 1; i >= 0; i--) {
                    final int index = indices[i];
                    if (index < insertAt) {
                        shift--;
                    }
                    elements.push(listModel.remove(index));
                }
                insertAt += shift;

                // insert stored elements from stack to model
                final ListSelectionModel sm = list.getSelectionModel();
                try {
                    sm.setValueIsAdjusting(true);
                    sm.clearSelection();
                    final int anchor = insertAt;
                    while (!elements.isEmpty()) {
                        listModel.insertElementAt(elements.pop(), insertAt);
                        sm.addSelectionInterval(insertAt, insertAt++);
                    }
                    final int lead = insertAt - 1;
                    if (!sm.isSelectionEmpty()) {
                        sm.setAnchorSelectionIndex(anchor);
                        sm.setLeadSelectionIndex(lead);
                    }
                } finally {
                    sm.setValueIsAdjusting(false);
                }
                return true;
            } catch (UnsupportedFlavorException ex) {
                return false;
            } catch (IOException ex) {
                // FIXME: Logging
                return false;
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel dataSetOrderPanel;
    private javax.swing.JRadioButton dateBandRadioButton;
    private javax.swing.JCheckBox domainAutoCheckBox;
    private javax.swing.JTextField domainInterval;
    private javax.swing.JTextField domainMax;
    private javax.swing.JTextField domainMin;
    private javax.swing.JPanel domainPanel;
    private javax.swing.JTextField fontSizeTextField;
    private javax.swing.JTextField fontSizeTextFieldLYAxis;
    private javax.swing.JTextField fontSizeTextFieldMain;
    private javax.swing.JTextField fontSizeTextFieldRYAxis;
    private javax.swing.JTextField fontSizeTextFieldSub1;
    private javax.swing.JTextField fontSizeTextFieldSub2;
    private javax.swing.JTextField fontSizeTextFieldxAxis;
    private javax.swing.JPanel gridPanelCategory;
    private javax.swing.JPanel gridPanelDate;
    private javax.swing.JPanel gridPanelNumber;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JComboBox intervalSelectorComboBox;
    private javax.swing.JCheckBox jCheckBox11;
    private javax.swing.JCheckBox jCheckBox12;
    private javax.swing.JComboBox jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JComboBox leftYAxisFontStyle;
    private javax.swing.JComboBox mainFontStyle;
    private javax.swing.JTextField mainTitle;
    private javax.swing.JTextField numberDomainIntervalTextField;
    private javax.swing.JTextField numberRangeIntervalTextField;
    private javax.swing.JTextField rangeIntervalTextField;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox rightYAxisFontStyle;
    private javax.swing.JPanel scalePanel;
    private javax.swing.JCheckBox showChartRemarksCheckBox;
    private javax.swing.JCheckBox showLegendCheckBox;
    private javax.swing.JRadioButton simpleAxisRadioButton;
    private javax.swing.JTextField simpleIntervalTextField;
    private javax.swing.JComboBox sub1FontStyle;
    private javax.swing.JComboBox sub2FontStyle;
    private javax.swing.JTextField subTitle1;
    private javax.swing.JTextField subTitle2;
    private javax.swing.JTabbedPane tabPanel;
    private javax.swing.JPanel timeAxisPanel;
    private javax.swing.JPanel titlePanel;
    private javax.swing.JCheckBox useAntiAliasingCheckBox;
    private javax.swing.JCheckBox useDays;
    private javax.swing.JCheckBox useDomainGridNumberCheckBox;
    private javax.swing.JCheckBox useGridDateCheckBox;
    private javax.swing.JCheckBox useHours;
    private javax.swing.JCheckBox useMinutes;
    private javax.swing.JCheckBox useMonthYear;
    private javax.swing.JCheckBox useRangeGridCheckBox;
    private javax.swing.JCheckBox useRangeGridNumberCheckBox;
    private javax.swing.JTextField widthTextField;
    private javax.swing.JTextField xAxis;
    private javax.swing.JComboBox xAxisFontStyle;
    private javax.swing.JCheckBox y1AutoCheckBox;
    private javax.swing.JTextField y1Axis;
    private javax.swing.JTextField y1Interval;
    private javax.swing.JTextField y1Max;
    private javax.swing.JTextField y1Min;
    private javax.swing.JCheckBox y2AutoCheckBox;
    private javax.swing.JTextField y2Axis;
    private javax.swing.JTextField y2Interval;
    private javax.swing.JTextField y2Max;
    private javax.swing.JTextField y2Min;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
