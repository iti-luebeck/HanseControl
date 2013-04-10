package de.uniluebeck.iti.hanse.hansecontrol.map;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.views.WidgetLayer;
import android.content.Context;
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

public class MapLayer extends SurfaceView implements SurfaceHolder.Callback{
	
//	GestureDetector gestureDetector;
	
	public static Bitmap testimage;
	MapSurface mapSurface;
	
	GestureDetector gestureDetector;
	ScaleGestureDetector scaleGestureDetector;
	
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
		testimage = BitmapFactory.decodeResource(getResources(), R.drawable.test_tile);
		gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}
			
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2,
					float distanceX, float distanceY) {
				mapSurface.translate(-distanceX, -distanceY);
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
				return true;
			}
		});
//		mapSurface = new MapSurface(testimage);
//		mapSurface.setX(0);
//		mapSurface.setY(0);
//		mapSurface.setWidth(700);
//		mapSurface.setHeight(500);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		final Paint paint = new Paint();
		paint.setColor(Color.LTGRAY);
		paint.setStrokeWidth(0);
		mapSurface = new MapSurface(testimage, getWidth(), getHeight());
		
//		MapSurface mapSurface = new MapSurface(testimage);
		
		//TODO try to replace this with scheduling task
		new Thread() {
			@Override
			public void run() {
				while (true) {
//					Log.d("tt", "drawing!");
					Canvas canvas = getHolder().lockCanvas();
//					canvas.drawBitmap(testimage, null, new Rect(0, 0, 300, 300), null);
//					mapSurface.scaleToViewport(getWidth(), getHeight());
					canvas.drawRect(new Rect(0,0,getWidth(), getHeight()), paint);
					mapSurface.draw(canvas);
					canvas.drawLine(0, 0, 300, 300, paint);
					
					getHolder().unlockCanvasAndPost(canvas);
					
					try {
						Thread.sleep(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		scaleGestureDetector.onTouchEvent(event);
		return gestureDetector.onTouchEvent(event);
	}
	
}

class MapSurface {
	
	Bitmap image;
	
	private float x,y;
	private float zoom;
	
	private float imgWidth, imgHeight;
	private float imgRatio; //  width / height
	
//	private float initViewPortX;
	private float initViewPortX_relativeToMap; // (x+lx) / w
//	private float initViewPortY;
	private float initViewPortY_relativeToMap; // (y+ly) / h
	
	
	public MapSurface(Bitmap image, float viewportWidth, float viewportHeight) {
		this.image = image;
		setImage(image);
		scaleToViewport(viewportWidth, viewportHeight);
	}
	
	public synchronized void setImage(Bitmap image) {
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
		canvas.drawBitmap(image, null, new RectF(x, y, x + getWidth(), y + getHeight()), null);
	}
	
	public void startPinchToZoom(float initViewPortX, float initViewPortY) {
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
}