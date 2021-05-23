package rpi.sensehat.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpi.sensehat.api.dto.CommandResult;
import rpi.sensehat.exception.CommandException;
import rpi.sensehat.exception.CommunicationException;
import rpi.sensehat.exception.InvalidSystemArchitectureException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by jcincera on 20/06/2017.
 */
public class SimpleCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(SimpleCommandExecutor.class);

    SimpleCommandExecutor() {
        if (!System.getProperty("os.arch").toLowerCase().contains("arm")) {
            throw new InvalidSystemArchitectureException("System architecture is not supported for this command executor");
        }
    }

    @Override
    public CommandResult execute(Command command, String... args) {
        try {

            // Create command
            final String completeCommand = createCompleteCommand(command, args);

            // Call
            int trace = Math.abs(ThreadLocalRandom.current().nextInt());
            log.trace("Executing command (" + trace  + "): " + command.name());
            ProcessBuilder pb = new ProcessBuilder("python", "-c", completeCommand);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            // Read output
            BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = output.readLine()) != null) {
                result.append(line);
                result.append(lineSeparator);
            }
            log.trace("Command execution result (" + trace + "): " +  result.toString().trim());

            // Handle result
            waitForCommand(p);
            checkCommandException(result);
            return new CommandResult(result.toString());
        }
        catch (Exception e) {
//            System.err.println(e);

            if (e instanceof CommandException) {
                throw (CommandException) e;
            }

            throw new CommunicationException("Communication with Sense Hat failed!", e);
        }
    }

    private void checkCommandException(StringBuilder result) {
        if (result.toString().contains("Traceback") || result.toString().contains("Error")) {
            throw new CommandException("Command execution failed!\n" + result.toString());
        }
    }

    private String createCompleteCommand(Command command, String[] args) {
        String rawCommand = (args != null && args.length > 0) ?
                String.format(command.getCommand(), (Object[]) args) :
                command.getCommand();

        return Command.IMPORT_SENSE_HAT.getCommand() + ";" +
                Command.SENSE_OBJECT.getCommand() + ";" +
                rawCommand;
    }

    private void waitForCommand(Process p) {
        try {
            p.waitFor();
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
//            System.err.println(e);
        }
    }
}
