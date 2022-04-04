/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/*
 * ClusterEditor.java
 *
 * Created on 29.09.2010, 10:25:48
 */
package de.jmonitoring.Cluster;

import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.base.charts.ClusterMatrixCharts;
import de.jmonitoring.utils.PrintUtilities;
import de.jmonitoring.References.ReferenceDescription;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.StoppableThread;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.*;

/**
 *
 * @author togro
 */
public class ClusterMatrixPanel extends javax.swing.JPanel implements Runnable {

    private StoppableThread stoppThread;
    private JPanel waitPanel = new JPanel(new BorderLayout());
    private MoniSoftProgressBar progressBar;
    private final MainApplication gui;
    private CtrlChartPanel chartPanel = null;

    /**
     * Creates new form ClusterEditor
     */
    public ClusterMatrixPanel(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setReferenceList();
        setFeatureList();
        setConsumptionCategories();
    }

    private void disposeMe() {
        Component c = this;
        while (c.getClass() != JInternalFrame.class) {
            c = c.getParent();
        }
        this.gui.disposeIFrame((JInternalFrame) c);
    }

    private void setReferenceList() {
        jList1.setCellRenderer(new CheckListRenderer());
        jList1.addMouseListener(new MouseAdapter() {
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
            }
        });

        DefaultListModel model = new DefaultListModel();
        TreeSet<ReferenceDescription> map = ReferenceInformation.getReferenceList();
        for (ReferenceDescription ref : map) {
            model.addElement(new CheckListItem(ref.getName() + " (" + ref.getDescription() + ")"));
        }
        jList1.setModel(model);
    }

    private void setFeatureList() {
        jList2.setCellRenderer(new CheckListRenderer());
        jList2.addMouseListener(new MouseAdapter() {
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
            }
        });

        DefaultListModel model = new DefaultListModel();
        TreeSet<ReferenceDescription> map = ReferenceInformation.getReferenceList();
        for (ReferenceDescription ref : map) {
            model.addElement(new CheckListItem(ref.getName() + " (" + ref.getDescription() + ")"));
        }
        jList2.setModel(model);
    }

    private void setConsumptionCategories() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(SensorCollectionHandler.getAllSensorCollectionNames(SensorCollectionHandler.COMPARE_COLLECTION).toArray());
        consumptionCategoryComboBox.setModel(model);
    }

    @Override
    public void run() {
        stoppThread.running = true;
        ArrayList<String> refs = new ArrayList<String>();
        ArrayList<String> feats = new ArrayList<String>();

        for (int i = 0; i < jList1.getModel().getSize(); i++) {
            CheckListItem refItem = (CheckListItem) jList1.getModel().getElementAt(i);
            if (refItem.isSelected) {
                refs.add(refItem.toString().split(" ", 0)[0]);
            }
        }

        for (int i = 0; i < jList2.getModel().getSize(); i++) {
            CheckListItem refItem = (CheckListItem) jList2.getModel().getElementAt(i);
            if (refItem.isSelected) {
                feats.add(refItem.toString().split(" ", 0)[0]);
            }
        }


        ClusterMatrixCharts panel = new ClusterMatrixCharts(refs, feats, (String) consumptionCategoryComboBox.getSelectedItem(), (String) yearChooserComboBox.getSelectedItem(), climateCorrectionCheckBox.isSelected(), progressBar, this.gui);
        chartPanel = panel.getChartPanel();
        mainScrollPane.getViewport().removeAll();
        mainScrollPane.getViewport().add(panel);
        mainScrollPane.repaint();

        progressBar.remove();
    }

    private static class CheckListItem {

        private String label;
        private boolean isSelected = false;

        public CheckListItem(String label) {
            this.label = label;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        @Override
        public String toString() {
            return label;
        }
    }
    ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle").getString("CANCEL") + "\n", true);
            if (progressBar != null) {
                progressBar.remove();
            }
            stoppThread.running = false;
        }
    };

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList2 = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        consumptionCategoryComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        yearChooserComboBox = new javax.swing.JComboBox();
        refreshButton = new javax.swing.JButton();
        climateCorrectionCheckBox = new javax.swing.JCheckBox();
        mainScrollPane = new javax.swing.JScrollPane();
        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(222, 7));
        setPreferredSize(new java.awt.Dimension(821, 705));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setMinimumSize(new java.awt.Dimension(200, 300));
        jPanel3.setPreferredSize(new java.awt.Dimension(200, 522));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle"); // NOI18N
        jLabel1.setText(bundle.getString("ClusterMatrixPanel.jLabel1.text")); // NOI18N

        jList1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane2.setViewportView(jList1);

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel2.setText(bundle.getString("ClusterMatrixPanel.jLabel2.text")); // NOI18N

        jList2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jList2.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane3.setViewportView(jList2);

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setText(bundle.getString("ClusterMatrixPanel.jLabel3.text")); // NOI18N

        consumptionCategoryComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        consumptionCategoryComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        consumptionCategoryComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        consumptionCategoryComboBox.setPreferredSize(new java.awt.Dimension(71, 19));
        consumptionCategoryComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                consumptionCategoryComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel4.setText(bundle.getString("ClusterMatrixPanel.jLabel4.text")); // NOI18N

        yearChooserComboBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        yearChooserComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2008", "2009", "2010", "2011", "2012", "2013", "2014", "2015", "2016", "2017", "2018", "2019", "2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028", "2029", "2030" }));
        yearChooserComboBox.setMinimumSize(new java.awt.Dimension(71, 19));
        yearChooserComboBox.setPreferredSize(new java.awt.Dimension(71, 19));

        refreshButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow_refresh.png"))); // NOI18N
        refreshButton.setText(bundle.getString("ClusterMatrixPanel.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        climateCorrectionCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        climateCorrectionCheckBox.setText(bundle.getString("ClusterMatrixPanel.climateCorrectionCheckBox.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(climateCorrectionCheckBox)
                        .addGap(0, 14, Short.MAX_VALUE))
                    .addComponent(jScrollPane2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(consumptionCategoryComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yearChooserComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(consumptionCategoryComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yearChooserComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(climateCorrectionCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addComponent(refreshButton)
                .addContainerGap())
        );

        jPanel1.add(jPanel3, java.awt.BorderLayout.LINE_START);

        mainScrollPane.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.add(mainScrollPane, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText(bundle.getString("ClusterMatrixPanel.jLabel47.text")); // NOI18N

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

        add(headPanel, java.awt.BorderLayout.PAGE_START);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 35));
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 35));
        jPanel2.setPreferredSize(new java.awt.Dimension(821, 35));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/printer.png"))); // NOI18N
        jButton2.setText(bundle.getString("ClusterMatrixPanel.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        jButton1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton1.setText(bundle.getString("ClusterMatrixPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        add(jPanel2, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (stoppThread != null) {
            stoppThread.running = false;
        }
        if (progressBar != null) {
            progressBar.remove();
        }
        disposeMe();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        PrintUtilities.printComponent(mainScrollPane.getViewport().getView());
//        chartPanel.createChartPrintJob();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void consumptionCategoryComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consumptionCategoryComboBoxActionPerformed
        // look if we have a climate correctable collection and set checkbox accordingly
        String collName = (String) consumptionCategoryComboBox.getSelectedItem();
        if (SensorCollectionHandler.isClimateCorrectionCollection(collName)) {
            climateCorrectionCheckBox.setEnabled(true);
        } else {
            climateCorrectionCheckBox.setEnabled(false);
            climateCorrectionCheckBox.setSelected(false);
        }
    }//GEN-LAST:event_consumptionCategoryComboBoxActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        progressBar = this.gui.getProgressBarpanel().addProgressBar("ClusterMatrix");
        progressBar.addProgressCancelButtonActionListener(action);

        JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));
        JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.CANCEL"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waitPanel.removeAll();
                waitPanel.setBackground(Color.WHITE);
                waitPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("OgivePlotChart.CLEANUP"), JLabel.CENTER));
                waitPanel.revalidate();
                stoppThread.running = false;
                progressBar.remove();
            }
        });
        waitPanel.removeAll();
        waitPanel.setBackground(Color.WHITE);
        waitPanel.add(waitLabel);
        waitPanel.add(cancelButton, BorderLayout.NORTH);
        mainScrollPane.getViewport().add(waitPanel);
        mainScrollPane.revalidate();

        stoppThread = new StoppableThread(this);
        stoppThread.start();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.CLUSTER_MATRIX.getPage());
    }//GEN-LAST:event_jButton7help
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox climateCorrectionCheckBox;
    private javax.swing.JComboBox consumptionCategoryComboBox;
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JList jList1;
    private javax.swing.JList jList2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JButton refreshButton;
    private javax.swing.JComboBox yearChooserComboBox;
    // End of variables declaration//GEN-END:variables
}
