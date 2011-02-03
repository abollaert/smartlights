package be.abollaert.domotics.light.server.kernel.persistence;

/**
 * Stored mood information, immutable.
 * 
 * @author alex
 */
public final class StoredMoodInfo {

	/** The name of the mood. */
	private final String name;
	
	/** The ID of the mood. */
	private final int id;
	
	/**
	 * Stored mood information.
	 * 
	 * @param 	id			The ID of the mood.
	 * @param 	name		The name of the mood.
	 */
	public StoredMoodInfo(final int id, final String name) {
		this.id = id;
		this.name = name;
	}
	
	/**
	 * Returns the name of the mood.
	 * 
	 * @return	The name of the mood.
	 */
	public final String getName() {
		return this.name;
	}
	
	/**
	 * Returns the ID of the mood.
	 * 
	 * @return	The ID of the mood.
	 */
	public final int getId() {
		return this.id;
	}
}
