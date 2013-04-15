package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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

/**
 * On this surface view all contents of the map will be drawn.
 * 
 * @author Stefan Hueske
 */
public class MapLayer extends SurfaceView implements SurfaceHolder.Callback{
	
//	GestureDetector gestureDetector;
	
	public static Bitmap testimage;
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
		testimage = BitmapManager.getInstance().getBitmap(getResources(), R.drawable.test_tile);
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
		});
		scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
			@Override
			public boolean onScaleBegin(ScaleGestureDetector detector) {
				mapSurface.startPinchToZoom(detector.getFocusX(), detector.getFocusY());
				return true;
			}
			
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				Log.d("multitouch", String.format("PinchToZoom: focusX=%f, factor=%f", detector.getFocusX(), detector.getScaleFactor()));
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
		surfaceDrawingFuture = MainScreen.executorService.schedule(new Runnable() {
			
			@Override
			public void run() {
				try{
//					Log.d("tt", "drawing!");
					Canvas canvas = getHolder().lockCanvas();
//					canvas.drawBitmap(testimage, null, new Rect(0, 0, 300, 300), null);
//					mapSurface.scaleToViewport(getWidth(), getHeight());
					canvas.drawRect(new Rect(0,0,getWidth(), getHeight()), surfaceBackgroundPaint);
					mapSurface.draw(canvas);
//										canvas.drawLine(0, 0, 300, 300, paint);
					
					getHolder().unlockCanvasAndPost(canvas);
					
					Thread.sleep(0);
					
				} catch (InterruptedException e) {
				} catch (Exception e) {
					Log.d("MapSurface", "Scheduled drawing throwed exception! ", e);
				}
				
			}
		}, 0, TimeUnit.MILLISECONDS);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
//		Log.d("errfind", "MapLayer.surfaceCreated()" + getWidth());
//		MapSurface mapSurface = new MapSurface(testimage);		
		if (!mapPositionRestoredFlag) {
			mapSurface.scaleToViewport(getWidth(), getHeight());
		}
		scheduleSurfaceDrawing();
		
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
		testimage.recycle();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		scaleGestureDetector.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}
	
	public void setMap(Map map) {
		mapSurface.setMap(map);
		mapSurface.scaleToViewport(getWidth(), getHeight());
		scheduleSurfaceDrawing();
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
}

class MapSurface {
	
	private Map map;
	private Bitmap image;
	
	private float x,y;
	private float zoom = 1;
	
	private float imgWidth, imgHeight;
	private float imgRatio; //  width / height
	
//	private float initViewPortX;
	private float initViewPortX_relativeToMap; // (x+lx) / w
//	private float initViewPortY;
	private float initViewPortY_relativeToMap; // (y+ly) / h
	
	public MapSurface() {
		
	}
	
	public synchronized void setMap(Map map) {
		this.map = map;
		if (map != null) {
			loadImage();
		}
	}
	
	public Map getMap() {
		return map;
	}
	
	private void loadImage() {
		image = BitmapManager.getInstance().getBitmap(map.getImagePath());
		imgWidth = (float)image.getWidth();
		imgHeight = (float)image.getHeight();
		imgRatio = imgWidth / imgHeight;
	}
		
	public synchronized void scaleToViewport(float viewportWidth, float viewportHeight) {
		if (viewportWidth / viewportHeight < imgRatio) {
			zoom = viewportWidth / imgWidth;
			x = 0;
			y = viewportHeight / 2 - getHeight() / 2; 
		} else {
			zoom = viewportHeight / imgHeight;
			y = 0;
			x = viewportWidth / 2 - getWidth() / 2;
		}
		Log.d("mapsurface", String.format("scaleToViewport(%f, %f): x=%f, y=%f, zoom=%f, imgWidth=%f, imgHeight=%f", 
				viewportWidth, viewportHeight, x, y, zoom, imgWidth, imgHeight));
	}
	
	public synchronized void translate(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	public synchronized void draw(Canvas canvas) {
		if (map == null) {
			return;
		}
		if (image.isRecycled()) {
			loadImage();
		}
		canvas.drawBitmap(image, null, new RectF(x, y, x + getWidth(), y + getHeight()), null);
	}
	
	public synchronized void startPinchToZoom(float initViewPortX, float initViewPortY) {
//		this.initViewPortX = initViewPortX;
//		this.initViewPortY = initViewPortY;
		initViewPortX_relativeToMap = (initViewPortX - x) / getWidth();
		initViewPortY_relativeToMap = (initViewPortY - y) / getHeight();
	}
	
	public synchronized void zoom(float viewportX, float viewportY,float factor) {
//		x -= (initViewPortX + x) * factor - viewportX;
//		y -= (initViewPortY + y) * factor - viewportY;
//		x += viewportX - initViewPortX;
//		y += viewportY - initViewPortY;
		zoom *= factor;
		x = viewportX - initViewPortX_relativeToMap * getWidth();
		y = viewportY - 
				initViewPortY_relativeToMap * getHeight();
		Log.d("multitouch", String.format("initViewPortX_relativeToMap=%f, viewportX=%f, getWidth=%f", 
				initViewPortX_relativeToMap, viewportX, getWidth()));
	}

	public float getX() {
		return x;
	}

	public synchronized void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public synchronized void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return imgWidth * zoom;
	}

	public float getHeight() {
		return imgHeight * zoom;
	}
	
	public float getZoom() {
		return zoom;
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public synchronized void setZoom(float zoom) {
		this.zoom = zoom;
	}
}