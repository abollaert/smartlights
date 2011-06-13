package be.abollaert.domotics.zigbee.zstack;

import java.util.Arrays;

public final class IEEEAddress {

	private final int[] address;
	
	public IEEEAddress(final int[] address) {
		this.address = address;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(address);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IEEEAddress other = (IEEEAddress) obj;
		if (!Arrays.equals(address, other.address))
			return false;
		return true;
	}
	
	public final int[] asByteArray() {
		return this.address;
	}
}
