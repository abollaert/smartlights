package be.techniquez.hometinkering.lightcontrol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.dimmer.DimmerLightControlBoard;

/**
 * Represents a dimmer board.
 * 
 * @author alex
 *
 */
public final class DimmerBoard {

	/** The physical board that is represented by this board. */
	private DimmerLightControlBoard physicalBoard;
	
	/** The channel occupation on this board. */
	private final Map<Integer, DimmerLight> channelOccupation = new HashMap<Integer, DimmerLight>();
	
	/**
	 * Creates a new instance of this board using the given physical board backing it.
	 * 
	 * @param 	physicalBoard	The physical board backing this dimmer board.
	 */
	public DimmerBoard(final DimmerLightControlBoard physicalBoard) {
		this.physicalBoard = physicalBoard;
	}
	
	/** Default constructor. */
	public DimmerBoard() {
	}
	
	/**
	 * @param physicalBoard the physicalBoard to set
	 */
	public final void setPhysicalBoard(DimmerLightControlBoard physicalBoard) {
		this.physicalBoard = physicalBoard;
	}

	/**
	 * Adds the given dimmer light on this board.
	 * 
	 * @param 	dimmerLight		The dimmer light that should be added on this board.
	 */
	public final void addDimmerLight(final DimmerLight dimmerLight) {
		this.channelOccupation.put(dimmerLight.getLightIndex(), dimmerLight);
	}
	
	/**
	 * Returns a list of all the free channels on this board.
	 * 
	 * @return	A list of all the free channels on this board.
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
	 * Gets the connected lights.
	 * 
	 * @return	The lights that are connected to this board.
	 */
	public final Map<Integer, DimmerLight> getConnectedLights() {
		final Map<Integer, DimmerLight> connectedLights = new HashMap<Integer, DimmerLight>();
		
		for (int i = 0; i < this.physicalBoard.getNumberOfChannels(); i++) {
			if (this.channelOccupation.get(i) != null) {
				connectedLights.put(i, this.channelOccupation.get(i));
			}
		}
		
		return connectedLights;
	}

	/**
	 * @return the physicalBoard
	 */
	public final DimmerLightControlBoard getPhysicalBoard() {
		return physicalBoard;
	}
	
	/**
	 * Returns the driver name of the driver responsible for the board.
	 * 
	 * @return	The name of the driver responsible for the board.
	 */
	public final String getDriverName() {
		return this.physicalBoard.getDriverName();
	}
	
}
