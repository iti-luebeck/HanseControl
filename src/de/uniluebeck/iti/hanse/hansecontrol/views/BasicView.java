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
package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * All custom views are based on this class.
 * It offers basic debug functionality.
 * 
 * @author Stefan Hueske
 */
public class BasicView extends RelativeLayout {
	public boolean DEBUG_MODE = true;
	
	Paint paint = new Paint();
	List<PointF> points = new LinkedList<PointF>();
	
	
	public BasicView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BasicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BasicView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		paint.setColor(Color.RED);
		paint.setStrokeWidth(5);
		paint.setStyle(Paint.Style.STROKE);
		if (DEBUG_MODE && !(this instanceof RosMapWidget)) {
			View view = new View(getContext()) {
				@Override
				protected void onDraw(Canvas canvas) {
					super.onDraw(canvas);
					paint.setAlpha(255);
					paint.setStrokeWidth(5);
					paint.setStyle(Paint.Style.STROKE);
//					for (int i = 1; i < points.size(); i++) {
//						PointF a = points.get(i-1);
//						PointF b = points.get(i);
//						canvas.drawLine(a.x, a.y, b.x, b.y, paint);
//					}
					canvas.drawLine(0, 0, getWidth(), getHeight(), paint);
					canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
					
					paint.setAlpha(80);
					paint.setStyle(Paint.Style.FILL);
					canvas.drawRect(new Rect(0,0, getWidth(), getHeight()), paint);
					
				}
			};
			addView(view);
		}
	}
	
	public Paint getDebugPaint() {
		return paint;
	}
		
//	@Override
//	protected void onDraw(Canvas canvas) {
//		
//	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (DEBUG_MODE && event.getActionMasked() == MotionEvent.ACTION_MOVE) {
			points.add(new PointF(event.getX(), event.getY()));
			invalidate();
			return true;
		}
		return true;
	}
}
