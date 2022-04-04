/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * FavoriteDialog.java
 *
 * Created on 20.10.2011, 11:13:25
 */
package de.jmonitoring.Components;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.filenamefilter.GRA_FilenameFilter;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListCellRenderer;

/**
 *
 * @author togro
 */
public class FavoriteDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 1L;
    private ArrayList<String> chartList = new ArrayList<String>(20);

    /**
     * Creates new form FavoriteDialog
     */
    public FavoriteDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        readProperties();
        setChartDescriberList();
        setCollectionList();
    }

    private void setChartDescriberList() {
        // Combobox gespeicherter Grafiken belegen (Lesen aus den GRA Dateien)
        CheckListItem cli;

        File userdir = new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.GRA_FOLDER);
        final String entries[] = userdir.list(new GRA_FilenameFilter()); // Liste der im Verzeichnis gespeicherten GRA-Dateien
        DefaultListModel model = new DefaultListModel();
        if (entries != null && entries.length > 0) {
            for (int i = 0; i < entries.length; i++) {
                entries[i] = entries[i].replace(".gra", ""); // das suffix zur Darstellung entfernen
                cli = new CheckListItem(entries[i]);
                cli.setSelected(checkThis(entries[i]));
                model.addElement(cli);
            }
        }

        jList1.setModel(model);
        jList1.setCellRenderer(new CheckListRenderer());
        jList1.removeMouseListener(jList1.getMouseListeners()[jList1.getMouseListeners().length - 1]);
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
    }

    /**
     * Ermittelt, ob der übergebene Eintrag in der gespeicherten Liste ist und
     * markiert werden soll
     *
     * @param entry
     * @return
     */
    private boolean checkThis(String entry) {
        boolean doCheck = false;
        if (getChartList().contains("\"" + entry + "\"")) {
            doCheck = true;
        }
        return doCheck;
    }

    /**
     * Belegen der Auswahlfelder entsprechend der gespeicherten Einstellungen
     */
    private void readProperties() {
        jCheckBox1.setSelected(MoniSoft.getInstance().getProjectProperties().getProperty("ShowFavoriteCharts").equals("1") ? true : false);
        jCheckBox2.setSelected(MoniSoft.getInstance().getProjectProperties().getProperty("ShowCompareTable").equals("1") ? true : false);

        String charts = MoniSoft.getInstance().getProjectProperties().getProperty("FavoriteCharts");
        getChartList().addAll(Arrays.asList(charts.split(",")));

        for (int i = 0; i < jComboBox1.getModel().getSize(); i++) {
            if (jComboBox1.getModel().getElementAt(i).equals(MoniSoft.getInstance().getProjectProperties().getProperty("FavoriteCollection"))) {
                jComboBox1.setSelectedIndex(i);
                break;
            }
        }

        jTextField1.setText(MoniSoft.getInstance().getProjectProperties().getProperty("LookBackDays"));
    }

    private String getSelectedCharts() {
        String listString = "";
        String sep = "";
        for (int i = 0; i < jList1.getModel().getSize(); i++) {
            CheckListItem item = (CheckListItem) jList1.getModel().getElementAt(i);
            if (item.isChecked()) {
                listString += sep + "\"" + item.getLabel() + "\"";
                sep = ",";
            }
        }
        return listString;
    }

    private void setCollectionList() {
        jComboBox1.setModel(SensorCollectionHandler.getSensorCollectionNamesAsComboBoxModel(SensorCollectionHandler.SIMPLE_COLLECTION, false, false));
        jComboBox1.removeItem(MoniSoftConstants.NO_SENSOR_SELECTED);
    }

    private static class CheckListRenderer extends JCheckBox implements ListCellRenderer {

        private static final long serialVersionUID = 1L;

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

    private static class CheckListItem {

        private String label;
        private boolean checked = false;

        private CheckListItem(String label) {
            this.label = label;
        }

        public boolean isSelected() {
            return checked;
        }

        public void setSelected(boolean isSelected) {
            this.checked = isSelected;
        }

        @Override
        public String toString() {
            return label;
        }

        public boolean isChecked() {
            return checked;
        }

        public void isChecked(boolean checked) {
            this.checked = checked;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    /**
     * Save project settings
     *
     * @param showMessage indicates if a message should be displayed after
     * writing
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean saveProperties(boolean showMessage) throws FileNotFoundException, IOException {
        boolean success = true;
        String file = MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.PROJECT_PROPS_FILE;
        FileOutputStream out = new FileOutputStream(file);
        MoniSoft.getInstance().getProjectProperties().store(out, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("--- PROJEKTSPEZIFISCHE EINSTELLUNGEN FÜR {0} ---"), new Object[]{MoniSoft.getInstance().getDBConnector().getDBName()}));
        out.close();
        if (showMessage) {
            Messages.showMessage(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("FavoriveDialog.SAVEDTO" + "\n"), new Object[]{file}), true);
        }

        return success;
    }

    public ArrayList<String> getChartList() {
        return this.chartList;
    }

    public void setChartList(ArrayList<String> chartList) {
        this.chartList = chartList;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jCheckBox2 = new javax.swing.JCheckBox();
        jComboBox1 = new javax.swing.JComboBox();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel47.setText(bundle.getString("FavoriteDialog.jLabel47.text")); // NOI18N

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 466, Short.MAX_VALUE)
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

        jPanel1.setMaximumSize(new java.awt.Dimension(32767, 300));
        jPanel1.setPreferredSize(new java.awt.Dimension(741, 270));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("FavoriteDialog.jLabel1.text")); // NOI18N

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox1.setText(bundle.getString("FavoriteDialog.jCheckBox1.text")); // NOI18N

        jList1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox2.setText(bundle.getString("FavoriteDialog.jCheckBox2.text")); // NOI18N

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setMinimumSize(new java.awt.Dimension(71, 21));
        jComboBox1.setPreferredSize(new java.awt.Dimension(71, 21));

        jTextField1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTextField1.setText(bundle.getString("FavoriteDialog.jTextField1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("FavoriteDialog.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jCheckBox1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jLabel2))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 696, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jCheckBox2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, 379, Short.MAX_VALUE)
                        .addGap(20, 20, 20))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox2)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(152, 152, 152))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setPreferredSize(new java.awt.Dimension(741, 35));

        jButton1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        jButton1.setText(bundle.getString("FavoriteDialog.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton2.setText(bundle.getString("FavoriteDialog.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(521, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(6, 6, 6))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Properties props = MoniSoft.getInstance().getProjectProperties();
        props.setProperty("ShowFavoriteCharts", jCheckBox1.isSelected() ? "1" : "0");
        props.setProperty("ShowCompareTable", jCheckBox2.isSelected() ? "1" : "0");
        props.setProperty("FavoriteCharts", getSelectedCharts());
        if (jComboBox1.getSelectedItem() != null) {
            props.setProperty("FavoriteCollection", jComboBox1.getSelectedItem().toString());
        }
        props.setProperty("LookBackDays", jTextField1.getText().trim());

        try {
            if (saveProperties(false)) {
                dispose();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("FaviriteDialog.DIE EINSTELLUNGEN KONNTEN NICHT GESPEICHERT WERDEN."), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("FavoriteDialog.WARNUNG"), JOptionPane.WARNING_MESSAGE);
            Messages.showException(e);
            Messages.showException(e);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.FAVORITE_DIALOG.getPage());
    }//GEN-LAST:event_jButton7help

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
