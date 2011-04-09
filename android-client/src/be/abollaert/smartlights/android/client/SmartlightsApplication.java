package be.abollaert.smartlights.android.client;

import java.io.IOException;

import be.abollaert.domotics.light.api.Driver;
import be.abollaert.domotics.light.drivers.tcp.TCPDriver;
import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

public final class SmartlightsApplication extends Application {

	/** The multicast lock. */
	private MulticastLock multicastLock;
	
	/** The driver. */
	private Driver driver;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate() {
		super.onCreate();
		
		final WifiManager wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
		
		this.multicastLock = wifiManager.createMulticastLock("smartlights");
		this.multicastLock.acquire();
		
		this.driver = new TCPDriver();
		
		try {
			this.driver.probe();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public final Driver getDriver() {
		return this.driver;
	}
}
