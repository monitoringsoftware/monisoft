package de.jmonitoring.TableModels;

import de.jmonitoring.DataHandling.IntervalMeasurement;
import de.jmonitoring.DataHandling.MonthlyUsageCalculator;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.base.sensors.SensorProperties;
import de.jmonitoring.utils.DateCalculation.DateTimeCalculator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author togro
 */
public class MonthlyUsageTableModel extends AbstractTableModel {

    private SensorProperties props; // Properties des momantanen Messpunkts
    private Double[] interpolatedConsumption = new Double[12];
    private Double[] storedConsumption = new Double[12];
    private Double[] assumedConsumption = new Double[12];
    private Double[] missingConsumption = new Double[12];
    private Double[] consumptionPerDay = new Double[12];
    private Double[] missingDays = new Double[12];
    private int year;
    private final MainApplication gui;

    public MonthlyUsageTableModel(MainApplication gui) {
        super();
        this.gui = gui;
    }

    public void init(int id, int year) {
        Double[] normalmonthLengths = {31d, 28d, 31d, 30d, 31d, 30d, 31d, 31d, 30d, 31d, 30d, 31d};
        Double[] leapYearmonthLengths = {31d, 29d, 31d, 30d, 31d, 30d, 31d, 31d, 30d, 31d, 30d, 31d}; // bei schaltjahren
        Double[] monthLengths;

        IntervalMeasurement measurement;
        Double tmp;
        props = SensorInformation.getSensorProperties(id);
        this.year = year;
        if ((DateTimeCalculator.isLeapYear(year))) {
            monthLengths = normalmonthLengths;
        } else {
            monthLengths = leapYearmonthLengths;
        }

        MonthlyUsageCalculator muc = new MonthlyUsageCalculator(this.gui);
        for (int month = 0; month <= 11; month++) {
            measurement = muc.getInterpolatedMonthlyUsage(month + 1, year, props.getSensorID());
            tmp = measurement.getValue();
            interpolatedConsumption[month] = tmp; // == null ? null : tmp;
            missingDays[month] = measurement.getMissing().doubleValue() / 1000d / 60d / 60d / 24d;
            tmp = muc.getStoredMonthlyUsage(month + 1, year, props.getSensorID());
            storedConsumption[month] = tmp;// == null ? null : tmp;
            consumptionPerDay[month] = interpolatedConsumption[month] == null ? null : interpolatedConsumption[month] / (monthLengths[month] - missingDays[month]);
            missingConsumption[month] = consumptionPerDay[month] == null ? null : (missingDays[month]) * consumptionPerDay[month];
            assumedConsumption[month] = interpolatedConsumption[month] == null ? null : interpolatedConsumption[month] + missingConsumption[month];
        }
    }

    @Override
    public int getRowCount() {
        return 12;
    }

    @Override
    public int getColumnCount() {
        return 7;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case 0:
                return "<html><center" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.MONTH") + "</center></html>";
            case 1:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.USAGE") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.RAWDATA") + ")</center></html>";
            case 2:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.USAGE") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.MONTHLYDATA") + ")</center></html>";
            case 3:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.MISSINGDAYS") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.DAYS") + ")</center></html>";
            case 4:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.USAGE") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.DAILYMEAN") + ")</center></html>";
            case 5:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.RAMAINING") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.NOTMEASURED") + ")</center></html>";
            case 6:
                return "<html><center>" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.MONTHLYUSAGE") + "<br><font size =1>(" + java.util.ResourceBundle.getBundle("de/jmonitoring/TableModels/Bundle").getString("MonthlyUsageTableModel.ESTIMATED") + ")</center></html>";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case 0:
                return Integer.class;
            case 1:
                return Double.class;
            case 2:
                return Double.class;
            case 3:
                return Double.class;
            case 4:
                return Double.class;
            case 5:
                return Double.class;
            case 6:
                return Double.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (col >= 6 && missingDays[row] > 0) { // Geschätzter Verbrauch und Feld für Annahme
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0:
                return MoniSoftConstants.getMonthFor(row) + " " + year;
            case 1:
                return interpolatedConsumption[row];
            case 2:
                return storedConsumption[row];
            case 3:
                return missingDays[row];
            case 4:
                return consumptionPerDay[row] == null || consumptionPerDay[row].isNaN() ? null : consumptionPerDay[row];
            case 5:
                return missingConsumption[row] == null || missingConsumption[row].isNaN() ? null : missingConsumption[row];
            case 6:
                if (missingDays[row] == 0 && storedConsumption[row] != null) {
                    return null;
                } else {
                    return assumedConsumption[row] == null || assumedConsumption[row].isNaN() ? null : assumedConsumption[row];
                }
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object object, int row, int col) {
//        BuildingProperties selectedBuilding = (BuildingProperties) buildings.get(row);
        switch (col) {
            case 0:
//                selectedBuilding.setBuildingID(((Integer) object));
                break;
            case 1:
//                selectedBuilding.setBuildingName(((String) object).trim());
                break;
            case 2:
//                selectedBuilding.setObjectID(((Integer) object));
                break;
            case 3:
//                selectedBuilding.setBuildingDescription(((String) object).trim());
                break;
            case 4:
//                selectedBuilding.setPlz(((Integer) object));
                break;
            case 5:

                break;
            case 6:
                assumedConsumption[row] = (Double) object;
                break;
        }
        fireTableCellUpdated(row, col);
    }
    
    public Double[] getRawMonths() {
        return interpolatedConsumption;
    }
    
    public Double[] getMonthlyMonths() {
        return storedConsumption;
    }
}
