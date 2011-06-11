package be.abollaert.domotics.zigbee.zstack;

final class CommandID {

	/** Prevent instantiation. */
	private CommandID() {
	}
	
	static final int SYS_VERSION = 0x2102;
	static final int SYS_VERSION_RESPONSE = 0x6102;
}
