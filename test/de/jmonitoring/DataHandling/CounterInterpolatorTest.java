/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.CounterChange.CounterChange;
import de.jmonitoring.DataHandling.Interpolators.CounterInterpolator;
import de.jmonitoring.DataHandling.Interpolators.CounterStep;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;


import org.jfree.data.time.TimePeriod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author togro
 */
@RunWith(Parameterized.class)
public class CounterInterpolatorTest {

    MeasurementTreeSet exactMonthlyMeasurements = new MeasurementTreeSet();
    MeasurementTreeSet outerOnlyIntervalMonthlyMeasurements = new MeasurementTreeSet();
    MeasurementTreeSet nullMonthlyMeasurements = new MeasurementTreeSet();
    MeasurementTreeSet innerOnlyMonthlyMeasurements = new MeasurementTreeSet();
    MeasurementTreeSet filledMonthlyMeasurements = new MeasurementTreeSet();
    private Long edgeTolerance;
    private Long durationOfValidity;
    private List<Double[]> results;
    private static final Long halfDay = 12L * 60L * 60L * 1000L;
    private static final Long five_hours = 5 * 60L * 60L * 1000L;
    private static final Long oneHour = 1 * 60L * 60L * 1000L;
    private static final Long oneDday = 24L * 60L * 60L * 1000L;
    private final ArrayList<CounterChange> IRRELEVANT_COUNTERCHANGES = new ArrayList<CounterChange>();
    private final ArrayList<CounterStep> IRRELEVANT_COUNTERSTEPS = new ArrayList<CounterStep>();

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                    {Long.MAX_VALUE, null, Arrays.asList(new Double[][]{ // edgetolerance infinite, duarationOfValidity infinite
                            {100d, null, 96.875, 100d, 119.089026}, // consumption
                            {0.134408602, null, 0.1302, 0.134408602, 0.16006}, // power
                            {100d, null, 101.5625, 100d, 90.47619}}) // counter value
                    },
                    {halfDay, five_hours, Arrays.asList(new Double[][]{ // edgetolerance 0.5 days, duarationOfValidity 5 hours
                            {100d, null, 96.875, 100d, 119.089026}, // consumption
                            {0.134408602, null, 0.1302, 0.134408602, 0.16006}, // power
                            {100d, null, 101.5625, 100d, 90.47619}}) // counter value
                    },
                    {oneHour, oneHour, Arrays.asList(new Double[][]{ // edgetolerance 1 hour, duarationOfValidity 1 hour
                            {100d, null, 96.875, 100d, 120d}, // consumption
                            {0.134408602, null, 0.1302, 0.134408602, 0.16129}, // power
                            {100d, null, 101.5625, 100d, 90d}}) // counter value
                    },});
    }

    public CounterInterpolatorTest(Long edgeTolerance, Long durationOfValidity, List<Double[]> results) {
        fillMeasurements();
        this.edgeTolerance = edgeTolerance;
        this.durationOfValidity = durationOfValidity;
        this.results = results;
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

    /**
     * Test of calculateInterval method, of class CounterInterpolator.
     */
    @Test
    public void testCalculateIntervalConsumption() {
        System.out.println("calculateInterval counter consumption");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.MONTH_INTERVAL, new Date(110, 0, 1).getTime());

        CounterInterpolator instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, exactMonthlyMeasurements, CounterMode.COUNTERCONSUMPTION, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        Double expResult = results.get(0)[0];
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result);

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, nullMonthlyMeasurements, CounterMode.COUNTERCONSUMPTION, durationOfValidity, edgeTolerance, IRRELEVANT_COUNTERSTEPS);
        assertNull(instance.calculateInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, outerOnlyIntervalMonthlyMeasurements, CounterMode.COUNTERCONSUMPTION, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(0)[2];
        if (expResult == null) {
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.00001);
        }

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, innerOnlyMonthlyMeasurements, CounterMode.COUNTERCONSUMPTION, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(0)[3];
        if (expResult == null) {
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.00001);
        }

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, filledMonthlyMeasurements, CounterMode.COUNTERCONSUMPTION, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(0)[4];
        if (expResult == null) {
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.00001);
        }
    }

    /**
     * Test of calculateInterval method, of class CounterInterpolator.
     */
    @Test
    public void testCalculateIntervalPower() {
        System.out.println("calculateInterval counter power");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.MONTH_INTERVAL, new Date(110, 0, 1).getTime());
//
        System.out.println("1)");
        CounterInterpolator instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, exactMonthlyMeasurements, CounterMode.COUNTERPOWER, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        Double expResult = results.get(1)[0];
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result, 0.00001);

        System.out.println("2)");
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, nullMonthlyMeasurements, CounterMode.COUNTERPOWER, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        assertNull(instance.calculateInterval());

        System.out.println("3)");
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, outerOnlyIntervalMonthlyMeasurements, CounterMode.COUNTERPOWER, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(1)[2];
        assertEquals(expResult, result, 0.00001);

        System.out.println("4)");
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, innerOnlyMonthlyMeasurements, CounterMode.COUNTERPOWER, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(1)[3];
        assertEquals(expResult, result, 0.00001);

        System.out.println("5)");
        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, filledMonthlyMeasurements, CounterMode.COUNTERPOWER, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(1)[4];
        assertEquals(expResult, result, 0.00001);
    }

    /**
     * Test of calculateInterval method, of class CounterInterpolator.
     */
    @Test
    public void testCalculateIntervalCounterValue() {
        System.out.println("calculateInterval counter value");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.MONTH_INTERVAL, new Date(110, 0, 1).getTime());

        CounterInterpolator instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, exactMonthlyMeasurements, CounterMode.COUNTERVALUE, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        Double expResult = results.get(2)[0];
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result);

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, nullMonthlyMeasurements, CounterMode.COUNTERVALUE, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        assertNull(instance.calculateInterval());

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, outerOnlyIntervalMonthlyMeasurements, CounterMode.COUNTERVALUE, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(2)[2];
        assertEquals(expResult, result, 0.00001);

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, innerOnlyMonthlyMeasurements, CounterMode.COUNTERVALUE, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(2)[3];
        assertEquals(expResult, result);

        instance = new CounterInterpolator(IRRELEVANT_COUNTERCHANGES, period, filledMonthlyMeasurements, CounterMode.COUNTERVALUE, edgeTolerance, durationOfValidity, IRRELEVANT_COUNTERSTEPS);
        result = instance.calculateInterval().getValue();
        expResult = results.get(2)[4];
        assertEquals(expResult, result, 0.00001);
    }

    private void fillMeasurements() {
        exactMonthlyMeasurements.add(new Measurement(new Date(110, 0, 1).getTime(), 100d));
        exactMonthlyMeasurements.add(new Measurement(new Date(110, 1, 1).getTime(), 200d));
        exactMonthlyMeasurements.setMeasurementBeforeInterval(null);
        exactMonthlyMeasurements.setMeasurementAfterInterval(null);

        nullMonthlyMeasurements.setMeasurementBeforeInterval(null);
        nullMonthlyMeasurements.setMeasurementAfterInterval(null);

        outerOnlyIntervalMonthlyMeasurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 11, 31, 12, 00).getTime(), 100d));
        outerOnlyIntervalMonthlyMeasurements.setMeasurementAfterInterval(new Measurement(new Date(110, 1, 1, 12, 00).getTime(), 200d));

        innerOnlyMonthlyMeasurements.add(new Measurement(new Date(110, 0, 1, 12, 00).getTime(), 100d));
        innerOnlyMonthlyMeasurements.add(new Measurement(new Date(110, 0, 31, 12, 00).getTime(), 200d));
        innerOnlyMonthlyMeasurements.setMeasurementBeforeInterval(null);
        innerOnlyMonthlyMeasurements.setMeasurementAfterInterval(null);

        filledMonthlyMeasurements.add(new Measurement(new Date(110, 0, 1, 10, 00).getTime(), 100d));
        filledMonthlyMeasurements.add(new Measurement(new Date(110, 0, 31, 13, 00).getTime(), 200d));
        filledMonthlyMeasurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 11, 31, 23, 30).getTime(), 90d));
        filledMonthlyMeasurements.setMeasurementAfterInterval(new Measurement(new Date(110, 1, 1, 00, 30).getTime(), 210d));
    }
}
