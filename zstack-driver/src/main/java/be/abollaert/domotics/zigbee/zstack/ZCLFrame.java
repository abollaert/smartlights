package be.abollaert.domotics.zigbee.zstack;

/**
 * This is a Zigbee Cluster Library frame.
 * 
 * @author alex
 */
final class ZCLFrame {
	
	enum FrameType {
		PROFILE, 
		CLUSTER_SPECIFIC;
		
		private static final FrameType getFromFrameControl(final int frameControl) {
			final int typeId = (frameControl & 0xA0) >> 6;
			
			if (typeId == 0) {
				return PROFILE;
			} else if (typeId == 1) {
				return CLUSTER_SPECIFIC;
			} else {
				throw new IllegalStateException("Invalid frame type in ZCL header : [" + typeId + "]");
			}
		}
	}
	
	enum Direction {
		CLIENT_TO_SERVER, 
		SERVER_TO_CLIENT;
		
		private static final Direction getFromFrameControl(final int frameControl) {
			final int direction = (frameControl & 0x10) >> 4;
			
			if (direction == 1) {
				return SERVER_TO_CLIENT;
			} else if (direction == 0) {
				return CLIENT_TO_SERVER;
			} else {
				throw new IllegalStateException("Invalid direction in ZCL header : [" + direction + "]");
			}
		}
	}
	
	enum ZCLCommandID {
		READ_ATTRIBUTES(0x00),
		READ_ATTRIBUTES_RESPONSE(0x01),
		WRITE_ATTRIBUTES(0x02),
		WRITE_ATTRIBUTES_UNDIVIDED(0x03),
		WRITE_ATTRIBUTES_RESPONSE(0x04),
		WRITE_ATTRIBUTES_NO_RESPONSE(0x05),
		CONFIGURE_REPORTING(0x06),
		CONFIGURE_REPORTING_RESPONSE(0x07),
		READ_REPORTING_CONFIGURATION(0x08),
		READ_REPORTING_CONFIGURATION_RESPONSE(0x09),
		REPORT_ATTRIBUTES(0x0a),
		DEFAULT_RESPONSE(0x0b),
		DISCOVER_ATTRIBUTES(0x0c),
		DISCOVER_ATTRIBUTES_RESPONSE(0x0d),
		READ_ATTRIBUTES_STRUCTURED(0x0e),
		WRITE_ATTRIBUTES_STRUCTURED(0x0f),
		WRITE_ATTRIBUTES_STRUCTURED_RESPONSE(0x10);
		
		private final int commandId;
		
		private ZCLCommandID(final int commandId) {
			this.commandId = commandId;
		}
		
		private static final ZCLCommandID byId(final int id) {
			for (final ZCLCommandID commandID : ZCLCommandID.values()) {
				if (commandID.commandId == id) {
					return commandID;
				}
			}
			
			return null;
		}
	}
	
	private final FrameType frameType;
	
	private final boolean containsManufacturerSpecificData;
	
	private final Direction direction;
	
	private final boolean disableDefaultResponse;
	
	private final int manufacturerCode;
	
	private final int transactionSequenceNumber;
	
	private final ZCLCommandID commandID;
	
	private final int[] payload;
	
	ZCLFrame(final int[] data) {
		int i = 0;
		
		final int frameControl = data[i++];
		
		this.frameType = FrameType.getFromFrameControl(frameControl);
		this.containsManufacturerSpecificData = (frameControl & 0x40) != 0;
		this.direction = Direction.getFromFrameControl(frameControl);
		this.disableDefaultResponse = (frameControl & 0x04) != 0;
		
		if (this.containsManufacturerSpecificData) {
			this.manufacturerCode = (data[i++] << 8) + (data[i++] & 0xFF);
		} else {
			this.manufacturerCode = 0;
		}
		
		this.transactionSequenceNumber = data[i++] & 0xFF;
		this.commandID = ZCLCommandID.byId(data[i++]);
		
		this.payload = new int[data.length - i];
		System.arraycopy(data, i, this.payload, 0, this.payload.length);
	}
	
	public final String toString() {
		final StringBuilder builder = new StringBuilder("ZCL Frame : ");
		builder.append("Frame Type [").append(this.frameType).append("], ");
		builder.append("Direction [").append(this.direction).append("], ");
		builder.append("Disable default response [").append(this.disableDefaultResponse).append("], ");
		
		if (this.containsManufacturerSpecificData) {
			builder.append("Manufacturer code [").append(ZStackUtils.asHex(this.manufacturerCode)).append("], ");
		}
		
		builder.append("Transaction sequence number [").append(ZStackUtils.asHex(this.transactionSequenceNumber)).append("], ");
		builder.append("Command ID [").append(this.commandID).append("], ");
		builder.append("Payload [").append(ZStackUtils.asString(this.payload)).append("]");
		
		return builder.toString();
	}
	

	final FrameType getFrameType() {
		return frameType;
	}

	final boolean isContainsManufacturerSpecificData() {
		return containsManufacturerSpecificData;
	}

	final Direction getDirection() {
		return direction;
	}

	final boolean isDisableDefaultResponse() {
		return disableDefaultResponse;
	}

	final int getManufacturerCode() {
		return manufacturerCode;
	}

	final int getTransactionSequenceNumber() {
		return transactionSequenceNumber;
	}

	final ZCLCommandID getCommandID() {
		return commandID;
	}

	final int[] getPayload() {
		return payload;
	}
}
