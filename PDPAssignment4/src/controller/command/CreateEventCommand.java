package controller.command;

import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import utilities.DateTimeUtil;
import java.util.Set;

/**
 * Command for creating calendar events (both single and recurring events).
 */
public class CreateEventCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new CreateEventCommand with the specified calendar.
   *
   * @param calendar the calendar in which to create events
   */
  public CreateEventCommand(ICalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.calendar = calendar;
  }

  /**
   * Creates a single event in the calendar.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the event
   * @param endDateTime   the end date and time of the event
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @return a response indicating success or failure
   */
  public String createEvent(String eventName, LocalDateTime startDateTime,
                            LocalDateTime endDateTime, boolean autoDecline) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }

    Event event = new Event(
            eventName,
            startDateTime,
            endDateTime,
            null,  // Default description
            null,  // Default location
            true   // Default to public
    );

    boolean success = calendar.addEvent(event, autoDecline);

    if (success) {
      return "Event '" + eventName + "' created successfully.";
    } else {
      return "Failed to create event due to conflicts.";
    }
  }

  /**
   * Creates an all-day event in the calendar.
   *
   * @param eventName   the name of the event
   * @param date        the date of the event
   * @param autoDecline if true, event creation will be declined if it conflicts
   * @return a response indicating success or failure
   */
  public String createAllDayEvent(String eventName, LocalDate date, boolean autoDecline) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (date == null) {
      return "Error: Date cannot be null";
    }

    Event event = Event.createAllDayEvent(
            eventName,
            date,
            null,  // Default description
            null,  // Default location
            true   // Default to public
    );

    boolean success = calendar.addEvent(event, autoDecline);

    if (success) {
      return "All-day event '" + eventName + "' created successfully.";
    } else {
      return "Failed to create all-day event due to conflicts.";
    }
  }

  /**
   * Creates a recurring event in the calendar with a specified number of occurrences.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the first occurrence
   * @param endDateTime   the end date and time of the first occurrence
   * @param weekdays      the days of the week on which the event repeats, e.g., "MWF"
   * @param occurrences   the number of times the event repeats
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @return a response indicating success or failure
   */
  private String createRecurringEvent(String eventName, LocalDateTime startDateTime,
                                      LocalDateTime endDateTime, String weekdays,
                                      int occurrences, boolean autoDecline) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      return "Error: Weekdays cannot be empty";
    }
    if (occurrences <= 0) {
      return "Error: Occurrences must be positive";
    }

    try {
      Set<java.time.DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent(
              eventName,
              startDateTime,
              endDateTime,
              null,  // Default description
              null,  // Default location
              true,  // Default to public
              repeatDays,
              occurrences
      );

      boolean success = calendar.addRecurringEvent(recurringEvent, autoDecline);

      if (success) {
        return "Recurring event '" + eventName + "' created successfully with "
                + occurrences + " occurrences.";
      } else {
        return "Failed to create recurring event due to conflicts.";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  /**
   * Creates a recurring event in the calendar that repeats until a specified end date.
   *
   * @param eventName     the name of the event
   * @param startDateTime the start date and time of the first occurrence
   * @param endDateTime   the end date and time of the first occurrence
   * @param weekdays      the days of the week on which the event repeats, e.g., "MWF"
   * @param untilDate     the date after which the event stops repeating
   * @param autoDecline   if true, event creation will be declined if it conflicts
   * @return a response indicating success or failure
   */
  public String createRecurringEvent(String eventName, LocalDateTime startDateTime,
                                     LocalDateTime endDateTime, String weekdays,
                                     LocalDate untilDate, boolean autoDecline) {
    if (eventName == null || eventName.trim().isEmpty()) {
      return "Error: Event name cannot be empty";
    }
    if (startDateTime == null) {
      return "Error: Start date/time cannot be null";
    }
    if (weekdays == null || weekdays.trim().isEmpty()) {
      return "Error: Weekdays cannot be empty";
    }
    if (untilDate == null) {
      return "Error: Until date cannot be null";
    }

    try {
      Set<java.time.DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent(
              eventName,
              startDateTime,
              endDateTime,
              null,  // Default description
              null,  // Default location
              true,  // Default to public
              repeatDays,
              untilDate
      );

      boolean success = calendar.addRecurringEvent(recurringEvent, autoDecline);

      if (success) {
        return "Recurring event '" + eventName + "' created successfully, repeating until "
                + DateTimeUtil.formatDate(untilDate) + ".";
      } else {
        return "Failed to create recurring event due to conflicts.";
      }
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String execute(String[] args) {
    // This method would parse command line arguments and call the appropriate creation method

    if (args.length < 1) {
      return "Error: Insufficient arguments for create command";
    }

    switch (args[0]) {
      case "single":
        // Example: single "Meeting" 2023-05-15T10:00 2023-05-15T11:00 true
        if (args.length < 5) {
          return "Error: Insufficient arguments for creating a single event";
        }
        try {
          String name = args[1];
          LocalDateTime start = DateTimeUtil.parseDateTime(args[2]);
          LocalDateTime end = DateTimeUtil.parseDateTime(args[3]);
          boolean autoDecline = Boolean.parseBoolean(args[4]);

          return createEvent(name, start, end, autoDecline);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      case "recurring":
        if (args.length < 7) {
          return "Error: Insufficient arguments for creating a recurring event";
        }
        try {
          String name = args[1];
          LocalDateTime start = DateTimeUtil.parseDateTime(args[2]);
          LocalDateTime end = DateTimeUtil.parseDateTime(args[3]);
          String weekdays = args[4];
          int occurrences = Integer.parseInt(args[5]);
          boolean autoDecline = Boolean.parseBoolean(args[6]);

          return createRecurringEvent(name, start, end, weekdays, occurrences, autoDecline);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      case "allday":
        // Example: allday "Holiday" 2023-05-15 true
        if (args.length < 4) {
          return "Error: Insufficient arguments for creating an all-day event";
        }
        try {
          String name = args[1];
          LocalDate date = DateTimeUtil.parseDate(args[2]);
          boolean autoDecline = Boolean.parseBoolean(args[3]);

          return createAllDayEvent(name, date, autoDecline);
        } catch (Exception e) {
          return "Error parsing arguments: " + e.getMessage();
        }

      default:
        return "Error: Unknown create event type: " + args[0];
    }
  }

  /**
   * @return
   */
  @Override
  public String getName() {
    return "create";
  }
}