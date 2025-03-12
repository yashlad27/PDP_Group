package controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import controller.command.CommandFactory;
import controller.parser.CommandParser;
import model.calendar.ICalendar;
import view.ICalendarView;

/**
 * A simplified implementation of the calendar controller that handles
 * event creation commands.
 */
public class CalendarController {

  private final ICalendar calendar;
  private final ICalendarView view;
  private final CommandParser parser;
  private final CommandFactory commandFactory;
  private static final String EXIT_COMMAND = "exit";

  /**
   * Constructs a new BasicCalendarController.
   *
   * @param commandFactory the command factory
   * @param view           the view for user interaction
   */
  public CalendarController(CommandFactory commandFactory, ICalendarView view) {
    if (commandFactory == null) {
      throw new IllegalArgumentException("CommandFactory cannot be null");
    }
    if (view == null) {
      throw new IllegalArgumentException("View cannot be null");
    }

    this.commandFactory = commandFactory;
    this.view = view;
    this.calendar = commandFactory.getCalendar();
    this.parser = new CommandParser(this.commandFactory);
  }

  /**
   * Processes a command and returns the result.
   *
   * @param commandStr the command string to process
   * @return the result of command execution
   */
  public String processCommand(String commandStr) {
    if (commandStr == null || commandStr.trim().isEmpty()) {
      return "Error: Command cannot be empty";
    }

    String trimmedCommand = commandStr.trim();

    // Check for exit command
    if (trimmedCommand.equalsIgnoreCase(EXIT_COMMAND)) {
      return "Exiting application.";
    }

    try {
      // Parse the command and execute it directly through CommandWithArgs
      CommandParser.CommandWithArgs commandWithArgs = parser.parseCommand(trimmedCommand);
      return commandWithArgs.execute();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    } catch (Exception e) {
      return "Unexpected error: " + e.getMessage();
    }
  }

  /**
   * Starts the controller in interactive mode.
   */
  public void startInteractiveMode() {
    view.displayMessage("Calendar Application Started");
    view.displayMessage("Enter commands (type 'exit' to quit):");

    String command;
    while (!(command = view.readCommand()).equalsIgnoreCase(EXIT_COMMAND)) {
      String result = processCommand(command);
      view.displayMessage(result);
    }

    view.displayMessage("Calendar Application Terminated");
  }

  /**
   * Starts the controller in headless mode with commands from a file.
   *
   * @param commandsFilePath the path to the file containing commands
   * @return true if all commands were executed successfully, false otherwise
   */
  public boolean startHeadlessMode(String commandsFilePath) {
    if (commandsFilePath == null || commandsFilePath.trim().isEmpty()) {
      view.displayError("Error: File path cannot be empty");
      return false;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(commandsFilePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.trim().isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase(EXIT_COMMAND)) {
          String result = processCommand(line);
          view.displayMessage(result);
          break;
        }

        String result = processCommand(line);
        view.displayMessage(result);

        if (line.equalsIgnoreCase(EXIT_COMMAND)) {
          break;
        }

        if (result.startsWith("Error")) {
          view.displayError("Command failed, stopping execution: " + result);
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      view.displayError("Error reading command file: " + e.getMessage());
      return false;
    }
  }
}