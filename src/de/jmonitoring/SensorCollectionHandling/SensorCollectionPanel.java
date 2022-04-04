package de.jmonitoring.SensorCollectionHandling;

import de.jmonitoring.Components.CompareValueMappingEditor;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.DnDListener.CompareEntryDropListener;
import de.jmonitoring.utils.DnDListener.SensorCollectionEntryDropListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import org.jdesktop.swingx.VerticalLayout;

/**
 *
 * @author togro
 */
public class SensorCollectionPanel extends javax.swing.JPanel {

    private JScrollPane scrollpane;
    private JPanel entryPanelHolder;
    private String title;
    private JViewport viewPort;
    private SensorCollection cv;
    final int initHeight = 20;
    private SensorCollectionItemHolder itemHolder = new SensorCollectionItemHolder();
    private int creator;
    private boolean climateCorrectable = false;

    public SensorCollectionPanel() {
        this(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SensorCollectionPanel.UNBENANNT"));
    }

    public SensorCollectionPanel(String t) {
        this(new SensorCollection(t), SensorCollectionHandler.SIMPLE_COLLECTION);
    }

    public SensorCollectionPanel(SensorCollection c, int creator) {
        initComponents();
        headerPanel.setComponentPopupMenu(titlePopUp);
        headerPanel.setBackground(MoniSoftConstants.SENSORCOLLECTION_PANEL_BACKGROUND);
        this.creator = creator;
        itemHolder.setLayout(new VerticalLayout(0));
        add(itemHolder, BorderLayout.CENTER);
        doLayout();
        revalidate();

        cv = c;
        title = c.getName();
        collectionTitleLabel.setText(title);
        climateCorrectable = SensorCollectionHandler.isClimateCorrectionCollection(c.getName());

        setPreferredSize(new Dimension(getWidth(), initHeight));
        setSize(getWidth(), initHeight);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                headerPanel.setBackground(Color.red);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                headerPanel.setBackground(MoniSoftConstants.SENSORCOLLECTION_PANEL_BACKGROUND);
            }
        });

        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                DropTarget dropTarget = new DropTarget(this, new SensorCollectionEntryDropListener());
                applyClimateCorrectionButton.setVisible(false);
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                DropTarget dropTarget1 = new DropTarget(this, new CompareEntryDropListener());
                applyClimateCorrectionButton.setVisible(true);
                applyClimateCorrectionButton.setSelected(climateCorrectable);
                break;
        }
    }

    public void setHeaderColor(Color c) {
        headerPanel.setBackground(c);
    }

    public String getTitle() {
        return title;
    }

    public void initSensors(JScrollPane scroll, boolean added) {
        scrollpane = scroll;
        viewPort = scrollpane.getViewport();
        entryPanelHolder = (JPanel) viewPort.getComponent(0);
        Iterator<SensorProperties> it = cv.getPropertySet().iterator();
        while (it.hasNext()) {
            expand(it.next());
        }
        adjustHeight(true, added);
        // TODO expandieren wenn leer!!
    }

    // Fügt einen Sensor zur Collection hinzu
    public void addSensor(int id) {
        expand(SensorInformation.getSensorProperties(id));
        cv.addSensor(SensorInformation.getSensorProperties(id));
        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                ((SensorCollectionEditor) getTopLevelAncestor()).setChangedState(true);
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                ((CompareValueMappingEditor) getTopLevelAncestor()).setChangedState(true);
                break;
        }

    }

    public void removeSensor(SensorProperties p) {
        cv.removeSensor(p);
        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                ((SensorCollectionEditor) getTopLevelAncestor()).setChangedState(true);
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                ((CompareValueMappingEditor) getTopLevelAncestor()).setChangedState(true);
                break;
        }
    }

    public SensorCollection getCompareValue() {
        return cv;
    }

    public void setCompareValue(SensorCollection cv) {
        this.cv = cv;
    }

    public void removeItem(SensorCollectionItem c) {
        itemHolder.remove(c);
        adjustHeight(true, false);
    }

    private void adjustHeight(boolean cascaded, boolean added) {
        int heightSum = 0;

        if (cascaded) {
            // Die Messpunkte dieser Verbrauchskategorie durchgehen und deren Höhen addieren
            for (int i = 0; i < itemHolder.getComponentCount(); i++) {
                heightSum += itemHolder.getComponent(i).getHeight();
            }
            // Höhe dieser Verbrauchskategorie anpassen und die Hähe der Kopfzeile addieren
            setPreferredSize(new Dimension(getWidth(), heightSum + initHeight));
            setSize(getWidth(), heightSum + initHeight);
        } else {
            setPreferredSize(new Dimension(getWidth(), headerPanel.getHeight())); // auf Größe des Kopfes beschränken
            setSize(getWidth(), headerPanel.getHeight());
        }
        revalidate();


        // Die einzelnen Verbrauchskategorien durchgehen und deren Höhen addieren
        heightSum = 0;
        for (int i = 0; i < entryPanelHolder.getComponentCount(); i++) {
            heightSum += entryPanelHolder.getComponent(i).getHeight() + 1;
        }
        if (added) {
            heightSum += 25 + 3;
        }
        entryPanelHolder.setPreferredSize(new Dimension(entryPanelHolder.getWidth(), heightSum));
        entryPanelHolder.setSize(entryPanelHolder.getWidth(), heightSum);
        entryPanelHolder.revalidate();
        scrollpane.revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titlePopUp = new javax.swing.JPopupMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        headerPanel = new javax.swing.JPanel();
        collectionTitleLabel = new javax.swing.JLabel();
        removeButton = new javax.swing.JButton();
        cascadeButton = new javax.swing.JToggleButton();
        applyClimateCorrectionButton = new javax.swing.JToggleButton();

        renameMenuItem.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/SensorCollectionHandling/Bundle"); // NOI18N
        renameMenuItem.setText(bundle.getString("SensorCollectionPanel.renameMenuItem.text")); // NOI18N
        renameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameMenuItemActionPerformed(evt);
            }
        });
        titlePopUp.add(renameMenuItem);

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setToolTipText(bundle1.getString("SensorCollectionPanel.toolTipText")); // NOI18N
        setMinimumSize(new java.awt.Dimension(250, 50));
        setPreferredSize(new java.awt.Dimension(250, 130));
        setLayout(new java.awt.BorderLayout());

        headerPanel.setBackground(new java.awt.Color(230, 206, 114));
        headerPanel.setToolTipText(bundle.getString("SensorCollectionPanel.headerPanel.toolTipText")); // NOI18N
        headerPanel.setPreferredSize(new java.awt.Dimension(450, 20));

        collectionTitleLabel.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        collectionTitleLabel.setText(bundle.getString("SensorCollectionPanel.collectionTitleLabel.text")); // NOI18N
        collectionTitleLabel.setToolTipText(bundle.getString("SensorCollectionPanel.collectionTitleLabel.toolTipText")); // NOI18N

        removeButton.setFont(new java.awt.Font("Dialog", 0, 5)); // NOI18N
        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/delete.png"))); // NOI18N
        removeButton.setToolTipText(bundle.getString("SensorCollectionPanel.removeButton.toolTipText")); // NOI18N
        removeButton.setBorder(null);
        removeButton.setContentAreaFilled(false);
        removeButton.setFocusPainted(false);
        removeButton.setFocusable(false);
        removeButton.setIconTextGap(0);
        removeButton.setMaximumSize(new java.awt.Dimension(12, 12));
        removeButton.setMinimumSize(new java.awt.Dimension(18, 18));
        removeButton.setPreferredSize(new java.awt.Dimension(18, 18));
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        cascadeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/section_expanded.png"))); // NOI18N
        cascadeButton.setBorder(null);
        cascadeButton.setBorderPainted(false);
        cascadeButton.setContentAreaFilled(false);
        cascadeButton.setFocusPainted(false);
        cascadeButton.setFocusable(false);
        cascadeButton.setIconTextGap(0);
        cascadeButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cascadeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        cascadeButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/section_collapsed.png"))); // NOI18N
        cascadeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cascadeButtonActionPerformed(evt);
            }
        });

        applyClimateCorrectionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/weather_cloudy_cross.png"))); // NOI18N
        applyClimateCorrectionButton.setToolTipText(bundle1.getString("SensorCollectionPanel.ClimateToolTip")); // NOI18N
        applyClimateCorrectionButton.setBorder(null);
        applyClimateCorrectionButton.setBorderPainted(false);
        applyClimateCorrectionButton.setContentAreaFilled(false);
        applyClimateCorrectionButton.setFocusPainted(false);
        applyClimateCorrectionButton.setFocusable(false);
        applyClimateCorrectionButton.setIconTextGap(0);
        applyClimateCorrectionButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        applyClimateCorrectionButton.setPreferredSize(new java.awt.Dimension(20, 20));
        applyClimateCorrectionButton.setRequestFocusEnabled(false);
        applyClimateCorrectionButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/weather_cloudy.png"))); // NOI18N
        applyClimateCorrectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyClimateCorrectionButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout headerPanelLayout = new javax.swing.GroupLayout(headerPanel);
        headerPanel.setLayout(headerPanelLayout);
        headerPanelLayout.setHorizontalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(collectionTitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 111, Short.MAX_VALUE)
                .addComponent(applyClimateCorrectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cascadeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        headerPanelLayout.setVerticalGroup(
            headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headerPanelLayout.createSequentialGroup()
                .addGroup(headerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cascadeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(collectionTitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(applyClimateCorrectionButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        add(headerPanel, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    public void setTitle(String t) {
        title = t;
        collectionTitleLabel.setText(title);
    }

    public boolean isClimateCorrectable() {
        return applyClimateCorrectionButton.isSelected();
    }

    public void sort() {
        TreeMap<String, SensorCollectionItem> map = new TreeMap<String, SensorCollectionItem>();
        String s;

        // Alle Items in einen Treemap schreiben, dadurch werden sie sortiert
        for (int i = 0; i < itemHolder.getComponentCount(); i++) {
            SensorCollectionItem item = (SensorCollectionItem) itemHolder.getComponent(i);
            s = item.getLabelText();
            map.put(s, item);
        }

        itemHolder.removeAll();

        // Alle Items in Liste wieder auslesen - sie sind nun sorteirt
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            itemHolder.add((SensorCollectionItem) map.get(it.next()));
        }
    }

    public void doCascade(boolean cascaded) {
        cascadeButton.setSelected(!cascaded);
        if (cascaded) {
            adjustHeight(true, false);
        } else {
            adjustHeight(false, false);
        }
        revalidate();
        scrollpane.revalidate(); // Neu auslegen
    }

    /**
     * Fügt einen neuen Messpunkt zur Verbrauchskategorie hinzu
     *
     * @param props
     */
    private void expand(SensorProperties props) {
        if (props == null) {
            System.out.println("Invalid sensorID in SensorCollection");
            return;
        }

        boolean showBuilding = false;
        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                showBuilding = false;
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                showBuilding = true;
                break;
        }
        SensorCollectionItem ci = new SensorCollectionItem(props, showBuilding);
        ci.setSize(200, 25);
        itemHolder.add(ci);
        sort();
        adjustHeight(true, false);
        revalidate();
        scrollpane.revalidate();

    }

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        cv = null;
        setSize(0, 0);
        removeAll();
        setPreferredSize(new Dimension(0, 0));
        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                ((SensorCollectionEditor) getTopLevelAncestor()).setChangedState(true);
                ((SensorCollectionEditor) getTopLevelAncestor()).removeEntry(this);
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                ((CompareValueMappingEditor) getTopLevelAncestor()).setChangedState(true);
                ((CompareValueMappingEditor) getTopLevelAncestor()).removeEntry(this);
                break;
        }

    }//GEN-LAST:event_removeButtonActionPerformed

    private void cascadeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cascadeButtonActionPerformed
        if (cascadeButton.isSelected()) {
            adjustHeight(false, false);
        } else {
            adjustHeight(true, false); // auffächern
        }
        revalidate();
        scrollpane.revalidate(); // Neu auslegen
    }//GEN-LAST:event_cascadeButtonActionPerformed

    private void applyClimateCorrectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyClimateCorrectionButtonActionPerformed
        switch (creator) {
            case SensorCollectionHandler.SIMPLE_COLLECTION:
                break;
            case SensorCollectionHandler.COMPARE_COLLECTION:
                ((CompareValueMappingEditor) getTopLevelAncestor()).setChangedState(true);
                break;
        }
    }//GEN-LAST:event_applyClimateCorrectionButtonActionPerformed

    private void renameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameMenuItemActionPerformed
        String oldTitle = title;
        Object input = JOptionPane.showInputDialog(this, "Umbenennen", "Eingabe", JOptionPane.OK_CANCEL_OPTION, null, null, title);
        if (input != null) {
            String newTitle = input.toString();
            setTitle(newTitle);

            switch (creator) {
                case SensorCollectionHandler.SIMPLE_COLLECTION:
                    ((SensorCollectionEditor) getTopLevelAncestor()).setChangedState(true);
                    ((SensorCollectionEditor) getTopLevelAncestor()).renameCollection(oldTitle, newTitle);
                    break;
                case SensorCollectionHandler.COMPARE_COLLECTION:
                    ((CompareValueMappingEditor) getTopLevelAncestor()).setChangedState(true);
                    ((CompareValueMappingEditor) getTopLevelAncestor()).renameCollection(oldTitle, newTitle);
                    break;
            }
        }
    }//GEN-LAST:event_renameMenuItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton applyClimateCorrectionButton;
    private javax.swing.JToggleButton cascadeButton;
    private javax.swing.JLabel collectionTitleLabel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton removeButton;
    private javax.swing.JMenuItem renameMenuItem;
    private javax.swing.JPopupMenu titlePopUp;
    // End of variables declaration//GEN-END:variables
}
