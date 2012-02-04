package be.abollaert.domotics.light.driver.base;

/** The type of serial channel. */
public enum ChannelType {
	DIGITAL("0"), DIMMER("1");
	
	/** The type string. */
	private final String typeString;
	
	/**
	 * Create a new instance using the given type string.
	 * 
	 * @param 	typeString		The type string.
	 */
	private ChannelType(final String typeString) {
		this.typeString = typeString;
	}
	
	/**
	 * Returns the matching type, <code>null</code> if no matching types found.
	 * 	
	 * @param 	typeString		The type string to match.
	 */
	static final ChannelType fromTypeString(final String typeString) {
		for (final ChannelType type : ChannelType.values()) {
			if (type.typeString.equals(typeString)) {
				return type;
			}
		}
		
		return null;
	}
	
	public final String getTypeString() {
		return this.typeString;
	}
}