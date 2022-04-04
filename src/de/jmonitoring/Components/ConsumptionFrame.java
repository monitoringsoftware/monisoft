package de.jmonitoring.Components;

import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.Cluster.Cluster;
import de.jmonitoring.DataHandling.*;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DataHandling.ClimateFactorHandler;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.Cluster.ClusterInformation;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.*;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.Statistics;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author togro
 */
public class ConsumptionFrame extends javax.swing.JInternalFrame {

    final static int CONSUMPTION_CATEGORY = 0;
    final static int BUILDING_CATEGORY = 1;
    private Color[] colorTable = {
        new Color(219, 15, 15),
        new Color(0, 0, 215),
        new Color(2, 196, 2),
        new Color(255, 153, 0),
        new Color(129, 12, 12),
        new Color(3, 46, 131),
        new Color(2, 120, 2),
        new Color(255, 203, 70),
        new Color(152, 98, 203),
        new Color(0, 187, 157),
        new Color(158, 154, 76),
        new Color(0, 152, 255),
        new Color(254, 241, 130),
        new Color(255, 97, 82)
    };
    private ArrayList<Color> colorGradient = new ArrayList<Color>();
    private HashMap<String, Integer> colorMapForBuildingCAT = new HashMap<String, Integer>();
    private HashMap<String, Integer> colorMapForConsumptionCAT = new HashMap<String, Integer>();
    private CategoryPlot currentPlot = null;
    private int currentMode;
    private HashSet<String> categories = new HashSet<String>();
    private JFreeChart chart;
    private String referenceUnit = "";
    private final String GROUP = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsumptionFrame.GROUP");
    private HashSet<String> climateCorrectedCategories = new HashSet<String>();
    private final MainApplication gui;
    private ChartPanel cp;

    /**
     * Creates new form ConsumptionFrame
     */
    public ConsumptionFrame(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        buildingCATRadioButton.setSelected(true);

        categories = SensorCollectionHandler.getAllSensorCollectionNames(SensorCollectionHandler.COMPARE_COLLECTION);

        setCategoryList();
        setSorterList();
        setBuildingList();
        setReferenceList();
        setColors();

        referenceSelectorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                drawPlot(currentMode);
            }
        });

        drawPlot(BUILDING_CATEGORY);

    }

    public JFreeChart createChart(CategoryDataset dataset, boolean stacked, String showReferenceUnit) {
        String showUnit = (String) targetUnitSelector.getSelectedItem();
        BarRenderer renderer;
        if (stacked) { // Gestapelt
            chart = ChartFactory.createStackedBarChart(
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsumptionFrame.VERBRAUCHE"), // chart title
                    "", // domain axis label
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsupmtionFrame.VERBRAUCH") + " [" + showUnit + "]", // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    true, // legend
                    true, // tooltips
                    false // urls
                    );

            currentPlot = (CategoryPlot) chart.getPlot();


            StackedBarRenderer tempRenderer = (StackedBarRenderer) currentPlot.getRenderer();
            tempRenderer.setTotalFormatter(new DecimalFormat("0.#"));
            tempRenderer.setTotalLabelFont(new Font("Dialog", Font.BOLD, 9));
            tempRenderer.setShowNegativeTotal(showLabelCheckBox.isSelected());
            tempRenderer.setShowPositiveTotal(showLabelCheckBox.isSelected());
            tempRenderer.setBaseItemLabelsVisible(showLabelCheckBox.isSelected());
            renderer = tempRenderer;
        } else { // nebeneinander
            chart = ChartFactory.createBarChart(
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsumptionFrame.VERBRAUCHE"), // chart title
                    "", // domain axis label
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsupmtionFrame.VERBRAUCH") + " [" + showUnit + "]", // range axis label
                    dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    true, // legend
                    true, // tooltips
                    false // urls
                    );

            currentPlot = (CategoryPlot) chart.getPlot();

            renderer = (BarRenderer) currentPlot.getRenderer();
        }

        String labelUnit;
        if (useReferenceCheckBox.isSelected()) {
            String yearRefLabel = "";
            if (useYearReferenceCheckBox.isSelected() && useYearReferenceCheckBox.isEnabled()) {
                yearRefLabel = " a";
            }
            labelUnit = " [" + showUnit + " / " + showReferenceUnit + " " + referenceSelectorList.getSelectedValue() + yearRefLabel + "]";
        } else {
            labelUnit = " [" + showUnit + "]";
        }

        currentPlot.getRangeAxis(0).setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsupmtionFrame.VERBRAUCH") + labelUnit);

        chart.setSubtitles(new ArrayList(Arrays.asList(new TextTitle(makeTitle(), new Font("Dialog", Font.PLAIN, 10)))));

        chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 12));
        chart.setBackgroundPaint(new Color(238, 238, 238));
        currentPlot.setBackgroundPaint(Color.WHITE);
        currentPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        currentPlot.getRangeAxis().setTickLabelFont(new Font("Dialog", 0, 9));
        currentPlot.getDomainAxis().setTickLabelFont(new Font("Dialog", 0, 9));


        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter()); // sonst Bonbon-Effekt....
        renderer.setBaseItemLabelsVisible(showLabelCheckBox.isSelected());
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(",##0.0")));
        renderer.setBaseItemLabelFont(new Font("Dialog", 0, 8));
        renderer.setShadowVisible(false);


        try {
            for (int i = 0; i < dataset.getRowCount(); i++) {
                if (currentMode == CONSUMPTION_CATEGORY) {
                    colorMapForConsumptionCAT = new HashMap<String, Integer>();
                    int index;
                    int step = (1020 / buildingSelectorList.getModel().getSize());
                    for (int b = 0; b < buildingSelectorList.getModel().getSize(); b++) {
                        String building = ((CheckListItem) buildingSelectorList.getModel().getElementAt(b)).getLabel();
                        index = b * step < colorMapForConsumptionCAT.size() ? b * step : (b * step) - colorMapForConsumptionCAT.size();
                        colorMapForConsumptionCAT.put(building, index);
                    }
                    renderer.setSeriesPaint(i, colorGradient.get(colorMapForConsumptionCAT.get((String) dataset.getRowKey(i))));
//                    renderer.setSeriesPaint(i, colorTable[colorMapForConsumptionCAT.get((String) dataset.getRowKey(i))]);
                    renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{1} ({0}) = {2} " + labelUnit, new DecimalFormat("0.##")));
                } else {
                    renderer.setSeriesPaint(i, colorTable[colorMapForBuildingCAT.get((String) dataset.getRowKey(i))]);
                    renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator("{0} ({1}) = {2} " + labelUnit, new DecimalFormat("0.##")));
                }
            }
        } catch (Exception e) {
            Messages.showException(e);
        }

        return chart;
    }

    public CategoryDataset createDataset(int type, boolean sort, boolean climatecorrection) {

        DefaultCategoryDataset result = new DefaultCategoryDataset();

        // Alle Gebäude in der Auswahlliste durchlaufen
        DatabaseQuery dq;
        String refValue = null;
        TreeSet<CategoryDatasetValue> sortedDataSet = new TreeSet<CategoryDatasetValue>();
        climateCorrectedCategories.clear();


        // alle gebäude ermitteln die dargestellt werden sollten
        TreeSet<String> buildings = new TreeSet<String>();
        if (buildingRadioButton.isSelected()) {
            for (int b = 0; b < buildingSelectorList.getModel().getSize(); b++) {
                CheckListItem buildingItem = (CheckListItem) buildingSelectorList.getModel().getElementAt(b);
                if (buildingItem.isSelected) {
                    buildings.add(buildingItem.getLabel());
                }
            }
        } else {
//            System.out.println(((String) buildingSelectorList.getSelectedValue()).split(" Gruppe" )[0]);
            Cluster cluster = ClusterInformation.getCluster(((String) buildingSelectorList.getSelectedValue()).split(" " + GROUP + " ")[0]);
            Integer group = Integer.valueOf(((String) buildingSelectorList.getSelectedValue()).split(" " + GROUP + " ")[1]);
//            System.out.println(cluster + "group " + group);
            TreeSet<Integer> clusterBuildings = cluster.getBuildingsForGroup(group);
            for (Integer id : clusterBuildings) {
                buildings.add(BuildingInformation.getBuildingNameFromID(id));
//                System.out.println(BuildingInformation.getBuildingNameFromID(id));
            }
        }

        // alle Gebäude durchlaufen
        for (String building : buildings) {
            //Alle Verbrauchskategorien für dieses Gebäude durchlaufen
            for (String category : categories) {
                for (int i = 0; i < categorySelectorList.getModel().getSize(); i++) {
                    CheckListItem categoryItem = (CheckListItem) categorySelectorList.getModel().getElementAt(i);
                    if (categoryItem.getLabel().equals(category) && categoryItem.isSelected) {
                        Integer sensorID = SensorInformation.getSensorIDForConsumptionCategory(BuildingInformation.getBuildingIDFromName(building), category);
                        if (sensorID == null) {
                            continue;
                        }
                        dq = new DatabaseQuery(sensorID);

                        if (useReferenceCheckBox.isSelected() && referenceSelectorList.getModel().getSize() > 0) { // Wenn eine Bezugsgröße gewünscht diese anbringen
                            refValue = referenceSelectorList.getSelectedValue().toString();
                            setReferenceUnit(UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(refValue)));
                        } else {
                            setReferenceUnit("");
                        }

                        // get climate factor
                        Double climateFactor = null;
                        if (climatecorrection && SensorCollectionHandler.isClimateCorrectionCollection(category)) {
                            climateFactor = new ClimateFactorHandler(this.gui).getClimateFactor(BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(building)).getPlz(), jYearChooser1.getYear(), monthSelector.getSelectedIndex());
                            climateCorrectedCategories.add(category);
//                            System.out.println("Korrektur an  " + category + " für Gebäude " + building + " (PLZ " + BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(building)).getPlz() + "),  Jahr " + jYearChooser1.getYear() + ": " + climateFactor);
                        }

                        climateFactor = climateFactor == null ? 1.0 : climateFactor;

                        switch (type) {
                            case CONSUMPTION_CATEGORY:
                                result.addValue(dq.getValueFor(monthSelector.getSelectedIndex() + 1, jYearChooser1.getYear(), refValue, UnitInformation.getUnitFromName((String) targetUnitSelector.getSelectedItem()), climateFactor, fullYearCheckBox.isSelected(), useYearReferenceCheckBox.isSelected()), building, category);
                                break;
                            case BUILDING_CATEGORY:
                                if (sort) {
                                    CategoryDatasetValue dsv = new CategoryDatasetValue(dq.getValueFor(monthSelector.getSelectedIndex() + 1, jYearChooser1.getYear(), refValue, UnitInformation.getUnitFromName((String) targetUnitSelector.getSelectedItem()), climateFactor, fullYearCheckBox.isSelected(), useYearReferenceCheckBox.isSelected()), category, building);
                                    sortedDataSet.add(dsv);
                                } else {
                                    result.addValue(dq.getValueFor(monthSelector.getSelectedIndex() + 1, jYearChooser1.getYear(), refValue, UnitInformation.getUnitFromName((String) targetUnitSelector.getSelectedItem()), climateFactor, fullYearCheckBox.isSelected(), useYearReferenceCheckBox.isSelected()), category, building);
                                }
                                break;
                        }
                        continue;
                    }
                }
            }
        }


        if (sort) {
            String sortBy = (String) sortByComboBox.getSelectedItem();
            // Zuerst belegen des Datensatzes mit den Werte der zu sortierenden Kategorie: damit ergibt sich die Reihenfolge der Kategorien
            for (CategoryDatasetValue v : sortedDataSet) {
                if (v.getRow().equals(sortBy)) {
                    result.addValue(v.getValue(), v.getRow(), v.getColumn());
                }
            }

            // jetzt den Rest zuweisen
            for (CategoryDatasetValue v : sortedDataSet) {
                if (!v.getRow().equals(sortBy)) {
                    result.addValue(v.getValue(), v.getRow(), v.getColumn());
                }
            }
        }
        return result;
    }

    private String makeTitle() {
        String title = "";
        if (fullYearCheckBox.isSelected()) {
            if (monthSelector.getSelectedIndex() == 0) {  // whole year and month is january -> normal calendar year
                title = String.valueOf(jYearChooser1.getYear());
            } else {
                title = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("12MONTHSFROM") + " " + MoniSoftConstants.getMonthFor(monthSelector.getSelectedIndex()) + " " + jYearChooser1.getYear();
            }
        } else {
            title = MoniSoftConstants.getMonthFor(monthSelector.getSelectedIndex()) + " " + jYearChooser1.getYear();
        }
        return title;
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        categorySelectorList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        targetUnitSelector = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        buildingRadioButton = new javax.swing.JRadioButton();
        clusterRadioButton = new javax.swing.JRadioButton();
        selectAllCheckBox = new javax.swing.JCheckBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        buildingSelectorList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        monthSelector = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jYearChooser1 = new com.toedter.calendar.JYearChooser();
        fullYearCheckBox = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        useReferenceCheckBox = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        referenceSelectorList = new javax.swing.JList();
        useYearReferenceCheckBox = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        sortByComboBox = new javax.swing.JComboBox();
        categoryCATRadioButton = new javax.swing.JRadioButton();
        showLabelCheckBox = new javax.swing.JCheckBox();
        meanCheckbox = new javax.swing.JCheckBox();
        sortCheckBox = new javax.swing.JCheckBox();
        stackedCheckBox = new javax.swing.JCheckBox();
        climateCorrectionCheckBox = new javax.swing.JCheckBox();
        buildingCATRadioButton = new javax.swing.JRadioButton();
        cancelButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jLabel68 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setTitle(bundle.getString("ConsumptionFrame.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(930, 740));
        setPreferredSize(new java.awt.Dimension(930, 740));

        jPanel1.setMaximumSize(new java.awt.Dimension(900, 570));
        jPanel1.setMinimumSize(new java.awt.Dimension(900, 570));
        jPanel1.setPreferredSize(new java.awt.Dimension(900, 570));
        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(250, 499));

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ConsumptionFrame.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        jScrollPane1.setMaximumSize(new java.awt.Dimension(259, 150));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(259, 150));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(259, 150));

        categorySelectorList.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        categorySelectorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        categorySelectorList.setToolTipText(bundle.getString("ConsumptionFrame.categorySelectorList.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(categorySelectorList);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel5.setText(bundle.getString("ConsumptionFrame.jLabel5.text")); // NOI18N

        targetUnitSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        targetUnitSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Wh", "kWh", "MWh", "GWh" }));
        targetUnitSelector.setSelectedIndex(1);
        targetUnitSelector.setMinimumSize(new java.awt.Dimension(71, 19));
        targetUnitSelector.setPreferredSize(new java.awt.Dimension(71, 19));
        targetUnitSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetUnitSelectorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(targetUnitSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(42, 42, 42))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetUnitSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ConsumptionFrame.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        buttonGroup2.add(buildingRadioButton);
        buildingRadioButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        buildingRadioButton.setSelected(true);
        buildingRadioButton.setText(bundle.getString("ConsumptionFrame.buildingRadioButton.text")); // NOI18N
        buildingRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildingRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup2.add(clusterRadioButton);
        clusterRadioButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        clusterRadioButton.setText(bundle.getString("ConsumptionFrame.clusterRadioButton.text")); // NOI18N
        clusterRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clusterRadioButtonActionPerformed(evt);
            }
        });

        selectAllCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        selectAllCheckBox.setText(bundle.getString("ConsumptionFrame.selectAllCheckBox.text")); // NOI18N
        selectAllCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllCheckBoxActionPerformed(evt);
            }
        });

        jScrollPane2.setMaximumSize(new java.awt.Dimension(259, 150));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(259, 150));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(259, 150));

        buildingSelectorList.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        buildingSelectorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        buildingSelectorList.setToolTipText(bundle.getString("ConsumptionFrame.buildingSelectorList.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(buildingSelectorList);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(buildingRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clusterRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(selectAllCheckBox))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buildingRadioButton)
                    .addComponent(clusterRadioButton)
                    .addComponent(selectAllCheckBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ConsumptionFrame.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel6.setText(bundle.getString("ConsumptionFrame.jLabel6.text")); // NOI18N

        monthSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        monthSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        monthSelector.setMaximumSize(new java.awt.Dimension(32767, 19));
        monthSelector.setMinimumSize(new java.awt.Dimension(60, 19));
        monthSelector.setPreferredSize(new java.awt.Dimension(60, 19));
        monthSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthSelectorActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel4.setText(bundle.getString("ConsumptionFrame.jLabel4.text")); // NOI18N

        jYearChooser1.setToolTipText(bundle.getString("ConsumptionFrame.jYearChooser1.toolTipText")); // NOI18N
        jYearChooser1.setEndYear(2050);
        jYearChooser1.setMinimumSize(new java.awt.Dimension(35, 18));
        jYearChooser1.setPreferredSize(new java.awt.Dimension(35, 19));
        jYearChooser1.setStartYear(2000);
        jYearChooser1.setValue(2009);
        jYearChooser1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jYearChooser1PropertyChange(evt);
            }
        });

        fullYearCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fullYearCheckBox.setSelected(true);
        fullYearCheckBox.setText(bundle.getString("ConsumptionFrame.fullYearCheckBox.text")); // NOI18N
        fullYearCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        fullYearCheckBox.setMinimumSize(new java.awt.Dimension(77, 17));
        fullYearCheckBox.setPreferredSize(new java.awt.Dimension(77, 17));
        fullYearCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullYearCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(monthSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fullYearCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(fullYearCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ConsumptionFrame.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        useReferenceCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useReferenceCheckBox.setText(bundle.getString("ConsumptionFrame.useReferenceCheckBox.text")); // NOI18N
        useReferenceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useReferenceCheckBoxActionPerformed(evt);
            }
        });

        jScrollPane3.setMaximumSize(new java.awt.Dimension(259, 115));
        jScrollPane3.setMinimumSize(new java.awt.Dimension(259, 115));
        jScrollPane3.setPreferredSize(new java.awt.Dimension(259, 115));

        referenceSelectorList.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        referenceSelectorList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "m² NGF" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        referenceSelectorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        referenceSelectorList.setToolTipText(bundle.getString("ConsumptionFrame.referenceSelectorList.toolTipText")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, useReferenceCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), referenceSelectorList, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        referenceSelectorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                referenceSelectorListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(referenceSelectorList);

        useYearReferenceCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useYearReferenceCheckBox.setText(bundle.getString("ConsumptionFrame.useYearReferenceCheckBox.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, useReferenceCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useYearReferenceCheckBox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useYearReferenceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useYearReferenceCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(useReferenceCheckBox)
                .addContainerGap())
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(useYearReferenceCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(useReferenceCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, Short.MAX_VALUE)
                .addComponent(useYearReferenceCheckBox))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(185, 185, 185))
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);

        jPanel4.setMinimumSize(new java.awt.Dimension(0, 900));
        jPanel4.setPreferredSize(new java.awt.Dimension(0, 900));
        jPanel4.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 35));
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 115));
        jPanel2.setPreferredSize(new java.awt.Dimension(847, 115));

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ConsumptionFrame.jPanel9.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        sortByComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        sortByComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        sortByComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        sortByComboBox.setPreferredSize(new java.awt.Dimension(71, 19));
        sortByComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByComboBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(categoryCATRadioButton);
        categoryCATRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        categoryCATRadioButton.setText(bundle.getString("ConsumptionFrame.categoryCATRadioButton.text")); // NOI18N
        categoryCATRadioButton.setToolTipText(bundle.getString("ConsumptionFrame.categoryCATRadioButton.toolTipText")); // NOI18N
        categoryCATRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                categoryCATRadioButtonActionPerformed(evt);
            }
        });

        showLabelCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showLabelCheckBox.setSelected(true);
        showLabelCheckBox.setText(bundle.getString("ConsumptionFrame.showLabelCheckBox.text")); // NOI18N
        showLabelCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLabelCheckBoxActionPerformed(evt);
            }
        });

        meanCheckbox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        meanCheckbox.setText(bundle.getString("ConsumptionFrame.meanCheckbox.text")); // NOI18N
        meanCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meanCheckboxActionPerformed(evt);
            }
        });

        sortCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        sortCheckBox.setText(bundle.getString("ConsumptionFrame.sortCheckBox.text")); // NOI18N
        sortCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortCheckBoxActionPerformed(evt);
            }
        });

        stackedCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        stackedCheckBox.setText(bundle.getString("ConsumptionFrame.stackedCheckBox.text")); // NOI18N
        stackedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stackedCheckBoxActionPerformed(evt);
            }
        });

        climateCorrectionCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        climateCorrectionCheckBox.setText(bundle.getString("ConsumptionFrame.climateCorrectionCheckBox.text")); // NOI18N
        climateCorrectionCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                climateCorrectionCheckBoxActionPerformed(evt);
            }
        });

        buttonGroup1.add(buildingCATRadioButton);
        buildingCATRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        buildingCATRadioButton.setText(bundle.getString("ConsumptionFrame.buildingCATRadioButton.text")); // NOI18N
        buildingCATRadioButton.setToolTipText(bundle.getString("ConsumptionFrame.buildingCATRadioButton.toolTipText")); // NOI18N
        buildingCATRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildingCATRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buildingCATRadioButton)
                    .addComponent(categoryCATRadioButton))
                .addGap(73, 73, 73)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(meanCheckbox)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(stackedCheckBox)
                        .addGap(116, 116, 116)
                        .addComponent(climateCorrectionCheckBox)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(showLabelCheckBox)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(sortCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sortByComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(stackedCheckBox)
                            .addComponent(sortCheckBox)
                            .addComponent(sortByComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(climateCorrectionCheckBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(meanCheckbox)
                            .addComponent(showLabelCheckBox)))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(buildingCATRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(categoryCATRadioButton)))
                .addGap(2, 2, 2))
        );

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        cancelButton.setText(bundle.getString("ConsumptionFrame.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        printButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        printButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/printer.png"))); // NOI18N
        printButton.setText(bundle.getString("ConsumptionFrame.printButton.text")); // NOI18N
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(655, Short.MAX_VALUE)
                        .addComponent(printButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(printButton)
                    .addComponent(cancelButton))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel18.setBackground(new java.awt.Color(0, 102, 204));
        jPanel18.setPreferredSize(new java.awt.Dimension(100, 40));

        jLabel68.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel68.setForeground(new java.awt.Color(255, 255, 255));
        jLabel68.setText(bundle.getString("ConsumptionFrame.jLabel68.text")); // NOI18N

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);
        jButton7.setIconTextGap(0);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7help(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel68, javax.swing.GroupLayout.PREFERRED_SIZE, 502, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 352, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel68, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel18Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );

        getContentPane().add(jPanel18, java.awt.BorderLayout.PAGE_START);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void categoryCATRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryCATRadioButtonActionPerformed
        sortCheckBox.setSelected(false);
        sortCheckBox.setEnabled(false);
        sortByComboBox.setEnabled(false);
        drawPlot(CONSUMPTION_CATEGORY);
    }//GEN-LAST:event_categoryCATRadioButtonActionPerformed

    private void buildingCATRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildingCATRadioButtonActionPerformed
        sortCheckBox.setEnabled(true);
        sortByComboBox.setEnabled(true);
        drawPlot(BUILDING_CATEGORY);
    }//GEN-LAST:event_buildingCATRadioButtonActionPerformed

    private void stackedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stackedCheckBoxActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_stackedCheckBoxActionPerformed

    private void drawPlot(int mode) {
        currentMode = mode;
        chart = createChart(createDataset(currentMode, sortCheckBox.isSelected(), climateCorrectionCheckBox.isSelected()), stackedCheckBox.isSelected(), getReferenceUnit());

        if (climateCorrectedCategories.size() > 0) {
            String correctedString = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CLIMATE_CORRECTION_FOR") + ": ";
            String sep = "";
            for (String cat : climateCorrectedCategories) {
                correctedString += sep + "'" + cat + "'";
                sep = ", ";
            }
            chart.addSubtitle(new TextTitle(correctedString, new Font("Dialog", Font.PLAIN, 10)));
        }

        updateMeanMarker();
        jPanel4.removeAll();
        cp = new ChartPanel(chart);
        cp.setMaximumDrawHeight(2000);
        cp.setMaximumDrawWidth(2000);
        jPanel4.add(cp, BorderLayout.CENTER);
        jPanel4.revalidate();


    }

    public String getReferenceUnit() {
        return referenceUnit;
    }

    public void setReferenceUnit(String referenceUnit) {
        this.referenceUnit = referenceUnit;
    }

    private void updateMeanMarker() {
        currentPlot.clearRangeMarkers();
        if (meanCheckbox.isSelected()) {
            String rowKey;
            String categoryKey;
            Double mean;
            Double sum;
            ValueMarker v;
            ArrayList<Number> values = new ArrayList<Number>();
            boolean stacked = stackedCheckBox.isSelected();
            DecimalFormat df = new DecimalFormat("0.0");

            if (stacked) {
                for (Object col : currentPlot.getDataset(0).getColumnKeys()) { // alle Kategorien durchlaufen
                    sum = 0d;
                    categoryKey = (String) col;

                    for (Object o : currentPlot.getDataset(0).getRowKeys()) { //  Datenreihen durchlaufen
                        rowKey = (String) o;
                        sum += (Double) currentPlot.getDataset(0).getValue(rowKey, categoryKey);
                    }
                    values.add(sum);
                }
                Number[] numbers = new Number[values.size()];
                mean = Statistics.calculateMean(values.toArray(numbers));
                v = new ValueMarker(mean, Color.BLACK, new BasicStroke(3.0f));
                v.setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsumptionFrame.MEAN") + " " + df.format(mean));
                v.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                v.setAlpha(0.6f);
                currentPlot.addRangeMarker(v);
            } else {
                for (Object o : currentPlot.getDataset(0).getRowKeys()) { //  Datenreihen durchlaufen
                    rowKey = (String) o;
                    Color paint = (Color) currentPlot.getRenderer().getSeriesPaint(currentPlot.getDataset(0).getRowIndex(rowKey));
                    values.clear();
                    for (Object col : currentPlot.getDataset(0).getColumnKeys()) { // alle Kategorien durchlaufen
                        categoryKey = (String) col;
                        values.add(currentPlot.getDataset(0).getValue(rowKey, categoryKey));
                    }
                    Number[] numbers = new Number[values.size()];
                    mean = Statistics.calculateMean(values.toArray(numbers));
                    v = new ValueMarker(mean, paint, new BasicStroke(3.0f));
                    v.setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsumptionFrame.MEAN") + " " + df.format(mean));
                    v.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
                    v.setAlpha(0.6f);
                    currentPlot.addRangeMarker(v);
                }
            }
        } else {
            currentPlot.clearRangeMarkers();
        }
    }

    private void setBuildingList() {
        DefaultListModel model = new DefaultListModel();
        CheckListItem cli;

        TreeSet<String> availableBuildings = getAvailableBuildings();
        for (String building : availableBuildings) {
            cli = new CheckListItem(building, " (" + BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(building)).getBuildingDescription() + ")");
            cli.setSelected(false);
            model.addElement(cli);
        }
        buildingSelectorList.setModel(model);
        buildingSelectorList.setCellRenderer(new CheckListRenderer());
        removeMouseListener();
        setCheckBoxMouseListener();
    }

    private void setColors() {
        int i = 0;
        for (String c : categories) {
            colorMapForBuildingCAT.put(c, i);
            i++;
        }
        colorGradient = fillColorArray();
    }

    public ArrayList<Color> fillColorArray() {
        ArrayList<Color> colorList = new ArrayList();
        int r = 255;
        int g = 0;
        int b = 0;
        boolean change;

        for (int i = 0; i <= 1020; i++) {
            change = false;
            if ((r == 255) && (g < 255) && (b == 0)) {
                g++;
                change = true;
            }
            if ((r > 0) && (g == 255) && (b == 0) && !change) {
                r--;
                change = true;
            }
            if ((r == 0) && (g == 255) && (b < 255) && !change) {
                b++;
                change = true;
            }
            if ((r == 0) && (g > 0) && (b == 255) && !change) {
                g--;
            }
            colorList.add(new Color(r, g, b));
        }
        return colorList;
    }

    private void setCategoryList() {
        DefaultListModel model = new DefaultListModel();
        CheckListItem cli;

        for (String s : categories) {
            cli = new CheckListItem(s, "");
            cli.setSelected(false);
            model.addElement(cli);
        }

        categorySelectorList.setModel(model);
        categorySelectorList.setCellRenderer(new CheckListRenderer());
        categorySelectorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();

                // Get index of item clicked
                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);

                // Toggle selected state
                item.setSelected(!item.isSelected());

                // Repaint cell
                list.repaint(list.getCellBounds(index, index));
                if (buildingRadioButton.isSelected()) {
                    setBuildingList();
                }
                selectAllCheckBox.setSelected(false);
                drawPlot(currentMode);
            }
        });
    }

    private void setSorterList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (String s : categories) {
            model.addElement(s);
        }

        sortByComboBox.setModel(model);
    }

    private void setReferenceList() {
        DefaultListModel model = new DefaultListModel();

        TreeSet availableRefs = getAvailableReferences();
        Iterator it = availableRefs.iterator();
        String s = "";
        while (it.hasNext()) {
            s = it.next().toString();
            model.addElement(s);
        }
        referenceSelectorList.setModel(model);
        referenceSelectorList.setSelectedIndex(0);

        if (model.isEmpty()) {
            useReferenceCheckBox.setSelected(false);
            useReferenceCheckBox.setEnabled(false);
        } else {
            useReferenceCheckBox.setEnabled(true);
        }
    }

    private TreeSet getAvailableReferences() {
        TreeSet refList = new TreeSet();

        // Aktuelle gebäude ermitteln
        TreeSet<String> buildings = new TreeSet<String>();
        if (buildingRadioButton.isSelected()) {
            for (int b = 0; b < buildingSelectorList.getModel().getSize(); b++) {
                CheckListItem buildingItem = (CheckListItem) buildingSelectorList.getModel().getElementAt(b);
                if (buildingItem.isSelected) {
                    buildings.add(buildingItem.getLabel());
                }
            }
        } else {
            Cluster cluster = ClusterInformation.getCluster(((String) buildingSelectorList.getSelectedValue()).split(" " + GROUP + " ")[0]);
            Integer group = Integer.valueOf(((String) buildingSelectorList.getSelectedValue()).split(" " + GROUP + " ")[1]);
            TreeSet<Integer> clusterBuildings = cluster.getBuildingsForGroup(group);
            for (Integer id : clusterBuildings) {
                buildings.add(BuildingInformation.getBuildingNameFromID(id));
//                System.out.println(BuildingInformation.getBuildingNameFromID(id));
            }
        }

        // Alle möglichen Referenzen holen
        Iterator<BuildingProperties> gebIt = BuildingInformation.getBuildingList().iterator();
        BuildingProperties props;
        ArrayList<ReferenceValue> references;
        while (gebIt.hasNext()) { // Alle Gebäude durchlaufen
            props = gebIt.next();
            references = BuildingInformation.getBuildingReferences(props.getBuildingID());
            for (ReferenceValue ref : references) {
                refList.add(ref.getName());
            }
        }

        Vector<String> currentRefs = new Vector<String>();
        // Ausgewählte Gebäude durchlaufen
        for (String building : buildings) {
            currentRefs.clear();
            references = BuildingInformation.getBuildingReferences(BuildingInformation.getBuildingIDFromName(building));
            // einzelne Referenzen für dieses Gebäude durchlaufen
            for (ReferenceValue ref : references) {
                currentRefs.add(ref.getName());
            }
            refList.retainAll(currentRefs);
        }
        return refList;
    }

    private TreeSet<String> getAvailableBuildings() {
        TreeSet<String> buildingList;
        String categoryName;

        // Alle möglichen Gebäude holen....
        buildingList = BuildingInformation.getBuildingNames();

        // Alle gewählten Verbrauchsklassen durchlaufen und für jedes Gebäude schauen, ob mindestens ein Wert vorhanden ist
        DefaultListModel categoryModel = (DefaultListModel) categorySelectorList.getModel();
        for (int i = 0; i < categoryModel.size(); i++) {
            CheckListItem item = (CheckListItem) categoryModel.get(i);
            if (item.isSelected) {
                categoryName = item.getLabel();
                buildingList.retainAll(SensorCollectionHandler.getInvolvedBuildingNames(categoryName));
            }
        }
        return buildingList;
    }

    private static class CheckListItem {

        private String label;
        private String description;
        private boolean isSelected = false;

        public CheckListItem(String label, String desc) {
            this.label = label;
            this.description = desc;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label + description;
        }
    }

    private static class CheckListRenderer extends JCheckBox implements ListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem) value).isSelected());
            setFont(list.getFont());
            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    public void setCheckBoxMouseListener() {
        buildingSelectorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {

                JList list = (JList) event.getSource();
                // Get index of item clicked
                int index = list.locationToIndex(event.getPoint());
                CheckListItem item = (CheckListItem) list.getModel().getElementAt(index);
                // Toggle selected state
                item.setSelected(!item.isSelected());

                // Repaint cell
                list.repaint(list.getCellBounds(index, index));

                setReferenceList();
                drawPlot(currentMode);
            }
        });
    }

    public void setDefaultMouseListener() {
        buildingSelectorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();
                // Get index of item clicked
                int index = list.locationToIndex(event.getPoint());
                // Repaint cell
                list.repaint(list.getCellBounds(index, index));

                setReferenceList();
                drawPlot(currentMode);
            }
        });
    }

    public void removeMouseListener() {
        buildingSelectorList.removeMouseListener(buildingSelectorList.getMouseListeners()[buildingSelectorList.getMouseListeners().length - 1]);
    }

    public static double randomValue(int n) {
        Random r = new Random();
        int result = r.nextInt(n - 20) + 20;
        return (int) result;
    }

    private void meanCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meanCheckboxActionPerformed
        updateMeanMarker();
    }//GEN-LAST:event_meanCheckboxActionPerformed

    private void sortCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortCheckBoxActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_sortCheckBoxActionPerformed

    private void showLabelCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLabelCheckBoxActionPerformed
        BarRenderer renderer = (BarRenderer) currentPlot.getRenderer();
        renderer.setBaseItemLabelsVisible(showLabelCheckBox.isSelected());
        if (renderer instanceof StackedBarRenderer) {
            StackedBarRenderer tempRenderer = (StackedBarRenderer) renderer;
            tempRenderer.setShowNegativeTotal(showLabelCheckBox.isSelected());
            tempRenderer.setShowPositiveTotal(showLabelCheckBox.isSelected());
            tempRenderer.setBaseItemLabelsVisible(showLabelCheckBox.isSelected());
            renderer = tempRenderer;
        }
    }//GEN-LAST:event_showLabelCheckBoxActionPerformed

    private void jYearChooser1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jYearChooser1PropertyChange
        drawPlot(currentMode);
    }//GEN-LAST:event_jYearChooser1PropertyChange

    private void useReferenceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useReferenceCheckBoxActionPerformed
        if (!useReferenceCheckBox.isSelected()) {
            useYearReferenceCheckBox.setSelected(false);
        }
        drawPlot(currentMode);
    }//GEN-LAST:event_useReferenceCheckBoxActionPerformed

    private void sortByComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByComboBoxActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_sortByComboBoxActionPerformed

    private void targetUnitSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetUnitSelectorActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_targetUnitSelectorActionPerformed

    private void selectAllCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllCheckBoxActionPerformed
        for (int i = 0; i < buildingSelectorList.getModel().getSize(); i++) {
            CheckListItem buildingItem = (CheckListItem) buildingSelectorList.getModel().getElementAt(i);
            buildingItem.setSelected(selectAllCheckBox.isSelected());
        }
        buildingSelectorList.repaint();
        setReferenceList();
        drawPlot(currentMode);
    }//GEN-LAST:event_selectAllCheckBoxActionPerformed

    private void clusterRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clusterRadioButtonActionPerformed
        selectAllCheckBox.setEnabled(false);
        // setze Clusterliste
        DefaultListModel model = new DefaultListModel();
        TreeMap<String, Cluster> clusterMap = ClusterInformation.getGlobalClusterMap();
        Cluster cluster;
        for (String clusterName : clusterMap.keySet()) {
            cluster = clusterMap.get(clusterName);
            for (int i = 1; i <= cluster.getGroupCount(); i++) {
                model.addElement(clusterName + " " + GROUP + " " + (i));
            }
        }
        buildingSelectorList.setCellRenderer(new DefaultListCellRenderer());
        removeMouseListener();
        setDefaultMouseListener();
        buildingSelectorList.setModel(model);
    }//GEN-LAST:event_clusterRadioButtonActionPerformed

    private void buildingRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildingRadioButtonActionPerformed
        selectAllCheckBox.setEnabled(true);
        selectAllCheckBox.setSelected(false);
        DefaultListModel model = new DefaultListModel();
        CheckListItem cli;

        TreeSet<String> availableBuildings = getAvailableBuildings();
        for (String building : availableBuildings) {
            cli = new CheckListItem(building, " (" + BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(building)).getBuildingDescription() + ")");
            cli.setSelected(false);
            model.addElement(cli);
        }
        buildingSelectorList.setModel(model);
        buildingSelectorList.setCellRenderer(new CheckListRenderer());
        removeMouseListener();
        setCheckBoxMouseListener();
    }//GEN-LAST:event_buildingRadioButtonActionPerformed

    private void climateCorrectionCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_climateCorrectionCheckBoxActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_climateCorrectionCheckBoxActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.CONSUMPTION_FRAME.getPage());
    }//GEN-LAST:event_jButton7help

    private void monthSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthSelectorActionPerformed
        drawPlot(currentMode);
    }//GEN-LAST:event_monthSelectorActionPerformed

    private void fullYearCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullYearCheckBoxActionPerformed
        if (fullYearCheckBox.isSelected()) {
            climateCorrectionCheckBox.setEnabled(true);
        } else {
            climateCorrectionCheckBox.setEnabled(false);
            climateCorrectionCheckBox.setSelected(false);
        }
        drawPlot(currentMode);
    }//GEN-LAST:event_fullYearCheckBoxActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
//        PrintUtilities.printComponent(jPanel4);
        cp.createChartPrintJob();
    }//GEN-LAST:event_printButtonActionPerformed

    private void referenceSelectorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_referenceSelectorListValueChanged
        if (evt.getValueIsAdjusting()) {
            drawPlot(currentMode);
        }
    }//GEN-LAST:event_referenceSelectorListValueChanged

    private void useYearReferenceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useYearReferenceCheckBoxActionPerformed
        if (useReferenceCheckBox.isSelected()) {
            drawPlot(currentMode);
        }
    }//GEN-LAST:event_useYearReferenceCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton buildingCATRadioButton;
    private javax.swing.JRadioButton buildingRadioButton;
    private javax.swing.JList buildingSelectorList;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton categoryCATRadioButton;
    private javax.swing.JList categorySelectorList;
    private javax.swing.JCheckBox climateCorrectionCheckBox;
    private javax.swing.JRadioButton clusterRadioButton;
    private javax.swing.JCheckBox fullYearCheckBox;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private com.toedter.calendar.JYearChooser jYearChooser1;
    private javax.swing.JCheckBox meanCheckbox;
    private javax.swing.JComboBox monthSelector;
    private javax.swing.JButton printButton;
    private javax.swing.JList referenceSelectorList;
    private javax.swing.JCheckBox selectAllCheckBox;
    private javax.swing.JCheckBox showLabelCheckBox;
    private javax.swing.JComboBox sortByComboBox;
    private javax.swing.JCheckBox sortCheckBox;
    private javax.swing.JCheckBox stackedCheckBox;
    private javax.swing.JComboBox targetUnitSelector;
    private javax.swing.JCheckBox useReferenceCheckBox;
    private javax.swing.JCheckBox useYearReferenceCheckBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
