package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.Interpolator;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import de.jmonitoring.utils.StoppableThread;
import de.jmonitoring.utils.UnitCalulation.Unit;
import de.jmonitoring.utils.intervals.DateInterval;

/**
 *
 * @author togro
 */
public class InterpolatorTest {

    private static final int SENSOR_ID = 0;
    private static final long HOUR_IN_MS = 1000 * 60 * 60;
	private static final Unit IRRELEVANT = null;

    public InterpolatorTest() {
        super();
    }

    /**
     * Test of getMaxValue method, of class Interpolator.
     */
    @Ignore
    @Test
    public void interpolatedMaxValue() {
        SensorInformation.setSensorList(testSensors());
        Interpolator interpolator = createInterpolator();

        interpolator.startInterpolation();
        assertEquals(5.5d, interpolator.getInterpolatedSet().getMaximumofIntervals(), 1E-4);
    }

    private Interpolator createInterpolator() {
        StoppableThread stoppableThread = new StoppableThread();
        stoppableThread.running = true;
        return new Interpolator(measurementTreeSet(), new DateInterval(new Date(2 * HOUR_IN_MS), new Date(5 * HOUR_IN_MS)), null, CounterMode.NOCOUNTER, SENSOR_ID, EventMode.EVENT_MEAN_MODE, stoppableThread, null) {
            @Override
            protected void showMessage(String message) {
                System.out.println(message);
            }
        };
    }

    private ArrayList<SensorProperties> testSensors() {
        ArrayList<SensorProperties> result = new ArrayList<SensorProperties>();
        result.add(new SensorProperties(SENSOR_ID) {
            @Override
            public boolean isCounter() {
                return true;
            }
        });
        return result;
    }

    private MeasurementTreeSet measurementTreeSet() {
        SortedSet<Measurement> measurements = new TreeSet<Measurement>();
        measurements.add(new Measurement(1 * HOUR_IN_MS, 5.5d));
        measurements.add(new Measurement(2 * HOUR_IN_MS, 5.5d));
        measurements.add(new Measurement(3 * HOUR_IN_MS, 5.5d));
        measurements.add(new Measurement(10 * HOUR_IN_MS, 5.5d));
        return new MeasurementTreeSet(measurements, IRRELEVANT);
    }
}
