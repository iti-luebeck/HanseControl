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
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MapWidget extends BasicView {
	
	public static final int ICON_MODE = 0;
	public static final int FULLSIZE_MODE = 1;
	
	private int currentMode = ICON_MODE;
	
	Float mX, mY; //last position while dragging
	
	DragLayer dragLayer = null;
	
	private int widgetID = -1;
	public static final String PREF_PREFIX = "MapWidget-";
	
//	public MapWidget(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init();
//	}
//
//	public MapWidget(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init();
//	}

	public MapWidget(int widgetID, Context context) {
		super(context);
		this.widgetID = widgetID;
		init();
	}
	
	private void init() {
		getDebugPaint().setColor(Color.GREEN);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
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
		
		if (getParent() instanceof WidgetLayer && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			float dX = event.getX() - mX;
			float dY = event.getY() - mY;
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.leftMargin += dX;
			params.topMargin += dY;
			setLayoutParams(params);
			invalidate();
			return true;
		}
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
				//TODO get size from constant in MainScreen!!
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
				params.width = 85;
				params.height = 85;
				params.setMargins(5, 0, 5, 8);
				setLayoutParams(params);
				break;
			case FULLSIZE_MODE:
				//TODO get size from upper derivation
				RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) getLayoutParams();
				params2.width = 200; 
				params2.height = 200;				
				setLayoutParams(params2);
				break;
		}
	}
	
	
	public void savePrefs(SharedPreferences.Editor ed) {
		String id = PREF_PREFIX + widgetID;
		ed.putInt(id+"-currentMode", currentMode);
		Log.d("ttt1save", id+"-currentMode");
		if (getMode() == FULLSIZE_MODE) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			ed.putInt(id+"-params.leftMargin", params.leftMargin);
			ed.putInt(id+"-params.topMargin", params.topMargin);
		}
	}
	
	public void loadPrefs(SharedPreferences prefs) {
		String id = PREF_PREFIX + widgetID;
		setMode(prefs.getInt(id+"-currentMode", ICON_MODE));
		if (getMode() == FULLSIZE_MODE) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.leftMargin = prefs.getInt(id+"-params.leftMargin", -1);
			params.topMargin = prefs.getInt(id+"-params.topMargin", -1);
		}
	}
	
//	public void setWidgetID(int widgetID) {
//		this.widgetID = widgetID;
//	}
	
}
