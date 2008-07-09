package be.techniquez.hometinkering.lightcontrol.model;

import java.io.IOException;

/**
 * A dimmer light.
 * 
 * @author alex
 */
public final class DimmerLight {
	
	/** The light identifier. */
	private final String lightIdentifier;

	/** The description of a light. */
	private final String description;
	
	/** The index of the light we are talking about. */
	private final int lightIndex;
	
	/** The board ID. */
	private final int boardId;
	
	/** The control board that is controlling the light. */
	private DimmerBoard controlBoard;
	
	/**
	 * Creates a new instance of a dimmer light. 
	 * 
	 * @param	lightIdentifier	The identifier of the light.
	 * @param 	lightIndex		The channel number on the board.
	 * @param 	controlBoard	The board controlling the light.
	 * @param	description		The description of the light.
	 */
	public DimmerLight(final String lightIdentifier, final int lightIndex, final int boardID, final String description) {
		this.lightIdentifier = lightIdentifier;
		this.lightIndex = lightIndex;
		this.boardId = boardID;
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
		return this.controlBoard.getPhysicalBoard().isLightOn(this.lightIndex);
	}
	
	/**
	 * Returns the dim percentage of the light.
	 * 
	 * @return	The dim percentage of the light.
	 */
	public final int getPercentage() {
		return this.controlBoard.getPhysicalBoard().getLightPercentage(this.lightIndex);
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
	 * Switches on the light.
	 */
	public final void switchOn() throws IOException {
		this.controlBoard.getPhysicalBoard().switchLight(this.lightIndex, true);
	}
	
	/**
	 * Switches off the light.
	 */
	public final void switchOff() throws IOException {
		this.controlBoard.getPhysicalBoard().switchLight(this.lightIndex, false);
	}
	
	/**
	 * Sets the light percentage to the given value.
	 * 
	 * @param 	percentage	The percentage to set the light to.
	 */
	public final void dim(final int percentage) throws IOException {
		this.controlBoard.getPhysicalBoard().setLightPercentage(this.lightIndex, percentage);
	}

	/**
	 * @return the boardId
	 */
	public final int getBoardId() {
		return boardId;
	}

	/**
	 * @param controlBoard the controlBoard to set
	 */
	public final void setControlBoard(final DimmerBoard controlBoard) {
		this.controlBoard = controlBoard;
		controlBoard.addDimmerLight(this);
	}

	/**
	 * @return the lightIndex
	 */
	public final int getLightIndex() {
		return lightIndex;
	}
}
