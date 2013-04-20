package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.ros.node.ConnectedNode;

import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.widget.ArrayAdapter;
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
	
	
	public final String ROS_TOPICS_CONFIG_FILE = "ros_topics.hctrlconf";
	private int currentIDforWidget = 0;
	private DragLayer dragLayer;
	
	private ConnectedNode connectedNode;
	
	public MapWidgetRegistry(Context context, DragLayer dragLayer, SharedPreferences mPrefs) {
		this.context = context;
		this.mPrefs = mPrefs;
		this.dragLayer = dragLayer;
			
		//read properties from local SharedPrefernces file
		HashMap<String, Set<String>> widgets = readWidgetsFromSharedPrefs(mPrefs);
		if (widgets.keySet().isEmpty()) {		
			//read properties from default config file
			File preconf = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator + ROS_TOPICS_CONFIG_FILE);
			if (preconf.exists()) {
				Properties prop = new Properties();
				try {
					prop.load(new BufferedInputStream(new FileInputStream(preconf)));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				widgets = readWidgetsFromDefaultWidgetsFile(prop);
			}
		}
		
		//create widgets
		
		for (String topic : widgets.keySet()) {
			for (String widgetType : widgets.get(topic)) {
				createWidget(WidgetType.valueOf(widgetType), topic);
			}
		}
		
		
//		RosTextWidget textWidget = new RosTextWidget(0, context, "chatter", dragLayer);
//		allWidgets.add(textWidget);
//		
//		textWidget = new RosTextWidget(1, context, "/hanse/langer/topic/name", dragLayer);
//		allWidgets.add(textWidget);
		
		//create colored test widgets
		int hueSteps = 30;
		float[] hsv = new float[] {0,1,1};
		int color = Color.HSVToColor(hsv);
		
		int minSize = 100;
		int maxSize = 250;
		for (int i = 1; i < 20; i++) {
			MapWidget widget = new MapWidget(
					(int)(Math.random() * (maxSize - minSize) + minSize), 
					(int)(Math.random() * (maxSize - minSize) + minSize), 
					currentIDforWidget++, context, dragLayer);
			widget.getDebugPaint().setColor(color);
						
			allWidgets.add(widget);
			
			hsv[0] = (hsv[0] + hueSteps) % 360;
			color = Color.HSVToColor(hsv);
		}
	}
	
	public RosMapWidget createWidget(WidgetType widgetType, String topic) {
		RosMapWidget widget = null;
		if (widgetType == WidgetType.ROS_TEXT_WIDGET) {
			widget = new RosTextWidget(currentIDforWidget++, context, topic, dragLayer);
			allWidgets.add(widget);
			widget.setNode(connectedNode);
		}
		return widget;
	}
		
	private HashMap<String, Set<String>> readWidgetsFromSharedPrefs(SharedPreferences pref) {
		HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
		
		String topicsStr = pref.getString("rosTopics", "");
		if (topicsStr.isEmpty()) {
			return res;
		}
		
		Set<String> topics = createStringSetFromCommaString(topicsStr);
		for (String topic : topics) {
			Set<String> widgets = createStringSetFromCommaString(pref.getString("rosTopics_" + topic, ""));
			res.put(topic, widgets);
		}
		
		return res;
	}
	
	public void savePrefs(SharedPreferences.Editor ed) {
		//Topic1 = WIDGET_TYPE1, WIDGET_TYPE2, ...
		//Topic2 = WIDGET_TYPE2, WIDGET_TYPE3, ...
		HashMap<String, Set<String>> widgets = new HashMap<String, Set<String>>();
		for (MapWidget w : allWidgets) {
			if (w instanceof RosMapWidget) {
				String topic = ((RosMapWidget)w).getRosTopic();
				Set<String> widgetTypes = widgets.get(topic);
				if (widgetTypes == null) {
					widgetTypes = new HashSet<String>();
					widgets.put(topic, widgetTypes);
				}
				widgetTypes.add(((RosMapWidget)w).getWidgetType().name());				
			}
		}
		
		ed.putString("rosTopics", createCommaStringFromStringSet(widgets.keySet()));
		for (String topic : widgets.keySet()) {
			Set<String> widgetSet = widgets.get(topic);
			ed.putString("rosTopics_" + topic, createCommaStringFromStringSet(widgetSet));
 		}
	}
	
	private String createCommaStringFromStringSet(Set<String> stringSet) {
		String res = "";
		for (String t : stringSet) {
			if (!res.isEmpty()) {
				res += ",";
			}
			res += t;
		}
		return res;
	}
	
	private Set<String> createStringSetFromCommaString(String stringSet) {
		Set<String> res = new HashSet<String>();
		String[] strings = stringSet.split(",");
		for (String t : strings) {
			res.add(t.trim());
		}
		return res;
	}
	
	private HashMap<String, Set<String>> readWidgetsFromDefaultWidgetsFile(Properties prop) {
		HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
		for (Object o : prop.keySet()) {
			String s = (String) o;
			res.put(s, createStringSetFromCommaString(prop.getProperty(s)));
		}
		return res;
	}
	
	public List<MapWidget> getAllWidgets() {
		return allWidgets;
	}
	
	public void setNode(ConnectedNode node) {
		this.connectedNode = node;
		for (MapWidget w : allWidgets) {
			if (w instanceof RosMapWidget) {
				((RosMapWidget)w).setNode(node);
			}
		}
	}
	
	public static enum WidgetType {
		ROS_TEXT_WIDGET
	}
	
}
