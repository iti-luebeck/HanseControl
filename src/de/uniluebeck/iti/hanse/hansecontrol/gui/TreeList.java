package de.uniluebeck.iti.hanse.hansecontrol.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.Tree;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.TreeValue;



import android.content.Context;
import android.content.pm.LabeledIntent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

public class TreeList<T extends TreeValue> extends LinearLayout {

//	Tree data = null;

	public static final int DRAG_RIGHT = 0;
	public static final int DRAG_LEFT = 1;
	int dragOrientation = DRAG_RIGHT;

	LinearLayout elementsLayout;
	ScrollView scrollView;
	ElementDragOutListener elementDragOutListener;
	SwipeInListener swipeInListener;

	Handler handler;
	
	public TreeList(Context context) {
		super(context);
		init(context);
	}

	public TreeList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TreeList(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setElementDragOutListener(
			ElementDragOutListener elementDragOutListener) {
		this.elementDragOutListener = elementDragOutListener;
	}
	
	public void setSwipeInListener(SwipeInListener swipeInListener) {
		this.swipeInListener = swipeInListener;
	}

	public void setDragOrientation(int dragOrientation) {
		this.dragOrientation = dragOrientation;
	}

	private void init(Context context) {
		handler = new Handler();
		setOrientation(LinearLayout.VERTICAL);
		// TODO remove test data
		
		elementsLayout = new LinearLayout(context);
		elementsLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView = new ScrollView(context);
		scrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		addView(scrollView);
		scrollView.addView(elementsLayout);

		
		
		
//		DummyTreeValue a = new DummyTreeValue("/this/is/a/long/topic/path",
//				"test_type");
//		DummyTreeValue b = new DummyTreeValue(
//				"/this/is/another/long/topic/path", "other_test_type");
//
//		@SuppressWarnings({ "unused", "unchecked" })
//		Tree<DummyTreeValue> tree = new Tree<DummyTreeValue>(a,
//				new Tree<DummyTreeValue>(a, new Tree<DummyTreeValue>(a),
//						new Tree<DummyTreeValue>(b)), new Tree<DummyTreeValue>(
//						b, new Tree<DummyTreeValue>(a),
//						new Tree<DummyTreeValue>(b)));
////		addData(tree);
////		addData(tree);
//		setData(Arrays.asList(new Tree[]{tree,tree}));

	}

	// TODO check usage of R.layout.treelist_item, remove if unused
	// TODO remove test class
	private class DummyTreeValue implements TreeValue {
		String name, desc;

		public DummyTreeValue(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return desc;
		}

		@Override
		public View getView() {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.treelist_item, null);
			((TextView) view.findViewById(R.id.topic)).setText(name);
			((TextView) view.findViewById(R.id.type)).setText(desc);

			return view;
		}
	}

//	public void addData(Tree data) {
////		this.data = data;
//		addDataTree(data);
//		invalidate();
//	}
	
	public void setData(final List<Tree<T>> data) {
//		this.data = data;
		
		
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				Log.d("topicdiscoveryTest", "adding " + data.size());
////				handler.post(new Runnable() {
//					
//					@Override
//					public void run() {
						clear();
//					}
//				});
				for (final Tree<T> t : data) {
//					handler.post(new Runnable() {
						
//						@Override
//						public void run() {
							addDataTree(t);
//						}
//					});
				}
//				handler.
//				handler.post(new Runnable() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						invalidate();				
//						
//					}
//				});
			}
		});
	}

	public void clear() {
		elementsLayout.removeAllViews();
		invalidate();
	}
	
	private void addDataTree(Tree data) {
		TreeElement view = buildTreeElement(data, null);
		elementsLayout.addView(view);
		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		Context c = getContext();
		p.setMargins(dipToPixels(5, c), dipToPixels(5, c), dipToPixels(5, c),
				dipToPixels(5, c));
		view.setLayoutParams(p);
		invalidate();
	}

	private TreeElement buildTreeElement(Tree data, TreeElement parent) {
		TreeElement view = new TreeElement(data, getContext(), this);
		if (parent != null) {
			parent.addSubTree(view);
		}
		for (int i = 0; i < data.getSubTrees().size(); i++) {
			buildTreeElement((Tree) data.getSubTrees().get(i), view);
		}
		return view;
	}

	public static interface TreeValue {
		public View getView();
	}

	public static class Tree<T extends TreeValue> {
		T value;
		List<Tree<T>> subTrees = new LinkedList<Tree<T>>();

		public Tree() { /* empty */
		}

		public Tree(T value, Tree<T>... trees) {
			this.value = value;
			subTrees.addAll(Arrays.asList(trees));
		}

		public void setValue(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public void addSubTree(Tree<T> tree) {
			subTrees.add(tree);
		}

		public List<Tree<T>> getSubTrees() {
			return subTrees;
		}
	}

//	public static interface ElementLongPressListener {
//		public void longPress(View view, Tree tree);
//	}

	public static interface SwipeInListener {
		public void onSwipeIn();
	}
	
	public static interface ElementDragOutListener {
		public void onElementDragOut(View element, Tree tree);
	}
	
	
	
	
	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// return super.onTouchEvent(event);
	// }
}

class TreeElement extends RelativeLayout {

	Paint paintBorder;
	Paint paintBackground;

	View view;
	Tree dataTree;

	LinearLayout subtreesLayout;
	List<TreeElement> subtreesList = new LinkedList<TreeElement>();

	TreeList parent;

	MotionEvent evDown;
	
	VelocityTracker velocityTracker = VelocityTracker.obtain();
	
	public TreeElement(Tree tree, Context context, TreeList parent) {
		super(context);
		this.parent = parent;
		dataTree = tree;
		RelativeLayout.LayoutParams p = null;

		setWillNotDraw(false);
		paintBorder = new Paint();
		paintBorder.setStyle(Paint.Style.STROKE);
		paintBorder.setColor(Color.BLACK);
		paintBorder.setStrokeWidth(4);

		paintBackground = new Paint();
		paintBackground.setColor(Color.BLUE);

		//setBackgroundColor(Color.parseColor("#22000000"));
		setBackgroundColor(Color.parseColor("#4fbef5e2"));
		
		

		// p = (LayoutParams) getLayoutParams();
		// Context c = getContext();
		// p.setMargins(dipToPixels(5, c), dipToPixels(5, c), dipToPixels(5, c),
		// dipToPixels(5, c));
		// setLayoutParams(p);
		//
		// setOnLongClickListener(new OnLongClickListener() {
		//
		// @Override
		// public boolean onLongClick(View v) {
		// if (TreeElement.this.parent.longPressListener != null) {
		// TreeElement.this.parent.longPressListener.longPress(TreeElement.this,
		// dataTree);
		// }
		// // Log.d("treelist", "LongClick: " + dataTree.getValue().toString());
		// return true;
		// }
		// });
		//
		// setOnDragListener(new OnDragListener() {
		//
		// @Override
		// public boolean onDrag(View v, DragEvent event) {
		// if (TreeElement.this.parent.longPressListener != null) {
		// TreeElement.this.parent.longPressListener.longPress(TreeElement.this,
		// dataTree);
		// }
		// // Log.d("treelist", "LongClick: " + dataTree.getValue().toString());
		// return true;
		// }
		// });

		// TreeElementBackground background = new
		// TreeElementBackground(context);
		// addView(background);
		// p = (RelativeLayout.LayoutParams) background.getLayoutParams();
		// p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		// p.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		// background.setLayoutParams(p);

		view = tree.getValue().getView();
		view.setId(10); // TODO generate unique ID, needed for relative layout
						// params
		// text.setText(tree.getValue() == null ? "null" :
		// tree.getValue().getName());
		addView(view);
		p = (RelativeLayout.LayoutParams) view.getLayoutParams();
		p.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		p.width = LayoutParams.MATCH_PARENT;
		p.setMargins(dipToPixels(23, getContext()),
				dipToPixels(5, getContext()), 0, dipToPixels(3, getContext()));
		view.setLayoutParams(p);

		subtreesLayout = new LinearLayout(context);
		subtreesLayout.setOrientation(LinearLayout.VERTICAL);
		addView(subtreesLayout);
		p = (RelativeLayout.LayoutParams) subtreesLayout.getLayoutParams();
		p.addRule(RelativeLayout.BELOW, view.getId());
		p.setMargins(dipToPixels(20, getContext()), 0, 0, 0);
		p.height = LayoutParams.WRAP_CONTENT;
		p.width = LayoutParams.MATCH_PARENT;
		// p.height = 600;
		subtreesLayout.setLayoutParams(p);

		invalidate();
	}

	public void addSubTree(TreeElement tree) {
		// Log.d("treelist", "addSubTree: " +
		// tree.dataTree.getValue().getName());
		subtreesList.add(tree);
		// subtreesLayout.addView(new Button(getContext()));
		subtreesLayout.addView(tree);

		LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) tree
				.getLayoutParams();
		int margin = dipToPixels(5, getContext());
		p.setMargins(margin, margin, margin, margin);

		tree.setLayoutParams(p);
		subtreesLayout.invalidate();
		invalidate();
		// ((View)getParent()).invalidate();
		Log.d("treelist", "addSubTree(), height:" + subtreesLayout.getHeight());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(new Rect(1, 1, getWidth() - 1, getHeight() - 1),
				paintBorder);

		canvas.drawRect(new Rect(1, 1, dipToPixels(16, getContext()),
				getHeight()), paintBackground);

		// super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		double distance = 0, angle = 0;
		if (evDown != null) {
			distance = Math.sqrt(Math.pow(evDown.getX() - ev.getX(), 2) + Math.pow(evDown.getY() - ev.getY(), 2));
			//angle left:-PI/2 top:(PI or -PI) right:PI/2 bottom:0
			angle = Math.atan2(ev.getX() - evDown.getX(), ev.getY() - evDown.getY());
		}
		
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			evDown = MotionEvent.obtain(ev);
			Log.d("draglayer", "DOWN");
			disableScroll();
			velocityTracker.clear();
			break;
		case MotionEvent.ACTION_MOVE:
			velocityTracker.addMovement(ev);
			Log.d("draglayer", "angle: " + angle);
			if (distance > 50 && (angle <  Math.PI * -3/4 || angle >  Math.PI * 3/4 || angle >  Math.PI * -1/4 && angle <  Math.PI * 1/4)) {
				enableScroll();
				Log.d("draglayer", "SCROLL ENABLED");
			} else if (parent.dragOrientation == TreeList.DRAG_RIGHT && distance > 50 && angle < Math.PI * 3/4 && angle > Math.PI * 1/4) {
				if (parent.elementDragOutListener != null) {
					Log.d("draglayer", "DRAG OUT");
					enableScroll(); //requestDisallowInterceptTouchEvent disables the onInterceptTouchEvent abilities of TreeListDragLayer
					parent.elementDragOutListener.onElementDragOut(TreeElement.this, dataTree);
//					return false;
				}
			} else if (parent.dragOrientation == TreeList.DRAG_LEFT && distance > 50 && angle > Math.PI * -3/4 && angle < Math.PI * -1/4) {
				if (parent.elementDragOutListener != null) {
					Log.d("draglayer", "DRAG OUT");
					enableScroll(); //requestDisallowInterceptTouchEvent disables the onInterceptTouchEvent abilities of TreeListDragLayer
					parent.elementDragOutListener.onElementDragOut(TreeElement.this, dataTree);
//					return false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			velocityTracker.computeCurrentVelocity(1000);
			Log.d("draglayer", (Math.PI * -1/4) + "velocity: " + velocityTracker.getXVelocity());
			
			if (parent.dragOrientation == TreeList.DRAG_RIGHT) {
				if (velocityTracker.getXVelocity() < -500 && angle < Math.PI * -1/4 && angle > Math.PI * -3/4) {
					Log.d("draglayer", "SWIPE LEFT");
					if (parent.swipeInListener != null) {
						parent.swipeInListener.onSwipeIn();
					}
				}				
			} else {
				if (velocityTracker.getXVelocity() > 500 && angle > Math.PI * 1/4 && angle < Math.PI * 3/4) {
					Log.d("draglayer", "SWIPE RIGHT");
					if (parent.swipeInListener != null) {
						parent.swipeInListener.onSwipeIn();
					}
				}
			}
			
			break;
		}

		return true;
	}
	
	private void disableScroll() {
		if (parent.getParent() != null) {
			parent.scrollView.requestDisallowInterceptTouchEvent(true);
		}
	}
	
	private void enableScroll() {
		if (parent.getParent() != null) {
			parent.scrollView.requestDisallowInterceptTouchEvent(false);
		}
	}
}

// class TreeElementText extends View {
//
// public TreeElementText(Context context) {
// super(context);
// }
//
// @Override
// protected void onDraw(Canvas canvas) {
// super.onDraw(canvas);
//
// }
// }

// class TreeElementBackground extends View {
// Paint paint = new Paint();
//
// public TreeElementBackground(Context context) {
// super(context);
// paint.setStyle(Paint.Style.STROKE);
// paint.setColor(Color.GREEN);
// }
//
// @Override
// protected void onDraw(Canvas canvas) {
// super.onDraw(canvas);
//
// canvas.drawRect(new Rect(1, 1, getWidth()-1, getHeight()-1), paint);
// }
//
// // @Override
// // protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
// // setMeasuredDimension(10, 10);
// // // TODO Auto-generated method stub
// //// super.onMeasure(widthMeasureeSpec, heightMeasureSpec);
// // }
// }

