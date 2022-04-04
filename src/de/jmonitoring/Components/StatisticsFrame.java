/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Components;

import com.toedter.calendar.JDateChooser;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.DnDListener.SensorSelectorDropListener;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.UnitCalulation.UnitTransfer;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.dnd.DropTarget;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.jfree.data.time.SimpleTimePeriod;

/**
 *
 * @author togro
 */
public class StatisticsFrame extends javax.swing.JInternalFrame {

    private final static int TODAY = 0;
    private final static int YESTERDAY = 1;
    private final static int LASTWEEK = 2;
    private final static int LASTMONTH = 3;
    private final MainApplication gui;

    /**
     * Creates new form StatisticsFrame
     */
    public StatisticsFrame(MainApplication gui) {
        this.gui = gui;
        initComponents();
        setSensorSelector();
    }

    private void setDateChooser(JDateChooser start, JDateChooser end, int span) {
        Calendar cal;
        switch (span) {
            case TODAY:
                start.setDate(new Date());
                end.setDate(new Date());
                break;
            case YESTERDAY:
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                start.setDate(cal.getTime());
                end.setDate(cal.getTime());
                break;
            case LASTWEEK:
                cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -7);
                start.setDate(cal.getTime());
                cal.add(Calendar.DATE, 6);
                end.setDate(cal.getTime());
                break;
            case LASTMONTH:
                cal = Calendar.getInstance();
                cal.add(Calendar.MONTH, -1);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                start.setDate(cal.getTime());
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                end.setDate(cal.getTime());
        }
    }

    private void setSensorSelector() {
        ArrayList<SensorProperties> sensors = SensorInformation.getSensorList();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (SensorProperties sensor : sensors) {
            model.addElement(sensor);
        }
        jComboBox1.setModel(model);
    }

    class WorkerThread extends StoppableThread {

        @Override
        public void run() {
            DateInterval dateInterval = new DateInterval(startDateChooser.getDate(), endDateChooser.getDate());
            Connection myConn = null;
            ResultSet rs = null;
            PreparedStatement stmt = null;
            SensorProperties props = (SensorProperties) jComboBox1.getSelectedItem();
            Integer sensorID = props.getSensorID();
            DescriptiveStatistics statistics = new DescriptiveStatistics();
            Double consumption = null;;
            Double meanPower = null;
            Double min = -99d;
            Double max = -99d;
            try {
                myConn = DBConnector.openConnection();

                if (SensorInformation.getSensorProperties(sensorID).isEvent()) { // Event
                    stmt = myConn.prepareStatement("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors=? and TimeStart >= ? and TimeStart <= ? order by TimeStart");
                    stmt.setInt(1, sensorID);
                    stmt.setString(2, dateInterval.getStartDateString(new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat)));
                    stmt.setString(3, dateInterval.getEndDateString(new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat)) + " 23:59:59");
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        statistics.addValue(rs.getDouble(3));
                    }
                } else { // Normaler Messwert
                    long startSecond = dateInterval.getStartDate().getTime() / 1000L;
                    long endSecond = dateInterval.getEndDate().getTime() / 1000L;
                    stmt = myConn.prepareStatement("select distinct hist.TimeStamp,hist.Value,l.EventType FROM T_History as hist inner join T_Log as l on l.id_Log = hist.T_Log_id_Log where hist.T_Sensors_id_Sensors=? and hist.TimeStamp >= ? and hist.TimeStamp <= ?");
                    stmt.setInt(1, sensorID);
                    stmt.setLong(2, startSecond);
                    stmt.setLong(3, endSecond);
                    rs = stmt.executeQuery();
                    List<Long> timesInSeconds = new ArrayList<Long>();
                    while (rs.next()) {
                        timesInSeconds.add(rs.getLong(1)); // in seconds from database!!
                        statistics.addValue(rs.getDouble(2));
                    }

                    findLongestGapInHours(timesInSeconds);

                    DecimalFormat decimalFormat1 = new DecimalFormat("0.0");
                    DecimalFormat integerFormat = new DecimalFormat("#");
                    String powerUnit;
                    String unit = props.getSensorUnit().getUnit();
                    if (props.isCounter()) {
                        DatasetWorker dw = new DatasetWorkerFactory(StatisticsFrame.this.gui).createFor(sensorID);
//                        DatasetWorker dw = new DatasetWorker(sensorID, StatisticsFrame.this.gui);
                        CounterMode consumptionMode = CounterMode.COUNTERCONSUMPTION;
                        consumptionMode.setAutoCounterChange(MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"));
                        CounterMode powerMode = CounterMode.COUNTERPOWER;
                        powerMode.setAutoCounterChange(MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"));
                        MeasurementTreeSet dataSet1 = dw.getInterpolatedData(dateInterval, new SimpleTimePeriod(dateInterval.getStartDate().getTime(), dateInterval.getEndDate().getTime()), consumptionMode);
                        MeasurementTreeSet dataSet2 = dw.getInterpolatedData(dateInterval, new SimpleTimePeriod(dateInterval.getStartDate().getTime(), dateInterval.getEndDate().getTime()), powerMode);
                        consumption = dataSet1.getValueForTime(dateInterval.getStartDate().getTime());
                        meanPower = dataSet2.getValueForTime(dateInterval.getStartDate().getTime());
                        min = dataSet2.getPeakMinMax().getMinimum();
                        max = dataSet2.getPeakMinMax().getMaximum();

                        UnitTransfer ut = new UnitTransfer();
                        powerUnit = ut.getPowerUnitFromConsumption(unit);
                        Long durationInHours = dateInterval.getSpanInHours();
                        longestGap.setText(decimalFormat1.format(findLongestGapInHours(timesInSeconds)) + " h");
                        artihmeticMean.setText(getStringRepresentation(meanPower) + " " + powerUnit);
                        median.setText("-");
                        minimum.setText(getStringRepresentation(min) + " " + powerUnit);
                        maximum.setText(getStringRepresentation(max) + " " + powerUnit);
                        numberOfValues.setText(integerFormat.format(statistics.getN()) + " ( ~ " + decimalFormat1.format(statistics.getN() / durationInHours) + " " + "Werte pro Stunde)");
                        totalConsumption.setText(getStringRepresentation(consumption) + " " + unit);
                        sum.setText("-");
                    } else { // no counter
                        Long durationInHours = dateInterval.getSpanInHours();
                        longestGap.setText(decimalFormat1.format(findLongestGapInHours(timesInSeconds)) + " h");
                        artihmeticMean.setText(getStringRepresentation(statistics.getMean()) + " " + unit);
                        median.setText(getStringRepresentation(statistics.getPercentile(50)) + " " + unit);
                        minimum.setText(getStringRepresentation(statistics.getMin()) + " " + unit);
                        maximum.setText(getStringRepresentation(statistics.getMax()) + " " + unit);
                        numberOfValues.setText(integerFormat.format(statistics.getN()) + " ( ~ " + decimalFormat1.format(statistics.getN() / durationInHours) + " " + "Werte pro Stunde)");
                        totalConsumption.setText("-");
                        sum.setText(getStringRepresentation(statistics.getSum()));
                    }
                }
            } catch (Exception e) {
                Messages.showMessage("Fehler bei Datenbankabfrage: " + e.getMessage() + "\n", true);
                Messages.showException(e);
                Messages.showException(e);
            } finally {
                DBConnector.closeConnection(myConn, stmt, rs);
                ((StoppableThread) Thread.currentThread()).running = false;
            }
        }
    }

    private String getStringRepresentation(Double d) {
        if (d == null) {
            return "null";
        }

        if (d.isNaN()) {
            return "NaN";
        }

        DecimalFormat decimalFormat3 = new DecimalFormat("0.000");
        return decimalFormat3.format(d);
    }

    private Double findLongestGapInHours(List<Long> times) {
        Double gap = 0d;
        Long keep = null;
        for (Long time : times) {
            if (keep == null) {
                keep = time;
            }
            if (time - keep > gap) {
                gap = (double) time - keep;
            }
            keep = time;
        }

        return gap / 60 / 60; // to hours
    }

    private void doCalculation() {
        StoppableThread workerThread = new WorkerThread();
        workerThread.running = true;
        workerThread.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        startDateChooser = new com.toedter.calendar.JDateChooser();
        endDateChooser = new com.toedter.calendar.JDateChooser();
        jButton11 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        numberOfValues = new javax.swing.JLabel();
        longestGap = new javax.swing.JLabel();
        artihmeticMean = new javax.swing.JLabel();
        median = new javax.swing.JLabel();
        minimum = new javax.swing.JLabel();
        maximum = new javax.swing.JLabel();
        sum = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        totalConsumption = new javax.swing.JLabel();
        goButton = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Messpunkstatistik");

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jLabel1.setText("Messpunkt:");

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel4.setText(bundle.getString("DatePanel.jLabel4.text")); // NOI18N

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setText(bundle.getString("DatePanel.jLabel5.text")); // NOI18N

        startDateChooser.setToolTipText(bundle.getString("DatePanel.startDateChooser.toolTipText")); // NOI18N
        startDateChooser.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        startDateChooser.setDoubleBuffered(false);
        startDateChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        startDateChooser.setMaxSelectableDate(null);
        startDateChooser.setMinSelectableDate(null);

        endDateChooser.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        endDateChooser.setDoubleBuffered(false);
        endDateChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jButton11.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jButton11.setText(bundle.getString("DatePanel.jButton11.text")); // NOI18N
        jButton11.setToolTipText(bundle.getString("DatePanel.jButton11.toolTipText")); // NOI18N
        jButton11.setFocusable(false);
        jButton11.setMargin(new java.awt.Insets(2, 1, 2, 0));
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jButton10.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jButton10.setText(bundle.getString("DatePanel.jButton10.text")); // NOI18N
        jButton10.setToolTipText(bundle.getString("DatePanel.jButton10.toolTipText")); // NOI18N
        jButton10.setFocusable(false);
        jButton10.setMargin(new java.awt.Insets(2, 1, 2, 0));
        jButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton10ActionPerformed(evt);
            }
        });

        jButton1.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jButton1.setText(bundle.getString("DatePanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(bundle.getString("DatePanel.jButton1.toolTipText")); // NOI18N
        jButton1.setFocusable(false);
        jButton1.setMargin(new java.awt.Insets(2, 1, 2, 0));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton12.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        jButton12.setText(bundle.getString("DatePanel.jButton12.text")); // NOI18N
        jButton12.setToolTipText(bundle.getString("DatePanel.jButton12.toolTipText")); // NOI18N
        jButton12.setFocusable(false);
        jButton12.setMargin(new java.awt.Insets(2, 1, 2, 0));
        jButton12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton12ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        jButton2.setText("Schliessen");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel2.setText("Anzahl Werte:");

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setText("Längste Lücke:");

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel6.setText("arithm. Mittel:");

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel7.setText("Median:");

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel8.setText("Minimum:");

        jLabel9.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel9.setText("Maximum:");

        jLabel10.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel10.setText("Summe:");

        numberOfValues.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        numberOfValues.setText(" ");

        longestGap.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        longestGap.setText(" ");

        artihmeticMean.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        artihmeticMean.setText(" ");

        median.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        median.setText(" ");

        minimum.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        minimum.setText(" ");

        maximum.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        maximum.setText(" ");

        sum.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        sum.setText(" ");

        jLabel11.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel11.setText("Verbrauch:");

        totalConsumption.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        totalConsumption.setText("  ");
        totalConsumption.setToolTipText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(median, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(artihmeticMean, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(longestGap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(numberOfValues, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maximum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minimum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(totalConsumption, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                .addContainerGap(238, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(numberOfValues))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(longestGap))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(artihmeticMean))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(median))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minimum, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(maximum))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(sum))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(totalConsumption))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        goButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cog.png"))); // NOI18N
        goButton.setText("Go!");
        goButton.setFocusable(false);
        goButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goButtonActionPerformed(evt);
            }
        });

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);
        jButton7.setIconTextGap(0);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        new DropTarget(jComboBox1,new SensorSelectorDropListener());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jButton7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(endDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(goButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(goButton, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel4)
                                .addComponent(startDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(jLabel5)
                                .addComponent(endDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        setDateChooser(startDateChooser, endDateChooser, TODAY);
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        setDateChooser(startDateChooser, endDateChooser, YESTERDAY);
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        setDateChooser(startDateChooser, endDateChooser, LASTWEEK);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        setDateChooser(startDateChooser, endDateChooser, LASTMONTH);
    }//GEN-LAST:event_jButton12ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        //doCalculation();
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void goButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goButtonActionPerformed
        doCalculation();
    }//GEN-LAST:event_goButtonActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.STATISTICS_FRAME.getPage());        // TODO add your handling code here:
    }//GEN-LAST:event_jButton7ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel artihmeticMean;
    private com.toedter.calendar.JDateChooser endDateChooser;
    private javax.swing.JButton goButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton7;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel longestGap;
    private javax.swing.JLabel maximum;
    private javax.swing.JLabel median;
    private javax.swing.JLabel minimum;
    private javax.swing.JLabel numberOfValues;
    private com.toedter.calendar.JDateChooser startDateChooser;
    private javax.swing.JLabel sum;
    private javax.swing.JLabel totalConsumption;
    // End of variables declaration//GEN-END:variables
}
