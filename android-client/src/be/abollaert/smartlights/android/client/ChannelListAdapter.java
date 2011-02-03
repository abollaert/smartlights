package be.abollaert.smartlights.android.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;

import android.app.Activity;
import android.location.Address;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;
import be.abollaert.domotics.light.api.DimmerChannelStateChangeListener;
import be.abollaert.domotics.light.api.DimmerInputChannelConfiguration;
import be.abollaert.domotics.light.api.DimmerModule;
import be.abollaert.domotics.light.api.Driver;
import be.abollaert.smartlights.R;

/**
 * The adapter we use for displaying the list of channels.
 * 
 * @author alex
 *
 */
final class ChannelListAdapter extends BaseAdapter {
	
	/**
	 * Adapts a channel to fit in the list.
	 * 
	 * @author alex
	 *
	 * @param <M>	The type of module.
	 */
	private abstract class ChannelAdapter<M> implements Comparable<ChannelAdapter<M>> {
		
		/** The name. */
		private final String name;
		
		/** The module. */
		private final M module;
		
		/** The channel number. */
		private final int channelNumber;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 	name				The name.
		 * @param 	module				The module.
		 * @param 	channelNumber		The channel number.
		 */
		ChannelAdapter(final String name, final M module, final int channelNumber) {
			this.name = name;
			this.module = module;
			this.channelNumber = channelNumber;
		}
		
		/**
		 * Returns the module.
		 * 
		 * @return	The module.
		 */
		final M getModule() {
			return this.module;
		}
		
		/**
		 * Returns the channel name.
		 * 
		 * @return	The channel name.
		 */
		final String getChannelName() {
			return this.name;
		}
		
		/**
		 * Returns the channel number.
		 * 	
		 * @return	The channel number.
		 */
		final int getChannelNumber() {
			return this.channelNumber;
		}
		
		/**
		 * Creates the view associated to the channel.
		 * 
		 * @return	The view associated to the channel.
		 */
		abstract View createView();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final int compareTo(ChannelAdapter<M> arg0) {
			return this.getChannelName().compareTo(arg0.name);
		}
	}
	
	private final class DigitalChannelAdapter extends ChannelAdapter<DigitalModule> implements DigitalChannelStateChangeListener {
		
		private CheckBox cbxSwitch;
		
		DigitalChannelAdapter(String name, DigitalModule module, int channelNumber) {
			super(name, module, channelNumber);
			
			module.addChannelStateListener(this);
		}

		@Override
		final View createView() {
			final RelativeLayout view = new RelativeLayout(context);
			
			final TextView nameView = new TextView(context);
			nameView.setText(this.getChannelName());
			nameView.setId(1);
			nameView.setTextSize(18);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(nameView, params);

			this.cbxSwitch = new CheckBox(context);
			this.cbxSwitch.setId(2);
			
			try {
				this.cbxSwitch.setChecked(this.getModule().getOutputChannelState(this.getChannelNumber()) == ChannelState.ON);
				this.cbxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						try {
							getModule().switchOutputChannel(getChannelNumber(), arg1 ? ChannelState.ON : ChannelState.OFF);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			
			view.addView(this.cbxSwitch, params);
			
			return view;
		}

		@Override
		public final void inputChannelStateChanged(final int channelNumber,final ChannelState newState) {
		}

		/**
		 * @param channelNumber
		 * @param newState
		 */
		@Override
		public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState) {
			System.out.println("Output state changed.");
			
			if (channelNumber == this.getChannelNumber()) {
				context.runOnUiThread(new Runnable() {
					@Override
					public final void run() {
						cbxSwitch.setChecked(newState == ChannelState.ON);
					}
				});
			}
		}
	}
	
	private final class DimmerChannelAdapter extends ChannelAdapter<DimmerModule> implements DimmerChannelStateChangeListener {
		
		private CheckBox cbxSwitch;
		
		/** The spinner. */
		private TextView percentage;
		
		DimmerChannelAdapter(String name, DimmerModule module, int channelNumber) {
			super(name, module, channelNumber);
			
			module.addChannelStateListener(this);
		}

		@Override
		final View createView() {
			final RelativeLayout view = new RelativeLayout(context);
			
			final TextView nameView = new TextView(context);
			nameView.setId(1);
			nameView.setText(this.getChannelName());
			nameView.setTextSize(18);
			
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(nameView, params);

			this.cbxSwitch = new CheckBox(context);
			this.cbxSwitch.setId(2);
			
			try {
				this.cbxSwitch.setChecked(this.getModule().getOutputChannelState(this.getChannelNumber()) == ChannelState.ON);
				this.cbxSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					@Override
					public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
						try {
							getModule().switchOutputChannel(getChannelNumber(), arg1 ? ChannelState.ON : ChannelState.OFF);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
				this.percentage = new TextView(context);
				this.percentage.setId(3);
				this.percentage.setText(String.valueOf(getModule().getDimmerPercentage(getChannelNumber())));
				this.percentage.setTextSize(18);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(this.cbxSwitch, params);
			
			final Button buttonMinus = new Button(context);
			buttonMinus.setText("-");
			buttonMinus.setId(4);
			buttonMinus.setTextSize(25);
			buttonMinus.setWidth(60);
			
			buttonMinus.setOnClickListener(new OnClickListener() {
				@Override
				public final void onClick(final View arg0) {
					final int currentPercentage = Integer.parseInt(String.valueOf(percentage.getText()));
					
					int targetPercentage = currentPercentage - 1;
					
					if (targetPercentage > 100) {
						targetPercentage = 100;
					} else if (targetPercentage < 0) {
						targetPercentage = 0;
					}
					
					try {
						getModule().dim(getChannelNumber(), (short)targetPercentage);
						percentage.setText(String.valueOf(targetPercentage));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF, this.cbxSwitch.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(buttonMinus, params);
			
			final Button buttonPlus = new Button(context);
			buttonPlus.setText("+");
			buttonPlus.setId(5);
			buttonPlus.setTextSize(25);
			buttonPlus.setWidth(60);
			
			buttonPlus.setOnClickListener(new OnClickListener() {
				@Override
				public final void onClick(final View arg0) {
					final int currentPercentage = Integer.parseInt(String.valueOf(percentage.getText()));
					
					int targetPercentage = currentPercentage + 1;
					
					if (targetPercentage > 100) {
						targetPercentage = 100;
					} else if (targetPercentage < 0) {
						targetPercentage = 0;
					}
					
					try {
						getModule().dim(getChannelNumber(), (short)targetPercentage);
						percentage.setText(String.valueOf(targetPercentage));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF, buttonMinus.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(buttonPlus, params);
			
			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.LEFT_OF, buttonPlus.getId());
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			view.addView(this.percentage, params);

			return view;
		}

		@Override
		public void inputChannelStateChanged(int channelNumber, ChannelState newState) {
		}

		@Override
		public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState, final int newPercentage) {
			if (channelNumber == this.getChannelNumber()) {
				context.runOnUiThread(new Runnable() {
					@Override
					public final void run() {
						percentage.setText(String.valueOf(newPercentage));
						cbxSwitch.setChecked(newState == ChannelState.ON);
					}
				});
			}
		}
	}
	
	/** The context. */
	private final Activity context;
	
	/** The driver that will be used. */
	private final List<ChannelAdapter<?>> channels = new ArrayList<ChannelAdapter<?>>();
	
	/**
	 * Create a new instance specifying the driver.
	 * 
	 * @param 	driver		The driver to use.
	 */
	ChannelListAdapter(final Activity context, final Driver driver) {
		this.context = context;
		
		for (final DigitalModule module : driver.getAllDigitalModules()) {
			try {
				for (int i = 0; i < module.getDigitalConfiguration().getNumberOfChannels(); i++) {
					final DigitalInputChannelConfiguration config = module.getDigitalConfiguration().getDigitalChannelConfiguration(i);
					
					if (config.getName() != null && !config.getName().trim().equals("")) {
						this.channels.add(new DigitalChannelAdapter(config.getName(), module, i));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (final DimmerModule module : driver.getAllDimmerModules()) {
			try {
				for (int i = 0; i < module.getDimmerConfiguration().getNumberOfChannels(); i++) {
					final DimmerInputChannelConfiguration config = module.getDimmerConfiguration().getDimmerChannelConfiguration(i);
					
					if (config.getName() != null && !config.getName().trim().equals("")) {
						this.channels.add(new DimmerChannelAdapter(config.getName(), module, i));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Collections.sort(this.channels);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final int getCount() {
		return this.channels.size();
	}

	/**
	 * @param arg0
	 * 
	 * @return
	 */
	@Override
	public final Object getItem(final int arg0) {
		return this.channels.get(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 */
	@Override
	public final long getItemId(int arg0) {
		return arg0;
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 */
	@Override
	public final View getView(final int arg0, final View convertView, final ViewGroup parent) {
		return this.channels.get(arg0).createView();
	}
}
