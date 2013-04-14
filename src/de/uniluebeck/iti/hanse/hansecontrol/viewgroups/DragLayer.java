package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget.CornerResizer;

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

/**
 * This Layer handles dragging, resizing and snapping of widgets.
 * It is also the root of all other ViewGroups in the MainScreen Activity.
 * 
 * @author Stefan Hueske
 */
public class DragLayer extends RelativeLayout {

	//TODO cleanup
	
	private MapWidget dragInProgress_widgetFromList = null;
	LinearLayout dragInProgress_widgetFromList_layout;
	// int dragInProgress_widgetFromList_oldIndex;

	private MapWidget dragInProgress_widgetOnWidgetLayer = null;
	private float dragInProgress_widgetOnWidgetLayer_mX;
	private float dragInProgress_widgetOnWidgetLayer_mY;

	private CornerResizer resizeInProgress = null;
	private float resizeInProgress_mX;
	private float resizeInProgress_mY;

	//cached RelativeLayout params in float format
	private float resizeCache_width, resizeCache_height, resizeCache_x, resizeCache_y;
	
	// define snapping range
	private float snapRange = TypedValue
			.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources()
					.getDisplayMetrics());

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
		// MapWidget dummy = new MapWidget(getContext());
		// dummy.getDebugPaint().setColor(Color.BLUE);
		//
		// RelativeLayout.LayoutParams params = new
		// RelativeLayout.LayoutParams(200, 200);
		// params.leftMargin = 90;
		// params.topMargin = 200;
		// //dummy.setVisibility(View.INVISIBLE);
		// dummy.setLayoutParams(params);
		// addView(dummy);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	// private void setWidgetPositionCenter(MotionEvent event, MapWidget widget)
	// {
	// RelativeLayout.LayoutParams params
	// = (RelativeLayout.LayoutParams) widget.getLayoutParams();
	// params.leftMargin = (int)(event.getX() - params.width / 2);
	// params.topMargin = (int)(event.getY() - params.height / 2);
	// widget.setLayoutParams(params);
	// }

	private void handleDrag_widgetFromList(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_MOVE:
			MapWidget widget = dragInProgress_widgetFromList;
			Log.w("touchlog", "Handling widget drag from list...");
			// set new widget position
			setWidgetPosition_edgeSnapping(widget,
					event.getX() - widget.getWidth() / 2,
					event.getY() - widget.getHeight() / 2);

			// setWidgetPositionCenter(event, widget);

			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// place widget at current position
			removeView(dragInProgress_widgetFromList);
			((WidgetLayer) findViewById(R.id.widgetLayer))
					.addView(dragInProgress_widgetFromList);
			dragInProgress_widgetFromList.showControls();
			dragInProgress_widgetFromList = null;
		}
	}

	private void handleDrag_widgetOnWidgetLayer(MotionEvent event) {
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_MOVE:
			MapWidget widget = dragInProgress_widgetOnWidgetLayer;
			Log.w("touchlog", "Handling widget drag on widget layer...");
			// set new widget position
			// RelativeLayout.LayoutParams params
			// = (RelativeLayout.LayoutParams) widget.getLayoutParams();
			// params.leftMargin = (int)(event.getX() -
			// dragInProgress_widgetOnWidgetLayer_mX);
			// params.topMargin = (int)(event.getY() -
			// dragInProgress_widgetOnWidgetLayer_mY);
			// widget.setLayoutParams(params);
			setWidgetPosition_edgeSnapping(widget, event.getX()
					- dragInProgress_widgetOnWidgetLayer_mX, event.getY()
					- dragInProgress_widgetOnWidgetLayer_mY);

			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			dragInProgress_widgetOnWidgetLayer = null;

			// place widget at current position
			// removeView(dragInProgress_widgetFromList);
			// ((WidgetLayer)
			// findViewById(R.id.widgetLayer)).addView(dragInProgress_widgetFromList);
			// dragInProgress_widgetFromList = null;
		}
	}

	private void handleResizing(MotionEvent event) {
		if (resizeInProgress == null) {
			return;
		}
		MapWidget widget = (MapWidget) resizeInProgress.getParent();
		widget.cancelDelayedHideControls();
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				RelativeLayout.LayoutParams pWidget = (RelativeLayout.LayoutParams) widget
						.getLayoutParams();
				if (resizeInProgress.getCorner() == CornerResizer.BOTTOM_RIGHT) {				
					float cornerOffsetX = resizeInProgress.getWidth() - resizeInProgress_mX;
					float cornerOffsetY = resizeInProgress.getHeight() - resizeInProgress_mY;
					
					//calc new size
					float newWidth = event.getX() - widget.getX() + cornerOffsetX;
					float newHeight = event.getY() - widget.getY() + cornerOffsetY;
							
					// perform snapping
					float cornerX = widget.getX() + newWidth;
					float cornerY = widget.getY() + newHeight;
					
					cornerX = getXPos_edgeSnapping(cornerX, widget.getY(), 
							widget.getY() + newHeight);
					
					cornerY = getYPos_edgeSnapping(cornerY, widget.getX(), 
							widget.getX() + newWidth);
					
					resizeCache_width = cornerX - widget.getX();
					resizeCache_height = cornerY - widget.getY();
					
//					pWidget.width = (int)(cornerX - widget.getX());
//					pWidget.height = (int)(cornerY - widget.getY());
					
				} else if (resizeInProgress.getCorner() == CornerResizer.BOTTOM_LEFT) {
					float cornerOffsetX = resizeInProgress_mX;
					float cornerOffsetY = resizeInProgress.getHeight() - resizeInProgress_mY;
					
					//calc new size
					float newWidth = resizeCache_width + resizeCache_x - (event.getX() - cornerOffsetX);
					float newHeight = event.getY() - resizeCache_y + cornerOffsetY;
					
					// perform snapping
					float cornerX = resizeCache_x + resizeCache_width - newWidth;
					float cornerY = resizeCache_y + newHeight;
					
					cornerX = getXPos_edgeSnapping(cornerX, resizeCache_y, 
							resizeCache_y + newHeight);
					
					cornerY = getYPos_edgeSnapping(cornerY, cornerX, 
							cornerX + newWidth);
					
					resizeCache_width = resizeCache_x + resizeCache_width - cornerX;
					resizeCache_height = cornerY - resizeCache_y;
					resizeCache_x = cornerX;
					
//					pWidget.width = (int)(resizeCache_width);
//					pWidget.height = (int)(resizeCache_height);
//					pWidget.leftMargin = (int)(resizeCache_x);
				} else if (resizeInProgress.getCorner() == CornerResizer.TOP_RIGHT) {
					float cornerOffsetX = resizeInProgress.getWidth() - resizeInProgress_mX;
					float cornerOffsetY = resizeInProgress_mY;
					
					//calc new size
					float newWidth = event.getX() - resizeCache_x + cornerOffsetX;
					float newHeight = resizeCache_height + resizeCache_y - (event.getY() - cornerOffsetY);
					
					// perform snapping
					float cornerX = resizeCache_x + newWidth;
					float cornerY = resizeCache_y + resizeCache_height - newHeight;
					
					cornerY = getYPos_edgeSnapping(cornerY, resizeCache_x, 
							resizeCache_x + newWidth);
					
					cornerX = getXPos_edgeSnapping(cornerX, cornerY, 
							cornerY + newHeight);
					
					resizeCache_width = cornerX - resizeCache_x;
					resizeCache_height = resizeCache_y + resizeCache_height - cornerY;
					resizeCache_y = cornerY;
					
//					pWidget.width = (int)(resizeCache_width);
//					pWidget.height = (int)(resizeCache_height);
//					pWidget.topMargin = (int)(resizeCache_y);
				} else if (resizeInProgress.getCorner() == CornerResizer.TOP_LEFT) {
					float cornerOffsetX = resizeInProgress_mX;
					float cornerOffsetY = resizeInProgress_mY;
					
					//calc new size
					float newWidth = resizeCache_width + resizeCache_x - (event.getX() - cornerOffsetX);
					float newHeight = resizeCache_height + resizeCache_y - (event.getY() - cornerOffsetY);
					
					// perform snapping
					float cornerX = resizeCache_x + resizeCache_width - newWidth;
					float cornerY = resizeCache_y + resizeCache_height - newHeight;
					
					cornerY = getYPos_edgeSnapping(cornerY, cornerX, 
							cornerX + newWidth);
					
					cornerX = getXPos_edgeSnapping(cornerX, cornerY, 
							cornerY + newHeight);
					
					resizeCache_width = resizeCache_x + resizeCache_width - cornerX;
					resizeCache_height = resizeCache_y + resizeCache_height - cornerY;
					resizeCache_x = cornerX;
					resizeCache_y = cornerY;
				} 
				
				//force minimal size
				//TODO use constants
				
//				pWidget.width = Math.max(80, pWidget.width);
//				pWidget.height = Math.max(80, pWidget.height);
//				resizeCache_width = Math.max(80, resizeCache_width);
//				resizeCache_height = Math.max(80, resizeCache_height);
				
				if (resizeCache_width > 85) {
					pWidget.width = (int)(resizeCache_width);
					pWidget.leftMargin = (int)(resizeCache_x);
				}
				if (resizeCache_height > 85) {
					pWidget.height = (int)(resizeCache_height);
					pWidget.topMargin = (int)(resizeCache_y);					
				}
				widget.setLayoutParams(pWidget);
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				resizeInProgress = null;
				widget.delayHideControls();
		}
	}

	/**
	 * 
	 * @param nativeX x-pos without snapping
	 * @param nativeY1 y-pos of first point of edge without snapping
	 * @param nativeY2 y-pos of second point of edge without snapping
	 * @return
	 */
	private float getXPos_edgeSnapping(float nativeX, float nativeY1,
			float nativeY2) {
		List<Integer> edgesX = new LinkedList<Integer>();
		// add screen edges
		edgesX.add(0);
		edgesX.add(getWidth() - 1);

		// add widget edges
		WidgetLayer widgetLayer = (WidgetLayer) findViewById(R.id.widgetLayer);
		for (int i = 0; i < widgetLayer.getChildCount(); i++) {
			View child = widgetLayer.getChildAt(i);
			if (child instanceof MapWidget && child != resizeInProgress.getParent()) {
				MapWidget w = (MapWidget) child;
				float widgetCenterY = w.getY() + w.getHeight() / 2;
				if (Math.abs(widgetCenterY - nativeY1) < w.getHeight() / 2 + snapRange
						|| Math.abs(widgetCenterY - nativeY2) 
						< w.getHeight() / 2 + snapRange) {
					edgesX.add((int) w.getX());
					edgesX.add((int) (w.getX() + w.getWidth()));
				}
			}
		}
		
		// perform snapping
		for (Integer edge : edgesX) {
			if (Math.abs(edge - nativeX) <= snapRange) {
				return edge;
			}
		}
		return nativeX;
	}
	
	/**
	 * 
	 * @param nativeY y-pos without snapping
	 * @param nativeX1 x-pos of first point of edge without snapping
	 * @param nativeX2 x-pos of second point of edge without snapping
	 * @return
	 */
	private float getYPos_edgeSnapping(float nativeY, float nativeX1,
			float nativeX2) {
		List<Integer> edgesY = new LinkedList<Integer>();
		// add screen edges
		edgesY.add(0);
		edgesY.add(getHeight() - 1);

		// add widget edges
		WidgetLayer widgetLayer = (WidgetLayer) findViewById(R.id.widgetLayer);
		for (int i = 0; i < widgetLayer.getChildCount(); i++) {
			View child = widgetLayer.getChildAt(i);
			if (child instanceof MapWidget && child != resizeInProgress.getParent()) {
				MapWidget w = (MapWidget) child;
				float widgetCenterX = w.getX() + w.getWidth() / 2;
				if (Math.abs(widgetCenterX - nativeX1) < w.getWidth() / 2 + snapRange
						|| Math.abs(widgetCenterX - nativeX2) 
						< w.getWidth() / 2 + snapRange) {
					edgesY.add((int) w.getY());
					edgesY.add((int) (w.getY() + w.getHeight()));
				}
			}
		}
		
		// perform snapping
		for (Integer edge : edgesY) {
			if (Math.abs(edge - nativeY) <= snapRange) {
				return edge;
			}
		}
		return nativeY;
	}

	/**
	 * 
	 * @param widget
	 *            widget to position
	 * @param nativeX
	 *            new x-pos without modification
	 * @param nativeY
	 *            new y-pos without modification
	 */
	private void setWidgetPosition_edgeSnapping(MapWidget widget,
			float nativeX, float nativeY) {
		RelativeLayout.LayoutParams pWidget = (RelativeLayout.LayoutParams) widget
				.getLayoutParams();

		// prevent offscreen positions
		nativeX = Math.max(nativeX, 0);
		nativeY = Math.max(nativeY, 0);
		nativeX = Math.min(nativeX, getWidth() - pWidget.width - 1);
		nativeY = Math.min(nativeY, getHeight() - pWidget.height - 1);

		// perform edge snapping
		List<Integer> edgesX = new LinkedList<Integer>();
		List<Integer> edgesY = new LinkedList<Integer>();
		edgesX.add(0);
		edgesY.add(0);
		edgesX.add((getWidth() - 1) - widget.getWidth());
		edgesY.add((getHeight() - 1) - widget.getHeight());

		// add edges for nearby widgets
		WidgetLayer widgetLayer = (WidgetLayer) findViewById(R.id.widgetLayer);
		for (int i = 0; i < widgetLayer.getChildCount(); i++) {
			View child = widgetLayer.getChildAt(i);
			if (child instanceof MapWidget && child != widget) {
				MapWidget w = (MapWidget) child;
				float widgetCenterX = w.getX() + w.getWidth() / 2;
				if (Math.abs(widgetCenterX - widget.getX()) < w.getWidth() / 2
						+ snapRange
						|| Math.abs(widgetCenterX
								- (widget.getX() + widget.getWidth())) < w
								.getWidth() / 2 + snapRange) {
					// top edge
					edgesY.add((int) w.getY());
					edgesY.add((int) (w.getY() + w.getHeight()));
					// bottom edge
					edgesY.add((int) w.getY() - widget.getHeight());
					edgesY.add((int) (w.getY() + w.getHeight() - widget
							.getHeight()));
				}
				float widgetCenterY = w.getY() + w.getHeight() / 2;
				if (Math.abs(widgetCenterY - widget.getY()) < w.getHeight() / 2
						+ snapRange
						|| Math.abs(widgetCenterY
								- (widget.getY() + widget.getHeight())) < w
								.getHeight() / 2 + snapRange) {
					// left edge
					edgesX.add((int) w.getX());
					edgesX.add((int) (w.getX() + w.getWidth()));
					// right edge
					edgesX.add((int) w.getX() - widget.getWidth());
					edgesX.add((int) (w.getX() + w.getWidth() - widget
							.getWidth()));
				}
			}
		}

		for (Integer edge : edgesX) {
			if (Math.abs(edge - nativeX) <= snapRange) {
				nativeX = edge;
				break;
			}
		}

		for (Integer edge : edgesY) {
			if (Math.abs(edge - nativeY) <= snapRange) {
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
		Log.w("touchlog",
				String.format(
						"DragLayer.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d",
						event.getX(), event.getY(), event.getAction(),
						event.getActionMasked()));
		if (dragInProgress_widgetFromList != null) {
			handleDrag_widgetFromList(event);
			return true;
		} else if (dragInProgress_widgetOnWidgetLayer != null) {
			handleDrag_widgetOnWidgetLayer(event);
			return true;
		} else if (resizeInProgress != null) {
			handleResizing(event);
			return true;
		}
		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		Log.w("touchlog",
				String.format(
						"DragLayer.onInterceptTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d",
						event.getX(), event.getY(), event.getAction(),
						event.getActionMasked()));
		if (dragInProgress_widgetFromList != null) {
			MapWidget widget = dragInProgress_widgetFromList;
			Log.d("touchlog",
					"DragLayer is now handling a dragged MapWidget from list, intercepting...");
			// remove widget from parent LinearLayout
			dragInProgress_widgetFromList_layout.removeView(widget);
			// add widget as own child
			addView(widget);
			// change widget mode to full size
			widget.setMode(MapWidget.FULLSIZE_MODE);
			// handle move event
			onTouchEvent(event);
			return true;
		}
		if (dragInProgress_widgetOnWidgetLayer != null) {
			Log.d("touchlog",
					"DragLayer is now handling a dragged MapWidget on WidgetLayer, intercepting...");
			onTouchEvent(event);
			return true;
		}
		if (resizeInProgress != null) {
			Log.d("touchlog",
					"DragLayer is now handling the resizing of a MapWidget, intercepting...");
			onTouchEvent(event);
			return true;
		}

		return false;
	}

	public void startWidgetDraggingFromList(MapWidget widget) {
		dragInProgress_widgetFromList = widget;
		dragInProgress_widgetFromList_layout = (LinearLayout) widget
				.getParent();
		// dragInProgress_widgetFromList_oldIndex =
		// dragInProgress_widgetFromList_layout.indexOfChild(widget);
	}

	public void startWidgetDraggingOnWidgetLayer(MapWidget widget, float mX,
			float mY) {
		this.dragInProgress_widgetOnWidgetLayer = widget;
		this.dragInProgress_widgetOnWidgetLayer_mX = mX;
		this.dragInProgress_widgetOnWidgetLayer_mY = mY;
	}

	public void startWidgetResizing(CornerResizer cornerResizer, Float mX,
			Float mY) {
		this.resizeInProgress = cornerResizer;
		this.resizeInProgress_mX = mX;
		this.resizeInProgress_mY = mY;
		
		MapWidget widget = (MapWidget) resizeInProgress.getParent();
		RelativeLayout.LayoutParams pWidget = (RelativeLayout.LayoutParams) widget.getLayoutParams();
		resizeCache_width = (float) pWidget.width;
		resizeCache_height = (float) pWidget.height;
		resizeCache_x = (float) pWidget.leftMargin;
		resizeCache_y = (float) pWidget.topMargin;		
	}
}
