package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.Observer;

import com.google.common.base.Preconditions;

import de.uniluebeck.iti.hanse.hansecontrol.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class MapWidget extends BasicView {
	
	public static final int ICON_MODE = 0;
	public static final int FULLSIZE_MODE = 1;
	
	private int currentMode = ICON_MODE;
	
	Float mX, mY; //last position while dragging
	
	DragLayer dragLayer = null;
	
	private int widgetID = -1;
	public static final String WIDGET_PREFIX = "MapWidget-";
	
	public int defaultWidth;
	public int defaultHeight;
	
	//allow ratio change??
	//public float zoom = 1; //TODO implement
	
//	public MapWidget(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init();
//	}
//
//	public MapWidget(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init();
//	}

	public MapWidget(int defaultWidth, int defaultHeight, int widgetID, Context context) {
		super(context);
		this.widgetID = widgetID;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		init();
	}
	
	private void init() {
		if (DEBUG_MODE) {
			View view = new View(getContext()) {
				@Override
				protected void onDraw(Canvas canvas) {
					super.onDraw(canvas);
					if (getMode() == ICON_MODE) {
						paint.setTextSize(20);
						paint.setStrokeWidth(1);
						paint.setStyle(Paint.Style.FILL);
						canvas.drawText("ICON", 30, 25, paint);
					}
				}
			};
			addView(view);	
		}
		CloseButton closeButton = new CloseButton(getContext(), this);
		addView(closeButton);
		int closeButtonWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(closeButtonWidth, closeButtonWidth);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		
		closeButton.setLayoutParams(params);		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		Log.w("touchlog", String.format("MapWidget.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
				event.getAction(), event.getActionMasked()));
		
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				mX = event.getX();
				mY = event.getY();
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				mX = null;
				mY = null;
				break;
		}
		
		if (getParent() instanceof WidgetLayer && event.getActionMasked() == MotionEvent.ACTION_MOVE && dragLayer != null) {
			//request touch event interception from dragLayer
			dragLayer.startWidgetDraggingOnWidgetLayer(this, mX, mY);
			return true;
		}
		
//		if (getParent() instanceof WidgetLayer && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
////			Log.w("touchlog", "event.getX(): " + event.getX());
//			float dX = event.getX() - mX;
//			float dY = event.getY() - mY;
//			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
//			params.leftMargin += dX;
//			params.topMargin += dY;
//			setLayoutParams(params);
//			invalidate();
//			return true;
//		}
		
		if (getParent() instanceof LinearLayout && event.getActionMasked() == MotionEvent.ACTION_MOVE && dragLayer != null) {
			float distY = mY - event.getY();
			float distX = mX - event.getX();
			float len = (float) Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
			float alpha = (float) Math.acos(distY/len);
			
			Log.w("touchcalc", String.format("distx: %f, disty: %f, len: %f, alpha: %f", distX, distY, len, alpha));
			
			if (distY > 20 && alpha < Math.PI / 4) {
				//dragging up
				
				Log.w("touchlog", "Up drag started!!!");
				
				dragLayer.startWidgetDraggingFromList(this);
				
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
	
	public void setDragStart(float mX, float mY) {
		this.mX = mX;
		this.mY = mY;
	}
	
	public void setDragLayer(DragLayer dragLayer) {
		this.dragLayer = dragLayer;
	}
	
	public void setMode(int mode) {
		currentMode = mode;
		updateMode();
	}
	
	public int getMode() {
		return currentMode;
	}

	public int getWidgetID() {
		return widgetID;
	}
	
	private void updateMode() {
		switch(currentMode) {
			case ICON_MODE:
				float pixSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 85, getResources().getDisplayMetrics());
				float pixMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
				
				//TODO get size from constant in MainScreen!!
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
				params.width = (int) pixSize;
				params.height = (int) pixSize;
				params.setMargins((int)pixMargin, 0, (int)pixMargin, (int)pixMargin);
				setLayoutParams(params);
				break;
			case FULLSIZE_MODE:
				//TODO get size from upper derivation
				RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) getLayoutParams();
//				params2.width = 200;
//				params2.height = 200;
				params2.width = defaultWidth;
				params2.height = defaultHeight;
				
				setLayoutParams(params2);
				break;
		}
	}
	
	
	public void savePrefs(String tabPrefix, SharedPreferences.Editor ed) {
		String id = tabPrefix + WIDGET_PREFIX + widgetID;
		ed.putInt(id+"-currentMode", currentMode);
		Log.d("ttt1save", id+"-currentMode");
		if (getMode() == FULLSIZE_MODE) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			ed.putInt(id+"-params.leftMargin", params.leftMargin);
			ed.putInt(id+"-params.topMargin", params.topMargin);
			ed.putInt(id+"-params.width", params.width);
			ed.putInt(id+"-params.height", params.height);
		}
	}
	
	public void loadPrefs(String tabPrefix, SharedPreferences prefs) {
		String id = tabPrefix + WIDGET_PREFIX + widgetID;
		setMode(prefs.getInt(id+"-currentMode", ICON_MODE));
		if (getMode() == FULLSIZE_MODE) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.leftMargin = prefs.getInt(id+"-params.leftMargin", -1);
			params.topMargin = prefs.getInt(id+"-params.topMargin", -1);
			params.width = prefs.getInt(id+"-params.width", defaultWidth);
			params.height = prefs.getInt(id+"-params.height", defaultHeight);
		}
	}
	
//	public void setWidgetID(int widgetID) {
//		this.widgetID = widgetID;
//	}
	
	public class CloseButton extends View {
		Paint paint;
		MapWidget parentWidget;
		
		public CloseButton(Context context, MapWidget parentWidget) {
			super(context);
			paint = new Paint();
			this.parentWidget = parentWidget;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (getMode() == FULLSIZE_MODE) {
				super.onDraw(canvas);
				canvas.drawLine(0, 0, getWidth() - 1, getHeight() - 1, paint);
				canvas.drawLine(0, getHeight() - 1, getWidth() - 1, 0, paint);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (getMode() == FULLSIZE_MODE) {
				WidgetLayer widgetLayer = (WidgetLayer) parentWidget.getParent();
				widgetLayer.removeWidget(parentWidget);
				return true;
			}
			return false;
		}
	}
}
