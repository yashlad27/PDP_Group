import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import controller.command.EditEventCommand;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the EditEventCommand class.
 */
public class EditEventCommandTest {

  private ICalendar calendar;
  private EditEventCommand editCommand;
  private Event testEvent;
  private LocalDateTime startDateTime;
  private LocalDateTime endDateTime;

  @Before
  public void setUp() {
    // Create a real calendar for testing
    calendar = new Calendar();
    editCommand = new EditEventCommand(calendar);

    // Create a test event to use in tests
    startDateTime = LocalDateTime.of(2023, 5, 15, 10, 0);
    endDateTime = LocalDateTime.of(2023, 5, 15, 11, 0);
    testEvent = new Event(
            "Test Meeting",
            startDateTime,
            endDateTime,
            "Original description",
            "Conference Room A",
            true
    );

    // Add the test event to the calendar
    calendar.addEvent(testEvent, false);
  }

  @Test
  public void testGetName() {
    assertEquals("edit", editCommand.getName());
  }

  @Test
  public void testEditSingleEventSubject() {
    // Execute - Edit the subject of a single event
    String[] args = {
            "single",
            "subject",
            "Test Meeting",
            startDateTime.toString(),
            "Updated Meeting"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // Find the edited event
    Event editedEvent = calendar.findEvent("Updated Meeting", startDateTime);

    // Verify the event was updated
    assertEquals("Updated Meeting", editedEvent.getSubject());
  }

  @Test
  public void testEditSingleEventDescription() {
    // Execute - Edit the description of a single event
    String[] args = {
            "single",
            "description",
            "Test Meeting",
            startDateTime.toString(),
            "Updated description"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // Find the event (subject hasn't changed)
    Event editedEvent = calendar.findEvent("Test Meeting", startDateTime);

    // Verify the description was updated
    assertEquals("Updated description", editedEvent.getDescription());
  }

  @Test
  public void testEditSingleEventLocation() {
    // Execute - Edit the location of a single event
    String[] args = {
            "single",
            "location",
            "Test Meeting",
            startDateTime.toString(),
            "Conference Room B"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // Find the event
    Event editedEvent = calendar.findEvent("Test Meeting", startDateTime);

    // Verify the location was updated
    assertEquals("Conference Room B", editedEvent.getLocation());
  }

  @Test
  public void testEditSingleEventPrivacy() {
    // Execute - Edit the privacy setting of a single event
    String[] args = {
            "single",
            "ispublic",
            "Test Meeting",
            startDateTime.toString(),
            "false"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // Find the event
    Event editedEvent = calendar.findEvent("Test Meeting", startDateTime);

    // Verify the privacy setting was updated
    assertFalse(editedEvent.isPublic());
  }

  @Test
  public void testEditNonExistentEvent() {
    // Execute - Try to edit an event that doesn't exist
    String[] args = {
            "single",
            "subject",
            "Non-existent Meeting",
            startDateTime.toString(),
            "Updated Meeting"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditAllEvents() {
    // Add another event with the same subject
    Event anotherEvent = new Event(
            "Test Meeting",
            LocalDateTime.of(2023, 5, 16, 10, 0),
            LocalDateTime.of(2023, 5, 16, 11, 0),
            "Another description",
            "Room B",
            true
    );
    calendar.addEvent(anotherEvent, false);

    // Execute - Edit all events with the subject "Test Meeting"
    String[] args = {
            "all",
            "location",
            "Test Meeting",
            "Virtual Meeting"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited 2 events"));

    // Check that both events were updated
    assertEquals("Virtual Meeting", calendar.findEvent("Test Meeting", startDateTime).getLocation());
    assertEquals("Virtual Meeting", calendar.findEvent("Test Meeting", LocalDateTime.of(2023, 5, 16, 10, 0)).getLocation());
  }

  @Test
  public void testEditEventsFromDate() {
    // Add events before and after a specific date
    Event beforeEvent = new Event(
            "Test Meeting",
            LocalDateTime.of(2023, 5, 14, 10, 0),
            LocalDateTime.of(2023, 5, 14, 11, 0),
            "Before description",
            "Room X",
            true
    );

    Event afterEvent = new Event(
            "Test Meeting",
            LocalDateTime.of(2023, 5, 16, 10, 0),
            LocalDateTime.of(2023, 5, 16, 11, 0),
            "After description",
            "Room Y",
            true
    );

    calendar.addEvent(beforeEvent, false);
    calendar.addEvent(afterEvent, false);

    // Execute - Edit events from a specific date
    LocalDateTime fromDate = LocalDateTime.of(2023, 5, 15, 0, 0);
    String[] args = {
            "series_from_date",
            "location",
            "Test Meeting",
            fromDate.toString(),
            "Updated Location"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited 2 events"));

    // Check that only events on or after the date were updated
    assertEquals("Room X", calendar.findEvent("Test Meeting", LocalDateTime.of(2023, 5, 14, 10, 0)).getLocation());
    assertEquals("Updated Location", calendar.findEvent("Test Meeting", startDateTime).getLocation());
    assertEquals("Updated Location", calendar.findEvent("Test Meeting", LocalDateTime.of(2023, 5, 16, 10, 0)).getLocation());
  }

  @Test
  public void testExecuteWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments
    String[] args = {};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testEditSingleEventWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments for single event
    String[] args = {"single", "subject", "Test Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testEditSeriesFromDateWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments for series from date
    String[] args = {"series_from_date", "subject", "Test Meeting"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testEditAllEventsWithInsufficientArgs() {
    // Execute - Call execute with insufficient arguments for all events
    String[] args = {"all", "subject"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error: Insufficient arguments"));
  }

  @Test
  public void testEditWithInvalidCommandType() {
    // Execute - Call execute with an invalid edit type
    String[] args = {"invalid_type", "subject", "Test Meeting", "New Subject"};
    String result = editCommand.execute(args);

    assertTrue(result.contains("Unknown edit command type"));
  }

  @Test
  public void testEditWithInvalidDateFormat() {
    // Execute - Call execute with invalid date format
    String[] args = {
            "single",
            "subject",
            "Test Meeting",
            "invalid-date-format",
            "Updated Meeting"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Error parsing date/time"));
  }

  @Test
  public void testEditWithInvalidProperty() {
    // Execute - Try to edit a property that doesn't exist
    String[] args = {
            "single",
            "invalidProperty",
            "Test Meeting",
            startDateTime.toString(),
            "New Value"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Failed to edit event"));
  }

  @Test
  public void testEditStartDateTime() {
    // Execute - Edit the start date/time of a single event
    LocalDateTime newStartDateTime = LocalDateTime.of(2023, 5, 15, 9, 0);
    String[] args = {
            "single",
            "startdatetime",
            "Test Meeting",
            startDateTime.toString(),
            newStartDateTime.toString()
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // The event will now be found at the new start time
    Event editedEvent = calendar.findEvent("Test Meeting", newStartDateTime);

    // Verify the start date/time was updated
    assertEquals(newStartDateTime, editedEvent.getStartDateTime());
  }

  @Test
  public void testEditEndDateTime() {
    // Execute - Edit the end date/time of a single event
    LocalDateTime newEndDateTime = LocalDateTime.of(2023, 5, 15, 12, 0);
    String[] args = {
            "single",
            "enddatetime",
            "Test Meeting",
            startDateTime.toString(),
            newEndDateTime.toString()
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("Successfully edited event"));

    // Find the event
    Event editedEvent = calendar.findEvent("Test Meeting", startDateTime);

    // Verify the end date/time was updated
    assertEquals(newEndDateTime, editedEvent.getEndDateTime());
  }

  @Test
  public void testEditNoMatchingEvents() {
    // Execute - Try to edit all events with a subject that doesn't exist
    String[] args = {
            "all",
            "subject",
            "Non-existent Meeting",
            "Updated Meeting"
    };
    String result = editCommand.execute(args);

    assertTrue(result.contains("No events found"));
  }
}