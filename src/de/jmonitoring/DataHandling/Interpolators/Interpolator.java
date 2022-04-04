package de.jmonitoring.DataHandling.Interpolators;

import de.jmonitoring.DataHandling.CounterChange.CounterChange;
import de.jmonitoring.DataHandling.CounterChange.CounterChangeDialog;
import de.jmonitoring.DataHandling.CounterChange.CounterChangeErrorDialog;
import de.jmonitoring.DataHandling.CounterChange.CounterChangeHandler;
import de.jmonitoring.DataHandling.CounterMode;
import de.jmonitoring.DataHandling.DataHandler;
import de.jmonitoring.DataHandling.EventMode;
import de.jmonitoring.DataHandling.IntervalMeasurement;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TreeMap;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 * This class is the core of the intepolation system<p> It calls the
 * {@link CounterInterpolator} or {@link WeightedMeanInterpolator} depending on
 * the given sensor<br>
 *
 * Its purpose is to aggregate/interpolate a given raw datase for a specified
 * time interval and return it a a result
 *
 * @author togro
 */
public class Interpolator {

    private boolean debugVerbose = true; // Zum Testen, erzeugt mehr Ausgaben
    private StoppableThread stoppableThread;
    private SimpleDateFormat HumanDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat);
    private SimpleDateFormat HumanDateFormat = new SimpleDateFormat(MoniSoftConstants.HumanDateFormat);
    private MeasurementTreeSet measurmentSet = new MeasurementTreeSet();
    private TreeMap<Long, MinMax> minMaxMap = new TreeMap<Long, MinMax>();
    private ArrayList<IntervalMarker> missingMarkerList = new ArrayList<IntervalMarker>(20);
    private Long missingTime = 0L; // Länge der nicht mit Werten abgedeckten Zeit im Abfrageintervall in Millisekunden
    private Long durationOfValidity = null;
    private Long edgeTolerance; // = 0L;
    private CounterMode counterMode;
    private int sensorID;
    private boolean verbose = false;
    private ArrayList<String> remarks = new ArrayList<String>();
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();
    private final DateInterval dateSpan;
    private TimePeriod aggregationPeriod;
    private EventMode eventMode;
    private MeasurementTreeSet measurements;
    private final MainApplication gui;
    private ArrayList<CounterStep> steps = new ArrayList<CounterStep>();
    private ArrayList<CounterChange> changes = null;
    
    // AZ: speichere eine Entscheidung, die für mehrere Warnungen gelten soll - MONISOFT-8
    public static boolean saveCancelDecision = false;
    public static int lastSensorID = -1;
    public IntervalMeasurement lastResult;
    
    public static Integer chartType = -1;
    
    public static Integer TIMESERIES_TAB = 1;
    public static Integer XYPLOT_TAB = 2;
    public static Integer CARPET_TAB = 3;
    public static Integer MAINTENANCE_TAB = 4;
    public static Integer OGIVE_TAB = 5;
    public static Integer TIMESERIES_PLOT_TAB = 6;
    public static Integer EXPORT_TAB = 7;
    
    

    /**
     * Create a new {@link Interpolator} with the given parameters
     *
     * @param measurements A {@link MeasurementTreeSet} of raw measurements
     * @param dateSpan The date span of the data
     * @param aggregationPeriod The interpolation interval
     * @param counterMode The counter mode
     * @param sensorID The sensor
     * @param eventMode The event mode
     * @param gui The calling GUI
     * @see MeasurementTreeSet
     * @see Measurement
     * @see CounterMode
     * @see EventMode
     */
    public Interpolator(MeasurementTreeSet measurements, DateInterval dateSpan, TimePeriod aggregationPeriod, CounterMode counterMode, int sensorID, EventMode eventMode, MainApplication gui) {
        this(measurements, dateSpan, aggregationPeriod, counterMode, sensorID, eventMode, (StoppableThread) Thread.currentThread(), gui);
    }

    /**
     * Create a new {@link Interpolator} with the given parameters<p> This
     * constructor is only used for Junit tests
     *
     * @param measurements A {@link MeasurementTreeSet} of raw measurements
     * @param dateSpan The date span of the data
     * @param aggregationPeriod The interpolation interval
     * @param counterMode The counter mode
     * @param sensorID The sensor
     * @param eventMode The event mode
     * @param thread A thread
     * @param gui The calling GUI
     * @see MeasurementTreeSet
     * @see Measurement
     * @see CounterMode
     * @see EventMode
     */
    protected Interpolator(MeasurementTreeSet measurements, DateInterval dateSpan, TimePeriod aggregationPeriod, CounterMode counterMode, int sensorID, EventMode eventMode, StoppableThread thread, MainApplication gui) {
        super();
        this.dateSpan = dateSpan;
        this.aggregationPeriod = aggregationPeriod;
        this.counterMode = counterMode;
        this.sensorID = sensorID;
        this.eventMode = eventMode;
        this.stoppableThread = thread;
        this.measurements = measurements;
        this.gui = gui;
        this.measurmentSet = MeasurementTreeSet.empty(measurements.getUnit());        
    }

    /**
     * Invokes the interpolation process
     *
     * @return <code>true</code> if the process finishes without errors
     */
    public synchronized boolean startInterpolation() {
        logger.debug("Zeitraum: " + dateSpan.getStartDateString(HumanDateFormat) + " bis " + dateSpan.getEndDateString(HumanDateFormat));
        return performInterpolation(createDataSetFor(sensorID));
    }

    /**
     * If this sensor is a direct usage, calculate virtual counzer values and
     * return the, otherwise return a copy of the original data
     *
     * @param sensorID The sensor
     * @return A {@link MeasurementTreeSet} of raw data
     */
    private MeasurementTreeSet createDataSetFor(int sensorID) {
        if (SensorInformation.getSensorProperties(sensorID).isUsage()) {
            showMessage("Interpolator.GENERATE_COUNTER_VALUES");
            return InterpolatorHelper.generateCounterValues(this.measurements);
        }
        if (SensorInformation.getSensorProperties(sensorID).isResetCounter()) {
            showMessage("Interpolator.GENERATE_COUNTER_VALUES");
            this.measurements = InterpolatorHelper.generateAscendingCounterValues(this.measurements);
        }

        MeasurementTreeSet intervalMeasurements = MeasurementTreeSet.copyOf(this.measurements);
        
        System.out.println( "# intervalMeasurements: " + intervalMeasurements.size() );
            
        // look for negative step and if there is a counter change in it add it to the measurements, if not add a cc
        if (SensorInformation.getSensorProperties(sensorID).isCounter()) {
            // check for steps
            CounterChangeHandler ch = new CounterChangeHandler();
            changes = ch.getCounterChanges(sensorID);
            steps = searchNegativeSteps(intervalMeasurements);            
            for (CounterStep step : steps) {
                Measurement oldM = step.getOldMeasurement();
                Measurement newM = step.getNewMeasurement();
                boolean stepHasChange = false;
                for (CounterChange change : changes) {                   
                    if (change.getTime().getTime() > oldM.getTime() && change.getTime().getTime() < newM.getTime()) {
                        intervalMeasurements.add(new Measurement(change.getTime().getTime(), change.getOldValue()));
                        intervalMeasurements.add(new Measurement(change.getTime().getTime() + 1L, change.getNewValue()));
                        stepHasChange = true;
                    }
                }
                if (!stepHasChange) {
                    if (counterMode.isAutoCounterChange()) {
//                        System.out.println("Unknown step! Add change between " + new Date(step.getOldMeasurement().getTime()) + " " + new Date(step.getNewMeasurement().getTime()));
                        CounterChange change = new CounterChange();
                        change.setTime(new Date(step.getOldMeasurement().getTime() + 60000L));
                        change.setSensorID(sensorID);
                        change.setFactor(SensorInformation.getSensorProperties(sensorID).getFactor());
                        change.setOldValue(step.getOldMeasurement().getValue());
                        change.setNewValue(step.getNewMeasurement().getValue());
                        ch.addOrUpdateCounterChange(change, sensorID);
//                        System.out.println("added NEW change at " + new Date(step.getOldMeasurement().getTime() + 60000L));
                    } else {
                        // Gibt es bereits eine Entscheidung für weitere Warnungen
                        if( saveCancelDecision == true && lastSensorID == sensorID )
                        {
                            // AZ: Es ist bereits ein Fenster für diese Berechnung geöffnet. Mache also nichts mehr - MONISOFT-8
                            ;
                        }
                        else if( saveCancelDecision == true )
                        {
                            // AZ: Es wird bei einer weiteren Sensorid gewarnt
                            // Merke dir die Sensor-ID
                            lastSensorID = sensorID;
                            this.gui.showMaintenanceChart(dateSpan, sensorID, true);
                            break;
                        }
                        else
                        {
                            CounterChangeErrorDialog dialog = new CounterChangeErrorDialog(null, true, sensorID, step.getOldMeasurement().getValue(), step.getNewMeasurement().getValue(), new Date(step.getOldMeasurement().getTime()), new Date(step.getNewMeasurement().getTime()));
                            dialog.setLocationRelativeTo(this.gui.getMainFrame());
                            dialog.setVisible(true);
                            // AZ: evtl. für weitere Schritte eine automatische Entscheidung herbeiführen
                            switch (dialog.getResult()) {
                                case CounterChangeErrorDialog.CANCEL: // abgebrochen
                                    stoppableThread.running = false;
                                    this.gui.showMaintenanceChart(dateSpan, sensorID, true);
                                    return null;
                                case CounterChangeErrorDialog.CONTINUE: // fortfahren mit wert null für Intervall
                                    break;
                                case CounterChangeErrorDialog.CHANGECOUNTER: // zählerwechsel
                                    CounterChangeDialog ccd = new CounterChangeDialog(this.gui, true);
                                    ccd.setLocationRelativeTo(this.gui.getMainFrame());
                                    ccd.setFields(step.getOldMeasurement().getValue(), step.getNewMeasurement().getValue(), new Date(step.getOldMeasurement().getTime() + 60000L), SensorInformation.getSensorProperties(sensorID).getFactor(), sensorID);
                                    ccd.setVisible(true);
                                    break;
                                case CounterChangeErrorDialog.CANCEL_AND_SAVE_DECISION: // Abbrechen und für alle weitere Warnungen durchführen
                                    saveCancelDecision = true;
                                    // Merke dir die Sensor-ID
                                    lastSensorID = sensorID;
                                    this.gui.showMaintenanceChart(dateSpan, sensorID, true);
                                    break;
                            }
                        }
                    }
                    // make sure that new chnages are put into the measurements
                    changes = ch.getCounterChanges(sensorID);
                    for (CounterChange change : changes) {
                        if (change.getTime().getTime() > oldM.getTime() && change.getTime().getTime() < newM.getTime()) {
                            intervalMeasurements.add(new Measurement(change.getTime().getTime(), change.getOldValue()));
                            intervalMeasurements.add(new Measurement(change.getTime().getTime() + 1L, change.getNewValue()));
                            stepHasChange = true;
                        }
                    }
                }
            }
        }
        return intervalMeasurements;
    }

    /**
     * Perform the interpolation
     *
     * @param completeMap The {@link MeasurementTreeSet} of raw data
     * @return At the moment always returns true
     */
    private synchronized boolean performInterpolation(MeasurementTreeSet completeMap) {
        if (!stoppableThread.running) {
            return false;
        }

        logger.debug("Interpoliere " + sensorID + " Zählermodus " + counterMode);
        TimePeriod aggregationInterval = determineAggregationInterval(dateSpan, aggregationPeriod);
        showMessage("Interpolator.CALCULATING");

        durationOfValidity = InterpolatorHelper.getDurationOfValidity(sensorID);

        // MONISOFT-22: Verschiebe die Endezeit um utc plus x
        long end = dateSpan.getEndDate().getTime() + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() * 1000; // ende auf das ende des Gesamtabfrageintervalls setzen
        // long end = dateSpan.getEndDate().getTime(); // ende auf das ende des Gesamtabfrageintervalls setzen
//        System.out.println("\n\n***************Ende Abfrageintervall: " + end + " " + dateSpan.getEndDate());
        long intervalEnd, intervalStart;
        Double intervalValue;
        Measurement lastValueBeforeInterval;
        Measurement nextValueAfterInterval;
        MeasurementTreeSet intervalMeasurements;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
        
        System.out.println( "Abfrageintervall - anfang: " + simpleDateFormat.format( new Date( aggregationInterval.getStart().getTime() ) ) );        
        System.out.println( "Abfrageintervall - end: " + simpleDateFormat.format( new Date( end ) ) );
        
        // MONISOFT-22: Verschiebe die Anfangszeit um utc plus x
        while (aggregationInterval.getStart().getTime() < end && stoppableThread.running) {
            intervalStart = aggregationInterval.getStart().getTime(); // Start und Ende des Aggregationsintervalls
            intervalEnd = aggregationInterval.getEnd().getTime() + 1;
            logger.trace("+++++++ Current interval: " + new Date(intervalStart) + " " + new Date(intervalEnd));

            Measurement startM = new Measurement(intervalStart, null); // Zwei Fake Messwerte erzeugen die den Start und das Ende des Intervalls markieren
            Measurement endM = new Measurement(intervalEnd + 1, null); // "inklusive" scheint beim Ende des subintervals unten nicht zu funktionieren, daher noch erweitern

            // Extrahieren aller Messwerte innerhalb des Aggregationsintervalls
            if (completeMap.ceiling(endM) != null) { // Gibt es einen Zeitpunkt auf dem Intervallende oder danach
                intervalMeasurements = new MeasurementTreeSet(completeMap.subSet(startM, true, endM, true), completeMap.getUnit());
            } else { // es gibt keinen Wert auf dem Intervallende oder danach
                intervalMeasurements = new MeasurementTreeSet(completeMap.subSet(startM, true, endM, false), completeMap.getUnit());
            }


            // Prüfen ob es innerhalb der Toleranz vor und nach dem Interpolationsintervall Werte gibt, die noch gültig sind. Wenn ja, diese merken.
            // wie weit ist der letze Wert VOR dem Intervall entfernt, Gültigkeitsspanne inbegriffen ?
            lastValueBeforeInterval = InterpolatorHelper.getPreviousKeepValue(completeMap, startM, durationOfValidity, counterMode.obtainEdgeTolerance(aggregationInterval));
            // wie weit ist der erste Wert NACH dem Intervall entfernt, Gültigkeitsspanne inbegriffen ?
            nextValueAfterInterval = InterpolatorHelper.getNextKeepValue(completeMap, endM, durationOfValidity, counterMode.obtainEdgeTolerance(aggregationInterval));

            intervalMeasurements.setMeasurementBeforeInterval(lastValueBeforeInterval);
            intervalMeasurements.setMeasurementAfterInterval(nextValueAfterInterval);

            // unterscheiden ob es ein Event ist oder nicht
            if (SensorInformation.getSensorProperties(sensorID).isEvent()) {
                logger.trace("Interpoliere Event" + " " + sensorID + " " + "von" + " " + new Date(intervalStart) + " " + "bis" + " " + new Date(intervalEnd));
                DataHandler dh = new DataHandler(sensorID);
                intervalValue = dh.getEventStateForInterval(new Date(intervalStart), new Date(intervalEnd), eventMode).getValue();
                measurmentSet.add(new Measurement(intervalStart, intervalValue));
                remarks.addAll(dh.getRemarks()); // Eventuelle Meldungen holen
            } else { // es ist kein event
                /*
                 * Calculate the value for the interval
                 */                
                IntervalMeasurement interpolationResult = calculateIntervalValue(aggregationInterval, intervalMeasurements);

                intervalValue = (interpolationResult == null) ? null : interpolationResult.getValue();
                // System.out.println("Interpolated result " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(new Date(intervalStart)) + " " + intervalValue);

                /*
                 * Zuordnung von Wert zu Zeit
                 * ****************************************************************
                 */
                measurmentSet.add(new Measurement(intervalStart, intervalValue));
                measurmentSet.addIntervalMinMax(intervalStart, interpolationResult == null ? new MinMax() : interpolationResult.getMinmax()); // add interval internal min max values to the list
                measurmentSet.probeValue(intervalValue); // test if the interval value is a new global maximum or minimum


                // Es gab keinen Wert - Bereichsmarker setzen
                if (intervalValue == null) {
                    System.out.println( "intervalStart: " + HumanDateTimeFormat.format( intervalStart ) );
                    System.out.println( "intervalEnd: " + HumanDateTimeFormat.format( intervalEnd ) );
                    
                    System.out.println( "missingMarkerlist: " + missingMarkerList.size() );
                    
                    missingMarkerList.add(new IntervalMarker(intervalStart, intervalEnd));
                    
                }

                if (interpolationResult != null) {
                    missingTime += interpolationResult.getMissing();
                } else {
                    missingTime += (intervalEnd - intervalStart);
                }
                
                logger.trace("Interpolated result " + new SimpleDateFormat(MoniSoftConstants.HumanDateTimeFormat).format(new Date(intervalStart)) + " " + intervalValue);
            }

            intervalMeasurements.clear();
            // das nächste Aggregationintervall setzen. Wenn das gesamte Abfrageintervall (kompletter Zeitraum kommt dann als SimpleTimePeriod) gewählt wurde Abbruch provozieren
            if (aggregationInterval instanceof RegularTimePeriod) {
                aggregationInterval = ((RegularTimePeriod) aggregationInterval).next(); // nächstes Intervall setzen
            } else {
                aggregationInterval = new SimpleTimePeriod(end + 1L, end + 2L); // so setzen, dass while schleife nicht mehr durchlaufen wird (es gibt nur das eine Intervall (kompletter Zeitraum)
            }
        }
        intervalMeasurements = null;
        return true;
    }

    /**
     * Generates a {@link TimePeriod} out of the given date interval if no
     * aggregationPeriod is given<br> Otherwise return a copy of the
     * aggregationPeriod
     *
     * @param dateSpan The total date span
     * @param aggregationPeriod The aggregation interval
     * @return
     */
    private TimePeriod determineAggregationInterval(DateInterval dateSpan, TimePeriod aggregationPeriod) {
        if (aggregationPeriod != null) {
            return (RegularTimePeriod) deepCopy(aggregationPeriod);
        }
        return new SimpleTimePeriod(dateSpan.getStartDate(), dateSpan.getEndDate());
    }

    /**
     * Get the cover tolerance fomr the CounterMode odf this sensor
     *
     * @param counterMode
     * @return
     */
    protected Double obtainCoverTolerance(CounterMode counterMode) {
        return counterMode.obtainCoverTolerance();
    }

    /**
     * Display a message to the message system
     *
     * @param messageKey The message
     */
    protected void showMessage(String messageKey) {
        Messages.showMessage(ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString(messageKey) + "...\n", verbose);
    }

    /**
     * Calculats the value for an interval. if not value can be calculated
     * <code>null</code> will be returned<p> This method calls either a
     * {@link WeightedMeanInterpolator} or a {@link CounterInterpolator}
     * depending on the type of the sensor.
     *
     * @param interval Das zu betrachtende Intervall
     * @param intervalMeasurements TreeSet der Messwerte
     * @return The interval value or <code>null</code> if it could not be
     * calculated
     * @see WeightedMeanInterpolator
     * @see CounterInterpolator
     */
    private synchronized IntervalMeasurement calculateIntervalValue(TimePeriod interval, MeasurementTreeSet intervalMeasurements) {
        IntervalMeasurement result = null;
        
        // System.out.println( "Counter: " + SensorInformation.getSensorProperties(sensorID).isCounter() );
        // System.out.println( "chartType: " + Interpolator.chartType );
        
        if( Interpolator.chartType == Interpolator.TIMESERIES_TAB )
        {
            // Im Bereich Timeseries sollen die Sonderformen des Counters greifen
            if (counterMode == CounterMode.NO_COUNTER_KUMULIERTE_LEISTUNG) {
                // Berechnung der Kumulierung
                // Ermittlung welche Zeitart gewählt ist (Tag, Monat, Stunde, ...)

                // Wenn Tagesmittel dann result *= 24
                // Wenn Wochenwerte dann result *= 168
                // Wenn Minute dann result /= 60
                // Wie geht Monisoft mit den Monaten um bei 2,30 oder 31 Tagen je Monat??

                WeightedMeanInterpolator weightedMeanInterpolator = new WeightedMeanInterpolator(interval, intervalMeasurements, InterpolatorHelper.getCoverTolerance(), durationOfValidity);
                result = weightedMeanInterpolator.calculateInterval();

                // System.out.println( "Kumulierte Leistung: " + result.getValue() );   

                // Ermittle die Anzahl der Stunden im aggregationsInvervall
                double anzahlStunden = ((double)(interval.getEnd().getTime() - interval.getStart().getTime() +1)) / (3600 * 1000);

                // System.out.println( "Kumulierte Leistung: " + ( result.getValue() * anzahlStunden ) );

                if( result == null || result.getValue() == null )
                    System.out.println( "result null " + interval );
                else
                    result.setValue(result.getValue() * anzahlStunden );


                //System.out.println(  "aggregationInterval: " + interval + " result: " + 
                //    HumanDateTimeFormat.format( result.getTime() ) + " " + result.getValue() );

                remarks.addAll(weightedMeanInterpolator.getRemarks());
            } else if (counterMode == CounterMode.NO_COUNTER_ZEIGE_WERTE) {
                // Berechnung der Kumulierung
                // System.out.println( "Zeige Werte: " );
                // System.out.println( "WeightedMeanInterpolator: " );
                WeightedMeanInterpolator weightedMeanInterpolator = new WeightedMeanInterpolator(interval, intervalMeasurements, InterpolatorHelper.getCoverTolerance(), durationOfValidity);
                result = weightedMeanInterpolator.calculateInterval();
                remarks.addAll(weightedMeanInterpolator.getRemarks());
            } else if (counterMode == CounterMode.NO_COUNTER_KUMULIERUNG) {
                // Berechnung der Kumulierung
                // System.out.println( "Kumulierung: von: " + HumanDateTimeFormat.format( interval.getStart() ) + " bis: " + HumanDateTimeFormat.format( interval.getEnd() ) );
                WeightedMeanInterpolator weightedMeanInterpolator = new WeightedMeanInterpolator(interval, intervalMeasurements, InterpolatorHelper.getCoverTolerance(), durationOfValidity);
                result = weightedMeanInterpolator.calculateInterval();
                // System.out.println( "lastResult: " + lastResult );

                double anzahlStunden = ((double)(interval.getEnd().getTime() - interval.getStart().getTime() +1)) / (3600 * 1000);            
                // System.out.println( "Kumulierung: " + ( result.getValue() * anzahlStunden ) );                    

                if( lastResult == null )
                {
                    lastResult = result;
                }
                else                
                {                    
                    if( result != null && lastResult != null && result.getValue() != null && lastResult.getValue() != null )
                    {
                        try
                        {
                            result.setValue( result.getValue() * anzahlStunden + lastResult.getValue() );
                        }
                        catch( Exception e )
                        {
                            System.out.println( "WeightedMeanInterpolator: Exception: " + e.getMessage() );
                        }
                    }
                    if( result != null && lastResult != null && ( result.getValue() == null || lastResult.getValue() == null ) )
                    {
                        System.out.println( "WeightedMeanInterpolator: Exception: " );
                        System.out.println( "result.getValue(): " + result.getValue() );
                        System.out.println( "lastResult.getValue(): " + lastResult.getValue() );
                        System.out.println( "interval.getStart().getTime(): " + HumanDateTimeFormat.format( interval.getStart().getTime() ) );
                    }
                    else                    
                        lastResult = result;
                }            
                // System.out.println(  "aggregationInterval: " + interval + " result: " + 
                //    HumanDateTimeFormat.format( result.getTime() ) + " " + result.getValue() );
                remarks.addAll(weightedMeanInterpolator.getRemarks()); 
            }
            else if (counterMode == CounterMode.NOCOUNTER) {
                System.out.println( "WeightedMeanInterpolator: " );
                WeightedMeanInterpolator weightedMeanInterpolator = new WeightedMeanInterpolator(interval, intervalMeasurements, InterpolatorHelper.getCoverTolerance(), durationOfValidity);
                result = weightedMeanInterpolator.calculateInterval();
                remarks.addAll(weightedMeanInterpolator.getRemarks());
            }
            else {
                // Berechnung des Intervalls für einen Zähler
                System.out.println( "Zähler: " );
                CounterChangeHandler ch = new CounterChangeHandler();
                ArrayList<CounterChange> intervalChanges = ch.getCounterChangesFor(changes, interval.getStart().getTime(), interval.getEnd().getTime());
                try
                {
                    CounterInterpolator counterInterpolator = new CounterInterpolator(intervalChanges, interval, intervalMeasurements, counterMode, counterMode.obtainEdgeTolerance(interval), durationOfValidity, steps);
                    result = counterInterpolator.calculateInterval();
                    remarks.addAll(counterInterpolator.getRemarks());
                }
                catch( Exception e )
                {
                    e.printStackTrace();
                }
            }
        }
        else
        {
            // Alle anderen Charttypen haben die bisherigen einfachen Counter für Zähler und Kontinuierlich
            if (!SensorInformation.getSensorProperties(sensorID).isCounter()) {
                System.out.println( "WeightedMeanInterpolator: " );
                WeightedMeanInterpolator weightedMeanInterpolator = new WeightedMeanInterpolator(interval, intervalMeasurements, InterpolatorHelper.getCoverTolerance(), durationOfValidity);
                result = weightedMeanInterpolator.calculateInterval();
                remarks.addAll(weightedMeanInterpolator.getRemarks());
            }
            else if( SensorInformation.getSensorProperties(sensorID).isCounter() ){
                // Berechnung des Intervalls für einen Zähler
                System.out.println( "Zähler: " );
                CounterChangeHandler ch = new CounterChangeHandler();
                ArrayList<CounterChange> intervalChanges = ch.getCounterChangesFor(changes, interval.getStart().getTime(), interval.getEnd().getTime());
                CounterInterpolator counterInterpolator = new CounterInterpolator(intervalChanges, interval, intervalMeasurements, counterMode, counterMode.obtainEdgeTolerance(interval), durationOfValidity, steps);
                result = counterInterpolator.calculateInterval();
                remarks.addAll(counterInterpolator.getRemarks());
            }
        }
        return result;
    }

    private ArrayList<CounterStep> searchNegativeSteps(MeasurementTreeSet intervalMeasurements) {
        ArrayList<CounterStep> stepList = new ArrayList<CounterStep>();
        Integer negativeStep = 0;
        Measurement keepM = new Measurement(null, Double.MIN_VALUE);        
        for (Measurement m : intervalMeasurements) {
            if (m.getValue() < keepM.getValue()) {        
                negativeStep++;
                stepList.add(new CounterStep(keepM, m));
            }            
            keepM = m;            
        }
        return stepList;
    }

    /**
     * Returns the result of the interpolation
     *
     * @return A copy of the resulting {@link MeasurementTreeSet}
     */
    public MeasurementTreeSet getInterpolatedSet() {
        return MeasurementTreeSet.copyOf(measurmentSet);
    }

    /**
     * Return generated markers for missing intervals
     *
     * @return A list of makers generated for intervals that could not be
     * calculated
     */
    public ArrayList<IntervalMarker> getMissingMarkers() {
        return new ArrayList<IntervalMarker>(missingMarkerList);
    }

    /**
     * Return the duration during which no values could be calculated
     *
     * @return The duration in ms
     */
    public Long getMissingTime() {
        return missingTime;
    }

    /**
     * Return any generated remarks
     *
     * @return A list of remarks that were generated during calculation
     */
    public ArrayList<String> getRemarks() {
        int missingNumber = missingMarkerList.size();
        if (missingNumber > 0) {
            remarks.add(missingNumber + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("Interpolator.INTERVALS_WITHOUT_VALUES") + "\n");
        }
        return remarks;
    }

    /**
     * Return the sonor of this interpolation
     *
     * @return The sensor ID
     */
    public int getSensorID() {
        return sensorID;
    }

    /**
     * Make a copy of a TimePeriod AND all its elements
     *
     * @param period The period to copy
     * @return The copy
     */
    private TimePeriod deepCopy(TimePeriod period) {

        // serialize ArrayList into byte array
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(period);
            byte buf[] = baos.toByteArray();
            oos.close();

            // deserialize byte array into ArrayList
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            TimePeriod newPeriod = (TimePeriod) ois.readObject();
            ois.close();

            return newPeriod;
        } catch (Exception e) {
            Messages.showException(e);
        }
        return null;
    }

    /**
     * Toggle verbose mode
     *
     * @param b If <code>true</code> more debug output will be generated
     */
    public void setVerbose(boolean b) {
        verbose = b;
    }
}
