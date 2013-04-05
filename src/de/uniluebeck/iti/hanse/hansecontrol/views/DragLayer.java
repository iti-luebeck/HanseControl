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
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class DragLayer extends RelativeLayout {
	
	private MapWidget dragInProgress_widgetFromList = null;
	LinearLayout dragInProgress_widgetFromList_layout;
//	int dragInProgress_widgetFromList_oldIndex;
	
	private MapWidget dragInProgress_widgetOnWidgetLayer = null;
	private float dragInProgress_widgetOnWidgetLayer_mX;
	private float dragInProgress_widgetOnWidgetLayer_mY;
		
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
	
//	private void setWidgetPositionCenter(MotionEvent event, MapWidget widget) {
//		RelativeLayout.LayoutParams params 
//					= (RelativeLayout.LayoutParams) widget.getLayoutParams();
//		params.leftMargin = (int)(event.getX() - params.width / 2);
//		params.topMargin = (int)(event.getY() - params.height / 2);
//		widget.setLayoutParams(params);
//	}
	
	private void handleDrag_widgetFromList(MotionEvent event) {
		switch(event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				MapWidget widget = dragInProgress_widgetFromList;
				Log.w("touchlog", "Handling widget drag from list...");
				//set new widget position
				setWidgetPosition_edgeSnapping(widget, 
						event.getX() - widget.getWidth() / 2,
						event.getY() - widget.getHeight() / 2);
				
//				setWidgetPositionCenter(event, widget);
				
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				//place widget at current position
				removeView(dragInProgress_widgetFromList);
				((WidgetLayer) findViewById(R.id.widgetLayer)).addView(dragInProgress_widgetFromList);
				dragInProgress_widgetFromList = null;
		}
	}
	
	private void handleDrag_widgetOnWidgetLayer(MotionEvent event) {
		switch(event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				MapWidget widget = dragInProgress_widgetOnWidgetLayer;
				Log.w("touchlog", "Handling widget drag on widget layer...");
				//set new widget position
//				RelativeLayout.LayoutParams params 
//							= (RelativeLayout.LayoutParams) widget.getLayoutParams();
//				params.leftMargin = (int)(event.getX() - dragInProgress_widgetOnWidgetLayer_mX);
//				params.topMargin = (int)(event.getY() - dragInProgress_widgetOnWidgetLayer_mY);
//				widget.setLayoutParams(params);
				setWidgetPosition_edgeSnapping(widget, 
						event.getX() - dragInProgress_widgetOnWidgetLayer_mX, 
						event.getY() - dragInProgress_widgetOnWidgetLayer_mY);
				
				
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				dragInProgress_widgetOnWidgetLayer = null;
				
				//place widget at current position
	//			removeView(dragInProgress_widgetFromList);
	//			((WidgetLayer) findViewById(R.id.widgetLayer)).addView(dragInProgress_widgetFromList);
	//			dragInProgress_widgetFromList = null;
		}
	}
	
	/**
	 * 
	 * @param widget widget to position
	 * @param nativeX new x-pos without modification
	 * @param nativeY new y-pos without modification
	 */
	private void setWidgetPosition_edgeSnapping(MapWidget widget, float nativeX, float nativeY) {
		RelativeLayout.LayoutParams pWidget = (RelativeLayout.LayoutParams) widget.getLayoutParams();
		
		//prevent offscreen positions
		nativeX = Math.max(nativeX, 0);
		nativeY = Math.max(nativeY, 0);
		nativeX = Math.min(nativeX, getWidth() - pWidget.width - 1);
		nativeY = Math.min(nativeY, getHeight() - pWidget.height - 1);
		
		//define snapping range
		float snapRange = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
		
		//perform edge snapping
		List<Integer> edgesX = new LinkedList<Integer>();
		List<Integer> edgesY = new LinkedList<Integer>();
		edgesX.add(0);
		edgesY.add(0);
		edgesX.add((getWidth() - 1) - widget.getWidth());
		edgesY.add((getHeight() - 1) - widget.getHeight());
		
		//add edges for nearby widgets
		WidgetLayer widgetLayer = (WidgetLayer) findViewById(R.id.widgetLayer);
		for (int i = 0; i < widgetLayer.getChildCount(); i++) {
			View child = widgetLayer.getChildAt(i);
			if (child instanceof MapWidget && child != widget) {
				MapWidget w = (MapWidget) child;
				float widgetCenterX = w.getX() + w.getWidth() / 2;
				if (Math.abs(widgetCenterX - widget.getX()) < w.getWidth() / 2 + snapRange
						|| Math.abs(widgetCenterX - (widget.getX() + widget.getWidth())) < w.getWidth() / 2 + snapRange) {
					//top edge
					edgesY.add((int)w.getY());
					edgesY.add((int)(w.getY() + w.getHeight()));
					//bottom edge
					edgesY.add((int)w.getY() - widget.getHeight());
					edgesY.add((int)(w.getY() + w.getHeight() - widget.getHeight()));
				}
				float widgetCenterY = w.getY() + w.getHeight() / 2;
				if (Math.abs(widgetCenterY - widget.getY()) < w.getHeight() / 2 + snapRange
						|| Math.abs(widgetCenterY - (widget.getY() + widget.getHeight())) < w.getHeight() / 2 + snapRange) {
					//left edge
					edgesX.add((int)w.getX());
					edgesX.add((int)(w.getX() + w.getWidth()));
					//right edge
					edgesX.add((int)w.getX() - widget.getWidth());
					edgesX.add((int)(w.getX() + w.getWidth() - widget.getWidth()));
				}
			}
		}
		
		for (Integer edge : edgesX) {
			if(Math.abs(edge - nativeX) <= snapRange) {
				nativeX = edge;
				break;
			}
		}
		
		for (Integer edge : edgesY) {
			if(Math.abs(edge - nativeY) <= snapRange) {
				nativeY = edge;
				break;
			}
		}
		
		pWidget.leftMargin = (int) nativeX;
		pWidget.topMargin = (int) nativeY;
		widget.setLayoutParams(pWidget);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.w("touchlog", String.format("DragLayer.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
				event.getAction(), event.getActionMasked()));
		if (dragInProgress_widgetFromList != null) {
			handleDrag_widgetFromList(event);
			return true;
		} else if (dragInProgress_widgetOnWidgetLayer != null) {
			handleDrag_widgetOnWidgetLayer(event);
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
			Log.w("touchlog", "DragLayer is now handling a dragged MapWidget from list, intercepting...");
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
		if (dragInProgress_widgetOnWidgetLayer != null) {
			Log.w("touchlog", "DragLayer is now handling a dragged MapWidget on WidgetLayer, intercepting...");
			onTouchEvent(event);
			return true;
		}
		return false;
	}
	
	public void startWidgetDraggingFromList(MapWidget widget) {
		dragInProgress_widgetFromList = widget;
		dragInProgress_widgetFromList_layout = (LinearLayout) widget.getParent();
//		dragInProgress_widgetFromList_oldIndex = dragInProgress_widgetFromList_layout.indexOfChild(widget);
	}
	
	public void startWidgetDraggingOnWidgetLayer(MapWidget widget, float mX, float mY) {
		this.dragInProgress_widgetOnWidgetLayer = widget;
		this.dragInProgress_widgetOnWidgetLayer_mX = mX;
		this.dragInProgress_widgetOnWidgetLayer_mY = mY;
	}
	
}
