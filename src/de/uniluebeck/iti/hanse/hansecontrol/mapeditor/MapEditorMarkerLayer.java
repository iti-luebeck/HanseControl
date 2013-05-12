package de.uniluebeck.iti.hanse.hansecontrol.mapeditor;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class MapEditorMarkerLayer extends RelativeLayout {
	
	Marker marker1;
	Marker marker2;
	
	MarkerPositionListener positionListener;
	
	public MapEditorMarkerLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapEditorMarkerLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapEditorMarkerLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		
	}
	
	//TODO in mapeditor: if false show toast!
	public synchronized boolean addMarker(final float x, final float y) {
		if (marker1 != null && marker2 != null) {
			return false;			
		}
		Bitmap image = BitmapManager.getInstance().getBitmap(getResources(), 
				marker1 == null ? R.drawable.mapeditor_marker1 : R.drawable.mapeditor_marker2);
		final Marker marker = new Marker(getContext(), image);
		addView(marker);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marker.getLayoutParams();
		params.width = image.getWidth();
		params.height = image.getHeight();
		marker.setLayoutParams(params);
		if (marker.getWidth() == 0) {
			marker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener()
	        {
	            @Override
	            public void onGlobalLayout()
	            {
	            	marker.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            	setMarkerPos(marker, x, y);
	            }
	        });
		} else {
			setMarkerPos(marker, x, y);			
		}
		if (marker1 == null) {
			marker1 = marker;
		} else {
			marker2 = marker;
		}
		return true;
	}
	
	private PointF getMarkerPos(Marker marker) {
		float x = marker.getX() + marker.getWidth() / 2;
		float y = marker.getY() + marker.getHeight() / 2;
		return new PointF(x, y);
	}
	
	public PointF getMarker1Pos() {
		return getMarkerPos(marker1);
	}
	
	public PointF getMarker2Pos() {
		return getMarkerPos(marker2);	
	}
	
	public void setMarker1Pos(float x, float y) {
		setMarkerPos(marker1, x, y);
	}
	
	public void setMarker2Pos(float x, float y) {
		setMarkerPos(marker2, x, y);
	}
	
	private void setMarkerPos(final Marker marker, final float x, final float y) {
		if (marker == null) {
			return;
		}
		
//		//workaround for startup
//        if (marker.getWidth() == 0) {
//			marker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//	            @Override
//	            public void onGlobalLayout() {
//	            	marker.getViewTreeObserver().removeGlobalOnLayoutListener(this);
//	            	setMarkerPos(marker, x, y);
//	            }
//	        });
//	        return;
//        }
		
		final float left = x - marker.getWidth() / 2;
		final float top = y - marker.getHeight() / 2;
		final float right = x + marker.getWidth() / 2;
		final float bottom = y + marker.getHeight() / 2;		
		//check if marker is in visible range
		//TODO idea for future versions: paint arrow on edge to indicate that the marker is not in visible range
		
		
		
		Log.d("errfind", (marker == marker1 ? "Marker1" : "Marker2 or null") + " left: " + left + " top: " + top);
			
		marker.post(new Runnable() {
			@Override
			public void run() {
				if (left < 0 || top < 0 || right >= getWidth() || bottom >= getHeight()) {
					marker.setVisibility(View.INVISIBLE);
				} else {
					marker.setVisibility(View.VISIBLE);
				}
//				marker.setX(left);
//				marker.setY(top);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) marker.getLayoutParams();
				params.leftMargin = (int) left;
				params.topMargin = (int) top;
				marker.setLayoutParams(params);
				marker.invalidate();
//				invalidate();
				
			}
		});		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}
	
	private class Marker extends View {
		Bitmap image;
		
		float mx;
		float my;
		
		private final static int YOFFSET = 50;
		
		public Marker(Context context, Bitmap image) {
			super(context);
			this.image = image;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(image, null, new RectF(0, 0, getWidth(), getHeight()), null);
		}
		
		@Override
		public boolean onTouchEvent(final MotionEvent event) {
			if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
				mx = event.getX();
				my = event.getY();
				return true;
			}
			if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
				Log.d("errfind", "mx=" + mx + " eventx=" + event.getX());
//				setX(getX() - mx + (event.getX()));
//				setY(getY() - my - YOFFSET + (event.getY()));
				
				setMarkerPos(this, getX() - mx + (event.getX()), getY() - my - YOFFSET + (event.getY()));
				
//				invalidate();
				return true;
			}
			if (event.getActionMasked() == MotionEvent.ACTION_UP) {
				if (positionListener != null) {
					positionListener.positionUpdate();
				}
				return true;
			}
			return false;
		}

	}
	
	public void setPositionListener(MarkerPositionListener positionListener) {
		this.positionListener = positionListener;
	}
	
	public static interface MarkerPositionListener {
		public void positionUpdate();
	}
	
}
