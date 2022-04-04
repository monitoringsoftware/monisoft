package de.jmonitoring.utils.UnitCalulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import net.sourceforge.jeval.Evaluator;

/**
 * A class for tranferring unit
 *
 * @author togro
 */
public class UnitTransfer {

    private static HashMap<String, String> transferTable = new HashMap<String, String>();
    private final String[] consumptionUnits = {"mWh", "Wh", "kWh", "MWh", "GWh", "TWh", "J", "kJ", "MJ", "Nm", "SKE", "Ws", "kWs"};
    private final String[] powerUnits = {"mW", "W", "kW", "MW", "GW", "TW"};
    private final String[] volumeUnits = {"l", "m³"};
    private final String[] temperatureUnits = {"°C", "°F"};
    private static HashMap<String, ArrayList<String>> aliases = new HashMap<String, ArrayList<String>>();

    /**
     * Transfers the given value in the fromUnit to a value in targetUnit
     *
     * @param fromUnit The unit of the given value
     * @param toUnit The taget unit
     * @param value The value given in unit fromUnit
     * @return The value in targetUnit
     */
    public static Double transfer(Unit fromUnit, Unit toUnit, Double value) {
        try {
            String from = fromUnit.getUnit();
            String to = toUnit.getUnit();
            // Wenn gleiche Einheit gewünscht direkt zurückgeben
            if (from.equals(to)) {
                return value;
            }

            Evaluator evaluator = new Evaluator();
            init();

            evaluator.putVariable("V", String.valueOf(value)); // variable name of jEval must not start wit a letter. append a V for "V"ariable

            // replace units of the same numerical values with an alias so that the tranferTable keeps small
            from = replaceAlias(fromUnit.getUnit());
            to = replaceAlias(toUnit.getUnit());

            if (from.equals(to)) {
                return value;
            }
            if (transferTable.get(fromUnit + ">" + to) != null) {
                return evaluator.getNumberResult(transferTable.get(fromUnit + ">" + to));
            } else {
                return null; // was not transferrable
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Translates a given usage unit to its respective ower unit.<p> For
     * example: "kWh" to "kW"
     *
     * @param unit The usage unit
     * @return The power unit
     */
    public String getPowerUnitFromConsumption(String unit) {
        String powerUnit = "";
        if (unit.equals("Wh")) {
            powerUnit = "W";
        }
        if (unit.equals("kWh")) {
            powerUnit = "kW";
        }
        if (unit.equals("MWh")) {
            powerUnit = "MW";
        }
        if (powerUnit.isEmpty()) {
            powerUnit = unit;
        }
        return powerUnit;
    }

    /**
     * Initializes the table of transfer factors and the alisas
     */
    private static void init() {
        // aliases
        String[] litreAliases = {"L", "l"};
        aliases.put("L", new ArrayList<String>(Arrays.asList(litreAliases)));
        String[] cbmAliases = {"m³", "m3", "cbm", "kbm"};
        aliases.put("cbm", new ArrayList<String>(Arrays.asList(cbmAliases)));
        String[] JouleAliases = {"J", "Nm", "Ws"};
        aliases.put("J", new ArrayList<String>(Arrays.asList(JouleAliases)));


        // Verbrauch
        transferTable.put("GWh>MWh", "#{V} * 1000.0");
        transferTable.put("GWh>kWh", "#{V} * 1000000.0");
        transferTable.put("GWh>Wh", "#{V} * 1000000000.0");

        transferTable.put("MWh>GWh", "#{V} * 0.001");
        transferTable.put("MWh>kWh", "#{V} * 1000.0");
        transferTable.put("MWh>Wh", "#{V} * 1000000.0");

        transferTable.put("kWh>GWh", "#{V} * 0.000001");
        transferTable.put("kWh>MWh", "#{V} * 0.001");
        transferTable.put("kWh>Wh", "#{V} * 1000.0");

        transferTable.put("Wh>GWh", "#{V} * 0.000000001");
        transferTable.put("Wh>MWh", "#{V} * 0.000001");
        transferTable.put("Wh>kWh", "#{V} * 0.001");

        transferTable.put("Wh>J", "#{V} * 3600.0");
        transferTable.put("Wh>kJ", "#{V} * 3600000.0");
        transferTable.put("Wh>MJ", "#{V} * 3600000000.0");

        transferTable.put("kWh>J", "#{V} * 3600000.0");
        transferTable.put("kWh>kJ", "#{V} * 3600000000.0");
        transferTable.put("kWh>MJ", "#{V} * 3600000000000.0");

        // Leistung
        transferTable.put("GW>MW", "#{V} * 1000.0");
        transferTable.put("GW>kW", "#{V} * 1000000.0");
        transferTable.put("GW>W", "#{V} *  1000000000.0");

        transferTable.put("MW>GW", "#{V} * 0.001");
        transferTable.put("MW>kW", "#{V} * 1000.0");
        transferTable.put("MW>W", "#{V} * 1000000.0");

        transferTable.put("kW>GW", "#{V} * 0.000001");
        transferTable.put("kW>MW", "#{V} * 0.001");
        transferTable.put("kW>W", "#{V} * 1000.0");

        transferTable.put("W>GW", "#{V} *  0.000000001");
        transferTable.put("W>MW", "#{V} * 0.000001");
        transferTable.put("W>kW", "#{V} * 0.001");



        // Volumen
        transferTable.put("L>cbm", "#{V} * 0.001");
        transferTable.put("cbm>L", "#{V} * 1000.0");

        // Temperatur
        transferTable.put("°C>°F", "((#{V}*9)/5)+32");
        transferTable.put("°F>°C", "(#{V}-32)*5/9");
    }

    /**
     * Replaces the given unit by a common alias for better comparison
     *
     * @param unit The unit
     * @return The alias of the unit
     */
    private static String replaceAlias(String unit) {
        String s = unit;
        // loop all aliases
        for (String alias : aliases.keySet()) {
            for (String innerUnit : aliases.get(alias)) {
                if (unit.equals(innerUnit)) {
                    s = alias;
                }
            }
        }
        return s;
    }
}
