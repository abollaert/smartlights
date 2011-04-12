package be.abollaert.smartlights.android.client;

import java.io.IOException;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import be.abollaert.smartlights.R;

public final class ExtraActivity extends BaseActivity {
	
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.extra);
		
		final Button btnAllOff = (Button)this.findViewById(R.id.btnAllOff);
		
		btnAllOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public final void onClick(final View v) {
				try {
					getDriver().allLightsOff();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	protected final void onDestroy() {
		super.onDestroy();
	}
}