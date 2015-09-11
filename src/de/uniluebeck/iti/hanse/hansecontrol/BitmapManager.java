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
