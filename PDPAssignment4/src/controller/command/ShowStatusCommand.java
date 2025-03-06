package controller.command;

import model.calendar.ICalendar;
import utilities.DateTimeUtil;

import java.time.LocalDateTime;

/**
 * Command for checking the user's status (busy or available) at a specific time.
 */
public class ShowStatusCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new ShowStatusCommand.
   *
   * @param calendar the calendar model
   */
  public ShowStatusCommand(ICalendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 3 || !args[0].equals("status") || !args[1].equals("on")) {
      return "Error: Invalid show status command format";
    }

    try {
      LocalDateTime dateTime = DateTimeUtil.parseDateTime(args[2]);
      boolean isBusy = calendar.isBusy(dateTime);

      return isBusy ? "busy" : "available";
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "show";
  }
}