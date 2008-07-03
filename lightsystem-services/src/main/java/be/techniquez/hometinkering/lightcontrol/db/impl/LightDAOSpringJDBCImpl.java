package be.techniquez.hometinkering.lightcontrol.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
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

}
