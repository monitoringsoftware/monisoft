package de.jmonitoring.DataHandling;

import static de.jmonitoring.DataHandling.CounterMode.NO_COUNTER_KUMULIERTE_LEISTUNG;
import static de.jmonitoring.DataHandling.CounterMode.NO_COUNTER_KUMULIERUNG;
import static de.jmonitoring.DataHandling.CounterMode.NO_COUNTER_ZEIGE_WERTE;
import static org.junit.Assert.*;

import org.junit.Test;

public class CounterModeTest {

    @Test
    public void interpolationModeIsCounterValueMode() {
        assertEquals(CounterMode.COUNTERVALUE, CounterMode.getInterpolationMode(false, true, true, true, true, true));
        assertEquals(CounterMode.COUNTERVALUE, CounterMode.getInterpolationMode(false, true, true, false, true, true));
    }

    @Test
    public void interpolationModeIsNoCounter() {
        assertEquals(CounterMode.NO_COUNTER_KUMULIERTE_LEISTUNG, CounterMode.getInterpolationMode(false, false, false, false, true, true));
        assertEquals(CounterMode.NO_COUNTER_ZEIGE_WERTE, CounterMode.getInterpolationMode(true, false, false, false, true, true));
        assertEquals(CounterMode.NO_COUNTER_KUMULIERUNG, CounterMode.getInterpolationMode(false, true, false, false, true, true));
        // assertEquals(CounterMode.NOCOUNTER, CounterMode.getInterpolationMode(true, true, false, false, true, true));
    }

    @Test
    public void interpolationModeIsCounterPower() {
        assertEquals(CounterMode.COUNTERPOWER, CounterMode.getInterpolationMode(true, false, true, false, true, true));
        assertEquals(CounterMode.COUNTERPOWER, CounterMode.getInterpolationMode(true, false, true, true, true, true));
        assertEquals(CounterMode.COUNTERPOWER, CounterMode.getInterpolationMode(true, false, false, true, true, true));
    }

    @Test
    public void interpolationModeIsCounterConsumption() {
        assertEquals(CounterMode.COUNTERCONSUMPTION, CounterMode.getInterpolationMode(false, false, true, false, true, true));
        assertEquals(CounterMode.COUNTERCONSUMPTION, CounterMode.getInterpolationMode(false, false, false, true, true, true));
        assertEquals(CounterMode.COUNTERCONSUMPTION, CounterMode.getInterpolationMode(false, false, true, true, true, true));
    }
}
