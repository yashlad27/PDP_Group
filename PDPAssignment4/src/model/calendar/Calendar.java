package model.calendar;

import model.event.Event;
import model.event.RecurringEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
  public List<Event> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    return events.stream()
            .filter(e -> eventOccursOnDate(e, date))
            .collect(Collectors.toList());
  }

  @Override
  public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
    if (startDate == null || endDate == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null");
    }
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    return events.stream()
            .filter(e -> eventOccursInRange(e, startDate, endDate))
            .collect(Collectors.toList());
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    return events.stream()
            .anyMatch(e -> isTimeInEventInterval(e, dateTime));
  }

  @Override
  public Event findEvent(String subject, LocalDateTime startDateTime) {
    if (subject == null || startDateTime == null) {
      throw new IllegalArgumentException("Subject and start date/time cannot be null");
    }

    return events.stream()
            .filter(e -> e.getSubject().equals(subject) &&
                    e.getStartDateTime().equals(startDateTime))
            .findFirst()
            .orElse(null);
  }

  @Override
  public List<Event> getAllEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public List<RecurringEvent> getAllRecurringEvents() {
    return new ArrayList<>(recurringEvents);
  }

  @Override
  public boolean exportToCSV(String filePath) {
    // For now, we'll just return true to simulate success
    // This will be implemented fully in a separate CSVExporter class
    return true;
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
    return !dateTime.isBefore(event.getStartDateTime()) &&
            !dateTime.isAfter(event.getEndDateTime());
  }
}