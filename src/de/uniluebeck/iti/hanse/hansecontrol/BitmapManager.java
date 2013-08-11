package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.HashMap;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * This class manages the loading and caching of bitmaps.
 * 
 * @author Stefan Hueske
 */
public class BitmapManager {
	
	private static BitmapManager instance = new BitmapManager();
	private HashMap<Integer, Bitmap> loadedBitmaps_ID = new HashMap<Integer, Bitmap>();
	private HashMap<String, Bitmap> loadedBitmaps_PATH = new HashMap<String, Bitmap>();
	
	/**
	 * Returns the static instance.
	 * @return instance
	 */
	public synchronized static BitmapManager getInstance() {
		return instance;
	}
	
	/**
	 * Load Bitmap from resource ID.
	 * @param res Applications Resources instance
	 * @param id Resource ID
	 * @return Bitmap
	 */
	public synchronized Bitmap getBitmap(Resources res, int id) {
		Bitmap bitmap = loadedBitmaps_ID.get(id);
		if (bitmap == null || bitmap.isRecycled()) {
			bitmap = BitmapFactory.decodeResource(res, id);
			loadedBitmaps_ID.put(id, bitmap);
		}
		return bitmap;
	}
	
	/**
	 * Load Bitmap from path.
	 * @param path Absolute path of Bitmap file
	 * @return Bitmap
	 */
	public synchronized Bitmap getBitmap(String path) {
		Bitmap bitmap = loadedBitmaps_PATH.get(path);
		if (bitmap == null || bitmap.isRecycled()) {
			try {
				bitmap = BitmapFactory.decodeFile(path);
				loadedBitmaps_PATH.put(path, bitmap);
			} catch (OutOfMemoryError e) {
				recycleAllBitmaps();
				return getBitmap(path);
			}
		}
		return bitmap;
	}
	
	/**
	 * Recycles all currently cached Bitmaps
	 */
	public synchronized void recycleAllBitmaps() {
		for (Bitmap bitmap : loadedBitmaps_ID.values()) {
			bitmap.recycle();
		}
		loadedBitmaps_ID.clear();
		for (Bitmap bitmap : loadedBitmaps_PATH.values()) {
			bitmap.recycle();
		}
		loadedBitmaps_PATH.clear();
	}
	
	/**
	 * Recycles Bitmap previously loaded from path.
	 * @param path Path of Bitmap to recycle
	 */
	public synchronized void recycleCachedImage(String path) {
		Bitmap bitmap = loadedBitmaps_PATH.get(path);
		if (bitmap != null) {
			bitmap.recycle();
		}
	}
}
