package be.freeaime.app.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class RecordManager {
    private static class Holder {
        private static final RecordManager INSTANCE = new RecordManager();
    }

    public static RecordManager getInstance() {
        return Holder.INSTANCE;
    }

    private final List<String[]> uptimeRecords;
    private final String bootTimeInEpochSecondString = getBootTimeInEpochSeconds();
    private final String logFileName = "uptime.csv";
    private final String workingDir = getJarDir();
    private final String pathToLogFileString = String.format("%s/%s", workingDir, logFileName);

    private final int currentRecordIndex;

    private RecordManager() {
        System.out.println("working directory: " + workingDir);
        if (workingDir == null) {
            System.err.println("error while getting working directory");
            System.exit(1);
        }
        uptimeRecords = readCSV(pathToLogFileString);
        if (uptimeRecords.size() == 0) {
            String[] header = { "sTime", "eTime" };
            uptimeRecords.add(header);
            uptimeRecords.add(getNewUptimeRecord());
        }
        final int lastRecordIndex = uptimeRecords.size() - 1;
        final String[] lastUptimeRecord = uptimeRecords.get(lastRecordIndex);
        final boolean lastUptimeRecordIsInvalid = recordHasInvalidValues(lastUptimeRecord);
        if (lastUptimeRecordIsInvalid) {
            uptimeRecords.add(getNewUptimeRecord());
        } else {
            // TODO:done typo, Rename hasBootTimeChanged
            final boolean bootTimeHasChanged = hasBootTimeChanged(lastUptimeRecord);
            if (bootTimeHasChanged) {
                uptimeRecords.add(getNewUptimeRecord());
            } else {
                uptimeRecords.set(lastRecordIndex, getNewUptimeRecord());
            }
        }
        saveUptimeRecords(pathToLogFileString, uptimeRecords);
        currentRecordIndex = uptimeRecords.size() - 1;
    }

    public static void updateRecord() {
        getInstance()._updateRecord();
    }

    private void _printAllUptimeRecords() {
        System.out.println(EntryPointULS.ruler);
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.uptimeRecords.forEach(record -> {
            final long bootTime=Long.parseLong(record[0]);
            // final Instant instant = Instant.ofEpochSecond(bootTime);
            // final ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
            // final LocalDate localDate = zonedDateTime.toLocalDate();
            final LocalDate localDate=LocalDate.ofInstant(Instant.ofEpochSecond(bootTime),ZoneId.systemDefault());
            System.out.println(localDate.format(formatter));
            printRecord(record);
        });
        System.out.println(EntryPointULS.ruler);
    }

    public static void printAllUptimeRecords() {
        getInstance()._printAllUptimeRecords();
    }

    private void _printUptime() {
        System.out.println(EntryPointULS.ruler);
        printRecord(getNewUptimeRecord());
        System.out.println(EntryPointULS.ruler);
    }

    /**
     * prints current uptime
     */
    public static void printUptimeRecord() {
        getInstance()._printUptime();
    }

    private void _updateRecord() {
        final String[] updatedRecord = getNewUptimeRecord();
        uptimeRecords.set(currentRecordIndex, updatedRecord);
        saveUptimeRecords(pathToLogFileString, uptimeRecords);
    }

    // private String getFormattedTimeString() { 
    //     LocalTime currentTime = LocalTime.now();
    //     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    //     String formattedTime = currentTime.format(formatter);
    //     return formattedTime;
    // }

    private void printRecord(String[] uptimeRecord) {
        try {
            final long startTime = Long.parseLong(uptimeRecord[0]);
            final long endTime = Long.parseLong(uptimeRecord[1]);
            final Instant startInstant = Instant.ofEpochSecond(startTime);
            final Instant endInstant = Instant.ofEpochSecond(endTime);
            final long secondsBetween = Duration.between(startInstant, endInstant).getSeconds();
            final long days = secondsBetween / 86400;
            final long hours = (secondsBetween % 86400) / 3600;
            final long minutes = (secondsBetween % 3600) / 60;
            final long seconds = secondsBetween % 60;
            final String output = (String.format("uptime %d days, %d hours, %d minutes, %d seconds.", days, hours,
                    minutes,
                    seconds)).toUpperCase();
            System.out.println(output);
        } catch (NumberFormatException | DateTimeException | ArithmeticException e) {
        }
    }

    private void save(String filePath, List<String[]> resultString) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            writer.writeAll(resultString); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveUptimeRecords(
            final String pathToLogFileString,
            final List<String[]> uptimeRecords) {
        // final int lastIndex = uptimeRecords.size() - 1; 
        // printRecord( uptimeRecords.get(lastIndex));
        save(pathToLogFileString, uptimeRecords);
    }

    private boolean hasBootTimeChanged(String[] lastUptimeRecord) {
        return !bootTimeInEpochSecondString.equals(lastUptimeRecord[0]);
    }

    public static boolean recordHasInvalidValues(String[] uptimeRecord) {
        try {
            for (int i = 0; i < 2; i++) {
                Long.parseLong(uptimeRecord[i]);
            }
            return false;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    private String[] getNewUptimeRecord() {
        final String newlyRecordedTimeInEpochSecondString = String.valueOf(Instant.now().getEpochSecond());
        return new String[] { bootTimeInEpochSecondString, newlyRecordedTimeInEpochSecondString };
    }

    /**
     * 
     * @return epoch time in seconds as a String or empty String if there are
     *         any issue with getting boot time
     */
    private String getBootTimeInEpochSeconds() {
        final String epochTimeFilePath = "/proc/stat";
        final String delimiter = " ";
        try (BufferedReader br = new BufferedReader(new FileReader(epochTimeFilePath))) {
            String currentLine;
            while ((currentLine = br.readLine()) != null) {
                if (currentLine.startsWith("btime")) {
                    final String split[] = currentLine.split(delimiter);
                    final int epochTimeIndex = 1;
                    final String bootTimeInEpochSeconds = split[epochTimeIndex];
                    return bootTimeInEpochSeconds;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }

    /**
     * Method to read a CSV file and print its content
     * 
     * @param filePath The path to the CSV file
     * @return return List<String[]> of size 0 if there are any issue open the file
     *         path
     */
    private List<String[]> readCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            final List<String[]> allRows = reader.readAll();
            return allRows;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 
     * @return path string or null
     */
    private String getJarDir() {
        try {
            // Get the URI of the JAR file
            File jarFile = new File(EntryPointULS.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // Get the directory of the JAR file
            return jarFile.getParentFile().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
