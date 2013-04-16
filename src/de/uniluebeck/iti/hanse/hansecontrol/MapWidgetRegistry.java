package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.LinearLayout;

/**
 * In this class all available widgets must be instantiated.
 * 
 * @author Stefan Hueske
 */
public class MapWidgetRegistry {
	List<MapWidget> allWidgets = new LinkedList<MapWidget>();
	Context context;
	
	SharedPreferences mPrefs;
	
	public MapWidgetRegistry(Context context, DragLayer dragLayer, SharedPreferences mPrefs) {
		this.context = context;
		this.mPrefs = mPrefs;
		
		RosTextWidget textWidget = new RosTextWidget(0, context, "chatter", dragLayer);
		allWidgets.add(textWidget);
		
		textWidget = new RosTextWidget(1, context, "/hanse/langer/topic/name", dragLayer);
		allWidgets.add(textWidget);
		
		//create colored test widgets
		int hueSteps = 30;
		float[] hsv = new float[] {0,1,1};
		int color = Color.HSVToColor(hsv);
		
		int minSize = 100;
		int maxSize = 250;
		for (int i = 2; i < 20; i++) {
			MapWidget widget = new MapWidget(
					(int)(Math.random() * (maxSize - minSize) + minSize), 
					(int)(Math.random() * (maxSize - minSize) + minSize), 
					i, context, dragLayer);
			widget.getDebugPaint().setColor(color);
			//assign unique widget ID
			widget.setId(i); //TODO widget Registry class?
						
			allWidgets.add(widget);
			
			hsv[0] = (hsv[0] + hueSteps) % 360;
			color = Color.HSVToColor(hsv);
		}
	}
	
	public List<MapWidget> getAllWidgets() {
		return allWidgets;
	}
	
	public void setNode(ConnectedNode node) {
		for (MapWidget w : allWidgets) {
			if (w instanceof RosMapWidget) {
				((RosMapWidget)w).setNode(node);
			}
		}
	}
	
}
