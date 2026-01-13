package com.xaiforge.application.command;

/**
 * Command to train a new ML model.
 */
public record TrainModelCommand(
    Long datasetId,
    String modelName,
    String algorithm,
    String targetColumn,
    int trainTestSplit,
    boolean crossValidation,
    Long userId
) implements Command {
}

