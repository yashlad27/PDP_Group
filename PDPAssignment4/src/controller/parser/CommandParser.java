package controller.parser;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import controller.command.CommandFactory;
import controller.command.CreateEventCommand;
import controller.command.EditEventCommand;
import controller.command.ICommand;
import model.calendar.ICalendar;
import utilities.DateTimeUtil;

/**
 * Improved parser for command-line input that uses a CommandFactory.
 */
public class CommandParser {

  private final CommandFactory commandFactory;

  // Command patterns
  private static final Pattern CREATE_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) (desc \"(.+?)\") ? ( at \"(.+?)\") ? ( private)?");

  private static final Pattern CREATE_RECURRING_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times ( desc \"(.+?)\") ? ( at \"(.+?)\") ? ( private)?");

  private static final Pattern CREATE_RECURRING_UNTIL_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2}) ( desc \"(.+?)\") ? ( at \"(.+?)\") ? ( private)?");

  private static final Pattern CREATE_ALL_DAY_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2}) ( desc \"(.+?)\")?( at \"(.+?)\")?( private)?");

  private static final Pattern CREATE_ALL_DAY_RECURRING_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times ( desc \"(.+?)\")?( at \"(.+?)\")?( private)?");

  private static final Pattern CREATE_ALL_DAY_RECURRING_UNTIL_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2}) ( desc \"(.+?)\")?( at \"(.+?)\")?( private)?");

  private static final Pattern PRINT_EVENTS_PATTERN =
          Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern PRINT_EVENTS_RANGE_PATTERN =
          Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) to (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern SHOW_STATUS_PATTERN =
          Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");

  private static final Pattern EXPORT_CALENDAR_PATTERN =
          Pattern.compile("export cal (.+)");

  private static final Pattern PRINT_EVENTS_ON_DATE_PATTERN =
          Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern PRINT_EVENTS_IN_RANGE_PATTERN =
          Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) to (\\d{4}-\\d{2}-\\d{2})");

  /**
   * Constructs a new CommandParser.
   *
   * @param commandFactory the factory for creating commands
   */
  public CommandParser(CommandFactory commandFactory) {
    this.commandFactory = commandFactory;
  }

  /**
   * Parses a command string and returns the appropriate Command object with arguments.
   *
   * @param commandString the command string to parse
   * @return a Command object that can execute the requested operation
   * @throws IllegalArgumentException if the command is invalid or unsupported
   */
  public CommandWithArgs parseCommand(String commandString) {

    // Parse create event commands
    if (commandString.startsWith("create event")) {
      return parseCreateEventCommand(commandString);
    }

    // Add this block to handle edit commands
    if (commandString.startsWith("edit event") || commandString.startsWith("edit events")) {
      return parseEditEventCommand(commandString);
    }

    if (commandString == null || commandString.trim().isEmpty()) {
      throw new IllegalArgumentException("Command string cannot be null or empty");
    }

    commandString = commandString.trim();

    // Check for exit command
    if (commandString.equalsIgnoreCase("exit")) {
      ICommand exitCommand = commandFactory.getCommand("exit");
      return new CommandWithArgs(exitCommand, new String[0]);
    }

    // Match against the different command patterns
    Matcher matcher;

    // Print events on date
    matcher = PRINT_EVENTS_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand printCommand = commandFactory.getCommand("print");
      String[] args = {"on_date", matcher.group(1)};
      return new CommandWithArgs(printCommand, args);
    }

    // Print events in range
    matcher = PRINT_EVENTS_RANGE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand printCommand = commandFactory.getCommand("print");
      String[] args = {"from_range", matcher.group(1), matcher.group(2)};
      return new CommandWithArgs(printCommand, args);
    }

    // Show status
    matcher = SHOW_STATUS_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand showCommand = commandFactory.getCommand("show");
      String[] args = {matcher.group(1)};
      return new CommandWithArgs(showCommand, args);
    }

    // Export calendar
    matcher = EXPORT_CALENDAR_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand exportCommand = commandFactory.getCommand("export");
      String[] args = {matcher.group(1)};
      return new CommandWithArgs(exportCommand, args);
    }

    // Parse create event commands
    if (commandString.startsWith("create event")) {
      return parseCreateEventCommand(commandString);
    }

    // If we reach here, the command was not recognized
    throw new IllegalArgumentException("Unrecognized or unsupported command: " + commandString);
  }

  /**
   * Parses a create event command.
   *
   * @param commandString the command string to parse
   * @return a CommandWithArgs for creating an event
   * @throws IllegalArgumentException if the command is invalid
   */
  private CommandWithArgs parseCreateEventCommand(String commandString) {
    ICommand createCommand = commandFactory.getCommand("create");
    Matcher matcher;

    // Regular event
    matcher = CREATE_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "single",
              matcher.group(2),  // event name
              matcher.group(3),  // start date/time
              matcher.group(4),  // end date/time
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    // Recurring event with occurrences
    matcher = CREATE_RECURRING_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "recurring",
              matcher.group(2),  // event name
              matcher.group(3),  // start date/time
              matcher.group(4),  // end date/time
              matcher.group(5),  // weekdays
              matcher.group(6),  // occurrences
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    // Recurring event until date
    matcher = CREATE_RECURRING_UNTIL_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "recurring-until",
              matcher.group(2),  // event name
              matcher.group(3),  // start date/time
              matcher.group(4),  // end date/time
              matcher.group(5),  // weekdays
              matcher.group(6),  // until date
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    // All-day event
    matcher = CREATE_ALL_DAY_EVENT_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "allday",
              matcher.group(2),  // event name
              matcher.group(3),  // date
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    // All-day recurring event with occurrences
    matcher = CREATE_ALL_DAY_RECURRING_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "allday-recurring",
              matcher.group(2),  // event name
              matcher.group(3),  // date
              matcher.group(4),  // weekdays
              matcher.group(5),  // occurrences
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    // All-day recurring event until date
    matcher = CREATE_ALL_DAY_RECURRING_UNTIL_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      boolean autoDecline = matcher.group(1) != null;
      String[] args = {
              "allday-recurring-until",
              matcher.group(2),  // event name
              matcher.group(3),  // date
              matcher.group(4),  // weekdays
              matcher.group(5),  // until date
              String.valueOf(autoDecline)
      };
      return new CommandWithArgs(createCommand, args);
    }

    throw new IllegalArgumentException("Invalid create event command format");
  }

  /**
   * Helper class to hold a command and its arguments.
   */
  public static class CommandWithArgs {
    private final ICommand command;
    private final String[] args;

    public CommandWithArgs(ICommand command, String[] args) {
      this.command = command;
      this.args = args;
    }

    public ICommand getCommand() {
      return command;
    }

    public String[] getArgs() {
      return args;
    }

    public String execute() {
      return command.execute(args);
    }
  }

  public ICommand parseEditEventCommand(String commandString, ICalendar calendar) {
    // Pattern for edit single event
    Pattern singlePattern = Pattern.compile(
            "edit event (\\w+) (.+) from (\\S+T\\S+) to (\\S+T\\S+) with (.+)");

    // Pattern for edit events from date
    Pattern seriesFromDatePattern = Pattern.compile(
            "edit events (\\w+) (.+) from (\\S+T\\S+) with (.+)");

    // Pattern for edit all events
    Pattern allEventsPattern = Pattern.compile(
            "edit events (\\w+) (.+) (.+)");

    // Try to match single event edit pattern
    Matcher singleMatcher = singlePattern.matcher(commandString);
    if (singleMatcher.matches()) {
      String property = singleMatcher.group(1);
      String subject = singleMatcher.group(2);
      LocalDateTime startDateTime = DateTimeUtil.parseDateTime(singleMatcher.group(3));
      LocalDateTime endDateTime = DateTimeUtil.parseDateTime(singleMatcher.group(4));
      String newValue = singleMatcher.group(5);

      return new EditEventCommand(calendar, "single", property, subject,
              startDateTime, newValue);
    }

    // Try to match series from date pattern
    Matcher seriesFromDateMatcher = seriesFromDatePattern.matcher(commandString);
    if (seriesFromDateMatcher.matches()) {
      String property = seriesFromDateMatcher.group(1);
      String subject = seriesFromDateMatcher.group(2);
      LocalDateTime startDateTime = DateTimeUtil.parseDateTime(seriesFromDateMatcher.group(3));
      String newValue = seriesFromDateMatcher.group(4);

      return new EditEventCommand(calendar, "series_from_date", property, subject,
              startDateTime, newValue);
    }

    // Try to match all events pattern
    Matcher allEventsMatcher = allEventsPattern.matcher(commandString);
    if (allEventsMatcher.matches()) {
      String property = allEventsMatcher.group(1);
      String subject = allEventsMatcher.group(2);
      String newValue = allEventsMatcher.group(3);

      return new EditEventCommand(calendar, "all", property, subject, newValue);
    }

    throw new IllegalArgumentException("Invalid edit command format: " + commandString);
  }

  /**
   * Parses a command string and returns the appropriate Command object.
   */
  public ICommand parseCommand(String commandString, ICalendar calendar) {
    if (commandString.startsWith("create event")) {
      // Create an instance of CreateEventCommand with the calendar
      CreateEventCommand createCommand = new CreateEventCommand(calendar);
      // The execution with arguments will happen when execute() is called
      return createCommand;
    } else if (commandString.startsWith("edit event") ||
            commandString.startsWith("edit events")) {
      return parseEditEventCommand(commandString, calendar);
    }
    // Add other command types as needed

    throw new IllegalArgumentException("Unknown command: " + commandString);
  }

  private CommandWithArgs parseEditEventCommand(String commandString) {
    ICommand editCommand = commandFactory.getCommand("edit");
    Matcher matcher;

    // Pattern for edit single event
    Pattern singlePattern = Pattern.compile(
            "edit event (\\w+) (.+) from (\\S+T\\S+) to (\\S+T\\S+) with (.+)");

    // Pattern for edit events from date
    Pattern seriesFromDatePattern = Pattern.compile(
            "edit events (\\w+) (.+) from (\\S+T\\S+) with (.+)");

    // Pattern for edit all events
    Pattern allEventsPattern = Pattern.compile(
            "edit events (\\w+) (.+) (.+)");

    // Try to match single event edit pattern
    matcher = singlePattern.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
              "single",
              matcher.group(1),  // property
              matcher.group(2),  // subject
              matcher.group(3),  // startDateTime
              matcher.group(5)   // newValue
      };
      return new CommandWithArgs(editCommand, args);
    }

    // Try to match series from date pattern
    matcher = seriesFromDatePattern.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
              "series_from_date",
              matcher.group(1),  // property
              matcher.group(2),  // subject
              matcher.group(3),  // startDateTime
              matcher.group(4)   // newValue
      };
      return new CommandWithArgs(editCommand, args);
    }

    // Try to match all events pattern
    matcher = allEventsPattern.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
              "all",
              matcher.group(1),  // property
              matcher.group(2),  // subject
              matcher.group(3)   // newValue
      };
      return new CommandWithArgs(editCommand, args);
    }

    throw new IllegalArgumentException("Invalid edit command format: " + commandString);
  }

  private CommandWithArgs parsePrintEventsCommand(String commandString) {
    ICommand printCommand = commandFactory.getCommand("print");
    Matcher matcher;

    // Try to match "print events on" pattern
    matcher = PRINT_EVENTS_ON_DATE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
              "on_date",
              matcher.group(1)  // date
      };
      return new CommandWithArgs(printCommand, args);
    }

    // Try to match "print events from...to" pattern
    matcher = PRINT_EVENTS_IN_RANGE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      String[] args = {
              "date_range",
              matcher.group(1),  // startDate
              matcher.group(2)   // endDate
      };
      return new CommandWithArgs(printCommand, args);
    }

    throw new IllegalArgumentException("Invalid print command format: " + commandString);
  }

  private CommandWithArgs parseShowStatusCommand(String commandString) {
    ICommand statusCommand = commandFactory.getCommand("show");
    Matcher matcher = SHOW_STATUS_PATTERN.matcher(commandString);

    if (matcher.matches()) {
      String[] args = {
              matcher.group(1)  // dateTime
      };
      return new CommandWithArgs(statusCommand, args);
    }

    throw new IllegalArgumentException("Invalid status command format: " + commandString);
  }

  private CommandWithArgs parseExportCommand(String commandString) {
    ICommand exportCommand = commandFactory.getCommand("export");
    Matcher matcher = EXPORT_CALENDAR_PATTERN.matcher(commandString);

    if (matcher.matches()) {
      String[] args = {
              matcher.group(1)  // filename
      };
      return new CommandWithArgs(exportCommand, args);
    }

    throw new IllegalArgumentException("Invalid export command format: " + commandString);
  }
}
