package be.abollaert.domotics.light.driver.base;


/**
 * Response PDU.
 * 
 * @author alex
 */
public final class ResponsePDU {
	
	/** Enumerates the types. */
	public enum Type {
		OK(0), 
		ERROR(1), 
		OUTPUT_CHANNEL_STATE_CHANGE(5), 
		INPUT_CHANNEL_STATE_CHANGE(6), 
		CONFIGURATION_CHANGED(7);
		
		private final int typeId;
		
		/**
		 * Create a new instance.
		 * 
		 * @param typeId
		 */
		private Type(final int typeId) {
			this.typeId = typeId;
		}
		
		/**
		 * Returns the {@link Type} that corresponds to the given type ID.
		 * 
		 * @param 		typeId		The type ID.
		 * 
		 * @return		The corresponding type.
		 */
		public static final ResponsePDU.Type byTypeId(final int typeId) {
			for (final ResponsePDU.Type type : Type.values()) {
				if (type.typeId == typeId) {
					return type;
				}
			}
			
			return null;
		}
	}
	
	/** The type. */
	private final ResponsePDU.Type type;
	
	/** The arguments. */
	private final int[] arguments;
	
	public ResponsePDU(final ResponsePDU.Type type, final int... arguments) {
		this.type = type;
		this.arguments = arguments;
	}
	
	public final ResponsePDU.Type getType() {
		return this.type;
	}
	
	public final int[] getArguments() {
		return this.arguments;
	}
}