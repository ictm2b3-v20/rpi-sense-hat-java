package rpi.sensehat.connector.mock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpi.sensehat.api.dto.CommandResult;
import rpi.sensehat.connector.Command;
import rpi.sensehat.connector.CommandExecutor;

/**
 * Created by jcincera on 04/07/2017.
 */
public class MockCommandExecutor implements CommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(MockCommandExecutor.class);

    @Override
    public CommandResult execute(Command command, String... args) {
        log.debug("Mocking command: " + command.getCommand());

        MockCommandResult result = new MockCommandResult("");
        result.setCommand(command);

        return result;
    }

}
