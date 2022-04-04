package de.jmonitoring.DataHandling.CounterChange;

import de.jmonitoring.DataHandling.DataHandler;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.Measurement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.VerticalLayout;

/**
 * This dialog is used for adding, removing or editing chounter changes for
 * senors
 *
 * @author togro
 */
public class CounterChangeDialog extends javax.swing.JDialog {

    private CounterChangeItemHolder ccHolder;
    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEAVE = 2;
    private boolean active = false;
    private HashSet<CounterChange> deletedChanges = new HashSet<CounterChange>();
    private HashSet<CounterChange> addedChanges = new HashSet<CounterChange>();
    private HashMap<Integer, HashSet<CounterChange>> changeMap = new HashMap<Integer, HashSet<CounterChange>>();
    private TreeSet<SensorProperties> counterList = new TreeSet<SensorProperties>();
    private boolean success = false;

    /**
     * Creates new form CounterChangeDialog
     *
     * @param gui The calling GUI
     * @param modal <code>true</code> if the dialog should be model
     */
    public CounterChangeDialog(MainApplication gui, boolean modal) {
        super(gui.getMainFrame(), modal);
        initComponents();

        jDateChooser1.getJCalendar().setTodayButtonVisible(true);

        counterList = SensorInformation.getCounterList();
        setSelector(counterList, false);
        initSpinners();
        active = true;
        ccHolder = new CounterChangeItemHolder(this);
        ccHolder.setLayout(new VerticalLayout(2));
        jScrollPane2.setViewportView(ccHolder);
        fillLocalChangeMap();
        setCounterChangesForCurrentSensor();
    }

    /**
     * Set the fields of the dialog to the given parameters
     *
     * @param oldValue The last value of the old meter
     * @param newValue The first value of the new meter
     * @param time The time of the meter change
     * @param factor The (new) factor
     * @param sensorID The sensor this change belogns to
     */
    public void setFields(Double oldValue, Double newValue, Date time, Double factor, int sensorID) {
        // Time
        jDateChooser1.setDate(time);
        Calendar cal = new GregorianCalendar();
        cal.setTime(time);
        setSpinnerValues(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        // sensor
        SensorProperties props = SensorInformation.getSensorProperties(sensorID);
        counterChangeSensorSelector.setSelectedItem(props);

        // values
        oldValueTextField.setText(oldValue.toString());
        newValueTextField.setText(newValue.toString());
        newFactorTextField.setText(factor.toString());
    }

    /**
     * ReferenceItemHolder aktualisieren und Scrollpane an das Ende oder den
     * Anfang scrollen oder so lassen
     *
     * @param loc Where to scroll to (BOTTOM or TOP or LEAVE)
     */
    public void updateScrollPane(int loc) {
        final int location = loc;
        ccHolder.revalidate();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                jScrollPane2.getViewport().repaint();

                JScrollBar scrollBar = jScrollPane2.getVerticalScrollBar();
                if (location == BOTTOM) {
                    scrollBar.setValue(scrollBar.getMaximum());
                } else if (location == TOP) {
                    scrollBar.setValue(scrollBar.getMinimum());
                } else if (location == LEAVE) {
                    // Was tun? Lassen?
                }
            }
        });
    }

    /**
     * Fügt einen Wechsel zu der Liste der neuen Wechsel hinzu. Er wird auch in
     * die Liste aller vorhandenen Wechsel aller Messpunkte eingefügt und in den
     * aktuellen Holder eingebaut
     *
     * @param c The counter chnage
     */
    public void registerAddedChange(CounterChange c) {
        ccHolder.addCounterChange(c);
        updateScrollPane(LEAVE);
        addedChanges.add(c);
        HashSet internalSet = changeMap.get(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
        if (internalSet == null) { // wenn bisher leer initialisieren
            internalSet = new HashSet<CounterChange>();
        }
        internalSet.add(c);
        changeMap.put(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID(), internalSet);
    }

    /**
     * Fügt einen Wechsel zu der Liste der zu löschenden Wechsel hinzu. Er wird
     * auch aus der Liste aller vorhandenen Wechsel aller Messpunkte entfernt.
     *
     * @param c The counter chnage
     */
    public void registerDeletedChange(CounterChange c) {
        if (addedChanges.contains(c)) {
            addedChanges.remove(c);
        } else {
            deletedChanges.add(c);
        }
        HashSet internalSet = changeMap.get(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
        internalSet.remove(c);
        changeMap.put(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID(), internalSet);
    }

    /**
     * Return the success status
     *
     * @return <code>true</code> if the operation was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel17 = new javax.swing.JPanel();
        jLabel60 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel63 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jLabel62 = new javax.swing.JLabel();
        hourSpinner = new javax.swing.JSpinner();
        minuteSpinner = new javax.swing.JSpinner();
        jLabel61 = new javax.swing.JLabel();
        jLabel64 = new javax.swing.JLabel();
        oldValueTextField = new de.jmonitoring.utils.textfields.DoubleTextField();
        jButton1 = new javax.swing.JButton();
        newValueTextField = new  de.jmonitoring.utils.textfields.DoubleTextField();
        jLabel65 = new javax.swing.JLabel();
        jLabel67 = new javax.swing.JLabel();
        newFactorTextField = new de.jmonitoring.utils.textfields.DoubleTextField();
        unitLabel1 = new javax.swing.JLabel();
        unitLabel2 = new javax.swing.JLabel();
        unitLabel3 = new javax.swing.JLabel();
        counterChangeSensorSelector = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(450, 339));
        setResizable(false);

        jPanel17.setBackground(new java.awt.Color(0, 102, 204));
        jPanel17.setPreferredSize(new java.awt.Dimension(100, 40));

        jLabel60.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel60.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel60.setText(bundle.getString("CounterChangeDialog.jLabel60.text")); // NOI18N

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

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel60, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel60, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel17Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );

        jButton13.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton13.setText(bundle.getString("CounterChangeDialog.jButton13.text")); // NOI18N
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton12.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        jButton12.setText(bundle.getString("CounterChangeDialog.jButton12.text")); // NOI18N
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("CounterChangeDialog.jPanel1.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9), new java.awt.Color(0, 0, 0))); // NOI18N

        jLabel63.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel63.setText(bundle.getString("CounterChangeDialog.jLabel63.text")); // NOI18N

        jDateChooser1.setToolTipText(bundle.getString("CounterChangeDialog.jDateChooser1.toolTipText")); // NOI18N
        jDateChooser1.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N

        jLabel62.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel62.setText(bundle.getString("CounterChangeDialog.jLabel62.text")); // NOI18N

        hourSpinner.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        hourSpinner.setToolTipText(bundle.getString("CounterChangeDialog.hourSpinner.toolTipText")); // NOI18N
        hourSpinner.setAlignmentX(0.0F);

        minuteSpinner.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        minuteSpinner.setToolTipText(bundle.getString("CounterChangeDialog.minuteSpinner.toolTipText")); // NOI18N
        minuteSpinner.setAlignmentX(0.0F);

        jLabel61.setText(bundle.getString("CounterChangeDialog.jLabel61.text")); // NOI18N

        jLabel64.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel64.setText(bundle.getString("CounterChangeDialog.jLabel64.text")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow-turn-180.png"))); // NOI18N
        jButton1.setToolTipText(bundle.getString("CounterChangeDialog.jButton1.toolTipText")); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(50, 19));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 19));
        jButton1.setPreferredSize(new java.awt.Dimension(50, 19));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel65.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel65.setText(bundle.getString("CounterChangeDialog.jLabel65.text")); // NOI18N

        jLabel67.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel67.setText(bundle.getString("CounterChangeDialog.jLabel67.text")); // NOI18N

        unitLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        unitLabel1.setText(bundle.getString("CounterChangeDialog.unitLabel1.text")); // NOI18N

        unitLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        unitLabel2.setText(bundle.getString("CounterChangeDialog.unitLabel2.text")); // NOI18N

        unitLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        unitLabel3.setText(bundle.getString("CounterChangeDialog.unitLabel3.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel63)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLabel62)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(4, 4, 4)
                        .addComponent(jLabel61)
                        .addGap(1, 1, 1)
                        .addComponent(minuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel67)
                            .addComponent(jLabel65)
                            .addComponent(jLabel64))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(oldValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(newValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(unitLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(newFactorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(unitLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unitLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel63)
                    .addComponent(jLabel62)
                    .addComponent(hourSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(minuteSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel61)
                    .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel64)
                            .addComponent(oldValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unitLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel65)
                            .addComponent(newValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unitLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel67)
                            .addComponent(newFactorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(unitLabel1))))
                .addContainerGap())
        );

        counterChangeSensorSelector.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        counterChangeSensorSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Wärmemengenzähler WS1", "Item 2", "Item 3", "Item 4" }));
        counterChangeSensorSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                counterChangeSensorSelectorActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("CounterChangeDialog.jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 153)));
        jPanel3.setMaximumSize(new java.awt.Dimension(32767, 15));
        jPanel3.setMinimumSize(new java.awt.Dimension(100, 15));
        jPanel3.setPreferredSize(new java.awt.Dimension(468, 15));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel1.setText(bundle.getString("CounterChangeDialog.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel2.setText(bundle.getString("CounterChangeDialog.jLabel2.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel3.setText(bundle.getString("CounterChangeDialog.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel4.setText(bundle.getString("CounterChangeDialog.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(59, 59, 59)
                .addComponent(jLabel1)
                .addGap(58, 58, 58)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                .addComponent(jLabel4)
                .addGap(42, 42, 42))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(0, 1, Short.MAX_VALUE))
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jButton3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jButton3.setText(bundle.getString("CounterChangeDialog.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/funnel.png"))); // NOI18N
        jToggleButton1.setToolTipText(bundle.getString("CounterChangeDialog.jToggleButton1.toolTipText")); // NOI18N
        jToggleButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                .addComponent(counterChangeSensorSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton12))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(93, 93, 93)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(counterChangeSensorSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(jButton13))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (!addedChanges.isEmpty() || !deletedChanges.isEmpty()) {
            if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.REALLYCANCEL"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.FRAGE"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
}//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Zahlen in Textfeldern in eine gültige Zahlen verwandeln (, in .)
        CounterChangeHandler ch = new CounterChangeHandler();

        // Wenn es neue Wechsel gibt
        if (!addedChanges.isEmpty()) {
            for (CounterChange change : addedChanges) {
                success = ch.addOrUpdateCounterChange(change, ((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
            }
        }

        // Wenn es zu löschende Wechsel gibt
        if (!deletedChanges.isEmpty()) {
            if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.THEREWILLBE") + " " + deletedChanges.size() + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.DELETE METER CHANGES"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.FRAGE"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                for (CounterChange change : deletedChanges) {
                    ch.removeCounterChange(change.getTime(), ((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
                }
            }
        }
        dispose();
}//GEN-LAST:event_okButtonActionPerformed

    private void counterChangeSensorSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_counterChangeSensorSelectorActionPerformed
        setCounterChangesForCurrentSensor();
    }//GEN-LAST:event_counterChangeSensorSelectorActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN);
        Double oldValue = null;
        Double newValue = null;
        Double newFactor = null;

        try {
            oldValue = nf.parse(oldValueTextField.getText().replace(".", ",")).doubleValue();
            newValue = nf.parse(newValueTextField.getText().replace(".", ",")).doubleValue();
            newFactor = nf.parse(newFactorTextField.getText().replace(".", ",")).doubleValue();
            Date date = jDateChooser1.getDate();
            if (date == null) {
                JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.FEHLENDES AUSTAUSCHDATUM"));
                return;
            }

            String hour = (String) hourSpinner.getValue();
            String minute = (String) minuteSpinner.getValue();
            String time = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat).format(date) + " " + hour + ":" + minute + ":00";
            SimpleDateFormat df = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
            registerAddedChange(new CounterChange(df.parse(time), oldValue, newValue, newFactor, ((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID()));
        } catch (ParseException ex) {
            Messages.showException(ex);
            if (oldValue == null || newValue == null) {
                if (JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.MISSINGVALUES"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.FRAGE"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        DataHandler dh = new DataHandler(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
        // Holen des eingetragenen Datum
        String hour = (String) hourSpinner.getValue();
        String minute = (String) minuteSpinner.getValue();
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        DecimalFormat df = new DecimalFormat("#0.###");

        if (jDateChooser1.getDate() != null) {
            String time = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat).format(jDateChooser1.getDate()) + " " + hour + ":" + minute + ":00";
            try {
                Date changeDate = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat).parse(time);

                Measurement prevMeasurement = dh.getPreviousDBEntry(null, changeDate);
                Measurement nextMeasurement = dh.getNextDBEntry(null, changeDate);

                Number prevValue = null, nextValue = null;
                if (prevMeasurement != null) {
                    prevValue = prevMeasurement.getValue();
                }
                if (nextMeasurement != null) {
                    nextValue = nextMeasurement.getValue();
                }

                if (prevValue != null) {
                    oldValueTextField.setText(df.format(prevValue));
                }

                if (nextValue != null) {
                    newValueTextField.setText(df.format(nextValue));
                }

                // Wandlungsfaktor des alten Zählers übernehmen
                newFactorTextField.setText(df.format(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getFactor()));
            } catch (ParseException e) {
                System.out.println(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CounterChangeDialog.MELDUNGNODATE"));
                Messages.showException(e);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if (jToggleButton1.isSelected()) {
            setSelector(counterList, true);
        } else {
            setSelector(counterList, false);
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.COUNTER_CHANGE.getPage());
    }//GEN-LAST:event_jButton7help
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox counterChangeSensorSelector;
    private javax.swing.JSpinner hourSpinner;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton7;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel60;
    private javax.swing.JLabel jLabel61;
    private javax.swing.JLabel jLabel62;
    private javax.swing.JLabel jLabel63;
    private javax.swing.JLabel jLabel64;
    private javax.swing.JLabel jLabel65;
    private javax.swing.JLabel jLabel67;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JSpinner minuteSpinner;
    private javax.swing.JTextField newFactorTextField;
    private javax.swing.JTextField newValueTextField;
    private javax.swing.JTextField oldValueTextField;
    private javax.swing.JLabel unitLabel1;
    private javax.swing.JLabel unitLabel2;
    private javax.swing.JLabel unitLabel3;
    // End of variables declaration//GEN-END:variables

    /**
     * Belegt das Auswahlfeld mit der Zählerliste. Bei bedarf werden nur die
     * Zähler angezeigt die einen Zählerwechsel eingetragen haben
     *
     * @param counterList Liste der SensorProperties der Zähler
     * @param withChangeOnly Sollen nur Zähler mit Wechsel angezeigt werden
     */
    private void setSelector(TreeSet<SensorProperties> counterList, boolean withChangeOnly) {
        SensorProperties props;
        counterChangeSensorSelector.removeAllItems();

        // Alle Zähler durchlaufen
        for (Iterator<SensorProperties> it = counterList.iterator(); it.hasNext();) {
            props = it.next();
            if (!MoniSoftConstants.NO_SENSOR_SELECTED.equals(props.getSensorName())) { // wenn es nicht der Platzhalter (<keine>) ist...
                if (withChangeOnly && changeMap.containsKey(props.getSensorID())) {
                    counterChangeSensorSelector.addItem(props);
                } else if (!withChangeOnly) {
                    counterChangeSensorSelector.addItem(props);
                }
            }
        }
        
        // counterChangeSensorSelector.getModel();
    }

    /**
     *
     * @param sensorID
     */
    private void setUnit(int sensorID) {
        String unit = SensorInformation.getSensorProperties(sensorID).getSensorUnit().getUnit();
        unit = "[" + unit + "]";
        unitLabel1.setText(unit);
        unitLabel2.setText(unit);
        unitLabel3.setText(unit);
    }

    /**
     * Initializes the time spinners with values
     */
    private void initSpinners() {
        ArrayList<String> items = new ArrayList<String>();
        String s;
        for (int i = 0; i <= 23; i++) {
            s = String.valueOf(i);
            items.add(s.length() == 1 ? "0" + s : s);
        }
        hourSpinner.setModel(new SpinnerListModel(items.toArray()));

        items.clear();
        for (int i = 0; i <= 59; i++) {
            s = String.valueOf(i);
            items.add(s.length() == 1 ? "0" + s : s);
        }
        minuteSpinner.setModel(new SpinnerListModel(items.toArray()));
    }

    /**
     * Set the spinner according to the given hour and minute
     *
     * @param h The hour
     * @param m The minute
     */
    private void setSpinnerValues(int h, int m) {
        String hour = String.valueOf(h);
        String minute = String.valueOf(m);

        hourSpinner.setValue(hour.length() == 1 ? "0" + hour : hour);
        minuteSpinner.setValue(minute.length() == 1 ? "0" + minute : minute);

    }

    /**
     * Belegt die Liste aller Wechsel aller Messpunkte mit den Einträgen aus der
     * DB
     */
    private void fillLocalChangeMap() {
        CounterChangeHandler h = new CounterChangeHandler();
        changeMap = h.getAllCounterChanges();
    }

    /**
     * Belegt den Holder mit den (aktuellen, noch nicht in die DB eingetragenen)
     * Wechseln des momentan gewählten Sensors.
     */
    private void setCounterChangesForCurrentSensor() {
        if (active) {
            ccHolder.removeAll();
            if (counterChangeSensorSelector.getSelectedItem() != null) {
                HashSet<CounterChange> changeSet = changeMap.get(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
                if (changeSet != null) {
                    for (CounterChange change : changeSet) {
                        ccHolder.addCounterChange(change);
                    }
                }
                setUnit(((SensorProperties) counterChangeSensorSelector.getSelectedItem()).getSensorID());
            }
            updateScrollPane(LEAVE);
        }
    }
}
