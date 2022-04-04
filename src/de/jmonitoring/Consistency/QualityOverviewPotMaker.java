/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Consistency;

import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DataHandling.DataHandler;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.DateBandAxis;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

/**
 *
 * @author togro
 */
public class QualityOverviewPotMaker {

    private XYPlot plot = new XYPlot();
    private String startDate, endDate;
    private ArrayList<String> labels = new ArrayList<String>(512);
    private DateInterval dateInterval;
    private StoppableThread t;// = (StoppableThread) Thread.currentThread();
    private Integer tolerance;
    private Stack<Color> colorStack = new Stack<Color>();
    private Color exColor = Color.ORANGE;
    private MoniSoftProgressBar progressBar;
    private int mode;
    private final MainApplication gui;

    /**
     * Erzeugt einen {@link XYPlot} mit horizontalen Balken (einer pro
     * Messpunkt) in denen Lücken in der Messwerterfassung dargestellt werden.
     * Die erzeugten Balken sind sensitiv und beim Klicken auf einen Balken
     *
     * @param dInterval
     * @param sensorList
     * @param tol
     */
    public QualityOverviewPotMaker(DateInterval dInterval, ArrayList<SensorProperties> sensorList, Integer tol, int mode, MainApplication gui) {
        this.tolerance = tol;
        this.mode = mode;
        this.gui = gui;
        t = (StoppableThread) Thread.currentThread();
        dateInterval = dInterval;
        progressBar = this.gui.getProgressBarpanel().addProgressBar(java.util.ResourceBundle.getBundle("de/jmonitoring/Consistency/Bundle").getString("QUALITYVIEW"));
        progressBar.addProgressCancelButtonActionListener(action);
        IntervalXYDataset set = createDataset(sensorList);
        ValueAxis domainAxis = new DateBandAxis(dateInterval, Day.class, false, true, true, true, 9);
        if (t.running && set != null) {
            plot.setDataset(set);
            plot.setDomainAxis(domainAxis);
            SymbolAxis yAxis = new SymbolAxis(java.util.ResourceBundle.getBundle("de/jmonitoring/Consistency/Bundle").getString("SENSORS"), labels.toArray(new String[labels.size()]));
            yAxis.setGridBandsVisible(false);
            plot.setRangeAxis(yAxis);
            XYBarRenderer renderer = new XYBarRenderer();
            renderer.setBarPainter(new StandardXYBarPainter());
            renderer.setShadowVisible(false);
            renderer.setUseYInterval(true);

            resetColors();

            // Erzeugen von Rendererrn für jeden Messpunkt (jeweils 2: okSeries und missingSeries)
            for (int counter = 0; counter < set.getSeriesCount(); counter++) {
//                System.out.println((counter) + " " + colorStack.peek().toString());
                renderer.setSeriesPaint(counter, colorStack.peek());
                renderer.setSeriesOutlinePaint(counter, colorStack.pop());
                if (colorStack.empty()) {
                    resetColors();
                }
            }
            plot.setDomainPannable(true);
            plot.setRangePannable(true);
            plot.setRenderer(renderer);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinesVisible(false);
            plot.setRangeGridlinesVisible(false);
        } else {
        }
        progressBar.remove();
    }

    /**
     * Erzeugt einen Datensatz zur Darstellung in der Qualitätsübersicht.
     * <br><br> Es werden jeweils mehrere Intervall-Serien pro Messpunkt erzeugt
     * die dann übereinander gelegt werden. In den Hintergrund kommt zunächst
     * ein grüner Balken über den gesamten Zeitbereich. Fehlende Daten werden in
     * eine "missingSeries geschrieben, die dann in den Vordergrund gelegt wird.
     *
     * @param sensorList Liste der {@link SensorProperties} der darzustellenden
     * Messpunkte
     * @return
     */
    private IntervalXYDataset createDataset(ArrayList<SensorProperties> sensorList) {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        long diffLong;
        Date sDate;
        Date sDatew;
        Date eDate = new Date();
        int lfd = 0;
        Connection myConn = null;
        SimpleDateFormat MySQLDateFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);

        startDate = dateInterval.getStartDateString(MySQLDateFormat);
        endDate = dateInterval.getEndDateString(MySQLDateFormat);

        long startSecond = dateInterval.getStartDate().getTime() / 1000L;
        long endSecond = dateInterval.getEndDate().getTime() / 1000L;

        XYIntervalSeriesCollection dataset = new XYIntervalSeriesCollection();

        progressBar.setMinMax(0, sensorList.size());
        Collections.reverse(sensorList);

        boolean hits;
        XYIntervalSeries missingSeries;
        XYIntervalSeries okSeries;
        XYIntervalSeries warningSeries;
        String ignorePart = "";
        Integer min, max;

        // Messpunkte durchlaufen
        for (SensorProperties currentProps : sensorList) {
            if (t.running) {
                int sensorID = currentProps.getSensorID();
                DataHandler handler = new DataHandler(sensorID);
                Date keepDate = dateInterval.getStartDate();
                Integer[] limitsWE = SensorInformation.getSensorProperties(sensorID).getWELimits();
                Integer[] limitsWT = SensorInformation.getSensorProperties(sensorID).getWTLimits();
                boolean sameValue = false;
                Double keepValue = null;
                Double value;

                labels.add(SensorInformation.getDisplayName(currentProps.getSensorID()));

                progressBar.setValue(lfd);
                progressBar.setText("Bearbeite" + " " + SensorInformation.getDisplayName(currentProps.getSensorID()));

                try {
                    myConn = DBConnector.openConnection();

                    if (MoniSoft.getInstance().getApplicationProperties().getProperty("UseIgnoreValue").equals("1")) {
                        
                        String ignoreValue = MoniSoft.getInstance().getApplicationProperties().getProperty("IgnoreValue");
                
                        // AZ: Ermittle alle zu ignorierenden Werte (Trenne nach Komma auf) - MONISOFT-10                     
                        if( ignoreValue != null )
                        {
                            List<String> ignoreValueList = Arrays.asList(ignoreValue.split(","));
                        
                            if( ignoreValueList != null && ignoreValueList.size() > 0 )
                            {             
                                for( int i = 0; i < ignoreValueList.size(); i++ )
                                {
                                    try
                                    {
                                        Double ignoreValueDouble = Double.valueOf(ignoreValueList.get(i));
                                        ignorePart += " and value <> " + ignoreValueDouble + " ";
                                    }
                                    catch( Exception e )
                                    {
                                        Messages.showMessage("Warning during createDataset ignoreValue not valid " + ignoreValueList.get(i) + ": " + e.getMessage() + "\n", true);
                                    }
                                }   
                            }
                        }
                    }

                    // KEIN Event
                    if (!currentProps.isEvent()) {
                        stmt = myConn.prepareStatement("select TimeStamp,Value from " + MoniSoftConstants.HISTORY_TABLE + " where T_Sensors_id_Sensors= ? and TimeStamp >= ? and TimeStamp <= ? " + ignorePart + "order by TimeStamp");
                        stmt.setInt(1, sensorID);
                        stmt.setLong(2, startSecond);
                        stmt.setLong(3, endSecond);
                        rs = stmt.executeQuery(); // stmt.executeQuery("select TimeStamp,Value from " + MoniSoftConstants.HISTORY_TABLE + " where T_Sensors_id_Sensors= " + sensorID + " and TimeStamp >= " + startSecond + " and TimeStamp <= " + endSecond + ignorePart + " order by TimeStamp");
                        boolean isStart = true;
                        sDate = null;
                        sDatew = null;
                        okSeries = new XYIntervalSeries(currentProps.getSensorID());                 // Serie 1
                        warningSeries = new XYIntervalSeries(currentProps.getSensorID() + "%");      // Serie 2
                        missingSeries = new XYIntervalSeries(currentProps.getSensorID() + "#");      // Serie 3
//                        addItem(lfd, okSeries, new Minute(MySQLDateFormat.parse(startDate)), new Minute(MySQLDateFormat.parse(endDate))); // grüner Balken für den gesamten Bereich
                        addItem(lfd, okSeries, new Minute(dateInterval.getStartDate()), new Minute(dateInterval.getEndDate())); // grüner Balken für den gesamten Bereich
                        hits = false;

                        //  ************ Datensätze des Messpunkts durchlaufen
                        while (rs.next()) {
                            if (mode == 0) {
                                // ************** Look for not changing values
                                if (isStart) {
                                    // Anfang des Zeitbereichs. Rot gefärbt bis der erste Wert auftaucht
                                    keepDate = new Date(rs.getLong(1) * 1000);
                                    value = rs.getDouble(2);
                                    keepValue = value;
                                    if (keepDate.getTime() - dateInterval.getStartDate().getTime() > 0) {
//                                    System.out.println(lfd + "StartRed from " + dateInterval.getStartDate() + " to " + keepDate);
                                        addItem(lfd, missingSeries, new Minute(dateInterval.getStartDate()), new Minute(keepDate));
                                    }
                                    isStart = false;
                                    continue;
                                }
                                // get current values
                                value = rs.getDouble(2);
                                Long dateLong = rs.getLong(1) * 1000;

                                if (keepValue != null && value.equals(keepValue)) { // same value as kept value
                                    sameValue = true;
                                    sDate = keepDate;
                                    eDate = new Date(dateLong);
                                } else {
                                    if (sameValue) {
                                        sameValue = false;
                                        diffLong = (eDate.getTime() - sDate.getTime()) / 60000;
                                        if (diffLong > tolerance) {
//                                        System.out.println(lfd + "Red from " + sDate + " to " + eDate);
                                            addItem(lfd, missingSeries, new Minute(sDate), new Minute(eDate));
                                        }
                                    }
                                    keepValue = value;
                                    keepDate = new Date(dateLong);
                                }
                                // **************  Look for not changing values
                            } else {
                                //  Anfang des Zeitbereichs. Rot gefärbt bis der erste Wert auftaucht
                                if (isStart && sDate == null) {
                                    sDate = new Date(rs.getLong(1) * 1000);
                                    addItem(lfd, missingSeries, new Minute(MySQLDateFormat.parse(startDate)), new Minute(sDate));
                                    continue;
                                }
                                isStart = false;
                                hits = true;    // keine weiteren Werte

                                eDate = new Date(rs.getLong(1) * 1000);

                                diffLong = (eDate.getTime() - sDate.getTime()) / 60000;

                                if (diffLong > tolerance) {
                                    addItem(lfd, missingSeries, new Minute(sDate), new Minute(eDate)); // add red br form last entry to this entry
                                }
                                sDate = eDate; // set last entry to this


                                // Grenzen je nach Tag (WT/WE)

                                if (DateTimeCalculator.isWeekend(eDate)) {
                                    min = limitsWE[0];
                                    max = limitsWE[1];
                                } else {
                                    min = limitsWT[0];
                                    max = limitsWT[1];
                                }

                                if ((max != null && rs.getDouble(2) > max) || (min != null && rs.getDouble(2) < min)) {
                                    if (sDatew == null) {
                                        sDatew = new Date(rs.getLong(1) * 1000);
                                    }
                                } else {
                                    if (sDatew != null) { // es wurde ein ungültiger Wert gefunden, sont wäre sDatew null
                                        addItem(lfd, warningSeries, new Minute(sDatew), new Minute(eDate));
                                        sDatew = null;
                                    }
                                }
                            }
                        }
                    } else { // Event /////////////////////////////////////////////////////// Event
                        // den ersten vorkommenden, vorhandenen Wert im Intervall bestimmen
                        stmt = myConn.prepareStatement("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = ? and TimeStart >= ? and TimeStart <= ? and State is not null order by TimeStart limit 1");
                        stmt.setInt(1, sensorID);
                        stmt.setString(2, startDate);
                        stmt.setString(3, endDate);
                        rs = stmt.executeQuery(); // stmt.executeQuery("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = " + sensorID + " and TimeStart >= '" + startDate + "' and TimeStart <= '" + endDate + "' and State is not null order by TimeStart limit 1");
                        if (rs.next()) {
                            sDate = rs.getTimestamp(1); // wenn es einen Wert gibt dessen Startzeitpunkt übernehmen (bis dahin alles rot)
                        } else {
                            sDate = MySQLDateFormat.parse(endDate);
                        }
//                        System.out.println("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = " + sensorID + " and TimeStart >= '" + startDate + "' and TimeStart <= '" + endDate + "' and State is null order by TimeStart");
                        stmt = myConn.prepareStatement("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = ? and TimeStart >= ? and TimeStart <= ? and State is null order by TimeStart");
                        stmt.setInt(1, sensorID);
                        stmt.setString(2, startDate);
                        stmt.setString(3, endDate);
                        rs = stmt.executeQuery(); //stmt.executeQuery("select TimeStart,TimeSpan,State from " + MoniSoftConstants.EVENT_TABLE + " where T_Sensors_id_Sensors = " + sensorID + " and TimeStart >= '" + startDate + "' and TimeStart <= '" + endDate + "' and State is null order by TimeStart");
                        okSeries = new XYIntervalSeries(currentProps.getSensorID());                 // Serie 1
                        warningSeries = new XYIntervalSeries(currentProps.getSensorID() + "%");      // Serie 2
                        missingSeries = new XYIntervalSeries(currentProps.getSensorID() + "#");      // Serie 3
                        addItem(lfd, okSeries, new Minute(dateInterval.getStartDate()), new Minute(dateInterval.getEndDate())); // grüner Balken für den gesamten Bereich
                        hits = true;
                        // LEERE !!!! Datensätze des Messpunkts durchlaufen (siehe sql-Abfrage!!)
                        addItem(lfd, missingSeries, new Minute(MySQLDateFormat.parse(startDate)), new Minute(sDate));
                        while (rs.next()) {
                            sDate = rs.getTimestamp(1);
                            eDate = new Date(sDate.getTime() + rs.getLong(2));
                            addItem(lfd, missingSeries, new Minute(sDate), new Minute(eDate));
                        }

                        Measurement prevMeasurement = handler.getPreviousDBEntry(dateInterval.getStartDate(), dateInterval.getEndDate());
                        if (prevMeasurement == null) {
                            hits = false;
                        } else {
                            eDate = new Date(prevMeasurement.getTime());
                        }

//
//                        // Den letzten Wert des Events vor Ende des Abfrageintervalls (aber innerhalb des Abfragezeitraums) bestimmen. Bei Events würde sonst alles bis zum Ende des Abfrageintervalls grün auch wenn das in der Zukunft liegt
//                        HashMap<Date, Number> map = handler.getPreviousDBEntry(dateInterval.getStartDate(), dateInterval.getEndDate());
//                        if (map.size() > 0) {
//                            Set<Date> dateEntry = map.keySet();
//                            Iterator<Date> dit = dateEntry.iterator();
//                            eDate = dit.next();
//                        } else {
//                            hits = false;
//                        }
                    }


                    if (mode == 0) {
                        if (sameValue) {
//                        System.out.println(lfd + "endred from " + sDate + " to " + eDate);
                            addItem(lfd, missingSeries, new Minute(sDate), new Minute(eDate));
                        }
                    } else {
                        if (!hits) {    // es gab keinen einzigen Wert im Intervall -> alles rot
                            addItem(lfd, missingSeries, new Minute(dateInterval.getStartDate()), new Minute(dateInterval.getEndDate()));
                        } else {
                            addItem(lfd, missingSeries, new Minute(eDate), new Minute(dateInterval.getEndDate())); // roter Balken vom letzten Wert zum Ende
                        }
                    }

                    // Serien hinzufügen. Die zuerst hinzugefügte Serie kommt in den Vordergrund
                    dataset.addSeries(missingSeries);
                    dataset.addSeries(warningSeries);
                    dataset.addSeries(okSeries);
                } catch (Exception e) {
                    Messages.showMessage("Error query database [QUA]" + e.getMessage() + "\n", true);
                    Messages.showException(e);
                    Messages.showException(e);
                } finally {
                    DBConnector.closeConnection(myConn, stmt, rs);
                }
                lfd++;
            } else {
                progressBar.remove();
                return null;
            }
        }
        return dataset;
    }
    ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            progressBar.remove();
            t.running = false;
//            t.interrupt();
        }
    };

    /**
     * Setzt die Reihenfolge der Farben wieder auf die Standardeihenfolge zurück
     */
    private void resetColors() {
        colorStack.push(Color.GREEN);
        colorStack.push(exColor);
        colorStack.push(Color.RED);
    }

    /**
     * Fügt einer Intervall-Serie eine neuen Zeitbereich hinzu
     *
     * @param sensorIndex Index der Messpunkts. Entspricht der "Zeile" des
     * Balkens in der Grafik.
     * @param s die Intervall-Serie
     * @param p0 Startzeitpunkt
     * @param p1 Endzeitpunkt
     */
    private void addItem(int sensorIndex, XYIntervalSeries s, RegularTimePeriod p0, RegularTimePeriod p1) {
        s.add(p0.getFirstMillisecond(), p0.getFirstMillisecond(), p1.getLastMillisecond(), sensorIndex, sensorIndex - 0.35, sensorIndex + 0.35);
    }

    /**
     * Liefert den erzeugten Plot zurück
     *
     * @return
     */
    public XYPlot getPlot() {
        return plot;
    }
}
