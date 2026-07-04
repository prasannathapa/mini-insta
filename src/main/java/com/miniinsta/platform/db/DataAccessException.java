package com.miniinsta.platform.db;

/**
 * Unchecked wrapper for {@link java.sql.SQLException}. Keeps checked SQL
 * exceptions from leaking into the service layer, which should not know it is
 * talking to a database at all.
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
