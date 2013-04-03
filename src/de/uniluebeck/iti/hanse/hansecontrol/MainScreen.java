package de.uniluebeck.iti.hanse.hansecontrol;

import de.uniluebeck.iti.hanse.hansecontrol.views.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

public class MainScreen extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_screen);
		
		MapWidget w1 = new MapWidget(getApplicationContext());
		MapWidget w2 = new MapWidget(getApplicationContext());
		MapWidget w3 = new MapWidget(getApplicationContext());
		
		final WidgetLayer mapViewer = (WidgetLayer) findViewById(R.id.mapViewer1);
		final LinearLayout widgetLayout = (LinearLayout) findViewById(R.id.widgetLayout);	
		final HorizontalScrollView widgetLayoutScroll = (HorizontalScrollView) findViewById(R.id.horizontalScrollView1);
		final DragLayer dragLayer = (DragLayer) findViewById(R.id.dragLayer1);
		
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
			MapWidget widget = new MapWidget(getApplicationContext());// {
//				@Override
//				public boolean onTouchEvent(MotionEvent event) {
//					//test: remove widget from list
//					
//					//turns scrolling off
////					widgetLayout.requestDisallowInterceptTouchEvent(true);
//					
//					Log.w("touchlog", String.format("TestMapwidget: x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
//							event.getAction(), event.getActionMasked()));
//					
////					if (event.getActionMasked() != MotionEvent.ACTION_MOVE) {
////						widgetLayoutScroll.setOnTouchListener(null);
////					}
//					
//					if (event.getY() < 0) { // TODO Better: mapwidget knows when its pushed up a short way, implement MapWidget.getIsDraggedUp() method
//						widgetLayout.removeView(this);
////						final MapWidget thisWidget = this;
////						widgetLayoutScroll.setOnTouchListener(new OnTouchListener() {
////							
////							@Override
////							public boolean onTouch(View v, MotionEvent event) {
////								//TODO dragging logic for widget
////								
////								//compute offset
////								int[] widgetLayoutPos = new int[2];
////								widgetLayout.getLocationOnScreen(widgetLayoutPos);
////								
////								int[] mapViewerPos = new int[2];
////								mapViewer.getLocationOnScreen(mapViewerPos);
////								
//////								event.offsetLocation()
////								
////								Log.w("touchtest", String.format("widgetLayout pos: x: %d y: %d", widgetLayoutPos[0], widgetLayoutPos[1]));
////								return true;
////							}
////						});
//					}
//					
////					if(event.getActionMasked() == MotionEvent.ACTION_OUTSIDE) {
////						widgetLayout.removeView(this);
////					}
////					return super.onTouchEvent(event);
//					return true;
//				}
//			};
			widget.getDebugPaint().setColor(color);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(85, 85);
			params.setMargins(5, 0, 5, 8);
			widget.setLayoutParams(params);
			widget.setDragLayer(dragLayer);
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
