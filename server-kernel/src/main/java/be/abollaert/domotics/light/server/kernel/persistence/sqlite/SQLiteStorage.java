package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import java.io.File;

/**
 * SQLite storage. This is used by the production code.
 * 
 * @author alex
 *
 */
public final class SQLiteStorage extends AbstractSQLiteStorage {

	/**
	 * {@inheritDoc}
	 */
	@Override
	final File getDatabaseFile() {
		return new File(System.getProperty("user.home") + "/domotics.sqlite3");
	}

}
