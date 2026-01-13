package com.xaiforge.application.query;

/**
 * Interface for query handlers in the CQRS pattern.
 * 
 * @param <Q> The query type
 * @param <R> The result type
 */
public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}

