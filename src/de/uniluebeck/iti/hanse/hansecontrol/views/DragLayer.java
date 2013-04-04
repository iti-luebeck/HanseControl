package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DragLayer extends RelativeLayout {
	
	private MapWidget dragInProgress_widgetFromList = null;
	LinearLayout dragInProgress_widgetFromList_layout;
	int dragInProgress_widgetFromList_oldIndex;
	
	public DragLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DragLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DragLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
//		MapWidget dummy = new MapWidget(getContext());
//		dummy.getDebugPaint().setColor(Color.BLUE);
//		
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
//		params.leftMargin = 90;
//		params.topMargin = 200;
//		//dummy.setVisibility(View.INVISIBLE);
//		dummy.setLayoutParams(params);
//		addView(dummy);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	private void setWidgetPosition(MotionEvent event) {
		RelativeLayout.LayoutParams params 
					= (RelativeLayout.LayoutParams) dragInProgress_widgetFromList.getLayoutParams();
		params.leftMargin = (int)(event.getX() - params.width / 2);
		params.topMargin = (int)(event.getY() - params.height / 2);
		dragInProgress_widgetFromList.setLayoutParams(params);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.w("touchlog", String.format("DragLayer.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
				event.getAction(), event.getActionMasked()));
		if (dragInProgress_widgetFromList != null) {
			switch(event.getActionMasked()) {
				case MotionEvent.ACTION_MOVE:
					MapWidget widget = dragInProgress_widgetFromList;
					Log.w("touchlog", "Handling widget position... TODO");
					//set new widget position
					setWidgetPosition(event);
					
					break;
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					//place widget at current position
					removeView(dragInProgress_widgetFromList);
					((WidgetLayer) findViewById(R.id.widgetLayer)).addView(dragInProgress_widgetFromList);
					dragInProgress_widgetFromList = null;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.w("touchlog", String.format("DragLayer.onInterceptTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
				event.getAction(), event.getActionMasked()));
		if (dragInProgress_widgetFromList != null) {
			MapWidget widget = dragInProgress_widgetFromList;
			Log.w("touchlog", "DragLayer is now handling a dragged MapWidget, intercepting...");
			//remove widget from parent LinearLayout
			dragInProgress_widgetFromList_layout.removeView(widget);
			//add widget as own child
			addView(widget);
			//change widget mode to full size
			widget.setMode(MapWidget.FULLSIZE_MODE);
			//handle move event
			onTouchEvent(event);
			return true;
		}
		return false;
	}
	
	public void startWidgetDraggingFromList(MapWidget widget) {
		dragInProgress_widgetFromList = widget;
		dragInProgress_widgetFromList_layout = (LinearLayout) widget.getParent();
		dragInProgress_widgetFromList_oldIndex = dragInProgress_widgetFromList_layout.indexOfChild(widget);
	}
	
}
