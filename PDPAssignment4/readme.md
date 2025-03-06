### Test Commands for Event Creation

```
create event "Team Meeting" from 2023-04-10T10:00 to 2023-04-10T11:00
create event --autoDecline "Important Interview" from 2023-04-10T10:30 to 2023-04-10T11:30
create event "All Day Conference" on 2023-04-15
create event "Weekly Status Meeting" from 2023-04-12T09:00 to 2023-04-12T10:00 repeats MW for 5 times
create event "Monthly Review" from 2023-04-20T14:00 to 2023-04-20T15:00 repeats F until 2023-06-30
```

### Test Commands for Querying Events

```
print events on 2023-04-10
print events on 2023-04-15
print events from 2023-04-10 to 2023-04-20
```

### Test Commands for Status Check

```
show status on 2023-04-10T10:15
show status on 2023-04-10T12:00
```

### Test Command for Export

```
export cal calendar_export.csv
```

### Test Commands with Expected Conflicts

```
create event --autoDecline "Conflicting Meeting" from 2023-04-10T10:15 to 2023-04-10T11:15
create event "Non-Declined Conflict" from 2023-04-10T10:45 to 2023-04-10T11:45
```

### Test Edge Cases

```
create event "Late Night Event" from 2023-04-25T23:00 to 2023-04-26T01:00
create event "Multi-Day Conference" from 2023-05-01T09:00 to 2023-05-03T17:00
```

To test the full flow, create a text file named `commands.txt` with these commands (one per line), ending with:

```
exit
```

```
java application.CalendarApp --mode headless commands.txt
```

