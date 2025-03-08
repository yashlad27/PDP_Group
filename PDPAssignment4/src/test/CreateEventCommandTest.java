import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import controller.command.CreateEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

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

  @Test
  public void testCreateEventSuccess() {
    // Execute - Create a single event, no auto-decline
    String result = createCommand.createEvent(
            "Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("created successfully"));

    // Check that the event was added to the calendar
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Meeting", addedEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), addedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), addedEvent.getEndDateTime());
  }

  @Test
  public void testCreateEventWithDescriptionAndLocation() {
    // Execute - Create a single event with description and location
    String result = createCommand.createEvent(
            "Birthday Party",
            LocalDateTime.of(2023, 5, 15, 18, 0),
            LocalDateTime.of(2023, 5, 15, 22, 0),
            false,
            "Celebrating Dad's 50th birthday",
            "Copacabana Restaurant",
            true);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Birthday Party", addedEvent.getSubject());
    assertEquals("Celebrating Dad's 50th birthday", addedEvent.getDescription());
    assertEquals("Copacabana Restaurant", addedEvent.getLocation());
    assertTrue(addedEvent.isPublic());
  }

  @Test
  public void testCreatePrivateEvent() {
    // Execute - Create a single private event
    String result = createCommand.createEvent(
            "Therapy Session",
            LocalDateTime.of(2023, 5, 15, 15, 0),
            LocalDateTime.of(2023, 5, 15, 16, 0),
            false,
            "Weekly therapy appointment",
            "Dr. Smith's Office",
            false);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Therapy Session", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
  }

  @Test
  public void testCreateEventWithAutoDeclineSuccess() {
    // Execute - Create a single event with auto-decline
    String result = createCommand.createEvent(
            "Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            true,
            null,
            null,
            true);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testCreateEventWithConflict() {
    // First, create an event
    createCommand.createEvent(
            "Meeting 1",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false,
            null,
            null,
            true);

    // Now try to create a conflicting event with autoDecline=true
    String result = createCommand.createEvent(
            "Meeting 2",
            LocalDateTime.of(2023, 5, 15, 10, 30),
            LocalDateTime.of(2023, 5, 15, 11, 30),
            true,
            null,
            null,
            true);

    assertTrue(result.contains("Failed to create event due to conflicts"));
    assertEquals(1, calendar.getAllEvents().size()); // Still only one event
  }

  @Test
  public void testCreateEventWithNullName() {
    // Execute - Create a single event with null name
    String result = createCommand.createEvent(
            null,
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("Error: Event name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateEventWithEmptyName() {
    // Execute - Create a single event with empty name
    String result = createCommand.createEvent(
            "   ",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("Error: Event name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateEventWithNullStartTime() {
    // Execute - Create a single event with null start time
    String result = createCommand.createEvent(
            "Meeting",
            null,
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("Error: Start date/time cannot be null"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // ALL DAY EVENT TESTS

  @Test
  public void testCreateAllDayEventSuccess() {
    // Execute - Create an all-day event, no auto-decline
    String result = createCommand.createAllDayEvent(
            "Holiday",
            LocalDate.of(2023, 5, 15),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("All-day event"));
    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Holiday", addedEvent.getSubject());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithDescriptionAndLocation() {
    // Execute - Create an all-day event with description and location
    String result = createCommand.createAllDayEvent(
            "Conference Day",
            LocalDate.of(2023, 5, 15),
            false,
            "Annual Tech Conference",
            "Convention Center",
            true);

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
    String result = createCommand.createAllDayEvent(
            "Mental Health Day",
            LocalDate.of(2023, 5, 15),
            false,
            "Personal day off",
            "Home",
            false);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Mental Health Day", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithNullName() {
    // Execute - Create an all-day event with null name
    String result = createCommand.createAllDayEvent(
            null,
            LocalDate.of(2023, 5, 15),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("Error: Event name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateAllDayEventWithNullDate() {
    // Execute - Create an all-day event with null date
    String result = createCommand.createAllDayEvent(
            "Holiday",
            null,
            false,
            null,
            null,
            true);

    assertTrue(result.contains("Error: Date cannot be null"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // RECURRING EVENT TESTS

  @Test
  public void testCreateRecurringEventSuccess() {
    // Execute - Create a recurring event
    String result = createCommand.createRecurringEvent(
            "Weekly Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            "MW",
            8,
            false,
            null,
            null,
            true);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventWithDescriptionAndLocation() {
    // Execute - Create a recurring event with description and location
    String result = createCommand.createRecurringEvent(
            "Yoga Class",
            LocalDateTime.of(2023, 5, 15, 18, 0),
            LocalDateTime.of(2023, 5, 15, 19, 0),
            "TR",
            12,
            false,
            "Beginner's yoga with Instructor Sarah",
            "Downtown Fitness Center",
            true);

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
    String result = createCommand.createRecurringEvent(
            "Therapy Session",
            LocalDateTime.of(2023, 5, 15, 15, 0),
            LocalDateTime.of(2023, 5, 15, 16, 0),
            "M",
            10,
            false,
            "Weekly therapy appointment",
            "Dr. Smith's Office",
            false);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence is private
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertFalse(firstOccurrence.isPublic());
  }

  @Test
  public void testCreateRecurringEventUntilSuccess() {
    // Execute - Create a recurring event until a specific date
    String result = createCommand.createRecurringEventUntil(
            "Daily Standup",
            LocalDateTime.of(2023, 5, 15, 9, 30),
            LocalDateTime.of(2023, 5, 15, 9, 45),
            "MTWRF",
            LocalDate.of(2023, 5, 31),
            false,
            null,
            null,
            true);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testCreateRecurringEventUntilWithDetailsSuccess() {
    // Execute - Create a recurring event until a specific date with details
    String result = createCommand.createRecurringEventUntil(
            "Weekly Review",
            LocalDateTime.of(2023, 5, 15, 16, 0),
            LocalDateTime.of(2023, 5, 15, 17, 0),
            "F",
            LocalDate.of(2023, 6, 30),
            false,
            "Project progress review",
            "Conference Room A",
            false);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence has the correct details
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Weekly Review", firstOccurrence.getSubject());
    assertEquals("Project progress review", firstOccurrence.getDescription());
    assertEquals("Conference Room A", firstOccurrence.getLocation());
    assertFalse(firstOccurrence.isPublic());
  }

  // EXECUTE METHOD TESTS

  @Test
  public void testExecuteSingleEvent() {
    // Execute - Create a single event via execute method
    String[] args = {
            "single",
            "Meeting",
            "2023-05-15T10:00",
            "2023-05-15T11:00",
            null,
            null,
            "true",
            "false"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteSingleEventWithDescription() {
    // Execute - Create a single event with description via execute method
    String[] args = {
            "single",
            "Job Interview",
            "2023-05-15T14:00",
            "2023-05-15T15:30",
            "Interview with Google for Software Engineer position",
            "Google Building 40, Mountain View",
            "true",
            "false"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Job Interview", addedEvent.getSubject());
    assertEquals("Interview with Google for Software Engineer position", addedEvent.getDescription());
    assertEquals("Google Building 40, Mountain View", addedEvent.getLocation());
  }

  @Test
  public void testExecutePrivateEvent() {
    // Execute - Create a private event via execute method
    String[] args = {
            "single",
            "Doctor Appointment",
            "2023-05-15T09:30",
            "2023-05-15T10:30",
            "Annual checkup",
            "Medical Center, Room 302",
            "false",
            "false"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Doctor Appointment", addedEvent.getSubject());
    assertFalse(addedEvent.isPublic());
  }

  @Test
  public void testExecuteAllDayEvent() {
    // Execute - Create an all-day event via execute method
    String[] args = {
            "allday",
            "Holiday",
            "2023-05-15",
            "false",
            null,
            null,
            "true"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteAllDayEventWithDetails() {
    // Execute - Create an all-day event with details via execute method
    String[] args = {
            "allday",
            "Conference Day",
            "2023-05-20",
            "false",
            "Annual Tech Conference",
            "Convention Center",
            "false"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Conference Day", addedEvent.getSubject());
    assertEquals("Annual Tech Conference", addedEvent.getDescription());
    assertEquals("Convention Center", addedEvent.getLocation());
    assertFalse(addedEvent.isPublic());
  }

  @Test
  public void testExecuteRecurringEvent() {
    // Execute - Create a recurring event via execute method
    String[] args = {
            "recurring",
            "Weekly Status Meeting",
            "2023-05-15T09:00",
            "2023-05-15T10:00",
            "MW",
            "8",
            "false",
            null,
            null,
            "true"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testExecuteRecurringEventWithDetails() {
    // Execute - Create a recurring event with details via execute method
    String[] args = {
            "recurring",
            "Yoga Class",
            "2023-05-15T18:00",
            "2023-05-15T19:00",
            "TR",
            "12",
            "false",
            "Beginner's yoga with Instructor Sarah",
            "Downtown Fitness Center",
            "true"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("created successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence has correct details
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Yoga Class", firstOccurrence.getSubject());
    assertEquals("Beginner's yoga with Instructor Sarah", firstOccurrence.getDescription());
    assertEquals("Downtown Fitness Center", firstOccurrence.getLocation());
  }

  @Test
  public void testExecuteRecurringUntilEvent() {
    // Execute - Create a recurring event until a specific date via execute method
    String[] args = {
            "recurring-until",
            "Daily Standup",
            "2023-05-15T09:30",
            "2023-05-15T09:45",
            "MTWRF",
            "2023-05-31",
            "false",
            null,
            null,
            "true"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);
  }

  @Test
  public void testExecuteRecurringUntilEventWithDetails() {
    // Execute - Create a recurring event until a specific date with details via execute method
    String[] args = {
            "recurring-until",
            "Weekly Review",
            "2023-05-15T16:00",
            "2023-05-15T17:00",
            "F",
            "2023-06-30",
            "false",
            "Project progress review",
            "Conference Room A",
            "false"
    };
    String result = createCommand.execute(args);

    assertTrue(result.contains("successfully"));
    assertTrue(calendar.getAllEvents().size() > 0);

    // Check at least the first occurrence has correct details
    Event firstOccurrence = calendar.getAllEvents().get(0);
    assertEquals("Weekly Review", firstOccurrence.getSubject());
    assertEquals("Project progress review", firstOccurrence.getDescription());
    assertEquals("Conference Room A", firstOccurrence.getLocation());
    assertFalse(firstOccurrence.isPublic());
  }

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
  public void testExecuteWithInvalidDateFormat() {
    // Execute - Call execute with invalid date format
    String[] args = {
            "single",
            "Meeting",
            "invalid-date",
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
}