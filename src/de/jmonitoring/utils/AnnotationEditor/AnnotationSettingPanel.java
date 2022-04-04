/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.AnnotationEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;

/**
 *
 * @author togro
 */
public class AnnotationSettingPanel extends javax.swing.JPanel {

    private AnnotationElement annotationElement;
    private boolean active = false;

    /**
     * Creates new form AnnotationSettingPanel
     */
    public AnnotationSettingPanel() {
        initComponents();

        DefaultComboBoxModel model = new DefaultComboBoxModel();

        for (AnnotationStrokes stroke : AnnotationStrokes.values()) {
            StrokePreview p = new StrokePreview(stroke.getStroke(), ""); //NOI18N
            model.addElement(p.getIcon());
        }

        strokeChooser.setModel(model);
    }

    public void setElement(AnnotationElement element) {
        annotationElement = element;
        fill();
    }

    private void fill() {
        active = false;
        Color fillC = annotationElement.getFillColor();
        Color lineC = annotationElement.getLineColor();


        if (fillC != null) {
            fillColorButton.setBackground(new Color(fillC.getRed(), fillC.getGreen(), fillC.getBlue(), 255));
        } else {
            fillColorButton.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/not_used.png"))); //NOI18N
        }

        fillAlphaSlider.setValue(base255To100(annotationElement.getFillAlpha()));


        if (lineC != null) {
            lineColorButton.setBackground(new Color(lineC.getRed(), lineC.getGreen(), lineC.getBlue(), 255));
        } else {
            lineColorButton.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/not_used.png"))); //NOI18N
        }

        lineAlphaSlider.setValue(base255To100(annotationElement.getLineAlpha()));

        BasicStroke stroke = annotationElement.getStroke();
        if (stroke != null) {
            jSpinner1.setValue(stroke.getLineWidth());
            strokeChooser.setSelectedIndex(getIndexOf(stroke));
        }
        active = true;
    }

    private int getIndexOf(BasicStroke stroke) {
        BasicStroke testStroke = new BasicStroke(1f, stroke.getEndCap(), stroke.getLineJoin(), stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
        for (int i = 0; i < AnnotationStrokes.values().length; i++) {
            if (AnnotationStrokes.values()[i].getStroke().equals(testStroke)) {
                return i;
            }
        }
        return 0;
    }

    public void setElementsEnabled(boolean enabled) {
        removeElementButton.setEnabled(enabled);
        for (int i = 0; i < jPanel2.getComponents().length; i++) {
            jPanel2.getComponent(i).setEnabled(enabled);
        }
    }

    public AnnotationElement getAnnotationElement() {
        return annotationElement;
    }

    public void updateElement() {
        if (!active) {
            return;
        }
        if (fillColorButton.getIcon() == null) {
            annotationElement.setFillColor(fillColorButton.getBackground());
            annotationElement.setFillAlpha(base100To255(fillAlphaSlider.getValue()));
        } else {
            annotationElement.setFillColor(null);
            annotationElement.setFillAlpha(0);
        }

        if (lineColorButton.getIcon() == null) {
            annotationElement.setLineColor(lineColorButton.getBackground());
            annotationElement.setLineAlpha(base100To255(lineAlphaSlider.getValue()));
            BasicStroke selectedStroke = (BasicStroke) AnnotationStrokes.values()[strokeChooser.getSelectedIndex()].getStroke();
            BasicStroke stroke = new BasicStroke((Float) jSpinner1.getValue(), selectedStroke.getEndCap(), selectedStroke.getLineJoin(), selectedStroke.getMiterLimit(), selectedStroke.getDashArray(), selectedStroke.getDashPhase());
            annotationElement.setStroke(stroke);
        } else {
            annotationElement.setLineColor(null);
            annotationElement.setLineAlpha(0);
            annotationElement.setStroke(null);
        }
    }

    private int base255To100(int base255) {
        return Math.round(base255 / 255f * 100f);
    }

    private int base100To255(int base100) {
        return Math.round(base100 / 100f * 255f);
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        removeElementButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        lineAlphaSlider = new javax.swing.JSlider();
        jLabel10 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        strokeSamplePanel = new javax.swing.JPanel();
        strokeChooser = new javax.swing.JComboBox();
        fillAlphaSlider = new javax.swing.JSlider();
        jLabel9 = new javax.swing.JLabel();
        lineColorButton = new javax.swing.JButton();
        fillColorButton = new javax.swing.JButton();
        jSpinner1 = new javax.swing.JSpinner();
        //jSpinner1.setEditor(new JSpinner.NumberEditor(jSpinner1, "0.0"));

        removeElementButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        removeElementButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross-circle.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle"); // NOI18N
        removeElementButton.setToolTipText(bundle.getString("REMOVE_FROM_MARKER")); // NOI18N
        removeElementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeElementButtonActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Appearance", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N

        lineAlphaSlider.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        lineAlphaSlider.setMajorTickSpacing(50);
        lineAlphaSlider.setMinorTickSpacing(10);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel10.setText(bundle.getString("FILL")); // NOI18N

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel8.setText(bundle.getString("OUTLINE")); // NOI18N

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, lineAlphaSlider, org.jdesktop.beansbinding.ELProperty.create("${value} %"), jLabel12, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel14.setText(bundle.getString("STROKE")); // NOI18N

        strokeSamplePanel.setPreferredSize(new java.awt.Dimension(224, 22));

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, removeElementButton, org.jdesktop.beansbinding.ELProperty.create("${enabled}"), strokeSamplePanel, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        strokeSamplePanel.setLayout(new java.awt.BorderLayout());

        strokeChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        strokeChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        strokeChooser.setMaximumSize(new java.awt.Dimension(32767, 18));
        strokeChooser.setMinimumSize(new java.awt.Dimension(60, 18));
        strokeChooser.setPreferredSize(new java.awt.Dimension(60, 18));
        strokeChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                strokeChooserActionPerformed(evt);
            }
        });
        strokeSamplePanel.add(strokeChooser, java.awt.BorderLayout.CENTER);

        fillAlphaSlider.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        fillAlphaSlider.setMajorTickSpacing(50);
        fillAlphaSlider.setMinorTickSpacing(10);
        fillAlphaSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fillAlphaSliderStateChanged(evt);
            }
        });

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, fillAlphaSlider, org.jdesktop.beansbinding.ELProperty.create("${value} %"), jLabel9, org.jdesktop.beansbinding.BeanProperty.create("text"));
        bindingGroup.addBinding(binding);

        lineColorButton.setBackground(new java.awt.Color(219, 15, 15));
        lineColorButton.setFocusPainted(false);
        lineColorButton.setFocusable(false);
        lineColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        lineColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        lineColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lineColorButtonActionPerformed(evt);
            }
        });

        fillColorButton.setBackground(new java.awt.Color(219, 15, 15));
        fillColorButton.setMinimumSize(new java.awt.Dimension(34, 17));
        fillColorButton.setPreferredSize(new java.awt.Dimension(34, 17));
        fillColorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fillColorButtonActionPerformed(evt);
            }
        });

        jSpinner1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.0f), Float.valueOf(0.0f), Float.valueOf(3.0f), Float.valueOf(0.1f)));
        jSpinner1.setMinimumSize(new java.awt.Dimension(47, 18));
        jSpinner1.setPreferredSize(new java.awt.Dimension(47, 18));
        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel14))
                .addGap(2, 2, 2)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lineColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fillAlphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lineAlphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(strokeSamplePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel10)
                    .addComponent(fillAlphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(fillColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel8)
                    .addComponent(lineColorButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lineAlphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12))
                .addGap(4, 4, 4)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel14)
                    .addComponent(strokeSamplePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addComponent(removeElementButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeElementButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void lineColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lineColorButtonActionPerformed
        Color col = lineColorButton.getBackground();
        col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("CHOOSE_COLOR"), new Color(col.getRed(), col.getGreen(), col.getBlue(), base100To255(lineAlphaSlider.getValue())));
        if (col != null) {
            lineColorButton.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
            annotationElement.setLineColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
            annotationElement.setLineAlpha(col.getAlpha());
            lineAlphaSlider.setValue(base255To100(col.getAlpha()));
            lineColorButton.setIcon(null);
        }
        updateElement();
    }//GEN-LAST:event_lineColorButtonActionPerformed

    private void removeElementButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeElementButtonActionPerformed
        this.getParent().remove(this);
    }//GEN-LAST:event_removeElementButtonActionPerformed

    private void fillColorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fillColorButtonActionPerformed
        Color col = fillColorButton.getBackground();
        col = JColorChooser.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("CHOOSE_COLOR"), new Color(col.getRed(), col.getGreen(), col.getBlue(), base100To255(fillAlphaSlider.getValue())));
        if (col != null) {
            fillColorButton.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
            annotationElement.setFillColor(new Color(col.getRed(), col.getGreen(), col.getBlue(), 255));
            annotationElement.setFillAlpha(col.getAlpha());
            fillAlphaSlider.setValue(base255To100(col.getAlpha()));
            fillColorButton.setIcon(null);
        }
        updateElement();
    }//GEN-LAST:event_fillColorButtonActionPerformed

    private void fillAlphaSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_fillAlphaSliderStateChanged
        if (!fillAlphaSlider.getValueIsAdjusting()) {
            annotationElement.setFillAlpha(base100To255(fillAlphaSlider.getValue()));
        }
        updateElement();
    }//GEN-LAST:event_fillAlphaSliderStateChanged

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        if (((Float) jSpinner1.getValue()) < 0.02f) {
            lineColorButton.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/not_used.png"))); //NOI18N
        } else {
            lineColorButton.setIcon(null);
        }
        updateElement();
    }//GEN-LAST:event_jSpinner1StateChanged

    private void strokeChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_strokeChooserActionPerformed
        updateElement();
    }//GEN-LAST:event_strokeChooserActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JSlider fillAlphaSlider;
    private javax.swing.JButton fillColorButton;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSlider lineAlphaSlider;
    private javax.swing.JButton lineColorButton;
    private javax.swing.JButton removeElementButton;
    private javax.swing.JComboBox strokeChooser;
    private javax.swing.JPanel strokeSamplePanel;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}