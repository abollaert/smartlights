package be.abollaert.domotics.zigbee.zstack;

final class ZStackUtils {

	private ZStackUtils() {
	}

	static final String asHex(final int aByte, final boolean prepend0x) {
		final StringBuilder builder = new StringBuilder(prepend0x ? "0x" : "");
		
		final String currentByteString = Integer.toHexString(aByte);
		
		if (currentByteString.length() % 2 == 1) {
			builder.append("0");
		}
		
		builder.append(currentByteString);
		
		return builder.toString();
	}
	
	static final String asHex(final int aByte) {
		return asHex(aByte, true);
	}

	/**
	 * Represents the given frame as a string.
	 * 
	 * @param 		frame		The frame.
	 * 	
	 * @return		A string representation of the frame.
	 */
	static final String asString(final int[] frame) {
		final StringBuilder builder = new StringBuilder();
		
		int i = 0;
		
		while (i < frame.length) {
			builder.append("0x");
			
			final String currentByteString = Integer.toHexString(frame[i] & 0xFF);
			
			if (currentByteString.length() == 1) {
				builder.append("0");
			}
			
			builder.append(currentByteString);
			
			if (i < frame.length - 1) {
				builder.append(" ");
			}
			
			i++;
		}
		
		return builder.toString();
	}
}
