package com.xaiforge.application.query;

/**
 * Marker interface for all queries in the CQRS pattern.
 * Queries represent read operations that do not change system state.
 * 
 * @param <R> The result type
 */
public interface Query<R> {
}

