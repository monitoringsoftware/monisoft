package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import de.jmonitoring.DataHandling.DataFilter.ValueFilter;
import de.jmonitoring.DataHandling.Interpolators.InterpolatorHelper;
import de.jmonitoring.DataHandling.Interpolators.WeightedMeanInterpolator;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.utils.intervals.CustomMinutePeriod;
import de.jmonitoring.utils.intervals.DateInterval;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import net.sourceforge.jeval.EvaluationException;
import net.sourceforge.jeval.Evaluator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;

/**
 * This class is the main class for manipulating data sets, filtering and interpolating
 * values
 *
 * @author togro
 */
public class DatasetWorker {

    private int sensorID;
    private ArrayList<IntervalMarker> markers = new ArrayList<IntervalMarker>(1024);
    private ArrayList<IntervalMarker> filterMarkers = new ArrayList<IntervalMarker>(1024);
    private Long missingTime = 0L;
    private Double minValue = null;
    private Double maxValue = null;
    private ReferenceValue reference = null;
    private Long timeReference = null;
    private EventMode eventMode = EventMode.EVENT_SIGNIFICANT_MODE;
    private ValueFilter valueFilter = null;
    private String remark = "";
    private Stack<Byte> dayList = new Stack<Byte>();
    private HashMap<Byte, Integer> dayMap = new HashMap<Byte, Integer>(7);
    private HashMap<Integer, String> dayString = new HashMap<Integer, String>(7);
    private byte dayCode = 0;
    private Integer fromHour = 0;
    private Integer toHour = 24;
    private long startConstraint = Long.MIN_VALUE, endConstraint = Long.MAX_VALUE;
    public static final byte SHOW_ALL_DAYS = 0;
    public static final byte SHOW_WEEKEND_ONLY = 31;
    public static final byte SHOW_WORKDAY_ONLY = 96;
    public static final byte SKIP_MONDAY = 1;
    public static final byte SKIP_TUESDAY = 2;
    public static final byte SKIP_WEDNESDAY = 4;
    public static final byte SKIP_THURSDAY = 8;
    public static final byte SKIP_FRIDAY = 16;
    public static final byte SKIP_SATURDAY = 32;
    public static final byte SKIP_SUNDAY = 64;
    private ArrayList<Long> markerToggleList = null;
    private boolean verbose = false;
    private final MainApplication gui;
    private boolean formulaContainsX = true;
    private boolean isVirtual = false;
    

    /**
     * Create a new instance for the given sensor
     *
     * @param id The sensorID
     * @param gui The GUI to use
     */
    public DatasetWorker(int id, MainApplication gui) {
        sensorID = id;
        this.gui = gui;
        initDayFilterLists();
    }

    /**
     * Returns interpolated values for the given time period.<p> Virtual sensor
     * will be calculated based their components.<br> if this
     * {@link DatasetWorker} was assigned a {@link ReferenceValue} it will be
     * applied Gibt interpolierte Werte für einen Messpunkt zurück. Virtuelle
     *
     * @param dateInterval The time interval
     * @param period The aggregation/interpolation interval
     * @param mode The counter mode (null if sensor is no counter)
     * @return A {@link MeasurementTreeSet} containing the {@link Measurement}s.
     */
    public synchronized MeasurementTreeSet getInterpolatedData(DateInterval dateInterval, TimePeriod period, CounterMode mode) {
        MeasurementTreeSet dataSet;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
                    
        if (SensorInformation.getSensorProperties(sensorID).isVirtual()) { // es handelt sich um einen virtuellen Messpunkt -> aus Komponenten zusammenbauen
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("js");
            isVirtual = true;
            
            String virtualDefinition = SensorInformation.getSensorProperties(sensorID).getVirtualDefinition();
            
            if( virtualDefinition != null && virtualDefinition.contains( "o:s" ) )
            {
                // AZ: Virtuelle Zähler für andere Zeiteinheiten - MONISOFT-17
                // Mach das FOREACH weg
                virtualDefinition = virtualDefinition.replace("o:s", "" );
                // Erstelle eine Liste der verwendeten ids
                TreeSet<Integer> components = SensorInformation.getVirtualComponents(virtualDefinition, SensorInformation.getSensorProperties(sensorID).getSensorName()); // id der Komponenten
                
                // Erstelle die Virtuale Definition mit IDs
                virtualDefinition = SensorInformation.replaceVirtualDefinitionIds(virtualDefinition); // holen der definition in der alle enthaltenen Messpunkte durch ihre definition ersetzt sind
                SensorInformation.resetFinalDefinition(); // zurcküsetzen der definition
                if (components == null) { // Eine der Komponenten war nicht vorhanden
                    return null;
                }

                Map<Integer, MeasurementTreeSet> allDataSetComponents = new TreeMap<Integer, MeasurementTreeSet>();


                int maxInterval = -1;
                
                // Suche das Maximum der Intervall-Werte, der im im Skript enthaltenen Sensoren
                for (Integer componentID : components)
                {
                    Logger.getLogger(DatasetWorker.class.getName()).log(Level.INFO, null, "componentId: " + componentID );
                    
                    SensorProperties sensorProperties = SensorInformation.getSensorProperties(componentID);
                    
                    Integer interval = sensorProperties.getInterval();
                    
                    Logger.getLogger(DatasetWorker.class.getName()).log(Level.INFO, "Interval: " + interval, new Exception("Test") );
                
                    if( interval > maxInterval )
                        maxInterval = interval;
                }
                
                Logger.getLogger(DatasetWorker.class.getName()).log(Level.INFO, "maxInterval: " + maxInterval, new Exception("Test") );

                if( maxInterval == 0 )
                {
                    maxInterval = 5;
                }
                
                Logger.getLogger(DatasetWorker.class.getName()).log(Level.INFO, null, "maxInterval: " + maxInterval );
                    
                // Lade die Daten für alle Komponenten
                for (Integer componentID : components)
                {
                    MeasurementTreeSet dataSetComponent = MeasurementTreeSet.empty(SensorInformation.getSensorProperties(sensorID).getSensorUnit());
                    // dataSetComponent = dataQueryForeach(dateInterval.getStartDate(), dateInterval.getEndDate(), period, mode, componentID);                    
                    
                    // Ziel sind 5-Min-Werte
                    // Period period = CustomMinutePeriod();                    
                    RegularTimePeriod fiveMinPeriod = GeneralDataSetGenerator.getPeriodForTimeStamp(maxInterval, dateInterval.getStartDate().getTime());
                    
                    dataSetComponent = dataQuery(dateInterval, fiveMinPeriod, mode, componentID);
                
                    // Durchlaufe das MeasurementTreeSet und baue daraus interpoliere Werte
                    
                    allDataSetComponents.put( componentID, dataSetComponent);
                }

                // Schleife über die erste ID
                // Verwende die erste Map und merke dir die ID der Komponente
                Integer firstComponentID = null;
                Integer secondComponentID = null;
                Integer thirdComponentID = null;
                Integer fourthComponentID = null;

                MeasurementTreeSet firstMeasurementTreeSet = null;
                MeasurementTreeSet secondMeasurementTreeSet = null;
                MeasurementTreeSet thirdMeasurementTreeSet = null;
                MeasurementTreeSet fourthMeasurementTreeSet = null;
                
                boolean first = true;
                boolean second = true;
                boolean third = true;
                boolean fourth = true;
                
                for( Map.Entry<Integer, MeasurementTreeSet> entry : allDataSetComponents.entrySet() )
                {
                    if( first )
                    {
                        firstComponentID = entry.getKey();
                        firstMeasurementTreeSet = entry.getValue();
                        first = false;
                    }
                    else if( second )
                    {
                        secondComponentID = entry.getKey();
                        secondMeasurementTreeSet = entry.getValue();
                        second = false;
                    }
                    else if( third )
                    {
                        thirdComponentID = entry.getKey();
                        thirdMeasurementTreeSet = entry.getValue();
                        third = false;
                    }
                    else if( fourth )
                    {
                        fourthComponentID = entry.getKey();
                        fourthMeasurementTreeSet = entry.getValue();
                        fourth = false;
                    }
                }
                
                // Die Ergebnisliste definieren
                dataSet = MeasurementTreeSet.empty(SensorInformation.getSensorProperties(sensorID).getSensorUnit());
                
                // Durchlaufe das erste Set
                for( Measurement measurement : firstMeasurementTreeSet )
                {   
                    // Hole dir den passenden Wert des 2. Zählers
                    Double secondValue = null;
                    if( secondMeasurementTreeSet != null )
                        secondValue = secondMeasurementTreeSet.getValueForTime( measurement.getTime() );
                    
                    Double thirdValue = null;
                    if( thirdMeasurementTreeSet != null )
                        thirdValue = thirdMeasurementTreeSet.getValueForTime( measurement.getTime() );
                                        
                    Double fourthValue = null;
                    if( fourthMeasurementTreeSet != null )
                        fourthValue = fourthMeasurementTreeSet.getValueForTime( measurement.getTime() );
                    
                    String resultDefinition = virtualDefinition;
                    
                    if( measurement.getValue() != null )
                        resultDefinition = resultDefinition.replace("[" + firstComponentID + "]", "" + measurement.getValue() );
                    
                    if( secondValue != null )
                        resultDefinition = resultDefinition.replace("[" + secondComponentID + "]", "" + secondValue );
                    
                    if( thirdValue != null )
                        resultDefinition = resultDefinition.replace("[" + thirdComponentID + "]", "" + thirdValue );
                    
                    if( fourthValue != null )
                        resultDefinition = resultDefinition.replace("[" + fourthComponentID + "]", "" + fourthValue );
                    
                    // Ersetze die Blockklammern
                    // resultDefinition = resultDefinition.replaceAll( "\\]", "" );
                    
                    // Wende die Foreach-Formel jetzt an und dann soll das Ergebnis in die ComposedData rein
                    Object result = null;
                    
                    try {
                        // Falls die Formel nicht aufgelöst werden konnte
                        if( resultDefinition.contains( "[" ) || resultDefinition.contains( "]" ) )
                        {   
                            /*
                            Logger.getLogger(DatasetWorker.class.getName()).log(Level.SEVERE, null, 
                                new Exception( "Formel konnte nicht aufgelöst werden: " + resultDefinition + 
                                    " Zeitpunkt: " + simpleDateFormat.format( dateInterval.getStartDate() ) ) );
                            */
                            System.out.println( "Formel konnte nicht aufgelöst werden: " + resultDefinition + " Zeitpunkt: " + 
                                    simpleDateFormat.format( measurement.getTime() ) );
                        }
                        else
                            result = engine.eval( resultDefinition );
                    } catch (ScriptException ex) {
                        Logger.getLogger(DatasetWorker.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    if( result != null )
                    {
                        Double resultDouble = null;
                                
                        try
                        {
                            if( result instanceof Integer )
                            {
                                int resultInt = (Integer) result;
                                resultDouble = (double) resultInt;                                
                            }
                            else
                                resultDouble = (Double) result;
                        }
                        catch( Exception ex )
                        {
                            Logger.getLogger(DatasetWorker.class.getName()).log(Level.SEVERE, null, ex);                            
                        }
                        
                        // Prüfen, ob der Datensatz in das Ergebnis kommen soll
                        Measurement newMeasurement = new Measurement( measurement.getTime(), resultDouble );
                        dataSet.add( newMeasurement );
                    }
                }
                
                Interpolator interpolator = new Interpolator(dataSet, dateInterval, period, mode, sensorID, eventMode, this.gui);
                interpolator.setVerbose(verbose);

                boolean state = interpolator.startInterpolation();
                
                if (state) {
                    for (String s : interpolator.getRemarks()) {
                        remark += "(" + SensorInformation.getDisplayName(sensorID) + ") " + s;
                    }

                    MeasurementTreeSet measurements = interpolator.getInterpolatedSet();
                    markers = interpolator.getMissingMarkers();
                    missingTime = interpolator.getMissingTime();
                    minValue = measurements.getMinimumOfIntervals();
                    maxValue = measurements.getMaximumofIntervals();

                    trimDataMap(measurements, dateInterval); // Beschneidet den übergebenen Datensatz auf die im Anfrageintervall gewünschte Zeitspanne
                    
                    dataSet.clear();
                    dataSet.addAll(measurements);                     
                } else {
                    dataSet.clear();
                }                
            }
            else
            {                
                TreeSet<Integer> components = SensorInformation.getVirtualComponents(virtualDefinition, SensorInformation.getSensorProperties(sensorID).getSensorName());
                String finalDefinition = SensorInformation.getFinalDefinition(); // holen der definition in der alle enthaltenen Messpunkte durch ihre definition ersetzt sind
                SensorInformation.resetFinalDefinition(); // zurcküsetzen der definition
                if (components == null) { // Eine der Komponenten war nicht vorhanden
                    return null;
                }
                // AZ: Variante für AND und OR-Verknüpfung einbauen
                // Der Standardfall ist, wenn eine der Komponenten im Intervall fehlt, gilt das ganze Intervall als null                        
                if( finalDefinition.contains("o:c"))
                {
                    formulaContainsX = false;
                    finalDefinition = finalDefinition.replaceAll("o:c", "");
                }        

                HashMap<Integer, TreeSet<Measurement>> dataMapCollector = new HashMap<Integer, TreeSet<Measurement>>(); // ein Collector für die erzeugten Ergebnismengen
                for (Integer componentID : components) {
                    dataSet = dataQuery(dateInterval, period, mode, componentID);
                    dataMapCollector.put(componentID, dataSet);
                }
                dataSet = getComposedData(dataMapCollector, finalDefinition);
            }
        } else { // kein virtueller Messpunkt
            isVirtual = false;
            dataSet = dataQuery(dateInterval, period, mode, sensorID);
        }

        if (dataSet != null) {
            if (dayCode != 0 || fromHour > 0 || toHour < 24 || startConstraint > Long.MIN_VALUE || endConstraint < Long.MAX_VALUE) { // keine Tage gewählt oder keine Uhrzeitbeschränkung -> alles gilt
                applyWeekDayTimeFilter(dataSet);
            }

            if (valueFilter != null) { // wenn ein Wertefilter definiert ist, die Serie filtern
                Double minTMP = minValue;
                Double maxTMP = maxValue;
                makeValueFilteredSeries(dataSet, dateInterval, period, valueFilter.isRemove());
                minValue = minTMP;
                maxValue = maxTMP;
            }

            // eine eventuell vorhandene Referenz anbringen
            if ((reference != null && reference.getValue() > 0d) || (timeReference != null && timeReference > 0)) { // Wenn ein Referenzwert vorhanden ist diesen anbingen
                makeSpecficSeries(dataSet, period); // TODO min/max anpassen?
            }
        }
        return dataSet;
    }

    /**
     * Set a code for a day filter
     *
     * @param code the code to use
     */
    public void setDayFilterCode(byte code) {
        dayCode = code;
    }

    /**
     * Sets the timefilter for daytime
     *
     * @param from The start hour
     * @param to The end hour
     */
    public void setTimeFilter(Integer from, Integer to) {
        fromHour = from;
        toHour = to;
    }

    /**
     * Sets date constrainfs
     *
     * @param start The start date
     * @param end The end date
     */
    public void setDateConstraints(long start, long end) {
        startConstraint = start;
        endConstraint = end == Long.MAX_VALUE ? end : end + 86400000L;  // eine Tag an Ende hinzufügen, damit auch der letzte gewünschte Tag komplett erscheint)
    }

    /**
     * Set a reference value
     *
     * @param ref The reference value
     * @see ReferenceValue
     */
    public void setReference(ReferenceValue ref) {
        reference = ref;
    }

    /**
     * Set a tie reference (e.g. per year etc.)
     *
     * @param timeRef The time
     */
    public void setTimeReference(Long timeRef) {
        timeReference = timeRef;
    }

    /**
     * Set the event mode
     *
     * @param mode The eventmode
     * @see EventMode
     */
    public void setEventMode(EventMode mode) {
        eventMode = mode;
    }

    /**
     * set a value filter
     *
     * @param The filter
     * @see ValueFilter
     */
    public void setValueFilter(ValueFilter f) {
        this.valueFilter = f;
    }

    /**
     * Return the minimum value of the query
     *
     * @return The min value
     */
    public Double getMinValue() {
        return minValue;
    }

    /**
     * Return the maximum value of the query
     *
     * @return The max value
     */
    public Double getMaxValue() {
        return maxValue;
    }

    /**
     * Return any markers that were generated
     *
     * @return A list of the markers
     */
    public ArrayList<IntervalMarker> getMarkers() {
        return markers;
    }

    /**
     * Return markers that were generated by filters
     *
     * @return A list of the markers
     */
    public synchronized ArrayList<IntervalMarker> getFilterMarkers() {
        // Filtermarker bauen
        if (markerToggleList == null) {
            return null;
        }

        Iterator<Long> it = markerToggleList.iterator();
        Long start = null, end = null;
        while (it.hasNext()) {
            start = it.next();
            if (it.hasNext()) {
                end = it.next();
            } else {
                end = new Date().getTime();
            }
            filterMarkers.add(new IntervalMarker(start.doubleValue(), end.doubleValue(), Color.RED));
        }
        return filterMarkers;
    }

    /**
     * return the duration of the time during which no values where present
     *
     * @return The duration
     */
    public long getMissingTime() {
        return missingTime;
    }

    /**
     * Return any generated remarks
     *
     * @return Te remarks
     */
    public String getRemark() {
        return remark;
    }

    public void setVerbose(boolean b) {
        verbose = b;
    }

    /**
     * Applies any factors to the data points.<p> Takes into consideration the
     * native factor of the sensor (in the sensor list) and any factor changes
     * during the query time
     *
     * @param dataSet The dataset to manipulate
     * @param id The sensorID (if not given the id of this {@link DatasetWorker}
     * is taken.
     */
    private synchronized void applyFactors(MeasurementTreeSet dataSet, Integer id) {
        int useID;
        if (id == null) {
            useID = sensorID;
        } else {
            useID = id;
        }

        Long time;
        TreeMap<Long, Double> factorMap = SensorInformation.getFactorList(useID);
        Double factor = SensorInformation.getSensorProperties(useID).getFactor(); // den Standardfaktor des Messpunkts holen
        if (factorMap.isEmpty()) {
            if (factor != 1d) {                            // Es gibt keine weiteren Faktoren für diesen Messpunkt -> den Standardfaktor verwenden wenn dieser <> 1 ist.
                if (MoniSoft.getInstance().isGUI() && this.gui.isLogCalculation()) {
                    Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CALC_FACTOR") + " " + factor + "\n", verbose);
                }
                for (Measurement measurement : dataSet) {
                    measurement.setValue(measurement.getValue() * factor);
                }
            }
        } else { // es gibt mehrere Faktoren
            for (Measurement measurement : dataSet) {
                time = measurement.getTime();
                while (!factorMap.isEmpty() && time >= factorMap.firstKey()) {                  // solange die Zeit noch vor dem ersten Faktorwechsel ist diesen anbringen und fortfahren
                    factor = factorMap.firstEntry().getValue();
                    if (MoniSoft.getInstance().isGUI() && this.gui.isLogCalculation()) {
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CALC_FACTOR") + " " + factor + " ab " + new Date(factorMap.firstKey()) + "\n", true);
                    }
                    factorMap.remove(factorMap.firstKey());
                }
//                System.out.println("Ab " + new Date(time) + " " + factor + "===" + measurement.getValue() * factor);
                measurement.setValue(measurement.getValue() * factor);
            }
        }
    }

    /**
     * Eliminates counter value 0
     *
     * @param dataSet The data to manipulate
     */
    private synchronized void removeInvalidCounterValues(MeasurementTreeSet dataSet) {
        for (Iterator<Measurement> it = dataSet.iterator(); it.hasNext();) {
            Measurement measurement = it.next();
            // System.out.println( "measurement: " + measurement.getTime() + " Value: " + measurement.getValue() );
            if ((measurement.getValue() == 0)) {
                it.remove();
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IGNORE_COUNTER_0") + "\n", verbose);
            }
        }
    }

    /**
     * Calculates a specific value with the {@link ReferenceValue} and time
     * reference (if given) of this {@link DatasetWorker}
     *
     * @param dataSet The dataset to manipulate
     * @param period The time period (if <code>null</code> it will not be used
     */
    private synchronized void makeSpecficSeries(TreeSet<Measurement> dataSet, TimePeriod period) {
        Long timeValue = timeReference;
        Double timeFactor = 1d;
        Double refFactor = 1d;

        if (timeReference != null) {
            Long intervalLength = (period.getEnd().getTime() - period.getStart().getTime()) + 1L;
            timeFactor = (double) timeValue / intervalLength;
        }

        if (reference != null) {
            refFactor = 1d / reference.getValue();
        }

        for (Measurement measurement : dataSet) {
            measurement.setValue((measurement == null || measurement.getValue() == null) ? null : measurement.getValue() * refFactor * timeFactor);
        }
        minValue = minValue * refFactor * timeFactor;
        maxValue = maxValue * refFactor * timeFactor;
    }

    /**
     * Filters the given dataset by the values of other sensors<p> It uses the
     * {@link ValueFilter} that was set for this {@link DatasetWorker}.
     *
     * @param dataSet The datset
     * @param dateInterval The date interval
     * @param period The aggregation/interpolation interval
     * @param remove If <code>true</code> the values will be romoved from the
     * dataset otherwise only markers will be generated for the relevant
     * timestamps
     */
    private synchronized void makeValueFilteredSeries(TreeSet<Measurement> dataSet, DateInterval dateInterval, TimePeriod period, boolean remove) {
        HashMap<Integer, MeasurementTreeSet> filterMapCollection = new HashMap<Integer, MeasurementTreeSet>(16); // eine Liste der Daten-Maps mit der SensorID als Key
        HashMap<Integer, String> variables = valueFilter.getVariables();
        String filterString = valueFilter.getValueFilterString();
        String filterStringDisplay = valueFilter.getVaulueFilterDisplayString();
        Evaluator e = new Evaluator();
        Integer sensorID_TMP = sensorID; // merken der aktuellen ID
        Integer removeCountNoValue = 0;
        Integer removeCount = 0;
        Integer sum = dataSet.size();
        DecimalFormat f = new DecimalFormat("0.0");
        Iterator<Integer> it = variables.keySet().iterator();

        while (it.hasNext()) { // Werte für alle Filter-Messpunkte holen und in er ArrayList speichern
            sensorID = it.next();
            DatasetWorker dw = new DatasetWorker(sensorID, this.gui);
            dw.setDayFilterCode(dayCode);
            dw.setDateConstraints(startConstraint, endConstraint);
            filterMapCollection.put(sensorID, dw.getInterpolatedData(dateInterval, period, CounterMode.NOCOUNTER)); // TODO NOCOUNTER ??????
        }

        long time;
        boolean b;
        Double value;
        markerToggleList = new ArrayList<Long>(1024);
        boolean markerON = false;
        Iterator<Measurement> dataSetIT = dataSet.iterator();
        Measurement m;
        while (dataSetIT.hasNext()) { // Zeitpunkte des zu filternden Messpunkts durchlaufen
            m = dataSetIT.next();
            time = m.getTime();
            // alle Werte aus den Maps zu diesem Zeitpunkt holen
            e.clearVariables();

            for (Integer filterSensorID : filterMapCollection.keySet()) {
                value = filterMapCollection.get(filterSensorID).getValueForTime(time);
                if (value != null) {
                    e.putVariable("V" + filterSensorID.toString(), Double.toString(value));
                }
            }

            try {
                b = e.getBooleanResult(filterString);
                if (!b) { // wenn Bedingung nicht erfüllt denn zu filternden Messwert löschen bzw. merkierung ausschalten
                    if (remove) {
                        dataSetIT.remove();
                    }
                    if (markerON) { // Marker einschalten
                        markerON = false;
                        markerToggleList.add(time);
                    }
                    removeCount++;
                } else {
                    if (!markerON) { // Marker ausschalten
                        markerON = true;
                        markerToggleList.add(time);
                    }
               }
            } catch (Exception ex) { // Fehler bei der Auswertung -> vermutlich war einer der Werte null -> entfernen da Bedingung ebenfalls nicht erfüllt
                if (remove) {
                    dataSetIT.remove();
                }
                removeCountNoValue++;
            }

        }
        
        // AZ: Die Anzahl der markierten Werte ist angepasst - MONISOFT-14
        removeCount = sum - (removeCount + removeCountNoValue);
        
        Double percent = ((double) (removeCount + removeCountNoValue) / (double) sum) * 100d;
        Double noValuePercent = ((double) (removeCountNoValue) / (double) sum) * 100d;

        if (remove) {
            remark += SensorInformation.getDisplayName(sensorID_TMP) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("FILTERED_WITH") + " (" + filterStringDisplay + "). " + (removeCount + removeCountNoValue) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("OF") + " " + sum + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("VALUES_DEL") + " (" + f.format(percent) + "%)";
        } else {
            remark += java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("PERIOD_MARKER") + " (" + filterStringDisplay + "). " + (removeCount + removeCountNoValue) + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("OF") + " " + sum + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("INTERVALS_MARKED") + " (" + f.format(percent) + "%)";
        }
        if (removeCountNoValue > 0) {
            remark += " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("THEREOF") + " " + removeCountNoValue + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("WITHOU_FILTER_VALUE") + " (" + f.format(noValuePercent) + "%)";
        }
        remark += "\n";
        sensorID = sensorID_TMP; // id wieder zurücksetzen
    }

    /**
     * Invokes the database query for a sensor and returns a
     * {@link MeasurementTreeSet} of the gathered data.<p> If the
     * aggrepagion/interpolation interval is
     * <code>null</code>, raw data will be returned, otherwise the data is
     * interpolated in the specified interval periods.
     *
     * @param dateInterval The date interval for the query
     * @param period The aggregation/interppolation interval
     * @param mode Mode for counter handling
     * @param id sensor id
     * @return A {@link MeasurementTreeSet} of the gathered data
     * @see MeasurementTreeSet
     * @see CounterMode
     * @see TimePeriod
     */
    private synchronized MeasurementTreeSet dataQuery(DateInterval dateInterval, TimePeriod period, CounterMode mode, int id) {
        DatabaseQuery dq = new DatabaseQuery(id);
        dq.setVerbose(verbose);
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
        
        System.out.println( "Verschiebe das DateInterval um " + SensorInformation.getSensorProperties(sensorID).getUtcPlusX() + " Sekunden" );
        
        Calendar newStartDateCal = Calendar.getInstance();        
        newStartDateCal.setTime( dateInterval.getStartDate() );
        long utcPlusX = 0;
        if( SensorInformation.getSensorProperties(sensorID).getUtcPlusX() != null )
            utcPlusX = SensorInformation.getSensorProperties(sensorID).getUtcPlusX();
        newStartDateCal.add( Calendar.SECOND, new Long( utcPlusX ).intValue() * -1 );        
        dateInterval.setStartDateComplete( newStartDateCal.getTime() );
        
        Calendar newEndDateCal = Calendar.getInstance();
        newEndDateCal.setTime( dateInterval.getEndDate() );
        newEndDateCal.add( Calendar.SECOND, new Long( utcPlusX ).intValue() * -1 );        
        dateInterval.setEndDateComplete( newEndDateCal.getTime() );
        
        System.out.println( "startDate: " + simpleDateFormat.format( dateInterval.getStartDate() ) );
        System.out.println( "endDate: " + simpleDateFormat.format( dateInterval.getEndDate() ) );
        
        MeasurementTreeSet measurements = dq.simpleQueryResult(dateInterval, false);

        // Den Wert der auf das nächste Abfrageintervall folgt holen und Map ergänzen, damit mit diesem Wert am Ende interpoliert werden kann wenn nötig.
        DataHandler dh = new DataHandler(id);
        Measurement nextMeasurement = dh.getNextDBEntry(null, dateInterval.getEndDate());
        if (nextMeasurement != null) {
            measurements.add(nextMeasurement);
            // System.out.println( "nextMeasurement: " + nextMeasurement.getTime() + " = " + nextMeasurement.getValue() );
        }
        
        // System.out.println( "# measurements: " + measurements.size() + " start: " + simpleDateFormat.format( dateInterval.getStartDate() ) + " end: " + simpleDateFormat.format( dateInterval.getEndDate() ) );

        Measurement prevMeasurement = dh.getPreviousDBEntry(null, dateInterval.getStartDate());
        if (prevMeasurement != null) {
            measurements.add(prevMeasurement);
            System.out.println( "prevMeasurement: " + prevMeasurement.getTime() + " = " + prevMeasurement.getValue() );
        }
        
        if (SensorInformation.getSensorProperties(id).isCounter()) { // ein Zähler
            if (SensorInformation.getSensorProperties(id).isResetCounter()) { // wenn es ein rücksetzender Messpunkt ist diesen "normalisieren"
            } else {
                removeInvalidCounterValues(measurements);
            }
        }

        applyFactors(measurements, id); // eventuelle Faktoren anbringen

        if (period != null) { // wenn kein Aggregationsintervall gewählt: die Rohwerte zurückgeben sonst interpolieren
            try {
                if (period instanceof SimpleTimePeriod) {
                    period = null;
                }
                // Interpolation anwerfen
                Interpolator interpolator = new Interpolator(measurements, dateInterval, period, mode, id, eventMode, this.gui);
                interpolator.setVerbose(verbose);

                boolean state = interpolator.startInterpolation();
                if (state) {
                    for (String s : interpolator.getRemarks()) {
                        remark += "(" + SensorInformation.getDisplayName(id) + ") " + s;
                    }

                    measurements = interpolator.getInterpolatedSet();                    
                    // Füge die Intervallmarker aller Bestandteile der Formel hinzu
                    if( isVirtual )
                    {
                        System.out.println( "# Markers: " + markers.size() );
                        markers.addAll(interpolator.getMissingMarkers());
                        System.out.println( "# Markers: " + markers.size() );
                    }
                    else
                    {
                        // bisheriges Vorgehen
                        markers = interpolator.getMissingMarkers();
                    }
                    missingTime = interpolator.getMissingTime();
                    minValue = measurements.getMinimumOfIntervals();
                    maxValue = measurements.getMaximumofIntervals();

                    trimDataMap(measurements, dateInterval); // Beschneidet den übergebenen Datensatz auf die im Anfrageintervall gewünschte Zeitspanne
                } else {
                    measurements = null;
                }
            } catch (Exception ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            }
        }
        return measurements;
    }
    
    private synchronized MeasurementTreeSet dataQueryForeach(Date startDate, Date endDate, TimePeriod period, CounterMode mode, int id) {
        DatabaseQuery dq = new DatabaseQuery(id);
        dq.setVerbose(verbose);
        MeasurementTreeSet measurements = dq.simpleQueryResultForDate(startDate, endDate, false);
        return measurements;
    }

    /**
     * Filters the dataset by week days<p> This method uses a code for each week
     * day (see method
     * <code>initDayFilterLists()</code>)
     *
     * @param dataSet The dataset
     *
     */
    private synchronized void applyWeekDayTimeFilter(TreeSet<Measurement> dataSet) {
        GregorianCalendar cal = new GregorianCalendar();
        GregorianCalendar beforeCal = new GregorianCalendar();
        GregorianCalendar afterCal = new GregorianCalendar();
        beforeCal.setTimeInMillis(startConstraint);
        afterCal.setTimeInMillis(endConstraint);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        ArrayList<Integer> daysToFilter = new ArrayList<Integer>();
        byte day;
        byte code = dayCode;
        String dayfilterString = "";
        String sep = "";
        // eine Liste der zu ignorierenden Tage erstellen (als Calendar-days) anhand des Codes
        while (code > 0 && !dayList.empty()) {
            day = dayList.pop();
            if (code - day >= 0) {
                code = (byte) (code - day);
                daysToFilter.add(dayMap.get(day));
                dayfilterString = dayString.get(dayMap.get(day)) + sep + dayfilterString;
                sep = ",";
            }
        }

        dayfilterString = dayfilterString.equals("") ? "" : java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IGNORE_DAY") + " " + " " + dayfilterString;
        remark += dayfilterString;

        String timeFilterRemark = "";
        for (Iterator<Measurement> it = dataSet.iterator(); it.hasNext();) {
            Measurement measurement = it.next();
            cal.setTimeInMillis(measurement.getTime());
            if (cal.before(beforeCal) || cal.after(afterCal) || cal.get(Calendar.HOUR_OF_DAY) < fromHour || cal.get(Calendar.HOUR_OF_DAY) > toHour || daysToFilter.contains(cal.get(Calendar.DAY_OF_WEEK))) {
                if (fromHour != 0 && toHour != 24) {
                    timeFilterRemark = java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("SHOW_ONLY") + " " + fromHour + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("AND") + " " + toHour + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CLOCK");
                }
                it.remove();
            }
        }

        remark += timeFilterRemark.equals("") ? "" : "\n" + timeFilterRemark;
    }

    /**
     * Calculation os virtual sensors<p> Calculates a resulting virtual value
     * out of the given formula and the dataset collection, holding the values
     * of the formulas components
     *
     * @param dataSetCollection The collection of datasets for the required
     * sensors
     * @param formula The virtual formula
     * @return A {@link MeasurementTreeSet} of the resulting values
     */
    private synchronized MeasurementTreeSet getComposedData(HashMap<Integer, TreeSet<Measurement>> dataSetCollection, String formula) {
        MeasurementTreeSet dataSet = MeasurementTreeSet.empty(SensorInformation.getSensorProperties(sensorID).getSensorUnit());
        Evaluator evaluator = new Evaluator();
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CREATE_VIRTUAL") + " " + SensorInformation.getSensorProperties(sensorID).getSensorName() + " = " + formula + "\n", verbose);
        Double min = Double.MAX_VALUE;
        Double max = -Double.MAX_VALUE;

        // Messpunktnamen durch ID ersetzen
        Pattern p = Pattern.compile("\\[[^\\[\\]]*\\]");
        Matcher m = p.matcher(formula);
        int lengthDiff = 0;
        int oldLength = formula.length();
        while (m.find()) {
            String f = formula.substring(m.start() - lengthDiff, m.end() - lengthDiff);
            f = f.substring(1, f.length() - 1);
            formula = formula.replaceFirst(f, String.valueOf(SensorInformation.getSensorIDFromNameORKey(f)));
            lengthDiff = oldLength - formula.length();
        }
        // Formel für jeval umbauen, d.h. [ durch #{V bzw. ] durch }  ersetzen
        formula = formula.replaceAll("\\[", "#{V"); // Test: immer ein V davor, falls der Messpunkt mit einer Zahl beginnt
        formula = formula.replaceAll("\\]", "}");
        Iterator<Integer> IDIterator = dataSetCollection.keySet().iterator(); // Iterator für die MesspunktIDs
        MeasurementTreeSet measurements = (MeasurementTreeSet) dataSetCollection.get(IDIterator.next()); // Iterator für die Zeitpunkte (genommen von der ersten map)

        long time;
        int id;
        Double d;
        boolean makeNULL;

        for (Measurement measurement : measurements) { // Alle Zeitpunkte durchlaufen
            IDIterator = dataSetCollection.keySet().iterator(); // neuen Iterator für die Messpunkte holen
            time = measurement.getTime(); // nächsten Zeitpunkt in der Reihe nehmen
            makeNULL = false;
            MeasurementTreeSet innerMeasurements;
            while (IDIterator.hasNext()) {  // Alle Messpunkte für den obigen Zeitpunkt durchlaufen
                id = IDIterator.next();
                innerMeasurements = (MeasurementTreeSet) dataSetCollection.get(id);
                
                if (innerMeasurements.getValueForTime(time) != null) {
//                    System.out.println("V" + SensorInformation.getSensorProperties(id).getSensorID() + "     " + dataMapCollection.get(id).get(time).toString());
                    evaluator.putVariable("V" + SensorInformation.getSensorProperties(id).getSensorID(), innerMeasurements.getValueForTime(time).toString()); // Wert dem Evaluator als Variable mitgeben
                } else {
//                    System.out.println("NULL");
                    evaluator.putVariable("V" + SensorInformation.getSensorProperties(id).getSensorID(), "0" ); // Wert dem Evaluator als Variable mitgeben
                    // AZ: Variante für AND und OR-Verknüpfung einbauen
                    if( isVirtual && formulaContainsX )
                        makeNULL = true; // Fehlt eine der Komponenten im Intervall gilt das ganze Intervall als null
                }
            }

            try {
                if (!makeNULL) {
                    // System.out.println( "formula: " + formula );                    
                    d = evaluator.getNumberResult(formula);                    
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yy HH:mm:ss" );
                    DecimalFormat df = new DecimalFormat("#.##");
                    /*
                    Date date = new Date( time );
                    Double v21Double = new Double( evaluator.getVariableValue( "V21" ) );
                    Double v35Double = new Double( evaluator.getVariableValue( "V35" ) );                    
                    System.out.println( "Formel: " + formula + " V21: " + df.format( v21Double ) + 
                            " V35: " + df.format( v35Double ) +
                            " Ergebnis " + simpleDateFormat.format( date ) + ": " + df.format(d) );
                    */
                    // System.out.println(" === " + d);
                    if (Double.isInfinite(d)) {
                        dataSet.add(new Measurement(time, null));
                    } else {
                        dataSet.add(new Measurement(time, d.doubleValue()));
                        min = (d < min) ? d : min; // Maximum und Minimum ermitteln
                        max = (d > max) ? d : max;
                    }
                } else { // eine der Komponenten ist null -> alles ist null
                    dataSet.add(new Measurement(time, null));
                }
            } catch (EvaluationException ex) {
                Messages.showException(ex);
                Messages.showException(ex);
            } catch (Exception e) {
                Messages.showException(e);
            }
        }

        minValue = min.doubleValue();
        maxValue = max.doubleValue();

        return dataSet;
    }

    /**
     * Trims the given dataset to the time raneg of the date interval
     *
     * @param dataSet The dataset
     * @param dateInterval The date range
     */
    private synchronized void trimDataMap(MeasurementTreeSet dataSet, DateInterval dateInterval) {
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy kk:mm:ss" );
        
        long utcPlusX = 0;
        if( SensorInformation.getSensorProperties(sensorID).getUtcPlusX() != null )
            utcPlusX = SensorInformation.getSensorProperties(sensorID).getUtcPlusX();
        
        System.out.println( "trimDataMap - start: " + simpleDateFormat.format( new Date( dateInterval.getStartDate().getTime() + utcPlusX * 1000 ) ) );
        System.out.println( "trimDataMap - end: " + simpleDateFormat.format( new Date( dateInterval.getEndDate().getTime() + utcPlusX * 1000 ) ) );
        
        if ((dataSet != null)) {
            for (Iterator<Measurement> it = dataSet.iterator(); it.hasNext();) {
                Measurement measurement = it.next();
                // MONISOFT-22: Verschiebe den Zeitraum
                if (measurement.getTime() < dateInterval.getStartDate().getTime() + utcPlusX * 1000 || 
                        measurement.getTime() > dateInterval.getEndDate().getTime() + utcPlusX * 1000 ) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Initializes the day codes for week day filtering
     */
    private void initDayFilterLists() {
        dayList.push((byte) 1);
        dayList.push((byte) 2);
        dayList.push((byte) 4);
        dayList.push((byte) 8);
        dayList.push((byte) 16);
        dayList.push((byte) 32);
        dayList.push((byte) 64);

        dayMap.put((byte) 1, Calendar.MONDAY);
        dayMap.put((byte) 2, Calendar.TUESDAY);
        dayMap.put((byte) 4, Calendar.WEDNESDAY);
        dayMap.put((byte) 8, Calendar.THURSDAY);
        dayMap.put((byte) 16, Calendar.FRIDAY);
        dayMap.put((byte) 32, Calendar.SATURDAY);
        dayMap.put((byte) 64, Calendar.SUNDAY);

        dayString.put(Calendar.MONDAY, "Mo");
        dayString.put(Calendar.TUESDAY, "Di");
        dayString.put(Calendar.WEDNESDAY, "Mi");
        dayString.put(Calendar.THURSDAY, "Do");
        dayString.put(Calendar.FRIDAY, "Fr");
        dayString.put(Calendar.SATURDAY, "Sa");
        dayString.put(Calendar.SUNDAY, "So");
    }
}
