# Calendar Application

This calendar application allows you to create, edit, and manage events in a virtual calendar. It supports both single events and recurring events, with features for tracking conflicts, querying the calendar, and exporting to CSV.

## Getting Started

### Prerequisites

- Java 11 or higher
- PIT and Mutation Testing tools (for development)
- Maven (for building)

### Running the Application

The application can run in two modes: interactive and headless.

#### Interactive Mode

In interactive mode, you can type commands and see results immediately:

```bash
java -cp target/classes CalendarApp --mode interactive
```

#### Headless Mode

In headless mode, the application reads commands from a file:

```bash
java -cp target/classes CalendarApp --mode headless commands.txt
```

Where `commands.txt` is a file containing commands, one per line, with an `exit` command at the end.

## Commands Reference

### Create Commands

#### Create a Single Event

```
create event [--autoDecline] <eventName> from <dateTimeString> to <dateTimeString>
```

Example:
```
create event Meeting from 2023-05-15T10:00 to 2023-05-15T11:00
```

With auto-decline for conflicts:
```
create event --autoDecline Team Meeting from 2023-05-15T10:00 to 2023-05-15T11:00
```

#### Create a Recurring Event with Specified Occurrences

```
create event [--autoDecline] <eventName> from <dateTimeString> to <dateTimeString> repeats <weekdays> for <N> times
```

Example:
```
create event Weekly Standup from 2023-06-01T09:00 to 2023-06-01T09:30 repeats MWF for 10 times
```

Weekday codes:
- `M`: Monday
- `T`: Tuesday
- `W`: Wednesday
- `R`: Thursday
- `F`: Friday
- `S`: Saturday
- `U`: Sunday

#### Create a Recurring Event Until a Specific Date

```
create event [--autoDecline] <eventName> from <dateTimeString> to <dateTimeString> repeats <weekdays> until <dateString>
```

Example:
```
create event Project Review from 2023-06-01T14:00 to 2023-06-01T15:00 repeats T until 2023-08-31
```

#### Create an All-Day Event

```
create event [--autoDecline] <eventName> on <dateString>
```

Example:
```
create event Company Holiday on 2023-07-04
```

#### Create a Recurring All-Day Event

```
create event [--autoDecline] <eventName> on <dateString> repeats <weekdays> for <N> times
```

Example:
```
create event Training Session on 2023-07-10 repeats MWF for 5 times
```

### Edit Commands

#### Edit a Single Event

```
edit event <property> <eventName> from <dateTimeString> to <dateTimeString> with <newValue>
```

Example:
```
edit event name Meeting from 2023-05-15T10:00 to 2023-05-15T11:00 with Updated Meeting
```

#### Edit Events Starting from a Specific Date

```
edit events <property> <eventName> from <dateTimeString> with <newValue>
```

Example:
```
edit events location Weekly Standup from 2023-06-01T09:00 with Conference Room B
```

#### Edit All Events with a Specific Name

```
edit events <property> <eventName> <newValue>
```

Example:
```
edit events description Team Lunch Team lunch at the cafeteria - please bring your own drinks
```

Editable properties:
- `name` or `subject`: The event's title
- `description`: The event's description
- `location`: The event's location
- `startdatetime`: The event's start date and time
- `enddatetime`: The event's end date and time
- `ispublic`: Whether the event is public (true/false)

### Query Commands

#### Print Events on a Specific Date

```
print events on <dateString>
```

Example:
```
print events on 2023-05-15
```

#### Print Events in a Date Range

```
print events from <dateString> to <dateString>
```

Example:
```
print events from 2023-05-15 to 2023-05-21
```

#### Check Busy Status

```
show status on <dateTimeString>
```

Example:
```
show status on 2023-05-15T10:30
```

### Export Command

#### Export Calendar to CSV

```
export cal <fileName>
```

Example:
```
export cal calendar_export.csv
```

### Exit Command

```
exit
```

## Date and Time Format

- Date format: `YYYY-MM-DD` (e.g., `2023-05-15`)
- Time format: `HH:MM` in 24-hour format (e.g., `14:30` for 2:30 PM)
- DateTime format: `YYYY-MM-DDThh:mm` (e.g., `2023-05-15T14:30`)

## Example Command Sequence

```
create event Team Meeting from 2023-05-15T10:00 to 2023-05-15T11:00
create event Weekly Standup from 2023-06-01T09:00 to 2023-06-01T09:30 repeats MWF for 10 times
edit events name Weekly Standup from 2023-06-01T09:00 with Daily Checkin
print events from 2023-06-01 to 2023-06-07
show status on 2023-06-05T09:15
export cal my_calendar.csv
exit
```

## Error Handling

If an invalid command is entered, the application will display an error message. In headless mode, the application will terminate immediately if an invalid command is encountered.

## Calendar Features

- **Conflicts**: Two events conflict if their time intervals overlap
- **Automatic Decline**: With `--autoDecline` flag, event creation is automatically declined if there's a conflict
- **Recurring Events**: Support for events that repeat on specific days of the week
- **All-Day Events**: Events that don't have a specific start/end time
- **CSV Export**: Export all events to a CSV file compatible with Google Calendar import