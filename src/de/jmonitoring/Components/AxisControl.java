/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * AxisControl.java
 *
 * Created on 16.07.2009, 14:15:24
 */
package de.jmonitoring.Components;

import de.jmonitoring.standardPlots.common.ChartDescriber;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.utils.RepeatButton;
import java.util.Iterator;
import java.util.List;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

/**
 *
 * @author togro
 */
public class AxisControl extends javax.swing.JPanel {

    public final static int LEFT = 0;
    public final static int RIGHT = 1;
    private int axisNo;
    private Double step = null;
    private final MainApplication gui;

    /**
     * Creates new form AxisControl
     */
    public AxisControl(int axis, MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();

        axisNo = axis;

        RepeatButton maxPlusButton = new RepeatButton("+");
        maxPlusButton.setPreferredSize(new java.awt.Dimension(15, 20));
        maxPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        maxPlusButton.setActionCommand("u+");
        maxPlusButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleAxis(evt);
            }
        });
        RepeatButton maxMinusButton = new RepeatButton("-");
        maxMinusButton.setPreferredSize(new java.awt.Dimension(15, 20));
        maxMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        maxMinusButton.setActionCommand("u-");
        maxMinusButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleAxis(evt);
            }
        });
        RepeatButton minPlusButton = new RepeatButton("+");
        minPlusButton.setPreferredSize(new java.awt.Dimension(15, 20));
        minPlusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        minPlusButton.setActionCommand("l+");
        minPlusButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleAxis(evt);
            }
        });
        RepeatButton minMinusButton = new RepeatButton("-");
        minMinusButton.setPreferredSize(new java.awt.Dimension(15, 20));
        minMinusButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        minMinusButton.setActionCommand("l-");
        minMinusButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleAxis(evt);
            }
        });

        MaxPanel.add(maxPlusButton);
        MaxPanel.add(maxMinusButton);

        MinPanel.add(minPlusButton);
        MinPanel.add(minMinusButton);
    }

    /**
     *
     * @param e
     */
    private void scaleAxis(java.awt.event.ActionEvent e) {
        String scaleWhat = e.getActionCommand();
        ValueAxis rangeAxis = getValueAxis(axisNo);
        step = (rangeAxis.getUpperBound() - rangeAxis.getLowerBound()) / 30;

        if (scaleWhat.equals("u+")) {
            rangeAxis.setRange(rangeAxis.getLowerBound(), rangeAxis.getUpperBound() + step.doubleValue());
        } else if (scaleWhat.equals("u-")) {
            rangeAxis.setRange(rangeAxis.getLowerBound(), rangeAxis.getUpperBound() - step.doubleValue());
        } else if (scaleWhat.equals("l+")) {
            rangeAxis.setRange(rangeAxis.getLowerBound() + step.doubleValue(), rangeAxis.getUpperBound());
        } else if (scaleWhat.equals("l-")) {
            rangeAxis.setRange(rangeAxis.getLowerBound() - step.doubleValue(), rangeAxis.getUpperBound());
        }
    }

    /**
     *
     * @param a
     * @return
     */
    private ValueAxis getValueAxis(int a) {
        JInternalFrame activeFrame = (JInternalFrame) this.gui.getDesktop().getSelectedFrame();
        JPanel activePanel = (JPanel) activeFrame.getContentPane().getComponent(0);
        ChartPanel activeChartPanel = (ChartPanel) activePanel.getComponent(0);

        Plot plot = activeChartPanel.getChart().getPlot();

        // Prüfen ob es ein CombinedPlot ist, falls ja den Hauptplot ermitteln)
        if (plot instanceof CombinedDomainXYPlot) {
            CombinedDomainXYPlot p = (CombinedDomainXYPlot) plot.getRootPlot();
            List<XYPlot> plots = p.getSubplots();
            Iterator<XYPlot> it = plots.iterator();
            while (it.hasNext()) {
                XYPlot tmpPlot = it.next();
                if (!(tmpPlot.getRangeAxis() instanceof SymbolAxis)) {
                    plot = tmpPlot;
                }
            }
        }

        if (plot instanceof CategoryPlot) {
            return ((CategoryPlot) plot).getRangeAxis(a);
        }
        if (plot instanceof XYPlot) {
            return ((XYPlot) plot).getRangeAxis(a);
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MaxPanel = new javax.swing.JPanel();
        MinPanel = new javax.swing.JPanel();
        MainPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true));
        setLayout(new java.awt.BorderLayout());

        MaxPanel.setLayout(new java.awt.GridLayout(2, 1));
        add(MaxPanel, java.awt.BorderLayout.PAGE_START);

        MinPanel.setLayout(new java.awt.GridLayout(2, 1));
        add(MinPanel, java.awt.BorderLayout.PAGE_END);

        MainPanel.setBackground(new java.awt.Color(255, 255, 255));
        MainPanel.setMaximumSize(new java.awt.Dimension(220, 32767));
        MainPanel.setMinimumSize(new java.awt.Dimension(22, 30));
        MainPanel.setPreferredSize(new java.awt.Dimension(22, 100));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/ruler-triangle.png"))); // NOI18N
        jButton1.setToolTipText("Achsenskalierung der Werteachsen bearbeiten");
        jButton1.setIconTextGap(0);
        jButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton1.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton1.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton1);

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/chart_curve_edit.png"))); // NOI18N
        jButton2.setToolTipText("Eigenschaften der Datenreihen ändern");
        jButton2.setIconTextGap(0);
        jButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton2.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton2.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton2.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton2);

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/edit.png"))); // NOI18N
        jButton3.setToolTipText("Beschriftungen ändern");
        jButton3.setIconTextGap(0);
        jButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton3.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton3.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton3);

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/grid.png"))); // NOI18N
        jButton4.setToolTipText("Gitternetzlinen bearbeiten");
        jButton4.setIconTextGap(0);
        jButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton4.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton4.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton4.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton4);

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/clock.png"))); // NOI18N
        jButton5.setToolTipText("Zeit bzw. Domänenachse bearbeiten");
        jButton5.setIconTextGap(0);
        jButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton5.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton5.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton5.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton5);

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/information-frame.png"))); // NOI18N
        jButton8.setToolTipText("Zeitachse bearbeiten");
        jButton8.setIconTextGap(0);
        jButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton8.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton8.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton8.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton8);

        jButton6.setToolTipText("");
        jButton6.setBorderPainted(false);
        jButton6.setContentAreaFilled(false);
        jButton6.setEnabled(false);
        jButton6.setIconTextGap(0);
        jButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton6.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton6.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton6.setPreferredSize(new java.awt.Dimension(20, 20));
        MainPanel.add(jButton6);

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/hand-point-180.png"))); // NOI18N
        jButton7.setToolTipText("Charteigenschaften in die Bedienelemente übernehmen");
        jButton7.setIconTextGap(0);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.setMaximumSize(new java.awt.Dimension(20, 20));
        jButton7.setMinimumSize(new java.awt.Dimension(20, 20));
        jButton7.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        MainPanel.add(jButton7);

        add(MainPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.TITLE);
        d.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.SCALE);
        d.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.DATASERIES);
        d.setVisible(true);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.GRID);
        d.setVisible(true);
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.TIMEAXIS);
        d.setVisible(true);
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        ChartDescriber desc = this.gui.getActiveCtrlChartPanel().getChartdescriber();
        this.gui.fillPanelFromDescriber(desc);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        ChartPropertyDialog d = new ChartPropertyDialog(this.gui, false, this.gui.getActiveChart(), this.gui.getActiveCtrlChartPanel());
        d.setLocationRelativeTo(this);
        d.setTab(ChartPropertyDialog.INFO);
        d.setVisible(true);
    }//GEN-LAST:event_jButton8ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel MainPanel;
    private javax.swing.JPanel MaxPanel;
    private javax.swing.JPanel MinPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    // End of variables declaration//GEN-END:variables
}
