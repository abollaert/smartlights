package be.abollaert.smartlights.android.client;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import be.abollaert.domotics.light.drivers.tcp.TCPDriver;

/**
 * Main entry point of the application.
 * 
 * @author alex
 * 
 */
public final class MainMenuActivity extends Activity {

	/** The TCP client. */
	private TCPDriver driver;

	/** The multicast lock. */
	private MulticastLock multicastLock;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		
		System.out.println("Acquire lock.");
		
		this.multicastLock = wifiManager.createMulticastLock("smartlights");
		this.multicastLock.acquire();
		
		this.driver = new TCPDriver();
		
		try {
			this.driver.probe();
		} catch (IOException e) {
			e.printStackTrace();
		}
 
		System.out.println(this.driver.getAllDigitalModules().size());
		
		final ListView contentView = new ListView(this);
		this.setContentView(contentView);
		
		contentView.setAdapter(new ChannelListAdapter(this, this.driver));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onDestroy() {
		super.onDestroy();
	}
}