package controller.command;

import model.calendar.ICalendar;
import model.event.Event;
import utilities.DateTimeUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Command for printing events on a specific date or within a date range.
 */
public class PrintEventsCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new PrintEventsCommand.
   *
   * @param calendar the calendar model
   */
  public PrintEventsCommand(ICalendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 2) {
      return "Error: Insufficient arguments for print events command";
    }

    try {
      if (args[0].equals("on") && args.length == 2) {
        // Print events on a specific date
        LocalDate date = DateTimeUtil.parseDate(args[1]);
        return printEventsOnDate(date);
      } else if (args[0].equals("from") && args.length == 4 && args[2].equals("to")) {
        // Print events within a date range
        LocalDate startDate = DateTimeUtil.parseDate(args[1]);
        LocalDate endDate = DateTimeUtil.parseDate(args[3]);
        return printEventsInRange(startDate, endDate);
      } else {
        return "Error: Invalid print events command format";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Prints events on a specific date.
   *
   * @param date the date to query
   * @return a formatted string with the events
   */
  private String printEventsOnDate(LocalDate date) {
    List<Event> events = calendar.getEventsOnDate(date);

    if (events.isEmpty()) {
      return "No events scheduled for " + DateTimeUtil.formatDate(date);
    }

    StringBuilder result = new StringBuilder();
    result.append("Events on ").append(DateTimeUtil.formatDate(date)).append(":\n");

    for (Event event : events) {
      result.append("• ").append(event.getSubject());

      if (!event.isAllDay()) {
        result.append(", from ")
                .append(DateTimeUtil.formatTime(event.getStartDateTime().toLocalTime()))
                .append(" to ")
                .append(DateTimeUtil.formatTime(event.getEndDateTime().toLocalTime()));
      } else {
        result.append(" (All day)");
      }

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(", Location: ").append(event.getLocation());
      }

      result.append("\n");
    }

    return result.toString();
  }

  /**
   * Prints events within a date range.
   *
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   * @return a formatted string with the events
   */
  private String printEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (endDate.isBefore(startDate)) {
      return "Error: End date cannot be before start date";
    }

    List<Event> events = calendar.getEventsInRange(startDate, endDate);

    if (events.isEmpty()) {
      return "No events scheduled between " + DateTimeUtil.formatDate(startDate) +
              " and " + DateTimeUtil.formatDate(endDate);
    }

    StringBuilder result = new StringBuilder();
    result.append("Events from ")
            .append(DateTimeUtil.formatDate(startDate))
            .append(" to ")
            .append(DateTimeUtil.formatDate(endDate))
            .append(":\n");

    // Group events by date
    LocalDate currentDate = null;

    for (Event event : events) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();

      // If we've moved to a new date, print the date header
      if (currentDate == null || !currentDate.equals(eventDate)) {
        result.append("\n").append(DateTimeUtil.formatDate(eventDate)).append(":\n");
        currentDate = eventDate;
      }

      result.append("• ").append(event.getSubject());

      if (!event.isAllDay()) {
        result.append(", from ")
                .append(DateTimeUtil.formatTime(event.getStartDateTime().toLocalTime()))
                .append(" to ")
                .append(DateTimeUtil.formatTime(event.getEndDateTime().toLocalTime()));
      } else {
        result.append(" (All day)");
      }

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        result.append(", Location: ").append(event.getLocation());
      }

      result.append("\n");
    }

    return result.toString();
  }

  @Override
  public String getName() {
    return "print";
  }
}