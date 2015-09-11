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
package de.uniluebeck.iti.hanse.hansecontrol.viewgroups;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import de.uniluebeck.iti.hanse.hansecontrol.R;
import de.uniluebeck.iti.hanse.hansecontrol.MainScreen.TopicTree;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.RegistrationException;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosDataProvider.RosDataConnection;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosService;
import de.uniluebeck.iti.hanse.hansecontrol.views.MapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.RosMapWidget;
import de.uniluebeck.iti.hanse.hansecontrol.views.roswidgets.RosPlotWidget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import static de.uniluebeck.iti.hanse.hansecontrol.gui.GuiTools.*;

/**
 * This is the root ViewGroup for all currently active full-size widgets.
 * 
 * @author Stefan Hueske
 */
public class WidgetLayer extends RelativeLayout {
	
	DragLayer dragLayer;
	
	RosService rosService;
	
	static int id = 0;
	
//	List<MapWidget> widgets = new LinkedList<MapWidget>();
	
//	public WidgetLayer(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init();
//	}
//
//	public WidgetLayer(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init();
//	}

	public WidgetLayer(Context context, DragLayer dragLayer) {
		super(context);
		this.dragLayer = dragLayer;
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
//		int insertIndex = Math.min(widgetlist.getChildCount(),widget.getId());
//		insertIndex = Math.max(0, insertIndex);
		widgetlist.addView(widget, 0);
//		widget.setMode(MapWidget.ICON_MODE);
	}
	
	private void initWidgetPos(RosMapWidget widget, float x, float y) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) widget.getLayoutParams();
		params.width = dipToPixels(300, getContext());
		params.height = dipToPixels(200, getContext());
		params.leftMargin = Math.max(0, (int)x - params.width/2);
		params.leftMargin = Math.min(getWidth() - params.width, params.leftMargin);
		params.topMargin = Math.max(0, (int)y - params.height/2);
		params.topMargin = Math.min(getHeight() - params.height, params.topMargin);
		
		Log.d("widgetlayer", "l: " + params.leftMargin + " t:" + params.topMargin  + "getWidth(): " + getWidth());

		widget.setLayoutParams(params);
	}
	
	public void createWidget(float x, float y, Class<? extends RosMapWidget> widgetClass, TopicTree topicTree) {
		Log.d("widgetlayer", "creating widget: " + widgetClass.getSimpleName());
		
		try {
//			int widgetID,	Context context, DragLayer dragLayer, TopicTree topicTree
//			RosMapWidget widget = new RosPlotWidget<T>() {}
			
//			RosMapWidget widget = new RosPlotWidget()
			
			RosMapWidget widget = (RosMapWidget) widgetClass.getConstructors()[0].newInstance(id++,getContext(),dragLayer, topicTree);
			widget.setWidgetLayer(this);
			addView(widget);
			initWidgetPos(widget, x, y);
			invalidate();
			if (rosService != null) {
				connectWidget(widget);
			}
		} catch (Exception e) {
			Log.e("widgetlayer", "Error instantiating Widget", e);
		}
		
	}
	
	private void connectWidget(RosMapWidget widget) {
		if (widget.getDataConnection() == null) {
			try {
				RosDataConnection con = rosService.getRosDataProvider().register(widget.getId(), 
						widget.getTopicTree().getTopic());
				widget.setDataConnection(con);
			} catch (RegistrationException e) {
				Log.e("widgetlayer", "Registration failed for Widget: " 
						+ widget.getClass().getSimpleName() + "@ID=" + widget.getId(), e);
			}			
		}
	}
	
	public void setRosService(RosService rosService) {
		this.rosService = rosService;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child instanceof RosMapWidget) {
				RosMapWidget widget = (RosMapWidget) child;
				connectWidget(widget);
			}
		}
	}
}
