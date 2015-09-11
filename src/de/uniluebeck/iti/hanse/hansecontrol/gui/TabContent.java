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

import de.uniluebeck.iti.hanse.hansecontrol.MapManager;
import de.uniluebeck.iti.hanse.hansecontrol.MapManager.Map;
import de.uniluebeck.iti.hanse.hansecontrol.gui.Tabbar.Tab;
import de.uniluebeck.iti.hanse.hansecontrol.rosbackend.RosService;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.DragLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.MapLayer;
import de.uniluebeck.iti.hanse.hansecontrol.viewgroups.WidgetLayer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class TabContent extends RelativeLayout {

	private int tabID = -1;
		
	public MapLayer mapLayer;
	public WidgetLayer widgetLayer;
	
	RosService rosService;
	
	public TabContent(Context context, int tabID) {
		super(context);
		this.tabID = tabID;
		init();
	}

//	public TabContainer(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		init();
//	}
//
//	public TabContainer(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		init();
//	}

	private void init() {
		initLayers();
	}
	
	private void initRosServiceSetters() {
		widgetLayer.setRosService(rosService);
	}
	
	static int test = 1;
	private synchronized void initLayers() {
		
		DragLayer dragLayer = new DragLayer(getContext());
		
		addView(dragLayer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		mapLayer = new MapLayer(getContext());
		dragLayer.addView(mapLayer);
		if (MapManager.getInstance().getMaps().size() >= 2) {
			Map map = MapManager.getInstance().getMaps().get(1);			
			mapLayer.setMap(map);
		}
//		addView(new View(getContext()) {
//			@Override
//			protected void onDraw(Canvas canvas) {
//				canvas.drawText("Tab " + tabID, 100, 100, new Paint());
//			}
//		});
				
		widgetLayer = new WidgetLayer(getContext(), dragLayer);
		dragLayer.setWidgetLayer(widgetLayer);
		dragLayer.addView(widgetLayer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
				
		dragLayer.invalidate();
		invalidate();
		
		if (rosService != null) {
			initRosServiceSetters();
		}
	}
	
	public synchronized void setRosService(RosService rosService) {
		if (this.rosService == null) {
			this.rosService = rosService;
			initRosServiceSetters();
		}
	}
	
}
