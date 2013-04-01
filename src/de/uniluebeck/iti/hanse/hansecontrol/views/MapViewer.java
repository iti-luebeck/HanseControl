package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MapViewer extends BasicView {
	
	public MapViewer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapViewer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		getDebugPaint().setColor(Color.BLUE);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
}
