package de.uniluebeck.iti.hanse.hansecontrol.views;

import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class WidgetLayer extends RelativeLayout {
	
//	List<MapWidget> widgets = new LinkedList<MapWidget>();
	
	public WidgetLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public WidgetLayer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public WidgetLayer(Context context) {
		super(context);
		init();
	}
	
	private void init() {
//		MapWidget dummy = new MapWidget(getContext());
//		dummy.getDebugPaint().setColor(Color.BLUE);
//		
//		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(200, 200);
//		params.leftMargin = 90;
//		params.topMargin = 200;
//		//dummy.setVisibility(View.INVISIBLE);
//		dummy.setLayoutParams(params);
//		addView(dummy);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	public void removeWidget(MapWidget widget) {
		removeView(widget);
		LinearLayout widgetlist = (LinearLayout)((DragLayer) getParent()).findViewById(R.id.widgetLayout);
		widgetlist.addView(widget, widget.getId());
		widget.setMode(MapWidget.ICON_MODE);
	}
	
}
