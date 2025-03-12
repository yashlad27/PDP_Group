package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import model.event.Event;

/**
 * Utility class for exporting events to csv format.
 */
public class CSVExporter {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");
  private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern(
      "yyyy-MM-dd");
  private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

  /**
   * Exports events to a CSV file in Google Calendar format.
   *
   * @param filePath the path to save the CSV file
   * @param events   the events to export
   * @return the absolute path of the saved file, or null if export failed
   */
  public static String exportToCSV(String filePath, List<Event> events) {
    try (FileWriter writer = new FileWriter(filePath)) {
      // Write CSV header
      writer.write(
          "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private\n");

      // Write each event
      for (Event event : events) {
        StringBuilder line = new StringBuilder();

        // Subject
        line.append(escapeCSV(event.getSubject())).append(",");

        // Determine if it's an all-day event
        boolean isAllDay = event.isAllDay();

        // Start Date & Time
        if (isAllDay && event.getDate() != null) {
          // All-day event with date
          line.append(event.getDate().format(DATE_FORMAT)).append(",");
          line.append(","); // No start time for all-day events
        } else if (event.getStartDateTime() != null) {
          // Regular event with start date/time
          line.append(event.getStartDateTime().format(DATE_FORMAT)).append(",");
          line.append(event.getStartDateTime().format(TIME_FORMAT)).append(",");
        } else {
          // Fallback (should not happen)
          line.append(",").append(",");
        }

        // End Date & Time
        if (isAllDay && event.getDate() != null) {
          // All-day event with date
          line.append(event.getDate().format(DATE_FORMAT)).append(",");
          line.append(","); // No end time for all-day events
        } else if (event.getEndDateTime() != null) {
          // Regular event with end date/time
          line.append(event.getEndDateTime().format(DATE_FORMAT)).append(",");
          line.append(event.getEndDateTime().format(TIME_FORMAT)).append(",");
        } else if (event.getStartDateTime() != null) {
          // Event with start time but no end time
          line.append(event.getStartDateTime().format(DATE_FORMAT)).append(",");
          line.append(event.getStartDateTime().format(TIME_FORMAT)).append(",");
        } else {
          // Fallback (should not happen)
          line.append(",").append(",");
        }

        // All Day Event
        line.append(isAllDay ? "True" : "False").append(",");

        // Description
        line.append(escapeCSV(event.getDescription() != null ? event.getDescription() : ""))
            .append(",");

        // Location
        line.append(escapeCSV(event.getLocation() != null ? event.getLocation() : "")).append(",");

        // Private
        line.append(!event.isPublic() ? "True" : "False").append("\n");

        writer.write(line.toString());
      }

      File file = new File(filePath);
      return file.getAbsolutePath();
    } catch (IOException e) {
      System.err.println("Error exporting to CSV: " + e.getMessage());
      return null;
    }
  }

  /**
   * Formats events for display in print commands.
   *
   * @param events    the events to format
   * @param singleDay whether this is for a single day view
   * @return a formatted string representation of the events
   */
  public static String formatEventsForDisplay(List<Event> events, boolean singleDay) {
    if (events.isEmpty()) {
      return "No events found.";
    }

    StringBuilder result = new StringBuilder();

    for (Event event : events) {
      result.append("• ").append(event.getSubject());

      if (event.isAllDay()) {
        result.append(" (All day");
        if (!singleDay && event.getDate() != null) {
          result.append(" on ").append(event.getDate().format(DISPLAY_DATE_FORMAT));
        }
        result.append(")");
      } else if (event.getStartDateTime() != null) {
        result.append(" - ");

        // If showing multiple days and the event spans multiple days, show full dates
        if (!singleDay) {
          result.append(event.getStartDateTime().format(DISPLAY_DATE_FORMAT)).append(" ");
        }

        result.append(event.getStartDateTime().format(DISPLAY_TIME_FORMAT));

        if (event.getEndDateTime() != null) {
          result.append(" to ");

          // If event ends on a different day, include the end date
          if (!event.getStartDateTime().toLocalDate()
              .equals(event.getEndDateTime().toLocalDate())) {
            result.append(event.getEndDateTime().format(DISPLAY_DATE_FORMAT)).append(" ");
          }

          result.append(event.getEndDateTime().format(DISPLAY_TIME_FORMAT));
        }
      }

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(" at ").append(event.getLocation());
      }

      result.append("\n");
    }

    return result.toString();
  }

  /**
   * Escapes a value for CSV output.
   *
   * @param value the value to escape
   * @return the escaped value
   */
  private static String escapeCSV(String value) {
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