package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.Window;
import java.io.IOException;
import java.util.List;

import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.SwitchEvent;

final class DigitalChannelEventDialog extends ChannelEventDialog implements DigitalChannelStateChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** The dimmer module. */
	private final DigitalModule module;
	
	/** The channel number. */
	private final int channelNumber;
	
	DigitalChannelEventDialog(final Window parent, final DigitalModule module, final int channelNumber) {
		super(parent, module.getDigitalConfiguration().getDigitalChannelConfiguration(channelNumber).getName(), module.getId(), channelNumber);
		
		this.module = module;
		this.channelNumber = channelNumber;
		this.module.addChannelStateListener(this);
	}

	@Override
	String getDialogTitle() {
		return this.module.getDigitalConfiguration().getDigitalChannelConfiguration(this.channelNumber).getName();
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
	public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState) {
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
