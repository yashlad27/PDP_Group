package utilities;

import model.event.Event;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CSVExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

  public boolean exportToCSV(String filePath, List<Event> events) {
    try (FileWriter writer = new FileWriter(filePath)) {
      // Write CSV header
      writer.write("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");

      // Write each event
      for (Event event : events) {
        StringBuilder line = new StringBuilder();

        // Subject
        line.append(escapeCSV(event.getSubject())).append(",");

        // Start Date & Time
        line.append(event.getStartDateTime().format(DATE_FORMAT)).append(",");

        if (event.isAllDay()) {
          line.append(","); // No start time for all-day events
        } else {
          line.append(event.getStartDateTime().format(TIME_FORMAT)).append(",");
        }

        // End Date & Time
        line.append(event.getEndDateTime().format(DATE_FORMAT)).append(",");

        if (event.isAllDay()) {
          line.append(","); // No end time for all-day events
        } else {
          line.append(event.getEndDateTime().format(TIME_FORMAT)).append(",");
        }

        // All Day Event
        line.append(event.isAllDay() ? "True" : "False").append(",");

        // Description
        line.append(escapeCSV(event.getDescription() != null ? event.getDescription() : "")).append(",");

        // Location
        line.append(escapeCSV(event.getLocation() != null ? event.getLocation() : "")).append(",");

        // Private
        line.append(!event.isPublic() ? "True" : "False").append("\n");

        writer.write(line.toString());
      }

      return true;
    } catch (IOException e) {
      System.err.println("Error exporting to CSV: " + e.getMessage());
      return false;
    }
  }

  private String escapeCSV(String value) {
    if (value == null) {
      return "";
    }

    // If the value contains comma, newline, or double quote, enclose in quotes
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      // Replace double quotes with two double quotes
      String escaped = value.replace("\"", "\"\"");
      return "\"" + escaped + "\"";
    }

    return value;
  }
}