package be.abollaert.smartlights.android.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import be.abollaert.domotics.light.api.ChannelState;
import be.abollaert.domotics.light.api.DigitalChannelStateChangeListener;
import be.abollaert.domotics.light.api.DigitalInputChannelConfiguration;
import be.abollaert.domotics.light.api.DigitalModule;

public final class DigitalChannelsActivity extends BaseActivity {
	
	private Map<DigitalModule, DigitalChannelStateChangeListener> listeners = new HashMap<DigitalModule, DigitalChannelStateChangeListener>();

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setTitle("Smartlights : Digital channels");
		
		final LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(15, 15, 15, 15);
		
		final Handler handler = new Handler() {
			public final void handleMessage(final Message msg) {
				final int channelNumber = msg.arg1;
				final DigitalModule module = (DigitalModule)msg.obj;
				final boolean state = (msg.arg2 != 0);
				
				final int boxId = module.getId() * 10 + channelNumber;
				final CheckBox checkBox = (CheckBox)findViewById(boxId);
				
				if (checkBox != null) {
					checkBox.setChecked(state);
				}
			}
		};
		
		try {
			for (final DigitalModule module : this.getDriver().getAllDigitalModules()) {
				final DigitalChannelStateChangeListener listener = new DigitalChannelStateChangeListener() {
					
					@Override
					public final void outputChannelStateChanged(final int channelNumber, final ChannelState newState) {
						final Message message = new Message();
						message.arg1 = channelNumber;
						message.obj = module;
						message.arg2 = (newState == ChannelState.ON ? 1 : 0);
						
						handler.sendMessage(message);
					}
					
					@Override
					public void inputChannelStateChanged(int channelNumber,
							ChannelState newState) {
					}
				};
				
				module.addChannelStateListener(listener);
				this.listeners.put(module, listener);
				
				for (int channelNumber = 0; channelNumber < module.getDigitalConfiguration().getNumberOfChannels(); channelNumber++) {
					final DigitalInputChannelConfiguration config = module.getDigitalConfiguration().getDigitalChannelConfiguration(channelNumber);
					
					if (config.getName() != null && !config.getName().trim().equals("")) {
						layout.addView(this.createRow(module, channelNumber));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.setContentView(layout);
	}
	
	
	@Override
	protected final void onDestroy() {
		super.onDestroy();
		
		for (final DigitalModule module : this.listeners.keySet()) {
			module.removeChannelStateListener(this.listeners.get(module));
		}
	}

	private final View createRow(final DigitalModule module, final int channelId) {
		final RelativeLayout layout = new RelativeLayout(this);
		layout.setPadding(5, 5, 5, 5);

		
		final CheckBox onOffBox = new CheckBox(this);
		
		try {
			onOffBox.setChecked(module.getOutputChannelState(channelId) == ChannelState.ON);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		onOffBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public final void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
				try {
					if (isChecked) {
						module.switchOutputChannel(channelId, ChannelState.ON);
					} else {
						module.switchOutputChannel(channelId, ChannelState.OFF);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		RelativeLayout.LayoutParams layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		onOffBox.setLayoutParams(layoutParameters);
		onOffBox.setId(module.getId() * 10 + channelId);
		
		final TextView nameView = new TextView(this);
		nameView.setText(module.getDigitalConfiguration().getDigitalChannelConfiguration(channelId).getName());
		nameView.setTypeface(null, Typeface.BOLD);
		
		layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParameters.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParameters.addRule(RelativeLayout.ALIGN_BASELINE, onOffBox.getId());
		
		nameView.setLayoutParams(layoutParameters);
		
		layout.addView(nameView);
		layout.addView(onOffBox);
		
		return layout;
	}

	
}
