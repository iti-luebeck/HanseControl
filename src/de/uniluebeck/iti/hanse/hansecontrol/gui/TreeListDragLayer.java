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



import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.Tree;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class TreeListDragLayer extends RelativeLayout implements TreeList.ElementDragOutListener {

	Bitmap dragImage;
	MotionEvent dragMove;
	View drawView;
	View currentViewElement;
	Tree currentTree;
	Paint paint = new Paint();
	
	ValueAnimator animDropDown;
	ValueAnimator animElemOpacity;
	
	TreeListDragLayerListener listener;
	
	final static float ICON_SCALE = 0.6f;
	
	public TreeListDragLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TreeListDragLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TreeListDragLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
//		setWillNotDraw(false);
		paint.setAntiAlias(true);
		drawView = new View(getContext()) {
			protected void onDraw(Canvas canvas) {
				if (dragImage != null && dragMove != null) {
//					Log.d("draglayer", "draw: x: " + (dragMove.getX() - dragImage.getWidth() / 2) + " y: " + 
//							(dragMove.getY() - dragImage.getHeight() / 2) );
					
					float x = dragMove.getX() - dragImage.getWidth() / 2;
					float y = dragMove.getY() - dragImage.getHeight() / 2;
					
					float scale = animDropDown == null ? ICON_SCALE : (Float)animDropDown.getAnimatedValue();
					
					canvas.save();
					canvas.scale(scale, scale, dragMove.getX(), dragMove.getY());
					
					canvas.drawBitmap(dragImage, x, y, paint);
					
					canvas.restore();
					
				}
			};
		};
		drawView.setId(11); //TODO fixme
		addView(drawView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
	}

	@Override
	public void onElementDragOut(final View view, Tree tree) {
		currentTree = tree;
		currentViewElement = view;
		animElemOpacity = ValueAnimator.ofFloat(1, 0.3f);
		animElemOpacity.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				view.setAlpha((Float)animation.getAnimatedValue());
			}
		});
		animElemOpacity.start();		
		
		dragImage = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
		int[] l = new int[2];
		int[] b = new int[2];
		
		view.getLocationOnScreen(l);
		this.getLocationOnScreen(b);
		l[0] = b[0] - l[0];
		l[1] = b[1] - l[1];
		
		dragMove = MotionEvent.obtain(0, 0, 0, l[0] - dragImage.getWidth() / 2, l[1] - dragImage.getHeight() / 2, 0, 0, 0, 0, 0, 0, 0);
		Canvas c = new Canvas(dragImage);
		view.draw(c);
		//bringToFront();
		drawView.bringToFront();
		requestLayout();
		invalidate();
		drawView.invalidate();
	}
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		if (dragImage != null && dragMove != null) {
////			Log.d("draglayer", "draw: x: " + (dragMove.getX() - dragImage.getWidth() / 2) + " y: " + 
////					(dragMove.getY() - dragImage.getHeight() / 2) );
//			canvas.drawBitmap(dragImage, 
//					dragMove.getX() - dragImage.getWidth() / 2, 
//					dragMove.getY() - dragImage.getHeight() / 2,
//					new Paint());
//		}
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d("draglayer", "touch: " + ev.getAction());
		switch (ev.getAction()) {
		case MotionEvent.ACTION_MOVE:
			dragMove = MotionEvent.obtain(ev);
			drawView.invalidate();
			break;
		case MotionEvent.ACTION_UP:
			animDropDown = ValueAnimator.ofFloat(ICON_SCALE, 0);
			animDropDown.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					drawView.invalidate();
				}
			});
			animDropDown.addListener(new Animator.AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					dragImage = null;
					dragMove = null;
					drawView.invalidate();
					animDropDown = null;
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					dragImage = null;
					dragMove = null;
					drawView.invalidate();
					animDropDown = null;
				}
			});
			animDropDown.start();
			
			animElemOpacity = ValueAnimator.ofFloat(0.3f, 1);
			animElemOpacity.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					currentViewElement.setAlpha((Float)animation.getAnimatedValue());
				}
			});
			animElemOpacity.start();
			
			if (listener != null) {
				listener.onElementDrop(ev.getX(), ev.getY(), currentTree);
			}
			
			break;
		}
		return dragImage != null;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.d("draglayer", "INTERCEPT: " + (dragImage != null));
		return dragImage != null;
	}
	
	public void setListener(TreeListDragLayerListener listener) {
		this.listener = listener;
	}
	
	public static interface TreeListDragLayerListener {
		public void onElementDrop(float x, float y, Tree tree);
	}
	
}
