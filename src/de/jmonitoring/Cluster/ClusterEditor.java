/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package de.jmonitoring.Cluster;

import de.jmonitoring.Components.NewClusterDialog;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.util.*;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

/**
 *
 * @author togro
 */
public class ClusterEditor extends javax.swing.JDialog {

    private TreeMap<String, Cluster> clusterMap = new TreeMap<String, Cluster>();
    private ArrayList<String> deletedClusterNames = new ArrayList<String>();
    private ArrayList<BuildingProperties> buildingSet = new ArrayList<BuildingProperties>();
    private TreeMap<Integer, HashSet<BuildingProperties>> groupMap = new TreeMap<Integer, HashSet<BuildingProperties>>();

    /**
     * Creates new form ClusterEditor
     */
    public ClusterEditor(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        fillBuildingSet();
        fillClusterMap();
        setClusterList();
        setGroupMap();
        setGroupSelector();
        setGroupList();
        trimBuildingList();
        setBuildingList();
    }

    /**
     * Fetches all buildings form {@link BuildingInformation} and puts the to a
     * local set
     */
    private void fillBuildingSet() {
        buildingSet = new ArrayList<BuildingProperties>();
        BuildingInformation.getBuildingList();
        for (BuildingProperties props : BuildingInformation.getBuildingList()) {
            buildingSet.add(props);
        }
    }

    /**
     * Refreshes the selector for the available buildings with a list of
     * buildings stored in the local buildingSet
     */
    private void setBuildingList() {
        DefaultListModel model = new DefaultListModel();
        for (BuildingProperties props : buildingSet) {
            model.addElement(props.getBuildingName() + " (" + props.getBuildingDescription() + ")");
        }
        allBuildingJList.setModel(model);
        allBuildingJList.revalidate();
    }

    /**
     * Fills a the local clusterMap from the global ClusterMap in {@link ClusterInformation}
     */
    private void fillClusterMap() {
        clusterMap = new TreeMap<String, Cluster>(ClusterInformation.getGlobalClusterMap());
    }

    /**
     * Updates the cluster combobox with the clusternames from the global
     * clusterMap
     */
    private void setClusterList() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String cluster : ClusterInformation.getGlobalClusterMap().keySet()) {
            model.addElement(cluster);
        }
        clusterComboBox.setModel(model);
    }

    /**
     * Updates the cluster combobox with the clusternames from the LOCAL
     * clusterMap
     */
    private void setClusterListLocal() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String cluster : clusterMap.keySet()) {
            model.addElement(cluster);
        }
        clusterComboBox.setModel(model);
    }

    /**
     * Sets the number of group in the group selector combobox according to the
     * number of groups in the selected cluster
     */
    private void setGroupSelector() {
        if (clusterComboBox.getSelectedItem() != null) {
            DefaultComboBoxModel groupModel = new DefaultComboBoxModel();
            for (int i = 1; i <= clusterMap.get((String) clusterComboBox.getSelectedItem()).getGroupCount(); i++) {
                groupModel.addElement(i);
            }
            groupSelector.setModel(groupModel);
            maxGroupCountLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle").getString("OF") + " " + clusterMap.get((String) clusterComboBox.getSelectedItem()).getGroupCount()); 
       } else {
            maxGroupCountLabel.setText("");
        }
    }

    /**
     * Builds a local groupMap for the selected cluster in the local
     * clusterMap<br> with the group number as id and a HaSet of the included
     * buildings {@link BuildingProperties}
     */
    private void setGroupMap() {
        if (clusterComboBox.getSelectedItem() != null) {
            Cluster cluster = clusterMap.get((String) clusterComboBox.getSelectedItem());
            for (int i = 1; i <= cluster.getGroupCount(); i++) {
                HashSet<BuildingProperties> groupBuildings = new HashSet<BuildingProperties>();
                for (Integer building : cluster.getBuildingsForGroup(i)) {
                    groupBuildings.add(BuildingInformation.getBuildingProperties(building));
//                    System.out.println("add " + BuildingInformation.getBuildingProperties(building).getBuildingID() + " " + BuildingInformation.getBuildingProperties(building).getBuildingName());
                }
                groupMap.put(i, groupBuildings);
            }
        }
    }

    /**
     * Loops the {@link BuildingProperties} stored in the local group map for
     * the currently selected<br> group number and populates the list of
     * group-buildings
     */
    private void setGroupList() {
        if (!groupMap.isEmpty()) {
            HashSet<BuildingProperties> buildings = groupMap.get((Integer) groupSelector.getSelectedItem());
            DefaultListModel model = new DefaultListModel();
            for (BuildingProperties props : buildings) {
//                System.out.println("buld id" + groupBuildings.getBuildingID());
                if (props != null) {
                    model.addElement(props.getBuildingName() + " (" + props.getBuildingDescription() + ")");
                }
            }
            buildingGroupList.setModel(model);
        } else {
            buildingGroupList.setModel(new DefaultListModel());
        }
    }

    /**
     * Removes all buildings which belong to the currently selected cluster form
     * the local buildingSet
     */
    private void trimBuildingList() {
        if (clusterComboBox.getSelectedItem() != null) {
            Cluster cluster = clusterMap.get((String) clusterComboBox.getSelectedItem());
            TreeSet<Integer> buildingIDs = new TreeSet<Integer>();
            for (int i = 1; i <= cluster.getGroupCount(); i++) {
                buildingIDs.addAll(cluster.getBuildingsForGroup(i));
            }

            BuildingProperties props;
            for (Integer id : buildingIDs) {
                props = BuildingInformation.getBuildingProperties(id);
                buildingSet.remove(props);
            }
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

        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        activatorPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        commitButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        clusterComboBox = new javax.swing.JComboBox();
        groupSelector = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        allBuildingJList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        buildingGroupList = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        newClusterButton = new javax.swing.JButton();
        deleteClusterButton = new javax.swing.JButton();
        maxGroupCountLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle"); // NOI18N
        jLabel47.setText(bundle.getString("ClusterEditor.jLabel47.text")); // NOI18N

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
                .addComponent(jLabel47)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 621, Short.MAX_VALUE)
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

        activatorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        cancelButton.setText(bundle.getString("ClusterEditor.cancelButton.text")); // NOI18N
        cancelButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        activatorPanel.add(cancelButton);

        commitButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        commitButton.setText(bundle.getString("ClusterEditor.commitButton.text")); // NOI18N
        commitButton.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        commitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commitButtonActionPerformed(evt);
            }
        });
        activatorPanel.add(commitButton);

        getContentPane().add(activatorPanel, java.awt.BorderLayout.PAGE_END);

        jPanel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("ClusterEditor.jLabel1.text")); // NOI18N

        clusterComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        clusterComboBox.setMinimumSize(new java.awt.Dimension(71, 20));
        clusterComboBox.setPreferredSize(new java.awt.Dimension(71, 20));
        clusterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clusterComboBoxActionPerformed(evt);
            }
        });

        groupSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        groupSelector.setMinimumSize(new java.awt.Dimension(71, 20));
        groupSelector.setPreferredSize(new java.awt.Dimension(71, 20));
        groupSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groupSelectorActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("ClusterEditor.jLabel4.text")); // NOI18N

        allBuildingJList.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jScrollPane1.setViewportView(allBuildingJList);

        buildingGroupList.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jScrollPane2.setViewportView(buildingGroupList);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("ClusterEditor.jLabel5.text")); // NOI18N

        addButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addButton.setText(bundle.getString("ClusterEditor.addButton.text")); // NOI18N
        addButton.setToolTipText(bundle.getString("ClusterEditor.addButton.toolTipText")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        removeButton.setText(bundle.getString("ClusterEditor.removeButton.text")); // NOI18N
        removeButton.setToolTipText(bundle.getString("ClusterEditor.removeButton.toolTipText")); // NOI18N
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        newClusterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/add.png"))); // NOI18N
        newClusterButton.setToolTipText(bundle.getString("ClusterEditor.newClusterButton.toolTipText")); // NOI18N
        newClusterButton.setMaximumSize(new java.awt.Dimension(20, 20));
        newClusterButton.setMinimumSize(new java.awt.Dimension(20, 20));
        newClusterButton.setPreferredSize(new java.awt.Dimension(20, 20));
        newClusterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newClusterButtonActionPerformed(evt);
            }
        });

        deleteClusterButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        deleteClusterButton.setToolTipText(bundle.getString("ClusterEditor.deleteClusterButton.toolTipText")); // NOI18N
        deleteClusterButton.setMaximumSize(new java.awt.Dimension(20, 20));
        deleteClusterButton.setMinimumSize(new java.awt.Dimension(20, 20));
        deleteClusterButton.setPreferredSize(new java.awt.Dimension(20, 20));
        deleteClusterButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteClusterButtonActionPerformed(evt);
            }
        });

        maxGroupCountLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        maxGroupCountLabel.setText(bundle.getString("ClusterEditor.maxGroupCountLabel.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(clusterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(newClusterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteClusterButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(groupSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxGroupCountLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(clusterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(newClusterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel5)
                                            .addComponent(groupSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(maxGroupCountLabel)))
                                    .addComponent(deleteClusterButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2))
                            .addComponent(jScrollPane1))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 187, Short.MAX_VALUE)
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeButton)
                        .addGap(199, 199, 199))))
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();     }//GEN-LAST:event_cancelButtonActionPerformed

    private void clusterComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clusterComboBoxActionPerformed
        fillBuildingSet(); // loads all available buildings in the set
        setGroupSelector();
        setGroupMap();
        setGroupList();
        trimBuildingList();
        setBuildingList();
    }//GEN-LAST:event_clusterComboBoxActionPerformed

    private void groupSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groupSelectorActionPerformed
        setGroupList();
    }//GEN-LAST:event_groupSelectorActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        Object selection[] = allBuildingJList.getSelectedValues();
        String buildingName;
        BuildingProperties props;
        Integer group = (Integer) groupSelector.getSelectedItem();
        if (group != null) {
            HashSet<BuildingProperties> buildingsInGroup = groupMap.get(group);
            Cluster cluster = clusterMap.get((String) clusterComboBox.getSelectedItem());
            for (int i = 0; i < selection.length; i++) {
                buildingName = ((String) selection[i]).split(" ")[0];
                props = BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(buildingName));
                buildingSet.remove(props);
                setBuildingList();
                buildingsInGroup.add(props);
                setGroupList();
                cluster.addBuilding(group, BuildingInformation.getBuildingIDFromName(buildingName));
            }
        }
    }//GEN-LAST:event_addButtonActionPerformed
    /**
     *
     * @param evt
     */
    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        Object selection[] = buildingGroupList.getSelectedValues();
        String buildingName;
        BuildingProperties props;
        Integer group = (Integer) groupSelector.getSelectedItem();
        HashSet<BuildingProperties> buildingsInGroup = groupMap.get(group);
        Cluster cluster = clusterMap.get((String) clusterComboBox.getSelectedItem());
        for (int i = 0; i < selection.length; i++) {
            buildingName = ((String) selection[i]).split(" ")[0];
            props = BuildingInformation.getBuildingProperties(BuildingInformation.getBuildingIDFromName(buildingName));
            buildingSet.add(props);
            setBuildingList();
            buildingsInGroup.remove(props);
            setGroupList();
            cluster.removeBuildingFromClusterGroup(group, BuildingInformation.getBuildingIDFromName(buildingName));
        }
    }//GEN-LAST:event_removeButtonActionPerformed
    /**
     * Save button pressed.<p> The local clusterMap (with all new and without
     * all removed clusters) gets copied<br>to the global map and all clusters
     * are rewritten to the database
     *
     * @param evt
     */
    private void commitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commitButtonActionPerformed
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(MoniSoftConstants.DEMO, true);
            return;
        }

        // delete clusters if there are any
        for (String clusterName : deletedClusterNames) {
            ClusterInformation.deleteCluster(clusterName);
        }

        ClusterInformation.setGlobalClusterMap(clusterMap);
        ClusterInformation.writeAllClusters();
        dispose();
    }//GEN-LAST:event_commitButtonActionPerformed
    /**
     *
     * @param evt
     */
    private void newClusterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newClusterButtonActionPerformed
        NewClusterDialog cd = new NewClusterDialog(null, true);
        cd.setLocationRelativeTo(this);
        Cluster cluster = cd.showDialog();
        if (cluster != null) {
            clusterMap.put(cluster.getName(), cluster); // add cluster to local map
            setClusterListLocal();  // update the cluster combobox with the new map
            clusterComboBox.setSelectedItem(cluster.getName()); // selects the new cluster in the combobox
        }
    }//GEN-LAST:event_newClusterButtonActionPerformed
    /**
     *
     * @param evt
     */
    private void deleteClusterButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteClusterButtonActionPerformed
        String selectedCluster = (String) clusterComboBox.getSelectedItem();
        if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle").getString("CLUSTER_DELETE_Q")+ " '" + selectedCluster + "' " + java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle").getString("DELETE_REALLY"), java.util.ResourceBundle.getBundle("de/jmonitoring/Cluster/Bundle").getString("CLUSTER_DELETE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            deletedClusterNames.add(selectedCluster);
            clusterMap.remove(selectedCluster);// remove cluster from local map
            clusterComboBox.removeItem(selectedCluster);
            setClusterListLocal();  // update the cluster combobox with the new map
            if (!clusterMap.isEmpty()) {
                clusterComboBox.setSelectedIndex(0); // select the first entry in the list
            } else {
                groupMap.clear();
                setGroupList();
            }
        }
    }//GEN-LAST:event_deleteClusterButtonActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.CLUSTER_EDITOR.getPage());
    }//GEN-LAST:event_jButton7help

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel activatorPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JList allBuildingJList;
    private javax.swing.JList buildingGroupList;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox clusterComboBox;
    private javax.swing.JButton commitButton;
    private javax.swing.JButton deleteClusterButton;
    private javax.swing.JComboBox groupSelector;
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel maxGroupCountLabel;
    private javax.swing.JButton newClusterButton;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
