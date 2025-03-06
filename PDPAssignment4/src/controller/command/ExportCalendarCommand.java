package controller.command;

import model.calendar.ICalendar;

/**
 * Command for exporting the calendar to a CSV file.
 */
public class ExportCalendarCommand implements ICommand {

  private final ICalendar calendar;

  /**
   * Constructs a new ExportCalendarCommand.
   *
   * @param calendar the calendar model
   */
  public ExportCalendarCommand(ICalendar calendar) {
    this.calendar = calendar;
  }

  @Override
  public String execute(String[] args) {
    if (args.length < 1) {
      return "Error: Missing filename for export command";
    }

    String filePath = args[0];
    boolean success = calendar.exportToCSV(filePath);

    if (success) {
      return "Calendar exported successfully to: " + filePath;
    } else {
      return "Failed to export calendar to: " + filePath;
    }
  }

  @Override
  public String getName() {
    return "export";
  }
}