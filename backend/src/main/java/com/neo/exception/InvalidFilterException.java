package com.neo.exception;

/**
 * Thrown when the combination of filter parameters supplied to a query is
 * logically inconsistent (e.g. dateFrom after dateTo), as opposed to a
 * single field being malformed (which Spring's own binding/validation
 * already rejects with a 400 before this is ever reached).
 */
public class InvalidFilterException extends RuntimeException {
    public InvalidFilterException(String message) {
        super(message);
    }
}