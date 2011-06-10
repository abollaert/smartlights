package be.abollaert.domotics.zigbee.zstack;

interface ZStackRequest extends ZStackCommand {

	byte[] getPayload();
}
