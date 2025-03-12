package model.calendar;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import model.event.Event;
import model.event.RecurringEvent;
import utilities.CSVExporter;
import utilities.DateTimeUtil;

/**
 * Implementation of the ICalendar interface that manages calendar events.
 */
public class Calendar implements ICalendar {

  private final List<Event> events;
  private final List<RecurringEvent> recurringEvents;
  private final Map<UUID, Event> eventById;
  private final Map<UUID, RecurringEvent> recurringEventById;

  /**
   * Constructs a new Calendar with no events.
   */
  public Calendar() {
    this.events = new ArrayList<>();
    this.recurringEvents = new ArrayList<>();
    this.eventById = new HashMap<>();
    this.recurringEventById = new HashMap<>();
  }

  /**
   * method to add event to the calendar.
   *
   * @param event       the event to add
   * @param autoDecline if true, the addition will be declined if it conflicts with existing events
   * @return true if created successfully.
   */
  @Override
  public boolean addEvent(Event event, boolean autoDecline) {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    // Check for conflicts if autoDecline is true
    if (autoDecline && hasConflict(event)) {
      return false;
    }

    // Add the event
    events.add(event);
    eventById.put(event.getId(), event);
    return true;
  }

  /**
   * Method to add recurring event to the calendar.
   *
   * @param recurringEvent the recurring event to add
   * @param autoDecline    if true, the addition will be declined if any occurrence conflicts
   * @return true if created successfully.
   */
  @Override
  public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
    if (recurringEvent == null) {
      throw new IllegalArgumentException("Recurring event cannot be null");
    }

    // Get all occurrences of the recurring event
    List<Event> occurrences = recurringEvent.getAllOccurrences();

    // Check for conflicts if autoDecline is true
    if (autoDecline) {
      for (Event occurrence : occurrences) {
        if (hasConflict(occurrence)) {
          return false;
        }
      }
    }

    // Add the recurring event and all its occurrences
    recurringEvents.add(recurringEvent);
    recurringEventById.put(recurringEvent.getId(), recurringEvent);

    // Add all occurrences to the events list
    for (Event occurrence : occurrences) {
      events.add(occurrence);
      eventById.put(occurrence.getId(), occurrence);
    }

    return true;
  }

  @Override
  public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
      String weekdays, LocalDate untilDate, boolean autoDecline) {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      RecurringEvent recurringEvent = new RecurringEvent(name, start, end, null,
          // Default description
          null,  // Default location
          true,  // Default to public
          repeatDays, untilDate);

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
      int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      // Create an all-day recurring event
      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent(name, startOfDay, endOfDay, description,
          location, isPublic, repeatDays, occurrences);

      // Mark it as an all-day event
      recurringEvent.setAllDay(true);

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  @Override
  public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
      LocalDate untilDate, boolean autoDecline, String description, String location,
      boolean isPublic) {
    try {
      Set<DayOfWeek> repeatDays = DateTimeUtil.parseWeekdays(weekdays);

      // Create an all-day recurring event
      LocalDateTime startOfDay = date.atStartOfDay();
      LocalDateTime endOfDay = date.atTime(23, 59, 59);

      RecurringEvent recurringEvent = new RecurringEvent(name, startOfDay, endOfDay, description,
          location, isPublic, repeatDays, untilDate);

      // Mark it as an all-day event
      recurringEvent.setAllDay(true);

      return addRecurringEvent(recurringEvent, autoDecline);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Method to find event on a certain date.
   *
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @return Event type object.
   */
  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      throw new IllegalArgumentException("Subject and start date/time cannot be null");
    }

    return events.stream()
        .filter(e -> e.getSubject().equals(subject) && e.getStartDateTime().equals(startDateTime))
        .findFirst().orElse(null);
  }

  /**
   * @return
   */
  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Edits a specific event in calendar.
   *
   * @param subject       the subject of the event to edit
   * @param startDateTime the start date/time of the event to edit
   * @param property      the property to edit (name, startTime, endTime, etc.)
   * @param newValue      the new value for the property
   * @return true if the operation is successful.
   */
  @Override
  public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    Event eventToEdit = findEvent(subject, startDateTime);

    if (eventToEdit == null) {
      return false;
    }

    return updateEventProperty(eventToEdit, property, newValue);
  }

  /**
   * Edits a specific event in calendar for a given date.
   *
   * @param subject       the subject of the recurring events to edit
   * @param startDateTime the start date/time to begin editing from
   * @param property      the property to edit
   * @param newValue      the new value for the property
   * @return true if the operation is successful.
   */
  @Override
  public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property,
      String newValue) {
    int count = 0;

    // gets all events with the given subject
    List<Event> matchingEvents = events.stream().filter(
            e -> e.getSubject().equals(subject) && !e.getStartDateTime().isBefore(startDateTime))
        .collect(Collectors.toList());

    // update each matching event
    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }

    return count;
  }

  /**
   * Edits multiple events at once.
   *
   * @param subject  the subject of the events to edit
   * @param property the property to edit
   * @param newValue the new value for the property
   * @return number of occurrences edited.
   */
  @Override
  public int editAllEvents(String subject, String property, String newValue) {
    int count = 0;

    List<Event> matchingEvents = events.stream().filter(e -> e.getSubject().equals(subject))
        .collect(Collectors.toList());

    for (Event event : matchingEvents) {
      if (updateEventProperty(event, property, newValue)) {
        count++;
      }
    }
    return count;
  }

  /**
   * retrieves oll reccurring events in the calendar.
   *
   * @return a list of all reccuring events.
   */
  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  /**
   * Export all events of the calendar to a  csv file
   *
   * @param filePath the path where the CSV file should be created
   * @return filePath of exported csv.
   */
  @Override
  public String exportToCSV(String filePath) throws IOException {
    return CSVExporter.exportToCSV(filePath, events);
  }

  /**
   * Checks if an event conflicts with any existing event in the calendar.
   *
   * @param event the event to check for conflicts
   * @return true if there is a conflict, false otherwise
   */
  private boolean hasConflict(Event event) {
    return events.stream().anyMatch(event::conflictsWith);
  }

  /**
   * Checks if an event occurs on a specific date.
   *
   * @param event the event to check
   * @param date  the date to check against
   * @return true if the event occurs on the given date, false otherwise
   */
  private boolean eventOccursOnDate(Event event, LocalDate date) {
    if (event == null || date == null) {
      return false;
    }
    LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
    LocalDate eventEndDate = event.getEndDateTime().toLocalDate();

    // The event occurs on the date if:
    // 1. The date is between the event's start and end dates (inclusive)
    return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
  }

  /**
   * Checks if an event occurs within a date range.
   *
   * @param event     the event to check
   * @param startDate the start date of the range
   * @param endDate   the end date of the range
   * @return true if the event occurs within the given range, false otherwise
   */
  private boolean eventOccursInRange(Event event, LocalDate startDate, LocalDate endDate) {
    if (event == null || startDate == null || endDate == null) {
      return false;
    }
    LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
    LocalDate eventEndDate = event.getEndDateTime().toLocalDate();

    // The event occurs in the range if:
    // 1. The event's end date is on or after the start date of the range, and
    // 2. The event's start date is on or before the end date of the range
    return !eventEndDate.isBefore(startDate) && !eventStartDate.isAfter(endDate);
  }

  /**
   * Checks if a given date and time falls within an event's interval.
   *
   * @param event    the event to check
   * @param dateTime the date and time to check
   * @return true if the date and time is within the event's interval, false otherwise
   */
  private boolean isTimeInEventInterval(Event event, LocalDateTime dateTime) {
    if (event == null || dateTime == null) {
      return false;
    }
    // The time is in the event's interval if:
    // 1. The time is on or after the event's start date/time, and
    // 2. The time is on or before the event's end date/time
    return !dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(
        event.getEndDateTime());
  }

  /**
   * Updates a specific property of an event.
   *
   * @param event    the event to update.
   * @param property the property to update
   * @param newValue the new value for the property
   * @return true if the update was successful, otherwise false.
   */
  private boolean updateEventProperty(Event event, String property, String newValue) {
    switch (property.toLowerCase()) {
      case "subject":
      case "name":
        event.setSubject(newValue);
        return true;
      case "description":
        event.setDescription(newValue);
        return true;
      case "location":
        event.setLocation(newValue);
        return true;
      case "start":
      case "starttime":
      case "startdatetime":
        try {
          LocalDateTime newStartTime;
          if (newValue.contains("T")) {
            // Full datetime format
            newStartTime = DateTimeUtil.parseDateTime(newValue);
          } else {
            // Time-only format, keep the same date
            LocalTime newTime = LocalTime.parse(newValue);
            newStartTime = LocalDateTime.of(event.getStartDateTime().toLocalDate(), newTime);
          }
          event.setStartDateTime(newStartTime);
          return true;
        } catch (Exception e) {
          return false;
        }
      case "end":
      case "endtime":
      case "enddatetime":
        try {
          LocalDateTime newEndTime;
          if (newValue.contains("T")) {
            // Full datetime format
            newEndTime = DateTimeUtil.parseDateTime(newValue);
          } else {
            // Time-only format, keep the same date
            LocalTime newTime = LocalTime.parse(newValue);
            newEndTime = LocalDateTime.of(event.getEndDateTime().toLocalDate(), newTime);
          }
          event.setEndDateTime(newEndTime);
          return true;
        } catch (Exception e) {
          return false;
        }
      case "visibility":
      case "ispublic":
      case "public":
      case "private":
        boolean isPublic = newValue.equalsIgnoreCase("public") || (newValue.equalsIgnoreCase("true")
            && !property.equals("private"));
        event.setPublic(isPublic);
        return true;
      default:
        return false;
    }
  }

  @Override
  public List<Event> getEventsOnDate(LocalDate date) {
    return events.stream().filter(event -> {
      // For events with a specific date/time
      if (event.getStartDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();

        // Handle multi-day events - check if the date falls within event duration
        if (event.getEndDateTime() != null) {
          LocalDate eventEndDate = event.getEndDateTime().toLocalDate();
          return !date.isBefore(eventStartDate) && !date.isAfter(eventEndDate);
        } else {
          // Single day event
          return eventStartDate.equals(date);
        }
      } else if (event.getDate() != null) {
        // For all-day events
        return event.getDate().equals(date);
      }
      return false;
    }).collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    return events.stream().filter(event -> {
      // For events with a specific date/time
      if (event.getStartDateTime() != null) {
        LocalDate eventStartDate = event.getStartDateTime().toLocalDate();
        LocalDate eventEndDate =
            (event.getEndDateTime() != null) ? event.getEndDateTime().toLocalDate()
                : eventStartDate;

        // Check if the event overlaps with the date range
        return !(eventEndDate.isBefore(startDate) || eventStartDate.isAfter(endDate));
      } else if (event.getDate() != null) {
        // For all-day events
        return !event.getDate().isBefore(startDate) && !event.getDate().isAfter(endDate);
      }
      return false;
    }).collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    return events.stream().anyMatch(event -> {
      // For events with a specific date/time
      if (event.getStartDateTime() != null && event.getEndDateTime() != null) {
        return !dateTime.isBefore(event.getStartDateTime()) && !dateTime.isAfter(
            event.getEndDateTime());
      }

      // For all-day events
      if (event.getDate() != null) {
        LocalDate targetDate = dateTime.toLocalDate();
        return event.getDate().equals(targetDate);
      }

      return false;
    });
  }
}