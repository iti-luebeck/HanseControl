package de.uniluebeck.iti.hanse.hansecontrol;

import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		
		MapWidget w1 = new MapWidget(getApplicationContext());
		MapWidget w2 = new MapWidget(getApplicationContext());
		MapWidget w3 = new MapWidget(getApplicationContext());
		
		LinearLayout widgetLayout = (LinearLayout)findViewById(R.id.widgetLayout);	
		
		w1.setLayoutParams(new LayoutParams(90, 90));
		
//		LayoutInflater inflater = (LayoutInflater) widgetLayout.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		inflater.infl
//		Button b1 = new Button(getApplicationContext());
		
//		widgetLayout.addView(b1);
//		widgetLayout.addView(w1);
//		widgetLayout.addView(w3);
		
		int hueSteps = 30;
		float[] hsv = new float[] {0,1,1};
		int color = Color.HSVToColor(hsv);
		
		for (int i = 0; i < 20; i++) {
			MapWidget widget = new MapWidget(getApplicationContext());
			widget.getDebugPaint().setColor(color);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
			params.setMargins(5, 0, 5, 8);
			widget.setLayoutParams(params);
			widgetLayout.addView(widget);
			
			hsv[0] = (hsv[0] + hueSteps) % 360;
			color = Color.HSVToColor(hsv);
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}

}
