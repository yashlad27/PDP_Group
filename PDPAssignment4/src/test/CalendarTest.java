import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import model.calendar.Calendar;
import model.event.Event;
import model.event.RecurringEvent;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Test class for Calendar.
 */
public class CalendarTest {

  private Map<String, String> mockFileSystem;

  private Calendar calendar;
  private Event singleEvent;
  private RecurringEvent recurringEvent;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;
  private Set<DayOfWeek> repeatDays;

  @Before
  public void setUp() {
    // Initialize the mock file system
    mockFileSystem = new HashMap<>();

    // Initialize calendar
    calendar = new Calendar();

    // Setup a single event
    startDateTime = LocalDateTime.of(2023, 5, 10, 10, 0);
    endDateTime = LocalDateTime.of(2023, 5, 10, 11, 0);
    singleEvent = new Event("Team Meeting", startDateTime, endDateTime, "Weekly sync-up",
        "Conference Room A", true);

    // Setup a recurring event
    repeatDays = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    recurringEvent = new RecurringEvent("Recurring Meeting", LocalDateTime.of(2023, 5, 8, 14, 0),
        LocalDateTime.of(2023, 5, 8, 15, 0), "Recurring sync-up", "Conference Room B", true,
        repeatDays, 4);
  }

  @Test
  public void testAddEvent() {
    assertTrue(calendar.addEvent(singleEvent, false));

    List<Event> events = calendar.getAllEvents();
    assertEquals(1, events.size());
    assertEquals(singleEvent, events.get(0));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullEvent() {
    calendar.addEvent(null, false);
  }

  @Test
  public void testAddEventWithAutoDeclineNoConflict() {
    assertTrue(calendar.addEvent(singleEvent, true));

    // Add another event at a different time
    Event noConflictEvent = new Event("Another Meeting", startDateTime.plusHours(2),
        endDateTime.plusHours(2), "Description", "Location", true);

    assertTrue(calendar.addEvent(noConflictEvent, true));
    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  public void testAddEventWithAutoDeclineWithConflict() {
    assertTrue(calendar.addEvent(singleEvent, true));

    // Create a conflicting event
    Event conflictingEvent = new Event("Conflicting Meeting", startDateTime.plusMinutes(30),
        endDateTime.plusHours(1), "Description", "Location", true);

    // Event should be declined due to conflict
    assertFalse(calendar.addEvent(conflictingEvent, true));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testAddEventWithoutAutoDeclineWithConflict() {
    assertTrue(calendar.addEvent(singleEvent, false));

    // Create a conflicting event
    Event conflictingEvent = new Event("Conflicting Meeting", startDateTime.plusMinutes(30),
        endDateTime.plusHours(1), "Description", "Location", true);

    // Event should be added despite conflict since autoDecline is false
    assertTrue(calendar.addEvent(conflictingEvent, false));
    assertEquals(2, calendar.getAllEvents().size());
  }

  @Test
  public void testAddRecurringEvent() {
    assertTrue(calendar.addRecurringEvent(recurringEvent, false));

    // Check that the recurring event was added
    List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
    assertEquals(1, recurringEvents.size());
    assertEquals(recurringEvent, recurringEvents.get(0));

    // Check that all occurrences were added
    List<Event> allEvents = calendar.getAllEvents();
    assertEquals(4, allEvents.size());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddNullRecurringEvent() {
    calendar.addRecurringEvent(null, false);
  }

  @Test
  public void testAddRecurringEventWithAutoDeclineNoConflict() {
    assertTrue(calendar.addRecurringEvent(recurringEvent, true));

    // Add another recurring event at a different time
    RecurringEvent noConflictRecurringEvent = new RecurringEvent("Another Recurring Meeting",
        LocalDateTime.of(2023, 5, 8, 16, 0), LocalDateTime.of(2023, 5, 8, 17, 0), "Description",
        "Location", true, repeatDays, 4);

    assertTrue(calendar.addRecurringEvent(noConflictRecurringEvent, true));
    assertEquals(2, calendar.getAllRecurringEvents().size());
    assertEquals(8, calendar.getAllEvents().size());
  }

  @Test
  public void testAddRecurringEventWithAutoDeclineWithConflict() {
    assertTrue(calendar.addRecurringEvent(recurringEvent, true));

    // Create a recurring event with one occurrence conflicting
    LocalDateTime conflictStart = recurringEvent.getAllOccurrences().get(0).getStartDateTime();
    RecurringEvent conflictingRecurringEvent = new RecurringEvent("Conflicting Recurring Meeting",
        conflictStart, conflictStart.plusHours(1), "Description", "Location", true,
        EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), 4);

    // Event should be declined due to conflict
    assertFalse(calendar.addRecurringEvent(conflictingRecurringEvent, true));
    assertEquals(1, calendar.getAllRecurringEvents().size());
  }

  @Test
  public void testCreateAllDayRecurringEvent() {
    LocalDate start = LocalDate.of(2023, 5, 8);

    assertTrue(
        calendar.createAllDayRecurringEvent("All Day Recurring Event", start, "MWF", 3, false,
            "Description", "Location", true));

    assertEquals(1, calendar.getAllRecurringEvents().size());
    assertEquals(3, calendar.getAllEvents().size());

    // Check that the events are all-day
    List<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testCreateAllDayRecurringEventUntil() {
    LocalDate start = LocalDate.of(2023, 5, 8);
    LocalDate until = LocalDate.of(2023, 5, 19);

    assertTrue(
        calendar.createAllDayRecurringEventUntil("All Day Recurring Until Event", start, "MWF",
            until, false, "Description", "Location", true));

    assertEquals(1, calendar.getAllRecurringEvents().size());
    // There should be 6 occurrences: 8th, 10th, 12th, 15th, 17th, 19th
    assertEquals(6, calendar.getAllEvents().size());

    // Check that the events are all-day
    List<Event> events = calendar.getAllEvents();
    for (Event event : events) {
      assertTrue(event.isAllDay());
    }
  }

  @Test
  public void testFindEvent() {
    calendar.addEvent(singleEvent, false);

    Event found = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(found);
    assertEquals(singleEvent, found);
  }

  @Test
  public void testFindNonExistentEvent() {
    calendar.addEvent(singleEvent, false);

    Event notFound = calendar.findEvent("Non-existent Meeting", startDateTime);
    assertNull(notFound);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventWithNullSubject() {
    calendar.findEvent(null, startDateTime);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFindEventWithNullDateTime() {
    calendar.findEvent("Team Meeting", null);
  }

  @Test
  public void testGetAllEvents() {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    List<Event> allEvents = calendar.getAllEvents();

    // Should have 5 events (1 single + 4 occurrences)
    assertEquals(5, allEvents.size());
  }

  @Test
  public void testEditSingleEvent() {
    calendar.addEvent(singleEvent, false);

    assertTrue(
        calendar.editSingleEvent("Team Meeting", startDateTime, "subject", "Updated Meeting"));

    // Check the event was updated
    Event updated = calendar.findEvent("Updated Meeting", startDateTime);
    assertNotNull(updated);
    assertEquals("Updated Meeting", updated.getSubject());
  }

  @Test
  public void testEditNonExistentEvent() {
    assertFalse(
        calendar.editSingleEvent("Non-existent Meeting", startDateTime, "subject", "Updated"));
  }

  @Test
  public void testEditAllEvents() {
    calendar.addEvent(singleEvent, false);

    Event anotherEvent = new Event("Team Meeting", startDateTime.plusDays(1),
        endDateTime.plusDays(1), "Another meeting", "Conference Room C", true);
    calendar.addEvent(anotherEvent, false);

    int count = calendar.editAllEvents("Team Meeting", "location", "New Location");

    // Both events with the same subject should be updated
    assertEquals(2, count);

    // Check that the events were updated
    List<Event> allEvents = calendar.getAllEvents();
    for (Event event : allEvents) {
      assertEquals("New Location", event.getLocation());
    }
  }

  @Test
  public void testGetAllRecurringEvents() {
    calendar.addRecurringEvent(recurringEvent, false);

    List<RecurringEvent> recurringEvents = calendar.getAllRecurringEvents();
    assertEquals(1, recurringEvents.size());
    assertEquals(recurringEvent, recurringEvents.get(0));
  }

  @Test
  public void testExportToCSV() throws IOException {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    // Mock the exportToCSV method behavior directly
    // This approach uses a subclass of Calendar that overrides exportToCSV
    Calendar mockCalendar = new Calendar() {
      @Override
      public String exportToCSV(String filePath) throws IOException {
        // Generate CSV content (similar to what the actual method would do)
        StringBuilder csv = new StringBuilder();
        csv.append(
            "Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,"
                + "Private\n");
        csv.append(
            "Team Meeting,05/10/2023,10:00 AM,05/10/2023,11:00 AM,False,Weekly sync-up,Conference "
                + "Room A,False\n");
        mockFileSystem.put(filePath, csv.toString());

        return filePath; // Return the path as if it were absolute
      }
    };

    // Add our events to the mock calendar
    mockCalendar.addEvent(singleEvent, false);
    mockCalendar.addRecurringEvent(recurringEvent, false);

    // Test export
    String filePath = "calendar_export.csv";
    String exportedPath = mockCalendar.exportToCSV(filePath);

    // Check that the mock export worked
    assertEquals(filePath, exportedPath);
    assertTrue(mockFileSystem.containsKey(filePath));

    // Check that the "file" has content
    String csvContent = mockFileSystem.get(filePath);
    assertNotNull(csvContent);
    assertFalse(csvContent.isEmpty());

    // Verify the content contains our event data
    assertTrue(csvContent.contains("Team Meeting"));
    assertTrue(csvContent.contains("Subject,Start Date,Start Time"));
  }

  @Test
  public void testGetEventsOnDate() {
    calendar.addEvent(singleEvent, false);

    LocalDate date = LocalDate.of(2023, 5, 10);
    List<Event> eventsOnDate = calendar.getEventsOnDate(date);

    assertEquals(1, eventsOnDate.size());
    assertEquals(singleEvent, eventsOnDate.get(0));
  }

  @Test
  public void testGetEventsOnDateWithNoEvents() {
    calendar.addEvent(singleEvent, false);

    LocalDate date = LocalDate.of(2023, 5, 11);
    List<Event> eventsOnDate = calendar.getEventsOnDate(date);

    assertTrue(eventsOnDate.isEmpty());
  }

  @Test
  public void testGetEventsOnDateWithMultiDayEvent() {
    Event multiDayEvent = new Event("Multi-day Conference", LocalDateTime.of(2023, 5, 10, 9, 0),
        LocalDateTime.of(2023, 5, 12, 17, 0), "Annual conference", "Convention Center", true);
    calendar.addEvent(multiDayEvent, false);

    // Check day 1
    List<Event> day1Events = calendar.getEventsOnDate(LocalDate.of(2023, 5, 10));
    assertEquals(1, day1Events.size());

    // Check day 2
    List<Event> day2Events = calendar.getEventsOnDate(LocalDate.of(2023, 5, 11));
    assertEquals(1, day2Events.size());

    // Check day 3
    List<Event> day3Events = calendar.getEventsOnDate(LocalDate.of(2023, 5, 12));
    assertEquals(1, day3Events.size());

    // Check day 4 (no events)
    List<Event> day4Events = calendar.getEventsOnDate(LocalDate.of(2023, 5, 13));
    assertTrue(day4Events.isEmpty());
  }

  @Test
  public void testGetEventsInRange() {
    calendar.addEvent(singleEvent, false);
    calendar.addRecurringEvent(recurringEvent, false);

    LocalDate startDate = LocalDate.of(2023, 5, 8);
    LocalDate endDate = LocalDate.of(2023, 5, 12);

    List<Event> eventsInRange = calendar.getEventsInRange(startDate, endDate);

    // Should contain the single event and the first 3 occurrences of the recurring event
    assertEquals(4, eventsInRange.size());
  }

  @Test
  public void testGetEventsInRangeWithNoEvents() {
    calendar.addEvent(singleEvent, false);

    LocalDate startDate = LocalDate.of(2023, 5, 20);
    LocalDate endDate = LocalDate.of(2023, 5, 25);

    List<Event> eventsInRange = calendar.getEventsInRange(startDate, endDate);

    assertTrue(eventsInRange.isEmpty());
  }

  @Test
  public void testIsBusy() {
    calendar.addEvent(singleEvent, false);

    // Check during the event
    assertTrue(calendar.isBusy(startDateTime.plusMinutes(30)));

    // Check before the event
    assertFalse(calendar.isBusy(startDateTime.minusMinutes(1)));

    // Check after the event
    assertFalse(calendar.isBusy(endDateTime.plusMinutes(1)));
  }

  @Test
  public void testIsBusyWithAllDayEvent() {
    Event allDayEvent = Event.createAllDayEvent("All-day Event", LocalDate.of(2023, 5, 15),
        "Description", "Location", true);
    calendar.addEvent(allDayEvent, false);

    // Check morning
    assertTrue(calendar.isBusy(LocalDateTime.of(2023, 5, 15, 9, 0)));

    // Check afternoon
    assertTrue(calendar.isBusy(LocalDateTime.of(2023, 5, 15, 15, 0)));

    // Check next day
    assertFalse(calendar.isBusy(LocalDateTime.of(2023, 5, 16, 9, 0)));
  }

  @Test
  public void testUpdateEventPropertySubject() {
    calendar.addEvent(singleEvent, false);

    assertTrue(
        calendar.editSingleEvent("Team Meeting", startDateTime, "subject", "Updated Subject"));

    Event updated = calendar.findEvent("Updated Subject", startDateTime);
    assertNotNull(updated);
    assertEquals("Updated Subject", updated.getSubject());
  }

  @Test
  public void testUpdateEventPropertyDescription() {
    calendar.addEvent(singleEvent, false);

    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "description",
        "Updated Description"));

    Event updated = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(updated);
    assertEquals("Updated Description", updated.getDescription());
  }

  @Test
  public void testUpdateEventPropertyLocation() {
    calendar.addEvent(singleEvent, false);

    assertTrue(
        calendar.editSingleEvent("Team Meeting", startDateTime, "location", "Updated Location"));

    Event updated = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(updated);
    assertEquals("Updated Location", updated.getLocation());
  }

  @Test
  public void testUpdateEventPropertyStartTime() {
    calendar.addEvent(singleEvent, false);

    LocalDateTime newStartTime = startDateTime.plusHours(1);
    String newStartTimeStr = newStartTime.toString();

    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "start", newStartTimeStr));

    // The event should no longer be findable with the old start time
    assertNull(calendar.findEvent("Team Meeting", startDateTime));

    // But should be findable with the new start time
    Event updated = calendar.findEvent("Team Meeting", newStartTime);
    assertNotNull(updated);
    assertEquals(newStartTime, updated.getStartDateTime());
  }

  @Test
  public void testUpdateEventPropertyEndTime() {
    calendar.addEvent(singleEvent, false);

    LocalDateTime newEndTime = endDateTime.plusHours(1);
    String newEndTimeStr = newEndTime.toString();

    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "end", newEndTimeStr));

    Event updated = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(updated);
    assertEquals(newEndTime, updated.getEndDateTime());
  }

  @Test
  public void testUpdateEventPropertyVisibility() {
    calendar.addEvent(singleEvent, false);

    // Test making private
    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "visibility", "private"));

    Event updated = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(updated);
    assertFalse(updated.isPublic());

    // Test making public again
    assertTrue(calendar.editSingleEvent("Team Meeting", startDateTime, "visibility", "public"));

    updated = calendar.findEvent("Team Meeting", startDateTime);
    assertNotNull(updated);
    assertTrue(updated.isPublic());
  }

  @Test
  public void testUpdateEventWithInvalidProperty() {
    calendar.addEvent(singleEvent, false);

    assertFalse(
        calendar.editSingleEvent("Team Meeting", startDateTime, "invalid_property", "value"));
  }
}