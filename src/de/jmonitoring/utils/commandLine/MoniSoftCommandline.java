/*
 * To change this template, choose Tools | Templates and open the template out
 * the editor.
 */
package de.jmonitoring.utils.commandLine;

import de.jmonitoring.Consistency.ValueTests;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import de.jmonitoring.Components.DateRangeProvider;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.DBOperations.DatabaseBackup;
import de.jmonitoring.DataHandling.*;
import de.jmonitoring.base.Messages;
import de.jmonitoring.standardPlots.common.ChartLoader;
import de.jmonitoring.base.MoniSoft;
import de.jmonitoring.base.MoniSoftConstants;
import de.jmonitoring.base.NoOperationGUI;
import de.jmonitoring.base.sensors.SensorInformation;
import de.jmonitoring.utils.intervals.DateInterval;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import org.apache.commons.cli.*;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.LoggerFactory;

/**
 *
 * @author togro
 */
public class MoniSoftCommandline {

    private final String[] actions = {"csv", "mon", "backup", "monthly", "check", "sensor", "gra", "events", "cmail", "ccfg", "cfmt", "version"};
    private final int CSV = 0;
    private final int MON = 1;
    private final int BACKUP = 2;
    private final int MONTHLY = 3;
    private final int CHECK = 4;
    private final int SENSOR = 5;
    private final int GRA = 6;
    private final int EVENTS = 7;
    private final int CMAIL = 8;
    private final int CCFG = 9;
    private final int CFMT = 10;
    private final int VERSION = 11;
    // LOG levels
    private final int INFO = 0;
    private final int ERROR = 1;
    private final int WARN = 2;
    private ArrayList actionList = new ArrayList(Arrays.asList(actions));
    private ch.qos.logback.classic.Logger logger = MoniSoft.getInstance().getLogger(); //(ch.qos.logback.classic.Logger) LoggerFactory.getLogger(MoniSoftCommandline.class);
    private String sensor = "";
    private String args[];
    boolean check = false;
    private String message = "";
    private String outFile;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(MoniSoftConstants.MySQLDateFormat);
    private int FILE_LIMIT = 100; // Minimal erlaubte Dateigröße out byte bei Kontrolle
    private String logFile = null;
    private Options options = null;
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private EmailAttachment attachment = null;

    public MoniSoftCommandline(String arguments[]) {
        args = arguments;

        setOption();
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getOutputFileName() {
        return outFile;
    }

    private void setOption() {
        options = new Options();
        Option action = OptionBuilder.withArgName("action=VALUE").hasArgs(2).withValueSeparator().withDescription("Action to be performed. VALUE must be one of:"
                + "\n\tcsv\t\t- Import CSV file"
                + "\n\tmon\t\t- Import MON file"
                + "\n\tbackup\t- Backup the database"
                + "\n\tmonthly\t- Calculate monthly consumption"
                + "\n\tcheck\t\t- check data integrity"
                + "\n\tsensor\t- create simple plot for the given sensor"
                + "\n\tgra\t\t- create chart from the given describer"
                + "\n\tevents\t- Reconstruct events"
                + "\n\tcmail\t\t- create mail config file"
                + "\n\tccfg\t\t- create access config file"
                + "\n\tcfmt\t\t- create csv format config"
                + "\n\tversion\t\t- show version\n\t.").create("action");
        options.addOption(action);
        Option startOption = OptionBuilder.withArgName("YYYY-mm-dd").hasArg().withDescription("Use this date as start date ").create("start");
        Option endOption = OptionBuilder.withArgName("YYYY-mm-dd").hasArg().withDescription("Use this date as start date ").create("end");
        options.addOption(startOption);
        options.addOption(endOption);
        Option mailCfg = OptionBuilder.withArgName("file").hasArg().withDescription("Config file for mail").create("mail");
        Option fmtCfg = OptionBuilder.withArgName("file").hasArg().withDescription("Config file for CSV format").create("fmt");
        Option connectCfg = OptionBuilder.withArgName("file").hasArg().withDescription("Config file to access database").create("cfg");
        options.addOption(connectCfg);
        options.addOption(mailCfg);
        options.addOption(fmtCfg);
        Option lookbackOption = OptionBuilder.withArgName("n").hasArg().withDescription("Look back n days from today").create("lookback");
        options.addOption(lookbackOption);
        Option log = OptionBuilder.withArgName("file").hasArg().withDescription("Use given file for log").create("log");
        options.addOption(log);
        Option outOption = OptionBuilder.withArgName("file").hasArg().withDescription("Write output of PNG creation to this file").create("out");
        options.addOption(outOption);
        Option inOption = OptionBuilder.withArgName("file").hasArg().withDescription("CSV/MON or GRA file to read").create("in");
        options.addOption(inOption);
        Option useOption = OptionBuilder.withArgName("sensor").hasArg().withDescription("Sensor to use for simple plot (action sensor)").create("use");
        options.addOption(useOption);
        Option month = OptionBuilder.withArgName("month").hasArg().withDescription("Month for monthly calculation").create("month");
        Option year = OptionBuilder.withArgName("year").hasArg().withDescription("Year for monthly calculation").create("year");
        options.addOption(month);
        options.addOption(year);
    }

    public void parseCommandLine() {
        CommandLine cmd;
        CommandLineParser parser = new GnuParser();
        try {
            cmd = parser.parse(options, args);
        } catch (org.apache.commons.cli.ParseException ex) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(200);
            formatter.printHelp("'java -splash: -j jar jmonitoring.jar' with the following options:", options);
            return;
        }

        if (cmd.hasOption("log")) {
            logFile = cmd.getOptionValue("log");
            initializeLogger(logFile);
        }

        speak("*** Starting command line execution ***", INFO);
        String action = getAction(cmd);
        if (action == null) {
            speak("The action argument in mandatory.", ERROR);
            return;
        }
        Integer actionID = actionList.indexOf(action);
        if (actionID == -1) {
            speak("Unknown action.", ERROR);
            return;
        }
        String cfgFile;
        String inputFile;
        String fmtFile;
        Properties accessProps;
        Date startDate;
        Date endDate;
        Integer lb;
        switch (actionID) {
            case CSV: // ************************************************
                if (!cmd.hasOption("in") || !cmd.hasOption("cfg") || !cmd.hasOption("fmt")) {
                    message += "You must specify the database access (-cfg), input file (-in) and file format (-fmt)" + "\n";;
                    speak("You must specify the database access (-cfg), input file (-in) and file format (-fmt)", ERROR);
                    break;
                }

                inputFile = cmd.getOptionValue("in");
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }
                fmtFile = cmd.getOptionValue("fmt");
                Properties formatProps = getPropsFrom(fmtFile);
                if (formatProps == null) {
                    break;
                }
                doCSVAction(inputFile, accessProps, formatProps);
                break;
            case MON:// ************************************************
                if (!cmd.hasOption("in") || !cmd.hasOption("cfg")) {
                    message += "You must specify the database access (-cfg), input file (-in)" + "\n";
                    speak("You must specify the database access (-cfg), input file (-in)", ERROR);
                    break;
                }
                inputFile = cmd.getOptionValue("in");
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }

                doMonAction(inputFile, accessProps);
                break;
            case BACKUP:// ************************************************
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }
                doBackup(accessProps);
                break;
            case MONTHLY:
                if (!cmd.hasOption("year") || !cmd.hasOption("cfg")) {
                    message += "You must specify the database access (-cfg) and at least a year (-year) for which to calculate. A month (-month) is optional" + "\n";
                    speak("You must specify the database access (-cfg) and at least a year (-year) for which to calculate. A month (-month) is optional", ERROR);
                    break;
                }
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }
                Integer year = Integer.valueOf(cmd.getOptionValue("year"));
                Integer month = null;
                if (cmd.hasOption("month")) {
                    month = Integer.valueOf(cmd.getOptionValue("month"));
                }
                doMonthly(accessProps, month, year);
                break;
            case CHECK:
                if (!cmd.hasOption("cfg") || ((!cmd.hasOption("end") || !cmd.hasOption("start")) && !cmd.hasOption("lookback"))) {
                    message += "You must specify the database access (-cfg), and a date interval by (-start/-end) or -lookback" + "\n";
                    speak("You must specify the database access (-cfg), and a date interval by (-start/-end) or -lookback", ERROR);
                    break;
                }
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }

                if (cmd.hasOption("lookback")) {
                    try {
                        lb = Integer.parseInt(cmd.getOptionValue("lookback"));
                        endDate = new Date();
                        Calendar cal = new GregorianCalendar();
                        cal.add(Calendar.DATE, -lb);
                        startDate = cal.getTime();
                    } catch (NumberFormatException ex) {
                        message += "The number of days is invalid." + "\n";
                        speak("The number of days is invalid.", ERROR);
                        break;
                    }
                } else {
                    try {
                        startDate = dateFormat.parse(cmd.getOptionValue("start"));
                        endDate = dateFormat.parse(cmd.getOptionValue("end"));
                    } catch (ParseException ex) {
                        message += "The start and/or end date are not valid. Use format YYYY-mm-dd" + "\n";
                        speak("The start and/or end date are not valid. Use format YYYY-mm-dd", ERROR);
                        break;
                    }
                }
                doCheck(accessProps, startDate, endDate);
                break;
            case SENSOR:
                if (!cmd.hasOption("use") || !cmd.hasOption("out") || !cmd.hasOption("cfg") || ((!cmd.hasOption("end") || !cmd.hasOption("start")) && !cmd.hasOption("lookback"))) {
                    message += "You must specify an output file (-out), the sensor (-use), the database access file (-cfg), and a date interval by (-start/-end) or -lookback" + "\n";
                    speak("You must specify an output file (-out), the sensor (-use), the database access file (-cfg), and a date interval by (-start/-end) or -lookback", ERROR);
                    break;
                }
                outFile = cmd.getOptionValue("out");
                sensor = cmd.getOptionValue("use");
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }

                if (cmd.hasOption("lookback")) {
                    try {
                        lb = Integer.parseInt(cmd.getOptionValue("lookback"));
                        endDate = new Date();
                        Calendar cal = new GregorianCalendar();
                        cal.add(Calendar.DATE, -lb);
                        startDate = cal.getTime();
                    } catch (NumberFormatException ex) {
                        message += "The number of days is invalid." + "\n";
                        speak("The number of days is invalid.", ERROR);
                        break;
                    }
                } else {
                    try {
                        startDate = dateFormat.parse(cmd.getOptionValue("start"));
                        endDate = dateFormat.parse(cmd.getOptionValue("end"));
                    } catch (ParseException ex) {
                        message += "The start and/or end date are not valid. Use format YYYY-mm-dd" + "\n";
                        speak("The start and/or end date are not valid. Use format YYYY-mm-dd", ERROR);
                        break;
                    }
                }
                makeSensorPNG(accessProps, sensor, df.format(startDate), df.format(endDate));
                break;
            case GRA:
                if (!cmd.hasOption("in") || !cmd.hasOption("out") || !cmd.hasOption("cfg") || ((!cmd.hasOption("end") || !cmd.hasOption("start")) && !cmd.hasOption("lookback"))) {
                    message += "You must specify an output file (-out), the GRA file (-in), the database access file (-cfg), and a date interval by (-start/-end) or -lookback" + "\n";
                    speak("You must specify an output file (-out), the GRA file (-in), the database access file (-cfg), and a date interval by (-start/-end) or -lookback", ERROR);
                    break;
                }
                outFile = cmd.getOptionValue("out");
                inputFile = cmd.getOptionValue("in");
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }

                if (cmd.hasOption("lookback")) {
                    try {
                        lb = Integer.parseInt(cmd.getOptionValue("lookback"));
                        endDate = new Date();
                        Calendar cal = new GregorianCalendar();
                        cal.add(Calendar.DATE, -lb);
                        startDate = cal.getTime();
                    } catch (NumberFormatException ex) {
                        message += "The number of days is invalid." + "\n";
                        speak("The number of days is invalid.", ERROR);
                        break;
                    }
                } else {
                    try {
                        startDate = dateFormat.parse(cmd.getOptionValue("start"));
                        endDate = dateFormat.parse(cmd.getOptionValue("end"));
                    } catch (ParseException ex) {
                        message += "The start and/or end date are not valid. Use format YYYY-mm-dd" + "\n";
                        speak("The start and/or end date are not valid. Use format YYYY-mm-dd", ERROR);
                        break;
                    }
                }
                makeGRA_PNG(accessProps, inputFile, startDate, endDate);
                break;
            case EVENTS:
                if (!cmd.hasOption("cfg")) {
                    message += "Cannot read database access file" + "\n";
                    speak("You must specify the database access file (-cfg)", ERROR);
                    break;
                }
                cfgFile = cmd.getOptionValue("cfg");
                accessProps = getPropsFrom(cfgFile);
                if (accessProps == null) {
                    message += "Cannot read database connection file" + "\n";
                    speak("Cannot read database connection file", ERROR);
                    break;
                }
                doEvents(accessProps);
                break;
            case CMAIL:
                if (!cmd.hasOption("out")) {
                    System.out.println("You must specify an output file (-out)"); // to STDOUT only becaus this will only be called from interactive session
                    return;
                }
                doMAIL(cmd.getOptionValue("out"));
                break;
            case CCFG:
                if (!cmd.hasOption("out")) {
                    System.out.println("You must specify an output file (-out)");// to STDOUT only becaus this will only be called from interactive session
                    return;
                }
                doCCFG(cmd.getOptionValue("out"));
                break;
            case CFMT:
                if (!cmd.hasOption("out")) {
                    System.out.println("You must specify an output file (-out)");// to STDOUT only becaus this will only be called from interactive session
                    return;
                }
                doFMT(cmd.getOptionValue("out"));
                break;
            case VERSION:
                showVersion();
                break;
        } // switch

        Properties mailProps = null;
        if (cmd.hasOption("mail")) {
            mailProps = getPropsFrom(cmd.getOptionValue("mail"));
            if (mailProps != null) {
                sendMail(mailProps, attachment);
            } else {
                speak("Could not send mail.", ERROR);
            }
        }
    }

    private String getAction(CommandLine cmd) {
        if (cmd.hasOption("action")) {
            return cmd.getOptionValue("action");
        } else {
            return null;
        }
    }

    private Properties getPropsFrom(String file) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("1c#*1112x1c2!!");
        Properties props = new EncryptableProperties(encryptor);
        try {
            FileInputStream in = new FileInputStream(file);
            props.load(in);
            in.close();
        } catch (Exception ex) {
            message += ex.getMessage() + "\n";
            speak(ex.getMessage(), ERROR);
            props = null;
        }
        return props;
    }

    private void storeProps(Properties props, String file) {
        try {
            FileOutputStream out = new FileOutputStream(file);
            props.store(out, "MoniSoft - Configfile");
            out.close();
        } catch (Exception ex) {
            speak(ex.getMessage(), ERROR);
        }
    }

    /**
     * Erzeugt einer PNG Datei aus einer bestehenden GRA-datei
     *
     * @param graFile
     */
    private void makeGRA_PNG(Properties accessProps, String graFile, final Date startDate, final Date endDate) {
        DBConnector conn = new DBConnector();
        if (conn.connectToDB(accessProps.getProperty("SERVER") + ":" + accessProps.getProperty("PORT"), accessProps.getProperty("DATABASE"), accessProps.getProperty("USER"), accessProps.getProperty("PASSWORD"), false, false)) {
            MoniSoft.getInstance().setDBConnector(conn);

            try {
                FileInputStream is = new FileInputStream(graFile);
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                ChartLoader loader = new ChartLoader(new NoOperationGUI(), new DateRangeProvider() {
                    @Override
                    public boolean useCustomPeriod() {
                        return true;
                    }

                    @Override
                    public DateInterval getCustomdateInterval() {
                        return new DateInterval(startDate, endDate);
                    }
                });

                loader.loadStoredChart(isr, null);
                isr.close();

                attachment = new EmailAttachment();
                attachment.setPath(getOutputFileName());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("GRA-Output from " + graFile);
                attachment.setName(getOutputFileName());


            } catch (Exception e) {
                Messages.showException(e);
            }
        } else {
            message += "Datenbankverbindung bei Grafikerzeugung fehlgeschlagen\n";
            speak("Datenbankverbindung fehlgeschlagen", ERROR);
        }
    }

    /**
     * Erzeugt eine einfache PNG-Grafik für den übergebenen Messpunkt
     */
    private void makeSensorPNG(Properties accessProps, String sensor, String startTime, String endTime) {
        DBConnector conn = new DBConnector();
        if (conn.connectToDB(accessProps.getProperty("SERVER") + ":" + accessProps.getProperty("PORT"), accessProps.getProperty("DATABASE"), accessProps.getProperty("USER"), accessProps.getProperty("PASSWORD"), false, false)) {
            MoniSoft.getInstance().setDBConnector(conn);



            try {
                InputStreamReader isr = new InputStreamReader(MoniSoft.class.getResourceAsStream("/de/jmonitoring/SupplementaryFiles/TimeSeriesDescriberDummy.gra"), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                OutputStreamWriter ow = new OutputStreamWriter(out, "UTF-8");
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.replace("<CL_SENSORKEY>", sensor);
                    line = line.replace("<CL_SENSORID>", String.valueOf(SensorInformation.getSensorIDFromNameORKey(sensor)));
                    line = line.replace("<CL_SENSORUNIT>", SensorInformation.getSensorProperties(SensorInformation.getSensorIDFromNameORKey(sensor)).getSensorUnit().getUnit());
                    line = line.replace("<CL_STARTDATE>", startTime);
                    line = line.replace("<CL_ENDDATE>", endTime);
                    ow.write(line + "\n");
                }

                ow.flush();
                ByteArrayInputStream new_is = new ByteArrayInputStream(out.toByteArray());
                InputStreamReader new_isr = new InputStreamReader(new_is, "UTF-8");
                ChartLoader loader = new ChartLoader(new NoOperationGUI(), new DateRangeProvider() {
                    @Override
                    public boolean useCustomPeriod() {
                        return false;
                    }

                    @Override
                    public DateInterval getCustomdateInterval() {
                        return null;
                    }
                });

                loader.loadStoredChart(new_isr, null);
                new_is.close();
                isr.close();

                attachment = new EmailAttachment();
                attachment.setPath(getOutputFileName());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("Sensor-Output for " + sensor);
                attachment.setName(getOutputFileName());
            } catch (Exception e) {
                Messages.showException(e);
            }
        } else {
            message += "Datenbankverbindung bei Grafikerzeugung fehlgeschlagen" + "\n";
            speak("Datenbankverbindung fehlgeschlagen", ERROR);
        }
    }

    /**
     * Verschickt eine Mail mit Fehlerbenachrichtigungrn
     *
     * @param props The properties of the mail to be sent
     */
    private void sendMail(Properties props, EmailAttachment attachment) {
        MultiPartEmail email = new MultiPartEmail();
        email.setHostName(props.getProperty("SMTP-SERVER"));
        email.setSmtpPort(Integer.parseInt(props.getProperty("SMTP-PORT")));
        email.setTLS(false);
//        email.setDebug(true);


        if (!props.getProperty("SMTP-USER").isEmpty() && !props.getProperty("SMTP-PASSWORD").isEmpty()) {
            email.setAuthenticator(new DefaultAuthenticator(props.getProperty("SMTP-USER"), props.getProperty("SMTP-PASSWORD")));
            email.setTLS(true);
        }

        try {
            email.setFrom(props.getProperty("FROM"));
            email.setSubject(props.getProperty("SUBJECT"));
            email.setMsg("MoniSoft messages:\n\n" + message + "\n");

            if (attachment != null) {
                email.attach(attachment);
            }

            String[] recipients = props.getProperty("TO").split(",");
            for (int i = 0; i < recipients.length; i++) {
                email.addTo(recipients[i]);
            }
            email.send();
        } catch (EmailException ex) {
            Messages.showException(ex);
        } catch (Exception e) {
            Messages.showException(e);
        }

    }

    private void doMonAction(String filename, Properties accessProps) {
        File file = new File(filename);
        if (!file.exists() || file.length() < FILE_LIMIT) {
            message += "Die MON-Datei '" + filename + "' ist nicht vorhanden oder leer.\n";
            speak("Die CSV-Datei '" + filename + "' ist nicht vorhanden oder leer.", ERROR);
        } else {
            DBConnector conn = new DBConnector();
            MoniSoft.getInstance().setDBConnector(conn);
            if (conn.connectToDB(accessProps.getProperty("SERVER") + ":" + accessProps.getProperty("PORT"), accessProps.getProperty("DATABASE"), accessProps.getProperty("USER"), accessProps.getProperty("PASSWORD"), false, false)) {
                MONDataImporter importer = new MONDataImporter(conn);
                importer.importMON(file, "UTF-8");
                String count = importer.getImportedCount() == 0 ? "Keine" : String.valueOf(importer.getImportedCount());

                if (count.equals("-1")) {
                    message += "Die Datei [" + filename + "] enthält keine Datensätze\t(" + filename + ")" + "\n";
                } else {
                    message += count + " Datensätze importiert\t(" + filename + ")" + "\n";
                }

                speak(message, INFO);
                conn.disconnectFromDB();
            } else {
                message += "Datenbankverbindung MON-Import fehlgeschlagen\n";
                speak("Datenbankverbindung fehlgeschlagen", ERROR);
            }
        }
    }

    private void doCSVAction(String filename, Properties accessProps, Properties formatProps) {
        File file = new File(filename);
        if (!file.exists() || file.length() < FILE_LIMIT) {
            message += "Die CSV-Datei [" + filename + "] ist nicht vorhanden oder leer.\n";
            speak("Die CSV-Datei [" + filename + "] ist nicht vorhanden oder leer.", ERROR);
        } else {
            DBConnector conn = new DBConnector();
            MoniSoft.getInstance().setDBConnector(conn);
            if (conn.connectToDB(accessProps.getProperty("SERVER") + ":" + accessProps.getProperty("PORT"), accessProps.getProperty("DATABASE"), accessProps.getProperty("USER"), accessProps.getProperty("PASSWORD"), false, false)) {
                CSVDataImporter importer = new CSVDataImporter(null, conn);

                Integer timeColumn = null;
                try {
                    if (!formatProps.getProperty("TIME_COLUMN").isEmpty()) {
                        timeColumn = Integer.valueOf(formatProps.getProperty("TIME_COLUMN"));
                    }
                } catch (Exception e) {
                    speak("Invalid entry for time column in format config.", WARN);
                }

                Integer header_line = 1;
                try {
                    header_line = Integer.valueOf(formatProps.getProperty("HEADER_LINE"));
                } catch (Exception e) {
                    speak("Invalid entry for header line in format config. Default to 1", WARN);
                }

                Integer first_line = 2;
                try {
                    first_line = Integer.valueOf(formatProps.getProperty("FIRST_IMPORT_LINE"));
                } catch (Exception e) {
                    speak("Invalid entry for first data line in format config. Default to 2", WARN);
                }

                Integer dateColumn = 1;
                try {
                    dateColumn = Integer.valueOf(formatProps.getProperty("DATE_COLUMN"));
                } catch (Exception e) {
                    speak("Invalid entry for date column in format config. Default to 1", WARN);
                }

                char quote = formatProps.getProperty("QUOTE").isEmpty() ? '"' : formatProps.getProperty("QUOTE").charAt(0);
                char divider = formatProps.getProperty("DIVIDER").isEmpty() ? ';' : formatProps.getProperty("DIVIDER").charAt(0);
                String timeFmt = formatProps.getProperty("TIME_FORMAT").isEmpty() ? null : formatProps.getProperty("TIME_FORMAT");
                String dateFmt = formatProps.getProperty("DATE_FORMAT").isEmpty() ? null : formatProps.getProperty("DATE_FORMAT");

                Integer importedCount = importer.loadCSVFiletoDB(file, header_line, first_line, dateColumn, timeColumn, divider, quote, dateFmt, timeFmt, true);

                if (null == importedCount) {
                    message += importer.getErrorReason() + "\n";
                    speak("Fehler beim CSV-Import von [" + filename + "] " + importer.getErrorReason(), ERROR);
                } else {
                    message += importedCount + " Messpunkte importiert aus [" + filename + "]\n";
                    speak(importedCount + " Messpunkte importiert aus [" + filename + "]", INFO);
                }

                for (String failed : importer.getFailed()) {
                    message += "Unbekannter Messpunkt in Datei [" + filename + "]: '" + failed + "'\n";
                    speak("Unbekannter Messpunkt in Datei [" + filename + "]: '" + failed + "'", WARN);
                }

                conn.disconnectFromDB();
            } else {
                message += "Datenbankverbindung bei CSV-Import fehlgeschlagen\n";
                speak("Datenbankverbindung bei CSV-Import fehlgeschlagen", ERROR);
            }
        }
    }

    private void doBackup(Properties daemonProps) {
        // Testen ob mysqldump installiert ist
        DatabaseBackup backup = new DatabaseBackup(new NoOperationGUI());
        if (!backup.testForMysqlDump()) {
            message += "mysqldump konnte nicht gefunden werden. Bitte installieren." + "\n";
            speak("\"mysqldump\" konnte nicht gefunden werden. Bitte installieren.", ERROR);
        } else {
            DBConnector backupConn = new DBConnector();
            MoniSoft.getInstance().setDBConnector(backupConn);
            if (backupConn.connectToDB(daemonProps.getProperty("SERVER") + ":" + daemonProps.getProperty("PORT"), daemonProps.getProperty("DATABASE"), daemonProps.getProperty("USER"), daemonProps.getProperty("PASSWORD"), false, false)) {
                backup.runBackup(backupConn, true);
            } else {
                message += "Datenbankverbindung bei Backup fehlgeschlagen\n";
                speak("Datenbankverbindung bei Backup fehlgeschlagen", ERROR);
            }
        }
    }

    private void doCheck(Properties daemonProps, Date startDate, Date endDate) {
        DBConnector conn = new DBConnector();
        MoniSoft.getInstance().setDBConnector(conn);
        if (conn.connectToDB(daemonProps.getProperty("SERVER") + ":" + daemonProps.getProperty("PORT"), daemonProps.getProperty("DATABASE"), daemonProps.getProperty("USER"), daemonProps.getProperty("PASSWORD"), false, false)) {
            ValueTests tests = new ValueTests(null);
            message += tests.checkAllRegularity(new DateInterval(startDate, endDate));
        } else {
            message += "Datenbankverbindung bei Datenüberprüfung fehlgeschlagen\n";
            speak("Datenbankverbindung bei Datenüberprüfung fehlgeschlagen", ERROR);
        }
        speak(message, INFO);
    }

    private void doCCFG(String filename) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("1c#*1112x1c2!!");
        Properties props = new EncryptableProperties(encryptor);

        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.print("Datenbank-Server IP:");
            props.setProperty("SERVER", userInput.readLine());
            System.out.print("Datenbank-Server Port [3306]:");
            String portString = userInput.readLine();
            portString = portString.isEmpty() ? "3306" : portString;
            props.setProperty("PORT", portString);
            System.out.print("Datenbankname:");
            props.setProperty("DATABASE", userInput.readLine());
            System.out.print("Nutzername:");
            props.setProperty("USER", userInput.readLine());
            System.out.print("Datenbankpasswort:");
            props.setProperty("PASSWORD", "ENC(" + encryptor.encrypt(userInput.readLine()) + ")");
        } catch (Exception e) {
            Messages.showException(e);
        }
        storeProps(props, filename);
    }

    private void doMAIL(String filename) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("1c#*1112x1c2!!");
        Properties props = new EncryptableProperties(encryptor);
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
            System.out.print("SMTP-SERVER:");
            props.setProperty("SMTP-SERVER", userInput.readLine());
            System.out.print("SMTP-PORT:");
            props.setProperty("SMTP-PORT", userInput.readLine());
            System.out.print("Absender:");
            props.setProperty("FROM", userInput.readLine());
            System.out.print("Empfänger (durch Komma getrennt):");
            props.setProperty("TO", userInput.readLine());
            System.out.print("Betreff:");
            props.setProperty("SUBJECT", userInput.readLine());
            System.out.print("SMTP-User (leer wenn nicht erforderlich:)");
            props.setProperty("SMTP-USER", userInput.readLine());
            System.out.print("SMTP-PASSWORD (leer wenn nicht erforderlich:)");
            props.setProperty("SMTP-PASSWORD", "ENC(" + encryptor.encrypt(userInput.readLine()) + ")");
        } catch (Exception e) {
            Messages.showException(e);
        }
        storeProps(props, filename);
    }

    private void doFMT(String filename) {
        Properties props = new Properties();
        BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Feldtrenner:");
            props.setProperty("DIVIDER", userInput.readLine());
            System.out.print("Quote:");
            props.setProperty("QUOTE", userInput.readLine());
            System.out.print("Datumsformat:");
            props.setProperty("DATE_FORMAT", userInput.readLine());
            System.out.print("Zeitformat:");
            props.setProperty("TIME_FORMAT", userInput.readLine());
            System.out.print("Spaltennummer des Datums:");
            props.setProperty("DATE_COLUMN", userInput.readLine());
            System.out.print("Spaltennummer der Uhrzeit:");
            props.setProperty("TIME_COLUMN", userInput.readLine());
            System.out.print("Zeilennummer der Spaltenköpfe:");
            props.setProperty("HEADER_LINE", userInput.readLine());
            System.out.print("Zeilennummer der ersten Datenzeile:");
            props.setProperty("FIRST_IMPORT_LINE", userInput.readLine());
        } catch (Exception e) {
            Messages.showException(e);
        }
        storeProps(props, filename);
    }

    private void doMonthly(Properties accessProps, Integer month, Integer year) {
        DBConnector conn = new DBConnector();
        MoniSoft.getInstance().setDBConnector(conn);
        MonthlyUsageCalculator muc = new MonthlyUsageCalculator(new NoOperationGUI());
        if (conn.connectToDB(accessProps.getProperty("SERVER") + ":" + accessProps.getProperty("PORT"), accessProps.getProperty("DATABASE"), accessProps.getProperty("USER"), accessProps.getProperty("PASSWORD"), false, false)) {
            muc.startWriteAllMonthlyUsage(month, year, false, new Object());
        } else {
            message += "Datenbankverbindung bei Monatsverbrauchsberechnung fehlgeschlagen\n";
            speak("Datenbankverbindung bei Monatsverbrauchsberechnung fehlgeschlagen", ERROR);
        }
    }

    private void doEvents(Properties daemonProps) {
        DBConnector conn = new DBConnector();
        MoniSoft.getInstance().setDBConnector(conn);
        if (conn.connectToDB(daemonProps.getProperty("SERVER") + ":" + daemonProps.getProperty("PORT"), daemonProps.getProperty("DATABASE"), daemonProps.getProperty("USER"), daemonProps.getProperty("PASSWORD"), false, false)) {
            Object syncObject = new Object();
            synchronized (syncObject) {
                EventReconstructor er = new EventReconstructor(new NoOperationGUI());
                er.startReconstructionAll(syncObject);
                try {
                    syncObject.wait();


                } catch (InterruptedException ex) {
                    logger.error(MoniSoftCommandline.class.getName(), ex);
                }
            }
            conn.disconnectFromDB();
        } else {
            message += "Datenbankverbindung bei Eventrekonstruktion fehlgeschlagen\n";
            speak("Datenbankverbindung bei Eventrekonstruktion fehlgeschlagen", ERROR);
        }
    }

    private void initializeLogger(String logFile) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyy-MM-dd HH:mm:ss} %-5level CLI %msg%n");
        encoder.start();

        logger.detachAppender("MAINAPPENDER");

        FileAppender fileAppender = new FileAppender();
        fileAppender.setName("MoniSoftFileAppender");
        fileAppender.setContext(loggerContext);
        fileAppender.setFile(logFile);
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        logger.addAppender(fileAppender);
        logger.setLevel(Level.INFO);
    }

    private void speak(String text, int level) {
        switch (level) {
            case ERROR:
                logger.error(text);
                break;
            case INFO:
                logger.info(text);
                break;
            case WARN:
                logger.warn(text);
        }
        System.out.println(text);
    }

    private void showVersion() {
        System.out.println("Version: " + MoniSoftConstants.getVersion());
    }
}
