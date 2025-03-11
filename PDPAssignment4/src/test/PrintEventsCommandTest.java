import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import controller.command.PrintEventsCommand;
import model.calendar.ICalendar;
import model.event.Event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PrintEventsCommandTest {

  // Manual mock implementation of ICalendar
  private static class MockCalendar implements ICalendar {
    private List<Event> eventsOnDateResult = new ArrayList<>();
    private List<Event> eventsInRangeResult = new ArrayList<>();
    private LocalDate lastCheckedDate = null;
    private LocalDate lastCheckedStartDate = null;
    private LocalDate lastCheckedEndDate = null;

    public void setEventsOnDateResult(List<Event> events) {
      this.eventsOnDateResult = events;
    }

    public void setEventsInRangeResult(List<Event> events) {
      this.eventsInRangeResult = events;
    }

    public LocalDate getLastCheckedDate() {
      return lastCheckedDate;
    }

    public LocalDate getLastCheckedStartDate() {
      return lastCheckedStartDate;
    }

    public LocalDate getLastCheckedEndDate() {
      return lastCheckedEndDate;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      this.lastCheckedDate = date;
      return eventsOnDateResult;
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      this.lastCheckedStartDate = startDate;
      this.lastCheckedEndDate = endDate;
      return eventsInRangeResult;
    }

    // Implement other methods from ICalendar with minimal implementations
    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      return false;
    }

    @Override
    public boolean addEvent(Event event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(model.event.RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, java.time.LocalDate untilDate, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, java.time.LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, java.time.LocalDate date, String weekdays,
                                                   java.time.LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<Event> getAllEvents() {
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime, String property, String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime, String property, String newValue) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) {
      return 0;
    }

    @Override
    public List<model.event.RecurringEvent> getAllRecurringEvents() {
      return null;
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return null;
    }
  }

  private static class MockEvent extends Event {
    public MockEvent(String subject, boolean isAllDay, LocalDateTime startDateTime,
                     LocalDateTime endDateTime, String location, boolean isPublic) {
      // Call the parent constructor with potentially adjusted parameters
      super(subject,
              getAdjustedStartTime(isAllDay, startDateTime),
              getAdjustedEndTime(isAllDay, startDateTime, endDateTime),
              null,
              location,
              isPublic);

      // Set all-day flag if needed
      if (isAllDay) {
        setAllDay(true);
      }
    }

    // Helper methods to adjust times for all-day events
    private static LocalDateTime getAdjustedStartTime(boolean isAllDay, LocalDateTime startDateTime) {
      if (isAllDay && startDateTime == null) {
        return LocalDateTime.of(LocalDate.of(2023, 4, 10), LocalTime.of(0, 0));
      }
      return startDateTime;
    }

    private static LocalDateTime getAdjustedEndTime(boolean isAllDay, LocalDateTime startDateTime, LocalDateTime endDateTime) {
      if (isAllDay && endDateTime == null) {
        LocalDate date = startDateTime != null ?
                startDateTime.toLocalDate() :
                LocalDate.of(2023, 4, 10);
        return LocalDateTime.of(date, LocalTime.of(23, 59, 59));
      }
      return endDateTime;
    }
  }

  private MockCalendar calendar;
  private PrintEventsCommand command;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    command = new PrintEventsCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("print", command.getName());
  }

  @Test
  public void testExecuteWithInsufficientArguments() {
    // Setup
    String[] args = {"on_date"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Error: Insufficient arguments for print command", result);
    assertEquals(null, calendar.getLastCheckedDate());
    assertEquals(null, calendar.getLastCheckedStartDate());
    assertEquals(null, calendar.getLastCheckedEndDate());
  }

  @Test
  public void testExecuteWithInvalidCommandType() {
    // Setup
    String[] args = {"invalid_type", "2023-04-10"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Unknown print command type: invalid_type", result);
    assertEquals(null, calendar.getLastCheckedDate());
    assertEquals(null, calendar.getLastCheckedStartDate());
    assertEquals(null, calendar.getLastCheckedEndDate());
  }

  @Test
  public void testExecutePrintOnDateWithNoEvents() {
    // Setup
    String[] args = {"on_date", "2023-04-10"};
    LocalDate expectedDate = LocalDate.parse("2023-04-10");
    calendar.setEventsOnDateResult(new ArrayList<>());

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDate, calendar.getLastCheckedDate());
    assertEquals("No events on 2023-04-10", result);
  }

  @Test
  public void testExecutePrintOnDateWithEvents() {
    // Setup
    String[] args = {"on_date", "2023-04-10"};
    LocalDate expectedDate = LocalDate.parse("2023-04-10");

    // Create regular event
    Event event1 = new MockEvent(
            "Meeting",
            false,
            LocalDateTime.of(2023, 4, 10, 10, 0),
            LocalDateTime.of(2023, 4, 10, 11, 0),
            "Room 101",
            true
    );

    // Create all-day event with proper initialization
    Event event2 = new MockEvent(
            "Conference",
            true,
            LocalDateTime.of(2023, 4, 10, 0, 0),  // Specify start time
            LocalDateTime.of(2023, 4, 10, 23, 59, 59),  // Specify end time
            "Conference Center",
            false
    );

    List<Event> events = Arrays.asList(event1, event2);
    calendar.setEventsOnDateResult(events);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDate, calendar.getLastCheckedDate());
    assertTrue("Result should start with 'Events on 2023-04-10': " + result,
            result.startsWith("Events on 2023-04-10"));
    // Check that event names are in the result
    assertTrue("Result should contain 'Meeting': " + result, result.contains("Meeting"));
    assertTrue("Result should contain 'Conference': " + result, result.contains("Conference"));
  }

  @Test
  public void testExecutePrintOnDateWithInvalidDate() {
    // Setup
    String[] args = {"on_date", "invalid-date"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertTrue(result.startsWith("Error parsing date:"));
    assertEquals(null, calendar.getLastCheckedDate());
  }

  @Test
  public void testExecutePrintDateRangeWithNoEvents() {
    // Setup
    String[] args = {"date_range", "2023-04-10", "2023-04-15"};
    LocalDate expectedStartDate = LocalDate.parse("2023-04-10");
    LocalDate expectedEndDate = LocalDate.parse("2023-04-15");
    calendar.setEventsInRangeResult(new ArrayList<>());

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedStartDate, calendar.getLastCheckedStartDate());
    assertEquals(expectedEndDate, calendar.getLastCheckedEndDate());
    assertEquals("No events from 2023-04-10 to 2023-04-15", result);
  }

  @Test
  public void testExecutePrintDateRangeWithEvents() {
    // Setup
    String[] args = {"date_range", "2023-04-10", "2023-04-15"};
    LocalDate expectedStartDate = LocalDate.parse("2023-04-10");
    LocalDate expectedEndDate = LocalDate.parse("2023-04-15");

    // Create regular event
    Event event1 = new MockEvent(
            "Meeting",
            false,
            LocalDateTime.of(2023, 4, 10, 10, 0),
            LocalDateTime.of(2023, 4, 10, 11, 0),
            "Room 101",
            true
    );

    // Create all-day event with proper initialization
    Event event2 = new MockEvent(
            "Conference",
            true,
            LocalDateTime.of(2023, 4, 12, 0, 0),  // Specify start time
            LocalDateTime.of(2023, 4, 12, 23, 59, 59),  // Specify end time
            "Conference Center",
            false
    );

    List<Event> events = Arrays.asList(event1, event2);
    calendar.setEventsInRangeResult(events);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedStartDate, calendar.getLastCheckedStartDate());
    assertEquals(expectedEndDate, calendar.getLastCheckedEndDate());
    assertTrue("Result should start with 'Events from 2023-04-10 to 2023-04-15': " + result,
            result.startsWith("Events from 2023-04-10 to 2023-04-15"));
    // Check that event names are in the result
    assertTrue("Result should contain 'Meeting': " + result, result.contains("Meeting"));
    assertTrue("Result should contain 'Conference': " + result, result.contains("Conference"));
  }

  @Test
  public void testExecutePrintFromRangeWithEvents() {
    // Setup - test the alternative type name "from_range"
    String[] args = {"from_range", "2023-04-10", "2023-04-15"};
    LocalDate expectedStartDate = LocalDate.parse("2023-04-10");
    LocalDate expectedEndDate = LocalDate.parse("2023-04-15");

    // Create test events
    Event event1 = new MockEvent(
            "Meeting",
            false,
            LocalDateTime.of(2023, 4, 10, 10, 0),
            LocalDateTime.of(2023, 4, 10, 11, 0),
            "Room 101",
            true
    );

    List<Event> events = Arrays.asList(event1);
    calendar.setEventsInRangeResult(events);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedStartDate, calendar.getLastCheckedStartDate());
    assertEquals(expectedEndDate, calendar.getLastCheckedEndDate());
    assertTrue(result.startsWith("Events from 2023-04-10 to 2023-04-15"));
    assertTrue(result.contains("Meeting"));
  }

  @Test
  public void testExecutePrintRangeWithInvalidDates() {
    // Setup
    String[] args = {"date_range", "invalid-start", "2023-04-15"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertTrue(result.startsWith("Error parsing dates:"));
    assertEquals(null, calendar.getLastCheckedStartDate());
    assertEquals(null, calendar.getLastCheckedEndDate());
  }

  @Test
  public void testExecutePrintRangeWithInsufficientDates() {
    // Setup
    String[] args = {"date_range", "2023-04-10"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Error: Missing dates for 'print events from...to' command", result);
    assertEquals(null, calendar.getLastCheckedStartDate());
    assertEquals(null, calendar.getLastCheckedEndDate());
  }

  @Test
  public void testConstructorWithNullCalendar() {
    try {
      new PrintEventsCommand(null);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(true);
    }
  }
}