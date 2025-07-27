package io.authid.core.shared.commands;

import org.springframework.shell.command.annotation.Command;

@Command(command = "test-cmd", alias = "tc")
public class TestCommand {
    @Command(command = "say-hello")
    public String hello() {
        return  "hello";
    }
}
