package be.abollaert.domotics.light.tools.serialdebug;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.SwitchEvent;

/**
 * Channel events.
 * 
 * @author alex
 */
abstract class ChannelEventDialog extends JDialog {

	/** */
	private static final long serialVersionUID = 1L;
	
	private final int channelNumber;
	
	private final JButton btnFetch = new JButton(new FetchEventsAction());
	
	/** The table with events. */
	private final JTable tblEvents = new JTable(new EventTableModel());
	
	/**
	 * Creates a new dialog.
	 * 
	 * @param	parent				The parent window.
	 * @param	channelName			The channel name.
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 */
	ChannelEventDialog(final Window parent, final String channelName, final int moduleId, final int channelNumber) {
		super(parent);
		
		this.channelNumber = channelNumber;
		
		final StringBuilder titleBuilder = new StringBuilder("Switch events for channel [");
		
		if (channelName != null) {
			titleBuilder.append(channelName).append("], [");
		}
		
		titleBuilder.append("channel ").append(channelNumber).append(" on module ").append(moduleId).append("]");
		
		this.setTitle(titleBuilder.toString());
		
		final JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.btnFetch);
		
		this.setLayout(new BorderLayout());
		this.add(buttonPanel, BorderLayout.NORTH);
		this.add(new JScrollPane(this.tblEvents), BorderLayout.CENTER);
		
		this.pack();
		this.setLocationRelativeTo(parent);
	}
	
	/**
	 * Set the dialog title.
	 * 
	 * @param 	channelName			The channel name.
	 * @param 	moduleId			The module ID.
	 * @param 	channelNumber		The channel number.
	 */
	private final void setTitle(final String channelName, final int moduleId, final int channelNumber) {
		final StringBuilder builder = new StringBuilder("Channel events for channel ");
		
		if (channelName != null) {
			builder.append("[").append(channelName).append("] ");
		}
		
		builder.append(" (channel number [").append(channelNumber).append("] on module [").append(moduleId).append("])");
		
		this.setTitle(builder.toString());
	}
	
	final EventTableModel getTableModel() {
		return (EventTableModel)this.tblEvents.getModel();
	}
	
	private final class FetchEventsAction extends AbstractAction {
		
		private FetchEventsAction() {
			super("Load events");
		}

		@Override
		public final void actionPerformed(final ActionEvent event) {
			try {
				final List<SwitchEvent> events = getEvents();
				getTableModel().clear();
				getTableModel().addAll(events);
				getTableModel().fireTableDataChanged();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	final class EventTableModel extends AbstractTableModel {

		private static final int COLUMN_TIMESTAMP = 0;
		
		private static final int COLUMN_STATE = 1;
		
		/** The backing store. */
		private List<SwitchEvent> backingStore = new ArrayList<SwitchEvent>();
		
		@Override
		public final int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return backingStore.size();
		}

		@Override
		public final Object getValueAt(final int rowIndex, final int columnIndex) {
			if (rowIndex < backingStore.size()) {
				final SwitchEvent event = this.backingStore.get(rowIndex);
				
				switch (columnIndex) {
					case COLUMN_TIMESTAMP:
						return event.getTimestamp();
					case COLUMN_STATE:
						return event.isOn() ? "on" : "off";
					default:
						return null;
				}
			}
			
			return null;
		}
		
		final void clear() {
			this.backingStore.clear();
		}
		
		final void addAll(final List<SwitchEvent> events) {
			this.backingStore.addAll(events);
		}
	}
	
	/**
	 * Returns the list of events.
	 * 
	 * @return	The list of events.
	 */
	abstract List<SwitchEvent> getEvents() throws IOException;
	
	/**
	 * Returns the channel name.
	 * 
	 * @return	The channel name.
	 */
	abstract String getDialogTitle();
}
