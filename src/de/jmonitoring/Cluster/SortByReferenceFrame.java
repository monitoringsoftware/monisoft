/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Cluster;

import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.CategoryDatasetValue;
import de.jmonitoring.References.ReferenceDescription;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 *
 * @author togro
 */
public class SortByReferenceFrame extends javax.swing.JInternalFrame {

    private JFreeChart chart;
    private CategoryPlot currentPlot = null;
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
    private HashMap<Integer, TreeSet<Integer>> clusterGroups;
    private TreeSet<CategoryDatasetValue> sortedDataSet;

    /**
     * Creates new form SortByReferenceFrame
     */
    public SortByReferenceFrame() {
        initComponents();
        fillBarWidthSpinner();
        setReferenceCombobox();
        drawPlot();
    }

    private CategoryDataset createDataSet() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();

        // Alle Gebäude holen
        ArrayList<BuildingProperties> buildingList = BuildingInformation.getBuildingList();

        // Aktuell gewählte Bezugsgröße
        String sortReference = (String) referenceSelector.getSelectedItem();

        // Gebäude durchlaufen und diese Bezugsgröße dafür ermitteln
        sortedDataSet = new TreeSet<CategoryDatasetValue>();
        ReferenceValue ref;
        for (BuildingProperties props : buildingList) {
            ref = BuildingInformation.getBuildingreference(props.getBuildingID(), sortReference);
            if (ref != null && ref.getValue() > 0) { // Gebäude ohne Wert für diese Bezugsgrösse fallen weg
                CategoryDatasetValue dsv = new CategoryDatasetValue(ref.getValue(), "", props.getBuildingName());
                sortedDataSet.add(dsv);
            }
        }


        Integer noOfBuildingsInGroup;
        Integer numberOfGroups = (Integer) noCluster.getValue();
        if ((Integer) noCluster.getValue() == 1) {
            noOfBuildingsInGroup = Integer.MAX_VALUE;
        } else {
            noOfBuildingsInGroup = (int) Math.round((double) sortedDataSet.size() / numberOfGroups);
        }

        TreeSet<Integer> runningSet = new TreeSet<Integer>();
        Integer counter = 1;
        Integer group = 1;
        clusterGroups = new HashMap<Integer, TreeSet<Integer>>(10);
        for (CategoryDatasetValue c : sortedDataSet) {
            if (counter > noOfBuildingsInGroup || (noOfBuildingsInGroup.equals(Integer.MAX_VALUE) && counter >= sortedDataSet.size())) {
                clusterGroups.put(group, runningSet);
                if (counter > noOfBuildingsInGroup) {
                    if (group < numberOfGroups) {
                        group++;
                        runningSet = new TreeSet<Integer>();
                    }
                }
                counter = 1;
            }
            counter++;
            runningSet.add(BuildingInformation.getBuildingIDFromName((String) c.getColumn()));

            if (c.getValue() >= (Integer) minSpinner.getValue() && c.getValue() <= (Integer) maxSpinner.getValue()) {
                dataSet.addValue(c.getValue(), group.toString(), c.getColumn());
            }
        }

        clusterGroups.put(group, runningSet);

        return dataSet;
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        BarRenderer renderer;
        chart = ChartFactory.createBarChart(
                java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SortByReferenceFrame.SORTBY") + "  " + (String) referenceSelector.getSelectedItem(), // chart title
                "", // domain axis label
                " [" + UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference((String) referenceSelector.getSelectedItem())) + "]", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // the plot orientation
                true, // legend
                true, // tooltips
                false // urls
                );

        currentPlot = (CategoryPlot) chart.getPlot();

        renderer = (BarRenderer) currentPlot.getRenderer();

        chart.getTitle().setFont(new Font("Dialog", Font.BOLD, 12));
        chart.removeLegend();
        chart.setBackgroundPaint(new Color(238, 238, 238));
        currentPlot.setBackgroundPaint(Color.WHITE);
        currentPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        currentPlot.getRangeAxis().setTickLabelFont(new Font("Dialog", 0, 9));
        currentPlot.getDomainAxis().setTickMarksVisible(false);
        currentPlot.getDomainAxis().setTickLabelsVisible(false);
        currentPlot.getDomainAxis().setVisible(false);
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin((Double) barWidthSpinner.getValue() - 5.0); // -4
        for (int col = 0; col < dataset.getRowCount(); col++) {
            renderer.setSeriesPaint(col, colorTable[col]);
        }

        renderer.setBaseToolTipGenerator(new CategoryToolTipGenerator() {

            @Override
            public String generateToolTip(CategoryDataset dataset, int row, int column) {
                return "<html><body><b>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SortByReferenceFrame.BUILDING") + " " + dataset.getColumnKey(column) + "</b>: " + dataset.getValue(row, column) + " m²</body></html>";
            }
        });
        renderer.setBarPainter(new StandardBarPainter()); // sonst Bonbon-Effekt....
        renderer.setShadowVisible(false);

        return chart;
    }

    private void drawPlot() {
        chart = createChart(createDataSet());
        mainPanel.removeAll();
        mainPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        mainPanel.revalidate();
    }

    private void setReferenceCombobox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        TreeSet<ReferenceDescription> availableRefs = ReferenceInformation.getReferenceList();
        Iterator<ReferenceDescription> it = availableRefs.iterator();
        String s = "";
        while (it.hasNext()) {
            s = it.next().getName();
            model.addElement(s);
        }
        referenceSelector.setModel(model);
    }

    private void fillBarWidthSpinner() {
        SpinnerModel model = new SpinnerNumberModel(4.0, 0.1, 8.0, 0.1);
        barWidthSpinner.setModel(model);
        barWidthSpinner.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dashPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        referenceSelector = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        minSpinner = new javax.swing.JSpinner();
        maxSpinner = new javax.swing.JSpinner();
        noCluster = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        barWidthSpinner = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        mainPanel = new javax.swing.JPanel();
        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setTitle(bundle.getString("SortByReferenceFrame.title")); // NOI18N
        setToolTipText(bundle.getString("SortByReferenceFrame.toolTipText")); // NOI18N
        setMinimumSize(new java.awt.Dimension(100, 50));
        setPreferredSize(new java.awt.Dimension(950, 450));

        dashPanel.setMinimumSize(new java.awt.Dimension(100, 40));
        dashPanel.setPreferredSize(new java.awt.Dimension(1161, 60));

        jButton1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle"); // NOI18N
        jButton1.setText(bundle1.getString("SortByReferenceFrame.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        referenceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        referenceSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        referenceSelector.setMinimumSize(new java.awt.Dimension(63, 20));
        referenceSelector.setPreferredSize(new java.awt.Dimension(63, 20));
        referenceSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                referenceSelectorActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle1.getString("SortByReferenceFrame.jLabel1.text")); // NOI18N

        minSpinner.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        minSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
        minSpinner.setPreferredSize(new java.awt.Dimension(85, 20));
        minSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minSpinnerStateChanged(evt);
            }
        });

        maxSpinner.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        maxSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(15000), Integer.valueOf(0), null, Integer.valueOf(1)));
        maxSpinner.setPreferredSize(new java.awt.Dimension(85, 20));
        maxSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxSpinnerStateChanged(evt);
            }
        });

        noCluster.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        noCluster.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10, 1));
        noCluster.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                noClusterStateChanged(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle1.getString("SortByReferenceFrame.jLabel2.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle1.getString("SortByReferenceFrame.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle1.getString("SortByReferenceFrame.jLabel4.text")); // NOI18N

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/block.png"))); // NOI18N
        jButton2.setText(bundle1.getString("SortByReferenceFrame.jButton2.text")); // NOI18N
        jButton2.setMaximumSize(new java.awt.Dimension(75, 20));
        jButton2.setMinimumSize(new java.awt.Dimension(75, 20));
        jButton2.setPreferredSize(new java.awt.Dimension(75, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        barWidthSpinner.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        barWidthSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                barWidthSpinnerStateChanged(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle1.getString("SortByReferenceFrame.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout dashPanelLayout = new javax.swing.GroupLayout(dashPanel);
        dashPanel.setLayout(dashPanelLayout);
        dashPanelLayout.setHorizontalGroup(
            dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dashPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(referenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dashPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(noCluster, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(dashPanelLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(barWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 168, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap())
        );
        dashPanelLayout.setVerticalGroup(
            dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dashPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dashPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(referenceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(noCluster, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(barWidthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(dashPanelLayout.createSequentialGroup()
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(1, 1, 1)))
                .addContainerGap())
        );

        getContentPane().add(dashPanel, java.awt.BorderLayout.PAGE_END);

        mainPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        mainPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText(bundle1.getString("SortByReferenceFrame.jLabel47.text")); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 741, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton7)
                .addContainerGap())
        );

        getContentPane().add(headPanel, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void referenceSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_referenceSelectorActionPerformed
        drawPlot();
    }//GEN-LAST:event_referenceSelectorActionPerformed

    private void minSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minSpinnerStateChanged
        drawPlot();
    }//GEN-LAST:event_minSpinnerStateChanged

    private void maxSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxSpinnerStateChanged
        drawPlot();
    }//GEN-LAST:event_maxSpinnerStateChanged

    private void noClusterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_noClusterStateChanged
        if (noCluster.getValue().equals(1)) {
            barWidthSpinner.setEnabled(false);
        } else {
            barWidthSpinner.setEnabled(true);
        }
        drawPlot();
    }//GEN-LAST:event_noClusterStateChanged

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }
        String name = "";
        String existsMessage = "";
        while (name.isEmpty() || ClusterInformation.clusterExists(name)) {
            name = JOptionPane.showInputDialog(this, existsMessage + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SensorTablePanel.NAME FÜR DEN CLUSTER"), "");
            if (name == null) {
                return;
            }
            existsMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SortByReferenceFrame.CLUSTERWITHNAME") + " " + name + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SortByReferenceFrame.EXISTS") + ".\n\n";
        }

        ClusterInformation.writeCluster(new Cluster(name, clusterGroups));
    }//GEN-LAST:event_jButton2ActionPerformed

    private void barWidthSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_barWidthSpinnerStateChanged
        drawPlot();
    }//GEN-LAST:event_barWidthSpinnerStateChanged

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.CLUSTER_SORT.getPage());
    }//GEN-LAST:event_jButton7help

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner barWidthSpinner;
    private javax.swing.JPanel dashPanel;
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
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSpinner maxSpinner;
    private javax.swing.JSpinner minSpinner;
    private javax.swing.JSpinner noCluster;
    private javax.swing.JComboBox referenceSelector;
    // End of variables declaration//GEN-END:variables
}
