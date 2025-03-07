package controller.command;

import java.io.IOException;

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

    try {
      // Change based on your ICalendar interface implementation
      String absolutePath = calendar.exportToCSV(filePath);
      return "Calendar exported successfully to: " + absolutePath;
    } catch (IOException e) {
      return "Failed to export calendar: " + e.getMessage();
    }
  }

  @Override
  public String getName() {
    return "export";
  }
}