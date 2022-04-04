package de.jmonitoring.DataHandling;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.WeatherCalculation.ClimateFactor;
import de.jmonitoring.WeatherCalculation.ClimateFactorReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Detemines the correct climate factor for a given PLZ and time.<p> The basis
 * are imported climate factor from the "Deutscher Wetterdienst"
 *
 * @author togro
 */
public class ClimateFactorHandler {

    private final MainApplication gui;

    /**
     * Constructor taking in the GUI (or nonGUI when CLI)
     *
     * @param gui The GUI
     */
    public ClimateFactorHandler(MainApplication gui) {
        super();
        this.gui = gui;
    }

    /**
     * Determine the climate factor and return its value
     *
     * @param postCode The post code
     * @param year The year
     * @param month The starting month
     * @return The climate factor for 12 months starting from the given year and
     * month
     */
    public Double getClimateFactor(int postCode, int year, int month) {
        Double factor = 1d;
        ArrayList<ClimateFactor> factorList = new ClimateFactorReader(this.gui).getClimateFactorsForPostCode(postCode);

        // get first day of year
        GregorianCalendar cal = new GregorianCalendar(year, month, 1);

        for (ClimateFactor cf : factorList) {
            if (cf.getStartdate().equals(cal.getTime())) {
                factor = cf.getFactor();
//                System.out.println("CF" + cf.getStartdate() + " " + factor);
            }
        }
        return factor;
    }
}
