/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WeatherAssignmentEditor.java
 *
 * Created on 01.10.2010, 17:38:10
 */
package de.jmonitoring.WeatherCalculation;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JInternalFrame;

// ALTER TABLE `revi_testdb`.`T_WeatherDefinition` ADD COLUMN `T_Building_id_Building` INTEGER  DEFAULT,
// DROP PRIMARY KEY,
// ADD PRIMARY KEY (`category`, `T_Building_id_Building`);
/**
 *
 * @author togro
 */
public class WeatherAssignmentEditor extends javax.swing.JPanel {

    private ArrayList<String> model1_Units = new ArrayList<String>(Arrays.asList("°C", "°F"));
    private ArrayList<String> model2_Units = new ArrayList<String>(Arrays.asList("%", "%rh"));
    private ArrayList<String> model3_Units = new ArrayList<String>(Arrays.asList("W/m²"));
    private ArrayList<String> model4_Units = new ArrayList<String>(Arrays.asList("m/s", "kmh"));
    private ArrayList<String> model5_Units = new ArrayList<String>(Arrays.asList("°", " ", ""));
    private ArrayList<String> model6_Units = new ArrayList<String>(Arrays.asList("ppm"));
    private ArrayList<String> model7_Units = new ArrayList<String>(Arrays.asList("l", "mm/h", "l/h", "l/m²", "mm/m²", "mm"));
    private ArrayList<String> model8_Units = new ArrayList<String>(Arrays.asList("bool", "n/a", "", " "));
    private final MainApplication gui;
    private boolean active = false;
    private HashMap<Integer, SensorAssignment> assignmentMap = new HashMap<Integer, SensorAssignment>();

    /**
     * Creates new form WeatherAssignmentEditor
     */
    public WeatherAssignmentEditor(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setBuildingComboBox();
        loadAssignments();
        initSelectors();

        buildingCombobox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    setSensorsFor(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
                }
            }
        });

        setSensorsFor(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        active = true;
    }

    private void loadAssignments() {
        for (BuildingProperties building : BuildingInformation.getBuildingList()) {
            SensorAssignment assignment = new SensorAssignment(building.getBuildingID());
            assignment.setTemperature(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_OUTSIDE_TEMPERATURE, building.getBuildingID()));
            assignment.setHumidity(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_OUTSIDE_HUMIDITY, building.getBuildingID()));
            assignment.setGlobalRadiation(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_GLOBAL_RADIATION, building.getBuildingID()));
            assignment.setWindSpeed(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_WIND_SPEED, building.getBuildingID()));
            assignment.setWindDirection(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_WIND_DIRECTION, building.getBuildingID()));
            assignment.setCarbonDioxide(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_CARBON_DIOXIDE, building.getBuildingID()));
            assignment.setRainAmount(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_RAIN_AMOUNT, building.getBuildingID()));
            assignment.setRainStatus(WeatherManager.getAssociatedSensor(MoniSoftConstants.WEATHER_RAIN_STATUS, building.getBuildingID()));
            assignmentMap.put(building.getBuildingID(), assignment);
        }
    }

    private void saveAssignments() {
        for (Integer buildingID : assignmentMap.keySet()) {
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_OUTSIDE_TEMPERATURE, assignmentMap.get(buildingID).getTemperature(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_OUTSIDE_HUMIDITY, assignmentMap.get(buildingID).getHumidity(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_GLOBAL_RADIATION, assignmentMap.get(buildingID).getGlobalRadiation(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_WIND_SPEED, assignmentMap.get(buildingID).getWindSpeed(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_WIND_DIRECTION, assignmentMap.get(buildingID).getWindDirection(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_CARBON_DIOXIDE, assignmentMap.get(buildingID).getCarbonDioxide(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_RAIN_AMOUNT, assignmentMap.get(buildingID).getRainAmount(), buildingID);
            WeatherManager.writeAssociatedSensor(MoniSoftConstants.WEATHER_RAIN_STATUS, assignmentMap.get(buildingID).getRainStatus(), buildingID);
        }
    }

    private void setSensorsFor(Integer buildingID) {
        active = false;
        SensorAssignment assignment = assignmentMap.get(buildingID);
        setSelectorsToNone();
        if (assignment.getTemperature() != null) {
            jComboBox1.getModel().setSelectedItem(assignment.getTemperature());
        }
        if (assignment.getHumidity() != null) {
            jComboBox2.getModel().setSelectedItem(assignment.getHumidity());
        }
        if (assignment.getGlobalRadiation() != null) {
            jComboBox3.getModel().setSelectedItem(assignment.getGlobalRadiation());
        }
        if (assignment.getWindSpeed() != null) {
            jComboBox4.getModel().setSelectedItem(assignment.getWindSpeed());
        }
        if (assignment.getWindDirection() != null) {
            jComboBox5.getModel().setSelectedItem(assignment.getWindDirection());
        }
        if (assignment.getCarbonDioxide() != null) {
            jComboBox6.getModel().setSelectedItem(assignment.getCarbonDioxide());
        }
        if (assignment.getRainAmount() != null) {
            jComboBox7.getModel().setSelectedItem(assignment.getRainAmount());
        }
        if (assignment.getRainStatus() != null) {
            jComboBox8.getModel().setSelectedItem(assignment.getRainStatus());
        }
        active = true;
    }

    private void setSelectorsToNone() {
        jComboBox1.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox2.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox3.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox4.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox5.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox6.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox7.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
        jComboBox8.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
    }

    private void initSelectors() {
        ArrayList<SensorProperties> sensors = SensorInformation.getSensorList();

        DefaultComboBoxModel model1 = new DefaultComboBoxModel();
        DefaultComboBoxModel model2 = new DefaultComboBoxModel();
        DefaultComboBoxModel model3 = new DefaultComboBoxModel();
        DefaultComboBoxModel model4 = new DefaultComboBoxModel();
        DefaultComboBoxModel model5 = new DefaultComboBoxModel();
        DefaultComboBoxModel model6 = new DefaultComboBoxModel();
        DefaultComboBoxModel model7 = new DefaultComboBoxModel();
        DefaultComboBoxModel model8 = new DefaultComboBoxModel();

        if (unitRestrictionToggleButton.isSelected()) {
            String unit;
            for (SensorProperties sensor : sensors) {
                unit = sensor.getSensorUnit().getUnit();
                if (model1_Units.contains(unit)) {
                    model1.addElement(sensor);
                }
                if (model2_Units.contains(unit)) {
                    model2.addElement(sensor);
                }
                if (model3_Units.contains(unit)) {
                    model3.addElement(sensor);
                }
                if (model4_Units.contains(unit)) {
                    model4.addElement(sensor);
                }
                if (model5_Units.contains(unit)) {
                    model5.addElement(sensor);
                }
                if (model6_Units.contains(unit)) {
                    model6.addElement(sensor);
                }
                if (model7_Units.contains(unit)) {
                    model7.addElement(sensor);
                }
                if (model8_Units.contains(unit)) {
                    model8.addElement(sensor);
                }
            }
        } else {
            // es sollen alle verfügbaren Messpunkte angezeigt werden
            model1 = new DefaultComboBoxModel(sensors.toArray());
            model1.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model2 = new DefaultComboBoxModel(sensors.toArray());
            model2.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model3 = new DefaultComboBoxModel(sensors.toArray());
            model3.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model4 = new DefaultComboBoxModel(sensors.toArray());
            model4.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model5 = new DefaultComboBoxModel(sensors.toArray());
            model5.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model6 = new DefaultComboBoxModel(sensors.toArray());
            model6.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model7 = new DefaultComboBoxModel(sensors.toArray());
            model7.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
            model8 = new DefaultComboBoxModel(sensors.toArray());
            model8.insertElementAt(MoniSoftConstants.NO_SENSOR_SELECTED, 0);
        }

        if (jComboBox1.isEnabled()) {
            jComboBox1.setModel(model1);
        }
        if (jComboBox2.isEnabled()) {
            jComboBox2.setModel(model2);
        }
        if (jComboBox3.isEnabled()) {
            jComboBox3.setModel(model3);
        }
        if (jComboBox4.isEnabled()) {
            jComboBox4.setModel(model4);
        }
        if (jComboBox5.isEnabled()) {
            jComboBox5.setModel(model5);
        }
        if (jComboBox6.isEnabled()) {
            jComboBox6.setModel(model6);
        }
        if (jComboBox7.isEnabled()) {
            jComboBox7.setModel(model7);
        }
        if (jComboBox8.isEnabled()) {
            jComboBox8.setModel(model8);
        }

        jComboBox1.setModel(model1);
        jComboBox2.setModel(model2);
        jComboBox3.setModel(model3);
        jComboBox4.setModel(model4);
        jComboBox5.setModel(model5);
        jComboBox6.setModel(model6);
        jComboBox7.setModel(model7);
        jComboBox8.setModel(model8);

        jComboBox1.revalidate();
    }

    private void setBuildingComboBox() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (BuildingProperties building : BuildingInformation.getBuildingList()) {
            model.addElement(building);
        }
        buildingCombobox.setModel(model);
    }

    private void disposeMe() {
        Component c = this;
        while (c.getClass() != JInternalFrame.class) {
            c = c.getParent();
        }
        this.gui.disposeIFrame((JInternalFrame) c);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        buildingCombobox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jComboBox6 = new javax.swing.JComboBox();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel6 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox();
        jLabel8 = new javax.swing.JLabel();
        jComboBox5 = new javax.swing.JComboBox();
        unitRestrictionToggleButton = new javax.swing.JToggleButton();
        jComboBox7 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        forAllBuildings = new javax.swing.JCheckBox();
        headPanel = new javax.swing.JPanel();
        jLabel47 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/WeatherCalculation/Bundle"); // NOI18N
        jLabel9.setText(bundle.getString("WeatherAssignmentEditor.jLabel9.text")); // NOI18N

        buildingCombobox.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        buildingCombobox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, forAllBuildings, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), buildingCombobox, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("WeatherAssignmentEditor.jPanel3.border.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9), new java.awt.Color(0, 0, 0))); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("WeatherAssignmentEditor.jLabel5.text")); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel3.setText(bundle.getString("WeatherAssignmentEditor.jLabel3.text")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel4.setText(bundle.getString("WeatherAssignmentEditor.jLabel4.text")); // NOI18N

        jComboBox3.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox3.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("WeatherAssignmentEditor.jLabel1.text")); // NOI18N

        jComboBox4.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox4.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });

        jComboBox6.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox6.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox6ActionPerformed(evt);
            }
        });

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel6.setText(bundle.getString("WeatherAssignmentEditor.jLabel6.text")); // NOI18N

        jComboBox8.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox8.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox8ActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText(bundle.getString("WeatherAssignmentEditor.jLabel8.text")); // NOI18N

        jComboBox5.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox5.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });

        unitRestrictionToggleButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        unitRestrictionToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/funnel.png"))); // NOI18N
        unitRestrictionToggleButton.setText(bundle.getString("WeatherAssignmentEditor.unitRestrictionToggleButton.text")); // NOI18N
        unitRestrictionToggleButton.setToolTipText(bundle.getString("WeatherAssignmentEditor.unitRestrictionToggleButton.toolTipText")); // NOI18N
        unitRestrictionToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unitRestrictionToggleButtonActionPerformed(evt);
            }
        });

        jComboBox7.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox7.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox7ActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText(bundle.getString("WeatherAssignmentEditor.jLabel2.text")); // NOI18N

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel7.setText(bundle.getString("WeatherAssignmentEditor.jLabel7.text")); // NOI18N

        jComboBox2.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox2.setPreferredSize(new java.awt.Dimension(71, 20));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton1.setText(bundle.getString("WeatherAssignmentEditor.jButton1.text")); // NOI18N
        jButton1.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton2.setText(bundle.getString("WeatherAssignmentEditor.jButton2.text")); // NOI18N
        jButton2.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton3.setText(bundle.getString("WeatherAssignmentEditor.jButton3.text")); // NOI18N
        jButton3.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton4.setText(bundle.getString("WeatherAssignmentEditor.jButton4.text")); // NOI18N
        jButton4.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton5.setText(bundle.getString("WeatherAssignmentEditor.jButton5.text")); // NOI18N
        jButton5.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton6.setText(bundle.getString("WeatherAssignmentEditor.jButton6.text")); // NOI18N
        jButton6.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton7.setText(bundle.getString("WeatherAssignmentEditor.jButton7.text")); // NOI18N
        jButton7.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross.png"))); // NOI18N
        jButton8.setText(bundle.getString("WeatherAssignmentEditor.jButton8.text")); // NOI18N
        jButton8.setPreferredSize(new java.awt.Dimension(20, 20));
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jComboBox7, javax.swing.GroupLayout.Alignment.LEADING, 0, 458, Short.MAX_VALUE)
                            .addComponent(jComboBox6, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox5, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox4, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox8, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(unitRestrictionToggleButton))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                            .addGroup(jPanel3Layout.createSequentialGroup()
                                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                                .addComponent(jLabel1)
                                                                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                            .addComponent(jLabel2)
                                                                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                    .addComponent(jLabel3)
                                                                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                            .addComponent(jLabel4)
                                                            .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                    .addComponent(jLabel5)
                                                    .addComponent(jComboBox5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                            .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(jLabel6)
                                            .addComponent(jComboBox6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel7)
                                    .addComponent(jComboBox7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(jComboBox8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(unitRestrictionToggleButton)
                .addContainerGap())
        );

        forAllBuildings.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        forAllBuildings.setText(bundle.getString("WeatherAssignmentEditor.forAllBuildings.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buildingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(forAllBuildings)
                        .addGap(0, 7, Short.MAX_VALUE)))
                .addGap(5, 5, 5))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(buildingCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(forAllBuildings))
                .addGap(41, 41, 41)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        headPanel.setBackground(new java.awt.Color(0, 102, 204));

        jLabel47.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel47.setForeground(new java.awt.Color(255, 255, 255));
        jLabel47.setText(bundle.getString("WeatherAssignmentEditor.jLabel47.text")); // NOI18N

        javax.swing.GroupLayout headPanelLayout = new javax.swing.GroupLayout(headPanel);
        headPanel.setLayout(headPanelLayout);
        headPanelLayout.setHorizontalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(headPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel47, javax.swing.GroupLayout.PREFERRED_SIZE, 232, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(405, Short.MAX_VALUE))
        );
        headPanelLayout.setVerticalGroup(
            headPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel47, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
        );

        add(headPanel, java.awt.BorderLayout.PAGE_START);

        jPanel2.setMinimumSize(new java.awt.Dimension(100, 35));
        jPanel2.setPreferredSize(new java.awt.Dimension(629, 35));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        closeButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        closeButton.setText(bundle.getString("WeatherAssignmentEditor.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        jPanel2.add(closeButton);

        saveButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        saveButton.setText(bundle.getString("WeatherAssignmentEditor.saveButton.text")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        jPanel2.add(saveButton);

        add(jPanel2, java.awt.BorderLayout.PAGE_END);

        bindingGroup.bind();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        disposeMe();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void unitRestrictionToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unitRestrictionToggleButtonActionPerformed
//        initSelectors();
    }//GEN-LAST:event_unitRestrictionToggleButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        saveAssignments();
    }//GEN-LAST:event_saveButtonActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox1.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setTemperature(null);
        } else {
            assignment.setTemperature((SensorProperties) jComboBox1.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox2.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setHumidity(null);
        } else {
            assignment.setHumidity((SensorProperties) jComboBox2.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox3.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setGlobalRadiation(null);
        } else {
            assignment.setGlobalRadiation((SensorProperties) jComboBox3.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox4.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setWindSpeed(null);
        } else {
            assignment.setWindSpeed((SensorProperties) jComboBox4.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox5.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setWindDirection(null);
        } else {
            assignment.setWindDirection((SensorProperties) jComboBox5.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void jComboBox6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox6ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox6.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setCarbonDioxide(null);
        } else {
            assignment.setCarbonDioxide((SensorProperties) jComboBox6.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox6ActionPerformed

    private void jComboBox7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox7ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox7.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setRainAmount(null);
        } else {
            assignment.setRainAmount((SensorProperties) jComboBox7.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox7ActionPerformed

    private void jComboBox8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox8ActionPerformed
        if (!active) {
            return;
        }
        SensorAssignment assignment = assignmentMap.get(((BuildingProperties) buildingCombobox.getSelectedItem()).getBuildingID());
        if (jComboBox8.getSelectedItem().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
            assignment.setRainStatus(null);
        } else {
            assignment.setRainStatus((SensorProperties) jComboBox8.getSelectedItem());
        }
    }//GEN-LAST:event_jComboBox8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jComboBox1.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jComboBox2.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        jComboBox3.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        jComboBox4.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        jComboBox5.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jComboBox6.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        jComboBox7.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        jComboBox8.setSelectedItem(MoniSoftConstants.NO_SENSOR_SELECTED);// TODO add your handling code here:
    }//GEN-LAST:event_jButton8ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox buildingCombobox;
    private javax.swing.JButton closeButton;
    private javax.swing.JCheckBox forAllBuildings;
    private javax.swing.JPanel headPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBox6;
    private javax.swing.JComboBox jComboBox7;
    private javax.swing.JComboBox jComboBox8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton saveButton;
    private javax.swing.JToggleButton unitRestrictionToggleButton;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
