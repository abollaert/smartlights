package be.abollaert.domotics.zigbee.zstack;

/**
 * @author alex
 */
public final class SysVersionRequest implements ZStackRequest {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final byte[] getPayload() {
		return new byte[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getCommandID() {
		return 0x2102;
	}
}
