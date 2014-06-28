package de.uniluebeck.iti.hanse.hansecontrol.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import de.uniluebeck.iti.hanse.hansecontrol.gui.Tabbar.Tab;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

public class Tabbar extends RelativeLayout {

	Tabholder tabholder;

	AddTabListener addTabListener;
	
	Paint separatorLinePaint;
	
	public Tabbar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public Tabbar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Tabbar(Context context) {
		super(context);
		init();
	}

	private void init() {
		tabholder = new Tabholder(getContext());
		addView(tabholder);
		tabholder.setId(15); //TODO generate IDs
		setBackgroundColor(Color.parseColor("#48000000"));
		setWillNotDraw(false);
		separatorLinePaint =  new Paint();
		separatorLinePaint.setColor(Color.DKGRAY);
		separatorLinePaint.setStrokeWidth(dipToPixels(5, getContext()));
		
		Button addTabButton = new Button(getContext());
		addTabButton.setText("+");
		addView(addTabButton);
		addTabButton.setId(16); //TODO generate IDs
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) addTabButton.getLayoutParams();
		p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		p.width = dipToPixels(10, getContext());
		p.height = LayoutParams.MATCH_PARENT;
//		p.leftMargin = currentPos;
		addTabButton.setLayoutParams(p);
		
		addTabButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (addTabListener != null) {
					addTabListener.onAddTab();
				}
			}
		});
		
		p = (RelativeLayout.LayoutParams) tabholder.getLayoutParams();
		p.addRule(RelativeLayout.LEFT_OF, addTabButton.getId());
		p.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		p.width = 200;
//		p.leftMargin = currentPos;
		tabholder.setLayoutParams(p);
		
		
		// TODO remove debug code below
		final Tab dummyTab1 = new Tab() {
			String name = "DummyTab1";

			@Override
			public void onSelected() {
				Log.d("tabbar", name + "onSelected()");
			}

			@Override
			public void onDeselect() {
				Log.d("tabbar", name + "onDeselect()");
			}

			@Override
			public String getTabName() {
				return name;
			}

			@Override
			public void onTabClose() {
				Log.d("tabbar", name + "onTabClose()");
			}
		};

		Tab dummyTab2 = new Tab() {
			String name = "DummyTab2";

			@Override
			public void onSelected() {
				Log.d("tabbar", name + "onSelected()");
			}

			@Override
			public void onDeselect() {
				Log.d("tabbar", name + "onDeselect()");
			}

			@Override
			public String getTabName() {
				return name;
			}

			@Override
			public void onTabClose() {
				Log.d("tabbar", name + "onTabClose()");
			}
		};

		Tab dummyTab3 = new Tab() {
			String name = "DummyTab3";

			@Override
			public void onSelected() {
				Log.d("tabbar", name + "onSelected()");
			}

			@Override
			public void onDeselect() {
				Log.d("tabbar", name + "onDeselect()");
			}

			@Override
			public String getTabName() {
				return name;
			}

			@Override
			public void onTabClose() {
				Log.d("tabbar", name + "onTabClose()");
			}
		};

//		addTab(dummyTab1);
//		addTab(dummyTab2);
//		addTab(dummyTab3);
//		addTab(dummyTab1);
//		addTab(dummyTab2);
//		addTab(dummyTab3);

		setAddTabListener(new AddTabListener() {
			
			@Override
			public void onAddTab() {
				addTab(dummyTab1);
			}
		});
		
	}

	public void addTab(Tab tab) {
		tabholder.addTab(tab);
	}

	public static interface Tab {
		public String getTabName();

		public void onSelected();

		public void onDeselect();

		public void onTabClose();
	}
	
	public void setAddTabListener(AddTabListener addTabListener) {
		this.addTabListener = addTabListener;
	}
	
	public static interface AddTabListener {
		public void onAddTab();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawLine(0, getHeight(), getWidth(), getHeight(), separatorLinePaint);
	}
	
	public int getTabCount() {
		return tabholder.tabViews.size();
	}
}

class TabView extends RelativeLayout {
	Tab tab;
	Paint paint;
	Paint backgroundPaintActive;
	Paint backgroundPaintInactive;
	
	private float xDown;
	private int xThreshold;
	
	TabViewListener listener;
	
	public static final int DESELECTED = 0;
	public static final int SELECTED = 1;
	private int selectionMode = DESELECTED;
	
	View content;
	
	public TabView(Context context, final Tab tab) {
		super(context);
		this.tab = tab;
		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setTextSize(30f);
		
		backgroundPaintActive = new Paint();
		backgroundPaintActive.setColor(Color.parseColor("#78dfea22"));
		backgroundPaintInactive = new Paint();
		backgroundPaintInactive.setColor(Color.parseColor("#30000000"));
		
		
		xThreshold = dipToPixels(5, context);
//		setWillNotDraw(false);
		
//		setBackgroundColor(Color.CYAN);
		content = new View(getContext()) {
			@Override
			protected void onDraw(Canvas canvas) {
				int p = dipToPixels(3, getContext()); //padding
				if (selectionMode == SELECTED) {
					canvas.drawRect(new Rect(p, p, getWidth()-p, getHeight()-p), backgroundPaintActive);
				} else {
					canvas.drawRect(new Rect(p, p, getWidth()-p, getHeight()-p), backgroundPaintInactive);
				}
				canvas.drawLine(p, p, getWidth()-p, p, paint);
				canvas.drawLine(getWidth()-p, p, getWidth()-p, getHeight()-p, paint);
				canvas.drawLine(p, p, p, getHeight()-p, paint);
				
				canvas.drawText(tab.getTabName(), p+20, p+30, paint);
			}
		};
		addView(content);
		
		Button closeTabButton = new Button(getContext());
		closeTabButton.setText("x");
		addView(closeTabButton);
		RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) closeTabButton.getLayoutParams();
//		p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		int m = dipToPixels(5, getContext());
		p.setMargins(m, 2*m, m, m);
		p.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		p.width = dipToPixels(30, getContext());
		p.height = dipToPixels(30, getContext());
//		p.leftMargin = currentPos;
		closeTabButton.setLayoutParams(p);
		closeTabButton.setPadding(0, -dipToPixels(10, getContext()), 0, 0);
//		closeTabButton.setMinimumHeight(0);
//		closeTabButton.setMinimumWidth(0);
		
//		closeTabButton.set
		
		closeTabButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onTabClose(TabView.this);
				}
			}
		});
		
		
		
	}

	
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		int p = dipToPixels(3, getContext()); //padding
//		if (selectionMode == SELECTED) {
//			canvas.drawRect(new Rect(p, p, getWidth()-p, getHeight()-p), backgroundPaint);
//		}
//		canvas.drawLine(p, p, getWidth()-p, p, paint);
//		canvas.drawLine(getWidth()-p, p, getWidth()-p, getHeight()-p, paint);
//		canvas.drawLine(p, p, p, getHeight()-p, paint);
//		
//		canvas.drawText(tab.getTabName(), p+20, p+30, paint);
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = ev.getX();
			listener.onDown(this, ev.getX());
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(xDown - ev.getX()) > xThreshold) {
				if (listener != null) {
					listener.onDrag(this, ev.getX());
				}
			}
			break;
		}
		return true;
	}
	
	public void setSelectionMode(int selectionMode) {
		if (this.selectionMode == SELECTED && selectionMode == DESELECTED) {
			tab.onDeselect();
		} else if (this.selectionMode == DESELECTED && selectionMode == SELECTED) {
			tab.onSelected();
		}
		this.selectionMode = selectionMode;
		content.invalidate();
	}
	
	public void setListener(TabViewListener listener) {
		this.listener = listener;
	}
	
	static interface TabViewListener {
		public void onDrag(TabView tabView, float xPos);
		public void onDown(TabView tabView, float xPos);
		public void onTabClose(TabView tabView);
	}
}

class Tabholder extends RelativeLayout {
//	private int xThreshold;
//	private float xDown;
//	
//	public static final int DEFAULT = 0;
//	public static final int DRAG = 1;
//	private int mode = DEFAULT;
	
	TabView dragTab = null;
	float dragXPos;
	
	LinkedList<TabView> tabViews = new LinkedList<TabView>();
	Map<TabView, ValueAnimator> tabXanimatorMap = new HashMap<TabView, ValueAnimator>();	
	Map<TabView, Integer> targetXvalueMap = new HashMap<TabView, Integer>();
	
	public Tabholder(Context context) {
		super(context);
//		xThreshold = dipToPixels(5, context);
	}

	private void selectTab(TabView selectedTabView) {
		for (TabView v : tabViews) {
			if (v != selectedTabView) {
				v.setSelectionMode(TabView.DESELECTED);
			}
		}
		selectedTabView.setSelectionMode(TabView.SELECTED);
	}
	
	public void addTab(Tab tab) {
		TabView tabView = new TabView(getContext(), tab);
		addView(tabView);
		tabViews.add(tabView);
//		addDragListener(tabView);
		tabView.setListener(new TabView.TabViewListener() {
			
			@Override
			public void onDrag(TabView tabView, float xPos) {
				dragTab = tabView;
				dragXPos = xPos;
			}
			
			@Override
			public void onDown(TabView tabView, float xPos) {
				selectTab(tabView);
			}
			
			@Override
			public void onTabClose(TabView tabView) {
				removeTab(tabView);
			}
		});
		updateTabLayout();
		selectTab(tabView);
	}
	
	public void removeTab(TabView tabView) {
		tabViews.remove(tabView);
		removeView(tabView);
		updateTabLayout();
	}

	private void animateTabX(final TabView v, int x) {
		if (tabXanimatorMap.get(v) != null) {
			tabXanimatorMap.get(v).cancel();
		}
		targetXvalueMap.put(v, x);
		ValueAnimator tabXanimator = ValueAnimator.ofInt((int)v.getX(), x);
		tabXanimatorMap.put(v, tabXanimator);
		tabXanimator.setDuration(200);
		tabXanimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) v.getLayoutParams();
				p.leftMargin = (Integer) animation.getAnimatedValue();
				v.setLayoutParams(p);
			}
		});
		tabXanimator.start();
	}
	
	private void updateTabLayout() {
		if (getChildCount() == 0) {
			return;
		}
		if (getWidth() == 0) {
			addLayoutListener(this, new Runnable() {
				@Override
				public void run() {
					updateTabLayout();
				}
			});
			return;
		}
		
//		Log.d("tabbar", "Tabholder width: " + getWidth());

		int tabWidth = getWidth() / getChildCount();
		tabWidth = Math.min(tabWidth, dipToPixels(300, getContext()));
		int currentPos = 0;
		
		for (TabView tv : tabViews) {
			if (tv != dragTab) {
				RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) tv.getLayoutParams();
				p.width = tabWidth;
//				p.leftMargin = currentPos;
				animateTabX(tv, currentPos);
				tv.setLayoutParams(p);
			}
			currentPos += tabWidth;
		}
		
		invalidate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
//			dragTab.setX(ev.getX() - dragXPos);
			
			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) dragTab.getLayoutParams();
//			p.width = tabWidth;
			p.leftMargin = (int)(ev.getX() - dragXPos);
			p.leftMargin = Math.max(0, p.leftMargin);
			p.leftMargin = Math.min(getWidth()-dragTab.getWidth(), p.leftMargin);			
			dragTab.setLayoutParams(p);
			
			dragTab.bringToFront();
			int vZ = -1; //z-index of nearby tab
			int dZ = -1; //z-index of dragged tab
			for (int i = 0; i < tabViews.size(); i++) {
				TabView v = tabViews.get(i);
				float vx = targetXvalueMap.get(v) == null ? v.getX() : targetXvalueMap.get(v);
				
				if (v != dragTab && Math.abs(vx - dragTab.getX()) < dragTab.getWidth() / 2) {
					vZ = i;
				}
				if (v == dragTab) {
					dZ = i;
				}
			}
			if (vZ != -1 && dZ != -1) {
				//switch tab order
				Collections.swap(tabViews, vZ, dZ);
				updateTabLayout();
			}
			break;
		case MotionEvent.ACTION_UP:
			dragTab = null;
			updateTabLayout();
			break;
		}
		return true;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (dragTab != null) {
			return onTouchEvent(ev);
		}
		return false;
	}

//	private void addDragListener(TabView tabView) {
//		tabView.setOnTouchListener(new OnTouchListener() {
//
//			@Override
//			public boolean onTouch(View v, MotionEvent ev) {
//				switch (ev.getAction()) {
//				case MotionEvent.ACTION_DOWN:
//					xDown = ev.getX();
//					break;
//				case MotionEvent.ACTION_MOVE:
//					if (mode == DEFAULT && Math.abs(xDown - ev.getX()) > xThreshold) {
//						mode = DRAG;
//					}
//					if (mode == DRAG) {
//						v.setX(ev.getX() - xDown);
//						
//					}
//					break;
//				case MotionEvent.ACTION_UP:
//					mode = DEFAULT;	
//					break;
//
//				default:
//					break;
//				}
//				return true;
//			}
//		});
//	}

}
