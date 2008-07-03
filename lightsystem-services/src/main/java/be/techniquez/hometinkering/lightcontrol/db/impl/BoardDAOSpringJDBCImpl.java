package be.techniquez.hometinkering.lightcontrol.db.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.db.BoardDAO;
import be.techniquez.hometinkering.lightcontrol.model.DigitalBoard;
import be.techniquez.hometinkering.lightcontrol.model.DimmerBoard;

/**
 * Spring JDBC template based implementation of the board DAO interface.
 * 
 * @author alex
 */
public final class BoardDAOSpringJDBCImpl implements BoardDAO {
	
	/** The JDBC template we are going to use here. */
	private final JdbcTemplate jdbcTemplate;
	
	/**
	 * Creates a new instance instructing it to use the given data source.
	 * 
	 * @param 	datasource	The data source that should be used.
	 */
	public BoardDAOSpringJDBCImpl(final DataSource datasource) {
		this.jdbcTemplate = new JdbcTemplate(datasource);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addDigitalBoard(final DigitalLightControlBoard digitalBoard) {
		this.jdbcTemplate.update("insert into boards (id, type, number_of_channels) values (?, ?, ?)", new Object[] { digitalBoard.getBoardID(), "DIGITAL", digitalBoard.getNumberOfChannels() });
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addDimmerBoard(final DimmerLightControlBoard dimmerBoard) {
		this.jdbcTemplate.update("insert into boards (id, type, number_of_channels) values (?, ?, ?)", new Object[] { dimmerBoard.getBoardID(), "DIMMER", dimmerBoard.getNumberOfChannels() });
	}

	/**
	 * {@inheritDoc}
	 */
	public final DigitalBoard getDigitalBoard(final int boardId) {
		final DigitalBoard board = (DigitalBoard)this.jdbcTemplate.query("select id, type, number_of_channels from boards where type = 'DIGITAL' and id = ?", new Object [] { boardId }, new ResultSetExtractor() {
			public final Object extractData(final ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					final DigitalBoard board = new DigitalBoard();
					return board;
				} else {
					return null;
				}
			}
		});
		
		return board;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DimmerBoard getDimmerBoard(final int boardId) {
		final DimmerBoard board = (DimmerBoard)this.jdbcTemplate.query("select id, type, number_of_channels from boards where type = 'DIMMER' and id = ?", new Object [] { boardId }, new ResultSetExtractor() {
			public final Object extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next()) {
					final DimmerBoard board = new DimmerBoard();
					return board;
				} else {
					return null;
				}
			}
		});
		
		return board;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeDigitalBoard(final int boardId) {
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeDimmerBoard(final int boardId) {
	}

}
