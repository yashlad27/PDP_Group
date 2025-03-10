import org.junit.Before;
import org.junit.Test;

import controller.command.ExitCommand;

import static org.junit.Assert.assertEquals;

public class ExitCommandTest {

  private ExitCommand command;

  @Before
  public void setUp() {
    command = new ExitCommand();
  }

  @Test
  public void testGetName() {
    assertEquals("exit", command.getName());
  }

  @Test
  public void testExecuteWithNoArguments() {
    // Setup
    String[] args = {};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testExecuteWithArguments() {
    // Setup - command should ignore any arguments
    String[] args = {"ignored", "arguments"};

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testExecuteWithNullArguments() {
    // Setup - command should handle null arguments gracefully
    String[] args = null;

    // Execute
    String result = command.execute(args);

    // Verify
    assertEquals("Exiting application.", result);
  }

  @Test
  public void testMultipleExecutions() {
    // Setup
    String[] args = {};

    // Execute multiple times to ensure consistent behavior
    String result1 = command.execute(args);
    String result2 = command.execute(args);
    String result3 = command.execute(args);

    // Verify
    assertEquals("Exiting application.", result1);
    assertEquals("Exiting application.", result2);
    assertEquals("Exiting application.", result3);
  }
}