package de.jmonitoring.DataHandling;

import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.StoppableThread;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JProgressBar;
import org.supercsv.exception.SuperCSVException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * This is the import for csv-data files<p> It reads a file and writes the
 * values to the database by identifying the assigned sensor by the sensorKey of
 * sensorName. The process first writes a temporary mon-File which is then read
 * by a {@link MONDataImporter}
 *
 * @author togro
 */
public class CSVDataImporter {

    private JProgressBar progressBar;
    private StoppableThread thisThread;
    private String errorReason = ""; //NOI18N
    private HashSet<String> failedList = new HashSet<String>(512);
    private DBConnector conn;
    private String[] header;
    private MONDataImporter mon;
    private File tempFile;
    private BufferedWriter out;
    private String dateTimeFormat;
    private String dateColumn;
    private String timeColumn;
    private Integer lineCount;
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger();

    /**
     * Creates an new importer with the given status progress bar and a database
     * connection
     *
     * @param progressBar The progressbar
     * @param connector The database connection
     */
    public CSVDataImporter(JProgressBar progressBar, DBConnector connector) {
        this.progressBar = progressBar;
        this.conn = connector;

        // if we cannot cast to StoppableThread we were called from commandline and must create our own StoppableThread
        try {
            thisThread = (StoppableThread) Thread.currentThread();
        } catch (ClassCastException e) {
            thisThread = new StoppableThread();
            thisThread.running = true;
        }
    }

    /**
     * Invokes the import and return the number of imported data points
     *
     * @param inFile The file to be read
     * @param headerRow The line number of the header
     * @param firstDataRow The line of the first data entries
     * @param dateCol The column number that holds the date (and time)
     * @param timeCol The column number that holds the tiem if seperated from
     * the date (otherwise null)
     * @param fieldSep The field seperator (mostly , or ;)
     * @param quote An optional quote character (not used)
     * @param dateFormat The format of the date (and time)
     * @param timeFormatThe format of the time (null if there is no seperate
     * time column)
     * @param verbose If true more messages are generated (not used)
     * @return The number of imported data points
     */
    public Integer loadCSVFiletoDB(File inFile, Integer headerRow, Integer firstDataRow, Integer dateCol, Integer timeCol, char fieldSep, char quote, String dateFormat, String timeFormat, boolean verbose) {
        System.out.println( "loadCSVFiletoDB: " + inFile.getName() );
        Integer count = null;
        setProgressValue(1);
        String encoding = "UTF-8"; //NOI18N
        try {
            mon = new MONDataImporter(conn);
            lineCount = getLineCount(inFile);
            prepareTmpFile(encoding);
            readHeader(inFile, encoding, new CsvPreference('"', fieldSep, "\r\n"), headerRow); //NOI18N
            setTimeFormat(dateFormat, timeFormat, dateCol, timeCol);
            boolean success = readCSV(inFile, encoding, new CsvPreference('"', fieldSep, "\r\n"), firstDataRow); //NOI18N
            closeTmpFile();

            setProgressString(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IMPORTING"));
            setProgressIndeterminate(true);

            count = mon.loadDataFromInfile(tempFile);
            Messages.showMessage(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("IMPORTED_LINES") + ": " + count + "\n", true);
            setProgressString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CSVImportDialog.FERTIG"));
            setProgressIndeterminate(false);
            setProgressValue(getProgressMaximum());
        } catch (Exception e) {
            Messages.showMessage(e.getMessage(), true);
            setProgressIndeterminate(false);
            setProgressValue(0);
            setProgressString(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("CSVImportDialog.CANCELED"));
            Messages.showException(e);
            if (!e.getClass().equals(NullPointerException.class)) {
                errorReason += e.getMessage() + "\n";
            }
        }
        return count;
    }

    /**
     * Returns the list of sensor identifyers that were found in the file but
     * have no correspondence in the sensor list.
     *
     * @return A set of failed sensor identifyers
     */
    public HashSet<String> getFailed() {
        return failedList;
    }

    /**
     * Returns the accumulated eror messages
     *
     * @return The error texts
     */
    public String getErrorReason() {
        return errorReason;
    }

    /**
     * Determine the number of lines in the file
     *
     * @param inFile The file
     * @return The number of lines
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Integer getLineCount(File inFile) throws FileNotFoundException, IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(inFile));
        lnr.skip(Long.MAX_VALUE);
        return lnr.getLineNumber();
    }

    /**
     * Reads the header line and filles the array of header strings
     *
     * @param file The file to read
     * @param encoding The character encoding to use
     * @param csvPreference The {@link CsvPreference}
     * @param headerRow The line number of the header row
     */
    private void readHeader(File file, String encoding, CsvPreference csvPreference, int headerRow) {
        FileInputStream fi = null;
        try {
            fi = new FileInputStream(file);
            BufferedReader b = new BufferedReader(new InputStreamReader(fi, encoding));
            ICsvMapReader inFile = new CsvMapReader(b, csvPreference);

            // Skip the first number of rows until we reach the header, only if header is not in first row
            if (headerRow > 1) {
                for (int i = 1; i < headerRow; i++) {
                    header = inFile.getCSVHeader(false);
                }
            } else {
                header = inFile.getCSVHeader(false);
            }
        } catch (SuperCSVException ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            try {
                fi.close();
            } catch (IOException ex) {
                Messages.showException(ex);
            }
        }
    }

    /**
     * Reads the csv-file and writes the temporary mon-file
     *
     * @param file The file to read
     * @param encoding The character encoding to use
     * @param csvPreference The {@link CsvPreference}
     * @param firstDataRow The line number of the first data row
     * @return <code>true</code> if the process was successful
     */
    private boolean readCSV(File file, String encoding, CsvPreference csvPreference, int firstDataRow) {
        boolean success = false;
        setProgressMaximum(lineCount);
        FileInputStream fi = null;
        Integer count = 0;
        Map<String, String> map;
        int n = 0;
        try {
            fi = new FileInputStream(file);
            BufferedReader b = new BufferedReader(new InputStreamReader(fi, encoding));
            ICsvMapReader inFile = new CsvMapReader(b, csvPreference);
            // Skip the rows until we reach the first data row
            for (int i = 1; i < firstDataRow; i++) {
                try {
                    inFile.read(header);
                } catch (SuperCSVException s) {
                    System.out.println(s.getMessage());
                }
            }
//  
            n = inFile.getLineNumber();
            boolean goOn = true;
            while (goOn) {
                try {
                    map = inFile.read(header);                    
                    if (map == null) {
                        break;
                    }
                } catch (SuperCSVException s) {
                    continue;
                }
                n = inFile.getLineNumber();
                setProgressValue(count++);
                setProgressString(java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("PARSING_LINE") + " " + count + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/DataHandling/Bundle").getString("OF") + " " + lineCount);
                writeToTmpFile(map);
            }
            success = true;
        } catch (SuperCSVException ex) {
            logger.error(ex.getMessage());
            Messages.showMessage("Not all lines may have been read, because there were lines (e.g. line " + n + ") which did not have the correct number of columns" + "\n", true);
            Messages.showException(ex);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            Messages.showException(ex);
        } finally {
            try {
                fi.close();
            } catch (IOException ex) {
                Messages.showException(ex);
            }
        }
        return success;
    }

    /**
     * Builds the datetime format string depending on them being in the same or
     * different columns
     *
     * @param dateFormat The date format
     * @param timeFormat The time format (null if combined with date)
     * @param dateColNum The column for the date
     * @param timeColNum The column for the time (null if combined with date))
     */
    private void setTimeFormat(String dateFormat, String timeFormat, Integer dateColNum, Integer timeColNum) {
        // if date and time fromat are in the same column build a common format
        if (timeFormat != null) {
            dateTimeFormat = dateFormat + " " + timeFormat; // in seperate columns
            dateColumn = header[dateColNum - 1];
            timeColumn = header[timeColNum - 1];
        } else {
            dateTimeFormat = dateFormat; // in the same column
            dateColumn = header[dateColNum - 1];
            timeColumn = "";
        }
    }

    /*
     * Prepare the temporary file and let it be deleted when MoniSoft exits.
     */
    private void prepareTmpFile(String encoding) throws FileNotFoundException, IOException {
        tempFile = File.createTempFile("moncsv", ".tmp");
        tempFile.deleteOnExit(); // tmp Datei löschen wenn Programm endet
        out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), encoding));
    }

    /**
     * Close the temporary file
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void closeTmpFile() throws FileNotFoundException, IOException {
        out.close();
    }

    /**
     * Write the generated map of the csv-file to the temporary file
     *
     * @param map The map containing the csv-file
     * @throws IOException
     */
    private void writeToTmpFile(Map<String, String> map) throws IOException {
        String time;
        String formattedTime;
        int id = -1;
        String value = "";
        if (timeColumn == null) { // Datum und Zeit in gleicher Spalte
            time = map.get(dateColumn);
        } else {
            time = map.get(dateColumn) + " " + map.get(timeColumn);
        }

        for (String column : map.keySet()) {
            if (!column.equals(dateColumn) && !column.equals(timeColumn)) {
                id = SensorInformation.getSensorIDFromNameORKey(column);
                value = map.get(column).replace(",", ".");

                if (id != -1) {
                    formattedTime = timeFormatter(dateTimeFormat, time);
                    if (formattedTime != null && !value.isEmpty()) {
                        out.write(id + "," + value + "," + formattedTime);
                        out.newLine();
                    }
                } else {
                    column = column.replace("\n", "");
                    failedList.add(column);
                }
            }
        }
    }

    /**
     * Re-formats the time to a common format for the mon-file
     *
     * @param time The time in vaild format
     * @return The new time in format <code>yyyy-mm-dd HH:mm:ss</code>
     */
    private String timeFormatter(String timeFormat, String time) {
        if (timeFormat.equals("yyyy-MM-dd HH:mm:ss")) { // schon im richtigen Format
            return time;
        }

        String outputTime;
        Date d;
        SimpleDateFormat inputFmt = new SimpleDateFormat(timeFormat);
        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            d = inputFmt.parse(time);
            outputTime = outputFmt.format(d);
        } catch (ParseException ex) {
            return null;
        }

        return outputTime;
    }

    /**
     * Update progress
     *
     * @param value
     */
    private void setProgressValue(int value) {
        if (this.progressBar == null) {
            return;
        }
        this.progressBar.setValue(value);
    }

    /**
     * Update progress text
     *
     * @param value
     */
    private void setProgressString(String text) {
        if (this.progressBar == null) {
            return;
        }
        this.progressBar.setString(text);
    }

    /**
     * Toggle progress bar indeterminate mode
     *
     * @param indeterminate If <code>true</code> the progressbar is switched to
     * indeterminate mode
     */
    private void setProgressIndeterminate(boolean indeterminate) {
        if (this.progressBar == null) {
            return;
        }
        this.progressBar.setIndeterminate(indeterminate);
    }

    /**
     * Set the maximum value of the progress bar
     *
     * @param value The max value
     */
    private void setProgressMaximum(int value) {
        if (this.progressBar == null) {
            return;
        }
        this.progressBar.setMaximum(value);
    }

    /**
     * Set the minimum value of the progress bar
     *
     * @param value The min value
     */
    private int getProgressMaximum() {
        if (this.progressBar == null) {
            return 0;
        }
        return this.progressBar.getMaximum();
    }
}
