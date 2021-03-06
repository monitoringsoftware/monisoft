/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NewUnitDialog.java
 *
 * Created on 07.10.2011, 13:19:15
 */
package de.jmonitoring.Components;

import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.utils.UnitCalulation.Unit;
import java.awt.Color;

/**
 *
 * @author togro
 */
public class NewUnitDialog extends javax.swing.JDialog {

    private Integer unitID = null;

    /**
     * Creates new form NewUnitDialog
     */
    public NewUnitDialog(java.awt.Frame parent, boolean modal, String unit, String sensor) {
        super(parent, modal);
        initComponents();
        unitTextField.setText(unit);
        sensorText.setText(sensor);
    }

    public Integer getUnitID() {
        return unitID;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel18 = new javax.swing.JPanel();
        jLabel68 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        unitTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        saveButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        errorLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        sensorText = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel18.setBackground(new java.awt.Color(0, 102, 204));
        jPanel18.setPreferredSize(new java.awt.Dimension(100, 40));

        jLabel68.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel68.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel68.setText(bundle.getString("NewUnitDialog.jLabel68.text")); // NOI18N

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel68, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(232, Short.MAX_VALUE))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel68, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        getContentPane().add(jPanel18, java.awt.BorderLayout.PAGE_START);

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("NewUnitDialog.jLabel1.text")); // NOI18N

        unitTextField.setFocusCycleRoot(true);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("NewUnitDialog.jLabel2.text")); // NOI18N

        saveButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        saveButton.setText(bundle.getString("NewUnitDialog.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        cancelButton.setText(bundle.getString("NewUnitDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        errorLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        errorLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        errorLabel.setText(bundle.getString("NewUnitDialog.errorLabel.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle.getString("NewUnitDialog.jLabel3.text")); // NOI18N

        sensorText.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        sensorText.setText(bundle.getString("NewUnitDialog.sensorText.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(errorLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(unitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                                .addComponent(cancelButton))
                            .addComponent(sensorText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, saveButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(errorLabel)
                                .addGap(50, 50, 50))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel3)
                                    .addComponent(sensorText))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(unitTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1))))
                        .addGap(0, 45, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {cancelButton, saveButton});

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        String unit = unitTextField.getText().trim();
        if (UnitInformation.getIDFromUnitName(unit) != null) {
            errorLabel.setForeground(Color.RED); // unit already exists
        } else {
            unitID = UnitInformation.insertOrUpdateUnit(new Unit(unit)); // new unit, add to db
            dispose();
        }
    }//GEN-LAST:event_saveButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel68;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JButton saveButton;
    private javax.swing.JLabel sensorText;
    private javax.swing.JTextField unitTextField;
    // End of variables declaration//GEN-END:variables
}
