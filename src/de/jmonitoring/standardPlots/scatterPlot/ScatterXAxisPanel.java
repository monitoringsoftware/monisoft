/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ScatterXAxisPanel.java
 *
 * Created on 30.11.2009, 14:19:27
 */

package de.jmonitoring.standardPlots.scatterPlot;

import de.jmonitoring.utils.DnDListener.SensorSelectorDropListener;
import java.awt.dnd.DropTarget;
/**
 *
 * @author togro
 */
public class ScatterXAxisPanel extends javax.swing.JPanel {

    /** Creates new form ScatterXAxisPanel */
    public ScatterXAxisPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        XY_xSensorSelector = new javax.swing.JComboBox();
        XY_AggSelector = new javax.swing.JComboBox();
        XY_DomainPowerCheck = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED), bundle.getString("ScatterXAxisPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("de/jmonitoring/standardPlots/scatterPlot/Bundle"); // NOI18N
        jLabel20.setText(bundle1.getString("ScatterXAxisPanel.jLabel20.text")); // NOI18N

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel21.setText(bundle1.getString("ScatterXAxisPanel.jLabel21.text")); // NOI18N

        XY_xSensorSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_xSensorSelector.setMaximumRowCount(20);
        XY_xSensorSelector.setEnabled(false);
        XY_xSensorSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        XY_xSensorSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        XY_AggSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        XY_AggSelector.setMaximumRowCount(15);
        XY_AggSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Originalmesswerte", "Stundenwerte", "Tageswerte", "Wochenwerte" }));
        XY_AggSelector.setEnabled(false);
        XY_AggSelector.setMinimumSize(new java.awt.Dimension(126, 17));
        XY_AggSelector.setPreferredSize(new java.awt.Dimension(126, 17));

        XY_DomainPowerCheck.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        XY_DomainPowerCheck.setText(bundle1.getString("ScatterXAxisPanel.XY_DomainPowerCheck.text")); // NOI18N
        XY_DomainPowerCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        XY_DomainPowerCheck.setEnabled(false);
        XY_DomainPowerCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));

        new DropTarget(XY_xSensorSelector,new SensorSelectorDropListener());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(XY_AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                        .addComponent(XY_DomainPowerCheck))
                    .addComponent(XY_xSensorSelector, 0, 327, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(XY_xSensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(XY_AggSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(XY_DomainPowerCheck)))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    static javax.swing.JComboBox XY_AggSelector;
    private javax.swing.JCheckBox XY_DomainPowerCheck;
    static javax.swing.JComboBox XY_xSensorSelector;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    // End of variables declaration//GEN-END:variables

}