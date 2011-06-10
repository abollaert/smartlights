package be.abollaert.domotics.zigbee.zstack;

public final class SysVersionResponse implements ZStackResponse {

	private int transportRevision;
	
	private int product;
	
	private int majorRelease;
	
	private int minorRelease;
	
	private int hardwareRevision;
	
	@Override
	public final int getCommandID() {
		return 0x6102;
	}

	@Override
	public final void parse(byte[] payload) {
		this.transportRevision = payload[0] & 0xFF;
		this.product = payload[1] & 0xFF;
		this.majorRelease = payload[2] & 0xFF;
		this.minorRelease = payload[3] & 0xFF;
		this.hardwareRevision = payload[4] & 0xFF;
	}

	public int getTransportRevision() {
		return transportRevision;
	}

	public int getProduct() {
		return product;
	}

	public int getMajorRelease() {
		return majorRelease;
	}

	public int getMinorRelease() {
		return minorRelease;
	}

	public int getHardwareRevision() {
		return hardwareRevision;
	}
}
