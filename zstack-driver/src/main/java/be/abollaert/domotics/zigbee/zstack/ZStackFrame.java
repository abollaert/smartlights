package be.abollaert.domotics.zigbee.zstack;

final class ZStackFrame {
	
	/** The command ID. */
	private final int commandID;
	
	/** The payload. */
	private final int[] payload;
	
	ZStackFrame(final int commandID, final int[] payload) {
		this.commandID = commandID;
		this.payload = payload;
	}
	
	final int getCommandID() {
		return this.commandID;
	}
	
	final int[] getPayload() {
		return this.payload;
	}
	
	final int[] toWireFrame() {
		final int[] payload = this.getPayload();
		
		// SOF - LEN - CMD ID - PAYLOAD - FCS.
		final int frameSize = 1 + 1 + 2 + payload.length + 1;
		
		final int[] frame = new int[frameSize];
		frame[0] = (ZStackModuleImpl.SOF & 0xFF);
		frame[1] = (payload.length & 0xFF);
		
		final int commandID = this.getCommandID();
		frame[2] = ((commandID >> 8) & 0xFF);
		frame[3] = (commandID & 0xFF);
		
		for (int i = 0; i < payload.length; i++) {
			frame[4 + i] = payload[i];
		}
		
		final int fcs = ZStackModuleImpl.calculateFCS(frame, 2, frame.length - 3);
		frame[frameSize - 1] = (byte)(fcs & 0xFF);
		
		return frame;
	}
}
