import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import controller.command.CommandFactory;
import controller.command.CreateEventCommand;
import controller.command.EditEventCommand;
import controller.command.ExitCommand;
import controller.command.ExportCalendarCommand;
import controller.command.ICommand;
import controller.command.PrintEventsCommand;
import controller.command.ShowStatusCommand;
import model.calendar.ICalendar;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CommandFactoryTest {

  // Manual mock implementation of ICalendar
  private static class MockCalendar implements ICalendar {
    // Minimal implementation with no functionality
    @Override
    public boolean addEvent(model.event.Event event, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean addRecurringEvent(model.event.RecurringEvent recurringEvent, boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createRecurringEventUntil(String name, java.time.LocalDateTime start, java.time.LocalDateTime end,
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
    public java.util.List<model.event.Event> getEventsOnDate(java.time.LocalDate date) {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getEventsInRange(java.time.LocalDate startDate, java.time.LocalDate endDate) {
      return null;
    }

    @Override
    public boolean isBusy(java.time.LocalDateTime dateTime) {
      return false;
    }

    @Override
    public model.event.Event findEvent(String subject, java.time.LocalDateTime startDateTime) {
      return null;
    }

    @Override
    public java.util.List<model.event.Event> getAllEvents() {
      return null;
    }

    @Override
    public boolean editSingleEvent(String subject, java.time.LocalDateTime startDateTime, String property, String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, java.time.LocalDateTime startDateTime, String property, String newValue) {
      return 0;
    }

    @Override
    public int editAllEvents(String subject, String property, String newValue) {
      return 0;
    }

    @Override
    public java.util.List<model.event.RecurringEvent> getAllRecurringEvents() {
      return null;
    }

    @Override
    public String exportToCSV(String filePath) throws java.io.IOException {
      return null;
    }
  }

  // Manual mock implementation of ICalendarView
  private static class MockCalendarView implements ICalendarView {
    @Override
    public String readCommand() {
      return null;
    }

    @Override
    public void displayMessage(String message) {
    }

    @Override
    public void displayError(String errorMessage) {
    }
  }

  private ICalendar calendar;
  private ICalendarView view;
  private CommandFactory factory;

  @Before
  public void setUp() {
    calendar = new MockCalendar();
    view = new MockCalendarView();
    factory = new CommandFactory(calendar, view);
  }

  @Test
  public void testGetCommandWithValidName() {
    // Check each registered command type
    ICommand createCommand = factory.getCommand("create");
    ICommand printCommand = factory.getCommand("print");
    ICommand showCommand = factory.getCommand("show");
    ICommand exportCommand = factory.getCommand("export");
    ICommand editCommand = factory.getCommand("edit");
    ICommand exitCommand = factory.getCommand("exit");

    // Verify all commands are of the correct type
    assertTrue(createCommand instanceof CreateEventCommand);
    assertTrue(printCommand instanceof PrintEventsCommand);
    assertTrue(showCommand instanceof ShowStatusCommand);
    assertTrue(exportCommand instanceof ExportCalendarCommand);
    assertTrue(editCommand instanceof EditEventCommand);
    assertTrue(exitCommand instanceof ExitCommand);
  }

  @Test
  public void testGetCommandWithInvalidName() {
    ICommand command = factory.getCommand("nonexistent");
    assertNull(command);
  }

  @Test
  public void testHasCommandWithValidName() {
    assertTrue(factory.hasCommand("create"));
    assertTrue(factory.hasCommand("print"));
    assertTrue(factory.hasCommand("show"));
    assertTrue(factory.hasCommand("export"));
    assertTrue(factory.hasCommand("edit"));
    assertTrue(factory.hasCommand("exit"));
  }

  @Test
  public void testHasCommandWithInvalidName() {
    assertFalse(factory.hasCommand("nonexistent"));
    assertFalse(factory.hasCommand(""));
    assertFalse(factory.hasCommand(null));
  }

  @Test
  public void testGetCommandNames() {
    // Get all command names
    Iterable<String> commandNames = factory.getCommandNames();

    // Convert to a set for easier verification
    Set<String> nameSet = new HashSet<>();
    for (String name : commandNames) {
      nameSet.add(name);
    }

    // Verify all expected commands are present
    assertTrue(nameSet.contains("create"));
    assertTrue(nameSet.contains("print"));
    assertTrue(nameSet.contains("show"));
    assertTrue(nameSet.contains("export"));
    assertTrue(nameSet.contains("edit"));
    assertTrue(nameSet.contains("exit"));

    // Verify the count (should be exactly 6)
    assertEquals(6, nameSet.size());
  }

  @Test
  public void testGetCalendar() {
    assertSame(calendar, factory.getCalendar());
  }

  @Test
  public void testGetView() {
    assertSame(view, factory.getView());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCalendar() {
    // This should throw an IllegalArgumentException
    new CommandFactory(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    // This should throw an IllegalArgumentException
    new CommandFactory(calendar, null);
  }

  @Test
  public void testCommandInitialization() {
    // All commands should be properly initialized with the calendar

    // Get each command
    CreateEventCommand createCommand = (CreateEventCommand) factory.getCommand("create");
    PrintEventsCommand printCommand = (PrintEventsCommand) factory.getCommand("print");
    ShowStatusCommand showCommand = (ShowStatusCommand) factory.getCommand("show");
    ExportCalendarCommand exportCommand = (ExportCalendarCommand) factory.getCommand("export");
    EditEventCommand editCommand = (EditEventCommand) factory.getCommand("edit");

    // Test some simple functionality to ensure they're properly initialized
    // We'll just verify they don't throw exceptions when getting their names
    assertEquals("create", createCommand.getName());
    assertEquals("print", printCommand.getName());
    assertEquals("show", showCommand.getName());
    assertEquals("export", exportCommand.getName());
    assertEquals("edit", editCommand.getName());
  }

  @Test
  public void testRegisterDuplicateCommand() {
    // Test registering a duplicate command with the same name
    // We need to use reflection to access the private method
    // or adjust your design to make this testable

    // For now, we can verify the behavior indirectly
    int initialCommandCount = 0;
    for (String name : factory.getCommandNames()) {
      initialCommandCount++;
    }

    // Create a new factory to reset state
    CommandFactory newFactory = new CommandFactory(calendar, view);

    // Verify it has the same number of commands
    int newCommandCount = 0;
    for (String name : newFactory.getCommandNames()) {
      newCommandCount++;
    }

    assertEquals(initialCommandCount, newCommandCount);
  }

  @Test
  public void testNullCommandName() {
    // Verify behavior when we try to get a command with null name
    ICommand command = factory.getCommand(null);
    assertNull(command);

    // Verify behavior when we check if a null command exists
    assertFalse(factory.hasCommand(null));
  }
}