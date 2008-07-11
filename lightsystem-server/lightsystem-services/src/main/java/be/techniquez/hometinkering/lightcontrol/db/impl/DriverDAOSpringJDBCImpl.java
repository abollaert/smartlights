package be.techniquez.hometinkering.lightcontrol.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import be.techniquez.hometinkering.lightcontrol.db.DriverDAO;

/**
 * Implementation of a driver DAO using spring jdbc template.
 * 
 * @author alex
 */
public final class DriverDAOSpringJDBCImpl implements DriverDAO {
	
	/** The spring JDBC template used for data access. */
	private final JdbcTemplate jdbcTemplate;
	
	/**
	 * Makes a new instance using the given template.
	 * 	
	 * @param 	jdbcTemplate	The template to use.
	 */
	public DriverDAOSpringJDBCImpl(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addInstalledDriver(final String className) {
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final Set<String> getInstalledDrivers() {
		final Set<String> installedDrivers = new HashSet<String>();
		
		final Collection<String> driverCollection = this.jdbcTemplate.query("select classname from lightcontrol.boarddrivers", 
				new RowMapper() {

					/**
					 * {@inheritDoc}
					 */
					public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
						return rs.getString(1);
					}
				}
		);
		
		installedDrivers.addAll(driverCollection);
		
		return installedDrivers;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeInstalledDriver(final String className) {
	}

}
