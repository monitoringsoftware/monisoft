/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.DataHandling;

import de.jmonitoring.DataHandling.Interpolators.InterpolatorHelper;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.utils.Measurement;
import de.jmonitoring.utils.MeasurementTreeSet;
import org.jfree.data.time.TimePeriod;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author togro
 */
public class InterpolatorHelperTest {

    public InterpolatorHelperTest() {
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

    private MeasurementTreeSet getTestMap() {
        MeasurementTreeSet completeMap = new MeasurementTreeSet();
        completeMap.add(new Measurement(100000L, 10d));
        completeMap.add(new Measurement(200000L, 20d));
        completeMap.add(new Measurement(300000L, 30d));
        completeMap.add(new Measurement(400000L, 40d));
        completeMap.add(new Measurement(500000L, 50d));
        return completeMap;
    }
    
        private MeasurementTreeSet getResultMap() {
        MeasurementTreeSet completeMap = new MeasurementTreeSet();
        completeMap.add(new Measurement(100000L, 10d));
        completeMap.add(new Measurement(200000L, 30d));
        completeMap.add(new Measurement(300000L, 60d));
        completeMap.add(new Measurement(400000L, 100d));
        completeMap.add(new Measurement(500000L, 150d));
        return completeMap;
    }
    

    /**
     * Test of getPreviousKeepValue method, of class InterpolatorHelper.
     */
    @Test
    public void testGetPreviousKeepValue() {
        System.out.println("getPreviousKeepValue");
        MeasurementTreeSet completeMap = getTestMap();
        Long valid = null;
        Long edgeTolerance = null;

        Measurement startMeasurement = new Measurement(100500L, null);
        Measurement expResult = completeMap.first();
        Measurement result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = completeMap.last();
        expResult = completeMap.getMeasurementForTime(400000L);
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(600000L, null);
        expResult = completeMap.last();
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(1000L, null);
        expResult = null;
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);


        // with edgeTolerance 5000, valid = 1000
        edgeTolerance = 5000L;
        valid = 1000L;
        startMeasurement = new Measurement(105000L, null);
        expResult = completeMap.first();
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(106000L, null);
        expResult = completeMap.first();
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(107000L, null);
        expResult = null;
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(1000L, null);
        expResult = null;
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        // with edgeTolerance null, valid = 1000
        edgeTolerance = null;
        valid = 1000L;
        startMeasurement = new Measurement(101000L, null);
        expResult = completeMap.first();
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(102000L, null);
        expResult = null;
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(300500L, null);
        expResult = completeMap.getMeasurementForTime(300000L);
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        startMeasurement = new Measurement(1000L, null);
        expResult = null;
        result = InterpolatorHelper.getPreviousKeepValue(completeMap, startMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);
    }

    /**
     * Test of getNextKeepValue method, of class InterpolatorHelper.
     */
    @Test
    public void testGetNextKeepValue() {
        System.out.println("getNextKeepValue");
        MeasurementTreeSet completeMap = getTestMap();
        Long valid = null;
        Long edgeTolerance = null;

        Measurement endMeasurement = new Measurement(400500L, null);
        Measurement expResult = completeMap.last();
        Measurement result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = completeMap.first();
        expResult = completeMap.getMeasurementForTime(200000L);
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(600000L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(500000L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);


        // with edgeTolerance 5000, valid = 1000
        edgeTolerance = 5000L;
        valid = 1000L;
        endMeasurement = new Measurement(495000L, null);
        expResult = completeMap.last();
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(494000L, null);
        expResult = completeMap.last();
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(493000L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(500500L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        
        // with edgeTolerance null, valid = 1000
        edgeTolerance = null;
        valid = 1000L;
        endMeasurement = new Measurement(499000L, null);
        expResult = completeMap.last();
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(498000L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(399500L, null);
        expResult = completeMap.getMeasurementForTime(400000L);
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);

        endMeasurement = new Measurement(50500L, null);
        expResult = null;
        result = InterpolatorHelper.getNextKeepValue(completeMap, endMeasurement, valid, edgeTolerance);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCoverTolerance method, of class InterpolatorHelper.
     */
    @Ignore
    @Test
    public void testGetCoverTolerance() {
        System.out.println("getCoverTolerance");
        Double expResult = null;
        Double result = InterpolatorHelper.getCoverTolerance();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getEdgeTolerance method, of class InterpolatorHelper.
     */
    @Ignore
    @Test
    public void testGetEdgeTolerance() {
        System.out.println("getEdgeTolerance");
        TimePeriod interval = null;
        Long expResult = null;
        Long result = InterpolatorHelper.getEdgeTolerance(interval);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDurationOfValidity method, of class InterpolatorHelper.
     */
    @Ignore
    @Test
    public void testGetDurationOfValidity() {
        System.out.println("getDurationOfValidity");
        int sensorID = 0;
        Long expResult = null;
        Long result = InterpolatorHelper.getDurationOfValidity(sensorID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of generateCounterValues method, of class InterpolatorHelper.
     */
    @Test
    public void testGenerateCounterValues() {
        System.out.println("generateCounterValues");
        MeasurementTreeSet dataSet = getTestMap();
        MeasurementTreeSet expResult = getResultMap();
        MeasurementTreeSet result = InterpolatorHelper.generateCounterValues(dataSet);
        assertEquals(expResult, result);
    }

    /**
     * Test of interpolate method, of class InterpolatorHelper.
     */
    @Test
    public void testInterpolate() {
        System.out.println("interpolate");
        long x = 1262304000L;
        long x1 = 1262302200L;
        long x2 = 1262340000L;
        double y1 = 90.0;
        double y2 = 100.0;
        Double expResult = 90.476190476;
        Double result = InterpolatorHelper.interpolate(x, x1, x2, y1, y2);
        assertEquals(expResult, result, 0.000000001);
    }
}
