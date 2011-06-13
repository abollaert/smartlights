package be.abollaert.domotics.zigbee.zstack;


/**
 * @author alex
 *
 */
public final class IncomingMessage {

	private final int groupId;
	
	private final int clusterId;
	
	private final int sourceAddress;
	
	private final int sourceEndPoint;
	
	private final int destEndPoint;
	
	private final boolean broadcast;
	
	private final int linkQuality;
	
	private final boolean securityUse;
	
	private final long timestamp;
	
	private final int transactionSequenceNumber;
	
	private final int[] payload;
	
	IncomingMessage(final int[] payload) {
		int i = 0;
		
		this.groupId = (payload[i++] << 8) + (payload[i++] & 0xFF);
		
		// Reversed due to backwards compat.
		this.clusterId = parseWord(payload, i);
		i += 2;
		
		this.sourceAddress = parseWord(payload, i);
		i += 2;
		
		this.sourceEndPoint = payload[i++];
		this.destEndPoint = payload[i++];
		this.broadcast = (payload[i++] == 0 ? false : true);
		this.linkQuality = payload[i++];
		this.securityUse = (payload[i++] == 0 ? false : true);
		
		this.timestamp = parseLong(payload, i);
		i += 4;
		
		this.transactionSequenceNumber = payload[i++];
		
		this.payload = new int[payload[i++]];
		System.arraycopy(payload, i, this.payload, 0, this.payload.length);
	}
	
	private int parseWord(final int[] payload, int index) {
		return (payload[index++] & 0xFF) + (payload[index++] << 8);
	}
	
	private final long parseLong(final int[] payload, int index) {
		return (long)payload[index++] + ((long)payload[index++] << 8) + ((long)payload[index++] << 16) + ((long)payload[index++] << 24);
	}
	
	public final int getGroupId() {
		return groupId;
	}

	public final int getClusterId() {
		return clusterId;
	}

	public final int getSourceAddress() {
		return sourceAddress;
	}

	public final int getSourceEndPoint() {
		return sourceEndPoint;
	}

	public final int getDestEndPoint() {
		return destEndPoint;
	}

	public final boolean isBroadcast() {
		return broadcast;
	}

	public final int getLinkQuality() {
		return linkQuality;
	}

	public final boolean isSecurityUse() {
		return securityUse;
	}

	public final long getTimestamp() {
		return timestamp;
	}

	public final int getTransactionSequenceNumber() {
		return transactionSequenceNumber;
	}

	public final int[] getPayload() {
		return payload;
	}
	
	public final String toString() {
		final StringBuilder builder = new StringBuilder("AF_INCOMING_MSG : Group ID [");
		builder.append(this.groupId).append("], ");
		builder.append("Cluster ID [").append(ZStackUtils.asHex(this.clusterId)).append("], ");
		builder.append("Source address [").append(ZStackUtils.asHex(this.sourceAddress)).append("], ");
		builder.append("Source endpoint [").append(ZStackUtils.asHex(this.sourceEndPoint)).append("], ");
		builder.append("Destination endpoint [").append(ZStackUtils.asHex(this.destEndPoint)).append("], ");
		builder.append("Broadcast[").append(this.broadcast).append("], ");
		builder.append("Link Quality [").append(this.linkQuality).append("], ");
		builder.append("Security use [").append(this.securityUse).append("], ");
		builder.append("Timestamp [").append(this.timestamp).append("], ");
		builder.append("Transaction sequence number [").append(this.transactionSequenceNumber).append("], ");
		builder.append("Payload : [").append(ZStackUtils.asString(this.payload)).append("]");
		
		return builder.toString();
	}
}
