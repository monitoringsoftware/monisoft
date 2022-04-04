package de.jmonitoring.utils.UnitCalulation;

import de.jmonitoring.Components.BuildingEditorDialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.text.MaskFormatter;

/**
 * This class represent a input panel for a single unit definition
 *
 * @author togro
 */
public class UnitDefinitionAddPanel extends javax.swing.JPanel {

    private final UnitDefinitionDialog caller;

    /**
     * Creates new form ReferenceAddPanel<p> It is bound to the given
     * {@link UnitDefinitionDialog} and its element can be locked.
     *
     * @param editable If false the element are locked for view only mode
     * @param dialog The parent {@link UnitDefinitionDialog} that hold this
     * panel
     */
    public UnitDefinitionAddPanel(boolean editable, UnitDefinitionDialog dialog) {
        initComponents();
        this.caller = dialog;
        nameTextField.setFormatterFactory(new JFormattedTextField.AbstractFormatterFactory() {
            @Override
            public AbstractFormatter getFormatter(JFormattedTextField tf) {
                try {
                    return new MaskFormatter("**********"); // limit input field to 10 characters (database has a limit to 10)
                } catch (ParseException ex) {
                    Logger.getLogger(UnitDefinitionAddPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
            }
        });
        nameTextField.addMouseListener(ml);
        nameTextField.setEnabled(editable);
    }
    /**
     * A {@link Mouselistener} for the nae input field
     */
    MouseListener ml = new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JFormattedTextField tf = (JFormattedTextField) e.getSource();
                    int offset = tf.viewToModel(e.getPoint());

                    if (offset > tf.getText().trim().length()) {
                        offset = tf.getText().trim().length();
                    }
                    tf.setCaretPosition(offset);
                }
            });
        }
    };

    /**
     * Sets the panles elemants according to the unit
     *
     * @param unit The unit to show
     */
    public void setFields(Unit unit) {
        nameTextField.setText(unit.getUnit());
    }

    /**
     * Create a new {@link Unit} from the given name.
     *
     * @return The unit
     */
    public Unit getUnitFields() {
        // check if name is valid
        if (nameTextField.getText().trim().isEmpty()) {
            return null;
        }

        return new Unit(nameTextField.getText().trim());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JFormattedTextField();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("UnitDefinitionAddPanel.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        setMaximumSize(new java.awt.Dimension(32767, 60));
        setMinimumSize(new java.awt.Dimension(0, 60));
        setPreferredSize(new java.awt.Dimension(439, 60));

        nameLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        nameLabel.setText(bundle.getString("UnitDefinitionAddPanel.nameLabel.text")); // NOI18N

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/add.png"))); // NOI18N
        addButton.setToolTipText(bundle.getString("UnitDefinitionAddPanel.addButton.toolTipText")); // NOI18N
        addButton.setFocusPainted(false);
        addButton.setFocusable(false);
        addButton.setMaximumSize(new java.awt.Dimension(25, 25));
        addButton.setMinimumSize(new java.awt.Dimension(25, 25));
        addButton.setPreferredSize(new java.awt.Dimension(25, 25));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        nameTextField.setText(bundle.getString("UnitDefinitionAddPanel.nameTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        if (getUnitFields() != null) { // if this is a valid unit
            caller.getUnitDefinitionHolder().addUnit(getUnitFields());
            caller.updateScrollPane(BuildingEditorDialog.BOTTOM);
        } else { // sonst warnen
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/UnitCalulation/Bundle").getString("INVALID_UNIT"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/UnitCalulation/Bundle").getString("ERROR"), JOptionPane.WARNING_MESSAGE);
        }
    }//GEN-LAST:event_addButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JFormattedTextField nameTextField;
    // End of variables declaration//GEN-END:variables
}
