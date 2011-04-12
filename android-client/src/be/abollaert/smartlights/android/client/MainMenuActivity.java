package be.abollaert.smartlights.android.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import be.abollaert.smartlights.R;

/**
 * Main entry point of the application.
 * 
 * @author alex
 * 
 */
public final class MainMenuActivity extends BaseActivity {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Smartlights");
		this.setContentView(R.layout.main);
		this.setupButtonListeners();
	}
	
	private final void setupButtonListeners() {
		final Button moodsButton = (Button)this.findViewById(R.id.btnMoods);
		moodsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				startActivity(new Intent(MainMenuActivity.this, ShowMoodsActivity.class));
			}
		});
		
		final Button digitalChannelsButton = (Button)this.findViewById(R.id.btnDigitalChannels);
		digitalChannelsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				startActivity(new Intent(MainMenuActivity.this, DigitalChannelsActivity.class));
			}
		});
		
		final Button dimmerChannelsButton = (Button)this.findViewById(R.id.btnDimmerChannels);
		dimmerChannelsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				startActivity(new Intent(MainMenuActivity.this, DimmerChannelsActivity.class));
			}
		});
		
		final Button extraButton = (Button)this.findViewById(R.id.btnExtra);
		extraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				startActivity(new Intent(MainMenuActivity.this, ExtraActivity.class));
			}
		});
	}
}