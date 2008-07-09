package be.techniquez.hometinkering.lightcontrol.model;

import java.io.IOException;

/**
 * A digital light.
 * 
 * @author alex
 */
public final class DigitalLight {

	/** The light identifier. */
	private final String lightIdentifier;
	
	/** The light index. */
	private final int lightIndex;
	
	/** The control board. */
	private DigitalBoard controlBoard;
	
	/** The board ID. */
	private final int boardId;
	
	/** The description of the light. */
	private final String description;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param 	lightIdentifier		The light identifier.
	 * @param 	lightIndex			The light index.
	 * @param 	controlBoard		The control board.
	 */
	public DigitalLight(final String lightIdentifier, final int lightIndex, final int boardId, final String description) {
		this.lightIdentifier = lightIdentifier;
		this.boardId = boardId;
		this.lightIndex = lightIndex;
		this.description = description;
	}
	
	/**
	 * Returns the description of the light.
	 * 
	 * @return	The description of the light.
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * Returns true if the light is on, false if not.
	 * 
	 * @return	True if the light is on, false if not.
	 */
	public final boolean isOn() {
		this.checkLightConnected();
		
		return this.controlBoard.getPhysicalControlBoard().isLightOn(this.lightIndex);
	}
	
	/**
	 * Returns the light identifier. 
	 * 
	 * @return	The light identifier.
	 */
	public final String getLightIdentifier() {
		return this.lightIdentifier;
	}
	
	/**
	 * Switches the light on.
	 */
	public final void switchOn() throws IOException {
		this.checkLightConnected();
		
		this.controlBoard.getPhysicalControlBoard().switchLight(this.lightIndex, true);
	}
	
	/**
	 * Switches the light off.
	 */
	public final void switchOff() throws IOException {
		this.checkLightConnected();
		
		this.controlBoard.getPhysicalControlBoard().switchLight(this.lightIndex, false);
	}
	
	/**
	 * Checks if the light is connected.
	 */
	private final void checkLightConnected() {
		if (this.controlBoard == null) {
			throw new IllegalStateException("The board this light is connected to is not connected to the system !");
		}
	}
	
	/**
	 * Returns the board ID.
	 * 
	 * @return	The board ID.
	 */
	public final int getBoardID() {
		return this.boardId;
	}

	/**
	 * @param controlBoard the controlBoard to set
	 */
	public final void setControlBoard(final DigitalBoard controlBoard) {
		this.controlBoard = controlBoard;
		controlBoard.addDigitalLight(this);
	}

	/**
	 * @return the lightIndex
	 */
	public final int getLightIndex() {
		return lightIndex;
	}
}
