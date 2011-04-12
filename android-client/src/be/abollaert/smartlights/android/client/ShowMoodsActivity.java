package be.abollaert.smartlights.android.client;

import java.io.IOException;
import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import be.abollaert.domotics.light.api.Mood;
import be.abollaert.smartlights.R;

/**
 * Activity to show all the moods.
 * 
 * @author alex
 */
public final class ShowMoodsActivity extends BaseActivity {
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle("Smartlights : Moods");
		
		try {
			final List<Mood> moods = getDriver().getAllMoods();
			
			final ListView listView = new ListView(this);
			listView.setPadding(15, 15, 15, 15);
			
			listView.setAdapter(new BaseAdapter() {
				@Override
				public final View getView(final int position, final View convertView, final ViewGroup parent) {
					Button button = null;
					
					if (convertView != null && convertView instanceof Button) {
						button = (Button)convertView;
					} else {
						button = new Button(ShowMoodsActivity.this);
						button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mood, 0, 0, 0);
					}
					
					final Mood mood = moods.get(position);
					button.setText(mood.getName());
					button.setTypeface(null, Typeface.BOLD);
					
					button.setOnClickListener(new View.OnClickListener() {	
						@Override
						public final void onClick(final View v) {
							try {
								mood.activate();
							} catch (IOException e) {
								showErrorDialog(e);
							}
						}
					});
					
					return button;
				}
				
				@Override
				public final long getItemId(final int position) {
					return moods.get(position).getId();
				}
				
				@Override
				public final Object getItem(final int position) {
					return moods.get(position);
				}
				
				/**
				 *
				 */
				@Override
				public final int getCount() {
					return moods.size();
				}
			});
			
			this.setContentView(listView);
		} catch (IOException e) {
			this.showErrorDialog(e);
		}
	}
}
