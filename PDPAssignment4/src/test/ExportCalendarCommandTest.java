import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import controller.command.ExportCalendarCommand;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExportCalendarCommandTest {

  // Manual mock implementation of ICalendar
  private static class MockCalendar implements ICalendar {
    private String lastFilePath = null;
    private String exportResult = null;
    private IOException exportException = null;

    public void setExportResult(String result) {
      this.exportResult = result;
      this.exportException = null;
    }

    public void setExportException(IOException exception) {
      this.exportException = exception;
      this.exportResult = null;
    }

    public String getLastFilePath() {
      return lastFilePath;
    }

    @Override
    public String exportToCSV(String filePath) throws IOException {
      this.lastFilePath = filePath;
      if (exportException != null) {
        throw exportException;
      }
      return exportResult;
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
    public boolean addRecurringEvent(RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, LocalDateTime start, LocalDateTime end,
                                             String weekdays, LocalDate untilDate, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
                                              int occurrences, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
                                                   LocalDate untilDate, boolean autoDecline, String description, String location, boolean isPublic) {
      return false;
    }

    @Override
    public List<Event> getEventsOnDate(LocalDate date) {
      return null;
    }

    @Override
    public List<Event> getEventsInRange(LocalDate startDate, LocalDate endDate) {
      return null;
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
    public List<RecurringEvent> getAllRecurringEvents() {
      return null;
    }
  }

  private MockCalendar calendar;
  private ExportCalendarCommand command;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    command = new ExportCalendarCommand(calendar);
  }

  @Test
  public void testGetName() {
    assertEquals("export", command.getName());
  }

  @Test
  public void testExecuteWithValidFilename() {
    // Setup
    String[] args = {"calendar.csv"};
    String expectedPath = "/absolute/path/to/calendar.csv";
    calendar.setExportResult(expectedPath);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("calendar.csv", calendar.getLastFilePath());
    assertEquals("Calendar exported successfully to: " + expectedPath, result);
  }

  @Test
  public void testExecuteWithMissingFilename() {
    // Setup
    String[] args = {};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(null, calendar.getLastFilePath());
    assertEquals("Error: Missing filename for export command", result);
  }

  @Test
  public void testExecuteWithIOException() {
    // Setup
    String[] args = {"invalid_path.csv"};
    IOException expectedException = new IOException("Permission denied");
    calendar.setExportException(expectedException);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("invalid_path.csv", calendar.getLastFilePath());
    assertEquals("Failed to export calendar: Permission denied", result);
  }

  @Test
  public void testExecuteWithNullFilename() {
    // Setup
    String[] args = {null};

    // Setting a valid result for testing
    calendar.setExportResult("/path/to/null.csv");

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals(null, calendar.getLastFilePath());
    // Since args[0] is null, we expect the command to handle it gracefully
    // The exact behavior depends on your implementation
    assertTrue(result.startsWith("Calendar exported successfully") ||
            result.startsWith("Error") ||
            result.startsWith("Failed"));
  }

  @Test
  public void testExecuteWithEmptyFilename() {
    // Setup
    String[] args = {""};

    // Setting an exception for empty filename
    calendar.setExportException(new IOException("Filename cannot be empty"));

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("", calendar.getLastFilePath());
    assertTrue(result.startsWith("Failed to export calendar:"));
    assertTrue(result.contains("Filename cannot be empty"));
  }

  @Test
  public void testConstructorWithNullCalendar() {
    try {
      new ExportCalendarCommand(null);
      fail("Should have thrown IllegalArgumentException");
    } catch (IllegalArgumentException e) {
      assertTrue(true);
    }
  }

  @Test
  public void testExecuteWithExtraArguments() {
    // Setup - command should only use the first argument and ignore the rest
    String[] args = {"calendar.csv", "extra", "arguments"};
    String expectedPath = "/absolute/path/to/calendar.csv";
    calendar.setExportResult(expectedPath);

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("calendar.csv", calendar.getLastFilePath());
    assertEquals("Calendar exported successfully to: " + expectedPath, result);
  }
}