package be.techniquez.hometinkering.lightcontrol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.digital.DigitalLightControlBoard;

/**
 * Digital board class, adds more metadata to the board information.
 * 
 * @author alex
 */
public final class DigitalBoard {

	/** The channel occupation on the board. */
	private final Map<Integer, DigitalLight> channelOccupation = new HashMap<Integer, DigitalLight>();

	/** The physical board implementation backing this model object. */
	private DigitalLightControlBoard physicalBoard;
	
	/**
	 * Creates a new instance around the given physical board. 
	 * 
	 * @param 	physicalBoard	The physical board.
	 */
	public DigitalBoard(final DigitalLightControlBoard physicalBoard) {
		this.physicalBoard = physicalBoard;
	}
	
	/** Default constructor. */
	public DigitalBoard() {
	}
	
	/**
	 * Adds the digital light on this board. 
	 * 
	 * @param 	light	The light that should be added.
	 */
	public final void addDigitalLight(final DigitalLight light) {
		this.channelOccupation.put(light.getLightIndex(), light);
	}
	
	/**
	 * Returns the list of free channels on this board.
	 * 
	 * @return	The list of free channels on this board.
	 */
	public final List<Integer> getFreeChannels() {
		final List<Integer> freeChannels = new ArrayList<Integer>();
		
		for (int i = 0; i < this.physicalBoard.getNumberOfChannels(); i++) {
			if (this.channelOccupation.get(i) == null) {
				freeChannels.add(i);
			}
		}
		
		return freeChannels;
	}
	
	/**
	 * Returns the physical control board that is associated to this digital board.
	 * 
	 * @return	The physical control board that is associated to this digital board.
	 */
	public final DigitalLightControlBoard getPhysicalControlBoard() {
		return this.physicalBoard;
	}
	
	/**
	 * Gets the connected lights.
	 * 
	 * @return	The lights that are connected to this board.
	 */
	public final Map<Integer, DigitalLight> getConnectedLights() {
		final Map<Integer, DigitalLight> connectedLights = new HashMap<Integer, DigitalLight>();
		
		for (int i = 0; i < this.physicalBoard.getNumberOfChannels(); i++) {
			if (this.channelOccupation.get(i) != null) {
				connectedLights.put(i, this.channelOccupation.get(i));
			}
		}
		
		return connectedLights;
	}

	/**
	 * @param physicalBoard the physicalBoard to set
	 */
	public final void setPhysicalBoard(final DigitalLightControlBoard physicalBoard) {
		this.physicalBoard = physicalBoard;
	}
	
	/**
	 * Returns the driver name of the board.
	 * 
	 * @return	The driver name of the board.
	 */
	public final String getDriverName() {
		return this.physicalBoard.getDriverName();
	}
}
