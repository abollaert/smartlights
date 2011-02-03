package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import be.abollaert.domotics.light.server.kernel.persistence.StorageException;

/**
 * Subclass indicating it is thrown from the SQLite backend.
 * 
 * @author alex
 */
final class SQLiteStorageException extends StorageException {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance specifying the message and cause of the error.
	 * 
	 * @param 	message		The message.
	 * @param 	cause		The cause.
	 */
	SQLiteStorageException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
