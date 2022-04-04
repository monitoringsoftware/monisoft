package de.jmonitoring.base;

import de.jmonitoring.base.sensors.SensorProperties;
import com.toedter.calendar.JDateChooser;
import de.jmonitoring.standardPlots.common.SeriesLooks;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.JOptionPane;

/**
 * A class for checking of valid date entries and sensor selection
 *
 * @author togro
 */
public class DateEntriesChecker {

    /**
     * This method checks the given dates and sensor list if the entries are
     * valid
     *
     * @param start The {@link  JDateChooser} for the start date
     * @param end The {@link  JDateChooser} for the end date
     * @param list The list of selected sensors
     * @return <code>true</true> if the enties are valid, otherwise <code>false</code>
     */
    public boolean hasValidEntries(JDateChooser start, JDateChooser end, ArrayList list) {
        boolean emptySelection = true;

        // are both dates selected ?
        if (start.getDate() == null || end.getDate() == null) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN KEINEN ZEITRAUM AUSGEWÄHLT."));
            return false;
        }
        // is end date > start date?
        if (end.getDate().getTime() < start.getDate().getTime()) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("END_BEFORE_START"));

            return false;
        }

        Calendar startCal = new GregorianCalendar();
        startCal.setTime(start.getDate());
        Calendar endCal = new GregorianCalendar();
        endCal.setTime(end.getDate());

        // is the yera range plausible
        if (startCal.get(Calendar.YEAR) < 2000 || startCal.get(Calendar.YEAR) > 2050) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("Invalid start date"));
            return false;
        }
        if (endCal.get(Calendar.YEAR) < 2000 || endCal.get(Calendar.YEAR) > 2050) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("INVALID_END_DATE"));
            return false;
        }

        // are sensors selected ?
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null) {
                if (list.get(i) instanceof SeriesLooks) {
                    if (!((SeriesLooks) list.get(i)).getSensor().isEmpty() && !((SeriesLooks) list.get(i)).getSensor().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
                        emptySelection = false;
                    }
                } else if (list.get(i) instanceof SensorProperties) {
                    if (!((SensorProperties) list.get(i)).getSensorName().isEmpty() && !((SensorProperties) list.get(i)).getSensorName().equals(MoniSoftConstants.NO_SENSOR_SELECTED)) {
                        emptySelection = false;
                    }
                }
            }
        }

        if (emptySelection) {
            JOptionPane.showMessageDialog(null, java.util.ResourceBundle.getBundle("de/jmonitoring/base/Bundle").getString("SIE HABEN KEINE MESSPUNKTE AUSGEWÄHLT."));
            return false;
        }
        return true;
    }
}
