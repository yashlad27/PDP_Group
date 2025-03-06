import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import controller.command.CreateEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
  public void testCreateEventSuccess() {
    // Execute - Create a single event, no auto-decline
    String result = createCommand.createEvent(
            "Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            false);

    // Verify
    assertTrue(result.contains("created successfully"));

    // Check that the event was added to the calendar
    assertEquals(1, calendar.getAllEvents().size());

    // Verify event details
    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Meeting", addedEvent.getSubject());
    assertEquals(LocalDateTime.of(2023, 5, 15, 10, 0), addedEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 5, 15, 11, 0), addedEvent.getEndDateTime());
  }

  @Test
  public void testCreateEventWithAutoDeclineSuccess() {
    // Execute - Create a single event with auto-decline
    String result = createCommand.createEvent(
            "Meeting",
            LocalDateTime.of(2023, 5, 15, 10, 0),
            LocalDateTime.of(2023, 5, 15, 11, 0),
            true);

    // Verify
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
            false);

    // Now try to create a conflicting event with autoDecline=true
    String result = createCommand.createEvent(
            "Meeting 2",
            LocalDateTime.of(2023, 5, 15, 10, 30),
            LocalDateTime.of(2023, 5, 15, 11, 30),
            true);

    // Verify that the creation failed
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
            false);

    // Verify
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
            false);

    // Verify
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
            false);

    // Verify
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
            false);

    // Verify
    assertTrue(result.contains("All-day event"));
    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());

    // Verify event details
    Event addedEvent = calendar.getAllEvents().get(0);
    assertEquals("Holiday", addedEvent.getSubject());
    assertTrue(addedEvent.isAllDay());
  }

  @Test
  public void testCreateAllDayEventWithNullName() {
    // Execute - Create an all-day event with null name
    String result = createCommand.createAllDayEvent(
            null,
            LocalDate.of(2023, 5, 15),
            false);

    // Verify
    assertTrue(result.contains("Error: Event name cannot be empty"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testCreateAllDayEventWithNullDate() {
    // Execute - Create an all-day event with null date
    String result = createCommand.createAllDayEvent(
            "Holiday",
            null,
            false);

    // Verify
    assertTrue(result.contains("Error: Date cannot be null"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
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
            "false"
    };
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteAllDayEvent() {
    // Execute - Create an all-day event via execute method
    String[] args = {
            "allday",
            "Holiday",
            "2023-05-15",
            "false"
    };
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("created successfully"));
    assertEquals(1, calendar.getAllEvents().size());
  }

  @Test
  public void testExecuteWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments
    String[] args = {};
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithUnknownEventType() {
    // Execute - Call execute with unknown event type
    String[] args = {"unknown", "Meeting"};
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("Error: Unknown create event type"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  @Test
  public void testExecuteWithInsufficientArgsForSingleEvent() {
    // Execute - Call execute with insufficient arguments for single event
    String[] args = {"single", "Meeting"};
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("Error: Insufficient arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }

  // Error handling tests for parsing issues

  @Test
  public void testExecuteWithInvalidDateFormat() {
    // Execute - Call execute with invalid date format
    String[] args = {
            "single",
            "Meeting",
            "invalid-date",
            "2023-05-15T11:00",
            "false"
    };
    String result = createCommand.execute(args);

    // Verify
    assertTrue(result.contains("Error parsing arguments"));
    assertEquals(0, calendar.getAllEvents().size()); // No event added
  }
}