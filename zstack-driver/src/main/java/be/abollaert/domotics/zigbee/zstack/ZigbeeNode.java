package be.abollaert.domotics.zigbee.zstack;

abstract class ZigbeeNode {

	private final int shortAddress;
	
	private final IEEEAddress ieeeAddress;
	
	private final int endPoint;
	
	private final ZStackModuleImpl module;
	
	ZigbeeNode(final int shortAddress, final IEEEAddress ieeeAddress, final int endPoint, final ZStackModuleImpl module) {
		this.shortAddress = shortAddress;
		this.ieeeAddress = ieeeAddress;
		this.endPoint = endPoint;
		this.module = module;
	}
	
	abstract void reportAttributes(final int[] report);
}
