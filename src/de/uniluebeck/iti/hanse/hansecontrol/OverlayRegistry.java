package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.ros.node.ConnectedNode;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import de.uniluebeck.iti.hanse.hansecontrol.views.AbstractOverlay;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.PoseOverlay;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;


public class OverlayRegistry {
    
	private List<AbstractOverlay> allOverlays = new LinkedList<AbstractOverlay>();
    Context context;
    private ConnectedNode connectedNode;
    private MapSurface mapSurface;
    
    public static final String OVERLAY_CONFIG_FILE = "overlays.hctrlconf";
    
    public OverlayRegistry(Context context) {
    	this.context = context;
    	
    	HashMap<String, Set<String>> overlays = new HashMap<String, Set<String>>();
    	
    	//read overlay types from file
    	File conf = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator + OVERLAY_CONFIG_FILE);
		if (conf.exists()) {
			Properties prop = new Properties();
			try {
				prop.load(new BufferedInputStream(new FileInputStream(conf)));
				overlays = readOverlaysFromFile(prop);
			} catch (Exception e) {
				Log.e("overlayregistry", "Error while loading overlays from file!", e);
			}
		}
    	 
    	//create overlay instances
    	for (String topic : overlays.keySet()) {
    		for (String overlayType : overlays.get(topic)) {
    			createOverlay(OverlayType.valueOf(overlayType), topic);
    		}
    	}
    	
//    	PoseOverlay poseOverlay = new PoseOverlay(context, "/hanse/position/estimate");
//    	allOverlays.add(poseOverlay);
    }    

    public List<AbstractOverlay> getAllOverlays() {
        return allOverlays;
    }
    
    public static enum OverlayType {
		POSE_OVERLAY
	}
    
    public synchronized void setNode(ConnectedNode node) {
		this.connectedNode = node;
		for (AbstractOverlay overlay : allOverlays) {
			overlay.setNode(node);
		}
	}
    
    public synchronized void setMapSurface(MapSurface mapSurface) {
		this.mapSurface = mapSurface;
		for (AbstractOverlay overlay : allOverlays) {
			overlay.setMapSurface(mapSurface);
		}
	}
    
    public synchronized AbstractOverlay createOverlay(OverlayType overlayType, String topic) {
    	AbstractOverlay overlay = null;
    	if (overlayType == OverlayType.POSE_OVERLAY) {
    		overlay = new PoseOverlay(context, topic);
    	}
    	allOverlays.add(overlay);
		if (connectedNode != null) {
			overlay.setNode(connectedNode);
		}
		if (mapSurface != null) {
			overlay.setMapSurface(mapSurface);
		}
    	return overlay;
    }
    
    private HashMap<String, Set<String>> readOverlaysFromFile(Properties prop) {
		HashMap<String, Set<String>> res = new HashMap<String, Set<String>>();
		for (Object o : prop.keySet()) {
			String s = (String) o;
			res.put(s, createStringSetFromCommaString(prop.getProperty(s)));
		}
		return res;
	}
    
    public synchronized void saveOverlaysToFile() {
    	File conf = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + File.separator + MapManager.MAPS_DIR + File.separator + OVERLAY_CONFIG_FILE);
    	Properties prop = new Properties();
    	HashMap<String, Set<String>> overlays = new HashMap<String, Set<String>>();
    	for (AbstractOverlay overlay : allOverlays) {
			String topic = overlay.getRosTopic();
			Set<String> overlayTypes = overlays.get(topic);
			if (overlayTypes == null) {
				overlayTypes = new HashSet<String>();
				overlays.put(topic, overlayTypes);
			}
			overlayTypes.add(overlay.getOverlayType().name());
		}
    	
    	for (String topic : overlays.keySet()) {
    		Set<String> overlaySet = overlays.get(topic);
    		prop.put(topic, createCommaStringFromStringSet(overlaySet));
    	}
    	
    	try {
			prop.store(new BufferedOutputStream(new FileOutputStream(conf)), null);
		} catch (Exception e) {
			Log.e("overlayregistry", "Error while saving overlays to file!", e);
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
}
