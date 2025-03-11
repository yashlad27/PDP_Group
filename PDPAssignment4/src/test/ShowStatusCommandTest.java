import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import controller.command.ShowStatusCommand;
import model.calendar.ICalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ShowStatusCommandTest {

  // Manual mock implementation of ICalendar
  private static class MockCalendar implements ICalendar {
    private boolean isBusyResult = false;
    private LocalDateTime lastCheckedDateTime = null;

    public void setIsBusyResult(boolean result) {
      this.isBusyResult = result;
    }

    public LocalDateTime getLastCheckedDateTime() {
      return lastCheckedDateTime;
    }

    @Override
    public boolean isBusy(LocalDateTime dateTime) {
      this.lastCheckedDateTime = dateTime;
      return isBusyResult;
    }

    // Implement other methods from ICalendar with minimal implementations
    @Override
    public boolean addEvent(model.event.Event event, boolean autoDecline) {
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
    public List<model.event.Event> getEventsOnDate(java.time.LocalDate date) {
      return null;
    }

    @Override
    public List<model.event.Event> getEventsInRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
      return null;
    }

    @Override
    public model.event.Event findEvent(String subject, LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public List<model.event.Event> getAllEvents() {
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

  private MockCalendar calendar;
  private ShowStatusCommand command;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    command = new ShowStatusCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("show", command.getName());
  }

  @Test
  public void testExecuteWithValidDateTimeBusy() {
    // Setup
    String[] args = {"2023-04-10T10:30"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(true);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Busy", result);
  }

  @Test
  public void testExecuteWithValidDateTimeAvailable() {
    // Setup
    String[] args = {"2023-04-10T10:30"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(false);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Available", result);
  }

  @Test
  public void testExecuteWithMissingArguments() {
    // Setup
    String[] args = {};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Error: Missing date/time for status command", result);
    assertEquals(null, calendar.getLastCheckedDateTime()); // Calendar should not have been called
  }

  @Test
  public void testExecuteWithInvalidDateTime() {
    // Setup
    String[] args = {"invalid-date-time"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertTrue(result.startsWith("Error parsing date/time:"));
    assertEquals(null, calendar.getLastCheckedDateTime()); // Calendar should not have been called
  }

  @Test
  public void testConstructorWithNullCalendar() {
    try {
      new ShowStatusCommand(null);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      // Expected exception
      assertTrue(true);
    }
  }

  @Test
  public void testExecuteAtMidnight() {
    // Setup
    String[] args = {"2023-04-10T00:00"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T00:00");
    calendar.setIsBusyResult(true);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T00:00:00: Busy", result);
  }

  @Test
  public void testExecuteWithExtraArguments() {
    // Setup - command should only use the first argument and ignore the rest
    String[] args = {"2023-04-10T10:30", "extra", "arguments"};
    LocalDateTime expectedDateTime = LocalDateTime.parse("2023-04-10T10:30");
    calendar.setIsBusyResult(true);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(expectedDateTime, calendar.getLastCheckedDateTime());
    assertEquals("Status on 2023-04-10T10:30:00: Busy", result);
  }
}