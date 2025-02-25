package model.calendar;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ICalendar {

  boolean addEvent(Event event, boolean autoDecline);

  boolean addRecurringEvent(ReccuringEvent re, boolean autoDecline);

  boolean editEvent(UUID eventId, Event updatedEvent);

  boolean editRecurringEventOccurrence(UUID recurringEventId, LocalDate occurenceDate,
                                       Event updatedEvent);

  boolean editRecurruingEventFromDate(UUID recurringEventId, LocalDate startDate,
                                      Event updatedEvent);

  boolean editAllRecurringEvents(UUID recurringEventId, RecurringEvent updatedEvent);

  List<Event> getEventsOnDate(LocalDate date);

  List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate);

  boolean isBusy(LocalDateTime dateTime);

  Event findEvent(String subject, LocalDateTime startDateTime);

  Event findEventById(UUID eventId);

  List<Event> getAllEvents();

  List<RecurringEvent> getAllRecurringEvents();

  boolean exportToCSV(String filePath);
}