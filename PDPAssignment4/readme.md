# Calendar Application

## Overview
This is a virtual calendar application that mimics features found in widely used calendar apps such as Google Calendar or Apple's iCalendar app. The application supports creating, editing, and querying calendar events, as well as exporting the calendar to a CSV file.

## Features
- Create single calendar events with subject, start date/time, and optional details
- Create recurring calendar events on specific weekdays
- Edit calendar events (single instances or recurring series)
- Query calendar for events on specific dates or date ranges
- Check availability at a specific date and time
- Export calendar to CSV format compatible with Google Calendar
- Interactive and headless operation modes
- Automatic conflict detection for events

## Technical Requirements
- Java version 11
- Maven build system
- PIT and Mutation Testing for quality assurance
- Implementation follows SOLID design principles
- Uses MVC architecture pattern

## How to Run

### Interactive Mode
```
java -jar CalendarApp.jar --mode interactive
```
In this mode, you can enter commands directly and see immediate results.

OR you would need to change your directory to src/ and run 
```
java CalendarApp.java --mode interactive
```
### Headless Mode
```
java -jar calendar-app.jar --mode headless resources/commands.txt
```
In this mode, the program reads commands from a text file and executes them sequentially.

OR you would need to change your directory to src/ and run
```
java CalendarApp.java --mode headless resources/commands.txt 
```

## Command Reference

### Create Events
- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString>`
- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`
- `create event [--autoDecline] <eventName> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`
- `create event [--autoDecline] <eventName> on <dateStringTtimeString>`
- `create event <eventName> on <dateStringTtimeString> repeats <weekdays> for <N> times`
- `create event <eventName> on <dateStringTtimeString> repeats <weekdays> until <dateStringTtimeString>`

### Edit Events
- `edit event <property> <eventName> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`
- `edit events <property> <eventName> from <dateStringTtimeString> with <NewPropertyValue>`
- `edit events <property> <eventName> <NewPropertyValue>`

### Query Events
- `print events on <dateStringTtimeString>`
- `print events from <dateStringTtimeString> to <dateStringTtimeString>`
- `show status on <dateStringTtimeString>`

### Export
- `export cal fileName.csv`

### Exit
- `exit`

## Implementation Details

### Architecture
The application follows the Model-View-Controller (MVC) architecture:
- **Model**: Represents the core calendar data structures and business logic
- **View**: Handles user interface and display formatting
- **Controller**: Processes user commands and updates the model accordingly

### Design Principles
- **Single Responsibility Principle**: Each class has a single purpose and reason to change
- **Open/Closed Principle**: Code is open for extension but closed for modification
- **Liskov Substitution Principle**: Subtypes are substitutable for their base types
- **Interface Segregation Principle**: Clients aren't forced to depend on interfaces they don't use
- **Dependency Inversion Principle**: High-level modules don't depend on low-level modules; both depend on abstractions

## Testing
- All code is thoroughly tested with JUnit tests
- PIT mutation testing is used to evaluate test quality
- Targeted 86% test coverage for both traditional coverage and mutation coverage

## Working Features
- All features as specified in the requirements document are fully implemented
- The application handles conflicts between events as specified
- Export functionality produces CSV files compatible with Google Calendar

## Limitations and Future Enhancements
- **Multiple Calendars**: Currently supports only a single calendar; future versions could support multiple calendars
- **Notifications**: No support for event reminders or notifications
- **Invitations**: No support for inviting other users to events
- **Synchronization**: Does not sync with external calendar services
- **GUI**: Currently only offers a text-based interface
- **Attachments**: No support for adding file attachments to events
- **Categories/Tags**: No support for categorizing or tagging events
- **Search**: Limited search functionality; cannot search by keywords within event descriptions
- **Timezone Management**: Currently assumes all times are in EST; could add support for multiple timezones
- **Mobile Access**: No mobile application or responsive web interface
- **Recurrence Exceptions**: Cannot create exceptions to recurring event patterns

## Team Contributions
- **Yash Lad**: Core event management and CSV export functionality
- **Gaurav Bidani**: Recurring events, conflict detection, and query functionality

## Additional Notes
- The application assumes all times are in EST timezone
- Currently supports a single calendar only
- Date strings should be in the format "YYYY-MM-DD"
- Time strings should be in the format "hh:mm"
- Weekday format: 'M' is Monday, 'T' is Tuesday, 'W' is Wednesday, 'R' is Thursday, 'F' is Friday, 'S' is Saturday, and 'U' is Sunday