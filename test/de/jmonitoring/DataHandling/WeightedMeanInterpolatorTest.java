/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.WeightedMeanInterpolator;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.standardPlots.common.GeneralDataSetGenerator;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
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
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author togro
 */
@RunWith(Parameterized.class)
public class WeightedMeanInterpolatorTest {

    private Double covertolerance;
    private Long durationOfValidity;
    private Double expectedValue;
    private List<Double> results;

    @Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][]{
                    {null, null, Arrays.asList(new Double[]{-1.1191, 15d, -1.01, -1.1191, -0.985, -1.01})},
                    {0.8, 10L * 60L * 1000L, Arrays.asList(new Double[]{-1.1191, 15d, -1.01, -1.1191, null, -1.01})},
                    {0.4, 10L * 60L * 1000L, Arrays.asList(new Double[]{-1.1191, 15d, -1.01, -1.1191, -0.97, -1.01})},});
    }

    public WeightedMeanInterpolatorTest(Double covertolerance, Long durationOfValidity, List<Double> results) {
        super();
        this.covertolerance = covertolerance;
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
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalFull() {
        System.out.println("calculateIntervalFull");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());


        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 0).getTime(), -1.07));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 10).getTime(), -0.95));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 20).getTime(), -1.17));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 30).getTime(), -1.41));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 40).getTime(), -1.21));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 50).getTime(), -1.04));
        measurements.add(new Measurement(new Date(109, 0, 1, 2, 0).getTime(), -0.8));
        measurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 0, 1, 0, 50).getTime(), -0.95));
        measurements.setMeasurementAfterInterval(new Measurement(new Date(109, 0, 1, 2, 10).getTime(), -0.95));

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(0);
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalEmptyWithSurroundingValuesOnly() {
        System.out.println("calculateIntervalEmptyWithSurroundingValuesOnly");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());

        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 0, 1, 0, 50).getTime(), 10d));
        measurements.setMeasurementAfterInterval(new Measurement(new Date(109, 0, 1, 2, 10).getTime(), 20d));

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(1);
        Double result = instance.calculateInterval().getValue();

        if (covertolerance != null && covertolerance > 0 && durationOfValidity == 10 * 60 * 1000) { // if these conditions are met the result will be null and asserEquals fill throw NULLPOinterxception insted of test result
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.0001);
        }
    }

    /**
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalSame() {
        System.out.println("calculateIntervalSame");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());

        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 0).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 10).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 20).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 30).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 40).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 50).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 2, 0).getTime(), -1.01));
        measurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 0, 1, 0, 50).getTime(), -1.01));
        measurements.setMeasurementAfterInterval(new Measurement(new Date(109, 0, 1, 2, 10).getTime(), -1.01));

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(2);
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalInnerOnly() {
        System.out.println("calculateIntervalInnerOnly");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());

        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 0).getTime(), -1.07));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 10).getTime(), -0.95));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 20).getTime(), -1.17));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 30).getTime(), -1.41));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 40).getTime(), -1.21));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 50).getTime(), -1.04));
        measurements.add(new Measurement(new Date(109, 0, 1, 2, 0).getTime(), -0.8));
        measurements.setMeasurementBeforeInterval(null);
        measurements.setMeasurementAfterInterval(null);

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(3);
        Double result = instance.calculateInterval().getValue();
        assertEquals(expResult, result, 0.0001);
    }

    /**
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalWithGap() {
        System.out.println("calculateIntervalWithGap");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());


        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 0).getTime(), -1.07));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 10).getTime(), -0.95));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 50).getTime(), -1.04));
        measurements.add(new Measurement(new Date(109, 0, 1, 2, 0).getTime(), -0.8));
        measurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 0, 1, 0, 50).getTime(), -0.95));
        measurements.setMeasurementAfterInterval(new Measurement(new Date(109, 0, 1, 2, 10).getTime(), -0.95));

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(4);
        Double result = instance.calculateInterval().getValue();

        if (covertolerance != null && covertolerance.equals(0.8) && durationOfValidity == 10 * 60 * 1000) { // if these conditions are met the result will be null and asserEquals fill throw NULLPOinterxception insted of test result
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.0001);
        }
    }

    /**
     * Test of calculateInterval method, of class WeightedMeanInterpolator.
     */
    @Test
    public void testCalculateIntervalSameGap() {
        System.out.println("calculateIntervalSameGap");
        TimePeriod period = GeneralDataSetGenerator.getPeriodForTimeStamp(MoniSoftConstants.HOUR_INTERVAL, new Date(109, 0, 1, 1, 0).getTime());


        MeasurementTreeSet measurements = new MeasurementTreeSet();
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 0).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 10).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 1, 50).getTime(), -1.01));
        measurements.add(new Measurement(new Date(109, 0, 1, 2, 0).getTime(), -1.01));
        measurements.setMeasurementBeforeInterval(new Measurement(new Date(109, 0, 1, 0, 50).getTime(), -1.01));
        measurements.setMeasurementAfterInterval(new Measurement(new Date(109, 0, 1, 2, 10).getTime(), -1.01));

        WeightedMeanInterpolator instance = new WeightedMeanInterpolator(period, measurements, this.covertolerance, this.durationOfValidity);
        Double expResult = results.get(5);
        Double result = instance.calculateInterval().getValue();

        if (covertolerance != null && covertolerance.equals(0.8) && durationOfValidity == 10 * 60 * 1000) { // if these conditions are met the result will be null and asserEquals fill throw NULLPOinterxception insted of test result
            assertNull(result);
        } else {
            assertEquals(expResult, result, 0.0001);
        }
    }

    private void fillMeasurements() {
    }
}
