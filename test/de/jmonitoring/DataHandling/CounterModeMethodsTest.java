/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.CounterChange.CounterChange;
import de.jmonitoring.DataHandling.Interpolators.CounterInterpolator;
import de.jmonitoring.DataHandling.Interpolators.CounterStep;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import java.util.ArrayList;
import org.jfree.data.time.TimePeriod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author togro
 */
public class CounterModeMethodsTest {

    private MeasurementTreeSet emptyMeasurements;
    private MeasurementTreeSet beforeOnlyMeasurements;
    private MeasurementTreeSet afterOnlyMeasurements;
    private MeasurementTreeSet outsideOnlyMeasurements;
    private MeasurementTreeSet innerOnlyMeasurements;
    private MeasurementTreeSet beforeAndInnerMeasurements;
    private MeasurementTreeSet afterAndInnerMeasurements;
    private final TimePeriod IRRELEVANT_TIMEPERIOD = null;
    private final CounterMode IRRELEVANT_COUNTERMODE = null;
    private final Long IRRELEVANT_LONG = null;
    private final ArrayList<CounterChange> IRRELEVANT_COUNTERCHANGES = new ArrayList<CounterChange>();
    private final ArrayList<CounterStep> IRRELEVANT_COUNTERSTEPS = new ArrayList<CounterStep>();

    public CounterModeMethodsTest() {
        setIntervals();
    }

    @BeforeClass
    public static void setUpClass() {
        MoniSoft moni = MoniSoft.createMonisoft(false);
        moni.setDummyLogger();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIsEmptyInterval() {
        CounterInterpolator instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, emptyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertTrue(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertFalse(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, afterOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertTrue(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, outsideOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertFalse(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, innerOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertFalse(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeAndInnerMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertFalse(instance.isEmptyInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, afterAndInnerMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        assertFalse(instance.isEmptyInterval());
    }

    @Test
    public void testCalculateDifferenceOfMeasurements() {
        CounterInterpolator instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, emptyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        Long starttime = 1000L;
        Long endTime = 2000L;
        Double result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(Double.NaN, result, 0.1);

        starttime = 200L;
        endTime = 300L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, innerOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(30d, result, 0.00001);

        starttime = 100L;
        endTime = 400L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(Double.NaN, result, 0.1);

        starttime = 100L;
        endTime = 400L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, afterOnlyMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(Double.NaN, result, 0.1);

        starttime = 100L;
        endTime = 300L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeAndInnerMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(Double.NaN, result, 0.1);

        starttime = 100L;
        endTime = 200L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeAndInnerMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(Double.NaN, result, 0.1);

        starttime = 200L;
        endTime = 300L;
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, IRRELEVANT_TIMEPERIOD, beforeAndInnerMeasurements, IRRELEVANT_COUNTERMODE, IRRELEVANT_LONG, IRRELEVANT_LONG, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateDifferenceOfMeasurements(starttime, endTime);
        assertEquals(4d, result, 0.000001);
    }

    private void setIntervals() {
        emptyMeasurements = new MeasurementTreeSet();

        beforeOnlyMeasurements = new MeasurementTreeSet();
        beforeOnlyMeasurements.setMeasurementBeforeInterval(new Measurement(100L, 10d));

        afterOnlyMeasurements = new MeasurementTreeSet();
        afterOnlyMeasurements.setMeasurementAfterInterval(new Measurement(400L, 10d));

        outsideOnlyMeasurements = new MeasurementTreeSet();
        outsideOnlyMeasurements.setMeasurementBeforeInterval(new Measurement(100L, 10d));
        outsideOnlyMeasurements.setMeasurementAfterInterval(new Measurement(400L, 35d));

        innerOnlyMeasurements = new MeasurementTreeSet();
        innerOnlyMeasurements.add(new Measurement(200L, 20d));
        innerOnlyMeasurements.add(new Measurement(300L, 50d));

        beforeAndInnerMeasurements = new MeasurementTreeSet();
        beforeAndInnerMeasurements.add(new Measurement(200L, 20d));
        beforeAndInnerMeasurements.add(new Measurement(300L, 24d));
        beforeAndInnerMeasurements.setMeasurementBeforeInterval(new Measurement(100L, 10d));

        afterAndInnerMeasurements = new MeasurementTreeSet();
        afterAndInnerMeasurements.add(new Measurement(200L, 20d));
        afterAndInnerMeasurements.add(new Measurement(300L, 26d));
        afterAndInnerMeasurements.setMeasurementAfterInterval(new Measurement(400L, 30d));
    }
}
