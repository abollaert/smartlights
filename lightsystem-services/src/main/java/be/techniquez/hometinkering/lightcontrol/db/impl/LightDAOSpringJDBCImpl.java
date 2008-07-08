package be.techniquez.hometinkering.lightcontrol.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import be.techniquez.hometinkering.lightcontrol.db.LightDAO;
import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;

/**
 * Spring JDBC template implementation of the light DAO interface.
 * 
 * @author alex
 *
 */
public final class LightDAOSpringJDBCImpl implements LightDAO {
	
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
	public final Map<String, DigitalLight> getConfiguredDigitalLights() {
		final Map<String, DigitalLight> digitalLights = new HashMap<String, DigitalLight>();
		
		List<DigitalLight> lights = this.jdbcTemplate.query("select l.* from lights l where (select b.type from boards b where b.id = l.board_id) = 'DIGITAL'" , new RowMapper() {

			/**
			 * {@inheritDoc}
			 */
			public final Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				final String lightId = rs.getString("id");
				final int index = rs.getInt("channel_number");
				final int boardId = rs.getInt("board_id");
				
				final DigitalLight light = new DigitalLight(lightId, index, boardId);
				
				return light;
			}
		});
		
		for (final DigitalLight light : lights) {
			digitalLights.put(light.getLightIdentifier(), light);
		}
		
		return digitalLights;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final Map<String, DimmerLight> getConfiguredDimmerLights() {
		final Map<String, DimmerLight> dimmerLights = new HashMap<String, DimmerLight>();
		List<DimmerLight> lights = this.jdbcTemplate.query("select l.* from lights l where (select b.type from boards b where b.id = l.board_id) = 'DIMMER'" , new RowMapper() {

			/**
			 * {@inheritDoc}
			 */
			public final Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				final String lightId = rs.getString("id");
				final int index = rs.getInt("channel_number");
				final int boardId = rs.getInt("board_id");
				
				final DimmerLight light = new DimmerLight(lightId, index, boardId);
				
				return light;
			}
		});
		
		for (final DimmerLight light : lights) {
			dimmerLights.put(light.getLightIdentifier(), light);
		}
		
		return dimmerLights;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void updateChannel(final int boardId, final int channelNumber, final String lightName, final String lightDescription) {
		if (this.lightExists(boardId, channelNumber)) {
			this.jdbcTemplate.update("update lights set id = ?, description = ? where board_id = ? and channel_number = ?", new Object[] { lightName, lightDescription, boardId, channelNumber });
		} else {
			this.jdbcTemplate.update("insert into lights (id, board_id, channel_number, description) values (?, ?, ?, ?)", new Object[] { lightName, boardId, channelNumber, lightDescription });
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
	private final boolean lightExists(final int boardId, final int channelNumber) {
		return (Boolean)this.jdbcTemplate.query("select count(id) from lights where board_id = ? and channel_number = ?", new Object[] { boardId, channelNumber }, new ResultSetExtractor() {

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
