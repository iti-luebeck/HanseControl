/*
* Copyright (c) 2015, Institute of Computer Engineering, University of Lübeck
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
* * Redistributions of source code must retain the above copyright notice, this
*   list of conditions and the following disclaimer.
* 
* * Redistributions in binary form must reproduce the above copyright notice,
*   this list of conditions and the following disclaimer in the documentation
*   and/or other materials provided with the distribution.
* 
* * Neither the name of the copyright holder nor the names of its
*   contributors may be used to endorse or promote products derived from
*   this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
* SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
* CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
* OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package de.uniluebeck.iti.hanse.hansecontrol.gui;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

public class Sidebar extends RelativeLayout {

	SidebarInner sidebarInner;
	
	MotionEvent grabberDown = null;
	float grabberDownSidebarX = 0;
	VelocityTracker velocityTracker = VelocityTracker.obtain();
	ValueAnimator animator;
	
	public static final int FOLDED = 0;
	public static final int UNFOLDED = 1;
	private int state = UNFOLDED;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	private int orientation = LEFT; //default orientation is left
	
	MotionEvent interceptActionDown;
	
	public Sidebar(Context context, int orientation) {
		super(context);
		this.orientation = orientation;
		init(context);
	}
	
	public Sidebar(Context context) {
		super(context);
		init(context);
	}

	public Sidebar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initOrientation(context, attrs);
		init(context);
	}

	public Sidebar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initOrientation(context, attrs);
		init(context);
	}
	
	public void setState(final int state) {
		this.state = state;
		if (sidebarInner.getWidth() == 0 && state == FOLDED) {
			//workaround, layout did not happen yet
			addLayoutListener(sidebarInner, new Runnable() {
				@Override
				public void run() {
					sidebarInner.post(new Runnable() {
						
						@Override
						public void run() {
							setState(state);
						}
					});
				}
			});
			return;
		}
		animateToXpos(state == FOLDED ? getXfolded() : getXunfolded());		
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	private float getXfolded() {
		if (orientation == LEFT) {
			return -sidebarInner.getWidth() + sidebarInner.sidebarGrabber.getWidth() * 0.8f;			
		} else {
			return sidebarInner.getWidth() - sidebarInner.sidebarGrabber.getWidth() * 0.8f;
		}
	}
	
	private float getXunfolded() {
		return 0;
	}
	
	private void initOrientation(Context context, AttributeSet attrs) {
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Sidebar, 0, 0);
		try {
			orientation = a.getInteger(R.styleable.Sidebar_orientation, orientation);
		} finally {
			a.recycle();
		}
	}
	
	private void init(Context context) {
		sidebarInner = new SidebarInner(context, this);
		addView(sidebarInner);
		
//		LinearLayout lin = new LinearLayout(context);
//		lin.setOrientation(LinearLayout.VERTICAL);
//		Button b = new Button(context);
//		View testview = new View(context);
//		
//		testview.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				
//				return true;
//			}
//		});
//		
//		testview.setBackgroundColor(Color.YELLOW);
//		lin.addView(b);
//		lin.addView(testview);
//		setContent(lin);
//		
//		b.setText("Hide");
//		b.setOnClickListener(new View.OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				setState(FOLDED);
//			}
//		});
//		Log.d("sidebar", "orientation: " + orientation);
		
		//test
		
		
		initGrabberTouchListener();
	}
	
	private void initGrabberTouchListener() {
		final SidebarGrabber grabber = sidebarInner.sidebarGrabber;
		grabber.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (animator != null) {
						animator.cancel();
					}
					velocityTracker.clear();
					grabber.setState(SidebarGrabber.ACTIVE);
					grabberDown = event;
					grabberDownSidebarX = event.getX() + grabber.getX();
//					Log.d("sidebar", "grabberDownSidebarX = " + grabberDownSidebarX);
					break;
				case MotionEvent.ACTION_MOVE:
					
					break;
				case MotionEvent.ACTION_UP:
					grabber.setState(SidebarGrabber.INACTIVE);
					grabberDown = null;
					break;
				}
				return true;
			}
		});
	}

//	@Override
//	public boolean dispatchTouchEvent(MotionEvent ev) {
//		
//		return super.dispatchTouchEvent(ev);
//	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (grabberDown != null) {
			return onTouchEvent(ev); //intercept gesture
		}
		Log.d("sidebar", "intercept event: " + ev.getAction());
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			interceptActionDown = ev;
			velocityTracker.clear();
			break;
		case MotionEvent.ACTION_MOVE:
			velocityTracker.addMovement(ev);
			break;
		case MotionEvent.ACTION_UP:
			//check if release happened inside Sidebar
//			Log.d("sidebar", "up: " + ev.getX() + " " + getX()+ " " + getWidth());
			if (ev.getX() < 0 || ev.getX() > getWidth()) {
				return false;
			}
			velocityTracker.computeCurrentVelocity(1000);
			float th = 200;
			Log.d("sidebar", "Velocity: " + velocityTracker.getXVelocity());
			if (orientation == LEFT) {
				//detect fling to the left, velocity is negative
				if (velocityTracker.getXVelocity() < -th) {
					setState(FOLDED);
					return true;
				}
			} else {
				//detect fling to the right, velocity is positive
				if (velocityTracker.getXVelocity() > th) {
					setState(FOLDED);
					return true;
				}
			}
		}		
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (grabberDown == null) {
			return false; 
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			velocityTracker.addMovement(event);
			velocityTracker.computeCurrentVelocity(1000);
//			Log.d("sidebar", "Velocity: " + velocityTracker.getXVelocity());
			float newX = event.getX() - grabberDownSidebarX;
			if (orientation == LEFT) {
				newX = Math.min(getXunfolded(), newX);
				newX = Math.max(getXfolded(), newX);				
			} else {
				newX = Math.max(getXunfolded(), newX);
				newX = Math.min(getXfolded(), newX);	
			}
			sidebarInner.setX(newX);
			break;
		case MotionEvent.ACTION_UP:
			sidebarInner.sidebarGrabber.setState(SidebarGrabber.INACTIVE);
			velocityTracker.computeCurrentVelocity(1000);
//			Log.d("sidebar", "grabber release velocity: " + velocityTracker.getXVelocity());
			grabberDown = null;
			grabberReleased();
			break;
		}
		return true;
	}
	
	private void animateToXpos(final float targetX) {
//		Log.d("sidebar", "Animating to: " + targetX);
		
		//sidebarInner.setX(targetX);
		
		animator = ValueAnimator.ofFloat(sidebarInner.getX(), targetX);
		animator.setDuration(300);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				sidebarInner.setX((Float)animation.getAnimatedValue());
//				Log.d("sidebar", "animate..." + animation.getAnimatedValue() + " target: " + targetX);
			}
		});
		animator.start();
	}
	
	private void grabberReleased() {
		float th = 200; //threshold to overrule the half-in-half-out rule
		float targetX = 0;
		float grabberMid = sidebarInner.getX() + sidebarInner.sidebarGrabber.getX() + sidebarInner.sidebarGrabber.getWidth() / 2;
		if (velocityTracker.getXVelocity() < -th) {
			//left fling
			targetX = orientation == LEFT ? getXfolded() : getXunfolded();
		} else if (velocityTracker.getXVelocity() > th) {
			//right fling			
			targetX = orientation == LEFT ? getXunfolded() : getXfolded();
		} else if (Math.abs(sidebarInner.getX() - getXfolded()) < Math.abs(sidebarInner.getX() - getXunfolded())) {
			//folded in
			targetX = getXfolded();
		} else {
			//folded out
			targetX = getXunfolded();
		}
		animateToXpos(targetX);
	}
	
	public void setContent(View content) {
		sidebarInner.setContent(content);
	}
	
}

class SidebarInner extends RelativeLayout {
	SidebarGrabber sidebarGrabber;
	View sidebarContent;	
	Sidebar parent;
	
	public SidebarInner(Context context, Sidebar sidebar) {
		super(context);
		parent = sidebar;
		init(context);
	}

//	public SidebarInner(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init(context);
//	}
//
//	public SidebarInner(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init(context);
//	}
	
	private void init(Context context) {
//		SidebarBackground sidebarBackground = new SidebarBackground(context);
//		addView(sidebarBackground);
		//TODO remove Background View?
		
		sidebarGrabber = new SidebarGrabber(context);
		addView(sidebarGrabber);
		RelativeLayout.LayoutParams params = (LayoutParams) sidebarGrabber.getLayoutParams();

		if (parent.getOrientation() == Sidebar.LEFT) {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		} else {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		}			
		params.width = dipToPixels(40, context);
		sidebarGrabber.setLayoutParams(params);			
		sidebarGrabber.setId(10); //TODO generate IDs
	}
	
	public void setContent(View content) {
		if (sidebarContent != null) {
			removeView(sidebarContent);
		}
		sidebarContent = content;
		addView(sidebarContent);
		RelativeLayout.LayoutParams params = (LayoutParams) sidebarContent.getLayoutParams();
		
		if (parent.getOrientation() == Sidebar.LEFT) {
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			params.addRule(RelativeLayout.LEFT_OF, sidebarGrabber.getId());
		} else {
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.RIGHT_OF, sidebarGrabber.getId());
		}
		
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//		int margins = dipToPixels(5, getContext());
//		params.setMargins(margins, margins, margins, margins);
		sidebarContent.setLayoutParams(params);
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText(this.getClass().getName(), 10, 30, new Paint());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}
}

//TODO remove this class ?
class SidebarDummyContent extends View {
	public SidebarDummyContent(Context context) {
		super(context);
		init(context);
	}

	public SidebarDummyContent(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public SidebarDummyContent(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		setBackgroundColor(Color.parseColor("#b98affc9"));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawText(this.getClass().getName(), 10, 30, new Paint());
		canvas.drawLine(0, 0, getWidth(), getHeight(), new Paint());
	}
}

class SidebarGrabber extends View {
	
	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;	
	private int state = INACTIVE;
	
	private float actionDownX = 0;

	public SidebarGrabber(Context context) {
		super(context);
		init();
	}

	public SidebarGrabber(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public SidebarGrabber(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		setState(state);
	}
	
	public void setState(int state) {
		this.state = state;
		switch (state) {
		case ACTIVE:
			setBackgroundColor(Color.parseColor("#a00d3eff"));
			break;
		case INACTIVE:
			setBackgroundColor(Color.parseColor("#330d3eff"));
			break;
		default:
			break;
		}
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		canvas.drawText(this.getClass().getName(), 10, 30, new Paint());
	}
}

class SidebarBackground extends View {
	public SidebarBackground(Context context) {
		super(context);
		init();
	}

	public SidebarBackground(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SidebarBackground(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		setBackgroundColor(Color.parseColor("#bba5ff3c"));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas); //TODO remove debugcode
		Paint p = new Paint();
		canvas.drawLine(0, 0, getWidth(), getHeight(), p);
//		canvas.drawText(this.getClass().getName(), 10, 30, new Paint());
	}
}

