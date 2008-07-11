package be.techniquez.hometinkering.lightcontrol.model;

import java.util.ArrayList;
import java.util.List;

import be.techniquez.hometinkering.lightcontrol.controlboard.spi.LightControlBoard;

/**
 * Base type of dimmer and digital boards.
 * 
 * @author alex
 *
 * @param 	<T>		The type of lights.
 * @param	<B>		The physical board type.
 */
@SuppressWarnings("unchecked")
public abstract class Board<T extends Light, B extends LightControlBoard> {
	
	/** The board types. */
	public enum BoardType {
		DIGITAL, DIMMER;
	}
	
	/** The channels. */
	private final List<T> channels = new ArrayList<T>();
	
	/** The physical board. */
	private final B physicalBoard;
	
	/**
	 * Creates a new board using the physical board.
	 * 
	 * @param 	physicalBoard		The physical board.
	 */
	protected Board(final B physicalBoard) {
		assert physicalBoard != null : "The physical board should not be null !";
		
		this.physicalBoard = physicalBoard;
		
		for (int i = 0; i < physicalBoard.getNumberOfChannels(); i++) {
			this.channels.add(this.createLight(i));
		}
	}
	
	/**
	 * Creates a light on the given board for the given channel number.
	 * 
	 * @param 	channelNumber	The channel number.
	 * 
	 * @return	A newly created light of the correct type.
	 */
	protected abstract T createLight(final int channelNumber);
	
	/**
	 * Returns the physical board that is backing this model board.
	 * 
	 * @return	The physical board backing this one.
	 */
	protected final B getPhysicalBoard() {
		return this.physicalBoard;
	}
	
	/**
	 * Returns the number of channels on this board.
	 * 
	 * @return	The number of channels on this board.
	 */
	public final int getNumberOfChannels() {
		return this.physicalBoard.getNumberOfChannels();
	}
	
	/**
	 * Returns the light on the given channel.
	 * 
	 * @param 	channel		The channel.
	 * 
	 * @return	The light on the given channel.
	 */
	public final T getLightOnChannel(final int channel) {
		assert channel >= 0 && channel < this.channels.size() : "The channel must be between [0] and [" + this.physicalBoard.getNumberOfChannels() + "]";
		
		return this.channels.get(channel);
	}
	
	/**
	 * Returns the light with the given name. Returns null if no light found.
	 * 
	 * @param 		name		The name of the light we are looking for.
	 * 
	 * @return		The light with the given name, null if not found.
	 */
	public final T getLightWithName(final String name) {
		for (final T light : this.channels) {
			if (name.equals(light.getName())) {
				return light;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns all the channels in order. They all contain channels.
	 * 
	 * @return	The list of all channels.
	 */
	public final List<T> getAllLights() {
		return this.channels;
	}
	
	/**
	 * Returns the id of the board.
	 * 
	 * @return	The ID of the board.
	 */
	public final int getID() {
		return this.physicalBoard.getBoardID();
	}
	
	/**
	 * Returns the firmware version of the board.
	 * 
	 * @return	The firmware version of the board.
	 */
	public final int getFirmwareVersion() {
		return this.physicalBoard.getBoardVersion();
	}
	
	/**
	 * Returns the type of this board.
	 * 
	 * @return	The type of this board.
	 */
	public abstract BoardType getType();
	
	/**
	 * Returns the driver name of the board.
	 * 
	 * @return	The driver name of the board.
	 */
	public final String getDriverName() {
		return this.physicalBoard.getDriverName();
	}
}
