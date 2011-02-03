package be.abollaert.domotics.light.server.kernel.persistence;

/**
 * Thrown when an error occurs in the persistent storage.
 * 
 * @author alex
 */
public abstract class StorageException extends RuntimeException {

	/** Serial version UID. */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	message		The message.
	 * @param 	cause		The cause.
	 */
	protected StorageException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
