package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

public class MapWidgetRegistry {
	List<MapWidget> allWidgets = new LinkedList<MapWidget>();
	Context context;
	
	public MapWidgetRegistry(Context context, DragLayer dragLayer) {
		this.context = context;
		
		//create colored test widgets
		int hueSteps = 30;
		float[] hsv = new float[] {0,1,1};
		int color = Color.HSVToColor(hsv);
		
		for (int i = 0; i < 20; i++) {
			MapWidget widget = new MapWidget(i, context);
			widget.getDebugPaint().setColor(color);
			//assign unique widget ID
			widget.setId(i); //TODO widget Registry class?
			
			widget.setDragLayer(dragLayer);
			
			allWidgets.add(widget);
			
			hsv[0] = (hsv[0] + hueSteps) % 360;
			color = Color.HSVToColor(hsv);
		}
	}
	
	public List<MapWidget> getAllWidgets() {
		return allWidgets;
	}
	
}
