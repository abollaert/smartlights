package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import org.junit.After;
import org.junit.Before;

import be.abollaert.domotics.light.server.kernel.persistence.Storage;

/**
 * Base class for the storage tests.
 * 
 * @author alex
 */
public abstract class AbstractStorageTest {
	
	/** The storage. */
	private Storage storage;
	
	/**
	 * Runs before a test.
	 */
	@Before
	public final void setupDatabase() {
		this.storage = new TemporarySQLiteStorageImpl();
		this.storage.start();
	}
	
	/**
	 * Runs after a test.
	 */
	@After
	public final void cleanup() {
		this.storage.stop();
	}
	
	/**
	 * Returns the storage.
	 * 
	 * @return	The storage.
	 */
	final Storage getStorage() {
		return this.storage;
	}
}
