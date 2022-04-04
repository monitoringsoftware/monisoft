/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/*
 * MonthyConsumtionPanel.java
 *
 * Created on 18.05.2011, 10:52:31
 */
package de.jmonitoring.Components;

import de.jmonitoring.DataHandling.*;
import de.jmonitoring.TableModels.MonthlyUsageTableModel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.DnDListener.SensorSelectorDropListener;
import de.jmonitoring.utils.swing.EDT;
import de.jmonitoring.utils.tablecellrenderer.DoubleCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.dnd.DropTarget;
import java.text.DecimalFormat;
import java.util.Calendar;
import javax.swing.ComboBoxModel;
import javax.swing.JInternalFrame;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
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
public class MonthlyConsumtionPanel extends javax.swing.JPanel {

    private int sensorID;
    private JFreeChart chart;
    private final MainApplication gui;
    private ChartPanel chartPanel = null;
    HighlightPredicate pRed = new HighlightPredicate() {
        @Override
        public boolean isHighlighted(Component renderer, org.jdesktop.swingx.decorator.ComponentAdapter adapter) {
            try {
                Double d = (Double) adapter.getValueAt(adapter.row, 3);
                if (d > 5) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    };
    HighlightPredicate pGreen = new HighlightPredicate() {
        @Override
        public boolean isHighlighted(Component renderer, org.jdesktop.swingx.decorator.ComponentAdapter adapter) {
            try {
                Double d = (Double) adapter.getValueAt(adapter.row, 3);
                if (d < 0.5) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
    };

    /**
     * Creates new form MonthyConsumtionPanel
     */
    public MonthlyConsumtionPanel(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setSelector();
        setYear();
        setTable();
    }

    private void setYear() {
        jYearChooser1.setValue(Calendar.getInstance().get(Calendar.YEAR));
    }

    private void setSelector() {
        Models models = new Models();
        ComboBoxModel model = models.getUsageListComboBoxModel();
//
        jComboBox1.setModel(model);
        jComboBox1.setSelectedIndex(0);
    }

    private synchronized void setTable() {
        if (((SensorProperties) jComboBox1.getSelectedItem()).getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            return;
        }
        sensorID = ((SensorProperties) jComboBox1.getSelectedItem()).getSensorID();

        MonthlyUsageTableModel model = new MonthlyUsageTableModel(this.gui);
        model.init(sensorID, jYearChooser1.getYear());
        jXTable1.setColumnControlVisible(true);
        jXTable1.setModel(model);
        jXTable1.setHighlighters(new ColorHighlighter(pRed, new Color(255, 72, 72), Color.BLACK, Color.LIGHT_GRAY, Color.BLACK), new ColorHighlighter(pGreen, Color.GREEN, Color.BLACK, Color.LIGHT_GRAY, Color.BLACK));
        jXTable1.getColumn(1).setCellRenderer(new DoubleCellRenderer("###,##0.0"));
        jXTable1.getColumn(2).setCellRenderer(new DoubleCellRenderer("###,##0.0"));
        jXTable1.getColumn(3).setCellRenderer(new DoubleCellRenderer("#0.0"));
        jXTable1.getColumn(4).setCellRenderer(new DoubleCellRenderer("###,##0.0"));
        jXTable1.getColumn(5).setCellRenderer(new DoubleCellRenderer("###,##0.0"));
        jXTable1.getColumn(6).setCellRenderer(new DoubleCellRenderer("###,##0.0"));
        jXTable1.setSortable(false);
        jXTable1.doLayout();

        makeChartWith(model);
    }

    /**
     * Bricht die Bearbeitung des aktuellen Dialogs ab. Bei veränderter
     * Sensorliste erfolgt eine Abfrage
     */
    private void close() {
        this.gui.disposeIFrame((JInternalFrame) this.getParent().getParent().getParent().getParent());
    }

    private void makeChartWith(MonthlyUsageTableModel model) {
        CategoryDataset dataset = createDataset(model);

        chart = ChartFactory.createBarChart(
                "", // chart title
                "", // domain axis label
                SensorInformation.getSensorProperties(sensorID).getSensorUnit().toString(), // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // the plot orientation
                true, // legend
                true, // tooltips
                false // urls
                );
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setBarPainter(new StandardBarPainter()); // sonst Bonbon-Effekt....
        plot.getRangeAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 10));
        plot.getDomainAxis().setTickLabelFont(new Font("Dialog", Font.BOLD, 10));
        plot.getRangeAxis().setLabelFont(new Font("Dialog", Font.BOLD, 12));
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(204, 51, 0));
        renderer.setSeriesPaint(1, new Color(0, 51, 204));
        renderer.setItemMargin(0.02);
    }

    private CategoryDataset createDataset(MonthlyUsageTableModel model) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Double[] data = model.getRawMonths();
        for (int i = 0; i < data.length; i++) {
            dataset.addValue(data[i], java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("RAWDATA"), MoniSoftConstants.getMonthFor(i));
        }
        data = model.getMonthlyMonths();
        for (int i = 0; i < data.length; i++) {
            dataset.addValue(data[i], java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("MONTHDATA"), MoniSoftConstants.getMonthFor(i));
        }
        return dataset;
    }

    private void saveTable(boolean overwrite) {
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }

        MonthlyUsageTableModel model = (MonthlyUsageTableModel) jXTable1.getModel();
        Integer month = null;
        int year = jYearChooser1.getValue();

        for (int row = 0; row < model.getRowCount(); row++) {
            for (int months = 0; months <= 11; months++) {
                if (MoniSoftConstants.getMonthFor(months).equals(((String) model.getValueAt(row, 0)).split(" ")[0])) {
                    month = months + 1;
                    break;
                }
            }

            Double value = (Double) model.getValueAt(row, 6);
            MonthlyUsageCalculator muc = new MonthlyUsageCalculator(this.gui);
            muc.writeToDB(month, year, value, sensorID, overwrite);
        }

        setTable();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jYearChooser1 = new com.toedter.calendar.JYearChooser();
        jPanel5 = new javax.swing.JPanel();
        jButton3 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTable1 = new org.jdesktop.swingx.JXTable();

        setLayout(new java.awt.BorderLayout());

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 30));
        jPanel1.setMinimumSize(new java.awt.Dimension(100, 30));
        jPanel1.setPreferredSize(new java.awt.Dimension(792, 30));

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setMaximumSize(new java.awt.Dimension(300, 20));
        jComboBox1.setMinimumSize(new java.awt.Dimension(71, 20));
        jComboBox1.setPreferredSize(new java.awt.Dimension(300, 20));

        jPanel4.setMaximumSize(new java.awt.Dimension(20, 32767));
        jPanel4.setMinimumSize(new java.awt.Dimension(20, 100));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("MonthlyConsumtionPanel.jLabel1.text")); // NOI18N

        jYearChooser1.setEndYear(2050);
        jYearChooser1.setMaximumSize(new java.awt.Dimension(52, 20));
        jYearChooser1.setStartYear(2000);

        jPanel5.setMaximumSize(new java.awt.Dimension(30, 32767));
        jPanel5.setMinimumSize(new java.awt.Dimension(30, 100));
        jPanel5.setPreferredSize(new java.awt.Dimension(30, 30));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jButton3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cog.png"))); // NOI18N
        jButton3.setText(bundle.getString("MonthlyConsumtionPanel.jButton3.text")); // NOI18N
        jButton3.setToolTipText(bundle.getString("MonthlyConsumtionPanel.jButton3.toolTipText")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

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

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/baricon.png"))); // NOI18N
        jToggleButton1.setText(bundle.getString("MonthlyConsumtionPanel.jToggleButton1.text")); // NOI18N
        jToggleButton1.setToolTipText(bundle.getString("CHARTTOOLTIP")); // NOI18N
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        new DropTarget(jComboBox1,new SensorSelectorDropListener());

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jLabel1)
                .addGap(0, 0, 0)
                .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jButton3)
                .addGap(80, 80, 80)
                .addComponent(jToggleButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 89, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jYearChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButton7)
                        .addComponent(jButton3)
                        .addComponent(jToggleButton1)))
                .addGap(8, 8, 8))
        );

        add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setMinimumSize(new java.awt.Dimension(100, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(792, 32));

        jButton1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        jButton1.setText(bundle.getString("MonthlyConsumtionPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveButtonPressed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton2.setText(bundle.getString("MonthlyConsumtionPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox1.setText(bundle.getString("MonthlyConsumtionPanel.jCheckBox1.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 304, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_END);

        jXTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4", "Title 5", "Title 6", "Title 7", "Title 8"
            }
        ));
        jXTable1.setVisibleRowCount(12);
        jScrollPane1.setViewportView(jXTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 792, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        add(jPanel3, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        close();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void SaveButtonPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveButtonPressed
        saveTable(jCheckBox1.isSelected());
    }//GEN-LAST:event_SaveButtonPressed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        setTable();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.MONTHLY_PANEL.getPage());
    }//GEN-LAST:event_jButton7help

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
//        EDT.perform(new Runnable() {
//            @Override
//            public void run() {
        if (jToggleButton1.isSelected()) {
            chartPanel = new ChartPanel(chart);
            remove(jPanel3);
            add(chartPanel, BorderLayout.CENTER);
        } else {
            if (chartPanel != null) {
                remove(chartPanel);
            }
            add(jPanel3, BorderLayout.CENTER);
        }
        revalidate();
//            }
//        });
    }//GEN-LAST:event_jToggleButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private org.jdesktop.swingx.JXTable jXTable1;
    private com.toedter.calendar.JYearChooser jYearChooser1;
    // End of variables declaration//GEN-END:variables
}
