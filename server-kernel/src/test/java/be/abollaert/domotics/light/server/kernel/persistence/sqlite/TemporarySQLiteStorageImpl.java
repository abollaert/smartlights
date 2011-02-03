package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import java.io.File;
import java.io.IOException;

/**
 * Implementation of the storage used by test classes.
 * 
 * @author alex
 */
final class TemporarySQLiteStorageImpl extends AbstractSQLiteStorage {

	/**
	 * @return
	 */
	@Override
	final File getDatabaseFile() {
		try {
			return File.createTempFile("junit-tcp-server-persistence", ".sqlite3");
		} catch (IOException e) {
			throw new IllegalStateException("Could not create temporary file due to an IO error [" + e.getMessage() + "]", e);
		}
	}
}
