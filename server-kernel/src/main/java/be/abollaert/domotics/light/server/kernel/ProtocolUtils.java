package be.abollaert.domotics.light.server.kernel;


/**
 * Util class for parsing protocols.
 * 
 * @author alex
 *
 */
final class ProtocolUtils {
	
	/**
	 * Utility class.
	 */
	private ProtocolUtils() {
	}
	
	public static final int[] parseListOfIntegers(final String numericCommaDelimitedString) {
		if (numericCommaDelimitedString == null) {
			return new int[0];
		}
		
		final String[] parts = numericCommaDelimitedString.split(",");
		
		final int[] integers = new int[parts.length];
		
		for (int i = 0; i < parts.length; i++) {
			integers[i] = Integer.valueOf(parts[i]);
		}
		
		return integers;
	}
}
