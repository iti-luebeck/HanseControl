/*
* Copyright (c) 2015, Institute of Computer Engineering, University of Lübeck
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* 
* * Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
//package de.uniluebeck.iti.hanse.hansecontrol;
//
//import hanse_msgs.pressure;
//import hanse_msgs.sollSpeed;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Properties;
//import java.util.Set;
//
//import org.ros.node.ConnectedNode;
//
//import std_msgs.Float64;
//
//import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
//import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
//import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosImageWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosOrientationWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosPlotWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosSonarWidget;
//import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosTextWidget;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.graphics.Color;
//import android.os.Environment;
//import android.util.Log;
//import android.widget.ArrayAdapter;
//import android.widget.LinearLayout;
//
///**
// * In this class all available widgets must be instantiated.
// * 
// * @author Stefan Hueske
// */
//public class MapWidgetRegistry {
//	List<MapWidget> allWidgets = new LinkedList<MapWidget>();
//	Context context;
//	
//	SharedPreferences mPrefs;
//	
//	
//	public static final String WIDGETS_CONFIG_FILE = "widgets.hctrlconf";
//	private int currentIDforWidget = 0;
//	private DragLayer dragLayer;
//	
//	private ConnectedNode connectedNode;
//	MainScreenFragment mainScreenFragment;
//	
//	public MapWidgetRegistry(Context context, DragLayer dragLayer, SharedPreferences mPrefs, MainScreenFragment mainScreenFragment) {
//		this.context = context;
//		this.mPrefs = mPrefs;
//		this.dragLayer = dragLayer;
//		this.mainScreenFragment = mainScreenFragment;
//		
//		//read properties from local SharedPrefernces file
//		HashMap<String, Set<String>> widgets = new HashMap<String, Set<String>>();
////		if (widgets.keySet().isEmpty()) {		
//		
//		synchronized (WIDGETS_CONFIG_FILE) {	
//			//read properties from default config file
//			File conf = new File(Environment.getExternalStorageDirectory()
//					.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator + WIDGETS_CONFIG_FILE);
//			if (conf.exists()) {
//				Properties prop = new Properties();
//				try {
//					prop.load(new BufferedInputStream(new FileInputStream(conf)));
//					widgets = readWidgetsFromFile(prop);
//				} catch (Exception e) {
//					Log.e("mapwidgetregistry", "Error while loading widgets from file!", e);
//				}
//			}
//		}
////		}
//		
//		//create widgets
//		
//		for (String topic : widgets.keySet()) {
//			for (String widgetType : widgets.get(topic)) {
//				try {
//					createWidget(WidgetType.valueOf(widgetType), topic);
//				} catch (Exception e) {
//					Log.e("mapwidgetregistry", "Error while creating widget type " + widgetType + " with topic " + topic, e);
//				}
//			}
//		}
//		
//		
////		RosTextWidget textWidget = new RosTextWidget(0, context, "chatter", dragLayer);
////		allWidgets.add(textWidget);
////		
////		textWidget = new RosTextWidget(1, context, "/hanse/langer/topic/name", dragLayer);
////		allWidgets.add(textWidget);
//		
//		//create colored test widgets
////		int hueSteps = 30;
////		float[] hsv = new float[] {0,1,1};
////		int color = Color.HSVToColor(hsv);
////		
////		int minSize = 100;
////		int maxSize = 250;
////		for (int i = 1; i < 20; i++) {
////			MapWidget widget = new MapWidget(
////					(int)(Math.random() * (maxSize - minSize) + minSize), 
////					(int)(Math.random() * (maxSize - minSize) + minSize), 
////					currentIDforWidget++, context, dragLayer, this, mainScreenFragment);
////			widget.getDebugPaint().setColor(color);
////						
////			allWidgets.add(widget);
////			
////			hsv[0] = (hsv[0] + hueSteps) % 360;
////			color = Color.HSVToColor(hsv);
////		}
//	}
//	
//	public RosMapWidget createWidget(WidgetType widgetType, String topic) {
//		Log.d("textwidgeterrfind", "creating widget instance: " + widgetType + " : " + topic);
//		RosMapWidget widget = null;
//		if (widgetType == WidgetType.ROS_TEXT_WIDGET__STRING) {
//			widget = new RosTextWidget<std_msgs.String>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, std_msgs.String._TYPE, 
//					WidgetType.ROS_TEXT_WIDGET__STRING){
//				@Override
//				public String getStringFromMsg(std_msgs.String msg) {
//					return msg.getData();
//				}
//			};
//		} else if (widgetType == WidgetType.ROS_TEXT_WIDGET__PRESSURE) {
//			widget = new RosTextWidget<hanse_msgs.pressure>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, hanse_msgs.pressure._TYPE,
//					WidgetType.ROS_TEXT_WIDGET__PRESSURE){
//				@Override
//				public String getStringFromMsg(pressure msg) {
//					return Short.toString(msg.getData());
//				};
//			};
//		} else if (widgetType == WidgetType.ROS_TEXT_WIDGET__SOLLSPEED) {
//			widget = new RosTextWidget<hanse_msgs.sollSpeed>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, hanse_msgs.sollSpeed._TYPE,
//					WidgetType.ROS_TEXT_WIDGET__SOLLSPEED){
//				@Override
//				public String getStringFromMsg(sollSpeed msg) {
//					return Byte.toString(msg.getData());
//				}
//			};
//		} else if (widgetType == WidgetType.ROS_TEXT_WIDGET__FLOAT64) {
//			widget = new RosTextWidget<std_msgs.Float64>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, std_msgs.Float64._TYPE,
//					WidgetType.ROS_TEXT_WIDGET__FLOAT64){
//				@Override
//				public String getStringFromMsg(Float64 msg) {
//					return Double.toString(msg.getData());
//				};
//			};
//		} else if (widgetType == WidgetType.ROS_IMAGE_WIDGET) {
//			widget = new RosImageWidget(currentIDforWidget++, context, topic, dragLayer, this, mainScreenFragment);
//		} else if (widgetType == WidgetType.ROS_PLOT_WIDGET__PRESSURE) {
//			widget = new RosPlotWidget<hanse_msgs.pressure>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, WidgetType.ROS_PLOT_WIDGET__PRESSURE) {
//
//				@Override
//				public String getDataTypeString() {
//					return hanse_msgs.pressure._TYPE;
//				}
//
//				@Override
//				public float getValue(pressure msg) {
//					return msg.getData();
//				}
//			};
//		} else if (widgetType == WidgetType.ROS_PLOT_WIDGET__SOLLSPEED) {
//			widget = new RosPlotWidget<hanse_msgs.sollSpeed>(currentIDforWidget++, context, topic, 
//					dragLayer, this, mainScreenFragment, WidgetType.ROS_PLOT_WIDGET__SOLLSPEED) {
//
//				@Override
//				public String getDataTypeString() {
//					return hanse_msgs.sollSpeed._TYPE;
//				}
//
//				@Override
//				public float getValue(sollSpeed msg) {
//					return msg.getData();
//				}
//			};
//		} else if (widgetType == WidgetType.ROS_PLOT_WIDGET__FLOAT64) {
//			widget = new RosPlotWidget<std_msgs.Float64>(currentIDforWidget++, context, topic,
//					dragLayer, this, mainScreenFragment, WidgetType.ROS_PLOT_WIDGET__FLOAT64) {
//
//				@Override
//				public String getDataTypeString() {
//					return std_msgs.Float64._TYPE;
//				}
//
//				@Override
//				public float getValue(Float64 msg) {
//					return (float) msg.getData();
//				}
//			};
//		} else if (widgetType == WidgetType.ROS_SONAR_WIDGET) {
//			widget = new RosSonarWidget(currentIDforWidget++, context, topic, dragLayer, this, 
//					mainScreenFragment);
//		} else if (widgetType == WidgetType.ROS_ORIENTATION_WIDGET) {
//			widget = new RosOrientationWidget(currentIDforWidget++, context, topic, dragLayer, this, 
//					mainScreenFragment);
//		}
//		
//		allWidgets.add(widget);
//		widget.setNode(connectedNode);
//		return widget;
//	}
//		
////	private HashMap<String, Set<String>> readWidgetsFromSharedPrefs(SharedPreferences pref) {
////		HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
////		
////		String topicsStr = pref.getString("rosTopics", "");
////		if (topicsStr.isEmpty()) {
////			return res;
////		}
////		
////		Set<String> topics = createStringSetFromCommaString(topicsStr);
////		for (String topic : topics) {
////			Set<String> widgets = createStringSetFromCommaString(pref.getString("rosTopics_" + topic, ""));
////			res.put(topic, widgets);
////		}
////		
////		return res;
////	} 
//	
////	public void savePrefs(SharedPreferences.Editor ed) {
////		//Topic1 = WIDGET_TYPE1, WIDGET_TYPE2, ...
////		//Topic2 = WIDGET_TYPE2, WIDGET_TYPE3, ...
////		HashMap<String, Set<String>> widgets = new HashMap<String, Set<String>>();
////		for (MapWidget w : allWidgets) {
////			if (w instanceof RosMapWidget) {
////				String topic = ((RosMapWidget)w).getRosTopic();
////				Set<String> widgetTypes = widgets.get(topic);
////				if (widgetTypes == null) {
////					widgetTypes = new HashSet<String>();
////					widgets.put(topic, widgetTypes);
////				}
////				widgetTypes.add(((RosMapWidget)w).getWidgetType().name());				
////			}
////		}
////		
////		ed.putString("rosTopics", createCommaStringFromStringSet(widgets.keySet()));
////		for (String topic : widgets.keySet()) {
////			Set<String> widgetSet = widgets.get(topic);
////			ed.putString("rosTopics_" + topic, createCommaStringFromStringSet(widgetSet));
//// 		}
////	}
//	
//	public synchronized void saveWidgetsToFile() {
//		File conf = new File(Environment.getExternalStorageDirectory()
//				.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator + WIDGETS_CONFIG_FILE);
//		Properties prop = new Properties();
//		HashMap<String, Set<String>> widgets = new HashMap<String, Set<String>>();
//		for (MapWidget w : allWidgets) {
//			if (w instanceof RosMapWidget) {
//				String topic = ((RosMapWidget)w).getRosTopic();
//				Set<String> widgetTypes = widgets.get(topic);
//				if (widgetTypes == null) {
//					widgetTypes = new HashSet<String>();
//					widgets.put(topic, widgetTypes);
//				}
//				widgetTypes.add(((RosMapWidget)w).getWidgetType().name());				
//			}
//		}
//		
//		for (String topic : widgets.keySet()) {
//			Set<String> widgetSet = widgets.get(topic);
//			prop.put(topic, createCommaStringFromStringSet(widgetSet));
// 		}
//		
//		try {
//			prop.store(new BufferedOutputStream(new FileOutputStream(conf)), null);
//		} catch (Exception e) {
//			Log.e("mapwidgetregistry", "Error while saving widgets to file!", e);
//		}
//	}
//	
//	private String createCommaStringFromStringSet(Set<String> stringSet) {
//		String res = "";
//		for (String t : stringSet) {
//			if (!res.isEmpty()) {
//				res += ",";
//			}
//			res += t;
//		}
//		return res;
//	}
//	
//	private Set<String> createStringSetFromCommaString(String stringSet) {
//		Set<String> res = new HashSet<String>();
//		String[] strings = stringSet.split(",");
//		for (String t : strings) {
//			res.add(t.trim());
//		}
//		return res;
//	}
//	
//	private HashMap<String, Set<String>> readWidgetsFromFile(Properties prop) {
//		HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
//		for (Object o : prop.keySet()) {
//			String s = (String) o;
//			res.put(s, createStringSetFromCommaString(prop.getProperty(s)));
//		}
//		return res;
//	}
//	
//	public List<MapWidget> getAllWidgets() {
//		
//		return allWidgets;
//	}
//	
//	public void setNode(ConnectedNode node) {
//		this.connectedNode = node;
//		for (MapWidget w : allWidgets) {
//			if (w instanceof RosMapWidget) {
//				((RosMapWidget)w).setNode(node);
//			}
//		}
//	}
//	
//	public static enum WidgetType {
//		ROS_SONAR_WIDGET,
//		ROS_IMAGE_WIDGET, 
//		ROS_PLOT_WIDGET__PRESSURE, 
//		ROS_PLOT_WIDGET__SOLLSPEED, 
//		ROS_PLOT_WIDGET__FLOAT64,
//		ROS_ORIENTATION_WIDGET,
//		ROS_TEXT_WIDGET__STRING,
//		ROS_TEXT_WIDGET__PRESSURE,
//		ROS_TEXT_WIDGET__SOLLSPEED,
//		ROS_TEXT_WIDGET__FLOAT64
//	}
//	
//	public RosMapWidget getRosMapWidget(String topic, WidgetType widgetType) {
//		for (MapWidget w : allWidgets) {
//			if (w instanceof RosMapWidget) {
//				RosMapWidget rw = (RosMapWidget) w;
//				if (rw.getRosTopic().equals(topic) && rw.getWidgetType() == widgetType) {
//					return rw;
//				}
//			}
//		}
//		return null;
//	}
//
//	public void unsubscribeAll() {
//		for (MapWidget w : allWidgets) {
//			if (w instanceof RosMapWidget) {
//				RosMapWidget rw = (RosMapWidget) w;
//				rw.unsubscribe(connectedNode);
//			}
//		}
//	}
//}
