package de.jmonitoring.DatabaseGeneration;

import de.jmonitoring.Components.PasswordDialog;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * This class contains the UI for the creation of a new project in the database.
 *
 * @author togro
 */
public class NewProjectDialog extends javax.swing.JDialog {

    private DBConnector connector = null;
    private File sensorFile;
    private File buildingFile;
    private final MainApplication gui;

    /**
     * Creates new form NewProjectDialog
     */
    public NewProjectDialog(MainApplication gui, boolean modal) {
        super(gui.getMainFrame(), modal);
        this.gui = gui;
        initComponents();
    }

    /**
     * Shows the dialog in which the user can set up the database creation and
     * define input files
     */
    public void showChoiceDialog() {
        createChoiceDialog.setLocationRelativeTo(this);
        createChoiceDialog.add(chooserPanel, BorderLayout.CENTER);
        createChoiceDialog.setVisible(true);
        createChoiceDialog.toFront();
    }

    public void setConnector(DBConnector conn) {
        connector = conn;
    }

    /**
     * Shows the password dialog and tries to connet to the given database
     */
    private void showPasswordDialog() {
        PasswordDialog pd = new PasswordDialog(this.gui.getMainFrame(), true, true, null, connector);
        pd.setFields(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultUser"), MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultServer"), MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultServerPort"), jTextField1.getText(), false);
        pd.setLocationRelativeTo(this);
        pd.setVisible(true);
        if (pd.hasConnected() && connector != null) { // Verbindung zur DB hat geklappt
            createChoiceDialog.remove(chooserPanel);
            createChoiceDialog.add(confirmPanel, BorderLayout.CENTER);
            createChoiceDialog.setVisible(false);
            createChoiceDialog.setVisible(true);
        }
        pd.dispose(); // Passwortdialog schliessen
    }

    /**
     * Closes the creation dialog and shows the password dialog to be able to
     * connect to the new scheme
     */
    private void closeAndConnect() {
        createChoiceDialog.dispose();
        this.gui.showPasswordDialog(); // this shows th password dialog from the GUI!!!
    }

    /**
     * Shows the manual
     *
     * @throws HeadlessException
     */
    private void showHelp() throws HeadlessException {
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.NEW_PROJECT.getPage());
    }

    /**
     * Starts the creation thread
     */
    private void runCreation() {
        Thread t = new Thread(new CreateThread());
        t.start();
    }

    /**
     * In this thread the database structure is created step by step
     */
    private class CreateThread implements Runnable {

        DBCreator creator = new DBCreator(NewProjectDialog.this.gui.getMainFrame());
        int result;
        boolean breakOnError = false;
        String errorMessage = "";

        @Override
        public void run() {
            createChoiceDialog.setVisible(true);
            if (creator.hasStructure() != DBCreator.IS_EMPTY) { //TODO Unterscheiden zwischen MoniSoft-Struktur und anderen Tabellen?
                result = JOptionPane.showConfirmDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.THEDATABASE") + " '" + MoniSoft.getInstance().getDBConnector().getDBName() + "' " + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.CONTAINSDATA"), java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.CONFIRM"), JOptionPane.YES_NO_OPTION);
            }

            if (result == JOptionPane.YES_OPTION) {    // Bearbeitung bginnen
                createChoiceDialog.remove(confirmPanel);
                createChoiceDialog.add(advancePanel, BorderLayout.CENTER);


                // Datenbankstruktur erstellen?
                if (jCheckBox1.isSelected()) {  // Datenbankstruktur erstellen?
                    point1Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/workHere.png")));
                    if (creator.createStructure()) {
                        point1Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/success.png")));
                    } else {
                        point1Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/fail.png")));
                        errorMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.FEHLER BEIM ANLEGEN DER TABELLENSTRUKTUR");
                        breakOnError = true;
                    }
                } else {
                    jLabel5.setForeground(Color.gray); // Option nicht gewählt
                }


                // Standardinhalte (Einheiten) sollen erzeugt werden
                if (jCheckBox2.isSelected() && !breakOnError) {  // Standardinhalte (Einheiten) sollen erzeugt werden
                    point2Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/workHere.png")));
                    if (creator.fillUnitTable()) {
                        point2Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/success.png")));
                    } else {
                        point2Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/fail.png")));
                        errorMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.FEHLER BEIM EINTRAGEN DER STANDARDINHALTE");
                        breakOnError = true;
                    }
                } else if (breakOnError) {
                    point2Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/restricted.png")));
                    jLabel4.setForeground(Color.gray);
                } else {
                    jLabel4.setForeground(Color.gray);
                }

                // Gebäudedatei aus CSV-Datei einlesen
                if (jCheckBox5.isSelected() && !breakOnError) {  // Standardinhalte (Einheiten) sollen erzeugt werden
                    point5Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/workHere.png")));
                    if (creator.fillBuildingTable(buildingFile, encodingComboBox.getSelectedItem().toString(), fieldDividerComboBox.getSelectedItem().toString().charAt(0)) == DBCreator.SUCCESS) {
                        point5Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/success.png")));
                    } else {
                        point5Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/fail.png")));
                        errorMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.FEHLER BEIM EINTRAGEN DER GEBÄUDELISTE. BITTE CSV-DATEI ÜBERPRÜFEN.");
                        breakOnError = true;
                    }
                } else if (breakOnError) {
                    point5Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/restricted.png")));
                    jLabel9.setForeground(Color.gray);
                } else {
                    jLabel9.setForeground(Color.gray);
                }

                // Messpunktliste aus CSV-Datei einlesen
                if (jCheckBox3.isSelected() && !breakOnError) {
                    point3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/workHere.png")));
                    if (creator.fillSensorTable(sensorFile, encodingComboBox.getSelectedItem().toString(), fieldDividerComboBox.getSelectedItem().toString().charAt(0), false) == DBCreator.SUCCESS) {
                        point3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/success.png")));
                    } else {
                        point3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/fail.png")));
                        errorMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.FEHLER BEIM EINTRAGEN DER MESSPUNKTLISTE. BITTE CSV-DATEI ÜBERPRÜFEN.");
                        breakOnError = true;
                    }
                } else if (breakOnError) {
                    point3Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/restricted.png")));
                    jLabel6.setForeground(Color.gray);
                } else {
                    jLabel6.setForeground(Color.gray);
                }

                // Messdatendatei (.mon) einlesen
                if (jCheckBox4.isSelected() && !breakOnError) {
                    point4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/workHere.png")));
                    if (creator.fillUnitTable()) {
                        point4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/success.png")));
                    } else {
                        point4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/fail.png")));
                        errorMessage = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.FEHLER BEIM EINTRAGEN DER MESSDATEN. BITTE MON-DATEI ÜBERPRÜFEN.");
                        breakOnError = true;
                    }
                } else if (breakOnError) {
                    point4Button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/restricted.png")));
                    jLabel7.setForeground(Color.gray);
                } else {
                    jLabel7.setForeground(Color.gray);
                }
            }

            if (breakOnError) {
                errorMessageLabel.setText(errorMessage);
                advancePanel.revalidate();
            }
            finishButton.setEnabled(true);
        }
    }

    /**
     * Get the file for the sensor list
     */
    private void openSensorFileChooser() {
        sensorFile = pickFile();
        if (sensorFile != null) {
            sensorFileTextField.setForeground(Color.BLACK);
            sensorFileTextField.setText(sensorFile.getName());
        }
    }

    /**
     * Get the file for the building list
     */
    private void openBuildingFileChooser() {
        buildingFile = pickFile();
        if (buildingFile != null) {
            buildingFileTextField.setForeground(Color.BLACK);
            buildingFileTextField.setText(buildingFile.getName());
        }
    }

    /**
     * pens a {@link JFileChooser} to pick the desired file
     */
    private File pickFile() {
        JFileChooser fc = new JFileChooser(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultSaveFolder"));
        fc.setMultiSelectionEnabled(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".csv") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.CSV-DATEI");
            }
        });

        Integer option = fc.showDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NewProjectDialog.ÖFFNEN"));
        if (option == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
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

        chooserPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        createChoiceDialog = new javax.swing.JDialog();
        titleBarPanel = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();
        confirmPanel = new javax.swing.JPanel();
        OKButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        sensorFileTextField = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        openSensorlistButton = new javax.swing.JButton();
        openDataFileButton = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        encodingComboBox = new javax.swing.JComboBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        buildingFileTextField = new javax.swing.JTextField();
        openBuildingFileButton = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        fieldDividerComboBox = new javax.swing.JComboBox();
        advancePanel = new javax.swing.JPanel();
        finishButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        point2Button = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        point1Button = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        point3Button = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        point4Button = new javax.swing.JButton();
        errorMessageLabel = new javax.swing.JLabel();
        point5Button = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        jTextArea1.setBackground(javax.swing.UIManager.getDefaults().getColor("CheckBoxMenuItem.background"));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jTextArea1.setText(bundle.getString("NewProjectDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(null);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/info.png"))); // NOI18N
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(false);
        jButton1.setFocusPainted(false);
        jButton1.setFocusable(false);
        jButton1.setRequestFocusEnabled(false);
        jButton1.setRolloverEnabled(false);

        jButton2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton2.setText(bundle.getString("NewProjectDialog.jButton2.text")); // NOI18N
        jButton2.setEnabled(false);

        jButton3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton3.setText(bundle.getString("NewProjectDialog.jButton3.text")); // NOI18N
        jButton3.setSelected(true);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbCatalogExistsButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("NewProjectDialog.jLabel1.text")); // NOI18N

        jTextField1.setFocusCycleRoot(true);

        jButton4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton4.setText(bundle.getString("NewProjectDialog.jButton4.text")); // NOI18N
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout chooserPanelLayout = new javax.swing.GroupLayout(chooserPanel);
        chooserPanel.setLayout(chooserPanelLayout);
        chooserPanelLayout.setHorizontalGroup(
            chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chooserPanelLayout.createSequentialGroup()
                .addGroup(chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(chooserPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1)
                        .addGap(5, 5, 5)
                        .addComponent(jScrollPane1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chooserPanelLayout.createSequentialGroup()
                        .addGap(67, 67, 67)
                        .addGroup(chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(chooserPanelLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 421, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        chooserPanelLayout.setVerticalGroup(
            chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chooserPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chooserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4)
                .addGap(24, 24, 24))
        );

        createChoiceDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        createChoiceDialog.setMinimumSize(new java.awt.Dimension(630, 365));
        createChoiceDialog.setModal(true);
        createChoiceDialog.setResizable(false);

        titleBarPanel.setBackground(new java.awt.Color(0, 102, 204));
        titleBarPanel.setPreferredSize(new java.awt.Dimension(100, 40));

        jLabel37.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText(bundle.getString("NewProjectDialog.jLabel37.text")); // NOI18N

        jButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jButton10.setBorderPainted(false);
        jButton10.setContentAreaFilled(false);
        jButton10.setFocusPainted(false);
        jButton10.setIconTextGap(0);
        jButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newProjectHelp(evt);
            }
        });

        javax.swing.GroupLayout titleBarPanelLayout = new javax.swing.GroupLayout(titleBarPanel);
        titleBarPanel.setLayout(titleBarPanelLayout);
        titleBarPanelLayout.setHorizontalGroup(
            titleBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 320, Short.MAX_VALUE)
                .addComponent(jButton10)
                .addContainerGap())
        );
        titleBarPanelLayout.setVerticalGroup(
            titleBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(titleBarPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(titleBarPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton10)
                    .addComponent(jLabel37))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        createChoiceDialog.getContentPane().add(titleBarPanel, java.awt.BorderLayout.NORTH);

        confirmPanel.setMinimumSize(new java.awt.Dimension(625, 316));
        confirmPanel.setPreferredSize(new java.awt.Dimension(625, 316));
        confirmPanel.setLayout(null);

        OKButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        OKButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        OKButton.setText(bundle.getString("NewProjectDialog.OKButton.text")); // NOI18N
        OKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OKButtonActionPerformed(evt);
            }
        });
        confirmPanel.add(OKButton);
        OKButton.setBounds(540, 260, 70, 26);

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        cancelButton.setText(bundle.getString("NewProjectDialog.cancelButton.text")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        confirmPanel.add(cancelButton);
        cancelButton.setBounds(400, 260, 130, 26);

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel2.setText(bundle.getString("NewProjectDialog.jLabel2.text")); // NOI18N
        confirmPanel.add(jLabel2);
        jLabel2.setBounds(10, 10, 487, 15);

        jCheckBox1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox1.setSelected(true);
        jCheckBox1.setText(bundle.getString("NewProjectDialog.jCheckBox1.text")); // NOI18N
        confirmPanel.add(jCheckBox1);
        jCheckBox1.setBounds(20, 50, 163, 21);

        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox2.setSelected(true);
        jCheckBox2.setText(bundle.getString("NewProjectDialog.jCheckBox2.text")); // NOI18N
        confirmPanel.add(jCheckBox2);
        jCheckBox2.setBounds(20, 80, 224, 21);

        jCheckBox3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox3.setText(bundle.getString("NewProjectDialog.jCheckBox3.text")); // NOI18N
        confirmPanel.add(jCheckBox3);
        jCheckBox3.setBounds(20, 140, 183, 21);

        jCheckBox4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox4.setText(bundle.getString("NewProjectDialog.jCheckBox4.text")); // NOI18N
        jCheckBox4.setEnabled(false);
        confirmPanel.add(jCheckBox4);
        jCheckBox4.setBounds(20, 170, 150, 21);

        sensorFileTextField.setForeground(new java.awt.Color(153, 153, 153));
        sensorFileTextField.setText(bundle.getString("NewProjectDialog.sensorFileTextField.text")); // NOI18N
        sensorFileTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                sensorFileTextFieldFocusGained(evt);
            }
        });
        confirmPanel.add(sensorFileTextField);
        sensorFileTextField.setBounds(230, 140, 310, 19);

        jTextField3.setForeground(new java.awt.Color(153, 153, 153));
        jTextField3.setText(bundle.getString("NewProjectDialog.jTextField3.text")); // NOI18N
        jTextField3.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTextField3FocusGained(evt);
            }
        });
        confirmPanel.add(jTextField3);
        jTextField3.setBounds(230, 170, 310, 19);

        openSensorlistButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/folder.png"))); // NOI18N
        openSensorlistButton.setFocusPainted(false);
        openSensorlistButton.setPreferredSize(new java.awt.Dimension(88, 21));
        openSensorlistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSensorlistButtonActionPerformed(evt);
            }
        });
        confirmPanel.add(openSensorlistButton);
        openSensorlistButton.setBounds(560, 140, 30, 21);

        openDataFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/folder.png"))); // NOI18N
        openDataFileButton.setFocusPainted(false);
        openDataFileButton.setPreferredSize(new java.awt.Dimension(88, 21));
        confirmPanel.add(openDataFileButton);
        openDataFileButton.setBounds(560, 170, 30, 21);

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText(bundle.getString("NewProjectDialog.jLabel8.text")); // NOI18N
        confirmPanel.add(jLabel8);
        jLabel8.setBounds(20, 220, 190, 13);

        encodingComboBox.setBackground(new java.awt.Color(255, 255, 255));
        encodingComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        encodingComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ISO-8859-1", "UTF-8" }));
        encodingComboBox.setPreferredSize(new java.awt.Dimension(61, 19));
        confirmPanel.add(encodingComboBox);
        encodingComboBox.setBounds(210, 220, 110, 19);

        jCheckBox5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jCheckBox5.setText(bundle.getString("NewProjectDialog.jCheckBox5.text")); // NOI18N
        confirmPanel.add(jCheckBox5);
        jCheckBox5.setBounds(20, 110, 130, 21);

        buildingFileTextField.setForeground(new java.awt.Color(153, 153, 153));
        buildingFileTextField.setText(bundle.getString("NewProjectDialog.buildingFileTextField.text")); // NOI18N
        confirmPanel.add(buildingFileTextField);
        buildingFileTextField.setBounds(230, 110, 310, 19);

        openBuildingFileButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/folder.png"))); // NOI18N
        openBuildingFileButton.setFocusPainted(false);
        openBuildingFileButton.setPreferredSize(new java.awt.Dimension(88, 21));
        openBuildingFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openBuildingFileButtonActionPerformed(evt);
            }
        });
        confirmPanel.add(openBuildingFileButton);
        openBuildingFileButton.setBounds(560, 110, 30, 21);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel10.setText(bundle.getString("NewProjectDialog.jLabel10.text")); // NOI18N
        confirmPanel.add(jLabel10);
        jLabel10.setBounds(20, 250, 56, 13);

        fieldDividerComboBox.setBackground(new java.awt.Color(255, 255, 255));
        fieldDividerComboBox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        fieldDividerComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { ",", ";" }));
        fieldDividerComboBox.setPreferredSize(new java.awt.Dimension(61, 19));
        confirmPanel.add(fieldDividerComboBox);
        fieldDividerComboBox.setBounds(90, 250, 40, 19);

        advancePanel.setMinimumSize(new java.awt.Dimension(625, 316));
        advancePanel.setLayout(null);

        finishButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        finishButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        finishButton.setText(bundle.getString("NewProjectDialog.finishButton.text")); // NOI18N
        finishButton.setEnabled(false);
        finishButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                finishButtonActionPerformed(evt);
            }
        });
        advancePanel.add(finishButton);
        finishButton.setBounds(460, 260, 150, 26);

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel3.setText(bundle.getString("NewProjectDialog.jLabel3.text")); // NOI18N
        advancePanel.add(jLabel3);
        jLabel3.setBounds(10, 10, 333, 15);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel4.setText(bundle.getString("NewProjectDialog.jLabel4.text")); // NOI18N
        advancePanel.add(jLabel4);
        jLabel4.setBounds(100, 110, 270, 15);

        point2Button.setBorder(null);
        point2Button.setBorderPainted(false);
        point2Button.setContentAreaFilled(false);
        point2Button.setFocusPainted(false);
        point2Button.setFocusable(false);
        point2Button.setRequestFocusEnabled(false);
        point2Button.setVerifyInputWhenFocusTarget(false);
        advancePanel.add(point2Button);
        point2Button.setBounds(50, 100, 40, 30);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel5.setText(bundle.getString("NewProjectDialog.jLabel5.text")); // NOI18N
        advancePanel.add(jLabel5);
        jLabel5.setBounds(100, 70, 270, 15);

        point1Button.setBorder(null);
        point1Button.setBorderPainted(false);
        point1Button.setContentAreaFilled(false);
        point1Button.setFocusPainted(false);
        point1Button.setFocusable(false);
        point1Button.setRequestFocusEnabled(false);
        point1Button.setVerifyInputWhenFocusTarget(false);
        advancePanel.add(point1Button);
        point1Button.setBounds(50, 60, 40, 30);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel6.setText(bundle.getString("NewProjectDialog.jLabel6.text")); // NOI18N
        advancePanel.add(jLabel6);
        jLabel6.setBounds(100, 190, 270, 15);

        point3Button.setBorder(null);
        point3Button.setBorderPainted(false);
        point3Button.setContentAreaFilled(false);
        point3Button.setFocusPainted(false);
        point3Button.setFocusable(false);
        point3Button.setRequestFocusEnabled(false);
        point3Button.setVerifyInputWhenFocusTarget(false);
        advancePanel.add(point3Button);
        point3Button.setBounds(50, 180, 40, 30);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel7.setText(bundle.getString("NewProjectDialog.jLabel7.text")); // NOI18N
        advancePanel.add(jLabel7);
        jLabel7.setBounds(100, 230, 270, 15);

        point4Button.setBorder(null);
        point4Button.setBorderPainted(false);
        point4Button.setContentAreaFilled(false);
        point4Button.setFocusPainted(false);
        point4Button.setFocusable(false);
        point4Button.setRolloverEnabled(false);
        point4Button.setVerifyInputWhenFocusTarget(false);
        advancePanel.add(point4Button);
        point4Button.setBounds(50, 220, 40, 30);

        errorMessageLabel.setFont(new java.awt.Font("Arial Black", 0, 11)); // NOI18N
        errorMessageLabel.setForeground(java.awt.Color.red);
        errorMessageLabel.setText(bundle.getString("NewProjectDialog.errorMessageLabel.text")); // NOI18N
        advancePanel.add(errorMessageLabel);
        errorMessageLabel.setBounds(20, 30, 550, 17);

        point5Button.setBorder(null);
        point5Button.setBorderPainted(false);
        point5Button.setContentAreaFilled(false);
        point5Button.setFocusPainted(false);
        point5Button.setFocusable(false);
        point5Button.setRequestFocusEnabled(false);
        point5Button.setVerifyInputWhenFocusTarget(false);
        advancePanel.add(point5Button);
        point5Button.setBounds(50, 140, 40, 30);

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel9.setText(bundle.getString("NewProjectDialog.jLabel9.text")); // NOI18N
        advancePanel.add(jLabel9);
        jLabel9.setBounds(100, 150, 270, 15);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 533, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 331, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        createChoiceDialog.dispose();
}//GEN-LAST:event_cancelActionPerformed

    private void dbCatalogExistsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbCatalogExistsButtonActionPerformed
        showPasswordDialog();
}//GEN-LAST:event_dbCatalogExistsButtonActionPerformed

    private void sensorFileTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_sensorFileTextFieldFocusGained
        sensorFileTextField.setForeground(Color.BLACK);
        if (sensorFileTextField.getText().equals(MoniSoftConstants.chooseFile)) {
            sensorFileTextField.setText("");
        }
}//GEN-LAST:event_sensorFileTextFieldFocusGained

    private void jTextField3FocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField3FocusGained
        jTextField3.setForeground(Color.BLACK);
        if (jTextField3.getText().equals(MoniSoftConstants.chooseFile)) {
            jTextField3.setText("");
        }
    }//GEN-LAST:event_jTextField3FocusGained

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        createChoiceDialog.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void OKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_OKButtonActionPerformed
        runCreation();
    }//GEN-LAST:event_OKButtonActionPerformed

    private void finishButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_finishButtonActionPerformed
        closeAndConnect();
}//GEN-LAST:event_finishButtonActionPerformed

    private void openSensorlistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSensorlistButtonActionPerformed
        openSensorFileChooser();
    }//GEN-LAST:event_openSensorlistButtonActionPerformed

    private void openBuildingFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openBuildingFileButtonActionPerformed
        openBuildingFileChooser();
    }//GEN-LAST:event_openBuildingFileButtonActionPerformed

    private void newProjectHelp(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newProjectHelp
        showHelp();
}//GEN-LAST:event_newProjectHelp
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton OKButton;
    private javax.swing.JPanel advancePanel;
    private javax.swing.JTextField buildingFileTextField;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel chooserPanel;
    private javax.swing.JPanel confirmPanel;
    private javax.swing.JDialog createChoiceDialog;
    private javax.swing.JComboBox encodingComboBox;
    private javax.swing.JLabel errorMessageLabel;
    private javax.swing.JComboBox fieldDividerComboBox;
    private javax.swing.JButton finishButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JButton openBuildingFileButton;
    private javax.swing.JButton openDataFileButton;
    private javax.swing.JButton openSensorlistButton;
    private javax.swing.JButton point1Button;
    private javax.swing.JButton point2Button;
    private javax.swing.JButton point3Button;
    private javax.swing.JButton point4Button;
    private javax.swing.JButton point5Button;
    private javax.swing.JTextField sensorFileTextField;
    private javax.swing.JPanel titleBarPanel;
    // End of variables declaration//GEN-END:variables
}
