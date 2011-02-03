package be.abollaert.domotics.light.api;


public enum ChannelState {
	
	/** The enumeration. */
	ON(1), OFF(0);
	
	/** The character. */
	private final int state;

	private ChannelState(final int state) {
		this.state = state;
	}
	
	static final ChannelState getState(final int state) {
		for (final ChannelState channelState : ChannelState.values()) {
			if (channelState.state == state) {
				return channelState;
			}
		}
		
		return null;
	}
	
	public final int getState() {
		return this.state;
	}
}
