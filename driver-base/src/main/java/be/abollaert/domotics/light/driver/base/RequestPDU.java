package be.abollaert.domotics.light.driver.base;


/**
 * PDU class. We parse the data into this class.
 * 
 * @author alex
 */
public final class RequestPDU {
	
	/** Enumerates the types. */
	public enum Type {
		GET_TYPE(1), 
		GET_ID(2),
		GET_FW_VERSION(3),
		GET_NR_CHANNELS(4),
		GET_FEATURES(5),
		GET_SW_THRESHOLD(20),
		GET_SW_TIMER(21),
		GET_CHANNEL_MAPPING(22),
		GET_DEFAULT_STATE(23),
		GET_DIMMER_THRESHOLD(24),
		GET_DEFAULT_PERCENTAGE(25),
		GET_DIMMER_DELAY(26),
		GET_DEFAULT_DIMMER_DIRECTION(27),
		SET_SW_THRESHOLD(50),
		SET_SW_TIMER(51),
		SET_CHANNEL_MAPPING(52),
		SET_DEFAULT_STATE(53),
		SET_DIMMER_THRESHOLD(54),
		SET_DEFAULT_PERCENTAGE(55),
		SET_DIMMER_DELAY(56),
		SET_DEFAULT_DIMMER_DIRECTION(57),
		SET_MODULE_ID(58),
		SAVE_CONFIGURATION(70),
		RELOAD_CONFIGURATION(71),
		GET_OUTPUT_STATE(80),
		GET_INPUT_STATE(81),
		SWITCH_OUTPUT(90),
		DIM(91);
		
		private final int typeId;
		
		/**
		 * Create a new instance.
		 * 
		 * @param typeId
		 */
		private Type(final int typeId) {
			this.typeId = typeId;
		}
		
		public final int getTypeId() {
			return this.typeId;
		}
		
		/**
		 * Returns the {@link Type} that corresponds to the given type ID.
		 * 
		 * @param 		typeId		The type ID.
		 * 
		 * @return		The corresponding type.
		 */
		static final RequestPDU.Type byTypeId(final int typeId) {
			for (final RequestPDU.Type type : Type.values()) {
				if (type.typeId == typeId) {
					return type;
				}
			}
			
			return null;
		}
	}
	
	/** The type. */
	private final RequestPDU.Type type;
	
	/** The arguments. */
	private final int[] arguments;
	
	/**
	 * Create a new {@link RequestPDU}.
	 * 
	 * @param 	type	The type of the PDU.
	 */
	public RequestPDU(final RequestPDU.Type type, int... arguments) {
		this.type = type;
		this.arguments = arguments;
	}
	
	/**
	 * Returns the type.
	 * 
	 * @return	The type.
	 */
	public final Type getType() {
		return this.type;
	}
	
	/**
	 * Returns the arguments.
	 * 
	 * @return	The arguments.
	 */
	public final int[] getArguments() {
		return this.arguments;
	}
}