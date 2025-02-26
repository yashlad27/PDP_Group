package model.calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ICalendar {

  /**
   * Adds a single event to the calendar.
   *
   * @param event       event to add.
   * @param autoDecline if true, event will not be added as it conflicts with existing events.
   * @return true if the event was successfully added without any conflicts.
   */
  boolean addEvent(Event event, boolean autoDecline);

  /**
   * Adds a recurring event to the calendar.
   *
   * @param re          recurring event to add.
   * @param autoDecline if true, event will not be added as it conflicts with existing events.
   * @return true if the event was successfully added without any conflicts.
   */
  boolean addRecurringEvent(ReccuringEvent re, boolean autoDecline);

  /**
   * Edits a single event using a Unique ID.
   *
   * @param eventId      the id of event to edit.
   * @param updatedEvent the event which has now updated information.
   * @return true if the event was found and updated, false otherwise.
   */
  boolean editEvent(UUID eventId, Event updatedEvent);

  /**
   * Edits a single occurrence of a recurring event.
   *
   * @param recurringEventId the id of the recurring event.
   * @param occurrenceDate   the date on which event takes place.
   * @param updatedEvent     final updated event information.
   * @return true if occurrence was found and updated
   */
  boolean editRecurringEventOccurrence(UUID recurringEventId, LocalDate occurrenceDate,
                                       Event updatedEvent);

  /**
   * Edits all occurrences of a recurring event starting from a specific date.
   *
   * @param recurringEventId the id of the recurring event.
   * @param startDate        the date from which editing needs to start for next events.
   * @param updatedEvent     the updated event information.
   * @return true if recurring event was updated correctly.
   */
  boolean editRecurruingEventFromDate(UUID recurringEventId, LocalDate startDate,
                                      Event updatedEvent);

  /**
   * Edits all occurrences of a recurring event.
   *
   * @param recurringEventId the id of the recurring event.
   * @param updatedEvent     the updated event information.
   * @return true if the recurring event was found and updated.
   */
  boolean editAllRecurringEvents(UUID recurringEventId, RecurringEvent updatedEvent);

  /**
   * Gets all events occurring on a specific date.
   *
   * @param date the date of event to find.
   * @return a list of events on the given date.
   */
  List<Event> getEventsOnDate(LocalDate date);

  /**
   * Gets all events occurring within a date range. [INCLUSIVE]
   *
   * @param startDate the start date of range.
   * @param endDate   the end date of range.
   * @return a list of events between a date range.
   */
  List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

  /**
   * Checks if there are any events at a specific date and time.
   *
   * @param dateTime the specific date and time to check.
   * @return true if there is atleast one event at the given date and time.
   */
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Find an event by its subject and start date/time.
   *
   * @param subject       the subject of the event.
   * @param startDateTime the start date and time of the event.
   * @return the matched event and return null if not found.
   */
  Event findEvent(String subject, LocalDateTime startDateTime);

  /**
   * Finds an event by it's ID.
   *
   * @param eventId the ID of the event.
   * @return the matching event, or null if not found.
   */
  Event findEventById(UUID eventId);

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events in the calendar.
   */
  List<Event> getAllEvents();

  /**
   * Gets all recurring events in the calendar.
   *
   * @return a list of all recurring events in the calendar.
   */
  List<RecurringEvent> getAllRecurringEvents();

  /**
   * Exports the calendar to a CSV File.
   *
   * @param filePath the path where the CSV File should be created.
   * @return true if export was successful, otherwise false.
   */
  boolean exportToCSV(String filePath);
}