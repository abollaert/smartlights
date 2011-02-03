package be.abollaert.domotics.light.server.kernel.persistence.sqlite;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.SwitchEvent;
import be.abollaert.domotics.light.server.kernel.persistence.Storage;
import be.abollaert.domotics.light.server.kernel.persistence.StorageException;
import be.abollaert.domotics.light.server.kernel.persistence.StoredChannelConfiguration;
import be.abollaert.domotics.light.server.kernel.persistence.StoredDimMoodElement;
import be.abollaert.domotics.light.server.kernel.persistence.StoredMoodInfo;
import be.abollaert.domotics.light.server.kernel.persistence.StoredSwitchMoodElement;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

/**
 * Storage that uses SQLite as the backend.
 * 
 * @author alex
 */
abstract class AbstractSQLiteStorage implements Storage {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(AbstractSQLiteStorage.class.getName());
	
	/** The executor used to execute queries. */
	private ExecutorService databaseExecutor;
	
	/** SQL statement used to create the channel configuration table. */
	private static final String SQL_CREATE_CHANNEL_CONFIG_TABLE = "create table if not exists channel_config (" + 
																  "module_id integer not null, " +
																  "channel_number integer not null, " +
																  "name text not null," +
																  "logging_enabled integer not null, " +
																  "constraint pk_channel_config primary key (module_id, channel_number)" +
																  ")";
	
	/** Create the table that contains the logs for the switches. */
	private static final String SQL_CREATE_ON_OFF_LOG_TABLE = "create table if not exists log_onoff (" +
															  "timestamp integer not null," +
															  "module_id integer not null, " +
															  "channel_number integer not null," +
															  "state integer not null)";
	/** Create an index on the timestamp. */
	private static final String SQL_CREATE_ONOFF_INDEX = "create index if not exists idx_onoff_log_ts on log_onoff(timestamp)";
	
	/** Create the table that contains the logs for the switches. */
	private static final String SQL_CREATE_DIM_LOG_TABLE = "create table if not exists log_dimmer (" +
															  "timestamp integer not null," +
															  "module_id integer not null, " +
															  "channel_number integer not null," +
															  "percentage integer not null)";
	
	/** Create an index on the timestamp. */
	private static final String SQL_CREATE_DIMMER_INDEX = "create index if not exists idx_dimmer_log_ts on log_dimmer(timestamp)";
	
	/** Insert the channel configuration. */
	private static final String SQL_INSERT_CHANNEL_CONFIG = "insert into channel_config (module_id, channel_number, name, logging_enabled) " +
															"values (?1, ?2, ?3, ?4)";
	
	/** Update the channel configuration. */
	private static final String SQL_UPDATE_CHANNEL_CONFIG = "update channel_config set " +
															"name = ?3, " +
															"logging_enabled = ?4 " +
															"where module_id = ?1 and channel_number = ?2";
	
	/** Selects a channel configuration. */
	private static final String SQL_SELECT_CHANNEL_CONFIG = "select name, logging_enabled from channel_config where module_id = ?1 and channel_number = ?2";
	
	/** SQL that needs to be executed if the table exists. */
	private static final String SQL_CHECK_TABLE_EXISTS = "select name from sqlite_master where type = 'table' and name = ?1";
	
	/** Add an on/off log entry. */
	private static final String SQL_ADD_ONOFF_LOG = "insert into log_onoff(timestamp, module_id, channel_number, state) values (?1, ?2, ?3, ?4)";
	
	/** Returns the switch events that occurred for the given channel in the given period. */
	private static final String SQL_GET_SWITCH_EVENTS = "select timestamp, state from log_onoff where module_id = ?1 and channel_number = ?2 and timestamp >= ?3 and timestamp <= ?4 order by timestamp asc";
	
	private static final String SQL_CREATE_MOOD_TABLE = "create table if not exists mood (" + 
														"id integer not null primary key autoincrement, " +
														"name text not null)";
	
	private static final String SQL_INSERT_MOOD = "insert into mood (name) values (?1)";
	
	private static final String SQL_UPDATE_MOOD = "update mood set name = ?2 where id = ?1";
	
	private static final String SQL_SELECT_ALL_MOODS = "select id, name from mood";
	
	private static final String SQL_DELETE_MOOD = "delete from mood where id = ?1";
	
	private static final String SQL_SELECT_MOOD = "select name from mood where id = ?1";
	
	private static final String SQL_CREATE_SWITCH_ELEMENT_TABLE = "create table if not exists mood_switch_element ( " +
																  "mood_id integer not null, " +
																  "module_id integer not null, " +
																  "channel_number integer not null, " +
																  "requested_state integer not null," +
																  "constraint pk_mood_switch_element primary key (mood_id, module_id, channel_number)," +
																  "constraint fk_mood_switch_element_mood foreign key (mood_id) references mood(id))";
	
	private static final String SQL_ADD_SWITCH_ELEMENT = "insert into mood_switch_element(mood_id, module_id, channel_number, requested_state) values (?1, ?2, ?3, ?4)";
	
	private static final String SQL_UPDATE_SWITCH_ELEMENT = "update mood_switch_element set requested_state = ?4 where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_DELETE_SWITCH_ELEMENT = "delete from mood_switch_element where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_GET_SWITCH_ELEMENT = "select requested_state from mood_switch_element where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_DELETE_SWITCH_ELEMENTS_FOR_MOOD = "delete from mood_switch_element where mood_id = ?1";
	
	private static final String SQL_SELECT_SWITCH_ELEMENTS_FOR_MOOD = "select module_id, channel_number, requested_state from mood_switch_element where mood_id = ?1";
	
	private static final String SQL_CREATE_DIM_ELEMENT_TABLE = "create table if not exists mood_dim_element (" + 
															   "mood_id integer not null, " +
															   "module_id integer not null," +
															   "channel_number integer not null," +
															   "percentage integer not null," +
															   "constraint pk_mood_dim_element primary key (mood_id, module_id, channel_number), " +
															   "constraint fk_mood_dim_element_mood foreign key (mood_id) references mood(id))";

	private static final String SQL_ADD_DIM_ELEMENT = "insert into mood_dim_element(mood_id, module_id, channel_number, percentage) values (?1, ?2, ?3, ?4)";
	
	private static final String SQL_UPDATE_DIM_ELEMENT = "update mood_dim_element set percentage = ?4 where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_DELETE_DIM_ELEMENT = "delete from mood_dim_element where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_GET_DIM_ELEMENT = "select percentage from mood_dim_element where mood_id = ?1 and module_id = ?2 and channel_number = ?3";
	
	private static final String SQL_DELETE_DIM_ELEMENTS_FOR_MOOD = "delete from mood_dim_element where mood_id = ?1";
	
	private static final String SQL_SELECT_DIM_ELEMENTS_FOR_MOOD = "select module_id, channel_number, percentage from mood_dim_element where mood_id = ?1";
	
	/** Gets the last key on insert in the database. */
	private static final String SQL_GET_LAST_INSERT_KEY = "select last_insert_rowid()";
	
	/** Begins a transaction. */
	private static final String SQL_BEGIN_TRANSACTION = "begin transaction";

	private static final String SQL_COMMIT_TRANSACTION = "commit transaction";
	
	private static final String SQL_ROLLBACK_TRANSACTION = "rollback transaction";
	
	/** SQL that enables foreign keys. */
	private static final String SQL_ENABLE_FOREIGN_KEYS = "PRAGMA foreign_keys = ON";
	
	/** The database connection. */
	private SQLiteConnection connection;

	/** This is a lock that serializes transactional statements. */
	private final Lock transactionLock = new ReentrantLock();
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void saveChannelConfiguration(final int moduleId, final int channelNumber, final StoredChannelConfiguration configuration) {
		String sql = null;
		
		if (this.loadChannelConfiguration(moduleId, channelNumber) != null) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Channel configuration for channel [" + moduleId + ", " + channelNumber + "] exists, using update.");
			}
			
			sql = SQL_UPDATE_CHANNEL_CONFIG;
		} else {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Channel configuration for channel [" + moduleId + ", " + channelNumber + "] does not exist, using insert.");
			}
			
			sql = SQL_INSERT_CHANNEL_CONFIG;
		}
		
		this.executeQuery(sql, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moduleId);
				statement.bind(2, channelNumber);
				statement.bind(3, configuration.getName());
				statement.bind(4, configuration.isLoggingEnabled() ? 1 : 0);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final StoredChannelConfiguration loadChannelConfiguration(final int moduleId, final int channelNumber) throws StorageException {
		final StoredChannelConfiguration[] channelConfig = new StoredChannelConfiguration[1];
		
		this.executeQuery(SQL_SELECT_CHANNEL_CONFIG, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moduleId);
				statement.bind(2, channelNumber);
			}
		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				final String channelName = statement.columnString(0);
				final boolean loggingEnabled = statement.columnInt(1) == 0 ? false : true;
				
				channelConfig[0] = new StoredChannelConfiguration(channelName, loggingEnabled);
			}
		});
		
		return channelConfig[0];
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note that we schedule the run
	 */
	@Override
	public final void start() throws StorageException {
		this.databaseExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			public final Thread newThread(final Runnable r) {
				return new Thread(r, "SQLite storage");
			}
		});
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Connecting to the database.");
		}
		
		Future<Boolean> future = this.databaseExecutor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				connection = new SQLiteConnection(getDatabaseFile());
				
				try {
					connection.open();
					
					return true;
				} catch (SQLiteException e) {
					throw new SQLiteStorageException("Could not open connection to the database...", e);
				}
			}
		});
		
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new SQLiteStorageException("Could not connect to the database because of an interruption [" + e.getMessage() + "]", e);
		} catch (ExecutionException e) {
			throw new SQLiteStorageException("Could not connect to the database because of an execution error [" + e.getMessage() + "]", e);
		}
		
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Checking schema.");
		}
		
		this.checkSchema();
		this.executeQuery(SQL_ENABLE_FOREIGN_KEYS);
	}
	
	/**
	 * Checks the schema in the database for tables that need to be created or updated.
	 * 
	 * @throws 	StorageException		If a storage error occurs.
	 */
	private final void checkSchema() throws StorageException {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Checking schema for tables that need to be created transparently.");
		}
		
		if (tableExists("channel_config")) {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Found channel config table.");
			}
		} else {
			if (logger.isLoggable(Level.INFO)) {
				logger.log(Level.INFO, "Did not find channel config table, creating.");
			}
			
			this.executeQuery(SQL_CREATE_CHANNEL_CONFIG_TABLE);
		}
		
		this.executeQuery(SQL_CREATE_DIM_LOG_TABLE);
		this.executeQuery(SQL_CREATE_DIMMER_INDEX);
		
		this.executeQuery(SQL_CREATE_ON_OFF_LOG_TABLE);
		this.executeQuery(SQL_CREATE_ONOFF_INDEX);
		
		// Moods.
		this.executeQuery(SQL_CREATE_MOOD_TABLE);
		this.executeQuery(SQL_CREATE_DIM_ELEMENT_TABLE);
		this.executeQuery(SQL_CREATE_SWITCH_ELEMENT_TABLE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void stop() throws StorageException {
		Future<Boolean> future = this.databaseExecutor.submit(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				if (connection != null) {
					connection.dispose();
				}
				
				return true;
			}
		});
		
		try {
			future.get();
		} catch (InterruptedException e) {
			throw new SQLiteStorageException("Could not disconnect from the database because of an interruption [" + e.getMessage() + "]", e);
		} catch (ExecutionException e) {
			throw new SQLiteStorageException("Could not disconnect from the database because of an execution error [" + e.getMessage() + "]", e);
		}
		
		this.databaseExecutor.shutdown();
		
		try {
			this.databaseExecutor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new SQLiteStorageException("Could not shutdown database executor, was interrupted while waiting for it's termination : [" + e.getMessage() + "]", e);
		}
	}
	
	/**
	 * Execute a query.
	 * 
	 * @param 	query					The query.
	 * @param 	parameterBinder			The binder.
	 * @param 	resultProcessor			The result processor.
	 * 
	 * @throws 	SQLiteException			If a SQL error occurs.
	 */
	private final void executeQuery(final String query, final ParameterBinder parameterBinder, final ResultProcessor resultProcessor) throws StorageException {
		final Callable<Boolean> databaseTask = new Callable<Boolean>() {
			@Override
			public final Boolean call() throws Exception {
				final SQLiteStatement statement = connection.prepare(query);
				
				if (parameterBinder != null) {
					parameterBinder.bindParameters(statement);
				}
				
				if (resultProcessor != null) {
					while (statement.step()) {
						resultProcessor.processRow(statement);
					}
				} else {
					statement.stepThrough();
				}
				
				return true;
			}
		};
		
		final Future<Boolean> result = this.databaseExecutor.submit(databaseTask);
		
		try {
			result.get();
		} catch (InterruptedException e) {
			throw new SQLiteStorageException("Interrupted while waiting for query to finish.", e);
		} catch (ExecutionException e) {
			throw new SQLiteStorageException("Execution error while waiting for query to finish.", e);
		}
	}
	
	/**
	 * Check if the given table exists in the schema. Returns <code>true</code> if it does, <code>false</code> if it does not.
	 * 
	 * @param 		tableName		The name of the requested table.
	 * 
	 * @return		<code>true</code> if the table exists in the schema, <code>false</code> if it does not.
	 * 
	 * @throws 		StorageException	If an error occurs during the execution of the query.
	 */
	private final boolean tableExists(final String tableName) throws StorageException {
		final Boolean[] exists = new Boolean[1];
		exists[0] = Boolean.FALSE;
		
		this.executeQuery(SQL_CHECK_TABLE_EXISTS, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, tableName);
			}
		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				exists[0] = Boolean.TRUE;
			}
		});
		
		return exists[0].booleanValue();
	}
	
	/**
	 * Execute a query.
	 * 
	 * @param query
	 * @param binder
	 * @throws SQLiteException
	 */
	private final void executeQuery(final String query, final ParameterBinder binder) throws StorageException {
		this.executeQuery(query, binder, null);
	}
	
	/**
	 * Execute a query.
	 * 
	 * @param query
	 * @param resultProcessor
	 * @throws SQLiteException
	 */
	private final void executeQuery(final String query, final ResultProcessor resultProcessor) throws StorageException {
		this.executeQuery(query, null, resultProcessor);
	}
	
	/**
	 * Execute a query.
	 * 
	 * @param 	query					The query.
	 * 
	 * @throws 	SQLiteException
	 */
	private final void executeQuery(final String query) throws StorageException {
		this.executeQuery(query, null, null);
	}
	
	/**
	 * Callback used to let the caller process the results of the query.
	 * 
	 * @author alex
	 */
	private interface ResultProcessor {
		void processRow(final SQLiteStatement statement) throws SQLiteException;
	}
	
	/**
	 * Callback used to let the caller bind the parameters.
	 * 
	 * @author alex
	 */
	private interface ParameterBinder {
		void bindParameters(final SQLiteStatement statement) throws SQLiteException;
	}
	
	/**
	 * Returns the database file.
	 * 
	 * @return	The database file.
	 */
	abstract File getDatabaseFile();
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void logDimEvent(int moduleId, int channelNumber, int percentage) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void logOnOffEvent(final int moduleId, final int channelNumber, final boolean on) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Storage : Adding on/off event for channel [" + channelNumber + "] on module [" + moduleId + "]");
		}
		
		this.executeQuery(SQL_ADD_ONOFF_LOG, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, new Date().getTime());
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
				statement.bind(4, on ? 1 : 0);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<SwitchEvent> getSwitchEventsForPeriod(final int moduleId, final int channelNumber, final Date startDate, final Date endDate) {
		if (logger.isLoggable(Level.INFO)) {
			logger.log(Level.INFO, "Storage : Search for switch events for channel [" + channelNumber + "] on module [" + moduleId + "], period [" + startDate + "] to [" + endDate + "]");
		}
		
		final List<SwitchEvent> events = new ArrayList<SwitchEvent>();
		
		this.executeQuery(SQL_GET_SWITCH_EVENTS, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moduleId);
				statement.bind(2, channelNumber);
				
				if (startDate == null) {
					statement.bind(3, 0);
				} else {
					statement.bind(3, startDate.getTime());
				}
				
				if (endDate == null) {
					statement.bind(4, new Date().getTime());
				} else {
					statement.bind(4, endDate.getTime());
				}
			}
		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				final Date timestamp = new Date(statement.columnLong(0));
				final boolean on = (statement.columnInt(1) == 1 ? true : false);
				
				events.add(new SwitchEvent(moduleId, channelNumber, timestamp, on));
			}
		});
		
		return events;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void saveMoodDimElement(final int moodId, final int moduleId, final int channelNumber, final int percentage) {
		final boolean[] exists = new boolean[1];
		
		this.executeQuery(SQL_GET_DIM_ELEMENT, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
			}

		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				exists[0] = true;
			}
		});
		
		String query = SQL_ADD_DIM_ELEMENT;

		if (exists[0]) {
			query = SQL_UPDATE_DIM_ELEMENT;
		}

		this.executeQuery(query, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
				statement.bind(4, percentage);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void saveMoodSwitchElement(final int moodId, final int moduleId, final int channelNumber, final ChannelState state) {
		final boolean[] exists = new boolean[1];

		this.executeQuery(SQL_GET_SWITCH_ELEMENT, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
			}

		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				exists[0] = true;
			}
		});
		
		String query = SQL_ADD_SWITCH_ELEMENT;

		if (exists[0]) {
			query = SQL_UPDATE_SWITCH_ELEMENT;
		}

		this.executeQuery(query, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement)
					throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
				statement.bind(4, state == ChannelState.ON ? 1 : 0);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<StoredMoodInfo> getStoredMoods() {
		final List<StoredMoodInfo> moodInfos = new ArrayList<StoredMoodInfo>();
		
		this.executeQuery(SQL_SELECT_ALL_MOODS, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				final int id = statement.columnInt(0);
				final String name = statement.columnString(1);
				
				moodInfos.add(new StoredMoodInfo(id, name));
			}
		});
		
		return moodInfos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<StoredDimMoodElement> getDimElementsForMood(final int moodId) {
		final List<StoredDimMoodElement> elements = new ArrayList<StoredDimMoodElement>();
		
		this.executeQuery(SQL_SELECT_DIM_ELEMENTS_FOR_MOOD, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
			}

		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				elements.add(new StoredDimMoodElement(moodId, statement.columnInt(0), statement.columnInt(1), statement.columnInt(2)));
			}
		});
		
		return elements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final List<StoredSwitchMoodElement> getSwitchElementsForMood(final int moodId) {
		final List<StoredSwitchMoodElement> elements = new ArrayList<StoredSwitchMoodElement>();
		
		this.executeQuery(SQL_SELECT_SWITCH_ELEMENTS_FOR_MOOD, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
			}

		}, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				elements.add(new StoredSwitchMoodElement(moodId, statement.columnInt(0), statement.columnInt(1), statement.columnInt(2) == 0 ? ChannelState.OFF : ChannelState.ON));
			}
		});
		
		return elements;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeMood(final int moodId) {
		try {
			this.startTransaction();

			this.executeQuery(SQL_DELETE_DIM_ELEMENTS_FOR_MOOD, new ParameterBinder() {
				@Override
				public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
					statement.bind(1, moodId);
				}
			});
			
			this.executeQuery(SQL_DELETE_SWITCH_ELEMENTS_FOR_MOOD, new ParameterBinder() {
				@Override
				public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
					statement.bind(1, moodId);
				}
			});
			
			this.executeQuery(SQL_DELETE_MOOD, new ParameterBinder() {
				@Override
				public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
					statement.bind(1, moodId);
				}
			});
			
			this.commit();
		} catch (SQLiteStorageException e) {
			this.rollback();
			
			throw e;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeMoodDimElement(final int moodId, final int moduleId, final int channelNumber) {
		this.executeQuery(SQL_DELETE_DIM_ELEMENT, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void removeMoodSwitchElement(final int moodId, final int moduleId, final int channelNumber) {
		this.executeQuery(SQL_DELETE_SWITCH_ELEMENT, new ParameterBinder() {
			@Override
			public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
				statement.bind(1, moodId);
				statement.bind(2, moduleId);
				statement.bind(3, channelNumber);
			}
		});
	}
	
	private final int getLastInsertKey() {
		final int[] generatedKey = new int[1];
		
		this.executeQuery(SQL_GET_LAST_INSERT_KEY, new ResultProcessor() {
			@Override
			public final void processRow(final SQLiteStatement statement) throws SQLiteException {
				generatedKey[0] = statement.columnInt(0);
			}
		});
		
		return generatedKey[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final StoredMoodInfo saveMoodInformation(final int moodId, final String moodName) {
		String statement = null;
		
		final boolean[] update = new boolean[1];
		
		if (this.loadMoodInformationFor(moodId) != null) {
			statement = SQL_UPDATE_MOOD;
			update[0] = true;
		} else {
			statement = SQL_INSERT_MOOD;
		}
		
		this.executeQuery(statement,
			new ParameterBinder() {
				@Override
				public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
					if (update[0]) {
						statement.bind(1, moodId);
						statement.bind(2, moodName);
					} else {
						statement.bind(1, moodName);
					}
				}
			}
		);
		
		if (update[0]) {
			return new StoredMoodInfo(moodId, moodName);
		} else {
			final int lastKeyInserted = this.getLastInsertKey();
			
			return new StoredMoodInfo(lastKeyInserted, moodName);
		}
	}
	
	@Override
	public final StoredMoodInfo loadMoodInformationFor(final int moodId) {
		final StoredMoodInfo[] moodArray = new StoredMoodInfo[1];
		
		this.executeQuery(SQL_SELECT_MOOD, 
			new ParameterBinder() {
				@Override
				public final void bindParameters(final SQLiteStatement statement) throws SQLiteException {
					statement.bind(1, moodId);
				}
			
			}, new ResultProcessor() {
				@Override
				public final void processRow(final SQLiteStatement statement) throws SQLiteException {
					moodArray[0] = new StoredMoodInfo(moodId, statement.columnString(0));
				}
			}
		);
		
		return moodArray[0];
	}
	
	private final void startTransaction() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Starting transaction.");
		}
		
		this.transactionLock.lock();
		this.executeQuery(SQL_BEGIN_TRANSACTION);
	}
	
	private final void commit() {
		if (logger.isLoggable(Level.FINE)) {
			logger.log(Level.FINE, "Committing transaction.");
		}
		
		try {
			this.executeQuery(SQL_COMMIT_TRANSACTION);
		} finally {
			this.transactionLock.unlock();
		}
	}
	
	private final void rollback() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Rolling back transaction.");
		}
		
		try {
			this.executeQuery(SQL_ROLLBACK_TRANSACTION);
		} finally {
			this.transactionLock.unlock();
		}
	}
}
