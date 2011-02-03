package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Window;
import java.io.IOException;
import java.util.List;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.SwitchEvent;

final class DimmerChannelEventDialog extends ChannelEventDialog implements DimmerChannelStateChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The dimmer module. */
	private final DimmerModule module;
	
	/** The channel number. */
	private final int channelNumber;
	
	DimmerChannelEventDialog(final Window parent, final DimmerModule module, final int channelNumber) {
		super(parent, module.getDimmerConfiguration().getDimmerChannelConfiguration(channelNumber).getName(), module.getId(), channelNumber);
		
		this.module = module;
		this.channelNumber = channelNumber;
		module.addChannelStateListener(this);
	}

	
	@Override
	String getDialogTitle() {
		return this.module.getDimmerConfiguration().getDimmerChannelConfiguration(this.channelNumber).getName();
	}

	@Override
	final List<SwitchEvent> getEvents() throws IOException {
		return this.module.getSwitchEvents(this.channelNumber, null, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void inputChannelStateChanged(final int channelNumber, final ChannelState newState) {
		// Not interested in these.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState, final int percentage) {
		if (channelNumber == this.channelNumber) {
			this.getTableModel().clear();
			
			try {
				this.getTableModel().addAll(this.getEvents());
				this.getTableModel().fireTableDataChanged();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
