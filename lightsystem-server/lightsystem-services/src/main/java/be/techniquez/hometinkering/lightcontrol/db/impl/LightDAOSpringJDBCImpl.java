package be.techniquez.hometinkering.lightcontrol.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;

import be.techniquez.hometinkering.lightcontrol.db.LightDAO;
import be.techniquez.hometinkering.lightcontrol.model.Board;
import be.techniquez.hometinkering.lightcontrol.model.Light;

/**
 * Spring JDBC template implementation of the light DAO interface.
 * 
 * @author alex
 *
 */
public final class LightDAOSpringJDBCImpl implements LightDAO {
	
	/** Logger. */
	private static final Logger logger = Logger.getLogger(LightDAOSpringJDBCImpl.class.getName());
	
	/** JDBC template that gets used for the database operations. */
	private final JdbcTemplate jdbcTemplate;
	
	/**
	 * Creates a new instance using the given datasource.
	 * 	
	 * @param 	datasource	The data source.
	 */
	public LightDAOSpringJDBCImpl(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final Light addDatabaseInformation(final Light light) {
		this.jdbcTemplate.query("select id, description from lights where board_id = ? and channel_number = ?", new Object[] { light.getBoard().getID(), light.getChannelNumber() },
				new RowCallbackHandler() {
					
					/**
					 * {@inheritDoc}
					 */
					public final void processRow(final ResultSet rs) throws SQLException {
						light.setName(rs.getString("id"));
						light.setDescription(rs.getString("description"));
					}
				}
		);
		
		return light;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final void updateLight(final Light light) {
		// Check if the board exists first and create if necessary...
		if (!this.boardExists(light.getBoard())) {
			logger.info("Board hosting the light does not have any info in the database yet, adding it automatically...");
			
			this.jdbcTemplate.update("insert into boards (id, type, number_of_channels) values (?, ?, ?)", new Object[] { light.getBoard().getID(), light.getBoard().getType().name(), light.getBoard().getNumberOfChannels() });
		}
		
		if (this.lightExists(light)) {
			logger.info("Light already known in the database, updating existing entry...");
			
			this.jdbcTemplate.update("update lights set id = ?, description = ? where board_id = ? and channel_number = ?", new Object[] { light.getName(), light.getDescription(), light.getBoard().getID(), light.getChannelNumber() });
		} else {
			logger.info("Light not known in the database yet, creating new entry...");
			
			this.jdbcTemplate.update("insert into lights (id, board_id, channel_number, description) values (?, ?, ?, ?)", new Object[] { light.getName(), light.getBoard().getID(), light.getChannelNumber(), light.getDescription() });
		}
	}
	
	
	/**
	 * Checks if the light is known in the database, retuens true if it does, false if not.
	 * 
	 * @param 	boardId				The ID of the board.
	 * @param 	channelNumber		The channel number.
	 * 
	 * @return	True if the light exists, false if not.
	 */
	@SuppressWarnings("unchecked")
	private final boolean lightExists(final Light light) {
		return (Boolean)this.jdbcTemplate.query("select count(id) from lights where board_id = ? and channel_number = ?", new Object[] { light.getBoard().getID(), light.getChannelNumber() }, new ResultSetExtractor() {

			/**
			 * {@inheritDoc}
			 */
			public final Object extractData(final ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return rs.getInt(1) == 0 ? false : true;
				}
				
				return false;
			}
		});
	}
	
	/**
	 * Checks if the board is known in the database, retuens true if it does, false if not.
	 * 
	 * @param 	boardId				The ID of the board..
	 * 
	 * @return	True if the board exists, false if not.
	 */
	@SuppressWarnings("unchecked")
	private final boolean boardExists(final Board board) {
		return (Boolean)this.jdbcTemplate.query("select count(id) from boards where id = ?", new Object[] { board.getID() }, new ResultSetExtractor() {

			/**
			 * {@inheritDoc}
			 */
			public final Object extractData(final ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					return rs.getInt(1) == 0 ? false : true;
				}
				
				return false;
			}
		});
	}
}
