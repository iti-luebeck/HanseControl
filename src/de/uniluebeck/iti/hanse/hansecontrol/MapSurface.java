package de.uniluebeck.iti.hanse.hansecontrol;

import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

public class MapSurface {
	
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
	
	private Paint textPaint;
	
	//translation to ros coordinate system
	float x_PoseOriginOnImage;
	float y_PoseOriginOnImage;
	float imagePoseScale;
	boolean poseAxisXinvert = false;
	boolean poseAxisYinvert = false;
	
	List<MapSurfaceListener> listeners = new LinkedList<MapSurface.MapSurfaceListener>();
	
	public MapSurface() {
		textPaint = new Paint();
		textPaint.setTextSize(20);
	}
	
	public synchronized void setMap(Map map) {
		this.map = map;
		if (map != null) {
			try {
				loadImage();
				initMap();
			} catch (Exception e) {
				Log.e("MapSurface", "Error while setting map!" + map.getConfigPath(), e);
			}
		}
	}
	
	private void initMap() {
		double imgDist = Math.sqrt(Math.pow(map.getX1_OnImage() - map.getX2_OnImage(),2) 
				+ Math.pow(map.getY1_OnImage() - map.getY2_OnImage(),2));
		double rosDist = Math.sqrt(Math.pow(map.getX1_OnPose() - map.getX2_OnPose(),2) 
				+ Math.pow(map.getY1_OnPose() - map.getY2_OnPose(),2));
		imagePoseScale = (float)(rosDist / imgDist);
		
		if (map.getX1_OnImage() < map.getX2_OnImage() != map.getX1_OnPose() < map.getX2_OnPose()) {
			poseAxisXinvert = true;
		}
		
		if (map.getY1_OnImage() < map.getY2_OnImage() != map.getY1_OnPose() < map.getY2_OnPose()) {
			poseAxisYinvert = true;
		}
		
		x_PoseOriginOnImage = map.getX1_OnImage() - (poseAxisXinvert ? -1 : 1) * (map.getX1_OnPose() / imagePoseScale);
		y_PoseOriginOnImage = map.getY1_OnImage() - (poseAxisYinvert ? -1 : 1) * (map.getY1_OnPose() / imagePoseScale);
	}
	
	public Map getMap() {
		return map;
	}
	
	private void loadImage() {
		image = BitmapManager.getInstance().getBitmap(map.getImagePath());
		if (image != null) {
			imgWidth = (float)image.getWidth();
			imgHeight = (float)image.getHeight();
			imgRatio = imgWidth / imgHeight;
		}
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
	
	//TODO remove this fkt
	public static void drawMarker(float x, float y, Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.LTGRAY);
		canvas.drawCircle(x, y, 5, p);
	}
	
	public synchronized void draw(Canvas canvas) {
		if (map == null) {
			canvas.drawText("No Map was found in folder " + MapManager.getInstance().getMapsDir(),
					50, 50, textPaint);
			return;
		}
		if (image == null) {
			return; //is empty map (= no map)
		}
		if (image.isRecycled()) {
			loadImage();
		}
		canvas.drawBitmap(image, null, new RectF(x, y, x + getWidth(), y + getHeight()), null);
		
		//TODO remove
//		drawMarker(getX(), getY(), canvas);
//		drawMarker(x + getWidth()-1, y + getHeight()-1, canvas);
//		PointF p1 = getPosOnViewport(50, 50);
//		drawMarker(p1.x, p1.y, canvas);
		
		synchronized (listeners) {
			for (final MapSurfaceListener listener : listeners) {
				MainScreen.executorService.execute(new Runnable() {
					
					@Override
					public void run() {
						listener.mapSurfaceRedraw();
					}
				});
			}
		}
	}
	
	public PointF getPosOnViewport(float x_onPose, float y_onPose) {
		float xRes = x + ((poseAxisXinvert ? -1 : 1) * (x_onPose / imagePoseScale) + x_PoseOriginOnImage) * zoom;
		float yRes = y + ((poseAxisYinvert ? -1 : 1) * (y_onPose / imagePoseScale) + y_PoseOriginOnImage) * zoom;
		return new PointF(xRes, yRes);
	}
	
	//TODO consider renaming pose to ros ?
	
	public PointF getPosOnPose(float x_OnViewport, float y_OnViewport) {
		float xOnImage = (x_OnViewport - x) / zoom;
		float yOnImage = (y_OnViewport - y) / zoom;
		
		
		
		float xRes = ((((x_OnViewport - x) / zoom) - x_PoseOriginOnImage) / (poseAxisXinvert ? -1 : 1)) * imagePoseScale;
		float yRes = ((((y_OnViewport - y) / zoom) - y_PoseOriginOnImage) / (poseAxisYinvert ? -1 : 1)) * imagePoseScale;
		return new PointF(xRes, yRes);
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
	
	public static interface MapSurfaceListener {
		public void mapSurfaceRedraw();
	}
	
	public void addListener(MapSurfaceListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
}