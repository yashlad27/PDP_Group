package controller.command;

import model.calendar.ICalendar;
import utilities.DateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ShowStatusCommand implements ICommand {
  private final ICalendar calendar;

  /**
   * Creates a ShowStatusCommand with the given calendar.
   *
   * @param calendar the calendar to query
   */
  public ShowStatusCommand(ICalendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Missing date/time for status command";
    }

    LocalDateTime dateTime;
    try {
      dateTime = DateTimeUtil.parseDateTime(args[0]);
    } catch (Exception e) {
      return "Error parsing date/time: " + e.getMessage();
    }

    boolean isBusy = calendar.isBusy(dateTime);

    return "Status on " + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
            ": " + (isBusy ? "Busy" : "Available");
  }

  @Override
  public String getName() {
    return "show";
  }
}