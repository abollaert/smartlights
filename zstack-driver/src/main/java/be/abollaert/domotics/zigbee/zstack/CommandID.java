package be.abollaert.domotics.zigbee.zstack;

final class CommandID {

	/** Prevent instantiation. */
	private CommandID() {
	}
	
	static final int SYS_VERSION = 0x2102;
	static final int SYS_VERSION_RESPONSE = 0x6102;
	
	static final int ZB_GET_DEVICE_INFO = 0x2606;
	static final int ZB_GET_DEVICE_INFO_RESPONSE = 0x6606;
	
	static final int SYSTEM_RESET = 0x4100;
	static final int SYSTEM_RESET_IND = 0x4180;
	
	static final int AF_INCOMING_MESSAGE = 0x4481;
	
	static final int ZDO_END_DEVICE_ANNCE_IND = 0x4593;
	
	static final int ZDO_SIMPLE_DESC_REQ = 0x2504;
	
	static final int AF_DATA_REQUEST = 0x2401;
	
}
