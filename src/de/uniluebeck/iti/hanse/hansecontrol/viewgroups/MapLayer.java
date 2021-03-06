/*
* Copyright (c) 2015, Institute of Computer Engineering, University of L�beck
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
package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
//import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MapSurface;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

/**
 * On this surface view all contents of the map will be drawn.
 * 
 * @author Stefan Hueske
 */
public class MapLayer extends SurfaceView implements SurfaceHolder.Callback {
	
//	GestureDetector gestureDetector;
	
//	public static Bitmap testimage;
	private MapSurface mapSurface;
	
	GestureDetector gestureDetector;
	ScaleGestureDetector scaleGestureDetector;
	ScheduledFuture surfaceDrawingFuture;
	Paint surfaceBackgroundPaint = new Paint();
	public static final String MAP_LAYER_PREFIX = "MapLayer-";
	
//	private SharedPreferences mPrefs;
//	private MainScreenFragment mainScreenFragment;
//	Runnable loadMapSurfacePrefsRunnable;
	
	private boolean mapPositionRestoredFlag = true;
	
	private MapLayerListener mapLayerListener;
	
	private OnLongPressListener onLongPressListener;
	
	public MapLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		getHolder().addCallback(this);
//		testimage = BitmapFactory.decodeResource(getResources(), R.drawable.test_tile);
//		testimage = BitmapManager.getInstance().getBitmap(getResources(), R.drawable.test_tile);
		gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				mapSurface.translate(-distanceX, -distanceY);
				scheduleSurfaceDrawing();
				return true;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				if (onLongPressListener != null) {
					onLongPressListener.onLongPress(e);
				}
			}
			
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				mapSurface.zoom(e.getX(), e.getY(), 1.5f);
				scheduleSurfaceDrawing();
				return true;
			}
		});
		
		scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				mapSurface.startPinchToZoom(detector.getFocusX(), detector.getFocusY());
				return true;
			}
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
//				Log.d("multitouch", String.format("PinchToZoom: focusX=%f, factor=%f", detector.getFocusX(), detector.getScaleFactor()));
				mapSurface.zoom(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
				scheduleSurfaceDrawing();
				return true;
			}
		});
//		mapSurface = new MapSurface(testimage);
//		mapSurface.setX(0);
//		mapSurface.setY(0);
//		mapSurface.setWidth(700);
//		mapSurface.setHeight(500);
		surfaceBackgroundPaint.setColor(Color.LTGRAY);
		surfaceBackgroundPaint.setStrokeWidth(0);
		mapSurface = new MapSurface();
//		Log.d("errfind", "MapLayer.init()" + getWidth());
	}

	private void scheduleSurfaceDrawing() {
		if (surfaceDrawingFuture != null) {
			surfaceDrawingFuture.cancel(true);
		}
		try {
			surfaceDrawingFuture = MainScreen.getExecutorService().schedule(new Runnable() {
				
				@Override
				public void run() {
					try{
	//					Log.d("tt", "drawing!");
						Canvas canvas = getHolder().lockCanvas();
	//					canvas.drawBitmap(testimage, null, new Rect(0, 0, 300, 300), null);
	//					mapSurface.scaleToViewport(getWidth(), getHeight());
						if (canvas == null) {
							Log.e("Errfind", "canvas is null!");
						}
						canvas.drawRect(new Rect(0,0,getWidth(), getHeight()), surfaceBackgroundPaint);
						mapSurface.draw(canvas);
	//										canvas.drawLine(0, 0, 300, 300, paint);
						
						getHolder().unlockCanvasAndPost(canvas);
						
//						Thread.sleep(0);
						
//					} catch (InterruptedException e) {
					} catch (Exception e) {
						Log.e("MapSurface", "Scheduled drawing throwed exception! ", e);
						try {
							getHolder().unlockCanvasAndPost(null);							
						} catch (Exception e1) { }
					}
				}
			}, 0, TimeUnit.MILLISECONDS);
		} catch (RejectedExecutionException e) {
			Log.e("MapLayer", "scheduleSurfaceDrawing() execution was rejected by static executor service");
		}
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
		Log.e("errfind", "MapLayer.surfaceCreated()" + getWidth() + "ID: " + this.hashCode());
//		MapSurface mapSurface = new MapSurface(testimage);		
		if (!mapPositionRestoredFlag) {
			
			mapSurface.scaleToViewport(getWidth(), getHeight());
		}
		scheduleSurfaceDrawing();
		
		if (mapLayerListener != null) {
			MainScreen.getExecutorService().execute(new Runnable() {
				
				@Override
				public void run() {
					mapLayerListener.onMapSurfaceCreated(mapSurface);
				}
			});
		}
		
//		MainScreen.executorService.execute(loadMapSurfacePrefsRunnable);
		
		//TODO try to replace this with scheduling task
//		new Thread() {
//			@Override
//			public void run() {
//				while (true) {
//					try{
//	//					Log.d("tt", "drawing!");
//						Canvas canvas = getHolder().lockCanvas();
//	//					canvas.drawBitmap(testimage, null, new Rect(0, 0, 300, 300), null);
//	//					mapSurface.scaleToViewport(getWidth(), getHeight());
//						canvas.drawRect(new Rect(0,0,getWidth(), getHeight()), paint);
//						mapSurface.draw(canvas);
////						canvas.drawLine(0, 0, 300, 300, paint);
//						
//						getHolder().unlockCanvasAndPost(canvas);
//						
//						Thread.sleep(0);
//						
//					} catch (Exception e) {
//						e.printStackTrace();
//						break;
//					}
//				}
//			}
//		}.start();
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if (mapSurface.getImage() != null) {
			mapSurface.getImage().recycle();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		//TODO remove this
//		PointF p2 = mapSurface.getPosOnPose(event.getX(), event.getY());
//		Log.e("testttt", event.getX() + " / " + event.getY() + " is " + p2.x + " / " + p2.y);
		
		scaleGestureDetector.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
		
	}
	
	public void setMap(final Map map) {
		addLayoutListener(this, new Runnable() {
			public void run() {
				MainScreen.getExecutorService().execute(new Runnable() {
					
					@Override
					public void run() {
						mapSurface.setMap(map);
						mapSurface.scaleToViewport(getWidth(), getHeight());
						scheduleSurfaceDrawing();
					}
				});				
			}
		});
	}
	
	public void savePrefs(String tabPrefix, SharedPreferences.Editor ed) {
		String id = tabPrefix + MAP_LAYER_PREFIX;
		
		ed.putFloat(id+"-mapSurface.x", mapSurface.getX());
		ed.putFloat(id+"-mapSurface.y", mapSurface.getY());
		ed.putFloat(id+"-mapSurface.zoom", mapSurface.getZoom());
		if (mapSurface.getMap() != null) {
			ed.putString(id + "-currentmap", mapSurface.getMap().getConfigPath());
		}
		
		ed.commit();
	}
	
	public void loadPrefs(String tabPrefix, final SharedPreferences prefs) {
//		Log.d("errfind", "MapLayer.loadPreds() " + getWidth());
		String id = tabPrefix + MAP_LAYER_PREFIX;
		
		if (MapManager.getInstance().getMaps().isEmpty()) {
			//no maps found
			return;
		}
		
		Map map = null;
		try {
			if (prefs.contains(id + "-currentmap")) {
				map = MapManager.getInstance().getMapFromConfigPath(prefs.getString(id + "-currentmap", ""));
			}
		} catch (Exception e) {
			Log.e("MapLayer", "Error while loading saved map");
		}
		if (map == null) {
			map = MapManager.getInstance().getMaps().get(0);
		}
		mapSurface.setMap(map);
		
		mapSurface.setX(prefs.getFloat(id+"-mapSurface.x", Float.MIN_VALUE));
		mapSurface.setY(prefs.getFloat(id+"-mapSurface.y", Float.MIN_VALUE));
		mapSurface.setZoom(prefs.getFloat(id+"-mapSurface.zoom", Float.MIN_VALUE));
		if (mapSurface.getX() == Float.MIN_VALUE 
				|| mapSurface.getY() == Float.MIN_VALUE
				|| mapSurface.getZoom() == Float.MIN_VALUE) {
			mapPositionRestoredFlag = false;
		}
//		scheduleSurfaceDrawing();
	}
	
	public MapSurface getMapSurface() {
		return mapSurface;
	}
	
	public static interface MapLayerListener {
		public void onMapSurfaceCreated(MapSurface mapSurface);
	}
	
	public void setMapLayerListener(final MapLayerListener mapLayerListener) {
		this.mapLayerListener = mapLayerListener;
		if (mapSurface != null) {
			MainScreen.getExecutorService().execute(new Runnable() {
				
				@Override
				public void run() {
					mapLayerListener.onMapSurfaceCreated(mapSurface);
				}
			});
		}
	}

	public Map getMap() {
		return mapSurface.getMap();
	}
	
	public interface OnLongPressListener {
		public void onLongPress(MotionEvent event);
	}
	
	public void setOnLongPressListener(OnLongPressListener onLongPressListener) {
		this.onLongPressListener = onLongPressListener;
	}
}