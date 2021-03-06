/*
 * QualitySelectorPanel.java
 *
 * Created on 1. September 2008, 17:35
 */
package de.jmonitoring.Consistency;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

/**
 *
 * @author togro
 */
public class QualitySelectorPanel extends javax.swing.JPanel {

    private String[] toleranceDurations = {java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.1 STUNDE"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.2 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.3 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.5 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.12 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.24 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.48 STUNDEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("QualitySelectorPanel.1 WOCHE")};
    private final static String allSensors = java.util.ResourceBundle.getBundle("de/jmonitoring/Consistency/Bundle").getString("ALL_SENSORS");
    private boolean active = false;
    private DefaultListModel allModel = new DefaultListModel();

//    JLabel messageLabel = new JLabel("Fehlerhafte Datumsangaben", JLabel.CENTER);
    /**
     * Creates new form QualitySelectorPanel
     */
    public QualitySelectorPanel() {
        initComponents();
        jDateChooser1.getJCalendar().setTodayButtonVisible(true);
        jDateChooser2.getJCalendar().setTodayButtonVisible(true);
        updateSelectionComboBox();
        setAllModel();
        active = true;
    }

    private void setAllModel() {
        allModel.clear();
        SensorProperties props;
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            props = (SensorProperties) SensorInformation.getSensorList().get(i);
            if (!MoniSoftConstants.NO_SENSOR_SELECTED.equals(props.getSensorName())) {
                allModel.addElement(props);
            }
        }
    }

    public JButton getStartButton() {
        return startButton;
    }
    
    public int getMode() {
        if (jRadioButton1.isSelected()) {
            return 0;
        }
        return 1;
    }
    /**
     *
     */
    private void updateSelectionComboBox() {
        DefaultComboBoxModel model = SensorCollectionHandler.getSensorCollectionNamesAsComboBoxModel(SensorCollectionHandler.SIMPLE_COLLECTION, false, true);
        model.removeElementAt(0);
        model.insertElementAt(allSensors, 0);
        savedSetSelector.setModel(model);
        savedSetSelector.setSelectedIndex(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        selectorScrollPane = new javax.swing.JScrollPane();
        DefaultListModel model = new DefaultListModel();
        sensorSelectionList = new JList();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        startButton = new javax.swing.JButton();
        toleranceSelector = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        savedSetSelector = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButton7 = new javax.swing.JButton();

        SensorProperties props;
        for (int i = 0; i < SensorInformation.getSensorList().size(); i++) {
            props = (SensorProperties) SensorInformation.getSensorList().get(i);
            if (!MoniSoftConstants.NO_SENSOR_SELECTED.equals(props.getSensorName())) {
                model.addElement(props);
            }
        }
        sensorSelectionList.setModel(model);
        selectorScrollPane.setViewportView(sensorSelectionList);

        setMinimumSize(new java.awt.Dimension(873, 69));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Consistency/Bundle"); // NOI18N
        jDateChooser1.setToolTipText(bundle.getString("QualitySelectorPanel.jDateChooser1.toolTipText")); // NOI18N
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jDateChooser1.setDateFormatString(bundle1.getString("DatePanel.DateFormatString")); // NOI18N
        jDateChooser1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jDateChooser1.setNextFocusableComponent(jDateChooser2);

        jDateChooser2.setDateFormatString(bundle1.getString("DatePanel.DateFormatString")); // NOI18N
        jDateChooser2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("QualitySelectorPanel.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("QualitySelectorPanel.jLabel2.text")); // NOI18N

        startButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        startButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        startButton.setText(bundle.getString("QualitySelectorPanel.startButton.text")); // NOI18N
        startButton.setFocusable(false);

        toleranceSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        toleranceSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1 Stunde", "2 Stunden", "3 Stunden", "5 Stunden", "12 Stunden", "24 Stunden", "48 Stunden", "1 Woche" }));
        toleranceSelector.setToolTipText(bundle.getString("QualitySelectorPanel.toleranceSelector.toolTipText")); // NOI18N
        toleranceSelector.setFocusable(false);
        toleranceSelector.setMaximumSize(new java.awt.Dimension(32767, 20));
        toleranceSelector.setMinimumSize(new java.awt.Dimension(68, 20));
        toleranceSelector.setPreferredSize(new java.awt.Dimension(68, 20));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle.getString("QualitySelectorPanel.jLabel3.text")); // NOI18N

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setText(bundle.getString("QualitySelectorPanel.jButton2.text")); // NOI18N
        jButton2.setToolTipText(bundle.getString("QualitySelectorPanel.jButton2.toolTipText")); // NOI18N
        jButton2.setFocusable(false);
        jButton2.setMaximumSize(new java.awt.Dimension(82, 20));
        jButton2.setMinimumSize(new java.awt.Dimension(82, 20));
        jButton2.setPreferredSize(new java.awt.Dimension(82, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        savedSetSelector.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        savedSetSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        savedSetSelector.setToolTipText(bundle.getString("QualitySelectorPanel.savedSetSelector.toolTipText")); // NOI18N
        savedSetSelector.setFocusable(false);
        savedSetSelector.setMaximumSize(new java.awt.Dimension(32767, 20));
        savedSetSelector.setMinimumSize(new java.awt.Dimension(71, 20));
        savedSetSelector.setPreferredSize(new java.awt.Dimension(71, 20));
        savedSetSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savedSetSelectorActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("QualitySelectorPanel.jLabel4.text")); // NOI18N

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jRadioButton1.setText(bundle.getString("QualitySelectorPanel.jRadioButton1.text")); // NOI18N
        jRadioButton1.setFocusable(false);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jRadioButton2.setSelected(true);
        jRadioButton2.setText(bundle.getString("QualitySelectorPanel.jRadioButton2.text")); // NOI18N
        jRadioButton2.setFocusable(false);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(savedSetSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toleranceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63)
                        .addComponent(jRadioButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jRadioButton2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 368, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(startButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4)
                            .addComponent(savedSetSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2)))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jRadioButton1)
                                    .addComponent(jRadioButton2)
                                    .addComponent(jLabel3)
                                    .addComponent(toleranceSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(startButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(16, 16, 16))))
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jButton7)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public ArrayList<SensorProperties> getSelectedSensors() {
        Object[] selectedObjects = sensorSelectionList.getSelectedValues();
        ArrayList<SensorProperties> selectedValues = new ArrayList(Arrays.asList(selectedObjects));

        return selectedValues;
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        JList list = null;
        int cn = getParent().getComponentCount();
        for (int i = 0; i < cn; i++) {
            Component c = getParent().getComponent(i);
            if (c instanceof JScrollPane) {
                list = (JList) ((JScrollPane) c).getViewport().getComponent(0);
            }
        }
        if (list != null) {
            list.setSelectionInterval(0, list.getModel().getSize() - 1);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void savedSetSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savedSetSelectorActionPerformed

        if (!active) {
            return;
        }

        JList list = null;
        int cn = getParent().getComponentCount();
        for (int i = 0; i < cn; i++) {
            Component c = getParent().getComponent(i);
            if (c instanceof JScrollPane) {
                list = (JList) ((JScrollPane) c).getViewport().getComponent(0);
            }
        }

        if (((String) savedSetSelector.getSelectedItem()).equals(allSensors)) {
            if (list != null) {
                list.setModel(allModel);
            }
        } else {
            DefaultListModel model = SensorCollectionHandler.getSensorCollectionAsListModel((String) savedSetSelector.getSelectedItem());

            if (list != null) {
                list.setModel(model);
            }
        }
    }//GEN-LAST:event_savedSetSelectorActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.DATA_QUALITY.getPage());
    }//GEN-LAST:event_jButton7help

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    public com.toedter.calendar.JDateChooser jDateChooser1;
    public com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JComboBox savedSetSelector;
    public javax.swing.JScrollPane selectorScrollPane;
    private javax.swing.JList sensorSelectionList;
    private javax.swing.JButton startButton;
    public javax.swing.JComboBox toleranceSelector;
    // End of variables declaration//GEN-END:variables
}
