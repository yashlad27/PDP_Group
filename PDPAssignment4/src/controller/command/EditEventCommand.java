package controller.command;

import java.time.LocalDateTime;

import model.calendar.ICalendar;
import utilities.DateTimeUtil;

public class EditEventCommand implements ICommand {
  private final ICalendar calendar;
  private final String commandType;
  private final String subject;
  private final String property;
  private final String newValue;
  private LocalDateTime startDateTime;

  /**
   * Creates a command to edit a single event.
   */
  public EditEventCommand(ICalendar calendar, String commandType, String property,
                          String subject, LocalDateTime startDateTime, String newValue) {
    this.calendar = calendar;
    this.commandType = commandType;
    this.property = property;
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.newValue = newValue;
  }

  /**
   * Creates a command to edit all events.
   */
  public EditEventCommand(ICalendar calendar, String commandType, String property,
                          String subject, String newValue) {
    this.calendar = calendar;
    this.commandType = commandType;
    this.property = property;
    this.subject = subject;
    this.newValue = newValue;
    this.startDateTime = null;
  }

  /**
   * Creates a basic edit command with calendar reference.
   * This constructor is used for registration with the command factory.
   * Actual command parameters will be provided when execute() is called.
   */
  public EditEventCommand(ICalendar calendar) {
    this.calendar = calendar;
    this.commandType = null;
    this.property = null;
    this.subject = null;
    this.newValue = null;
    this.startDateTime = null;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 3) {
      return "Error: Insufficient arguments for edit command";
    }

    String type = args[0];

    if (type.equals("single")) {
      if (args.length < 5) {
        return "Error: Insufficient arguments for editing a single event";
      }

      String property = args[1];
      String subject = args[2];
      LocalDateTime startDateTime;
      try {
        startDateTime = DateTimeUtil.parseDateTime(args[3]);
      } catch (Exception e) {
        return "Error parsing date/time: " + e.getMessage();
      }
      String newValue = args[4];

      boolean success = calendar.editSingleEvent(subject, startDateTime, property, newValue);

      if (success) {
        return "Successfully edited event '" + subject + "'.";
      } else {
        return "Failed to edit event. Event not found or invalid property.";
      }
    } else if (type.equals("series_from_date")) {
      if (args.length < 5) {
        return "Error: Insufficient arguments for editing events from date";
      }

      String property = args[1];
      String subject = args[2];
      LocalDateTime startDateTime;
      try {
        startDateTime = DateTimeUtil.parseDateTime(args[3]);
      } catch (Exception e) {
        return "Error parsing date/time: " + e.getMessage();
      }
      String newValue = args[4];

      int count = calendar.editEventsFromDate(subject, startDateTime, property, newValue);

      if (count > 0) {
        return "Successfully edited " + count + " events in the series.";
      } else {
        return "No matching events found to edit.";
      }
    } else if (type.equals("all")) {
      if (args.length < 4) {
        return "Error: Insufficient arguments for editing all events";
      }

      String property = args[1];
      String subject = args[2];
      String newValue = args[3];

      int count = calendar.editAllEvents(subject, property, newValue);

      if (count > 0) {
        return "Successfully edited " + count + " events.";
      } else {
        return "No events found with the subject '" + subject + "'.";
      }
    } else {
      return "Unknown edit command type: " + type;
    }
  }

  @Override
  public String getName() {
    return "edit";
  }
}