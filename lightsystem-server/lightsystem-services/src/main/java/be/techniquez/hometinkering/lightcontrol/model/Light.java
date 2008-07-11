package be.techniquez.hometinkering.lightcontrol.model;

/**
 * Base class for the digital lights as well as the dimmer lights.
 * 
 * @author alex
 */
@SuppressWarnings("unchecked")
public abstract class Light<T extends Board> implements Comparable<Light> {

	/** The channel number. */
	private final int channelNumber;
	
	/** The board. */
	private final T board;
	
	/** The name of the light. */
	private String name;
	
	/** The description of the light. */
	private String description;
	
	/**
	 * Creates a new light on the given board and channel number.
	 * 
	 * @param 	board			The board.
	 * @param 	channelNumber	The channel number.
	 */
	Light(final T board, final int channelNumber) {
		assert board != null : "The board should not be null";
		assert channelNumber >= 0 && channelNumber < board.getNumberOfChannels();
		
		this.board = board;
		this.channelNumber = channelNumber;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final int compareTo(final Light otherLight) {
		return this.channelNumber - otherLight.channelNumber;
	}

	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public final String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Returns the board the light is connected to.
	 * 
	 * @return	The board the light is connected to.
	 */
	public final T getBoard() {
		return this.board;
	}
	
	/**
	 * Returns the channel number.
	 * 
	 * @return	The channel number.
	 */
	public final int getChannelNumber() {
		return this.channelNumber;
	}
}
