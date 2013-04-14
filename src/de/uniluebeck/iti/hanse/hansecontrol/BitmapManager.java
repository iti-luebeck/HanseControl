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
	HashMap<Integer, Bitmap> loadedBitmaps = new HashMap<Integer, Bitmap>();
	
	public synchronized static BitmapManager getInstance() {
		return instance;
	}
	
	public synchronized Bitmap getBitmap(Resources res, int id) {
		Bitmap bitmap = loadedBitmaps.get(id);
		if (bitmap == null || bitmap.isRecycled()) {
			bitmap = BitmapFactory.decodeResource(res, id);
			loadedBitmaps.put(id, bitmap);
		}
		return bitmap;
	}
	
	public synchronized void recycleAllBitmaps() {
		for (Bitmap bitmap : loadedBitmaps.values()) {
			bitmap.recycle();
		}
		loadedBitmaps.clear();
	}
}
