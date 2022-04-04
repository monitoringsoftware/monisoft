/*
 * CExportDialog.java
 *
 * Created on 29. August 2007, 16:26
 */
package de.jmonitoring.Components;

import com.toedter.calendar.JDateChooser;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DataHandling.*;
import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.ComboBoxModels.Models;
import de.jmonitoring.utils.FileCompressor;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.CustomMinutePeriod;
import de.jmonitoring.utils.intervals.CustomSecondPeriod;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.intervals.IntervalSelectorEntry;
import java.awt.Color;
import java.awt.Cursor;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import org.jfree.data.time.*;

/**
 * Stellt einen Exportdialog dar in dem die zu exportierenden Messpunkte, der
 * Zeitraum, das Ausgabeformat und die Ausgabedatei(en) ausgewählt werden können
 *
 * @author togro
 */
public class ExportDialog extends javax.swing.JDialog {

    private boolean hasSensors = false;
    private boolean hasDates = false;
    private boolean hasFilename = false;
    private int decimal_separator_index = 0;
    private DateInterval dateInterval = new DateInterval();
    private double aggInterval;
    private RegularTimePeriod runningInterval;
    private Object objects[];
    private Thread thread;
    private String[] fieldSeperators = {java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.KOMMA"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.SEMIKOLON"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.TAB"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.LEERZEICHEN")};
    private String[] decimalSeperators = {java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.PUNKT"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.KOMMA")};
    private final DatasetWorkerFactory datasetWorkerFactory;

    /**
     * Erzeugt einen neun Dialog dessen Sensorauswahlfeld mit den Messpunkten in
     * <code>sensorList</code> belegt ist
     *
     * @param sensorList Liste der im Auswahlfeld darzustellenden Messpunkte
     * @param parent
     * @param modal
     * @param datasetWorkerFactory
     */
    public ExportDialog(ArrayList<SensorProperties> sensorList, DefaultComboBoxModel cm, java.awt.Frame parent, boolean modal, DatasetWorkerFactory datasetWorkerFactory) {
        super(parent, modal);
        this.datasetWorkerFactory = datasetWorkerFactory;
        initComponents();
        startDateChooser.getJCalendar().setTodayButtonVisible(true);
        endDateChooser.getJCalendar().setTodayButtonVisible(true);

        setSeparators();
        SensorProperties props;
        // Auswahliste bauen

        availableSensorList.setModel(new Models().getSensorListListModel());


        startDateChooser.setMinSelectableDate(MoniSoft.getInstance().getDataDateRange().getStartDate());
        startDateChooser.setMaxSelectableDate(MoniSoft.getInstance().getDataDateRange().getEndDate());
        endDateChooser.setMinSelectableDate(MoniSoft.getInstance().getDataDateRange().getStartDate());
        endDateChooser.setMaxSelectableDate(MoniSoft.getInstance().getDataDateRange().getEndDate());
        jFileChooser1.setCurrentDirectory(new File(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.DATA_FOLDER));
        fileNameTextField.setText(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder") + System.getProperty("file.separator") + MoniSoft.getInstance().getDBConnector().getDBName() + System.getProperty("file.separator") + MoniSoftConstants.DATA_FOLDER + System.getProperty("file.separator"));
        FieldComboBox.setSelectedIndex(Integer.parseInt(MoniSoft.getInstance().getApplicationProperties().getProperty("ExportFieldSeparator")));
        DecComboBox.setSelectedIndex(Integer.parseInt(MoniSoft.getInstance().getApplicationProperties().getProperty("ExportDecimalSeparator")));
        NumberFormatTextField.setText(MoniSoft.getInstance().getApplicationProperties().getProperty("ExportNumberFormat"));

        cm.insertElementAt(new IntervalSelectorEntry((MoniSoftConstants.RAW_INTERVAL)), 0);
        exportIntervalChooser.setModel(cm);
        exportIntervalChooser.setSelectedItem(new IntervalSelectorEntry(MoniSoftConstants.HOUR_INTERVAL));

        // Lesen der Gespeicherten Selektionen (ser-Dateien)
        updateSelectionComboBox();
    }

    private void setSeparators() {
        FieldComboBox.setModel(new DefaultComboBoxModel(fieldSeperators));
        DecComboBox.setModel(new DefaultComboBoxModel(decimalSeperators));

        FieldComboBox.setSelectedItem(objects);
        DecComboBox.setSelectedItem(objects);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        CounterModebuttonGroup = new javax.swing.ButtonGroup();
        eventModeButtongroup = new javax.swing.ButtonGroup();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        exportCancelButton = new javax.swing.JButton();
        exportSaveButton = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jPanel2 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        FieldComboBox = new javax.swing.JComboBox();
        DecComboBox = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        NumberFormatTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        availableSensorList = new javax.swing.JList();
        exportAddOneButton = new javax.swing.JButton();
        exportRemoveOne = new javax.swing.JButton();
        exportAddALLButton = new javax.swing.JButton();
        exportRemoveAllButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        listToBeExported = new javax.swing.JList();
        savedSetSelector = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        fileNameTextField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        startDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooser = new com.toedter.calendar.JDateChooser();
        jLabel5 = new javax.swing.JLabel();
        exportIntervalChooser = new javax.swing.JComboBox();
        jPanel6 = new javax.swing.JPanel();
        ExportConsumtionRadioButton = new javax.swing.JRadioButton();
        ExportPowerRadioButton = new javax.swing.JRadioButton();
        ExportCounterRadioButton = new javax.swing.JRadioButton();
        jPanel7 = new javax.swing.JPanel();
        singleFileCheckBox = new javax.swing.JCheckBox();
        compressCheckBox = new javax.swing.JCheckBox();
        saveSelectionCheckBox = new javax.swing.JCheckBox();
        jTextField2 = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        eventMeanRadioButton = new javax.swing.JRadioButton();
        eventSignificantRadioButton = new javax.swing.JRadioButton();

        jPanel3.setBackground(new java.awt.Color(0, 102, 204));

        jLabel6.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel6.setText(bundle.getString("ExportDialog.jLabel6.text")); // NOI18N

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );

        setTitle(bundle.getString("ExportDialog.title")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setMinimumSize(new java.awt.Dimension(750, 350));
        setResizable(false);

        exportCancelButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        exportCancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        exportCancelButton.setText(bundle.getString("ExportDialog.exportCancelButton.text")); // NOI18N
        exportCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCancelButtonActionPerformed(evt);
            }
        });

        exportSaveButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        exportSaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        exportSaveButton.setText(bundle.getString("ExportDialog.exportSaveButton.text")); // NOI18N
        exportSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSaveButtonActionPerformed(evt);
            }
        });

        jProgressBar1.setForeground(new java.awt.Color(0, 102, 255));
        jProgressBar1.setString(bundle.getString("ExportDialog.jProgressBar1.string")); // NOI18N
        jProgressBar1.setStringPainted(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("ExportDialog.jPanel2.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel2.setDoubleBuffered(false);

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel9.setText(bundle.getString("ExportDialog.jLabel9.text")); // NOI18N

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel10.setText(bundle.getString("ExportDialog.jLabel10.text")); // NOI18N

        FieldComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        FieldComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Komma", "Semikolon", "Tab", "Leerzeichen" }));
        FieldComboBox.setPreferredSize(new java.awt.Dimension(61, 19));

        DecComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        DecComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Punkt", "Komma" }));
        DecComboBox.setPreferredSize(new java.awt.Dimension(61, 19));
        DecComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                DecComboBoxItemStateChanged(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel11.setText(bundle.getString("ExportDialog.jLabel11.text")); // NOI18N

        NumberFormatTextField.setColumns(8);
        NumberFormatTextField.setText(bundle.getString("ExportDialog.NumberFormatTextField.text")); // NOI18N
        NumberFormatTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NumberFormatTextFieldKeyTyped(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                NumberFormatTextFieldKeyTyped(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                NumberFormatTextFieldKeyTyped(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel7.setText(bundle.getString("ExportDialog.jLabel7.text")); // NOI18N

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel12.setText(bundle.getString("ExportDialog.jLabel12.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FieldComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(DecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NumberFormatTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FieldComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(DecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(NumberFormatTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel12)))
        );

        jPanel1.setDoubleBuffered(false);

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel2.setText(bundle.getString("ExportDialog.jLabel2.text")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel1.setText(bundle.getString("ExportDialog.jLabel1.text")); // NOI18N

        availableSensorList.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        availableSensorList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Eintrag 1", "Eintrag 2", "Eintrag 3", "Eintrag 4", "Eintrag 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        availableSensorList.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(availableSensorList);

        exportAddOneButton.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        exportAddOneButton.setText(bundle.getString("ExportDialog.exportAddOneButton.text")); // NOI18N
        exportAddOneButton.setToolTipText(bundle.getString("ExportDialog.exportAddOneButton.toolTipText")); // NOI18N
        exportAddOneButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportAddOneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAddSelectionButtonActionPerformed(evt);
            }
        });

        exportRemoveOne.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        exportRemoveOne.setText(bundle.getString("ExportDialog.exportRemoveOne.text")); // NOI18N
        exportRemoveOne.setToolTipText(bundle.getString("ExportDialog.exportRemoveOne.toolTipText")); // NOI18N
        exportRemoveOne.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportRemoveOne.setMaximumSize(new java.awt.Dimension(46, 24));
        exportRemoveOne.setMinimumSize(new java.awt.Dimension(46, 24));
        exportRemoveOne.setPreferredSize(new java.awt.Dimension(46, 24));
        exportRemoveOne.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRemoveSelectionActionPerformed(evt);
            }
        });

        exportAddALLButton.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        exportAddALLButton.setText(bundle.getString("ExportDialog.exportAddALLButton.text")); // NOI18N
        exportAddALLButton.setToolTipText(bundle.getString("ExportDialog.exportAddALLButton.toolTipText")); // NOI18N
        exportAddALLButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportAddALLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportAddALLButtonActionPerformed(evt);
            }
        });

        exportRemoveAllButton.setFont(new java.awt.Font("Monospaced", 1, 12)); // NOI18N
        exportRemoveAllButton.setText(bundle.getString("ExportDialog.exportRemoveAllButton.text")); // NOI18N
        exportRemoveAllButton.setToolTipText(bundle.getString("ExportDialog.exportRemoveAllButton.toolTipText")); // NOI18N
        exportRemoveAllButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        exportRemoveAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportRemoveAllButtonActionPerformed(evt);
            }
        });

        listToBeExported.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        listToBeExported.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(listToBeExported);

        savedSetSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        savedSetSelector.setMinimumSize(new java.awt.Dimension(61, 20));
        savedSetSelector.setPreferredSize(new java.awt.Dimension(61, 20));
        savedSetSelector.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                savedSetSelectorItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportRemoveAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(exportRemoveOne, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                    .addComponent(exportAddALLButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(exportAddOneButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(savedSetSelector, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 399, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(savedSetSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 266, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(86, 86, 86)
                        .addComponent(exportAddOneButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportRemoveOne, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportAddALLButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exportRemoveAllButton)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jScrollPane1, jScrollPane2});

        jPanel4.setDoubleBuffered(false);

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText(bundle.getString("ExportDialog.jLabel8.text")); // NOI18N

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/folder.png"))); // NOI18N
        jButton1.setMaximumSize(new java.awt.Dimension(50, 20));
        jButton1.setMinimumSize(new java.awt.Dimension(50, 20));
        jButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 870, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel8)
                        .addComponent(fileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("ExportDialog.jPanel5.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9), new java.awt.Color(0, 0, 0))); // NOI18N
        jPanel5.setDoubleBuffered(false);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle.getString("ExportDialog.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("ExportDialog.jLabel4.text")); // NOI18N

        startDateChooser.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        startDateChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        endDateChooser.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        endDateChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("ExportDialog.jLabel5.text")); // NOI18N

        exportIntervalChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        exportIntervalChooser.setMinimumSize(new java.awt.Dimension(83, 19));
        exportIntervalChooser.setPreferredSize(new java.awt.Dimension(83, 19));
        exportIntervalChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportIntervalChooserActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(exportIntervalChooser, 0, 115, Short.MAX_VALUE)
                    .addComponent(endDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                    .addComponent(startDateChooser, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(startDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(endDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(exportIntervalChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(57, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("ExportDialog.jPanel6.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel6.setDoubleBuffered(false);

        CounterModebuttonGroup.add(ExportConsumtionRadioButton);
        ExportConsumtionRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ExportConsumtionRadioButton.setSelected(true);
        ExportConsumtionRadioButton.setText(bundle.getString("ExportDialog.ExportConsumtionRadioButton.text")); // NOI18N
        ExportConsumtionRadioButton.setToolTipText(bundle.getString("ExportDialog.ExportConsumtionRadioButton.toolTipText")); // NOI18N

        CounterModebuttonGroup.add(ExportPowerRadioButton);
        ExportPowerRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ExportPowerRadioButton.setText(bundle.getString("ExportDialog.ExportPowerRadioButton.text")); // NOI18N
        ExportPowerRadioButton.setToolTipText(bundle.getString("ExportDialog.ExportPowerRadioButton.toolTipText")); // NOI18N

        CounterModebuttonGroup.add(ExportCounterRadioButton);
        ExportCounterRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        ExportCounterRadioButton.setText(bundle.getString("ExportDialog.ExportCounterRadioButton.text")); // NOI18N
        ExportCounterRadioButton.setToolTipText(bundle.getString("ExportDialog.ExportCounterRadioButton.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ExportConsumtionRadioButton)
                    .addComponent(ExportPowerRadioButton)
                    .addComponent(ExportCounterRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ExportConsumtionRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ExportPowerRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ExportCounterRadioButton))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("ExportDialog.jPanel7.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel7.setDoubleBuffered(false);

        singleFileCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        singleFileCheckBox.setText(bundle.getString("ExportDialog.singleFileCheckBox.text")); // NOI18N
        singleFileCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleFileCheckBoxActionPerformed(evt);
            }
        });

        compressCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        compressCheckBox.setText(bundle.getString("ExportDialog.compressCheckBox.text")); // NOI18N

        saveSelectionCheckBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        saveSelectionCheckBox.setText(bundle.getString("ExportDialog.saveSelectionCheckBox.text")); // NOI18N

        jTextField2.setFont(new java.awt.Font("SansSerif", 0, 12)); // NOI18N

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(singleFileCheckBox)
                    .addComponent(compressCheckBox)
                    .addComponent(saveSelectionCheckBox)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap(60, Short.MAX_VALUE)
                .addComponent(singleFileCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(compressCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveSelectionCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("ExportDialog.jPanel8.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9))); // NOI18N
        jPanel8.setDoubleBuffered(false);

        eventModeButtongroup.add(eventMeanRadioButton);
        eventMeanRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        eventMeanRadioButton.setSelected(true);
        eventMeanRadioButton.setText(bundle.getString("ExportDialog.eventMeanRadioButton.text")); // NOI18N
        eventMeanRadioButton.setToolTipText(bundle.getString("ExportDialog.eventMeanRadioButton.toolTipText")); // NOI18N

        eventModeButtongroup.add(eventSignificantRadioButton);
        eventSignificantRadioButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        eventSignificantRadioButton.setText(bundle.getString("ExportDialog.eventSignificantRadioButton.text")); // NOI18N
        eventSignificantRadioButton.setToolTipText(bundle.getString("ExportDialog.eventSignificantRadioButton.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventMeanRadioButton)
                    .addComponent(eventSignificantRadioButton))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(eventMeanRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventSignificantRadioButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jProgressBar1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(exportCancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(1, 1, 1))
                            .addComponent(exportSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(14, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(53, 53, 53)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addComponent(exportCancelButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 7, Short.MAX_VALUE)
                        .addComponent(exportSaveButton))
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {exportCancelButton, exportSaveButton});

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void exportSaveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSaveButtonActionPerformed
        if (MoniSoft.getInstance().ISTRIAL) {
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.DEAKTIVATED") + "\n", true);
            return;
        }

        // Datumswerte ermitteln und pruefen
        dateInterval.setStartDate(startDateChooser.getDate());
        if (endDateChooser.getDate() != null) {
            dateInterval.setEndDate(new Date(endDateChooser.getDate().getTime() + 86399000));
        } else {
            dateInterval.setEndDate(null);
        }

        if (dateInterval.getStartDate() == null || dateInterval.getEndDate() == null) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.NOTIMERANGE"));
            return;
        }

        File f = new File(fileNameTextField.getText());
        if (f.isDirectory()) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.NOFILENAME"));
            return;
        }

        if (listToBeExported.getModel().getSize() == 0) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.NoSensor"));
            return;
        }

        // Lesen der selektierten Messpunkte
        DefaultListModel exportedList = (DefaultListModel) listToBeExported.getModel();

        // Speichern der Selektion bei Bedarf
        if (saveSelectionCheckBox.isSelected()) {
            String serName = jTextField2.getText();
            if (serName.isEmpty()) {
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.NOSELECTIONNAME"));
                return;
            } else {
                HashSet<Integer> idList = new HashSet<Integer>();
                for (int i = 0; i < listToBeExported.getModel().getSize(); i++) {
                    idList.add(((SensorProperties) listToBeExported.getModel().getElementAt(i)).getSensorID());
                }
                SensorCollectionHandler.insertCollection(serName, idList, SensorCollectionHandler.SIMPLE_COLLECTION, false, true);
            }
        }

        if (f.exists()) {
            if (JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.OVERWRITE"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.QUESTION"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
                return;
            }

        }
        f = null;
        objects = exportedList.toArray();
        // Lesen des Zeitintervalls

        aggInterval = ((IntervalSelectorEntry) exportIntervalChooser.getSelectedItem()).doubleValue();

        if (aggInterval < 0) {
            switch ((int) aggInterval) {
                case (int) MoniSoftConstants.RAW_INTERVAL:
                    runningInterval = null;
                    break;
                case (int) MoniSoftConstants.HOUR_INTERVAL:
                    runningInterval = new Hour(0, new Day(dateInterval.getStartDate()));
                    break;
                case (int) MoniSoftConstants.DAY_INTERVAL:
                    runningInterval = new Day(dateInterval.getStartDate());
                    break;
                case (int) MoniSoftConstants.WEEK_INTERVAL:
                    runningInterval = new Week(dateInterval.getStartDate());
                    break;
                case (int) MoniSoftConstants.MONTH_INTERVAL:
                    runningInterval = new Month(dateInterval.getStartDate());
                    break;
                case (int) MoniSoftConstants.YEAR_INTERVAL:
                    runningInterval = new Year(dateInterval.getStartDate());
                    break;
//            default:
//                runningInterval = new CustomMinutePeriod((byte) aggInterval, dateInterval.getStartDate());
            }
        } else if (aggInterval < 1) {
            runningInterval = new CustomSecondPeriod((byte) (aggInterval * 60), dateInterval.getStartDate());
        } else {
            runningInterval = new CustomMinutePeriod((byte) aggInterval, dateInterval.getStartDate());
        }

        // AZ: der save-Button bleibt aktiviert - MONISOFT-8
        exportSaveButton.setEnabled(true);
        exportCancelButton.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.ABBRECHEN"));
        thread = new ExportThread();
        thread.setName("ExportThread");
        thread.start();
    }//GEN-LAST:event_exportSaveButtonActionPerformed

    /**
     *
     */
    class ExportThread extends StoppableThread {

        @Override
        public void run() {
            running = true;
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            try {
                if (aggInterval == MoniSoftConstants.RAW_INTERVAL) {
                    if (exportRawData(objects)) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.DATENEXPORT ABGESCHLOSSEN."));
                    }
                } else {
                    if (exportIntervalData(objects)) {
                        JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.DATENEXPORT ABGESCHLOSSEN."));
                    }
                }
            } catch (IOException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }

            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            jProgressBar1.setValue(0);
            jProgressBar1.setString("");
            exportSaveButton.setEnabled(true);
            exportCancelButton.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.SCHLIESSEN"));
            jTextField2.setText("");
            saveSelectionCheckBox.setSelected(false);
        }
    }

    /**
     *
     */
    private void updateSelectionComboBox() {
        savedSetSelector.setModel(SensorCollectionHandler.getSensorCollectionNamesAsComboBoxModel(SensorCollectionHandler.SIMPLE_COLLECTION, true, false));
        savedSetSelector.setSelectedIndex(0);

    }

    private void exportCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCancelButtonActionPerformed
        if (!(thread == null) && thread.isAlive()) {
            if (JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.EXPORT WIRKLICH ABBRECHEN"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.QUESTION"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                thread.stop();
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                jProgressBar1.setValue(0);
                jProgressBar1.setString("");
                exportSaveButton.setEnabled(true);
                exportCancelButton.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.CLOSE"));
            }
        } else {
            this.setVisible(false);
        }
    }//GEN-LAST:event_exportCancelButtonActionPerformed

    private void exportAddALLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAddALLButtonActionPerformed
        DefaultListModel availableList = (DefaultListModel) availableSensorList.getModel();
        DefaultListModel exportedList = (DefaultListModel) listToBeExported.getModel();
        exportedList.clear();
        for (int i = 0; i < availableList.size(); i++) {
            exportedList.addElement(availableList.getElementAt(i));
        }
    }//GEN-LAST:event_exportAddALLButtonActionPerformed

    private void exportRemoveAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRemoveAllButtonActionPerformed
        DefaultListModel exportedList = (DefaultListModel) listToBeExported.getModel();
        exportedList.clear();
    }//GEN-LAST:event_exportRemoveAllButtonActionPerformed

    private void exportRemoveSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportRemoveSelectionActionPerformed
        Object exportedSelected[] = listToBeExported.getSelectedValues();
        DefaultListModel exportedListModel = (DefaultListModel) listToBeExported.getModel();
        for (int i = 0; i < exportedSelected.length; i++) {
            exportedListModel.removeElement(exportedSelected[i]);
        }
        listToBeExported.clearSelection();
}//GEN-LAST:event_exportRemoveSelectionActionPerformed

    private void exportAddSelectionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportAddSelectionButtonActionPerformed
        Object availabeSelected[] = availableSensorList.getSelectedValues();
        DefaultListModel exportedList = (DefaultListModel) listToBeExported.getModel();
        boolean isNew;
        for (int i = 0; i < availabeSelected.length; i++) {
            isNew = true;
            for (int j = 0; j < exportedList.size(); j++) {
                if (availabeSelected[i].equals(exportedList.getElementAt(j))) {
                    isNew = false;
                }
            }
            if (isNew) {
                exportedList.addElement(availabeSelected[i]);
            }
        }
        availableSensorList.clearSelection();
}//GEN-LAST:event_exportAddSelectionButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        int returnVal = jFileChooser1.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jFileChooser1.getSelectedFile();
            fileNameTextField.setText(file.toString());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * Methode NumberFormatTextFieldKeyTyped: Wenn Format manuell geändert die
     * Vorschau anpassen
     */
    private void NumberFormatTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NumberFormatTextFieldKeyTyped
        handleFormatPreview();
}//GEN-LAST:event_NumberFormatTextFieldKeyTyped

    /**
     * Methode DecComboBoxItemStateChanged: Wenn Dezimaldarstellung gewechselt
     * die Vorschau anpassen
     */
    private void DecComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_DecComboBoxItemStateChanged
        handleFormatPreview();
    }//GEN-LAST:event_DecComboBoxItemStateChanged

    /**
     * Methode savedSetSelectorItemStateChanged: Liest das gewählte Messpunktset
     * aus der serialisierten Datei und setzt is in die Auswahlliste
     */
    private void savedSetSelectorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_savedSetSelectorItemStateChanged
        listToBeExported.setModel(SensorCollectionHandler.getSensorCollectionAsListModel((String) savedSetSelector.getSelectedItem()));
}//GEN-LAST:event_savedSetSelectorItemStateChanged

    /**
     * Methode exportIntervalChooserActionPerformed: Wenn "Rohdaten" gewählt
     * wird der Dateimodus auf "Eine Datei pro Messpunkt" gesetzt
     */
    private void exportIntervalChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportIntervalChooserActionPerformed
        if (((IntervalSelectorEntry) exportIntervalChooser.getSelectedItem()).intValue() == MoniSoftConstants.RAW_INTERVAL) {
            CounterModebuttonGroup.clearSelection();
            ExportConsumtionRadioButton.setEnabled(false);
            ExportCounterRadioButton.setEnabled(false);
            ExportPowerRadioButton.setEnabled(false);
            singleFileCheckBox.setSelected(true);
        } else {
            if (!ExportConsumtionRadioButton.isEnabled()) {
                ExportConsumtionRadioButton.setEnabled(true);
                ExportCounterRadioButton.setEnabled(true);
                ExportPowerRadioButton.setEnabled(true);
                ExportConsumtionRadioButton.setSelected(true);
            }
        }
    }//GEN-LAST:event_exportIntervalChooserActionPerformed

    private void singleFileCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleFileCheckBoxActionPerformed
        if (!singleFileCheckBox.isSelected()) {
            if (exportIntervalChooser.getSelectedIndex() == 0) {
                exportIntervalChooser.setSelectedIndex(1);
            }
            ExportConsumtionRadioButton.setEnabled(true);
            ExportCounterRadioButton.setEnabled(true);
            ExportPowerRadioButton.setEnabled(true);
            ExportConsumtionRadioButton.setSelected(true);
        }
    }//GEN-LAST:event_singleFileCheckBoxActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.CSV_EXPORT.getPage());
}//GEN-LAST:event_jButton7help

    /**
     * Methode handleFormatPreview: Passt die Vorschau für das Exportformat je
     * nach gewählten Optionen an
     */
    private void handleFormatPreview() {
        decimal_separator_index = DecComboBox.getSelectedIndex();
        DecimalFormat decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("en", "EN"));
        switch (decimal_separator_index) {
            case 0:
                decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("en", "EN"));
                break;
            case 1:
                decFormat = (DecimalFormat) NumberFormat.getInstance(new Locale("de", "DE"));
                break;
        }
        NumberFormatTextField.setForeground(Color.BLACK);
        try {
            decFormat.applyPattern(NumberFormatTextField.getText());
            jLabel7.setText(decFormat.format(17.123456789));
        } catch (Exception e) {
            NumberFormatTextField.setForeground(Color.RED);
            Messages.showException(e);
            Messages.showException(e);
        }
    }

    /**
     * Methode getDateValue: Gibt das Datum des übergebenen DateChoosers zurück
     *
     * @param chooser Der zu betrachtende DateChooser
     */
    public String getDateValue(JDateChooser chooser) {
        Date date = chooser.getDate();
        // Wenn keine Datum geliefert wird null zurückgeben
        if (date == null) {
            return null;
        }
        // Umformatieren in MySQL-Format
        // TODO Was passiert wenn locale englisch??? Anpassen!!
        SimpleDateFormat mySQLformat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = mySQLformat.format(date);
        return dateString;
    }

    private boolean exportRawData(Object objects[]) throws IOException {
        SensorProperties entry;
//        ArrayList data[] = new ArrayList[3]; // 0 enthaelt die Zeitpunkte, 1 die Werte
        MeasurementTreeSet dataSet;
//        Date date = new Date();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"); // TODO anpassbar machen
        boolean first;

        // Einstellungen für Ausgabeformat definieren
        String field_separator = MoniSoftConstants.getFieldSeparator(FieldComboBox.getSelectedIndex());

        // Zahlenformat festlegen
        DecimalFormat decFormat = MoniSoftConstants.getDecimalFormat(DecComboBox.getSelectedIndex(), NumberFormatTextField.getText());

        jProgressBar1.setMaximum(objects.length);
        for (int i = 0; i < objects.length; i++) {       // die einzelnen Sensoren (und damit Dateien) durchgehen
            entry = (SensorProperties) objects[i];

            jProgressBar1.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.CALCULATING") + "  " + SensorInformation.getDisplayName(entry.getSensorID())); // Fortschtrittsbalken aktualisieren
            dataSet = readDataFromDB(entry.getSensorID());     // Daten fuer den jeweiligen Sensor aus DB holen

            String fileName;
            if (fileNameTextField.getText().contains(".")) {
                String filenamePart = fileNameTextField.getText().split("\\.")[0];
                String suffix = fileNameTextField.getText().split("\\.")[1];
                if (suffix == null || suffix.isEmpty()) {
                    suffix = "csv";
                }
                fileName = filenamePart + "_" + SensorInformation.getDisplayName(entry.getSensorID()) + "." + suffix;
            } else {
                fileName = fileNameTextField.getText() + "_" + SensorInformation.getDisplayName(entry.getSensorID()) + ".csv";
            }

            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), MoniSoft.getInstance().getApplicationProperties().getProperty("FileEncoding")));

            // einzelne Zeitpunkte (Zeilen) durchgehen
            first = true;
            for (Measurement measurement : dataSet) {

                if (first) { // wenn erste Zeile einen den Kopf ausgeben
                    out.write(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.TIMESTAMP") + field_separator + SensorInformation.getDisplayName(entry.getSensorID()));
                    out.newLine();
                    first = false;
                }
                out.write(outputFormat.format(new Date(measurement.getTime())) + field_separator + decFormat.format(measurement.getValue()));
                out.newLine();
            }

            out.close();

            if (compressCheckBox.isSelected()) {
                jProgressBar1.setIndeterminate(true);
                jProgressBar1.setString("Komprimiere Datei ...");
                compressFile(new File(fileName));  // zip the output file
                jProgressBar1.setIndeterminate(false);
            }

            jProgressBar1.setValue(i + 1);
        }
        return true;
    }

    /**
     *
     *
     * @param objects
     * @return
     * @throws java.io.IOException
     */
    private boolean exportIntervalData(Object objects[]) throws IOException {
        ArrayList dateList = new ArrayList(500);
        Date date = new Date();
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        if (aggInterval == MoniSoftConstants.DAY_INTERVAL || aggInterval == MoniSoftConstants.WEEK_INTERVAL) {
            outputFormat = new SimpleDateFormat("dd.MM.yyyy");
        }
        if (aggInterval == MoniSoftConstants.MONTH_INTERVAL) {
            outputFormat = new SimpleDateFormat("MM.yyyy");
        }
        if (aggInterval == MoniSoftConstants.YEAR_INTERVAL) {
            outputFormat = new SimpleDateFormat("yyyy");
        }

        SensorProperties entry;
        Stack timeStack = new Stack();
        Stack valueStack = new Stack();
        File outFile;
        StringBuilder headerLine = new StringBuilder(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.TIMESTAMP"));
        ArrayList<BufferedReader> buffers = new ArrayList<BufferedReader>();
        MeasurementTreeSet dataSet;

        // Forschrittsbalken auf die Anzahl der zu exportierdnden Objekte setzen
        jProgressBar1.setMaximum(objects.length);

        // Einstellungen für Ausgabeformat definieren
        String field_separator = MoniSoftConstants.getFieldSeparator(FieldComboBox.getSelectedIndex());

        // Zahlenformat festlegen
        DecimalFormat decFormat = MoniSoftConstants.getDecimalFormat(DecComboBox.getSelectedIndex(), NumberFormatTextField.getText());

        // Liste aller geforderten "Zwangs-"Zeitpunkte erzeugen
        int noOfValues = 0;
        while (runningInterval.getFirstMillisecond() < dateInterval.getEndDate().getTime()) {
            noOfValues++;
//            System.out.println("Zwangszeitpunkt: " + runningInterval.getStart());
            dateList.add(runningInterval.getStart().getTime());
            runningInterval = runningInterval.next();
        }
        
        for (int i = 0; i < objects.length; ++i) {       // die einzelnen Sensoren durchgehen
            entry = (SensorProperties) objects[i];
            dataSet = readDataFromDB(entry.getSensorID());     // Daten fuer den jeweiligen Sensor aus DB holen
//            System.out.println("Daten gelesen " + map.size());
//            System.out.println("Anzahl " + data[0].size());
            headerLine.append(field_separator).append(SensorInformation.getDisplayName(entry.getSensorID()));
            jProgressBar1.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.CALCULATING") + " " + SensorInformation.getDisplayName(entry.getSensorID()));
            // Stacks leeren
            timeStack.empty();
            valueStack.empty();

            for (Measurement measurement : dataSet.descendingSet()) {
                
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy hh:mm:ss" );
                Date tempDate = new Date( measurement.getTime() );
                
                // System.out.println( "Measurement: " + simpleDateFormat.format( tempDate ) + " Value: " + measurement.getValue() );
                
                valueStack.push(measurement.getValue());
                timeStack.push(measurement.getTime());
            }

            BufferedWriter out;
            try {
                if (singleFileCheckBox.isSelected()) {  // Wenn Einzeldateien gewünscht sind "echte" ansonsten temporäre Dateien für jeden Messpunkt erzeugen
                    outFile = new File(fileNameTextField.getText());
                    String fileName = outFile.getName();  // der reine Dateiname
                    String path = outFile.getParent();  // das reine Verzeichnis
                    String suffix = getFileParts(fileName, 1);   // der Dateisuffix
                    String prefix = getFileParts(fileName, 0);  // der reine Dateiprefix
                    outFile = new File(path + File.separator + prefix + "_" + SensorInformation.getDisplayName(entry.getSensorID()) + suffix);
                } else {
                    outFile = File.createTempFile("msoft_" + SensorInformation.getDisplayName(entry.getSensorID()), ".mtp"); // Temporäre Datei für jeden Sensor erzeugen welche die Daten zwischenspeichert
                    outFile.deleteOnExit();
                }
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), MoniSoft.getInstance().getApplicationProperties().getProperty("FileEncoding")));
                buffers.add(new BufferedReader(new FileReader(outFile)));  // Schon mal einen Lese-Buffer für die eben erzeugte Datei erzeugen (werden unten benötigt)

                // "Zwangs"zeitpunkte durchlaufen und in Datei schreiben, leerer Wert wenn keine Daten
                long runningTime;
                boolean start = true;
                boolean first = true;
                for (int timeIndex = 0; timeIndex < dateList.size() && !timeStack.empty(); ++timeIndex) {
                    runningTime = (Long) dateList.get(timeIndex);

                    // solange Daten gelesen wurde die VOR dem gewünschten Bereich liegen  -> rauswerfen
                    while (!timeStack.empty() && start && runningTime > (Long) timeStack.peek()) {
                        timeStack.pop();
                        valueStack.pop();
                    }

                    start = false;
                    // wenn erste Zeile dann Kopf schreiben, ansonsten einen Zeilenumbruch
                    if (!first) {
                        out.newLine();
                    } else if (singleFileCheckBox.isSelected()) {
                        out.write(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.TIMESTAMP") + field_separator + SensorInformation.getDisplayName(entry.getSensorID()));
                        out.newLine();
                    }
                    // schreiben der Daten
                    if ((!timeStack.empty()) && runningTime == (Long) timeStack.peek()) {
//                        System.out.println(" voll und gleich");
                        date.setTime((Long) timeStack.pop());
                        if (valueStack.peek() != null) {
                            if (singleFileCheckBox.isSelected()) {
                                out.write(outputFormat.format(date) + field_separator);
                            }
                            out.write(decFormat.format(valueStack.pop()));
                        } else {
                            valueStack.pop();
                            if (singleFileCheckBox.isSelected()) {
                                out.write(outputFormat.format(date) + field_separator);
                            }
                            out.write(""); // leerer Eintrag
                        }
                        first = false;
                    } else {
                        date.setTime(runningTime);
                        if (singleFileCheckBox.isSelected()) {
                            out.write(outputFormat.format(date) + field_separator);
                        }
                        out.write("");
                        first = false;
                    }

                }
                out.newLine();
                out.close();


                if (singleFileCheckBox.isSelected() && compressCheckBox.isSelected()) {
                    jProgressBar1.setIndeterminate(true);
                    jProgressBar1.setString("Komprimiere Datei ...");
                    compressFile(outFile);  // zip the output file
                    jProgressBar1.setIndeterminate(false);
                }
            } catch (Exception e) {
                Messages.showException(e);
                thread.stop();
            }
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setValue(i + 1);
        }
        
        // AZ: nach dem Durchlauf CounterChangeErrorDialog auf false setzen - MONISOFT-8
        Interpolator.saveCancelDecision = false;
        Interpolator.chartType = Interpolator.EXPORT_TAB;
        

        // Einzelne Datumswerte und Dateien durchlaufen und die eigentliche Ausgabedatei schreiben
        jProgressBar1.setValue(0);
        jProgressBar1.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.SCHREIBE DATEI"));
        jProgressBar1.setMaximum(dateList.size());

        if (!singleFileCheckBox.isSelected()) {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileNameTextField.getText()), MoniSoft.getInstance().getApplicationProperties().getProperty("FileEncoding")));
            StringBuilder lineString; // = new StringBuilder("");
            String currentSep;
            out.write(headerLine.toString());
            out.newLine();
            // einzelne Zeitpunkte durchgehen (eine Zeile pro Zeitpunkt)
            for (int line = 0; line < dateList.size(); line++) {
                lineString = new StringBuilder("");
                jProgressBar1.setValue(line);
                currentSep = "";
                // einzelne temporäre Dateien (Messpunkte) durchgehen und in einer Zeile aneinanderhängen
                for (int fileNo = 0; fileNo < objects.length; fileNo++) {
                    lineString.append(currentSep).append(buffers.get(fileNo).readLine());
                    currentSep = field_separator;
                }
                out.write(outputFormat.format(dateList.get(line)) + field_separator + lineString.toString());
                out.newLine();
            }

            out.close();
            // Alle Dateien schliessen und löschen
            for (int fileNo = 0; fileNo < objects.length; fileNo++) {
                buffers.get(fileNo).close();
            }

            if (compressCheckBox.isSelected()) {
                jProgressBar1.setIndeterminate(true);
                jProgressBar1.setString("Komprimiere Datei ...");
                compressFile(new File(fileNameTextField.getText()));  // zip the output file
                jProgressBar1.setIndeterminate(false);
            }

        }

        return true;
    }

    /**
     * Methode readDataFromDB Liest den übergebenen Messwert aus der DB und
     * erzeugt die aggregierten Zeitserien für Zeiten, Werte und Marker in einer
     * ArrayList
     *
     * @param sensor Name des Sensors
     * @param startDate Startdatum
     * @param endDate Enddatum
     * @param interval Aggregationsintervall
     * @return data ArrayList mit den 3 ArrayLists für Zeiten, Werte und Marker
     */
    private MeasurementTreeSet readDataFromDB(int sensorID) {

        MeasurementTreeSet dataSet;
        EventMode eventMode = EventMode.EVENT_SIGNIFICANT_MODE;
        boolean isCounter = SensorInformation.getSensorProperties(sensorID).isCounter();
        boolean isUsageUnit = SensorInformation.getSensorProperties(sensorID).isUsage();

        // Datum des Tages vor dem Abfrageintervall holen damit der Startwert bestimmt werden kan
        if (aggInterval != MoniSoftConstants.RAW_INTERVAL) { // wenn nicht Rohdaten
            dateInterval.setStartDate(getPreviousDay(dateInterval.getStartDate()));
        }

        if (eventMeanRadioButton.isSelected()) {
            eventMode = EventMode.EVENT_MEAN_MODE;
        }
        jProgressBar1.setString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ExportDialog.READFROMDB") + " " + SensorInformation.getDisplayName(sensorID));

        DatasetWorker dw = this.datasetWorkerFactory.createFor(sensorID);

        dw.setReference(null);
        dw.setEventMode(eventMode);
        dw.setVerbose(true);
        dataSet = (MeasurementTreeSet) dw.getInterpolatedData(dateInterval, GeneralDataSetGenerator.getPeriodForTimeStamp(aggInterval, dateInterval.getStartDate().getTime()), CounterMode.getInterpolationMode(ExportPowerRadioButton.isSelected(), ExportCounterRadioButton.isSelected(), isCounter, isUsageUnit, MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"), MoniSoft.getInstance().getApplicationProperties().getProperty("CalcPartlyConsumptions").equals("1")));

        return dataSet;
    }

    /**
     * Liefert den gewünschten Teil des Dateinamens zurück<br> Wenn part = 0
     * wird der Name der Datei ohne Endung geliefert<br> Wenn part = 1 wird die
     * Dateiendung geliefert<br> Wenn part einen anderen Wert hat wird der
     * Dateiname selbst wieder geliefert
     *
     * @param file Der Name der Datei
     * @param part 0 oder 1
     * @return
     */
    private String getFileParts(String file, int part) {
        String suffix;
        String prefix;
        if (file.indexOf(".") != -1) {
            suffix = file.substring(file.lastIndexOf("."), file.length());
            prefix = file.substring(0, file.lastIndexOf("."));
        } else {
            suffix = "";
            prefix = file;
        }
        switch (part) {
            case 0:
                return prefix;
            case 1:
                return suffix;
        }
        return file;
    }

    /**
     * Liefert den Tag vor dem übergebenen Tag zurück
     *
     * @param day
     * @return
     */
    private Date getPreviousDay(Date date) {
        Calendar cal_date = Calendar.getInstance();
        cal_date.setTime(date);
        cal_date.add(Calendar.DATE, -1);
        date = cal_date.getTime();
        return date;
    }

    private void compressFile(File file) {
        boolean zipped = new FileCompressor().compressFile(file, true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup CounterModebuttonGroup;
    private javax.swing.JComboBox DecComboBox;
    private javax.swing.JRadioButton ExportConsumtionRadioButton;
    private javax.swing.JRadioButton ExportCounterRadioButton;
    private javax.swing.JRadioButton ExportPowerRadioButton;
    private javax.swing.JComboBox FieldComboBox;
    private javax.swing.JTextField NumberFormatTextField;
    private javax.swing.JList availableSensorList;
    private javax.swing.JCheckBox compressCheckBox;
    public com.toedter.calendar.JDateChooser endDateChooser;
    private javax.swing.JRadioButton eventMeanRadioButton;
    private javax.swing.ButtonGroup eventModeButtongroup;
    private javax.swing.JRadioButton eventSignificantRadioButton;
    private javax.swing.JButton exportAddALLButton;
    private javax.swing.JButton exportAddOneButton;
    private javax.swing.JButton exportCancelButton;
    public javax.swing.JComboBox exportIntervalChooser;
    private javax.swing.JButton exportRemoveAllButton;
    private javax.swing.JButton exportRemoveOne;
    private javax.swing.JButton exportSaveButton;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton7;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField2;
    public javax.swing.JList listToBeExported;
    private javax.swing.JCheckBox saveSelectionCheckBox;
    private javax.swing.JComboBox savedSetSelector;
    private javax.swing.JCheckBox singleFileCheckBox;
    public com.toedter.calendar.JDateChooser startDateChooser;
    // End of variables declaration//GEN-END:variables
}
