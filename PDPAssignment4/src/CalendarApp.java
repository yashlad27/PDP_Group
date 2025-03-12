import controller.CalendarController;
import controller.command.CommandFactory;
import model.calendar.Calendar;
import model.calendar.ICalendar;
import view.ConsoleView;
import view.ICalendarView;

/**
 * Main class for the Calendar Application.
 */
public class CalendarApp {

  /**
   * Main method to run the application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    // Create model, view, and controller
    ICalendar calendar = new Calendar();
    ICalendarView view = new ConsoleView();

    // Create the command factory with the calendar
    CommandFactory commandFactory = new CommandFactory(calendar, view);

    // Create the controller with the correct types
    CalendarController controller = new CalendarController(commandFactory, view);

    // Check arguments
    if (args.length < 2) {
      view.displayError(
          "Insufficient arguments. Usage: --mode [interactive|headless filename.txt]");
      return;
    }

    String modeArg = args[0].toLowerCase();
    String modeValue = args[1].toLowerCase();

    if (!modeArg.equals("--mode")) {
      view.displayError("Invalid argument. Expected: --mode");
      return;
    }

    // Handle different modes
    if (modeValue.equals("interactive")) {
      controller.startInteractiveMode();
    } else if (modeValue.equals("headless")) {
      if (args.length < 3) {
        view.displayError("Headless mode requires a filename. Usage: --mode headless filename.txt");
        return;
      }

      String filename = args[2];
      boolean success = controller.startHeadlessMode(filename);

      if (!success) {
        view.displayError("Headless mode execution failed.");
        System.exit(1);
      }
    } else {
      view.displayError("Invalid mode. Expected: interactive or headless");
    }

    // Close the view resources
    if (view instanceof ConsoleView) {
      ((ConsoleView) view).close();
    }
  }
}