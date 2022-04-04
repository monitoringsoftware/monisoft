/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.WeatherCalculation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import com.mysql.jdbc.exceptions.MySQLSyntaxErrorException;

import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.Messages;
import de.jmonitoring.utils.StoppableThread;
import java.sql.PreparedStatement;

/**
 *
 * @author togro
 */
public class ClimateFactorReader {

    private static boolean climateImportLocked;
    private File inputFile;
    private MoniSoftProgressBar progressBar;
    private final MainApplication gui;

    public ClimateFactorReader(MainApplication gui) {
        this(null, gui);
    }

    public ClimateFactorReader(File file, MainApplication gui) {
        super();
        inputFile = file;
        this.gui = gui;
    }

    public static boolean isClimateImportLocked() {
        return climateImportLocked;
    }

    public void setInputFile(File file) {
        inputFile = file;
    }

    public void importFactors() {
        if (climateImportLocked) {
            return;
        }
        climateImportLocked = true;
        progressBar = this.gui.getProgressBarpanel().addProgressBar("Import der Klimafaktoren");
        progressBar.addProgressCancelButtonActionListener(action);
        doWork.start();
    }
    ActionListener action = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Messages.showMessage("Import der Klimafaktoren abgebrochen" + "\n", true);
            progressBar.remove();
            doWork.running = false;
        }
    };
    /**
     * The thread that does the work
     */
    StoppableThread doWork = new StoppableThread(new Runnable() {
        @Override
        public void run() {
            ((StoppableThread) Thread.currentThread()).running = true;
            try {
                readWorker();
            } catch (Exception e) {
                Messages.showException(e);
            } finally {
                climateImportLocked = false;
            }
        }
    });

    private void readWorker() throws IOException {
//        File inputFile = new File(inputFile);
        Workbook w;
        try {
            w = Workbook.getWorkbook(inputFile);
            // Get the first sheet
            Sheet sheet = w.getSheet(1);

            int numberrows = sheet.getRows() - 3;
            int numbercols = sheet.getColumns() - 2;
            int numberOfEntries = numbercols * numberrows;

            progressBar.setMinMax(0, numberOfEntries);
            progressBar.setValue(0);

            Cell[] startMonths = sheet.getRow(0);
            String plz;
            ArrayList<ClimateFactor> list = new ArrayList<ClimateFactor>(10000);
            int count = 0;
            for (int row = 3; row < sheet.getRows(); row++) { // data begins in row 4
                if (!((StoppableThread) Thread.currentThread()).running) {
                    return;
                }
                count++;
                plz = sheet.getCell(0, row).getContents();
                for (int column = 2; column < sheet.getColumns(); column++) {
                    Cell cell = sheet.getCell(column, row);
                    list.add(new ClimateFactor(plz, cell.getContents(), startMonths[column].getContents()));
                }

                if (count == 2000) {
                    writeClimateFactorToDB(list);
                    count = 0;
                    list.clear();
                }

                progressBar.setText("Import der Klimafaktoren (" + row * numbercols + " of " + numberOfEntries + ")");
                progressBar.setValue(row * numbercols);
            }
            w.close();
            writeClimateFactorToDB(list);
        } catch (BiffException e) {
            Messages.showException(e);
        } //catch (InterruptedException e) {
//            Messages.showException(e);
//        }
        progressBar.remove();

    }

    private void writeClimateFactorToDB(ArrayList<ClimateFactor> list) {
        Connection myConn = null;
        PreparedStatement pstmt = null;
        Statement stmt = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();

            StringBuilder valuesString = new StringBuilder("values");
            int count = 0;
            String sep = "";
            for (ClimateFactor cf : list) { // TODO zussammenfassen in gruppen mit set ... values (500er)

                valuesString.append(sep).append("(").append(cf.getPlz()).append(",").append(cf.getFactor()).append(",'").append(cf.getStartdateString()).append("')");
                sep = ",";
                count++;
                if (count == 2000) {
                    count = 0;
//                    System.out.println("v" + valuesString);
                    stmt.executeUpdate("insert ignore into T_DWDClimateFactors (PLZ,Factor,StartDate) " + valuesString.toString());
                    valuesString = new StringBuilder("values");
                    sep = "";
                }
            }

            // write remaining when not empty
            if (!valuesString.toString().equals("values")) {
                stmt.executeUpdate("insert ignore into T_DWDClimateFactors (PLZ,Factor,StartDate) " + valuesString.toString());
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showOptionPane("Sie haben nicht die nötigen Rechte um Veränderungen vorzunehmen.");
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(null, pstmt, null);
            DBConnector.closeConnection(myConn, stmt, null);
        }
    }

    public ArrayList<ClimateFactor> getClimateFactorsForPostCode(Integer postCode) {
        ArrayList<ClimateFactor> list = new ArrayList<ClimateFactor>();
        Connection myConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            myConn = DBConnector.openConnection();
            stmt = myConn.createStatement();
            rs = stmt.executeQuery("select Factor,StartDate from T_DWDClimateFactors where PLZ=" + postCode);
            while (rs.next()) {
                list.add(new ClimateFactor(postCode, rs.getDouble(1), rs.getDate(2)));
            }
        } catch (MySQLSyntaxErrorException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (SQLException ex) {
            Messages.showException(ex);
            Messages.showException(ex);
        } catch (Exception e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(myConn, stmt, rs);
        }

        return list;
    }
}
