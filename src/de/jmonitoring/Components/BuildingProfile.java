/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/*
 * BuildingProfile.java
 *
 * Created on 29.09.2010, 09:42:12
 */
package de.jmonitoring.Components;

import de.jmonitoring.DataHandling.*;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.Cluster.ClusterInformation;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.jdesktop.swingx.JXImagePanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author togro
 */
public class BuildingProfile extends javax.swing.JPanel {

    private BufferedImage defaultimage;
    private ArrayList<String> consumptionCategories = new ArrayList<String>();
    private ArrayList<ReferenceValue> references = new ArrayList<ReferenceValue>();
    private HashMap<String, ReferenceValue> refMap = new HashMap<String, ReferenceValue>();
    private int currentBuilding;
    private boolean active = false;
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
    private final MainApplication gui;

    /**
     * Creates new form BuildingProfile
     */
    public BuildingProfile(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setBuildingList();

        jXImagePanel1.setComponentPopupMenu(new ImagePopup());
        try {
            defaultimage = ImageIO.read(getClass().getResource("/de/jmonitoring/icons/empty_building.jpg"));
        } catch (IOException ex) {
            System.out.println("Default picture not found");
        }
        buildingSelectorList.setSelectedIndex(0);
        fillProfile(BuildingInformation.getBuildingIDFromName((String) buildingSelectorList.getSelectedItem()));
        setClusterLabel();
    }

    class ImagePopup extends JPopupMenu {

        public ImagePopup() {
            super();
            init();
        }

        private void init() {
            JMenuItem loadItem = new JMenuItem("Bild laden");
            loadItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    loadImage();
                }
            });

            JMenuItem removeItem = new JMenuItem("Bild entfernen");
            removeItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    BuildingInformation.writeBuildingImage(currentBuilding, null);
                    showImage(currentBuilding);
                }
            });

            this.add(loadItem);
            this.add(removeItem);
        }
    }

    private void disposeMe() {
        Component c = this;
        while (c.getClass() != JInternalFrame.class) {
            c = c.getParent();
        }
        this.gui.disposeIFrame((JInternalFrame) c);
    }

    private void setBuildingList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        TreeSet<String> availableBuildings = BuildingInformation.getBuildingNames();
        for (String building : availableBuildings) {
            model.addElement(building);
        }
        buildingSelectorList.setModel(model);
    }

    private void fillProfile(int buildingID) {
        currentBuilding = buildingID;
        nRLabel.setText(BuildingInformation.getBuildingNameFromID(buildingID));
        namelabel.setText(BuildingInformation.getBuildingProperties(buildingID).getBuildingDescription());
        clusterText.setText("-");
        showImage(buildingID);
        showReferences(buildingID);
        showMapping(buildingID);
        setOverview();
        setClusterLabel();

    }

    private void setClusterLabel() {
        ArrayList<String> clusters = ClusterInformation.getClusterNamesForBuilding(BuildingInformation.getBuildingIDFromName((String) buildingSelectorList.getSelectedItem()));
        String clusterString = "";
        String sep = "";
        for (String cluster : clusters) {
            clusterString += sep + cluster;
            sep = ", ";
        }
        clusterText.setText(clusterString);
    }

    private void showImage(int buildingID) {
        jXImagePanel1.setStyle(JXImagePanel.Style.SCALED_KEEP_ASPECT_RATIO);

        BufferedImage img = BuildingInformation.readBuildingImage(buildingID);

        if (img != null) {
            jXImagePanel1.setImage(img);
        } else {
            jXImagePanel1.setImage(defaultimage);
        }

        jXImagePanel1.revalidate();
        jXImagePanel1.repaint();
    }

    private void showReferences(int buildingID) {
        DecimalFormat df = new DecimalFormat("########0.0");
        references = BuildingInformation.getBuildingReferences(buildingID);
        DefaultListModel model1 = new DefaultListModel();
        DefaultListModel model2 = new DefaultListModel();
        refMap.clear();
        for (ReferenceValue reference : references) {
            refMap.put(reference.getName(), reference);
            if (ReferenceInformation.getUnitIDForReference(reference.getName()) != null) {
                model1.addElement("<html><body><table><tr><td width = 120><b>" + reference.getName() + ":</b></td><td>" + df.format(reference.getValue()) + "</td><td>" + UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(reference.getName())) + "</td></tr></table></body></html>");
                model2.addElement(reference.getName());
            }
        }
        referenceList.setModel(model1);
        referenceSelectorList.setModel(model2);
        referenceSelectorList.setSelectedIndex(0);
    }

    private void showMapping(int buildingID) {
        String virtual = "";
        String name;
        DefaultListModel model = new DefaultListModel();
        HashSet<String> categorySet = BuildingInformation.getUsageCategoryNames(buildingID);
        for (String category : categorySet) {
            name = category;
            consumptionCategories.add(name);
            HashSet<Integer> categorySensors = SensorCollectionHandler.getSensorsForCollection(category);
            for (Integer sensorID : categorySensors) {
                if (SensorInformation.getSensorProperties(sensorID).getBuildingID() == buildingID) {
                    if (SensorInformation.getSensorProperties(sensorID).isVirtual()) {
                        virtual = "<br>(" + SensorInformation.getSensorProperties(sensorID).getVirtualDefinition() + ")";
                    } else {
                        virtual = "";
                    }
                    model.addElement("<html><body><table><tr><td width = 120><b>" + name + ":</b></td><td><b>" + SensorInformation.getDisplayName(sensorID) + " (" + SensorInformation.getSensorProperties(sensorID).getSensorDescription() + ")" + "</b>" + virtual + "</td></tr></table></body></html>");
                }
            }
        }

        jList2.setModel(model);
    }

    private void loadChart() {
        if (!active || jTabbedPane1.getSelectedIndex() == 0) {
            return;
        }
        String refUnit = "";
        if (referenceSelectorList.getSelectedValue() != null) {
            refUnit = UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference((String) referenceSelectorList.getSelectedValue()));
        }
        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(createChart(createY1Dataset(), createY2Dataset2(), (String) y1_usageUnitComboBox.getSelectedItem(), (String) y2_usageUnitComboBox.getSelectedItem(), refUnit, false)));
        chartPanel.revalidate();
    }

    public void setOverview() {
        overviewPanel.removeAll();
        // Übersichtspanel bestücken
        ArrayList<String> excludeList = new ArrayList<String>(Arrays.asList(MoniSoftConstants.BUILDING_COLLECTIONIDS, MoniSoftConstants.BUILDING_ID, MoniSoftConstants.BUILDING_ACTIVE, MoniSoftConstants.BUILDING_NAME, MoniSoftConstants.BUILDING_DESCRIPTION));

        overviewPanel.setLayout(new VerticalLayout(2));
        HashMap properties = BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName((String) buildingSelectorList.getSelectedItem())).getPropertyList();
        String s;
        for (Object entry : properties.keySet()) {
            if (excludeList.contains((String) entry)) {
                continue;
            }
            s = properties.get(entry) == null ? "" : String.valueOf(properties.get(entry));
            overviewPanel.add(new FeatureItem((String) entry, s));
        }
        overviewPanel.revalidate();
    }

    /**
     * Ersetzt die aktuellen (lokalen) BuildingProperties für diesen Messpunkt
     * durch die angegebenen BuioldingProperties
     *
     * @param oldProperties
     * @param newProperties
     */
    public void replaceBuilding(BuildingProperties newProperties) {
        BuildingInformation.writeBuildingProperty(currentBuilding, newProperties);
        setOverview();
        showReferences(currentBuilding);
        showMapping(currentBuilding);
    }

    /**
     * Erzeugt einen Datensatz für alle Verbrauchskategorien des jeweiligen
     * Gebäudes
     *
     * @return
     */
    private DefaultCategoryDataset createY1Dataset() {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        DatabaseQuery dq;
        String referenceName = "";
        String category;
        String building = (String) buildingSelectorList.getSelectedItem();
        if (consumptionCategories == null) {
            return null;
        }
        Iterator<String> it = consumptionCategories.iterator();

        //Alle Verbrauchskategorien für dieses Gebäude durchlaufen
        while (it.hasNext()) {
            category = it.next();
            Integer sensorID = SensorInformation.getSensorIDForConsumptionCategory(BuildingInformation.getBuildingIDFromName(building), category);
            if (sensorID == null) {
                continue;
            }
            dq = new DatabaseQuery(sensorID);
            if (useReferenceCheckBox.isSelected() && referenceSelectorList.getSelectedValue() != null) { // Wenn eine Bezugsgröße gewünscht diese anbringen
                referenceName = referenceSelectorList.getSelectedValue().toString();
            }
            boolean useYearReference = useYearReferenceCheckbox.isSelected() && useYearReferenceCheckbox.isEnabled();
            result.addValue(dq.getValueFor(monthChooser.getSelectedIndex() + 1, jYearChooser1.getYear(), referenceName, UnitInformation.getUnitFromName((String) y1_usageUnitComboBox.getSelectedItem()), null, fullYearCheckBox.isSelected(), useYearReference), building, category);
        }
        System.out.println("res 1 " + result.getColumnCount() + " " + result.getRowCount());
        return result;
    }

    private DefaultCategoryDataset createY2Dataset2() {
        DefaultCategoryDataset result = new DefaultCategoryDataset();
        DatabaseQuery dq;
        String referenceName = "";
        String category;
        String building = (String) buildingSelectorList.getSelectedItem();
        if (consumptionCategories == null) {
            return null;
        }
        Iterator<String> it = consumptionCategories.iterator();

        //Alle Verbrauchskategorien für dieses Gebäude durchlaufen
        while (it.hasNext()) {
            category = it.next();
            Integer sensorID = SensorInformation.getSensorIDForConsumptionCategory(BuildingInformation.getBuildingIDFromName(building), category);
            if (sensorID == null) {
                continue;
            }
            dq = new DatabaseQuery(sensorID);
            if (useReferenceCheckBox.isSelected() && referenceSelectorList.getSelectedValue() != null) { // Wenn eine Bezugsgröße gewünscht diese anbringen
                referenceName = referenceSelectorList.getSelectedValue().toString();
            }
            boolean useYearReference = useYearReferenceCheckbox.isSelected() && useYearReferenceCheckbox.isEnabled();
            Double value = dq.getValueFor(monthChooser.getSelectedIndex() + 1, jYearChooser1.getYear(), referenceName, UnitInformation.getUnitFromName((String) y2_usageUnitComboBox.getSelectedItem()), null, fullYearCheckBox.isSelected(), useYearReference);
            if (value != null) {
                result.addValue(value, building, category);
            }
        }
        System.out.println("res 2 " + result.getColumnCount() + " " + result.getRowCount());
        return result;
    }

    private String makeTitle() {
        String title = "";
        if (fullYearCheckBox.isSelected()) {
            if (monthChooser.getSelectedIndex() == 0) {  // whole year and month is january -> normal calendar year
                title = String.valueOf(jYearChooser1.getYear());
            } else {
                title = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("12MONTHSFROM") + " " + MoniSoftConstants.getMonthFor(monthChooser.getSelectedIndex()) + " " + jYearChooser1.getYear();
            }
        } else {
            title = MoniSoftConstants.getMonthFor(monthChooser.getSelectedIndex()) + " " + jYearChooser1.getYear();
        }
        return title;
    }

    private JFreeChart createChart(DefaultCategoryDataset y1Dataset, DefaultCategoryDataset y2Dataset, String y1unit, String y2unit, String referenceUnit, boolean stacked) {
        JFreeChart chart;
        CategoryPlot currentPlot = null;
        BarRenderer renderer;
        if (stacked) {
            chart = ChartFactory.createStackedBarChart(
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.Verbrauche") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.BUILDING") + " " + buildingSelectorList.getSelectedItem(), // chart title
                    "", // domain axis label
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " " + y1unit, // range axis label
                    y1Dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    true, // legend
                    true, // tooltips
                    false // urls
                    );
            currentPlot = (CategoryPlot) chart.getPlot();

            renderer = (StackedBarRenderer) currentPlot.getRenderer();
        } else {
            chart = ChartFactory.createBarChart(
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.Verbrauche") + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.BUILDING") + " " + buildingSelectorList.getSelectedItem(), // chart title
                    "", // domain axis label
                    java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " " + y1unit, // range axis label
                    y1Dataset, // data
                    PlotOrientation.VERTICAL, // the plot orientation
                    true, // legend
                    true, // tooltips
                    false // urls
                    );

            currentPlot = (CategoryPlot) chart.getPlot();
            if (useReferenceCheckBox.isSelected()) {
                String yearRefLabel = "";
                if (useYearReferenceCheckbox.isSelected() && useYearReferenceCheckbox.isEnabled()) {
                    yearRefLabel = " a";
                }
                currentPlot.getRangeAxis(0).setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " [" + y1unit + " / " + referenceUnit + " " + referenceSelectorList.getSelectedValue() + yearRefLabel + "]");
            } else {
                currentPlot.getRangeAxis(0).setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " [" + y1unit + "]"); // keine Bezugsgröße gewählt
            }

            renderer = (BarRenderer) currentPlot.getRenderer();
        }

        chart.setSubtitles(new ArrayList(Arrays.asList(new TextTitle(makeTitle(), new Font("Dialog", Font.PLAIN, 10)))));

        chart.setBackgroundPaint(new Color(238, 238, 238));
        chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 14));

        currentPlot.getRangeAxis(0).setLabelFont(new Font("Dialog", Font.PLAIN, 12)); // Achsenbeschriftung
        currentPlot.getRangeAxis(0).setTickLabelFont(new Font("Dialog", Font.PLAIN, 10));
        currentPlot.setBackgroundPaint(Color.WHITE);
        currentPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Set series appearance
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter()); // sonst Bonbon-Effekt....
        renderer.setBaseItemLabelsVisible(true);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(",##0.0")));
        renderer.setBaseItemLabelFont(new Font("Dialog", 0, 8));
        renderer.setShadowVisible(false);

        // Farbenzuordnung
        renderer.setSeriesPaint(0, colorTable[0]);

        // zweite Achse wenn gewünscht (für andere medien z.B Wasser)
        if (y2Dataset != null && y2Dataset.getRowCount() > 0) {
            NumberAxis axis2 = new NumberAxis();
            if (useReferenceCheckBox.isSelected()) {
                String yearRefLabel = "";
                if (useYearReferenceCheckbox.isSelected() && useYearReferenceCheckbox.isEnabled()) {
                    yearRefLabel = " a";
                }
                axis2.setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " [" + y2unit + " / " + referenceUnit + " " + referenceSelectorList.getSelectedValue() + yearRefLabel + "]");
            } else {
                axis2.setLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("BuildingProfile.USAGE") + " [" + y2unit + "]"); // keine Bezugsgröße gewählt
            }
            currentPlot.setRangeAxis(1, axis2);
            currentPlot.setDataset(1, y2Dataset);
            BarRenderer renderer2 = new BarRenderer();
            // Aussehen der datenreihen festlegen
            renderer2.setSeriesPaint(0, colorTable[1]);
            renderer2.setDrawBarOutline(false);
            renderer2.setBarPainter(new StandardBarPainter()); // sonst Bonbon-Effekt....
            renderer2.setBaseItemLabelsVisible(true);
            renderer2.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat(",##0.0")));
            renderer2.setBaseItemLabelFont(new Font("Dialog", 0, 8));
            renderer2.setShadowVisible(false);

            currentPlot.setRenderer(1, renderer2);
            currentPlot.getRangeAxis(1).setLabelFont(new Font("Dialog", Font.PLAIN, 12));
            currentPlot.getRangeAxis(1).setTickLabelFont(new Font("Dialog", Font.PLAIN, 10));
            currentPlot.mapDatasetToRangeAxis(1, 1);
        }
        return chart;
    }

    public void setActive(boolean a) {
        active = a;
    }

    private void newBuildingSelected() {
        if (!active) {
            return;
        }
        fillProfile(BuildingInformation.getBuildingIDFromName((String) buildingSelectorList.getSelectedItem()));
        loadChart();
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.showOpenDialog(this.gui.getMainFrame());
        File file = chooser.getSelectedFile();
        BuildingInformation.writeBuildingImage(currentBuilding, file);

        showImage(currentBuilding);
        BufferedImage image = BuildingInformation.readBuildingImage(currentBuilding);
        jXImagePanel1.setImage(image);
        jXImagePanel1.revalidate();
        jXImagePanel1.repaint();
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

        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jXImagePanel1 = new org.jdesktop.swingx.JXImagePanel();
        nRLabel = new javax.swing.JLabel();
        namelabel = new javax.swing.JLabel();
        overviewPanel = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        referenceList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        clusterLabel = new javax.swing.JLabel();
        clusterText = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jYearChooser1 = new com.toedter.calendar.JYearChooser();
        jLabel3 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        monthChooser = new javax.swing.JComboBox();
        fullYearCheckBox = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        useReferenceCheckBox = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        referenceSelectorList = new javax.swing.JList();
        useYearReferenceCheckbox = new javax.swing.JCheckBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        y1_usageUnitComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        y2_usageUnitComboBox = new javax.swing.JComboBox();
        chartPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        buildingSelectorList = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(874, 650));
        setLayout(new java.awt.BorderLayout());

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel47.setText(bundle.getString("BuildingProfile.jLabel47.text")); // NOI18N

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
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 704, Short.MAX_VALUE)
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

        add(headPanel, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        jXImagePanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jXImagePanel1.setToolTipText(bundle.getString("BuildingProfile.jXImagePanel1.toolTipText")); // NOI18N
        jXImagePanel1.setMaximumSize(new java.awt.Dimension(150, 150));
        jXImagePanel1.setMinimumSize(new java.awt.Dimension(150, 150));
        jXImagePanel1.setPreferredSize(new java.awt.Dimension(150, 150));

        javax.swing.GroupLayout jXImagePanel1Layout = new javax.swing.GroupLayout(jXImagePanel1);
        jXImagePanel1.setLayout(jXImagePanel1Layout);
        jXImagePanel1Layout.setHorizontalGroup(
            jXImagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 302, Short.MAX_VALUE)
        );
        jXImagePanel1Layout.setVerticalGroup(
            jXImagePanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 172, Short.MAX_VALUE)
        );

        nRLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        nRLabel.setText(bundle.getString("BuildingProfile.nRLabel.text")); // NOI18N

        namelabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        namelabel.setText(bundle.getString("BuildingProfile.namelabel.text")); // NOI18N

        overviewPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        overviewPanel.setToolTipText(bundle.getString("BuildingProfile.overviewPanel.toolTipText")); // NOI18N

        javax.swing.GroupLayout overviewPanelLayout = new javax.swing.GroupLayout(overviewPanel);
        overviewPanel.setLayout(overviewPanelLayout);
        overviewPanelLayout.setHorizontalGroup(
            overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
        overviewPanelLayout.setVerticalGroup(
            overviewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jScrollPane1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        referenceList.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        referenceList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        referenceList.setToolTipText(bundle.getString("BuildingProfile.referenceList.toolTipText")); // NOI18N
        jScrollPane1.setViewportView(referenceList);

        jScrollPane2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jList2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jList2.setToolTipText(bundle.getString("BuildingProfile.jList2.toolTipText")); // NOI18N
        jScrollPane2.setViewportView(jList2);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel1.setText(bundle.getString("BuildingProfile.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel2.setText(bundle.getString("BuildingProfile.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE)
                .addContainerGap())
        );

        clusterLabel.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        clusterLabel.setText(bundle.getString("BuildingProfile.clusterLabel.text")); // NOI18N

        clusterText.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        clusterText.setText(bundle.getString("BuildingProfile.clusterText.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(overviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jXImagePanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(nRLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(namelabel, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clusterLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clusterText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nRLabel)
                    .addComponent(namelabel)
                    .addComponent(clusterLabel)
                    .addComponent(clusterText))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jXImagePanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(overviewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab(bundle.getString("BuildingProfile.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("BuildingProfile.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        jYearChooser1.setToolTipText(bundle.getString("BuildingProfile.jYearChooser1.toolTipText")); // NOI18N
        jYearChooser1.setMaximum(2050);
        jYearChooser1.setMinimum(2005);
        jYearChooser1.setMinimumSize(new java.awt.Dimension(52, 18));
        jYearChooser1.setPreferredSize(new java.awt.Dimension(52, 18));
        jYearChooser1.setYear(2009);
        jYearChooser1.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jYearChooser1PropertyChange(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel3.setText(bundle.getString("BuildingProfile.jLabel3.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel7.setText(bundle.getString("BuildingProfile.jLabel7.text")); // NOI18N

        monthChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        monthChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" }));
        monthChooser.setMinimumSize(new java.awt.Dimension(71, 19));
        monthChooser.setPreferredSize(new java.awt.Dimension(71, 19));
        monthChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                monthChooserActionPerformed(evt);
            }
        });

        fullYearCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fullYearCheckBox.setSelected(true);
        fullYearCheckBox.setText(bundle.getString("BuildingProfile.fullYearCheckBox.text")); // NOI18N
        fullYearCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fullYearCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                            .addComponent(monthChooser, 0, 0, Short.MAX_VALUE))
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(fullYearCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(monthChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, Short.MAX_VALUE)
                .addComponent(fullYearCheckBox)
                .addContainerGap())
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("BuildingProfile.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        useReferenceCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useReferenceCheckBox.setText(bundle.getString("BuildingProfile.useReferenceCheckBox.text")); // NOI18N
        useReferenceCheckBox.setToolTipText(bundle.getString("BuildingProfile.useReferenceCheckBox.toolTipText")); // NOI18N
        useReferenceCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useReferenceCheckBoxActionPerformed(evt);
            }
        });

        referenceSelectorList.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        referenceSelectorList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "m² NGF", "m² BGF", "l/h Luftwechsel", "l/h Abluftmenge", "Anzahl AP", "m³ Volumen" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        referenceSelectorList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        referenceSelectorList.setToolTipText(bundle.getString("BuildingProfile.referenceSelectorList.toolTipText")); // NOI18N
        referenceSelectorList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                referenceSelectorListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(referenceSelectorList);

        useYearReferenceCheckbox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        useYearReferenceCheckbox.setText(bundle.getString("BuildingProfile.useYearReferenceCheckbox.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, useReferenceCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), useYearReferenceCheckbox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        useYearReferenceCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useYearReferenceCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(useReferenceCheckBox))
                .addGap(10, 10, 10))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(useYearReferenceCheckbox, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(useReferenceCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(useYearReferenceCheckbox)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("BuildingProfile.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel4.setText(bundle.getString("BuildingProfile.jLabel4.text")); // NOI18N

        y1_usageUnitComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y1_usageUnitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Wh", "kWh", "MWh", "GWh" }));
        y1_usageUnitComboBox.setSelectedIndex(1);
        y1_usageUnitComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        y1_usageUnitComboBox.setMinimumSize(new java.awt.Dimension(71, 20));
        y1_usageUnitComboBox.setPreferredSize(new java.awt.Dimension(71, 20));
        y1_usageUnitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y1_usageUnitComboBoxActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel5.setText(bundle.getString("BuildingProfile.jLabel5.text")); // NOI18N

        y2_usageUnitComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        y2_usageUnitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "l", "L", "m³", "cbm", "kbm", "m3" }));
        y2_usageUnitComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        y2_usageUnitComboBox.setMinimumSize(new java.awt.Dimension(71, 20));
        y2_usageUnitComboBox.setPreferredSize(new java.awt.Dimension(71, 20));
        y2_usageUnitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                y2_usageUnitComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(y1_usageUnitComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4)
                    .addComponent(y2_usageUnitComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(5, 5, 5)
                .addComponent(y1_usageUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addGap(5, 5, 5)
                .addComponent(y2_usageUnitComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(108, Short.MAX_VALUE))
        );

        chartPanel.setBackground(new java.awt.Color(255, 204, 204));
        chartPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(chartPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(bundle.getString("BuildingProfile.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel5.setPreferredSize(new java.awt.Dimension(746, 35));

        buildingSelectorList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        buildingSelectorList.setToolTipText(bundle.getString("BuildingProfile.buildingSelectorList.toolTipText")); // NOI18N
        buildingSelectorList.setMinimumSize(new java.awt.Dimension(400, 24));
        buildingSelectorList.setPreferredSize(new java.awt.Dimension(400, 24));
        buildingSelectorList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buildingSelectorListActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton1.setText(bundle.getString("BuildingProfile.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/pencil.png"))); // NOI18N
        jButton2.setToolTipText(bundle.getString("BuildingProfile.jButton2.toolTipText")); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(23, 23));
        jButton2.setMinimumSize(new java.awt.Dimension(23, 23));
        jButton2.setPreferredSize(new java.awt.Dimension(23, 23));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setText(bundle.getString("BuildingProfile.jLabel6.text")); // NOI18N

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 127, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buildingSelectorList, javax.swing.GroupLayout.PREFERRED_SIZE, 446, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1)
                    .addComponent(buildingSelectorList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)))
        );

        add(jPanel5, java.awt.BorderLayout.PAGE_END);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        disposeMe();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void buildingSelectorListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buildingSelectorListActionPerformed
        newBuildingSelected();
    }//GEN-LAST:event_buildingSelectorListActionPerformed

    private void useReferenceCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useReferenceCheckBoxActionPerformed
        if (!useReferenceCheckBox.isSelected()) {
            useYearReferenceCheckbox.setSelected(false);
        }
        loadChart();
}//GEN-LAST:event_useReferenceCheckBoxActionPerformed

    private void jYearChooser1PropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jYearChooser1PropertyChange
        loadChart();
    }//GEN-LAST:event_jYearChooser1PropertyChange

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        jYearChooser1PropertyChange(null);
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void referenceSelectorListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_referenceSelectorListValueChanged
        if (evt.getValueIsAdjusting() && useReferenceCheckBox.isSelected()) {
            loadChart();
        }
    }//GEN-LAST:event_referenceSelectorListValueChanged

    private void y1_usageUnitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y1_usageUnitComboBoxActionPerformed
        loadChart();
    }//GEN-LAST:event_y1_usageUnitComboBoxActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        BuildingEditorDialog editor = new BuildingEditorDialog(this.gui.getMainFrame(), true, this, true, false);
        editor.setLocationRelativeTo(this);
        editor.setFields(BuildingInformation.getBuildingProperties(currentBuilding));
        editor.setVisible(true);
        // editor ist APPLICATION_MODAL, hier wird gewartet bis das Dialogfenster beendet ist
    }//GEN-LAST:event_jButton2ActionPerformed

    private void y2_usageUnitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_y2_usageUnitComboBoxActionPerformed
        loadChart();
    }//GEN-LAST:event_y2_usageUnitComboBoxActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.BUILDING_PROFILE.getPage());
    }//GEN-LAST:event_jButton7help

    private void monthChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_monthChooserActionPerformed
        loadChart();
    }//GEN-LAST:event_monthChooserActionPerformed

    private void fullYearCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fullYearCheckBoxActionPerformed
        loadChart();
    }//GEN-LAST:event_fullYearCheckBoxActionPerformed

    private void useYearReferenceCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useYearReferenceCheckboxActionPerformed
        if (useReferenceCheckBox.isSelected()) {
            loadChart();
        }
    }//GEN-LAST:event_useYearReferenceCheckboxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox buildingSelectorList;
    private javax.swing.JPanel chartPanel;
    private javax.swing.JLabel clusterLabel;
    private javax.swing.JLabel clusterText;
    private javax.swing.JCheckBox fullYearCheckBox;
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private org.jdesktop.swingx.JXImagePanel jXImagePanel1;
    private com.toedter.calendar.JYearChooser jYearChooser1;
    private javax.swing.JComboBox monthChooser;
    private javax.swing.JLabel nRLabel;
    private javax.swing.JLabel namelabel;
    private javax.swing.JPanel overviewPanel;
    private javax.swing.JList referenceList;
    private javax.swing.JList referenceSelectorList;
    private javax.swing.JCheckBox useReferenceCheckBox;
    private javax.swing.JCheckBox useYearReferenceCheckbox;
    private javax.swing.JComboBox y1_usageUnitComboBox;
    private javax.swing.JComboBox y2_usageUnitComboBox;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
