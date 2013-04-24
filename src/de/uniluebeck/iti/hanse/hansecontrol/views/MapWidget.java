package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.LinkedList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import de.uniluebeck.iti.hanse.hansecontrol.BitmapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreenFragment;
import de.uniluebeck.iti.hanse.hansecontrol.MapWidgetRegistry;
import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.ClipData.Item;
import android.database.Observable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView.FindListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RemoteViews.ActionException;

/**
 * This is the base class of all widgets.
 * It is responsible for functionality which is 
 * available for all widgets like dragging, resizing, closing.
 * 
 * @author Stefan Hueske
 */
public class MapWidget extends BasicView {
	
	public static final int ICON_MODE = 0;
	public static final int FULLSIZE_MODE = 1;
	
	private int currentMode = ICON_MODE;
	
	Float mX, mY; //last position while dragging
	
	DragLayer dragLayer = null;
	MapWidgetRegistry mapWidgetRegistry;
	MainScreenFragment mainScreenFragment;
	
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

	CloseButton closeButton;
	
	ShowContextMenuButton showContextMenuButton;
	
	CornerResizer cornerResizerTopLeft;
	CornerResizer cornerResizerTopRight;
	CornerResizer cornerResizerBottomLeft;
	CornerResizer cornerResizerBottomRight;
	
	private boolean controlsVisible = false;
	private ScheduledFuture autoHideFuture = null;
	
	RemoveWidgetButton removeWidgetButton;
	boolean removeWidgetButtonVisible = false;	
	
	
	//TODO use bitmapmanager!
//	Bitmap bitmap_closeButton, bitmap_resizer;
	
	public MapWidget(int defaultWidth, int defaultHeight, int widgetID, Context context, DragLayer dragLayer, MapWidgetRegistry mapWidgetRegistry, MainScreenFragment mainScreenFragment) {
		super(context);
		this.mapWidgetRegistry = mapWidgetRegistry;
		this.mainScreenFragment = mainScreenFragment;
		this.widgetID = widgetID;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		setId(widgetID);
		setDragLayer(dragLayer);
		init();
	}
	
	private void init() {
		if (DEBUG_MODE && !(this instanceof RosMapWidget)) {
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
		initCloseButton();
		initCornerResizer();
		initRemoveWidgetButton();
		initShowContextMenuButton();
		
//		bitmap_closeButton = BitmapFactory.decodeResource(getResources(), R.drawable.trashbin);
//		bitmap_closeButton = BitmapManager.getInstance().getBitmap(getResources(), R.drawable.trashbin);
//		bitmap_resizer = BitmapFactory.decodeResource(getResources(), R.drawable.resize);
//		bitmap_resizer = BitmapManager.getInstance().getBitmap(getResources(), R.drawable.resize);
		
	}
	
	private void initShowContextMenuButton() {
		showContextMenuButton = new ShowContextMenuButton(getContext(), this);
		addView(showContextMenuButton);
		int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, width);
		showContextMenuButton.setLayoutParams(params);
		showContextMenuButton.setVisibility(View.INVISIBLE);
	}
	
	private void initRemoveWidgetButton() {
		removeWidgetButton = new RemoveWidgetButton(getContext(), this);
		addView(removeWidgetButton);
		int removeWidgetButtonWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(removeWidgetButtonWidth, removeWidgetButtonWidth);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		removeWidgetButton.setLayoutParams(params);
		removeWidgetButton.setVisibility(View.INVISIBLE);
	}
	
	private void initCloseButton() {
		closeButton = new CloseButton(getContext(), this);
		addView(closeButton);
		int closeButtonWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(closeButtonWidth, closeButtonWidth);
//		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		closeButton.setLayoutParams(params);
		closeButton.setVisibility(View.INVISIBLE);
	}
	
	private void initCornerResizer() {
		cornerResizerTopLeft = new CornerResizer(getContext(), CornerResizer.TOP_LEFT);
		cornerResizerTopRight = new CornerResizer(getContext(), CornerResizer.TOP_RIGHT);
		cornerResizerBottomLeft = new CornerResizer(getContext(), CornerResizer.BOTTOM_LEFT);
		cornerResizerBottomRight = new CornerResizer(getContext(), CornerResizer.BOTTOM_RIGHT);
		
		addView(cornerResizerTopLeft);
		addView(cornerResizerTopRight);
		addView(cornerResizerBottomLeft);
		addView(cornerResizerBottomRight);
		
		int cornerResizerWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(cornerResizerWidth, cornerResizerWidth);
		
		cornerResizerTopLeft.setLayoutParams(new RelativeLayout.LayoutParams(params));
		cornerResizerTopRight.setLayoutParams(new RelativeLayout.LayoutParams(params));
		cornerResizerBottomLeft.setLayoutParams(new RelativeLayout.LayoutParams(params));
		cornerResizerBottomRight.setLayoutParams(new RelativeLayout.LayoutParams(params));
		
		cornerResizerTopLeft.setVisibility(View.INVISIBLE);
		cornerResizerTopRight.setVisibility(View.INVISIBLE);
		cornerResizerBottomLeft.setVisibility(View.INVISIBLE);
		cornerResizerBottomRight.setVisibility(View.INVISIBLE);
	}
	
	public MainScreenFragment getMainScreenFragment() {
		return mainScreenFragment;
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
		
		//single tap on "empty space" in MapWidget in Fullsize-Mode
		if (getMode() == FULLSIZE_MODE && event.getActionMasked() == MotionEvent.ACTION_UP) {
			//play animation to make closebutton and cornerresizers visible
			Log.d("animator", "animation started");
			
//			cornerResizerTopLeft.setYPos(20);
//			cornerResizerTopLeft.setXPos(20);
			
			if (controlsVisible) {
				hideControls();
			} else {
				showControls(event);
			}
		}
		
		//single tap on "empty space" in MapWidget in Icon-Mode
		if (getMode() == ICON_MODE && event.getActionMasked() == MotionEvent.ACTION_UP) {
			if (removeWidgetButton.getVisibility() == View.VISIBLE) {
				removeWidgetButton.setAlpha(1);
				fadeObjects(500, 0, removeWidgetButton).addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						removeWidgetButton.setVisibility(View.INVISIBLE);
					}
				});
			} else {
				removeWidgetButton.setAlpha(0);
				fadeObjects(500, 1, removeWidgetButton);
				removeWidgetButton.setVisibility(View.VISIBLE);
				MainScreen.executorService.schedule(new Runnable() {
					
					@Override
					public void run() {
						removeWidgetButton.post(new Runnable() {
							
							@Override
							public void run() {
								fadeObjects(500, 0, removeWidgetButton).addListener(new AnimatorListenerAdapter() {
									@Override
									public void onAnimationEnd(Animator animation) {
										removeWidgetButton.setVisibility(View.INVISIBLE);
										removeWidgetButtonVisible = false;																		
									}
								});
							}
						});
					}
				}, 2000, TimeUnit.MILLISECONDS);
			}
		}
		
		int minDragDistance = 5;
		
		//dragging on WidgetLayer
		if (getParent() instanceof WidgetLayer && event.getActionMasked() == MotionEvent.ACTION_MOVE && dragLayer != null
				&& Math.abs(mX - event.getX()) >= minDragDistance && Math.abs(mY - event.getY()) >= minDragDistance) {
			//request touch event interception from dragLayer
			dragLayer.startWidgetDraggingOnWidgetLayer(this, mX, mY);
			return true;
		}
		
		//dragging on WidgetList
		if (getParent() instanceof LinearLayout && event.getActionMasked() == MotionEvent.ACTION_MOVE && dragLayer != null) {
			float distY = mY - event.getY();
			float distX = mX - event.getX();
			float len = (float) Math.sqrt(Math.pow(distX, 2) + Math.pow(distY, 2));
			float alpha = (float) Math.acos(distY/len);
			
//			Log.w("touchcalc", String.format("distx: %f, disty: %f, len: %f, alpha: %f", distX, distY, len, alpha));
			
			if (distY > 20 && alpha < Math.PI / 4) {
				//dragging up
				dragLayer.startWidgetDraggingFromList(this);
				return true;
			}
		}
		return super.onTouchEvent(event);
	}
	
	public void showControls() {
		MotionEvent event = MotionEvent.obtain(0, 0, 0, getWidth() / 2, getHeight() / 2, 0, 0, 0, 0, 0, 0, 0);
		showControls(event);
	}
	
	public void showControls(MotionEvent event) {
		//reset previous layout settings
		((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
		((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
		((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
		((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
		((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
		((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
		((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
		((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
		((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
		
		fadeObjects(1, 0, cornerResizerTopLeft, cornerResizerTopRight, cornerResizerBottomLeft, cornerResizerBottomRight, closeButton, showContextMenuButton);
		fadeObjects(200, 1, cornerResizerTopLeft, cornerResizerTopRight, cornerResizerBottomLeft, cornerResizerBottomRight, closeButton, showContextMenuButton);
		
		int duration = 400;
		
		cornerResizerTopLeft.setVisibility(View.VISIBLE);
		ObjectAnimator cornerResizerTopLeft_x = ObjectAnimator.ofFloat(cornerResizerTopLeft, "xPos", event.getX(), 0);
		cornerResizerTopLeft_x.setDuration(duration);
		ObjectAnimator cornerResizerTopLeft_y = ObjectAnimator.ofFloat(cornerResizerTopLeft, "yPos", event.getY(), 0);
		cornerResizerTopLeft_y.setDuration(duration);
		
		cornerResizerTopRight.setVisibility(View.VISIBLE);
		ObjectAnimator cornerResizerTopRight_x = ObjectAnimator.ofFloat(cornerResizerTopRight, "xPos", event.getX(), getWidth() - cornerResizerTopRight.getWidth());
		cornerResizerTopRight_x.setDuration(duration);
		ObjectAnimator cornerResizerTopRight_y = ObjectAnimator.ofFloat(cornerResizerTopRight, "yPos", event.getY(), 0);
		cornerResizerTopRight_y.setDuration(duration);
		
		cornerResizerBottomLeft.setVisibility(View.VISIBLE);
		ObjectAnimator cornerResizerBottomLeft_x = ObjectAnimator.ofFloat(cornerResizerBottomLeft, "xPos", event.getX(), 0);
		cornerResizerBottomLeft_x.setDuration(duration);
		ObjectAnimator cornerResizerBottomLeft_y = ObjectAnimator.ofFloat(cornerResizerBottomLeft, "yPos", event.getY(), getHeight() - cornerResizerBottomLeft.getHeight());
		cornerResizerBottomLeft_y.setDuration(duration);
		
		cornerResizerBottomRight.setVisibility(View.VISIBLE);
		ObjectAnimator cornerResizerBottomRight_x = ObjectAnimator.ofFloat(cornerResizerBottomRight, "xPos", event.getX(), getWidth() - cornerResizerBottomRight.getWidth());
		cornerResizerBottomRight_x.setDuration(duration);
		ObjectAnimator cornerResizerBottomRight_y = ObjectAnimator.ofFloat(cornerResizerBottomRight, "yPos", event.getY(), getHeight() - cornerResizerBottomRight.getHeight());
		cornerResizerBottomRight_y.setDuration(duration);
		
		closeButton.setVisibility(View.VISIBLE);
		ObjectAnimator closeButton_x = ObjectAnimator.ofFloat(closeButton, "xPos", event.getX(), (getWidth() - 1) / 2 - (closeButton.getWidth() - 1) / 2);
		closeButton_x.setDuration(duration);
		ObjectAnimator closeButton_y = ObjectAnimator.ofFloat(closeButton, "yPos", event.getY(), 0);
		closeButton_y.setDuration(duration);
		
		showContextMenuButton.setVisibility(View.VISIBLE);
		ObjectAnimator showContextMenuButton_x = ObjectAnimator.ofFloat(showContextMenuButton, "xPos", event.getX(), (getWidth() - 1) / 2 - (showContextMenuButton.getWidth() - 1) / 2);
		showContextMenuButton_x.setDuration(duration);
		ObjectAnimator showContextMenuButton_y = ObjectAnimator.ofFloat(showContextMenuButton, "yPos", event.getY(), getHeight() - showContextMenuButton.getHeight());
		showContextMenuButton_y.setDuration(duration);
		
		
		AnimatorSet showControls = new AnimatorSet();
		showControls
			.play(cornerResizerTopLeft_x)
			.with(cornerResizerTopLeft_y)
			.with(cornerResizerTopRight_x)
			.with(cornerResizerTopRight_y)
			.with(cornerResizerBottomLeft_x)
			.with(cornerResizerBottomLeft_y)
			.with(cornerResizerBottomRight_x)
			.with(cornerResizerBottomRight_y)
			.with(closeButton_x)
			.with(closeButton_y)
			.with(showContextMenuButton_x)
			.with(showContextMenuButton_y);
		
		showControls.start();
		
		showControls.addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) { }
			
			@Override
			public void onAnimationRepeat(Animator animation) { }
			
			@Override
			public void onAnimationEnd(Animator animation) {
				//set margins to zero, preventing issues while layout resizing
				((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).topMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).topMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).topMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).topMargin = 0;
				((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).topMargin = 0;
				((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).leftMargin = 0;
				((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).topMargin = 0;
				
				((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
				((RelativeLayout.LayoutParams) cornerResizerTopLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
				((RelativeLayout.LayoutParams) cornerResizerTopRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				((RelativeLayout.LayoutParams) cornerResizerBottomLeft.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				((RelativeLayout.LayoutParams) cornerResizerBottomRight.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
				((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
				((RelativeLayout.LayoutParams) closeButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_TOP);
				((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
				((RelativeLayout.LayoutParams) showContextMenuButton.getLayoutParams()).addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				
				controlsVisible = true;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) { }
		});
		
		delayHideControls();
	}
	
	public void cancelDelayedHideControls() {
		if (autoHideFuture != null) {
			autoHideFuture.cancel(false);
		}
	}
	
	public void delayHideControls() {
		cancelDelayedHideControls();
		autoHideFuture = MainScreen.executorService.schedule(new Runnable() {
			
			@Override
			public void run() {
				Log.d("test", "executorTask");
				//run task on UI thread
				post(new Runnable() {
					
					@Override
					public void run() {
						hideControls();
					}
				});
			}
		}, 3000, TimeUnit.MILLISECONDS);
	}
	
	public void hideControls() {
		if (!controlsVisible) {
			return;
		}
		fadeObjects(500, 0, cornerResizerTopLeft, cornerResizerTopRight, cornerResizerBottomLeft, cornerResizerBottomRight, closeButton, showContextMenuButton).addListener(new Animator.AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {}
			
			@Override
			public void onAnimationRepeat(Animator animation) {}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				controlsVisible = false;
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {}
			
		});
	}
	
	private AnimatorSet fadeObjects(int duration, float alpha, Object... objects) {
		AnimatorSet animatorSet = new AnimatorSet();
		List<Animator> animations = new LinkedList<Animator>();
		for (Object object : objects) {
			ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(object, "alpha", alpha);
			fadeAnimator.setDuration(duration);
			animations.add(fadeAnimator);
		}
		animatorSet.playTogether(animations);
		animatorSet.start();
		return animatorSet;
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
				if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
					RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) getLayoutParams();
	//				params2.width = 200;
	//				params2.height = 200;
					params2.width = defaultWidth;
					params2.height = defaultHeight;
					
					setLayoutParams(params2);
				}
				break;
		}
	}
	
	
	public void savePrefs(String tabPrefix, SharedPreferences.Editor ed) {
		String id = null;
		if (this instanceof RosMapWidget) {
			RosMapWidget rw = (RosMapWidget) this;
			id = tabPrefix + WIDGET_PREFIX + rw.getRosTopic() + rw.getWidgetType().name();
		} else {
			id = tabPrefix + WIDGET_PREFIX + widgetID;
		}
		
		ed.putInt(id+"-currentMode", currentMode);
//		Log.d("ttt1save", id+"-currentMode");
		if (getMode() == FULLSIZE_MODE) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			ed.putInt(id+"-params.leftMargin", params.leftMargin);
			ed.putInt(id+"-params.topMargin", params.topMargin);
			ed.putInt(id+"-params.width", params.width);
			ed.putInt(id+"-params.height", params.height);
		}
	}
	
	public void loadPrefs(String tabPrefix, SharedPreferences prefs) {
		String id = null;
		if (this instanceof RosMapWidget) {
			RosMapWidget rw = (RosMapWidget) this;
			id = tabPrefix + WIDGET_PREFIX + rw.getRosTopic() + rw.getWidgetType().name();
		} else {
			id = tabPrefix + WIDGET_PREFIX + widgetID;
		}
		
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
	
	public class AnimatedView extends View {
		
		public AnimatedView(Context context) {
			super(context);
		}
		
		public float getXPos() {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			return params.leftMargin;
		}
		
		public float getYPos() {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			return params.topMargin;
		}
		
		public void setXPos(float xPos) {
//			Log.d("animatortest", "setXPos() " + xPos);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.leftMargin = (int) xPos;
			setLayoutParams(params);
		}
		
		public void setYPos(float yPos) {
//			Log.d("animatortest", "setYPos() " + yPos);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) getLayoutParams();
			params.topMargin = (int) yPos;
			setLayoutParams(params);
		}
	}
	
	public class CloseButton extends AnimatedView {
		Paint paint;
		MapWidget parentWidget;
		
		public CloseButton(Context context, MapWidget parentWidget) {
			super(context);
			paint = new Paint();
			this.parentWidget = parentWidget;
//			parentWidget.getMainScreenFragment().getActivity().registerForContextMenu(this);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (getMode() == FULLSIZE_MODE) {
				super.onDraw(canvas);
//				canvas.drawLine(0, 0, getWidth() - 1, getHeight() - 1, paint);
//				canvas.drawLine(0, getHeight() - 1, getWidth() - 1, 0, paint);
				
				//TODO consider to use a async task instead!
//				canvas.drawBitmap(bitmap_closeButton, 0, 0, null);
				canvas.drawBitmap(BitmapManager.getInstance().getBitmap(getResources(), R.drawable.trashbin), null, new RectF(0, 0, getWidth(), getHeight()), null);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (!controlsVisible) {
				return false;
			}
			Log.w("touchlog", String.format("MapWidget.CloseButton.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
					event.getAction(), event.getActionMasked()));
			if (getMode() == FULLSIZE_MODE) {
				//TODO remove test
//				parentWidget.getMainScreenFragment().getActivity().openContextMenu(this);
				//TODO uncomment
				hideControls();
				WidgetLayer widgetLayer = (WidgetLayer) parentWidget.getParent();
				widgetLayer.removeWidget(parentWidget);
				return true;
			}
			return false;
		}
//		private void layoutControls
//		
//		@Override
//		protected void onLayout(boolean changed, int left, int top, int right,
//				int bottom) {
//			super.onLayout(changed, left, top, right, bottom);
//			
//		}
	}
	
	public class CornerResizer extends AnimatedView {
		
		public static final int TOP_LEFT = 0;
		public static final int TOP_RIGHT = 1;
		public static final int BOTTOM_LEFT = 2;
		public static final int BOTTOM_RIGHT = 3;
		
		private int corner;
		Paint paint;
		
		private Float mX, mY;
		
		public CornerResizer(Context context, int corner) {
			super(context);
			this.corner = corner;
			paint = new Paint();
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
//			canvas.drawLine(0, 0, getWidth() - 1, getHeight() - 1, paint);
//			canvas.drawLine(0, getHeight() - 1, getWidth() - 1, 0, paint);
			canvas.drawBitmap(BitmapManager.getInstance().getBitmap(getResources(), R.drawable.resize), null, new RectF(0, 0, getWidth(), getHeight()), null);
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			Log.w("touchlog", String.format("MapWidget.CornerResizer.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
					event.getAction(), event.getActionMasked()));
			if (!controlsVisible) {
				return false;
			}
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
			//dragging on WidgetLayer
			if (getMode() == FULLSIZE_MODE && event.getActionMasked() == MotionEvent.ACTION_MOVE && dragLayer != null) {
				//request touch event interception from dragLayer
				Log.d("touchlog", "CornerResizer: requesting interception from draglayer...");
				dragLayer.startWidgetResizing(this, mX, mY);
//				return true;
			}
			return true;
		}
		
		public int getCorner() {
			return corner;
		}
	}
	
	public class RemoveWidgetButton extends AnimatedView {
		Paint paint;
		MapWidget parentWidget;
		
		public RemoveWidgetButton(Context context, MapWidget parentWidget) {
			super(context);
			paint = new Paint();
			this.parentWidget = parentWidget;
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (getMode() == ICON_MODE) {
				super.onDraw(canvas);
//				canvas.drawLine(0, 0, getWidth() - 1, getHeight() - 1, paint);
//				canvas.drawLine(0, getHeight() - 1, getWidth() - 1, 0, paint);
				
				//TODO consider to use a async task instead!
//				canvas.drawBitmap(bitmap_closeButton, 0, 0, null);
				canvas.drawBitmap(BitmapManager.getInstance().getBitmap(getResources(), R.drawable.trash_icon2), null, new RectF(0, 0, getWidth(), getHeight()), null);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (getVisibility() != View.VISIBLE || getMode() != ICON_MODE) {
				return false;
			}
//			Log.w("touchlog", String.format("MapWidget.CloseButton.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
//					event.getAction(), event.getActionMasked()));
			LinearLayout widgetLayout = (LinearLayout) parentWidget.getParent();
			widgetLayout.removeView(parentWidget);
			mapWidgetRegistry.getAllWidgets().remove(parentWidget);
			return false;
		}
		
//		private void layoutControls
//		
//		@Override
//		protected void onLayout(boolean changed, int left, int top, int right,
//				int bottom) {
//			super.onLayout(changed, left, top, right, bottom);
//			
//		}
	}
	
	public class ShowContextMenuButton extends AnimatedView {
		Paint paint;
		MapWidget parentWidget;
		
		public ShowContextMenuButton(Context context, MapWidget parentWidget) {
			super(context);
			paint = new Paint();
			this.parentWidget = parentWidget;
			parentWidget.getMainScreenFragment().getActivity().registerForContextMenu(this);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			if (getMode() == FULLSIZE_MODE) {
				super.onDraw(canvas);
//				canvas.drawLine(0, 0, getWidth() - 1, getHeight() - 1, paint);
//				canvas.drawLine(0, getHeight() - 1, getWidth() - 1, 0, paint);
				
				//TODO consider to use a async task instead!
//				canvas.drawBitmap(bitmap_closeButton, 0, 0, null);
				canvas.drawBitmap(BitmapManager.getInstance().getBitmap(getResources(), R.drawable.gears_icon), null, new RectF(0, 0, getWidth(), getHeight()), null);
			}
		}
		
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			if (!controlsVisible) {
				return false;
			}
			Log.w("touchlog", String.format("MapWidget.CloseButton.onTouchEvent(): x: %f, y: %f, action: %d, actionmasked: %d", event.getX(), event.getY(), 
					event.getAction(), event.getActionMasked()));
			if (getMode() == FULLSIZE_MODE && event.getActionMasked() == MotionEvent.ACTION_UP) {
				parentWidget.getMainScreenFragment().getActivity().openContextMenu(this);
				//TODO uncomment
//				hideControls();
//				WidgetLayer widgetLayer = (WidgetLayer) parentWidget.getParent();
//				widgetLayer.removeWidget(parentWidget);
			}
			return true;
		}
		
		public MapWidget getParentWidget() {
			return parentWidget;
		}
		
		public void performAction(MenuItem item) {
			if (item.getItemId() == R.id.close_other) {
				parentWidget.getMainScreenFragment().closeAllOtherMapWidgets(parentWidget);
			} else if (item.getItemId() == R.id.move_to_new_tab && parentWidget instanceof RosMapWidget) {
				parentWidget.getMainScreenFragment().closeMapWidgetAndMoveToNewTab((RosMapWidget)parentWidget);
			}
		}
	}
	
	
//	@Override
//	public void forceLayout() {
//		super.forceLayout();
//		cornerResizerTopLeft.forceLayout();
//		cornerResizerTopRight.forceLayout();
//		cornerResizerBottomLeft.forceLayout();
//		cornerResizerBottomRight.forceLayout();
//		closeButton.forceLayout();
//	}
}
