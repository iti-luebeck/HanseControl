package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;

import android.os.Environment;
import android.util.Log;

public class MapManager {
	
	public static final String MAPS_DIR = "HanseControl_data";
	public static final String MAPCONFIG_EXTENSION = ".hctrlmap";
	
	List<MapManager.Map> maps = new LinkedList<MapManager.Map>();
	private Map emptyMap;
	
	private static MapManager instance = new MapManager();
	
	public static MapManager getInstance() {
		return instance;
	}
	
	public String getMapsDir() {
		File extStorage = Environment.getExternalStorageDirectory();
		return extStorage.getAbsolutePath() + File.separator + MAPS_DIR;
	}
	
	public MapManager() {
		reloadMapData();
	}
	
	public void reloadMapData() {
		maps.clear();
//		Log.d("mapmanager", extStorage.getAbsolutePath() + File.separator + MAPS_DIR);
		File mapsDir = new File(getMapsDir());
		if (!mapsDir.exists()) {
			Log.e("MapManager", String.format("Maps folder '%s' was not found on external storage!", MAPS_DIR));
			return;
		}
		
		//find all maps in the data dir files
		for (File f : mapsDir.listFiles()) {
			if (f.getName().endsWith(MAPCONFIG_EXTENSION)) {
				Log.d("MapManager", "Found map config file: " + f.getAbsolutePath());
				try {
					Map map = new Map(f);
					maps.add(map);
					Log.d("MapManager", String.format("New Map added! Name: %s, Image: %s", 
							map.getName(), map.getConfigPath()));
				} catch(Exception e) {
					Log.e("MapManager", "Error while decoding map config file: " + f.getAbsolutePath(), e);
				}
			}
		}
		
		//add empty map
		try {
			this.emptyMap = new Map(null);
			maps.add(this.emptyMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<MapManager.Map> getMaps() {
		return maps;
	}
	
	public Map getMapFromConfigPath(String configPath) {
		for (Map m : maps) {
			if (m.getConfigPath().equals(configPath)) {
				return m;
			}
		}
		return null;
	}
	
	public void recycleAllMapImages() {
		for (Map m : maps) {
			BitmapManager.getInstance().recycleCachedImage(m.getImagePath());
		}
	}
	
	public static class Map {
		//TODO configuration data to locate pose on map
		String name;
		String configPath;
		String imagePath;
		
		//two points on the map and the pose to convert coordinates between them
		float x1_OnPose;
		float y1_OnPose;
		int x1_OnImage;
		int y1_OnImage;
		
		float x2_OnPose;
		float y2_OnPose;
		int x2_OnImage;
		int y2_OnImage;
		
		public Map() {
			name = "";
			configPath = "";
			imagePath = "";
		}
		
		public Map(File hctrlmapFile) throws FileNotFoundException, IOException {
			if (hctrlmapFile == null) {
				name = "Empty map";
				configPath = "";
				imagePath = "";
				return;
			}
			Properties prop = new Properties();
			prop.load(new BufferedInputStream(new FileInputStream(hctrlmapFile)));
			name = prop.getProperty("name");
			if (new File(prop.getProperty("filepath")).exists()) {
				//file path was absolute
				imagePath = prop.getProperty("filepath");
			} else {
				//file path was relative
				imagePath = hctrlmapFile.getParent() + File.separator + prop.getProperty("filepath");
			}
			configPath = hctrlmapFile.getAbsolutePath();
			
			try {
				x1_OnPose = Float.parseFloat(prop.getProperty("x1_OnPose"));
				y1_OnPose = Float.parseFloat(prop.getProperty("y1_OnPose"));
				x1_OnImage = Integer.parseInt(prop.getProperty("x1_OnImage"));
				y1_OnImage = Integer.parseInt(prop.getProperty("y1_OnImage"));
				
				x2_OnPose = Float.parseFloat(prop.getProperty("x2_OnPose"));
				y2_OnPose = Float.parseFloat(prop.getProperty("y2_OnPose"));
				x2_OnImage = Integer.parseInt(prop.getProperty("x2_OnImage"));
				y2_OnImage = Integer.parseInt(prop.getProperty("y2_OnImage"));				
			} catch (Exception e) {
				Log.e("MapManager.Map", "Error while parsing map-pose translation data!", e);
			}
		}

		public void writeConfigFile() throws Exception {
			Properties prop = new Properties();
			prop.put("name", name);
			prop.put("filepath", imagePath);
			
			prop.put("x1_OnPose", Float.toString(x1_OnPose));
			prop.put("y1_OnPose", Float.toString(y1_OnPose));
			prop.put("x1_OnImage", Integer.toString(x1_OnImage));
			prop.put("y1_OnImage", Integer.toString(y1_OnImage));
			
			prop.put("x2_OnPose", Float.toString(x2_OnPose));
			prop.put("y2_OnPose", Float.toString(y2_OnPose));
			prop.put("x2_OnImage", Integer.toString(x2_OnImage));
			prop.put("y2_OnImage", Integer.toString(y2_OnImage));
			
			prop.store(new BufferedOutputStream(new FileOutputStream(configPath)), 
					"Created with HanseControl App");
		}
		
		public synchronized String getName() {
			return name;
		}

		public synchronized void setName(String name) {
			this.name = name;
		}

		public synchronized String getConfigPath() {
			return configPath;
		}

		public synchronized void setConfigPath(String configPath) {
			this.configPath = configPath;
		}

		public synchronized String getImagePath() {
			return imagePath;
		}

		public synchronized void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}

		public synchronized float getX1_OnPose() {
			return x1_OnPose;
		}

		public synchronized void setX1_OnPose(float x1_OnPose) {
			this.x1_OnPose = x1_OnPose;
		}

		public synchronized float getY1_OnPose() {
			return y1_OnPose;
		}

		public synchronized void setY1_OnPose(float y1_OnPose) {
			this.y1_OnPose = y1_OnPose;
		}

		public synchronized int getX1_OnImage() {
			return x1_OnImage;
		}

		public synchronized void setX1_OnImage(int x1_OnImage) {
			this.x1_OnImage = x1_OnImage;
		}

		public synchronized int getY1_OnImage() {
			return y1_OnImage;
		}

		public synchronized void setY1_OnImage(int y1_OnImage) {
			this.y1_OnImage = y1_OnImage;
		}

		public synchronized float getX2_OnPose() {
			return x2_OnPose;
		}

		public synchronized void setX2_OnPose(float x2_OnPose) {
			this.x2_OnPose = x2_OnPose;
		}

		public synchronized float getY2_OnPose() {
			return y2_OnPose;
		}

		public synchronized void setY2_OnPose(float y2_OnPose) {
			this.y2_OnPose = y2_OnPose;
		}

		public synchronized int getX2_OnImage() {
			return x2_OnImage;
		}

		public synchronized void setX2_OnImage(int x2_OnImage) {
			this.x2_OnImage = x2_OnImage;
		}

		public synchronized int getY2_OnImage() {
			return y2_OnImage;
		}

		public synchronized void setY2_OnImage(int y2_OnImage) {
			this.y2_OnImage = y2_OnImage;
		}
	}

	public Map getEmptyMap() {
		return emptyMap;
	}
}
