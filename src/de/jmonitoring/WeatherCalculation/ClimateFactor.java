/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoftConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author togro
 */
public class ClimateFactor {

    private Integer plz;
    private Double factor;
    private Date startDate;
    private SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");

    public ClimateFactor(Integer plz, Double factor, Date startDate) {
        this.plz = plz;
        this.factor = factor;
        this.startDate = startDate;
    }

    public ClimateFactor() {
    }

    public ClimateFactor(String plzString, String factorString, String startDateString) {
        plz = Integer.parseInt(plzString);
        factor = Double.parseDouble(factorString.replace(",", "."));
        try {
            startDate = df.parse(startDateString);
        } catch (ParseException ex) {
            Messages.showException(ex);
        }
    }

    public Double getFactor() {
        return factor;
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }

    public Integer getPlz() {
        return plz;
    }

    public void setPlz(Integer plz) {
        this.plz = plz;
    }

    public Date getStartdate() {
        return startDate;
    }

    public void setStartdate(Date startdate) {
        this.startDate = startdate;
    }

    public void setFactor(String factorString) {
        factor = Double.parseDouble(factorString.replace(",", "."));
    }

    public void setPlz(String plzString) {
        plz = Integer.parseInt(plzString);
    }

    public void setStartdate(String startDateString) {
        try {
            startDate = df.parse(startDateString);
        } catch (ParseException ex) {
            Messages.showException(ex);
        }
    }

    public String getPlzString() {
        return String.valueOf(plz);
    }

    public String getFactorString() {
        return String.valueOf(factor);
    }

    public String getStartdateString() {
        return new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat).format(startDate);
    }
}
