package de.uniluebeck.iti.hanse.hansecontrol.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MapWidget extends BasicView {
	
	public MapWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MapWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MapWidget(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		getDebugPaint().setColor(Color.GREEN);
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
