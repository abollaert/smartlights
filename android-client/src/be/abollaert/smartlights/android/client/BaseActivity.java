package be.abollaert.smartlights.android.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import be.abollaert.domotics.light.api.Driver;

abstract class BaseActivity extends Activity {
	
	protected final Driver getDriver() {
		return ((SmartlightsApplication)this.getApplicationContext()).getDriver();
	}
	
	protected final void showErrorDialog(final Throwable exception) {
		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(exception.getMessage())
					 .setPositiveButton("Close", new DialogInterface.OnClickListener() {
						@Override
						public final void onClick(final DialogInterface dialog, final int which) {
							finish();
						}
					});
		
		final AlertDialog alert = dialogBuilder.create();
		alert.show();
	}
	
}
