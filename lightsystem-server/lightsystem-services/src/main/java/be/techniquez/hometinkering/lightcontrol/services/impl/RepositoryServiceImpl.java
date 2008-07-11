package be.techniquez.hometinkering.lightcontrol.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.ControlBoardDriver;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControllerStateListener;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControllerStateListener;
import be.techniquez.hometinkering.lightcontrol.db.DriverDAO;
import be.techniquez.hometinkering.lightcontrol.db.LightDAO;
import be.techniquez.hometinkering.lightcontrol.model.Board;
import be.techniquez.hometinkering.lightcontrol.model.DigitalBoard;
import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerBoard;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;
import be.techniquez.hometinkering.lightcontrol.model.Light;
import be.techniquez.hometinkering.lightcontrol.services.LightSystemEventListener;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Repository containing most of the information needed to make things work.
 * This includes the drivers known to the system, and also the mappings from
 * boards and channels to actual lights.
 * 
 * @author alex
 */
public final class RepositoryServiceImpl implements RepositoryService, DigitalLightControllerStateListener, DimmerLightControllerStateListener {

	/** Logger instance. */
	private static final Logger logger = Logger.getLogger(RepositoryServiceImpl.class.getName());

	/** The DAO to use when asking for drivers. */
	private final DriverDAO driverDAO;

	/** The light DAO. */
	private final LightDAO lightDAO;

	/** Digital boards connected to the system. */
	private final List<DigitalBoard> digitalBoards = new ArrayList<DigitalBoard>();
	
	/** Dimmer boards connected to the system. */
	private final List<DimmerBoard> dimmerBoards = new ArrayList<DimmerBoard>();
	
	/** The event listeners registered to this class. */
	private final Set<LightSystemEventListener> eventListeners = new HashSet<LightSystemEventListener>();
	
	/**
	 * Creates a new instance.
	 * 
	 * @param driverDAO
	 *            The DAO for the drivers.
	 * @param lightDAO
	 *            The DAO for the lights.
	 */
	public RepositoryServiceImpl(final DriverDAO driverDAO, final LightDAO lightDAO) {
		// Initialize the fields.
		this.driverDAO = driverDAO;
		this.lightDAO = lightDAO;

		this.reload();
	}
	
	/**
	 * Reloads the repository.
	 */
	@SuppressWarnings("unchecked")
	public final void reload() {
		this.digitalBoards.clear();
		this.dimmerBoards.clear();
		
		final Set<ControlBoardDriver> drivers = getDeclaredControlBoardDrivers();
		
		for (final ControlBoardDriver driver : drivers) {
			for (final DigitalLightControlBoard digitalPhysicalBoard : driver.loadConnectedDigitalBoards()) {
				final DigitalBoard digitalBoard = new DigitalBoard(digitalPhysicalBoard);
				this.digitalBoards.add(digitalBoard);
				digitalPhysicalBoard.addStateEventListener(this);
			}
			
			for (final DimmerLightControlBoard physicalDimmerBoard : driver.loadConnectedDimmerBoards()) {
				final DimmerBoard dimmerBoard = new DimmerBoard(physicalDimmerBoard);
				this.dimmerBoards.add(dimmerBoard);
				physicalDimmerBoard.addStateChangeListener(this);
			}
		}
		
		for (final Light light : this.allLights()) {
			this.lightDAO.addDatabaseInformation(light);
		}
	}
	
	/**
	 * Gets all the lights currently connected to the system.
	 * 
	 * @return	All the lights currently connected to the system.
	 */
	@SuppressWarnings("unchecked")
	private final List<Light> allLights() {
		final List<Light> allLights = new ArrayList<Light>();
		
		for (final Board board : this.allBoards()) {
			allLights.addAll(board.getAllLights());
		}
	
		return allLights;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final List<Board> allBoards() {
		final List<Board> allBoards = new ArrayList<Board>();
		
		allBoards.addAll(this.digitalBoards);
		allBoards.addAll(this.dimmerBoards);
		
		return allBoards;
	}

	/**
	 * Gets instances of every driver in the database.
	 * 
	 * @return	Instances of every driver in the database.
	 */
	@SuppressWarnings("unchecked")
	private final Set<ControlBoardDriver> getDeclaredControlBoardDrivers() {
		logger.info("Getting the control board drivers that are declared in the database...");
		
		final Set<ControlBoardDriver> drivers = new HashSet<ControlBoardDriver>();
		
		final Set<String> declaredDriverClassNames = this.driverDAO.getInstalledDrivers();
		
		for (final String driverClassName : declaredDriverClassNames) {
			try {
				final Class driverClass = Class.forName(driverClassName);
				final ControlBoardDriver driver = (ControlBoardDriver)driverClass.newInstance();
				
				drivers.add(driver);
			} catch (ClassNotFoundException e) {
				logger.log(Level.WARNING, "Cannot load driver class [" + driverClassName + "], driver class could not be found...");
			} catch (IllegalAccessException e) {
				logger.log(Level.WARNING, "Cannot load driver class [" + driverClassName + "], no-arg constructor was not accessible...");
			} catch (InstantiationException e) {
				logger.log(Level.WARNING, "Cannot load driver class [" + driverClassName + "], instantiation failed...", e);
			}
		}
		
		return drivers;
	}
	
	/**
	 * Gets the digital light on the given board with the given index.
	 * 
	 * @param 	boardId			The board ID.
	 * @param 	lightIndex		The light index.
	 * @return
	 */
	private final DigitalLight getDigitalLight(final int boardId, final int lightIndex) {
		final DigitalBoard board = this.getDigitalBoard(boardId);
		
		// If the board is connected we are safe because we generate the info out of it.
		if (board != null) {
			return board.getLightOnChannel(lightIndex);
		}
		
		return null;
	}
	
	/**
	 * Gets the dimmer light in the given board with the given index.
	 * 
	 * @param	boardId		The ID of the board.
	 * @param 	lightIndex	The index of the board.
	 * @return
	 */
	private final DimmerLight getDimmerLight(final int boardId, final int lightIndex) {
		final DimmerBoard board = this.getDimmerBoard(boardId);
		
		// If the board is connected we are safe because we generate the info out of it.
		if (board != null) {
			return board.getLightOnChannel(lightIndex);
		}
		
		return null;
	}
	
	/**
	 * Check if the given board is a digital one.
	 * 
	 * @param 	boardId		The ID of the board.
	 * 
	 * @return	True if the board is digital, false if not.
	 */
	private final boolean isDigitalBoard(final int boardId) {
		return this.getDigitalBoard(boardId) != null;
 	}
	
	/**
	 * Returns the digital board with the given ID, null if not found.
	 * 
	 * @param 		boardId		The sought after board ID.
	 * 
	 * @return		The board with the given ID, null if not found.
	 */
	private final DigitalBoard getDigitalBoard(final int boardId) {
		for (final DigitalBoard board : this.digitalBoards) {
			if (board.getID() == boardId) {
				return board;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the dimmer board with the given ID, null if not found.
	 * 
	 * @param 		boardId		The ID of the board.
	 * 
	 * @return		The board or null if the board is not known.
	 */
	private final DimmerBoard getDimmerBoard(final int boardId) {
		for (final DimmerBoard board : this.dimmerBoards) {
			if (board.getID() == boardId) {
				return board;
			}
		}
		
		return null;
	}
	
	/**
	 * Check if the given board is a dimmer one.
	 * 
	 * @param 	boardId		The ID of the board.
	 * 
	 * @return	True if the board is dimmer, false if not.
	 */
	private final boolean isDimmerBoard(final int boardId) {
		return getDimmerBoard(boardId) != null;
	}
	

	/**
	 * Adds an event listener.
	 * 
	 * @param 	listener		The listener.
	 */
	public final void addListener(final LightSystemEventListener listener) {
		this.eventListeners.add(listener);
	}
	
	/**
	 * Fires an digital event for the given light.
	 * 
	 * @param 	light		The light for which the event is fired.
	 */
	private final void fireDigitalEvent(final DigitalLight light) {
		for (final LightSystemEventListener listener : this.eventListeners) {
			listener.digitalLightStatusChanged(light);
		}
	}
	
	/**
	 * Fires a dimmer event for the given light.
	 * 
	 * @param 	dimmerLight		The light for which the event is fired.
	 */
	private final void fireDimmerEvent(final DimmerLight dimmerLight) {
		for (final LightSystemEventListener listener : this.eventListeners) {
			listener.dimmerLightStatusChanged(dimmerLight);
		}
	}
	
	/**
	 * Fires an event for the given board ID and light index.
	 * 
	 * @param 	boardId			The board ID.
	 * @param 	lightIndex		The index of the light.
	 */
	private final void fireEvent(final int boardId, final int lightIndex) {
		if (this.isDigitalBoard(boardId)) {
			this.fireDigitalEvent(this.getDigitalLight(boardId, lightIndex));
		} else if (this.isDimmerBoard(boardId)) {
			this.fireDimmerEvent(this.getDimmerLight(boardId, lightIndex));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void lightSwitchedOn(final int boardId, final int lightIndex) {
		this.fireEvent(boardId, lightIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void percentageChanged(int boardId, int lightIndex, int newPercentage) {
		this.fireEvent(boardId, lightIndex);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public final void lightSwitchedOff(final int boardId, final int lightIndex) {
		this.fireEvent(boardId, lightIndex);
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final Light getLight(final int boardId, final int channel) {
		if (this.isDigitalBoard(boardId)) {
			return this.getDigitalLight(boardId, channel);
		} else if (this.isDimmerBoard(boardId)) {
			return this.getDimmerLight(boardId, channel);
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public final void saveLight(final Light light) {
		this.lightDAO.updateLight(light);
	}
}
