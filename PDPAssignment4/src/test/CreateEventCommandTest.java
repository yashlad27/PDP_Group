import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.CreateEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the CreateEventCommand class without using Mockito.
 */
public class CreateEventCommandTest {

  private ICalendar calendar;
  private CreateEventCommand createCommand;

  @Before
  public void setUp() {
    // Create a real calendar for testing
    calendar = new Calendar();
    createCommand = new CreateEventCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("create", createCommand.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    new CreateEventCommand(null);
  }

  // SINGLE EVENT TESTS

  @Test
  public void testCreateSingleEventSuccess() {
    // Execute - Create a single event, no auto-decline
    String[] args = {
            "single",
            "Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            null,  // description
            null,  // location
            "true",  // isPublic
            "false"  // autoDecline
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));

    // Check that the event was added to the calendar
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Meeting", addedEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0),
            addedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0),
            addedEvent.getEndDateTime());
  }

  @Test
  public void testCreateSingleEventWithDescriptionAndLocation() {
    // Execute - Create a single event with description and location
    String[] args = {
            "single",
            "Birthday Party",
            "2023-05-15T18:00",
            "2023-05-15T22:00",
            "Celebrating Dad's 50th birthday",
            "Copacabana Restaurant",
            "true",  // isPublic
            "false"  // autoDecline
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Birthday Party", addedEvent.getSubject());
    assertEquals("Celebrating Dad's 50th birthday", addedEvent.getDescription());
    assertEquals("Copacabana Restaurant", addedEvent.getLocation());
    assertTrue(addedEvent.isPublic());
  }

  @Test
  public void testCreatePrivateSingleEvent() {
    // Execute - Create a single private event
    String[] args = {
            "single",
            "Therapy Session",
            "2023-05-15T15:00",
            "2023-05-15T16:00",
            "Weekly therapy appointment",
            "Dr. Smith's Office",
            "false",  // isPublic
            "false"   // autoDecline
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Therapy Session", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
  }

  @Test
  public void testCreateSingleEventWithAutoDeclineSuccess() {
    // Execute - Create a single event with auto-decline
    String[] args = {
            "single",
            "Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            null,
            null,
            "true",   // isPublic
            "true"    // autoDecline
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateSingleEventWithConflict() {
    // First, create an event
    String[] firstArgs = {
            "single",
            "Meeting 1",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            null,
            null,
            "true",   // isPublic
            "false"   // autoDecline
    };
    createCommand.execute(firstArgs);

    // Now try to create a conflicting event with autoDecline=true
    String[] secondArgs = {
            "single",
            "Meeting 2",
            "2023-05-15T10:30",
            "2023-05-15T11:30",
            null,
            null,
            "true",   // isPublic
            "true"    // autoDecline
    };

    String result = createCommand.execute(secondArgs);

    assertTrue(result.contains("Failed to create event due to conflicts"));
    assertEquals(1, calendar.getAllEvents().size()); // Still only one event
  }

  @Test
  public void testCreateSingleEventWithInvalidName() {
    // Execute - Create a single event with null/empty name
    String[] args = {
            "single",
            "",  // Empty name
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            null,
            null,
            "true",
            "false"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error") || result.contains("name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateSingleEventWithInvalidDateTime() {
    // Execute - Create a single event with invalid date time
    String[] args = {
            "single",
            "Meeting",
            "invalid-date",  // Invalid date format
            "2023-05-15T11:00",
            null,
            null,
            "true",
            "false"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error parsing arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // ALL DAY EVENT TESTS

  @Test
  public void testCreateAllDayEventSuccess() {
    // Execute - Create an all-day event, no auto-decline
    String[] args = {
            "allday",
            "Holiday",
            "2023-05-15",  // date
            "false",       // autoDecline
            null,          // description
            null,          // location
            "true"         // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Holiday", addedEvent.getSubject());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithDescriptionAndLocation() {
    // Execute - Create an all-day event with description and location
    String[] args = {
            "allday",
            "Conference Day",
            "2023-05-15",
            "false",            // autoDecline
            "Annual Tech Conference",
            "Convention Center",
            "true"              // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Conference Day", addedEvent.getSubject());
    assertEquals("Annual Tech Conference", addedEvent.getDescription());
    assertEquals("Convention Center", addedEvent.getLocation());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreatePrivateAllDayEvent() {
    // Execute - Create a private all-day event
    String[] args = {
            "allday",
            "Mental Health Day",
            "2023-05-15",
            "false",         // autoDecline
            "Personal day off",
            "Home",
            "false"          // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Mental Health Day", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithInvalidDate() {
    // Execute - Create an all-day event with invalid date
    String[] args = {
            "allday",
            "Holiday",
            "invalid-date",  // Invalid date format
            "false",
            null,
            null,
            "true"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error parsing arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // RECURRING EVENT TESTS

  @Test
  public void testCreateRecurringEventSuccess() {
    // Execute - Create a recurring event
    String[] args = {
            "recurring",
            "Weekly Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            "MW",              // weekdays
            "8",               // occurrences
            "false",           // autoDecline
            null,              // description
            null,              // location
            "true"             // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventWithDescriptionAndLocation() {
    // Execute - Create a recurring event with description and location
    String[] args = {
            "recurring",
            "Yoga Class",
            "2023-05-15T18:00",
            "2023-05-15T19:00",
            "TR",               // weekdays
            "12",               // occurrences
            "false",            // autoDecline
            "Beginner's yoga with Instructor Sarah",
            "Downtown Fitness Center",
            "true"              // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence has the correct details
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Yoga Class", firstOccurrence.getSubject());
    assertEquals("Beginner's yoga with Instructor Sarah", firstOccurrence.getDescription());
    assertEquals("Downtown Fitness Center", firstOccurrence.getLocation());
  }

  @Test
  public void testCreatePrivateRecurringEvent() {
    // Execute - Create a private recurring event
    String[] args = {
            "recurring",
            "Therapy Session",
            "2023-05-15T15:00",
            "2023-05-15T16:00",
            "M",                // weekdays
            "10",               // occurrences
            "false",            // autoDecline
            "Weekly therapy appointment",
            "Dr. Smith's Office",
            "false"             // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence is private
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertFalse(firstOccurrence.isPublic());
  }

  @Test
  public void testCreateRecurringEventWithInvalidWeekdays() {
    // Execute - Create a recurring event with invalid weekdays
    String[] args = {
            "recurring",
            "Weekly Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            "XYZ",             // Invalid weekdays
            "8",
            "false",
            null,
            null,
            "true"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateRecurringEventWithInvalidOccurrences() {
    // Execute - Create a recurring event with invalid occurrences
    String[] args = {
            "recurring",
            "Weekly Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            "MW",
            "-1",              // Invalid occurrences
            "false",
            null,
            null,
            "true"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // RECURRING UNTIL EVENT TESTS

  @Test
  public void testCreateRecurringEventUntilSuccess() {
    // Execute - Create a recurring event until a specific date
    String[] args = {
            "recurring-until",
            "Daily Standup",
            "2023-05-15T09:30",
            "2023-05-15T09:45",
            "MTWRF",            // weekdays
            "2023-05-31",       // until date
            "false",            // autoDecline
            null,               // description
            null,               // location
            "true"              // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventUntilWithDetailsSuccess() {
    // Execute - Create a recurring event until a specific date with details
    String[] args = {
            "recurring-until",
            "Weekly Review",
            "2023-05-15T16:00",
            "2023-05-15T17:00",
            "F",                 // weekdays
            "2023-06-30",        // until date
            "false",             // autoDecline
            "Project progress review",
            "Conference Room A",
            "false"              // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence has the correct details
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Weekly Review", firstOccurrence.getSubject());
    assertEquals("Project progress review", firstOccurrence.getDescription());
    assertEquals("Conference Room A", firstOccurrence.getLocation());
    assertFalse(firstOccurrence.isPublic());
  }

  @Test
  public void testCreateRecurringEventUntilWithInvalidDate() {
    // Execute - Create a recurring event with invalid until date
    String[] args = {
            "recurring-until",
            "Daily Standup",
            "2023-05-15T09:30",
            "2023-05-15T09:45",
            "MTWRF",
            "invalid-date",     // Invalid until date
            "false",
            null,
            null,
            "true"
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("Error"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // ALL-DAY RECURRING EVENT TESTS

  @Test
  public void testCreateAllDayRecurringEventSuccess() {
    // Execute - Create an all-day recurring event
    String[] args = {
            "allday-recurring",
            "Team Building Day",
            "2023-05-15",        // date
            "F",                 // weekdays
            "8",                 // occurrences
            "false",             // autoDecline
            "Monthly team building activity",
            "Various Locations",
            "true"               // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateAllDayRecurringEventUntilSuccess() {
    // Execute - Create an all-day recurring event until date
    String[] args = {
            "allday-recurring-until",
            "Holiday",
            "2023-05-15",        // date
            "MF",                // weekdays
            "2023-12-31",        // until date
            "false",             // autoDecline
            "Company holiday",
            null,
            "true"               // isPublic
    };

    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  // ERROR HANDLING TESTS

  @Test
  public void testExecuteWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments
    String[] args = {};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithUnknownEventType() {
    // Execute - Call execute with unknown event type
    String[] args = {"unknown", "Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Unknown create event type"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEvent() {
    // Execute - Call execute with insufficient arguments for single event
    String[] args = {"single", "Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringEvent() {
    // Execute - Call execute with insufficient arguments for recurring event
    String[] args = {"recurring", "Weekly Meeting"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithInsufficientArgsForRecurringUntilEvent() {
    // Execute - Call execute with insufficient arguments for recurring-until event
    String[] args = {"recurring-until", "Daily Standup"};
    String result = createCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }
}