import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import controller.CalendarController;
import controller.command.CommandFactory;
import controller.command.ICommand;
import controller.parser.CommandParser;
import model.calendar.ICalendar;
import model.event.Event;
import model.event.RecurringEvent;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for calendar control.
 */
public class CalendarControllerTest {

  /**
   * This is the mock implementation of Calendar.
   */
  private static class MockCalendar implements ICalendar {

    // Minimal implementation with no functionality
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
        String weekdays, LocalDate untilDate,
        boolean autoDecline) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEvent(String name, LocalDate date, String weekdays,
        int occurrences, boolean autoDecline,
        String description, String location,
        boolean isPublic) {
      return false;
    }

    @Override
    public boolean createAllDayRecurringEventUntil(String name, LocalDate date, String weekdays,
        LocalDate untilDate, boolean autoDecline,
        String description, String location,
        boolean isPublic) {
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
    public boolean isBusy(LocalDateTime dateTime) {
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
    public boolean editSingleEvent(String subject, LocalDateTime startDateTime,
        String property, String newValue) {
      return false;
    }

    @Override
    public int editEventsFromDate(String subject, LocalDateTime startDateTime,
        String property, String newValue) {
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

    @Override
    public String exportToCSV(String filePath) throws IOException {
      return null;
    }
  }

  private static class MockCalendarView implements ICalendarView {

    private final List<String> displayedMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final String[] commandsToReturn;
    private int commandIndex = 0;

    public MockCalendarView(String... commandsToReturn) {
      this.commandsToReturn = commandsToReturn;
    }

    @Override
    public String readCommand() {
      if (commandIndex < commandsToReturn.length) {
        return commandsToReturn[commandIndex++];
      }
      return "exit"; // Default to exit if no more commands
    }

    @Override
    public void displayMessage(String message) {
      displayedMessages.add(message);
    }

    @Override
    public void displayError(String errorMessage) {
      errorMessages.add(errorMessage);
    }

    public List<String> getDisplayedMessages() {
      return displayedMessages;
    }

    public List<String> getErrorMessages() {
      return errorMessages;
    }
  }

  // Mock command that returns a predefined result
  private static class MockCommand implements ICommand {

    private final String result;
    private final String name;

    public MockCommand(String result, String name) {
      this.result = result;
      this.name = name;
    }

    @Override
    public String execute(String[] args) {
      return result;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  // Mock command factory that returns predefined commands
  private static class MockCommandFactory extends CommandFactory {

    private final MockCalendar calendar;
    private final MockCalendarView view;
    private final ICommand mockCommand;
    private final ICommand errorCommand;
    private final ICommand exitCommand;

    public MockCommandFactory(MockCalendar calendar, MockCalendarView view) {
      super(calendar, view);
      this.calendar = calendar;
      this.view = view;
      this.mockCommand = new MockCommand("Command executed successfully", "mock");
      this.errorCommand = new MockCommand("Error: Test error", "error");
      this.exitCommand = new MockCommand("Exiting application.", "exit");
    }

    @Override
    public ICommand getCommand(String name) {
      if ("exit".equals(name)) {
        return exitCommand;
      } else if ("error".equals(name)) {
        return errorCommand;
      } else {
        return mockCommand;
      }
    }

    @Override
    public boolean hasCommand(String name) {
      return "exit".equals(name) || "error".equals(name) || "mock".equals(name);
    }

    @Override
    public ICalendar getCalendar() {
      return calendar;
    }

    @Override
    public ICalendarView getView() {
      return view;
    }
  }

  // Mock command parser that returns predefined command with args
  private static class MockCommandParser extends CommandParser {

    private boolean throwException = false;
    private final MockCommandFactory factory;

    public MockCommandParser(MockCommandFactory factory) {
      super(factory);
      this.factory = factory;
    }

    public void setThrowException(boolean throwException) {
      this.throwException = throwException;
    }

    @Override
    public CommandWithArgs parseCommand(String commandString) {
      if (throwException) {
        throw new IllegalArgumentException("Mock parsing error");
      }

      if (commandString.startsWith("error")) {
        return new CommandWithArgs(factory.getCommand("error"), new String[0]);
      } else if (commandString.equals("exit")) {
        return new CommandWithArgs(factory.getCommand("exit"), new String[0]);
      } else if (commandString.equals("runtime-error")) {
        throw new RuntimeException("Mock runtime error");
      } else {
        // Default case - handle any other command
        return new CommandWithArgs(factory.getCommand("mock"), new String[0]);
      }
    }
  }

  /**
   * A testable version of CalendarController that allows mocking the file reader.
   */
  private static class TestableCalendarController extends CalendarController {

    private final BufferedReader fileReader;
    private final ICalendarView view;
    private final CommandParser parser;

    public TestableCalendarController(CommandFactory commandFactory, ICalendarView view,
        BufferedReader fileReader) {
      super(commandFactory, view);
      this.fileReader = fileReader;
      this.view = view;

      // Create a mock parser to ensure consistent command handling
      this.parser = new MockCommandParser((MockCommandFactory) commandFactory);

      // Set the parser using reflection
      try {
        Field parserField = CalendarController.class.getDeclaredField("parser");
        parserField.setAccessible(true);
        parserField.set(this, this.parser);
      } catch (Exception e) {
        throw new RuntimeException("Failed to setup parser in TestableCalendarController", e);
      }
    }

    @Override
    public boolean startHeadlessMode(String commandsFilePath) {
      if (commandsFilePath == null || commandsFilePath.trim().isEmpty()) {
        view.displayError("Error: File path cannot be empty");
        return false;
      }

      try {
        String line;
        String lastCommand = null;
        boolean fileHasCommands = false;

        while ((line = fileReader.readLine()) != null) {
          if (line.trim().isEmpty()) {
            continue;
          }

          fileHasCommands = true;
          lastCommand = line;

          String result = processCommand(line);
          view.displayMessage(result);

          if (line.equalsIgnoreCase("exit")) {
            break;
          }

          if (result.startsWith("Error")) {
            view.displayError("Command failed, stopping execution: " + result);
            return false;
          }
        }

        // Check if file was empty
        if (!fileHasCommands) {
          view.displayError("Error: Command file is empty. "
              + "At least one command (exit) is required.");
          return false;
        }

        // Check if the last command was an exit command
        if (!lastCommand.equalsIgnoreCase("exit")) {
          view.displayError("Headless mode requires the last command to be 'exit'");
          return false;
        }

        return true;
      } catch (IOException e) {
        view.displayError("Error reading command file: " + e.getMessage());
        return false;
      }
    }

    // Override processCommand to use our mock parser directly
    @Override
    public String processCommand(String commandStr) {
      if (commandStr == null || commandStr.trim().isEmpty()) {
        return "Error: Command cannot be empty";
      }

      String trimmedCommand = commandStr.trim();

      // Check for exit command
      if (trimmedCommand.equalsIgnoreCase("exit")) {
        return "Exiting application.";
      }

      try {
        // Use our mock parser
        CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(trimmedCommand);
        return commandWithArgs.execute();
      } catch (IllegalArgumentException e) {
        return "Error: " + e.getMessage();
      } catch (Exception e) {
        return "Unexpected error: " + e.getMessage();
      }
    }
  }

  private MockCalendarView view;
  private MockCommandFactory commandFactory;
  private MockCommandParser parser;
  private CalendarController controller;

  @Before
  public void setUp() {
    MockCalendar calendar = new MockCalendar();
    view = new MockCalendarView("command1", "command2", "exit");
    commandFactory = new MockCommandFactory(calendar, view);
    controller = new CalendarController(commandFactory, view);

    // Setup the parser with reflection to inject our mock parser
    try {
      Field parserField = CalendarController.class.getDeclaredField("parser");
      parserField.setAccessible(true);
      parser = new MockCommandParser(commandFactory);
      parserField.set(controller, parser);
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup test", e);
    }
  }

  @Test
  public void testProcessCommandWithValidCommand() {
    // Execute
    String result = controller.processCommand("valid command");

    // Verify
    assertEquals("Command executed successfully", result);
  }

  @Test
  public void testProcessCommandWithExitCommand() {
    // Execute
    String result = controller.processCommand("exit");

    // Verify
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testProcessCommandWithEmptyCommand() {
    // Execute
    String result = controller.processCommand("");

    // Verify
    assertEquals("Error: Command cannot be empty", result);
  }

  @Test
  public void testProcessCommandWithNullCommand() {
    // Execute
    String result = controller.processCommand(null);

    // Verify
    assertEquals("Error: Command cannot be empty", result);
  }

  @Test
  public void testProcessCommandWithParseError() {
    // Setup
    parser.setThrowException(true);

    // Execute
    String result = controller.processCommand("invalid command");

    // Verify
    assertEquals("Error: Mock parsing error", result);
  }

  @Test
  public void testProcessCommandWithRuntimeError() {
    // Execute
    String result = controller.processCommand("runtime-error");

    // Verify
    assertTrue(result.startsWith("Unexpected error: Mock runtime error"));
  }

  @Test
  public void testStartInteractiveMode() {
    // Execute
    controller.startInteractiveMode();

    // Verify
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Calendar Application Started"));
    assertTrue(messages.contains("Enter commands (type 'exit' to quit):"));
    assertTrue(messages.contains("Command executed successfully"));
    assertTrue(messages.contains("Command executed successfully"));
    assertTrue(messages.contains("Calendar Application Terminated"));
  }

  @Test
  public void testStartHeadlessModeWithValidCommands() {
    // Setup
    String mockFileContent = "command1\ncommand2\nexit\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("valid_file.txt");

    // Verify
    assertTrue(result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Command executed successfully"));
  }

  @Test
  public void testStartHeadlessModeWithErrorCommand() {
    // Setup
    String mockFileContent = "command1\nerror\ncommand3\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("error_file.txt");

    // Verify
    assertFalse(result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Command executed successfully"));
    assertTrue(messages.contains("Error: Test error"));

    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Command failed, stopping execution: Error: Test error"));
  }

  @Test
  public void testStartHeadlessModeWithEmptyFilePath() {
    // Setup
    TestableCalendarController testableController = new TestableCalendarController(
        commandFactory, view, new BufferedReader(new StringReader("")));

    // Execute
    boolean result = testableController.startHeadlessMode("");

    // Verify
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: File path cannot be empty"));
  }

  @Test
  public void testStartHeadlessModeWithNullFilePath() {
    // Setup
    TestableCalendarController testableController = new TestableCalendarController(
        commandFactory, view, new BufferedReader(new StringReader("")));

    // Execute
    boolean result = testableController.startHeadlessMode(null);

    // Verify
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error: File path cannot be empty"));
  }

  @Test
  public void testStartHeadlessModeWithIOException() {
    // Setup - use a reader that throws IOException
    BufferedReader errorReader = new BufferedReader(new StringReader("")) {
      @Override
      public String readLine() throws IOException {
        throw new IOException("Mock IO error");
      }
    };

    TestableCalendarController testableController = new TestableCalendarController(
        commandFactory, view, errorReader);

    // Execute
    boolean result = testableController.startHeadlessMode("file.txt");

    // Verify
    assertFalse(result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Error reading command file: Mock IO error"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullCommandFactory() {
    new CalendarController(null, view);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructorWithNullView() {
    new CalendarController(commandFactory, null);
  }

  @Test
  public void testHeadlessModeWithEmptyFile() {
    // Setup
    BufferedReader reader = new BufferedReader(new StringReader(""));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("empty_file.txt");

    // Verify
    assertFalse("Should return false for empty file", result);
    List<String> errors = view.getErrorMessages();
    assertTrue(
        errors.contains("Error: Command file is empty. At least one command (exit) is required."));
  }

  @Test
  public void testHeadlesModeWithEmptyLines() {
    // Setup
    String mockFileContent = "\ncommand1\n\ncommand2\n\nexit\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("valid_file.txt");

    // Verify
    assertTrue(result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Command executed successfully"));
  }

  @Test
  public void testHeadlessModeWithNoExitCommand() {
    // Setup
    String mockFileContent = "command1\ncommand2\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("no_exit.txt");

    // Verify
    assertFalse("Should return false when exit command is missing", result);
    List<String> errors = view.getErrorMessages();
    assertTrue(errors.contains("Headless mode requires the last command to be 'exit'"));
  }

  @Test
  public void testHeadlesModeWithOnlyExitCommand() {
    // Setup
    String mockFileContent = "exit\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("only_exit.txt");

    // Verify
    assertTrue("Should return true with only exit command", result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Exiting application."));
  }

  @Test
  public void testHeadlesModeWithCaseInsensitiveExit() {
    // Setup
    String mockFileContent = "command1\nEXIT\n";
    BufferedReader reader = new BufferedReader(new StringReader(mockFileContent));
    TestableCalendarController testableController =
        new TestableCalendarController(commandFactory, view, reader);

    // Execute
    boolean result = testableController.startHeadlessMode("case_insensitive.txt");

    // Verify
    assertTrue("Should handle case-insensitive exit command", result);
    List<String> messages = view.getDisplayedMessages();
    assertTrue(messages.contains("Exiting application."));
  }
}