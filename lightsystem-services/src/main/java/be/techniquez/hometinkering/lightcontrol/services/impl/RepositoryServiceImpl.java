package be.techniquez.hometinkering.lightcontrol.services.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.ControlBoardDriver;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControllerStateListener;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;
import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControllerStateListener;
import be.techniquez.hometinkering.lightcontrol.db.BoardDAO;
import be.techniquez.hometinkering.lightcontrol.db.DriverDAO;
import be.techniquez.hometinkering.lightcontrol.db.LightDAO;
import be.techniquez.hometinkering.lightcontrol.model.DigitalBoard;
import be.techniquez.hometinkering.lightcontrol.model.DigitalLight;
import be.techniquez.hometinkering.lightcontrol.model.DimmerBoard;
import be.techniquez.hometinkering.lightcontrol.model.DimmerLight;
import be.techniquez.hometinkering.lightcontrol.services.LightSystemEventListener;
import be.techniquez.hometinkering.lightcontrol.services.RepositoryService;

/**
 * Repository containing most of the information needed to make things work.
 * This includes the drivers known to the system, and also the mappings from
 * boards and channels to actual lights.
 * 
 * @author alex
 */
public final class RepositoryServiceImpl implements RepositoryService,
		DigitalLightControllerStateListener, DimmerLightControllerStateListener {

	/** Logger instance. */
	private static final Logger logger = Logger
			.getLogger(RepositoryServiceImpl.class.getName());

	/** The DAO to use when asking for drivers. */
	private final DriverDAO driverDAO;

	/** The light DAO. */
	private final LightDAO lightDAO;

	/** The board DAO. */
	private final BoardDAO boardDAO;

	/** The digital control boards that are connected to the system. */
	private final Map<Integer, DigitalBoard> connectedDigitalBoards = new HashMap<Integer, DigitalBoard>();

	/** The dimmer control boards that are connected to the system. */
	private final Map<Integer, DimmerBoard> connectedDimmerBoards = new HashMap<Integer, DimmerBoard>();

	/** Contains the connected and configured digital lights. */
	private final Map<String, DigitalLight> connectedDigitalLights = new HashMap<String, DigitalLight>();

	/** Contains the connected and configured dimmer lights. */
	private final Map<String, DimmerLight> connectedDimmerLights = new HashMap<String, DimmerLight>();

	/** Contains the configured and disconnected digital lights. */
	private final Map<String, DigitalLight> disconnectedDigitalLights = new HashMap<String, DigitalLight>();

	/** Contains the configured and disconnected dimmer lights. */
	private final Map<String, DimmerLight> disconnectedDimmerLights = new HashMap<String, DimmerLight>();

	/** The listeners. */
	private final Set<LightSystemEventListener> listeners = new HashSet<LightSystemEventListener>();

	/**
	 * Creates a new instance.
	 * 
	 * @param driverDAO
	 *            The DAO for the drivers.
	 * @param lightDAO
	 *            The DAO for the lights.
	 */
	public RepositoryServiceImpl(final DriverDAO driverDAO,
			final LightDAO lightDAO, final BoardDAO boardDAO) {
		// Initialize the fields.
		this.driverDAO = driverDAO;
		this.lightDAO = lightDAO;
		this.boardDAO = boardDAO;

		// Load the drivers.
		final Set<String> installedDrivers = this.driverDAO
				.getInstalledDrivers();

		for (final String driverClassName : installedDrivers) {
			this.addBoardsForDriver(driverClassName);
		}

		// OK we know what is connected to the system, let's see how it actually
		// relates
		// to what is defined in the database.
		logger.info("Loading digital lights");

		final Map<String, DigitalLight> digitalLights = this.lightDAO
				.getConfiguredDigitalLights();

		logger.info("Done, got [" + digitalLights.size()
				+ "] digital lights, classifying");

		for (final DigitalLight light : digitalLights.values()) {
			this.classifyDigitalLight(light);
		}

		logger.info("Loading dimmer lights");

		// Get the mapped lights and initialize them.
		final Map<String, DimmerLight> dimmerLights = this.lightDAO
				.getConfiguredDimmerLights();

		logger.info("Done, got [" + dimmerLights.size()
				+ "] dimmer lights, classifying");

		for (final DimmerLight light : dimmerLights.values()) {
			this.classifyDimmerLight(light);
		}

		logger.info("Repository is ready for use");

		// OK done.
	}

	/**
	 * Classifies the digital lights into connected or not connected, and
	 * initializes the board connection.
	 * 
	 * @param light
	 *            The light to classify.
	 */
	private final void classifyDigitalLight(final DigitalLight light) {
		if (this.connectedDigitalBoards.get(light.getBoardID()) != null) {
			logger.info("The light [" + light.getLightIdentifier()
					+ "] is connected to board [" + light.getBoardID()
					+ "], board was found.");

			light.setControlBoard(this.connectedDigitalBoards.get(light
					.getBoardID()));

			this.connectedDigitalLights.put(light.getLightIdentifier(), light);
		} else {
			logger.info("The light [" + light.getLightIdentifier()
					+ "] is connected to board [" + light.getBoardID()
					+ "], board was NOT found.");

			this.disconnectedDigitalLights.put(light.getLightIdentifier(),
					light);
		}
	}

	/**
	 * Classifies the light into connected or not connected, and initializes the
	 * board connection.
	 * 
	 * @param light
	 *            The light.
	 */
	private final void classifyDimmerLight(final DimmerLight light) {
		if (this.connectedDimmerBoards.get(light.getBoardId()) != null) {
			logger.info("The light [" + light.getLightIdentifier()
					+ "] is connected to board [" + light.getBoardId()
					+ "], board was found.");

			light.setControlBoard(this.connectedDimmerBoards.get(light
					.getBoardId()));

			this.connectedDimmerLights.put(light.getLightIdentifier(), light);
		} else {
			logger.info("The light [" + light.getLightIdentifier()
					+ "] is connected to board [" + light.getBoardId()
					+ "], board was NOT found.");

			this.disconnectedDimmerLights
					.put(light.getLightIdentifier(), light);
		}
	}

	/**
	 * Adds all the boards that are connected through the driver that has the
	 * given class name. All errors are just dumped in the warning log, but do
	 * not fail anything.
	 * 
	 * @param driverClassName
	 *            The class name of the driver to load the boards through.
	 */
	@SuppressWarnings("unchecked")
	private final void addBoardsForDriver(final String driverClassName) {
		logger.info("Probing using driver [" + driverClassName + "]");

		try {
			final Class driverClass = Class.forName(driverClassName);
			final ControlBoardDriver driver = (ControlBoardDriver) driverClass
					.newInstance();

			logger.info("Loading digital boards");

			final DigitalLightControlBoard[] digitalBoards = driver
					.loadConnectedDigitalBoards();

			logger.info("Done, got [" + digitalBoards.length + "] boards");

			for (final DigitalLightControlBoard board : digitalBoards) {
				if (this.boardDAO.getDigitalBoard(board.getBoardID()) != null) {
					logger
							.info("Digital board with ID ["
									+ board.getBoardID()
									+ "] is known in the database, adding to connected boards...");

					this.addConnectedDigitalBoard(board);
				} else {
					logger
							.info("Cannot find a board definition in the database for board with ID ["
									+ board.getBoardID()
									+ "], adding automagically...");

					this.boardDAO.addDigitalBoard(board);
					this.addConnectedDigitalBoard(board);
				}

				board.addStateEventListener(this);
			}

			logger.info("Loading dimmer boards");

			final DimmerLightControlBoard[] dimmerBoards = driver
					.loadConnectedDimmerBoards();

			logger.info("Done, got [" + dimmerBoards.length + "] boards");

			for (final DimmerLightControlBoard board : dimmerBoards) {
				if (this.boardDAO.getDimmerBoard(board.getBoardID()) != null) {
					logger
							.info("Found board definition for board with ID ["
									+ board.getBoardID()
									+ "] in the database, adding to connected boards...");
					this.addConnectedDimmerBoard(board);
				} else {
					logger
							.info("Cannot find a board definition for board with ID ["
									+ board.getBoardID()
									+ "] in the database, adding automagically...");
					this.boardDAO.addDimmerBoard(board);
					this.addConnectedDimmerBoard(board);
				}

				board.addStateChangeListener(this);
			}

			logger.info("Boards for driver [" + driverClassName + "] added");
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING, "Cannot load driver [" + driverClassName
					+ "] as it is probably not on the classpath !", e);
		} catch (IllegalAccessException e) {
			logger.log(Level.WARNING, "Cannot instantiate driver ["
					+ driverClassName
					+ "] as it probably does not have a public constructor !",
					e);
		} catch (InstantiationException e) {
			logger.log(Level.WARNING, "Cannot instantiate driver ["
					+ driverClassName
					+ "] as there was probably an error in the constructor !",
					e);
		}
	}

	/**
	 * Adds the given board to the repo.
	 * 
	 * @param board
	 *            The board to add to the repo.
	 */
	private final void addConnectedDigitalBoard(
			final DigitalLightControlBoard board) {
		// Sanity check...
		if (this.connectedDigitalBoards.get(board.getBoardID()) != null) {
			throw new IllegalStateException(
					"Board with ID ["
							+ board.getBoardID()
							+ "] is already known on this system, cannot add it multiple times !");
		}

		this.connectedDigitalBoards.put(board.getBoardID(), new DigitalBoard(
				board));
	}

	/**
	 * Adds the given board to the repo.
	 * 
	 * @param board
	 *            The board to add.
	 */
	private final void addConnectedDimmerBoard(
			final DimmerLightControlBoard board) {
		if (this.connectedDimmerBoards.get(board.getBoardID()) != null) {
			throw new IllegalStateException(
					"Board with ID ["
							+ board.getBoardID()
							+ "] is already known in the repo, cannot add it multiple times !");
		}

		this.connectedDimmerBoards.put(board.getBoardID(), new DimmerBoard(
				board));
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, DigitalLight> getConnectedDigitalLights() {
		return this.connectedDigitalLights;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, DigitalLight> getDisconnectedDigitalLights() {
		return this.disconnectedDigitalLights;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, DimmerLight> getConnectedDimmerLights() {
		return this.connectedDimmerLights;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<String, DimmerLight> getDisconnectedDimmerLights() {
		return this.disconnectedDimmerLights;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DigitalLight getDigitalLight(final String id) {
		DigitalLight light = this.connectedDigitalLights.get(id);

		if (light == null) {
			light = this.disconnectedDigitalLights.get(id);
		}

		return light;
	}

	/**
	 * {@inheritDoc}
	 */
	public final DimmerLight getDimmerLight(final String id) {
		DimmerLight light = this.connectedDimmerLights.get(id);

		if (light == null) {
			light = this.disconnectedDimmerLights.get(id);
		}

		return light;
	}

	/**
	 * Gets the dimmer light on one of the connected boards with the given light
	 * index on the given board.
	 * 
	 * @param boardId
	 *            The ID of the board.
	 * @param lightIndex
	 *            The index of the light.
	 * 
	 * @return The dimmer light, null if not found.
	 */
	private final DimmerLight getDimmerLightOnBoardAndChannel(
			final int boardId, final int lightIndex) {
		for (final DimmerLight light : this.connectedDimmerLights.values()) {
			if (light.getBoardId() == boardId
					&& light.getLightIndex() == lightIndex) {
				return light;
			}
		}

		return null;
	}

	/**
	 * Gets the digital light on one of the boards with the given light index
	 * and the given board ID.
	 * 
	 * @param boardId
	 *            The ID of the board.
	 * @param lightIndex
	 *            The light index.
	 * 
	 * @return The digital light, null if not found.
	 */
	private final DigitalLight getDigitalLightOnBoardAndChannel(
			final int boardId, final int lightIndex) {
		for (final DigitalLight light : this.connectedDigitalLights.values()) {
			if (light.getBoardID() == boardId
					&& light.getLightIndex() == lightIndex) {
				return light;
			}
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addLightSystemEventListener(
			final LightSystemEventListener listener) {
		this.listeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void removeLightSystemEventListener(
			final LightSystemEventListener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void lightSwitchedOff(final int boardId, final int lightIndex) {
		// First check if it is a dimmable light...
		logger.info("Light [" + lightIndex + "] on board [" + boardId
				+ "] has been switched off, checking if we can propagate...");

		DimmerLight changedDimmerLight = this.getDimmerLightOnBoardAndChannel(
				boardId, lightIndex);

		if (changedDimmerLight != null) {
			logger.info("Changed status for dimmer light ["
					+ changedDimmerLight.getLightIdentifier()
					+ "], notifying listeners...");
			this.notifyDimmerLightStatusChanged(changedDimmerLight);
		} else {
			DigitalLight changedDigitalLight = this
					.getDigitalLightOnBoardAndChannel(boardId, lightIndex);

			if (changedDigitalLight != null) {
				logger.info("Changed status for digital light ["
						+ changedDigitalLight.getLightIdentifier()
						+ "], notifying listeners...");

				this.notifyDigitalLightStatusChanged(changedDigitalLight);
			} else {
				logger
						.info("Cannot match light ["
								+ lightIndex
								+ "] on board ["
								+ boardId
								+ "] to any light that is configured, so ignoring event...");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void lightSwitchedOn(final int boardId, final int lightIndex) {
		logger.info("Light [" + lightIndex + "] on board [" + boardId
				+ "] has been switched on, checking if we can propagate...");

		// First check if it is a dimmable light...
		DimmerLight changedDimmerLight = this.getDimmerLightOnBoardAndChannel(
				boardId, lightIndex);

		if (changedDimmerLight != null) {
			logger.info("Changed status for dimmer light ["
					+ changedDimmerLight.getLightIdentifier()
					+ "], notifying listeners...");
			this.notifyDimmerLightStatusChanged(changedDimmerLight);
		} else {
			DigitalLight changedDigitalLight = this
					.getDigitalLightOnBoardAndChannel(boardId, lightIndex);

			if (changedDigitalLight != null) {
				logger.info("Changed status for digital light ["
						+ changedDigitalLight.getLightIdentifier()
						+ "], notifying listeners...");

				this.notifyDigitalLightStatusChanged(changedDigitalLight);
			} else {
				logger
						.info("Cannot match light ["
								+ lightIndex
								+ "] on board ["
								+ boardId
								+ "] to any light that is configured, so ignoring event...");
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final void percentageChanged(final int boardId,
			final int lightIndex, final int newPercentage) {
		logger.info("Light [" + lightIndex + "] on board [" + boardId
				+ "] has changed percentage to [" + newPercentage
				+ "], seeing if we can propagate...");

		// First check if it is a dimmable light...
		DimmerLight changedDimmerLight = this.getDimmerLightOnBoardAndChannel(
				boardId, lightIndex);

		if (changedDimmerLight != null) {
			logger.info("Changed status for dimmer light ["
					+ changedDimmerLight.getLightIdentifier()
					+ "], notifying listeners...");
			this.notifyDimmerLightStatusChanged(changedDimmerLight);
		} else {
			logger
					.info("Cannot match light ["
							+ lightIndex
							+ "] on board ["
							+ boardId
							+ "] to any light that is configured, so ignoring event...");
		}
	}

	/**
	 * Notifies the listeners that the state for the given dimmer light has
	 * changed.
	 * 
	 * @param light
	 *            The light whose status has changed.
	 */
	private final void notifyDimmerLightStatusChanged(final DimmerLight light) {
		for (final LightSystemEventListener listener : this.listeners) {
			listener.dimmerLightStatusChanged(light);
		}
	}

	/**
	 * Notifies the listeners that the state for the given digital light has
	 * changed.
	 * 
	 * @param light
	 *            The light for which the status has changed.
	 */
	private final void notifyDigitalLightStatusChanged(final DigitalLight light) {
		for (final LightSystemEventListener listener : this.listeners) {
			listener.digitalLightStatusChanged(light);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<Integer, List<Integer>> getAllFreeDigitalChannels() {
		final Map<Integer, List<Integer>> freeDigitalChannels = new HashMap<Integer, List<Integer>>();

		for (final DigitalBoard digitalBoard : this.connectedDigitalBoards
				.values()) {
			final List<Integer> freeChannelsOnBoard = digitalBoard
					.getFreeChannels();
			freeDigitalChannels.put(digitalBoard.getPhysicalControlBoard()
					.getBoardID(), freeChannelsOnBoard);
		}

		return freeDigitalChannels;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<Integer, List<Integer>> getAllFreeDimmerChannels() {
		final Map<Integer, List<Integer>> freeDimmerChannels = new HashMap<Integer, List<Integer>>();

		for (final DimmerBoard board : this.connectedDimmerBoards.values()) {
			final List<Integer> freeDimmerChannelsOnBoard = board
					.getFreeChannels();
			freeDimmerChannels.put(board.getPhysicalBoard().getBoardID(),
					freeDimmerChannelsOnBoard);
		}

		return freeDimmerChannels;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<Integer, Map<Integer, DigitalLight>> getDigitalBoardsConfiguration() {
		final Map<Integer, Map<Integer, DigitalLight>> configuration = new HashMap<Integer, Map<Integer, DigitalLight>>();

		for (final DigitalBoard board : this.connectedDigitalBoards.values()) {
			final Map<Integer, DigitalLight> configuredLights = board
					.getConnectedLights();

			for (final Integer i : board.getFreeChannels()) {
				configuredLights.put(i, null);
			}

			configuration.put(board.getPhysicalControlBoard().getBoardID(),
					configuredLights);
		}

		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public final Map<Integer, Map<Integer, DimmerLight>> getDimmerBoardsConfiguration() {
		final Map<Integer, Map<Integer, DimmerLight>> configuration = new HashMap<Integer, Map<Integer, DimmerLight>>();

		for (final DimmerBoard board : this.connectedDimmerBoards.values()) {
			final Map<Integer, DimmerLight> configuredLights = board
					.getConnectedLights();

			for (final Integer i : board.getFreeChannels()) {
				configuredLights.put(i, null);
			}

			configuration.put(board.getPhysicalBoard().getBoardID(),
					configuredLights);
		}

		return configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDriverNameForBoard(final int boardId) {
		if (this.connectedDigitalBoards.get(boardId) != null) {
			return this.connectedDigitalBoards.get(boardId).getDriverName();
		} else if (this.connectedDimmerBoards.get(boardId) != null) {
			return this.connectedDimmerBoards.get(boardId).getDriverName();
		}
		
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final int getNumberOfChannelsOnBoard(final int boardId) {
		if (this.connectedDigitalBoards.get(boardId) != null) {
			return this.connectedDigitalBoards.get(boardId).getPhysicalControlBoard().getNumberOfChannels();
		} else if (this.connectedDimmerBoards.get(boardId) != null) {
			return this.connectedDimmerBoards.get(boardId).getPhysicalBoard().getNumberOfChannels();
		}
		
		return -1;
	}
}
