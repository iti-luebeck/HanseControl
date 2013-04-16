package de.uniluebeck.iti.hanse.hansecontrol;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import android.os.Environment;
import android.util.Log;

public class MapManager {
	
	private static final String MAPS_DIR = "HanseControl_data";
	private static final String MAPCONFIG_EXTENSION = ".hctrlmap";
	
	List<MapManager.Map> maps = new LinkedList<MapManager.Map>();
	
	private static MapManager instance = new MapManager();
	
	public static MapManager getInstance() {
		return instance;
	}
	
	public String getMapsDir() {
		File extStorage = Environment.getExternalStorageDirectory();
		return extStorage.getAbsolutePath() + File.separator + MAPS_DIR;
	}
	
	public MapManager() {
//		Log.d("mapmanager", extStorage.getAbsolutePath() + File.separator + MAPS_DIR);
		File mapsDir = new File(getMapsDir());
		if (!mapsDir.exists()) {
			Log.e("MapManager", String.format("Maps folder '%s' was not found on external storage!", MAPS_DIR));
			return;
		}
		
		//find all map config files
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
		
		public Map(File hctrlmapFile) throws FileNotFoundException, IOException {
			Properties prop = new Properties();
			prop.load(new BufferedInputStream(new FileInputStream(hctrlmapFile)));
			name = prop.getProperty("name");
			imagePath = hctrlmapFile.getParent() + File.separator + prop.getProperty("filepath");
			configPath = hctrlmapFile.getAbsolutePath();
		}
		
		public String getName() {
			return name;
		}
		
		public String getConfigPath() {
			return configPath;
		}
		
		public String getImagePath() {
			return imagePath;
		}
	}
}
