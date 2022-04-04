package de.jmonitoring.Components;

import de.jmonitoring.ApplicationProperties.AppPrefsDialog;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.DBIdentifier;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.networking.SSHTunnel;
import de.jmonitoring.utils.swing.EDT;
import java.awt.HeadlessException;

/**
 * This Dialog show the password dialog for database connections<p>
 *
 * @author togro
 */
public class PasswordDialog extends javax.swing.JDialog {

    private Color INVISIBLE = new Color(238, 238, 238);
    private DBConnector connector = null;
    private boolean GUIOnly = false; // Wenn wahr wird die Componente nur als Oberfläche für die Passworteingabe verwendet
    private boolean connectionSuccess = false;
    private final SSHTunnel ssht;

    /**
     * Creates a new Password Dialog
     *
     * @param parent The parent frame
     * @param modal Is this dialog modal
     * @param GUIonly Shall this dialog only be used to retrieve the password
     * without loading the project
     * @param defaultDB The project taht should be the dafault when the dialog
     * appears
     * @param conn The database connection to use
     */
    public PasswordDialog(java.awt.Frame parent, boolean modal, boolean GUIonly, String defaultDB, DBConnector conn) {
        super(parent, modal);
        this.ssht = new SSHTunnel(parent);
        initComponents();

        getRootPane().setDefaultButton(pwdOKButton);
        connector = conn;
        GUIOnly = GUIonly;
        if (GUIOnly) { // wenn nur als GUI benutzt ist die DB-Auswahl gesperrt und die übergebene DB wird angezeigt
            dbSelector.setEnabled(false);
            dbSelector.addItem(defaultDB); // TODO Nur hinzufügen wenn nicht schon enthalten?
            dbSelector.setSelectedItem(defaultDB);
            jButton1.setEnabled(false);
        }
        pwdField.requestFocusInWindow();
    }

    /**
     * Set the dialogs fields
     *
     * @param user Username
     * @param server Servername (IP-Adress)
     * @param port Server-port (most: 3306)
     * @param database database scheme (project) to connect to
     * @param refreshDatabases true if the list of available databases should be
     * refreshed
     */
    public void setFields(String user, String server, String port, String database, boolean refreshDatabases) {
        EDT.always();
        userField.setText(user);
        serverField.setSelectedItem(server);
        portField.setText(port);
        if (database != null) {
            dbSelector.setSelectedItem(database);
        } else {
            dbSelector.setSelectedIndex(0);
        }
        pwdField.selectAll();
    }

    /**
     * Sets the entry fields of the SSH-dialog to the given values
     *
     * @param user The ssh user
     * @param server The ssh server
     * @param port The ssh port (most: 22)
     * @param tunnelIP The internal IP of the datanase server
     * @param remotePort The port of the datanase server
     * @param localPort The port on the local machine wich will be the tunnel
     * endpoint
     */
    public void setSSHFields(String user, String server, String port, String tunnelIP, String remotePort, String localPort, String useTunnel) {
        UseSSHCheckBox.setSelected(useTunnel.equals("1"));
        SSHUserTextField.setText(user);
        SSHServerTextField.setText(server);
        SSHServerPortTextField.setText(port);
        SSHTunnelIPTextField.setText(tunnelIP);
        SSHTunnelPortTextField.setText(remotePort);
        SSHLocalPortTextField.setText(localPort);
        if (UseSSHCheckBox.isSelected()) {
            SSHStatusLabel.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/minus-shield.png")));
            SSHStatusLabel.setToolTipText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-NO_CONNECT"));
        }
    }

    /**
     * Returns the SSH session
     *
     * @return The {@link SSHTunnel}
     */
    public SSHTunnel getTunnelSession() {
        return ssht;
    }

    /**
     * Returns the state of the "favorites-skip" checkbox
     *
     * @return <code>true</code> if favorites should be skipped at startup
     */
    public boolean getSkipFavorites() {
        return skipFavoriteCharts.isSelected();
    }

    /**
     * Returns the state of the connection creation
     *
     * @return <code>true</code> if the are connected to the database
     */
    public boolean hasConnected() {
        return connectionSuccess;
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        serverField = new javax.swing.JComboBox();
        jLabel59 = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        pwdField = new javax.swing.JPasswordField();
        jLabel26 = new javax.swing.JLabel();
        dbSelector = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        failedLabel = new javax.swing.JLabel();
        saveAsDefaultCheckbox = new javax.swing.JCheckBox();
        skipFavoriteCharts = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        SSHServerTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        SSHUserTextField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        SSHServerPortTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        SSHTunnelIPTextField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        SSHTunnelPortTextField = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        SSHLocalPortTextField = new javax.swing.JTextField();
        UseSSHCheckBox = new javax.swing.JCheckBox();
        failedLabelSSH = new javax.swing.JLabel();
        saveSettingsCeckbox = new javax.swing.JCheckBox();
        jPanel14 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        pwdCancelButton = new javax.swing.JButton();
        pwdOKButton = new javax.swing.JButton();
        SSHStatusLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        setTitle(bundle.getString("PasswordDialog.title")); // NOI18N
        setAlwaysOnTop(true);
        setMinimumSize(new java.awt.Dimension(410, 220));
        setResizable(false);

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jTabbedPane1.setMaximumSize(new java.awt.Dimension(32767, 250));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(445, 300));

        jPanel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jPanel1.setMaximumSize(new java.awt.Dimension(420, 240));
        jPanel1.setMinimumSize(new java.awt.Dimension(420, 0));
        jPanel1.setPreferredSize(new java.awt.Dimension(420, 240));

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setText(bundle.getString("PasswordDialog.jLabel3.text")); // NOI18N

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel16.setText(bundle.getString("PasswordDialog.jLabel16.text")); // NOI18N

        serverField.setBackground(new java.awt.Color(255, 255, 255));
        serverField.setEditable(true);
        serverField.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        serverField.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "localhost", "127.0.0.1" }));
        serverField.setPreferredSize(new java.awt.Dimension(134, 19));

        jLabel59.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel59.setText(bundle.getString("PasswordDialog.jLabel59.text")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("PasswordDialog.jLabel1.text")); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("PasswordDialog.jLabel2.text")); // NOI18N

        jLabel26.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel26.setText(bundle.getString("PasswordDialog.jLabel26.text")); // NOI18N

        dbSelector.setBackground(new java.awt.Color(255, 255, 255));
        dbSelector.setEditable(true);
        dbSelector.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        dbSelector.setPreferredSize(new java.awt.Dimension(84, 19));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow_refresh.png"))); // NOI18N
        jButton1.setToolTipText(bundle.getString("PasswordDialog.jButton1.toolTipText")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshDatabaseAction(evt);
            }
        });

        failedLabel.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        failedLabel.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        failedLabel.setText(bundle.getString("PasswordDialog.failedLabel.text")); // NOI18N

        saveAsDefaultCheckbox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        saveAsDefaultCheckbox.setText(bundle.getString("PasswordDialog.saveAsDefaultCheckbox.text")); // NOI18N

        skipFavoriteCharts.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        skipFavoriteCharts.setText(bundle.getString("PasswordDialog.skipFavoriteCharts.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveAsDefaultCheckbox)
                    .addComponent(skipFavoriteCharts)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel16))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(dbSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(pwdField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(userField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                    .addComponent(serverField, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jLabel59)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(jLabel3)
                    .addComponent(failedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(448, 448, 448))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(serverField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel59)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(pwdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dbSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel26))
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(failedLabel)
                .addGap(18, 18, 18)
                .addComponent(skipFavoriteCharts)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveAsDefaultCheckbox)
                .addGap(21, 21, 21))
        );

        jTabbedPane1.addTab(bundle.getString("PasswordDialog.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 240));
        jPanel2.setPreferredSize(new java.awt.Dimension(408, 240));

        jLabel4.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel4.setText(bundle.getString("PasswordDialog.jLabel4.text")); // NOI18N

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("PasswordDialog.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel17.setText(bundle.getString("PasswordDialog.jLabel17.text")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHServerTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("PasswordDialog.jLabel5.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHUserTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel7.setText(bundle.getString("PasswordDialog.jLabel7.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHServerPortTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SSHUserTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(SSHServerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SSHServerPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(SSHServerTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SSHServerPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SSHUserTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), bundle.getString("PasswordDialog.jPanel4.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 9), new java.awt.Color(102, 102, 102))); // NOI18N

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel18.setText(bundle.getString("PasswordDialog.jLabel18.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHTunnelIPTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel6.setText(bundle.getString("PasswordDialog.jLabel6.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHTunnelPortTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel19.setText(bundle.getString("PasswordDialog.jLabel19.text")); // NOI18N

        binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, UseSSHCheckBox, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SSHLocalPortTextField, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SSHLocalPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(SSHTunnelIPTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SSHTunnelPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(45, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(SSHTunnelIPTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(SSHTunnelPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(SSHLocalPortTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        UseSSHCheckBox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        UseSSHCheckBox.setText(bundle.getString("PasswordDialog.UseSSHCheckBox.text")); // NOI18N

        failedLabelSSH.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        failedLabelSSH.setForeground(javax.swing.UIManager.getDefaults().getColor("Button.background"));
        failedLabelSSH.setText(bundle.getString("PasswordDialog.failedLabelSSH.text")); // NOI18N

        saveSettingsCeckbox.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        saveSettingsCeckbox.setText(bundle.getString("PasswordDialog.saveSettingsCeckbox.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(saveSettingsCeckbox)
                    .addComponent(UseSSHCheckBox)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                    .addComponent(failedLabelSSH, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 406, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(failedLabelSSH)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(UseSSHCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveSettingsCeckbox)
                .addContainerGap(79, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab(bundle.getString("PasswordDialog.jPanel2.TabConstraints.tabTitle"), jPanel2); // NOI18N

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel14.setBackground(new java.awt.Color(0, 102, 204));
        jPanel14.setPreferredSize(new java.awt.Dimension(100, 40));

        jLabel37.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel37.setForeground(new java.awt.Color(255, 255, 255));
        jLabel37.setText(bundle.getString("PasswordDialog.jLabel37.text")); // NOI18N

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

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel37, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 163, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7)
                    .addComponent(jLabel37))
                .addContainerGap())
        );

        getContentPane().add(jPanel14, java.awt.BorderLayout.NORTH);

        jPanel5.setPreferredSize(new java.awt.Dimension(413, 40));

        pwdCancelButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        pwdCancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        pwdCancelButton.setText(bundle.getString("PasswordDialog.pwdCancelButton.text")); // NOI18N
        pwdCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdCancelButtonActionPerformed(evt);
            }
        });

        pwdOKButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        pwdOKButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        pwdOKButton.setText(bundle.getString("PasswordDialog.pwdOKButton.text")); // NOI18N
        pwdOKButton.setSelected(true);
        pwdOKButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pwdOKButtonActionPerformed(evt);
            }
        });

        SSHStatusLabel.setMinimumSize(new java.awt.Dimension(20, 20));
        SSHStatusLabel.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SSHStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                .addComponent(pwdCancelButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pwdOKButton, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(SSHStatusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pwdOKButton)
                        .addComponent(pwdCancelButton)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel5, java.awt.BorderLayout.SOUTH);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pwdCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdCancelButtonActionPerformed
        dispose();
}//GEN-LAST:event_pwdCancelButtonActionPerformed

    private void pwdOKButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pwdOKButtonActionPerformed
        EDT.performBlocking(new Runnable() {
            @Override
            public void run() {
                tryConnect();
            }
        });
};//GEN-LAST:event_pwdOKButtonActionPerformed

    private void refreshDatabaseAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshDatabaseAction
        refreshDatabases();
}//GEN-LAST:event_refreshDatabaseAction

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.PASSWORD_DIALOG.getPage());
    }//GEN-LAST:event_jButton7help

    /**
     * Refreshes the list of available projects bases on the user (which the
     * user has permissions to see)
     *
     */
    private void refreshDatabases() {
        if (MoniSoft.getInstance().ISTRIAL) { // Bei der Testversion kann nichts ausgewählt werden
            return;
        }

        if (UseSSHCheckBox.isSelected()) { // wenn erforderlich Tunnelverbindung herstellen
            digTunnel();
        }

        DefaultComboBoxModel cm = (DefaultComboBoxModel) dbSelector.getModel();

        ArrayList<String> dbList = new DBIdentifier(serverField.getSelectedItem().toString() + ":" + portField.getText(), userField.getText(), new String(pwdField.getPassword())).getAccessibleTables();
        if (dbList == null) {
            failedLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CONNECTION_FAILED"));
            failedLabel.setForeground(Color.RED);
            return;
        }

        if (dbList.isEmpty()) {
            dbSelector.setEnabled(false);
            failedLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("NO_PROJECTS_FOR") + "  " + userField.getText());
            failedLabel.setForeground(Color.RED);
        } else {
            dbSelector.setEnabled(true);
            failedLabel.setForeground(INVISIBLE);
            cm.removeAllElements();
            for (String db : dbList) {
                cm.addElement(db);
            }
            for (int i = 0; i < dbSelector.getItemCount(); i++) {
                if (dbSelector.getItemAt(i).equals(MoniSoft.getInstance().getApplicationProperties().getProperty("DefaultDB"))) {
                    dbSelector.setSelectedIndex(i);
                }
            }
        }
    }

    /**
     * Try to build up a ssh-tunnel to the server
     *
     * @return <code>true</code> if the connection was successful
     */
    private boolean digTunnel() {
        boolean success = false;
        SSHStatusLabel.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/minus-shield.png")));
        SSHStatusLabel.setToolTipText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-NO_CONNECT"));
        try {
            if (!ssht.isConnected()) {
                ssht.buildTunnel(SSHServerTextField.getText().trim(), SSHServerPortTextField.getText().trim(), SSHTunnelIPTextField.getText().trim(), SSHTunnelPortTextField.getText().trim(), SSHLocalPortTextField.getText().trim(), SSHUserTextField.getText().trim());
                if (ssht.isConnected()) {
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH_SUCCESS") + "\n", true);
                    success = true;
                    SSHStatusLabel.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/tick-shield.png")));
                    SSHStatusLabel.setToolTipText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-CONNECT"));

                    // if wished save the ssh settings
                    if (saveSettingsCeckbox.isSelected()) {
                        saveSSHSettings();
                    }
                }
            } else {
                SSHStatusLabel.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/tick-shield.png")));
                SSHStatusLabel.setToolTipText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-CONNECT"));
                success = true; // es gab einen gültige Tunnel Verbindung
            }
        } catch (Exception ex) {
            SSHStatusLabel.setIcon(new ImageIcon(getClass().getResource("/de/jmonitoring/icons/minus-shield.png")));
            SSHStatusLabel.setToolTipText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-NO_CONNECT"));
            Messages.showException(ex);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH_FAIL") + " " + ex.getMessage() + " \n", true);
        }

        return success;
    }

    /**
     * Try to connct to the database
     *
     * @throws HeadlessException
     */
    private void tryConnect() throws HeadlessException {
        String userName = userField.getText();
        String server = serverField.getSelectedItem().toString() + ":" + portField.getText();
        String dataBase = dbSelector.getSelectedItem().toString();
        MoniSoft.getInstance().setSkipFavorites(skipFavoriteCharts.isSelected());
        setVisible(false);

        //  dig tunnel if wished
        if (UseSSHCheckBox.isSelected()) {
            if (digTunnel()) { // tunnel creation was successful
                failedLabelSSH.setForeground(INVISIBLE);
                // try to connect to database
                if (connector.connectToDB(server, dataBase, userName, new String(pwdField.getPassword()), GUIOnly, true)) {
                    failedLabel.setForeground(INVISIBLE);
                    connectionSuccess = true;
                } else {
                    jTabbedPane1.setSelectedIndex(0);
                    failedLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH-OK_CONNECTION_FAIL"));
                    failedLabel.setForeground(Color.RED);
                    connectionSuccess = false;
                    setVisible(true);
                }
            } else { // tunnel creation was not successful
                jTabbedPane1.setSelectedIndex(1);
                failedLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SSH_FAIL"));
                failedLabelSSH.setForeground(Color.RED);
                setVisible(true);
            }
        } else { // create connection without tunnel
            SSHStatusLabel.setIcon(null);
            failedLabelSSH.setForeground(INVISIBLE);
            // try to connect to database
            if (connector.connectToDB(server, dataBase, userName, new String(pwdField.getPassword()), GUIOnly, true)) {
                failedLabel.setForeground(INVISIBLE);
                connectionSuccess = true;
            } else {
                jTabbedPane1.setSelectedIndex(0);
                failedLabel.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CONNECTION_FAILED"));
                failedLabel.setForeground(Color.RED);
                connectionSuccess = false;
                setVisible(true);
            }
        }

        // connection to the database was successful
        if (connectionSuccess == true) {
            MoniSoft.getInstance().getApplicationProperties().setProperty("LAST_SERVER", serverField.getSelectedItem().toString());
            MoniSoft.getInstance().getApplicationProperties().setProperty("LAST_PORT", portField.getText());
            MoniSoft.getInstance().getApplicationProperties().setProperty("LAST_DB", dataBase);
            MoniSoft.getInstance().getApplicationProperties().setProperty("LAST_USER", userName);
            if (saveAsDefaultCheckbox.isSelected()) {
                MoniSoft.getInstance().getApplicationProperties().setProperty("DefaultUser", userName);
                MoniSoft.getInstance().getApplicationProperties().setProperty("DefaultDB", dataBase);
                MoniSoft.getInstance().getApplicationProperties().setProperty("DefaultServer", serverField.getSelectedItem().toString());
                MoniSoft.getInstance().getApplicationProperties().setProperty("DefaultServerPort", portField.getText());
            }
            try {
                AppPrefsDialog.saveProperties(false, false);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SETTINGS_NO_SAVE"), "Warning", JOptionPane.WARNING_MESSAGE);
                Messages.showException(ex);
            }
        }
//        revalidate();
    }

    /**
     * Saves the current SSH settings in the application properties
     */
    private void saveSSHSettings() {
        if (UseSSHCheckBox.isSelected()) {
            MoniSoft.getInstance().getApplicationProperties().setProperty("UseSSHTunnel", "1");
        } else {
            MoniSoft.getInstance().getApplicationProperties().setProperty("UseSSHTunnel", "0");
        }
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHServerIP", SSHServerTextField.getText().trim());
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHServerPort", SSHServerPortTextField.getText().trim());
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHTunnelIP", SSHTunnelIPTextField.getText().trim());
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHTunnelPort", SSHTunnelPortTextField.getText().trim());
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHLocalPort", SSHLocalPortTextField.getText().trim());
        MoniSoft.getInstance().getApplicationProperties().setProperty("SSHUser", SSHUserTextField.getText().trim());
        try {
            AppPrefsDialog.saveProperties(false, false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SETTINGS_NO_SAVE"), "Warning", JOptionPane.WARNING_MESSAGE);
            Messages.showException(e);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    static javax.swing.JTextField SSHLocalPortTextField;
    static javax.swing.JTextField SSHServerPortTextField;
    static javax.swing.JTextField SSHServerTextField;
    private javax.swing.JLabel SSHStatusLabel;
    static javax.swing.JTextField SSHTunnelIPTextField;
    static javax.swing.JTextField SSHTunnelPortTextField;
    static javax.swing.JTextField SSHUserTextField;
    private javax.swing.JCheckBox UseSSHCheckBox;
    private javax.swing.JComboBox dbSelector;
    private javax.swing.JLabel failedLabel;
    private javax.swing.JLabel failedLabelSSH;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel59;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField portField;
    private javax.swing.JButton pwdCancelButton;
    protected javax.swing.JPasswordField pwdField;
    private javax.swing.JButton pwdOKButton;
    private javax.swing.JCheckBox saveAsDefaultCheckbox;
    private javax.swing.JCheckBox saveSettingsCeckbox;
    private javax.swing.JComboBox serverField;
    private javax.swing.JCheckBox skipFavoriteCharts;
    static javax.swing.JTextField userField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
