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
