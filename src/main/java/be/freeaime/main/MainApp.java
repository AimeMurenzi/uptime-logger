package be.freeaime.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

public class MainApp {
    private static final String bootTimeInEpochSecondString = getBootTimeInEpochSeconds();

    public static void main(String... args) throws IOException {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("-v") || args[0].equalsIgnoreCase("--version")) {
                printAppVersion();
                return;
            } else {
                System.out.println("-v --version Displays application version");
                return;
            }
        }

        printAppVersion();
        final String osName = System.getProperty("os.name").toLowerCase();
        final boolean serviceIsNotRunningOnLinux = !osName.contains("linux");
        if (serviceIsNotRunningOnLinux) {
            System.out.println("This uptime logging service only runs on Linux distributions currently. Exiting");
            return;
        }

        final String workingDir = getJarDir();
        System.out.println("working directory: " + workingDir);
        if (workingDir == null) {
            System.err.println("error while getting working directory");
            System.exit(1);
        }
        final String logFileName = "uptime.csv";
        final String pathToLogFileString = String.format("%s/%s", workingDir, logFileName);
        final List<String[]> uptimeRecords = readCSV(pathToLogFileString);
        if (uptimeRecords.size() == 0) {
            String[] header = { "sTime", "eTime" };
            uptimeRecords.add(header);
            uptimeRecords.add(getNewUptimeRecord());
        }
        // TODO: done rename lastRecordedBootTimeInEpochMillisecondsString
        // TODO: done account for null values
        // TODO: done fix record length => length >=2
        // TODO: done fix possible index out of bound issues for record
        // TODO: done rename logUptime => saveUptimeRecords
        // TODO: done astRecordedBootTimeInEpochSecondString can not be null
        final int lastRecordIndex = uptimeRecords.size() - 1;
        final String[] lastUptimeRecord = uptimeRecords.get(lastRecordIndex);
        final boolean lastUptimeRecordIsInvalid = recordHasInvalidValues(lastUptimeRecord);
        if (lastUptimeRecordIsInvalid) {
            uptimeRecords.add(getNewUptimeRecord());
        } else {
            // TODO: done fix over writing previous record
            final boolean bootTimeHasChanged = hasBooTimeChanged(lastUptimeRecord);
            if (bootTimeHasChanged) {
                uptimeRecords.add(getNewUptimeRecord());
            } else {
                uptimeRecords.set(lastRecordIndex, getNewUptimeRecord());
            }
        }
        saveUptimeRecords(pathToLogFileString, uptimeRecords);
        final long fiveMinutes = 300000;
        final int currentRecordIndex = uptimeRecords.size() - 1;
        while (true) {
            try {
                Thread.sleep(fiveMinutes);
                final String[] updatedRecord = getNewUptimeRecord();
                uptimeRecords.set(currentRecordIndex, updatedRecord);
                saveUptimeRecords(pathToLogFileString, uptimeRecords);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] getNewUptimeRecord() {
        final String newlyRecordedTimeInEpochSecondString = String.valueOf(Instant.now().getEpochSecond());
        return new String[] { bootTimeInEpochSecondString, newlyRecordedTimeInEpochSecondString };
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

    private static void printAppVersion() {
        final String title = PropertyUtil.getProperty("app.name");
        final String version = PropertyUtil.getProperty("app.version");
        // final String
        // ruler="------------------------------------------------------------------------";
        final String ruler = "════════════════════════════════════════════════════════════════════════";
        if (title != null && version != null) {
            System.out.println(ruler);
            System.out.println(title.toUpperCase());
            System.out.println("VERSION " + version);
            System.out.println(ruler);
        }
    }

    private static boolean hasBooTimeChanged(String[] lastUptimeRecord) {
        return !bootTimeInEpochSecondString.equals(lastUptimeRecord[0]);
    }

    private static void outputUptime(String sTime, String eTime) {
        try {
            final long startTime = Long.parseLong(sTime);
            final long endTime = Long.parseLong(eTime);
            final Instant startInstant = Instant.ofEpochSecond(startTime);
            final Instant endInstant = Instant.ofEpochSecond(endTime);
            final long secondsBetween = Duration.between(startInstant, endInstant).getSeconds();
            final long days = secondsBetween / 86400;
            final long hours = (secondsBetween % 86400) / 3600;
            final long minutes = (secondsBetween % 3600) / 60;
            final long seconds = secondsBetween % 60;
            System.out.format("uptime %d days, %d hours, %d minutes, %d seconds.\n", days, hours, minutes, seconds);
        } catch (NumberFormatException | DateTimeException | ArithmeticException e) {
        }
    }

    private static void saveUptimeRecords(
            final String pathToLogFileString,
            final List<String[]> uptimeRecords) {
        final int lastIndex = uptimeRecords.size() - 1;
        final String newlyRecordedTimeInEpochSecondsString = uptimeRecords.get(lastIndex)[1];
        final String lastRecordedBootTimeInEpochSecondsString = uptimeRecords.get(lastIndex)[0];
        outputUptime(lastRecordedBootTimeInEpochSecondsString, newlyRecordedTimeInEpochSecondsString);
        save(pathToLogFileString, uptimeRecords);
    }

    /**
     * 
     * @return epoch time in seconds as a String or empty String if there are
     *         any issue with getting boot time
     */
    private static String getBootTimeInEpochSeconds() {
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

    public static String getFormattedTimeString() {
        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        String formattedTime = currentTime.format(formatter);
        return formattedTime;
    }

    /**
     * 
     * @return path string or null
     */
    public static String getJarDir() {
        try {
            // Get the URI of the JAR file
            File jarFile = new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            // Get the directory of the JAR file
            return jarFile.getParentFile().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void save(String filePath, List<String[]> resultString) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath),
                CSVWriter.DEFAULT_SEPARATOR,
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END)) {
            writer.writeAll(resultString);
            System.out.println(
                    String.format("[%s] CSV file was saved successfully at %s", getFormattedTimeString(), filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to read a CSV file and print its content
     * 
     * @param filePath The path to the CSV file
     * @return return List<String[]> of size 0 if there are any issue open the file
     *         path
     */
    public static List<String[]> readCSV(String filePath) {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            final List<String[]> allRows = reader.readAll();
            return allRows;
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
