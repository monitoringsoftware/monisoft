package de.jmonitoring.utils;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import de.jmonitoring.utils.UnitCalulation.Unit;

public class MeasurementTreeSetTest {

    private static final long HOUR_IN_MS = 1000 * 60 * 60;
	private static final Unit IRRELEVANT = null;

	public MeasurementTreeSetTest() {
		super();
	}

	// Es gibt einen Zeitpunkt vor dem Intervallende
	@Test
	public void testQuestionedIntervalNotGreaterExistingInterval() {
		MeasurementTreeSet set = measurements();
		MeasurementTreeSet between = set.extractMeasurementsBetween(measurementAt(1), measurementAt(3));
		assertTrue(between.containsValueForTime(hour(1)));
		assertTrue(between.containsValueForTime(hour(3)));
	}

	// Es gibt einen Zeitpunkt auf dem Intervallende
	@Test
	public void testQuestionedIntervalEqualsExistingInterval() {
		MeasurementTreeSet set = measurements();
		MeasurementTreeSet between = set.extractMeasurementsBetween(measurementAt(1), measurementAt(10));
		assertTrue(between.containsValueForTime(hour(1)));
		assertTrue(between.containsValueForTime(hour(10)));
	}

	// Es gibt einen Zeitpunkt nach dem Intervallende
	@Test
	public void testQuestionedIntervalGreaterExistingInterval() throws Exception {
		MeasurementTreeSet set = measurements();
		MeasurementTreeSet between = set.extractMeasurementsBetween(measurementAt(1), measurementAt(11));
		assertTrue(between.containsValueForTime(hour(1)));
		assertTrue(between.containsValueForTime(hour(10)));
	}

	private MeasurementTreeSet measurements() {
    	List<Measurement> measurements = new ArrayList<Measurement>();
    	measurements.add(measurementAt(1));
    	measurements.add(measurementAt(2));
    	measurements.add(measurementAt(3));
    	measurements.add(measurementAt(10));
    	return new MeasurementTreeSet(new TreeSet<Measurement>(measurements), IRRELEVANT);
	}

	private Measurement measurementAt(int hour) {
		return new Measurement(hour * HOUR_IN_MS, (double) hour);
	}

	public long hour(int hour) {
		return hour * HOUR_IN_MS;
	}
}
