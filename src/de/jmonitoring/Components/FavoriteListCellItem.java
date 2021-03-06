/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FavoriteListCell.java
 *
 * Created on 24.10.2011, 14:33:08
 */
package de.jmonitoring.Components;

/**
 *
 * @author togro
 */
public class FavoriteListCellItem extends javax.swing.JPanel {

    private String text;
    private Boolean selected;
    private Integer dayValue;

    /** Creates new form FavoriteListCell */
    public FavoriteListCellItem(String label) {
        initComponents();
        setText(label);
    }

    public Integer getDayValue() {
        return dayValue;
    }

    public void setDayValue(Integer dayValue) {
        this.dayValue = dayValue;
//        jCheckBox1.setSelected(this.dayValue);
    }

    public Boolean isSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
        jCheckBox1.setSelected(this.selected);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        jLabel1.setText(text);
    }

    @Override
    public String toString() {
        return text;
    }




    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();

        setMaximumSize(new java.awt.Dimension(32767, 30));
        setMinimumSize(new java.awt.Dimension(0, 30));
        setPreferredSize(new java.awt.Dimension(434, 30));

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jCheckBox1.setText(bundle.getString("FavoriteListCellItem.jCheckBox1.text")); // NOI18N

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7" }));
        jComboBox1.setMaximumSize(new java.awt.Dimension(32767, 21));
        jComboBox1.setMinimumSize(new java.awt.Dimension(71, 21));
        jComboBox1.setPreferredSize(new java.awt.Dimension(71, 21));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("FavoriteListCellItem.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jCheckBox1)
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel1))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
}
