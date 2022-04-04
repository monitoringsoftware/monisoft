package de.jmonitoring.Components;

import de.jmonitoring.ApplicationProperties.AppPrefsDialog;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;
import org.jfree.data.time.SimpleTimePeriod;

import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DatasetWorker;
import de.jmonitoring.DataHandling.DatasetWorkerFactory;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import de.jmonitoring.TableModels.CompareJXTable;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import de.jmonitoring.utils.tablecellrenderer.ColoredDoubleCellRenderer;

/**
 *
 * @author togro
 */
public class CompareTableFrame extends javax.swing.JInternalFrame implements Runnable {

    private StoppableThread stoppThread;
    private JProgressBar progress = new JProgressBar();
    private TreeMap<Integer, Double> mean_1 = new TreeMap<Integer, Double>();
    private TreeMap<Integer, Double> mean_2 = new TreeMap<Integer, Double>();
    private TreeMap<Integer, Double> mean_7 = new TreeMap<Integer, Double>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("E dd.MM.yyyy");
    private String d1, d2, d3;
    private boolean active = false;
    private ArrayList<Integer> idList = new ArrayList<Integer>();
    private Integer[] abberationPercentages = new Integer[6];
    private JXTable theTable;
    private final MainApplication gui;

    /**
     * Creates new form CompareTableFrame TODO: unify with other constructor!
     */
    public CompareTableFrame(MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setAbberationLimits();
        setLimitTexts();
        referenceDateChooser.getJCalendar().setTodayButtonVisible(true);
        referenceDateChooser.setDate(new Date());

        setHeaderDates();

        setSize(this.gui.getDesktop().getWidth(), this.gui.getDesktop().getHeight() / 2);
        updateSelectionComboBox();

        active = true; // aktiv setzen nachdem Combobox gebaut, sonst wird das event ausgelöst
    }

    /**
     * Creates new form CompareTableFrame
     */
    public CompareTableFrame(String collection, MainApplication gui) {
        super();
        this.gui = gui;
        initComponents();
        setAbberationLimits();
        setLimitTexts();
        idList = new ArrayList<Integer>(SensorCollectionHandler.getSensorsForCollection(collection));
        savedSetSelector.setSelectedItem(collection);
        referenceDateChooser.getJCalendar().setTodayButtonVisible(true);
        referenceDateChooser.setDate(new Date());

        setHeaderDates();

        setSize(this.gui.getDesktop().getWidth(), this.gui.getDesktop().getHeight() / 2);
        updateSelectionComboBox();
    }

    private void setHeaderDates() {
        d1 = DateTimeCalculator.getTimeSpanBeforeToday(1, 0, referenceDateChooser.getDate()).getStartDateString(dateFormat);// + "<br>-<br>" + DateTimeCalculator.getTimeSpanBeforeToday(1, 0).getEndDateString(dateFormat);
        d2 = DateTimeCalculator.getTimeSpanBeforeToday(8, 0, referenceDateChooser.getDate()).getStartDateString(dateFormat);// + "<br>-<br>" + DateTimeCalculator.getTimeSpanBeforeToday(7, 0).getEndDateString(dateFormat);
        d3 = DateTimeCalculator.getTimeSpanBeforeToday(9, 7, referenceDateChooser.getDate()).getStartDateString(dateFormat) + "<br>-<br>" + DateTimeCalculator.getTimeSpanBeforeToday(9, 7, referenceDateChooser.getDate()).getEndDateString(dateFormat);
    }

    private void setAbberationLimits() {
        // default settings
        abberationPercentages[0] = 20;
        abberationPercentages[1] = 30;
        abberationPercentages[2] = 50;
        abberationPercentages[3] = -20;
        abberationPercentages[4] = -30;
        abberationPercentages[5] = -50;

        String s = MoniSoft.getInstance().getApplicationProperties().getProperty("AbberationLimits");
        String[] limitString = s.split(",");


        if (limitString.length == 6) {   // the number of string is not correct, use the default values from above
            Integer[] limits = new Integer[6];
            for (int i = 0; i < limitString.length; i++) {
                try {
                    limits[i] = Integer.valueOf(limitString[i]); // if the number has incorrect values or is no number use the default value
                } catch (NumberFormatException e) {
                    limits[i] = abberationPercentages[i];
                }
                abberationPercentages[i] = limits[i];
            }
        }
    }

    private void setWorkingPanel() {
        JLabel waitLabel = new JLabel(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/waiting.gif")));
        JButton cancelButton = new JButton(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.TABELLENGENERIERUNG ABBRECHEN"));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainPanel.removeAll();
                mainPanel.setBackground(Color.WHITE);
                mainPanel.add(new JLabel(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.BEARBEITUNG ABGEBROCHEN"), JLabel.CENTER));
                mainPanel.revalidate();
                stoppThread.running = false;
                disposeMe();
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.ABGEBROCHEN"), true);
            }
        });
        mainPanel.removeAll();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(waitLabel, BorderLayout.CENTER);
        mainPanel.add(progress, BorderLayout.SOUTH);
        mainPanel.add(cancelButton, BorderLayout.NORTH);
        mainPanel.revalidate();
    }

    private void setLimitTexts() {
        jLabel2.setText("+" + abberationPercentages[0].toString() + "%");
        jLabel3.setText("+" + abberationPercentages[1].toString() + "%");
        jLabel4.setText("+" + abberationPercentages[2].toString() + "%");
        jLabel5.setText(abberationPercentages[3].toString() + "%");
        jLabel6.setText(abberationPercentages[4].toString() + "%");
        jLabel7.setText(abberationPercentages[5].toString() + "%");
    }

    /**
     * Durchlaufen der Messpunkte und erzeugen der Tabelle
     */
    @Override
    public void run() {
        savedSetSelector.setEnabled(false);
        printTableButton.setEnabled(false);

        stoppThread.running = true;
        CompareTableModel cm = new CompareTableModel();
        int sensorID;
        Date referenceDate = referenceDateChooser.getDate();
        progress.setMaximum(idList.size()); // hier natürlich die auswahl anpassen!
        for (int i = 0; i < idList.size(); i++) {
            progress.setValue(i);
            sensorID = idList.get(i);
            if (!stoppThread.running) { // herausspringen wenn running nicht mehr true (abbrechen gedrückt)
                break;
            }
            cm.addSensor(SensorInformation.getSensorProperties(sensorID));
            mean_1.put(sensorID, getRecentMeanValue(sensorID, 1, 0, referenceDate));
            mean_2.put(sensorID, getRecentMeanValue(sensorID, 8, 0, referenceDate));
            mean_7.put(sensorID, getRecentMeanValue(sensorID, 7, 6, referenceDate));
        }

        if (stoppThread.running) { // herausspringen wenn running nicht mehr true (abbrechen gedrückt)
            theTable = new CompareJXTable(cm, abberationPercentages);
            theTable.setFont(new java.awt.Font("Dialog", 0, 9));
            theTable.packAll();
            theTable.setColumnControlVisible(true);

            JScrollPane scrollPane = new JScrollPane(theTable);
            mainPanel.removeAll();
            mainPanel.add(scrollPane, BorderLayout.CENTER);
            mainPanel.revalidate();
        }

        savedSetSelector.setEnabled(true);
        printTableButton.setEnabled(true);
        active = true; // force if called form favorite
    }

    public synchronized Double getRecentMeanValue(int sensorID, int daysBefore, int daysDuration, Date date) {
        Integer lookBackDays;
        Integer lookBackDuration;
        Date today;
        lookBackDays = daysBefore; // negativ, wir wollen ja in die Vergangenheit
        lookBackDuration = daysDuration;
        today = date;
        Double value;

        RecentMeanValueThread ts = new RecentMeanValueThread(sensorID, lookBackDays, lookBackDuration, today, gui);
        ts.start();

        try {
            ts.join(5000);
        } catch (InterruptedException ex) {
            Messages.showException(ex);
        }

        value = ts.getValue();

        if (daysDuration > 0 && value != null && SensorInformation.getSensorProperties(sensorID).isCounter()) {
//            System.out.println("Mittel über " + (daysDuration +1) + " Tage");
            value = value / (daysDuration + 1);
        }

        return value;
    }

    private static class RecentMeanValueThread extends StoppableThread {

        private Integer lookBackDays;
        private Integer lookBackDuration;
        private Date today;
        private Double value = null;
        private final MainApplication gui;
        private final int sensorID;

        public RecentMeanValueThread(int sensorID, Integer lookBackDays, Integer lookBackDuration, Date today, MainApplication gui) {
            this.sensorID = sensorID;
            this.lookBackDays = lookBackDays;
            this.lookBackDuration = lookBackDuration;
            this.today = today;
            this.gui = gui;
        }

        @Override
        public void run() {
            running = true;
            CounterMode mode;
            DateInterval span = DateTimeCalculator.getTimeSpanBeforeToday(lookBackDays, lookBackDuration, today);
            DatasetWorker dw = new DatasetWorkerFactory(this.gui).createFor(sensorID);
            if (SensorInformation.getSensorProperties(sensorID).isCounter()) {
                mode = CounterMode.COUNTERCONSUMPTION;
            } else {
                mode = CounterMode.NOCOUNTER;
            }
            mode.setAutoCounterChange(MoniSoft.getInstance().getApplicationProperties().getProperty("AutomaticCounterChange").equals("1"));
            MeasurementTreeSet dataSet = dw.getInterpolatedData(span, new SimpleTimePeriod(span.getStartDate(), span.getEndDate()), mode);
            value = dataSet.first().getValue();
        }

        public Double getValue() {
            return value;
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

        abberationDialog = new javax.swing.JDialog();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        plus1 = new javax.swing.JSpinner();
        plus2 = new javax.swing.JSpinner();
        plus3 = new javax.swing.JSpinner();
        minus1 = new javax.swing.JSpinner();
        minus2 = new javax.swing.JSpinner();
        minus3 = new javax.swing.JSpinner();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        abberationOKButtonjButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        savedSetSelector = new javax.swing.JComboBox();
        jButton7 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        closeButton = new javax.swing.JButton();
        colorCodePanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        referenceDateChooser = new com.toedter.calendar.JDateChooser();
        printTableButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();

        abberationDialog.setAlwaysOnTop(true);
        abberationDialog.setLocationByPlatform(true);
        abberationDialog.setMinimumSize(new java.awt.Dimension(257, 227));
        abberationDialog.setModal(true);
        abberationDialog.setResizable(false);
        abberationDialog.setUndecorated(true);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle"); // NOI18N
        jLabel9.setText(bundle.getString("CompareTableFrame.jLabel9.text")); // NOI18N

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel10.setText(bundle.getString("CompareTableFrame.jLabel10.text")); // NOI18N

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel11.setText(bundle.getString("CompareTableFrame.jLabel11.text")); // NOI18N

        jLabel12.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel12.setText(bundle.getString("CompareTableFrame.jLabel12.text")); // NOI18N

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel13.setText(bundle.getString("CompareTableFrame.jLabel13.text")); // NOI18N

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel14.setText(bundle.getString("CompareTableFrame.jLabel14.text")); // NOI18N

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel15.setText(bundle.getString("CompareTableFrame.jLabel15.text")); // NOI18N

        plus1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        plus1.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));

        plus2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        plus2.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));

        plus3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        plus3.setModel(new javax.swing.SpinnerNumberModel(0, 0, 100, 1));

        minus1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        minus1.setModel(new javax.swing.SpinnerNumberModel(0, -100, 0, 1));

        minus2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        minus2.setModel(new javax.swing.SpinnerNumberModel(0, -100, 0, 1));

        minus3.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        minus3.setModel(new javax.swing.SpinnerNumberModel(0, -100, 0, 1));

        jLabel16.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel16.setText(bundle.getString("CompareTableFrame.jLabel16.text")); // NOI18N

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel17.setText(bundle.getString("CompareTableFrame.jLabel17.text")); // NOI18N

        jLabel18.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel18.setText(bundle.getString("CompareTableFrame.jLabel18.text")); // NOI18N

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel19.setText(bundle.getString("CompareTableFrame.jLabel19.text")); // NOI18N

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel20.setText(bundle.getString("CompareTableFrame.jLabel20.text")); // NOI18N

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel21.setText(bundle.getString("CompareTableFrame.jLabel21.text")); // NOI18N

        abberationOKButtonjButton.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        abberationOKButtonjButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/accept.png"))); // NOI18N
        abberationOKButtonjButton.setText(bundle.getString("CompareTableFrame.abberationOKButtonjButton.text")); // NOI18N
        abberationOKButtonjButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                abberationOKButtonjButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout abberationDialogLayout = new javax.swing.GroupLayout(abberationDialog.getContentPane());
        abberationDialog.getContentPane().setLayout(abberationDialogLayout);
        abberationDialogLayout.setHorizontalGroup(
            abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abberationDialogLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12))
            .addGroup(abberationDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(abberationDialogLayout.createSequentialGroup()
                        .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(plus1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(plus2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(plus3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minus1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minus2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(minus3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17)
                            .addComponent(jLabel18)
                            .addComponent(jLabel19)
                            .addComponent(jLabel20)
                            .addComponent(jLabel21)))
                    .addComponent(abberationOKButtonjButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        abberationDialogLayout.setVerticalGroup(
            abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(abberationDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(plus1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(plus2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel17))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(plus3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(minus1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(minus2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(abberationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(minus3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(abberationOKButtonjButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle(bundle.getString("CompareTableFrame.title")); // NOI18N

        jPanel2.setMinimumSize(new java.awt.Dimension(100, 30));
        jPanel2.setPreferredSize(new java.awt.Dimension(884, 30));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText(bundle.getString("CompareTableFrame.jLabel1.text")); // NOI18N

        savedSetSelector.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        savedSetSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        savedSetSelector.setMinimumSize(new java.awt.Dimension(68, 20));
        savedSetSelector.setPreferredSize(new java.awt.Dimension(68, 20));
        savedSetSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                savedSetSelectorActionPerformed(evt);
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
                jButton7help(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(savedSetSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 360, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 355, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(savedSetSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel1.setMinimumSize(new java.awt.Dimension(100, 35));
        jPanel1.setPreferredSize(new java.awt.Dimension(748, 35));
        jPanel1.setLayout(new java.awt.BorderLayout());

        closeButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        closeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cancel.png"))); // NOI18N
        closeButton.setText(bundle.getString("CompareTableFrame.closeButton.text")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        jPanel1.add(closeButton, java.awt.BorderLayout.LINE_END);

        colorCodePanel.setMinimumSize(new java.awt.Dimension(0, 30));
        colorCodePanel.setPreferredSize(new java.awt.Dimension(612, 30));

        jLabel2.setBackground(java.awt.Color.yellow);
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText(bundle.getString("CompareTableFrame.jLabel2.text")); // NOI18N
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel2.setIconTextGap(5);
        jLabel2.setOpaque(true);
        jLabel2.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel3.setBackground(java.awt.Color.orange);
        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText(bundle.getString("CompareTableFrame.jLabel3.text")); // NOI18N
        jLabel3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel3.setIconTextGap(5);
        jLabel3.setOpaque(true);
        jLabel3.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });

        jLabel4.setBackground(java.awt.Color.red);
        jLabel4.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText(bundle.getString("CompareTableFrame.jLabel4.text")); // NOI18N
        jLabel4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel4.setIconTextGap(5);
        jLabel4.setMaximumSize(new java.awt.Dimension(33, 20));
        jLabel4.setOpaque(true);
        jLabel4.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel4MouseClicked(evt);
            }
        });

        jLabel5.setBackground(new java.awt.Color(205, 255, 205));
        jLabel5.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText(bundle.getString("CompareTableFrame.jLabel5.text")); // NOI18N
        jLabel5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setIconTextGap(5);
        jLabel5.setOpaque(true);
        jLabel5.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel6.setBackground(new java.awt.Color(51, 204, 0));
        jLabel6.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText(bundle.getString("CompareTableFrame.jLabel6.text")); // NOI18N
        jLabel6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel6.setIconTextGap(5);
        jLabel6.setOpaque(true);
        jLabel6.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel6MouseClicked(evt);
            }
        });

        jLabel7.setBackground(new java.awt.Color(0, 151, 53));
        jLabel7.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText(bundle.getString("CompareTableFrame.jLabel7.text")); // NOI18N
        jLabel7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel7.setIconTextGap(5);
        jLabel7.setMaximumSize(new java.awt.Dimension(33, 20));
        jLabel7.setOpaque(true);
        jLabel7.setPreferredSize(new java.awt.Dimension(40, 25));
        jLabel7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel7MouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel8.setText(bundle.getString("CompareTableFrame.jLabel8.text")); // NOI18N

        referenceDateChooser.setDateFormatString(bundle.getString("DatePanel.DateFormatString")); // NOI18N
        referenceDateChooser.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        referenceDateChooser.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                referenceDateChooserPropertyChange(evt);
            }
        });

        printTableButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        printTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/printer.png"))); // NOI18N
        printTableButton.setText(bundle.getString("CompareTableFrame.printTableButton.text")); // NOI18N
        printTableButton.setToolTipText(bundle.getString("CompareTableFrame.printTableButton.toolTipText")); // NOI18N
        printTableButton.setMaximumSize(new java.awt.Dimension(20, 20));
        printTableButton.setMinimumSize(new java.awt.Dimension(20, 20));
        printTableButton.setPreferredSize(new java.awt.Dimension(20, 20));
        printTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTableButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout colorCodePanelLayout = new javax.swing.GroupLayout(colorCodePanel);
        colorCodePanel.setLayout(colorCodePanelLayout);
        colorCodePanelLayout.setHorizontalGroup(
            colorCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorCodePanelLayout.createSequentialGroup()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(referenceDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(printTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );
        colorCodePanelLayout.setVerticalGroup(
            colorCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(colorCodePanelLayout.createSequentialGroup()
                .addGroup(colorCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(colorCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(colorCodePanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(colorCodePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(jLabel8)
                            .addComponent(referenceDateChooser, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(printTableButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        colorCodePanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {jLabel3, jLabel4, jLabel5, jLabel6, jLabel7});

        jPanel1.add(colorCodePanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_END);

        mainPanel.setBackground(new java.awt.Color(255, 255, 255));
        mainPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        active = false;
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    private void savedSetSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_savedSetSelectorActionPerformed
        if (active && !(savedSetSelector.getSelectedItem().toString().equals(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("SensorCollectionHandler.PLEASE_CHOOSE")))) {
            idList = new ArrayList<Integer>(SensorCollectionHandler.getSensorsForCollection((String) savedSetSelector.getSelectedItem()));
            setHeaderDates();
            startCalc(false);
        }
    }//GEN-LAST:event_savedSetSelectorActionPerformed

    private void referenceDateChooserPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_referenceDateChooserPropertyChange
        if (active && !(savedSetSelector.getSelectedItem().toString().equals(java.util.ResourceBundle.getBundle("de/jmonitoring/DBOperations/Bundle").getString("SensorCollectionHandler.PLEASE_CHOOSE")))) {
            setHeaderDates();
            startCalc(false);
        }
    }//GEN-LAST:event_referenceDateChooserPropertyChange

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel2MouseClicked

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel3MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel3MouseClicked

    private void jLabel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel4MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel4MouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel5MouseClicked

    private void jLabel6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel6MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel6MouseClicked

    private void jLabel7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel7MouseClicked
        showAbberationEntryPanel();
    }//GEN-LAST:event_jLabel7MouseClicked

    private void abberationOKButtonjButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_abberationOKButtonjButtonActionPerformed
        abberationDialog.setVisible(false);

        abberationPercentages[0] = (Integer) plus1.getValue();
        abberationPercentages[1] = (Integer) plus2.getValue();
        abberationPercentages[2] = (Integer) plus3.getValue();
        abberationPercentages[3] = (Integer) minus1.getValue();
        abberationPercentages[4] = (Integer) minus2.getValue();
        abberationPercentages[5] = (Integer) minus3.getValue();

        setLimitTexts();
        saveAbberationToProperties();
        theTable.getColumn(5).setCellRenderer(new ColoredDoubleCellRenderer("0.00", abberationPercentages));
        theTable.getColumn(6).setCellRenderer(new ColoredDoubleCellRenderer("0.00", abberationPercentages));

        ((CompareTableModel) theTable.getModel()).fireTableDataChanged();
    }//GEN-LAST:event_abberationOKButtonjButtonActionPerformed

    private void printTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printTableButtonActionPerformed
        MessageFormat headerForm = new MessageFormat(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("COMPARE_TABLE") + " '" + (String) savedSetSelector.getSelectedItem() + "' " + MoniSoft.getInstance().getDBConnector().getDBName());
        MessageFormat footerForm = new MessageFormat(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SensorTablePanel.PAGE") + " {0}");
        try {
            theTable.print(JTable.PrintMode.FIT_WIDTH, headerForm, footerForm, true, null, true);
        } catch (PrinterException ex) {
            Messages.showException(ex);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("SensorTablePanel.FEHLER BEIM DRUCKEN. BITTE DRUCKER ÜBERPRÜFEN"), true);
        }
    }//GEN-LAST:event_printTableButtonActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.COMPARE_TABLE.getPage());
    }//GEN-LAST:event_jButton7help

    public void startCalc(boolean forceActive) {
        if (!active && !forceActive) {
            return;
        }
        setWorkingPanel();
        stoppThread = new StoppableThread(this);
        stoppThread.start();
    }

    private void saveAbberationToProperties() {
        String limitString = "";
        String sep = "";
        for (int i = 0; i < abberationPercentages.length; i++) {
            limitString += sep + String.valueOf(abberationPercentages[i]);
            sep = ",";
        }
        MoniSoft.getInstance().getApplicationProperties().setProperty("AbberationLimits", limitString);
        try {
            AppPrefsDialog.saveProperties(false, false);
        } catch (FileNotFoundException e) {
            Messages.showMessage("Die Anwendungseinsdtellungen können nicht geschrieben werden. Die Datei existiert nicht.", true);
        } catch (IOException e) {
            Messages.showMessage("Die Anwendungseinsdtellungen können nicht geschrieben werden.", true);
        }
    }

    class CompareTableModel extends AbstractTableModel {

        private ArrayList<SensorProperties> sensors = new ArrayList<SensorProperties>();

        public void addSensor(SensorProperties sensor) {
            sensors.add(sensor);
        }

        @Override
        public int getRowCount() {
            return sensors.size();
        }

        @Override
        public int getColumnCount() {
            return 7;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
//
            SensorProperties properties = (SensorProperties) sensors.get(rowIndex);
            int id = properties.getSensorID();
            Double percent;
            switch (columnIndex) {
                case 0:
                    return properties.getSensorName() + " " + properties.getSensorDescription();
                case 1:
                    return properties.getSensorUnit();
                case 2: // letzter Tag
                    return mean_1.get(id);
                case 3:
                    return mean_2.get(id);
                case 4:
                    return mean_7.get(id);
                case 5:
                    if (mean_2.get(id) != null && mean_1.get(id) != null) {
                        percent = ((mean_1.get(id) - mean_2.get(id)) / mean_2.get(id)) * 100f;
                        return percent.equals(Double.NaN) ? 0d : percent;
                    } else {
                        return null;
                    }
                case 6:
                    if (mean_7.get(id) != null && mean_1.get(id) != null) {
                        percent = ((mean_1.get(id) - mean_7.get(id)) / mean_7.get(id)) * 100f;
                        return percent.equals(Double.NaN) ? 0d : percent;
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.MESSPUNKT") + "</center></html>";
                case 1:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.UNIT") + "<br></center></html>";
                case 2:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.CURRENTDAY") + "<br><font size =1>" + d1 + "</center></html>";
                case 3:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.LASTWEEK") + "<br><font size =1>" + d2 + "</center></html>";
                case 4:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.WEEKLYMEAN") + "<br><font size =1>" + d3 + "</center></html>";
                case 5:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.DIFFERENCE") + "<br><font size =1>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.PREDAY") + "</font><br>[%]</center></html>";
                case 6:
                    return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.DIFFERENCE") + "<br><font size =1>" + java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CompareTableFrame.WEEKLYMEAN") + "</font><br>[%]</center></html>";
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 0:
                    return String.class;
                case 1:
                    return String.class;
                case 2:
                    return Double.class;
                case 3:
                    return Double.class;
                case 4:
                    return Double.class;
                case 5:
                    return Double.class;
                case 6:
                    return Double.class;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }

    private void showAbberationEntryPanel() {
        plus1.setValue(abberationPercentages[0]);
        plus2.setValue(abberationPercentages[1]);
        plus3.setValue(abberationPercentages[2]);
        minus1.setValue(abberationPercentages[3]);
        minus2.setValue(abberationPercentages[4]);
        minus3.setValue(abberationPercentages[5]);

        abberationDialog.setVisible(true);
    }

    private void updateSelectionComboBox() {
        savedSetSelector.setModel(SensorCollectionHandler.getSensorCollectionNamesAsComboBoxModel(SensorCollectionHandler.SIMPLE_COLLECTION, true, false));
        savedSetSelector.setSelectedIndex(0);
    }

    private void disposeMe() {
        stoppThread = null;
        this.gui.disposeIFrame(this);
    }

    public void setIDLIst(ArrayList<Integer> list) {
        idList = list;
    }

    public void setReferencedate(Date date) {
        referenceDateChooser.setDate(date);
    }

    public void setActive(boolean b) {
        active = b;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog abberationDialog;
    private javax.swing.JButton abberationOKButtonjButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel colorCodePanel;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSpinner minus1;
    private javax.swing.JSpinner minus2;
    private javax.swing.JSpinner minus3;
    private javax.swing.JSpinner plus1;
    private javax.swing.JSpinner plus2;
    private javax.swing.JSpinner plus3;
    private javax.swing.JButton printTableButton;
    private com.toedter.calendar.JDateChooser referenceDateChooser;
    private javax.swing.JComboBox savedSetSelector;
    // End of variables declaration//GEN-END:variables
}
