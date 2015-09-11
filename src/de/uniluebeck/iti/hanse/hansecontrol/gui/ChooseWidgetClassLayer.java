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

import de.uniluebeck.iti.hanse.hansecontrol.MainScreen.TopicTree;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeList.Tree;
import de.uniluebeck.iti.hanse.hansecontrol.gui.TreeListDragLayer.TreeListDragLayerListener;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

public class ChooseWidgetClassLayer extends RelativeLayout implements TreeListDragLayerListener {

	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;
	private int mode = INACTIVE;
	
	private static final int ALPHA = 180;
	Paint backgroundPaint;
	ValueAnimator backgroundAlphaAnim;
	
//	ScrollView scrollView;
	Listener listener;
	
	public ChooseWidgetClassLayer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ChooseWidgetClassLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ChooseWidgetClassLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		setWillNotDraw(false);
		backgroundPaint = new Paint();
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setAlpha(0);
		backgroundPaint.setStyle(Style.FILL);
//		scrollView = new ScrollView(getContext());
		RelativeLayout.LayoutParams params = new RelativeLayout
				.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		int m = getHeight() / 6;
		params.setMargins(m, m, m, m);
//		addView(scrollView, params);
 	}

	@Override
	public void onElementDrop(final float x, final float y, Tree tree) {
		backgroundAlphaAnim = ValueAnimator.ofInt(0,ALPHA);
		backgroundAlphaAnim.setDuration(300);
		backgroundAlphaAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				backgroundPaint.setAlpha((Integer) animation.getAnimatedValue());
				ChooseWidgetClassLayer.this.invalidate();
			}
		});
		mode = ACTIVE;
		backgroundAlphaAnim.start();
		
		//TODO replace buttons with icons + text (ImageButton?)	
		LinearLayout buttonsLayout = new LinearLayout(getContext());
		buttonsLayout.setOrientation(LinearLayout.VERTICAL);
		
		
		RelativeLayout.LayoutParams params = new RelativeLayout
				.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int m = getHeight() / 6;
		params.setMargins(m, m, m, m);
		addView(buttonsLayout, params);
		
		
//		scrollView.addView(buttonsLayout);
//		scrollView.invalidate();
		if (tree.getValue() instanceof TopicTree) {
			final TopicTree topicTree = (TopicTree) tree.getValue();
			Log.d("choosewidgettest", "getting handlers: " + topicTree.getTopic().getType());
			for (final Class<? extends RosMapWidget> widgetClass : RosMapWidget.getRegistry()
					.getRosMapWidgetClass(topicTree.getTopic().getType())) {
				Log.d("choosewidgettest", "adding option: " + widgetClass.getSimpleName());
				Button button = new Button(getContext());
				button.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (listener != null) {
							setMode(INACTIVE);
							listener.onWidgetClassChosen(x, y, widgetClass, topicTree);
						}
					}
				});
				button.setText(widgetClass.getSimpleName());
				buttonsLayout.addView(button);
			}
			buttonsLayout.invalidate();
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (mode == ACTIVE) {
			canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), backgroundPaint);			
		}
	}
	
	private void setMode(int m) {
		switch (m) {
		case INACTIVE:
			mode = INACTIVE;
			removeAllViews();
			if (backgroundAlphaAnim != null) {
				backgroundAlphaAnim.cancel();			
			}
			invalidate();
			break;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mode == INACTIVE) {
			return false;
		}
		setMode(INACTIVE);
		return true;
	}
	
	public void setListener(Listener listener) {
		this.listener = listener;
	}
	
	public static interface Listener {
		public void onWidgetClassChosen(float x, float y, Class<? extends RosMapWidget> widgetClass, TopicTree topicTree);
	}
}



