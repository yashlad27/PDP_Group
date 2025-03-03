package controller.parser;

import controller.command.ICommand;
import controller.command.CommandFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Improved parser for command-line input that uses a CommandFactory.
 */
public class CommandParser {

  private final CommandFactory commandFactory;

  // Command patterns
  private static final Pattern CREATE_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");

  private static final Pattern CREATE_RECURRING_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times");

  private static final Pattern CREATE_RECURRING_UNTIL_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) from (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) to (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern CREATE_ALL_DAY_EVENT_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern CREATE_ALL_DAY_RECURRING_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) for (\\d+) times");

  private static final Pattern CREATE_ALL_DAY_RECURRING_UNTIL_PATTERN =
          Pattern.compile("create event (--autoDecline )?(.+?) on (\\d{4}-\\d{2}-\\d{2}) repeats ([MTWRFSU]+) until (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern PRINT_EVENTS_PATTERN =
          Pattern.compile("print events on (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern PRINT_EVENTS_RANGE_PATTERN =
          Pattern.compile("print events from (\\d{4}-\\d{2}-\\d{2}) to (\\d{4}-\\d{2}-\\d{2})");

  private static final Pattern SHOW_STATUS_PATTERN =
          Pattern.compile("show status on (\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})");

  private static final Pattern EXPORT_CALENDAR_PATTERN =
          Pattern.compile("export cal (.+)");

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
      String[] args = {"on", matcher.group(1)};
      return new CommandWithArgs(printCommand, args);
    }

    // Print events in range
    matcher = PRINT_EVENTS_RANGE_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand printCommand = commandFactory.getCommand("print");
      String[] args = {"from", matcher.group(1), "to", matcher.group(2)};
      return new CommandWithArgs(printCommand, args);
    }

    // Show status
    matcher = SHOW_STATUS_PATTERN.matcher(commandString);
    if (matcher.matches()) {
      ICommand showCommand = commandFactory.getCommand("show");
      String[] args = {"status", "on", matcher.group(1)};
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
}