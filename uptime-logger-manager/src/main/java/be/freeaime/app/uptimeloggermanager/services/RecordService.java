package be.freeaime.app.uptimeloggermanager.services;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import be.freeaime.app.uptimeloggermanager.Config;

public class RecordService {
    private static class Holder {
        private static final RecordService INSTANCE = new RecordService();
    }

    public static RecordService getInstance() {
        return Holder.INSTANCE;
    }

    private final List<String[]> uptimeRecords = new ArrayList<>();
    private static final String pathToLogFileString = String.format("%s/%s", Config.SERVICE_JAR_DST_DIR,
            Config.SERVICE_LOGGER_FILE_NAME);

    private RecordService() {

    }

    private static boolean recordHasInvalidValues(String[] uptimeRecord) {
        try {
            for (int i = 0; i < 2; i++) {
                Long.parseLong(uptimeRecord[i]);
            }
            return false;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return true;
        }
    }

    public static String getRecords() {
        final List<String[]> uptimeRecords = getInstance().uptimeRecords;
        uptimeRecords.clear();

        uptimeRecords.addAll(getInstance().readCSV(pathToLogFileString));

        final StringBuilder recordListStringBuilder = new StringBuilder();
        if (uptimeRecords.size() > 0) {
            final DateTimeFormatter bootTimeFormatter = DateTimeFormatter.ofPattern("yyy-MM-dd");
            for (final String[] record : uptimeRecords) {
                final boolean uptimeRecordIsInvalid = recordHasInvalidValues(record);
                if (uptimeRecordIsInvalid) {
                    continue;
                }

                final long bootTime = Long.parseLong(record[0]);
                final long lastRecordedTime = Long.parseLong(record[1]);
                final LocalDateTime bootDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(bootTime),
                        ZoneId.systemDefault());
                final LocalDateTime lastRecordedDateTime = LocalDateTime
                        .ofInstant(Instant.ofEpochSecond(lastRecordedTime), ZoneId.systemDefault());

                final Duration duration = Duration.between(bootDateTime, lastRecordedDateTime);

                final long daysBetween = ChronoUnit.DAYS.between(bootDateTime, lastRecordedDateTime);
                final long hoursBetween = duration.toHours() % 24;
                final long minutesBetween = duration.toMinutes() % 60;
                final String formattedDays = String.format("%04d", daysBetween);
                final String formattedHours = String.format("%02d", hoursBetween);
                final String formattedMinutes = String.format("%02d", minutesBetween);
                final String formattedDuration = String.format("%sDays # %sH:%sM", formattedDays, formattedHours,
                        formattedMinutes);

                final LocalDate localDate = LocalDate.ofInstant(Instant.ofEpochSecond(bootTime),
                        ZoneId.systemDefault());
                final String formattedDateString = localDate.format(bootTimeFormatter);

                final String recordString = String.format("%s # %s\n", formattedDateString, formattedDuration);
                recordListStringBuilder.append(recordString);
            }

        }
        return recordListStringBuilder.toString();
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
}
