package be.abollaert.domotics.zigbee.zstack;

interface ZStackResponse extends ZStackCommand {

	void parse(final byte[] payload);
}
