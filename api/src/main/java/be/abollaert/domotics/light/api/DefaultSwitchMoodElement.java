package be.abollaert.domotics.light.api;

public class DefaultSwitchMoodElement extends AbstractMoodElement implements SwitchMoodElement {

	/** The state. */
	private final ChannelState state;
	
	/**
	 * Create a new instance specifying the module ID and channel number. The state parameter indicates the requested state.
	 * 
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 * @param 	state				The requested state.
	 */
	public DefaultSwitchMoodElement(final int moduleId, final int channelNumber, final ChannelState state) {
		super(moduleId, channelNumber);
		
		this.state = state;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final ChannelState getRequestedState() {
		return this.state;
	}
}
