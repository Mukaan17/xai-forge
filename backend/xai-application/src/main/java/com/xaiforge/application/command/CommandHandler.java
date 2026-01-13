package com.xaiforge.application.command;

/**
 * Interface for command handlers in the CQRS pattern.
 * 
 * @param <C> The command type
 * @param <R> The result type
 */
public interface CommandHandler<C extends Command, R> {
    R handle(C command);
}

