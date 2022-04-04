/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Components;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DataHandling.*;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.PrintUtilities;
import de.jmonitoring.References.ReferenceDescription;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYBoxAnnotation;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.TextAnchor;

/**
 *
 * @author togro
 */
public class SectionChart extends javax.swing.JInternalFrame {

    private DescriptiveStatistics domainstats;
    private DescriptiveStatistics rangestats;
    private JFreeChart chart;
    private XYLineAndShapeRenderer renderer;
    private final MainApplication gui;
    private CtrlChartPanel chartPanel;

    /**
     * Creates new form SectionChart
     */
    public SectionChart(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setConsumptionCategories();
        setReferenceList();
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        yearChooserComboBox.setSelectedIndex(cal.get(Calendar.YEAR) - 2000);
        setClimateCorrectionCheckBoxState();
    }

    private void disposeMe() {
        dispose();
    }

    /**
     * Populates the comparecollection selector with the available collections
     */
    private void setConsumptionCategories() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(SensorCollectionHandler.getAllSensorCollectionNames(SensorCollectionHandler.COMPARE_COLLECTION).toArray());
        consumptionCategoryComboBox.setModel(model);
    }

    /**
     * Populates the reference selector with all possible reference values
     */
    private void setReferenceList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        TreeSet<ReferenceDescription> map = ReferenceInformation.getReferenceList();
        for (ReferenceDescription ref : map) {
            model.addElement(ref.getName() + " (" + ref.getDescription() + ")");
        }
        referenceComboBox.setModel(model);
    }

    private void createChart() {
        ArrayList<BuildingProperties> buildings = BuildingInformation.getBuildingList();
        String buildingName;
        Integer sensorID;
        int buildingID;
        DatabaseQuery dq;
        Double absoluteValue;
        Double specificValue;
        ArrayList<ReferenceValue> refList;
        boolean hasRef;
        String referenceString = ((String) referenceComboBox.getSelectedItem()).split(" ")[0];
        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        domainstats = new DescriptiveStatistics();
        rangestats = new DescriptiveStatistics();

        // loop all buildings and add their values to the series and the statistics
        Double climatefactor;
        for (BuildingProperties props : buildings) {
            hasRef = false;
            buildingName = props.getBuildingName();
            buildingID = props.getBuildingID();
            XYSeries series = new XYSeries(buildingName);

            refList = BuildingInformation.getBuildingReferences(buildingID); // Ermitteln aller zu diesem Gebäude gehörenden Bezugsgrößen
            if (refList != null) {
                for (ReferenceValue ref : refList) {
                    if (referenceString.equals(ref.getName())) {
                        hasRef = true;
                        break;
                    }
                }
            }

            if (!hasRef) {
                continue;
            }


            sensorID = SensorCollectionHandler.getCategorySensorForBuilding(buildingID, (String) consumptionCategoryComboBox.getSelectedItem()); // Sensor der für das Gebäude in der angegebenen Kategorie "zuständig"ist
            if (sensorID != null) {

                climatefactor = 1d;
                if (climateCorrectionCheckBox.isSelected()) {
                    climatefactor = new ClimateFactorHandler(this.gui).getClimateFactor(props.getPlz(), Integer.parseInt((String) yearChooserComboBox.getSelectedItem()), 0);
                }

                dq = new DatabaseQuery(sensorID);
                absoluteValue = dq.getValueFor(monthSelector.getSelectedIndex() + 1, Integer.parseInt((String) yearChooserComboBox.getSelectedItem()), null, UnitInformation.getUnitFromName((String) targetUnitComboBox.getSelectedItem()), climatefactor, true, false);
                specificValue = dq.getValueFor(monthSelector.getSelectedIndex() + 1, Integer.parseInt((String) yearChooserComboBox.getSelectedItem()), referenceString, UnitInformation.getUnitFromName((String) targetUnitComboBox.getSelectedItem()), climatefactor, true, true);
                series.add(specificValue, absoluteValue);
                rangestats.addValue(absoluteValue);
                domainstats.addValue(specificValue);
                seriesCollection.addSeries(series);
            }
        }

        // Einheiten holen
        String refUnit = UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(referenceString));

        // Bauen des Charts
        chart = ChartFactory.createXYLineChart(
                (String) consumptionCategoryComboBox.getSelectedItem(), // chart title
                java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SectionChart.SPECIFICUSAGE") + " " + "[" + (String) targetUnitComboBox.getSelectedItem() + "/" + refUnit + " " + referenceString + " a]", // x axis label
                java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SectionChart.TOTALUSAGE") + " " + "[" + (String) targetUnitComboBox.getSelectedItem() + "/a]", // y axis label
                seriesCollection, // data
                PlotOrientation.VERTICAL,
                false, // include legend
                true, // tooltips
                false // urls
                );

        chart.setSubtitles(new ArrayList(Arrays.asList(new TextTitle(makeTitle(), new Font("Dialog", Font.PLAIN, 10)))));



        XYPlot plot = chart.getXYPlot();
        renderer = new XYLineAndShapeRenderer(false, true);
        final ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.INSIDE2, TextAnchor.BOTTOM_LEFT, TextAnchor.CENTER_RIGHT, 0);
        for (int i = 0; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, getShape(0, 2));
            renderer.setSeriesPaint(i, Color.RED);
            renderer.setSeriesPositiveItemLabelPosition(i, p);
        }

        renderer.setBaseItemLabelFont(new Font("Dialog", Font.PLAIN, 8));
        renderer.setBaseItemLabelGenerator(new XYItemLabelGenerator() {
            @Override
            public String generateLabel(XYDataset dataset, int series, int item) {
                return (String) dataset.getSeriesKey(series);
            }
        });

        renderer.setBaseItemLabelsVisible(true);
        plot.setRenderer(renderer);

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.getRangeAxis().setLabelFont(new Font("Dialog", Font.BOLD, 10));
        plot.getDomainAxis().setLabelFont(new Font("Dialog", Font.BOLD, 10));
        plot.getDomainAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 9));
        plot.getRangeAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 9));
        plot.setBackgroundPaint(new Color(244, 244, 244));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        updateAnnotations();

        chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 12));
        chartPanel = new CtrlChartPanel(chart, this.gui);
        chartPanel.setMouseWheelEnabled(true);

        mainPanel.removeAll();
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        mainPanel.revalidate();
    }

    private String makeTitle() {
        String title = "";

        if (monthSelector.getSelectedIndex() == 0) {  // whole year and month is january -> normal calendar year
            title = String.valueOf(yearChooserComboBox.getSelectedItem());
        } else {
            title = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("12MONTHSFROM") + " " + MoniSoftConstants.getMonthFor(monthSelector.getSelectedIndex()) + " " + yearChooserComboBox.getSelectedItem();
        }

        return title;
    }

    /**
     * Returns a shape of the given type
     *
     * @param type
     * @param index
     * @return
     */
    private Shape getShape(int type, int index) {
        int size = index + 1; //
        float s;
        switch (type) {
            case 0:
                s = (size + 1f) / 2;
                return new Ellipse2D.Float(-s, -s, 2 * s, 2 * s);
            case 1:
                s = (size + 1f) / 2;
                return new Rectangle2D.Float(-s, -s, 2 * s, 2 * s);
            case 2:
                s = (size + 1f);
                double element = s * Math.sin(Math.PI / 4.);
                GeneralPath p = new GeneralPath();
                p.moveTo(0, -s);
                p.lineTo(element, element);
                p.lineTo(-element, element);
                p.lineTo(0, -s);
                return p;
        }
        return null;
    }

    /**
     * Removes all markers and annotations from the chart
     */
    private void removeMarkers() {
        XYPlot plot = chart.getXYPlot();
        ArrayList<ValueMarker> domainMarkers = new ArrayList<ValueMarker>(plot.getDomainMarkers(Layer.BACKGROUND));
        ArrayList<ValueMarker> rangeMarkers = new ArrayList<ValueMarker>(plot.getRangeMarkers(Layer.BACKGROUND));

        for (ValueMarker marker : domainMarkers) {
            plot.removeDomainMarker(marker, Layer.BACKGROUND);
        }

        for (ValueMarker marker : rangeMarkers) {
            plot.removeRangeMarker(marker, Layer.BACKGROUND);
        }
    }

    /**
     * Draws the lines defining the means or percentile values
     *
     * @param range
     * @param domain
     */
    private void drawMarkers(double range, double domain) {
        ValueMarker rangeMarker = new ValueMarker(range, Color.BLACK, new BasicStroke(2), null, null, 1f);
        ValueMarker domainMarker = new ValueMarker(domain, Color.BLACK, new BasicStroke(2), null, null, 1f);
        XYPlot plot = chart.getXYPlot();
        plot.addRangeMarker(rangeMarker, Layer.BACKGROUND);
        plot.addDomainMarker(domainMarker, Layer.BACKGROUND);
    }

    /**
     * Plots the colored BoxAnnotations (areas) which mark the 4 zones unsing
     * the given values
     *
     * @param rangeValue
     * @param domainValue
     */
    private void drawPercentileAnnotations(double rangeValue, double domainValue) {
        renderer.removeAnnotations();
        XYPlot plot = chart.getXYPlot();
        Paint zone1 = new Color(255, 0, 0, 60);
        Paint zone2 = new Color(0, 255, 0, 60);
        XYBoxAnnotation badAnnotation = new XYBoxAnnotation(domainValue, rangeValue, plot.getDomainAxis().getUpperBound(), plot.getRangeAxis().getUpperBound(), new BasicStroke(0.0f), zone1, zone1);
        XYBoxAnnotation goodAnnotation = new XYBoxAnnotation(0d, 0d, domainValue, rangeValue, new BasicStroke(0.0f), zone2, zone2);
        renderer.addAnnotation(badAnnotation, Layer.BACKGROUND);
        renderer.addAnnotation(goodAnnotation, Layer.BACKGROUND);
    }

    /**
     * Sets the climateCheckbox enabled if the comarecollection is climate
     * corrected collection.
     */
    private void setClimateCorrectionCheckBoxState() {
        if (SensorCollectionHandler.isClimateCorrectionCollection((String) consumptionCategoryComboBox.getSelectedItem())) {
            climateCorrectionCheckBox.setEnabled(true);
        } else {
            climateCorrectionCheckBox.setEnabled(false);
            climateCorrectionCheckBox.setSelected(false);
        }
    }

    /**
     * Updates (removes and sets new) all annotations and markers in the chart
     * according to the settign of the percentile sliders<p> The values are
     * calculated from the
     * <code>DescriptiveStatistics<code>-Objects for each axis<br>
     * Also prints the text below the sliders
     */
    private void updateAnnotations() {
        try {
            removeMarkers();
        } catch (NullPointerException e) {
        }

        if (rangeOffsetSlider.getValue() == 0 && domainOffsetSlider.getValue() == 0) {
            drawPercentileAnnotations(rangestats.getMean(), domainstats.getMean());
            drawMarkers(rangestats.getMean(), domainstats.getMean());
            rangePercentileLabel.setText("Mittelwert");
            domainPercentileLabel.setText("Mittelwert");
        } else if (rangeOffsetSlider.getValue() == 0) {
            drawPercentileAnnotations(rangestats.getMean(), domainstats.getPercentile(Math.abs(domainOffsetSlider.getValue())));
            drawMarkers(rangestats.getMean(), domainstats.getPercentile(Math.abs(domainOffsetSlider.getValue())));
            rangePercentileLabel.setText("Mittelwert");
            domainPercentileLabel.setText(domainOffsetSlider.getValue() + "%" + " " + "der Gebäude unterhalb");
        } else if (domainOffsetSlider.getValue() == 0) {
            drawPercentileAnnotations(rangestats.getPercentile(Math.abs(rangeOffsetSlider.getValue())), domainstats.getMean());
            drawMarkers(rangestats.getPercentile(Math.abs(rangeOffsetSlider.getValue())), domainstats.getMean());
            rangePercentileLabel.setText(rangeOffsetSlider.getValue() + "%" + " " + "der Gebäude unterhalb");
            domainPercentileLabel.setText("Mittelwert");
        } else {
            drawPercentileAnnotations(rangestats.getPercentile(rangeOffsetSlider.getValue()), domainstats.getPercentile(domainOffsetSlider.getValue()));
            drawMarkers(rangestats.getPercentile(rangeOffsetSlider.getValue()), domainstats.getPercentile(domainOffsetSlider.getValue()));
            rangePercentileLabel.setText(rangeOffsetSlider.getValue() + "%" + " " + "der Gebäude unterhalb");
            domainPercentileLabel.setText(domainOffsetSlider.getValue() + "%" + " " + "der Gebäude unterhalb");
        }

        // if sync-button selected keep in sync
        if (jToggleButton1.isSelected() && rangeOffsetSlider.getValue() != domainOffsetSlider.getValue()) {
            domainOffsetSlider.setValue(rangeOffsetSlider.getValue());
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

        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        domainPercentileLabel = new javax.swing.JLabel();
        domainOffsetSlider = new javax.swing.JSlider();
        jPanel5 = new javax.swing.JPanel();
        rangePercentileLabel = new javax.swing.JLabel();
        rangeOffsetSlider = new javax.swing.JSlider();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        yearChooserComboBox = new javax.swing.JComboBox();
        monthSelector = new javax.swing.JComboBox();
        jPanel7 = new javax.swing.JPanel();
        consumptionCategoryComboBox = new javax.swing.JComboBox();
        climateCorrectionCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        targetUnitComboBox = new javax.swing.JComboBox();
        jPanel8 = new javax.swing.JPanel();
        referenceComboBox = new javax.swing.JComboBox();
        mainPanel = new javax.swing.JPanel();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setTitle(bundle.getString("SectionChart.title")); // NOI18N
        setMinimumSize(new java.awt.Dimension(800, 670));
        setPreferredSize(new java.awt.Dimension(800, 670));

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText(bundle.getString("SectionChart.jLabel47.text")); // NOI18N

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

        javax.swing.GroupLayout headPanelLayout = new javax.swing.GroupLayout(headPanel);
        headPanel.setLayout(headPanelLayout);
        headPanelLayout.setHorizontalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 546, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );

        getContentPane().add(headPanel, java.awt.BorderLayout.PAGE_START);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 35));
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 35));
        jPanel2.setPreferredSize(new java.awt.Dimension(821, 35));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/printer.png"))); // NOI18N
        jButton2.setText(bundle.getString("SectionChart.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton1.setText(bundle.getString("SectionChart.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setMinimumSize(new java.awt.Dimension(200, 300));
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 522));

        refreshButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow_refresh.png"))); // NOI18N
        refreshButton.setText(bundle.getString("SectionChart.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("SectionChart.jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9), java.awt.Color.black)); // NOI18N

        domainPercentileLabel.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        domainPercentileLabel.setText(bundle.getString("SectionChart.domainPercentileLabel.text")); // NOI18N

        domainOffsetSlider.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        domainOffsetSlider.setMajorTickSpacing(25);
        domainOffsetSlider.setMinorTickSpacing(5);
        domainOffsetSlider.setPaintLabels(true);
        domainOffsetSlider.setPaintTicks(true);
        domainOffsetSlider.setSnapToTicks(true);
        domainOffsetSlider.setValue(0);
        domainOffsetSlider.setName(bundle.getString("SectionChart.domainOffsetSlider.name")); // NOI18N
        domainOffsetSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                domainOffsetSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(domainOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(domainPercentileLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(domainOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(domainPercentileLabel))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("SectionChart.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N

        rangePercentileLabel.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        rangePercentileLabel.setText(bundle.getString("SectionChart.rangePercentileLabel.text")); // NOI18N

        rangeOffsetSlider.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        rangeOffsetSlider.setMajorTickSpacing(25);
        rangeOffsetSlider.setMinorTickSpacing(5);
        rangeOffsetSlider.setPaintLabels(true);
        rangeOffsetSlider.setPaintTicks(true);
        rangeOffsetSlider.setSnapToTicks(true);
        rangeOffsetSlider.setValue(0);
        rangeOffsetSlider.setName(bundle.getString("SectionChart.rangeOffsetSlider.name")); // NOI18N
        rangeOffsetSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rangeOffsetSliderStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(rangeOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(rangePercentileLabel)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(rangeOffsetSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rangePercentileLabel))
        );

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chain.png"))); // NOI18N
        jToggleButton1.setText(bundle.getString("SectionChart.jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(bundle.getString("SectionChart.jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jToggleButton1.setPreferredSize(new java.awt.Dimension(22, 12));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("SectionChart.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel7.setText(bundle.getString("SectionChart.jLabel7.text")); // NOI18N

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel6.setText(bundle.getString("SectionChart.jLabel6.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("SectionChart.jLabel4.text")); // NOI18N

        yearChooserComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        yearChooserComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2000", "2001", "2002", "2003", "2004", "2005", "2006", "2007", "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        yearChooserComboBox.setFocusCycleRoot(true);
        yearChooserComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        yearChooserComboBox.setPreferredSize(new java.awt.Dimension(71, 19));

        monthSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        monthSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        monthSelector.setMinimumSize(new java.awt.Dimension(71, 19));
        monthSelector.setPreferredSize(new java.awt.Dimension(71, 19));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(monthSelector, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(yearChooserComboBox, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 10, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(monthSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yearChooserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("SectionChart.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        consumptionCategoryComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        consumptionCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        consumptionCategoryComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        consumptionCategoryComboBox.setPreferredSize(new java.awt.Dimension(71, 19));
        consumptionCategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consumptionCategoryComboBoxActionPerformed(evt);
            }
        });

        climateCorrectionCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        climateCorrectionCheckBox.setText(bundle.getString("SectionChart.climateCorrectionCheckBox.text")); // NOI18N
        climateCorrectionCheckBox.setToolTipText(bundle.getString("SectionChart.climateCorrectionCheckBox.toolTipText")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel5.setText(bundle.getString("SectionChart.jLabel5.text")); // NOI18N
        jLabel5.setToolTipText(bundle.getString("SectionChart.jLabel5.toolTipText")); // NOI18N

        targetUnitComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        targetUnitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Wh", "kWh", "MWh", "GWh" }));
        targetUnitComboBox.setSelectedIndex(1);
        targetUnitComboBox.setMinimumSize(new java.awt.Dimension(59, 18));
        targetUnitComboBox.setPreferredSize(new java.awt.Dimension(59, 18));
        targetUnitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetUnitComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(targetUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(climateCorrectionCheckBox)
                    .addComponent(consumptionCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 8, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(consumptionCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(climateCorrectionCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(targetUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("SectionChart.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        referenceComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        referenceComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        referenceComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        referenceComboBox.setPreferredSize(new java.awt.Dimension(71, 19));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(referenceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 8, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(referenceComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addGap(83, 83, 83)
                                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.LINE_START);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.BorderLayout());
        jPanel1.add(mainPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        disposeMe();     }//GEN-LAST:event_jButton1ActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        createChart();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
//        PrintUtilities.printComponent(mainPanel);
        chartPanel.createChartPrintJob();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void rangeOffsetSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rangeOffsetSliderStateChanged
        updateAnnotations();
    }//GEN-LAST:event_rangeOffsetSliderStateChanged

    private void domainOffsetSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_domainOffsetSliderStateChanged
        updateAnnotations();
    }//GEN-LAST:event_domainOffsetSliderStateChanged

    private void consumptionCategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consumptionCategoryComboBoxActionPerformed
        setClimateCorrectionCheckBoxState();
    }//GEN-LAST:event_consumptionCategoryComboBoxActionPerformed

    private void targetUnitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetUnitComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_targetUnitComboBoxActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.SECTION_CHART.getPage());
    }//GEN-LAST:event_jButton7help
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox climateCorrectionCheckBox;
    private javax.swing.JComboBox consumptionCategoryComboBox;
    private javax.swing.JSlider domainOffsetSlider;
    private javax.swing.JLabel domainPercentileLabel;
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JComboBox monthSelector;
    private javax.swing.JSlider rangeOffsetSlider;
    private javax.swing.JLabel rangePercentileLabel;
    private javax.swing.JComboBox referenceComboBox;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox targetUnitComboBox;
    private javax.swing.JComboBox yearChooserComboBox;
    // End of variables declaration//GEN-END:variables
}
