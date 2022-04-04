package de.jmonitoring.DataHandling;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;
import de.jmonitoring.ApplicationProperties.ApplicationPropertyReader;
import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.MainGUI;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.StoppableThread;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This class reconstrucs the events in T_History and puts the in T_Events<p>
 * Reconstructed events can be deleted form T_History if wanted.
 *
 * @author togro
 */
public class EventReconstructor {

    private Object syncObject;
    private MoniSoftProgressBar progressBar;
    private final MainApplication gui;
    private static boolean isEventReconstructionLocked;
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Create a new reconstructor with the given GUI
     *
     * @param gui The GUI to use
     */
    public EventReconstructor(MainApplication gui) {
        super();
        this.gui = gui;
    }

    /**
     * Flag method that return the lock status
     *
     * @return <code>true</code> if a reconstruction is already active and
     * working
     */
    public static boolean isEventReconstructionLocked() {
        return isEventReconstructionLocked;
    }

    /**
     * Initializes the event reconstruction
     *
     * @param sensorID The event sensor
     * @return  <code>true</code> if the operation was successful
     * @throws ParseException
     */
    public boolean reconstructEvent(int sensorID) throws ParseException {
        boolean success = false;
        SimpleDateFormat MySQLDateTimeFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateTimeFormat);
        Long maxValidTime = SensorInformation.getSensorProperties(sensorID).getMaxChangeTimes()[0];
        if (maxValidTime == null) {
            maxValidTime = Long.MAX_VALUE;
        }
        DataHandler dh = new DataHandler(sensorID);
        
        EventMeasurement oldestEvent = dh.getOldestEventInEventtable();
        Long oldestEventTime = 0L;

        TreeSet<EventMeasurement> newStateChanges = new TreeSet<EventMeasurement>();
        
        // AZ: Funktionalität abgewandelt: Wenn bereits Events in der Liste sind, dann
        // wird der neue Algorithmus verwendet, ansonsten der bisherige - MONISOFT-16
        if( oldestEvent != null )
        {
            // AZ: Beginn neuer Teil
            List<EventMeasurement> existingEvents = dh.getAllEvents();
            List<EventMeasurement> newEvents = getAllNewEvents(sensorID);
            List<EventMeasurement> resultEvents = new ArrayList<EventMeasurement>();

            // System.out.println( "# Vorhandene Events: " + existingEvents.size() );
            // System.out.println( "# Neue Events: " + newEvents.size() );

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" );

            // Führe die beiden Listen zusammen
            existingEvents.addAll( newEvents );

            // Sortiere chronologisch
            Collections.sort( existingEvents );

            // System.out.println( "# Vorhandene Events nach merge: " + existingEvents.size() );

            EventMeasurement lastEventMeasurement = null;

            Long startTimeLastEvent = existingEvents.get( existingEvents.size() -1 ).getTimeStart();
            Long lastduration = -1L;
            
            // 2. Durchlauf: Optimiere die Wechsel zwischen 1 und 0 (Doppelteinträge hintereinander löschen
            for(EventMeasurement event : existingEvents)
            {
                // System.out.println( "# event - TimeStart: " + simpleDateFormat.format( event.getTimeStart() ) + " Duration: " + event.getDuration() + " Value: " + event.getValue() );
                
                if( lastEventMeasurement != null &&
                    !event.getTimeStart().equals( startTimeLastEvent ) &&
                    ( event.getTimeStart().equals( lastEventMeasurement.getTimeStart()) ) )
                {
                    // Welches der beiden Events hat den Eintrag Duration = 0 (=> das ist das neu importierte Event)
                    if( event.getDuration() == 0 )
                    {
                        // System.out.println( "event.getTimeStart(): " + simpleDateFormat.format( event.getTimeStart() ) + " Duration: " + event.getDuration() + " Value: " + event.getValue() );
                        // System.out.println( "lastEventMeasurement.getTimeStart(): " + simpleDateFormat.format( lastEventMeasurement.getTimeStart() ) + " Duration: " + lastEventMeasurement.getDuration() + " Value: " + event.getValue() );
                        
                        // setze den Value des lastEventMeasurement auf den Event value
                        lastEventMeasurement.setValue( event.getValue() );
                    }
                } 

                if( lastEventMeasurement == null ||
                    ( !event.getValue().equals( lastEventMeasurement.getValue() ) ) )
                    // Wenn 2 Events hintereinander nicht den gleichen Zustand haben, dann füge sie in die Ergebnisliste ein
                    resultEvents.add( event );
                
                if( lastEventMeasurement != null &&
                    event.getTimeStart().equals( startTimeLastEvent ) &&
                    event.getValue().equals( lastEventMeasurement.getValue() ) )
                {
                    // ermittle den letzten gültigen gleichen Eintrag in der Ergebnisliste
                    EventMeasurement lastValidEntry = resultEvents.get( resultEvents.size()-1 );
                    lastduration = event.getTimeStart() - lastValidEntry.getTimeStart();
                    lastValidEntry.setDuration(lastduration);
                }
                
                lastEventMeasurement = event;
            }
            
            // System.out.println( "# Vorhandene Events nach merge: " + resultEvents.size() );
            
            lastEventMeasurement = null;

            // 3. Durchlauf: Berechne die Zeitdauern dazwischen
            for(EventMeasurement event : resultEvents)
            {
                // Sind wir beim letzten Event                
                // System.out.println( "# event - TimeStart: " + simpleDateFormat.format( event.getTimeStart() ) + " Duration: " + event.getDuration() + " Value: " + event.getValue() );
                
                // if( event.getTimeStart().equals( startTimeLastEvent ) )
                //    break;
                
                // Ermittle Zeitdauer zum nächsten Event
                if( lastEventMeasurement != null )
                {
                    Long duration = event.getTimeStart() - lastEventMeasurement.getTimeStart();

                    // System.out.println( "# event duration old: " + lastEventMeasurement.getDuration() + " new: " + duration );
                    lastEventMeasurement.setDuration( duration );
                }
                
                lastEventMeasurement = event;
            }
            
            // System.out.println( "# resultEvents.size: " + resultEvents.size() );

            // Lösche alle Events für den Sensor aus der Event-Tabelle
            dh.deleteEventsForSensor( sensorID );

            // Umwandeln des Ergebnisses in ein TreeSet
            newStateChanges.addAll( resultEvents );
        }
        else
        {
            // bisheriger Code        
            if (oldestEvent != null) { // if the is no event in the table, null is returned -> make it the 0 the be definately smaller than the first event in the history table
                oldestEventTime = oldestEvent.getTimeStart() + oldestEvent.getDuration();
            }
        
            // get all events in history table which are after the last event in event table (including its duration)
            newStateChanges = getChangesSince(oldestEventTime, sensorID);

            if (newStateChanges.isEmpty()) { // no new events after the oldest one in the evetn table -> nothing to do ...
                return true;
            }

            if (oldestEvent != null) { // if there is no old value in the event table, it must not be handled
                Integer oldestEventState = oldestEvent.getValue();
                Long duration;
                if ((newStateChanges.first().getTimeStart() - oldestEventTime) > maxValidTime) {
                    newStateChanges.add(new EventMeasurement(oldestEventTime, newStateChanges.first().getTimeStart() - oldestEventTime, null)); // there is a gap. set the state to null for it
                } else {
                    if (oldestEventState.equals(newStateChanges.first().getValue().intValue())) { // still same state
                        duration = (newStateChanges.first().getTimeStart() + newStateChanges.first().getDuration()) - oldestEventTime;
                        newStateChanges.remove(newStateChanges.first()); // if the new value is the same as the old one -> ommit it
                    } else { // state has changed
                        duration = newStateChanges.first().getTimeStart() - oldestEventTime;
                    }

                    dh.updateEvent(new Date(oldestEventTime), duration);
                }
            }
        }

        // now loop all new events and put them into the event table
        PreparedStatement stmt = null;
        Connection myConn = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("insert ignore into T_Events set T_Sensors_id_Sensors= ?,TimeStart= ?,TimeSpan= ?,State= ?");
            for (EventMeasurement m : newStateChanges) {
                stmt.setInt(1, sensorID);
                stmt.setString(2, MySQLDateTimeFormat.format(new Date(m.getTimeStart())));
                stmt.setLong(3, m.getDuration());
                if (m.getValue() != null) {
                    stmt.setInt(4, m.getValue().intValue());
                } else {
                    stmt.setNull(4, Types.INTEGER);
                }
                stmt.executeUpdate();
//                System.out.println(new Date(m.getTimeStart()) + "  " + m.getValue() + " for " + m.getDuration() / 60000 + "minutes");
            }
            success = true;
            logger.info("Reconstructed event " + sensorID);
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            logger.warn(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } catch (Exception ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }       
        
        return success;
    }    

    /**
     * Return all event data pints that are in T_History before a given
     * timestamp
     *
     * @param oldestEventTime The timestamp before which to look
     * @param sensorID The sensor
     * @return A list of {@link EventMeasurement}s
     */
    private TreeSet<EventMeasurement> getChangesSince(Long oldestEventTime, Integer sensorID) {
        Integer interval = SensorInformation.getSensorProperties(sensorID).getInterval();
        Long maxValid = SensorInformation.getSensorProperties(sensorID).getMaxChangeTimes()[0];
        boolean sensorOnlyShowsChanges = interval == 0;

        TreeSet<EventMeasurement> dataChanges = new TreeSet<EventMeasurement>();
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select TimeStamp,Value from " + MoniSoftConstants.HISTORY_TABLE + " where T_Sensors_id_Sensors= ? and " + MoniSoftConstants.HISTORY_TIMESTAMP + "> ?  order by TimeStamp");
            stmt.setInt(1, sensorID);
            stmt.setLong(2, oldestEventTime / 1000);
            rs = stmt.executeQuery();
            Integer state;
            Integer anchorState = null;
            Long anchorTime = null;
            Long lastTime = null;
            TreeMap<Long, Integer> data = rawdataAsList(rs);
            for (Long time : data.keySet()) {
                state = data.get(time);
                if (anchorState == null) { // only when keepstate is not defined yet
                    anchorState = state; // throw anchor
                    anchorTime = time;
                    lastTime = time; // this is the last one, too
                    continue;
                }

                if (!isValidNextMeasurement(time, data, sensorOnlyShowsChanges, maxValid)) { // the measurement is to far away from the last one, fill in period with state null
                    dataChanges.add(new EventMeasurement(anchorTime, lastTime - anchorTime, anchorState));
                    dataChanges.add(new EventMeasurement(lastTime, time - lastTime, null));
                    lastTime = time;
                    anchorTime = time;
                    anchorState = state;
                } else {
                    if (state.equals(anchorState)) {
                        lastTime = time;
                    } else {
                        dataChanges.add(new EventMeasurement(anchorTime, time - anchorTime, anchorState));
                        lastTime = time;
                        anchorTime = time;
                        anchorState = state;
                    }
                }
            }

            if (!data.isEmpty()) {
                dataChanges.add(new EventMeasurement(anchorTime, data.lastKey() - anchorTime, anchorState));
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return dataChanges;
    }
    
    /**
     * Return all new event data from the history-table
     * timestamp
     *
     * @param sensorID The sensor
     * @return A list of {@link EventMeasurement}s
     */
    private List<EventMeasurement> getAllNewEvents(Integer sensorID)
    {        
        Connection myConn = null;
        PreparedStatement stmt = null;
        ResultSet rs;
        List<EventMeasurement> result = new ArrayList<EventMeasurement>();
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.prepareStatement("select TimeStamp,Value from " + MoniSoftConstants.HISTORY_TABLE + " where T_Sensors_id_Sensors= ? order by TimeStamp");
            stmt.setInt(1, sensorID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                result.add( new EventMeasurement( rs.getLong(1) * 1000, 0L, new Double( rs.getDouble(2)).intValue()));
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, null);
        }
        return result;
    }

    /**
     * Generates a {@link TreeMap} from the given {@link ResultSet}<p> Uses the
     * timestanps as key and the status value as content
     *
     * @param rs The {@link ResultSet}
     * @return A {@link TreeMap} of the data
     */
    private TreeMap<Long, Integer> rawdataAsList(ResultSet rs) {
        TreeMap<Long, Integer> map = new TreeMap<Long, Integer>();
        try {
            while (rs.next()) {
                map.put(rs.getLong(1) * 1000, rs.getInt(2));
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        }
        return map;
    }

    /**
     * Tests wether the next measurement after a certain timestamp is valid or
     * if there is a time gap
     *
     * @param thisMeasuremtTime The time of the measurement to check
     * @param data The datset
     * @param onlyChanges If the sensor is set to show only changes no test is
     * possible so return <code>true</code>
     * @param maxValid The maximum time a value for this sensor is valid before
     * it expires
     * @return <code>true</code> if the value is valid
     */
    private boolean isValidNextMeasurement(Long thisMeasuremtTime, TreeMap<Long, Integer> data, boolean onlyChanges, Long maxValid) {
        if (onlyChanges) {
            return true;
        } else {
            if (maxValid == null) {
                maxValid = Long.MAX_VALUE;
            }
            Long prevMeasuremtTime = data.lowerKey(thisMeasuremtTime);
            if (thisMeasuremtTime - (prevMeasuremtTime + maxValid) > 0) { // there is a gap
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * Return a list of all event sensors from the database
     *
     * @param myConn The databsse connection
     * @return A list of all sensor IDs
     */
    private ArrayList<Integer> getEventSensors(Connection myConn) {
        ArrayList<Integer> eventList = new ArrayList<Integer>(1024);
        Statement stmt = null;
        ResultSet rs = null;

        try {
            myConn = (myConn == null) ? DBConnector.openConnection() : myConn;
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select id_Sensors as id from " + MoniSoftConstants.SENSOR_TABLE + " where isEvent=1 order by Sensor");
            while (rs.next()) {
                eventList.add(rs.getInt("id"));
            }
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return eventList;
    }

    /**
     * Deletes the data of the given sensor from T_History
     *
     * @param id The sensor
     * @param leave If <code>true</code> all or the newset data will be left in
     * T_History. The time perdiod depends on the application settings
     * @see ApplicationPropertyReader
     */
    private void removeEventFromHistory(int id, boolean leave) {
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Long maxDate = null;
        String leaveString = "";
        Long leaveValue = 0L;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            if (leave) { // Es sollen die letzten Events stehen bleiben
                rs = stmt.executeQuery("select max(TimeStamp) from T_History where T_Sensors_id_Sensors=" + id);
                while (rs.next()) {
                    maxDate = rs.getLong(1);
                }
                if (maxDate != null) { // Berechnen des Belassungsdatums
                    leaveValue = Long.valueOf(MoniSoft.getInstance().getApplicationProperties().getProperty("LeaveEvents")); // Wert in Stunden

                    if (leaveValue == 0) {
                        return;
                    }

                    leaveValue = leaveValue * 3600L; // in Sekunden
                    leaveString = " and TimeStamp < " + (maxDate - leaveValue);
                }
            }
            stmt.executeUpdate("delete low_priority from T_History where T_Sensors_id_Sensors=" + id + leaveString); // Löschung durchführen
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CHANGES_DENY"));
        } catch (SQLException ex) {
            Messages.showException(ex);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }
    }

    /**
     * Deletes the data of the all sensors from T_History
     *
     * @param leave If <code>true</code> all or the newset data will be left in
     * T_History. The time perdiod depends on the application settings
     * @see ApplicationPropertyReader
     */
    private void removeAllEventsFromHistory(boolean leave) {
        ArrayList<Integer> eventList;

        if (leave && Long.valueOf(MoniSoft.getInstance().getApplicationProperties().getProperty("LeaveEvents")) == 0) { // es sollen keine Events bereinigt werden
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("KEEP_ALL") + "\n", true);
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("KEEP_ALL"));
            return;
        }

        eventList = getEventSensors(null);
        Iterator<Integer> it = eventList.iterator();
        int id;
        int count = 1;
        while (it.hasNext()) {
            id = it.next();
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CLEAR_EVENT") + " " + id + " (" + count + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("OF") + " " + eventList.size() + ")" + "\n", true);
            removeEventFromHistory(id, leave);
            count++;
        }
        logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CLEARING_UP"));
        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("CLEARING_UP") + "\n", true);
    }

    /**
     * This methos does the reconstruction work
     *
     * @return <code>true</code> if the reconstruction was successful
     */
    private boolean reconstructAllWorker() {
        final boolean fromGUI = (gui instanceof MainGUI);

        ArrayList<Integer> eventList;
        boolean reconstructed = false;
        try {
            eventList = getEventSensors(null);
            Iterator<Integer> it = eventList.iterator();
            int id;
            int count = 1;

            if (fromGUI) {
                progressBar.setMinMax(0, eventList.size());
            }
            logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("EVENTS_RECON"));
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("EVENTS_RECON") + "\n", true);
            while (it.hasNext()) {
                if (!((StoppableThread) Thread.currentThread()).running) {
                    if (fromGUI) {
                        progressBar.remove();
                    }
                    return false;
                }

                id = it.next();
                if (fromGUI) {
                    progressBar.setValue(count);
                    progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("PROCESSING_EVENT") + " " + SensorInformation.getDisplayName(id));
                }

//                Thread.currentThread().sleep(5);
                reconstructed = reconstructEvent(id) || reconstructed; // solbald ein event angefasst wurde ist das hier true
                count++;
            }
            if (reconstructed) {
                Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("RECON_FINISHED") + "\n", true);
                logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("RECON_FINISHED"));
//                DBMaintenance.optimizeEvents(); // TODO zeitgesteuert
            }
            if (fromGUI) {
                progressBar.remove();
            }

        } catch (ParseException ex) {
            System.out.println(ex.getMessage());
            Messages.showException(ex);
        } catch (Exception e) {
            Messages.showException(e);
        }
        return reconstructed;
    }

    /**
     * Invokes the reconstruction
     *
     * @param o An notifying Object
     */
    public void startReconstructionAll(Object o) {
        final boolean fromGUI = (gui instanceof MainGUI);
        if (isEventReconstructionLocked) {
            return;
        }
        isEventReconstructionLocked = true;
        syncObject = o;
        if (fromGUI) {
            progressBar = this.gui.getProgressBarpanel().addProgressBar(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("EVENT_RECON"));
        }
        ActionListener action = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fromGUI) {
                    progressBar.remove();
                }
                reconstructAllThread.running = false;
            }
        };
        if (fromGUI) {
            progressBar.addProgressCancelButtonActionListener(action);
        }
        reconstructAllThread.start();
    }
    /**
     * This thread start the calculation in a new thread
     */
    StoppableThread reconstructAllThread = new StoppableThread(new Runnable() {
        @Override
        public void run() {
            synchronized (syncObject) {
                ((StoppableThread) Thread.currentThread()).running = true;
                try {
                    if (reconstructAllWorker()) {
                        removeAllEventsFromHistory(MoniSoft.getInstance().getApplicationProperties().getProperty("UseLeaveEvents").equals("1")); // Alle Events aus der History löschen (mit Optimierung)
                    } else {
                        logger.info(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("NO_EVENTS"));
                        Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("NO_EVENTS") + "\n\n", true);
                    }
                } catch (Exception e) {
                    Messages.showException(e);
                } finally {
                    syncObject.notify();
                    isEventReconstructionLocked = false;
                }
            }
        }
    });
}
